 package pl.ijestfajnie.circles.kernel;
 
 public class Process implements Runnable {
 	
 	public enum PROCESS_STATUS {
 		WAITING, RUNNING, SETUP, TERMINATED
 	}
 	
 	private PROCESS_STATUS currentStatus;
 	private String[] cartridge;
 	private int id;
 	private int currentPointer = 0;
 	
 	public Process(int id, String cartridge[]) {
 		currentStatus = PROCESS_STATUS.SETUP;
 		this.cartridge = cartridge;
 		this.id = id;
 	}
 	
 	public PROCESS_STATUS GetStatus() {
 		return currentStatus;
 	}
 	
 	public int GetID() {
 		return id;
 	}
 
 	@Override
 	public void run() {
 		currentStatus = PROCESS_STATUS.RUNNING;
 		for (; currentPointer < cartridge.length; currentPointer++) {
			MichieASM.parse(id, cartridge, currentPointer);
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
