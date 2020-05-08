 package com.cs2340.spacetrader;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class TradeView extends Activity
 {
 	private ShipInventory sInventory;
 	private TextView cashDisplay;
 	private TextView capacityDisplay;
 	private Planet planet;
 	private MarketVisit market;
 	
 	private Good water;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_trade);
         
         planet = GameSetup.theMap.getPlanetArray()[0];
         sInventory = GameSetup.thePlayer.getship().getInventory();
         market = new MarketVisit(sInventory, planet);
         //market.checkIn();
         
         //slots on display to display current balances
         cashDisplay = (TextView) findViewById(R.id.trade_cash);
         capacityDisplay = (TextView) findViewById(R.id.trade_capacity);
 
 
         //initialize values in slots
         refreshDisplays();
     }
     
     private void refreshDisplays()
     {
 		cashDisplay.setText('$' + String.valueOf(sInventory.getMoneyLeft()));
 		capacityDisplay.setText(String.valueOf(sInventory.getCapacityLeft()));
     }
     
     public void buy(View view)
     {
     	//quick weedouts
    	if (market.getShipInventory().getMoneyLeft() <= 0) return;
    	if (market.getShipInventory().getCapacityLeft() <= 0) return;
    	Good theGood = market.getPlanetInventory().getGood("Water");
    	if (market.getPlanetInventory().getGoodFromInventory(theGood).getQuantity() <= 0) return;
 
     	market.buyFromPlanet(market.getPlanetInventory().getGood("Water"), 1);
     	
     	refreshDisplays();
     }
     
     public void sell(View view)
     {
     	//quick weedout
     	if (sInventory.getCapacityLeft() >= sInventory.getCapacityMax()) return;
     	
     	market.sellToPlanet(market.getPlanetInventory().getGood("Water"), 1);
     	
     	refreshDisplays();
     }
     
     public void gotoPlanet(View view)
     {
     	Intent intent = new Intent(TradeView.this, PlanetView.class);
         startActivity(intent);
     }
 }
