 package ch.hsr.objectCaching.testFrameworkServer;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.transform.stream.StreamSource;
 
 import ch.hsr.objectCaching.account.Account;
 import ch.hsr.objectCaching.account.AccountImpl;
 import ch.hsr.objectCaching.scenario.Scenario;
 
 public class TestCaseFactory 
 {
 	private TestCase testCase;
 	private ArrayList<Account> accounts;
 	private Logger logger;
 	private String testCaseFileName;
 	
 	public TestCaseFactory(String testCaseFileName)
 	{
 		this.testCaseFileName = testCaseFileName;
 		logger = Logger.getLogger("TestFrameWorkServer");
 		accounts = new ArrayList<Account>();
 	}
 	
 	
 	public ArrayList<Account> getAccounts()
 	{
 		return accounts;
 	}
 	
 	public TestCase getTestCase()
 	{
 		return testCase;
 	}
 	
 	public void convertXML()
 	{
 		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
 	    XMLStreamReader stax;
 	    Account newAccount = null;
 	    Scenario newScenario = null;
 		try {
 			stax = inputFactory.createXMLStreamReader(new StreamSource(new File(testCaseFileName)));
 		      while(stax.hasNext()) 
 		      {
 		    	  stax.next();
 		    	  if(stax.hasName() && stax.getName().toString().equals("TestCase") && stax.isStartElement())
 		    	  {
 		    		  testCase = new TestCase(stax.getAttributeValue(0));
 		    	  }
 		    	  if(stax.hasName() && stax.getName().toString().equals("Account") && stax.isStartElement())
 		    	  {
 		    		  newAccount = new AccountImpl();
		    		  newAccount.setBalance(Double.valueOf(stax.getAttributeValue(0)));
 		    		  accounts.add(newAccount);
 		    	  }
 		    	  
 		    	  if(stax.hasName() && stax.getName().toString().equals("Scenario") && stax.isStartElement())
 		    	  {
 		    		  newScenario = testCase.generateNewScenario(Integer.valueOf(stax.getAttributeValue(0)));
 		    	  }
 		    	  
 		    	  if(stax.hasName() && stax.getName().toString().equals("Write") && stax.isStartElement())
 		    	  {
 		    		 newScenario.setWriteAction(Integer.valueOf(stax.getAttributeValue(0)), Integer.valueOf(stax.getAttributeValue(1)));
 		    	  }
 		    	  if(stax.hasName() && stax.getName().toString().equals("Read") && stax.isStartElement())
 		    	  {
 		    		  newScenario.setReadAction(Integer.valueOf(stax.getAttributeValue(0)));
 		    	  }
 		    	  if(stax.hasName() && stax.getName().toString().equals("Increment") && stax.isStartElement())
 		    	  {
 		    		  newScenario.setIncrementAction(Integer.valueOf(stax.getAttributeValue(0)), Long.valueOf(stax.getAttributeValue(1)), Float.valueOf(stax.getAttributeValue(2)));
 		    	  }
 		      }
 		} catch (XMLStreamException e) {
 			logger.log(Level.SEVERE, "Uncaught exception", e);
 		}  
 	}
 }
