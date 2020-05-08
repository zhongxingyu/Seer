 /**
  * Container class for the shared resource between the worker thread 
  * to the communication thread. The Shared resource is a String which will
  * contain the status of the Worker, so the communication thread will be able
  * to send it to the LunaCora. 
  * This class includes synchronized methods to work with the shared resource. 
  * 
  */
 
 /**
  * @author Eduardo Hernandez Marquina
  * 
  */
 class Message {
 	private String msg = "";
	private boolean statusToComsume = false;
 
 	/**
 	 * Method to get the shared resource It will put sleeping the thread who
 	 * call the method until there where something to consume.
 	 * 
 	 * @return A String with the worker's status
 	 * 
 	 */
 	synchronized String getResource() {
 		if (!isStatusToComsume())
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		System.out.println("Obtenido: " + getMsg());
 		setStatusToComsume(false);
 		notify();
 		return getMsg();
 	}
 
 	/**
 	 * Method to get the shared resource It will put sleeping the thread who
 	 * call the method until there where nothing to consume, in order to not
 	 * overwrite any worker status.
 	 * 
 	 * @param msg
 	 *            A String with the worker's status
 	 * 
 	 */
 	synchronized void putResource(String msg) {
 		if (isStatusToComsume())
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		setMsg(msg);
 		setStatusToComsume(true);
 		System.out.println("Colocado: " + getMsg());
 		notify();
 	}
 
 	
 	public String getMsg() {
 		return msg;
 	}
 
 	public void setMsg(String msg) {
 		this.msg = msg;
 	}
 
 	public boolean isStatusToComsume() {
 		return statusToComsume;
 	}
 
 	public void setStatusToComsume(boolean statusToComsume) {
 		this.statusToComsume = statusToComsume;
 	}
 }
