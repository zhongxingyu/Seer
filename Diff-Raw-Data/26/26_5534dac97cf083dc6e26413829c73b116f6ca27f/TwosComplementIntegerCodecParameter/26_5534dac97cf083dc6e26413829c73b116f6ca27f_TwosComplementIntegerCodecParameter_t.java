 package org.hbird.transport.payloadcodec.codecparameters.number;
 
 import java.util.BitSet;
 
 import org.hbird.transport.commons.util.BitSetUtility;
 import org.hbird.transport.commons.util.BytesUtility;
 import org.hbird.transport.payloadcodec.codecparameters.CodecParameter;
 import org.hbird.transport.spacesystemmodel.encoding.Encoding;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Mark Doyle
  * @param <T>
  */
 public class TwosComplementIntegerCodecParameter extends CodecParameter<Integer> {
 	private static final long serialVersionUID = 4601475539675898978L;
 	private static final Logger LOG = LoggerFactory.getLogger(TwosComplementIntegerCodecParameter.class);
 
 	public TwosComplementIntegerCodecParameter(final Parameter<Integer> hostParameter, final Encoding encoding) {
 		super(hostParameter, encoding);
 	}
 
 	@Override
 	public void decode(final byte[] inBytes, final int offset) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void decode(final BitSet inBitset, final int offset) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Extracting " + encoding.getSizeInBits() + " bit int value from " + BitSetUtility.binDump(inBitset));
 		}
 
 		BitSet actualParameter = inBitset.get(0, encoding.getSizeInBits());
 
 		final byte[] byteArray = BitSetUtility.toByteArray(actualParameter, encoding.getSizeInBits());
 		final Integer output = Integer.valueOf(BytesUtility.combine(byteArray, encoding.getSizeInBits(), true).intValue());
 
 		this.setValue(output);
 	}
 
 	@Override
 	public Byte[] encodeToByteArray(final Byte[] targetBytes, final int offset) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public BitSet encodeToBitSet(final BitSet out, final int originalOffset) {
 		BitSet result = out;
 		Integer value = getValue();
 		int offset = originalOffset;
 
 		// TODO this method
 		final long unsignedInt = value.longValue();
 
 		// checking whether the value fits into the bit string of length - 1
 		final long absValue = Math.abs(unsignedInt);
 		if (absValue > Math.pow(2.0, encoding.getSizeInBits()) - 1 || unsignedInt == Long.MIN_VALUE) {
 			// TODO Could indicate a programming or configuration bug which is not recoverable. Maybe System.exit?
 			throw new IllegalArgumentException("The value of " + unsignedInt + " does not fit into a bit string of " + (encoding.getSizeInBits() - 1) + " bits.");
 		}
 
 		// setting all bits to zero
 		// bitSetTarget.clear(offset, offset + getSizeInBits() - 1);
 
 		// setting up the number in reverse order
 		int mask = 1;
 		offset += encoding.getSizeInBits() - 1;
 
 		for (int i = 0; i < encoding.getSizeInBits(); i++, mask <<= 1) {
 			if ((mask & absValue) > 0) {
 				result.set(offset - i);
 			}
 		}
 
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Calculated Bitset from value " + value.intValue() + " was: " + BitSetUtility.binDump(result));
 		}
 
 		return result;
 	}
 
 }
