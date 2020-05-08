 package Application;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.net.SocketException;
 import java.security.KeyStore;
 import java.security.UnrecoverableKeyException;
 import java.util.Scanner;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 public class Client {
 	private SSLSocket socket;
 	private PrintWriter out;
 	private BufferedReader in;
 
 	public static void main(String[] args) throws IOException {
 		new Client();
 	}
 
 	public Client() throws IOException {
 		run();
 	}
 
 	private void run() throws IOException {
 		String host = "localhost";
 		Scanner scan = new Scanner(System.in);
 
 		System.setProperty("javax.net.ssl.trustStore", "keys/hca_trusted.jks");
 
 		System.out.println("Enter user/key (e.g socialstyrelsen):");
 		String user = scan.next();
 		System.out.println("Enter password:");
 		char[] passphrase = Shared.readPassword(scan).toCharArray();
 
 		SSLSocketFactory factory = null;
 
 		try {
 			SSLContext ctx;
 			KeyManagerFactory kmf;
 			KeyStore ks;
 
 			ctx = SSLContext.getInstance("TLS");
 			kmf = KeyManagerFactory.getInstance("SunX509");
 			ks = KeyStore.getInstance("JKS");
 
 			try {
 				ks.load(new FileInputStream("keys/" + user + ".jks"),null);
 			} catch (IOException ex) {	
 				System.err.println("Invalid username/key provided. Exiting");
 				return;
 			}
 
 			kmf.init(ks, passphrase);
 			ctx.init(kmf.getKeyManagers(), null, null);
 
 			factory = ctx.getSocketFactory();
 		} catch(UnrecoverableKeyException ex){
 			System.err.println("Invalid password provided. Exiting");
 			return;
 		}
 		catch (Exception e) {
 			System.out.println(e.getClass());
 			throw new IOException(e.getMessage());
 		}
 
 		try {
 			socket = (SSLSocket) factory.createSocket(host, Shared.SERVER_PORT);
 		} catch (ConnectException ex) {
 			System.out.println("Could not connect to server. Reason: "
 					+ ex.getMessage());
 			return;
 		}
 
 		socket.setNeedClientAuth(true);
 
 
 		System.out.print("Connecting to server..  ");
 		socket.startHandshake();
 		
 		String username = Shared.commonNameFrom(socket);
 		
 		if(!username.equals(Shared.SERVER_KEY)){
 			System.err.println("ERROR: Connected to wrong server ("+username+"). Exiting");
 			return;
 		}
 		
 		socket.setKeepAlive(true);
 
 		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
 				socket.getOutputStream())));
 
 		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
 		System.out.println("Connected");
 		String help = "Possible commands:" + "%n\tlist patient|all"
 				+ "%n\tcreate patient doctor nurse division data"
 				+ "%n\tdelete record" + "%n\thelp" + "%n\texit"
				+ "%n\tedit recordID newData";
 
 		try {
 			System.out.printf(help);
 
 			while (!out.checkError()) {
 				System.out.printf("%nhc>");
 				String command = scan.next();
 				Request req = new Request();
 				if (command.equals("list")) {
 					String patient = scan.next();
 					req.action = "list";
 					req.args.add(patient);// all or id
 				} else if (command.equals("create")) {
 					req.action = "create";
 					String patient = scan.next();
 					String doctor = scan.next();
 					String nurse = scan.next();
 					String division = scan.next();
 					String data = scan.next();
 					req.args.add(patient);
 					req.args.add(doctor);
 					req.args.add(nurse);
 					req.args.add(division);
 					req.args.add(data);
 				} else if (command.equals("delete")) {
 					String record = scan.next();
 					req.action = "delete";
 					req.args.add(record);
 				} else if (command.equals("show")){
 					String ID = scan.next();
 					String edit = scan.next();
 					req.action = "edit";
 					req.args.add(ID);
 					req.args.add(edit);
 				} else if (command.equals("help")) {
 					System.out.printf(help);
 				} else if (command.equals("exit")) {
 					break;
 				} else {
 					System.out.println("Error: Unknown command");
 					req.action = "heartbeat";
 				}
 				if (req.action != null) {
 					Response resp = request(req);
 					System.out.printf(resp.message);
 				}
 			}
 		} catch (SocketException ex) {
 			System.err.printf("%nUnexpectedly Disconnected from server");
 		} catch (IOException ex) {
 			System.err.printf("%n" + ex.getMessage());
 		}
 		System.out.printf("%nClosing connection.");
 		socket.close();
 	}
 
 	private Response request(Request req) throws IOException, SocketException {
 		Response response = null;
 		try {
 
 			Gson gson = new Gson();
 			String json = gson.toJson(req);
 
 			out.println("REQUEST");
 			out.println(json.length());
 			out.println(json);
 			out.flush();
 
 			while (!in.readLine().equals("RESPONSE")) {
 				System.err.println("proto error");
 				continue;
 			}
 
 			int length = Integer.parseInt(in.readLine());
 			StringBuilder sb = new StringBuilder();
 			while (length > sb.length()) {
 				sb.append(in.readLine());
 			}
 			response = gson.fromJson(sb.toString(), Response.class);
 
 		} catch (JsonSyntaxException ex) {
 			if (ex.getCause() != null) {
 				throw (SocketException) ex.getCause();
 			} else {
 				throw new IOException("Could not parse server response");
 			}
 		}
 		return response;
 	}
 
 }
