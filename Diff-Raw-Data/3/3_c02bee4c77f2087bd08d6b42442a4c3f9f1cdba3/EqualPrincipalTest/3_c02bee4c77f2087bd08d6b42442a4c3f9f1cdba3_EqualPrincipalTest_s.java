 import java.math.BigDecimal;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class EqualPrincipalTest {
 
 	@Test
 	public void should_pay_back_proper_amount_money_when_borrow_120k_with_rate_4_86_in_10_years() {
 		// setup
 		int termInMonth = 10 * 12;
		BigDecimal totalAmountOfLoan = new BigDecimal(100000);
//		BigDecimal totalAmountOfLoan = new BigDecimal(120000);
 		BigDecimal yearRate = new BigDecimal("0.0486");
 		EqualPrincipal actualEqualPrincipal = new EqualPrincipal(
 				totalAmountOfLoan, termInMonth, yearRate);
 
 		// exercise
 		RepayPlan shouldPayAmount = actualEqualPrincipal.calculateRepayPlan();
 
 		// verify
 		BigDecimal actualTotalAmount = shouldPayAmount.getTotalAmount();
 
 		Assert.assertTrue("total amount: " + actualTotalAmount, new BigDecimal(
 				"149403.00").compareTo(actualTotalAmount) == 0);
 	}
 
 	@Test
 	public void should_pay_back_proper_amount_money_when_borrow_120k_with_rate_4_86_in_20_years() {
 		// setup
 		int termInMonth = 20 * 12;
 		BigDecimal totalAmountOfLoan = new BigDecimal(120000);
 		BigDecimal rate = new BigDecimal("0.0486");
 		EqualPrincipal actualEqualPrincipal = new EqualPrincipal(
 				totalAmountOfLoan, termInMonth, rate);
 
 		// exercise
 		RepayPlan shouldPayAmount = actualEqualPrincipal.calculateRepayPlan();
 
 		// verify
 		BigDecimal actualTotalAmount = shouldPayAmount.getTotalAmount();
 		Assert.assertTrue("total amount: " + actualTotalAmount, new BigDecimal(
 				"178563.00").compareTo(actualTotalAmount) == 0);
 	}
 
 	@Test
 	public void verifyMonthlyPayments() {
 		// setup
 		int termInMonth = 6;
 		BigDecimal totalAmountOfLoan = new BigDecimal(120000);
 		BigDecimal rate = new BigDecimal("0.0486");
 		EqualPrincipal actualEqualPrincipal = new EqualPrincipal(
 				totalAmountOfLoan, termInMonth, rate);
 
 		// exercise
 		RepayPlan shouldPayAmount = actualEqualPrincipal.calculateRepayPlan();
 
 		// verify
 		List<BigDecimal> actualTotalAmount = shouldPayAmount
 				.getMonthlyPayments();
 		Assert.assertTrue(new BigDecimal("20486.00")
 				.compareTo(actualTotalAmount.get(0)) == 0);
 
 	}
 }
