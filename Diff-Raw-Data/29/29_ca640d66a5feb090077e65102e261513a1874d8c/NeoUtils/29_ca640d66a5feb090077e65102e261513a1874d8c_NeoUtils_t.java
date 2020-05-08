 package org.cotrix.neo;
 
 import static org.cotrix.common.Utils.*;
import static org.cotrix.neo.domain.Constants.*;
import static org.cotrix.neo.domain.Constants.Relations.*;
 import static org.neo4j.graphdb.Direction.*;
 
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 import com.thoughtworks.xstream.XStream;
 
 public class NeoUtils {
 
 	private static XStream stream = new XStream();
 	
 	
 	public static void removeEntityNode(Node n) {
 		
 		notNull("node",n);
 		
 		if (n.hasRelationship(INCOMING))
			throw new IllegalStateException("entity "+n.getProperty(id_prop)+":"+n.getLabels()+" cannot be removed: there are other entities that link to it, directly or indirectly");
 		
 		removeNode(n);
 					
 	}
 	
 	public static void removeNode(Node n) {
 		
 		notNull("node",n);
 		
 		for (Relationship r : n.getRelationships(OUTGOING)) {
 			
 			//we do not remove links
			if (!r.isType(LINK) && r.isType(INSTANCEOF)) {
 				removeNode(r.getEndNode());
 				r.delete();
 			}
 		}
 		
 		n.delete();
 			
 	}
 	
 	public static XStream binder() {
 		return stream; 
 	}
 }
