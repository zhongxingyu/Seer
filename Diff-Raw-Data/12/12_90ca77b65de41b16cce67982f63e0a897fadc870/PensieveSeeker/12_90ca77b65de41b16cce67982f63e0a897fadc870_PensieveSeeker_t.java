 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.tm.pensieve.seeker;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.lib.search.lucene.analysis.NgramAnalyzer;
 import net.sf.okapi.lib.search.lucene.query.SimpleConcordanceFuzzyQuery;
 import net.sf.okapi.lib.search.lucene.query.TmFuzzyQuery;
 import net.sf.okapi.tm.pensieve.common.Metadata;
 import net.sf.okapi.tm.pensieve.common.MetadataType;
 import net.sf.okapi.tm.pensieve.common.TmHit;
 import net.sf.okapi.tm.pensieve.common.TmMatchType;
 import net.sf.okapi.tm.pensieve.common.TranslationUnit;
 import net.sf.okapi.tm.pensieve.common.TranslationUnitField;
 import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.TermAttribute;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.QueryWrapperFilter;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.Directory;
 
 /**
  * Used to query the TM
  * 
  * @author Christian Hargraves
  * @author HARGRAVEJE
  */
 public class PensieveSeeker implements ITmSeeker, Iterable<TranslationUnit> {
 	private static final Logger LOGGER = Logger.getLogger(PensieveSeeker.class.getName());
 
 	private final static NgramAnalyzer defaultFuzzyAnalyzer = new NgramAnalyzer(Locale.ENGLISH, 4);
 	private final static float MAX_HITS_RATIO = 0.01f;
 	private final static int MIN_MAX_HITS = 500;
 	// TODO: externalize penalties in the future
 	private static float SINGLE_CODE_DIFF_PENALTY = 0.5f;
 	private static float WHITESPACE_OR_CASE_PENALTY = 2.0f;
 
 	// maxTopDocuments = indexReader.maxDoc * MAX_HITS_CONSTANT
 	private int maxTopDocuments;
 	private Directory indexDir;
 	private IndexReader indexReader;
 	private IndexSearcher indexSearcher;
 
 	/**
 	 * Creates an instance of TMSeeker
 	 * 
 	 * @param indexDir
 	 *            The Directory implementation to use for the queries
 	 * @throws IllegalArgumentException
 	 *             If the indexDir is not set
 	 */
 	public PensieveSeeker(Directory indexDir) throws IllegalArgumentException {
 		if (indexDir == null) {
 			throw new IllegalArgumentException("'indexDir' cannot be null!");
 		}
 		this.indexDir = indexDir;
 	}
 
 	/**
 	 * gets an iterator to traverse all translation units in the indexdir
 	 * 
 	 * @return the iterator for translation units
 	 */
 	// TODO: Needs to accept query items and parameters
 	public Iterator<TranslationUnit> iterator() {
 		return new TranslationUnitIterator();
 	}
 
 	/**
 	 * Get the current Lucene {@link Directory}
 	 * 
 	 * @return the current Lucene {@link Directory}
 	 */
 	public Directory getIndexDir() {
 		return indexDir;
 	}
 
 	private BooleanQuery createQuery(Metadata metadata) {
 		return createQuery(metadata, null);
 	}
 
 	private BooleanQuery createQuery(Metadata metadata, Query q) {
 		BooleanQuery bQuery = new BooleanQuery();
 		if (q != null) {
 			bQuery.add(q, BooleanClause.Occur.MUST);
 		}
 
 		if (metadata != null) {
 			for (MetadataType type : metadata.keySet()) {
 				bQuery.add(new TermQuery(new Term(type.fieldName(), metadata.get(type))),
 						BooleanClause.Occur.MUST);
 			}
 		}
 		return bQuery;
 	}
 
 	/**
 	 * Translates a Document into a TranslationUnit
 	 * 
 	 * @param doc
 	 *            The Document to translate
 	 * @return a TranslationUnit that represents what was returned in the document.
 	 */
 	TranslationUnit getTranslationUnit(Document doc) {
 		// TODO Make sure metadata is supported here
 		TranslationUnit tu = new TranslationUnit(new TranslationUnitVariant(getLocaleValue(doc,
 				TranslationUnitField.SOURCE_LANG), new TextFragment(getFieldValue(doc,
 				TranslationUnitField.SOURCE))), new TranslationUnitVariant(getLocaleValue(doc,
 				TranslationUnitField.TARGET_LANG), new TextFragment(getFieldValue(doc,
 				TranslationUnitField.TARGET))));
 
 		for (MetadataType type : MetadataType.values()) {
 			tu.setMetadataValue(type, getFieldValue(doc, type));
 		}
 		return tu;
 	}
 
 	/**
 	 * Gets a Document's Field Value
 	 * 
 	 * @param doc
 	 *            The document ot get the field value from
 	 * @param field
 	 *            The field to extract
 	 * @return The value of the field
 	 */
 	String getFieldValue(Document doc, TranslationUnitField field) {
 		return getFieldValue(doc, field.name());
 	}
 
 	/**
 	 * Gets a Document's Field Value
 	 * 
 	 * @param doc
 	 *            The document ot get the field value from
 	 * @param type
 	 *            The field to extract
 	 * @return The value of the field
 	 */
 	String getFieldValue(Document doc, MetadataType type) {
 		return getFieldValue(doc, type.fieldName());
 	}
 
 	/**
 	 * Gets a Document's Field Value
 	 * 
 	 * @param doc
 	 *            The document ot get the field value from
 	 * @param fieldName
 	 *            The name of the field to extract
 	 * @return The value of the field
 	 */
 	String getFieldValue(Document doc, String fieldName) {
 		String fieldValue = null;
 		Field tempField = doc.getField(fieldName);
 		if (tempField != null) {
 			fieldValue = tempField.stringValue();
 		}
 		return fieldValue;
 	}
 
 	LocaleId getLocaleValue(Document doc, TranslationUnitField field) {
 		return new LocaleId(getFieldValue(doc, field.name()), false);
 	}
 
 	protected IndexSearcher createIndexSearcher() throws CorruptIndexException, IOException {
 		return new IndexSearcher(openIndexReader());
 	}
 
 	protected IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException {
 		if (indexSearcher != null) {
 			return indexSearcher;
 		}
 		indexSearcher = createIndexSearcher();
 		return indexSearcher;
 	}
 
 	protected IndexReader openIndexReader() throws CorruptIndexException, IOException {
 		if (indexReader == null) {
 			indexReader = IndexReader.open(indexDir, true);
 			maxTopDocuments = (int) ((float) indexReader.maxDoc() * MAX_HITS_RATIO);
 			if (maxTopDocuments < MIN_MAX_HITS) {
 				maxTopDocuments = MIN_MAX_HITS;
 			}
 		}
 		return indexReader;
 	}
 
 	private List<TmHit> getTopHits(Query query, Metadata metadata) throws IOException {
 		IndexSearcher is = getIndexSearcher();
 		QueryWrapperFilter filter = null;
 		int maxHits = 0;
 		List<TmHit> tmHitCandidates = new ArrayList<TmHit>(maxTopDocuments);
 
 		// create a filter based on the specified metadata
 		if (metadata != null && !metadata.isEmpty()) {
 			filter = new QueryWrapperFilter(createQuery(metadata));
 		}
 
 		// collect hits in increments of maxTopDocuments until we have all the possible candidate hits
 		TopScoreDocCollector topCollector;
 		do {
 			maxHits += maxTopDocuments;
 			topCollector = TopScoreDocCollector.create(maxHits, true);
 			is.search(query, filter, topCollector);
 		} while (topCollector.getTotalHits() >= maxHits);
 
 		// Go through the candidates and create TmHits from them
 		TopDocs topDocs = topCollector.topDocs();
 		for (int i = 0; i < topDocs.scoreDocs.length; i++) {
 			ScoreDoc scoreDoc = topDocs.scoreDocs[i];
 			TmHit tmHit = new TmHit();
 			tmHit.setDocId(scoreDoc.doc);
 			tmHit.setScore(scoreDoc.score);
 
 			List<Code> tmCodes = Code.stringToCodes(getFieldValue(getIndexSearcher().doc(
 					tmHit.getDocId()), TranslationUnitField.SOURCE_CODES));
 			String tmCodedText = getFieldValue(getIndexSearcher().doc(tmHit.getDocId()),
 					TranslationUnitField.SOURCE_EXACT);
 
 			tmHit.setTu(createTranslationUnit(getIndexSearcher().doc(tmHit.getDocId()),
 					tmCodedText, tmCodes));
 			tmHitCandidates.add(tmHit);
 		}
 
 		// remove duplicate hits
 		ArrayList<TmHit> noDups = new ArrayList<TmHit>(new LinkedHashSet<TmHit>(tmHitCandidates));
 		return noDups;
 	}
 
 	public List<TmHit> searchExact(TextFragment query, Metadata metadata) {
 		TermQuery termQuery = new TermQuery(new Term(TranslationUnitField.SOURCE_EXACT.name(),
 				query.getCodedText()));
 		List<TmHit> tmHitCandidates;
 		BooleanQuery bQuery = createQuery(metadata, termQuery);
 
 		try {
 			tmHitCandidates = getTopHits(bQuery, metadata);
 
 			for (TmHit tmHit : tmHitCandidates) {
 				tmHit.setScore(100.0f);
 				tmHit.setMatchType(TmMatchType.EXACT);
 			}
 
 			/*
 			 * System.out.println(queryFrag.toString()); System.out.println(tmHit.getScore());
 			 * System.out.println(tmHit.getTu().toString()); System.out.println();
 			 */
 
 			// sort TmHits on TmMatchType, Score and Source String
 			Collections.sort(tmHitCandidates);
 		} catch (IOException e) {
 			throw new OkapiIOException("Could not complete query.", e);
 		}
 
 		return tmHitCandidates;
 	}
 
 	/**
 	 * Search for exact and fuzzy matches
 	 * 
 	 * @param queryFrag
 	 *            the fragment to query.
 	 * @param threshold
 	 *            the minimal score value to return.
 	 * @param max
 	 *            the maximum number of hits to return.
 	 * @param metadata
 	 *            any associated attributes to use for filter.
 	 * @return the list of hits of the given argument.
 	 * @throws IllegalArgumentException
 	 *             If threshold is greater than 100 or less than 0
 	 */
 	public List<TmHit> searchFuzzy(TextFragment query, int threshold, int max, Metadata metadata) {
 		if (threshold < 0 || threshold > 100) {
 			throw new IllegalArgumentException("");
 		}
 
 		float searchThreshold = (float) threshold;
 		if (threshold < 0)
 			searchThreshold = 0.0f;
 		if (threshold > 100)
 			searchThreshold = 100.0f;
 
 		String queryText = query.getText();
 
 		// create basic ngram analyzer to tokenize query
 		TokenStream queryTokenStream = defaultFuzzyAnalyzer.tokenStream(TranslationUnitField.SOURCE
 				.name(), new StringReader(queryText));
 		// get the TermAttribute from the TokenStream
 		TermAttribute termAtt = (TermAttribute) queryTokenStream.addAttribute(TermAttribute.class);
 		TmFuzzyQuery fQuery = new TmFuzzyQuery(searchThreshold, TranslationUnitField.SOURCE.name());
 		try {
 			queryTokenStream.reset();
 			while (queryTokenStream.incrementToken()) {
 				Term t = new Term(TranslationUnitField.SOURCE.name(), termAtt.term());
 				fQuery.add(t);
 			}
 			queryTokenStream.end();
 			queryTokenStream.close();
 		} catch (IOException e) {
 			throw new OkapiIOException(e.getMessage(), e);
 		}
 
 		return getFuzzyHits(max, searchThreshold, fQuery, query, metadata);
 	}
 
 	public List<TmHit> searchSimpleConcordance(String query, int threshold, int max,
 			Metadata metadata) {
 		if (threshold < 0 || threshold > 100) {
 			throw new IllegalArgumentException("");
 		}
 
 		float searchThreshold = (float) threshold;
 		if (threshold < 0)
 			searchThreshold = 0.0f;
 		if (threshold > 100)
 			searchThreshold = 100.0f;
 
 		// create basic ngram analyzer to tokenize query
 		TokenStream queryTokenStream = defaultFuzzyAnalyzer.tokenStream(TranslationUnitField.SOURCE
 				.name(), new StringReader(query));
 		// get the TermAttribute from the TokenStream
 		TermAttribute termAtt = (TermAttribute) queryTokenStream.addAttribute(TermAttribute.class);
 		SimpleConcordanceFuzzyQuery fQuery = new SimpleConcordanceFuzzyQuery(searchThreshold);
 		try {
 			queryTokenStream.reset();
 			while (queryTokenStream.incrementToken()) {
 				Term t = new Term(TranslationUnitField.SOURCE.name(), termAtt.term());
 				fQuery.add(t);
 			}
 			queryTokenStream.end();
 			queryTokenStream.close();
 		} catch (IOException e) {
 			throw new OkapiIOException(e.getMessage(), e);
 		}
 
 		return getConcordanceHits(max, fQuery, query, metadata);
 	}
 
 	/**
 	 * Search for fuzzy matches and adjust hit type and score based on differences with whitespace, codes and casing.
 	 * 
 	 * 
 	 * @param threshold
 	 *            the minumum score to return (between 0.0 and 1.0)
 	 * @param max
 	 *            the maximum number of hits to return.
 	 * @param query
 	 *            the query
 	 * @param queryFrag
 	 *            the text fragment for the query.
 	 * @param metadata
 	 *            any associated attributes to use for filter.
 	 * @return the list of hits found for the given arguments (never null).
 	 */
 	List<TmHit> getFuzzyHits(int max, float threshold, Query query, TextFragment queryFrag,
 			Metadata metadata) {
 		List<TmHit> tmHitCandidates;
		List<TmHit> tmHitsToRemove = new LinkedList<TmHit>();
 		List<Code> queryCodes = queryFrag.getCodes();
 
 		try {
 			tmHitCandidates = getTopHits(query, metadata);
 			for (TmHit tmHit : tmHitCandidates) {
 				List<Code> tmCodes = Code.stringToCodes(getFieldValue(getIndexSearcher().doc(
 						tmHit.getDocId()), TranslationUnitField.SOURCE_CODES));
 				String tmCodedText = getFieldValue(getIndexSearcher().doc(tmHit.getDocId()),
 						TranslationUnitField.SOURCE_EXACT);
 
 				// remove codes so we can compare text only
 				String sourceTextOnly = TextFragment.getText(tmCodedText);
 
 				TmMatchType matchType = TmMatchType.FUZZY;
 				Float score = tmHit.getScore();
 				tmHit.setCodeMismatch(false);
 				if (queryCodes.size() != tmCodes.size()) {
 					tmHit.setCodeMismatch(true);
 				}
 
 				// These are 100%, adjust match type and penalize for whitespace
 				// and case difference
 				if (score >= 100.0f && tmCodedText.equals(queryFrag.getCodedText())) {
 					matchType = TmMatchType.EXACT;
 				} else if (score >= 100.0f && sourceTextOnly.equals(queryFrag.getText())) {
 					matchType = TmMatchType.FUZZY_FULL_TEXT_MATCH;
 				} else if (score >= 100.0f) {
 					// must be a whitespace or case difference
 					score -= WHITESPACE_OR_CASE_PENALTY;
 				}
 
 				// code penalty
 				if (queryCodes.size() != tmCodes.size()) {
 					score -= (SINGLE_CODE_DIFF_PENALTY * (float) Math.abs(queryCodes.size()
 							- (float) tmCodes.size()));
 				}
 
 				tmHit.setScore(score);
 				tmHit.setMatchType(matchType);
 
 				// check if the penalties have pushed the match below threshold
				// add any such hits to a list for later removal
 				if (tmHit.getScore() < threshold) {
					tmHitsToRemove.add(tmHit);
 				}
 			}
			
			// remove hits that went below the threshold						
			tmHitCandidates.removeAll(tmHitsToRemove);
 
 			/*
 			 * System.out.println(queryFrag.toString()); System.out.println(tmHit.getScore());
 			 * System.out.println(tmHit.getMatchType()); System.out.println(tmHit.getTu().toString());
 			 * System.out.println();
 			 */
 
 			// sort TmHits on TmMatchType, Score and Source String
 			Collections.sort(tmHitCandidates);
 		} catch (IOException e) {
 			throw new OkapiIOException("Could not complete query.", e);
 		}
 
 		int lastHitIndex = max;
 		if (max >= tmHitCandidates.size()) {
 			lastHitIndex = tmHitCandidates.size();
 		}
 		return tmHitCandidates.subList(0, lastHitIndex);
 	}
 
 	/**
 	 * Search for concordance matches
 	 * 
 	 * 
 	 * @param threshold
 	 *            the minumum score to return (between 0.0 and 1.0)
 	 * @param query
 	 *            the query
 	 * @param queryFrag
 	 *            the text fragment for the query.
 	 * @param metadata
 	 *            any associated attributes to use for filter.
 	 * @return the list of hits found for the given arguments (never null).
 	 */
 	List<TmHit> getConcordanceHits(int max, Query query, String queryFrag, Metadata metadata) {
 		List<TmHit> tmHitCandidates;
 
 		try {
 			tmHitCandidates = getTopHits(query, metadata);
 			for (TmHit tmHit : tmHitCandidates) {
 				tmHit.setScore(tmHit.getScore());
 				tmHit.setMatchType(TmMatchType.SIMPLE_CONCORDANCE);
 			}
 
 			/*
 			 * System.out.println(queryFrag.toString()); System.out.println(tmHit.getScore());
 			 * System.out.println(tmHit.getMatchType()); System.out.println(tmHit.getTu().toString());
 			 * System.out.println();
 			 */
 
 			// sort TmHits on TmMatchType, Score and Source String
 			Collections.sort(tmHitCandidates);
 
 		} catch (IOException e) {
 			throw new OkapiIOException("Could not complete query.", e);
 		}
 
 		int lastHitIndex = max;
 		if (max >= tmHitCandidates.size()) {
 			lastHitIndex = tmHitCandidates.size();
 		}
 		return tmHitCandidates.subList(0, lastHitIndex);
 	}
 
 	/**
 	 * Creates a {@link TranslationUnit} for a given document.
 	 * 
 	 * @param doc
 	 *            the document from which to create the new translation unit.
 	 * @param srcCodedText
 	 *            the source coded text to re-use.
 	 * @param srcCodes
 	 *            the source codes to re-use.
 	 * @return a new translation unit for the given document.
 	 */
 	private TranslationUnit createTranslationUnit(Document doc, String srcCodedText,
 			List<Code> srcCodes) {
 		TextFragment frag = new TextFragment();
 		frag.setCodedText(srcCodedText, srcCodes, false);
 		TranslationUnitVariant srcTuv = new TranslationUnitVariant(getLocaleValue(doc,
 				TranslationUnitField.SOURCE_LANG), frag);
 
 		frag = new TextFragment();
 		List<Code> codes = Code
 				.stringToCodes(getFieldValue(doc, TranslationUnitField.TARGET_CODES));
 		String codedText = getFieldValue(doc, TranslationUnitField.TARGET);
 		frag.setCodedText(codedText == null ? "" : codedText, codes, false);
 		TranslationUnitVariant trgTuv = new TranslationUnitVariant(getLocaleValue(doc,
 				TranslationUnitField.TARGET_LANG), frag);
 
 		TranslationUnit tu = new TranslationUnit(srcTuv, trgTuv);
 		for (MetadataType type : MetadataType.values()) {
 			tu.setMetadataValue(type, getFieldValue(doc, type));
 		}
 		return tu;
 	}
 
 	private TranslationUnit createTranslationUnit(Document doc) {
 		TextFragment frag = new TextFragment();
 		List<Code> codes = Code
 				.stringToCodes(getFieldValue(doc, TranslationUnitField.SOURCE_CODES));
 		frag.setCodedText(getFieldValue(doc, TranslationUnitField.SOURCE_EXACT), codes, false);
 		TranslationUnitVariant srcTuv = new TranslationUnitVariant(getLocaleValue(doc,
 				TranslationUnitField.SOURCE_LANG), frag);
 
 		frag = new TextFragment();
 		codes = Code.stringToCodes(getFieldValue(doc, TranslationUnitField.TARGET_CODES));
 		String codedText = getFieldValue(doc, TranslationUnitField.TARGET);
 		frag.setCodedText(codedText == null ? "" : codedText, codes, false);
 		TranslationUnitVariant trgTuv = new TranslationUnitVariant(getLocaleValue(doc,
 				TranslationUnitField.TARGET_LANG), frag);
 
 		TranslationUnit tu = new TranslationUnit(srcTuv, trgTuv);
 		for (MetadataType type : MetadataType.values()) {
 			tu.setMetadataValue(type, getFieldValue(doc, type));
 		}
 		return tu;
 	}
 
 	private class TranslationUnitIterator implements Iterator<TranslationUnit> {
 
 		private int currentIndex;
 		private int maxIndex;
 		private IndexReader ir;
 
 		TranslationUnitIterator() {
 			try {
 				ir = openIndexReader();
 			} catch (CorruptIndexException cie) {
 				throw new OkapiIOException(cie.getMessage(), cie);
 			} catch (IOException ioe) {
 				throw new OkapiIOException(ioe.getMessage(), ioe);
 			}
 			currentIndex = 0;
 			maxIndex = ir.maxDoc();
 		}
 
 		public boolean hasNext() {
 			return currentIndex < maxIndex;
 		}
 
 		public TranslationUnit next() {
 			TranslationUnit tu = null;
 			if (hasNext()) {
 				try {
 					// Using createTranslationUnit(), not
 					// createTranslationUnit()
 					// ensure that we get the inline codes
 					tu = createTranslationUnit(ir.document(currentIndex++));
 				} catch (CorruptIndexException cie) {
 					throw new OkapiIOException(cie.getMessage(), cie);
 				} catch (IOException ioe) {
 					throw new OkapiIOException(ioe.getMessage(), ioe);
 				}
 			}
 			return tu;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException(
 					"Will not support remove method - Please remove items via ITmSeeker interface");
 		}
 	}
 
 	public void close() {
 		try {
 			if (indexSearcher != null) {
 				indexSearcher.close();
 			}
 
 			if (indexReader != null) {
 				indexReader.close();
 			}
 		} catch (IOException e) {
 			LOGGER.log(Level.WARNING, "Exception closing Pensieve index.", e); //$NON-NLS-1$
 		}
 	}
 }
