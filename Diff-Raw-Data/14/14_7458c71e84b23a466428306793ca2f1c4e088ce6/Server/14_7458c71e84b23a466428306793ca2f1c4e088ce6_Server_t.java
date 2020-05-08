 package com.shreyaschand.MEDIC.Server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 	
 public class Server {
 
 	private ConcurrentHashMap<String, Doctor> doctors;
 
 	public static void main(String[] args) throws IOException {
 		new Server().start();
 	}
 
 	public void start() throws IOException {
 		doctors = new ConcurrentHashMap<String, Doctor>();
 		ServerSocket ssock = new ServerSocket(1028);
 		System.out.println("Listening on port 1028.\n");
 
 		while (true) {
 			System.out.println("Waiting for connection...");
 			Socket csocket = ssock.accept();
 			System.out.println("Client connected.");
 			new Thread(new ConversationManager(csocket)).start();
 		}
 
 	}
 
 	private class ConversationManager implements Runnable {
 		private Socket socket;
 		private BufferedReader reader;
 		private PrintWriter writer;
 
 		public ConversationManager(Socket sock) {
 			socket = sock;
 		}
 
 		public void run() {
 			try {
 				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 				writer = new PrintWriter(socket.getOutputStream());
 				String handshake = reader.readLine();
 				System.out.println("\t" + handshake);
 				if (handshake == null) {kill();return;}
 				else if (handshake.contains("$DOC$")) {
 					System.out.println("\tIt's a doctor!");
 					doctors.put(handshake.substring(5), new Doctor(socket, writer, reader));
 					writer.println("okay");
 					writer.flush();
					System.out.println(doctorList());
 					return;
 				} else {
 					System.out.println("\tIt's a patient!");
 					String doctorName = "$list";
 					while(doctorName.equals("$list")){
						System.out.println(doctorList());
 						writer.println(doctorList());
 						writer.flush();
 						doctorName = reader.readLine();
 					}
 					Doctor doctor = null;
 					
 					synchronized (doctors) {
 						if (doctorName != null && doctorList().contains(doctorName)) {
 							doctor = doctors.remove(doctorName);
 						} else {kill();	return;}
 					}
 					mediateConversation(doctor);
 					kill();
 					return;
 				}
 			} catch (IOException e) {
 				kill();
 				return;
 			}
 		}
 
 		private void mediateConversation(Doctor doctor) throws IOException {
			System.out.println("Conversating now.");
 			String message;
 			while((message = reader.readLine()) != null){
 				doctor.writer.println(message);
				doctor.writer.flush();
 			}
 			doctor.writer.close();
 			doctor.reader.close();
 			doctor.sock.close();
 		}
 
 		private void kill() {
 			clean();
 			try{
 				socket.close();
 			}catch (IOException e){}//Assume closed
 		}
 
 		private void clean() {
 			writer.close();
 			try {
 				reader.close();
 			} catch (IOException e) {}//Assume closed
 		}
 		
 		private String doctorList() {
 			String doctorList = "$";
 			Set<String> set = doctors.keySet();
 			for (String s : set) {
 				doctorList += s + "$";
 			}
 			return doctorList;
 		}
 	}
 	
 	private class Doctor {
 		public Socket sock;
 		public PrintWriter writer;
 		public BufferedReader reader;
 
 		public Doctor(Socket sock, PrintWriter writer, BufferedReader reader) {
 			this.sock = sock;
 			this.writer = writer;
 			this.reader = reader;
 		}
 	}
 }
