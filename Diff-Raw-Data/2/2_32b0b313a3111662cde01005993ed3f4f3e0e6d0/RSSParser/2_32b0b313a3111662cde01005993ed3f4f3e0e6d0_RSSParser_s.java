 /*---------------------------------------------------------------------------*\
   $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
 * This interface defines a simplified view of an RSS channel, providing
  * only the methods necessary for <i>curn</i> to work. <i>curn</i> uses
  * the {@link RSSParserFactory} class to get a specific implementation of
  * an <tt>RSSParser</tt>. This strategy isolates the bulk of the code from
  * the underlying RSS parser, making it easier to substitute different
  * parsers as more of them become available.
  *
  * @see RSSParserFactory
  * @see RSSChannel
  * @see RSSItem
  *
  * @version <tt>$Revision$</tt>
  */
 public interface RSSParser
 {
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Parse an RSS feed.
      *
      * @param stream  the <tt>InputStream</tt> for the feed
      *
      * @return an <tt>RSSChannel</tt> object representing the RSS data from
      *         the site.
      *
      * @throws IOException        unable to read from URL
      * @throws RSSParserException unable to parse RSS XML
      */
     public RSSChannel parseRSSFeed (InputStream stream)
         throws IOException,
                RSSParserException;
 }
