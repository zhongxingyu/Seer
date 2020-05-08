 package org.iucn.sis.server.api.queries;
 
 /**
  * CannedQueries.java
  * 
  * Canned Queries should include hand-written, probably 
  * language-specific SQL queries that are necessary for 
  * whatever reason.  While there should not be many of 
  * these, sometimes they are needed. 
  * 
  * @author carl.scott@solertium.com
  *
  */
 public interface CannedQueries {
 	
 	public String getSubscribableWorkingSets(int userid);
 	
 	public String getWorkingSetsForTaxon(int taxonid);
 
 }
