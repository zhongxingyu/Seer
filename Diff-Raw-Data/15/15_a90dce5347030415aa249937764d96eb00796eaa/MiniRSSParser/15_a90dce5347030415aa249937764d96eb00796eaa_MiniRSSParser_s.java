 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.parser.minirss;
 
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSParser;
 import org.clapper.curn.parser.RSSParserException;
 
 import org.clapper.util.logging.Logger;
 
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.Reader;
 import java.net.URL;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * <p><tt>MiniRSSParser</tt> is a stripped down RSS parser. It handles
  * files in
  * {@link <a target="_top" href="http://www.atomenabled.org/developers/">Atom</a>}
  * format (0.3) and RSS formats
  * {@link <a target="_top" href="http://backend.userland.com/rss091">0.91</a>},
  * 0.92,
  * {@link <a target="_top" href="http://web.resource.org/rss/1.0/">1.0</a>} and
  * {@link <a target="_top" href="http://blogs.law.harvard.edu/tech/rss">2.0</a>}.
  * However, it doesn't store all the possible RSS items. It stores those
  * items that the <i>curn</i> utility requires (plus a few more), but
  * lacks support for others.  Thus, it is unsuitable for use as a
  * general-purpose RSS parser (though it's perfectly suited for use
  * in <i>curn</i>).</p>
  *
  * <p><b>Notes:</b>
  *
  * <ol>
  *    <li> This API relies on the SAX 2 (org.xml.sax.*) package of XML parser
  *         classes; you must have those classes in your CLASSPATH to use this
  *         API.
  *
  *    <li> If a specific XML parser class is not specified to the constructor,
  *         this class defaults to using the Apache Xerces XML parser class.
  * </ol>
  *
  * <b>Warning:</b> This class is NOT thread safe. Because of the nature of
  * XML SAX event-driven (i.e., callback-driven) parsing, an instance of this
  * object must maintain parser state as instance data. 
  *
  * @version <tt>$Revision$</tt>
  */
 public class MiniRSSParser
     extends DefaultHandler
     implements RSSParser, ErrorHandler
 {
     /*----------------------------------------------------------------------*\
 			     Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String DEFAULT_XML_PARSER_CLASS_NAME =
                                       "org.apache.xerces.parsers.SAXParser";
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     private FeedInfo  feedInfo        = null;
     private Channel   channel         = null;
     private String    parserClassName = DEFAULT_XML_PARSER_CLASS_NAME;
     private XMLReader xmlReader       = null;
 
     /**
      * For logging
      */
     private static Logger log = new Logger (MiniRSSParser.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Allocate a new <tt>MiniRSSParser</tt> object that uses the default
      * Apache Xerces SAX XML parser.
      */
     public MiniRSSParser()
     {
         this (null);
     }
 
     /**
      * Allocate a new <tt>MiniRSSParser</tt> object that uses the specified
      * XML parser. The parser class must implement the SAX
      * <tt>XMLReader</tt> interface. The class is not actually loaded and
     * verified until one of the {@link #parse(File,String) parse} methods
     * is called.
      *
      * @param parserClassName  the fully-qualified parser class name
      */
     public MiniRSSParser (String parserClassName)
     {
 	if (parserClassName == null)
 	    parserClassName = DEFAULT_XML_PARSER_CLASS_NAME;
 
 	this.parserClassName = parserClassName;
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Parse an RSS feed. <b>Warning:</b> This method is NOT thread safe.
      * Because of the nature of XML SAX event-driven (i.e.,
      * callback-driven) parsing, an instance of this object must maintain
      * parser state as instance data.
      *
      * @param feedInfo The <i>curn</i> {@link FeedInfo} object for the feed
      * @param stream   the <tt>InputStream</tt> for the feed
      * @param encoding the encoding of the data in the field, if known, or
      *                 null
      *
      * @return an <tt>RSSChannel</tt> object representing the RSS data from
      *         the site.
      *
      * @throws IOException        unable to read from URL
      * @throws RSSParserException unable to parse RSS XML
      *
     * @see #parse(URL)
     * @see #parse(File)
     * @see #parse(File,String)
     * @see #parse(Reader)
      * @see Channel
      * @see RSSChannel
      */
     public final RSSChannel parseRSSFeed (FeedInfo    feedInfo,
                                           InputStream stream,
                                           String      encoding)
         throws IOException,
                RSSParserException
     {
         Reader r;
 
         if (encoding == null)
             r = new InputStreamReader (stream);
         else
             r = new InputStreamReader (stream, encoding);
 
         return parse (feedInfo, r);
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
                     Implementing ErrorHandler Interface
     \*----------------------------------------------------------------------*/
 
     /**
      * Handle a recoverable error from the SAX XML parser.
      *
      * @param ex  the parser exception
      *
      * @throws SAXException on error
      */
     public void error (SAXParseException ex)
         throws SAXException
     {
         log.error ("Recoverable SAX parser error", ex);
     }
 
     /**
      * Handle a non-recoverable error from the SAX XML parser.
      *
      * @param ex  the parser exception
      *
      * @throws SAXException on error
      */
     public void fatalError (SAXParseException ex)
         throws SAXException
     {
         throw new SAXException ("Fatal SAX parser error: " + ex.toString());
     }
 
     /**
      * Handle a warning from the SAX XML parser.
      *
      * @param ex  the parser exception
      *
      * @throws SAXException on error
      */
     public void warning (SAXParseException ex)
         throws SAXException
     {
         log.error ("SAX parser warning", ex);
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
                         Overriding XMLReaderAdapter
     \*----------------------------------------------------------------------*/
 
     /**
      * Handle the start of an XML element. This method assumes that it's
      * getting the first element in the RSS file. It examines that element,
      * and hands off control to either a <tt>V1Parser</tt> or
      * <tt>V2Parser</tt> object for version-specific parsing.
      *
      * @param namespaceURI       the Namespace URI, or the empty string if the
      *                           element has no Namespace URI or if Namespace
      *                           processing is not being performed
      * @param namespaceLocalName the local name (without prefix), or the empty
      *                           string if Namespace processing is not being
      *                           performed.
      * @param elementName        the qualified element name (with prefix), or
      *                           the empty string if qualified names are not
      *                           available
      * @param attributes         the attributes attached to the element.
      *
      * @throws SAXException parsing error
      */
     public void startElement (String     namespaceURI,
                               String     namespaceLocalName,
                               String     elementName,
                               Attributes attributes)
         throws SAXException
     {
         // We're at the top of the document.
 
         channel = new Channel();
 
         if (elementName.equals ("rdf:RDF"))
         {
             channel.setRSSFormat ("RSS 1.0");
             xmlReader.setContentHandler (new V1Parser (channel,
                                                        feedInfo,
                                                        elementName));
         }
 
         else if (elementName.equals ("feed"))
         {
             String version = attributes.getValue ("version");
             if (version == null)
                 channel.setRSSFormat ("Atom");
             else
                 channel.setRSSFormat ("Atom " + version);
             xmlReader.setContentHandler (new AtomParser (channel,
                                                          feedInfo,
                                                          elementName));
         }
 
         else if (elementName.equals ("rss"))
         {
             String version = attributes.getValue ("version");
             channel.setRSSFormat ("RSS " + version);
 
             // For curn's purposes, there's considerable similarity between
             // RSS version 0.91 and RSS version 2--so much so that the same
             // parser logic will work for both.
 
             if (version.startsWith ("0.9") || version.startsWith ("2."))
             {
                 xmlReader.setContentHandler (new V2Parser (channel,
                                                            feedInfo,
                                                            elementName));
             }
 
             else
             {
                 throw new SAXException ("Unknown RSS version: " + version);
             }
         }
 
         else
         {
             throw new SAXException ("Unknown or unsupported RSS type. "
                                   + "First XML element is <"
                                   + elementName
                                   + ">");
         }
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Parses an RSS feed from an already-open <tt>Reader</tt> object.
      *
      * @param feedInfo The <i>curn</i> {@link FeedInfo} object for the feed
      * @param r        The <tt>Reader</tt> that will produce the RSS XML
      *
      * @return the <tt>Channel</tt> object containing the parsed RSS data
      *
      * @throws IOException        error opening or reading from the URL
      * @throws RSSParserException error parsing the XML
      *
      * @see #parseRSSFeed(InputStream,String)
     * @see #parse(File)
     * @see #parse(File,String)
     * @see #parse(URL)
      * @see Channel
      * @see RSSChannel
      */
     private RSSChannel parse (FeedInfo feedInfo, Reader r)
 	throws IOException,
 	       RSSParserException
     {
 	try
         {
             xmlReader = XMLReaderFactory.createXMLReader (parserClassName);
             xmlReader.setContentHandler (this);
             xmlReader.setErrorHandler (this);
 
             this.feedInfo = feedInfo;
             xmlReader.parse (new InputSource (r));
         }
 
         catch (SAXException ex)
         {
             throw new RSSParserException (ex);
         }
 
         finally
         {
             this.feedInfo = null;
         }
 
         return channel;
     }
 }
