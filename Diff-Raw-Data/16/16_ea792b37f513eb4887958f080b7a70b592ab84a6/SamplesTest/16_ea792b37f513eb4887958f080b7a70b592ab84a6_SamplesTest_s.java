 package ruffkat.hombucha.store;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.Rollback;
 import ruffkat.hombucha.measure.Measurements;
 import ruffkat.hombucha.measure.Molarity;
 import ruffkat.hombucha.model.Container;
 import ruffkat.hombucha.model.Ferment;
 import ruffkat.hombucha.model.Friend;
 import ruffkat.hombucha.model.Mushroom;
 import ruffkat.hombucha.model.Online;
 import ruffkat.hombucha.model.Sample;
 import ruffkat.hombucha.util.Dates;
 
 import javax.measure.quantity.Volume;
 import javax.persistence.EntityNotFoundException;
 import java.net.URL;
 import java.util.Calendar;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 public class SamplesTest extends FunctionalTest {
     private Ferment ferment;
 
     @Autowired
     private Samples samples;
 
     @Before
     public void setUp()
             throws Exception {
         ferment = new Ferment();
         ferment.setStart(Dates.date(Calendar.JUNE, 12, 2011));
         ferment.setStop(Dates.date(Calendar.JUNE, 26, 2011));
     }
 
     @Test
     @Rollback(false)
     public void testSaveAndLoad() {
         entityManager.persist(ferment);
 
         Sample<Molarity> sample = samples.create(ferment,
                 Measurements.molarity("7.0 mol/l"));
 
         entityManager.persist(sample);
 
         Long id = sample.getOid();
 
         assertNotNull(id);
         assertEquals(sample, samples.load(id));
     }
 
     @Test
     @Rollback(false)
     public void testSaveAndDelete() {
         entityManager.persist(ferment);
 
         Sample<Volume> sample = samples.create(ferment,
                 Measurements.volume("6.0 l"));
 
         entityManager.persist(sample);
 
         Long id = sample.getOid();
 
         sample = samples.load(id);
         samples.delete(sample);
 
         try {
             samples.load(id);
             fail("expected an exception");
         } catch (EntityNotFoundException e) {
         }
     }
 
     @Test
     @Rollback(false)
    public void SaveSample()
             throws Exception {
         Friend friend = new Friend("Christina Toyota");
         entityManager.persist(friend);
 
         Online online = new Online("Recycled Glass Dispenser",
                 new URL("http://www.westelm.com"));
         entityManager.persist(online);
 
         // Create a mother
         Mushroom mother = new Mushroom();
         mother.setSource(friend);
         mother.setName("Mama");
         mother.setReceived(Dates.date(Calendar.MAY, 12, 2011));
         entityManager.persist(mother);
 
         // First baby
         Mushroom baby1 = new Mushroom();
         baby1.setSource(friend);
         baby1.setName("Baby 1");
         baby1.setReceived(Dates.date(Calendar.MAY, 12, 2011));
         baby1.setMother(mother);
         entityManager.persist(baby1);
 
         // Second baby
         Mushroom baby2 = new Mushroom();
         baby2.setSource(friend);
         baby2.setName("Baby 2");
         baby2.setReceived(Dates.date(Calendar.MAY, 12, 2011));
         baby2.setMother(mother);
         entityManager.persist(baby2);
 
         // Create a reactor to hold the brew
         Container containerA = new Container();
         containerA.setSource(online);
         containerA.setName("A");
         containerA.setReceived(Dates.date(Calendar.JUNE, 11, 2011));
         entityManager.persist(containerA);
 
         // Create another reactor to hold the brew
         Container containerB = new Container();
         containerB.setSource(online);
         containerB.setName("B");
         containerB.setReceived(Dates.date(Calendar.JUNE, 11, 2011));
         entityManager.persist(containerB);
 
 //        Ferment ancientPuErh2006 = new Ferment();
 //        ancientPuErh2006.setProcessing(Processing.BATCH);
 //        ancientPuErh2006.setContainer(containerA);
 //        ancientPuErh2006.setStart(Dates.date(Calendar.JUNE, 12, 2011));
 //        ancientPuErh2006.setStop(Dates.date(Calendar.JUNE, 26, 2011));
 //        entityManager.persist(ancientPuErh2006);
 //
 //        Ferment bloodOrangePuErh = new Ferment();
 //        bloodOrangePuErh.setProcessing(Processing.CONTINUOUS);
 //        bloodOrangePuErh.setContainer(containerB);
 //        bloodOrangePuErh.setStart(Dates.date(Calendar.JUNE, 12, 2011));
 //        entityManager.persist(bloodOrangePuErh);
 //
 //        Sample<Molarity> ph = samples.ph(bloodOrangePuErh);
 //        entityManager.persist(ph);
 
         entityManager.flush();
     }
 }
