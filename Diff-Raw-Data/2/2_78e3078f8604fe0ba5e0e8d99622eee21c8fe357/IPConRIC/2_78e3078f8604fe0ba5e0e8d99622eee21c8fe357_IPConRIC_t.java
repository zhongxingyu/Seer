 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;
 
 import java.util.UUID;
 
 /**
  * Object to hold Revision/Issue/Cluster triple for use in drls
  * @author dws04
  *
  */
 public class IPConRIC extends IPConFact {
 	
 	/*final Integer revision;
 	final String issue;
 	final UUID cluster;*/
 	
 	/**
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 */
 	public IPConRIC(Integer revision, String issue, UUID cluster) {
 		super(revision, issue, cluster);
 		/*this.revision = revision;
 		this.issue = issue;
 		this.cluster = cluster;*/
 	}
 	/*
 	
 	*//**
 	 * @return the revision
 	 *//*
 	public Integer getRevision() {
 		return revision;
 	}
 
 
 	*//**
 	 * @return the issue
 	 *//*
 	public String getIssue() {
 		return issue;
 	}
 
 
 	*//**
 	 * @return the cluster
 	 *//*
 	public UUID getCluster() {
 		return cluster;
 	}*/
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
		return "IPConRIC [revision=" + revision + ", issue=" + issue + ", cluster=" + cluster + "]";
 	}
 	
 	
 }
