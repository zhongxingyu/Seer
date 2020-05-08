 package br.com.expense.model;
 
 import static br.com.expense.model.Currency.REAL;
 import static br.com.expense.model.TransactionType.CREDIT;
 import static br.com.expense.model.TransactionType.DEBIT;
 import static org.junit.Assert.assertEquals;
 
 import java.math.BigDecimal;
 
 import org.junit.Test;
 
 public class TransactionTest {
 
 	@Test
 	public void mustReturnNegativeValueForDebitTransactions() {
 		Transaction transaction = new Transaction();
 		transaction.setType(DEBIT);
 		transaction.setCurrencyInfo(new CurrencyInfo(new BigDecimal("10"), REAL, new BigDecimal("1")));
 		assertEquals(new BigDecimal("-10.00"), transaction.getValue());
 	}
 	
 	@Test
	public void shouldNotInvertSignForDebitTransactionIfAlreadyNegative() {
		Transaction transaction = new Transaction();
		transaction.setType(DEBIT);
		transaction.setCurrencyInfo(new CurrencyInfo(new BigDecimal("-10"), REAL, new BigDecimal("1")));
		assertEquals(new BigDecimal("-10.00"), transaction.getValue());
	}	
	
	@Test
 	public void mustNotReturnNegativeValueForCreditTransactions() {
 		Transaction transaction = new Transaction();
 		transaction.setType(CREDIT);
 		transaction.setCurrencyInfo(new CurrencyInfo(new BigDecimal("10"), REAL, new BigDecimal("1")));
 		assertEquals(new BigDecimal("10.00"), transaction.getValue());
 	}	
 	
 }
