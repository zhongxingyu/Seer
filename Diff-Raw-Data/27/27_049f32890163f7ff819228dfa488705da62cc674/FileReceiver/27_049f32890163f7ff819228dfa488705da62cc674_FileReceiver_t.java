 package file;
 
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import message.Message;
 import message.Messageable;
 
 public class FileReceiver implements Runnable {
 
 	private boolean running;
 	private final static int BUFFER = 2048;
 	
 	private List<Message> fileMessageQueue;
 	private DataInputStream in;
 	
 	private String rootFolder = "";
 	
 	public FileReceiver(String rootFolder) {
 		this.rootFolder = rootFolder;
 		fileMessageQueue = new ArrayList<Message>();
 		running = false;
 	}
 	
 	public void setDataInputStream(DataInputStream in) {
 		this.in = in;
 	}
 	
 	public void enqueueMessage(Message m) {
 		fileMessageQueue.add(m);
 	}
 	
 	private Message dequeueMessage() {
 		if (fileMessageQueue.isEmpty()) {
 			running = false;
 			return null;
 		}
 		return fileMessageQueue.remove(0);
 	}
 	
 	public void stop() {
 		running = false;
 	}
 	
 	public boolean isStopped() {
 		return !running;
 	}
 	
 	@Override
 	public void run() {
 		running = true;
 
 		if (running && in != null) {
 			byte[] mybytearray = new byte[BUFFER];
 			try {
 				int fileNameSize = in.readInt();
 				StringBuilder filenamebuilder = new StringBuilder();
 				for (int x = 0; x < fileNameSize; ++x) {
 					filenamebuilder.append(in.readChar());
 				}
 				String filename = filenamebuilder.toString();
				//make the directories if they do not exist
				File f = new File(rootFolder);
				if (!f.exists()) {
					f.mkdirs();
				}
				f = new File(rootFolder + filename);
				String localName = f.getAbsolutePath();
 				System.out.println("Receiving: " + localName);
 				FileOutputStream fos = new FileOutputStream(localName,
 						false);
 				BufferedOutputStream bos = new BufferedOutputStream(fos);
 				long fileLength = in.readLong();
 				int bytesRead = 0;
 				while (fileLength > 0) { // Index file being sent
 					if (fileLength > BUFFER) {
 						bytesRead = in.read(mybytearray);
 					} 
 					else {
 						bytesRead = in.read(mybytearray, 0, (int) fileLength);
 					}
 					fileLength -= bytesRead;
 					bos.write(mybytearray, 0, bytesRead);
 					bos.flush();
 				}
 				bos.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			System.err.println("Receiver does not have an input stream!");
 		}
 		stop();
 	}
 }
