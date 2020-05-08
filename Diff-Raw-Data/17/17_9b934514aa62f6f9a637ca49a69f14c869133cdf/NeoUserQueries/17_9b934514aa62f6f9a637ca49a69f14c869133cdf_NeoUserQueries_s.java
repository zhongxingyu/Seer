 package org.cotrix.neo.repository;
 
 import static java.lang.String.*;
 import static org.cotrix.action.ResourceType.*;
 import static org.cotrix.common.Constants.*;
 import static org.cotrix.neo.domain.Constants.*;
 import static org.cotrix.neo.domain.Constants.NodeType.*;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.annotation.Priority;
 import javax.enterprise.inject.Alternative;
 import javax.inject.Singleton;
 
 import org.cotrix.domain.common.IteratorAdapter;
 import org.cotrix.domain.user.Role;
 import org.cotrix.domain.user.User;
 import org.cotrix.neo.domain.NeoUser;
 import org.cotrix.neo.domain.utils.NeoNodeIterator;
 import org.cotrix.repository.Criterion;
 import org.cotrix.repository.MultiQuery;
 import org.cotrix.repository.Query;
 import org.cotrix.repository.spi.UserQueryFactory;
 import org.neo4j.cypher.javacompat.ExecutionResult;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ResourceIterator;
 
 @Singleton
 @Alternative
 @Priority(RUNTIME)
 public class NeoUserQueries extends NeoQueries implements UserQueryFactory {
 
 	
 	// helpers
 	
 
 	Iterator<User> users(ResourceIterator<Node> it) {
 		return new IteratorAdapter<>(new NeoNodeIterator<>(it, NeoUser.factory));
 	}
 
 	@Override
 	public NeoMultiQuery<User, User> allUsers() {
 		
 		return new NeoMultiQuery<User,User>(engine) {
 			
 			{
 				match(format("(%1$s:%2$s)",$node,USER.name()));
 				rtrn(format("%1$s as %2$s",$node,$result));
 			}
 
 			@Override
 			public Iterator<User> iterator() {
 
 				ExecutionResult result = executeNeo();
 
 				ResourceIterator<Node> it = result.columnAs($result);
 
 				return users(it);
 			}
 		};
 	}
 
 	@Override
 	public Query<User, User> userByName(final String name) {
 		
 		return new Query.Private<User,User>() {
 
 			@Override
 			public User execute() {
 
 				String query = format("MATCH (%1$s:%2$s) WHERE %1$s.name = '%3$s'  RETURN %1$s as %4$s", 
 						  $node, 
 						  USER.name(),
 						  name,
 						  $result);
 				
 				
 				ExecutionResult result = engine.execute(query);
 
 				try (
 						ResourceIterator<Node> it = result.columnAs($result)
 				) {
 					
 					return it.hasNext()? new NeoUser(it.next()).entity():null;
 				}
 			}
 
 		};
 	}
 
 	@Override
 	public MultiQuery<User, User> teamFor(final String codelistId) {
 		
 		return new NeoMultiQuery<User,User>(engine) {
 
 			@Override
 			public Iterator<User> iterator() {
 
 				Collection<User> matches = new HashSet<User>();
 				
				for (User user : allUsers().execute())
 					if (!user.fingerprint().specificRolesOver(codelistId,codelists).isEmpty())
 						matches.add(user);
 				
 				return matches.iterator();
 			}
 
 		};
 
 	}
 
 	@Override
 	public Criterion<User> byName() {
 		
 		return new NeoCriterion<User>() {
 			
 			@Override
 			protected String process(NeoMultiQuery<?, ?> query) {
 				return format("%1$s.%2$s",$node,name_prop);
 			}
 		};
 	}
 	
 	@Override
 	public Criterion<User> byFullName() {
 		
 		return new NeoCriterion<User>() {
 			
 			@Override
 			protected String process(NeoMultiQuery<?, ?> query) {
 				return format("%1$s.%2$s",$node,fullname_prop);
 			}
 		};
 	}
 	
 
 	@Override
 	public MultiQuery<User, User> usersWithRole(final Role role) {
 		
 		return new NeoMultiQuery<User,User>(engine) {
 
 			@Override
 			public Iterator<User> iterator() {
 
 				Collection<User> matches = new HashSet<User>();
 				
 				for (User user : allUsers().execute())
 					if (user.is(role))
 						matches.add(user);
 				
 				return matches.iterator();
 			}
 
 		};
 	}
 
 }
