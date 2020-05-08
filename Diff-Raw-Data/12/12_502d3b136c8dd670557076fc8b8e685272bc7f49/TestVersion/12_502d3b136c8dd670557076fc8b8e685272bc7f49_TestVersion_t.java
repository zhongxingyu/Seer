 package nl.astraeus.persistence.version;
 
 import nl.astraeus.persistence.Transaction;
 import org.junit.Test;
 
 /**
  * User: rnentjes
  * Date: 11/25/12
  * Time: 8:57 PM
  */
 public class TestVersion {
 
 
     public static void main(String [] args) {
     }
 
    @Test(expected = IllegalStateException.class)
     public void testOptimisticLockingException() {
         if (PersonDao.get().size() == 0) {
             new Transaction() {
                 @Override
                 public void execute() {
                     Person person = new Person("Pipo de Clown");
 
                     PersonDao.get().store(person);
                 }
             };
         }
 
         final Person person1 = PersonDao.get().findAll().iterator().next();
         final Person person2 = PersonDao.get().find(person1.getId());
 
         new Transaction() {
             @Override
             public void execute() {
                 person1.setName("Pipo de Clown 1");
 
                 PersonDao.get().store(person1);
             }
         };
 
         new Transaction() {
             @Override
             public void execute() {
                 person2.setName("Pipo de Clown 2");
 
                 PersonDao.get().store(person2);
             }
         };
     }
 }
