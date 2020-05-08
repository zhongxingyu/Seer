 package EpicsChins.util.tasks;
 
 import EpicsChins.util.Data;
 import EpicsChins.util.GUI;
 import EpicsChins.util.Method;
 import EpicsChins.util.Tiles;
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.tab.Equipment;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Bank;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Filter;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.node.Item;
 import org.powerbot.game.bot.Context;
 
 /**
  * User: Epics
  * Date: 8/28/12
  * Time: 8:21 PM
  */
 public class Banking extends Strategy implements Runnable {
 
 	@Override
 	public void run() {
 
 		int antipoisonData = 0;
 		int flaskRenewalCountData = 0;
 		int prayerPotCountData = 0;
 		int rangingFlaskData = 0;
 		int rangingFlaskExtremeData = 0;
 
 		for (Item y : Inventory.getItems()) {
 			for (int x : Data.ANTIPOISON_ALL) {
 				if (y.getId() == x) {
 					antipoisonData++;
 				}
 			}
 			for (int x : Data.FLASK_PRAYER_RENEWAL) {
 				if (y.getId() == x) {
 					flaskRenewalCountData++;
 				}
 			}
 			for (int x : Data.POT_PRAYER) {
 				if (y.getId() == x) {
 					prayerPotCountData++;
 				}
 			}
 			for (int x : Data.FLASK_RANGING) {
 				if (y.getId() == x) {
 					rangingFlaskData++;
 				}
 			}
 			for (int x : Data.FLASK_RANGING_EXTREME) {
 				if (y.getId() == x) {
 					rangingFlaskExtremeData++;
 				}
 			}
 		}
 
 		Item greegree = Inventory.getItem(Data.GREEGREE_IDS);
 
 		if (Data.logBankingCode == 0) {
 			Context.get().getActiveScript().log.info("Running banking code");
 			Data.logBankingCode++;
 		}
 
 		if (Tiles.AREA_GRAND_TELE.contains(Players.getLocal().getLocation())) {
 			Context.get().getActiveScript().log.info("You have the n00b varrock teleport. Walking to bank");
 		}
 
 		if (!Tiles.AREA_GE.contains(Players.getLocal().getLocation()) && (!Tiles.AREA_GRAND_TELE.contains(Players.getLocal().getLocation()))) {
 			Method.breakTab();
 		}
 
 		Method.checkRun();
 		if (!Bank.isOpen()) {
 			if (Inventory.getCount(10034) > 0) {
 				Context.get().getActiveScript().log.info("Chins present in inventory, equipping...");
 				Item chinItem = Inventory.getItem(10034);
 				chinItem.getWidgetChild().click(true);
 				Time.sleep(2000, 4000);
 			}
 			if (Calculations.distanceTo(Tiles.TILE_GRAND_BANK) >= 4) {
 				Walking.findPath(Tiles.TILE_GRAND_BANK).traverse();
 				Method.runState();
 				Camera.turnTo(Tiles.TILE_GRAND_BANK);
 				Players.getLocal().isMoving();
 			} else if (Calculations.distanceTo(Tiles.TILE_GRAND_BANK) <= 8) {
 				Bank.open();
 			}
 		}
 
 		if (Data.chinNumber < 2000 && Bank.isOpen()) {
 			if (Data.chinNumber == 0) {
 				Context.get().getActiveScript().log.info("NO chins detected. Let's get some.");
 			}
 			if (Data.chinNumber < 2000 && Bank.withdraw(10034, 2000)) {
 				Time.sleep(80);
 				Bank.close();
 				if (!Bank.isOpen()) {
 					Context.get().getActiveScript().log.info("Recalculating chin count");
 					Data.runCheck = true;
 					Timer t = new Timer(2500);
 					while (t.isRunning()) {
 						Time.sleep(50);
 					}
 				}
 			} else if (Bank.getItem(10034).getStackSize() <= 1500 && Data.chinNumber < 2000) {
 				Context.get().getActiveScript().log.info("Not enough chins to continue! Shutting down...");
 				Game.logout(true);
 				Context.get().getActiveScript().stop();
 			}
 			if (Bank.close()) {
 				if (Inventory.getCount(10034) >= 0) {
 					Item chinItem = Inventory.getItem(10034);
 					chinItem.getWidgetChild().click(true);
 					if (Inventory.getCount(10034) < 1) {
 						Data.chinNumber = Equipment.getItem(10034).getStackSize();
 					}
 				}
 			}
 		}
 		if (Bank.isOpen()) {
 			if (greegree == null && Bank.isOpen()) {
 				Item greegreeItem = Bank.getItem(new Filter<Item>() {
 					@Override
 					public boolean accept(final Item m) {
 						for (int id : Data.GREEGREE_IDS) {
 							if (m.getId() == id) {
 								return true;
 							}
 						}
 						return false;
 					}
 				});
 				if (greegreeItem != null && Inventory.getCount(greegreeItem.getId()) == 0 && Data.usingGreegree && Bank.getItemCount(greegreeItem.getId()) >= 1) {
 					Bank.withdraw(greegreeItem.getId(), 1);
 					Time.sleep(80);
 					if (Inventory.getCount(greegreeItem.getId()) != 0) {
 						Context.get().getActiveScript().log.info("Greegree withdrawn");
 					}
 				} else if (Bank.getItemCount(greegreeItem.getId()) == 0 && Data.usingGreegree && Inventory.getCount(greegreeItem.getId()) == 0) {
 					Context.get().getActiveScript().log.info("No greegree is present. Shutting down...");
 					Bank.close();
 					Game.logout(true);
 					Context.get().getActiveScript().stop();
 				}
 
 			}
 
 			if (Inventory.getCount(Data.TAB_VARROCK) == 0 && Bank.getItem(Data.TAB_VARROCK).getStackSize() > 0) {
 				Bank.withdraw(Data.TAB_VARROCK, 1);
 				Time.sleep(80);
 				if (Inventory.getCount(Data.TAB_VARROCK) != 0) {
 					Context.get().getActiveScript().log.info("Varrock tab withdrawn");
 				} else if (Bank.getItem(Data.TAB_VARROCK).getStackSize() == 0 && Inventory.getCount(Data.TAB_VARROCK) < 1) {
 					Context.get().getActiveScript().log.info("Not enough tabs. Shutting down...");
 					Bank.close();
 					Game.logout(true);
 					Context.get().getActiveScript().stop();
 				}
 			}
 
 			if (Inventory.getCount(GUI.foodUser) == 0 && Bank.getItemCount(GUI.foodUser) >= 1) {
 				Bank.withdraw(GUI.foodUser, 1);
 				Time.sleep(80);
 				if (Inventory.getCount(GUI.foodUser) != 0) {
 					Context.get().getActiveScript().log.info("Food withdrawn");
 				}
 			} else if (Bank.getItem(GUI.foodUser).getStackSize() <= 1 && Inventory.getCount(GUI.foodUser) < 1) {
 				Context.get().getActiveScript().log.severe("Food isn't present, shutting down...");
 				Bank.close();
 				Game.logout(true);
 				Context.get().getActiveScript().stop();
 			}
 
 			if (antipoisonData == 0 && Bank.getItemCount(GUI.antipoisonUser) > 0) {
 				Bank.withdraw(GUI.antipoisonUser, 1);
 				Time.sleep(80);
 				if (Inventory.getCount(GUI.antipoisonUser) == 1) {
 					Context.get().getActiveScript().log.info("Antipoison withdrawn");
 				} else if (Bank.getItem(GUI.antipoisonUser).getStackSize() < 1 && antipoisonData < 1) {
 					Context.get().getActiveScript().log.info("Not enough antipoison. Shutting down...");
 					Bank.close();
 					Game.logout(true);
 					Context.get().getActiveScript().stop();
 				}
 				return;
 			}
 			if (prayerPotCountData == 0) {
 				Bank.withdraw(Data.POT_PRAYER_DOSE_4, 18);
 				Time.sleep(80);
				if (prayerPotCountData == 3) {
 					Context.get().getActiveScript().log.info("Prayer pots withdrawn");
 				} else if (Bank.getItem(Data.POT_PRAYER_DOSE_4).getStackSize() < 18 && prayerPotCountData < 18) {
 					Context.get().getActiveScript().log.info("Not enough prayer pots. Shutting down... ");
 					Bank.close();
 					Game.logout(true);
 					Context.get().getActiveScript().stop();
 				}
 				return;
 			}
 			if (flaskRenewalCountData == 0) {
 				Bank.withdraw(Data.FLASK_PRAYER_RENEWAL_FULL, 3);
 				Time.sleep(80);
				if (flaskRenewalCountData == 3) {
 					Context.get().getActiveScript().log.info("Renewal flasks withdrawn");
 				} else if (Bank.getItem(Data.FLASK_PRAYER_RENEWAL_FULL).getStackSize() < 3 && flaskRenewalCountData < 3) {
 					Context.get().getActiveScript().log.info("Not enough prayer renewal flasks. Shutting down...");
 					Bank.close();
 					Game.logout(true);
 					Context.get().getActiveScript().stop();
 				}
 				return;
 			}
 			if (rangingFlaskExtremeData == 0 || rangingFlaskData == 0) {//TODO implement extreme
 				if(Bank.getItem(Data.FLASK_RANGING_EXTREME_FULL).getStackSize() > 3){
 					Bank.withdraw(Data.FLASK_RANGING_EXTREME_FULL, 3);
 					Time.sleep(80);
 					Data.withdrawedRangingExtreme = true;
 				}
 
				if(!Data.withdrawedRangingExtreme && Bank.getItem(Data.FLASK_RANGING_FULL).getStackSize() > 3){
 				Bank.withdraw(Data.FLASK_RANGING_FULL, 3);
 					Time.sleep(80);
 					Data.withdrawedRangingExtreme = false;

 				}
 
 				if (rangingFlaskData == 3 || rangingFlaskExtremeData == 3) {
 					Context.get().getActiveScript().log.info("Range flasks withdrawn");
 				} else if (Bank.getItem(Data.FLASK_RANGING_FULL).getStackSize() < 3 && rangingFlaskData < 3 || Bank.getItem(Data.FLASK_RANGING_EXTREME_FULL).getStackSize() < 3 && rangingFlaskExtremeData < 3) {
 					Context.get().getActiveScript().log.info("Not enough ranged flasks. Shutting down...");
 					Bank.close();
 					Game.logout(true);
 					Context.get().getActiveScript().stop();
 				}
 				return;
 			}
 			if (Inventory.getCount() == 28) {
 				Bank.close();
 			} else {
 				Context.get().getActiveScript().log.info("Banking was done wrongly, retrying...");
 				Bank.depositInventory();
 			}
 		}
 		if (Players.getLocal().getHpPercent() <= 70) {
 			Context.get().getActiveScript().log.info("HP is low when banking, eating");
 			Bank.open();
 			Time.sleep(Random.nextInt(500, 700));
 			if (Bank.isOpen()) {
 				Bank.depositInventory();
 				Bank.withdraw(GUI.foodUser, 2);
 				Bank.close();
 			}
 			final Item FOOD = Inventory.getItem(GUI.foodUser);
 			if (FOOD.getWidgetChild().interact("Eat")) {
 				Time.sleep(Random.nextInt(900, 1200));
 			}
 		}
 		Data.logBankingCode = 0;
 	}
 
 	@Override
 	public boolean validate() {
 		int antipoisonData = 0;
 		int flaskRenewalCountData = 0;
 		int flaskRangeCountData = 0;
 		int prayerPotCountData = 0;
 
 		for (Item y : Inventory.getItems()) {
 			for (int x : Data.ANTIPOISON_ALL) {
 				if (y.getId() == x) {
 					antipoisonData++;
 				}
 			}
 			for (int x : Data.FLASK_PRAYER_RENEWAL) {
 				if (y.getId() == x) {
 					flaskRenewalCountData++;
 				}
 			}
 			for (int x : Data.FLASK_RANGING) {
 				if (y.getId() == x) {
 					flaskRangeCountData++;
 				}
 			}
 			for (int x : Data.POT_PRAYER) {
 				if (y.getId() == x) {
 					prayerPotCountData++;
 				}
 			}
 		}
 		/*
 		Context.get().getActiveScript().log.info("ANTIPOISON: " + antipoisonData);
 		Context.get().getActiveScript().log.info("Are we poisoned: " + Method.isPoisoned());
 		Context.get().getActiveScript().log.info("FLASK RENEWAL: " + flaskRenewalCountData);
 		Context.get().getActiveScript().log.info("PRAYER POTS: " + prayerPotCountData);
 		Context.get().getActiveScript().log.info("TABS: " + Inventory.getCount(Data.TAB_VARROCK));
 		Context.get().getActiveScript().log.info("CHINS: " + Data.chinNumber);
 		Context.get().getActiveScript().log.info("Checking shit?: " + Data.runCheck);
 		Context.get().getActiveScript().log.info("Starting shit?: " + Data.START_SCRIPT);
 		*/
 		return (((Method.isPoisoned() && antipoisonData == 0)) || antipoisonData == 0 || flaskRenewalCountData == 0 || prayerPotCountData == 0 || flaskRangeCountData == 0 || Inventory.getCount(Data.TAB_VARROCK) < 1 || Data.chinNumber == 0 || Players.getLocal().getHpPercent() <= 25) && Game.isLoggedIn() && !Data.runCheck && Data.START_SCRIPT;
 	}
 }
