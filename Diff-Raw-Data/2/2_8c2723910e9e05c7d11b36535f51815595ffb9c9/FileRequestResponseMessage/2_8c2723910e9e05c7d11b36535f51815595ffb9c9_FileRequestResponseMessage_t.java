 package de.tr0llhoehle.buschtrommel.models;
 
 public class FileRequestResponseMessage extends Message {
 
	public static final String TYPE_FIELD = "FILE TRANSFER RESPONSE";
 	
 	private ResponseCode code;
 	private long expectedVolume;
 
 	/**
 	 * Create an FileRequestResponseMessage
 	 * 
 	 * @param code
 	 *            the resonse code to send
 	 * @param expectedTransferVolume
 	 *            the excpeted transfer volume. MUST be > 0
 	 */
 	public FileRequestResponseMessage(ResponseCode code,
 			long expectedTransferVolume) {
 		if (expectedTransferVolume < 0)
 			throw new IllegalArgumentException("transfer volume must be >= 0");
 
 		this.code = code;
 		this.expectedVolume = expectedTransferVolume;
 
 		type = TYPE_FIELD;
 	}
 
 	/**
 	 * Returns the FileRequest Response Message as string, ending with a message seperator, which is followed by the bit-stream that follows
 	 */
 	@Override
 	public String Serialize() {
 		return type + FIELD_SEPERATOR + getResponseCodeAsString() + FIELD_SEPERATOR + expectedVolume + MESSAGE_SPERATOR;
 	}
 
 	/**
 	 * Converts the response code to a string
 	 * @return
 	 */
 	public String getResponseCodeAsString() {
 		switch (code) {
 		case NEVER_TRY_AGAIN:
 			return "NEVER TRY AGAIN";
 		case TRY_AGAIN_LATER:
 			return "TRY AGAIN LATER";
 		case OK:
 			return "OK";
 		}
 
 		return "fuuuu";
 	}
 
 	
 	/**
 	 * Returns the code that was sent in this message
 	 * @return the response code
 	 */
 	public ResponseCode getResponseCode() {
 		return code;
 	}
 	
 	public enum ResponseCode {
 		OK, TRY_AGAIN_LATER, NEVER_TRY_AGAIN
 	}
 
 	public long getExpectedVolume() {
 		return expectedVolume;
 	}
 }
