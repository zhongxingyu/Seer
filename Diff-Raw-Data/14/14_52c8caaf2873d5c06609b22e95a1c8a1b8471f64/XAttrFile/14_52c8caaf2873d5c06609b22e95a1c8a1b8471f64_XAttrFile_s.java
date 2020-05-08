 package de.jotschi.jxattr;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 public class XAttrFile extends File {
 
 	private static final long serialVersionUID = -4293973542600858334L;
 
 	public XAttrFile(String filePath) {
 		super(filePath);
 	}
 
 	public void setAttribute(String key, String value) throws IOException {
 		setAttribute(key, value.getBytes());
 	}
 
 	/**
 	 * Sets the given byte value for the defined key
 	 * 
 	 * @param key
 	 * @param value
 	 * @throws IOException
 	 */
 	public void setAttribute(String key, byte[] value) throws IOException {
 		boolean wasSuccessful = XAttr.setXAttr(this, key, value);
 		if (!wasSuccessful) {
 			throw new IOException("Could not set property {" + key + "}.");
 		}
 	}
 
 	/**
 	 * Returns the attribute for the given key
 	 * 
 	 * @param key
 	 * @return
 	 * @throws IOException
 	 */
 	public String getAttributeAsString(String key) throws IOException {
 		return new String(getAttribute(key));
 	}
 
 	public byte[] getAttribute(String key) throws IOException {
 		return XAttr.getXAttrAsByte(this, key);
 
 	}
 
 	public Vector<String> getAttributeList() {
 		return XAttr.listXAttr(this);
 	}
 }
