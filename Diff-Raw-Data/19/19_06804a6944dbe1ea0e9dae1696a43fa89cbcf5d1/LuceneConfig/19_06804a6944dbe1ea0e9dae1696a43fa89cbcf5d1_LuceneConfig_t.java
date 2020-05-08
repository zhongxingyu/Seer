 package org.wyona.yarep.impl.search.lucene;
 
 import java.io.File;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.log4j.Category;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.WhitespaceAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.tika.config.TikaConfig;
 import org.wyona.commons.io.FileUtil;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.search.Indexer;
 import org.wyona.yarep.core.search.SearchException;
 
 /**
  * Yarep Lucene configuration for Lucene indexer and searcher implementation
  */
 public class LuceneConfig {
     
     private static Category log = Category.getInstance(LuceneConfig.class);
     
     private File fulltextSearchIndexFile = null;
     private File propertiesSearchIndexFile = null;
     private Analyzer fulltextAnalyzer = null;
     private Analyzer propertyAnalyzer = null;
     private String FULLTEXT_INDEX_DIR = "fulltext";
     private String PROPERTIES_INDEX_DIR = "properties";
     private TikaConfig tikaConfig;
     private int writeLockTimeout = 0;
     private Repository repo;
     
     private Indexer indexer = null;
     
     public LuceneConfig(Configuration searchIndexConfig, String configParent, Repository repo) throws SearchException {
         this.repo = repo;
         configure(searchIndexConfig, configParent);
     }
     
     public void configure(Configuration searchConfig, String configParent) throws SearchException {
         try {
             if (searchConfig != null) {
                 if(searchConfig.getNamespace() == "" || searchConfig.getNamespace() == null) {
                     deprecatedConfigure(searchConfig, configParent);
                     return;
                 }
                 
                 File searchIndexSrcFile = new File(searchConfig.getChild("index-location").getAttribute("file", "index"));
                 if (!searchIndexSrcFile.isAbsolute()) {
                     searchIndexSrcFile = FileUtil.file(configParent, searchIndexSrcFile.toString());
                 }
 
                 Configuration luceneConfig = searchConfig.getChild("lucene");
                 
                 String fulltextAnalyzerClass = luceneConfig.getChild("fulltext-analyzer").getAttribute("class","org.apache.lucene.analysis.standard.StandardAnalyzer");
                 fulltextAnalyzer = (Analyzer) Class.forName(fulltextAnalyzerClass).newInstance();
                 
                 // TODO: For search within properties the WhitespaceAnalyzer is used because the StandardAnalyzer doesn't accept resp. misinterprets escaped query strings, e.g. 03\:07\- ...
                 String propertyAnalyzerClass = luceneConfig.getChild("property-analyzer").getAttribute("class","org.apache.lucene.analysis.WhitespaceAnalyzer");
                 propertyAnalyzer = (Analyzer) Class.forName(propertyAnalyzerClass).newInstance();
 
                 
                 fulltextSearchIndexFile = new File(searchIndexSrcFile, FULLTEXT_INDEX_DIR);
                // Because of backwards compatibility the source directory is used as fulltext directory
                 if (!fulltextSearchIndexFile.isDirectory() && searchIndexSrcFile.exists()) {
                    log.warn("Because '" + fulltextSearchIndexFile + "' does not exist, the source directory is used as fulltext directory: " + searchIndexSrcFile);
                     fulltextSearchIndexFile = searchIndexSrcFile;
                 }
                if (!fulltextSearchIndexFile.exists()) {
                    log.warn("No such 'fulltext' search index path: " + fulltextSearchIndexFile);
                } else {
                    log.info("Fulltext search index path: " + fulltextSearchIndexFile);
                }
                 
                 // Create a lucene search index if it doesn't exist yet
                 // IMPORTANT: This doesn't work within a clustered environment, because the cluster node starting first will lock the index and all other nodes will not be able to startup!
                 //this.indexWriter = createIndexWriter(fulltextSearchIndexFile, analyzer);
                 
                 String localTikaConfigSrc = luceneConfig.getChild("local-tika-config").getAttribute("file", null);
                 if (localTikaConfigSrc != null) {
                     File localTikaConfigFile = new File(localTikaConfigSrc);
                     if (!localTikaConfigFile.isAbsolute()) {
                         localTikaConfigFile = FileUtil.file(configParent, localTikaConfigFile.toString());
                     }
                     if (localTikaConfigFile.isFile()) {
                         log.warn("Use local tika config: " + localTikaConfigFile.getAbsolutePath());
                         tikaConfig = new TikaConfig(localTikaConfigFile);
                     } else {
                         log.error("No such file: " + localTikaConfigFile + " (Default tika config will be used)");
                         tikaConfig = TikaConfig.getDefaultConfig();
                     }
                 } else {
                     log.info("Use default tika config");
                     tikaConfig = TikaConfig.getDefaultConfig();
                 }
                 
                 // Create properties index dir subdirectory in order to save the lucene index for searching on properties
                 propertiesSearchIndexFile = new File(searchIndexSrcFile, PROPERTIES_INDEX_DIR);
                if (!propertiesSearchIndexFile.exists()) {
                    log.warn("No such 'properties' search index path: " + propertiesSearchIndexFile);
                } else {
                    log.info("Properties search index path: " + propertiesSearchIndexFile);
                }
                 
                 // IMPORTANT: This doesn't work within a clustered environment, because the cluster node starting first will lock the index and all other nodes will not be able to startup!
                 //this.propertiesIndexWriter = createIndexWriter(propertiesSearchIndexFile, whitespaceAnalyzer);
                 writeLockTimeout = 14;//luceneConfig.getChild("write-lock-timeout").getAttributeAsInteger("ms");
 
             } else {
                 log.warn("No search index dir (<search-index src=\"...\"/>) configured within: " + configParent);
             }
         } catch (Exception e) {
             log.error(e.toString());
             throw new SearchException("Could not read repository configuration: " 
                     + e.getMessage(), e);
         }
     }
 
     
     /**
      * @deprecated
      * @param searchIndexConfig
      * @param configParent
      */
     public void deprecatedConfigure(Configuration searchIndexConfig, String configParent) throws SearchException {
         log.warn("This config schema is deprecated (" + repo.getConfigFile() + ")! Use the new schema described at http://svn.wyona.com/repos/public/yarep/trunk/src/test/repository/new-vfs-example/repository.xml");
         try {
             if (searchIndexConfig != null) {
                 File searchIndexSrcFile = new File(searchIndexConfig.getAttribute("src", "index"));
                 if (!searchIndexSrcFile.isAbsolute()) {
                     searchIndexSrcFile = FileUtil.file(configParent, searchIndexSrcFile.toString());
                 }
                 
                 boolean isFulltextIndexingEnabled = searchIndexConfig.getAttributeAsBoolean(
                         "index-fulltext", true);
                 boolean isPropertyIndexingEnabled = searchIndexConfig.getAttributeAsBoolean(
                         "index-properties", true);
                 
                 fulltextAnalyzer = new StandardAnalyzer();
                 // TODO: For search within properties the WhitespaceAnalyzer is used because the StandardAnalyzer doesn't accept resp. misinterprets escaped query strings, e.g. 03\:07\- ...
                 propertyAnalyzer = new WhitespaceAnalyzer();
                 
                 indexer = new LuceneIndexer();
                 
                 fulltextSearchIndexFile = new File(searchIndexSrcFile, FULLTEXT_INDEX_DIR);
                 if (!fulltextSearchIndexFile.isDirectory() && searchIndexSrcFile.exists()) {
                     fulltextSearchIndexFile = searchIndexSrcFile;
                 }
                 log.info("Fulltext search index path: " + fulltextSearchIndexFile);
                 
                 // Create a lucene search index if it doesn't exist yet
                 // IMPORTANT: This doesn't work within a clustered environment, because the cluster node starting first will lock the index and all other nodes will not be able to startup!
                 //this.indexWriter = createIndexWriter(fulltextSearchIndexFile, analyzer);
                 
                 String localTikaConfigSrc = searchIndexConfig.getAttribute("local-tika-config", null);
                 if (localTikaConfigSrc != null) {
                     File localTikaConfigFile = new File(localTikaConfigSrc);
                     if (!localTikaConfigFile.isAbsolute()) {
                         localTikaConfigFile = FileUtil.file(configParent, localTikaConfigFile.toString());
                     }
                     if (localTikaConfigFile.isFile()) {
                         log.warn("Use local tika config: " + localTikaConfigFile.getAbsolutePath());
                         tikaConfig = new TikaConfig(localTikaConfigFile);
                     } else {
                         log.error("No such file: " + localTikaConfigFile + " (Default tika config will be used)");
                         tikaConfig = TikaConfig.getDefaultConfig();
                     }
                 } else {
                     log.info("Use default tika config");
                     tikaConfig = TikaConfig.getDefaultConfig();
                 }
                 
                 // Create properties index dir subdirectory in order to save the lucene index for searching on properties
                 propertiesSearchIndexFile = new File(searchIndexSrcFile, PROPERTIES_INDEX_DIR);
                 log.warn("Properties search index path: " + propertiesSearchIndexFile);
                 
                 // IMPORTANT: This doesn't work within a clustered environment, because the cluster node starting first will lock the index and all other nodes will not be able to startup!
                 //this.propertiesIndexWriter = createIndexWriter(propertiesSearchIndexFile, whitespaceAnalyzer);
                 writeLockTimeout = 14;//searchIndexConfig.getAttribute("write-lock-timeout", 14);
             } else {
                 log.warn("No search index dir (<search-index src=\"...\"/>) configured.");
             }
             
         } catch (Exception e) {
             log.error("Could not read configuration", e);
         }
     }
     
     public File getFulltextSearchIndexFile() {
         return fulltextSearchIndexFile;
     }
 
     public File getPropertiesSearchIndexFile() {
         return propertiesSearchIndexFile;
     }
 
     public Analyzer getFulltextAnalyzer() {
         return fulltextAnalyzer;
     }
 
     public Analyzer getPropertyAnalyzer() {
         return propertyAnalyzer;
     }
 
     public String getFULLTEXT_INDEX_DIR() {
         return FULLTEXT_INDEX_DIR;
     }
 
     public String getPROPERTIES_INDEX_DIR() {
         return PROPERTIES_INDEX_DIR;
     }
 
     public Indexer getIndexer() {
         return indexer;
     }
 
     public TikaConfig getTikaConfig() {
         return tikaConfig;
     }
     
     public int getWriteLockTimeout() {
         return writeLockTimeout;
     }
 
     public Repository getRepo() {
         return repo;
     }
 }
