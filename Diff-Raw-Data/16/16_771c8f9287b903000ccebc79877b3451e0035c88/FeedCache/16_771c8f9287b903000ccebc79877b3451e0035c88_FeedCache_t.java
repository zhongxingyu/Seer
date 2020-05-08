 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004 Brian M. Clapper. All rights reserved.
 
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
 
 package org.clapper.curn;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InvalidClassException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OptionalDataException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StreamCorruptedException;
 import java.io.Writer;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import org.clapper.curn.util.Util;
 
 import org.clapper.util.io.XMLWriter;
 import org.clapper.util.logging.Logger;
 
 /**
  * Defines the in-memory format of the <i>curn</i> cache, and provides
  * methods for saving and restoring the cache.
  *
  * @see Curn
  * @see org.clapper.curn.parser.RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public class FeedCache implements Serializable
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String XML_ROOT_ELEMENT_TAG       = "curn_cache";
     private static final String XML_ROOT_ATTR_TIMESTAMP    = "time_written";
     private static final String XML_ENTRY_ELEMENT_TAG      = "cache_entry";
     private static final String XML_ENTRY_ATTR_TIMESTAMP   = "timestamp";
     private static final String XML_ENTRY_ATTR_CHANNEL_URL = "channel_URL";
     private static final String XML_ENTRY_ATTR_ENTRY_URL   = "entry_URL";
     private static final String XML_ENTRY_ATTR_ENTRY_ID    = "entry_ID";
 
     /*----------------------------------------------------------------------*\
                               Private Classes
     \*----------------------------------------------------------------------*/
 
     private class FeedCacheMap extends HashMap<String, FeedCacheEntry>
     {
         FeedCacheMap()
         {
             super();
         }
     }
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * The configuration
      */
     private ConfigFile config;
 
     /**
      * The actual cache, indexed by unique ID.
      */
     private FeedCacheMap cacheByID;
 
     /**
      * Alternate cache (not saved, but regenerated on the fly), indexed
      * by URL.
      */
     private FeedCacheMap cacheByURL;
 
     /**
      * Whether or not the cache has been modified since saved or loaded
      */
     private boolean modified = false;
 
     /**
      * Current time
      */
     private long currentTime = System.currentTimeMillis();
 
     /**
      * For log messages
      */
     private static Logger log = new Logger (FeedCache.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Construct a new, empty cache object.
      *
      * @param config  the <i>curn</i> configuration
      */
     FeedCache (ConfigFile config)
     {
         this.config = config;
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Load the cache from the file specified in the configuration. If the
      * file doesn't exist, this method quietly returns.
      *
      * @throws CurnException  unable to read cache
      */
     public void loadCache()
         throws CurnException
     {
         File cacheFile = config.getCacheFile();
         this.cacheByURL = new FeedCacheMap();
 
         log.debug ("Reading cache from \"" + cacheFile.getPath() + "\"");
 
         if (! cacheFile.exists())
         {
             log.debug ("Cache \"" + cacheFile.getPath() + "\" doesn't exist.");
             this.cacheByID  = new FeedCacheMap();
         }
 
         else
         {
             // First, try as an old-style serialized cache. If that fails,
             // try as a new-style XML file. If both fail, then puke.
 
             this.cacheByID = readSerializedObjectsCache (cacheFile);
             if (this.cacheByID == null)
                 this.cacheByID = readSerializedXMLCache (cacheFile);
 
             if (this.cacheByID == null)
             {
                 throw new CurnException (Curn.BUNDLE_NAME,
                                          "FeedCache.badCacheFile",
                                          "Unable to load cache file \"{0}\" "
                                        + "as either a file of serialized Java "
                                        + "objects or an XML file. Punting.",
                                          new Object[] {cacheFile.getPath()});
             }
 
             if (log.isDebugEnabled())
                 dumpCache ("before pruning");
             pruneCache();
             if (log.isDebugEnabled())
                 dumpCache ("after pruning");
             modified = false;
         }
     }
 
     /**
      * Attempt to save the cache back to disk. Does nothing if the cache
      * hasn't been modified since it was saved.
      *
      * @throws CurnException  unable to write cache
      */
     public void saveCache()
         throws CurnException
     {
         if (this.modified)
         {
             File cacheFile = config.getCacheFile();
             /*
             try
             {
                 ObjectOutputStream objOut = new ObjectOutputStream
                                            (new FileOutputStream (cacheFile));
 
                 log.debug ("Saving cache to \"" + cacheFile.getPath() + "\"");
                 objOut.writeObject (cacheByID);
                 objOut.close();
                 this.modified = false;
             }
 
             catch (IOException ex)
             {
                 throw new CurnException (ex);
             }
             */
 
             try
             {
                 log.debug ("Saving cache to \"" + cacheFile.getPath() + "\"");
 
                 // Create the DOM.
 
                 DocumentBuilderFactory docFactory;
                 DocumentBuilder        docBuilder;
                 Document               dom;
 
                 docFactory = DocumentBuilderFactory.newInstance();
                 docBuilder = docFactory.newDocumentBuilder();
                 dom        = docBuilder.newDocument();
 
                 Element root = dom.createElement (XML_ROOT_ELEMENT_TAG);
                 root.setAttribute (XML_ROOT_ATTR_TIMESTAMP,
                                    String.valueOf (System.currentTimeMillis()));
                 dom.appendChild (root);
 
                 for (String id : cacheByID.keySet())
                 {
                     FeedCacheEntry entry = cacheByID.get (id);
 
                     Element e = dom.createElement (XML_ENTRY_ELEMENT_TAG);
 
                     e.setAttribute (XML_ENTRY_ATTR_TIMESTAMP,
                                     String.valueOf (entry.getTimestamp()));
                     e.setAttribute (XML_ENTRY_ATTR_ENTRY_ID,
                                     entry.getUniqueID());
                     e.setAttribute (XML_ENTRY_ATTR_ENTRY_URL,
                                     entry.getEntryURL().toString());
                     e.setAttribute (XML_ENTRY_ATTR_CHANNEL_URL,
                                     entry.getChannelURL().toString());
 
                     root.appendChild (e);
                 }
 
                 // Transform it to the output file.
 
                 TransformerFactory tf = TransformerFactory.newInstance();
                 Transformer transformer = tf.newTransformer();
                 transformer.transform (new DOMSource (dom),
                                        new StreamResult
                                           (new XMLWriter
                                              (new FileWriter (cacheFile))));
             }
 
             catch (IOException ex)
             {
                 throw new CurnException (ex);
             }
 
             catch (ParserConfigurationException ex)
             {
                 throw new CurnException (ex);
             }
 
             catch (TransformerException ex)
             {
                 throw new CurnException (ex);
             }
         }
     }
 
     /**
      * Determine whether the cache contains an entry with the specified
      * unique ID.
      *
      * @param id  the ID to check.
      *
      * @return <tt>true</tt> if the ID is present in the cache,
      *         <tt>false</tt> if not
      */
     public boolean containsID (String id)
     {
         boolean hasKey = cacheByID.containsKey (id);
         log.debug ("Cache contains \"" + id + "\"? " + hasKey);
         return hasKey;
     }
 
     /**
      * Determine whether the cache contains the specified URL.
      *
      * @param url  the URL to check. This method normalizes it.
      *
      * @return <tt>true</tt> if the ID is present in the cache,
      *         <tt>false</tt> if not
      */
     public boolean containsURL (URL url)
     {
         String  urlKey = Util.urlToLookupKey (url);
         boolean hasURL = cacheByURL.containsKey (urlKey);
         log.debug ("Cache contains \"" + urlKey + "\"? " + hasURL);
         return hasURL;
     }
 
     /**
      * Get an item from the cache by its unique ID.
      *
      * @param id  the unique ID to check
      *
      * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
      *         not found
      */
     public FeedCacheEntry getItem (String id)
     {
         return (FeedCacheEntry) cacheByID.get (id);
     }
 
     /**
      * Get an item from the cache by its URL.
      *
      * @param url the URL
      *
      * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
      *         not found
      */
     public FeedCacheEntry getItemByURL (URL url)
     {
         return (FeedCacheEntry) cacheByURL.get (Util.urlToLookupKey (url));
     }
 
     /**
      * Add (or replace) a cached URL.
      *
      * @param uniqueID   the unique ID string for the cache entry, or null.
      *                   If null, the URL is used as the unique ID.
      * @param url        the URL to cache. May be an individual item URL, or
      *                   the URL for an entire feed.
      * @param parentFeed the associated feed
      *
      * @see Util#normalizeURL
      */
     public synchronized void addToCache (String   uniqueID,
                                          URL      url,
                                          FeedInfo parentFeed)
     {
         String urlKey = Util.urlToLookupKey (url);
 
         if (uniqueID == null)
             uniqueID = urlKey;
 
         URL parentURL = parentFeed.getURL();
         FeedCacheEntry entry = new FeedCacheEntry (uniqueID,
                                                    parentURL,
                                                    url,
                                                    System.currentTimeMillis());
 
         log.debug ("Adding cache entry for URL \""
                   + entry.getEntryURL().toExternalForm()
                   + "\". ID=\""
                   + uniqueID
                   + "\", channel URL: \""
                   + entry.getChannelURL().toExternalForm()
                   + "\"");
         cacheByID.put (uniqueID, entry);
         cacheByURL.put (urlKey, entry);
         modified = true;
     }
 
     /**
      * Set the cache's notion of the current time, which affects how elements
      * are pruned when loaded from the cache. Only meaningful if set before
      * the <tt>loadCache()</tt> method is called. If this method is never
      * called, then the cache uses the current time.
      *
      * @param datetime  the time to use
      */
     public void setCurrentTime (Date datetime)
     {
         this.currentTime = datetime.getTime();
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Attempt to load the specified cache file as an old-style file
      * of serialized Java objects. Logs, but does not throw any exceptions.
      *
      * @param cacheFile  the file to read
      *
      * @return a deserialized FeedCacheMap on success, or null on failure
      */
     private FeedCacheMap readSerializedObjectsCache (File cacheFile)
     {
        ObjectInputStream  objIn         = null;
        FeedCacheMap       result        = null;
        String             cacheFilePath = cacheFile.getPath();
 
         try
         {
             log.info ("Attempting to load \""
                     + cacheFilePath
                      + "\" as an old-style file of serialized Java objects.");
             objIn = new ObjectInputStream (new FileInputStream (cacheFile));
             HashMap map = (HashMap) objIn.readObject();
 
             result = new FeedCacheMap();
             for (Iterator it = map.keySet().iterator(); it.hasNext(); )
             {
                 String key = (String) it.next();
                 result.put (key, (FeedCacheEntry) map.get (key));
             }
 
            log.warn ("Loaded old-style cache \""
                    + cacheFilePath
                    + "\". If cache updating is enabled, the cache will "
                    + "automatically be converted to a new-style XML cache.");
         }
 
         catch (ClassNotFoundException ex)
         {
             log.info ("Failed to load cache as serialized Java objects", ex);
         }
 
         catch (InvalidClassException ex)
         {
             log.info ("Failed to load cache as serialized Java objects", ex);
         }
 
         catch (StreamCorruptedException ex)
         {
             log.info ("Failed to load cache as serialized Java objects", ex);
         }
 
         catch (OptionalDataException ex)
         {
             log.info ("Failed to load cache as serialized Java objects", ex);
         }
 
         catch (IOException ex)
         {
             log.info ("Failed to load cache as serialized Java objects", ex);
         }
 
         finally
         {
             if (objIn != null)
             {
                 try
                 {
                     objIn.close();
                 }
 
                 catch (IOException ex)
                 {
                     log.error ("Failed to close cache file", ex);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Attempt to load the specified cache file as an old-style file
      * of serialized Java objects. Logs, but does not throw any exceptions.
      *
      * @param cacheFile  the file to read
      *
      * @return a deserialized FeedCacheMap on success, or null on failure
      *
      * @throws CurnException file is XML, but there's something wrong with it
      */
     private FeedCacheMap readSerializedXMLCache (File cacheFile)
         throws CurnException
     {
         FeedCacheMap result        = null;
         String       cacheFilePath = cacheFile.getPath();
         long         fileModTime   = cacheFile.lastModified();
 
         // First, parse the XML file into a DOM.
 
         try
         {
             DocumentBuilderFactory docFactory;
             DocumentBuilder        docBuilder;
             Document               dom;
 
             docFactory = DocumentBuilderFactory.newInstance();
 
             try
             {
                 docBuilder = docFactory.newDocumentBuilder();
             }
 
             catch (ParserConfigurationException ex)
             {
                 throw new CurnException (ex);
             }
 
             log.info ("Attempting to parse \""
                     + cacheFilePath
                     + "\" as XML.");
 
             dom = docBuilder.parse (new InputSource
                                        (new FileReader (cacheFile)));
 
             log.debug ("XML parse succeeded.");
 
             // Get the top-level element and verify that it's the one
             // we want.
 
             Element root = dom.getDocumentElement();
             String rootTagName = root.getTagName();
 
             if (! rootTagName.equals (XML_ROOT_ELEMENT_TAG))
             {
                 throw new CurnException (Curn.BUNDLE_NAME,
                                          "FeedCache.nonCacheXML",
                                          "File \"{0}\" is not a curn XML "
                                        + "cache file. The root XML element is "
                                        + "<{1}>, not the expected <{2}>.",
                                          new Object[]
                                          {
                                              cacheFilePath,
                                              rootTagName,
                                              XML_ROOT_ELEMENT_TAG
                                          });
             }
 
             // Okay, it's a curn cache. Start traversing the child nodes,
             // parsing each cache entry.
 
             result = new FeedCacheMap();
 
             NodeList childNodes = root.getChildNodes();
             for (int i = 0; i < childNodes.getLength(); i++)
             {
                 Node childNode = childNodes.item (i);
 
                 // Skip non-element nodes (like text).
 
                 if (childNode.getNodeType() != Node.ELEMENT_NODE)
                     continue;
 
                 String nodeName = childNode.getNodeName();
                 if (! nodeName.equals (XML_ENTRY_ELEMENT_TAG))
                 {
                     log.warn ("Skipping unexpected XML element <"
                             + nodeName
                             + "> in curn XML cache file \""
                             + cacheFilePath
                             + "\".");
                     continue;
                 }
 
                 FeedCacheEntry entry = parseXMLCacheEntry (childNode);
                 if (entry != null)
                     result.put (entry.getUniqueID(), entry);
             }
         }
 
         catch (SAXException ex)
         {
             throw new CurnException (Curn.BUNDLE_NAME,
                                      "FeedCache.xmlParseFailure",
                                      "Unable to parse cache file \"{0}\" "
                                    + "as an XML file.",
                                      new Object[] {cacheFilePath},
                                      ex);
         }
 
         catch (IOException ex)
         {
             throw new CurnException (Curn.BUNDLE_NAME,
                                      "FeedCache.xmlParseFailure",
                                      "Unable to parse cache file \"{0}\" "
                                    + "as an XML file.",
                                      new Object[] {cacheFilePath},
                                      ex);
         }
 
         return result;
     }
 
     /**
      * Parse an XML feed cache entry.
      *
      * @param node  the XML node for the feed cache entry
      *
      * @return the FeedCacheEntry, or null on error
      */
     private FeedCacheEntry parseXMLCacheEntry (Node node)
     {
         // Parse out the attributes.
 
         NamedNodeMap attrs = node.getAttributes();
         String entryID = getXMLAttribute (attrs,
                                           XML_ENTRY_ATTR_ENTRY_ID,
                                           null);
         String sChannelURL = getXMLAttribute (attrs,
                                               XML_ENTRY_ATTR_CHANNEL_URL,
                                               entryID);
         String sEntryURL = getXMLAttribute (attrs,
                                            XML_ENTRY_ATTR_ENTRY_URL,
                                            entryID);
         String sTimestamp = getXMLAttribute (attrs,
                                              XML_ENTRY_ATTR_TIMESTAMP,
                                              entryID);
 
         if ((entryID == null) ||
             (sChannelURL == null) ||
             (sEntryURL == null) ||
             (sTimestamp == null))
         {
             // Error(s) already logged.
 
             return null;
         }
 
         // Parse the timestamp.
 
         long timestamp = 0;
         try
         {
             timestamp = Long.parseLong (sTimestamp);
         }
 
         catch (NumberFormatException ex)
         {
             log.error ("Bad timestamp value of \""
                      + sTimestamp
                      + "\" for <"
                      + XML_ENTRY_ELEMENT_TAG
                      + "> with unique ID \""
                      + entryID
                      + "\". Skipping entry.");
             return null;
         }
 
         // Parse the URLs.
 
         URL channelURL = null;
         try
         {
             channelURL = new URL (sChannelURL);
         }
 
         catch (MalformedURLException ex)
         {
             log.error ("Bad channel URL \""
                      + sChannelURL
                      + "\" for <"
                      + XML_ENTRY_ELEMENT_TAG
                      + "> with unique ID \""
                      + entryID
                      + "\". Skipping entry.");
             return null;
         }
 
         URL entryURL = null;
         try
         {
             entryURL = new URL (sEntryURL);
         }
 
         catch (MalformedURLException ex)
         {
             log.error ("Bad entry URL \""
                      + sEntryURL
                      + "\" for <"
                      + XML_ENTRY_ELEMENT_TAG
                      + "> with unique ID \""
                      + entryID
                      + "\". Skipping entry.");
             return null;
         }
 
         return new FeedCacheEntry (entryID, channelURL, entryURL, timestamp);
     }
 
     /**
      * Prune the loaded cache of out-of-date data.
      */
     private void pruneCache()
     {
         log.debug ("PRUNING CACHE");
         log.debug ("Cache's notion of current time: "
                  + new Date (currentTime));
 
         for (Iterator itKeys = cacheByID.keySet().iterator();
              itKeys.hasNext(); )
         {
             String itemKey = (String) itKeys.next();
             FeedCacheEntry entry = (FeedCacheEntry) cacheByID.get (itemKey);
             URL channelURL = entry.getChannelURL();
             boolean removed = false;
 
             if (log.isDebugEnabled())
                 dumpCacheEntry (itemKey, entry, "");
 
             FeedInfo feedInfo = config.getFeedInfoFor (channelURL);
 
             if (feedInfo == null)
             {
                 // Cached URL no longer corresponds to a configured site
                 // URL. Kill it.
 
                 log.debug ("Cached item \""
                          + itemKey
                          + "\", with base URL \""
                          + channelURL.toString()
                          + "\" no longer corresponds to a configured feed. "
                          + "Tossing it.");
                 itKeys.remove();
                 removed = true;
             }
 
             else
             {
                 long timestamp  = entry.getTimestamp();
                 long maxCacheMS = feedInfo.getMillisecondsToCache();
                 long expires    = timestamp + maxCacheMS;
 
                 if (log.isDebugEnabled())
                 {
                     log.debug ("    Cache time: "
                              + feedInfo.getDaysToCache()
                              + " days ("
                              + maxCacheMS
                              + " ms)");
                     log.debug ("    Expires: "
                              + new Date (expires).toString());
                 }
 
                 if (timestamp > currentTime)
                 {
                     log.debug ("Cache time for item \""
                              + itemKey
                              + "\" is in the future, relative to cache's "
                              + "notion of current time. Setting its "
                              + "timestamp to the current time.");
                     entry.setTimestamp (currentTime);
                     this.modified = true;
                 }
 
                 else if (expires < currentTime)
                 {
                     log.debug ("Cache time for item \""
                              + itemKey
                              + "\" has expired. Deleting cache entry.");
                     itKeys.remove();
                     this.modified = true;
                     removed = true;
                 }
             }
 
             if (! removed)
             {
                 // Add to URL cache.
 
                 String urlKey = Util.urlToLookupKey (entry.getEntryURL());
                 log.debug ("Loading URL \""
                          + urlKey
                          + "\" into in-memory lookup cache.");
                 cacheByURL.put (urlKey, entry);
             }
         }
 
         log.debug ("DONE PRUNING CACHE");
     }
 
     /**
      * Dump the contents of the cache, via the "debug" log facility.
      *
      * @param label  a label, or initial message, to identify the dump
      */
     private void dumpCache (String label)
     {
         log.debug ("CACHE DUMP: " + label);
         Set<String> sortedKeys = new TreeSet<String> (cacheByID.keySet());
         for (String itemKey : sortedKeys)
             dumpCacheEntry (itemKey, (FeedCacheEntry) cacheByID.get (itemKey), "");
     }   
 
     /**
      * Dump a single cache entry via the "debug" log facility.
      *
      * @param itemKey the hash table key for the item
      * @param entry   the cache entry
      * @param indent  string to use to indent output, if desired
      */
     private void dumpCacheEntry (Object         itemKey,
                                  FeedCacheEntry entry,
                                  String         indent)
     {
         long timestamp  = entry.getTimestamp();
 
         if (indent == null)
             indent = "";
 
         log.debug (indent + "Cached item \"" + itemKey.toString() + "\"");
         log.debug (indent + "    Item URL: " + entry.getEntryURL().toString());
         log.debug (indent + "    Channel URL: "
                  + entry.getChannelURL().toString());
         log.debug (indent + "    Cached on: "
                  + new Date (timestamp).toString());
     }
 
     /**
      * Retrieve an XML attribute value from a list of attributes. The
      * attribute is assumed to be required. If it's missing, the error is
      * logged (but an exception is not thrown).
      *
      * @param attrs   the list of attributes
      * @param name    the attribute name
      * @param entryID entry ID string, if available, for errors
      *
      * @return the attribute's value, or null if the attribute wasn't found
      *
      * @throws DOMException  DOM parsing error
      */
     private String getXMLAttribute (NamedNodeMap attrs,
                                     String       name,
                                     String       entryID)
         throws DOMException
     {
         Node attr = attrs.getNamedItem (name);
         String value = null;
 
         if (attr == null)
         {
             if (entryID == null)
                 entryID = "?";
 
             log.error ("<" + XML_ENTRY_ELEMENT_TAG + "> is missing required " +
                        "\"" + name + "\" XML attribute.");
         }
 
         else
         {
             assert (attr.getNodeType() == Node.ATTRIBUTE_NODE);
             value = attr.getNodeValue();
         }
 
         return value;
     }
 }
