 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a BSD-style license:
 
   Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
 
   1.  Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.
 
   2.  The end-user documentation included with the redistribution, if any,
       must include the following acknowlegement:
 
         "This product includes software developed by Brian M. Clapper
         (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
         copyright (c) 2004-2007 Brian M. Clapper."
 
       Alternately, this acknowlegement may appear in the software itself,
       if wherever such third-party acknowlegements normally appear.
 
   3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
       nor any of the names of the project contributors may be used to
       endorse or promote products derived from this software without prior
       written permission. For written permission, please contact
       bmc@clapper.org.
 
   4.  Products derived from this software may not be called "clapper.org
       Java Utility Library", nor may "clapper.org" appear in their names
       without prior written permission of Brian M. Clapper.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
   NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.parser.rome;
 
 import org.clapper.curn.parser.ParserUtil;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 import org.clapper.curn.parser.RSSLink;
 
 import org.clapper.util.logging.Logger;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  * This class implements the <tt>RSSChannel</tt> interface and defines an
  * adapter for the {@link <a href="https://rome.dev.java.net/">Rome</a>}
  * RSS Parser's <tt>SyndFeedI</tt> type.
  *
  * @see org.clapper.curn.parser.RSSParserFactory
  * @see org.clapper.curn.parser.RSSParser
  * @see org.clapper.curn.parser.RSSChannel
  * @see org.clapper.curn.parser.RSSItem
  * @see RSSItemAdapter
  *
  * @version <tt>$Revision$</tt>
  */
 public class RSSChannelAdapter extends RSSChannel
 {
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * The real channel object
      */
     private SyndFeed syndFeed;
 
     /**
      * For log messages
      */
     private static final Logger log = new Logger (RSSChannelAdapter.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Allocate a new <tt>RSSChannelAdapter</tt> object that wraps the
      * specified Rome <tt>SyndFeedI</tt> object.
      *
      * @param syndFeed  the <tt>SyndFeedI</tt> object
      */
     RSSChannelAdapter(SyndFeed syndFeed)
     {
         this.syndFeed = syndFeed;
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Create a new, empty instance of the underlying concrete
      * class.
      *
      * @return the new instance
      */
     public RSSChannel newInstance()
     {
         return new RSSChannelAdapter(new SyndFeedImpl());
     }
 
     /**
      * Get a <tt>Collection</tt> of the items in this channel. All objects
      * in the collection are of type <tt>RSSItem</tt>.
      *
      * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
      *         The collection will be empty (never null) if there are
      *         no items.
      */
     public Collection<RSSItem> getItems()
     {
         Collection<RSSItem> result = new HashSet<RSSItem>();
 
         for (Iterator it = syndFeed.getEntries().iterator(); it.hasNext(); )
             result.add(new RSSItemAdapter((SyndEntry) it.next(), this));
 
         return result;
     }
 
     /**
      * Change the items the channel the ones in the specified collection.
      * If the collection is empty, the items are cleared. The items are
      * copied from the supplied collection. (A reference to the supplied
      * collection is <i>not</i> saved in this object.)
      *
      * @param newItems  new collection of <tt>RSSItem</tt> items.
      */
     public void setItems (Collection<? extends RSSItem> newItems)
     {
         Collection<SyndEntry> syndItems = new ArrayList<SyndEntry>();
 
        if (newItems != null)
         {
            for (RSSItem ourItem : newItems)
            {
                RSSItemAdapter itemAdapter = (RSSItemAdapter) ourItem;
                syndItems.add(itemAdapter.getSyndEntry());
            }
         }
 
         // We're storing values from a genericized collection into a
         // non-genericized collection. We have to copy the items to a new
         // collection. Use of a Collection<Object> avoids a compiler
         // "unchecked cast" warning.
 
         syndFeed.setEntries(new ArrayList<Object> (syndItems));
     }
 
     /**
      * Remove an item from the set of items.
      *
      * @param item  the item to remove
      *
      * @return <tt>true</tt> if removed, <tt>false</tt> if not found
      */
     public boolean removeItem (RSSItem item)
     {
         Collection<RSSItem> items = getItems();
         boolean removed = items.remove(item);
         setItems(items);
         return removed;
     }
 
     /**
      * Get the channel's title
      *
      * @return the channel's title, or null if there isn't one
      *
      * @see #setTitle(String)
      */
     public String getTitle()
     {
         // Rome leaves leading, trailing and embedded newlines in place.
         // While this is syntactically okay, curn prefers the description
         // to be one long line. ParserUtil.normalizeCharacterData() strips
         // leading and trailing newlines, and converts embedded newlines to
         // spaces.
 
         return ParserUtil.normalizeCharacterData(syndFeed.getTitle());
     }
 
     /**
      * Set the channel's title
      *
      * @param newTitle the channel's title, or null if there isn't one
      *
      * @see #getTitle()
      */
     public void setTitle(String newTitle)
     {
         syndFeed.setTitle(newTitle);
     }
 
     /**
      * Get the channel's description
      *
      * @return the channel's description, or null if there isn't one
      */
     public String getDescription()
     {
         // Rome leaves leading, trailing and embedded newlines in place.
         // While this is syntactically okay, curn prefers the description
         // to be one long line. ParserUtil.normalizeCharacterData() strips
         // leading and trailing newlines, and converts embedded newlines to
         // spaces.
 
         return ParserUtil.normalizeCharacterData(syndFeed.getDescription());
     }
 
     /**
      * Set the channel's description
      *
      * @param desc the channel's description, or null if there isn't one
      *
      * @see #getDescription
      */
     public void setDescription (String desc)
     {
         syndFeed.setDescription(desc);
     }
 
     /**
      * Get the channel's published links.
      *
      * @return the collection of links, or an empty collection
      *
      * @see RSSChannel#getLink
      */
     public final Collection<RSSLink> getLinks()
     {
         // Since ROME doesn't support multiple links per feed, we have to
         // assume that this link is the link for the feed. Try to figure
         // out the MIME type, and default to the MIME type for an RSS feed.
         // Mark the feed as type "self".
 
         Collection<RSSLink> results = new ArrayList<RSSLink>();
 
         try
         {
             URL url = new URL(syndFeed.getLink());
             results.add(new RSSLink(url,
                                     ParserUtil.getLinkMIMEType (url),
                                     RSSLink.Type.SELF));
         }
 
         catch (MalformedURLException ex)
         {
             log.error("Bad channel URL \"" + syndFeed.getLink() +
                       "\" from underlying parser: " + ex.toString());
         }
 
         return results;
     }
     /**
      * Set the channel's list of published links (its URLs).
      *
      * @param links the links
      *
      * @see #getLink
      * @see #getLinks
      */
     public void setLinks(Collection<RSSLink> links)
     {
         // Since ROME doesn't support multiple links per feed, we have
         // to assume that the first link is the link for the feed.
 
         if ((links != null) && (links.size() > 0))
         {
             RSSLink link = links.iterator().next();
             this.syndFeed.setLink(link.getURL().toExternalForm());
         }
     }
 
 
     /**
      * Get the channel's publication date.
      *
      * @return the date, or null if not available
      */
     public Date getPublicationDate()
     {
         return syndFeed.getPublishedDate();
     }
 
     /**
      * Set the channel's publication date.
      *
      * @param date  the publication date, or null if not available
      */
     public void setPublicationDate(Date date)
     {
         this.syndFeed.setPublishedDate(date);
     }
 
     /**
      * Get the channel's copyright string
      *
      * @return the copyright string, or null if not available
      */
     public String getCopyright()
     {
         return syndFeed.getCopyright();
     }
 
     /**
      * Set the channel's copyright string
      *
      * @param copyright  the copyright string, or null if not available
      *
      * @see #getCopyright
      */
     public void setCopyright(String copyright)
     {
         this.syndFeed.setCopyright(copyright);
     }
 
     /**
      * Get the RSS format the channel is using.
      *
      * @return the format, or null if not available
      */
     public String getRSSFormat()
     {
         return syndFeed.getFeedType();
     }
 
     /**
      * Get the RSS format the channel is using, in native format. This
      * method exists for underlying implementations that store the RSS
      * format as something other than a string; the method allows the
      * {@link #makeCopy} method to copy the RSS format without knowing
      * how it's stored. The default implementation of this method
      * simply calls {@link #getRSSFormat}.
      *
      * @return the format, or null if not available
      *
      * @see #getRSSFormat
      * @see #setNativeRSSFormat
      */
     public Object getNativeRSSFormat()
     {
         return getRSSFormat();
     }
 
     /**
      * Set the RSS format the channel is using.
      *
      * @param format the format, or null if not available
      *
      * @see #getRSSFormat
      * @see #getNativeRSSFormat
      */
     public void setNativeRSSFormat(Object format)
     {
         ((SyndFeedImpl) this.syndFeed).setFeedType((String) format);
     }
 
     /**
      * Get the channel's author list.
      *
      * @return the authors, or null (or an empty <tt>Collection</tt>) if
      *         not available
      *
      * @see #addAuthor
      * @see #clearAuthors
      */
     public Collection<String> getAuthors()
     {
         // ROME supports only one author per feed.
 
         Collection<String> result = null;
         String author = syndFeed.getAuthor();
         if (author != null)
             result = Collections.singleton(syndFeed.getAuthor());
         return result;
     }
 
     /**
      * Add to the channel's author list.
      *
      * @param author  another author string to add
      *
      * @see #getAuthors
      * @see #clearAuthors
      */
     public void addAuthor(String author)
     {
         // ROME supports only one author per feed. So, if the author field
         // is already set, don't overwrite it. (Assumes the first author is
         // the primary author.)
 
         if (syndFeed.getAuthor() == null)
             syndFeed.setAuthor(author);
     }
 
     /**
      * Clear the authors list.
      *
      * @see #getAuthors
      * @see #addAuthor
      */
     public void clearAuthors()
     {
         // Rome does not support this field
     }
 }
