 /** NgramAnnotator.java
  * @author Weston Feely
  */
 
 package edu.cmu.deiis.annotators;
 
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.cas.FSIndex;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSArray;
 import org.apache.uima.tutorial.RoomNumber;
 
 import edu.cmu.deiis.types.*;
 
 /**
  * Annotator that creates 1-,2-, and 3-grams from annotated tokens.
  */
 public class NgramAnnotator extends JCasAnnotator_ImplBase {
   public void process (JCas aJCas) {
     // get tokens
     FSIndex tokenIndex = aJCas.getAnnotationIndex(Token.type);
     // loop over tokens
     Iterator tokenIter = tokenIndex.iterator();
     Token penult = null;
     Token antepenult = null;
     while (tokenIter.hasNext()) {
       // grab a token
       Token token = (Token) tokenIter.next();
       // check previous tokens
       if (penult == null && antepenult == null) {
         // Case 1: Previous two tokens are not set
         // set previous token
         penult = token;
       }
       else if (antepenult == null)
       {
         // Case 2: Penultimate token is set, antepenultimate token is not set
         // make a bigram
         NGram bigram = new NGram(aJCas);
         bigram.setElementType("Bigram");
         bigram.setElements(new FSArray(aJCas, 2));
         // add tokens to bigram
         bigram.setElements(0,penult);
         bigram.setElements(1,token);
         // set bigram begin and end
         bigram.setBegin(penult.getBegin());
         bigram.setEnd(token.getEnd());
         // add bigram to indexes
         bigram.addToIndexes();
         // set previous two tokens
         antepenult = penult;
         penult = token;
       }
       else {
         // Case 3: Previous two tokens are set
         // make a bigram
         NGram bigram = new NGram(aJCas);
         bigram.setElementType("Bigram");
         bigram.setElements(new FSArray(aJCas, 2));
         // add tokens to bigram
         bigram.setElements(0,penult);
         bigram.setElements(1,token);
         // set bigram begin and end
         bigram.setBegin(penult.getBegin());
         bigram.setEnd(token.getEnd());
         // add bigram to indexes
         bigram.addToIndexes();
         // make a trigram
         NGram trigram = new NGram(aJCas);
        trigram.setElementType("Trigram");
         trigram.setElements(new FSArray(aJCas, 3));
         // add tokens to trigram
         trigram.setElements(0,antepenult);
         trigram.setElements(1,penult);
         trigram.setElements(2,token);
         // set trigram begin and end
         trigram.setBegin(antepenult.getBegin());
         trigram.setEnd(token.getEnd());
         // add trigram to indexes
         trigram.addToIndexes();
         // set previous two tokens
         antepenult = penult;
         penult = token;
       }
       // set up a unigram
       NGram unigram = new NGram(aJCas);
       unigram.setElementType("Unigram");
       unigram.setElements(new FSArray(aJCas, 1));
       // add token to unigram
       unigram.setElements(0,token);
       // set unigram begin and end
       unigram.setBegin(token.getBegin());
       unigram.setEnd(token.getEnd());
       // add unigram to indexes
       unigram.addToIndexes();
     }
   }
 }
