 package org.melati.poem.test;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import org.melati.poem.Database;
 import org.melati.poem.PoemDatabaseFactory;
 import org.melati.poem.AccessToken;
 import org.melati.poem.Column;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemTask;
 import org.melati.poem.Table;
 import org.melati.poem.DatabaseInitialisationPoemException;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 
 /**
  * A TestCase that runs in a Database session.
  * 
  * @author timp
  * @since 19-May-2006
  */
 public abstract class PoemTestCase extends TestCase implements Test {
 
   /**
    * The name of the test case
    */
   private String fName;
 
   /** Default db name */
   public static final String databaseName = "melatijunit";  // change to poemtest
   
   private AccessToken userToRunAs;
 
   /**
    * Constructor.
    */
   public PoemTestCase() {
     super();
     fName = null;
   }
 
   /**
    * Constructor.
    * 
    * @param name
    */
   public PoemTestCase(String name) {
     super(name);
     fName = name;
   }
   boolean problem = false;
   String dbUrl = null;
   /**
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
     problem = false;
     assertEquals(4, getDb().getFreeTransactionsCount());
   }
 
   /**
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     if (!problem) {
       checkDbUnchanged();
       assertEquals("Not all transactions free", 4, getDb().getFreeTransactionsCount());
     }
   }
   /** Properties, named for this class. */
   public static Properties databaseDefs = null;
 
   private Properties databaseDefs() {
     if (databaseDefs == null)
       databaseDefs = getProperties();
     return databaseDefs;
   }
 
   /**
    * Run the test in a session.
    * 
    * @see junit.framework.TestCase#runTest()
    */
   protected void runTest() throws Throwable {
     assertNotNull(fName);
     try {
       // use getMethod to get all public inherited
       // methods. getDeclaredMethods returns all
       // methods of this class but excludes the
       // inherited ones.
       final Method runMethod = getClass().getMethod(fName, null);
       if (!Modifier.isPublic(runMethod.getModifiers())) {
         fail("Method \"" + fName + "\" should be public");
       }
       // Ensures that we are invoking on
       // the object that method belongs to.
       final Object _this = this;
       getDb().inSession(getUserToRunAs(), // HACK
           new PoemTask() {
             public void run() {
               try {
                 runMethod.invoke(_this, new Class[0]);
               } catch (Throwable e) {
                 problem = true;
                 e.fillInStackTrace();
                 throw new RuntimeException(e);
               }
             }
             public String toString() { 
               return "PoemTestCase:"+ fName;
             }
           });
     } catch (NoSuchMethodException e) {
       fail("Method \"" + fName + "\" not found");
     }
   }
 
   protected void checkDbUnchanged() {
     getDb().inSession(AccessToken.root, // HACK
         new PoemTask() {
           public void run() {
             databaseUnchanged();
           }
         });
 
   }
   protected void databaseUnchanged() { 
     assertEquals("Setting changed", 0, getDb().getSettingTable().count());
     assertEquals("Group changed", 1, getDb().getGroupTable().count());
     assertEquals("GroupMembership changed", 1, getDb().getGroupMembershipTable().count());
     assertEquals("Capability changed", 5, getDb().getCapabilityTable().count());
     assertEquals("GroupCapability changed", 1, getDb().getGroupCapabilityTable().count());
     assertEquals("TableCategory changed", 2, getDb().getTableCategoryTable().count());
     assertEquals("User changed", 2, getDb().getUserTable().count());
     assertEquals("ColumnInfo changed", 69, getDb().getColumnInfoTable().count());
     assertEquals("TableInfo changed", 9, getDb().getTableInfoTable().count());
     checkTablesAndColumns(9,69);
   }
 
   protected void checkTablesAndColumns(int tableCount, int columnCount) {
     checkTables(tableCount);
     checkColumns(columnCount);
   }
   protected void checkTables(int tableCount) {
     Enumeration e = getDb().tables();
     int count = 0;
     while (e.hasMoreElements()) {
       Table t = (Table)e.nextElement();
       if (t.getTableInfo().statusExistent()) count++;
     }
     if (count != tableCount) {
       System.out.println(fName + " Additional tables - expected:" + 
               tableCount + " found:" + count);
       e = getDb().tables();
       while (e.hasMoreElements()) {
         Table t = (Table)e.nextElement();
         System.out.println(t.getTableInfo().troid() + " " +
                 t.getTableInfo().statusExistent() + " " +
                 t);
       }      
     }
     assertEquals(tableCount, count);
   }
   protected void checkColumns(int columnCount) {
     Enumeration e = getDb().columns();
     int count = 0;
     while (e.hasMoreElements()) {
       Column c = (Column)e.nextElement();
       if (c.getColumnInfo().statusExistent())
         count++;
     }
     if (count != columnCount) {
       System.out.println(fName + " Additional columns - expected:" + 
               columnCount + " found:" + count);
       e = getDb().columns();
       while (e.hasMoreElements()) {
         System.out.println((Column)e.nextElement());
       }      
     }
     assertEquals(columnCount, count);
   }
   
   protected void dumpTable(Table t) {
     Enumeration them = t.selection();
     while (them.hasMoreElements()) {
       Persistent it = (Persistent)them.nextElement();
       System.err.println(it.troid() + " " + it.getCooked("name") + " " +
           it.getTable().getName());
     }
     
   }
   /**
    * Gets the name of a TestCase.
    * 
    * @return returns a String
    */
   public String getName() {
     return fName;
   }
 
   /**
    * Sets the name of a TestCase.
    * 
    * @param name
    *          The name to set
    */
   public void setName(String name) {
     fName = name;
   }
 
   /**
    * @return Returns the db.
    */
   public Database getDb() {
     return getDb(databaseName);
   }
 
   public Database getDb(String dbNameP) {
     if (dbNameP == null)
       throw new NullPointerException();
     Database dbL = null;
     try {
       dbL = getDatabase(dbNameP);
     } catch (DatabaseInitialisationPoemException e) {
       e.printStackTrace();
       fail(e.getMessage());
     }
     return dbL;
   }
 
   public Database getDatabase(String name){ 
     Properties defs = databaseDefs();
     String pref = "org.melati.poem.test.PoemTestCase." + name + ".";
 
     return PoemDatabaseFactory.getDatabase(name,
             getOrDie(defs, pref + "url"), 
             getOrDie(defs, pref + "user"),
             getOrDie(defs, pref + "password"),
             getOrDie(defs, pref + "class"),
             getOrDie(defs, pref + "dbmsclass"),
             new Boolean(getOrDie(defs, pref + "addconstraints")).booleanValue(),
             new Boolean(getOrDie(defs, pref + "logsql")).booleanValue(),
             new Boolean(getOrDie(defs, pref + "logcommits")).booleanValue(),
             new Integer(getOrDie(defs, pref + "maxtransactions")).intValue());
   }
   public AccessToken getUserToRunAs() {
     if (userToRunAs == null) return AccessToken.root;
     return userToRunAs;
   }
 
   public void setUserToRunAs(AccessToken userToRunAs) {
     if (userToRunAs == null) 
       this.userToRunAs = AccessToken.root;
     else
       this.userToRunAs = userToRunAs;
   }
 
   private Properties getProperties() {
     String className = "org.melati.poem.test.PoemTestCase";
     String name = className + ".properties";
    InputStream is = EverythingDatabaseTables.class.getResourceAsStream(name);
 
     if (is == null)
       throw new RuntimeException(new FileNotFoundException(name + ": is it in CLASSPATH?"));
 
     Properties them = new Properties();
     try {
       them.load(is);
     } catch (IOException e) {
       throw new RuntimeException(new IOException("Corrupt properties file `" + name + "': " +
       e.getMessage()));
     }
 
     return them;
   }
   /**
    * Return a property.
    * 
    * @param properties the {@link Properties} object to look in 
    * @param propertyName the property to get 
    * @return the property value
    * @throws NoSuchPropertyException if the property is not set
    */
   public static String getOrDie(Properties properties, String propertyName) {
     String value = properties.getProperty(propertyName);
     if (value == null)
       throw new RuntimeException("Property " + propertyName + " not found in " + properties);
     return value;
   }
 
 }
