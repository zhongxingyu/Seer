 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
import ecologylab.serialization.SIMPLTranslationException;

 public interface XMLParser
 {
 	static final int START_DOCUMENT= 0;
 	static final int START_ELEMENT = 1;
 	static final int CHARACTERS = 2;
 	static final int END_ELEMENT = 3;
 	static final int END_DOCUMENT = 4;
 	static final int CDATA = 5;
 	static final int ELSE = 99;
 	
	int getEventType() throws SIMPLTranslationException;
 	String getText();
 	String getName();
	int next() throws SIMPLTranslationException;
	int nextTag() throws SIMPLTranslationException;
 	String getPrefix();
 	String getLocalName();
 	int getAttributeCount();
 	String getAttributeLocalName(int index);
 	String getAttributePrefix(int index);
 	String getAttributeValue(int index);
 }
