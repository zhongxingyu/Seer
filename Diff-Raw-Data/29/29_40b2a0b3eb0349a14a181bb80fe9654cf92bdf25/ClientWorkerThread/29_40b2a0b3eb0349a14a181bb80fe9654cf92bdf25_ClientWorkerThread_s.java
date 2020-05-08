 /**
  * 
  */
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 /**
  * Responsible class for the communication with the LunaCore and the 
  * termination of the instance in case of successful.
  * 
  * @author Eduardo Hernandez Marquina
  * 
  */
 public class ClientWorkerThread implements Runnable {
 	private int rowID;
 	private String receiptHandle;
 	private Message messageObject;
 	private String serverName;
 	private int port;
 	InstanceManager instanceManager;
 
 	public ClientWorkerThread(int rowID, String receiptHandle,
 			Message messageObjet, String serverName, int port,
 			InstanceManager instanceManager) {
 		super();
 		setReceiptHandle(receiptHandle);
 		setRowID(rowID);
 		setMessageObject(messageObjet);
 		setServerName(serverName);
 		setPort(port);
 		setInstanceManager(instanceManager);
 		new Thread(this, "ClientWorkerThread").start();
 	}
 
 	@Override
 	public void run() {
 		Logger l = new Logger();
 		DataInputStream input;
 		DataOutputStream output;
 
 		try {
 			Socket client = new Socket(getServerName(), getPort());
 			// It will try to connect with Lunacore for 10 minutes as much
 			// if it will not able to reach it, a exception will be launched
 			client.setSoTimeout(10 * 60 * 1000);
 			input = new DataInputStream(client.getInputStream());
 			output = new DataOutputStream(client.getOutputStream());
 
 			// sending rowID to LunaCore
 			output.writeUTF(String.valueOf(this.getRowID()));
 			l.logging("Sent rowID: " + this.getRowID());
 
 			// sending receiptHandle to LunaCore
 			output.writeUTF(this.getReceiptHandle());
 			l.logging("Sent receiptHandle: " + this.getReceiptHandle());
 
 			// The Sending worker's Status while
 			String msg = "";
 			do {
 				// the next statement will sleep the thread until a new Shared
 				// resource will be ready to consume
 				msg = getMessageObject().getResource();
 				output.writeUTF(msg);
 				l.logging("Sent message: " + msg);
				if (msg.equals("The rendering has been finished"))
					output.writeUTF("I've got the ending point");
			} while (!msg.equals("The rendering has been finished")); //TODO: probar que sale de aqui!!
			
			l.logging("Out of the while finish!!!");
			output.writeUTF("Out of the while finish!!!");
 			/*
 			 * At this point the rendering job is done. It is needed now to
 			 * terminate the instance
 			 */
 			output.close();
 			input.close();
 			client.close();
			l.logging("About to finish!!!");
 			// Kill instance
 			getInstanceManager().killmePlease(); 
			l.logging("terminated!!!");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Message getMessageObject() {
 		return messageObject;
 	}
 
 	public void setMessageObject(Message messageObject) {
 		this.messageObject = messageObject;
 	}
 
 	public int getRowID() {
 		return rowID;
 	}
 
 	public void setRowID(int rowID) {
 		this.rowID = rowID;
 	}
 
 	public String getReceiptHandle() {
 		return receiptHandle;
 	}
 
 	public void setReceiptHandle(String receiptHandle2) {
 		this.receiptHandle = receiptHandle2;
 	}
 
 	public String getServerName() {
 		return serverName;
 	}
 
 	public void setServerName(String serverName) {
 		this.serverName = serverName;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public InstanceManager getInstanceManager() {
 		return instanceManager;
 	}
 
 	public void setInstanceManager(InstanceManager instanceManager) {
 		this.instanceManager = instanceManager;
 	}
 
 }
