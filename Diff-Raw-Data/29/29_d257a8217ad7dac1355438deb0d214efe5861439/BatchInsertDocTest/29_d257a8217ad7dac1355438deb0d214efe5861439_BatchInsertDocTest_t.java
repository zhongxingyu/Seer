 package com.github.cneftali.neo4j.data.batchimport;
 
 import org.neo4j.helpers.collection.MapUtil;
 import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
 import org.neo4j.unsafe.batchinsert.BatchInserter;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
 import org.neo4j.unsafe.batchinsert.BatchInserters;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import static com.github.cneftali.neo4j.data.batchimport.RelTypes.BUY;
 import static com.github.cneftali.neo4j.data.batchimport.RelTypes.LIVES;
 
 public class BatchInsertDocTest {
     private static final Logger LOGGER = LoggerFactory.getLogger(BatchInsertDocTest.class);
 
     private static final int MAX_LAST_NAME = 150;
     private static final int MAX_FIRST_NAME = 150;
     private static final int NO_TRANSACTION_USER_PERCENT = 70;
     private static final int MAX_TRANSACTION_PER_MEMBER = 100;
     private static final int NO_ACTIVITY_USER_PERCENT = 80;
     private static final int MAX_ACTIVITY_PER_MEMBER = 100;
 
     private static final String DB_PATH = "target/graph.db";
     private static final double MAX_AMOUNT = 1000.0;
     private static final double MIN_AMOUNT = 1.0;
     public static final int NUMBER_OF_PRODUCTS = 1000;
     public static final int NUMBER_OF_SITES = 30000;
     public static final int NUMBER_OF_MEMBERS = 1000000;
 
     private static int allTransactionCount;
     private static int allActionCount;
     private static int noTransactionMember;
     private static int noActionMember;
 
     private static int indice = 0;
     private static int product_min_indice = 0;
     private static int product_max_indice = 0;
     private static int site_min_indice = 0;
     private static int site_max_indice = 0;
 
     public static void main(final String[] args) throws IOException {
 
         LOGGER.info("Generation ...");
         final BatchInserter inserter = BatchInserters.inserter(DB_PATH);
         final BatchInserterIndexProvider indexProvider = new LuceneBatchInserterIndexProvider(inserter);
 
         insertCountries(inserter, indexProvider);
         insertProduits(inserter, indexProvider);
         insertSites(inserter, indexProvider);
         insertPersons(inserter, indexProvider);
 
         // Make sure to shut down the index provider as well
         indexProvider.shutdown();
         inserter.shutdown();
 
         float actualNoTransactionMember = (float) noTransactionMember / (float) NUMBER_OF_MEMBERS * 100;
         float actualNoActivityMember = (float) noActionMember / (float) NUMBER_OF_MEMBERS * 100;
         LOGGER.info("No transaction user percent : {}%", actualNoTransactionMember);
         LOGGER.info("No activity user percent : {}%", actualNoActivityMember);
         LOGGER.info("Count all transactions : {}", allTransactionCount);
         LOGGER.info("Count all actions : {}", allActionCount);
         LOGGER.info("Generation done !");
     }
 
     private static void insertCountries(final BatchInserter inserter, final BatchInserterIndexProvider indexProvider) {
         final BatchInserterIndex countries = indexProvider.nodeIndex("Countries", MapUtil.stringMap("type", "exact"));
         countries.setCacheCapacity("name", Country.values().length);
 
         LOGGER.info("Begin country indice : {}", indice);
         for (final Country country : Country.values()) {
             final Map<String, Object> properties = new HashMap<>(2);
             properties.put("name", country.name());
 
             long node = inserter.createNode(properties);
             countries.add(node, properties);
             indice++;
         }
        LOGGER.info("End country indice : {}", indice - 1);
         //make the changes visible for reading, use this sparsely, requires IO!
         countries.flush();
     }
 
     public static void insertProduits(final BatchInserter inserter, final BatchInserterIndexProvider indexProvider) throws IOException {
         final BatchInserterIndex produits = indexProvider.nodeIndex("Products", MapUtil.stringMap("type", "exact"));
         produits.setCacheCapacity("id", 1000);
         produits.setCacheCapacity("name", 1000);
 
         //Produit
        product_min_indice = indice;
         LOGGER.info("Begin product indice : {}", product_min_indice);
        for (int cpt = product_min_indice; cpt < product_min_indice + NUMBER_OF_PRODUCTS; cpt++) {
             final Map<String, Object> properties = new HashMap<>(3);
             properties.put("id", cpt);
             properties.put("name", "product" + String.valueOf(cpt));
             properties.put("description", "desc" + String.valueOf(cpt));
             long node = inserter.createNode(properties);
             produits.add(node, properties);
             indice++;
         }
        product_max_indice = indice - 1;
         LOGGER.info("End product indice : {}", product_max_indice);
         //make the changes visible for reading, use this sparsely, requires IO!
         produits.flush();
     }
 
     public static void insertSites(final BatchInserter inserter, final BatchInserterIndexProvider indexProvider) throws IOException {
         final BatchInserterIndex activities = indexProvider.nodeIndex("Sites", MapUtil.stringMap("type", "exact"));
         activities.setCacheCapacity("site", 30000);
         activities.setCacheCapacity("id", 30000);
 
         //Site
        site_min_indice = indice;
         LOGGER.info("Begin site indice : {}", site_min_indice);
        for (int cpt = site_min_indice; cpt < site_min_indice + NUMBER_OF_SITES; cpt++) {
             final Map<String, Object> properties = new HashMap<>(2);
             properties.put("id", cpt);
             properties.put("site", "site" + String.valueOf(cpt));
             long node = inserter.createNode(properties);
             activities.add(node, properties);
             indice++;
         }
        site_max_indice = indice - 1;
         LOGGER.info("End site indice : {}", site_max_indice);
         //make the changes visible for reading, use this sparsely, requires IO!
         activities.flush();
     }
 
     public static void insertPersons(final BatchInserter inserter, final BatchInserterIndexProvider indexProvider) throws IOException {
         final BatchInserterIndex persons = indexProvider.nodeIndex("Members", MapUtil.stringMap("type", "exact"));
         persons.setCacheCapacity("id", 100000);
         persons.setCacheCapacity("email", 100000);
         persons.setCacheCapacity("type", 100000);
 
         //Member
        int member_min_indice = indice;
         LOGGER.info("Begin member indice : {}", member_min_indice);
        for (int cpt = member_min_indice; cpt < member_min_indice + NUMBER_OF_MEMBERS; cpt++) {
             final Map<String, Object> properties = new HashMap<>(6);
             properties.put("id", cpt);
             properties.put("firstname", "firstname" + randBetween(1, MAX_FIRST_NAME));
             properties.put("lastname", "lastname" + randBetween(1, MAX_LAST_NAME));
             properties.put("email", "email" + String.valueOf(cpt) + "@example.com");
             properties.put("birthdate", getRandomBirthDay().getTime());
             properties.put("type", "Members");
 
             long node = inserter.createNode(properties);
             insertRelationshipPersonCountry(inserter, node);
             insertRelationshipPersonProduit(inserter, node);
             insertRelationshipSite(inserter, node);
             persons.add(node, properties);
             indice++;
         }
 
        int member_max_indice = indice - 1;
         LOGGER.info("End member indice : {}", member_max_indice);
         //make the changes visible for reading, use this sparsely, requires IO!
         persons.flush();
     }
 
     //Transactions
     public static void insertRelationshipPersonProduit(final BatchInserter inserter, long nodePerson) throws IOException {
         if (randBetween(0, 100) > (NO_TRANSACTION_USER_PERCENT)) {
             int countTransaction = randBetween(1, MAX_TRANSACTION_PER_MEMBER);
             for (int i = 0; i < countTransaction; i++) {
                 final Map<String, Object> properties = new HashMap<>(2);
                 properties.put("amount", getRandomAmout());
                 properties.put("date", getRandomBusinessDate().getTime());
                 inserter.createRelationship(nodePerson, (long) randBetween(product_min_indice, product_max_indice), BUY, properties);
             }
             allTransactionCount += countTransaction;
         } else {
             noTransactionMember++;
         }
     }
 
     //Activities
     public static void insertRelationshipSite(final BatchInserter inserter, long nodePerson) throws IOException {
         if (randBetween(0, 100) > (NO_ACTIVITY_USER_PERCENT)) {
             int countAction = randBetween(1, MAX_ACTIVITY_PER_MEMBER);
             for (int i = 0; i < countAction; i++) {
                 final Map<String, Object> properties = new HashMap<>(1);
                 properties.put("date", getRandomBusinessDate().getTime());
                 inserter.createRelationship(nodePerson, (long) randBetween(site_min_indice, site_max_indice), DynamicRelType.getType("ACTION" + randBetween(1, 10)), properties);
             }
             allActionCount += countAction;
         } else {
             noActionMember++;
         }
     }
 
     private static Date getRandomBusinessDate() {
         final Calendar calendar = Calendar.getInstance();
         int year = randBetween(2000, 2013);
         int dayOfYear = randBetween(1, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
 
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
         return calendar.getTime();
     }
 
     public static void insertRelationshipPersonCountry(final BatchInserter inserter, long nodePerson) throws IOException {
         inserter.createRelationship(nodePerson, (long) randBetween(1, Country.values().length), LIVES, null);
     }
 
     private static java.util.Date getRandomBirthDay() {
         final Calendar calendar = Calendar.getInstance();
         int year = randBetween(1950, 2000);
         int dayOfYear = randBetween(1, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
 
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
         return calendar.getTime();
     }
 
     private static int randBetween(int start, int end) {
         return start + (int) Math.round(Math.random() * (end - start));
     }
 
     private static double getRandomAmout() {
         final Random random = new Random();
         return MIN_AMOUNT + (MAX_AMOUNT - MIN_AMOUNT) * random.nextDouble();
     }
 }
