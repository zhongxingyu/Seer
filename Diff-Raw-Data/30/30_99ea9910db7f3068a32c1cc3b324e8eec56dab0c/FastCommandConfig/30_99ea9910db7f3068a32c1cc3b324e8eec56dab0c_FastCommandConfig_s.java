 /*
  * Copyright (2005-2007) Schibsted Søk AS
  *
  */
 package no.schibstedsok.searchportal.mode.config;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
 import no.schibstedsok.searchportal.result.Navigator;
 import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
 import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
  * @version <tt>$Id$</tt>
  */
 @Controller("FastSearchCommand")
 public class FastCommandConfig extends CommandConfig {
     
     private static final Logger LOG = Logger.getLogger(FastCommandConfig.class);
     
     private static final String[] ALL_COLLECTIONS = {
         "retriever",
         "tv",
         "webcrawlno1",
         "webcrawlno1",
         "webcrawlno1deep1",
         "webcrawlno2",
         "wikipedia",
         "wikipedia2",
         "WikipediaSV",
         "robots",
         "yellow",
         "white",
         "weather",
         "carelscrawl",        
         "moreover",
         "retrievernordic",
         "mano",
         "skiinfo"
     };
 
     private final List<String> collections = new ArrayList<String>();
     private Map searchParameters;
     private boolean lemmatise;
     private boolean spellcheck;
     private String spellchecklanguage;
     private final Map<String, Navigator> navigators = new HashMap<String,Navigator>();
     private String sortBy;
     private boolean collapsing;
     private String queryServerUrl;
     private boolean keywordClusteringEnabled = false;
     private String qtPipeline;
     private transient volatile String collectionString;
     private boolean expansion;
     
     private String resultView;
     private boolean clustering = false;
     private boolean ignoreNavigation = false;
     private int offensiveScoreLimit = 0;
     private int spamScoreLimit = 0;
 
     private String filter;
     private String filtertype;
     private String project;
 
     private boolean relevantQueries = false;
 
     /**
      * 
      */
     public FastCommandConfig(){
     }
 
     /** Copy Constructor. *
      * @param asc 
      */
     public FastCommandConfig(final SearchConfiguration asc){
 
         if(asc != null && asc instanceof FastCommandConfig){
             final FastCommandConfig fsc = (FastCommandConfig) asc;
             collections.addAll(fsc.collections);
             searchParameters = fsc.searchParameters;
             lemmatise = fsc.lemmatise;
             spellcheck = fsc.spellcheck;
             spellchecklanguage = fsc.spellchecklanguage;
             navigators.putAll(fsc.navigators);
             sortBy = fsc.sortBy;
             collapsing = fsc.collapsing;
             queryServerUrl = fsc.queryServerUrl;
             keywordClusteringEnabled = fsc.keywordClusteringEnabled;
             qtPipeline = fsc.qtPipeline;
             collectionString = fsc.collectionString;
             resultView = fsc.resultView;
             clustering = fsc.clustering;
             ignoreNavigation = fsc.ignoreNavigation;
             offensiveScoreLimit = fsc.offensiveScoreLimit;
             spamScoreLimit = fsc.spamScoreLimit;
             filter = fsc.filter;
             filtertype = fsc.filtertype;
             project = fsc.project;
             relevantQueries = fsc.relevantQueries;
         }
     }
 
     /**
      * 
      * @return 
      */
     public List<String> getCollections() {
         return collections;
     }
 
     /**
      * 
      * @param collectionName 
      */
     public void addCollection(final String collectionName) {
         collections.add(collectionName);
     }
 
     /**
      * 
      * @return 
      */
     public String getCollectionFilterString() {
         if (collectionString == null) {
             collectionString = createCollectionFilterString();
         }
 
         return collectionString;
     }
 
     private String createCollectionFilterString() {
         
         String result = "";
         
         if (collections.size() > 1) {
 
             final Collection invertedCollection = new ArrayList(Arrays.asList(ALL_COLLECTIONS));
             invertedCollection.removeAll(collections);
             final String [] coll = prependMetaCollection(invertedCollection);
             result = StringUtils.join(coll, ' ');
 
         } else if (collections.size() == 1) {
             result = "+meta.collection:" + collections.get(0);
         }
         return result;
     }
 
     private String[] prependMetaCollection(final Collection<String> collectionStrings) {
         
         final String coll[] = collectionStrings.toArray(new String[collectionStrings.size()]);
 
         if ("adv".equals(this.filtertype)) {
             for (int i = 0; i < coll.length; i++) {
                 if (i == 0)
                     coll[i] = " size:>0 ANDNOT meta.collection:" + coll[i];
                 else
                     coll[i] = " ANDNOT meta.collection:" + coll[i];
             }
         } else { 
             for (int i = 0; i < coll.length; i++) {
                 coll[i] = " -meta.collection:" + coll[i];
             }            
         }
         return coll;
     }
 
     /**
      * 
      * @return 
      */
     public String getQueryServerUrl() {
         return queryServerUrl;
     }
 
     /**
      * 
      * @return 
      */
     public Map getSearchParameters() {
         return searchParameters;
     }
 
     /**
      * 
      * @param searchParameters 
      */
     public void setSearchParameters(final Map searchParameters) {
         this.searchParameters = searchParameters;
     }
 
     /**
      * 
      * @param parameterName 
      * @param parameterValue 
      */
     public void setParameter(final String parameterName, final Object parameterValue) {
         searchParameters.put(parameterName, parameterValue);
     }
 
     /**
      * 
      * @return 
      */
     public boolean isLemmatise() {
         return lemmatise;
     }
 
     /**
      * 
      * @param lemmatise 
      */
     public void setLemmatise(final boolean lemmatise) {
         this.lemmatise = lemmatise;
     }
 
     /**
      * 
      * @return 
      */
     public boolean isSpellcheck() {
         return spellcheck;
     }
 
     /**
      * 
      * @param spellcheckEnabled 
      */
     public void setSpellcheck(final boolean spellcheckEnabled) {
         this.spellcheck = spellcheckEnabled;
     }
     
     /**
      * 
      * @return 
      */
     public String getSpellchecklanguage() {
         return spellchecklanguage;
     }
 
     /**
      * 
      * @param spellchecklanguage 
      */
     public void setSpellchecklanguage(final String spellchecklanguage) {
         this.spellchecklanguage = spellchecklanguage;
     }
 
 
     /**
      * 
      * @return 
      */
     public Map<String, Navigator> getNavigators() {
         return navigators;
     }
 
     /**
      * 
      * @param navigator 
      * @param navKey 
      */
     public void addNavigator(final Navigator navigator, final String navKey) {
         navigators.put(navKey, navigator);
     }
 
     /**
      * 
      * @param navigatorKey 
      * @return 
      */
     public Navigator getNavigator(final String navigatorKey) {
         return navigators.get(navigatorKey);
     }
 
     /**
      * 
      * @return 
      */
     public String getSortBy() {
         return sortBy;
     }
 
     /**
      * 
      * @param sortBy 
      */
     public void setSortBy(final String sortBy) {
         this.sortBy = sortBy;
     }
 
     /**
      * 
      * @return 
      */
     public boolean isCollapsing() {
         return collapsing;
     }
 
     /**
      * 
      * @param collapsingEnabled 
      */
     public void setCollapsing(final boolean collapsingEnabled) {
         this.collapsing = collapsingEnabled;
     }
 
     /**
      * 
      * @return 
      */
     public String getResultView() {
         return resultView;
     }
 
     /**
      * 
      * @param resultView 
      */
     public void setResultView(final String resultView) {
         this.resultView = resultView;
     }
 
     /**
      * 
      * @param queryServerUrl 
      */
     public void setQueryServerUrl(final String queryServerUrl) {
         this.queryServerUrl = queryServerUrl;
     }
 
     /**
      * 
      * @return 
      */
     public boolean isKeywordClusteringEnabled() {
         return keywordClusteringEnabled;
     }
 
     /**
      * 
      * @param keywordClusteringEnabled 
      */
     public void setKeywordClusteringEnabled(final boolean keywordClusteringEnabled) {
         this.keywordClusteringEnabled = keywordClusteringEnabled;
     }
 
     /**
      * 
      * @return 
      */
     public String getQtPipeline() {
         return qtPipeline;
     }
 
     /**
      * 
      * @param qtPipeline 
      */
     public void setQtPipeline(final String qtPipeline) {
         this.qtPipeline = qtPipeline;
     }
 
     /**
      * 
      * @return 
      */
     public boolean isClustering() {
         return clustering;
     }
 
     /**
      * 
      * @return 
      */
     public boolean isIgnoreNavigation() {
         return ignoreNavigation;
     }
 
     /**
      * 
      * @param ignoreNavigationEnabled 
      */
     public void setIgnoreNavigation(final boolean ignoreNavigationEnabled) {
         this.ignoreNavigation = ignoreNavigationEnabled;
     }
     
     /**
      * 
      * @return 
      */
     public int getOffensiveScoreLimit() {
         return offensiveScoreLimit;
     }
 
     /**
      * 
      * @return 
      */
     public int getSpamScoreLimit() {
         return spamScoreLimit;
     }
 
     /**
      * 
      * @return 
      */
     public boolean isRelevantQueries() {
         return relevantQueries;
     }
 
     /**
      * 
      * @return 
      */
     public String getFilter() {
         return filter;
     }
     
     /**
      * 
      * @return 
      */
     public String getFiltertype() {
         return filtertype;
     }
 
     /**
      * 
      * @param filtertype 
      */
     public void setFiltertype(final String filtertype) {
         this.filtertype = filtertype;
     }
     
     /**
      * 
      * @return 
      */
     public String getProject() {
         return project;
     }
 
     /**
      * 
      * @param project 
      */
     public void setProject(final String project) {
         this.project = project;
     }
 
     void setSpamScoreLimit(final int i) {
         spamScoreLimit = i;
     }
 
     /**
      * Setter for property clustering.
      * 
      * @param clusteringEnabled New value of property clustering.
      */
     public void setClustering(final boolean clusteringEnabled) {
         this.clustering = clusteringEnabled;
     }
 
     /**
      * Setter for property collectionFilterString.
      * @param collectionFilterString New value of property collectionFilterString.
      */
     public void setCollectionFilterString(final String collectionFilterString) {
         this.collectionString = collectionString;
     }
 
     /**
      * Setter for property filter.
      * @param filter New value of property filter.
      */
     public void setFilter(final String filter) {
         this.filter = filter;
     }
 
     /**
      * Setter for property offensiveScoreLimit.
      * @param offensiveScoreLimit New value of property offensiveScoreLimit.
      */
     public void setOffensiveScoreLimit(final int offensiveScoreLimit) {
         this.offensiveScoreLimit = offensiveScoreLimit;
     }
 
     /**
      * Setter for property relevantQueries.
      * 
      * @param relevantQueriesEnabled New value of property relevantQueries.
      */
     public void setRelevantQueries(final boolean relevantQueriesEnabled) {
         this.relevantQueries = relevantQueriesEnabled;
     }
     
     /**
      * Returns true if expansion is enabled. Expansion means the possibility 
      * to retrieve all of the documents that has been collapsed for a domain. If
      * this is set to false the templates won't get the information that there
      * are collapsed documents.
      *
      * @return true if expansion is enabled.
      */
     public boolean isExpansion() {
         return expansion;
     }
 
     /**
      * Setter for the expansionEnabled property.
      *
      * @param expansion 
      */
     public void setExpansion(final boolean expansion) {
         this.expansion = expansion;
     }
 
     @Override
    public CommandConfig readSearchConfiguration(
             final Element element,
             final SearchConfiguration inherit) {
         
         super.readSearchConfiguration(element, inherit);
 
         final FastCommandConfig fscInherit = inherit instanceof FastCommandConfig
                 ? (FastCommandConfig) inherit
                 : null;
         
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "clustering", ParseType.Boolean, element, "false");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "collapsing", ParseType.Boolean, element, "false");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "expansion", ParseType.Boolean, element, "false");
 
         if (element.getAttribute("collections").length() > 0) {
             final String[] collections = element.getAttribute("collections").split(",");
             for (String collection : collections) {
                 addCollection(collection);
             }
         }else if(null != fscInherit){
             collections.addAll(fscInherit.getCollections());
         }
 
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "filter", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "project", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "project", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "filtertype", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "ignoreNavigation", ParseType.Boolean, element, "false");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "offensiveScoreLimit", ParseType.Int, element, "-1");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "qtPipeline", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "queryServerUrl", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "relevantQueries", ParseType.Boolean, element, "false");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "sortBy", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "spamScoreLimit", ParseType.Int, element, "-1");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "spellcheck", ParseType.Boolean, element, "false");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "spellchecklanguage", ParseType.String, element, "");
         AbstractDocumentFactory.fillBeanProperty(this, inherit, "lemmatise", ParseType.Boolean, element, "false");
 
         if (getQueryServerUrl() == null || "".equals(getQueryServerUrl())) {
             LOG.debug("queryServerURL is empty for " + getName());
         }
 
         // navigators
         if (fscInherit != null && fscInherit.getNavigators() != null) {
             
             navigators.putAll(fscInherit.getNavigators());
         }
 
         final NodeList nList = element.getElementsByTagName("navigators");
 
         for (int i = 0; i < nList.getLength(); ++i) {
             final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
             for (Navigator navigator : navigators) {
                 addNavigator(navigator, navigator.getId());
             }
 
         }
 
         return this;
     }
     
     
 }
