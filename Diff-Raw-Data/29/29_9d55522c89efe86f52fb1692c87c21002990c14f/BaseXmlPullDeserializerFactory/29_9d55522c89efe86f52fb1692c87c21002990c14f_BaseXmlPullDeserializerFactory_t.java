 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
 import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.XMLTools;
 
 public abstract class BaseXmlPullDeserializerFactory
 {
 	private static BaseXmlPullDeserializerFactory factory = null;
 	
 	public static BaseXmlPullDeserializerFactory getXMLPullDeserializerFactory()
 	{
 		if(factory == null)
 		{
 			synchronized (BaseXmlPullDeserializerFactory.class)
 			{
 				if(factory == null)
 				{
 					
 					try
 					{
 						Class c = Class.forName("com.ecologylab.XmlAndroidParserFactory");
 						factory = (BaseXmlPullDeserializerFactory) XMLTools.getInstance(c);
 					}
 					catch (Exception e)
 					{
 						System.out.println("andoid XML parser not loaded. returning JDK pull parser");
 						factory = new XmlJDKParserFactory();
 					}
 				}
 			}
 		}
 		
 		return factory;
 	}
 	
 	public abstract StringPullDeserializer getFormatSerializer(TranslationScope translationScope,
			TranslationContext translationContext, DeserializationHookStrategy deserializationHookStrategy) throws SIMPLTranslationException;
 }
