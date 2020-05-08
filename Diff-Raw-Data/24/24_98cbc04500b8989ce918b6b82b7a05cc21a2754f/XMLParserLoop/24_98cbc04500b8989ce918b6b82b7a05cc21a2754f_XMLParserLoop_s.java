 package com.touchableheroes.android.xml.parser;
 
 import java.io.InputStream;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.util.Xml;
 
 /**
  * @author Andreas Siebert, ask@touchableheroes.com
  */
 public class XMLParserLoop {
 
 	private final XMLParserFacade parser;
 	private final XMLEventPipe pipe;
 
 	public XMLParserLoop(final XMLParserFacade parser, final XMLEventPipe pipe) {
 		this.parser = parser;
 		this.pipe = pipe;
 	}
 	
 	public void useInput(final InputStream stream) {
 		parser.useInput(stream);
 	}
 	
 	public void run() {
 		do {
			parser.next();
 
 			if (parser.isStartTag()) {
 				final boolean isEmptyTag = parser.isEmptyTag();
 				pipe.startTag(isEmptyTag);
				
//				if( parser.isEndTag() )
//					pipe.endTag();
 			} else if (parser.isEndTag()) {
 				pipe.endTag();
 			} else if (parser.isText() || parser.isStartOfDoc()
 					|| parser.isEndOfDoc()) {
 				; // skip event-type
 			}
 		} while (!parser.isEndOfDoc());
 	}
 
 	/**
 	 * factory-method to create parser-loop.
 	 * 
 	 * @return never NULL
 	 */
 	public static final XMLParserLoop create(
 			final XMLTagHandler<? extends DomainSpecificBinding> handler) {
 		try {
 			final XmlPullParser parser = Xml.newPullParser();
 
 			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 			parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
 //			parser.setFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, true);
 			final XMLParserFacade facade = new XMLParserFacade(parser);
 
 			return new XMLParserLoop(facade, new XMLEventPipe(parser, handler,
 					new XMLTagFacade(parser)));
 		} catch (final Throwable x) {
 			throw new IllegalStateException(
 					"Couldn't create xml-parser. Exception.", x);
 		}
 	}
 
 }
