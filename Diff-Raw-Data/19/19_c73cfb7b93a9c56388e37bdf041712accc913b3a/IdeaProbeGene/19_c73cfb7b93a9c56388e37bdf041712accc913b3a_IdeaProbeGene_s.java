 package org.geworkbench.bison.datastructure.bioobjects;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;
 
 /**
  * ProbeGene data structure. The key attribute is Probe Id.
  * 
  * @author zm2165
  * @version $Id$
  */
 public class IdeaProbeGene implements Serializable, Comparable<IdeaProbeGene>{
 	
 	private static final long serialVersionUID = -8212763757638573012L;
 	
 	private String probeId;
 	private int locs=0;		//LOC edges of the gene
 	private int gocs=0;		//GOC edges of the gene
 	private ArrayList<IdeaEdge> edges=new ArrayList<IdeaEdge>();
 	private double cumLoc=1;//default is big
 	private double cumGoc=1;
 	private double nes;
 	
 	public IdeaProbeGene(String probeId){
 		this.probeId=probeId;
 	}	
 	
 	public String getProbeId() {
 		return probeId;
 	}
 
 	public void setLocs(int locs) {
 		this.locs = locs;
 	}
 
 	public int getLocs() {
 		return locs;
 	}
 
 	public void setGocs(int gocs) {
 		this.gocs = gocs;
 	}
 
 	public int getGocs() {
 		return gocs;
 	}
 
 	public void setCumLoc(double cumLoc) {
 		this.cumLoc = cumLoc;
 	}
 
 	public double getCumLoc() {
 		return cumLoc;
 	}
 
 	public void setCumGoc(double cumGoc) {
 		this.cumGoc = cumGoc;
 	}
 
 	public double getCumGoc() {
 		return cumGoc;
 	}
 
 	public void setEdges(ArrayList<IdeaEdge> edges) {
 		this.edges = edges;
 	}
 
 	public ArrayList<IdeaEdge> getEdges() {
 		return edges;
 	}
 
 	public int compareTo(IdeaProbeGene otherP) {
		double d = otherP.getNes() - nes;
		if(d<0) return -1;
		else if (d>0) return 1;
		else return 0;
 	}
 
 	public void setNes(double nes) {
 		this.nes = nes;
 	}
 
 	public double getNes() {
 		return nes;
 	}
 
 }
