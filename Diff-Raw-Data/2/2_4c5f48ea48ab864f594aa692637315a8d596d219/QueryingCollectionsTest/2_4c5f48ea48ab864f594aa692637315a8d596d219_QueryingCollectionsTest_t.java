 package me.arin.mwjt.mongoDriver;
 
 import com.mongodb.*;
 import me.arin.mwjt.SetupMongo;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.util.*;
 
 public final class QueryingCollectionsTest extends SetupMongo {
     private DBCollection collection;
     protected static DB db = null;
 
     @BeforeClass
     public static void beforeClass() throws Exception {
         SetupMongo.setUp();
         db = mongo.getDB(CollectionBasicsTest.DB_NAME);
     }
 
     @Before
     public void beforeMethod() {
         db.getCollection(CollectionBasicsTest.COLLECTION_NAME).drop();
         collection = db.createCollection(CollectionBasicsTest.COLLECTION_NAME, new BasicDBObject());
     }
 
     @Test
     public void getAllDocs() {
         final int num = 10;
         for (int i = 0; i < num; i++) {
             collection.insert(new BasicDBObject("name", "arin" + i));
         }
 
         // collection.find() gets em all
         final DBCursor dbCursor = collection.find();
         Assert.assertEquals(num, dbCursor.length());
 
         int i = 0;
         for (DBObject dbObject : dbCursor) {
             String name = "arin" + i++;
             Assert.assertTrue(name.equals(dbObject.get("name")));
         }
     }
 
     @Test
     public void introToDBCursor() {
         for (int i = 0; i < 10; i++) {
             collection.insert(new BasicDBObject("i", i));
         }
 
         // the queries return a DBCursor which is just an iterable object w/ some extra methods
         final DBCursor dbCursor = collection.find();
         Assert.assertTrue(dbCursor instanceof Iterable);
         Assert.assertTrue(dbCursor instanceof Iterator);
 
         // iterate over it and you'll bget back your DBObjects
         for (DBObject dbObject : dbCursor) {
             Assert.assertTrue(dbObject instanceof DBObject);
         }
 
         Assert.assertEquals("# of dbObjects in the cursor", 10, dbCursor.length());
         Assert.assertEquals("# of dbObjects matched the query (regardless of limit/offset)",
                 10,
                 dbCursor.count());
 
         // returns void so i cant assert anything meaningful but closes cursor on the server...
         dbCursor.close();
     }
 
     @Test
     public void getNumberOfDocsInACollection() {
         final int num = 10;
 
         for (int i = 0; i < num; i++) {
             collection.insert(new BasicDBObject("i", i));
         }
 
         // ask the collection for count....
         Assert.assertEquals(num, collection.count());
 
         // query, fetch everything & count (not a good idea)
         Assert.assertEquals(num, collection.find().length());
     }
 
     @Test
     public void limitNumDocs() {
         final int num = 10;
         for (int i = 0; i < num; i++) {
             collection.insert(new BasicDBObject("name", "arin" + i));
         }
 
         // lets limit to 4
         final int limit = 4;
         final DBCursor data = collection.find().limit(limit);
 
         // cursor only hold 4
         Assert.assertEquals(limit, data.length());
 
         // but it knows that there's 10 total if limit wasn't used...
         Assert.assertEquals(num, data.count());
     }
 
     @Test
     public void limitNumDocsButCheckTheTotalThatMatched() {
         final int num = 10;
         for (int i = 0; i < num; i++) {
             collection.insert(new BasicDBObject("name", "arin" + i));
         }
 
         // lets limit to 4
         final int limit = 4;
         final DBCursor data = collection.find().limit(limit);
         Assert.assertEquals(limit, data.length());
 
         // cursor can tell you the total # of matches when you use a limit
         Assert.assertEquals(num, data.count());
     }
 
     @Test
     public void getByOffset() {
         final int num = 10;
         for (int i = 0; i < num; i++) {
             collection.insert(new BasicDBObject("name", "arin" + i));
         }
 
         int offset = 1;
         // they call it skip
         final DBCursor data = collection.find().skip(offset);
         Assert.assertEquals("arin" + offset, data.iterator().next().get("name"));
     }
 
     @Test
     public void getFirstDocFromCollection() {
         for (int i = 0; i < 10; i++) {
             collection.insert(new BasicDBObject("name", "arin" + i));
         }
 
         // collection.findOne() gets the first doc
         final DBObject first = collection.findOne();
         Assert.assertTrue("arin0".equals(first.get("name")));
     }
 
     @Test
     public void restrictReturnedFields() {
         final BasicDBObject dbObject = new BasicDBObject("name", "arin").append("x", 1)
                                                                         .append("age", 32);
         collection.insert(dbObject);
 
         // only get back the "x" field ... _id is always returned
         DBObject doc = collection.findOne(new BasicDBObject(), new BasicDBObject("x", true));
         Assert.assertTrue(doc.containsField("x"));
         Assert.assertTrue(doc.containsField("_id"));
         Assert.assertFalse(doc.containsField("name"));
         Assert.assertFalse(doc.containsField("age"));
 
         // now we can get back all fields except for x
         doc = collection.findOne(new BasicDBObject(), new BasicDBObject("x", false));
         Assert.assertFalse(doc.containsField("x"));
         Assert.assertTrue(doc.containsField("_id"));
         Assert.assertTrue(doc.containsField("age"));
         Assert.assertTrue(doc.containsField("name"));
     }
 
     @Test
     public void querySingleFieldEquals() {
         // insert 10 docs w/ incrementing value for i
         for (int i = 0; i < 10; i++) {
             collection.insert(new BasicDBObject("i", i));
         }
 
         // add an extra doc where i == 5
         final int valueWeCareAbout = 5;
         collection.insert(new BasicDBObject("i", valueWeCareAbout));
 
         // now select stuff where i == 5
         final BasicDBObject query = new BasicDBObject("i", valueWeCareAbout);
         final DBCursor dbCursor = collection.find(query);
 
         // should get 2 results..
         Assert.assertEquals(2, dbCursor.length());
 
         // make sure i == 5 in both
         for (DBObject dbObject : dbCursor) {
             Assert.assertEquals(valueWeCareAbout, dbObject.get("i"));
         }
     }
 
     @Test
     public void querySingleFieldNotEqual() {
         // insert 10 docs w/ incrementing value for i
         for (int i = 0; i < 10; i++) {
             collection.insert(new BasicDBObject("i", i));
         }
 
         final int valueWeCareAbout = 5;
 
         // now select stuff where i != 5
         final BasicDBObject query = new BasicDBObject("i", new BasicDBObject("$ne", valueWeCareAbout));
         final DBCursor dbCursor = collection.find(query);
 
         // should get 9 results..
         Assert.assertEquals(9, dbCursor.length());
 
         // make sure i == 5 in both
         for (DBObject dbObject : dbCursor) {
             Assert.assertNotSame(valueWeCareAbout, dbObject.get("i"));
         }
     }
 
     @Test
     public void querySingleFieldEqualsAndShowItsCaseSensitive() {
         final String targetName = "ARIN";
         final String fieldName = "name";
 
         collection.insert(new BasicDBObject(fieldName, targetName));
 
         // add an extra doc where name == arin (all lowercase)
         final BasicDBObject query = new BasicDBObject(fieldName, targetName.toLowerCase());
         final DBCursor dbCursor = collection.find(query);
 
         // should get NO results
         Assert.assertEquals(0, dbCursor.length());
     }
 
     @Test
     public void queryMultipleFieldsViaAnd() {
         // insert some docs...
         final String targetName = "arin";
         final int targetI = 5;
 
         collection.insert(new BasicDBObject("i", targetI).append("name", targetName));
         collection.insert(new BasicDBObject("i", targetI).append("name", targetName));
         collection.insert(new BasicDBObject("i", 6).append("name", targetName));
         collection.insert(new BasicDBObject("i", 6).append("name", "bob"));
         collection.insert(new BasicDBObject("i", 6).append("name", "arin"));
         collection.insert(new BasicDBObject("i", targetI).append("name", "bob"));
 
         // now select stuff where i == 5 and name == arin
         // and is default for multiple criteria
         final BasicDBObject query = new BasicDBObject("i", targetI).append("name", targetName);
         final DBCursor dbCursor = collection.find(query);
 
         // should get 2 results..
         Assert.assertEquals(2, dbCursor.length());
 
         // make sure i == 5 and name == arin in both
         for (DBObject dbObject : dbCursor) {
             Assert.assertEquals(targetI, dbObject.get("i"));
             Assert.assertEquals(targetName, dbObject.get("name"));
         }
     }
 
     @Test
     public void queryMultipleFieldsViaOr() {
         final int targetI = 5;
         final String fieldI = "i";
 
         final String targetName = "arin";
         final String fieldName = "name";
 
         // we wanna find these 2: where i == 5 or name == "arin"
         collection.insert(new BasicDBObject(fieldI, targetI).append(fieldName, "not arin"));
         collection.insert(new BasicDBObject(fieldI, 666).append(fieldName, targetName));
 
         // add some garbage data
         collection.insert(new BasicDBObject(fieldI, 666));
         collection.insert(new BasicDBObject(fieldName, targetName.toUpperCase()));
         collection.insert(new BasicDBObject(fieldI, 888));
         collection.insert(new BasicDBObject(fieldI, 999));
 
         // lets construct the or
         // in json it would look like [{ i : 5 } , { name : 'arin' }]
         final List<BasicDBObject> or = new ArrayList<BasicDBObject>();
         or.add(new BasicDBObject(fieldI, targetI));
         or.add(new BasicDBObject(fieldName, targetName));
 
         final BasicDBObject query = new BasicDBObject("$or", or);
         final DBCursor cursor = collection.find(query);
 
         // we have 2 results - both total and returned
         Assert.assertEquals(2, cursor.count());
         Assert.assertEquals(2, cursor.length());
 
         // assert i == 5 || name.equals(arin)
         for (DBObject dbObject : cursor) {
             final Integer i = (Integer) dbObject.get(fieldI);
             final String name = (String) dbObject.get(fieldName);
 
             Assert.assertTrue(i == targetI || targetName.equals(name));
         }
     }
 
     @Test
     public void queryGreaterThan() {
         int total = 10;
         int targetInt = 5;
 
         for (int i = 0; i < total; i++) {
             collection.insert(new BasicDBObject("i", i));
         }
 
         BasicDBObject queryGreaterThan = new BasicDBObject("i", new BasicDBObject("$gt", targetInt));
         final DBCursor greaterThanCursor = collection.find(queryGreaterThan);
         for (DBObject dbObject : greaterThanCursor) {
             final Integer iValue = (Integer) dbObject.get("i");
             Assert.assertTrue(iValue > targetInt);
         }
 
         BasicDBObject queryGreaterThanEqual = new BasicDBObject("i",
                 new BasicDBObject("$gte", targetInt));
         final DBCursor greaterEqualThanCursor = collection.find(queryGreaterThanEqual);
         for (DBObject dbObject : greaterEqualThanCursor) {
             final Integer iValue = (Integer) dbObject.get("i");
             Assert.assertTrue(iValue >= targetInt);
         }
     }
 
     @Test
     public void queryLessThan() {
         int total = 10;
         int targetInt = 5;
 
         for (int i = 0; i < total; i++) {
             collection.insert(new BasicDBObject("i", i));
         }
 
         BasicDBObject queryLessThan = new BasicDBObject("i", new BasicDBObject("$lt", targetInt));
         final DBCursor lessThanCursor = collection.find(queryLessThan);
         for (DBObject dbObject : lessThanCursor) {
             final Integer iValue = (Integer) dbObject.get("i");
             Assert.assertTrue(iValue < targetInt);
         }
 
         BasicDBObject queryLessThanEqual = new BasicDBObject("i", new BasicDBObject("$lte", targetInt));
         final DBCursor lessThanEqualCursor = collection.find(queryLessThanEqual);
         for (DBObject dbObject : lessThanEqualCursor) {
             final Integer iValue = (Integer) dbObject.get("i");
             Assert.assertTrue(iValue <= targetInt);
         }
     }
 
     @Test
     public void queryValueInAListEquals() {
         // insert a bunch of docs who's field names contains an array of names
         collection.insert(new BasicDBObject("names", new String[]{"arin", "william", "deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"josh", "scorpion"}));
         collection.insert(new BasicDBObject("names", new String[]{"sub zero"}));
 
         // now lets get back all the docs where the names field contains "arin"
         BasicDBObject queryViaEquals = new BasicDBObject("names", "arin");
         DBCursor dbCursorViaEquals = collection.find(queryViaEquals);
         Assert.assertTrue(1 == dbCursorViaEquals.length());
 
         // now lets get back all the docs where the names field contains "deez"
         queryViaEquals = new BasicDBObject("names", "deez");
         dbCursorViaEquals = collection.find(queryViaEquals);
         Assert.assertTrue(2 == dbCursorViaEquals.length());
 
        // now search for some random string
         queryViaEquals = new BasicDBObject("names", "random ass string");
         dbCursorViaEquals = collection.find(queryViaEquals);
         Assert.assertTrue(0 == dbCursorViaEquals.length());
     }
 
     @Test
     public void queryListValueIsInASet() {
         // insert a bunch of docs who's field names contains an array of names
         collection.insert(new BasicDBObject("names", new String[]{"arin", "william", "deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"josh", "scorpion"}));
         collection.insert(new BasicDBObject("names", new String[]{"sub zero"}));
 
         final Set<String> findAnyOfThese = new HashSet<String>();
         findAnyOfThese.add("nuts");
         findAnyOfThese.add("josh");
 
         // now lets get back all the docs where the names field is in the target set
         BasicDBObject in = new BasicDBObject("$in", findAnyOfThese);
         BasicDBObject queryViaIn = new BasicDBObject("names", in);
 
         // we should get 3 docs... any doc with names.contains(nuts) || names.contains(josh)
         final DBCursor dbCursor = collection.find(queryViaIn);
         Assert.assertEquals(3, dbCursor.length());
 
         for (DBObject dbObject : dbCursor) {
             List<String> names = (List<String>) dbObject.get("names");
             Assert.assertTrue(names.contains("nuts") || names.contains("josh"));
         }
     }
 
     @Test
     public void queryExistenceOfAField() {
         collection.insert(new BasicDBObject("i", 666));
         collection.insert(new BasicDBObject("i", 666));
         collection.insert(new BasicDBObject("i", 666));
         collection.insert(new BasicDBObject("i", 666));
         collection.insert(new BasicDBObject("kill", 666));
         collection.insert(new BasicDBObject("kill", 666));
 
         // now get all docs that have a field named kill
         final BasicDBObject existsQuery = new BasicDBObject("kill", new BasicDBObject("$exists", true));
 
         DBCursor dbCursor = collection.find(existsQuery);
         Assert.assertEquals(2, dbCursor.length());
         for (DBObject dbObject : dbCursor) {
             Assert.assertTrue(dbObject.containsField("kill"));
         }
 
         // ok - lets get docs that DO NOT bave a kill field
         final BasicDBObject notExistsQuery = new BasicDBObject("kill",
                 new BasicDBObject("$exists", false));
 
         dbCursor = collection.find(notExistsQuery);
         Assert.assertEquals(4, dbCursor.length());
         for (DBObject dbObject : dbCursor) {
             Assert.assertFalse(dbObject.containsField("kill"));
         }
     }
 
     @Test
     public void queryListValueIsNotInASet() {
         // insert a bunch of docs who's field names contains an array of names
         collection.insert(new BasicDBObject("names", new String[]{"arin", "william", "deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"josh", "scorpion"}));
         collection.insert(new BasicDBObject("names", new String[]{"sub zero"}));
 
         final Set<String> notIN = new HashSet<String>();
         notIN.add("nuts");
 
         // now lets get back all the docs where the names field is in the target set
         BasicDBObject nin = new BasicDBObject("$nin", notIN);
         BasicDBObject qryNin = new BasicDBObject("names", nin);
 
         // we should get 2 docs... any doc with !names.contains(nuts)
         final DBCursor dbCursor = collection.find(qryNin);
         Assert.assertEquals(2, dbCursor.length());
 
         for (DBObject dbObject : dbCursor) {
             List<String> names = (List<String>) dbObject.get("names");
             Assert.assertFalse(names.contains("nuts"));
         }
     }
 
     @Test
     public void findDocsWithAListOfCertainSize() {
         collection.insert(new BasicDBObject("names", new String[]{"arin", "william", "deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"deez", "nuts"}));
         collection.insert(new BasicDBObject("names", new String[]{"josh", "scorpion"}));
         collection.insert(new BasicDBObject("names", new String[]{"sub zero"}));
 
         int size = 2;
         DBObject sizeQry = new BasicDBObject("names", new BasicDBObject("$size", size));
         final DBCursor dbCursor = collection.find(sizeQry);
 
         // wanna find any doc where names.length == 2
         Assert.assertEquals(2, dbCursor.length());
 
         for (DBObject dbObject : dbCursor) {
             final List names = (List) dbObject.get("names");
             Assert.assertEquals(size, names.size());
         }
     }
 
     @Test
     public void matchAllEntriesInEmbededList() {
         final String[] allOfThese = {"deez", "nuts"};
 
         collection.insert(new BasicDBObject("names", allOfThese));
         collection.insert(new BasicDBObject("names", new String[]{"deez"}));
         collection.insert(new BasicDBObject("names", new String[]{"nuts"}));
 
         BasicDBObject allQry = new BasicDBObject("names", new BasicDBObject("$all", allOfThese));
         final DBCursor dbCursor = collection.find(allQry);
 
         Assert.assertEquals(1, dbCursor.length());
         final List names = (List) dbCursor.iterator().next().get("names");
         Assert.assertArrayEquals(allOfThese, names.toArray(new String[]{}));
     }
 
     /**
      * type codes listed at: http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24type
      * <p/>
      * codes used in this test
      * String == 2
      * 32-bit int == 16
      */
     @Test
     public void queryFieldIsCertainType() {
         collection.insert(new BasicDBObject("i", "i is a string"));
         collection.insert(new BasicDBObject("i", 666));
 
         final BasicDBObject qryIIsString = new BasicDBObject("i", new BasicDBObject("$type", 2));
         final DBCursor stringCursor = collection.find(qryIIsString);
         Assert.assertTrue(stringCursor.length() == 1);
         Assert.assertTrue(stringCursor.iterator().next().get("i") instanceof String);
 
         final BasicDBObject qryIIsIntegre = new BasicDBObject("i", new BasicDBObject("$type", 16));
         final DBCursor integerCursor = collection.find(qryIIsIntegre);
         Assert.assertTrue(integerCursor.length() == 1);
         Assert.assertTrue(integerCursor.iterator().next().get("i") instanceof Integer);
     }
 
     @Test
     public void queryStringsByRegex() {
         final ArrayList<String> aNames = new ArrayList<String>();
         aNames.add("arin");
         aNames.add("alice");
         aNames.add("Alex");
 
         collection.insert(new BasicDBObject("name", aNames.get(0)));
         collection.insert(new BasicDBObject("name", aNames.get(1)));
         collection.insert(new BasicDBObject("name", aNames.get(2)));
         collection.insert(new BasicDBObject("name", "bob"));
         collection.insert(new BasicDBObject("name", "lucifer"));
 
         // find stuff that starts with an 'a' - arin & alice
         BasicDBObject startsWithAQuery = new BasicDBObject("name", new BasicDBObject("$regex", "^a"));
         DBCursor aResults = collection.find(startsWithAQuery);
         Assert.assertEquals(2, aResults.length());
         for (DBObject aResult : aResults) {
             final String name = (String) aResult.get("name");
             Assert.assertTrue(name.equals("arin") || name.equals("alice"));
             Assert.assertTrue(name.startsWith("a"));
             Assert.assertFalse(name.startsWith("A"));
         }
 
         // ok now case insensitive - using the 'i' option
         startsWithAQuery = new BasicDBObject("name",
                 new BasicDBObject("$regex", "^a").append("$options", "i"));
         aResults = collection.find(startsWithAQuery);
 
         Assert.assertEquals(3, aResults.length());
         for (DBObject aResult : aResults) {
             final String name = (String) aResult.get("name");
             Assert.assertTrue(name.equals("arin") || name.equals("alice") || name.equals("Alex"));
             Assert.assertTrue(name.startsWith("a") || name.startsWith("A"));
         }
     }
 
     @Test
     /**
      * these docs look like
      * {
      *      address: {"city": STRING, "zip" INTEGER}}
      * }
      */
     public void valuesInEmbededObjectEquals() {
         final BasicDBObject user1 = new BasicDBObject();
         user1.put("address", new BasicDBObject("city", "SF").append("zip", 94107));
 
         final BasicDBObject user2 = new BasicDBObject();
         user2.put("address", new BasicDBObject("city", "LA").append("zip", 90210));
 
         collection.insert(user1, user2);
 
         // now lets query and find docs where address.city == SF
         final BasicDBObject query = new BasicDBObject("address.city", "SF");
         final DBObject sfUser = collection.findOne(query);
         final Map address = (Map) sfUser.get("address");
         Assert.assertEquals(94107, address.get("zip"));
     }
 
     @Test
     /**
      * these docs look like
      * {
      *      profile: {"age": INT, "email" INTEGER}}
      * }
      */
     public void valuesInEmbededObjectMeetCriteria() {
         final BasicDBObject user1 = new BasicDBObject();
         user1.put("profile", new BasicDBObject("age", 32).append("email", "a@example.com"));
 
         final BasicDBObject user2 = new BasicDBObject();
         user2.put("profile", new BasicDBObject("age", 55).append("email", "b@example.com"));
 
         final BasicDBObject user3 = new BasicDBObject();
         user3.put("profile", new BasicDBObject("age", 20).append("email", "c@example.com"));
 
         collection.insert(user1, user2, user3);
 
         // now lets query and find users who are older than 20 - should be 2 of em
         final BasicDBObject query = new BasicDBObject("profile.age", new BasicDBObject("$gt", 20));
         final DBCursor results = collection.find(query);
         Assert.assertEquals(2, results.length());
 
         for (DBObject result : results) {
             Map profile = (Map) result.get("profile");
             final Integer age = (Integer) profile.get("age");
             Assert.assertTrue(age > 20);
         }
     }
 
     @Test
     public void sortResultsByField() {
         final List<DBObject> list = new ArrayList<DBObject>();
         for (int i = 1; i < 100; i++) {
             list.add(new BasicDBObject("age", i));
         }
 
         // shuffle the list so the docs are inserted in random order...
         Collections.shuffle(list);
         collection.insert(list);
 
         // get by age ASC
         final DBCursor asc = collection.find().sort(new BasicDBObject("age", 1));
         int lastAge = 0;
 
         for (DBObject dbObject : asc) {
             int age = ((Integer) dbObject.get("age")).intValue();
             Assert.assertTrue(age > lastAge);
             lastAge = age;
         }
 
         // get by age DESC
         final DBCursor desc = collection.find().sort(new BasicDBObject("age", -1));
         lastAge = 100;
 
         for (DBObject dbObject : desc) {
             int age = ((Integer) dbObject.get("age")).intValue();
             Assert.assertTrue(age < lastAge);
             lastAge = age;
         }
     }
 }
