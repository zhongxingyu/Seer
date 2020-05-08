 package exception;
 
 import static com.googlecode.catchexception.CatchException.*;
 import static org.assertj.core.api.Assertions.assertThat;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class DepositTest {
 
     DepositService depositService;
 
     @Before
     public void setUp() throws Exception {
         depositService = new DepositService();
     }
 
     @Test
     public void deposit_below_minimum_amount_is_not_allowed() throws Exception {
         depositService.setMinimumDepositAmount(5.00);
 
         try {
             depositService.deposit(4.99);
         } catch (DepositService.InvalidDepositAmountException error) {
             //expected InvalidDepositAmountException
         }
     }
 
     @Ignore
     @Test(expected = DepositService.InvalidDepositAmountException.class)
     public void deposit_below_minimum_amount_is_not_allowed_v2() throws Exception {
         depositService.setMinimumDepositAmount(5.00);
 
         depositService.deposit(4.99);
 
         //exception expected
     }
 
     @Ignore
     @Test
     public void deposit_below_minimum_amount_is_not_allowed_v3() throws Exception {
         depositService.setMinimumDepositAmount(5.00);
 
         catchException(depositService).deposit(4.99);
 
        // assertThat(xxx).isExactlyInstanceOf(DepositService.InvalidDepositAmountException.class);
     }
 }
