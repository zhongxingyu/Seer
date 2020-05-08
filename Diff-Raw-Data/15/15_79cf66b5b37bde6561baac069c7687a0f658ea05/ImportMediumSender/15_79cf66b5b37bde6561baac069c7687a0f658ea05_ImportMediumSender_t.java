 package ms.utils.networking.client;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.Iterator;
 
 import ms.domain.ImportMedium;
 import ms.domain.SendeListe;
 
 /**
  * 
  * Thread der ständig läuft um fertige Importmedien zu versenden. Importmedien
  * können dem Thread mit
  * 
  * <code>
  * addMediumForTransfer(ImportMedium m);
  * </code>
  * 
  * hinzugefügt werden. Der ImportMediumSender verschickt diese dann. Dies muss
  * allerdings nicht sofort passieren.
  * 
  */
 
 public class ImportMediumSender extends AbstractServerConnection implements
 		Runnable {
 
 	SendeListe mediumlist;
 
 	public SendeListe getsendeliste() {
 
 		return mediumlist;
 
 	}
 
 	public ImportMediumSender(String host, int port)
 			throws UnknownHostException, IOException {
 		super(host, port);
 		mediumlist = new SendeListe();
 	}
 
 	/**
 	 * Hinzufügen eines Importmediums für den Versand
 	 * 
 	 * @param m
 	 */
 	public void addMediumForTransfer(ImportMedium m) {
 
 		mediumlist.add(m);
 
 	}
 
 	public synchronized void run() {
 		while (true) {
 			synchronized (mediumlist) {
 				Iterator<ImportMedium> it = mediumlist.getList().iterator();
 				while (it.hasNext()) {
 					ImportMedium m = it.next();
 					try {
 						sendImportMedium(m);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					it.remove();
 				}
 			}
 			Thread.yield();
 			try {
 				Thread.sleep(10000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Verschickt Importmedium
 	 * 
 	 * @param m
 	 * @throws IOException
 	 */
 	private void sendImportMedium(ImportMedium m) throws IOException {
 		sendMessage("TRANSFER");
 		sendMedium(m);
 	}
 
 	private void sendMedium(ImportMedium m) {
 		String reply;
 		try {
 			reply = receiveMessage();
 			if (!reply.equals("TRANSFER READY"))
 				return;
 
 			Integer id = m.getID();
 			String _id = id.toString();
 			sendMessage(_id);
 			if (!receiveMessage().equals("ID OK"))
 				return;
 
 			Iterator<String> name_it = m.names.iterator();
 			Iterator<ByteBuffer> file_it = m.items.iterator();
 
 			Integer element_count = m.names.size();
 			String _element_count = element_count.toString();
 			sendMessage(_element_count);
 			if (!receiveMessage().equals("ELEMENTCOUNT OK"))
 				return;
 
 			while (name_it.hasNext()) {
 				String name = name_it.next();
 				ByteBuffer buf = file_it.next();
 
 				sendMessage(name);
 				if (!receiveMessage().equals("NAME OK"))
 					return;
 
 				sendByteBuffer(buf);
 			}
 
 			sendMessage("END TRANSFER");
 			if (!receiveMessage().equals("END OK"))
 				return;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void sendByteBuffer(ByteBuffer buf) {
 		Integer size = buf.capacity();
 		String _size = size.toString();
 		try {
 			sendMessage(_size);
 
 			if (!receiveMessage().equals("SIZE OK"))
 				return;
 
 			OutputStream sender = commSocket.getOutputStream();
 			sender.write(buf.array(), 0, buf.capacity());
 			sender.flush();
 
 			if (!receiveMessage().equals("ELEMENT OK"))
 				return;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
