 package couch4ben;
 
 import org.databene.benerator.test.BeneratorIntegrationTest;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.*;
 
 public class Couch4BenIntegrationTest extends BeneratorIntegrationTest{
 
     private static final String TEST_ENVIRONMENT = "couchdbtest";
 
     @Before
     @After
     public void clearCollection() {
         CouchDB db = CouchDBTestUtil.createAndClearCouchClient(TEST_ENVIRONMENT, dataModel);
        context.setSetting("db", db);
     }
 
     @Test
     public void testFixedSubGenerationCount() {
         // create 3 persons
         parseAndExecute(
                 "<setup>" +
                         "	<import platforms='couchdb' />" +
                         "	<couchdb database='db' environment='" + TEST_ENVIRONMENT + "' />" +
                         "	<generate type='mit_user' count='3' consumer='db'>" +
                         "		<attribute name='name' type='string' />" +
                         "		<attribute name='age' type='int' min='18' max='78' />" +
                         "		<part name='addresses' container='list' count='2'>" +
                         "			<attribute name='street' pattern='[A-Z][a-z]{4} Street'/>" +
                         "			<attribute name='houseNo' type='int' min='2' max='9' />" +
                         "		</part>" +
                         "	</generate>" +
                         "</setup>");
 
         // verify results
        CouchDB db = (CouchDB) context.getSetting("db");
         verifyPersonConstraints(db, 3, null, false, 2, 2);
     }
 
 
     @Test
     public void testVariableSubGenerationCount() {
         // create 100 persons
         parseAndExecute(
                 "<setup>" +
                         "	<import platforms='couchdb' />" +
                         "	<couchdb database='db' environment='" + TEST_ENVIRONMENT + "' />" +
                         "	<generate type='mit_user' count='100' consumer='db'>" +
                         "		<attribute name='name' type='string' />" +
                         "		<attribute name='age' type='int' min='18' max='78' />" +
                         "		<part name='addresses' container='list' minCount='1' maxCount='3'>" +
                         "			<attribute name='street' pattern='[A-Z][a-z]{4} Street'/>" +
                         "			<attribute name='houseNo' type='int' min='2' max='9' />" +
                         "		</part>" +
                         "	</generate>" +
                         "</setup>");
 
         // verify results
        CouchDB db = (CouchDB) context.getSetting("db");
         verifyPersonConstraints(db, 100, null, false, 1, 3);
     }
 
     @Test
     @Ignore("not implemented")
     public void testPlainUpdate() {
         CouchDB db = CouchDBUtil.createCouchDBForEnvironment(TEST_ENVIRONMENT, "db", context.getDataModel());
        context.setSetting("db", db);
         createPersons(db);
         // iterate persons and set each persons age to 33
         parseAndExecute(
                 "<setup>" +
                         "	<iterate type='mit_user' source='db' consumer='db.updater(),ConsoleExporter'>" +
                         "		<attribute name='age' constant='33' />" +
                         "	</iterate>" +
                         "</setup>");
         verifyPersonConstraints(db, 3, 33, true, 2, 2);
     }
 
     @Test
     @Ignore("not implemented")
     public void testSubUpdate() {
         CouchDB db = CouchDBUtil.createCouchDBForEnvironment(TEST_ENVIRONMENT, "db", context.getDataModel());
        context.setSetting("db", db);
         createPersons(db);
         // iterate persons and set each persons age to 33
         parseAndExecute(
                 "<setup>" +
                         "	<iterate type='mit_user' source='db' consumer='db.updater(),ConsoleExporter'>" +
                         "		<part name='addresses' source='mit_user' container='list'>" +
                         "			<attribute name='houseNo' constant='123' />" +
                         "		</part>" +
                         "	</iterate>" +
                         "</setup>");
         verifyPersonConstraints(db, 3, null, true, 2, 2);
     }
 
     private void createPersons(CouchDB db) {
         db.store(createPerson("Alice"));
         db.store(createPerson("Bob"));
         db.store(createPerson("Charly"));
     }
 
     private DBObject createPerson(String name) {
         DBObject person = new DBObject();
         person.put("name", name);
         person.put("age", 23);
         List<DBObject> addresses = new ArrayList<DBObject>();
         addresses.add(createAddress(name.charAt(0) + "Main Street", 13));
         addresses.add(createAddress(name.charAt(0) + "2nd Street", 14));
         person.put("addresses", addresses);
         return person;
     }
 
     private DBObject createAddress(String streetName, int houseNo) {
         DBObject address = new DBObject();
         address.put("street", streetName);
         address.put("houseNo", houseNo);
         return address;
     }
 
 
     private void verifyPersonConstraints(CouchDB db, long personCount, Integer expectedAge, boolean streetByName, int minAddressCount, int maxAddressCount) {
 
         List<DBObject> allDocuments = db.getAllDocuments();
 
         assertEquals(personCount, allDocuments.size());
         HashSet<Integer> usedCounts = new HashSet<Integer>();
         for(DBObject doc : allDocuments){
             String name = (String) doc.get("name");
             assertNotNull(name);
             Object age = doc.get("age");
             assertNotNull(age);
             assertTrue(age instanceof Integer);
             if (expectedAge != null)
                 assertEquals(expectedAge, age);
             List<Map<String, Object>> addresses = (List<Map<String, Object>>) doc.get("addresses");
             assertNotNull(addresses);
             int addressCount = addresses.size();
             assertTrue(addressCount >= minAddressCount);
             assertTrue(addressCount <= maxAddressCount);
             usedCounts.add(addressCount);
             for (int i = 0; i < addressCount; i++) {
                 String street = (String) addresses.get(i).get("street");
                 assertTrue(street.endsWith(" Street"));
                 if (streetByName)
                     assertEquals(street.charAt(0), name.charAt(0));
             }
             System.out.println("OK: " + doc);
         }
         if (minAddressCount < maxAddressCount)
             assertTrue("Expected more than one address count, but found " + usedCounts, usedCounts.size() > 1);
     }
 }
