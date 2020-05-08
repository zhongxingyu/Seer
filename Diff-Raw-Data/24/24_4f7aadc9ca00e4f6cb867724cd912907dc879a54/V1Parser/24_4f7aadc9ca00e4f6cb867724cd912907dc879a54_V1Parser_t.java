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
  * <p><tt>V1Parser</tt> is a stripped down RSS parser for RSS version 1.
  * It's intended to be invoked once a <tt>MiniRSSParser</tt> has determined
  * whether the XML feed represents version 1 RSS or not.</p>
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
 * @see V2Parser
  */
 public class V1Parser extends ParserCommon
 {
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Creates a new <tt>V1Parser</tt> to parse the remainder of an RSS
      * XML file, filling the specified <tt>Channel</tt> object with the
      * parsed contents.
      *
      * @param channel      the <tt>Channel</tt> to be filled
      * @param firstElement the first element in the file, which was already
      *                     parsed by a <tt>MiniRSSParser</tt> object, before
      *                     it handed control to this object
      */
     V1Parser (Channel channel, String firstElement)
     {
         super (channel);
         elementStack.push (new ElementStackEntry (firstElement, firstElement));
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
         {
             startChannel (elementName, attributes);
         }
 
         else if (elementName.equals ("item"))
         {
             Item item = new Item();
 
             channel.addItem (item);
             elementStack.push (new ElementStackEntry (elementName, item));
         }
 
         else if (container instanceof Channel)
         {
             startChannelSubelement (elementName, attributes, entry);
         }
 
         else if (container instanceof Item)
         {
             startItemSubelement (elementName, attributes, entry);
         }
 
         else
         {
             elementStack.push (new ElementStackEntry (elementName,
                                                       elementName));
         }
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
      * @param attributes   the attributes
      *
      * @throws SAXException on error
      */
     private void startChannel (String     elementName,
                                Attributes attributes)
         throws SAXException
     {
         try
         {
             String url = attributes.getValue ("rdf:about");
 
             if (url != null)
                 channel.setLink (url);
 
             elementStack.push (new ElementStackEntry (elementName, channel));
         }
 
         catch (MalformedURLException ex)
         {
             throw new SAXException (ex.toString());
         }
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
 
         elementStack.push (new ElementStackEntry (elementName, channel));
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
         }
 
         catch (MalformedURLException ex)
         {
             throw new SAXException (ex.toString());
         }
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
 
         if (elementName.equals ("title"))
             item.setTitle (chars);
 
         else if (elementName.equals ("link"))
             setItemLink (item, chars);
 
         else if (elementName.equals ("description"))
             item.setDescription (chars);
     }
 
     /**
      * Some sites (notably, O'Reilly's Meerket) specify links with
      * additional cruft in them, e.g., "13027@http://www.gizmodo.com/".
      * This method is a hack that strips such things out before storing the
      * link in the item.
      *
      * @param item the item
      * @param url  the string containing the URL
      *
      * @throws SAXException on error
      */
     private void setItemLink (Item item, String url)
         throws SAXException
     {
         try
         {
             item.setLink (new URL (url));
         }
 
         catch (MalformedURLException ex)
         {
             // Scan forward, character by character, looking for a valid
             // protocol. Abort if (a) there's no ":" anywhere in the
             // string, or (b) we reach the ":" without finding a valid URL.
 
             int iColon = url.indexOf (":");
             if (iColon < 0)
             {
                 throw new SAXException ("Can't save item link \""
                                       + url.toString()
                                       + "\": "
                                       + ex.toString());
             }
 
             boolean ok = false;
             for (int i = 1; i < iColon; i++)
             {
                 try
                 {
                     item.setLink (new URL (url.substring (i)));
                     ok = true;
                     break;
                 }
 
                 catch (MalformedURLException ex2)
                 {
                 }
             }
 
             if (! ok)
             {
                 throw new SAXException ("Can't save item link \""
                                       + url.toString()
                                       + "\": "
                                       + ex.toString());
             }
         }
     }
 }
