 package br.com.greenvoip.core;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import br.com.greenvoip.dao.UserDAO;
 import br.com.greenvoip.entity.User;
 import br.com.greenvoip.entity.User.Status;
 
 public class SocketThread implements Runnable {
 
 	private Socket socket;
 	private BufferedReader bufferedReader = null;
 	private PrintWriter printWriter = null;
 
 	private long from = 0;
 	private long to = 0;
 
 	public SocketThread(Socket socket) {
 		this.socket = socket;
 	}
 
 	private boolean init() {
 		try {
 			InputStreamReader inputStreamReader = new InputStreamReader(
 					socket.getInputStream());
 			bufferedReader = new BufferedReader(inputStreamReader);
 			printWriter = new PrintWriter(socket.getOutputStream(), true);
 		} catch (IOException e) {
 			System.err.println("Impossible to read from input and output streams.");
 			e.printStackTrace();
 			return false;
 		}
 
 		if (parseInput() == false) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean parseInput() {
 		String line = "";
 		String[] lineSplited;
 
 		try {
 			line = bufferedReader.readLine();
 
 			if (line != "") {
				lineSplited = line.split(" ");
 				from = Long.valueOf(lineSplited[0]);
 				to = Long.valueOf(lineSplited[1]);
 				return true;
 			} else {
 				System.err.println("Input was empty.");
 				printWriter.println("Input was empty");
 				return false;
 			}
 		} catch (IOException e) {
 			System.err.println("Impossible to read from input stream.");
 			e.printStackTrace();
 			return false;
 		} catch (NumberFormatException e) {
 			System.err.println("Input stream was malformed.");
 			printWriter.println("Input stream was malformed.");
			printWriter.println("Usage:");
			printWriter.println("  <from> <to>");
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	private void closeResources() {
 		try { 
 			bufferedReader.close();
 			printWriter.close();
 			socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void printUserInfo(User user) {
 		printWriter.println();
		printWriter.println("User information: ");
		printWriter.println("  Id: " + user.getId());
		printWriter.println("  Nome: " + user.getName());
		printWriter.println("  Email: " + user.getEmail());
		printWriter.println("  Password: " + user.getPassword());
		printWriter.println("  Status: " + user.getStatus().toString());
		printWriter.println("  Number: " + user.getNumber());
		printWriter.println("  Credit: " + user.getCredit().getCredit());
 		printWriter.println();
 	}
 
 	@Override
 	public void run() {
		if (init() == false) {
			closeResources();
			return;
		}
 
 		UserDAO userDAO = new UserDAO();
 		User user = userDAO.findUserByNumber(from);
 
 		if (user.getCredit().getCredit() > 0
 				&& user.getStatus() == Status.ACTIVE) {
 			System.out.println(from + " -> " + to + " : true");
 			printWriter.println(from + " -> " + to + " : true");
 		} else {
 			System.out.println(from + " -> " + to + " : false");
 			printWriter.println(from + " -> " + to + " : false");
 		}
 
 		printUserInfo(user);
 		closeResources();
 	}
 
 }
