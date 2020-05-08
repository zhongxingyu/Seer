 package org.yajug.users.persistence.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.yajug.users.domain.Member;
 import org.yajug.users.domain.utils.MappingHelper;
 import org.yajug.users.persistence.MongoDao;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 /**
  * Manage members data from/to the Mongo collection
  * 
  * @author Bertrand Chevrier <bertrand.chevrier@yajug.org>
  */
 @Singleton
 public class MemberMongoDao extends MongoDao{
 	
 	private final static String COLLECTION_NAME = "members";
 	
 	@Inject private MappingHelper mappingHelper;
 	
 	/**
 	 * Get the members collection (ie. db.members in mongo)
 	 * @return the collection
 	 */
 	private DBCollection members(){
 		return getCollection(COLLECTION_NAME);
 	}
 
 	public List<Member> getAll(){
 		List<Member> members = new ArrayList<>();
 		
 		DBCursor cursor = members().find();
 		try {
             while(cursor.hasNext()) {
             	Member member = map(Member.class, (BasicDBObject)cursor.next());
                 if(member != null){
                 	members.add(member);
                 }
             }
         } finally {
             cursor.close();
         }
 		return members;
 	}
 	
 	public List<Member> getAllIn(Set<Long> keys){
 		List<Member> members = new ArrayList<>();
 		if(keys != null && keys.size() > 0){
 			
 			BasicDBList docKeys = new BasicDBList();
 			docKeys.addAll(keys);
 			DBObject query = new BasicDBObject("key", new BasicDBObject("$in", docKeys));
 			
 			
 			DBCursor cursor = members().find(query);
 			try {
 	            while(cursor.hasNext()) {
 	            	Member member = map(Member.class, (BasicDBObject)cursor.next());
 	                if(member != null){
 	                	members.add(member);
 	                }
 	            }
 	        } finally {
 	            cursor.close();
 	        }
 		}
 		return members;
 	}
 	
 	public Member getOne(long key){
 		Member member = null;
 		if(key > 0){
 			DBCursor cursor = members().find(new BasicDBObject("key", key)).limit(1);
 			try {
 	            while(cursor.hasNext()) {
 	            	member = map(Member.class, (BasicDBObject)cursor.next());
 	            }
 	        } finally {
 	            cursor.close();
 	        }
 		}
 		return member;
 	}
 	
 	public List<Member> search(String expression){
 		List<Member> members = new ArrayList<>();
 		
		BasicDBObject searchExpression = new BasicDBObject("$regex", expression);
 		BasicDBList search = new BasicDBList();
 		search.add(new BasicDBObject("firstName", searchExpression));
 		search.add(new BasicDBObject("lastName", searchExpression));
 		search.add(new BasicDBObject("email", searchExpression));
 		search.add(new BasicDBObject("company", searchExpression));
 		
 		//create a mongo search query document
 		BasicDBObject query = new BasicDBObject("$or", search);
 		DBCursor cursor = members().find(query);
 		try {
             while(cursor.hasNext()) {
             	Member member = map(Member.class, (BasicDBObject)cursor.next());
                 if(member != null){
                 	members.add(member);
                 }
             }
         } finally {
             cursor.close();
         }
 		return members;
 	}
 	
 	public boolean insert(Member member){
 		boolean saved = false;
 		if(member != null){
 			//get next key
 			if(member.getKey() <= 0){
 				member.setKey(getNextKey(COLLECTION_NAME));
 			}
 			BasicDBObject doc =  new BasicDBObject("key", member.getKey())
                     .append("firstName", member.getFirstName())
                     .append("lastName",  member.getLastName())
                     .append("company",  member.getCompany())
                     .append("email", member.getEmail());
 			if(member.getRoles() != null){
 				doc.append("roles", mappingHelper.enumsToStrings(member.getRoles()));
 			}
 			saved = handleWriteResult(
 						members().insert(doc)
 					);
 		}
 		
 		return saved;
 	}
 	
 	public boolean update(Member member){
 		boolean saved = false;
 		if(member != null){
 			//get next key
 			if(member.getKey() > 0){
 				BasicDBObject query =  new BasicDBObject("key", member.getKey());
 				BasicDBObject doc =  
 					new BasicDBObject("firstName", member.getFirstName())
 		                    .append("lastName",  member.getLastName())
 		                    .append("company",  member.getCompany())
 		                    .append("email", member.getEmail());
 				if(member.getRoles() != null){
 					doc.append("roles", mappingHelper.enumsToStrings(member.getRoles()));
 				}
 				saved = handleWriteResult(
 						members().update(query, doc)
 					);
 			}
 		}
 		return saved;
 	}
 	
 	public boolean remove(Member member){
 		boolean removed = false;
 		if(member != null && member.getKey() > 0){
 			removed = handleWriteResult(
 						members().remove(new BasicDBObject("key", member.getKey()))
 					);
 		}
 		return removed;
 	}
 }
