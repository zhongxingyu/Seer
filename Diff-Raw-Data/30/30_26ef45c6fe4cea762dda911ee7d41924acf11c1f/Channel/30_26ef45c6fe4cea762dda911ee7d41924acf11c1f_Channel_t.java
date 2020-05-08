 /*---------------------------------------------------------------------------*\
   $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.rssget.parser.minirss;
 
 import org.clapper.rssget.parser.RSSChannel;
 import org.clapper.rssget.parser.RSSItem;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * This class contains a subset of standard RSS channel data, providing
  * only the methods necessary for <i>rssget</i> to work.
  *
 * @see org.clapper.rssget.parser.RSSParserFactory
 * @see org.clapper.rssget.parser.RSSParser
  * @see RSSItem
  *
  * @version <tt>$Revision$</tt>
  */
 public class Channel implements RSSChannel
 {
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private Collection  items       = new ArrayList();
     private String      title       = null;
     private String      description = null;
     private URL         url         = null;
     private Date        pubDate     = null;
     private String      rssFormat   = null;
     private String      copyright   = null;
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Constructor. Objects of this type can only be created within this
      * package.
      */
     Channel()
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get a <tt>Collection</tt> of the items in this channel. All objects
      * in the collection are of type <tt>RSSItem</tt>.
      *
      * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
      *         The collection will be empty (never null) if there are no
      *         items. This <tt>Collection</tt> is a copy of the underlying
      *         <tt>collection</tt> being used.
      */
     public Collection getItems()
     {
         Collection result = new ArrayList();
 
         result.addAll (items);
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
     public void setItems (Collection newItems)
     {
         items.clear();
         items.addAll (newItems);
     }
 
     /**
      * Add an item.
      *
      * @param item  item to add
      */
     public void addItem (Item item)
     {
         items.add (item);
     }
 
     /**
      * Get the channel's title
      *
      * @return the channel's title, or null if there isn't one
      */
     public String getTitle()
     {
         return title;
     }
 
     /**
      * Set the channel's title
      *
      * @param title the channel's title, or null if there isn't one
      */
     public void setTitle (String title)
     {
         this.title = title;
     }
 
     /**
      * Get the channel's description
      *
      * @return the channel's description, or null if there isn't one
      */
     public String getDescription()
     {
         return description;
     }
 
     /**
      * Set the channel's description
      *
      * @param description the channel's description, or null if there isn't one
      */
     public void setDescription (String description)
     {
         this.description = description;
     }
 
     /**
      * Get the channel's published URL.
      *
      * @return the URL, or null if not available
      */
     public URL getLink()
     {
         return url;
     }
 
     /**
      * Set the channel's published URL.
      *
      * @param url  the URL, as a string
      *
      * @throws MalformedURLException  bad URL
      */
     public void setLink (String url)
         throws MalformedURLException
     {
         this.url = new URL (url);
     }
 
     /**
      * Get the channel's publication date.
      *
      * @return the date, or null if not available
      */
     public Date getPublicationDate()
     {
         return pubDate;
     }
 
     /**
      * Set the channel's publication date.
      *
      * @param date the date, or null if not available
      */
     public void setPublicationDate (Date date)
     {
         this.pubDate = date;
     }
 
     /**
      * Get the channel's copyright string
      *
      * @return the copyright string, or null if not available
      */
     public String getCopyright()
     {
         return copyright;
     }
 
     /**
      * Set the copyright string for the channel
      *
      * @param copyright the copyright string, or null if not available
      */
     public void setCopyright (String copyright)
     {
         this.copyright = copyright;
     }
 
     /**
      * Get the RSS format the channel is using.
      *
      * @return the format, or null if not available
      */
     public String getRSSFormat()
     {
         return rssFormat;
     }
 
     /**
      * Set the RSS format the channel is using.
      *
      * @param format the format, or null if not available
      */
     public void setRSSFormat (String format)
     {
         this.rssFormat = format;
     }
 }
