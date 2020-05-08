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
 
 	private Byte currentlyHandling = null;
 
 	private byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
 		boolean state = true;
 
 		if (currentlyHandling != null) {
 			// figure out which type we were handling and send IoBuffer to that method to continue handling
 			return handleType(in, out, state, currentlyHandling);
 		}
 
 		byte first = in.get();
 
 		// If we have a FEND and some data, we are ready to go
 		if (first == FEND && in.hasRemaining()) {
 			byte type = in.get();
 
 			if (type == FEND) {
 				// drop second FEND as this could be a sync flush
 				if (in.hasRemaining()) {
 					type = in.get();
 				}
 				else {
 					return false;
 				}
 			}
 
 			// The type indicator is split across two nibbles so we need to get those first...
 			byte commandType = (byte) (type & LOW_NIBBLE_MASK);
 
 			@SuppressWarnings("unused")
 			// Not implemented send to port yet
 			byte port = (byte) ((type >> 4) & LOW_NIBBLE_MASK);
 
 			currentlyHandling = commandType;
 			state = handleType(in, out, state, commandType);
 		}
 
 		return state;
 	}
 
 	private boolean handleType(IoBuffer in, ProtocolDecoderOutput out, boolean state, byte commandType) {
 		boolean localState = state;
 		// Now we can perform the correct logic given the type.
 		switch (commandType) {
 			case DATA_FRAME:
 				localState = handleDataFrame(in, out);
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
 			case RETURN:
 				break;
 			default:
 				// Corruption
 				LOG.error("Corrupt KISS frame; unknown command type: 0x" + Integer.toHexString(commandType & 0xFF));
 				currentlyHandling = null;
 				localState = true;
 				break;
 		}
 		return localState;
 	}
 
 	/**
 	 * Writes out the data portion of the frame.
 	 * 
 	 * @param in
 	 * @param out
 	 * @return
 	 */
 	private boolean handleDataFrame(IoBuffer in, ProtocolDecoderOutput out) {
 		if (!in.hasRemaining()) {
 			// Nothing here! Might need more data from the OS network buffer.
 			return false;
 		}
 
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
 
 			// If there is no data left and we have not yet found a FEND then we need more data from the buffer
 			// returning false signifies this.
 			if (!in.hasRemaining()) {
 				return false;
 			}
 		}
 
 		// The next byte is now FEND so we have the full data payload and can write out the data.
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("KISS frame data = " + BytesUtility.hexDump(data));
 		}
 		out.write(data);
 		currentlyHandling = null;
 		data = ArrayUtils.EMPTY_BYTE_ARRAY;
 
 		return true;
 	}
 }
