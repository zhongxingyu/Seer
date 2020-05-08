 package squeal.task;
 
 import java.awt.event.KeyEvent;
 import java.util.Scanner;
 
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Keyboard;
 import org.powerbot.game.api.methods.tab.Inventory;
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
 				
 				getAccount();
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
 				
 				if(Inventory.isFull()) {				
 
 					if(Widgets.get(548, 159).click(true)) {
 						
 						Time.sleep(100, 200);
 						
 						if (Game.logout(false)) {
 							
 							//Method sometimes fails.. The game fails to logout and the script stops for some currently unknown reason.
 							
 						}
 						
 					}
 					
 				}
 			}
 			
 		}
 		
 	}
 	
 	public void getAccount() {
 		
 		try {
 			
 			if(Squeal.anotherAccount()) {	
 				
 				String acc = Squeal.nextAccount();
 				
 				System.out.println(acc);
 				
 				Scanner manageAccount = new Scanner(acc);
 				
 				manageAccount.useDelimiter(":");
 				
 				String account = manageAccount.next();
 				
 				System.out.println("Account: " + account);
 				
 				if(manageAccount.hasNext()) {
 					
 					String password = manageAccount.next();
 					
 					Squeal.setPassword(password);
 					
 					System.out.println("Pass: " + password);
 					
 				} else {
 					
 					Squeal.setPassword(Squeal.getGlobalPassword());
 					
 					System.out.println("Pass: " + Squeal.getCurrentPassword());
 					
 				}
 				
 				Squeal.setAccount(account);
 				
 			} else {
 				
 				System.out.println("There are not anymore accounts");
 				
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
 			
 			Keyboard.sendText(Squeal.getCurrentAccount(), false);
 			
 		}
 		
 		if(Widgets.get(596, 76).click(true)) {
 			
 			Time.sleep(100, 200);
 			
 			Keyboard.sendText(Squeal.getCurrentPassword(), false);
 			
 		}
 		
 		
 	}
 	
 	public void attemptLog() {
 		
 		if(Widgets.get(596, 57).click(true)) {
 			
 			Time.sleep(3000, 5000);
 			
 			if(Widgets.get(596, 13).getText().startsWith("Invalid")) {
 				
 				System.out.println("Bad Login: " + Squeal.getCurrentAccount());
 				
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
