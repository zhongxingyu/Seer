 package net.jonp.sorm.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.HashSet;
 
 import net.jonp.sorm.CacheMode;
 import net.jonp.sorm.SormContext;
 import net.jonp.sorm.SormSession;
 import net.jonp.sorm.example.Person;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests ORM accesses on the Person test object.
  */
 public class PersonTest
 {
     private SormContext context;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp()
         throws Exception
     {
         context = DBInit.dbinit();
     }
 
     @AfterClass
     public static void tearDownClass()
     {
         DBInit.delete();
     }
 
     @Test
     public void testSingleInsert()
         throws SQLException
     {
         final SormSession session = context.getTransientSession(CacheMode.None);
 
         try {
             testSingleInsertImpl(session);
         }
         finally {
             session.close();
         }
     }
 
     @Test
     public void testCachedSingleInsert()
         throws SQLException
     {
         final SormSession session = context.getTransientSession(CacheMode.Immediate);
 
         try {
             testSingleInsertImpl(session);
         }
         finally {
             session.close();
         }
     }
 
     private void testSingleInsertImpl(final SormSession session)
         throws SQLException
     {
         final Person person = buildSimpleObjects(1)[0];
         Person.Orm.create(session, person);
 
         final Person test = Person.Orm.read(session, person.getId());
         assertEquals(person.getName(), test.getName());
         assertEquals(person.getGender(), test.getGender());
         assertEquals(person.getDob(), test.getDob());
     }
 
     /**
      * Build objects with no inter-object relationships. Does not insert them
      * into the database.
      * 
      * @param count The number of objects to build.
      * @return The objects that were built.
      */
     private Person[] buildSimpleObjects(final int count)
     {
         final Calendar dobcal = Calendar.getInstance();
         dobcal.add(Calendar.YEAR, -count);
         dobcal.add(Calendar.MONTH, -count);
 
         final Person[] objs = new Person[count];
         for (int i = 0; i < count; i++) {
             objs[i] = new Person();
             objs[i].setName("Person " + i);
             objs[i].setDob(dobcal.getTime());
 
             // Alternate male/female
             if (i % 2 == 0) {
                 objs[i].setGender("male");
             }
             else {
                 objs[i].setGender("female");
             }
 
             dobcal.add(Calendar.DAY_OF_YEAR, i);
         }
 
         return objs;
     }
 
     /**
      * Build a number of objects with inter-object relationships of all types,
      * and insert them into the database (one at a time). A {@link SormSession}
      * for this thread will be left open.
      * 
      * @param count The number of objects to build and insert.
      * @return The objects that were built.
      * @throws SQLException If there was an error inserting the objects.
      */
     private Person[] populate(final int count)
         throws SQLException
     {
         final SormSession session = context.getSession();
 
         final Calendar dobcal = Calendar.getInstance();
         dobcal.add(Calendar.YEAR, -count);
         dobcal.add(Calendar.MONTH, -count);
 
         final Person[] objs = new Person[count];
         for (int i = 0; i < count; i++) {
             objs[i] = new Person();
             objs[i].setName("Person " + i);
             objs[i].setDob(dobcal.getTime());
 
             // Alternate male/female
             if (i % 2 == 0) {
                 objs[i].setGender("male");
             }
             else {
                 objs[i].setGender("female");
             }
 
             Person.Orm.create(session, objs[i]);
 
             // Alternate marrying the current with the previous with making
             // the current pair the previous pair's children
             if (i % 4 == 1) {
                 objs[i - 1].setSpouse(objs[i - 0].getId());
                 objs[i - 0].setSpouse(objs[i - 1].getId());
             }
            else {
                 objs[i - 1].setFather(objs[i - 3].getId());
                 objs[i - 1].setMother(objs[i - 2].getId());
                 objs[i - 0].setFather(objs[i - 3].getId());
                 objs[i - 0].setMother(objs[i - 2].getId());
             }
 
             objs[i].setFriends(new HashSet<Person>());
 
             // Make the children of each set of parents friends
             if (i % 8 == 7) {
                 objs[i - 5].getFriends().add(objs[i - 1]);
                 objs[i - 1].getFriends().add(objs[i - 5]);
                 Person.Orm.mapFriends(session, objs[i - 5], objs[i - 1]);
 
                 objs[i - 4].getFriends().add(objs[i - 1]);
                 objs[i - 1].getFriends().add(objs[i - 4]);
                 Person.Orm.mapFriends(session, objs[i - 4], objs[i - 1]);
 
                 objs[i - 5].getFriends().add(objs[i - 0]);
                 objs[i - 0].getFriends().add(objs[i - 5]);
                 Person.Orm.mapFriends(session, objs[i - 5], objs[i - 0]);
 
                 objs[i - 4].getFriends().add(objs[i - 0]);
                 objs[i - 0].getFriends().add(objs[i - 4]);
                 Person.Orm.mapFriends(session, objs[i - 4], objs[i - 0]);
             }
 
             dobcal.add(Calendar.DAY_OF_YEAR, i);
         }
 
         return objs;
     }
 }
