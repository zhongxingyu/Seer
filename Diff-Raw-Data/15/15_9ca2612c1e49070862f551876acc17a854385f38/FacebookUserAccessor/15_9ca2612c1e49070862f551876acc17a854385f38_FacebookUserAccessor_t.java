 package esl.cuenet.source.accessors;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.mongodb.BasicDBObject;
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
 import java.util.regex.Pattern;
 
 public class FacebookUserAccessor extends MongoDB implements IAccessor {
 
     private Logger logger = Logger.getLogger(FacebookUserAccessor.class);
     private Attribute[] attributes = null;
     private boolean[] setFlags = new boolean[6];
     private String name, locationName;
     private String email;
     private String workplaceName;
     private String birthday;
     private OntModel model = null;
 
     public FacebookUserAccessor() {
         super("test");
     }
 
     public FacebookUserAccessor(OntModel model) {
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
         throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                 + FacebookUserAccessor.class.getName());
     }
 
     @Override
     public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
         if (attribute.compareTo(attributes[1])==0) {       /* name */
             setFlags[1] = true;
             this.name = value;
         } else if (attribute.compareTo(attributes[2])==0) {  /* birthday */
             setFlags[2] = true;
             this.birthday = value;
         } else if (attribute.compareTo(attributes[3])==0) {  /* location */
             setFlags[3] = true;
             this.locationName = value;
         } else if (attribute.compareTo(attributes[4])==0) {  /* work */
             setFlags[4] = true;
             this.workplaceName = value;
         } else if (attribute.compareTo(attributes[5])==0) {  /* email */
             setFlags[5] = true;
             this.email = value;
         } else {
             throw new AccesorInitializationException("String value being initialized for wrong attribute "
                     + FacebookUserAccessor.class.getName());
         }
     }
 
     @Override
     public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
         throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                 + FacebookUserAccessor.class.getName());
     }
 
     @Override
     public IResultSet executeQuery() throws SourceQueryException {
         DBReader reader = this.startReader("fb_users");
         BasicDBObject query = new BasicDBObject();
         if (setFlags[1]) query.append("name", name);
         if (setFlags[2]) {
             Pattern bdayPattern = Pattern.compile("^" + birthday, Pattern.CASE_INSENSITIVE);
             query.append("birthday", birthday);
         }
 
         reader.query(query);
         BasicDBObject result = null;
         while (reader.hasNext()) {
             result = (BasicDBObject) reader.next();
             BasicDBObject r = new BasicDBObject("name", result.get("name"));
             r.put("birthday", result.get("birthday"));
             logger.info(r.toString());
         }
 
         return convertResults(result);
     }
 
     private IResultSet convertResults(BasicDBObject result) {
         ResultSetImpl resultSet = new ResultSetImpl("Facebook User Accessor");
        if (result == null) return resultSet;

         Individual personIndividual = Utils.createPersonFromFacebookRecord(result, model);
 
         List<Individual> re = new ArrayList<Individual>();
         re.add(personIndividual);
         resultSet.addResult(re);
 
         return resultSet;
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
     
     public IResultSet executeQuery (String name, String birthday) throws SourceQueryException {
         if (name != null) {
             this.name = name;
             setFlags[1] = true;
         }
         if (birthday != null) {
             this.birthday = birthday;
             setFlags[2] = true;
         }
         return executeQuery();
     }
 
 }
