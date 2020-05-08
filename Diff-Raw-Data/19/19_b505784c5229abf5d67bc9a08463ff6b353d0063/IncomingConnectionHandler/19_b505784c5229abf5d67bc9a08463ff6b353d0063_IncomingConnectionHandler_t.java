 package nl.groep5.xchange.externalInput;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.RandomAccessFile;
 import java.net.Socket;
 
 import nl.groep5.xchange.Settings;
 
 public class IncomingConnectionHandler extends Thread {
 
 	private static final String SEARCH_COMMAND = "SEARCH ";
 	private static final String GET_COMMAND = "GET ";
 	private Socket client;
 	private BufferedReader bufferedReader;
 	private PrintWriter printWriter;
 	private BufferedOutputStream bufferedOutputStream;
 
 	public IncomingConnectionHandler(Socket client) {
 		this.client = client;
 	}
 
 	@Override
 	public void run() {
 		try {
 			handle(client);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * handle incoming messages all messages are strings ending on "\n"
 	 */
 	private void handle(Socket socket) throws Exception {
 		System.out.println("New incoming connection from "
 				+ socket.getInetAddress().getHostAddress());
 
 		bufferedReader = new BufferedReader(new InputStreamReader(
 				socket.getInputStream()));
 
 		bufferedOutputStream = new BufferedOutputStream(
 				socket.getOutputStream(), Settings.getBlockSize());
 
 		printWriter = new PrintWriter(socket.getOutputStream(), true);
 
 		String line = null;
 		do {
 			line = bufferedReader.readLine();
 		} while (line == null);
 
 		System.out.println("command from client " + line);
 
 		if (line.startsWith(SEARCH_COMMAND)) {
 			handleSearch(line.substring(SEARCH_COMMAND.length()));
 		} else if (line.startsWith(GET_COMMAND)) {
 			handleGet(line.substring(GET_COMMAND.length()));
 		} else {
 			handleError();
 		}
 
 		socket.close();
 	}
 
 	private void handleGet(String substring) {
 		String[] command = substring.split(" ");
 		String fileName = "";
 		for (int i = 0; i <= command.length - 2; i++) {
 			fileName += command[i];
 		}
 		File file = new File(Settings.getSharedFolder() + fileName);
 		if (!file.exists()) {
 			handleError();
 		}
 		int blockNr = Integer.parseInt(command[command.length - 1]);
 
 		try {
 			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
 			randomAccessFile.seek(Settings.getBlockSize() * blockNr);
 			byte[] content = new byte[Settings.getBlockSize()];
 			randomAccessFile.read(content, 0, Settings.getBlockSize());
 			randomAccessFile.close();
 
 			bufferedOutputStream.write(content);
 			bufferedOutputStream.flush();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void handleSearch(final String pattern) {
 		File[] foundFiles = new File(Settings.getSharedFolder())
 				.listFiles(new FileFilter() {
 
 					@Override
 					public boolean accept(File file) {
 						if (pattern.equals("*")
								|| (file.getName().toLowerCase()
										.contains(pattern.toLowerCase()))) {
 							return true;
 						}
 						return false;
 					}
 				});
 
 		for (File file : foundFiles) {
 			printWriter.print(file.getName() + " " + file.length() + " ");
 		}
 
 		printWriter.println();
 	}
 
 	private void handleError() {
 		printWriter.println("FAIL");
 	}
 }
