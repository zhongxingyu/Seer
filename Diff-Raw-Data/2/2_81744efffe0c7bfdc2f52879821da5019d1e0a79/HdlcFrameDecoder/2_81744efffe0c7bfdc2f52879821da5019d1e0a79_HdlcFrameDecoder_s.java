 package org.hbird.transport.protocols.hdlc;
 
 import java.nio.ByteBuffer;
 
 /**
  * Decodes an HDLC frame and returns the HDLC information section. Essentially, this is the packet or payload of the
  * frame.
  * 
  * @author Mark Doyle
  * 
  */
 public class HdlcFrameDecoder {
 
	/** whether the frame decoder should expect a checksum and calculate the validity of the frame. */
 	private boolean useChecksum = true;
 
 	// Not implemented yet
 	@SuppressWarnings("unused")
 	private byte address = 0;
 
 	// Not implemented yet
 	@SuppressWarnings("unused")
 	private byte control = 0;
 
 	/**
 	 * Decodes a byte array as an HDLC frame.
 	 * 
 	 * @param dataIn
 	 * @return raw decoded hdlc information byte array, that is, the packet in the HDLC frame.
 	 */
 	public final byte[] decode(byte[] dataIn) {
 		ByteBuffer in = ByteBuffer.wrap(dataIn);
 
 		// TODO Address and Control still to be implemented.
 		address = in.get();
 		control = in.get();
 
 		if (useChecksum) {
 			// TODO get checksum here and do calculations
 		}
 
 		byte[] hdlcInformation = new byte[in.remaining()];
 		in.get(hdlcInformation);
 
 		return hdlcInformation;
 	}
 
 	/**
 	 * Getter for {@link HdlcFrameDecoder#useChecksum}
 	 * 
 	 * @return {@link HdlcFrameDecoder#useChecksum}
 	 */
 	public boolean isUseChecksum() {
 		return useChecksum;
 	}
 
 	/**
 	 * Setter for {@link HdlcFrameDecoder#useChecksum}
 	 * 
 	 * @param boolean to set {@link HdlcFrameDecoder#useChecksum}
 	 */
 	public void setUseChecksum(boolean useChecksum) {
 		this.useChecksum = useChecksum;
 	}
 }
