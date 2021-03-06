 package org.wikimedia.diffdb;
 
 /**
  * Copyright 2011 Apache Software Foundation
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
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
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Fieldable;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 /** Simple command-line based search demo. */
 public class Searcher {
 	private static final String searchKey = "rev_id";
	private static int MAX_HITS = 10000;
 
 	private Searcher() {
 	}
 
 	private static String truncateString(String str, int max) {
 		if (str.length() + 3 > max) {
 			return str.substring(0, max - 3) + "...";
 		} else {
 			return str;
 		}
 	}
 
 	/** Simple command-line based search demo. */
 	public static void main(String[] args) throws Exception {
 		String usage = "Usage:\tjava "
 				+ Searcher.class.getName()
 				+ " [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/java/4_0/demo.html for details.";
 		if (args.length > 0
 				&& ("-h".equals(args[0]) || "-help".equals(args[0]))) {
 			System.out.println(usage);
 			System.exit(0);
 		}
 
 		String index = "index";
 		String field = "added";
 		String queries = null;
 		int repeat = 0;
 		boolean raw = false;
 		String queryString = null;
 		int hitsPerPage = 10;
 
 		for (int i = 0; i < args.length; i++) {
 			if ("-index".equals(args[i])) {
 				index = args[i + 1];
 				i++;
 			} else if ("-field".equals(args[i])) {
 				field = args[i + 1];
 				i++;
 			} else if ("-queries".equals(args[i])) {
 				queries = args[i + 1];
 				i++;
 			} else if ("-query".equals(args[i])) {
 				queryString = args[i + 1];
 				i++;
 			} else if ("-repeat".equals(args[i])) {
 				repeat = Integer.parseInt(args[i + 1]);
 				i++;
 			} else if ("-raw".equals(args[i])) {
 				raw = true;
 			} else if ("-paging".equals(args[i])) {
 				hitsPerPage = Integer.parseInt(args[i + 1]);
 				if (hitsPerPage <= 0) {
 					System.err
 							.println("There must be at least 1 hit per page.");
 					System.exit(1);
 				}
 				i++;
 			}
 		}
 
 		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File(
 				index)));
 		System.err.println("Index contains " + searcher.maxDoc()
 				+ " documents.");
 		Analyzer analyzer = new SimpleNGramAnalyzer(3);
 
 		BufferedReader in = null;
 		if (queries != null) {
 			in = new BufferedReader(new InputStreamReader(new FileInputStream(
 					queries), "UTF-8"));
 		} else {
 			in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
 		}
 		QueryParser parser = new QueryParser(Version.LUCENE_34, field, analyzer);
 		while (true) {
 			if (queries == null && queryString == null) { // prompt the user
 				System.out.println("Enter query: ");
 			}
 
 			String line = queryString != null ? queryString : in.readLine();
 
 			if (line == null || line.length() == -1) {
 				break;
 			}
 
 			line = line.trim();
 			if (line.length() == 0) {
 				break;
 			}
 
 			Query query = parser.parse(line);
 			System.out.println("Searching for: " + query.toString(field));
 
 			if (repeat > 0) { // repeat & time as benchmark
 				Date start = new Date();
 				for (int i = 0; i < repeat; i++) {
 					searcher.search(query, null, 100);
 				}
 				Date end = new Date();
 				System.out.println("Time: " + (end.getTime() - start.getTime())
 						+ "ms");
 			}
 
 			doPagingSearch(in, searcher, query, hitsPerPage, raw,
 					queries == null && queryString == null);
 
 			if (queryString != null) {
 				break;
 			}
 		}
 		searcher.close();
 	}
 
	public static void setMAX_HITS(int max_hits) {
		MAX_HITS = max_hits;
	}

	public static int getMAX_HITS() {
		return MAX_HITS;
	}

 	/**
 	 * This demonstrates a typical paging search scenario, where the search
 	 * engine presents pages of size n to the user. The user can then go to the
 	 * next page if interested in the next hits.
 	 * 
 	 * When the query is executed for the first time, then only enough results
 	 * are collected to fill 5 result pages. If the user wants to page beyond
 	 * this limit, then the query is executed another time and all hits are
 	 * collected.
 	 * 
 	 */
 	private static void printResultHeading(List<Fieldable> fields) {
 		if (fields != null) {
 			System.out.print("#\tScore\t");
 			Iterator<Fieldable> it = fields.iterator();
 			while (it.hasNext()) {
 				Fieldable value = it.next();
 				System.out.print(value.name() + "\t");
 			}
 			System.out.println();
 		}
 	}
 
 	public static String createFilename(Query query) {
 		String sFileName = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
 		Date date = new Date();
		sFileName = dateFormat.format(date) + '_' + query.toString();
 		sFileName = sFileName.replace(" ", "_");
		sFileName = sFileName.replace(":", "_");
 		return sFileName;
 	}
 
 	public static void writeResults(Query query, IndexSearcher searcher,TopDocs results) {
 		String sFileName = createFilename(query);
		int max_hits = getMAX_HITS();
 		int hits = results.totalHits;
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(sFileName);
 			for (int i = 0; i<hits;i++){
				if (i>max_hits){ 
					//limit number of written results to MAX_HITS. 
					break;
				}
 				Document doc = searcher.doc(i); 
 				List<Fieldable> fields = doc.getFields();
 				Iterator<Fieldable> it = fields.iterator();
 				if (i==0){
 					while (it.hasNext()){ 
 						Fieldable field = it.next();
 						writer.append(field.name() + "\t");
 					}
 					writer.append("\n");
 				//Not sure if I need to reset the iterator;
 				}
 				while (it.hasNext()) {
 					Fieldable field = it.next();
					writer.append(field.stringValue() + "\t");
 				}
 				writer.append("\n");
 			}
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			System.out.println("Cannot write to file");
 			e.printStackTrace();
 		} finally {
 			try {
 				writer.close();
 			} catch (IOException e) {
 				System.out.println("Cannot close writer");
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public static void doPagingSearch(BufferedReader in,
 			IndexSearcher searcher, Query query, int hitsPerPage, boolean raw,
 			boolean interactive) throws IOException {
 		// Collect enough docs to show 5 pages
 		TopDocs results = searcher.search(query, 5 * hitsPerPage);
 		ScoreDoc[] hits = results.scoreDocs;
 
 		int numTotalHits = results.totalHits;
 		System.out.println(numTotalHits + " total matching documents");
 
 		int start = 0;
 		int end = Math.min(numTotalHits, hitsPerPage);
 
 		while (true) {
 			if (end > hits.length) {
 				System.out
 						.println("Only results 1 - " + hits.length + " of "
 								+ numTotalHits
 								+ " total matching documents collected.");
 				System.out.println("Collect more (y/n) ?");
 				String line = in.readLine();
 				if (line.length() == 0 || line.charAt(0) == 'n') {
 					break;
 				}
 
 				hits = searcher.search(query, numTotalHits).scoreDocs;
 			}
 
 			end = Math.min(hits.length, start + hitsPerPage);
 
 			for (int i = start; i < end; i++) {
 				if (raw) { // output raw format
 					System.out.println("doc=" + hits[i].doc + " score="
 							+ hits[i].score);
 					continue;
 				}
 
 				Document doc = searcher.doc(hits[i].doc);
 				String key = doc.get(searchKey);
 				List<Fieldable> fields = doc.getFields();
 				Iterator<Fieldable> it = fields.iterator();
 				if (key != null) {
 					if (i == 0) {
 						printResultHeading(fields);
 					}
 					System.out.print((i + 1) + ".\t" + hits[i].score + "\t");
 					while (it.hasNext()) {
 						Fieldable field = it.next();
 						System.out.print(StringEscapeUtils
 								.escapeJava(truncateString(field.stringValue(),
 										100))
 								+ "\t");
 					}
 					// System.out.println((i + 1) + ". " + key);
 					System.out.println();
 				} else {
 					System.out.println((i + 1) + ". " + "No key (" + searchKey
 							+ ") for this document");
 				}
 
 			}
 
 			if (!interactive || end == 0) {
 				break;
 			}
 
 			if (numTotalHits >= end) {
 				boolean quit = false;
 				while (true) {
 					System.out.print("Press ");
 					if (start - hitsPerPage >= 0) {
 						System.out.print("(p)revious page, ");
 					}
 					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, (w)rite to file, ");
 					}
 					System.out
 							.println("(q)uit or enter number to jump to a page.");
 
 					String line = in.readLine();
 					if (line.length() == 0 || line.charAt(0) == 'q') {
 						quit = true;
 						break;
 					}
 					if (line.charAt(0) == 'p') {
 						start = Math.max(0, start - hitsPerPage);
 						break;
 					} else if (line.charAt(0) == 'n') {
 						if (start + hitsPerPage < numTotalHits) {
 							start += hitsPerPage;
 						}
 						break;
 					} else if (line.charAt(0) == 'w') {
 						writeResults(query,searcher, results);
 
 					} else {
 						int page = Integer.parseInt(line);
 						if ((page - 1) * hitsPerPage < numTotalHits) {
 							start = (page - 1) * hitsPerPage;
 							break;
 						} else {
 							System.out.println("No such page");
 						}
 					}
 				}
 				if (quit)
 					break;
 				end = Math.min(numTotalHits, start + hitsPerPage);
 			}
 		}
 	}
 }
 /*
  * Local variables: tab-width: 2 c-basic-offset: 2 indent-tabs-mode: t End:
  */
