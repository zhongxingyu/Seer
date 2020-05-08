 package Networking;
 
 import javax.swing.JFrame;
 
 /**
  * Abstract class that all managers extend from.
  * 
  * @author Peter Zhang
  */
 public abstract class Client extends JFrame{
 
 	/**
 	 * This is called by ServerReaders' receiveData(Object), taking in a Request variable casted from ObjectInput.
 	 * Must be implemented by the Manager subclasses so to parse the Request variable accordingly.
 	 */
 	public abstract void receiveData(Request req);
 	
	private ServerReader reader;
 	private StreamWriter writer;
 }
