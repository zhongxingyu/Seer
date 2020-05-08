 package squeal.task;
 
 import java.awt.event.KeyEvent;
 import java.util.Scanner;
 
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Keyboard;
 import org.powerbot.game.api.methods.widget.Lobby;
 import org.powerbot.game.api.util.Time;
 
 import squeal.Squeal;
 
 public class EnterGame extends Strategy implements Runnable {
 
 	private boolean clientDead;
 
 	@Override
 	public void run() {
 		
 		if(Squeal.isLogoutNeeded()) {
 			
 			while(Game.isLoggedIn()) {
 				
 				Game.logout(false);
 				
 				Time.sleep(1000);
 				
 			}
 			
 			Squeal.setLogout(false);
 			
 		}
 
 		clientDead = false;
 		
 		if(!Lobby.isOpen() || !Lobby.getOpenDialog().isOpen()) {
 			
 			while(!Lobby.isOpen() && !clientDead && !Game.isLoggedIn() && Widgets.get(596, 70).isOnScreen()) {
 				
 				getId();
 				clearDetails();
 				enterDetails();
 				attemptLog();
 				
 			}
 			
 			
 		} else {
 			
 			if(clientDead == false) {
 			
 				while(!Game.isLoggedIn()) {
 					
 					if(Lobby.enterGame()) {
 						
 						//Method sometimes fails for some unknown reason.
 						
 						Time.sleep(3000, 5000);	
 						
 					}
 					
 				}
 				
 				if(Widgets.get(1313, 11).isOnScreen()) {
 					
 					Widgets.get(1313, 2).click(true);
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	public void getId() {
 		
 		try {
 			
 			if(Squeal.anotherId()) {	
 				
 				String id = Squeal.nextId();
 				
 				System.out.println(id);
 				
 				Scanner manageId = new Scanner(id);
 				
 				manageId.useDelimiter(":");
 				
 				String aId = manageId.next();
 				
 				System.out.println("Id: " + aId);
 				
 				if(manageId.hasNext()) {
 					
 					String serial = manageId.next();
 					
 					Squeal.setSerial(serial);
 					
 					System.out.println("Serial: " + serial);
 					
 				} else {
 					
 					Squeal.setSerial(Squeal.getGlobalSerial());
 					
 					System.out.println("Serial: " + Squeal.getCurrentSerial());
 					
 				}
 				
 				Squeal.setId(aId);
 				Squeal.addSpin();
 				
 			} else {
 				
 				System.out.println("There are not anymore ids");
 				
 				Squeal.stopScript();
 				
 			}
 					
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 			
 		}
 		
 	}
 	
 	public void clearDetails() {
 		
 		while(Widgets.get(596, 70).getText().length() > 1) {
 			
 			Widgets.get(596, 70).click(true);
 			
 			Keyboard.sendKey((char)KeyEvent.VK_BACK_SPACE);
 			
 		}
 		
 		while(Widgets.get(596, 76).getText().length() > 1) {
 			
 			Widgets.get(596, 76).click(true);
 			
 			Keyboard.sendKey((char)KeyEvent.VK_BACK_SPACE);
 			
 		}
 		
 	}
 	
 	public void enterDetails() {
 		
 		if(Widgets.get(596, 70).click(true)) {
 			
 			Time.sleep(100, 200);
 			
 			Keyboard.sendText(Squeal.getCurrentId(), false);
 			
 		}
 		
 		if(Widgets.get(596, 76).click(true)) {
 			
 			Time.sleep(100, 200);
 			
			Keyboard.sendText(Squeal.getCurrentSerial(), false);
 			
 		}
 		
 		
 	}
 	
 	public void attemptLog() {
 		
 		if(Widgets.get(596, 57).click(true)) {
 			
 			Time.sleep(3000, 5000);
 			
 			if(Widgets.get(596, 13).getText().startsWith("Invalid")) {
 				
 				System.out.println("Bad Login: " + Squeal.getCurrentId());
 				
 				Widgets.get(596, 65).click(true);
 				
 			}
 			
 			if(Widgets.get(596, 13).getText().startsWith("Too many")) {
 				
 				if(Widgets.get(596, 65).click(true)) {
 					
 					System.out.println("Too many bad logins, waiting 5 minutes.");
 					
 					Time.sleep(3000 * 100);
 					
 				}
 				
 			}
 			
 			if(Widgets.get(596, 13).getText().startsWith("Your game")) {
 				
 				//I need a method to make the client restart.
 				
 				//Widgets.get(596, 65).click(true);
 				
 				//We are now forcing the client into its anti-random mode.
 				
 				clientDead = true;
 				System.out.println("Crash Prevented.");
 				
 			}
 			
 		} 
 		
 	}
 	
 	public boolean validate() {
 		
 		return !Game.isLoggedIn() || Squeal.isLogoutNeeded();
 		
 	}
 	
 }
