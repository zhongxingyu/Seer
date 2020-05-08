 package org.hbird.transport.payloadcodec.codecparameters.number;
 
 import java.nio.ByteBuffer;
 import java.util.BitSet;
 
 import org.hbird.core.commons.util.BitSetUtility;
 import org.hbird.core.commons.util.BytesUtility;
 import org.hbird.core.spacesystemmodel.encoding.Encoding;
 import org.hbird.core.spacesystemmodel.tmtc.Parameter;
 import org.hbird.transport.payloadcodec.codecparameters.CodecParameter;
 import org.hbird.transport.payloadcodec.exceptions.IncorrectJavaTypeParameter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Mark Doyle
  * @author Johannes Klug (John Clever)
  */
 public class UnsignedIntegerCodecParameter extends CodecParameter<Integer> {
 	private static final long serialVersionUID = 6820348312687042896L;
 	private static final Logger LOG = LoggerFactory.getLogger(UnsignedIntegerCodecParameter.class);
 
 	public UnsignedIntegerCodecParameter(final Parameter<Integer> hostParameter, final Encoding encoding) {
 		super(hostParameter, encoding);
 	}
 
 	@Override
 	public void decode(final byte[] inBytes, final int bitOffset) {
 		// TODO Do this without BitSet.
 		decode(BitSetUtility.fromByteArray(inBytes), bitOffset);
 	}
 
 	@Override
 	public void decode(final BitSet inBitset, final int bitOffset) {
 		final BitSet actualParameter = inBitset.get(bitOffset, bitOffset + encoding.getSizeInBits());
 
 		LOG.trace("Extracted " + encoding.getSizeInBits() + " bit int value from " + BitSetUtility.binDump(actualParameter));
 
 		final byte[] byteArray = BitSetUtility.toByteArray(actualParameter, encoding.getSizeInBits(), encoding.getByteOrder(), true);
 		ByteBuffer endianBytes = ByteBuffer.wrap(byteArray).order(encoding.getByteOrder());
 
 		LOG.trace("Byte array = " + BytesUtility.hexDump(byteArray));
 		LOG.trace("Endianness applied byte array = " + BytesUtility.hexDump(endianBytes.array()));
 
 		int output = 0;
 		if (encoding.getSizeInBits() <= Byte.SIZE) {
 			byte byteValue = endianBytes.get();
 			output = byteValue & 0xFF;
 		}
 		else if (encoding.getSizeInBits() <= Short.SIZE) {
 			short shortValue = endianBytes.getShort();
 			output = shortValue & 0xFFFF;
 		}
 		else if (encoding.getSizeInBits() <= Integer.SIZE) {
 			int intValue = endianBytes.getInt();
 			output = intValue & 0xFFFFFFFF;
 		}
 		else if (encoding.getSizeInBits() > Integer.SIZE) {
 			// The type is larger than 32 bits so we cannot process it in Parameter<Integer> it must be
 			// built with a Parameter<Long>
 			throw new IncorrectJavaTypeParameter(getQualifiedName(), encoding.getSizeInBits());
 		}
 
 		LOG.trace("Output(bin) = " + Long.toBinaryString(output));
 
 		this.setValue(output);
 	}
 
 	@Override
 	public BitSet encodeToBitSet(final BitSet out, final int offset) {
 		final long unsignedInt = getValue();
 
 		// checking whether the value fits into the bit string of length - 1
 		long absValue = Math.abs(unsignedInt);
 		if (absValue > Math.pow(2.0, encoding.getSizeInBits()) - 1 || unsignedInt == Long.MIN_VALUE) {
 			throw new RuntimeException("The value of " + unsignedInt + " does not fit into a bit string of " + encoding.getSizeInBits() + " bits.");
 		}
 
 		// Split into Bytes.
 		int numberOfBytes = encoding.getSizeInBits() / Byte.SIZE;
 		// Any remaining bits require an extra Byte
 		if (encoding.getSizeInBits() % Byte.SIZE != 0) {
 			numberOfBytes++;
 		}
 		ByteBuffer littleEndianBuffer = ByteBuffer.allocate(numberOfBytes);
 		if (encoding.getSizeInBits() <= Byte.SIZE) {
 			littleEndianBuffer.put((byte) absValue).flip();
 			absValue = littleEndianBuffer.order(encoding.getByteOrder()).get();
 		}
 		else if (encoding.getSizeInBits() <= Short.SIZE) {
 			littleEndianBuffer.putShort((short) absValue).flip();
 			absValue = littleEndianBuffer.order(encoding.getByteOrder()).getShort();
 		}
 		else if (encoding.getSizeInBits() <= Integer.SIZE) {
 			littleEndianBuffer.putInt((int) absValue).flip();
 			absValue = littleEndianBuffer.order(encoding.getByteOrder()).getInt();
 		}
 		else if (encoding.getSizeInBits() > Integer.SIZE) {
 			// The type is larger than 32 bits so we cannot process it in Parameter<Integer> it must be
 			// built with a Parameter<Long>
 			throw new IncorrectJavaTypeParameter(getQualifiedName(), encoding.getSizeInBits());
 		}
 
 		// setting all bits to zero
 		out.clear(offset, offset + encoding.getSizeInBits() - 1);
 
 		// setting up the number in reverse order which allows us to map the number to a BitSet
 		// in big endian order.
 		long mask = 1;
 
 		final int encodingOffset = offset + encoding.getSizeInBits() - 1;
 
 		for (int i = 0; i < encoding.getSizeInBits(); i++, mask <<= 1) {
 			if ((mask & absValue) > 0) {
 				out.set(encodingOffset - i);
 			}
 		}
 
 		LOG.trace("Calculated encoded Bitset from value " + getValue() + " was: " + BitSetUtility.binDump(out));
 
 		return out;
 	}
 
 	@Override
 	public byte[] encodeToByteArray(final byte[] targetBytes, final int offset) {
 		throw new UnsupportedOperationException();
 	}
 
 }
