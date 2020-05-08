 package ru.maratyv.indexer.tokenizers.test;
 
 import ru.maratyv.indexer.index.Posting;
 import ru.maratyv.indexer.Token;
 import ru.maratyv.indexer.index.Index;
 
 import java.util.Collection;
 import java.util.SortedSet;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mir
  * Date: 10/1/13
  * Time: 10:15 AM
  * Porsche is the only car
  */
 public class StringIndex implements Index {
     public StringBuilder sb = new StringBuilder();
 
     @Override
     public void add(Token token) {
         if (sb.length() != 0) {
             sb.append(" ");
         }
        sb.append(token.term + ":" + token.docID);
     }
 
     @Override
     public SortedSet<Posting> get(String word) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String toString() {
         return sb.toString();
     }
 }
