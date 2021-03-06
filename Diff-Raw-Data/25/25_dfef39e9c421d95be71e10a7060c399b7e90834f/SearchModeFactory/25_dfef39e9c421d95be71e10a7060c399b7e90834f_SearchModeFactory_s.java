 // Copyright (2006-2007) Schibsted Søk AS
 package no.schibstedsok.searchportal.mode;
 
 
 import no.schibstedsok.commons.ioc.BaseContext;
 import no.schibstedsok.commons.ioc.ContextWrapper;
 import no.schibstedsok.searchportal.InfrastructureException;
 import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.AbstractYahooSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.AddressSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.AdvancedFastSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.BlendingNewsSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.BlocketSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.BlogSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.CatalogueAdsSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.CatalogueBannersSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.CatalogueSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.ClusteringESPFastConfiguration;
 import no.schibstedsok.searchportal.mode.config.DailyWordConfiguration;
 import no.schibstedsok.searchportal.mode.config.ESPFastSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.FastSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.HittaMapSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.HittaSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.MathExpressionSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.MobileSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.NavigatableESPFastConfiguration;
 import no.schibstedsok.searchportal.mode.config.NewsAggregatorSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.NewsMyNewsSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.NewsSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.OverturePPCSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.PicSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.PlatefoodPPCSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.PrisjaktSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.SearchMode;
 import no.schibstedsok.searchportal.mode.config.SensisSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.StaticSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.StockSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.StormWeatherSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.TvEnrichSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.TvSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.TvWaitSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.VehicleSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.WebSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.WhiteSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.YahooIdpSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.YahooMediaSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.YellowGeoSearchConfiguration;
 import no.schibstedsok.searchportal.mode.config.YellowSearchConfiguration;
 import no.schibstedsok.searchportal.query.transform.QueryTransformerConfig;
 import no.schibstedsok.searchportal.result.Navigator;
 import no.schibstedsok.searchportal.result.handler.ResultHandlerConfig;
 import no.schibstedsok.searchportal.site.Site;
 import no.schibstedsok.searchportal.site.SiteContext;
 import no.schibstedsok.searchportal.site.SiteKeyedFactory;
 import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
 import no.schibstedsok.searchportal.site.config.DocumentLoader;
 import no.schibstedsok.searchportal.site.config.ResourceContext;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * @author <a href="mailto:mick@wever.org>mick</a>
  * @version <tt>$Id$</tt>
  */
 public final class SearchModeFactory extends AbstractDocumentFactory implements SiteKeyedFactory {
 
     /**
      * The context any SearchModeFactory must work against. *
      */
     public interface Context extends BaseContext, ResourceContext, SiteContext {
     }
 
     // Constants -----------------------------------------------------
 
     private static final Map<Site, SearchModeFactory> INSTANCES = new HashMap<Site, SearchModeFactory>();
     private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();
 
     /**
      * TODO comment me. *
      */
     public static final String MODES_XMLFILE = "modes.xml";
 
 
     private static final Map<SearchMode, Map<String, SearchConfiguration>> COMMANDS
             = new HashMap<SearchMode, Map<String, SearchConfiguration>>();
     private static final ReentrantReadWriteLock COMMANDS_LOCK = new ReentrantReadWriteLock();
 
     private static final Logger LOG = Logger.getLogger(SearchModeFactory.class);
     private static final String ERR_DOC_BUILDER_CREATION
             = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";
     private static final String ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED
             = "Each FastNavigator is only allowed to have one child. Parent was ";
     private static final String ERR_FAST_EPS_QR_SERVER =
             "Query server address cannot contain the scheme (http://): ";
     private static final String INFO_PARSING_MODE = "Parsing mode ";
     private static final String INFO_PARSING_CONFIGURATION = " Parsing configuration ";
     private static final String INFO_PARSING_NAVIGATOR = "  Parsing navigator ";
     private static final String INFO_PARSING_RESULT_HANDLER = "  Parsing result handler ";
     private static final String INFO_PARSING_QUERY_TRANSFORMER = "  Parsing query transformer ";
     private static final String INFO_CONSTRUCT = "  Construct ";
     private static final String DEBUG_PARSED_PROPERTY = "  Property property ";
     private static final String ERR_PARENT_COMMAND_NOT_FOUND = "Parent command {0} not found for {1} in mode {2}";
 
     // Attributes ----------------------------------------------------
 
     private final Map<String, SearchMode> modes = new HashMap<String, SearchMode>();
     private final ReentrantReadWriteLock modesLock = new ReentrantReadWriteLock();
 
     private final DocumentLoader loader;
     private final Context context;
 
     private String templatePrefix;
 
     // Static --------------------------------------------------------
 
     /**
      * TODO comment me. *
      */
     public static SearchModeFactory valueOf(final Context cxt) {
 
         final Site site = cxt.getSite();
 
         SearchModeFactory instance;
         try {
             INSTANCES_LOCK.readLock().lock();
             instance = INSTANCES.get(site);
         } finally {
             INSTANCES_LOCK.readLock().unlock();
         }
 
         if (instance == null) {
             try {
                 instance = new SearchModeFactory(cxt);
             } catch (ParserConfigurationException ex) {
                 LOG.error(ERR_DOC_BUILDER_CREATION, ex);
             }
         }
         return instance;
     }
 
     /**
      * TODO comment me. *
      */
     public boolean remove(final Site site) {
 
         try {
             INSTANCES_LOCK.writeLock().lock();
             return null != INSTANCES.remove(site);
         } finally {
             INSTANCES_LOCK.writeLock().unlock();
         }
     }
 
     // Constructors --------------------------------------------------
 
     /**
      * Creates a new instance of ModeFactoryImpl
      */
     private SearchModeFactory(final Context cxt)
             throws ParserConfigurationException {
 
         LOG.trace("ModeFactory(cxt)");
         try {
             INSTANCES_LOCK.writeLock().lock();
 
             context = cxt;
 
             // configuration files
             final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setValidating(false);
             final DocumentBuilder builder = factory.newDocumentBuilder();
             loader = context.newDocumentLoader(cxt, MODES_XMLFILE, builder);
 
             // update the store of factories
             INSTANCES.put(context.getSite(), this);
             // start initialisation
             init();
 
         } finally {
             INSTANCES_LOCK.writeLock().unlock();
         }
 
     }
 
     // Public --------------------------------------------------------
 
     /**
      * TODO comment me. *
      */
     public SearchMode getMode(final String id) {
 
         LOG.trace("getMode(" + id + ")");
 
         SearchMode mode = getModeImpl(id);
         if (mode == null && id != null && id.length() > 0 && context.getSite().getParent() != null) {
             // not found in this site's modes.xml. look in parent's site.
             final SearchModeFactory factory = valueOf(ContextWrapper.wrap(
                     Context.class,
                     new SiteContext() {
                         public Site getSite() {
                             return context.getSite().getParent();
                         }
                     },
                     context
             ));
             mode = factory.getMode(id);
         }
         return mode;
     }
 
     // Package protected ---------------------------------------------
 
     /* Test use it. **/
 
     Map<String, SearchMode> getModes() {
 
         return Collections.unmodifiableMap(modes);
     }
 
     // Protected -----------------------------------------------------
 
     // Private -------------------------------------------------------
 
     private void init() {
 
         loader.abut();
         LOG.debug("Parsing " + MODES_XMLFILE + " started");
         final Document doc = loader.getDocument();
         final Element root = doc.getDocumentElement();
 
         if (null != root) {
             templatePrefix = root.getAttribute("template-prefix");
 
             // loop through modes.
             final NodeList modeList = root.getElementsByTagName("mode");
 
             for (int i = 0; i < modeList.getLength(); ++i) {
                 final Element modeE = (Element) modeList.item(i);
                 final String id = modeE.getAttribute("id");
 
                 LOG.info(INFO_PARSING_MODE + modeE.getLocalName() + " " + id);
                 final SearchMode inherit = getMode(modeE.getAttribute("inherit"));
 
                 final SearchMode mode = new SearchMode(inherit);
 
                 mode.setId(id);
                mode.setExecutor(parseExecutor(modeE.getAttribute("executor"),
                         inherit != null ? inherit.getExecutor() : SearchMode.SearchCommandExecutorConfig.SEQUENTIAL));
 
                 fillBeanProperty(mode, inherit, "analysis", ParseType.Boolean, modeE, "false");
 
                 // setup new commands list for this mode
                 final Map<String, SearchConfiguration> modesCommands = new HashMap<String, SearchConfiguration>();
                 try {
                     COMMANDS_LOCK.writeLock().lock();
                     COMMANDS.put(mode, modesCommands);
                 } finally {
                     COMMANDS_LOCK.writeLock().unlock();
                 }
 
                 // now loop through commands
                 for (CommandTypes commandType : CommandTypes.values()) {
                     final NodeList commandsList = modeE.getElementsByTagName(commandType.getXmlName());
 
 
                     for (int j = 0; j < commandsList.getLength(); ++j) {
                         final Element commandE = (Element) commandsList.item(j);
 
                         final SearchConfiguration sc = commandType.parseSearchConfiguration(context, commandE, mode);
                         modesCommands.put(sc.getName(), sc);
                         mode.addSearchConfiguration(sc);
                     }
                 }
                 // add mode
                 try {
                     modesLock.writeLock().lock();
                     modes.put(id, mode);
                 } finally {
                     modesLock.writeLock().unlock();
                 }
             }
         }
 
         // finished
         LOG.debug("Parsing " + MODES_XMLFILE + " finished");
 
     }
 
     private static SearchMode.SearchCommandExecutorConfig parseExecutor(
             final String name,
             final SearchMode.SearchCommandExecutorConfig def) {
 
         try {
            return SearchMode.SearchCommandExecutorConfig.valueOf(name.toUpperCase());
 
         } catch (IllegalArgumentException iae) {
             LOG.error("Unparsable executor " + name, iae);
         }
         return def;
     }
 
     private SearchMode getModeImpl(final String id) {
 
         try {
             modesLock.readLock().lock();
             return modes.get(id);
 
         } finally {
             modesLock.readLock().unlock();
         }
     }
 
     // Inner classes -------------------------------------------------
 
     private enum CommandTypes {
         COMMAND(AbstractSearchConfiguration.class),
         ESP_FAST_COMMAND(ESPFastSearchConfiguration.class),
         ADDRESS_COMMAND(AddressSearchConfiguration.class),
         ADVANCED_FAST_COMMAND(AdvancedFastSearchConfiguration.class),
         NAVIGATABLE_ESP_FAST_COMMAND(NavigatableESPFastConfiguration.class),
         BLENDING_NEWS_COMMAND(BlendingNewsSearchConfiguration.class),
         FAST_COMMAND(FastSearchConfiguration.class),
         HITTA_COMMAND(HittaSearchConfiguration.class),
         MATH_COMMAND(MathExpressionSearchConfiguration.class),
         MOBILE_COMMAND(MobileSearchConfiguration.class),
         NEWS_COMMAND(NewsSearchConfiguration.class),
         OVERTURE_PPC_COMMAND(OverturePPCSearchConfiguration.class),
         PLATEFOOD_PPC_COMMAND(PlatefoodPPCSearchConfiguration.class),
         PICTURE_COMMAND(PicSearchConfiguration.class),
         SENSIS_COMMAND(SensisSearchConfiguration.class),
         STATIC_COMMAND(StaticSearchConfiguration.class),
         STOCK_COMMAND(StockSearchConfiguration.class),
         STORMWEATHER_COMMAND(StormWeatherSearchConfiguration.class),
         TVSEARCH_COMMAND(TvSearchConfiguration.class),
         TVWAITSEARCH_COMMAND(TvWaitSearchConfiguration.class),
         TVENRICH_COMMAND(TvEnrichSearchConfiguration.class),
         YAHOO_IDP_COMMAND(YahooIdpSearchConfiguration.class),
         YAHOO_MEDIA_COMMAND(YahooMediaSearchConfiguration.class),
         YELLOWPAGES_COMMAND(YellowSearchConfiguration.class),
         YELLOWGEOPAGES_COMMAND(YellowGeoSearchConfiguration.class),
         WEB_COMMAND(WebSearchConfiguration.class),
         WHITEPAGES_COMMAND(WhiteSearchConfiguration.class),
         DAILY_WORD_COMMAND(DailyWordConfiguration.class),
         BLOG_COMMAND(BlogSearchConfiguration.class),
         PRISJAKT_COMMAND(PrisjaktSearchConfiguration.class),
         BLOCKET_COMMAND(BlocketSearchConfiguration.class),
         VEHICLE_COMMAND(VehicleSearchConfiguration.class),
         CATALOGUE_COMMAND(CatalogueSearchConfiguration.class),
         CATALOGUE_ADS_COMMAND(CatalogueAdsSearchConfiguration.class),
         HITTAMAP_COMMAND(HittaMapSearchConfiguration.class),
         CATALOGUE_BANNERS_COMMAND(CatalogueBannersSearchConfiguration.class),
         CLUSTERING_ESP_FAST_COMMAND(ClusteringESPFastConfiguration.class),
         NEWS_AGGREGATOR_COMMAND(NewsAggregatorSearchConfiguration.class),
         NEWS_MY_NEWS_COMMAND(NewsMyNewsSearchConfiguration.class);
 
         private final Class<? extends SearchConfiguration> clazz;
         private final String xmlName;
         private final QueryTransformerFactory queryTransformerFactory = new QueryTransformerFactory();
         private final ResultHandlerFactory resultHandlerFactory = new ResultHandlerFactory();
 
         CommandTypes(final Class<? extends SearchConfiguration> clazz) {
             this.clazz = clazz;
             xmlName = name().replaceAll("_", "-").toLowerCase();
         }
 
         public String getXmlName() {
             return xmlName;
         }
 
         public SearchConfiguration parseSearchConfiguration(
                 final Context cxt,
                 final Element commandE,
                 final SearchMode mode) {
 
             final String parentName = commandE.getAttribute("inherit");
             final String id = commandE.getAttribute("id");
 
             final SearchConfiguration inherit = findParent(parentName, mode);
 
             if (!"".equals(parentName) && inherit == null) {
                 throw new IllegalArgumentException(
                         MessageFormat.format(ERR_PARENT_COMMAND_NOT_FOUND, parentName, id, mode.getId()));
             }
 
             LOG.info(INFO_PARSING_CONFIGURATION + commandE.getLocalName() + " " + id);
 
             try {
                 final Constructor<? extends SearchConfiguration> con;
                 con = clazz.getConstructor(SearchConfiguration.class);
                 final SearchConfiguration sc;
                 sc = con.newInstance(inherit);
                 fillBeanProperty(sc, inherit, "resultsToReturn", ParseType.Int, commandE, "-1");
 
                 if (sc instanceof AbstractSearchConfiguration) {
                     // everything extends AbstractSearchConfiguration
                     final AbstractSearchConfiguration asc = (AbstractSearchConfiguration) sc;
 
                     asc.setName(id);
                     fillBeanProperty(sc, inherit, "alwaysRun", ParseType.Boolean, commandE, "false");
 
                     if (commandE.hasAttribute("field-filters")) {
                         if (commandE.getAttribute("field-filters").length() > 0) {
                             final String[] fieldFilters = commandE.getAttribute("field-filters").split(",");
                             for (String fieldFilter : fieldFilters) {
                                 if (fieldFilter.contains(" AS ")) {
                                     final String[] ff = fieldFilter.split(" AS ");
                                     asc.addFieldFilter(ff[0].trim(), ff[1].trim());
                                 } else {
                                     asc.addFieldFilter(fieldFilter, fieldFilter);
                                 }
                             }
                         } else {
                             // If attribute is present and empty, clear the field filters. This creates an option
                             // for child commands to not inherit field filters.
                             asc.clearFieldFilters();
                         }
                     }
 
                     fillBeanProperty(sc, inherit, "paging", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "queryParameter", ParseType.String, commandE, "");
 
                     if (commandE.getAttribute("result-fields").length() > 0) {
                         final String[] resultFields = commandE.getAttribute("result-fields").split(",");
                         for (String resultField : resultFields) {
                             asc.addResultField(resultField.trim().split(" AS "));
                         }
                     }
 
                     fillBeanProperty(sc, inherit, "statisticalName", ParseType.String, commandE, "");
 
                 }
                 if (sc instanceof FastSearchConfiguration) {
                     final FastSearchConfiguration fsc = (FastSearchConfiguration) sc;
                     final FastSearchConfiguration fscInherit = inherit instanceof FastSearchConfiguration
                             ? (FastSearchConfiguration) inherit
                             : null;
                     fillBeanProperty(sc, inherit, "clustering", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "collapsing", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "expansion", ParseType.Boolean, commandE, "false");
 
                     if (commandE.getAttribute("collections").length() > 0) {
                         fsc.getCollections().clear();
                         final String[] collections = commandE.getAttribute("collections").split(",");
                         for (String collection : collections) {
                             fsc.addCollection(collection);
                         }
                     }
 
                     fillBeanProperty(sc, inherit, "filter", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "project", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "project", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "filtertype", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "ignoreNavigation", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "offensiveScoreLimit", ParseType.Int, commandE, "-1");
                     fillBeanProperty(sc, inherit, "qtPipeline", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "queryServerUrl", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "relevantQueries", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "sortBy", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "spamScoreLimit", ParseType.Int, commandE, "-1");
                     fillBeanProperty(sc, inherit, "spellcheck", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "spellchecklanguage", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "lemmatise", ParseType.Boolean, commandE, "false");
 
                     if (fsc.getQueryServerUrl() == null || "".equals(fsc.getQueryServerUrl())) {
                         LOG.debug("queryServerURL is empty for " + fsc.getName());
                     }
 
                     // navigators
                     if (fscInherit != null && fscInherit.getNavigators() != null) {
                         for (final Map.Entry<String, Navigator> nav : fscInherit.getNavigators().entrySet()) {
                             fsc.addNavigator(nav.getValue(), nav.getKey());
                         }
                     }
 
                     final NodeList nList = commandE.getElementsByTagName("navigators");
 
                     for (int i = 0; i < nList.getLength(); ++i) {
                         final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
                         for (Navigator navigator : navigators) {
                             fsc.addNavigator(navigator, navigator.getId());
                         }
 
                     }
                 }
                 if (sc instanceof ESPFastSearchConfiguration) {
                     final ESPFastSearchConfiguration esc = (ESPFastSearchConfiguration) sc;
 
                     final ESPFastSearchConfiguration ascInherit = inherit instanceof ESPFastSearchConfiguration
                             ? (ESPFastSearchConfiguration) inherit
                             : null;
 
                     fillBeanProperty(sc, inherit, "view", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "sortBy", ParseType.String, commandE, "default");
                     fillBeanProperty(sc, inherit, "collapsingRemoves", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "collapsingEnabled", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "expansionEnabled", ParseType.Boolean, commandE, "false");
                     fillBeanProperty(sc, inherit, "qtPipeline", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "queryServer", ParseType.String, commandE, "");
 
                     if (null != esc.getQueryServer() && esc.getQueryServer().startsWith("http://")) {
                         throw new IllegalArgumentException(ERR_FAST_EPS_QR_SERVER + esc.getQueryServer());
                     }
 
                     // navigators
                     final NodeList nList = commandE.getElementsByTagName("navigators");
                     for (int i = 0; i < nList.getLength(); ++i) {
                         final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
                         for (Navigator navigator : navigators) {
                             esc.addNavigator(navigator, navigator.getId());
                         }
 
                     }
                 }
 
                 if (sc instanceof NavigatableESPFastConfiguration) {
                     final NavigatableESPFastConfiguration nasc = (NavigatableESPFastConfiguration) sc;
                     // navigators
                     final NodeList nList = commandE.getElementsByTagName("navigators");
                     for (int i = 0; i < nList.getLength(); ++i) {
                         final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
                         for (Navigator navigator : navigators) {
                             nasc.addNavigator(navigator, navigator.getId());
                         }
                     }
                 }
                 if (sc instanceof HittaSearchConfiguration) {
                     fillBeanProperty(sc, inherit, "catalog", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "key", ParseType.String, commandE, "");
                 }
 
                 if (sc instanceof PrisjaktSearchConfiguration) {
                 }
 
                 if (sc instanceof BlocketSearchConfiguration) {
                     final BlocketSearchConfiguration bsc = (BlocketSearchConfiguration) sc;
 
                     /**
                      * Read blocket.se's around 400 most commonly used search phrases excluding vehicle oriented stuff, from blocket_search_words.xml.
                      */
                     final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                     factory.setValidating(false);
                     final DocumentBuilder builder = factory.newDocumentBuilder();
                     DocumentLoader loader = cxt.newDocumentLoader(cxt, bsc.getBlocketConfigFileName(), builder);
                     loader.abut();
 
                     final Map<String, String> blocketmap = new HashMap<String, String>();
                     final Document doc = loader.getDocument();
                     final Element root = doc.getDocumentElement();
 
                     final NodeList wordList = root.getElementsByTagName("word");
 
                     // loop through words.
                     for (int i = 0; i < wordList.getLength(); ++i) {
                         final Element wordElement = (Element) wordList.item(i);
                         final String cid = wordElement.getAttribute("category-id");
                         final String catName = wordElement.getAttribute("category");
                         final String word = wordElement.getTextContent();
                         // Put words into a map
                         blocketmap.put(word, cid + ":" + catName);
                     }
                     bsc.setBlocketMap(blocketmap);
                 }
 
                 if (sc instanceof VehicleSearchConfiguration) {
                     final VehicleSearchConfiguration vsc = (VehicleSearchConfiguration) sc;
 
                     /**
                      * Read vehicle specific properties for bytbil.com and blocket.se
                      */
                     final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                     factory.setValidating(false);
                     final DocumentBuilder builder = factory.newDocumentBuilder();
                     DocumentLoader loader = cxt.newDocumentLoader(cxt, vsc.getAccessoriesFileName(), builder);
                     loader.abut();
 
                     final Set<String> accessoriesSet = new HashSet<String>();
                     final Document doc = loader.getDocument();
                     final Element root = doc.getDocumentElement();
 
                     final NodeList accList = root.getElementsByTagName("accessory");
 
                     /**
                      * Put car accessory search words from xml in a set
                      */
                     for (int i = 0; i < accList.getLength(); ++i) {
                         final Element wordElement = (Element) accList.item(i);
                         final String acc = wordElement.getTextContent();
                         accessoriesSet.add(acc);
                     }
                     vsc.setAccessoriesSet(accessoriesSet);
 
 
                     final Map<String, String> carMap = new HashMap<String, String>();
                     final DocumentBuilder builder2 = factory.newDocumentBuilder();
                     DocumentLoader carLoader = cxt.newDocumentLoader(cxt, vsc.getCarsPropertiesFileName(), builder2);
                     carLoader.abut();
 
                     final Document doc2 = carLoader.getDocument();
                     final Element root2 = doc2.getDocumentElement();
 
                     /**
                      * Put car words from xml into a map
                      */
                     final NodeList carList = root2.getElementsByTagName("car");
 
                     for (int i = 0; i < carList.getLength(); ++i) {
                         final Element wordElement = (Element) carList.item(i);
                         final String brand = wordElement.getAttribute("brand");
                         final String model = wordElement.getAttribute("model");
                         final String car = wordElement.getTextContent();
                         carMap.put(car, brand + ";" + model);   // "volvo p 1800" , "volvo;p 1800"
                     }
                     vsc.setCarsMap(carMap);
                 }
 
                 if (sc instanceof AbstractYahooSearchConfiguration) {
                     fillBeanProperty(sc, inherit, "encoding", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "partnerId", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "host", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "port", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "hostHeader", ParseType.String, commandE, "");
                 }
                 if (sc instanceof YahooMediaSearchConfiguration) {
 
                     fillBeanProperty(sc, inherit, "catalog", ParseType.String, commandE,
                             YahooMediaSearchConfiguration.DEFAULT_CATALOG);
                     fillBeanProperty(sc, inherit, "ocr", ParseType.String, commandE,
                             YahooMediaSearchConfiguration.DEFAULT_OCR);
                     fillBeanProperty(sc, inherit, "site", ParseType.String, commandE, "");
                 }
                 if (sc instanceof OverturePPCSearchConfiguration) {
                     fillBeanProperty(sc, inherit, "url", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "type", ParseType.String, commandE, "");
                 }
                 if (sc instanceof PlatefoodPPCSearchConfiguration) {
                     fillBeanProperty(sc, inherit, "url", ParseType.String, commandE, "");
                 }
                 if (sc instanceof YahooIdpSearchConfiguration) {
                     fillBeanProperty(sc, inherit, "database", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "dateRange", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "filter", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "hideDomain", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "language", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "languageMix", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "region", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "regionMix", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "spellState", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "unique", ParseType.String, commandE, "");
                 }
                 if (sc instanceof PicSearchConfiguration) {
 
                     fillBeanProperty(sc, inherit, "queryServerHost", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "queryServerPort", ParseType.String, commandE, "");
                     fillBeanProperty(sc, inherit, "country", ParseType.String, commandE, "no");
                     fillBeanProperty(sc, inherit, "filter", ParseType.String, commandE, "medium");
                     fillBeanProperty(sc, inherit, "customerId", ParseType.String, commandE, "558735");
 
                     LOG.debug("Inherited customerid " + ((PicSearchConfiguration) sc).getCustomerId());
 
                 }
                 if (sc instanceof MobileSearchConfiguration) {
                     final MobileSearchConfiguration msc = (MobileSearchConfiguration) sc;
 
                     // TODO use fillBeanProperty pattern instead
                     msc.setPersonalizationGroup(commandE.getAttribute("personalization-group"));
                     // TODO use fillBeanProperty pattern instead
                     msc.setTelenorPersonalizationGroup(commandE.getAttribute("telenor-personalization-group"));
                     // TODO use fillBeanProperty pattern instead
                     msc.setSortBy(commandE.getAttribute("sort-by"));
                     // TODO use fillBeanProperty pattern instead
                     msc.setSource(commandE.getAttribute("source"));
                     // TODO use fillBeanProperty pattern instead
                     msc.setFilter(commandE.getAttribute("filter"));
                 }
                 if (sc instanceof BlendingNewsSearchConfiguration) {
                     final BlendingNewsSearchConfiguration bnsc = (BlendingNewsSearchConfiguration) sc;
 
                     final String[] filters = commandE.getAttribute("filters").split(",");
 
                     final List<String> filterList = new ArrayList<String>();
 
                     for (int i = 0; i < filters.length; i++) {
                         filterList.add(filters[i].trim());
                     }
                     // TODO use fillBeanProperty pattern instead
                     bnsc.setFiltersToBlend(filterList);
                     // TODO use fillBeanProperty pattern instead
                     bnsc.setDocumentsPerFilter(Integer.parseInt(commandE.getAttribute("documentsPerFilter")));
                 }
 
                 if (sc instanceof StormWeatherSearchConfiguration) {
                     final StormWeatherSearchConfiguration swsc = (StormWeatherSearchConfiguration) sc;
                     if (commandE.getAttribute("xml-elements").length() > 0) {
                         final String[] elms = commandE.getAttribute("xml-elements").split(",");
                         for (String elm : elms) {
                             swsc.addElementValue(elm.trim());
                         }
                     }
 
                     // Add inherited xml elemts.
                     if (inherit instanceof StormWeatherSearchConfiguration) {
                         final StormWeatherSearchConfiguration swsi = (StormWeatherSearchConfiguration) inherit;
                         for (String elm : swsi.getElementValues()) {
                             swsc.addElementValue(elm);
                         }
                     }
                 }
 
                 if (sc instanceof TvSearchConfiguration) {
                     final TvSearchConfiguration tssc = (TvSearchConfiguration) sc;
                     final String[] defaultChannels = commandE.getAttribute("default-channels").split(",");
                     for (String channel : defaultChannels) {
                         tssc.addDefaultChannel(channel.trim());
                     }
                     // TODO use fillBeanProperty pattern instead
                     tssc.setResultsToFetch(Integer.parseInt(commandE.getAttribute("results-to-fetch")));
 
                 }
 
                 if (sc instanceof TvWaitSearchConfiguration) {
                     final TvWaitSearchConfiguration twsc = (TvWaitSearchConfiguration) sc;
                     fillBeanProperty(twsc, inherit, "index", ParseType.Int, commandE, "0");
                     fillBeanProperty(twsc, inherit, "waitOn", ParseType.String, commandE, null);
                     fillBeanProperty(twsc, inherit, "useMyChannels", ParseType.Boolean, commandE, "false");
                 }
 
                 if (sc instanceof TvEnrichSearchConfiguration) {
                     final TvEnrichSearchConfiguration tesc = (TvEnrichSearchConfiguration) sc;
                     fillBeanProperty(tesc, inherit, "waitOn", ParseType.String, commandE, null);
                 }
 
                 if (sc instanceof CatalogueSearchConfiguration) {
                     final CatalogueSearchConfiguration csc = (CatalogueSearchConfiguration) sc;
                     fillBeanProperty(csc, inherit, "queryParameterWhere", ParseType.String, commandE, "");
                     fillBeanProperty(csc, inherit, "searchBy", ParseType.String, commandE, "");
                     fillBeanProperty(csc, inherit, "split", ParseType.Boolean, commandE, "false");
                 }
                 if (sc instanceof CatalogueAdsSearchConfiguration) {
                     final CatalogueAdsSearchConfiguration casc = (CatalogueAdsSearchConfiguration) sc;
                     fillBeanProperty(casc, inherit, "queryParameterWhere", ParseType.String, commandE, "");
                 }
 
                 if (sc instanceof CatalogueBannersSearchConfiguration) {
                     final CatalogueBannersSearchConfiguration cbsc = (CatalogueBannersSearchConfiguration) sc;
                     fillBeanProperty(cbsc, inherit, "queryParameterWhere", ParseType.String, commandE, "");
                 }
 
                 if (sc instanceof ClusteringESPFastConfiguration) {
                     final ClusteringESPFastConfiguration cefc = (ClusteringESPFastConfiguration) sc;
                     fillBeanProperty(cefc, inherit, "clusterIdParameter", ParseType.String, commandE, "clusterId");
                     fillBeanProperty(cefc, inherit, "resultsPerCluster", ParseType.Int, commandE, "");
                     fillBeanProperty(cefc, inherit, "clusterField", ParseType.String, commandE, "cluster");
                     fillBeanProperty(cefc, inherit, "clusterMaxFetch", ParseType.Int, commandE, "10");
                     fillBeanProperty(cefc, inherit, "nestedResultsField", ParseType.String, commandE, "entries");
                     fillBeanProperty(cefc, inherit, "sortField", ParseType.String, commandE, "publishedtime");
                     fillBeanProperty(cefc, inherit, "defaultSort", ParseType.String, commandE, "descending");
                     fillBeanProperty(cefc, inherit, "userSortParameter", ParseType.String, commandE, "sort");
                 }
 
                 if (sc instanceof NewsAggregatorSearchConfiguration) {
                     final NewsAggregatorSearchConfiguration nasc = (NewsAggregatorSearchConfiguration) sc;
                     fillBeanProperty(nasc, inherit, "xmlSource", ParseType.String, commandE, "");
                     fillBeanProperty(nasc, inherit, "xmlMainFile", ParseType.String, commandE, "fp_main_main.xml");
                     fillBeanProperty(nasc, inherit, "geographicFields", ParseType.String, commandE, "");
                     fillBeanProperty(nasc, inherit, "categoryFields", ParseType.String, commandE, "");
                 }
 
                 if (sc instanceof NewsMyNewsSearchConfiguration) {
 
                 }
 
                 // query transformers
                 NodeList qtNodeList = commandE.getElementsByTagName("query-transformers");
                 final Element qtRootElement = (Element) qtNodeList.item(0);
                 if (qtRootElement != null) {
                     qtNodeList = qtRootElement.getChildNodes();
 
                     // clear all inherited query-transformers
                     sc.clearQueryTransformers();
 
                     for (int i = 0; i < qtNodeList.getLength(); i++) {
                         final Node node = qtNodeList.item(i);
                         if (!(node instanceof Element)) {
                             continue;
                         }
                         final Element qt = (Element) node;
                         if (queryTransformerFactory.supported(qt.getTagName())) {
                             sc.addQueryTransformer(queryTransformerFactory.parseQueryTransformer(qt));
                         }
                     }
                 }
 
                 // result handlers
                 NodeList rhNodeList = commandE.getElementsByTagName("result-handlers");
                 final Element rhRootElement = (Element) rhNodeList.item(0);
                 if (rhRootElement != null) {
                     rhNodeList = rhRootElement.getChildNodes();
 
                     // clear all inherited result handlers
                     sc.clearResultHandlers();
 
                     for (int i = 0; i < rhNodeList.getLength(); i++) {
                         final Node node = rhNodeList.item(i);
                         if (!(node instanceof Element)) {
                             continue;
                         }
                         final Element rh = (Element) node;
                         if (resultHandlerFactory.supported(rh.getTagName())) {
                             sc.addResultHandler(resultHandlerFactory.parseResultHandler(rh));
                         }
                     }
                 }
 
                 return sc;
 
             } catch (InstantiationException ex) {
                 throw new InfrastructureException(ex);
             } catch (IllegalAccessException ex) {
                 throw new InfrastructureException(ex);
             } catch (SecurityException ex) {
                 throw new InfrastructureException(ex);
             } catch (NoSuchMethodException ex) {
                 throw new InfrastructureException(ex);
             } catch (IllegalArgumentException ex) {
                 throw new InfrastructureException(ex);
             } catch (InvocationTargetException ex) {
                 throw new InfrastructureException(ex);
             } catch (ParserConfigurationException e) {
                 throw new InfrastructureException(e);
             }
         }
 
         private SearchConfiguration findParent(
                 final String id,
                 final SearchMode mode) {
 
             SearchMode m = mode;
             SearchConfiguration config = null;
             do {
                 final Map<String, SearchConfiguration> configs;
                 try {
                     COMMANDS_LOCK.readLock().lock();
                     configs = COMMANDS.get(m);
                 } finally {
                     COMMANDS_LOCK.readLock().unlock();
                 }
                 config = configs.get(id);
                 m = m.getParentSearchMode();
 
             } while (config == null && m != null);
 
             return config;
         }
 
         private Collection<Navigator> parseNavigators(final Element navsE) {
 
             final Collection<Navigator> navigators = new ArrayList<Navigator>();
             final NodeList children = navsE.getChildNodes();
             for (int i = 0; i < children.getLength(); ++i) {
                 final Node child = children.item(i);
                 if (child instanceof Element && "navigator".equals(((Element) child).getTagName())) {
                     final Element navE = (Element) child;
                     final String id = navE.getAttribute("id");
                     final String name = navE.getAttribute("name");
                     final String sortAttr = navE.getAttribute("sort") != null && navE.getAttribute("sort").length() > 0
                             ? navE.getAttribute("sort").toUpperCase() : "COUNT";
                     LOG.info(INFO_PARSING_NAVIGATOR + id + " [" + name + "]" + ", sort=" + sortAttr);
                     final Navigator.Sort sort = Navigator.Sort.valueOf(sortAttr);
 
                     final Navigator nav = new Navigator(
                             name,
                             navE.getAttribute("field"),
                             navE.getAttribute("display-name"),
                             sort);
                     nav.setId(id);
                     final Collection<Navigator> childNavigators = parseNavigators(navE);
                     if (childNavigators.size() > 1) {
                         throw new IllegalStateException(ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED + id);
                     } else if (childNavigators.size() == 1) {
                         nav.setChildNavigator(childNavigators.iterator().next());
                     }
                     navigators.add(nav);
                 }
             }
 
             return navigators;
         }
     }
 
 
     private static final class QueryTransformerFactory extends AbstractFactory {
 
         QueryTransformerFactory() {
         }
 
         QueryTransformerConfig parseQueryTransformer(final Element qt) {
 
             return ((QueryTransformerConfig) construct(qt)).readQueryTransformer(qt);
         }
 
         protected Class<? extends QueryTransformerConfig> findClass(final String xmlName) {
 
             Class clazz = null;
             final String bName = xmlToBeanName(xmlName);
             final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());
 
             LOG.info("findClass " + className);
 
             try {
                 clazz = (Class<? extends QueryTransformerConfig>) Class.forName(
                         "no.schibstedsok.searchportal.query.transform."
                                 + className
                                 + "QueryTransformerConfig");
 
             } catch (ClassNotFoundException cnfe) {
                 LOG.error(cnfe.getMessage(), cnfe);
             }
             return clazz;
         }
 
     }
 
     private static final class ResultHandlerFactory extends AbstractFactory {
 
         ResultHandlerFactory() {
         }
 
         ResultHandlerConfig parseResultHandler(final Element rh) {
 
             return ((ResultHandlerConfig) construct(rh)).readResultHandler(rh);
         }
 
         protected Class<? extends ResultHandlerConfig> findClass(final String xmlName) {
 
             Class clazz = null;
             final String bName = xmlToBeanName(xmlName);
             final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());
 
             LOG.info("findClass " + className);
 
             try {
                 clazz = (Class<? extends ResultHandlerConfig>) Class.forName(
                         "no.schibstedsok.searchportal.result.handler."
                                 + className
                                 + "ResultHandlerConfig");
 
             } catch (ClassNotFoundException cnfe) {
                 LOG.error(cnfe.getMessage(), cnfe);
             }
             return clazz;
         }
 
     }
 
     private static abstract class AbstractFactory {
         AbstractFactory() {
         }
 
         boolean supported(final String xmlName) {
 
             return null != findClass(xmlName);
         }
 
         protected Object construct(final Element element) {
 
             final String xmlName = element.getTagName();
             LOG.info(INFO_CONSTRUCT + xmlName);
 
             try {
                 return findClass(xmlName).newInstance();
 
             } catch (InstantiationException ex) {
                 throw new InfrastructureException(ex);
             } catch (IllegalAccessException ex) {
                 throw new InfrastructureException(ex);
             }
         }
 
         protected abstract Class<? extends Object> findClass(final String xmlName);
     }
 }
