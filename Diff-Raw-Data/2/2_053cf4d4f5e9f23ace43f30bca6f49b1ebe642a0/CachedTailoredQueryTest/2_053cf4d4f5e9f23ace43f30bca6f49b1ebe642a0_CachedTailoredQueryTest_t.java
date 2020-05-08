 /**
  * 
  */
 package org.melati.poem.test;
 
 //import java.util.Enumeration;
 
 import org.melati.poem.CachedTailoredQuery;
 import org.melati.poem.Column;
 //import org.melati.poem.Field;
 //import org.melati.poem.FieldSet;
 import org.melati.poem.Table;
 import org.melati.util.EnumUtils;
 
 /**
  * @author timp
  */
 public class CachedTailoredQueryTest extends PoemTestCase {
 
   /**
    * Constructor for CachedTailoredQueryTest.
    * 
    * @param name
    */
   public CachedTailoredQueryTest(String name) {
     super(name);
   }
 
   /*
    * @see PoemTestCase#setUp()
    */
   protected void setUp()
       throws Exception {
     super.setUp();
   }
 
   /*
    * @see PoemTestCase#tearDown()
    */
   protected void tearDown()
       throws Exception {
     super.tearDown();
   }
 
   /**
    * @see org.melati.poem.CachedTailoredQuery#selection()
    */
   public void testSelection() {
 
   }
 
   /** 
    * This needs more thought.
    * 
    * @see org.melati.poem.CachedTailoredQuery.selection_firstRaw()'
    */
   public void testSelection_firstRaw() {
     Column[] cols = new Column[2];
     cols[0] = getDb().getTableInfoTable().getColumn("category");
     cols[1] = getDb().getTableCategoryTable().troidColumn();
     Table[] tables = new Table[2];
     tables[0] = getDb().getTableInfoTable();
     tables[1] = getDb().getTableCategoryTable();
     int queries = getDb().getQueryCount();
     CachedTailoredQuery ctq = new CachedTailoredQuery(cols,
                                                       tables, 
                                                       null,
                                                       null);
     int queries2 = getDb().getQueryCount();
     assertEquals(queries, queries2);
     getDb().setLogSQL(true);
     assertEquals(18,EnumUtils.vectorOf(ctq.selection_firstRaw()).size());
     int queries3 = getDb().getQueryCount();
    assertEquals(queries2 + 1, queries3); 
     assertEquals(18,EnumUtils.vectorOf(ctq.selection_firstRaw()).size());
     int queries4 = getDb().getQueryCount();
     assertEquals(queries3, queries4);
 
   }
 
   /**
    * @see org.melati.poem.CachedTailoredQuery# CachedTailoredQuery(String,
    *      Column[], Table[], String, String)
    */
   public void testCachedTailoredQueryStringColumnArrayTableArrayStringString() {
     Column[] cols = new Column[2];
     cols[0] = getDb().getTableInfoTable().getColumn("category");
     cols[1] = getDb().getTableCategoryTable().troidColumn();
     Table[] tables = new Table[2];
     tables[0] = getDb().getTableInfoTable();
     tables[1] = getDb().getTableCategoryTable();
     int queries = getDb().getQueryCount();
     CachedTailoredQuery ctq = new CachedTailoredQuery(cols,
                                                       tables, 
                                                       null,
                                                       null);
     int queries2 = getDb().getQueryCount();
     assertEquals(queries, queries2);
     getDb().setLogSQL(true);
     assertEquals(18,EnumUtils.vectorOf(ctq.selection()).size());
     int queries3 = getDb().getQueryCount();
     assertEquals(queries2 + 1, queries3); 
     assertEquals(18,EnumUtils.vectorOf(ctq.selection()).size());
     int queries4 = getDb().getQueryCount();
     assertEquals(queries3, queries4);
     
     /*
     System.err.println(ctq.toString());
     Enumeration en = ctq.selection();
     while (en.hasMoreElements()) {
       FieldSet fs = (FieldSet) en.nextElement();
       Enumeration fields = fs.elements();
       System.err.println("--");
       while (fields.hasMoreElements()) {
         Field f = (Field) fields.nextElement();
         System.err.println(f.getName() + "=" + f.getRawString());
       }
 
     }
     System.err.println("==");
     */
     CachedTailoredQuery ctqDistinct = new CachedTailoredQuery("DISTINCT", cols,
                                                               tables, null,
                                                               null);
     assertEquals(4,EnumUtils.vectorOf(ctqDistinct.selection()).size());
     /* 
     System.err.println(ctqDistinct.toString());
     Enumeration en2 = ctqDistinct.selection();
     while (en2.hasMoreElements()) {
       FieldSet fs = (FieldSet) en2.nextElement();
       Enumeration fields = fs.elements();
       System.err.println("--");
       while (fields.hasMoreElements()) {
         Field f = (Field) fields.nextElement();
         System.err.println(f.getName() + "=" + f.getRawString());
       }
     }
     */
   }
 
   /**
    * @see org.melati.poem.CachedTailoredQuery#CachedTailoredQuery(Column[],
    *      Table[], String, String)
    */
   public void testCachedTailoredQueryColumnArrayTableArrayStringString() {
 
   }
 
   /**
    * @see org.melati.poem.CachedTailoredQuery#upToDate()
    */
   public void testUpToDate() {
 
   }
 
 }
