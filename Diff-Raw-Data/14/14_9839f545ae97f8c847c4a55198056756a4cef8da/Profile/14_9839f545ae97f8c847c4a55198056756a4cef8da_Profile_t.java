 package controllers.site;
 
 import com.google.gson.JsonPrimitive;
 import controllers.CommonController;
 import controllers.SecuredController;
 import play.mvc.Before;
 import tracker.accounts.Account;
 import tracker.accounts.LoginData;
 import tracker.accounts.ProfileData;
 import tracker.accounts.SignupData;
 import tracker.cache.AccountsCache;
 import tracker.cache.TorrentStatsCache;
 import tracker.torrents.TorrentData;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Profile extends SecuredController {
 	@Before(only = { "profile", "save", "edit", "changePasskey" })
 	protected static void checkAuthentification() throws Throwable {
 		SecuredController.checkAuthentification();
 	}
 
 	public static void profile() throws Throwable { // TODO: remove redirect to /view/{id}
 		view(CommonController.currentAccount.getId());
 
 //		Account account = CommonController.account;
 //		Boolean isMe = true;
 //		render(account, isMe);
 	}
 
 	public static void view(Long accountId) throws Throwable {
 		Account account;
 		Boolean isMe = (CommonController.currentAccount != null) && (CommonController.currentAccount.getId().equals(accountId));
 		if (isMe) {
 			account = CommonController.currentAccount;
 		} else {
 			account = AccountsCache.getInstance().getById(accountId);
 		}
 
 		if (account == null) {
 			notFound("There is no user with such ID.");
 			return;
 		}
 
 		List<TorrentData> activeTorrents = account.getActiveTorrents().getTorrents();
 
 		// Precache torrent stats.
 		List<Long> torrentIds = new ArrayList<Long>();
 		for (TorrentData torrentData : activeTorrents) {
 			torrentIds.add(torrentData.getTorrentId());
 		}
 		TorrentStatsCache.getInstance().getByMultipleIds(torrentIds);
 
 		render(account, activeTorrents, isMe);
 	}
 
 	public static void edit() {
 		Account account = CommonController.currentAccount;
 		ProfileData profileData = account.getProfileData();
 
 		render(account, profileData);
 	}
 
 	public static void save(String displayName, String email, ProfileData profileData) throws Throwable {
 		SaveJsonReply reply = new SaveJsonReply();
 		reply.displayName = displayName;
 		reply.email = email;
 		reply.profileData = profileData;
 
 		if (!displayName.matches(".*")) { // TODO: implement me
 			reply.errors.add("Отображаемое имя содержит недопустимые символы.");
 		}
 
 		if (!email.matches("^[A-Za-z0-9.%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}")) {
 			reply.errors.add("E-mail введен неверно. Должен быть вида example@mail.com");
 		}
 
 		if (!profileData.getIcq().matches("[0-9\\-]{0,255}")) {
 			reply.errors.add("Номер ICQ введен неверно. Допускаются только цифры и знак тире.");
 		}
 
 		if (!profileData.getJabber().matches("^[A-Za-z0-9.%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}")) {
 			reply.errors.add("Jabber ID введен неверно. Должен быть вида example@jabber.org");
 		}
 
 		// Skype: It must be between 6-32 characters, start with a letter and contain only letters and numbers (no spaces or special characters).
 		if (!profileData.getSkype().matches("[0-9a-zA-Z\\-]{6,32}")) {
 			reply.errors.add("Skype ID введен неверно.");
 		}
 
 		if (profileData.getGender() < 0 || profileData.getGender() > 2) {
 			profileData.setGender(0);
 		}
 
 		if (reply.errors.size() == 0) {
 			Account account = CommonController.currentAccount;
 			account.setDisplayName(displayName);
 			account.setEmail(email);
 			account.setProfileData(profileData);
 			account.save();
 		}
 
 		renderJSON(reply);
 	}
 
 	public static void changePasskey() throws Throwable {
 		Account account = CommonController.currentAccount;
 		account.createPasskey();
 		account.save();
 
 		renderJSON(new JsonPrimitive(account.getPasskey()));
 	}
 
 	public static void login() {
 		String loginMessage = flash.get("loginMessage");
 
 		if (flash.contains("returnUrl")) {
 			flash.put("returnUrl", flash.get("returnUrl"));
 		}
 
 		render(loginMessage);
 	}
 
 	public static void loginProcess(LoginData loginData) throws Throwable {
 		if (!loginData.authenticate(session)) {
 			flash.put("loginMessage", "Не удалось выполнить вход. Проверьте правильность ввода логина и пароля.");
 			login();
 		} else {
 			if (flash.contains("returnUrl")) {
 				CommonController.redirect(flash.get("returnUrl"));
 			} else {
 				Torrents.page(null, null, null);
 			}
 		}
 	}
 
 	public static void logout() throws Throwable {
 		session.clear();
 		Torrents.page(null, null, null);
 	}
 
 	public static void signup() {
 		render();
 	}
 
 	public static void signupProcess(SignupData signupData) throws Throwable {
 		Account account = Account.create(signupData.login, signupData.password, signupData.email, signupData.displayName);

		LoginData loginData = new LoginData();
		loginData.login = signupData.login;
		loginData.password = signupData.password;
		loginData.authenticate(session);

		Torrents.page(null, null, null);
 		renderText("signupProcess");
 	}
 
 	public static void checkLogin(String value) throws Throwable {
 		if (value.length() < 3 || value.length() > 32) { // TODO: get from config
 			renderJSON(new CheckJsonReply(false, "Длина логина должна быть от 3 до 32 симолов."));
 		}
 
 		if (!value.matches("[0-9a-zA-Z\\-]+")) { // TODO: get from config
 			renderJSON(new CheckJsonReply(false, "Логин может содержать только латинские символы и цифры."));
 		}
 
 		Account account = Account.byLogin(value);
 
 		if (account == null) {
 			renderJSON(new CheckJsonReply(true, "Желаемый логин свободен."));
 		} else {
 			renderJSON(new CheckJsonReply(false, "Желаемый логин уже занят."));
 		}
 	}
 
 	public static void checkEmail(String value) throws Throwable {
 		if (!value.matches("^[A-Za-z0-9.%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}")) {
 			renderJSON(new CheckJsonReply(false, "E-mail введен неверно."));
 		}
 
 		Account account = Account.byEmail(value);
 
 		if (account == null) {
 			renderJSON(new CheckJsonReply(true, "Желаемый e-mail доступен."));
 		} else {
 			renderJSON(new CheckJsonReply(false, "Желаемый e-mail уже занят."));
 		}
 	}
 
 	public static void checkDisplayName(String value) throws Throwable {
 		if (value.length() < 3 || value.length() > 32) { // TODO: get from config
 			renderJSON(new CheckJsonReply(false, "Длина имени должна быть от 3 до 32 симолов."));
 		}
 
 		Account account = Account.byDisplayName(value);
 
 		if (account == null) {
 			renderJSON(new CheckJsonReply(true, "Желаемое имя свободно."));
 		} else {
 			renderJSON(new CheckJsonReply(false, "Желаемое имя уже занято."));
 		}
 	}
 
 	public static class SaveJsonReply {
 		public List<String> errors = new ArrayList<String>();
 
 		public String displayName = null;
 		public String email = null;
 		public ProfileData profileData = null;
 	}
 
 	public static class CheckJsonReply {
 		public Boolean status;
 		public String message;
 
 		public CheckJsonReply(Boolean status, String message) {
 			this.status = status;
 			this.message = message;
 		}
 	}
 }
