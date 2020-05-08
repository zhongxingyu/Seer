 package ReactorEE.simulator;
 
 import ReactorEE.Networking.Message;
 import ReactorEE.Networking.SocketUtil;
 import ReactorEE.swing.MainGUI;
 
 public class MultiplayerStepLooper extends StepLooper {
 	private String listenerIP;
 	private int portNo;
 	private Message sendPlant;
 	
 	public MultiplayerStepLooper(PlantController controller, MainGUI GUI, String listenerIP, int portNo) {
 		super(controller, GUI);
 		this.listenerIP = listenerIP;
 		this.portNo = portNo;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * Additionally sends a packet containing the plant over the network on every iteration.
 	 */
 	@Override
 	public void run(){
 		 try {
 		        while (true) {
 		        	if(!controller.getPlant().isPaused() & !controller.getPlant().isGameOver()){
 		        		controller.step(1);
 		        		GUI.updateGUI();
 		        		sendPlant = new Message();
 		        		sendPlant.run(SocketUtil.toBypeArray(controller.getPlant()), listenerIP, portNo);
		        		
 		        	}
 		        	if(controller.getPlant().isGameOver()){
 		        		GUI.endGameHandler();
 		        		break;
 		        	}
 	        		Thread.sleep(500);
 		        }
 		    } catch (InterruptedException e) {
 		        e.printStackTrace();
 		    } catch (Exception e) {
 				e.printStackTrace();
 			}
 		 
 	}
 
 }
