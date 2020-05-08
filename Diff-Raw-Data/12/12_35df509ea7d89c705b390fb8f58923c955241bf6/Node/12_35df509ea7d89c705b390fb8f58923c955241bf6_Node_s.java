package cit.uniroma1.dis.wsngroup.extensions.simulations;
 
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class Node {
 	private long ID;
 	private String name;
 	private LinkedList<Node> tempEdges;
 	private int tokens;
 	private int inDegree;
 	private int outDegree;
 	private int CWPcount;
 	private int CWEcount;
 	private double CWPestimation;
 	private double CWEestimation;
 	private List<Long> encountered;
 	private List<Long> subnetwork;
 	private FlajoletMartinSketch DUCsketch;
 	private FlajoletMartinSketch ENSsketch;
 	private long DUCestimation;
 	private long ENSestimation;
 	
 	public Node(long tagid, String n) {
 		this(tagid);
 		name = n;
 	}
 	
 	public Node (long tagid) {
 		ID = tagid;
 		name = "";
 		tempEdges = new LinkedList<Node>();
 		tokens = 0;
 		inDegree = 0;
 		outDegree = 0;
 		CWPcount = 0;
 		CWEcount = 0;
 		CWPestimation = -1;
 		CWEestimation = -1;
 		encountered = new LinkedList<Long>();
 		subnetwork = new LinkedList<Long>();
 		subnetwork.add(ID);
 		DUCsketch = null;
 		ENSsketch = null;
 		DUCestimation = -1;
 		ENSestimation = -1;
 	}
 	
 	public void initializeSketch(int bitmapSize, FM_HashFunction[] hashes) {
 		DUCsketch = new FlajoletMartinSketch(bitmapSize, hashes);
 		ENSsketch = new FlajoletMartinSketch(bitmapSize, hashes);
 		ENSsketch.update(ID);
 	}
 
 	public long getID() {
 		return ID;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public LinkedList<Node> getTempEdges() {
 		return tempEdges;
 	}
 	
 	public void addTempEdge(Node n) {
 		tempEdges.add(n);
 	}
 	
 	public void removeTempEdge(Node n) {
 		tempEdges.remove(n);
 	}
 	
 	public void resetTempEdges() {
 		tempEdges.clear();
 	}
 
 	public int getCWPcount() {
 		return CWPcount;
 	}
 	
 	public int getCWEcount() {
 		return CWEcount;
 	}
 
 	public void addToken() {
 		tokens++;
 	}
 	
 	public void removeToken() {
 		tokens--;
 	}
 	
 	public int getTokens() {
 		return tokens;
 	}
 	
 	public void incrementCWPCount() {
 		CWPcount++;
 	}
 	
 	public void incrementCWECount() {
 		CWEcount++;
 	}
 	
 	public double getCWPestimation() {
 		return CWPestimation;
 	}
 	
 	public double getCWEestimation() {
 		return CWEestimation;
 	}
 	
 	public long getDUCestimation() {
 		return DUCestimation;
 	}
 	
 	public long getENSestimation() {
 		return ENSestimation;
 	}
 	
 	public void setCWPestimation(double p) {
 		CWPestimation = p;
 	}
 	
 	public void setCWEestimation(double p) {
 		CWEestimation = p;
 	}
 	
 	public void setDUCestimation(long e) {
 		DUCestimation = e;
 	}
 	
 	public void setENSestimation(long e) {
 		ENSestimation = e;
 	}
 	
 	
 	public int getInDegree() {
 		return inDegree;
 	}
 
 	public void incrementInDegree() {
 		inDegree++;
 	}
 	
 	public int getOutDegree() {
 		return outDegree;
 	}
 
 	public void incrementOutDegree() {
 		outDegree++;
 	}
 	
 	public FlajoletMartinSketch getDUCsketch() {
 		return DUCsketch;
 	}
 	
 	public FlajoletMartinSketch getENSsketch() {
 		return ENSsketch;
 	}
 	
 	public void sumSketchToENSsketch(FlajoletMartinSketch fm2) {
 		if (fm2 != null)
 			ENSsketch.sum(fm2);
 	}
 
 	public String printCWPestimation() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append(ID);
 		if (!name.equals(""))
 			sb.append("(" + name + ")");
 		
 		sb.append(";" + CWPestimation + ";" + inDegree + ";" + outDegree + "\n");
 		
 		return sb.toString();
 	}
 	
 	public String printCWEestimation() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append(ID);
 		if (!name.equals(""))
 			sb.append("(" + name + ")");
 		
 		sb.append(";" + CWEestimation + ";" + inDegree + ";" + outDegree + "\n");
 		
 		return sb.toString();
 	}
 	
 	public String printDUCestimation() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append(ID);
 		if (!name.equals(""))
 			sb.append("(" + name + ")");
 		
 		sb.append(";" + DUCsketch.estimateCount() + ";" + encountered.size() + "\n");
 		
 		return sb.toString();
 	}
 	
 	public String printENSestimation() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append(ID);
 		if (!name.equals(""))
 			sb.append("(" + name + ")");
 		
 		sb.append(";" + ENSsketch.estimateCount() + ";" + subnetwork.size() + "\n");
 		
 		return sb.toString();
 	}
 	
 	/** Updates FM sketch for the DUC estimation */
 	public void addIdToDUCsketch(long id) {
 		System.out.println(ID);
 		DUCsketch.update(id);
 	}
 	
 	public void addEncountered(long id) {
 		if (!encountered.contains(id))
 			encountered.add(id);
 	}
 	
 	public List<Long> getEncountered() {
 		return encountered;
 	}
 	
 	public int getDistinctUsers() {
 		return encountered.size();
 	}
 	
 	public List<Long> getSubnetwork() {
 		return subnetwork;
 	}
 	
 	public int getSubnetworkSize() {
 		return subnetwork.size();
 	}
 	
 	public void addToSubnetwork(long id) {
 		if (!subnetwork.contains(id))
 			subnetwork.add(id);
 	}
 	
 	public void addToSubnetwork(List<Long> ids) {
 		for (long id : ids)
 			if (!subnetwork.contains(id))
 				subnetwork.add(id);
 	}
 }
 
 class NodesIDComparator implements Comparator<Node> {
 	@Override
 	public int compare(Node n1, Node n2) {
 		if (n1.getID() == n2.getID()) return 0;
 		if (n1.getID() < n2.getID()) return -1;
 		return 1;
 	}
 }
 
 class NodesCWPComparator implements Comparator<Node> {
 	@Override
 	public int compare(Node n1, Node n2) {
 		if (n1.getCWPestimation() == n2.getCWPestimation()) return 0;
 		if (n1.getCWPestimation() > n2.getCWPestimation()) return -1;
 		return 1;
 	}
 }
 
 class NodesCWEComparator implements Comparator<Node> {
 	@Override
 	public int compare(Node n1, Node n2) {
 		if (n1.getCWEestimation() == n2.getCWEestimation()) return 0;
 		if (n1.getCWEestimation() > n2.getCWEestimation()) return -1;
 		return 1;
 	}
 }
 
 class NodesDistinctCountComparator implements Comparator<Node> {
 	@Override
 	public int compare(Node n1, Node n2) {
 		if (n1.getDUCsketch().estimateCount() == n2.getDUCsketch().estimateCount()) return 0;
 		if (n1.getDUCsketch().estimateCount() > n2.getDUCsketch().estimateCount()) return -1;
 		return 1;
 	}
 }
 
 class NodesInDegreeComparator implements Comparator<Node> {
 	@Override
 	public int compare(Node n1, Node n2) {
 		if (n1.getInDegree() == n2.getInDegree()) return 0;
 		if (n1.getInDegree() > n2.getInDegree()) return -1;
 		return 1;
 	}
 }
 
 class NodesOutDegreeComparator implements Comparator<Node> {
 	@Override
 	public int compare(Node n1, Node n2) {
 		if (n1.getOutDegree() == n2.getOutDegree()) return 0;
 		if (n1.getOutDegree() > n2.getOutDegree()) return -1;
 		return 1;
 	}
 }
