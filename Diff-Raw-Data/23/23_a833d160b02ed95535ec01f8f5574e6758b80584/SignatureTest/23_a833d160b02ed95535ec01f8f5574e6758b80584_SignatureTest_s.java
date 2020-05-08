 package se.l4.sofa.dbus.spi;
 
 import org.testng.annotations.Test;
 
 @Test
 public class SignatureTest
 {
 	public void testByteSig()
 	{
 		checkParse("y");
 	}
 	
 	public void testBoolSig()
 	{
 		checkParse("b");
 	}
 	
 	public void testInt32()
 	{
 		checkParse("i");
 	}
 	
 	public void testUInt32()
 	{
 		checkParse("u");
 	}
 	
 	public void testInt16()
 	{
 		checkParse("n");
 	}
 	
 	public void testUInt16()
 	{
 		checkParse("q");
 	}
 	
 	public void testInt64()
 	{
 		checkParse("x");
 	}
 	
 	public void testUInt64()
 	{
 		checkParse("t");
 	}
 	
 	public void testDouble()
 	{
 		checkParse("d");
 	}
 	
 	public void testString()
 	{
 		checkParse("s");
 	}
 	
 	public void testObjectPath()
 	{
 		checkParse("o");
 	}
 	
 	public void testSignature()
 	{
 		checkParse("g");
 	}
 	
 	public void testVariant()
 	{
 		checkParse("v");
 	}
 	
 	public void testStringArray()
 	{
 		checkParse("as");
 	}
 	
 	public void testStruct()
 	{
 		checkParse("(s)");
 		checkParse("(is)");
 		checkParse("(ias)");
 		checkParse("(iasa{ss})");
 	}
 	
 	public void testDictEntry()
 	{
 		checkParse("{it}");
 		checkParse("{ias}");
 	}
 	
 	private static void checkParse(String s)
 	{
 		Signature sig = Signature.parse(s);
 		
 		assert s.equals(sig.getValue())
 			: "Parsing of " + s + " failed, got " + sig;
 	}
 }
