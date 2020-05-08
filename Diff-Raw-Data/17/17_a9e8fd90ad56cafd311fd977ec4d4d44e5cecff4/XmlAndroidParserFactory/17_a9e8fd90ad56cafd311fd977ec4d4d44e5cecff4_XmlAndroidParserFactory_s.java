 package com.ecologylab;
 
 import ecologylab.serialization.DeserializationHookStrategy;
 import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.deserializers.pullhandlers.stringformats.BaseXmlPullDeserializerFactory;
 import ecologylab.serialization.deserializers.pullhandlers.stringformats.StringPullDeserializer;
 
 public class XmlAndroidParserFactory extends BaseXmlPullDeserializerFactory
 {
	public StringPullDeserializer getFormatSerializer(TranslationScope translationScope,
 			TranslationContext translationContext, DeserializationHookStrategy deserializationHookStrategy)
 	{
 		return new AndroidXMLDeserializer(translationScope, translationContext);
 	}	
 	
 	public XmlAndroidParserFactory()
 	{}
 }
