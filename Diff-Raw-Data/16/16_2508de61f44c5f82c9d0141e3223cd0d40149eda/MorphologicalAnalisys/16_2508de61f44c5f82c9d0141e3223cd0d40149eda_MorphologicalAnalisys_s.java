 package dart.blackcat.talker.ru;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class MorphologicalAnalisys implements Serializable {
 
 	private static final Log LOG = LogFactory.getLog(MorphologicalAnalisys.class);
 	private static final long serialVersionUID = -7330773290835787138L;
 	
 	private String 		word;
 	private String[] 	prefixes;
 	private String[] 	roots;
 	private String 		interfix;
 	private String[] 	suffixes;
 	private String		end;
 	private WordClass 	wordClass;
 	
 	SortedSet<String> 	rootSet = (new Root()).getSortedMorfemes(false);
 	SortedSet<String> 	prefixSet = (new Prefix()).getSortedMorfemes(false);
 	SortedSet<String> 	suffixSet = (Suffix.getInstance()).getSortedMorfemes(false);
 	SortedSet<String> 	interfixSet = (new Interfix()).getSortedMorfemes(false);
 	
 	public MorphologicalAnalisys(String word) {
 		this.word = word;
 		
 		
 		
 		for (Iterator<String> i = rootSet.iterator(); i.hasNext();) {
 			String root = i.next();
 			
 			int position = word.indexOf(root);
 			if (position > -1) {
 				LOG.debug("Root: " + root);	//TODO second root (use interfixSet)
 				roots = new String[] {root};
 				
 				String prefixesString = word.substring(0, position);
 				LOG.debug("Prefix(es): " + prefixesString);
 				if (prefixesString.isEmpty()) {
 					prefixes = new String[0];
 				} else {
 					for (Iterator<String> j = prefixSet.iterator(); j.hasNext();) {
 						String prefix = j.next();
 						if (prefixesString.equalsIgnoreCase(prefix)) {
 							prefixes = new  String[] {prefix};
 						}
 					}
 					if (this.prefixes == null) {
 						LOG.error("Complex prefix! not implemented yet!"); // TODO
 						prefixes = new  String[] {prefixesString};
 					}
 				}
 				
 				String suffixAndEnd = word.substring(position + root.length());
 				LOG.debug("SuffixAndEnd: " + suffixAndEnd);
 				if (suffixAndEnd.isEmpty()) {
 					suffixes = new String[0];
 					end = "";
 					wordClass = new Verb();
 					return;
 				} else {
 					// TODO suffixes (use suffixSet)	// test
 					List<String> wordSuffixList = new ArrayList<String>();
 					for (Iterator<String> k = suffixSet.iterator(); k.hasNext();) {
 						String suffix = k.next();
 						if (suffixAndEnd.startsWith(suffix)) {
 							wordSuffixList.add(suffix);
 							LOG.debug("Suffix: " + suffix);
 							if (wordClass == null) {
 								wordClass = Suffix.getInstance().resolveWordClass(suffix);
 							}
							suffixAndEnd.substring(suffix.length());
 						}
 					}
 					suffixes = new String[wordSuffixList.size()];
 					wordSuffixList.toArray(suffixes);
 					
 					
 					// TODO end		// test
 					end = suffixAndEnd;
 				}
 				
 //				wordClass = new Noun(); //TODO plug
 //				return;
 			}
 		}
 		if (wordClass == null) {
 			throw new MorphemeParsingException("Root not found.");	
 		}
 	}
 	
 	/**
 	 * Find root in string
 	 * @param s {@link String}
 	 * @return {@link String} array with three elements: 0 - first word part; 1 - root; 2 - second word part.
 	 */
 	public String[] findRoot(String s) {
 		return prefixes;
 	}
 	
 	public String[] getPrefixes() {
 		return prefixes;
 	}
 	public void setPrefixes(String[] prefixes) {
 		this.prefixes = prefixes;
 	}
 	public String[] getRoots() {
 		return roots;
 	}
 	public void setRoots(String[] roots) {
 		this.roots = roots;
 	}
 	public String getInterfix() {
 		return interfix;
 	}
 	public void setInterfix(String interfix) {
 		this.interfix = interfix;
 	}
 	public String[] getSuffixes() {
 		return suffixes;
 	}
 	public void setSuffixes(String[] suffixes) {
 		this.suffixes = suffixes;
 	}
 	public String getEnd() {
 		return end;
 	}
 	public void setEnd(String end) {
 		this.end = end;
 	}
 	public String getWord() {
 		return word;
 	}
 	public void setWord(String word) {
 		this.word = word;
 	}
 	public WordClass getWordClass() {
 		return wordClass;
 	}
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer("MorphologicalAnalisys: \n");
 		for (int i = 0; i < prefixes.length; i++) {
 			sb.append("prefix: ").append(prefixes[i]).append("\n");
 		}
 		for (int i = 0; i < roots.length; i++) {
 			sb.append("root: ").append(roots[i]).append("\n");
 		}
 		for (int i = 0; i < suffixes.length; i++) {
 			sb.append("suffix: ").append(suffixes[i]).append("\n");
 		}
 		if ( ! (end == null || end.isEmpty()) ) {
 			sb.append("end: ").append(end).append("\n");	
 		}
 		return sb.toString();
 	}
 }
