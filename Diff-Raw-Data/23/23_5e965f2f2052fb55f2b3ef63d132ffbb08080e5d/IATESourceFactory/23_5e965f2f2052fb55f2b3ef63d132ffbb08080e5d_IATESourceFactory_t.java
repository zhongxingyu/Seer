 package eu.monnetproject.translation.sources.iate;
 
 import eu.monnetproject.config.Configurator;
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.translation.TranslationSource;
 import eu.monnetproject.translation.TranslationSourceFactory;
 import eu.monnetproject.translation.monitor.Messages;
 
 import java.util.Properties;
 
 public class IATESourceFactory implements TranslationSourceFactory {
 
 	@Override
 	public TranslationSource getSource(Language srcLang, Language trgLang) {
 		final Properties config = Configurator.getConfig("eu.monnetproject.translation.sources.iate");
 		Boolean use = Boolean.parseBoolean(config.getProperty("use"));
 		if (use) {
 			try {
				TranslationSource source = null; 
				IATEReaderSource iateSource = new IATEReaderSource(srcLang, trgLang, config);
				if(iateSource.isSourceWorking()) 
					source = iateSource;				
 				Messages.info("IATE Source available");
 				return source;
 			} catch(Exception e) {
 				e.printStackTrace();
 				Messages.warning("IATE Factory returned null");				
 				return null;
 			}			
 		} else {
 			return null;
 		}
 	}
 }
