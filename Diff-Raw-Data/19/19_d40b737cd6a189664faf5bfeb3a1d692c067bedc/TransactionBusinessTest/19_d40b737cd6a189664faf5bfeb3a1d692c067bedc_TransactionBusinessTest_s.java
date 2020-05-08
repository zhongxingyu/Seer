 package br.com.expense.business;
 
 import org.junit.Test;
 
 import br.com.expense.business.TransactionBusiness;
 import br.com.expense.business.TransactionBusinessImpl;
 
 public class TransactionBusinessTest {
 
 	@Test
	public void x() {
 		TransactionBusiness business = new TransactionBusinessImpl();
 		business.process("src/test/resources/");
 	}
 	
 }
