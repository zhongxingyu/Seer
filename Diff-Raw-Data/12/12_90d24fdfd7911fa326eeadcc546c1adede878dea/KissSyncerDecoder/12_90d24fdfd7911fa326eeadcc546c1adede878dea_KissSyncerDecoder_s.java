 package org.hbird.transport.protocols.kiss;
 
 import static org.hbird.transport.protocols.kiss.KissConstants.DATA_FRAME;
 import static org.hbird.transport.protocols.kiss.KissConstants.FEND;
 import static org.hbird.transport.protocols.kiss.KissConstants.FESC;
 import static org.hbird.transport.protocols.kiss.KissConstants.FULL_DUPLEX;
 import static org.hbird.transport.protocols.kiss.KissConstants.P;
 import static org.hbird.transport.protocols.kiss.KissConstants.RETURN;
 import static org.hbird.transport.protocols.kiss.KissConstants.SET_HARDWARE;
 import static org.hbird.transport.protocols.kiss.KissConstants.SLOT_TIME;
 import static org.hbird.transport.protocols.kiss.KissConstants.TFEND;
 import static org.hbird.transport.protocols.kiss.KissConstants.TFESC;
 import static org.hbird.transport.protocols.kiss.KissConstants.TX_DELAY;
 import static org.hbird.transport.protocols.kiss.KissConstants.TX_TAIL;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
 import org.apache.mina.filter.codec.ProtocolDecoderOutput;
 import org.hbird.core.commons.util.BytesUtility;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * See standard: http://www.ka9q.net/papers/kiss.html
  * 
  * @author Mark Doyle
  * @author Johannes Klug
  * 
  */
 public class KissSyncerDecoder extends CumulativeProtocolDecoder {
 	private static final Logger LOG = LoggerFactory.getLogger(KissSyncerDecoder.class);
 
 	private static final int LOW_NIBBLE_MASK = 0x0F;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
 		boolean state = true;
 
 		byte first = in.get();
 
 		// If we have a FEND and some data, we are ready to go
 		if (first == FEND && in.hasRemaining()) {
 			byte type = in.get();
 
 			// If the type is RETURN we need to get out of here.
 			if (type == RETURN) {
 				// exit KISS
 				state = true; // TODO Check this.
 			}
 			else {
 				if (type == FEND) {
 					// drop second FEND as this could be a sync flush
 					type = in.get();
 				}
 
 				// The type indicator is split across two nibbles so we need to get those first...
 				byte commandType = (byte) (type & LOW_NIBBLE_MASK);
 
 				@SuppressWarnings("unused")
 				// Not implemented send to port yet
 				byte port = (byte) ((type >> 4) & LOW_NIBBLE_MASK);
 
 				// Now we can perform the correct logic given the type.
 				switch (commandType) {
 					case DATA_FRAME:
 						state = handleDataFrame(in, out);
 						break;
 					case TX_DELAY:
 						break;
 					case P:
 						break;
 					case SLOT_TIME:
 						break;
 					case TX_TAIL:
 						break;
 					case FULL_DUPLEX:
 						break;
 					case SET_HARDWARE:
 						break;
 					default:
 						// Corruption
 						LOG.error("Corrupt KISS frame; unknown command type: 0x" + Integer.toHexString(commandType & 0xFF));
 						state = true;
 						break;
 				}
 			}
 		}
 
 		return state;
 	}
 
 	/**
 	 * Writes out the data portion of the frame.
 	 * 
 	 * @param in
 	 * @param out
 	 * @return
 	 */
 	private static boolean handleDataFrame(IoBuffer in, ProtocolDecoderOutput out) {
 		byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;
 		byte next = (byte) 0x00;
 		while ((next = in.get()) != FEND) {
 			if (next == FESC) {
 				// escape mode active
 				next = in.get();
 				if (next == TFEND) {
 					next = FEND;
 				}
 				else if (next == TFESC) {
 					next = FESC;
 				}
 				else {
 					// escaped mode error; no action is taken and frame assembly continues
 					LOG.warn("Unexpected byte 0x" + Integer.toHexString(next & 0xFF)
 							+ " after escape in KISS TNC frame. Expected TFESC or TFEND. Ignoring escape.");
 				}
 			}
 			data = ArrayUtils.add(data, next);
 		}
 
 		// The next byte is now FEND so we have the full data payload and can write out the data.
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("KISS frame data = " + BytesUtility.hexDump(data));
 		}
 		out.write(data);
 
 		return true;
 	}
 }
