 package ReactorEE.simulator;
 
 import ReactorEE.swing.MainGUI;
 
 public class StepLooper extends Thread{
 	private PlantController controller;
 	private MainGUI GUI;
 	public StepLooper(PlantController controller, MainGUI GUI){
 		this.controller = controller;
 		this.GUI = GUI;
 	}
 	
 	
 	public void run(){
 		 try {
 		        while (true) {
 		        	if(!controller.getPlant().isPaused() & !controller.getPlant().isGameOver()){
 		        		controller.step(1);
 		        		GUI.updateGUI();
 		        		
 		        	}
 		        	if(controller.getPlant().isGameOver()){
 		        		GUI.endGameHandler();
 		        		break;
 		        	}
 	        		Thread.sleep(500);
 		        }
 		    } catch (InterruptedException e) {
 		        e.printStackTrace();
 		    }
 		 
 	}
 
 
 	public MainGUI getGUI() {
 		return GUI;
 	}

 }
