 package ch.compass.gonzoproxy.utils;
 
 import java.util.ArrayList;
 
 import ch.compass.gonzoproxy.model.Field;
 
 public class PacketUtils {
 
 	public static final int NEXT_IDENTIFIER_OFFSET = 2;
 	public static final int DEFAULT_FIELDLENGTH = 1;
 	public static final String CONTENT_LENGTH_FIELD = "Lc";
 	public static final String CONTENT_IDENTIFIER = "Ci";
	public static final String CONTENT_DATA = "DATA";
 	
 	public static final int ENCODING_OFFSET = 2;
 	public static final int WHITESPACE_OFFSET = 1;
 
 	public static int getEncodedFieldLength(int fieldLength, boolean whitespace) {
 		if(whitespace) {
 			return fieldLength * (ENCODING_OFFSET + WHITESPACE_OFFSET);
 		}
 		else {
 			return fieldLength * (ENCODING_OFFSET + WHITESPACE_OFFSET)
 					- WHITESPACE_OFFSET;
 		}
 	}
 
 
 	public static int findFieldInPlainPacket(byte[] plainPacket, int offset,
 			Field field) {
 		String nextIdentifier = field.getValue();
 		byte[] byteField = new byte[2];
 		for (int i = offset; i < plainPacket.length - 1; i++) {
 			byteField[0] = plainPacket[i];
 			byteField[1] = plainPacket[i + 1];
 			if (new String(byteField).equals(nextIdentifier)) {
 				return i;
 			}
 		}
 		return 0;
 	}
 
 	public static int getRemainingContentSize(int contentStartIndex,
 			int contentLength, int offset) {
 		return contentLength
 				- ((offset - contentStartIndex) / (ENCODING_OFFSET + WHITESPACE_OFFSET));
 	}
 
 	public static boolean isContentLengthField(Field processingField) {
 		return processingField.getName() != null
 				&& processingField.getName().equals(CONTENT_LENGTH_FIELD);
 	}
 
 	public static int calculateSubContentLength(int offset, int nextIdentifier) {
 		return (nextIdentifier - offset) / (ENCODING_OFFSET + WHITESPACE_OFFSET);
 	}
 
 	public static boolean isContentIdentifierField(Field processingField) {
 		return processingField.getName().equals(CONTENT_IDENTIFIER);
 	}
 
 	public static boolean hasCustomLenght(int fieldLength) {
 		return fieldLength > DEFAULT_FIELDLENGTH;
 	}
 
 	public static byte[] extractField(byte[] plainPacket,
 			int fieldLength, int currentOffset) {
 		return ByteArraysUtils.trim(plainPacket, currentOffset, fieldLength);
 	}
 
 
 	public static int findNextContentIdentifierField(int offset,
 			ArrayList<Field> templateFields) {
 		
 		int fieldIndex = 1;
 		for (int i = offset; i < templateFields.size(); i++) {
 			if(isContentIdentifierField(templateFields.get(i))){
 				return fieldIndex;
 			}
 			fieldIndex ++;
 		}
 		return 0;
 	}
 	
 	public static boolean isIdentifiedContent(ArrayList<Field> templateFields, int offset,
 			Field processingField) {
 		return PacketUtils.isContentIdentifierField(processingField) && templateFields.size() > offset + 1;
 	}
 
 }
