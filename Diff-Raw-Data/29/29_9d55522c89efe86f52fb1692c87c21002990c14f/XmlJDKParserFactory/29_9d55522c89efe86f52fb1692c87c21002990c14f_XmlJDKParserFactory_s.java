 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
 import ecologylab.serialization.DeserializationHookStrategy;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.deserializers.pullhandlers.PullDeserializer;
 
 public class XmlJDKParserFactory extends BaseXmlPullDeserializerFactory
 {
 	public StringPullDeserializer getFormatSerializer(TranslationScope translationScope,
 			TranslationContext translationContext, DeserializationHookStrategy deserializationHookStrategy)
 	{
		return new XMLPullDeserializer(translationScope, translationContext,
				deserializationHookStrategy);
 	}

	
 }
