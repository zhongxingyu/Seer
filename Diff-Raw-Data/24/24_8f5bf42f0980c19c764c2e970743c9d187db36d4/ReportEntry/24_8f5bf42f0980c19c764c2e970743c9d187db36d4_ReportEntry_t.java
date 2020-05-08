 package sw10.animus.reports;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import sw10.animus.analysis.ICostResult;
 
 import com.ibm.wala.ipa.callgraph.CGNode;
 
 /* 
 * Class for holding report representative data   
  */
 public class ReportEntry {
	/* Full path to java source file */
 	private String source;
 	
 	/* Package from source root directory */
 	private String packages;
 
 	/* Entry nodes and their results  */
 	private Map<CGNode, ICostResult> costByEntry;
 	
	/* Line numbers to highlight for each node */
 	private Map<CGNode, Set<Integer>> lineNumbersByEntry;
 	
 	/* Referenced nodes with their respective results for a given entry node */
 	private Map<CGNode, LinkedList<Map<CGNode, ICostResult>>> referencesByEntry;
 	
 	public ReportEntry() {
 		this.source = "";
 		this.costByEntry = new HashMap<CGNode, ICostResult>();
 		this.lineNumbersByEntry = new HashMap<CGNode, Set<Integer>>();
 		this.referencesByEntry = new HashMap<CGNode, LinkedList<Map<CGNode,ICostResult>>>();
 	}
 	
 	public String getSource() {
 		return source;
 	}
 	
 	public void setSource(String source) {
 		this.source = source;
 	}
 	
 	public String getPackage() {
 		return packages;
 	}
 	
 	public void setPackages(String packages) {
 		this.packages = packages;
 	}
 	
 	public void addEntry(CGNode node, ICostResult cost) {
 		this.costByEntry.put(node, cost);
 	}
 	
 	public Map<CGNode, ICostResult> getEntries() {
 		return costByEntry;
 	}
 	
 	public Set<Integer> getLineNumbers(CGNode entry) {
 		return lineNumbersByEntry.get(entry);
 	}
 	
 	public void setLineNumbers(Set<Integer> lineNumbers, CGNode entry) {
 		if(lineNumbersByEntry.containsKey(entry)) {
 			lineNumbersByEntry.get(entry).addAll(lineNumbers);
 		} else {
 			lineNumbersByEntry.put(entry, lineNumbers);
 		}
 	}
 }
