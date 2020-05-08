 /*---------------------------------------------------------------------------*\
  $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.output.freemarker;
 
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.SimpleDate;
 import freemarker.template.SimpleHash;
 import freemarker.template.SimpleNumber;
 import freemarker.template.SimpleSequence;
 import freemarker.template.Template;
 import freemarker.template.TemplateBooleanModel;
 import freemarker.template.TemplateException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Date;
 import org.clapper.curn.Constants;
 import org.clapper.curn.CurnConfig;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.Version;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 import org.clapper.curn.parser.RSSLink;
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.io.FileUtil;
 import org.clapper.util.logging.Logger;
 import org.clapper.util.text.TextUtil;
 
 /**
  * <p>Handles transforming parsed channel data via a FreeMarker template.
  * This class is used by the {@link FreeMarkerOutputHandler}, but it's also
  * available for use by plug-ins and other components.</p>
  *
  * <p>This class builds a FreeMarker data model; each call to
  * {@link #addChannel addChannel()} adds the data for a channel (i.e., feed)
  * to the data structure. When the {@link #transform transform()} method is
  * invoked, this handler loads the FreeMarker template and feeds it the
  * FreeMarker data model, producing the output. The FreeMarker template
  * can produce any kind of document; this class doesn't care.</p>
  *
  * <h3>The FreeMarker Data Model</h3>
  *
  * <p>This handler builds the following FreeMarker data model tree.</p>
  *
  * <pre>
  * <b>Tree</b>                                         <b>Description</b>
  *
  * (root)
  *  |
  *  +-- curn
  *  |    |
  *  |    +-- showToolInfo                       (boolean) whether or not
  *  |    |                                      to display curn information
  *  |    |                                      in the output
  *  |    |
  *  |    +-- version                            version of curn
  *  |    |
  *  |    +-- buildID                            curn's build ID
  *  |
  *  +-- totalItems                              total items for all channels
  *  |
  *  +-- dateGenerated                           date generated
  *  |
  *  +-- extraText                               extra text, from the config
  *  |
  *  +-- encoding                                encoding, from the config
  *  |
  *  +-- tableOfContents                         hash of TOC data
  *  |    |
  *  |    +-- needed                             whether a TOC is needed
  *  |    |
  *  |    +-- channels                           sequence of channel TOC
  *  |          |                                items
  *  |          |
  *  |          +-- (channel)                    TOC entry for one channel
  *  |                |
  *  |                +-- title                  channel title
  *  |                |
  *  |                +-- url                    channel URL (from the XML)
  *  |                |
  *  |                +-- configuredURL          channel/feed URL from the
  *  |                |                          config file
  *  |                |
  *  |                +-- totalItems             total items in channel
  *  |                |
  *  |                +-- channelAnchor          HTML anchor for channel
  *  |
  *  +-- channels                                sequence of channel (feed) data
  *         |
  *         +-- (channel)                        hash for a single channel (feed)
  *                 |
  *                 +-- index                    channel's index in list
  *                 |
  *                 +-- totalItems               total items in channel
  *                 |
  *                 +-- title                    channel title
  *                 |
  *                 +-- description              the channel's description, or
  *                                              "" if unavailable
  *                 |
  *                 +-- anchorName               HTML anchor for channel
  *                 |
  *                 +-- url                      channel's URL (as published in
  *                 |                            the feed)
  *                 |
  *                 +-- configuredURL            channel's URL (as configured to
  *                 |                            <i>curn</i>)
  *                 |
  *                 +-- id                       channel's unique ID
  *                 |
  *                 +-- date                     channel's last-modified date
  *                 |                            (might be missing)
  *                 |
  *                 |-- rssFormat                RSS format of channel (Atom,
  *                 |                            RSS 0.92, etc.)
  *                 |
  *                 +-- author                   all the authors, combined into
  *                 |                            a single string
  *                 |
  *                 |-- authors                  sequence of strings, each one
  *                 |                            denoting an author of the feed
  *                 |
  *                 +-- items                    sequence of channel items
  *                       |
  *                       +-- (item)             entry for one item
  *                             |
  *                             +-- index        item's index in channel
  *                             |
  *                             +-- title        item's title
  *                             |
  *                             +-- url          item's unique URL
  *                             |
  *                             +-- id           item's unique ID
  *                             |
  *                             +-- date         the date
  *                             |                (might be missing)
  *                             |
  *                             +-- author       the author or authors of the
  *                             |                item, combined in a single
  *                             |                string
  *                             |
  *                             +-- author       a sequence of individual
  *                             |                author name strings
  *                             |
  *                             +-- description  description/summary
  * </pre>
  *
  * <p>In addition, the data model provides (at the top level) the following
  * methods:</p>
  *
  * <pre>
  * (root)
  *  |
  *  +-- wrapText (string[, indentation[, lineLength]])
  *  |
  *  +-- indentText (string, indentation)
  *  |
  *  +-- stripHTML (string)
  * </pre>
  *
  *
  * @version <tt>$Revision$</tt>
  */
 public class FreeMarkerFeedTransformer
 {
     /*----------------------------------------------------------------------*\
                                Private Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * Configuration keyword for built-in template
      */
     private static final String CFG_TEMPLATE_LOAD_BUILTIN = "builtin";
 
     /**
      * Configuration keyword: Built-in HTML template
      */
     private static final String CFG_BUILTIN_HTML_TEMPLATE = "html";
 
     /**
      * Configuration keyword: Built-in text template
      */
     private static final String CFG_BUILTIN_TEXT_TEMPLATE = "text";
 
     /**
      * Configuration keyword: Built-in summary template
      */
     private static final String CFG_BUILTIN_SUMMARY_TEMPLATE = "summary";
 
     /**
      * Configuration keyword for template loading from classpath
      */
     private static final String CFG_TEMPLATE_LOAD_FROM_CLASSPATH = "classpath";
 
     /**
      * Configuration keyword for template loading from URL
      */
     private static final String CFG_TEMPLATE_LOAD_FROM_URL = "url";
 
     /**
      * Configuration keyword for template loading from file
      */
     private static final String CFG_TEMPLATE_LOAD_FROM_FILE = "file";
 
     /**
      * Built-in HTML template.
      */
     public final static TemplateLocation BUILTIN_HTML_TEMPLATE =
         new TemplateLocation (TemplateType.CLASSPATH,
                               "org/clapper/curn/output/freemarker/HTML.ftl");
 
     /**
      * Built-in text template
      */
     public final static TemplateLocation BUILTIN_TEXT_TEMPLATE =
         new TemplateLocation (TemplateType.CLASSPATH,
                               "org/clapper/curn/output/freemarker/Text.ftl");
 
     /**
      * Built-in summary template
      */
     public final static TemplateLocation BUILTIN_SUMMARY_TEMPLATE =
         new TemplateLocation (TemplateType.CLASSPATH,
                               "org/clapper/curn/output/freemarker/Summary.ftl");
 
     /**
      * Prefix to use with generated channel anchors.
      */
     private static final String CHANNEL_ANCHOR_PREFIX = "feed";
 
     /*----------------------------------------------------------------------*\
                              Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private freemarker.template.Configuration freemarkerConfig;
     private SimpleHash                        freemarkerDataModel;
     private SimpleHash                        freemarkerTOCData;
     private SimpleSequence                    freemarkerTOCItems;
     private SimpleSequence                    freemarkerChannelsData;
     private TemplateLocation                  templateLocation = null;
     private String                            mimeType = "text/plain";
     private String                            title = null;
     private String                            extraText = null;
     private String                            encoding = null;
     private CurnConfig                        config = null;
     private int                               totalChannels = 0;
     private int                               totalItems = 0;
     private int                               tocThreshold = 0;
 
     /**
      * For logging
      */
     private static final Logger log =
         new Logger (FreeMarkerFeedTransformer.class);
 
     /*----------------------------------------------------------------------*\
                                    Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Creates a new instance of <tt>FreeMarkerFeedTransformer</tt>.
      *
      * @param config                    The <i>curn</i> configuration object
      * @param showToolInfo              Whether or not to display curn info in
      *                                  the generated output. (The FreeMarker
      *                                  template may choose to ignore this item.)
      */
     public FreeMarkerFeedTransformer(final CurnConfig config,
                                      final boolean    showToolInfo)
     {
         this(config, showToolInfo, Integer.MAX_VALUE);
     }
 
     /**
      * Creates a new instance of <tt>FreeMarkerFeedTransformer</tt>.
      *
      * @param config                    The <i>curn</i> configuration object
      * @param showToolInfo              Whether or not to display curn info in
      *                                  the generated output. (The FreeMarker
      *                                  template may choose to ignore this item.)
      * @param tableOfContentsThreshold  How many items must be present before
      *                                  a table of contents should be included
      *                                  in the output. (The FreeMarker template
      *                                  may choose to ignore this item.)
      */
     public FreeMarkerFeedTransformer(final CurnConfig config,
                                      final boolean    showToolInfo,
                                      final int        tableOfContentsThreshold)
     {
         this.config = config;
         this.tocThreshold = tableOfContentsThreshold;
 
         // Create the FreeMarker configuration.
 
         freemarkerConfig = new freemarker.template.Configuration();
         freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
         freemarkerConfig.setTemplateLoader(new CurnTemplateLoader());
         freemarkerConfig.setLocalizedLookup(false);
 
         // Create the data model and populate it with invariant stuff.
 
         freemarkerDataModel = new SimpleHash();
         SimpleHash map = new SimpleHash();
 
         freemarkerDataModel.put("configFile", map);
         URL configFileURL = config.getConfigurationFileURL();
         if (configFileURL == null)
             map.put("url", "?");
         else
             map.put("url", configFileURL.toString());
 
         map = new SimpleHash();
         freemarkerDataModel.put("curn", map);
         map.put("version", Version.getVersionNumber());
         map.put("buildID", Version.getBuildID());
         if (showToolInfo)
             map.put("showToolInfo", true);
         else
             map.put("showToolInfo", false);
 
         this.freemarkerTOCData = new SimpleHash();
         freemarkerDataModel.put("tableOfContents", this.freemarkerTOCData);
         freemarkerTOCItems = new SimpleSequence();
         this.freemarkerTOCData.put("channels", freemarkerTOCItems);
 
         freemarkerChannelsData = new SimpleSequence();
         freemarkerDataModel.put("channels", freemarkerChannelsData);
 
 
         // Methods accessible from the template
 
         freemarkerDataModel.put("wrapText", new WrapTextMethod());
         freemarkerDataModel.put("indentText", new IndentTextMethod());
         freemarkerDataModel.put("stripHTML", new StripHTMLMethod());
         freemarkerDataModel.put("escapeHTML", new EscapeHTMLMethod());
     }
 
     /*----------------------------------------------------------------------*\
                                 Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Set the FreeMarker template from a configuration item. This method
      * parses a common configuration syntax and sets the internal template
      * location based on the value.
      *
      * @param section  the name of the section containing the configuration
      *                 item that specifies the template
      * @param itemName the configuration item name
      *
      * @throws ConfigurationException configuration error
      * @throws CurnException          some other error
      *
      * @see #setTemplate
      */
     public void setTemplateFromConfig(final String     section,
                                       final String     itemName)
         throws ConfigurationException,
                CurnException
     {
         parseTemplateLocation(config, section, itemName);
     }
 
     /**
      * Set the FreeMarker template via a {@link TemplateLocation} object.
      *
      * @param templateLocation the template location
      * @param mimeType         MIME type for the content the template generates
      *
      * @throws CurnException on error
      *
      * @see #setTemplateFromConfig
      */
     public void setTemplate(TemplateLocation templateLocation,
                             String           mimeType)
         throws CurnException
     {
         this.templateLocation = templateLocation;
         this.mimeType         = mimeType;
     }
 
     /**
      * Set the title (which is stored in the FreeMarker data model). If this
     * method isn't called, the title will be when {@link #transform} is
      * called.
      *
      * @param title  the title
      */
     public void setTitle(String title)
     {
         this.title = title;
     }
 
     /**
      * Set the "extra text" item, to be stored in the FreeMarker data model.
      *
      * @param text  the text
      */
     public void setExtraText(String text)
     {
         this.extraText = text;
     }
 
     /**
      * Set the encoding, which is stored in the FreeMarker data model.
      * If not specified, the default system encoding is used.
      *
      * @param encoding the encoding to use
      */
     public void setEncoding(String encoding)
     {
         this.encoding = encoding;
     }
 
     /**
      * Get the MIME type, as parsed from the template specification.
      *
      * @return the MIME type, or null if {@link #setTemplateFromConfig}
      *         was never called
      */
     public String getMIMEType()
     {
         return mimeType;
     }
 
     /**
      * Store the data in the specified feed (channel) in the FreeMarker
      * data model. The data model can later be passed to a FreeMarker
      * template by calling {@link #transform}.
      *
      * @param channel           the parsed channel (feed) data
      * @param feedInfo          the feed metadata
      * @param allowEmbeddedHTML whether or not to permit embedded HTML in the
      *                          feed output
      *
      * @throws CurnException on error
      */
     public void addChannel(final RSSChannel channel,
                            final FeedInfo   feedInfo,
                            final boolean    allowEmbeddedHTML)
         throws CurnException
     {
         if (! allowEmbeddedHTML)
             channel.stripHTML();
 
         // Add the channel information to the data model.
 
         Collection<RSSItem> items = channel.getItems();
         int totalItemsInChannel = items.size();
 
         if (totalItemsInChannel == 0)
             return;
 
         this.totalItems += totalItemsInChannel;
         totalChannels++;
 
         String channelAnchorName = CHANNEL_ANCHOR_PREFIX
                                  + String.valueOf(totalChannels);
         RSSLink link;
 
         // Store the channel data.
 
         SimpleHash channelData = new SimpleHash();
         freemarkerChannelsData.add(channelData);
         channelData.put("index", new SimpleNumber(totalChannels));
         channelData.put("totalItems", new SimpleNumber(totalItemsInChannel));
         channelData.put("anchorName", channelAnchorName);
 
         String channelTitle = channel.getTitle();
         if (channelTitle == null)
             channelTitle = "";
         channelData.put("title", channelTitle.trim());
 
         String description = channel.getDescription();
         if (description == null)
             description = "";
         channelData.put("description", description.trim());
 
         if (config.showRSSVersion())
             channelData.put("rssFormat", channel.getRSSFormat());
 
         // Publish two URLs for the channel: The one from the configuration
         // file (feedInfo.getURL()) and the one that's actually published in
         // the downloaded RSS XML.
 
         URL feedInfoURL = feedInfo.getURL();
         channelData.put("configuredURL", feedInfoURL.toString());
 
         URL channelURL;
         link = channel.getLinkWithFallback("text/html");
         if (link == null)
             channelURL = feedInfoURL;
         else
             channelURL = link.getURL();
         channelData.put("url", channelURL.toString());
 
         String id = channel.getID();
         if (id == null)
             id = channelURL.toExternalForm();
 
         channelData.put("id", id);
 
         Date channelDate = null;
         channelData.put("showDate", true);
 
         if (channelDate != null)
         {
             channelData.put("date", new SimpleDate(channelDate,
                                                    SimpleDate.DATETIME));
         }
 
         Collection<String> authors = channel.getAuthors();
         SimpleSequence authorsData = new SimpleSequence();
         String authorString = "";
         if (authors != null)
         {
             for (String author : authors)
                 authorsData.add(author);
 
             authorString = TextUtil.join(authors, ", ");
         }
 
         channelData.put("authors", authorsData);
         channelData.put("author", authorString);
 
         // Store a table of contents entry for the channel.
 
         SimpleHash tocData = new SimpleHash();
         tocData.put("title", channelTitle);
         tocData.put("totalItems", new SimpleNumber(totalItemsInChannel));
         tocData.put("channelAnchor", channelAnchorName);
         freemarkerTOCItems.add(tocData);
 
         // Create a collection for the channel items.
 
         SimpleSequence itemsData = new SimpleSequence();
         channelData.put("items", itemsData);
 
         // Now, put in the data for each item in the channel.
 
         String[] desiredItemDescTypes;
 
         int i = 0;
         for (RSSItem item : items)
         {
             SimpleHash itemData = new SimpleHash();
             itemsData.add(itemData);
 
             i++;
             itemData.put("index", new SimpleNumber(i));
             itemData.put("showDate", true);
             Date itemDate = item.getPublicationDate();
             if (itemDate != null)
             {
                 itemData.put("date", new SimpleDate(itemDate,
                                                     SimpleDate.DATETIME));
             }
 
             link = item.getLinkWithFallback("text/html");
             assert (link != null);
             URL itemURL = link.getURL();
             itemData.put("url", itemURL.toString());
             id = item.getID();
             if (id == null)
                 id = itemURL.toString();
             itemData.put("id", id);
 
             itemData.put("showAuthor", true);
 
             // Add both a combined author string ("author") and a sequence of
             // individual authors.
 
             authorString = "";
             authors = item.getAuthors();
             authorsData = new SimpleSequence();
             if ((authors != null) && (authors.size() > 0))
             {
                 authorString = TextUtil.join(authors, ", ");
                 for (String author : authors)
                     authorsData.add(author);
             }
 
             itemData.put("author", authorString);
             itemData.put("authors", authorsData);
 
             String itemTitle = item.getTitle();
             if (itemTitle == null)
                 itemTitle = "(No Title)";
             itemData.put("title", itemTitle.trim());
 
             String desc = item.getSummary();
 
             if (desc == null)
                 desc = "";
 
             itemData.put("description", desc.trim());
         }
     }
 
     /**
      * Transform the data model via the FreeMarker template, writing the
      * transformed channel data to the specified <tt>Writer</tt>
      *
      * @param out where to write the transformed data
      *
      * @throws CurnException on error
      */
     public void transform(final Writer out)
         throws CurnException
     {
         transform(new PrintWriter(out));
     }
 
     /**
      * Transform the data model via the FreeMarker template, writing the
      * transformed channel data to the specified <tt>PrintWriter</tt>
      *
      * @param out where to write the transformed data
      *
      * @throws CurnException on error
      */
     public void transform(final PrintWriter out)
         throws CurnException
     {
         // Put the remainder of the data in the data model.
 
         freemarkerDataModel.put("dateGenerated",
                                 new SimpleDate(new Date(), SimpleDate.DATETIME));
         freemarkerDataModel.put("title", (title != null) ? title : "");
         freemarkerDataModel.put("extraText", (extraText != null) ? extraText : "");
 
         freemarkerDataModel.put("encoding",
                                 (encoding != null) ? encoding
                                                    : FileUtil.getDefaultEncoding());
 
         freemarkerDataModel.put("totalItems", new SimpleNumber(totalItems));
 
         // Get the template.
 
         if (templateLocation == null)
             throw new CurnException("(BUG) templateLocation not set.");
 
         String templateName = templateLocation.getName();
         Template template;
 
         try
         {
             // Create the FreeMarker template.
 
             template = freemarkerConfig.getTemplate (templateName);
         }
 
         catch (IOException ex)
         {
             log.error ("Error creating FreeMarker template", ex);
             throw new CurnException
                          (Constants.BUNDLE_NAME,
                           "FreeMarkerOutputHandler.cantGetFreeMarkerTemplate",
                           "Cannot create FreeMarker template",
                           ex);
         }
 
         if (totalItems >= tocThreshold)
             freemarkerTOCData.put ("needed", TemplateBooleanModel.TRUE);
         else
             freemarkerTOCData.put ("needed", TemplateBooleanModel.FALSE);
 
         try
         {
             template.process (freemarkerDataModel, out);
         }
 
         catch (TemplateException ex)
         {
             log.error ("Error processing FreeMarker template", ex);
             throw new CurnException
                           (Constants.BUNDLE_NAME,
                            "FreeMarkerOutputHandler.cantProcessTemplate",
                            "Error while processing FreeMarker template " +
                            "\"{0}\"",
                            new Object[] {templateLocation.getLocation()});
         }
 
         catch (IOException ex)
         {
             throw new CurnException
                           (Constants.BUNDLE_NAME,
                            "FreeMarkerOutputHandler.cantProcessTemplate",
                            "Error while processing FreeMarker template " +
                            "\"{0}\"",
                            new Object[] {templateLocation.getLocation()});
         }
     }
 
     /*----------------------------------------------------------------------*\
                                Protected Methods
     \*----------------------------------------------------------------------*/
 
     /*----------------------------------------------------------------------*\
                                 Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Parse and validate the template file configuration parameter. Sets
      * the templateFile instance variable.
      *
      * @param config    the parsed <i>curn</i> configuration data
      * @param section   the name of the section
      * @param itemName  the configuration item name
      *
      * @throws ConfigurationException  configuration error
      * @throws CurnException           any other error
      */
     private void parseTemplateLocation(final CurnConfig config,
                                        final String     section,
                                        final String     itemName)
         throws ConfigurationException,
                CurnException
     {
         // Get the template file configuration as explicit tokens from the
         // config parser. Saves parsing them here, plus the config file has
         // mechanisms for quoting white space within a token.
 
         String[] templateTokens =
             config.getConfigurationTokens(section, itemName);
 
         if (templateTokens == null)
         {
             templateTokens = new String[]
                              {
                                  CFG_TEMPLATE_LOAD_BUILTIN,
                                  CFG_BUILTIN_HTML_TEMPLATE
                              };
         }
 
         else
         {
             // The configuration parser only breaks the line into tokens if
             // there are quoted fields. So, it's possible for there to be
             // one token (no quoted fields), two tokens (a single quoted
             // field) or many tokens.
 
             if (templateTokens.length == 1)
             {
                 // Split it on white space.
 
                 templateTokens = templateTokens[0].split (" ");
             }
 
             if ((templateTokens.length != 2) && (templateTokens.length != 3))
             {
                 throw new ConfigurationException
                     (section,
                      "\"TemplateFile\" value \"" +
                      config.getConfigurationValue (section, itemName) +
                      "\" (\"" +
                      config.getRawValue (section, itemName) +
                      "\") must have two or three fields.");
             }
         }
 
         String templateType = templateTokens[0].trim();
 
         if (templateType.equalsIgnoreCase(CFG_TEMPLATE_LOAD_BUILTIN))
         {
             if (templateTokens[1].equals(CFG_BUILTIN_HTML_TEMPLATE))
             {
                 this.templateLocation = BUILTIN_HTML_TEMPLATE;
                 this.mimeType = "text/html";
             }
 
             else if (templateTokens[1].equals(CFG_BUILTIN_TEXT_TEMPLATE))
             {
                 this.templateLocation = BUILTIN_TEXT_TEMPLATE;
                 this.mimeType = "text/plain";
             }
 
             else if (templateTokens[1].equals(CFG_BUILTIN_SUMMARY_TEMPLATE))
             {
                 this.templateLocation = BUILTIN_SUMMARY_TEMPLATE;
                 this.mimeType = "text/plain";
             }
 
             else
             {
                 throw new ConfigurationException(section,
                                                  "Unknown built-in " +
                                                  "template file \"" +
                                                  templateTokens[1] + "\"");
             }
         }
 
         else if (templateType.equalsIgnoreCase(CFG_TEMPLATE_LOAD_FROM_URL))
         {
             this.templateLocation = new TemplateLocation(TemplateType.URL,
                                                          templateTokens[1]);
             if (templateTokens.length == 3)
                 this.mimeType = templateTokens[2];
         }
 
         else if (templateType.equalsIgnoreCase(CFG_TEMPLATE_LOAD_FROM_FILE))
         {
             this.templateLocation = new TemplateLocation(TemplateType.FILE,
                                                          templateTokens[1]);
             if (templateTokens.length == 3)
                 this.mimeType = templateTokens[2];
         }
 
         else if (templateType.equalsIgnoreCase(CFG_TEMPLATE_LOAD_FROM_CLASSPATH))
         {
             this.templateLocation = new TemplateLocation(TemplateType.CLASSPATH,
                                                          templateTokens[1]);
             if (templateTokens.length == 3)
                 this.mimeType = templateTokens[2];
         }
 
         else
         {
             throw new ConfigurationException
                 (section,
                  "\"TemplateFile\" value \"" +
                  config.getRawValue (section, itemName) +
                  "\" has unknown type \"" + templateType + "\".");
         }
     }
 }
