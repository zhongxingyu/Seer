 package squeal.task;
 
 import org.powerbot.concurrent.Task;
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.util.Time;
 
 import squeal.Squeal;
 
 public class UseSpin extends Strategy implements Task {
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		
 		if(Widgets.get(1139, 2).isOnScreen()) {
 			
 			if(Widgets.get(1139, 6).getText().equals("0") && !Widgets.get(1252, 3).isOnScreen()) {
 				
 				if(Widgets.get(548, 159).click(true) && !Widgets.get(1322, 8).isOnScreen()) {
 					
 					Time.sleep(100, 200);
 					
 					//We will set the client to need a logout.
 					
 					Squeal.setLogout(true);
 					
 					/*
 					if(Game.logout(false)) {
 						
 						//Method sometimes fails.. The game fails to logout and the script stops for some currently unknown reason.
 						
 					}
 					*/
 					
 				}
 				
 			} else {
 				
 				if(Widgets.get(1252, 3).isOnScreen()) {
 					
 					while((!Widgets.get(1253, 181).isOnScreen() && Widgets.get(1252, 3).isOnScreen()) && !Widgets.get(1253, 259).isOnScreen()) {
 						
 						Widgets.get(1252, 3).click(true);
 						
 						Time.sleep(1000, 1500);
 						
 					}
 					
 				} else {
 					
 					while((!Widgets.get(1253, 181).isOnScreen() && Widgets.get(1139, 2).isOnScreen()) && !Widgets.get(1253, 259).isOnScreen()) {
 				
 						Widgets.get(1139, 7).click(true);
 				
 						Time.sleep(1000, 2000);
 				
 					}
 					
 				}
 			
 			}
 			
 		}
 		
 		while((Widgets.get(1253, 181).isOnScreen() && !Widgets.get(1253, 259).isOnScreen())) {
 			
 			Widgets.get(1253, 181).click(true);
 			
 			Time.sleep(100, 200);
 			
 		}
 		
 		if(Widgets.get(1253, 259).isOnScreen()) {
 			
 			final String prize = Widgets.get(1253, 259).getText();
 			
 			if(Widgets.get(1253, 278).isOnScreen() && Widgets.get(1253, 278).getText().startsWith("Subscribe")) {
 				
 				while(Widgets.get(1253, 322).isOnScreen() && !Widgets.get(1253, 323).isOnScreen()) {
 					
 					Widgets.get(1253, 328).click(true);
 					
 					Time.sleep(1000, 1500);
 					
 					if(Widgets.get(1253, 387).isOnScreen()) {
 						
 						Widgets.get(1253, 387).click(true);
 						
 						Time.sleep(1000, 1500);
 						
 					}
 					
 				}
 					
 				Widgets.get(1253, 343).click(true); //done
 				
 				Time.sleep(1000, 1500);
 
 				try {
 					
 					Squeal.logPrize(Squeal.getCurrentAccount() + " discarded prize: ");
 					Squeal.logPrize(prize);
 
 					Squeal.flushLog();
 					
 				} catch (Exception e1) {
 					
 					e1.printStackTrace();
 					
 				}
 							
 				System.out.println("Discarded prize");
 					
 			} else {
 				
 				while(!Widgets.get(1253, 276).isOnScreen() && Widgets.get(1253, 275).isOnScreen()) {
 					
 					Widgets.get(1253, 278).click(true); 
 						
 					Time.sleep(1000, 1500);
 						
 				}
 				
 				Widgets.get(1253, 343).click(true);
 				
 				try {
 					
 					Squeal.logPrize(Squeal.getCurrentAccount() + " claimed prize: ");
 					Squeal.logPrize(prize);
 					
 					Squeal.flushLog();
 					
 				} catch (Exception e1) {
 					
 					e1.printStackTrace();
 					
 				}
 				
 				System.out.println(Squeal.getCurrentAccount() + " claimed: \n" + prize);
 				
 				Time.sleep(1000, 1500);
 			
 				
 			}
 			
 		}
 		
 	}
 	
 	public boolean validate() {
 		
		return (Widgets.get(1139, 2).isOnScreen() || Widgets.get(1253, 181).isOnScreen() || Widgets.get(1253, 259).isOnScreen()) && !Widgets.get(1322, 8).isOnScreen() && !Widgets.get(1313, 11).isOnScreen() && !Widgets.get(1337, 26).isOnScreen();
 		
 	}
 
 }
