 /*---------------------------------------------------------------------------*\
   $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.rssget.htmloutput;
 
 import org.clapper.rssget.OutputHandler;
 import org.clapper.rssget.RSSGetException;
 import org.clapper.rssget.Util;
 import org.clapper.rssget.RSSGetConfiguration;
 import org.clapper.rssget.parser.RSSChannel;
 import org.clapper.rssget.parser.RSSItem;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import java.util.Date;
 import java.util.Collection;
 import java.util.Iterator;
 
 import java.net.URL;
 
 import org.enhydra.xml.xmlc.html.*;
 import org.enhydra.xml.xmlc.XMLCUtil;
 import org.w3c.dom.*;
 import org.w3c.dom.html.*;
 
 /**
  * Provides an output handler that produces plain text to a
  * <tt>PrintWriter</tt>.
  *
 * @see OutputHandler
 * @see org.clapper.rssget.rssget
  * @see org.clapper.rssget.parser.RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public class HTMLOutputHandler implements OutputHandler
 {
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private PrintWriter           out                 = null;
     private RSSOutputHTML         doc                 = null;
     private String                oddItemRowClass     = null;
     private String                oddChannelClass     = null;
     private int                   rowCount            = 0;
     private int                   channelCount        = 0;
     private RSSGetConfiguration   config              = null;
     private HTMLTableRowElement   channelRow          = null;
     private HTMLTableRowElement   channelSeparatorRow = null;
     private Node                  channelRowParent    = null;
     private HTMLAnchorElement     itemAnchor          = null;
     private HTMLTableCellElement  itemTitleTD         = null;
     private HTMLTableCellElement  itemDescTD          = null;
     private HTMLTableCellElement  channelTD           = null;
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Construct a new <tt>HTMLOutputHandler</tt>.
      */
     public HTMLOutputHandler()
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Initializes the output handler for another set of RSS channels.
      *
      * @param writer  the <tt>PrintWriter</tt> where the handler should send
      *                output
      * @param config  the parsed <i>rssget</i> configuration data
      *
      * @throws RSSGetException  initialization error
      */
     public void init (PrintWriter         writer,
                       RSSGetConfiguration config)
         throws RSSGetException
     {
         this.doc                 = new RSSOutputHTML();
         this.config              = config;
         this.out                 = writer;
         this.oddChannelClass     = doc.getElementChannelTD().getClassName();
         this.oddItemRowClass     = doc.getElementItemTitleTD().getClassName();
         this.channelRow          = doc.getElementChannelRow();
         this.channelSeparatorRow = doc.getElementChannelSeparatorRow();
         this.channelRowParent    = channelRow.getParentNode();
         this.itemAnchor          = doc.getElementItemAnchor();
         this.itemTitleTD         = doc.getElementItemTitleTD();
         this.itemDescTD          = doc.getElementItemDescTD();
         this.channelTD           = doc.getElementChannelTD();
     }
 
     /**
      * Display the list of <tt>RSSItem</tt> news items to whatever output
      * is defined for the underlying class.
      *
      * @param channel The channel containing the items to emit. The method
      *                should emit all the items in the channel; the caller
      *                is responsible for clearing out any items that should
      *                not be seen.
      *
      * @throws RSSGetException  unable to write output
      */
     public void displayChannel (RSSChannel channel)
         throws RSSGetException
     {
         Collection items = channel.getItems();
 
         if (items.size() == 0)
             return;
 
         int       i = 0;
         Iterator  it;
 
         channelCount++;
 
         // Insert separator row first.
 
         channelRowParent.insertBefore (channelSeparatorRow.cloneNode (true),
                                        channelSeparatorRow);
         // Do the rows of output.
 
         for (i = 0, it = items.iterator(); it.hasNext(); i++, rowCount++)
         {
             RSSItem item = (RSSItem) it.next();
 
             if (i == 0)
             {
                 // First row in channel has channel title and link.
 
                 doc.setTextChannelTitle (channel.getTitle());
                 itemAnchor.setHref (item.getLink().toExternalForm());
             }
 
             else
             {
                 doc.setTextChannelTitle ("");
                 itemAnchor.setHref ("");
             }
 
             String desc  = item.getDescription();
             String title = item.getTitle();
 
             doc.setTextItemTitle ((title == null) ? "(No Title)" : title);
             doc.setTextItemDescription ((desc == null) ? "" : desc);
             itemAnchor.setHref (item.getLink().toExternalForm());
 
             itemTitleTD.removeAttribute ("class");
             itemDescTD.removeAttribute ("class");
 
             if ((rowCount % 2) == 1)
             {
                 // Want to use the "odd row" class to distinguish the
                 // rows. For the description, though, only do that if
                 // if's not empty.
 
                 itemTitleTD.setAttribute ("class", oddItemRowClass);
 
                 if (desc != null)
                     itemDescTD.setAttribute ("class", oddItemRowClass);
             }
 
             if ((channelCount % 2) == 1)
                 channelTD.setClassName (oddChannelClass);
             else
                 channelTD.setClassName ("");
 
             channelRowParent.insertBefore (channelRow.cloneNode (true),
                                            channelSeparatorRow);
         }
 
     }
     
     /**
      * Flush any buffered-up output. <i>rssget</i> calls this method
      * once, after calling <tt>displayChannelItems()</tt> for all channels.
      *
      * @throws RSSGetException  unable to write output
      */
     public void flush() throws RSSGetException
     {
         // Remove the cloneable row.
 
         removeElement (doc.getElementChannelRow());
 
         // Add configuration info, if available.
 
         doc.setTextVersion (Util.getVersion());
 
         URL configFileURL = config.getConfigurationFileURL();
         if (configFileURL == null)
             removeElement (doc.getElementConfigFileRow());
         else
             doc.setTextConfigURL (configFileURL.toString());
 
         // Write the document.
 
         out.print (doc.toDocument());
         out.flush();
 
         // Kill the document.
 
         doc = null;
     }
 
     /**
      * Get the content (i.e., MIME) type for output produced by this output
      * handler.
      *
      * @return the content type
      */
     public String getContentType()
     {
         return "text/html";
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Remove an element from the document.
      *
      * @param element the <tt>Node</tt> representing the element in the DOM
      */
     private void removeElement (Node element)
     {
         Node parentNode = element.getParentNode();
 
         if (parentNode != null)
             parentNode.removeChild (element);
     }
 }
