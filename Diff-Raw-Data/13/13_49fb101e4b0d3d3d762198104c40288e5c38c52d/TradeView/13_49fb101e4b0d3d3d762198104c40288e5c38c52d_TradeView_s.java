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
         //sInventory = new ShipInventory(100, 10);
         sInventory = GameSetup.thePlayer.getship().getInventory();
         market = new MarketVisit(sInventory, planet);
         
         //slots on display to display current balances
         cashDisplay = (TextView) findViewById(R.id.trade_cash);
        capacityDisplay = (TextView) findViewById(R.id.trade_capacity); 
        
         //initialize values in slots
        //refreshDisplays();
     }
     
     private void refreshDisplays()
     {
    	cashDisplay.setText('$' + sInventory.getMoneyLeft());
        capacityDisplay.setText(sInventory.getCapacityLeft());
     }
     
     public void buy(View view)
     {
     	//quick weedouts
     	//if (sInventory.getMoneyLeft() <= 0) return;
     	//if (sInventory.getCapacityLeft() <= 0) return;
     	
     	//market.buyFromPlanet(water, 1);
     	
     	//refreshDisplays();
     }
     
     public void sell(View view)
     {
     	//quick weedout
     	//if (sInventory.getCapacityLeft() == 0) return;
     	
     	//market.sellToPlanet(water, 1);
     	
     	//refreshDisplays();
     }
     
     public void gotoPlanet(View view)
     {
     	Intent intent = new Intent(TradeView.this, PlanetView.class);
         startActivity(intent);
     }
 }
