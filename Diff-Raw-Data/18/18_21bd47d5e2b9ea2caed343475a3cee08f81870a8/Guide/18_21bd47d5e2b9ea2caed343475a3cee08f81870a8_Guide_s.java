 package game.objects;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import core.GameManager;
 import core.ItemListManager;
 import core.PlayerInterface;
 
 public class Guide extends NPC implements Runnable{
 	
 	private static Guide guide;
 	private Thread runThread;
 	private ScheduledExecutorService execRun;
 	
 	private String[] text = {"...*story*.../nFollow me!",
 							"Don't idle around",
 							"This is as far as i can bring you. Take this and good Luck!"};
 
 	private int oldCounter = -1;
 	private int textCounter;
 	
 	private Guide(int xPos, int yPos){
 		System.err.println("construct Guide");
 		setMoveableType(-5);
 		setPattern(-10);
 		initializeNeutralNPC(xPos, yPos);
 		initializeThread();
 		
 	}
 
 	public void run(){
 		
 		if(GameManager.getInstance().overWorld){
 			if(this.getBound().intersects(Player.getInstance().getBoundCore()) && GameManager.getInstance().interact){
 				GameManager.getInstance().showIngameText = true;
 				interact();
 				//System.out.println("TxtCounter@"+textCounter+"interact");
 					
 				if(getReachDestination() && ItemListManager.weaponIDCounter[0] == 1){
 					ItemListManager.weaponIDCounter[0] = 0;
 					ItemListManager.dropItem(getX(),getY()-50,2,0,0);
 				}
 				
 			} else {
 				//System.out.println("TxtCounter@"+textCounter+"else");
 				GameManager.getInstance().interact = false;
 				GameManager.getInstance().showIngameText = false;
 			} 
 			
 			
 			if(!getReachDestination()){
 				
 				if(textCounter == 0){
 					setSpeed(1);
 					followObject(Player.getInstance());
 				}
 				
 				//System.out.println("Guide@Pos"+getX()+"x"+getY());
 
 				if(textCounter == 1 && !GameManager.getInstance().interact)
 					executePattern();
 				
 			} else {
 				textCounter = 2;
 				setSpeed(1);
 				followObject(Player.getInstance());
 			}
 				
 		}
 		
 		
 	}
 	
 	private void interact(){
 		
 		if(GameManager.getInstance().promptText){
 			
 			
 			if(oldCounter != textCounter){
 				PlayerInterface.getInstance().setText(text[textCounter]);
 				System.out.println("promptText:true -> buildText");
 			}
 			else {
 				GameManager.getInstance().interact = false;
 			}
 				
 			
 			PlayerInterface.getInstance().buildText();
 			oldCounter = textCounter;
 		}
 		
 		textCounter = 1;
 		
 	}
 	
 
 	private void initializeThread(){
 	
 		runThread = new Thread(this);
 		execRun = Executors.newSingleThreadScheduledExecutor();
 		
 		execRun.scheduleWithFixedDelay(runThread, 10, 30, TimeUnit.MILLISECONDS);
 		
 		GameManager.addGameObject(this);
 	}
 	
 	public void deleteInstance(){
 		setVisible(false);
 		setAlive(false);
 		GameManager.updateGameObject();
 		execRun.shutdown();
 		execRun = null;
 		guide = null;
 	}
 	
 	public static void resetInstance(){
 		if(guide != null)
 			guide.deleteInstance();
 		
 	}
 	
 	public static Guide getInstance(int xPos, int yPos){
 		if(guide == null)
 			guide = new Guide(xPos, yPos);
 		return guide;
 		
 	}
 }
