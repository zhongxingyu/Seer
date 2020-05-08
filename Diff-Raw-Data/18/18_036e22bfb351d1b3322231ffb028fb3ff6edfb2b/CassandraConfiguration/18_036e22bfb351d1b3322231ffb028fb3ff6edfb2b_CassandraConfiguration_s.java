 package com.pardot.analyticsservice.cassandra;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import javax.validation.constraints.NotNull;
 import java.util.List;
 
 /**
  * Pardot, an ExactTarget company
  * User: Michael Frank
  * Date: 4/17/13
  */
 public class CassandraConfiguration {
 	@NotNull
 	@JsonProperty
	private List<String> connectionPoints;
 
 
	public List<String> getConnectionPoints() {
		return connectionPoints;
 	}
 
	public void setConnectionPoints(List<String> connectionPoints) {
		this.connectionPoints = connectionPoints;
 	}
 }
