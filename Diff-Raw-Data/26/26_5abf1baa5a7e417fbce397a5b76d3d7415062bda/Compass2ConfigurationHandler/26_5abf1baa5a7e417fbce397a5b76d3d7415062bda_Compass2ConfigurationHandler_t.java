 package no.ovitas.compass2.config;
 
 import java.io.File;
 
 import no.ovitas.compass2.exception.ConfigurationException;
 
 import org.apache.commons.digester.Digester;
 import org.apache.log4j.Logger;
 
 /**
  * @author csanyi
  * 
  */
 public class Compass2ConfigurationHandler {
 	
 	// Attributes
 	
 	private static Logger logger = Logger.getLogger(Compass2ConfigurationHandler.class);
 	protected Compass2Configuration config = null;
 	private static Compass2ConfigurationHandler instance = null;
 
 	
 	public static Compass2ConfigurationHandler getInstance(){
 		if(instance==null){
 			instance = new Compass2ConfigurationHandler();
 		}
 		return instance;
 	}
 	// Getter / setter methods
 	
 	public Compass2Configuration getConfig() {
 		return config;
 	}
 
 	
 	// Constructor
 	public Compass2ConfigurationHandler() {
 	}
 
 	// Methods
 	
 	/**
 	 * Actual content loading.
 	 */
 	public void loadConfig(String configPath) throws ConfigurationException{
 	
 		Digester digester = new Digester();
 		setupDigester(digester);
 		digester.setValidating(true);
 		digester.setStackAction(new ConfigDigesterStackAction());
 		
 		File cfile = new File( configPath);
 		if(cfile!=null && cfile.canRead() && cfile.isFile()){
 			try {
 				config = (Compass2Configuration)digester.parse(cfile);
 			} catch (Exception e) {
 				logger.fatal("Exception occured while loading and processing the configuration: " + e.getMessage());
 				throw new ConfigurationException("Exception occured while loading and processing the configuration: " + e.getMessage(),e);
 			}
 		}
 	}
 	
 	/**
 	 * Setup digester
 	 */
 	private void setupDigester(Digester digester){
 
 		String prefix = ConfigConstants.TAG_COPMASS2_CONFIG;
 		
 		String fullTextSearchTag				= prefix + "/" + ConfigConstants.TAG_FULL_TEXT_SEARCH;
 		String languageToolsTag					= prefix + "/" + ConfigConstants.TAG_LANGUAGE_TOOLS;
 		String knowledgeBasesTag				= prefix + "/" + ConfigConstants.TAG_KNOWLEDGE_BASES;
 		String resultTag						= prefix + "/" + ConfigConstants.TAG_RESULT;
 		
 		String fullTextSearchImplementationTag	= fullTextSearchTag		+ "/" + ConfigConstants.TAG_FULL_TEXT_SEARCH_IMPLEMENTATION;
 		String contentIndexerImplementationTag	= fullTextSearchTag		+ "/" + ConfigConstants.TAG_CONTENT_INDEXER_IMPLEMENTATION;
 		String languageToolsImplementationTag	= languageToolsTag		+ "/" + ConfigConstants.TAG_LANGUAGE_TOOLS_IMPLEMENTATION;
 		String knowledgeBaseTag					= knowledgeBasesTag		+ "/" + ConfigConstants.TAG_KNOWLEDGE_BASE;
 		String knowledgeBaseImplementationTag	= knowledgeBaseTag		+ "/" + ConfigConstants.TAG_KNOWLEDGE_BASE_IMPLEMENTATION;
 		String expansionTag						= knowledgeBaseTag		+ "/" + ConfigConstants.TAG_EXPANSION;
 		String associationTypesTag 				= expansionTag			+ "/" + ConfigConstants.TAG_ASSOCIATION_TYPES;
 		String associationTypeTag				= associationTypesTag	+ "/" + ConfigConstants.TAG_ASSOCIATION_TYPE;
 		String paramTag							= ConfigConstants.TAG_PARAMS + "/"	+ ConfigConstants.TAG_PARAM;
 		
 		// Compass2Configuration
 		digester.addObjectCreate(prefix, Compass2Configuration.class);
 
 		// FullTextSearch
 		digester.addObjectCreate (fullTextSearchTag, FullTextSearch.class);
 		digester.addSetNext		 (fullTextSearchTag, "setFullTextSearch");
 		digester.addSetProperties(fullTextSearchTag, ConfigConstants.ATTR_PREFIX_MATCH, "prefixMatch");
 		digester.addSetProperties(fullTextSearchTag, ConfigConstants.ATTR_FUZZY_MATCH, "fuzzyMatch");
 		
 		
 		// FullTextSearchImplementation
 		digester.addObjectCreate (fullTextSearchImplementationTag, FullTextSearchImplementation.class);
 		digester.addSetNext		 (fullTextSearchImplementationTag, "setFullTextSearchImplementation");
 		digester.addSetProperties(fullTextSearchImplementationTag, ConfigConstants.ATTR_CLASS, "className");
 		
 		// FullTextSearchImplementation Params
 		digester.addObjectCreate (fullTextSearchImplementationTag + "/" + ConfigConstants.TAG_PARAMS, ParamContainer.class);
 		digester.addSetNext		 (fullTextSearchImplementationTag + "/" + ConfigConstants.TAG_PARAMS, "setParams");
 
 		// FullTextSearchImplementation Param
 		digester.addObjectCreate (fullTextSearchImplementationTag + "/" + paramTag, Param.class);
 		digester.addSetNext      (fullTextSearchImplementationTag + "/" + paramTag, "addParam");
 		digester.addSetProperties(fullTextSearchImplementationTag + "/" + paramTag, ConfigConstants.ATTR_NAME, "name");
 		digester.addSetProperties(fullTextSearchImplementationTag + "/" + paramTag, ConfigConstants.ATTR_VALUE, "value");
 		
 		// ContentIndexerImplementation
 		digester.addObjectCreate (contentIndexerImplementationTag, ContentIndexerImplementation.class);
 		digester.addSetNext		 (contentIndexerImplementationTag, "setContentIndexerImplementation");
 		digester.addSetProperties(contentIndexerImplementationTag, ConfigConstants.ATTR_CLASS, "className");
 		
 		// LanguageTools
 		digester.addObjectCreate (languageToolsTag, LanguageTools.class);
 		digester.addSetNext(languageToolsTag, "setLanguageTools");
 		
 		// LanguageToolsImplementation
 		digester.addObjectCreate (languageToolsImplementationTag, LanguageToolsImplementation.class);
 		digester.addSetNext		 (languageToolsImplementationTag, "setLanguageToolsImplementation");
 		digester.addSetProperties(languageToolsImplementationTag, ConfigConstants.ATTR_CLASS, "className");
 		
 		// LanguageToolsImplementation Params
 		digester.addObjectCreate (languageToolsImplementationTag + "/" + ConfigConstants.TAG_PARAMS, ParamContainer.class);
 		digester.addSetNext		 (languageToolsImplementationTag + "/" + ConfigConstants.TAG_PARAMS, "setParams");
 
 		// LanguageToolsImplementation Param
 		digester.addObjectCreate (languageToolsImplementationTag + "/" + paramTag, Param.class);
 		digester.addSetNext      (languageToolsImplementationTag + "/" + paramTag, "addParam");
 		digester.addSetProperties(languageToolsImplementationTag + "/" + paramTag, ConfigConstants.ATTR_NAME, "name");
 		digester.addSetProperties(languageToolsImplementationTag + "/" + paramTag, ConfigConstants.ATTR_VALUE, "value");
 		
 		// KnowledgeBases
 		digester.addObjectCreate (knowledgeBasesTag, KnowledgeBases.class);
 		digester.addSetNext		 (knowledgeBasesTag, "setKnowledgeBases");
 		
 		// KnowledgeBase
 		digester.addObjectCreate (knowledgeBaseTag, KnowledgeBase.class);
 		digester.addSetNext		 (knowledgeBaseTag, "addElement");
 		digester.addSetProperties(knowledgeBaseTag, ConfigConstants.ATTR_NAME, "name");
 		
 		// KnowledgeBaseImplementation
 		digester.addObjectCreate (knowledgeBaseImplementationTag, KnowledgeBaseImplementation.class);
 		digester.addSetNext		 (knowledgeBaseImplementationTag, "setKnowledgeBaseImplementation");
 		digester.addSetProperties(knowledgeBaseImplementationTag, ConfigConstants.ATTR_CLASS, "className");
 		
 		// KnowledgeBaseImplementation Params
 		digester.addObjectCreate (knowledgeBaseImplementationTag + "/" + ConfigConstants.TAG_PARAMS, ParamContainer.class);
 		digester.addSetNext		 (knowledgeBaseImplementationTag + "/" + ConfigConstants.TAG_PARAMS, "setParams");
 
 		// KnowledgeBaseImplementation Param
 		digester.addObjectCreate (knowledgeBaseImplementationTag + "/" + paramTag, Param.class);
 		digester.addSetNext      (knowledgeBaseImplementationTag + "/" + paramTag, "addParam");
 		digester.addSetProperties(knowledgeBaseImplementationTag + "/" + paramTag, ConfigConstants.ATTR_NAME, "name");
 		digester.addSetProperties(knowledgeBaseImplementationTag + "/" + paramTag, ConfigConstants.ATTR_VALUE, "value");
 		
 		// Expansion
 		digester.addObjectCreate (expansionTag, Expansion.class);
 		digester.addSetNext		 (expansionTag, "setExpansion");
 		digester.addSetProperties(expansionTag, ConfigConstants.ATTR_USE_RANDOM_WEIGHT, "useRandomWeight");
 		digester.addSetProperties(expansionTag, ConfigConstants.ATTR_PREFIX_MATCH, "prefixMatch");
 		digester.addSetProperties(expansionTag, ConfigConstants.ATTR_EXPANSION_THRESHOLD, "expansionThreshold");
 		digester.addSetProperties(expansionTag, ConfigConstants.ATTR_MAX_NUM_OF_TOPIC_TO_EXPAND, "maxNumOfTopicToExpand");
 		
 		// AssociationTypes
 		digester.addObjectCreate (associationTypesTag, AssociationTypes.class);	
		digester.addSetNext		 (associationTypesTag, "setAssociationTypes");
 		
 		// AssociationType
 		digester.addObjectCreate (associationTypeTag, AssociationType.class);
 		digester.addSetNext      (associationTypeTag, "addElement");
 		digester.addSetProperties(associationTypeTag, ConfigConstants.ATTR_ID, "id");
 		digester.addSetProperties(associationTypeTag, ConfigConstants.ATTR_NAME, "name");
 		digester.addSetProperties(associationTypeTag, ConfigConstants.ATTR_WEIGHT_AHEAD, "weightAhead");
 		digester.addSetProperties(associationTypeTag, ConfigConstants.ATTR_WEIGHT_ABACK, "weigthAback");
 		
 		// Result
 		digester.addObjectCreate (resultTag, Result.class);
 		digester.addSetNext		 (resultTag, "setResult");
 		digester.addSetProperties(resultTag, ConfigConstants.ATTR_RESULT_THRESHOLD, "resultThreshold");
 		
 	}
 
 }
