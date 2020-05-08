 package squeal.task;
 
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.util.Time;
 
 import squeal.Squeal;
 
 public class UseSpin extends Strategy implements Runnable {
 
 	private final int SPIN_BUTTON = 89;
 	private final int CLAIM_BUTTON = 186;
 	private final int IS_CLAIMED = 184;
 	private final int DISCARD_BUTTON = 236;
 	private final int CONVERT_COIN_TEXT = 45;
 	private final int CONFIRM_DISCARD = 295; 
 	private final int IS_DISCARDED = 231;
 	private final int DONE = 237;
 	private final int PRIZE_TEXT = 167;
 	
 	@Override
 	public void run() {
 		
 		if(Widgets.get(1139, 2).isOnScreen()) {
 			
 			if(Widgets.get(1139, 6).getText().equals("0") && !Widgets.get(1252, 3).isOnScreen()) {
 				
 				if(Widgets.get(548, 160).click(true) && !Widgets.get(1322, 8).isOnScreen()) {
 					
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
 					
					while(!Widgets.get(1316, 19).isOnScreen() && !Widgets.get(1322, 8).isOnScreen() && !Widgets.get(1313, 11).isOnScreen() && !Widgets.get(1337, 26).isOnScreen() && (!Widgets.get(1253, SPIN_BUTTON).isOnScreen() && Widgets.get(1252, 3).isOnScreen()) && !Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) {
 						
 						Widgets.get(1252, 3).click(true);
 						
 						Time.sleep(1000, 1500);
 						
 					}
 					
 				} else {
 					
 					while((!Widgets.get(1253, SPIN_BUTTON).isOnScreen() && Widgets.get(1139, 2).isOnScreen()) && !Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) {
 				
 						Widgets.get(1139, 7).click(true);
 				
 						Time.sleep(1000, 2000);
 				
 					}
 					
 				}
 			
 			}
 			
 		}
 		
 		//click the spin button
 		
 		while((Widgets.get(1253, SPIN_BUTTON).isOnScreen() && !Widgets.get(1253, CLAIM_BUTTON).isOnScreen())) {
 			
 			Widgets.get(1253, SPIN_BUTTON).click(true);
 			
 			Time.sleep(100, 200);
 			
 		}
 		
 		if(Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) {
 			
 			String prize = Widgets.get(1253, PRIZE_TEXT).getText();
 			
 			if(Widgets.get(1253, CLAIM_BUTTON).isOnScreen() && Widgets.get(1253, CLAIM_BUTTON).getText().startsWith("Subscribe")) {
 				
 				while(Widgets.get(1253, DISCARD_BUTTON).isOnScreen() && !Widgets.get(1253, IS_DISCARDED).isOnScreen()) {
 					
 					Widgets.get(1253, DISCARD_BUTTON).click(true);
 					
 					Time.sleep(1000, 1500);
 					
 					if(Widgets.get(1253, CONFIRM_DISCARD).isOnScreen()) {
 						
 						Widgets.get(1253, CONFIRM_DISCARD).click(true);
 						
 						Time.sleep(1000, 1500);
 						
 					}
 					
 				}
 					
 				Widgets.get(1253, DONE).click(true); //done
 				
 				Time.sleep(1000, 1500);
 
 				try {
 					
 					Squeal.logPrize(Squeal.getCurrentId() + " discarded prize: ");
 					Squeal.logPrize(prize);
 
 					Squeal.flushLog();
 					
 				} catch (Exception e1) {
 					
 					e1.printStackTrace();
 					
 				}
 							
 				System.out.println("Discarded prize");
 					
 			} else {
 				
 				if(Squeal.getConvertOption()) {
 					
 					if(Squeal.getFishMaskOption()) {
 						
 						if(prize.startsWith("Not bad! You won: Fish mask")) {
 							
 							if(Widgets.get(1253, IS_CLAIMED).isOnScreen() && !Widgets.get(1253, IS_DISCARDED).isOnScreen()) {
 								
 								while(Widgets.get(1253, DISCARD_BUTTON).isOnScreen() && !Widgets.get(1253, IS_DISCARDED).isOnScreen()) {
 									
 									Widgets.get(1253, DISCARD_BUTTON).click(true);
 									
 									Time.sleep(1000, 1500);
 									
 									if(Widgets.get(1253, CONFIRM_DISCARD).isOnScreen()) {
 										
 										String p = Widgets.get(1253, CONVERT_COIN_TEXT).getText();
 										
 										Widgets.get(1253, CONFIRM_DISCARD).click(true);
 										
 										Time.sleep(1000, 1500);
 										
 										prize = p + " from " + prize;
 										
 									}
 									
 								}
 								
 							} else {
 								
 								while(!Widgets.get(1253, IS_CLAIMED).isOnScreen() && Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) {
 								
 									Widgets.get(1253, CLAIM_BUTTON).click(true); 
 										
 									Time.sleep(1000, 1500);
 									
 								}
 								
 							}
 							
 						} else {
 							
 							if(Widgets.get(1253, DISCARD_BUTTON).getText().startsWith("Convert")) {
 								
 								while(Widgets.get(1253, DISCARD_BUTTON).isOnScreen() && !Widgets.get(1253, IS_DISCARDED).isOnScreen()) {
 									
 									Widgets.get(1253, DISCARD_BUTTON).click(true);
 									
 									Time.sleep(1000, 1500);
 									
 									if(Widgets.get(1253, CONFIRM_DISCARD).isOnScreen()) {
 										
 										String p = Widgets.get(1253, CONVERT_COIN_TEXT).getText();
 										
 										Widgets.get(1253, CONFIRM_DISCARD).click(true);
 										
 										Time.sleep(1000, 1500);
 										
 										prize = p + " from " + prize;
 										
 									}
 									
 								}
 								
 							} else {
 								
 								while(!Widgets.get(1253, IS_CLAIMED).isOnScreen() && Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) {
 									
 									Widgets.get(1253, CLAIM_BUTTON).click(true); 
 										
 									Time.sleep(1000, 1500);
 									
 								}
 								
 							}
 							
 						}
 						
 					} else {
 						
 						if(Widgets.get(1253, DISCARD_BUTTON).getText().startsWith("Convert")) {
 							
 							while(Widgets.get(1253, DISCARD_BUTTON).isOnScreen() && !Widgets.get(1253, IS_DISCARDED).isOnScreen()) {
 								
 								Widgets.get(1253, DISCARD_BUTTON).click(true);
 								
 								Time.sleep(1000, 1500);
 								
 								if(Widgets.get(1253, CONFIRM_DISCARD).isOnScreen()) {
 									
 									String p = Widgets.get(1253, CONVERT_COIN_TEXT).getText();
 									
 									Widgets.get(1253, CONFIRM_DISCARD).click(true);
 									
 									Time.sleep(1000, 1500);
 									
 									prize = p + " from " + prize;
 									
 								}
 								
 							}
 							
 						} else {
 							
 							while(!Widgets.get(1253, IS_CLAIMED).isOnScreen() && Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) {
 								
 								Widgets.get(1253, CLAIM_BUTTON).click(true); 
 									
 								Time.sleep(1000, 1500);
 								
 							}
 							
 						}
 						
 					}
 					
 					
 				} else {
 					
 					if(Widgets.get(1253, IS_CLAIMED).isOnScreen() && !Widgets.get(1253, IS_DISCARDED).isOnScreen()) {
 						
 						while(Widgets.get(1253, DISCARD_BUTTON).isOnScreen() && !Widgets.get(1253, IS_DISCARDED).isOnScreen()) {
 							
 							Widgets.get(1253, DISCARD_BUTTON).click(true);
 							
 							Time.sleep(1000, 1500);
 							
 							if(Widgets.get(1253, CONFIRM_DISCARD).isOnScreen()) {
 								
 								String p = Widgets.get(1253, CONVERT_COIN_TEXT).getText();
 								
 								Widgets.get(1253, CONFIRM_DISCARD).click(true);
 								
 								Time.sleep(1000, 1500);
 								
 								prize = p + " from " + prize;
 								
 							}
 							
 						}
 						
 					} else {
 						
 						while(!Widgets.get(1253, IS_CLAIMED).isOnScreen() && Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) {
 						
 							Widgets.get(1253, CLAIM_BUTTON).click(true); 
 								
 							Time.sleep(1000, 1500);
 							
 						}
 						
 					}
 						
 				}
 				
 				Widgets.get(1253, DONE).click(true);
 				
 				try {
 					
 					Squeal.logPrize(Squeal.getCurrentId() + " claimed prize: ");
 					Squeal.logPrize(prize);
 					
 					Squeal.flushLog();
 					
 				} catch (Exception e1) {
 					
 					e1.printStackTrace();
 					
 				}
 				
 				System.out.println(Squeal.getCurrentId() + " claimed: \n" + prize);
 				
 				Time.sleep(1000, 1500);
 			
 				
 			}
 			
 		}
 		
 	}
 	
 	public boolean validate() {
 		
 		return (Widgets.get(1139, 2).isOnScreen() || Widgets.get(1253, SPIN_BUTTON).isOnScreen() || Widgets.get(1253, CLAIM_BUTTON).isOnScreen()) && !Widgets.get(1322, 8).isOnScreen() && !Widgets.get(1313, 11).isOnScreen() && !Widgets.get(1337, 26).isOnScreen() && !Widgets.get(1316, 19).isOnScreen();
 		
 	}
 
 }
