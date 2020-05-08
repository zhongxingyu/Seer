 package com.lucid.analysis;
 
 
 import org.apache.lucene.analysis.TokenFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
 
 import java.io.IOException;
 
 /**
  * A high-performance kstem filter for english.
  * <p/>
  * See <a href="http://ciir.cs.umass.edu/pubfiles/ir-35.pdf">
  * "Viewing Morphology as an Inference Process"</a>
  * (Krovetz, R., Proceedings of the Sixteenth Annual International ACM SIGIR
  * Conference on Research and Development in Information Retrieval, 191-203, 1993).
  * <p/>
  * All terms must already be lowercased for this filter to work correctly.
  * <p/>
  * Copyright 2008, Lucid Imagination, Inc.
  */
 
 public class LucidKStemFilter extends TokenFilter {
   private final LucidKStemmer stemmer = new LucidKStemmer();
   private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
   private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
 
   public LucidKStemFilter(TokenStream in) {
     super(in);
   }
 
   /**
    * Returns the next, stemmed, input Token.
    *
    * @return The stemed form of a token.
    * @throws IOException
    */
   @Override
   public final boolean incrementToken() throws IOException {
     if (!input.incrementToken())
       return false;
 
     char[] term = termAttribute.buffer();
     int len = termAttribute.length();
     if ((!keywordAtt.isKeyword()) && stemmer.stem(term, len)) {
      char[] chars = stemmer.asString().toCharArray();
      termAttribute.copyBuffer(chars, 0, chars.length);
     }
 
     return true;
   }
 }
