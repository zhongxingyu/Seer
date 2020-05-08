 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controllers;
 
 
import Model.Expense;
import Model.ExpenseType;
import Model.PaymentMean;
import Persistence.ExpenseTypeRepository;
 import eapli.exception.EmptyList;
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
  * @author nbento
  */
 public class RegisterExpenseControllerTest {
     
     public RegisterExpenseControllerTest() {
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
      * Teste verificar lista ExpenseType vazia
      */
     @Test(expected = eapli.exception.EmptyList.class)
     public void testGetExpenseTypeException() throws EmptyList{
         System.out.println("getExpenseType - Exception EmptyList");
         RegisterExpenseController controller = new RegisterExpenseController();
         controller.getExpenseType();
     }
 
     /**
      * Teste verificar lista PaymentMean vazia
      */
     @Test(expected = eapli.exception.EmptyList.class)
     public void testGetPaymentMeanException() throws EmptyList{
         System.out.println("getPaymentMean - Exception EmptyList");
         RegisterExpenseController controller = new RegisterExpenseController();
         controller.getPaymentMean();
     }
     
     /**
      * Teste verifica se o obj quando null lança exception
      */
     @Test(expected = IllegalArgumentException.class)
     public void testCreateExpense() {
         System.out.println("createExpense - Exception NullPointerException");
         RegisterExpenseController controller = new RegisterExpenseController();
         controller.createExpense(null, null, null, null, null);
     }
 }
