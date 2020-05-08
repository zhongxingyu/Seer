 package com.pardot.analyticsservice.resources;
 
 
 import com.pardot.analyticsservice.cassandra.CassandraConfiguration;
 import com.yammer.metrics.annotation.Timed;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 /**
  * Pardot, an ExactTarget company
  * User: Michael Frank
  * Date: 4/17/13
  */
 
 @Path("/analyticsdata")
 @Produces(MediaType.APPLICATION_JSON)
 public class AnalyticsDataResource {
 
 	private CassandraConfiguration cassandraConfiguration;
 
 	public AnalyticsDataResource(CassandraConfiguration cassandraConfiguration) {
 		this.cassandraConfiguration = cassandraConfiguration;
 	}
 
 	@GET
 	@Timed
 	public CassandraConfiguration showConfiguration() {
 		return cassandraConfiguration;
 	}
 
 }
