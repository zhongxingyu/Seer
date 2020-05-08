 package net.cyklotron.cms.search.analysis;
 
 import java.io.IOException;
 
 import org.apache.lucene.analysis.pl.PolishAnalyzer;
 import org.apache.lucene.analysis.stempel.StempelStemmer;
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
        final StempelStemmer stempelStemmer = new StempelStemmer(
            fileSystem.getInputStream(PolishAnalyzer.DEFAULT_STEMMER_FILE));
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
