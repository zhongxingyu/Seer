 package it.infn.chaos.mds.business;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.ref.common.business.BusinessObject;
 
 abstract public class BSONBusinessObject extends BusinessObject{
 	private static final long	serialVersionUID	= 470392155775116596L;
 
 	abstract public void fillFromBson(Object bson) throws Throwable;
 	
 	abstract public Object toBson() throws Throwable;
 	
 	
 	
 	/**
 	 * @param data
 	 * @return
 	 */
 	private String convertToHex(byte[] data) {
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0; i < data.length; i++) {
 			int halfbyte = (data[i] >>> 4) & 0x0F;
 			int two_halfs = 0;
 			do {
 				if ((0 <= halfbyte) && (halfbyte <= 9))
 					buf.append((char) ('0' + halfbyte));
 				else
 					buf.append((char) ('a' + (halfbyte - 10)));
 				halfbyte = data[i] & 0x0F;
 			} while (two_halfs++ < 1);
 		}
 		return buf.toString();
 	}
 
 	/**
 	 * @param text
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 * @throws UnsupportedEncodingException
 	 */
	public String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
 		MessageDigest md = MessageDigest.getInstance("SHA-1");
 		byte[] sha1hash = new byte[40];
 		md.update(text.getBytes("iso-8859-1"), 0, text.length());
 		sha1hash = md.digest();
 		return convertToHex(sha1hash);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ref.common.business.BusinessObject#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws Throwable 
 	 */
 	public String toBsonHash() throws Throwable {
 		return SHA1(toBson().toString());
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.ref.common.business.BusinessObject#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ref.common.business.BusinessObject#toString()
 	 */
 	@Override
 	public String toString() {
 		return "";
 	}
 }
