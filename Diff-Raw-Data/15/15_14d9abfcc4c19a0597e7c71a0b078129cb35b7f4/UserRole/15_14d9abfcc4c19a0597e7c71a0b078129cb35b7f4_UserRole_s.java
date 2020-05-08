 package ch.hslu.appe.fs1301.business.shared;
 
 public class UserRole {
	public static final int None = 0;
	public static final int Customer = 1;
	public static final int Sysuser = 1 << 1;
	public static final int Admin = 1 << 2;
 }
