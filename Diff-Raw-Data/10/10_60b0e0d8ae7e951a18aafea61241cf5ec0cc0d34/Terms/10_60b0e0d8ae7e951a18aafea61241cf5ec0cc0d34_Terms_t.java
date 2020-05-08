 package plusone.utils;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class Terms {
 
     public static class Term implements Comparable {
 	
     /** 
      * Warning: doc_train might not do what you think (it can contain
      * testing documents).  See generateData in PaperAbstract.
      */
 	private List<PaperAbstract> doc_train;
 	private List<PaperAbstract> doc_test;
 	
 	public int id, totalCount = 0;
 	
 	public Term(int id) {
 	    this.id = id;
 	    doc_train = new ArrayList<PaperAbstract>();
 	    doc_test = new ArrayList<PaperAbstract>();
 	}
 	
 	public int compareTo(Object obj) {
             Term other = (Term)obj;
             int otherCount = other.totalCount;
             if (id == other.id && this != obj) throw new RuntimeException("MOO");
            return (totalCount == otherCount) ? -(id - other.id) : -(totalCount - otherCount);
 	}
 	
 	public void addDoc(PaperAbstract doc, boolean test){
 	    if (test)
 		doc_test.add(doc);
 	    else
 		doc_train.add(doc);
 	}
 	
 	public int idfRaw() { return doc_train.size(); }
 	
 	public double trainingIdf(int nDocs) {
 	    return Math.log(nDocs / (double)idfRaw());
 	}
 	
 	public List<PaperAbstract> getDocTrain() { return doc_train; }
 	public List<PaperAbstract> getDocTest() { return doc_train; }
     }
     
     class TermsEnumerator implements Iterable {
 	class TermsIterator implements Iterator {
 	    int c = 0;
 	    public boolean hasNext() { return c < terms.length; }
 	    public Term next() { return terms[c++]; }
 	    public void remove() {}	    
 	}
 
 	Term[] terms;
 	TermsEnumerator(Term[] terms) { this.terms = terms; }
 	public Iterator<Term> iterator() { return new TermsIterator(); }
     }
     
     Term[] originalTerms;
     Term[] sortedTerms;
 
     public Terms(Term[] terms) {
 	originalTerms = terms;
 	sortedTerms = new Term[terms.length];
 
 	for (int c = 0; c < originalTerms.length; c ++) {
 	    sortedTerms[c] = originalTerms[c];
 	}
 	Arrays.sort(sortedTerms);
     }
 
     public int size() { return originalTerms.length; }
 
     public Term get(int c) { return originalTerms[c]; }
 
     public Iterable<Term> getSortedTermsIterable() { 
 	return new TermsEnumerator(sortedTerms);
     }
 }
