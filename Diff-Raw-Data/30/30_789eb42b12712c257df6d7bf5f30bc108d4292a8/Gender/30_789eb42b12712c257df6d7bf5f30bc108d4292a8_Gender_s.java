 /**
  *
  */
 package q.domain;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 12, 2011
  *
  *       People gender pojo.
  */
 public enum Gender {
 	MALE(1, "男", "他"), FEMALE(2, "女", "她");
 
 	private int value;
 
 	private String cnname;
 
 	private String cncall;
 
 	private Gender(int value, String cnname, String cncall) {
 		this.value = value;
 		this.cnname = cnname;
 		this.cncall = cncall;
 	}
 
 	public int getValue() {
 		return value;
 	}
 
 	public String getCnname() {
 		return cnname;
 	}
 
 	public String getCncall() {
 		return cncall;
 	}
 
 	public static Gender convertValue(int value) {
 		if(value == MALE.value) {
 			return MALE;
 		} else if(value == FEMALE.value) {
 			return FEMALE;
 		} else {
 			return null;
 		}
 	}
 
 	public boolean isFemale() {
 		return value == FEMALE.value;
 	}
 
 	/**
 	 * @param int1
 	 * @return
 	 */
 	public static boolean valid(int value) {
 		return null == convertValue(value) ? false : true;
 	}
 
 
 
 }
