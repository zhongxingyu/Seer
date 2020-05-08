 package net.cyklotron.cms.search.analysis;
 
 import java.io.IOException;
 
 import org.apache.lucene.analysis.pl.PolishAnalyzer;
 import org.apache.lucene.analysis.stempel.StempelStemmer;
import org.egothor.stemmer.Trie;
 import org.objectledge.filesystem.FileSystem;
 
 public class StempelStemmerFactoryImpl
     implements StempelStemmerFactory
 {
 
     final private FileSystem fileSystem;
 
     public StempelStemmerFactoryImpl(FileSystem fileSystem)
     {
         this.fileSystem = fileSystem;
     }
 
     @Override
     public Stemmer createStempelStemmer()
         throws IOException
     {
        // TODO fileSysetm didn't find resource, tell me why during review
        // URL resource = fileSystem.getResource(PolishAnalyzer.DEFAULT_STEMMER_FILE);
        // fileSystem.getInputStream(PolishAnalyzer.DEFAULT_STEMMER_FILE));
        Trie loaded = StempelStemmer.load(PolishAnalyzer.class
            .getResourceAsStream(PolishAnalyzer.DEFAULT_STEMMER_FILE));
        final StempelStemmer stempelStemmer = new StempelStemmer(loaded);
         return new Stemmer()
             {
                 @Override
                 public CharSequence stem(CharSequence term)
                 {
                     return stempelStemmer.stem(term);
                 }
             };
     }
 }
