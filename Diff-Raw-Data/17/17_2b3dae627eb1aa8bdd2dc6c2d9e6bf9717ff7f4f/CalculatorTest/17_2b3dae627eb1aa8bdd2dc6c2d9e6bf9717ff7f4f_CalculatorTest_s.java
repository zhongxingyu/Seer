 
 /**
  *
  * @author Martin
  */
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Random;
 import multicast.MulticastQueue;
 import org.junit.*;
 import static org.junit.Assert.*;
 import replicated_calculator.Callback;
 import replicated_calculator.ClientNonRobust;
 import replicated_calculator.ClientTUI;
 import week6.ServerReplicated;
 import week6.multicast.ProperConnectorClient;
 
 public class CalculatorTest {
 	private int servers = 3;
 	private int clients = 4;
 	private int port = 1337;
 	private ServerReplicated server[];
 	private ProperConnectorClient client[];
 	private int passesPerClient = 4;
 	
 	@Before
 	public void setup() {
 		server = new ServerReplicated[servers];
 		client = new ProperConnectorClient[clients*servers];
 		
 		for(int i=0; i<servers; i++)
 			server[i] = new ServerReplicated();
 		
 		for(int i=0; i<(clients*servers); i++)
 			client[i] = new ProperConnectorClient();
 		
 		int p = port;
 		server[0].createGroup(port,p+1);	
 		p++;
 		
 		try {
 			for (int i=1; i<servers; i++) {
 				p++;
 				System.out.println("Joining with server " + i + " server port " + p + " and client port " + (p+1));
 				server[i].joinGroup(p, new InetSocketAddress(InetAddress.getLocalHost(), port), p+1);
 				p++;
 				wait(1);
 			}
 
 			p = port;
 			int c = 0;
 			for (int i=0; i<servers; i++) {
 				for (int j=0; j<clients; j++) {
 					System.out.println("Connecting with: " + c + " on server " + i + " server port " + (p+1) + " with client port " + (p+100+c));
 					if(client[c].connect(new InetSocketAddress(InetAddress.getLocalHost(), p+1), p+100+c, Integer.toString(c)))
 						System.out.println("Connected.");
 					else
 						System.out.println("Connection failed.");
 					c++;
 				}
 				p++;
 				p++;
 			}
 		} catch (IOException e) {}
 	}
 	
 	synchronized public static void report(String str) {
 	System.out.println(str);
     }
 	
 	@Test
 	public void doesItWork() {
 		Random r = new Random();
 		String[] vars = {"a","b","c","b","e","f"};
 		int c, var1, var2, var3, type;
 		BigInteger value;
 		
 		for(int i = 0; i < client.length*passesPerClient; i++){
 			var1 = r.nextInt(vars.length);
 			var2 = r.nextInt(vars.length);
 			var3 = r.nextInt(vars.length);
 			type = r.nextInt(2);
 			c = r.nextInt(client.length);
 			System.out.println("Type: " + type);
 			switch(type){
 				// Add
 				case 1:
 					System.out.println("Adding: " + vars[var3] + " = " + vars[var1] + " + " + vars[var2]);
 					client[c].add(vars[var1], vars[var2], vars[var3]);
 					
 					break;
 					
 				
 				// Assign
 				default:
 					value = new BigInteger(8, r);
 					System.out.println("Assigning: " + vars[var1] + " = " + value);
 					client[c].assign(vars[var1], value);
 					break;
 			}
 		}
 		
 		wait(1);
 		
 		for(String s : vars){
 			curr = null;
 			prev = null;
 			for(int i = 0; i < client.length; i++){
 				final String v = s;
 				final String cl = "Client" + i + ": " + s + " = ";
 				client[i].read(s, new Callback<BigInteger>(){
 					public void result(BigInteger bi){
 						set(bi);
 					}
 				});
 				wait(1);
 				System.out.println(cl + curr);
 				
 				if (prev!=null)
 					assertEquals(curr, prev);
 				
 				prev=curr;
 			}
 			wait(1);
 		}
 	}
 	
 	public void set(BigInteger bi) {
 		curr = bi;
 	}
 	
 	private BigInteger curr, prev;
 	
 	public static void wait(int secs) {
 		try {
			Thread.currentThread().sleep(secs*200);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 }
