 package paskma.main;
 
 import pyftpclient_layer.CClient;
 import pyftpclient_layer.CFtpFile;
 import pyftpclient_layer.CFileStream;
 
 import java.io.ByteArrayOutputStream;
 
 /**
  * User: paskma
  * Date: Oct 10, 2011
  * Time: 12:40:19 PM
  */
 public class Main {
 	private static void p(Object o) {
 		System.out.println(o);
 	}
 	
 	public static void main(String[] args) {
 		if (args.length != 1) {
 			p("No task given.");
 			p(" General basic experiments.");
 			p("  --test      (simple interaction with polite built-in server)");
 			p("  --wild      (simple interaction with real public FTP server; into the wild!)");
 			p("");
 			p(" The confirmation bug experiments.");
 			p(" The server fails to confirm data transfer, returns 500 instead of 255, or it returns random response.");
			p(" If the client contains a bug, th state machine does not reset state after the failed transaction.");
 			p("  --confirm-fail      (interaction with failing built-in server (bug-free client))");
 			p("  --confirm-fail-cbug (interaction with failing built-in server (client bug))");
 			p("  --confirm-rand      (interaction with built-in randomized server (bug-free client))");
 			p("  --confirm-rand-cbug (integration with built-in randomized server (client bug))");
 			p("  --confirm-rand1000           (1000 experiments as above)");
 			p("  --confirm-rand-cbug1000      (1000 experiments as above)");
 			p("");
 			p(" The pasv response reading bug experiments.");
 			p("  --pasv-rand");
 			p("  --pasv-rand-cbug");
 			p("  --pasv-rand1000");
 			p("  --pasv-rand-cbug1000");
 			return;
 		}
 		
 		String arg = args[0];
 		
 		if (arg.equals("--test")) {
 			demoTestNetwork();
 		} else if (arg.equals("--wild")) {
 			demoWild();
 		} else if (arg.equals("--confirm-fail")) {
 			demoFail(false);
 		} else if (arg.equals("--confirm-fail-cbug")) {
 			demoFail(true);
 		} else if (arg.equals("--confirm-rand")) {
 			demoRand(false);
 		} else if (arg.equals("--confirm-rand-cbug")) {
 			demoRand(true);
 		} else if (arg.equals("--confirm-rand1000")) {
 			demoRand(false, 1000);
 		} else if (arg.equals("--confirm-rand-cbug1000")) {
 			demoRand(true, 1000);
 		} else if (arg.equals("--pasv-rand")) {
 			demoPasvRand(false);
 		} else if (arg.equals("--pasv-rand-cbug")) {
 			demoPasvRand(true);
 		} else if (arg.equals("--pasv-rand1000")) {
 			demoPasvRand(false, 1000);
 		} else if (arg.equals("--pasv-rand-cbug1000")) {
 			demoPasvRand(true, 1000);
 		} else {
 			p("Option not recognized: " + arg);
 		}
 	}
 	
 	/**
 	 * Demonstration of client working agains a built-in server that fails
 	 * to confirm a data transfer (however it does not break the protocol).
 	 * 
 	 * If the client contains a bug the data transfer seems to go well (which is wrong)
 	 * and the client stucks in wrong state. An exception is raised upon next operation.
 	 * 
 	 * If the client is bug-free then it figures out that the data transfer failed
 	 * and returns null. State transitions are ok.
 	 */ 
 	private static void demoFail(boolean clientDataConfirmationBug) {
 		if (clientDataConfirmationBug)
 			p("C:Server fails, client raises exception due to a bug");
 		else
 			p("C:Server fails, client handles that gracefully");
 			
 		CClient client = new CClient(CClient.NET_TEST_FAIL);
 		client.setDataTransferConfirmationBug(clientDataConfirmationBug);
 		client.connect("ignored", 21);
 		client.login("anonymous", "osgiftp@kiv.zcu.cz");
 		
 		byte[] f = client.retrieveFile("xx");
 		if (f != null)
 			p("C:File is:\n"+ new String(f));
 		else
 			p("C:File transfer failed.");
 
 		p("C:##Second shot...");
 		
 		f = client.retrieveFile("xx");
 		if (f != null)
 			p("C:File(2) is:\n"+ new String(f));
 		else
 			p("C:File(2) transfer failed.");
 		
 		client.logout();
 		
 	}
 	
 	/**
 	 * Call demoRand(boolean) in loop, to prove or disprove a bug.
 	 * 
 	 * If the client contains a bug it is usually found in about 200 cycles.
 	 * If the client is bug-free all cycles are perfomed without an exception.
 	 */
 	private static void demoRand(boolean clientDataConfirmationBug, int loops) {
 		int i = 0;
 		try {
 			for (i = 0; i < loops; i++) {
 				demoRand(clientDataConfirmationBug);
 				//exception will break the loop
 			}
 			p("All loops performed " + loops);
 		} catch (Throwable ex) {
 			ex.printStackTrace();
 			p("Exception found in loop " + i);
 		}
 	}
 	
 	private static void demoPasvRand(boolean clientDataConfirmationBug, int loops) {
 		int i = 0;
 		try {
 			for (i = 0; i < loops; i++) {
 				demoPasvRand(clientDataConfirmationBug);
 				//exception will break the loop
 			}
 			p("All loops performed " + loops);
 		} catch (Throwable ex) {
 			ex.printStackTrace();
 			p("Exception found in loop " + i);
 		}
 	}
 	
 	/**
 	 * Demonstration of client working agains a built-in server that can respond
 	 * an random code to every command.
 	 * 
 	 * You get different results upon each call.
 	 * 
 	 * If the client is bug-free it never raises an exception.
 	 */
 	private static void demoRand(boolean clientDataConfirmationBug) {
 		if (clientDataConfirmationBug)
 			p("C:Server with random behavior, client might raises exception due to a bug");
 		else
 			p("C:Server with random behavior, client handles that gracefully");
 			
 		CClient client = new CClient(CClient.NET_CODE_RAND);
 		client.setDataTransferConfirmationBug(clientDataConfirmationBug);
 
 		boolean suc = client.connect("ignored", 21);
 		if (!suc) {
 			p("C:Can not connect, end.");
 			return;
 		}
 		suc = client.login("anonymous", "osgiftp@kiv.zcu.cz");
 		if (!suc) {
 			p("C:Can not log in, end.");
 			return;
 		}
 		
 		byte[] f = client.retrieveFile("xx");
 		if (f != null)
 			p("C:File is:\n"+ new String(f));
 		else
 			p("C:File transfer failed.");
 
 		p("C:##Second shot...");
 		
 		f = client.retrieveFile("xx");
 		if (f != null)
 			p("C:File(2) is:\n"+ new String(f));
 		else
 			p("C:File(2) transfer failed.");
 		
 		suc = client.logout();
 		if (!suc) {
 			p("C:Can not logout.");
 			return;
 		}
 	}
 	
 	private static void demoPasvRand(boolean clientPasvResponseReadingBug) {
 		if (clientPasvResponseReadingBug)
 			p("C:Server with random behavior, client might raises exception due to a bug");
 		else
 			p("C:Server with random behavior, client handles that gracefully");
 			
 		CClient client = new CClient(CClient.NET_CODE_RAND);
 		client.setPasvResponseReadingBug(clientPasvResponseReadingBug);
 
 		boolean suc = client.connect("ignored", 21);
 		if (!suc) {
 			p("C:Can not connect, end.");
 			return;
 		}
 		suc = client.login("anonymous", "osgiftp@kiv.zcu.cz");
 		if (!suc) {
 			p("C:Can not log in, end.");
 			return;
 		}
 		
 		byte[] f = client.retrieveFile("xx");
 		if (f != null)
 			p("C:File is:\n"+ new String(f));
 		else
 			p("C:File transfer failed.");
 
 		p("C:##Second shot...");
 		
 		f = client.retrieveFile("xx");
 		if (f != null)
 			p("C:File(2) is:\n"+ new String(f));
 		else
 			p("C:File(2) transfer failed.");
 		
 		suc = client.logout();
 		if (!suc) {
 			p("C:Can not logout.");
 			return;
 		}
 	}
 	
 	/**
 	 * Simple happy-day scenario with built-in server.
 	 */
 	private static void demoTestNetwork() {
 		p("TestNetwork demo");
 		CClient client = new CClient(CClient.NET_TEST);
 		client.connect("ignored", 21);
 		client.login("anonymous", "osgiftp@kiv.zcu.cz");
 		
 		CFtpFile lst[];
 		lst = client.listFiles();
 
 		for (CFtpFile i : lst) {
 			System.out.println(i);
 		}
 		
 		byte[] contentXX = client.retrieveFile("xx");
 		p("XX:'" +  (new String(contentXX)) + "'");
 		client.logout();
 	}
 
 	/**
 	 * Demonstrates an interaction with real FTP server.
 	 */
 	private static void demoWild() {
 		String host;
 		host = "ftp.zcu.cz";
 		host = "ftp.gnu.org";
 		CClient client = new CClient();
 		client.connect(host, 21);
 		client.login("anonymous", "osgiftp@kiv.zcu.cz");
 		
 		CFtpFile lst[];
 		lst = client.listFiles();
 
 		for (CFtpFile i : lst) {
 			System.out.println(i);
 		}
 		
 		client.changeWorkingDirectory("pub");
 
 		lst = client.listFiles();
 
 		for (CFtpFile i : lst) {
 			System.out.println(i);
 		}
 
 		byte[] welcome = client.retrieveFile("welcome.msg");
 		if (welcome != null)
 			System.out.println(new String(welcome));
 
 		CFileStream stream = client.retrieveFileStream("welcome.msg");
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 		int c;
 		while ((c = stream.read()) != -1) {
 			buffer.write((byte)c);
 			break;
 		}
 		stream.close();
 		System.out.println("From stream:");
 		System.out.println(new String(buffer.toByteArray()));
 
 
 		System.out.println("Still healthy?");
 		welcome = client.retrieveFile("welcome.msg");
 		
 		System.out.println("Desperate delete");
 		client.deleteFile("mythical_file.docxxx");
 		
 		client.logout();
 		System.out.println("Done.");
 	}
 }
