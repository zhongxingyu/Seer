 package com.touchableheroes.android.xml.parser;
 
 
 import java.io.InputStream;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import com.touchableheroes.android.log.Logger;
 
 /**
  * @author Andreas Siebert, ask@touchableheroes.com
  */
 public class XMLParserFacade {
 
 	private final XmlPullParser parser;
 
 	public XMLParserFacade(final XmlPullParser parser) {
 		this.parser = parser;
 	}
 
 	public void useInput(final InputStream stream) {
 		try {
 			this.parser.setInput(stream, "UTF-8");
 		} catch (final Throwable tx) {
 			Logger.debug("Couldn't read xml-file");
 			tx.printStackTrace();
 		}
 	}
 	
 	public boolean isEndOfDoc() {
 		try {
 			return (parser.getEventType() == XmlPullParser.END_DOCUMENT);
 		} catch (final Throwable x) {
 			throw new IllegalStateException(
 					"Couldn't request type of xml-parser-event. Got an exception.", x);
 		}
 	}
 
	public boolean next() {
 		try {
 			parser.next();
			return true;
		} catch(  final Throwable x ) {
			Logger.error( "Skip the following exception (check log)" );
			x.printStackTrace();
			return false;
 		}
 	}
 
 	public boolean isStartTag() {
 		try {
 			return parser.getEventType() == XmlPullParser.START_TAG;
 		} catch (final Throwable x) {
 			throw new IllegalStateException(
 					"Couldn't call getEventType() to check type of event. Got an exception.", x);
 		}
 	}
 
 	public boolean isEndTag() {
 		try {
 			return parser.getEventType() == XmlPullParser.END_TAG;
 		} catch (final Throwable x) {
 			throw new IllegalStateException(
 					"Couldn't call getEventType() to check type of event. Got an exception.", x);
 		}
 	}
 
 	public boolean isText() {
 		try {
 			return parser.getEventType() == XmlPullParser.TEXT;
 		} catch (final Throwable x) {
 			throw new IllegalStateException(
 					"Couldn't call getEventType() to check type of event. Got an exception.", x);
 		}
 	}
 
 	public boolean isStartOfDoc() {
 		try {
 			return parser.getEventType() == XmlPullParser.START_DOCUMENT;
 		} catch (final Throwable x) {
 			throw new IllegalStateException(
 					"Couldn't call getEventType() to check type of event. Got an exception.", x);
 		}
 	}
 
 	public boolean isEmptyTag() {
 		try {
 			return parser.isEmptyElementTag();
 		} catch( final Throwable x) {
 			throw new IllegalStateException( "Couldn't check isEmptyElementTag()", x );
 		}
 	}
 	
 	
 
 }
