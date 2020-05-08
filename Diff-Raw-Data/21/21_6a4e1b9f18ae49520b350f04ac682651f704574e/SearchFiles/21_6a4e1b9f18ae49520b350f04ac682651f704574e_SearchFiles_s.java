 package search;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.no.NorwegianAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 public class SearchFiles {
 
 	public SearchFiles() {}
 	public Document Search(String searchString, Directory index, Analyzer analyzer){
 
 		try {
 			QueryParser q = new MultiFieldQueryParser(Version.LUCENE_CURRENT
 					, new String[] {"label","synonyms"},
 					analyzer);
 
 			int hitsPerPage = 3;
 			IndexReader reader = IndexReader.open(index);
 			IndexSearcher searcher = new IndexSearcher(reader);
 
 			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
 			searcher.search(q.parse(searchString), collector);
 			ScoreDoc[] hits = collector.topDocs().scoreDocs;
 			System.out.println("");
 			System.out.println("Found " + hits.length + " hits.");
 			System.out.println("-----------------------------------");
 			for(ScoreDoc c:hits){
 				float score = c.score;
 				if(searcher.doc(c.doc).get("Chapter").contains("L"))
 					score *=0.5;
 				if(searcher.doc(c.doc).get("Chapter").lastIndexOf('.') == 5)
 					score *=0.6;
 				else if(searcher.doc(c.doc).get("Chapter").lastIndexOf('.') == 3)
 					score *=0.7;
 				c.score = score;
 			}
 			Arrays.sort(hits, new Comparator<ScoreDoc>() {
 				@Override
 				public int compare(ScoreDoc o1, ScoreDoc o2) {
					return (int) (o1.score - o2.score);
 				}
 			});
 			
 			for(ScoreDoc c:hits){
 				System.out.println(searcher.doc(c.doc).get("Chapter"));
				System.out.println(c.score);
				System.out.println("-----------------------");
 			}
 		} catch (ParseException | IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 
 	}
 }
