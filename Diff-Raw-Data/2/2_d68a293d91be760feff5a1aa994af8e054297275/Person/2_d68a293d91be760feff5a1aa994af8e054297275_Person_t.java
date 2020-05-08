 package neo4j.sandbox.model;
 
 import java.util.Set;
 
 import org.neo4j.graphdb.Direction;
 import org.springframework.data.neo4j.annotation.Fetch;
 import org.springframework.data.neo4j.annotation.GraphId;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.annotation.Query;
 import org.springframework.data.neo4j.annotation.RelatedTo;
 
 import com.google.common.collect.Iterables;
 
 @NodeEntity
 public class Person {
 
     @GraphId
     private Long id;
 
     @Fetch @RelatedTo(type="updated_with", direction=Direction.OUTGOING)
     private Set<Status> statusHistory;
     
     @Fetch @Query(value = "start person=node({self}) match (person)-[:updated_with]->(status) return status order by status.created limit 1")
     private Iterable<Status> current;
     
     public Status updateStatus(Status statusUpdate) {
         statusHistory.add(statusUpdate);
         return statusUpdate;
     }
 
     public Long getId() {
         return id;
     }
     
     public Status currentStatus() {
        return Iterables.getOnlyElement(current);
     }
     
 }
