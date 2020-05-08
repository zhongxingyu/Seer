 /**
  * 
  */
 import java.util.Date;
 
 /**
  * Responsible class for the rendering job
  * 
  * @author Eduardo Hernandez Marquina
  * @author Hector Veiga
  * @author Gerardo Travesedo
  * 
  */
 public class Renderer implements Runnable {
 
 	private String rowID;
 	private String receiptHandle;
 	private Message MessageObject;
 	private InstanceManager instanceManager;
 
 	public Renderer(int rowID2, String receiptHandle, Message msg,
 			InstanceManager instanceManager) {
 		super();
 		setReceiptHandle(receiptHandle);
 		setRowID(rowID);
 		setMessageObject(msg);
 		setInstanceManager(instanceManager);
 		new Thread(this, "Renderer").start();
 	}
 
 	@Override
 	public void run() {
 		Logger l = new Logger();
 
 		// TODO: WHAT IS THE PURPOUS???
 		Date time1 = new Date();
 		long epoch1 = (long) System.currentTimeMillis() / 1000;
 
 		// rendering code
 		getMessageObject().putResource(l.logging("Probando 1"));
 		try {
 			Thread.sleep(1 * 1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		getMessageObject().putResource(l.logging("Probando 2"));
 		try {
 			Thread.sleep(1 * 1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		getMessageObject().putResource(l.logging("Probando 3"));
 		try {
 			Thread.sleep(1 * 1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		getMessageObject().putResource("The rendering has been finished");
 
 	}
 
 	// TODO: ver si se usa en la renderizacion
 	public static String substringBetween(String str, String open, String close) {
 		if (str == null || open == null || close == null) {
 			return null;
 		}
 		int start = str.indexOf(open);
 		if (start != -1) {
 			int end = str.indexOf(close, start + open.length());
 			if (end != -1) {
 				return str.substring(start + open.length(), end);
 			}
 		}
 		return null;
 	}
 
 	// TODO: ver si se usa en la renderizacion
 	public static int giveMeSeconds(String line, String del1, String del2) {
 		String durVid = "";
 		String[] durVidPieces;
 
 		durVid = substringBetween(line, del1, del2);
 		durVidPieces = durVid.split(":");
 		int secs = (Integer.parseInt(durVidPieces[0]) * 3600)
 				+ (Integer.parseInt(durVidPieces[1]) * 60)
 				+ Integer.parseInt(durVidPieces[2].substring(0, 2));
 		return secs;
 	}
 
 	// private static void updateStatus(String percentage) {
 	// // Update Database Status
 	// try {
 	// Connection connec = DriverManager.getConnection(
 	// "jdbc:mysql://64.131.110.162/luna", "Europa", "a23d578");
 	// Statement s = connec.createStatement();
 	// String query1 = "UPDATE requests SET status='" + percentage
 	// + "' WHERE id='" + rowId + "'";
 	// s.executeUpdate(query1);
 	// s.close();
 	// connec.close();
 	// } catch (Exception e) {
 	// logging("Exception catch!!!! Something wrong updating status of a request");
 	// logging(e.toString());
 	// }
 	// }
 
 	// private static void updateParameter(String parameter, long quantity) {
 	// logging("Updating parameter '" + parameter + "' by " + quantity);
 	//
 	// // Update Database Parameter
 	// try {
 	// Connection connec = DriverManager.getConnection(
 	// "jdbc:mysql://64.131.110.162/luna", "Europa", "a23d578");
 	// Statement s = connec.createStatement();
 	// String query1 = "UPDATE parameters SET value=value+"
 	// + String.valueOf(quantity) + " WHERE parameter='"
 	// + parameter + "'";
 	// s.executeUpdate(query1);
 	// s.close();
 	// connec.close();
 	// } catch (Exception e) {
 	// logging("Exception catch!!!! Something wrong updating status of a request");
 	// logging(e.toString());
 	// }
 	// }
 
 	public String getRowID() {
 		return rowID;
 	}
 
 	public void setRowID(String rowID) {
 		this.rowID = rowID;
 	}
 
 	public String getReceiptHandle() {
 		return receiptHandle;
 	}
 
 	public void setReceiptHandle(String receiptHandle) {
 		this.receiptHandle = receiptHandle;
 	}
 
 	public Message getMessageObject() {
 		return MessageObject;
 	}
 
 	public void setMessageObject(Message messageObject) {
 		MessageObject = messageObject;
 	}
 
 	public InstanceManager getInstanceManager() {
 		return instanceManager;
 	}
 
 	public void setInstanceManager(InstanceManager instanceManager) {
 		this.instanceManager = instanceManager;
 	}
 
 }
