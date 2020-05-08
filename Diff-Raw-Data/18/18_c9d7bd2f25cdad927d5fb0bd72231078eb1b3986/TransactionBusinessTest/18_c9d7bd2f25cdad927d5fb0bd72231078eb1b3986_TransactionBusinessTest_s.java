 package br.com.expense.business;
 
 import org.junit.Test;
 
 import br.com.expense.business.TransactionBusiness;
 import br.com.expense.business.TransactionBusinessImpl;
 
 public class TransactionBusinessTest {
 
 	@Test
 	public void generateReport() {
 		TransactionBusiness business = new TransactionBusinessImpl();
 		business.process("src/test/resources/");
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
	public void shoulBreakIfDirectoryDoesNotExists() {
 		TransactionBusiness business = new TransactionBusinessImpl();
 		business.process("j:/test");
 	}
 	
 }
