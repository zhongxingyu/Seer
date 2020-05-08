 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.model;
 
 import java.sql.Timestamp;
 
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.MemberRepository;
 import lombok.Data;
 import lombok.val;
 
 /**
  * @author ne_Sachirou
  * 
  */
 @Data
 public class Member implements java.io.Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6229745806342640308L;
 
 	/**
 	 * 
 	 * @param id
 	 * @param rowPassword
 	 * @return
 	 * @throws DBAccessException
 	 */
 	public static Boolean isCorrectPassword(String id, String rawPassword)
 			throws DBAccessException {
 		val member = new MemberRepository().find(id);
 		if (member == null)
 			return false;
 		return member.isCorrectPassword(rawPassword);
 	}
 
 	private String id;
 	private String password;
 	private String salt;
	private Boolean isPasswordReseted;
 	private MemberAuthority authority;
 	private Timestamp createdAt;
 	private Timestamp updatedAt;
 	private Timestamp deletedAt;
 
 	/**
 	 * 
 	 * @param rawPassword
 	 * @return
 	 */
 	public Boolean isCorrectPassword(String rawPassword) {
 		val password = new PasswordStreacher().streach(rawPassword, getSalt());
 		if (getPassword().equals(password))
 			return true;
 		return false;
 	}
 }
