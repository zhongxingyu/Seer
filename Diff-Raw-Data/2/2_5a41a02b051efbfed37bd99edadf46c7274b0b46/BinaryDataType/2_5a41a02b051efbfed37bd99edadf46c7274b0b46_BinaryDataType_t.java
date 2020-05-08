 package ecologylab.serialization.types.scalar;
 
 import java.nio.ByteBuffer;
 
 import ecologylab.generic.Base64Coder;
 import ecologylab.serialization.ScalarUnmarshallingContext;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.types.CrossLanguageTypeConstants;
 import ecologylab.serialization.types.ScalarType;
 
 @simpl_inherit
 public class BinaryDataType extends ScalarType<ByteBuffer>
 implements CrossLanguageTypeConstants 
 {
 	public BinaryDataType()
 	{
 		super(ByteBuffer.class, JAVA_BINARY_DATA, null, null);
 	}
 	
 	/**
 	 * read the Base64 encoded string . convert to byte array. 
 	 */
 	@Override
 	public ByteBuffer getInstance(String value, String[] formatStrings,
 			ScalarUnmarshallingContext scalarUnmarshallingContext) 
 	{
 			return ByteBuffer.wrap(Base64Coder.decode(value));
 	}
 
 	/**
 	 * read the binary content of the input byteBuffer, and encode it using Base64
 	 */
 	@Override
 	public String marshall(ByteBuffer input, TranslationContext serializationContext)
 	{	
		return new String(Base64Coder.encode(input.array()));
 	}
 }
