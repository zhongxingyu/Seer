 package uk.co.samatkins;
 
 public class Base64 {
 	
 	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
 			+ "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "+/";
 	private static final int SPLIT_LINES_AT = 76;
 	
 	public static String encode(String source) {
 		StringBuilder builder = new StringBuilder();
 		
 		int length = source.getBytes().length;
 		
 		// Calculate padding
 		int paddingLength = (3 - (length % 3)) % 3;
 		length += paddingLength;
 		
 		// Pad array
 		byte[] byteArray = new byte[length]; 
 		System.arraycopy(source.getBytes(), 0, byteArray, 0, length-paddingLength);
 		
 		// Encode chars
 		int j;
 		for (int i=0; i<byteArray.length; i+=3) {
 			j = ((byteArray[i] & 0xff) << 16) +
 				((byteArray[i + 1] & 0xff) << 8) + 
 				(byteArray[i + 2] & 0xff);
 			builder.append(CHARS.charAt( (j >> 18) & 0x3f))
 					.append(CHARS.charAt( (j >> 12) & 0x3f))
 					.append(CHARS.charAt( (j >> 6) & 0x3f))
 					.append(CHARS.charAt( j & 0x3f));
 		}
 		
 		// Split into lines and add ==s
 		String result = builder.toString();
 		return splitLines(result.substring(0, result.length() - paddingLength)
 				+ "==".substring(0, paddingLength));
 	}
 	
 	public static void main(String[] args) {
 		String encoded = Base64.encode("Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.");
 		System.out.println(encoded);
 		System.out.println(Base64.decode(encoded));
 		
 	}
 	
 	private static String splitLines(String input) {
 		StringBuilder builder = new StringBuilder();
 		for (int i=0; i < input.length(); i+= SPLIT_LINES_AT) {
 			builder.append(input.substring(i, Math.min(input.length(), i + SPLIT_LINES_AT)))
 					.append("\r\n");
 			
 		}
 		return builder.toString();
 	}
 	
 	public static String decode(String source) {
 		StringBuilder builder = new StringBuilder();
 		
 		// Strip-out the line breaks
 		for (int i=0; i<source.length(); i+= SPLIT_LINES_AT + 2) {
 			int remaining = source.length() - i;
 			builder.append(source.substring(i, i + Math.min(remaining, SPLIT_LINES_AT)));
 		}
 		
 		String singleLine = builder.toString();
 		builder = new StringBuilder();
		int j;
 		for (int i=0; i<singleLine.length(); i+=4) {
			j = (CHARS.indexOf(singleLine.charAt(i)) << 18)
				+ (CHARS.indexOf(singleLine.charAt(i+1)) << 12)
				+ (CHARS.indexOf(singleLine.charAt(i+2)) << 6)
				+ (CHARS.indexOf(singleLine.charAt(i+3)) );
			builder.append((char) (j >> 16 & 0xff))
					.append((char) (j >> 8 & 0xff))
					.append((char) (j & 0xff));
 		}
 		
 		return builder.toString();
 	}
 }
