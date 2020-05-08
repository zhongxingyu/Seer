 package ch.hslu.appe.fs1301.business.shared;
 
 public class UserRole {
	public static final int NONE = 0;
	public static final int CUSTOMER = 1;
	public static final int SYSUSER = 1 << 1;
	public static final int ADMIN = 1 << 2;
 }
