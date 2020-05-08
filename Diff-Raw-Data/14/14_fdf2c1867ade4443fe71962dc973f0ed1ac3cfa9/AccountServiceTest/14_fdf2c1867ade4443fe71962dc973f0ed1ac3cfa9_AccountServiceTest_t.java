 package se.liu.tdp024.web.test;
 
 import com.google.gson.*;
 import com.google.gson.reflect.TypeToken;
 import java.lang.reflect.Type;
 import java.util.List;
 import javax.ws.rs.core.Response;
 import org.junit.*;
 import se.liu.tdp024.entity.Account;
 import se.liu.tdp024.entity.Account;
 import se.liu.tdp024.logic.bean.AccountBean;
 import se.liu.tdp024.logic.util.HTTPHelper;
 import se.liu.tdp024.util.EMF;
 import se.liu.tdp024.util.Monlog;
 import se.liu.tdp024.web.service.AccountService;
 
 public class AccountServiceTest {
     private static String ExistingBankKey = "";
     private static String ExistingPersonKey = "";
 
     public AccountServiceTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         Monlog.LoggingOn = false;
 
         String PersonAPI_URL = "http://enterprise-systems.appspot.com/person/";
         String BankAPI_URL =   "http://enterprise-systems.appspot.com/bank/";
 
         JsonParser jp = new JsonParser();
         JsonObject jo;
         JsonElement je;
 
         // Get an existing Person for testing
         String resp = HTTPHelper.get(PersonAPI_URL + "find.name", "name", "Zorro");
         Assert.assertFalse("Couldn't connect to PersonAPI", resp == null);
         je = jp.parse(resp); Assert.assertTrue(je.isJsonObject());
         jo = je.getAsJsonObject();
         Assert.assertTrue("Person \"Zorro\" not found", jo.isJsonObject());
         ExistingPersonKey = jo.get("key").getAsString();
 
         // Get an existing Bank for testing
         resp = HTTPHelper.get(BankAPI_URL + "find.name", "name", "SWEDBANK");
         Assert.assertFalse("Couldn't connect to BankAPI", resp == null);
         je = jp.parse(resp); Assert.assertTrue(je.isJsonObject());
         jo = je.getAsJsonObject();
         Assert.assertTrue("Bank \"SWEDBANK\" not found", jo.isJsonObject());
         ExistingBankKey = jo.get("key").getAsString();
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
         Monlog.LoggingOn = true;
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
         EMF.close();
     }
 
     @Test
     public void testCreate() {
         AccountService service = new AccountService();
         Response response = service.createAccount(ExistingPersonKey,
                                                   ExistingBankKey,
                                                   Account.SALARY);
         Assert.assertEquals(200, response.getStatus());
         Object entity = response.getEntity();
         Account account = new Gson().fromJson((String)entity, Account.class);
         Assert.assertEquals(ExistingPersonKey, account.getPersonKey());
     }
 
     @Test
     public void testCreateFailure() {
         AccountService service = new AccountService();
         Response response = service.createAccount("",
                                                   "",
                                                   Account.SALARY);
         Assert.assertEquals(500, response.getStatus());
     }
 
     @Test
     public void testListByPersonID() {
         AccountBean.create(Account.SALARY, ExistingPersonKey, ExistingBankKey);
         AccountBean.create(Account.SALARY, ExistingPersonKey, ExistingBankKey);
         AccountService service = new AccountService();
 
         Response response = service.listByPersonKey(ExistingPersonKey);
         Assert.assertEquals(200, response.getStatus());
 
         Object entity = response.getEntity();
         Type listType = new TypeToken<List<Account>>(){}.getType();
         List<Account> accounts = new Gson().fromJson((String)entity, listType);
         Assert.assertEquals(2, accounts.size());
     }
 
     @Test
     public void testListByBankID() {
         AccountBean.create(Account.SALARY, ExistingPersonKey, ExistingBankKey);
         AccountBean.create(Account.SALARY, ExistingPersonKey, ExistingBankKey);
         AccountService service = new AccountService();
 
         Response response = service.listByBankKey(ExistingPersonKey);
         Assert.assertEquals(200, response.getStatus());
 
         Object entity = response.getEntity();
         Type listType = new TypeToken<List<Account>>(){}.getType();
         List<Account> accounts = new Gson().fromJson((String)entity, listType);
         Assert.assertEquals(2, accounts.size());
     }
 
     @Test
     public void testListByPersonIDFailure() {
         AccountService service = new AccountService();
         Response response = service.listByPersonKey("");
         Assert.assertEquals(200, response.getStatus());
 
         Object entity = response.getEntity();
         Type listType = new TypeToken<List<Account>>(){}.getType();
         List<Account> accounts = new Gson().fromJson((String)entity, listType);
         Assert.assertEquals(0, accounts.size());
     }
 
     @Test
     public void testListByBankIDFailure() {
         AccountService service = new AccountService();
         Response response = service.listByBankKey("");
         Assert.assertEquals(200, response.getStatus());
 
         Object entity = response.getEntity();
         Type listType = new TypeToken<List<Account>>(){}.getType();
         List<Account> accounts = new Gson().fromJson((String)entity, listType);
         Assert.assertEquals(0, accounts.size());
     }
 
     @Test
     public void testWithdraw() {
         AccountService as = new AccountService();
         long testAccount = AccountBean.create(Account.SALARY,
                ExistingPersonKey, ExistingBankKey).getAccountNumber();
 
         // Withdraw non-existing money
         Response response = as.withdraw(testAccount, 100);
         Assert.assertEquals(500, response.getStatus());
 
         // Deposit cash for testing purposes and try a valid withdraw
         Assert.assertTrue(AccountBean.depositCash(testAccount, 200));
 
         // Verify money transfered
         Assert.assertEquals(200, AccountBean.balance(testAccount));
 
         response = as.withdraw(testAccount, 80);
         Assert.assertEquals(200, response.getStatus());
 
         // Try to withdraw negative amount
         response = as.withdraw(testAccount, -50);
         Assert.assertEquals(500,response.getStatus());
 
         // Try to withdraw from nonexisting account.
         response = as.withdraw(999, 100);
         Assert.assertEquals(500,response.getStatus());
     }
 
     @Test
     public void testDeposit() {
         // Set up test-account
         AccountService as = new AccountService();
         long testAccount = AccountBean.create(Account.SALARY,
                ExistingPersonKey, ExistingBankKey).getAccountNumber();
 
         // Test valid deposit
         Response response = as.deposit(testAccount, 100);
         Assert.assertEquals(200, response.getStatus());
 
         // Test invalid amount (amount to large)
         response = as.deposit(testAccount, Long.MAX_VALUE);
         Assert.assertEquals(500, response.getStatus());
 
         // Test invalid amount (negative)
         response = as.deposit(testAccount, -50);
         Assert.assertEquals(500, response.getStatus());
 
         // Deposit
         response = as.deposit(999, 100);
         Assert.assertEquals(500, response.getStatus());
     }
 
     @Test
     public void testTransfer() {
         // Set up test-accounts
         AccountService as = new AccountService();
         long recieverAcc = AccountBean.create(Account.SALARY,
                ExistingPersonKey, ExistingBankKey).getAccountNumber();
 
         long senderAcc = AccountBean.create(Account.SAVINGS,
                ExistingPersonKey, ExistingBankKey).getAccountNumber();
 
         Response response = as.deposit(senderAcc, 100);
         Assert.assertEquals(200, response.getStatus());
 
         response = as.transfer(senderAcc, recieverAcc, 20);
         Assert.assertEquals(200, response.getStatus());
 
         // Verify money transfered
         Assert.assertEquals(20, AccountBean.balance(recieverAcc));
 
         // Test invalid transfer (not enough money)
         response = as.transfer(senderAcc, recieverAcc, 200);
         Assert.assertEquals(500, response.getStatus());
 
         // Test invalid transfer (negative amount)
         response = as.transfer(senderAcc, recieverAcc, -20);
         Assert.assertEquals(500, response.getStatus());
 
         // Test invalid transfer (unknown sender)
         response = as.transfer(999, recieverAcc, 20);
         Assert.assertEquals(500, response.getStatus());
 
         // Test invalid transfer (unknown reciever)
         response = as.transfer(senderAcc, 999, 20);
         Assert.assertEquals(500, response.getStatus());
     }
 }
