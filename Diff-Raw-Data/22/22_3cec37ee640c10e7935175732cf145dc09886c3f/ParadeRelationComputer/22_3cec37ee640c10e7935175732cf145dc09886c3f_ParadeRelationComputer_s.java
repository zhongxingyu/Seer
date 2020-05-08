 package org.makumba.parade.aether;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.makumba.Pointer;
 import org.makumba.Transaction;
 import org.makumba.aether.RelationComputationException;
 import org.makumba.aether.RelationComputer;
 import org.makumba.db.hibernate.HibernateTransactionProvider;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.tools.ParadeLogger;
 import org.makumba.providers.TransactionProvider;
 
 /**
  * This relation computer populates the relation table with ownership relations between users and rows
  * 
  * TODO once this will be available in parade, also add the usage relations "uses" for trainees setting their mentors
  * row or so.
  * 
  * @author Manuel Gay
  * 
  */
 public class ParadeRelationComputer implements RelationComputer {
 
     public static final String PARADE_RELATION_TABLE = "org.makumba.devel.relations.Relation";
 
     public static final String PARADE_DATABASE_NAME = ParadeProperties.getParadeProperty("aether.databaseName");
 
     private Logger logger = ParadeLogger.getParadeLogger(ParadeRelationComputer.class.getName());
 
     private TransactionProvider tp;
 
     public void computeRelations() throws RelationComputationException {
 
         logger.fine("Computing all relations of " + getName());
 
         Transaction t = tp.getConnectionTo(PARADE_DATABASE_NAME);
 
         computeRowUserRelations(t);
 
         // trainee-mentor relations
         // we populate the relations table with those relations
 
         // first we remove all the previous relations
 
         Vector<Dictionary<String, Object>> old = t
                 .executeQuery(
                         "SELECT rel.id AS rel FROM org.makumba.devel.relations.Relation rel WHERE rel.type = 'traineeOf'",
                         null);
         for (Dictionary<String, Object> dictionary : old) {
             t.delete((Pointer) dictionary.get("rel"));
         }
 
         Vector<Dictionary<String, Object>> users = t.executeQuery(
                 "SELECT u.login as trainee, u.mentor.login as mentor FROM User u WHERE u.mentor != null", null);
         logger.info("Found " + users.size() + " trainee to mentor relations");
 
         for (Dictionary<String, Object> dic : users) {
             Dictionary<String, Object> rel = buildTraineeToMentorRelation(dic);
             t.insert(PARADE_RELATION_TABLE, rel);
         }
 
         t.close();
 
         logger.fine("Finished computing all relations of " + getName());
 
     }
 
     private void computeRowUserRelations(Transaction t) {
         // row-user relations
         // we populate the relations table with those relations
 
         // first we remove all the previous relations
         Vector<Dictionary<String, Object>> old = t.executeQuery(
                 "SELECT rel.id AS rel FROM org.makumba.devel.relations.Relation rel WHERE rel.type = 'owns'", null);
         for (Dictionary<String, Object> dictionary : old) {
             t.delete((Pointer) dictionary.get("rel"));
         }
 
         Vector<Dictionary<String, Object>> res = t.executeQuery(
                 "SELECT r.rowname AS row, r.user.login AS user FROM Row r WHERE r.user != null", null);
         logger.info("Found " + res.size() + " user to row relations");
         for (Dictionary<String, Object> d : res) {
             Dictionary<String, Object> rel = buildUserRowRelation(d);
             t.insert(PARADE_RELATION_TABLE, rel);
         }
     }
 
     private Dictionary<String, Object> buildUserRowRelation(Dictionary<String, Object> d) {
         Dictionary<String, Object> rel = new Hashtable<String, Object>();
         rel.put("fromURL", "user://" + d.get("user"));
         rel.put("toURL", "row://" + d.get("row"));
         rel.put("type", "owns");
         return rel;
     }
 
     private Dictionary<String, Object> buildTraineeToMentorRelation(Dictionary<String, Object> d) {
         Dictionary<String, Object> rel = new Hashtable<String, Object>();
         rel.put("fromURL", "user://" + d.get("trainee"));
         rel.put("toURL", "user://" + d.get("mentor"));
         rel.put("type", "traineeOf");
         return rel;
     }
 
     public String getName() {
         return "ParaDe relation computer";
     }
 
     public void updateRelation(String objectURL) throws RelationComputationException {
 
         logger.fine("Updating relation of object " + objectURL);
 
         if (objectURL.startsWith("user://")) {
             String userLogin = objectURL.substring("user://".length());
 
             Transaction t = tp.getConnectionTo(PARADE_DATABASE_NAME);
 
             Vector<Dictionary<String, Object>> res = t
                     .executeQuery(
                             "SELECT rel.id AS rel FROM org.makumba.devel.relations.Relation rel WHERE rel.fromURL = $1 AND rel.type = 'owns'",
                             new Object[] { objectURL });
 
             for (Dictionary<String, Object> dictionary : res) {
                 Pointer relPtr = (Pointer) dictionary.get("rel");
 
                 logger.fine("Deleting old relation " + relPtr.getUid());
 
                 // delete old record
                 t.delete(relPtr);
 
                 // generate new record
                 // we look for all the rows owned by the user to update
                 Vector<Dictionary<String, Object>> res1 = t.executeQuery(
                         "SELECT r.rowname AS row, r.user.login AS user FROM Row r WHERE r.user.login = $1",
                         new Object[] { userLogin });
                 for (Dictionary<String, Object> d : res1) {
                     Dictionary<String, Object> rel = buildUserRowRelation(d);
                     logger.fine("Generating new relation " + rel.get("fromURL") + " --(" + rel.get("type") + ")--> "
                             + rel.get("toURL"));
                     t.insert(PARADE_RELATION_TABLE, rel);
                 }
             }
         }
     }
 
     public ParadeRelationComputer() {
        tp = new TransactionProvider(new HibernateTransactionProvider());
     }
 
     public void deleteRelation(String objectURL) throws RelationComputationException {
         // TODO Auto-generated method stub
 
     }
 
 }
