 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controllers;
 
 import Model.PayMode;
 import Model.TypeOfExpense;
import Persistence.IExpenseRepository;
 import Persistence.InMemory.ExpenseRepositoryImpl;
 import Persistence.InMemory.TypeOfExpenseRepositoryImpl;
import Persistence.PersistenceFactory;
import Persistence.TypeOfExpenseRepository;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Márcio
  */
 public class ExpenseRegisterControllerTest {
     
     public ExpenseRegisterControllerTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of registerExpense method, of class ExpenseRegisterController.
      */
     @Test
     public void testRegisterExpense() {
         System.out.println("registerExpense");
         BigDecimal amount = new BigDecimal(200);
         TypeOfExpense type = new TypeOfExpense("Almoço","Descrição");
         Date date = new Date(2013, 04, 19);
         PayMode payM = null;
         String comment = "Almoço de Amigos";
         ExpenseRegisterController instance = new ExpenseRegisterController();
         instance.registerExpense(amount, type, date, payM, comment);
        
        IExpenseRepository repo = PersistenceFactory.buildPersistenceFactory().iexpenseRepository();
 
         String expResult = "Almoço de Amigos";
         String result = repo.getListExpense().get(repo.getListExpense().size() - 1).getDescription();
         assertEquals(expResult, result);
     }
 
    /**
      * Test of getExpenseTypes method, of class ExpenseRegisterController.
      */
     @Test
     public void testGetExpenseTypes() {
         System.out.println("getExpenseTypes");
         ExpenseRegisterController instance = new ExpenseRegisterController();
        TypeOfExpenseRepository repo = PersistenceFactory.buildPersistenceFactory().TypeOfExpenseRepository();
         repo.save(new TypeOfExpense("Lazer","Descrição"));
        List<TypeOfExpense> lista = repo.getListTExpense();
         String expResult = "Lazer";
         String result = lista.get(lista.size()-1).getDescription();
         assertEquals(expResult, result);
  
        
     }
 }
