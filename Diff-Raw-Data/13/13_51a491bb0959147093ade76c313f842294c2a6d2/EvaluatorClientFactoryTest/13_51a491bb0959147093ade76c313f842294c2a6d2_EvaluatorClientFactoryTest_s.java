 package br.ufmg.dcc.vod.ncrawler.distributed.client;
 
 import static org.junit.Assert.assertTrue;
 
 import java.net.MalformedURLException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClient;
 import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClientFactory;
 
 public class EvaluatorClientFactoryTest {
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testAll() throws RemoteException, AlreadyBoundException, MalformedURLException, NotBoundException {
 		EvaluatorClientFactory f = new EvaluatorClientFactory(9090);
 		
 		String[] list = Naming.list("rmi://localhost:9090");
 		System.out.println(Arrays.toString(list));
 		
 		assertTrue(list.length == 0);
 		f.createAndBind();
 
 		list = Naming.list("rmi://localhost:9090");
 		
 		assertTrue(list.length == 1);
 		assertTrue(list[0].contains(EvaluatorClient.NAME));
 		System.out.println(Arrays.toString(list));
 		
 		f.shutdown();
		
		list = Naming.list("rmi://localhost:9090");
		
		assertTrue(list.length == 0);
 	}
 	
 }
