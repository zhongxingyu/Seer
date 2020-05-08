 package com.astrider.sfc.src.model;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import com.astrider.sfc.app.lib.BaseModel;
 import com.astrider.sfc.app.lib.Mailer;
 import com.astrider.sfc.app.lib.Mapper;
 import com.astrider.sfc.app.lib.Validator;
 import com.astrider.sfc.app.lib.FlashMessage.Type;
 import com.astrider.sfc.app.lib.util.AuthUtils;
 import com.astrider.sfc.app.lib.util.StringUtils;
 import com.astrider.sfc.src.model.dao.UserDao;
 import com.astrider.sfc.src.model.dao.UserStatsDao;
 import com.astrider.sfc.src.model.vo.db.UserStatsVo;
 import com.astrider.sfc.src.model.vo.db.UserVo;
 import com.astrider.sfc.src.model.vo.form.ConfirmEmailVo;
 import com.astrider.sfc.src.model.vo.form.RegisterFormVo;
 import static com.astrider.sfc.ApplicationContext.*;
 
 /**
  * ユーザー登録関連Model.
  * @author astrider
  *
  */
 public class RegisterModel extends BaseModel {
 	
 	/**
 	 * ユーザー新規登録.
 	 * @param request
 	 * @return
 	 */
 	public boolean registerUser(HttpServletRequest request) {
 		// フォーム情報取得
 		Mapper<RegisterFormVo> m = new Mapper<RegisterFormVo>();
 		RegisterFormVo registerForm = (RegisterFormVo) m.fromHttpRequest(request);
 		HttpSession session = request.getSession();
 		session.setAttribute(SESSION_REGISTER_FORM, registerForm);
 
 		// 汎用バリデーション
 		Validator<RegisterFormVo> validator = new Validator<RegisterFormVo>(registerForm);
 		if (!validator.valid()) {
 			flashMessage.addMessage(validator.getFlashMessage());
 			return false;
 		}
 
 		// パスワード一致
 		if (!registerForm.getPassword().equals(registerForm.getPasswordConfirm())) {
 			flashMessage.addMessage("パスワードが一致しません");
 			return false;
 		}
 
 		// DBにユーザーを追加
 		UserVo user = new UserVo(registerForm);
 		user.setAuthToken(AuthUtils.encrypt(registerForm.getPassword()));
 		user.setEmailToken(StringUtils.getEmailToken(registerForm.getEmail()));
 		UserDao userDao = new UserDao();
 		boolean succeed = userDao.insert(user, false);
 		
 		if (!succeed) {
 			flashMessage.addMessage("仮登録に失敗しました。お客様のメールアドレスは既に登録されている可能性があります。");
 			userDao.rollback();
 			userDao.close();
 			return false;
 		}
 
 		// 仮登録メール　本文作成
 		String to = user.getEmail();
		String subject = "仮登録メール";
 		String body = "";
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append(user.getUserName() + "様\n\n");
 			sb.append("この度はsanteに仮登録いただきありがとうございます。\n");
 			sb.append("以下のURLをクリックすることによって本登録が完了いたします。\n");
 			sb.append("https://" + request.getServerName() + request.getContextPath() + PAGE_REGISTER_CONFIRMEMAIL);
 			sb.append("?email=" + URLEncoder.encode(user.getEmail(), "UTF-8"));
 			sb.append("&token=" + URLEncoder.encode(user.getEmailToken(), "UTF-8"));
 			body = sb.toString();
 		} catch (UnsupportedEncodingException e) {
 			flashMessage.addMessage("仮登録完了のメール本文の作成に失敗しました");
 			userDao.rollback();
 			userDao.close();
 			return false;
 		}
 
 		// 仮登録メール送信
 		Mailer mailer = new Mailer(to, subject, body);
 		if (!mailer.send()) {
 			flashMessage.addMessage("仮登録完了のメール送信に失敗しました");
 			userDao.rollback();
 			userDao.close();
 			return false;
 		}
 		
 		userDao.commit();
 		userDao.close();
 		
 		flashMessage.addMessage("お客様のメールアドレスに仮登録完了メールが送信されました。メール記載のリンクから本登録手続きを完了してください。");
 		flashMessage.setMessageType(Type.INFO);
 		session.removeAttribute(SESSION_REGISTER_FORM);
 
 		return true;
 	}
 
 	/**
 	 * メールアドレス確認.
 	 * @param request
 	 * @return
 	 */
 	public boolean confirmMail(HttpServletRequest request) {
 		// 引数取得
 		Mapper<ConfirmEmailVo> mapper = new Mapper<ConfirmEmailVo>();
 		ConfirmEmailVo vo = mapper.fromHttpRequest(request);
 
 		// 引数の整合性確認
 		Validator<ConfirmEmailVo> validator = new Validator<ConfirmEmailVo>(vo);
 		if (!validator.valid()) {
 			flashMessage.addMessage(validator.getFlashMessage());
 			return false;
 		}
 
 		// 認証トークン確認
 		UserDao userDao = new UserDao();
 		UserVo user = userDao.selectByEmailToken(vo);
 		if (user == null) {
 			flashMessage.addMessage("トークンが確認できませんでした。");
 			userDao.close();
 			return false;
 		}
 
 		// ユーザーを認証済みに
 		user.setAvailable(true);
 		user.setConfirmed(true);
 		if (!userDao.update(user)) {
 			flashMessage.addMessage("不明なエラー");
 			userDao.close();
 			return false;
 		}
 		userDao.close();
 
 		// userStatsテーブルにレコードを追加
 		UserStatsDao userStatsDao = new UserStatsDao();
 		UserStatsVo userStats = new UserStatsVo();
 		userStats.setUserId(user.getUserId());
 		userStatsDao.insert(userStats);
 		userStatsDao.close();
 
 		// sessionをログイン済みに
 		HttpSession session = request.getSession();
 		session.setAttribute(SESSION_USER, user);
 
 		return true;
 	}
 }
