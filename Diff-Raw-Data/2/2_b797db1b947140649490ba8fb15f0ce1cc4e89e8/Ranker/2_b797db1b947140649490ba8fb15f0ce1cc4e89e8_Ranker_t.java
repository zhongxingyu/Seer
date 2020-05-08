 package edu.nyu.cs.cs2580;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.Vector;
 import java.util.Collections;
 import java.util.Comparator;
 
 class Ranker {
 	private Index _index;
 	private static Map<String, Double> IDFMap = new HashMap<String, Double>();
 	final double betaCosine = 950;
 	final double betaPhrase = 14;
 	final double betaQL = -2.0;
 	final double betaNumViews = 0.001;
 
 	public Ranker(String index_source) {
 		_index = new Index(index_source);
 	}
 
 	public Vector<ScoredDocument> runquery(String query) {
 		Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
 		for (int i = 0; i < _index.numDocs(); ++i) {
 			retrieval_results.add(runquery(query, i));
 		}
 		return getSorted(retrieval_results);
 	}
 
 	public Vector<ScoredDocument> cosineRanker(String query) {
 		Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
 		for (int i = 0; i < _index.numDocs(); ++i) {
 			retrieval_results.add(cosineRanker(query, i));
 		}
 		return getSorted(retrieval_results);
 	}
 
 	public Vector<ScoredDocument> queryLikelihoodRanker(String query) {
 		Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
 		for (int i = 0; i < _index.numDocs(); ++i) {
 			retrieval_results.add(queryLikelihoodRanker(query, i));
 		}
 		return getSorted(retrieval_results);
 	}
 	
 	public Vector<ScoredDocument> numviewsRanker(String query) {
 		Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
 		for (int i = 0; i < _index.numDocs(); ++i) {
 			retrieval_results.add(numviewsRanker(query, i));
 		}
 		return getSorted(retrieval_results);
 	}
 	
 	public Vector<ScoredDocument> phraseRanker(String query) {
 		Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
 		for (int i = 0; i < _index.numDocs(); ++i) {
 			retrieval_results.add(phraseRanker(query, i));
 		}
 		return getSorted(retrieval_results);
 	}
 
 	public Vector<ScoredDocument> linearRanker(String query) {
 		Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument>();
 		for (int i = 0; i < _index.numDocs(); ++i) {
 			double score = 0.0;
 			ScoredDocument sd = cosineRanker(query, i);
 			score += (betaCosine * sd._score);
 			System.out.print("\nCosine score" + score);
 			sd = phraseRanker(query, i);
 			score += (betaPhrase * sd._score);
 			System.out.print(" Phrase score" + (betaPhrase * sd._score ));
 			sd = queryLikelihoodRanker(query, i);
 			score += (betaQL * sd._score);
 			System.out.print(" QL score" + (betaQL * sd._score));
 			sd = numviewsRanker(query, i);
 			score += (betaNumViews * sd._score);
 			System.out.print(" Numviews score" + (betaNumViews * sd._score));
 			sd = new ScoredDocument(i, sd._title, score);
 			retrieval_results.add(sd);
 		}
 		return getSorted(retrieval_results);
 	}
 
 	public ScoredDocument runquery(String query, int did) {
 
 		// Build query vector
 		Vector<String> qv = buildQueryVector(query);
 
 		// Get the document vector. For hw1, you don't have to worry about the
 		// details of how index works.
 		Document d = _index.getDoc(did);
 		Vector<String> dv = d.get_title_vector();
 
 		// Score the document. Here we have provided a very simple ranking
 		// model,
 		// where a document is scored 1.0 if it gets hit by at least one query
 		// term.
 		double score = 0.0;
 		for (int i = 0; i < dv.size(); ++i) {
 			for (int j = 0; j < qv.size(); ++j) {
 				if (dv.get(i).equals(qv.get(j))) {
 					score = 1.0;
 					break;
 				}
 			}
 		}
 
 		return new ScoredDocument(did, d.get_title_string(), score);
 	}
 
 	public ScoredDocument cosineRanker(String query, int did) {
 		Vector<String> qv = buildQueryVector(query);
 
 		// Get the document vector. For hw1, you don't have to worry about the
 		// details of how index works.
 		Document d = _index.getDoc(did);
 		Vector<String> dv = d.get_body_vector();
 		double score = 0;
 		Vector<Double> docRepresentation = new Vector<Double>();
 		Vector<Double> queryRepresentation = new Vector<Double>();
 		Map<String, Integer> docFrequencyMap = returnDocumentFrequencyMap(dv);
 		Map<String, Integer> queryFrequencyMap = returnDocumentFrequencyMap(qv);
 		Set<String> termSet = new HashSet<String>();
 
 		// ///////////////////////////////////////////////////////////////////////
 		// Create Representation for document and query. ///
 		// ///////////////////////////////////////////////////////////////////////
 		for (String title : dv) {
 			if (termSet.add(title)) {
 				int _tf = docFrequencyMap.get(title);
 				double _idf = returnIDF(title);
 				double _tf_idf = _tf * _idf;
 				docRepresentation.add(_tf_idf);
 				if (qv.contains(title)) {
 					int _tfQ = queryFrequencyMap.get(title);
 					double _idfQ = returnIDF(title);
 					double _tf_idfQ = _tfQ * _idfQ;
 					queryRepresentation.add(_tf_idfQ);
 				} else {
 					queryRepresentation.add(0.0);
 				}
 			}
 		}
 		// /////////////////////////////////////////////////////////////////////////
 		// Perform query and document vector normalization //
 		// ////////////////////////////////////////////////////////////////////////
 		Vector<Double> normalizedDocRepresentation = new Vector<Double>();
 		Vector<Double> normalizedQueryRepresentation = new Vector<Double>();
 		double docDenominator = 0.0;
 		double queryDenominator = 0.0;
 		for (int i = 0; i < docRepresentation.size(); i++) {
 			docDenominator += Math.pow(docRepresentation.get(i), 2);
 			queryDenominator += Math.pow(queryRepresentation.get(i), 2);
 		}
 		docDenominator = Math.sqrt(docDenominator);
 		queryDenominator = Math.sqrt(queryDenominator);
 		for (int i = 0; i < docRepresentation.size(); i++) {
 			if (docDenominator != 0) {
 				normalizedDocRepresentation.add(docRepresentation.get(i)
 						/ docDenominator);
 			} else {
 				normalizedDocRepresentation.add(0.0);
 			}
 			if (queryDenominator != 0) {
 				normalizedQueryRepresentation.add(queryRepresentation.get(i)
 						/ queryDenominator);
 			} else {
 				normalizedQueryRepresentation.add(0.0);
 			}
 		}
 		// ////////////////////////////////////////////////////////////////////////
 
 		// ////////////////////////////////////////////////////////////////////////
 		// Cosine Ranker ///
 		// ////////////////////////////////////////////////////////////////////////
 		double numerator = 0.0;
 		double documentSquare = 0.0;
 		double querySquare = 0.0;
 		for (int i = 0; i < normalizedDocRepresentation.size(); i++) {
 			numerator = numerator
 					+ (normalizedDocRepresentation.get(i) * normalizedQueryRepresentation
 							.get(i));
 			documentSquare = documentSquare
 					+ Math.pow(normalizedDocRepresentation.get(i), 2);
 			querySquare = querySquare
 					+ Math.pow(normalizedQueryRepresentation.get(i), 2);
 		}
 		double denominator = 0.0;
 		denominator = Math.sqrt((documentSquare * querySquare));
 		if (denominator != 0) {
 			score = numerator / denominator;
 		} else {
 			score = 0;
 		}
 		return new ScoredDocument(did, d.get_title_string(), score);
 	}
 
 	private double returnIDF(String s) {
 		if (IDFMap.containsKey(s)) {
 			return IDFMap.get(s);
 		} else {
 			int termFrequencyInAllDocuments = Document.documentFrequency(s);
 			int numberOfTotalDocuments = _index.numDocs();
 			double calcIdf = 1 + (Math.log(numberOfTotalDocuments
 					/ termFrequencyInAllDocuments) / Math.log(2));
 			IDFMap.put(s, calcIdf);
 			return calcIdf;
 		}
 	}
 
 	private Map<String, Integer> returnDocumentFrequencyMap(
 			Vector<String> docVector) {
 		Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
 		for (String s : docVector) {
 			if (frequencyMap.containsKey(s)) {
 				Integer frequency = frequencyMap.get(s) + 1;
 				frequencyMap.put(s, frequency);
 			} else {
 				frequencyMap.put(s, 1);
 			}
 		}
 		return frequencyMap;
 	}
 	
 	
 	private Vector < String > buildQueryVector(String query) {
 		Scanner s = new Scanner(query);
 		Vector<String> qv = new Vector<String>();
 		while (s.hasNext()) {
 			String term = s.next();
 			qv.add(term);
 		}
 		return qv;
 	}
 
 	public ScoredDocument phraseRanker(String query, int did) {
 		String queryStr = query + "\t" + query + "\t" + "-1";
 		Document queryDocument = new Document(-1, queryStr);
 		Vector < String > qpv = queryDocument.get_phrase_vector();
 		Document d = _index.getDoc(did);
 		int score = 0;
 		if (qpv.size() == 0 && query.length() > 0) {
 			score += d.getTermFrequency(query);
 		} else {
 			for (String s: qpv) {
 				score += d.getPhraseFrequency(s);
 			}
 		}
 		return new ScoredDocument(did, d.get_title_string(), score);
 	}
 	
 	public ScoredDocument numviewsRanker(String query, int did) {
 		Document d = _index.getDoc(did);
 		if (d == null) {
 			return new ScoredDocument(did, "", 0);
 		}
 		return new ScoredDocument(did, d.get_title_string(), d.get_numviews());
 		
 	}
 
 
 	public ScoredDocument queryLikelihoodRanker(String query, int did) {
 		Scanner s = new Scanner(query);
 		Vector<String> qv = new Vector<String>();
 		while (s.hasNext()) {
 			String term = s.next();
 			qv.add(term);
 		}
 		// Get the document vector. For hw1, you don't have to worry about the
 		// details of how index works.
 		Document d = _index.getDoc(did);
 		Vector<String> dv = d.get_body_vector();
 		double score = 0;
 		Vector<Double> queryRepresentation = new Vector<Double>();
 		Map<String, Integer> docFrequencyMap = returnDocumentFrequencyMap(dv);
 		Set<String> termSet = new HashSet<String>();
 		double numOfWordsDoc = (double)findNumOfWords(docFrequencyMap);
 		double numOfWordsColl = (double)Document.termFrequency();
 		double lambda = 0.5;
 		double f_qi, c_qi;
 
 		// Create Representation for query. ///
 		// ///////////////////////////////////////////////////////////////////////
 		for (String title : qv) {
 			if (termSet.add(title)) {
 				if (docFrequencyMap.containsKey(title)) {
 					f_qi = (double)docFrequencyMap.get(title);
 				} else {
 					f_qi = 0;
 				}
 				double f_qi_d = 0;
 				if (numOfWordsDoc != 0) {
 					f_qi_d = (1 - lambda) * (f_qi / numOfWordsDoc);
 				}
 				c_qi = Document.termFrequency(title);
 				double c_qi_d = 0;
 				if (numOfWordsColl != 0) {
 					c_qi_d = lambda * (c_qi / numOfWordsColl);
 				}
 				queryRepresentation.add(f_qi_d + c_qi_d);
 			}
 		}
 
 		// Query Likelihood ///
 		//////////////////////////////////////////////////////////////////////////
 		for (int i = 0; i < queryRepresentation.size(); i++) { 
 			score += Math.log(queryRepresentation.get(i))/Math.log(2.0) ;
 		}
		score = Math.pow(2, score);
 
 		return new ScoredDocument(did, d.get_title_string(), score);
 	}
 
 	private int findNumOfWords(Map<String, Integer> mp) {
 		int count = 0;
 		Iterator<Entry<String, Integer>> it = mp.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry pairs = (Map.Entry) it.next();
 			count = count + (Integer) pairs.getValue();
 		}
 		return count;
 	}
 
 	public Vector<ScoredDocument> getSorted(Vector<ScoredDocument> doc) {
 		Comparator<ScoredDocument> comparator = new Comparator<ScoredDocument>() {
 			public int compare(ScoredDocument c1, ScoredDocument c2) {
 				Double d1= (c1 == null) ? Double.POSITIVE_INFINITY : c1._score;
 				Double d2= (c2 == null) ? Double.POSITIVE_INFINITY : c2._score;
 				return  d2.compareTo(d1);
 			}
 		};
 		Collections.sort(doc,comparator);
 		return doc;
 	}
 	
 	public Document getDoc(int did) {
 		Document d = _index.getDoc(did);
 		return d;
 	}
 }
