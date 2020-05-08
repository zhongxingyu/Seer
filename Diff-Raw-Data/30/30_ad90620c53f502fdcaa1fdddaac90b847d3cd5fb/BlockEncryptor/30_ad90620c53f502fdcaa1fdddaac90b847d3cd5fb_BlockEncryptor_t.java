 package org.twuni.common.crypto.rsa;
 
 import static org.twuni.common.util.ByteArrayUtils.padLeft;
 
 import java.math.BigInteger;
 import java.util.Random;
 
 import org.twuni.common.crypto.InputLengthException;
import org.twuni.common.util.ByteArrayUtils;
 
 class BlockEncryptor extends BlockTransformer {
 
 	private final Random random;
 	private final int inputBlockSize;
 	private final int outputBlockSize;
 
 	public BlockEncryptor( BigInteger modulus ) {
 		super( modulus );
 		random = new Random();
		outputBlockSize = modulus.bitLength() / 8;
 		inputBlockSize = outputBlockSize - 3;
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
 	protected BigInteger read( byte [] input ) {
 
 		if( input.length > inputBlockSize ) {
 			throw new InputLengthException( String.format( "Input length %s cannot be greater than block size %s.", Integer.valueOf( input.length ), Integer.valueOf( inputBlockSize ) ) );
 		}
 
 		return super.read( pad( input, inputBlockSize ) );
 
 	}
 
 	@Override
 	protected byte [] write( BigInteger input ) {
		byte [] output = ByteArrayUtils.trim( input.toByteArray() );
		if( output.length > outputBlockSize ) {
			throw new InputLengthException( String.format( "Output length %s cannot be greater than block size %s.", Integer.valueOf( output.length ), Integer.valueOf( outputBlockSize ) ) );
		}
		return padLeft( output, outputBlockSize );
 	}
 
 	private byte [] pad( byte [] array, int length ) {
 		int padding = length - array.length;
 		byte [] padded = new byte [length + 2];
 		byte [] p = BigInteger.valueOf( padding ).toByteArray();
 		System.arraycopy( p, 0, padded, 2 - p.length, p.length );
 		System.arraycopy( array, 0, padded, padded.length - array.length, array.length );
 		if( padding > 0 ) {
 			byte [] junk = new byte [padding];
 			random.nextBytes( junk );
 			System.arraycopy( junk, 0, padded, 2, junk.length );
 		}
 		return padded;
 	}
 
 }
