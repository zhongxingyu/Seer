 package org.netvogue.server.neo4japi.repository;
 
 //project specific
 
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.*;
 //Spring specific
 import org.springframework.data.neo4j.annotation.Query;
 import org.springframework.data.neo4j.repository.*;
 
 public interface BrandRepository extends GraphRepository<Brand>{
	@Query( "START n=node:search({0}) WHERE n.userType? = {1} RETURN n")
 	Iterable<Brand> findBynameSearch(String query, USER_TYPE userType);
 }
