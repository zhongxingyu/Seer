 package de.tr0llhoehle.buschtrommel.models;
 
 public class GetFileMessage extends Message {
 
 	private String hash;
 	private long offset;
 	private long length;
 
 	/**
 	 * Creates an instance of GetFileMessage
 	 * 
 	 * @param hash
 	 *            the SHA1-Hash of the desired file as upper-case String,
 	 *            without the leading 0x.
 	 * @param offset
 	 *            Number of bytes to skip from the beginning of the file
 	 * @param length
 	 *            Number of bytes to transfer
 	 */
 	public GetFileMessage(String hash, long offset, long length) {
 		this.hash = hash;
 		this.offset = offset;
 		this.length = length;
 		type = "GET FILE";
 	}
 
 	@Override
 	public String Serialize() {
		return type + FIELD_SEPERATOR + hash + FIELD_SEPERATOR + offset
				+ offset + length + FIELD_SEPERATOR + MESSAGE_SPERATOR;
 	}
 
 }
