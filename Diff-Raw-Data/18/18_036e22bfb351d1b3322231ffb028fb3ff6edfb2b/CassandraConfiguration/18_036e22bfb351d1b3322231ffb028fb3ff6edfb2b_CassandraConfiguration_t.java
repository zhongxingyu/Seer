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
	private List<String> contactPoints;
 
 
	public List<String> getContactPoints() {
		return contactPoints;
 	}
 
	public void setContactPoints(List<String> contactPoints) {
		this.contactPoints = contactPoints;
 	}
 }
