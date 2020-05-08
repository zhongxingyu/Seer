 package ceid.netcins.content;
 
 import java.io.Serializable;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 /**
  * This ContentField type contains a set of "terms" (String values) and the 
  * corresponding "term frequencies" (tf), obtained from a source such as a file.
  * 
  * @author Andreas Loupasakis
  */
 public class TokenizedField extends ContentField implements Serializable {
 
 	private static final long serialVersionUID = 3889036995034671146L;
 
 	// Terms are provided in sorted order;
 	private Hashtable<String, Integer> termFreq;
 
 	private static final Random rng = new Random(System.currentTimeMillis());
 
 	public TokenizedField(String name, TreeMap<String, Integer> tfm, boolean isPublic) {
 		super(name, isPublic);
 
 		// Allocate the necessary size and transfer the terms and tfs
 		termFreq = new Hashtable<String, Integer>();
 		if (tfm != null)
 			termFreq.putAll(tfm);
 	}
 
 	public TokenizedField(String name, TreeMap<String, Integer> tfm) {
 		this(name, tfm, ContentField.defaultAccessMode);
 	}
 
 	/**
 	 * Constructor which must be used if we dont have term frequencies
 	 * 
 	 * @param name
 	 * @param tfm
 	 */
 	public TokenizedField(String name, TreeSet<String> tfm, boolean isPublic) {
 		super(name, isPublic);
 
 		// Allocate the necessary size and transfer the terms
 		termFreq = new Hashtable<String, Integer>();
 		if (tfm != null)
 			for (String term : tfm) {
 				Integer freq = (termFreq.containsKey(term) ? termFreq.get(term) : 0);
 				termFreq.put(term, freq);
 			}
 	}
 
 	public TokenizedField(String name, TreeSet<String> tfm) {
 		this(name, tfm, ContentField.defaultAccessMode);
 	}
 
 	public void merge(TokenizedField tkf) {
 		if (tkf.termFreq == null || tkf.termFreq.size() == 0)
 			return;
 		if (termFreq == null) {
 			termFreq = new Hashtable<String, Integer>(tkf.termFreq);
 			return;
 		}
 		Iterator<String> keys = tkf.termFreq.keySet().iterator();
 		Iterator<Integer> values = tkf.termFreq.values().iterator();
 		while (keys.hasNext()) {
 			String otherKey = keys.next();
 			Integer otherValue = values.next();
 			addTerm(otherKey, otherValue);
 		}
 	}
 
 	public String[] getTerms() {
		Object[] o = termFreq.keySet().toArray();
		String[] ret = new String[o.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (String)o[i];
		return ret;
 	}
 
 	public Integer[] getTF() {
		Object[] o = termFreq.values().toArray();
		Integer[] ret = new Integer[o.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (Integer)o[i];
		return ret;
 	}
 
 	public void addTerm(String term) {
 		addTerm(term, 1);
 	}
 
 	public void addTerm(String term, Integer freq) {
 		if (freq == null || freq < 0)
 			throw new RuntimeException("Negative frequency");
 		Integer oldFreq = termFreq.get(term);
 		if (oldFreq == null)
 			oldFreq = Integer.valueOf(0);
 		termFreq.put(term, freq + oldFreq);
 	}
 
 	public String randomTerm() {
 		if (termFreq.isEmpty())
 			return null;
 		int num = rng.nextInt(termFreq.size());
 		return (String)(termFreq.keySet().toArray()[num]);
 	}
 
 	/* (non-Javadoc)
 	 * @see ceid.netcins.content.ContentField#size()
 	 */
 	public int size() {
 		int sum = super.size();
 		for (String s : termFreq.keySet()) {
 			sum += s.getBytes().length + 4; // 4 bytes for the term frequencies
 		}
 		return sum;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		boolean isFirst = true;
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("TKF{ \"" + name + "\" : [ ");
 		Iterator<String> keys = termFreq.keySet().iterator();
 		Iterator<Integer> values = termFreq.values().iterator();
 		while (keys.hasNext()) {
 			if (!isFirst)
 				buffer.append(" , ");
 			else
 				isFirst = false;
 
 			String term = keys.next();
 			Integer freq = values.next();
 			buffer.append("{ \" " + term + "\"");
 			if (freq != 0)
 				buffer.append(" : " + freq);
 			buffer.append(" }");
 		}
 		buffer.append("]}");
 		return buffer.toString();
 	}
 
 	public String toStringWithoutTF() {
 		boolean isFirst = true;
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("TKF{ \"" + name + "\" : [ ");
 		Iterator<String> keys = termFreq.keySet().iterator();
 		while (keys.hasNext()) {
 			if (!isFirst)
 				buffer.append(" , ");
 			else
 				isFirst = false;
 
 			buffer.append(keys.next());
 		}
 		buffer.append("]}");
 		return buffer.toString();
 	}
 }
