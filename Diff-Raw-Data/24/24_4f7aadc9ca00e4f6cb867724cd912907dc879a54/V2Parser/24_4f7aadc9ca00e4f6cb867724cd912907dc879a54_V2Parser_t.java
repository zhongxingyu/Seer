 /*---------------------------------------------------------------------------*\
   $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.rssget.parser.minirss;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.Stack;
 import java.util.Date;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 
 /**
  * <p><tt>V2Parser</tt> is a stripped down RSS parser for RSS versions
  * 0.91, 0.92 and 2. It's intended to be invoked once a
  * <tt>MiniRSSParser</tt> has determined whether the XML feed represents
  * version 1 RSS or not. For <i>rssget</i>'s purposes, there's considerable
  * similarity between RSS version 0.91 and RSS version 2--so much so that
  * the same parser logic will work for both. (The same cannot be said for
  * RSS version 1, which uses a different enough syntax to require a
  * separate parser.)</p>
  *
  * <p>This parser doesn't store all the possible RSS items. It stores
  * those items that the <i>rssget</i> utility requires (plus a few more),
  * but lacks support for others. For instance, it ignores <tt>image</tt>,
  * <tt>cloud</tt>, <tt>textinput</tt> and other elements that <i>rssget</i>
 * has no interest in displaying.</p>
  *
  * @version <tt>$Revision$</tt>
 *
 * @see MiniRSSParser
 * @see V1Parser
  */
 public class V2Parser extends ParserCommon
 {
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Creates a new <tt>V2Parser</tt> to parse the remainder of an RSS
      * XML file, filling the specified <tt>Channel</tt> object with the
      * parsed contents.
      *
      * @param channel      the <tt>Channel</tt> to be filled
      * @param firstElement the first element in the file, which was already
      *                     parsed by a <tt>MiniRSSParser</tt> object, before
      *                     it handed control to this object
      */
     V2Parser (Channel channel, String elementName)
     {
         super (channel);
         elementStack.push (new ElementStackEntry (elementName, elementName));
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
                         Overriding XMLReaderAdapter
     \*----------------------------------------------------------------------*/
 
     /**
      * Handle the start of an XML element.
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
         ElementStackEntry entry = (ElementStackEntry) elementStack.peek();
         Object container = entry.getContainer();
 
         if (elementName.equals ("channel"))
             startChannel (elementName, attributes);
 
         else if (container instanceof Channel)
             startChannelSubelement (elementName, attributes, entry);
 
         else if (container instanceof Item)
             startItemSubelement (elementName, attributes, entry);
     }
 
     /**
      * Handle the end of an XML element.
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
      *
      * @throws SAXException parsing error
      */
     public void endElement (String namespaceURI,
                             String namespaceLocalName,
                             String elementName)
         throws SAXException
     {
         if (elementStack.empty())
             throw new SAXException ("(BUG) Empty stack at end of element.");
 
         ElementStackEntry entry = (ElementStackEntry) elementStack.pop();
         String entryElementName = entry.getElementName();
 
         if (! entryElementName.equals (elementName))
         {
             throw new SAXException ("Element \""
                                   + elementName
                                   + "\" doesn't match element on stack (\""
                                   + entry.getElementName()
                                   + "\")");
         }
 
         Object container = entry.getContainer();
 
         if (container != null)
         {
             if (container instanceof Channel)
                 endChannelElement (elementName, entry);
 
             else if (container instanceof Item)
                 endItemElement (elementName, entry);
         }
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Handle the start of the channel element.
      *
      * @param elementName  the element name, which is pushed onto the stack
      *                     along with the <tt>Channel</tt> object
      * @param attributes   the attributes (currently not used)
      *
      * @throws SAXException on error
      */
     private void startChannel (String     elementName,
                                Attributes attributes)
         throws SAXException
     {
         elementStack.push (new ElementStackEntry (elementName, channel));
     }
 
     /**
      * Handle the start of an XML element that's nested within a "channel"
      * element.
      *
      * @param elementName       the element name, which is pushed onto the
      *                          stack along with the <tt>Channel</tt> object
      * @param attributes        the attributes (currently not used)
      * @param parentStackEntry  the stack entry for the parent channel element
      *
      * @throws SAXException on error
      */
     private void startChannelSubelement (String            elementName,
                                          Attributes        attributes,
                                          ElementStackEntry parentStackEntry)
         throws SAXException
     {
         Channel channel = (Channel) parentStackEntry.getContainer();
 
         if (elementName.equals ("item"))
         {
             Item item = new Item();
 
             channel.addItem (item);
             elementStack.push (new ElementStackEntry (elementName, item));
         }
 
         else
         {
             elementStack.push (new ElementStackEntry (elementName, channel));
         }
     }
 
     /**
      * Handles the end of a channel element. This includes the channel
      * element itself and any nested channel elements. This method should
      * only be called with the popped stack entry is known to have
      * a <tt>Channel</tt> object in its <tt>container</tt> data field.
      *
      * @param elementName the name of the XML element being ended
      * @param stackEntry  the associated (popped) stack entry
      *
      * @throws SAXException on error
      */
     private void endChannelElement (String            elementName,
                                     ElementStackEntry stackEntry)
         throws SAXException
     {
         Channel channel = (Channel) stackEntry.getContainer();
         String  chars   = stackEntry.getCharBuffer().toString().trim();
 
         if (chars.length() == 0)
             chars = null;
 
         try
         {
             if (elementName.equals ("title"))
                 channel.setTitle (chars);
 
             else if (elementName.equals ("link"))
                 channel.setLink (chars);
 
             else if (elementName.equals ("description"))
                 channel.setDescription (chars);
 
             else if (elementName.equals ("pubDate"))
                 channel.setPublicationDate (parseRFC822Date (chars));
 
             else if (elementName.equals ("copyright"))
                 channel.setCopyright (chars);
         }
 
         catch (MalformedURLException ex)
         {
             throw new SAXException (ex.toString());
         }
     }
 
     /**
      * Handle the start of an XML element that's nested within an "item"
      * element.
      *
      * @param elementName       the element name, which is pushed onto the
      *                          stack along with the <tt>Item</tt> object
      * @param attributes        the attributes (currently not used)
      * @param parentStackEntry  the stack entry for the parent item element
      *
      * @throws SAXException on error
      */
     private void startItemSubelement (String            elementName,
                                       Attributes        attributes,
                                       ElementStackEntry parentStackEntry)
         throws SAXException
     {
         Item item = (Item) parentStackEntry.getContainer();
         elementStack.push (new ElementStackEntry (elementName, item));
     }
 
 
     /**
      * Handles the end of an item element. This includes the item
      * element itself and any nested item elements. This method should
      * only be called with the popped stack entry is known to have
      * a <tt>Item</tt> object in its <tt>container</tt> data field.
      *
      * @param elementName the name of the XML element being ended
      * @param stackEntry  the associated (popped) stack entry
      *
      * @throws SAXException on error
      */
     private void endItemElement (String            elementName,
                                  ElementStackEntry stackEntry)
         throws SAXException
     {
         Item    item  = (Item) stackEntry.getContainer();
         String  chars   = stackEntry.getCharBuffer().toString().trim();
 
         if (chars.length() == 0)
             chars = null;
 
         try
         {
             if (elementName.equals ("title"))
                 item.setTitle (chars);
 
             else if (elementName.equals ("link"))
                 item.setLink (new URL (chars));
 
             else if (elementName.equals ("description"))
                 item.setDescription (chars);
 
             else if (elementName.equals ("pubDate"))
                 item.setPublicationDate (parseRFC822Date (chars));
 
             else if (elementName.equals ("category"))
                 item.addCategory (chars);
         }
 
         catch (MalformedURLException ex)
         {
             throw new SAXException (ex.toString());
         }
     }
 }
