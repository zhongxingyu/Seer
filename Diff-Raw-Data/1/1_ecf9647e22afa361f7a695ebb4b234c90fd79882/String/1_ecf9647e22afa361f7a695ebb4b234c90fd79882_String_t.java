 package java.lang;
 
 public final class String {
 	public native String concat(String str);
 	public native int length();
 	public native boolean contains(String str);
 	public native boolean endsWith(String str);
	public native boolean equals(String str);
 	public native int indexOf(char ch);
 	public native int indexOf(String str);
 	public native int lastIndexOf(char ch);
 	public native int lastIndexOf(String str);
 	public native String replace(char ch, char newChar);
 	public native boolean startsWith(String str);
 	public native String substring(int start);
 	public native String substring(int start, int end);
 	public native String toLowerCase();
 	public native String toUpperCase();
 	public native String toString();
 }
