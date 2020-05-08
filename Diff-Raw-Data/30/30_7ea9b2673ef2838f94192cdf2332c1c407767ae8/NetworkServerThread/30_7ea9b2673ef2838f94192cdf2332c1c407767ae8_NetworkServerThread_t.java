 package ms.server.networking;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.List;
 
 import ms.server.database.DbAdapter;
 import ms.server.domain.Auftrag;
 import ms.server.log.Log;
 
 import org.apache.log4j.Logger;
 
 public class NetworkServerThread implements Runnable {
 
 	private Socket clientSocket;
 
 	private BufferedReader receiver = null;
 
 	private PrintWriter sender = null;
 	private Logger logger = Log.getLogger();
 
 	public NetworkServerThread(Socket clientSocket) {
 		this.clientSocket = clientSocket;
 	}
 
 	public void run() {
 		String receivedMessage = null;
 		while (true) {
 		
 			try {
 				receivedMessage = receiveMessage();
 			} catch (IOException e) {
 				logger.error("Cannot read from receiver");
 				e.printStackTrace();
 			}
 			
 			if (receivedMessage.equals("END")) {
 				try {
 					sendMessage("END OK");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return;
 			}
 
 			if (receivedMessage.equals("INFO")) {
 				sendTaskList();
 			}
 
 			if (receivedMessage.equals("TRANSFER")) {
 				try {
 					sendMessage("TRANSFER READY");
 
 					String namemsg = receiveMessage();
 					sendMessage("TRANSFER NAME OK");
 
 					String sizemsg = receiveMessage();
 					int size = Integer.parseInt(sizemsg);
 					sendMessage("TRANSFER SIZE OK");
 
 					receiveFile(namemsg, size);
 					sendMessage("ENDTRANSFER");
 				} catch (IOException e) {
 					logger.error("cannot write to sender");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void sendTaskList() {
 		List<Auftrag> lp = DbAdapter.getOrderList();
 
 		for (Auftrag name : lp) {
 			try {
 				sendMessage(String.valueOf(name.getID()));
 				if (!receiveMessage().equals("OK"))
 					logger.fatal("Error in Network protocol");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			sendMessage("ENDINFO");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void receiveFile(String name, int size) throws IOException {
 		logger.info("Waiting for Filetransfer...");
 		int bytesread = 0;
 		byte[] filebuffer = new byte[size];
 		InputStream reader = clientSocket.getInputStream();
 		FileOutputStream writer = new FileOutputStream(name + "_rec");
 		BufferedOutputStream bos = new BufferedOutputStream(writer);
 		while ((bytesread += reader.read(filebuffer, 0, filebuffer.length)) != -1) {
 			logger.info("reading...");
 			logger.info(bytesread);
 			logger.info("filebuffer: " + filebuffer);
 			if (bytesread >= size)
 				break;
 		}
 		logger.info("Transfer Server finished");
 
 		bos.write(filebuffer, 0, size);
 		bos.flush();
 		bos.close();
 	}
 
 	private String receiveMessage() throws IOException {
 		try {
 			receiver = new BufferedReader(new InputStreamReader(clientSocket
 					.getInputStream()));
 		} catch (IOException e) {
 			logger.error("Error: Cannot get InputStream");
 			e.printStackTrace();
 		}
 
		return receiver.readLine();
 	}
 
 	private void sendMessage(String reply) throws IOException {
 		try {
 			sender = new PrintWriter(new OutputStreamWriter(clientSocket
 					.getOutputStream()), true);
 			logger.info("SERVER: Server message: " + reply);
 		} catch (IOException e) {
 			logger.error("Error: Cannot get OutputStream");
 		}
 
 		sender.println(reply);
 		logger.info("The Reply: " + reply);
 	}
 }
