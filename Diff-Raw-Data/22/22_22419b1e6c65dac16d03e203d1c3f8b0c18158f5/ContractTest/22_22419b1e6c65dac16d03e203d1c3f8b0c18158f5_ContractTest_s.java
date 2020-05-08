 package de.aidger.model.models;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.Date;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * Tests the Contract class.
  *
  * @author aidGer Team
  */
 public class ContractTest {
 
     protected Contract contract = null;
 
     @BeforeClass
     public static void beforeClassSetUp() {
         de.aidger.model.Runtime.getInstance().initialize();
     }
 
     @Before
     public void setUp() {
         contract = new Contract();
         contract.setId(1);
        contract.setCompletionDate(new Date(100));
        contract.setConfirmationDate(new Date(10));
         contract.setDelegation(false);
         contract.setEndDate(new Date(1000));
         contract.setStartDate(new Date(20));
         contract.setType("Type");
     }
 
     /**
      * Test of constructor, of class Contract.
      */
     @Test
     public void testConstructor() throws AdoHiveException {
         System.out.println("Constructor");
 
         contract.setNew(true);
         contract.save();
 
         Contract result = new Contract(contract.getById(
                 contract.getId()));
 
         assertNotNull(result);
         assertEquals(contract, result);
     }
 
     /**
      * Test of validation, of class Contract.
      */
     @Test
     public void testValidation() throws AdoHiveException {
         System.out.println("Validation");
 
         contract.setNew(true);
         assertTrue(contract.save());
 
         contract.setCompletionDate(null);
         assertFalse(contract.save());
         contract.resetErrors();
        contract.setCompletionDate(new Date(100));
 
         contract.setConfirmationDate(null);
         assertFalse(contract.save());
         contract.resetErrors();
        contract.setConfirmationDate(new Date(10));
 
         contract.setEndDate(null);
         assertFalse(contract.save());
         contract.resetErrors();
         contract.setEndDate(new Date(1000));
 
         contract.setStartDate(null);
         assertFalse(contract.save());
         contract.resetErrors();
         contract.setStartDate(new Date(20));
 
         contract.setType(null);
         assertFalse(contract.save());
         contract.resetErrors();
 
         contract.setType("012345678901234567890");
         assertFalse(contract.save());
         contract.resetErrors();
         contract.setType("Type");
 
        contract.setConfirmationDate(new Date(101));
         assertFalse(contract.save());
         contract.resetErrors();
         contract.setConfirmationDate(new Date(10));
 
         contract.setEndDate(new Date(10));
         assertFalse(contract.save());
         contract.resetErrors();
         contract.setEndDate(new Date(1000));
     }
 
     /**
      * Test of validateOnRemove, of class Contract.
      */
     @Test
     public void testValidateOnRemove() throws AdoHiveException {
         System.out.println("validateOnRemove");
 
         assertTrue(contract.save());
         assertTrue(contract.remove());
 
         contract.save();
         Employment employment = new Employment();
         employment.setAssistantId(-1);
         employment.setContractId(contract.getId());
         employment.setCourseId(-1);
         employment.setCostUnit("0711");
         employment.setFunds(1);
         employment.setHourCount(40);
         employment.setMonth((byte) 10);
         employment.setQualification("g");
         employment.setRemark("Remark");
         employment.setYear((short) 2010);
         employment.save();
 
         assertFalse(contract.remove());
         contract.resetErrors();
         employment.remove();
 
         assertTrue(contract.remove());
     }
 
     /**
      * Test of clone method, of class Contract.
      */
     @Test
     public void testClone() {
         System.out.println("clone");
 
         Contract result = contract.clone();
 
         assertEquals(contract.getId(), result.getId());
         assertEquals(contract.getCompletionDate(), result.getCompletionDate());
         assertEquals(contract.getConfirmationDate(), result.getConfirmationDate());
         assertEquals(contract.isDelegation(), result.isDelegation());
         assertEquals(contract.getEndDate(), result.getEndDate());
         assertEquals(contract.getStartDate(), result.getStartDate());
         assertEquals(contract.getType(), result.getType());
     }
 
     /**
      * Test of equals method, of class Contract.
      */
     @Test
     public void testEquals() {
         System.out.println("equals");
         Contract result = contract.clone();
 
         assertEquals(contract, result);
         assertFalse(contract.equals(new Object()));
     }
 
     /**
      * Test of hashCode method, of class Contract.
      */
     @Test
     public void testHashCode() {
         System.out.println("hashCode");
         Contract result = contract.clone();
 
         assertEquals(contract.hashCode(), result.hashCode());
     }
 
 }
