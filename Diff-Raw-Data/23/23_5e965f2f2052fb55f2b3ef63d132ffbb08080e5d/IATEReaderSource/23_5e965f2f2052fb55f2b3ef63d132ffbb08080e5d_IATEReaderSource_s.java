 package eu.monnetproject.translation.sources.iate;
 
 
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.translation.Chunk;
 import eu.monnetproject.translation.PhraseTable;
 import eu.monnetproject.translation.TranslationSource;
 import eu.monnetproject.translation.sources.cache.NRTCacheIndexer;
 
 public class IATEReaderSource implements TranslationSource {
 	private final Language srcLang, trgLang;
 	private Properties config;
 	private NRTCacheIndexer cacheIndexer;
 	private String[] contexts;
	
 	public IATEReaderSource(Language srcLang, Language trgLang, Properties config) {
 		this.srcLang = srcLang;
 		this.trgLang = trgLang;
 		this.config = config;
 		cacheIndexer = new NRTCacheIndexer(this.config, srcLang, trgLang, true);
 		contexts = config.getProperty("domains").split(";");
 	}
 
 	@Override
 	public String getName() {
 		return "IATE";
 	}
 
 	@Override
 	public void close() {		
 		cacheIndexer.close();
 	}
 
 	@Override
 	public PhraseTable candidates(Chunk chunk) {
 		Set<String> translations = new HashSet<String>();
 		Set<String> cacheResults = null;	
 		for(String context : contexts) {	
 			cacheResults = cacheIndexer.getTranslations(chunk.getSource().toLowerCase().trim(), "domain" + context.trim());			
 			if(!(cacheResults == null)) {
 				if(!(cacheResults.contains("koitranslationnahihaiiskaiatepe"))) 
 					translations.addAll(cacheResults);			
 			}
 		}		
 		return new PhraseTableImpl(translations, chunk.getSource(), srcLang, trgLang, getName());
 	}
 
 
 	@Override
 	public String[] featureNames() {	
 		String[] featureNames = new String[1];
 		featureNames[0] = "inIATE";
 		return featureNames;
 	}
		
 }
 
 
 
 
