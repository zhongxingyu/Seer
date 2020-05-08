 package org.netvogue.server.neo4japi.repository;
 
 import java.util.Date;
 
 import org.netvogue.server.neo4japi.domain.StatusUpdate;
 import org.netvogue.server.neo4japi.service.StatusUpdateData;
 import org.springframework.data.neo4j.annotation.Query;
 import org.springframework.data.neo4j.repository.GraphRepository;
 
 public interface StatusUpdateRepository extends GraphRepository<StatusUpdate> {
 	
 	@Query("START user=node:search(username={0}) MATCH user-[r?:STATUS]-update delete r return update, user")
 	StatusUpdateData getlatestStatusUpdate(String username);
 	
 	@Query("START n=node:search(username={0}) " +
 		   "MATCH n-[r:STATUS]-oldsu-[:NEXT*0..]-su " +
 		   "return su SKIP {1} LIMIT {2}")
 	Iterable<StatusUpdate> getMyStatusUpdates(String username, int skip, int limit);
 	
 	@Query("START n=node:search(username={0}) MATCH n-[rels:NETWORK*0..1]-user " +
 			"WHERE ALL(r in rels WHERE r.status? = 'CONFIRMED') " +
 			"WITH user " +
 			"MATCH user-[:STATUS]-oldsu-[:NEXT*0..]-update " +
			"return update, user SKIP {1} LIMIT {2} ")
 	Iterable<StatusUpdateData> getAllStatusUpdates(String username, int skip, int limit);
 	
 	@Query("START n=node:search(username={0}) MATCH n-[r?:STATUS]->oldsu DELETE r " +
 			"WITH n,oldsu " +
 			"CREATE n-[:STATUS]->(newsu {statusid:{1},statusUpdate:{2},postedDate:{3},__type__:'org.netvogue.server.neo4japi.domain.StatusUpdate'}) " +
 			"WITH newsu, oldsu CREATE newsu-[:NEXT]->oldsu WHERE oldsu <> null RETURN newsu")
 	StatusUpdate newStatusUpdate(String username, String statusid, String statusupdate, Date createddate);
 	
 	@Query("START n=node:statusid(statusid={0}) SET n.statusUpdate = {1}")
 	void editStatusUpdate(String id, String message);
 	
 	@Query("START n=node:statusid(statusid={0}) MATCH n<-[:STATUS]-u, n<-[:NEXT]-previous, n-[:NEXT]->next " +
 			"")
 	void deleteStatusUpdate(String id);
 	
 	//Query to delete all status updates and its relationships
 	//START n=node:search("username:*") MATCH n-[r:STATUS]-oldsu-[ne:NEXT*0..]-su FOREACH(nee in ne: DELETE nee) DELETE r, su;
 }
