 package de.javawi.jstun.attribute;
 
 import de.javawi.jstun.util.Utility;
 import de.javawi.jstun.util.UtilityException;
 
 public class Software extends AbstractMessageAttribute {
 	
 	// it must be a UTF-8 encoded sequence of less than 128 characters
	private final static String software = "JSTUN2";
 	
 	public Software() {
 		super(MessageAttributeType.Software);
 	}
 
 	// TODO test this out
 	@Override
 	public byte[] getBytes() throws UtilityException {
 		byte[] result = new byte[COMMONHEADERSIZE + software.length()];
		
		int length = software.length();
 
 		// common attribute header
 		// type
		System.arraycopy(Utility.integerToTwoBytes(MessageAttributeType.Software.getEncoding()), 0, result, 0, 2);
 		// length
		System.arraycopy(Utility.integerToTwoBytes(length), 0, result, 2, 2);
 
 		if (length % ALIGNMENT != 0)
 			length += ALIGNMENT - (length % ALIGNMENT);
 		
 		// software attribute header
		System.arraycopy(software.getBytes(), 0, result, 4, length);
 
 		return result;
 	}
 
 }
