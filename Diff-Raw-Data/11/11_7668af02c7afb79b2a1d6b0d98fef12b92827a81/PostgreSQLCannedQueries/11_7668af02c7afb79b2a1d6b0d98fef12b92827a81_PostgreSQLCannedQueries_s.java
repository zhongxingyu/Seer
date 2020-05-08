 package org.iucn.sis.server.api.queries;
 
 public class PostgreSQLCannedQueries extends BaseCannedQueries {
 	
 	public String getSubscribableWorkingSets(int userid) {
 		return format("SELECT * FROM working_set " +
 		"WHERE working_set.id NOT IN ( "+
 		"SELECT working_setid FROM working_set_subscribe_user " +
 		"WHERE userid = %s"+
 		") "+
 		"ORDER BY working_set.name", userid);
 	}
 	
 	@Override
 	public String getWorkingSetsForTaxon(int taxonid) {
 		return format("select * from working_set where working_set.id in " + 
 		"(select working_setid from working_set_taxon where taxonid = '%s');", taxonid);
 	}
 
 }
