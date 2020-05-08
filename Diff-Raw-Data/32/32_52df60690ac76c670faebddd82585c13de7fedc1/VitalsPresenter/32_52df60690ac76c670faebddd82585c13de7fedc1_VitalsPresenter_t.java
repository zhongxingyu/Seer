 package edu.gatech.statusquo.spacetrader.presenter;
 
 import org.eclipse.swt.widgets.Shell;
 
 import edu.gatech.statusquo.spacetrader.driver.*;
 import edu.gatech.statusquo.spacetrader.model.Good;
 import edu.gatech.statusquo.spacetrader.model.Player;
 import edu.gatech.statusquo.spacetrader.model.Ship;
 import edu.gatech.statusquo.spacetrader.view.*;
 
 public class VitalsPresenter {
 	Shell shell;
 	Driver driver;
 	static VitalsView vitalsView;
 	TradeGoodsView tradeGoodsView;
 	static Player player;
 	static Ship ship;
 	
 	/**
 	 * class constructor using listed parameters
 	 * @param s
 	 * @param d
 	 * @param vv
 	 * @param p
 	 * @param tgv
 	 */
 	public VitalsPresenter(Shell s, Driver d, VitalsView vv, Player p, TradeGoodsView tgv) {
 		this.shell = s;
 		this.driver = d;
 		VitalsPresenter.vitalsView = vv;
 		VitalsPresenter.player = p;
 		this.tradeGoodsView = tgv;
 		ship = player.getShip();
 		
 		setPlayerVitals();
 		setShipVitals();
 	}
 	
 	/**
 	 * Sets the players currency and the ships fuel for view by user
 	 */
 	public static void setPlayerVitals() {
 		Ship temp = player.getShip();
 		int currentFuelLvl = temp.getFuelLevel();
 		String[] playerState = {Integer.toString(Player.getCurrency()), Integer.toString(currentFuelLvl)};
 		vitalsView.tableItem.setText(playerState);
 	}
 	
 	/**
 	 * Sets view for cargo bay by user and displays what it contains
 	 */
 	public static void setShipVitals() {
 		vitalsView.cargoList.removeAll();
 		Good[] cargoBay = ship.getCargoBay();
 		for (int i = 0; i < cargoBay.length; i++) {
 			if (cargoBay[i] != null) {
				Good good = cargoBay[i];
				String cargo = good.toString();
 				vitalsView.cargoList.add(cargo);
 			}
 		}
 	}
 }
