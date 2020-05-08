 package esl.cuenet.source.accessors;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.ObjectProperty;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.util.PrintUtil;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import esl.cuenet.model.Constants;
 import esl.cuenet.query.IResultIterator;
 import esl.cuenet.query.IResultSet;
 import esl.cuenet.query.ResultIterator;
 import esl.cuenet.query.drivers.mongodb.MongoDB;
 import esl.cuenet.source.AccesorInitializationException;
 import esl.cuenet.source.Attribute;
 import esl.cuenet.source.IAccessor;
 import esl.cuenet.source.SourceQueryException;
 import esl.datastructures.Location;
 import esl.datastructures.TimeInterval;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class FacebookRelationAccessor extends MongoDB implements IAccessor {
 
     private Logger logger = Logger.getLogger(FacebookRelationAccessor.class);
     private Attribute[] attributes = null;
     private boolean[] setFlags = new boolean[1];
     private String id;
     private OntModel model = null;
 
     public FacebookRelationAccessor() {
         super("test");
     }
 
     public FacebookRelationAccessor(OntModel model) {
         this();
         this.model = model;
     }
 
     @Override
     public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
         this.attributes = attributes;
     }
 
     @Override
     public void start() {
         for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
     }
 
     @Override
     public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
         throw new AccesorInitializationException("Incorrect Assignment: No time interval attributes in "
                 + YahooPlaceFinderAPI.class.getName());
 
     }
 
     @Override
     public void associateLocation(Attribute attribute, Location timeInterval) throws AccesorInitializationException {
         throw new AccesorInitializationException("Incorrect Assignment: No location attributes in "
                 + YahooPlaceFinderAPI.class.getName());
 
     }
 
     @Override
     public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
         if (attribute.compareTo(attributes[0])==0) {       /* name */
             setFlags[0] = true;
             this.id = "" + value;
         }
         else
             throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                     + FacebookUserAccessor.class.getName());
     }
 
     @Override
     public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
         if (attribute.compareTo(attributes[0])==0) {       /* name */
             setFlags[0] = true;
             this.id = "" + getIDFromName(value);
             logger.info("Associated ID from name: " + id);
         }
         else
             throw new AccesorInitializationException("String value being initialized for wrong attribute "
                     + FacebookUserAccessor.class.getName());
     }
 
     @Override
     public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
         throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                 + FacebookUserAccessor.class.getName());
     }
 
     @Override
     public IResultSet executeQuery() throws SourceQueryException {
         DBReader reader = this.startReader("fb_relationships");
         ResultSetImpl resultSet = new ResultSetImpl("Facebook Relation Accessor for : " + id);
 
         BasicDBList clauses = new BasicDBList();
 
         if ( !setFlags[0] )  {
             logger.info("Empty");
             return new ResultSetImpl("Facebook Relation Accessor");
         }
 
         BasicDBObject inputPersonPredicates = new BasicDBObject("id", id);
         BasicDBObject inputPersonDBObject = getPerson(inputPersonPredicates);

        if (inputPersonDBObject == null) {
            logger.info("Could not find person with id = " + id + " in database.");
            return resultSet;
        }

         Individual inputPersonIndividual = Utils.createPersonFromFacebookRecord(inputPersonDBObject, model);
 
         ObjectProperty knowsProperty = model.getObjectProperty(
                 Constants.CuenetNamespace + "knows");
 
         clauses.add(new BasicDBObject("id", id));
         clauses.add(new BasicDBObject("relation", id));
         BasicDBObject query = new BasicDBObject("$or", clauses);
 
         reader.query(query);
 
         String tmpID, tmpName;
         while (reader.hasNext()) {
             DBObject rel = reader.next();
             BasicDBObject normalizedRelation = new BasicDBObject(rel.toMap());
 
             /* normalize if the first entry is not the requested ID*/
             if ( ((String)normalizedRelation.get("id")).compareTo(id) != 0 ) {
                 tmpID = (String) normalizedRelation.get("relation");
                 tmpName = getNameFromFB(normalizedRelation.getString("relation"));
 
                 normalizedRelation.put("relation", normalizedRelation.get("id"));
                 normalizedRelation.put("relation_name", getNameFromFB((String)normalizedRelation.get("id")));
                 normalizedRelation.put("id",  tmpID);
                 normalizedRelation.put("name",  tmpName);
             }
 
             //result.add(normalizedRelation);
 
             BasicDBObject friend = getPerson(new BasicDBObject("id", normalizedRelation.getString("relation")));
             Individual friendIndividual = Utils.createPersonFromFacebookRecord(friend, model);
             inputPersonIndividual.addProperty(knowsProperty, friendIndividual);
 
             List<Individual> result = new ArrayList<Individual>();
             result.add(inputPersonIndividual);
             result.add(friendIndividual);
             resultSet.addResult(result);
         }
 
         return resultSet;
     }
 
     private String getIDFromName(String name) {
         BasicDBObject query = new BasicDBObject("name", name);
         BasicDBObject person = getPerson(query);
         if (person == null) return null;
 
         if (person.containsField("id")) return  person.getString("id");
         return null;
     }
 
     private BasicDBObject getPerson(BasicDBObject inputPredicates) {
         DBReader reader = this.startReader("fb_users");
         reader.query(inputPredicates);
         DBObject o = null;
         while (reader.hasNext()) {
             o = reader.next();
         }
         return (BasicDBObject) o;
     }
 
     private String getNameFromFB(String userID) {
         BasicDBObject query = new BasicDBObject("id", userID);
         BasicDBObject p = getPerson(query);
 
         if (p.containsField("name")) return p.getString("name");
         return null;
     }
 
     private IResultSet convertResults(BasicDBObject result) {
 
         ResultSetImpl resultSet = new ResultSetImpl("Facebook Relation Accessor");
 
         Individual personIndividual = Utils.createPersonFromFacebookRecord(result, model);
 
         List<Individual> re = new ArrayList<Individual>();
         re.add(personIndividual);
         resultSet.addResult(re);
 
         return resultSet;
     }
 
     public IResultSet executeQuery (long id) throws SourceQueryException {
         this.id = "" + id;
         setFlags[0] = true;
         return executeQuery();
     }
 
     private class ResultSetImpl implements IResultSet {
         private String result;
         private ResultIterator resultIterator = new ResultIterator(model);
 
         public ResultSetImpl (String result) {this.result = result;}
 
         public void addResult(List<Individual> individuals) {
             this.resultIterator.add(individuals);
         }
 
         @Override
         public String printResults() {
             return result;
         }
 
         @Override
         public IResultIterator iterator() {
             return resultIterator;
         }
     }
 
 }
