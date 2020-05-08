 package de.jotschi.jxattr;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Vector;
 
 /**
  * Wrapper class for the XAttrNativeBindings class that contains the jni
  * bindings
  * 
  * @author jotschi
  * 
  */
 public class XAttr {
 
 	private final static String USER_PREFIX = "user.";
 
 	private static final int MAX_ATTRIBUTES_SIZE = 10000;
 
 	/**
 	 * Returns a list of attributes for the given file
 	 * 
 	 * @param file
 	 * @return
 	 */
 	public static Vector<String> listXAttr(File file) {
 
 		byte[] value = new byte[MAX_ATTRIBUTES_SIZE];
 		XAttrNativeBindings.listxattr(file.getAbsolutePath(), value,
 				value.length);
 
 		Vector<String> attributes = new Vector<String>();
 		String currentKey = "";
 		for (int i = 0; i < MAX_ATTRIBUTES_SIZE; i++) {
 			byte currentByte = value[i];
 			if (currentByte == 0 && currentKey.length() != 0) {
 				attributes.add(currentKey);
 				currentKey = "";
 				continue;
 			} else if (currentByte == 0 && currentKey.length() == 0) {
 				break;
 			} else {
 				currentKey += (char) currentByte;
 			}
 		}
 		return attributes;
 	}
 
 	/**
 	 * Returns the size of the given attribute.
 	 * 
 	 * @param file
 	 * @param key
 	 * @return
 	 * @throws IOException
 	 */
 	public static int getAttributeSize(File file, String key)
 			throws IOException {
 		int len = XAttrNativeBindings.getxattr(file.getAbsolutePath(),
 				USER_PREFIX + key, new byte[0], 0);
 		return len;
 	}
 
 	/**
 	 * Returns the value of the given attribute for the specified file in a
 	 * string format.
 	 * 
 	 * @param file
 	 * @param key
 	 * @return
 	 * @throws IOException
 	 */
 	public static String getXAttrAsString(File file, String key)
 			throws IOException {
 		return new String(getXAttrAsByte(file, key));
 	}
 
 	/**
 	 * Returns the value of the given attribute for the specified file in a byte
 	 * format.
 	 * 
 	 * @param file
 	 * @param key
 	 * @return
 	 * @throws IOException
 	 */
 	public static byte[] getXAttrAsByte(File file, String key)
 			throws IOException {
 		int expectedLenght = XAttr.getAttributeSize(file, key);
 		key = USER_PREFIX + key;
 
 		byte[] value = new byte[expectedLenght];
 		int len = XAttrNativeBindings.getxattr(file.getAbsolutePath(), key,
 				value, value.length);
 		if (len < 0) {
 			throw new IOException("The attribute {" + key
 					+ "} contains no data for file {" + file.getAbsolutePath()
 					+ "}");
 		}
 		byte[] trimmedValue = Arrays.copyOfRange(value, 0, len);
 		return trimmedValue;
 	}
 
 	/**
 	 * Sets the attribute string value for the given key
 	 * 
 	 * @param file
 	 * @param key
 	 * @param value
 	 * @return
 	 * @throws IOException
 	 */
 	public static boolean setXAttr(File file, String key, String value)
 			throws IOException {
 		int flag = XAttrNativeBindings.XATTR_REPLACE;
 		if (getAttributeSize(file, key) == -1) {
 			flag = XAttrNativeBindings.XATTR_CREATE;
 		}
 
 		int statusCode = XAttrNativeBindings.setxattr(file.getAbsolutePath(),
 				USER_PREFIX + key, value, value.length(), flag);
 		return statusCode == 0 ? true : false;
 	}
 
 	/**
 	 * Sets the attribute byte value for the given key
 	 * 
 	 * @param file
 	 * @param key
 	 * @param value
 	 * @return
 	 * @throws IOException
 	 */
 	public static boolean setXAttr(File file, String key, byte[] value)
 			throws IOException {
 		int flag = XAttrNativeBindings.XATTR_REPLACE;
 		if (getAttributeSize(file, key) == -1) {
 			flag = XAttrNativeBindings.XATTR_CREATE;
 		}
 
 		int statusCode = XAttrNativeBindings.setxattr(file.getAbsolutePath(),
 				USER_PREFIX + key, value, value.length, flag);
 		return statusCode == 0 ? true : false;
 	}
 }
