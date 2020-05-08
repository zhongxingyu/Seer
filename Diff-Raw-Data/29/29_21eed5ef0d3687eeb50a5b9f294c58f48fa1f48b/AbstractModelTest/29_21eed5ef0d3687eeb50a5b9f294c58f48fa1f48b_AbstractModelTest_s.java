 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.aidger.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.HourlyWage;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * 
  * @author rmbl
  */
 public class AbstractModelTest {
 
     /**
      * Test of getAll method, of class AbstractModel.
      */
     @Test
     public void testGetAll() throws AdoHiveException {
         System.out.println("getAll");
         Employment e = new Employment();
         e.setCostUnit("0711");
         e.save();
 
         Employment f = new Employment();
         f.setCostUnit("0711");
         f.save();
 
         List list = e.getAll();
 
         assertNotNull(list);
         assertEquals(list.size(), 2);
         assertEquals(list.get(0), e);
         assertEquals(list.get(1), f);
     }
 
     /**
      * Test of getById method, of class AbstractModel.
      */
     @Test
     public void testGetById() throws AdoHiveException {
         System.out.println("getById");
 
         Employment e = new Employment();
         e.setCostUnit("0711");
         e.save();
 
         Employment result = (Employment) e.getById(e.getId());
 
         assertNotNull(result);
         assertEquals(e.getId(), result.getId());
     }
 
     /**
      * Test of getByKeys method, of class AbstractModel.
      */
     @Test
     public void testGetByKeys() throws AdoHiveException {
         System.out.println("getByKeys");
 
         HourlyWage h = new HourlyWage();
         h.setQualification("Tester");
         h.setMonth((byte) 10);
         h.setYear((short) 2010);
         h.save();
 
         HourlyWage result = (HourlyWage) h.getByKeys("Tester", (byte) 10,
                 (short) 2010);
 
         assertNotNull(result);
         assertEquals(h, result);
 
         Employment e = new Employment();
         e.setCostUnit("0711");
         e.save();
 
         Employment res = (Employment) e.getByKeys(e.getId());
 
         assertEquals(e, res);
     }
 
     /**
      * Test of save method, of class AbstractModel.
      */
     @Test
     public void testSave() throws AdoHiveException {
         System.out.println("save");
 
         Employment e = new Employment();
         e.setCostUnit("0711");
         e.save();
 
         assertTrue(e.getId() > 0);
         assertEquals(e, e.getById(e.getId()));
 
         e.setAssistantId(1);
         e.setContractId(1);
         e.save();
 
         assertEquals(e, e.getById(e.getId()));
     }
 
     /**
      * Test of remove method, of class AbstractModel.
      */
     @Test
     public void testRemove() throws Exception {
         System.out.println("remove");
 
         Employment e = new Employment();
         e.setCostUnit("0711");
         e.save();
         int id = e.getId();
 
         e.remove();
 
         assertNull(e.getById(id));
         assertTrue(e.getId() <= 0);
     }
 
 }
