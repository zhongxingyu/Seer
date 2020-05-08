 package vagueobjects.ir.lda.lucene;
 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import bak.pcj.list.IntArrayList;
 import bak.pcj.list.IntList;
 import org.apache.log4j.Logger;
 import org.apache.lucene.index.*;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 import org.apache.lucene.util.PriorityQueue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Passes through Lucene index and builds the vocabulary of terms with
  * high TF/IDF scores.
  */
 public class WordProcessor {
 
     static Logger logger = Logger.getLogger(WordProcessor.class);
 
     private final int minDocFreq;
     private final IndexReader indexReader;
     private final String[] fields;
     private final int vocabSize;
     /**
      * Vocabulary, alphabetically sorted
      */
     private String[] vocabulary;
     private String[] requiredTerms;
     private int[][] termsInDocs;
     private final WordFilter tokenFilter;
 
     /**
      * Instantiates the class
      * @param indexPath - directory where index is located
      * @param vocabSize - upper bound for the dictionary size
      * @param filter - this class defines some additional rules to filter out unwanted tokens
      * @param fields  - array of Lucene field name to be used as source
      * @throws IOException  thrown when some low levelIO problem occurs
      */
     public WordProcessor(String indexPath,
         int vocabSize, WordFilter filter, String... fields) throws IOException{
         Directory directory = new NIOFSDirectory(new File(indexPath));
         this.indexReader  =  IndexReader.open(directory);
         this.fields = fields;
         this.vocabSize = vocabSize;
         this.minDocFreq = 2;
         this.tokenFilter = filter;
     }
     /**
      * Instantiates the class
      * @param indexReader - reads from the supplied Lucene index
      * @param vocabSize - upper bound for the dictionary size
      * @param filter - this class defines some additional rules to filter out unwanted tokens
      * @param fields  - array of Lucene field name to be used as source
      * @throws IOException  thrown when some low levelIO problem occurs
      */
     public WordProcessor(IndexReader indexReader,
         int vocabSize, WordFilter filter, String... fields) {
         this.indexReader = indexReader;    
         this.fields = fields;
         this.vocabSize = vocabSize;
         this.minDocFreq = 2;
         this.tokenFilter = filter;
     }
     /**
      * Instantiates the class
      * @param indexReader - reads from the supplied Lucene index
      * @param vocabSize - upper bound for the dictionary size
      * @param fields  - array of Lucene field name to be used as source
      * @throws IOException  thrown when some low levelIO problem occurs
      */
     public WordProcessor(IndexReader indexReader,
         int vocabSize, int minDocFreq, String... fields) {
         this.indexReader = indexReader;
         this.fields = fields;
         this.vocabSize = vocabSize;
         this.minDocFreq = minDocFreq;
         this.tokenFilter = WordFilter.ACCEPT_ALL;
     }
 
     public WordProcessor withRequiredTerms(String... requiredTerms){
         this.requiredTerms = requiredTerms;
         return this;
     }
 
     Counts countTerms() throws IOException{
         Counts counts = new Counts();
         counts.addAll(requiredTerms);
         for(String field: fields){
             TermEnum termsEnum   = indexReader.terms(new Term(field));
             Term currentTerm;
             do {
                 currentTerm = termsEnum.term();
                 if (currentTerm == null || !field.equals(currentTerm.field())) {
                     break;
                 }
                 String text  = currentTerm.text();
                 if(tokenFilter.accept(text)){
                     if (termsEnum.docFreq()>= minDocFreq) {
                         TermDocs termDocs = indexReader.termDocs(currentTerm);
                         int freq =0;
                         while (termDocs.next()){
                             freq +=termDocs.freq();
                         }
                         counts.increment(text, freq);
                     }
                 }
             } while(termsEnum.next());
         }
         return counts;
     }
 
     TermQueue fillQueue(int numDocs, Counts counts) throws IOException{
         TermQueue termQueue = new TermQueue(vocabSize);
         for(String field: fields){
             TermEnum termEnum = indexReader.terms(new Term(field));
             while (termEnum.next()) {
                 Term term = termEnum.term();
                 int docFreq = termEnum.docFreq();
                 String text = term.text();
                 if(!counts.containsKey(text)){
                     continue;
                 }
                 int total = counts.get(text);
                 if (docFreq < minDocFreq) {
                     continue;
                 }
                 TermStats termStats = new TermStats(text, docFreq, total, numDocs);
                 termQueue.insertWithOverflow(termStats);
             }
         }
         return termQueue;
     }
     /**
      * Extracts tokens from Lucene index with highest TF-IDF scores
      * @throws IOException  once low-level IO Exception occurs
      */
     public void process() throws IOException{
         process(new String[0]);
     }
 
     public void process( String... requiredTerms) throws IOException{
         Counts counts = countTerms( );
  
         int numDocs = indexReader.numDocs();
         logger.debug("initial number of documents " + numDocs);
         TermQueue termQueue = fillQueue(numDocs, counts);
 
         TermStats current;
         int vocabSize = Math.min(this.vocabSize, termQueue.size());
         this.vocabulary = new String[vocabSize];
         
         for (int i = 0; (current = termQueue.pop()) != null; ++i) {
             vocabulary[i] = current.text;
         }
 
         Arrays.sort(vocabulary);
 
         IntList[] docList = new IntList[numDocs];
         for(int i=0; i<docList.length;++i){
             docList[i] = new IntArrayList();
         }
 
         for(int ti=0; ti< vocabulary.length; ++ti){
             String token = vocabulary[ti];
             for(String field: fields){
                 TermDocs te = indexReader.termDocs(new Term (field, token));
                 while(te.next()){
                     int doc = te.doc();
                     int freq = te.freq();
                     //repeat the token as many times it occurs in current doc
                     for(int f=0; f<freq;++f){
                         docList[doc].add(ti);
                     }
                 }
             }
         }
         //exclude the documents that have  lesser than 3 terms
         int count=0;
         this.termsInDocs  = new int[docList.length][];
         for (int d = 0; d < docList.length; ++d) {
             if (docList[d].size()>=3){
                 this.termsInDocs[count++] = docList[d].toArray();
             }
         }
         termsInDocs = Arrays.copyOf(termsInDocs, count);
     }
 
     public String[] getVocabulary() {
         return vocabulary;
     }
 
     public int[][] getTermsInDocs() {
         return termsInDocs;
     }
 
 
     static class Counts extends HashMap<String, Integer> {
         
         void addAll(String... inserts){
             for(String key: inserts){
                 put(key, Integer.MAX_VALUE);
             }
         }
 
         void increment(String key, int count) {
             if (!containsKey(key)) {
                 put(key, count);
             } else {
                 int c = get(key);
                 put(key, count + c);
             }
         }
     }
 
     static class TermQueue extends PriorityQueue<TermStats> {
         TermQueue(int size) {
             initialize(size);
         }
 
         @Override
         protected boolean lessThan(TermStats ts1, TermStats ts2) {
             return ts1.score < ts2.score;
         }
     }
 
     static class TermStats {
         final String text;
         final double score;
 
         public TermStats(String text, int df, int totalTermFreq, int numDocs) {
             this.text = text;
             //estimate for term frequency - does not use actual document-specific term frequency
             double tf = totalTermFreq / (1d + df);
             this.score = tf * Math.log(numDocs / (1d + df));
         }
 
         @Override
         public String toString() {
             return "TermStats{term='" + text + "', score=" + score + '}';
         }
     }    
 }
