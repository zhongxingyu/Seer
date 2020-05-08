 package ru.maratyv.indexer;
 
 import ru.maratyv.indexer.index.HashMapIndex;
 import ru.maratyv.indexer.index.Index;
 import ru.maratyv.indexer.index.Posting;
 import ru.maratyv.indexer.tokenizers.FileTokenizer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mir
  * Date: 9/30/13
  * Time: 4:33 PM
  * Porsche is the only car
  */
 public class FileIndexer implements Indexer {
 
     private Index index = new HashMapIndex();
 
     public FileIndexer(File inputFile) throws IOException, DerictoryIsNotSpecified {
         index(inputFile);
     }
 
     public Collection<Posting> find(String term) {
         if (term == null) return new ArrayList<Posting>(0);
         Collection<Posting> found = index.get(term.toLowerCase());
         if (found == null) found = new ArrayList<Posting>(0);
         return found;
     }
 
     @Override
     public Collection<Posting> findBoth(String word1, String word2) {
         SortedSet<Posting> postingsWord1 = index.get(word1);
         SortedSet<Posting> postingsWord2 = index.get(word2);
         List<Posting> intersection = new ArrayList<Posting>();
         Iterator<Posting> iteratorWord2 = postingsWord2.iterator();
         Posting word2Posting;
         if (iteratorWord2.hasNext()) {
             word2Posting = iteratorWord2.next();
         } else {
             return intersection;
         }
         for (Posting word1Posting:postingsWord1) {
             while (word1Posting.compareTo(word2Posting) > 0) {
                 if (iteratorWord2.hasNext()) {
                     word2Posting = iteratorWord2.next();
                 } else {
                     return intersection;
                 }
             }
             if (word1Posting.compareTo(word2Posting) == 0) {
                 intersection.add(word1Posting);
             }
         }
         return intersection;
     }
 
     @Override
     public void index(File inputFile) throws IOException, DerictoryIsNotSpecified {
         if (inputFile.isFile()) {
            FileTokenizer ft = new FileTokenizer(inputFile);
            ft.addTokensTo(index);
         } else if (inputFile.isDirectory()) {
             for (File file:inputFile.listFiles()) {
                index(file);
             }
         } else {
             throw new DerictoryIsNotSpecified();
         }
     }
 }
