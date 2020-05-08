 package com.github.tteofili.nlputils;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 
 /**
  * A Lucene based {@link NaiveBayesClassifier}
  */
 public class LuceneSimpleNaiveBayesClassifier implements NaiveBayesClassifier<String, String> {
 
     private Map<String, Double> priors;
 
     private IndexSearcher indexSearcher;
     private String textFieldName;
     private String classFieldName;
     private Map<String, Double> classCounts;
     private int docsWithClassSize;
     private Analyzer analyzer;
 
     public void train(IndexSearcher indexSearcher, String textFieldName, String classFieldName, Analyzer analyzer) throws IOException {
         this.indexSearcher = indexSearcher;
         this.textFieldName = textFieldName;
         this.classFieldName = classFieldName;
         this.analyzer = analyzer;
         createVocabulary();
         preComputePriors();
     }
 
 
     private void preComputePriors() throws IOException {
         priors = new HashMap<String, Double>();
         for (String cl : classCounts.keySet()) {
             priors.put(cl, calculatePrior(cl));
         }
     }
 
     private void createVocabulary() throws IOException {
         // take the existing classes
         classCounts = new HashMap<String, Double>();
         TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);
         for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
             String cl = indexSearcher.doc(scoreDoc.doc).getField(classFieldName).stringValue();
             Double cld = classCounts.get(cl);
             if (cld != null) {
                 classCounts.put(cl, cld + 1);
             } else {
                 classCounts.put(cl, 1d);
             }
 
         }
         docsWithClassSize = topDocs.totalHits;
     }
 
     private String[] tokenizeDoc(String doc) throws IOException {
         Collection<String> result = new LinkedList<String>();
         TokenStream tokenStream = analyzer.tokenStream(textFieldName, new StringReader(doc));
         while (tokenStream.incrementToken()) {
             CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
             result.add(charTermAttribute.toString());
         }
         return result.toArray(new String[result.size()]);
     }
 
     @Override
     public String calculateClass(String inputDocument) throws Exception {
         if (indexSearcher == null) {
             throw new RuntimeException("need to train the classifier (use train method)");
         }
         Double max = 0d;
         String foundClass = null;
 
         for (String cl : classCounts.keySet()) {
             Double clVal = priors.get(cl) * calculateLikelihood(inputDocument, cl);
             if (clVal > max) {
                 max = clVal;
                 foundClass = cl;
             }
         }
         return foundClass;
     }
 
 
     private Double calculateLikelihood(String document, String c) throws IOException {
         // for each word
         Double result = 1d;
         for (String word : tokenizeDoc(document)) {
             // search with text:word AND class:c
             int hits = countWordInClassC(c, word);
 
             // num : count the no of times the word appears in documents of class c (+1)
             double num = hits + 1; // +1 is added because of add 1 smoothing
 
             // den : for the whole dictionary, count the no of times a word appears in documents of class c (+|V|)
            double den = countInClassC(c) + docsWithClassSize;
 
             // P(w|c) = num/den
             double wordProbability = num / den;
             result *= wordProbability;
         }
 
         // P(d|c) = P(w1|c)*...*P(wn|c)
         return result;
     }
 
     private double countInClassC(String c) throws IOException {
         TopDocs topDocs = indexSearcher.search(new TermQuery(new Term(classFieldName, c)), Integer.MAX_VALUE);
         int res = 0;
         for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
             res += indexSearcher.getIndexReader().getTermVectors(scoreDoc.doc).terms(textFieldName).size();
         }
         return res;
     }
 
     private int countWordInClassC(String c, String word) throws IOException {
         BooleanQuery booleanQuery = new BooleanQuery();
         booleanQuery.add(new BooleanClause(new TermQuery(new Term(textFieldName, word)), BooleanClause.Occur.MUST));
         booleanQuery.add(new BooleanClause(new TermQuery(new Term(classFieldName, c)), BooleanClause.Occur.MUST));
         TopDocs topDocs = indexSearcher.search(booleanQuery, 1);
         return topDocs.totalHits;
     }
 
     private Double calculatePrior(String currentClass) throws IOException {
         return (double) docCount(currentClass) / docsWithClassSize;
     }
 
     private int docCount(String countedClass) throws IOException {
         return indexSearcher.search(new TermQuery(new Term(classFieldName, countedClass)), 1).totalHits;
     }
 }
