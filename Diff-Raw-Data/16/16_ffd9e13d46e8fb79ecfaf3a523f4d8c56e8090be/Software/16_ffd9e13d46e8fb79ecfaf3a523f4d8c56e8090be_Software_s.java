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
 		
 		// common attribute header
 		// type
 		System.arraycopy(Utility.integerToTwoBytes(MessageAttributeType.Software.getEncoding()), 0, result, 0, 2);
 		// length
		System.arraycopy(Utility.integerToTwoBytes(software.length()), 0, result, 2, 2);
 		
		int padding = software.getBytes().length % ALIGNMENT;
 		// software attribute header
		System.arraycopy(software.getBytes(), 0, result, 4, software.length());
		// add padding
		System.arraycopy(null, 0, result, software.length(), padding);
 		return result;
 	}
 
 }
