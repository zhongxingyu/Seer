 package org.twuni.common.crypto.rsa;
 
 import java.math.BigInteger;
 
 import org.twuni.common.util.ByteArrayUtils;
 
 class BlockDecryptor extends BlockTransformer {
 
 	private final int inputBlockSize;
 	private final int outputBlockSize;
 
 	public BlockDecryptor( BigInteger modulus ) {
 		super( modulus );
		inputBlockSize = ( modulus.bitLength() + 7 ) / 8;
 		outputBlockSize = inputBlockSize - 3;
 	}
 
 	@Override
 	public int getInputBlockSize() {
 		return inputBlockSize;
 	}
 
 	@Override
 	public int getOutputBlockSize() {
 		return outputBlockSize;
 	}
 
 	@Override
 	protected byte [] write( BigInteger input ) {
 		byte [] output = ByteArrayUtils.padLeft( input.toByteArray(), outputBlockSize + 2 );
 		if( output.length > outputBlockSize ) {
 			output = trim( output );
 		}
 		return output;
 	}
 
 	private byte [] trim( byte [] array ) {
 		byte [] p = new byte [2];
 		System.arraycopy( array, 0, p, 0, p.length );
 		int padding = new BigInteger( 1, p ).intValue();
 		byte [] trimmed = new byte [array.length - 2 - padding];
 		System.arraycopy( array, padding + 2, trimmed, 0, trimmed.length );
 		return trimmed;
 	}
 
 }
