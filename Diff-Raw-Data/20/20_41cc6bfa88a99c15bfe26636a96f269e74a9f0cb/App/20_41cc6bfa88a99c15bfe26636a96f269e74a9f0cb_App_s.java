 package edu.ibs.core;
 
 import edu.ibs.common.enums.AccountRole;
 import edu.ibs.common.enums.Fraction;
 import edu.ibs.core.controller.CSUIDJpaController;
import edu.ibs.core.entity.Account;
import edu.ibs.core.entity.BankBook;
import edu.ibs.core.entity.Currency;
import edu.ibs.core.entity.Money;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * Hello world!
  *
  */
 public class App {
 
 	static {
 		PropertyConfigurator.configure(App.class.getClassLoader().getResource("log4j.properties"));
 	}
 
 	public static void main(String[] args) throws Exception {
 		CSUIDJpaController jpa = new CSUIDJpaController();
		BankBook b = jpa.select(BankBook.class, 1l);
		Account u = new Account("vadim.martos@gmail.com", "asd", AccountRole.USER);
		jpa.insert(u);
		Currency curr = new Currency("USD", 8350f, Fraction.TWO);
		jpa.insert(curr);
		Money money = new Money(100, curr);
		BankBook book = new BankBook(money, null, false);
		jpa.insert(book);
		int i = 0;
 	}
 }
