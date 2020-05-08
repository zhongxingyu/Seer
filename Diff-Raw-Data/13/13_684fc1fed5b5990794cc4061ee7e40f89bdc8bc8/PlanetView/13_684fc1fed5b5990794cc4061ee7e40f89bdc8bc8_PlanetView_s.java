 package com.cs2340.spacetrader;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PlanetView extends FragmentActivity{
 	private Context context = this;
 	public Contract contract;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_planet);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         Resources rs = getResources();
         LinearLayout pv = (LinearLayout) findViewById(R.id.planet_layout);
         if (GameSetup.theMap.getPlanet(GameSetup.thePlayer.getship().getPlanetName()).getNTechLevel() > 2){
         	pv.setBackgroundDrawable(rs.getDrawable((R.drawable.hightechback)));
         }
         else{
         	pv.setBackgroundDrawable(rs.getDrawable((R.drawable.lowtechback)));
         }
         
         TextView name = (TextView) findViewById(R.id.planet_current_planet);
         name.setText(GameSetup.thePlayer.getship().getPlanetName());
         
         Button contractButton = (Button) findViewById(R.id.planet_button_contract);
         if (GameSetup.thePlayer.hasContract){        	
         	contractButton.setEnabled(false);
         }
         else{
         	contractButton.setEnabled(true);
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
         getMenuInflater().inflate(R.menu.menu_options, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) 
     {
         switch (item.getItemId()) {
             case R.id.save_game:
             	item.setEnabled(false);
             	item.setTitle("Saving");
             	SaveState state = new SaveState(GameSetup.thePlayer, GameSetup.theMap);
             	if (MemoryService.saveGame(state, this))
         		{
             		Toast.makeText(this, "Game saved successfully", Toast.LENGTH_SHORT).show();
         		}
             	else
             	{
             		Toast.makeText(this, "Failed to save game", Toast.LENGTH_SHORT).show();
             	}
             	item.setTitle("Save");
             	item.setEnabled(true);
                 return true;
             case R.id.quit_game:
             	Intent intent = new Intent(this, Launcher.class);
             	startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     /**
      * Continue to TradeView from PlanetView
      * @param view PlanetView
      */
     public void startTradeView(View view)
     {
     	Intent intent = new Intent(PlanetView.this, TradeView.class);
     	startActivity(intent);
     }
 
     /**
      * GUI returns to Space from PlanetView
      * @param view PlanetView
      */
     public void startSpaceView(View view)
     {
     	Intent intent = new Intent(PlanetView.this, Space.class);
     	startActivity(intent);
     }
     
     /**
      * Continue to UpgradeShipView
      * @param view
      */
     public void upgradeShip(View view)
     {
     	Intent intent = new Intent(PlanetView.this, UpgradeShipView.class);
     	startActivity(intent);
     }
     
     /**
      * Randomly generates the contract
      */
     public void createContract()
     {
     	contract = GameSetup.theMap.getPlanet(GameSetup.thePlayer.getship().getPlanetName()).getContract();
     	contract.generateContract();	//add param "toGo", "fightPirate", "bringGood" to test specific contracts
     }
     
     /**
      * Displays a dialog to accept or reject a contract
      * @param view
      */
     public void lookForContract(View view){
     	createContract();
     	
     	FragmentManager fm = getSupportFragmentManager();
         ContractFragment editNameDialog = new ContractFragment();
         editNameDialog.dialog = contract.getString();
         editNameDialog.show(fm, "fragment_contract");
     }
     
     /**
      * Class for creating the dialog box to accept/reject a contract
      * @author David
      *
      */
     private class ContractFragment extends DialogFragment {
     	public String dialog = "";
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Use the Builder class for convenient dialog construction
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             
             builder.setTitle(R.string.contract_title);
             builder.setMessage(dialog);
             builder.setPositiveButton(R.string.contract_accept, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                     	   	GameSetup.thePlayer.hasContract = true;
                        		GameSetup.thePlayer.setContract(contract);
                        		Button contractButton = (Button) findViewById(R.id.planet_button_contract);
                        		contractButton.setEnabled(false);
                        		Toast.makeText(context, "You've accepted the contract", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.contract_decline, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
             // Create the AlertDialog object and return it
             return builder.create();
         }
     }
 }
