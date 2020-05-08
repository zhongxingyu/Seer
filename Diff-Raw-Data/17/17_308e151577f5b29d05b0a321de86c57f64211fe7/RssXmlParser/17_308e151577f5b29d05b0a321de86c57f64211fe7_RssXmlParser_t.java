 package ljw.jpodsuck;
 import java.io.Reader;
 import org.xml.sax.ContentHandler;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 import org.xml.sax.InputSource;
 
 public class RssXmlParser {
 	RssXmlParser(Reader buffer, PodcastsInterface podcasts) {
 		this.podcasts = podcasts;
 		this.buffer = buffer;
 	}
 	public boolean parse()
 	{
 		
 		XMLReader xr;
 		try {
 			xr = XMLReaderFactory.createXMLReader();
 			RssSaxHandler rssSaxHandler = new RssSaxHandler(podcasts);
 			xr.setContentHandler(rssSaxHandler);
 			xr.setErrorHandler(rssSaxHandler);
 			xr.parse(new InputSource(buffer));
 			return true;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (Exception e)
 		{
 			System.out.println("Caught an exception");
 			e.printStackTrace();
 		}
 		return false;
 	}
 	private Reader buffer;
 	private PodcastsInterface podcasts;
 }
 
 class RssSaxHandler extends DefaultHandler
 {
 	enum RssElement {
		TOP, CHANNELTITLE {
			void characters(PodcastsInterface podcasts, RssElement currElement, char[] ch, int start, int length) throws SAXException {
				RssElement.title.characters(podcasts, currElement, ch, start, length);
			}
		},
 		channel {
 			RssElement startElement(PodcastsInterface podcasts, RssElement currElement, Attributes atts) throws SAXException {
 				if (currElement == RssElement.TOP)
 				{
 					currElement = RssElement.channel;
 				}
 				return currElement;
 			}
 			RssElement endElement (PodcastsInterface podcasts, RssElement currElement, String localName) throws SAXException {
 				if (currElement == RssElement.channel)
 				{
 					return RssElement.TOP;	
 				}
 				else
 					throw new RuntimeException("Parser problem: " + this.name());
 			}
 		},
 		title {
 			RssElement startElement(PodcastsInterface podcasts, RssElement currElement, Attributes atts) throws SAXException {
 				title.delete(0, title.length());
 				if (currElement == RssElement.item)
 				{
 					currElement = RssElement.title;
 				}
 				else if (currElement == RssElement.channel)
 				{
 					currElement = RssElement.CHANNELTITLE;
 				}
 				return currElement;
 			}
 			RssElement endElement (PodcastsInterface podcasts, RssElement currElement, String localName) throws SAXException {
 				if (currElement == RssElement.title)
 				{
 					podcasts.addContextTitle(title.toString());
 					return RssElement.item;	
 				}
 				else if (currElement == RssElement.CHANNELTITLE)
 				{
 					podcasts.setChannelTitle(title.toString());
 					return RssElement.channel;
 				}
 				else
 					throw new RuntimeException("Parser problem: " + this.name());
 			}
 			void characters(PodcastsInterface podcasts, RssElement currElement, char[] ch, int start, int length) throws SAXException {
 				title.append(ch, start, length);
 			}
 			StringBuilder title = new StringBuilder();
 		},
 		pubdate {
 			RssElement startElement(PodcastsInterface podcasts, RssElement currElement, Attributes atts) throws SAXException {
 				if (currElement == RssElement.item)
 				{
 					currElement = RssElement.pubdate;
 				}
 				return currElement;
 			}
 			RssElement endElement(PodcastsInterface podcasts, RssElement currElement, String localName) throws SAXException {
 				if (currElement == RssElement.pubdate)
 				{
 					return RssElement.item;
 				}
 				else
 					throw new RuntimeException("Parser problem");
 			}
 			void characters(PodcastsInterface podcasts, RssElement currElement, char[] ch, int start, int length) throws SAXException {
 				pubdate.append(ch, start, length);
 			}
 			StringBuilder pubdate = new StringBuilder();
 		},
 		enclosure {
 			RssElement startElement(PodcastsInterface podcasts, RssElement currElement, Attributes atts) throws SAXException {
 				if (currElement == RssElement.item)
 				{
 					podcasts.addContextUrlInfo(atts.getValue("url"), java.lang.Integer.parseInt(atts.getValue("length")));
 					currElement = RssElement.enclosure;
 				}
 				return currElement;
 			}
 			RssElement endElement (PodcastsInterface podcasts, RssElement currElement, String localName) throws SAXException {
 				if (currElement == RssElement.enclosure)
 				{
 					return RssElement.item;
 				}
 				else
 					throw new RuntimeException("Parser problem");
 			}
 		},
 		item {
 			RssElement startElement(PodcastsInterface podcasts, RssElement currElement, Attributes atts) throws SAXException {
 				if (currElement == RssElement.channel)
 				{
 					podcasts.contextCreateItem();
 					currElement =  RssElement.item;
 				}
 				return currElement;
 			}
 			RssElement endElement (PodcastsInterface podcasts, RssElement currElement, String localName) throws SAXException {
 				if (currElement == RssElement.item)
 				{
 					podcasts.confirmContextItem();
 					return RssElement.channel;
 				}
 				else
 					throw new RuntimeException("Parser problem");
 			}
 		}
 		;
 		RssElement startElement(PodcastsInterface podcasts, RssElement currElement, Attributes atts) throws SAXException {
 			return currElement;
 		}
 		RssElement endElement (PodcastsInterface podcasts, RssElement currElement, String localName) throws SAXException {
 			return currElement;
 		}
 		void characters(PodcastsInterface podcasts, RssElement currElement, char[] ch, int start, int length) throws SAXException {
 			
 		}

 	}
 	public RssSaxHandler(PodcastsInterface podcasts)
 	{
 		super();
 		this.podcasts = podcasts;
 	}
     public void startDocument ()
     {
     }
 
     public void endDocument ()
     {
     }
     public void startElement (String uri, String localName, String qName, Attributes atts)
 	{
     	try {
     		currElement = RssElement.valueOf(RssElement.class, localName.toLowerCase()).startElement(podcasts, currElement, atts);
     	} catch (IllegalArgumentException e) {
     		
     	} catch (SAXException e) {
     		
     	}
 	}
 
 
 	public void endElement (String uri, String localName, String qName) {
 		try {
 			currElement = RssElement.valueOf(RssElement.class, localName.toLowerCase()).endElement(podcasts, currElement, localName);
		} catch (IllegalArgumentException e) {
     		
     	} catch (SAXException e) {
     		
     	} catch (Exception e) {
     		
     	}
 	}
 	
 	public void characters(char[] ch, int start, int length) {
 		try {
     		currElement.characters(podcasts, currElement, ch, start, length);
     	} catch (IllegalArgumentException e) {
     		
     	} catch (SAXException e) {
     		
     	} catch (Exception e) {
     		
     	}
 	}
 	private PodcastsInterface podcasts;
 	private RssElement currElement = RssElement.TOP;
 }
