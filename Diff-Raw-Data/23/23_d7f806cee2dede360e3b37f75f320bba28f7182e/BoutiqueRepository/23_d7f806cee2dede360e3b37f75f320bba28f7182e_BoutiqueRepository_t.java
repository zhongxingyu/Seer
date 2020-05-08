 package org.netvogue.server.neo4japi.repository;
 
 //project specific
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.*;
 
 //Spring specific
 import org.springframework.data.neo4j.annotation.Query;
 import org.springframework.data.neo4j.repository.*;
 import org.springframework.data.repository.query.Param;
 
 public interface BoutiqueRepository extends GraphRepository<Boutique>{
	@Query( "START n=node:search({0}) WHERE n.userType! = {1} RETURN n")
 	Iterable<Boutique> findBynameSearch(String query, USER_TYPE userType);
 	
 	@Query("START me=node:search({username}) /" +
 		    "MATCH me-[r:NETWORK]-network-[:COLLECTION]-collections /" +
 			"WHERE r.status == 'CONFIRMED' /" +
 		    "RETURN collections /" +
 			"SKIP {pagenumber*pagesize} LIMIT{pagesize}")
 	Iterable<Collection> getAllCollections(@Param("username") String username);
 	
 	@Query("START me=node:search({username}) /" +
 		    "MATCH me-[r:NETWORK]-network-[:COLLECTION]-collections /" +
 			"WHERE r.status == 'CONFIRMED' /" +
 		    "RETURN collections /" +
 			"SKIP {pagenumber*pagesize} LIMIT{pagesize}")
 	Iterable<Linesheet> getAllLinesheets(@Param("username") String username);
 }
