 package org.csgames.ai.client.network;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 
 public class NetworkLayer {
 
 	private final static int PORT = 9090;
 	private Socket socket;
 	private BufferedReader reader;
 	private BufferedWriter writer;
 
 	public NextMoveSender waitOnMoveToBeAsked() throws IOException, GameEndedException {
 		while (reader == null) {
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		String line = reader.readLine();
		if(line == null || line.trim().equals("quit")) {
 			throw new GameEndedException();
 		}
 		String[][] map = generateMap(line);
 		return new NextMoveSender(map, writer);
 	}
 
 	private String[][] generateMap(String line) {
 		List<String[]> lines = new LinkedList<String[]>();
 		for (String y : line.split(";")) {
 			lines.add(y.split(","));
 		}
 		return lines.toArray(new String[][] {});
 	}
 
 	public void connectToServer() throws UnknownHostException, IOException {
 		Thread th = new Thread(new Runnable() {
 
 			public void run() {
 				try {
 					System.out.println("Connecting to server...");
 					socket = new Socket("localhost", PORT);
 					System.out.println("Done.");
 					reader = new BufferedReader(new InputStreamReader(
 							socket.getInputStream()));
 					writer = new BufferedWriter(new OutputStreamWriter(
 							socket.getOutputStream()));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		th.start();
 	}
 
 }
