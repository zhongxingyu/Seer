 package ch.hsr.objectCaching.testFrameworkServer;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 
 import ch.hsr.objectCaching.interfaces.Account;
 import ch.hsr.objectCaching.interfaces.AccountImpl;
 import ch.hsr.objectCaching.interfaces.Action;
 import ch.hsr.objectCaching.interfaces.ClientInterface;
 import ch.hsr.objectCaching.interfaces.Configuration;
 import ch.hsr.objectCaching.interfaces.ReadAction;
 import ch.hsr.objectCaching.interfaces.Scenario;
 import ch.hsr.objectCaching.interfaces.ServerInterface;
 import ch.hsr.objectCaching.interfaces.WriteAction;
 import ch.hsr.objectCaching.testFrameworkServer.Client.Status;
 
 public class Server implements ServerInterface
 {
 	private ClientList clientList;
 	private ArrayList<TestCase> testCases;
 	private Dispatcher dispatcher;
 	private TestCase activeTestCase;
 	private TestCaseFactory factory;
 	private Configuration configuration;
 	private Account account;
 	private ConfigurationFactory configFactory;
 	
 	public Server()
 	{
 		configFactory = new ConfigurationFactory();
 		clientList = configFactory.getClientList();
 		configuration = configFactory.getConfiguration();
 		generateTestCases();
 		establishClientConnection();
 		createRmiRegistry();
 		dispatcher = new Dispatcher(configuration.getServerSocketPort());
 		account = new AccountImpl();
 		new Thread(dispatcher).start();
 	}
 	
 	private void generateTestCases()
 	{
 		factory = new TestCaseFactory();
 		factory.convertXML();
 		testCases = factory.getTestCases();
 		activeTestCase = testCases.get(0);
 		configuration.setNameOfSystemUnderTest(activeTestCase.getSystemUnderTest());
 	}
 	
 	private void startTestCase()
 	{
 		System.out.println("Starting TestCase");
 		dispatcher.setSystemUnderTest(activeTestCase.getSystemUnderTest(), account);
 		initializeClients();
 	}
 	
 	private void initializeClients()
 	{
 		System.out.println("initializeClients");
 		Scenario temp;
 		for(int i = 0; i < clientList.size(); i++)
 		{
 			if((temp = activeTestCase.getScenarios().get(i)) != null)
 			{
 				try {
 					clientList.getClient(i).getClientStub().initialize(temp, configuration);
 				} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				try {
 					clientList.getClient(i).getClientStub().initialize(activeTestCase.getScenario(0), configuration);
 				} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private void establishClientConnection() 
 	{
 		System.out.println("establishClientConnection");
 		try {
 			for(int i = 0; i < clientList.size(); i++)
 			{
 				System.out.println(clientList.getClient(i).getIp());
 				ClientInterface clientStub = (ClientInterface)Naming.lookup("rmi://" + clientList.getClient(i).getIp() + ":" + configuration.getClientRmiPort() + "/Client");
 				clientList.getClient(i).setClientStub(clientStub);
 			}
 			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void setReady(String ip) 
 	{
 		System.out.println("Setted ready with: " + ip);
 		Client temp;
 		if((temp = clientList.getClientByIp(ip)) != null)
 		{
 			temp.setStatus(Status.READY);
 		}
 		if(checkAllReady())
 		{
 			start();
 		}
 	}
 	
 	public int getSocketPort()
 	{
 		return configuration.getServerSocketPort();
 	}
 	
 	private void start()
 	{
 		System.out.println("start");
 		for(int i = 0; i < clientList.size(); i++)
 		{
 			try {
 				clientList.getClient(i).getClientStub().startTest();
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private boolean checkAllReady()
 	{
 		for(int i = 0; i < clientList.size(); i++)
 		{
 			if(clientList.getClient(i).getStatus() == Status.NOTREADY)
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 		
 	private void createRmiRegistry()
 	{
 		try {
 			LocateRegistry.createRegistry(configuration.getServerRMIPort());
 			ServerInterface skeleton = (ServerInterface) UnicastRemoteObject.exportObject(this, configuration.getServerRMIPort());
 			Registry reg = LocateRegistry.getRegistry(configuration.getServerRMIPort());
 			reg.rebind(configuration.getServerRegistryName(), skeleton);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	
 	@Override
 	public void setResults(Scenario scenario, String clientIp) 
 	{
 		//TODO: Auswertung der ankommenden Resultate
 		System.out.println("results setted");
 		System.out.println(scenario.getId());
 		for(int i = 0; i < scenario.getActionList().size(); i++)
 		{
 			Action action = scenario.getActionList().get(i);
 			if(action instanceof WriteAction)
 			{
 				System.out.println("Action was a Write-Action with: " + ((WriteAction)action).getValue());
 			}
 			
 			if(action instanceof ReadAction)
 			{
 				System.out.println("Action was a Read-Action with: " + ((ReadAction)action).getBalance());
 			}
 		}
 //		ReportGenerator report = new ReportGenerator();
 //		report.addScenario(scenario);
 //		report.makeSummary();
 		
 		for(int i = 0; i < testCases.size(); i++)
 		{
			if(testCases.get(i).equals(activeTestCase) && testCases.get(i + 1) != null)
 			{
 				activeTestCase = testCases.get(i + 1);
 				startTestCase();
 			}
 			else
 			{
 				stopClient(clientIp);
 			}
 		
 		}
 	}
 	
 	private void stopClient(String clientIp)
 	{
 		Client temp;
 		try {
 			
 			if((temp = clientList.getClientByIp(clientIp)) != null)
 			{
 				System.out.println("Stop Client with " + clientIp);
 				temp.getClientStub().shutdown();
 			}
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static void main(String[] args) 
 	{
 		Server myServer = new Server();
 		myServer.startTestCase();
 	}
 }
