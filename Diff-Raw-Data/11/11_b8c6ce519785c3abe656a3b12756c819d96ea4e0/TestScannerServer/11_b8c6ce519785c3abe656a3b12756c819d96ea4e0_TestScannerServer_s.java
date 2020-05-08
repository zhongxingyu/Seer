 package fi.helsinki.cs.tmc.testscanner;
 
 import com.google.gson.Gson;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * Provides the services of <code>TestScanner</code> as a continually running server.
  * 
  * <p>
  * The purpose of this is to speed up the test suite.
  * 
  * <p>
  * When the server starts, it opens a local TCP server socket and
  * prints the port number to stdout. It then awaits connections
  * until a connection sends the message "SHUTDOWN!".
  * Each connection should send a list of path names
  * to scan for test methods, followed by an empty line.
  * The server then sends back the same kind of JSON array as
  * <code>TestScanner</code> and closes the connection.
  */
 public class TestScannerServer {
 	public static void main(String[] args) throws IOException {
 		ServerSocket server = createListeningServerSocket();
 		TestScanner testScanner = new TestScanner();
 		
 		boolean shouldContinue = true;
 		while (shouldContinue) {
 			Socket sock = server.accept();
 			shouldContinue &= handleRequest(sock, testScanner);
 			testScanner.clearSources();
 		}
 	}
 	
 	private static ServerSocket createListeningServerSocket() throws IOException {
 		ServerSocket server = new ServerSocket();
 		server.bind(new InetSocketAddress("localhost", 0));
 		System.out.println(server.getLocalPort());
 		System.out.close();
 		return server;
 	}
 	
 	private static boolean handleRequest(Socket sock, TestScanner testScanner) throws IOException {
 		ArrayList<String> lines = readInput(sock);
 		sock.shutdownInput();
 		
 		if (lines.contains("SHUTDOWN!")) {
 			return false;
 		}
 		
 		for (String line : lines) {
 			testScanner.addSource(new File(line));
 		}
 		
		List<TestMethod> testMethods = testScanner.findTests();
 		
 		writeOutput(sock, testMethods);
 		
 		sock.close();
 		
 		return true;
 	}
 	
 	private static ArrayList<String> readInput(Socket sock) throws IOException {
 		Scanner inputScanner = new Scanner(sock.getInputStream(), "UTF-8");
 		ArrayList<String> lines = new ArrayList<String>();
 		while (inputScanner.hasNextLine()) {
 			String line = inputScanner.nextLine();
 			if (line.isEmpty()) {
 				break;
 			} else {
 				lines.add(line);
 			}
 		}
 		return lines;
 	}
 
 	private static void writeOutput(Socket sock, List<TestMethod> testMethods) throws IOException {
 		Writer writer = new OutputStreamWriter(sock.getOutputStream(), "UTF-8");
 		String json = new Gson().toJson(testMethods);
 		writer.write(json);
 		writer.flush();
 	}
 }
