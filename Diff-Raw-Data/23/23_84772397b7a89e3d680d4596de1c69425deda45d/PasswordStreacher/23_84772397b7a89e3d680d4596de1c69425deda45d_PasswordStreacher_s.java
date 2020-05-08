 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.model;
 
 /**
  * @author ne_Sachirou
  */
 public class PasswordStreacher implements java.io.Serializable {
	private static final long serialVersionUID = 4507387870799164808L;
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String genSalt() {
 		// TODO Auto-generated method stub
 		return "";
 	}
 
 	/**
 	 * 
 	 * @param rowPassword
 	 * @param salt
 	 * @param iteration
 	 * @return
 	 */
 	public String streach(String rawPassword, String salt, int iteration) {
 		// TODO Auto-generated method stub
 		return "";
 	}
 
 	/**
 	 * 
 	 * @param rawPassword
 	 * @param salt
 	 * @return
 	 */
 	public String streach(String rawPassword, String salt) {
 		return streach(rawPassword, salt, 10000);
 	}
 }
