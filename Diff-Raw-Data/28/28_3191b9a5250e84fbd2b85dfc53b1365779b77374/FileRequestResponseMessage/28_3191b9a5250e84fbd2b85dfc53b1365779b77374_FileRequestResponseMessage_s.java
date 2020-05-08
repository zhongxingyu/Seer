 package de.tr0llhoehle.buschtrommel.models;
 
 public class FileRequestResponseMessage extends Message {
 
 	public static final String TYPE_FIELD = "FILEREQUESTRESPONSE";
 	
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
	 * Returns the FileRequest Response Message as string, ending with the last
	 * field seperator that is before the bit-stream that follows
 	 */
 	@Override
 	public String Serialize() {
		return type + FIELD_SEPERATOR + getResponseCode() + FIELD_SEPERATOR;
 	}
 
	private String getResponseCode() {
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
 
 	public enum ResponseCode {
 		OK, TRY_AGAIN_LATER, NEVER_TRY_AGAIN
 	}
 }
