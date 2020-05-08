 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamConstants;
 
 public class XMLParserSun implements XMLParser
 {
 	XMLStreamReader	xmlStreamReader;
 	
 	public XMLParserSun(XMLStreamReader parser)
 	{
 		xmlStreamReader = parser;
 	}
 	
 	public XMLStreamReader getParser()
 	{
 		return xmlStreamReader;
 	}
 	
 	public void setParser(XMLStreamReader parser)
 	{
 		xmlStreamReader = parser;
 	}
 	
 	public int eventTypeTransfer(int event)
 	{
 		switch(event)
 		{
 			case(XMLStreamConstants.START_ELEMENT):
 				return START_ELEMENT;
 			case(XMLStreamConstants.END_ELEMENT):
 				return END_ELEMENT;
 			case(XMLStreamConstants.START_DOCUMENT):
 				return START_DOCUMENT;
 			case(XMLStreamConstants.END_DOCUMENT):
 				return END_DOCUMENT;
 			case(XMLStreamConstants.CHARACTERS):
 				return CHARACTERS;
 			case(XMLStreamConstants.CDATA):
 				return CDATA;
 			default:
 				return ELSE;
 		}
 	}
 	
 	@Override
 	public int getEventType()
 	{
 		return eventTypeTransfer(xmlStreamReader.getEventType());
 	}
 
 	@Override
 	public String getText()
 	{
 		return xmlStreamReader.getText();
 	}
 
 	@Override
 	public String getName()
 	{
 		return xmlStreamReader.getName().toString();
 	}
 
 	@Override
	public int next()
 	{
 		try
 		{
 			return eventTypeTransfer(xmlStreamReader.next());
 		}
 		catch (XMLStreamException e)
 		{
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
		return ELSE;
 	}
 
 	@Override
	public int nextTag()
 	{
 		try
 		{
 			return eventTypeTransfer(xmlStreamReader.nextTag());
 		}
 		catch (XMLStreamException e)
 		{
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
		return ELSE;
 	}
 
 	@Override
 	public String getPrefix()
 	{
 		return xmlStreamReader.getPrefix();
 	}
 
 	@Override
 	public String getLocalName()
 	{
 		return xmlStreamReader.getLocalName();
 	}
 
 	@Override
 	public int getAttributeCount()
 	{
 		return xmlStreamReader.getAttributeCount();
 	}
 
 	@Override
 	public String getAttributeLocalName(int index)
 	{
 		return xmlStreamReader.getAttributeLocalName(index);
 	}
 
 	@Override
 	public String getAttributePrefix(int index)
 	{
 		return xmlStreamReader.getAttributePrefix(index);
 	}
 
 	@Override
 	public String getAttributeValue(int index)
 	{
 		return xmlStreamReader.getAttributeValue(index);
 	}
 
 }
