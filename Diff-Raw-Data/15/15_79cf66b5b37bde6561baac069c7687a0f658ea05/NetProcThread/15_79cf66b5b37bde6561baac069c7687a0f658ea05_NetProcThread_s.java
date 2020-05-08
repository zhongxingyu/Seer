 package ms.utils.networking.server;
 
import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

 import ms.domain.Auftrag;
 import ms.domain.ImportMedium;
 import ms.utils.networking.BasicNetIO;
 import ms.utils.server.database.DbAuftragsManager;
 
 public class NetProcThread extends BasicNetIO implements Runnable {
 
 	public NetProcThread(Socket clientSocket) {
 		commSocket = clientSocket;
 	}
 
 	public void run() {
 		String receivedMessage = null;
 		while (true) {
 			try {
 				if (commSocket.isConnected())
 					receivedMessage = receiveMessage();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			if (receivedMessage == null) {
 				SocketAddress remoteaddr = commSocket.getRemoteSocketAddress();
 				logger.info("Client " + remoteaddr.toString()
 						+ " has disconnected");
 				return;
 			}
 
 			if (receivedMessage.equals("INFO")) {
 				sendTaskList();
 			}
 
 			if (receivedMessage.equals("TRANSFER")) {
 				ImportMedium m = receiveImportMedium();
 			}
 		}
 	}
 
 	private ImportMedium receiveImportMedium() {
 		ImportMedium m = null;
 		try {
 			sendMessage("TRANSFER READY");
 			String id = receiveMessage();
 			Integer _id = new  Integer(id).intValue();
 			sendMessage("ID OK");
 
 			String element_count = receiveMessage();
 			sendMessage("ELEMENTCOUNT OK");
 			int count = new Integer(element_count).intValue();
 
 			ArrayList<String> names = new ArrayList<String>();
 			ArrayList<ByteBuffer> items = new ArrayList<ByteBuffer>();
 
 			for (int i = 0; i < count; ++i) {
 				String name = receiveMessage();
 				names.add(name);
 				sendMessage("NAME OK");
 
 				ByteBuffer b = receiveBuffer();
 				items.add(b);
 			}
 
 			if (receiveMessage().equals("END TRANSFER"))
 				sendMessage("END OK");
 			
 			m = new ImportMedium(_id, names, items);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return m;
 	}
 
 	private ByteBuffer receiveBuffer() {
 		int totalbytes = 0;
 		ByteBuffer b = null;
 
 		String _size = null;
 		
 		try {
 			_size = receiveMessage();
 			sendMessage("SIZE OK");
 			int size = new Integer(_size).intValue();
 			byte[] filebuffer = new byte[size];
 			b = ByteBuffer.allocate(size);
 
 			commSocket.setReceiveBufferSize(size);
 			InputStream reader = commSocket.getInputStream();
 
 			int readuntilnow = 0;
 			while ((totalbytes += reader.read(filebuffer, readuntilnow, filebuffer.length-readuntilnow)) != -1) {
 				if (totalbytes >= size)
 					break;
 				int len = totalbytes-readuntilnow;
 				b.put(filebuffer, readuntilnow, len);
 				readuntilnow = totalbytes;
 			}
 
 			sendMessage("ELEMENT OK");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return b;
 	}
 
 	private void sendTaskList() {
 		List<Auftrag> lp = DbAuftragsManager.getinstance(null).getAuftragList();
 		sendObject(lp);
 	}
 }
