 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.validator;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import lombok.Data;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.exception.ValidateException;
 import tk.c4se.halt.ih31.nimunimu.model.Member;
 
 /**
  * @author ne_Sachirou
  */
 @Data
 public class LoginValidator implements java.io.Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private String id;
 	private String password;
 
 	public LoginValidator() {
 	}
 
 	/**
 	 * 
 	 * @param req
 	 */
 	public LoginValidator(HttpServletRequest req) {
 		setId(req.getParameter("id").trim());
 		setPassword(req.getParameter("password").trim());
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Map<String, Exception> validate() {
 		Map<String, Exception> errors = new HashMap<>();
 		if (getId() == null || getId().isEmpty()) {
 			errors.put("Validate", new ValidateException("IDを入力してください。"));
 		}
 		if (getPassword() == null || getPassword().isEmpty()) {
 			errors.put("Validate", new ValidateException("パスワードを入力してください。"));
 		}
 		Boolean isCorrectPassword = false;
 		try {
 			isCorrectPassword = Member
 					.isCorrectPassword(getId(), getPassword());
 		} catch (DBAccessException e) {
 			errors.put("DBAccess", e);
 		}
 		if (!isCorrectPassword) {
 			errors.put("Login", new ValidateException("IDかパスワードが異なります。"));
 		}
 		return errors;
 	}
 }
