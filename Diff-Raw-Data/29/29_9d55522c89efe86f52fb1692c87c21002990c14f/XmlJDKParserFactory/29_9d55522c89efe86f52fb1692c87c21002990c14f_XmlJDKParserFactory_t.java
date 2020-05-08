 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
 import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.deserializers.pullhandlers.PullDeserializer;
 
 public class XmlJDKParserFactory extends BaseXmlPullDeserializerFactory
 {
 	public StringPullDeserializer getFormatSerializer(TranslationScope translationScope,
 			TranslationContext translationContext, DeserializationHookStrategy deserializationHookStrategy)
			throws SIMPLTranslationException
 	{
		try
		{
			return new XMLPullDeserializer(translationScope, translationContext,
					deserializationHookStrategy);
		}
		catch (Exception e)
		{
			throw new SIMPLTranslationException(
					"Could not create XML deserializer, check platform! If Android. Have you included ecologylabFundamentalAndroid project?",
					e);
		}
 	}
 }
