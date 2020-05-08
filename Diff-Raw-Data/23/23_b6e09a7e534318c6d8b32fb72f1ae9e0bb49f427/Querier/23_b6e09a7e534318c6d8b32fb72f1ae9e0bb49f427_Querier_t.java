 package uk.ac.ebi.arrayexpress.utils.saxon.search;
 
 /*
  * Copyright 2009-2010 Functional Genomics Group, European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import net.sf.saxon.om.NodeInfo;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermEnum;
 import org.apache.lucene.search.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Querier
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private IndexEnvironment env;
 
     public Querier( IndexEnvironment env )
     {
         this.env = env;
     }
 
     public List<String> getTerms( String fieldName, int minFreq ) throws IOException
     {
         List<String> termsList = new ArrayList<String>();
         IndexReader ir = null;
         TermEnum terms = null;
 
         try {
             ir = IndexReader.open(this.env.indexDirectory, true);
             terms = ir.terms(new Term(fieldName, ""));
             if (null != terms) {
                 while (null != terms.term() && fieldName.equals(terms.term().field())) {
                     if (terms.docFreq() >= minFreq) {
                         termsList.add(terms.term().text());
                     }
                     if (!terms.next())
                         break;
                 }
                 terms.close();
             }
         } catch (Exception x) {
             logger.error("Caught an exception:", x);
         } finally {
             if (null != terms) {
                 terms.close();
             }
             if (null != ir) {
                 ir.close();
             }
         }
         return termsList;
     }
 
     public void dumpTerms( String fieldName )
     {
         try {
             IndexReader ir = IndexReader.open(this.env.indexDirectory, true);
             TermEnum terms = ir.terms(new Term(fieldName, ""));
             File f = new File(System.getProperty("java.io.tmpdir"), fieldName + "_terms.txt");
             BufferedWriter w = new BufferedWriter(new FileWriter(f));
             StringBuilder sb = new StringBuilder();
             Integer count = 0;
             while (fieldName.equals(terms.term().field())) {
                 sb.append(terms.docFreq()).append('\t').append(terms.term().text()).append('\n');
                 if (!terms.next())
                     break;
             }
             w.write(sb.toString());
             w.close();
             terms.close();
             ir.close();
         } catch (Exception x) {
             logger.error("Caught an exception:", x);
         }
     }
 
     public List<NodeInfo> query( Query query )
     {
         List<NodeInfo> result = null;
         try {
             IndexReader ir = IndexReader.open(this.env.indexDirectory, true);
 
             // empty query returns everything
             if (query instanceof BooleanQuery && ((BooleanQuery)query).clauses().isEmpty()) {
                 logger.info("Empty search, returned all [{}] documents", this.env.documentNodes.size());
                 return this.env.documentNodes;
             }
 
             // to show _all_ available nodes
             IndexSearcher isearcher = new IndexSearcher(ir);
             logger.info("Will search index [{}], query [{}]", this.env.indexId, query.toString());
 
            // +1 is a trick to prevent from having an exception thrown if documentNodes.size() value is 0
            TopDocs hits = isearcher.search(query, this.env.documentNodes.size() + 1);
             logger.info("Search returned [{}] hits", hits.totalHits);
 
             result = new ArrayList<NodeInfo>(hits.totalHits);
             for (ScoreDoc d : hits.scoreDocs) {
                 result.add(this.env.documentNodes.get(d.doc));
             }
 
             isearcher.close();
             ir.close();
         } catch (Exception x) {
             logger.error("Caught an exception:", x);
         }
 
         return result;
     }
 }
