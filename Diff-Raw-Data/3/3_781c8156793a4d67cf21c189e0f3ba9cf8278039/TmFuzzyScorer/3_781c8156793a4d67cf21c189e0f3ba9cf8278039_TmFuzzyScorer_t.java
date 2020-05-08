 package net.sf.okapi.tm.pensieve.scorers;
 
 import gnu.trove.TIntIntHashMap;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sf.okapi.tm.pensieve.common.TranslationUnitField;
 
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermPositions;
 import org.apache.lucene.search.Scorer;
 import org.apache.lucene.search.Similarity;
 import org.apache.lucene.util.OpenBitSet;
 import org.apache.lucene.util.OpenBitSetIterator;
 
 public class TmFuzzyScorer extends Scorer {
 	// note that the ROUGH_THRESHOLD is the lowest accepted threshold
 	// TODO: externalize this
 	private static float ROUGH_CUTOFF = 0.50f;
 
 	private List<Term> terms;
 	private IndexReader reader;
 	private float threshold;
 	private float score;
 	private int currentDoc;
 	private int roughThresholdFreq;
 	private OpenBitSetIterator docPointerIterator;
 	private TIntIntHashMap scoredDocs;
 	private int uniqueTermSize;
 
 	public TmFuzzyScorer(float threshold, Similarity similarity,
 			List<Term> terms, IndexReader reader) throws IOException {
 		super(similarity);
 		this.reader = reader;
 		this.threshold = threshold;
 		this.terms = terms;
 		this.roughThresholdFreq = (int) (terms.size() * ROUGH_CUTOFF);
 		this.scoredDocs = new TIntIntHashMap();
 	}
 
 	private void calculateScores() throws IOException {
 		// initialize buffers
 		OpenBitSet docPointers = new OpenBitSet(reader.maxDoc());
 		TermPositions tp = null;
 		
 		List<Term> uniqueTerms = new LinkedList<Term>(new LinkedHashSet<Term>(terms));
 		uniqueTermSize = uniqueTerms.size();
 		for (Iterator<Term> iter = uniqueTerms.iterator(); iter.hasNext();) {
 			try {
 				tp = reader.termPositions(iter.next());
 				while (tp.next()) {
 					int f = scoredDocs.adjustOrPutValue(tp.doc(), 1, 1);
 					if (f > roughThresholdFreq) {
 						docPointers.fastSet(tp.doc());
 					}
 				}
 			} finally {
 				if (tp != null) {
 					tp.close();
 				}
 			}
 		}
 
 		if (docPointers.cardinality() > 0) {
 			docPointerIterator = (OpenBitSetIterator) docPointers.iterator();
 		}
 	}
 
 	@Override
 	public int advance(int target) throws IOException {
 		if (target == NO_MORE_DOCS) {
 			return NO_MORE_DOCS;
 		}
 
 		while((currentDoc = nextDoc()) < target) {		
 		}
 		
 		return currentDoc;
 	}
 
 	@Override
 	public float score() throws IOException {
 		return score;
 	}
 
 	public boolean scoresDocsOutOfOrder() {
 		return false;
 	}
 
 	@Override
 	public int nextDoc() throws IOException {
 		// test for first time
 		if (docPointerIterator == null) {
 			calculateScores();
			if (docPointerIterator == null) {
				return NO_MORE_DOCS;
			}
 		}
 
 		while (true) {
 			currentDoc = docPointerIterator.nextDoc();
 			if (currentDoc == NO_MORE_DOCS) {
 				return currentDoc;
 			}
 			
 			if (calculateScore() >= threshold) {
 				return currentDoc;
 			} 
 		}
 	}
 	
 	private float calculateScore() throws IOException {
 		score = (float) ((2.0f * (float) scoredDocs.get(currentDoc)) / (float) (reader
 				.getTermFreqVector(currentDoc, TranslationUnitField.SOURCE.name()).size() 
 				+ uniqueTermSize)) * 100.0f;
 		
 		return score;
 	}
 }
