 package ch.nomoresecrets.mediastopf.server.networking;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 public class NetworkServerThread implements Runnable {
 
 	private Socket clientSocket;
 
 	private BufferedReader receiver = null;
 
 	private PrintWriter sender = null;
 
 	public NetworkServerThread(Socket clientSocket) {
 		this.clientSocket = clientSocket;
 	}
 
 	@Override
 	public void run() {
 		String receivedMessage = null;
 
 		try {
 			receivedMessage = receiveMessage();
 		} catch (IOException e) {
 			System.out.println("Cannot read from receiver");
 			e.printStackTrace();
 		}
 		
 		String reply = MediaStopfProtocol.ProccessRequest(receivedMessage);
 		
 
 		try {
 			sendMessage(reply);
 			if (receivedMessage.equals("TRANSFER"))
 				receiveFile();
 		} catch (IOException e) {
 			System.out.println("cannot write to sender");
 			e.printStackTrace();
 		}
 	}
 	
 	private void receiveFile() throws IOException {
 		System.out.println("Waiting for Filetransfer...");
 		final int FILESIZE = 21319002;
 		int bytesread = 0;
 		byte[] filebuffer = new byte[FILESIZE];
 		InputStream reader = clientSocket.getInputStream();
 		FileOutputStream writer = new FileOutputStream("a_filename");
 		BufferedOutputStream bos = new BufferedOutputStream(writer);

 		while((bytesread += reader.read(filebuffer, 0, filebuffer.length)) != -1) {
 			System.out.println("reading...");
 			System.out.println(bytesread);
 			System.out.println("filebuffer: " + filebuffer);
 			if (bytesread >= FILESIZE)
 				break;
 		}
 		System.out.println("Transfer Server finished");
 		
 		bos.write(filebuffer, 0, FILESIZE);
 		bos.flush();
 		bos.close();
 	}
 
 	private String receiveMessage() throws IOException {
 		try {
 			receiver = new BufferedReader(new InputStreamReader(clientSocket
 					.getInputStream()));
 		} catch (IOException e) {
 			System.out.println("Error: Cannot get InputStream");
 			e.printStackTrace();
 		}
 		String receivedMessage;
 
 		receivedMessage = receiver.readLine();
 
 		System.out.println("A message from a client: ");
 		System.out.println(receivedMessage);
 
 		return receivedMessage;
 	}
 
 	private void sendMessage(String reply) throws IOException {
 		try {
 			sender = new PrintWriter(new OutputStreamWriter(clientSocket
 					.getOutputStream()), true);
 		} catch (IOException e) {
 			System.out.println("Error: Cannot get OutputStream");
 		}
 		
 		sender.println(reply);
 		System.out.println("The Reply: " + reply);
 	}
 }
