 package tests;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Date;
 import java.math.BigDecimal;
 import java.util.Calendar;
 import myunit.AbstractTest;
 import myunit.UnitTest;
 import myunit.BeforeClass;
 import oop.MusicGroup;
 import oop.Member;
 import oop.Substitute;
 
 public class SubstituteTest extends AbstractTest {
   List<String> l;
   Date mon, tue, wed, thu, fri, sat, sun;
 
   @BeforeClass
   public void initializeCommonValues() {
     Calendar cal = Calendar.getInstance();
     
 
     cal.set(2012, 9, 22);
     mon = cal.getTime();
 
     cal.set(2012, 9, 23);
     tue = cal.getTime();
 
     cal.set(2012, 9, 24);
     wed = cal.getTime();
 
     cal.set(2012, 9, 18);
     thu = cal.getTime();
 
     cal.set(2012, 9, 19);
     fri = cal.getTime();
 
     cal.set(2012, 9, 20);
     sat = cal.getTime();
 
     cal.set(2012, 9, 21);
     sun = cal.getTime();
   }
 
  
   @UnitTest
   public void testChangeType() {
     MusicGroup g = new MusicGroup("The Goers");
     Member m = new Member("klaus","Moor", "0815", "Chello");
     Member m1= new Member("karl", "koala", "0816", "Guitar");
 
     g.addMember(m);
     m = g.memberToSub(m);
 
     List<Member> l = g.getCurrentMembers();
 
     for(Member mem : g.getCurrentMembers()) {
       assertTrue(mem instanceof Substitute);
     }
 
     g.subToMember(m);
     for(Member mem : g.getCurrentMembers()) {
       assertTrue(!(mem instanceof Substitute));
     }
   }
 
   @UnitTest
   public void testIsAvailable() {
     MusicGroup g = new MusicGroup("The Goers");
     Member m = new Member("klaus","Moor", "0815", "Chello");
     Member m1= new Member("karl", "koala", "0816", "Guitar");
 
     g.addMember(m);
     m = g.memberToSub(m);
 
     List<Member> l = g.getCurrentMembers();
 
     for(Member mem : g.getCurrentMembers()) {
       assertTrue(!(mem.isAvailable(new Date())));
     }
 
     g.newRehearsal("tirol", mon, tue, new BigDecimal("20.16"));
     g.newRehearsal("tirol", tue, wed, new BigDecimal("20.16"));
     g.newRehearsal("tirol", wed, thu, new BigDecimal("20.16"));
 
     for(Member mem : g.getCurrentMembers()) {
      //assertTrue(mem.isAvailable(new Date()));
       assertTrue(!(mem.isAvailable(new Date(Long.MAX_VALUE))));
     }
 
     g.subToMember(m);
     for(Member mem : g.getCurrentMembers()) {
       assertTrue(mem.isAvailable(new Date()));
     }
   }
 
 
 }
