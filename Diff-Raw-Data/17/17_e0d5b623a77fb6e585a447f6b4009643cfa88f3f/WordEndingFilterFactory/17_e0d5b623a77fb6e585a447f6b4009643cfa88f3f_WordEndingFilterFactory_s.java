 package biz.ixxi.analysis.wordending;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.elasticsearch.common.inject.Inject;
 import org.elasticsearch.common.inject.assistedinject.Assisted;
 import org.elasticsearch.common.settings.Settings;
 import org.elasticsearch.index.Index;
 import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
 import org.elasticsearch.index.analysis.AnalysisSettingsRequired;
 import org.elasticsearch.index.settings.IndexSettings;
 
 
@AnalysisSettingsRequired
 public class WordEndingFilterFactory extends AbstractTokenFilterFactory {
 
     private final String mode;
 
     @Inject
     public WordEndingFilterFactory(Index index, @IndexSettings Settings indexSettings,
                                       @Assisted String name, @Assisted Settings settings) {
     	
         super(index, indexSettings, name, settings);
        this.mode = settings.get("node.mode", "default");
        System.out.println("*************************************pouet**********");
        System.out.println(settings);
        logger.debug("*********************** pouet *****************************");
        logger.debug(settings.getAsMap().toString());
     }
 
     @Override
     final public TokenStream create(TokenStream tokenStream) {
         return new WordEndingFilter(tokenStream, this.mode);
     }
 }
