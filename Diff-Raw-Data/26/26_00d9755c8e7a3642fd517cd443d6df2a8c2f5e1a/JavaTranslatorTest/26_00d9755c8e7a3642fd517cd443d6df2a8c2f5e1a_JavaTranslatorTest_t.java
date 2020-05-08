 package tests.translators.java;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import ecologylab.appframework.PropertiesAndDirectories;
 import ecologylab.generic.Debug;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.DescriptorBase;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.annotations.simpl_collection;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.annotations.simpl_scalar;
 import ecologylab.serialization.formatenums.Format;
 import ecologylab.serialization.library.rss.Channel;
 import ecologylab.serialization.library.rss.Item;
 import ecologylab.serialization.library.rss.RssState;
 import ecologylab.translators.CodeTranslationException;
 import ecologylab.translators.CodeTranslator;
 import ecologylab.translators.java.JavaTranslator;
 import ecologylab.translators.parser.JavaDocParser;
 
 public class JavaTranslatorTest 
 extends Debug
 {
 	public static final String	OUTPUT_DIR	= PropertiesAndDirectories.isWindows() ? "c:/tmp/" : "/tmp/";
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			testJavaCodeGeneration(OUTPUT_DIR + "serialize.xml", OUTPUT_DIR + "test/");
 			testSerializationWithPolymorph(args[0]);
 			testJavaCodeGenerationWithPolymorphicInfo(OUTPUT_DIR + "polySerialize.xml", OUTPUT_DIR +"testpoly/");
 			testJavaCodeGenerationWithInheritance(OUTPUT_DIR);
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Testing serialising Translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testSerialization(String filename) throws Exception
 	{
 		println("testSerialization("+filename);
 		DescriptorBase.setJavaParser(new JavaDocParser());
 		SimplTypesScope ts2 = SimplTypesScope.get("RSSTranslations5", RssState.class, Item.class,
 				Channel.class);
 		SimplTypesScope.enableGraphSerialization();
 		SimplTypesScope.serialize(ts2, new File(filename), Format.XML);
 		
 	}
 
 	/**
 	 * Testing serialising Translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testSerializationWithPolymorph(String filename) throws Exception
 	{
 		println("testSerializationWithPolymorph("+filename);
 		DescriptorBase.setJavaParser(new JavaDocParser());
		SimplTypesScope ts2 = SimplTypesScope.get("RSSTranslations", RssState.class, Item.class, Channel.class);
 		SimplTypesScope.enableGraphSerialization();
 		SimplTypesScope.serialize(ts2, new File(filename), Format.XML);
 	}
 
 	/**
 	 * Testing deserialising the translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testDeserialization(String filename) throws Exception
 	{
 		println("testDeserialization("+filename);
 		SimplTypesScope ts = SimplTypesScope.get("tscope_tscope2", SimplTypesScope.class,
 				ClassDescriptor.class, FieldDescriptor.class);
 		SimplTypesScope.enableGraphSerialization();
 		SimplTypesScope t = (SimplTypesScope) ts.deserialize(new File(filename), Format.XML);
 		System.out.println("Test");
 	}
 
 	/**
 	 * Testing the Java code generation from serialised translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testJavaCodeGeneration(String filename, String codeLocation) throws Exception
 	{
 		println("testJavaCodeGeneration"+filename);
 		testSerialization(filename);
 		CodeTranslator c = new JavaTranslator();
 		SimplTypesScope ts = SimplTypesScope.get("tscope_tscope2", SimplTypesScope.class,
 				ClassDescriptor.class, FieldDescriptor.class);
 		SimplTypesScope.enableGraphSerialization();
 		SimplTypesScope t = (SimplTypesScope) ts.deserialize(new File(filename), Format.XML);
 
 		c.translate(new File(codeLocation), t, null);
 	}
 
 	/**
 	 * Testing the Java code generation from serialised translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testJavaCodeGenerationWithPolymorphicInfo(String filename, String codeLocation)
 			throws Exception
 	{
 		testSerializationWithPolymorph(filename);
 		println("testJavaCodeGenerationWithPolymorphicInfo("+filename+", "+ codeLocation);
 		CodeTranslator c = new JavaTranslator();
 		SimplTypesScope ts = SimplTypesScope.get("tscope_tscope_polymorphic", SimplTypesScope.class,
 				ClassDescriptor.class, FieldDescriptor.class);
 		SimplTypesScope.enableGraphSerialization();
 		SimplTypesScope t = (SimplTypesScope) ts.deserialize(new File(filename), Format.XML);
 
 		c.translate(new File(codeLocation), t, null);
 	}
 
 	@simpl_inherit
 	class Base extends ElementState
 	{
 		@simpl_scalar
 		private String	fieldA;
 	}
 
 	@simpl_inherit
 	class Sub extends Base
 	{
 		@simpl_scalar
 		private String						fieldB;
 
 		@simpl_scalar
 		private int								fieldC;
 
 		@simpl_collection("item")
 		private ArrayList<String>	strs;
 	}
 
 	private static void testJavaCodeGenerationWithInheritance(String outputDir) throws IOException,
 			SIMPLTranslationException, CodeTranslationException
 	{
 		println("testJavaCodeGenerationWithInheritance(" + outputDir);
 		SimplTypesScope ts = SimplTypesScope.get("test_inheritance", Base.class, Sub.class);
 		SimplTypesScope.enableGraphSerialization();
 		
 		SimplTypesScope.serialize(ts, new File(outputDir + "ts.xml"), Format.XML);
 		
 		CodeTranslator jt = new JavaTranslator();
 		jt.translate(new File(outputDir + "jt"), ts, null);
 	}
 
 }
