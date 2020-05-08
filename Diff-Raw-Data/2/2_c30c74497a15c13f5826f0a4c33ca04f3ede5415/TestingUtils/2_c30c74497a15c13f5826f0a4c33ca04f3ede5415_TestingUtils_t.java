 package tests;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.deserializers.parsers.tlv.Utils;
 import ecologylab.serialization.formatenums.Format;
 import ecologylab.translators.cocoa.CocoaTranslationException;
 import ecologylab.translators.cocoa.CocoaTranslator;
 
 public class TestingUtils
 {
 
 	public static void testSerailization(Object object, DualBufferOutputStream outStream, Format format)
 			throws SIMPLTranslationException
 	{
 		SimplTypesScope.serialize(object, outStream, format);
 		printOutput(outStream, format);
 	}
 
 	public static void testDeserailization(InputStream inputStream,
 			SimplTypesScope translationScope, Format format) throws SIMPLTranslationException
 	{
 		Object object = translationScope.deserialize(inputStream, format);
 		DualBufferOutputStream outputStream = new DualBufferOutputStream();		
 		testSerailization(object, outputStream, Format.XML);		
 	}
 
 	public static void test(Object object, SimplTypesScope translationScope, Format format)
 			throws SIMPLTranslationException
 	{
 		DualBufferOutputStream outputStream = new DualBufferOutputStream();
 		
 		testSerailization(object, outputStream, format);		
 		
 		testDeserailization(new ByteArrayInputStream(outputStream.toByte()), translationScope,
 				format);
 
 		System.out.println();
 	}
 	
 	public static void generateCocoaClasses(SimplTypesScope typeScope) throws SIMPLTranslationException
 	{
 		CocoaTranslator ct = new CocoaTranslator();
 		try
 		{
			ct.translateToObjC(new File("/Users/nskhan84/Desktop/TestCases"), typeScope);
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (CocoaTranslationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void printOutput(DualBufferOutputStream outputStream, Format format)
 	{
 		if(format == Format.TLV)
 		{
 			Utils.writeHex(System.out, outputStream.toByte());			
 		}
 		else
 		{
 			System.out.println(outputStream.toString());
 		}
 	}
 }
