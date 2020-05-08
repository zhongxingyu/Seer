 package com.example.skidrow.activities;
 
 import com.example.skidrow.AppUtil;
 import com.example.skidrow.R;
 import com.example.skidrow.activities.MarketActivity.Person;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class GasRepairShopActivity extends Activity {
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.gas_repair_shop);
         fillGasStationInfo();
     	final Button gasButton=(Button)this.findViewById(R.id.button1);
     	gasButton.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View v) {
 				gasTransaction();
 			}
 
     	});
         
     }
 	public void changeGameLayout(View view){
     	int viewId = view.getId();
     	
     	Intent intent;
     	switch(viewId){
     		case R.id.playerStatsView:
     			intent = new Intent(this, PlayerStatsActivity.class);
     			break;
     		case R.id.cityView:
     			intent = new Intent(this, MapActivity.class);
     			break;
     		case R.id.marketView:
     			intent = new Intent(this, MarketActivity.class);
     			break;
     		case R.id.repairShopView:
                 intent = new Intent(this, RepairShopActivity.class);
                 break;
     		case R.id.shipRepairShopView:
     			intent = new Intent(this, ShipRepairShopActivity.class);
     			break;
     		default:
     			intent = new Intent(this, PlayerStatsActivity.class);
     			break;
     	}
     	startActivity(intent);
     }
 	public void fillGasStationInfo(){
 		TextView money= (TextView)this.findViewById(R.id.money);
 		TextView fuelPrice= (TextView)this.findViewById(R.id.fuelPrice);
 		TextView fuel= (TextView)this.findViewById(R.id.fuel);
 		TextView fuelCapacity= (TextView)this.findViewById(R.id.fuelCapacity);
 		TextView fuelEfficiency= (TextView)this.findViewById(R.id.fuelEfficiency);
 		money.setText("Money: $"+AppUtil.game.getMoney());
 		fuelPrice.setText("Fuel Price: $"+AppUtil.game.getGasPrice(AppUtil.game.getCurrentCity()));
 		fuel.setText("Fuel left: "+AppUtil.game.getGas());
 		fuelCapacity.setText("Fuel Capacity: "+AppUtil.game.getShip().getFuelCapacity());
 		fuelEfficiency.setText("Fuel Efficiency: "+AppUtil.game.getShip().getFuelEfficiency());
 	}
 	private void gasTransaction(){
     	
     	final double gasPrice=AppUtil.game.getGasPrice(AppUtil.game.getCurrentCity());
     	final double availableSpace=AppUtil.game.getShip().getFuelCapacity()-AppUtil.game.getGas();
     	//Setting all the appropriate listeners for the dialog layout
     	LayoutInflater inflater = this.getLayoutInflater();
    	final RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.buy_gas_popup, null);
    	final TextView goodCost = (TextView)layout.findViewById(R.id.fuelCost);
     	final TextView quantity = (TextView)layout.findViewById(R.id.quantityValue);
     	final TextView playerMoneyView = (TextView)layout.findViewById(R.id.playerMoney);
    	final TextView availableCargo = (TextView)layout.findViewById(R.id.availableFuel);
    	final TextView textView2 = (TextView)layout.findViewById(R.id.textView7);
    	textView2.setText("Available: ");
     	goodCost.setText("$"+gasPrice);
     	playerMoneyView.setText(AppUtil.game.getPlayerStatInfo()[7]);
    	availableCargo.setText(String.format("%.2f", availableSpace));
     	SeekBar slider = (SeekBar)layout.findViewById(R.id.buySellQuantity);
     	slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				double buySellQuant =  ((progress*availableSpace)/100);
 				Log.i("GasReapirShop","Progess: "+buySellQuant);
 				String playerMoneyStr = AppUtil.game.getPlayerStatInfo()[7];
 				double playerMoney = Integer.parseInt((String) playerMoneyStr.subSequence(1, playerMoneyStr.length()));
				availableCargo.setText(String.format("%.2f", Math.max(availableSpace-buySellQuant,0)));
				quantity.setText(String.format("%.2f", buySellQuant));
 				playerMoney = playerMoney - buySellQuant * gasPrice;
				textView2.setText("Available: ");
				playerMoneyView.setText("$"+String.format("%.2f",playerMoney));
 			}
 		});
     	
         AlertDialog.Builder popup = new AlertDialog.Builder(this);
         popup.setView(layout)
         	.setTitle("Buy Gas")
 			.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
 
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 			})
 			.setPositiveButton("Buy", new DialogInterface.OnClickListener() {
 				
 				
 				
 				public void onClick(DialogInterface dialog, int which) {
 					buyGas(Double.parseDouble(quantity.getText().toString()));
 					fillGasStationInfo();
 				}
 			});
         popup.create().show();
     }
 	public void buyGas(double quant) {
 		 if(quant*AppUtil.game.getGasPrice(AppUtil.game.getCurrentCity())<AppUtil.game.getMoney()){
 			 AppUtil.game.setMoney((int)(AppUtil.game.getMoney()-quant*AppUtil.game.getGasPrice(AppUtil.game.getCurrentCity())));
 			 AppUtil.game.setGas(AppUtil.game.getGas()+quant);
 			 showMessage("Transaction Completed");
 		 } else{
 			 showMessage("Not enough money!");
 		 }
 	}
 	public void showMessage(String message){
  		LayoutInflater inflater = this.getLayoutInflater();
  		final LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.message_popup, null);
  		AlertDialog.Builder popup2 = new AlertDialog.Builder(this, 0);
  		popup2.setView(layout).setTitle(message).setNegativeButton("Ok", new DialogInterface.OnClickListener(){
 			
 	    	 public void onClick(DialogInterface dialog, int which) {
 						
 					}
 					
 			 });
  		popup2.create().show();
  	}
 }
