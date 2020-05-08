 package engine;
 
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Provides;
 import com.google.inject.name.Names;
 import indexer.NGramAnalyzer;
 import indexer.NGramIndexer;
 import indexer.NGramTokenizer;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created by User: ting
  * Date: 10/14/12
  * Time: 6:44 PM
  */
 public class IndexModule extends AbstractModule {
 
     @Override
     protected void configure() {
         //Bind the analyzer to ngram tokenizer
         bind(String.class)
                .annotatedWith(Names.named("source directory"))
                .toInstance("/home/ting/Documents/iRegexData/Enron2Lvl");
     }
 
     @Provides
     NGramAnalyzer provideNGramAnalyzer() {
        return new NGramAnalyzer(new NGramTokenizer(4));
     }
 
     public static void main(String[] args) throws IOException{
        Directory indexDir = new NIOFSDirectory(new File("/home/ting/Documents/iRegexData/index/tmp"));
 
         Injector injector = Guice.createInjector(new IndexModule());
         NGramIndexer indexer = injector.getInstance(NGramIndexer.class);
         indexer.constructIndex(indexDir);
     }
 
 }
