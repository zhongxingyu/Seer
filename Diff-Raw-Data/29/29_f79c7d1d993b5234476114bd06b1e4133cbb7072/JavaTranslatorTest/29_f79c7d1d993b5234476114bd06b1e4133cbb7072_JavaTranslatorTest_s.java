 package ecologylab.translators.java.test;
 
 import java.io.File;
 import java.io.IOException;
 
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.DescriptorBase;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.simpl_inherit;
 import ecologylab.serialization.library.rss.Channel;
 import ecologylab.serialization.library.rss.Item;
 import ecologylab.serialization.library.rss.RssState;
 import ecologylab.standalone.xmlpolymorph.BItem;
 import ecologylab.standalone.xmlpolymorph.SchmItem;
 import ecologylab.standalone.xmlpolymorph.Schmannel;
 import ecologylab.translators.java.JavaTranslationException;
 import ecologylab.translators.java.JavaTranslator;
 import ecologylab.translators.parser.JavaDocParser;
 
 public class JavaTranslatorTest {
 	
 	public static void main(String[] args) 
 	{
 		try{
 			//testJavaCodeGeneration(args[0],args[1]);
 //			testSerializationWithPolymorph(args[0]);
 			// testJavaCodeGenerationWithPolymorphicInfo(args[0],args[2]);
 			testJavaCodeGenerationWithInheritance();
 		}catch(Exception ex)
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
 		DescriptorBase.setJavaParser(new JavaDocParser());
 		TranslationScope ts2 = TranslationScope.get("RSSTranslations5", RssState.class, Item.class, Channel.class);
 		TranslationScope.setGraphSwitch();		
 		ts2.serialize(new File(filename));
 	}
 	
 	/**
 	 * Testing serialising Translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testSerializationWithPolymorph(String filename) throws Exception
 	{
 		DescriptorBase.setJavaParser(new JavaDocParser());
 		TranslationScope ts2 = TranslationScope.get("RSSTranslations", Schmannel.class, BItem.class, SchmItem.class,
 				RssState.class, Item.class, Channel.class);
 		TranslationScope.setGraphSwitch();		
 		ts2.serialize(new File(filename));
 	}
 	
 	/**
 	 * Testing deserialising the translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testDeserialization(String filename) throws Exception
 	{
 		TranslationScope ts = TranslationScope.get("tscope_tscope2", TranslationScope.class, ClassDescriptor.class, FieldDescriptor.class);
 		TranslationScope.setGraphSwitch();
 		TranslationScope t = (TranslationScope)ts.deserialize(filename);
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
 		testSerialization(filename);
 		JavaTranslator c = new JavaTranslator();
 		TranslationScope ts = TranslationScope.get("tscope_tscope2", TranslationScope.class, ClassDescriptor.class, FieldDescriptor.class);
 		TranslationScope.setGraphSwitch();		
 		TranslationScope t = (TranslationScope)ts.deserialize(filename);
 		
 		TranslationScope.AddTranslationScope(t.getName(),t);
 		c.translateToJava(new File(codeLocation),t);
 	}	
 	
 	/**
 	 * Testing the Java code generation from serialised translation scope
 	 * 
 	 * @param filename
 	 * @throws Exception
 	 */
 	private static void testJavaCodeGenerationWithPolymorphicInfo(String filename, String codeLocation) throws Exception
 	{
 		testSerializationWithPolymorph(filename);
 		JavaTranslator c = new JavaTranslator();
 		TranslationScope ts = TranslationScope.get("tscope_tscope_polymorphic", TranslationScope.class, ClassDescriptor.class, FieldDescriptor.class);
 		TranslationScope.setGraphSwitch();		
 		TranslationScope t = (TranslationScope)ts.deserialize(filename);
 		
 		TranslationScope.AddTranslationScope(t.getName(),t);
 		c.translateToJava(new File(codeLocation),t);
 	}	
 	
 	@simpl_inherit class Base extends ElementState
 	{
 		@simpl_scalar private String fieldA;
 	}
 	
 	@simpl_inherit class Sub extends Base
 	{
 		@simpl_scalar private String fieldB;
 		@simpl_scalar private int    fieldC;
 	}
 
 	private static void testJavaCodeGenerationWithInheritance() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		TranslationScope ts = TranslationScope.get("test_inheritance", Base.class, Sub.class);
 		ts.setGraphSwitch();
 		ts.serialize(new File("c:/tmp/ts.xml"));
 		JavaTranslator jt = new JavaTranslator();
 		jt.translateToJava(new File("c:/tmp/jt"), ts);
 	}
 	
 }
