 package br.com.camiloporto.cloudfinance.ui.mobile;
 
 import org.openqa.selenium.support.PageFactory;
 import org.testng.annotations.Test;
 
 import br.com.camiloporto.cloudfinance.ui.mobile.page.RootAccountHomePage;
 import br.com.camiloporto.cloudfinance.ui.mobile.page.TemplatePage;
 import br.com.camiloporto.cloudfinance.ui.mobile.page.TransactionDetailPage;
 import br.com.camiloporto.cloudfinance.ui.mobile.page.TransactionFormPage;
 import br.com.camiloporto.cloudfinance.ui.mobile.page.TransactionHomePage;
 
 public class TransactionWUITest extends AbstractWUITest {
 	
 //	@Override
 //	protected WebDriver newWebDriver() {
 //		return new FirefoxDriver();
 //	}
 	
 	@Test
 	public void shouldShowNewTransactionForm() {
 		loginExistentUser();
 		RootAccountHomePage rootAccountPage = PageFactory.initElements(driver, RootAccountHomePage.class);
 		rootAccountPage.assertIsOnPage();
 		rootAccountPage.selectRootAccount(NEWUSER_GMAIL_COM);
 		
 		goToPath("/transaction/newForm");
 		TransactionFormPage transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		transactionFormPage.assertPageTitle("Nova Transação");
 		transactionFormPage.assertFormElementsArePresent();
 		transactionFormPage.assertOriginAccountsAreListed("Passivos");
 		transactionFormPage.assertDestAccountsAreListed("Despesas");
 	}
 
 
 	
 	@Test
 	public void shouldAddNewTransaction() {
 		loginExistentUser();
 		RootAccountHomePage rootAccountPage = PageFactory.initElements(driver, RootAccountHomePage.class);
 		rootAccountPage.assertIsOnPage();
 		rootAccountPage.selectRootAccount(NEWUSER_GMAIL_COM);
 		
 		//FIXME trabalhar melhor o locale. vide http://www.mkyong.com/spring-mvc/spring-mvc-internationalization-example/
 		goToPath("/transaction/newForm?lang=pt_BR");
 		TransactionFormPage transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		transactionFormPage.fillNewTransaction("Receitas", "Despesas", "28/08/2013", "149,90", "pagamento de INSS").submit();
 
 		TransactionHomePage transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.fillTransactionDateFilter("28/08/2013", "28/08/2013").submitDateFilter();
 		transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.assertTransactionsIsPresent("Receitas", "Despesas", "28/08/2013", "149,90", "pagamento de INSS");
 	}
 	
 	@Test
 	public void shouldShowErrorsOnAddingNewTransaction() {
 		loginExistentUser();
 		goToPath("/transaction/newForm?lang=pt_BR");
 		TransactionFormPage transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		
 		//try to submit empty form. all required field are empty
 		transactionFormPage.submit();
 		
 		transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		transactionFormPage.assertHasErrorMessages();
 	}
 	
 	@Test
 	public void shouldListTransactionsWithinDatePeriodInformed() {
 		loginExistentUser();
 		
 		goToPath("/transaction/newForm?lang=pt_BR");
 		TransactionFormPage transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		transactionFormPage.fillNewTransaction("Receitas", "Despesas", "25/08/2013", "149,90", "pagamento de INSS").submit();
 		
 		goToPath("/transaction/newForm?lang=pt_BR");
 		transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		transactionFormPage.fillNewTransaction("Receitas", "Despesas", "28/08/2013", "319,09", "Feira de Supermercado").submit();
 		
 		TransactionHomePage transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.fillTransactionDateFilter("26/08/2013", "30/08/2013").submitDateFilter();
 		
 		transactionHomePage.setWebDriver(driver);
 		transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.assertTransactionDateFilterEquals("26/08/2013", "30/08/2013");
 		transactionHomePage.assertTransactionsIsPresent("Receitas", "Despesas", "28/08/2013", "319,09", "Feira de Supermercado");
 		transactionHomePage.assertTransactionsIsNotPresent("Receitas", "Despesas", "25/08/2013", "149,90", "pagamento de INSS");
 		
 	}
 	
 	@Test
 	public void shouldShowTransactionDetail() {
 		loginExistentUser();
 		goToPath("/transaction/newForm?lang=pt_BR");
 		TransactionFormPage transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		String date = "28/08/2013";
 		transactionFormPage.fillNewTransaction("Receitas", "Despesas", date, "149,90", "pagamento de INSS").submit();
 
 		TransactionHomePage transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.fillTransactionDateFilter(date, date).submitDateFilter();
 		transactionHomePage.showDetailOfTransaction("Receitas", "Despesas", date, "149,90", "pagamento de INSS");
 		
 		TransactionDetailPage detailPage = PageFactory.initElements(driver, TransactionDetailPage.class);
 		detailPage.assertTransactionsIs("Receitas", "Despesas", date, "149,90", "pagamento de INSS");
 	}
 	
 	@Test
 	public void shouldDeleteTransaction() {
 		createNewTestUser(generateSampleUserLogin(), generateSampleUserPass());
 		goToPath("/transaction/newForm?lang=pt_BR");
 		TransactionFormPage transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		String date28082013 = "28/08/2013";
 		transactionFormPage.fillNewTransaction("Receitas", "Despesas", date28082013, "149,90", "pagamento de INSS").submit();
 
 		TransactionHomePage transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.fillTransactionDateFilter(date28082013, date28082013).submitDateFilter();
 		transactionHomePage.showDetailOfTransaction("Receitas", "Despesas", date28082013, "149,90", "pagamento de INSS");
 		
 		TransactionDetailPage detailPage = PageFactory.initElements(driver, TransactionDetailPage.class);
 		detailPage.deleteTransaction();
 		
 		transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.fillTransactionDateFilter("20/08/2013", "30/08/2013").submitDateFilter();
 		transactionHomePage.assertTransactionsIsNotPresent("Receitas", "Despesas", "25/08/2013", "149,90", "pagamento de INSS");
 	}
 	
 	@Test
 	public void shouldNavigateFromTransactionListToNewTransactionScreen() {
 		loginExistentUser();
 		goToPath("/transaction");
 		TransactionHomePage transactionHomePage = PageFactory.initElements(driver, TransactionHomePage.class);
 		transactionHomePage.clickNewTransactionNavigationLink();
 		
 		TemplatePage transactionFormPage = PageFactory.initElements(driver, TransactionFormPage.class);
 		transactionFormPage.assertIsOnPage();
 		
 	}
 	
 	
 }
