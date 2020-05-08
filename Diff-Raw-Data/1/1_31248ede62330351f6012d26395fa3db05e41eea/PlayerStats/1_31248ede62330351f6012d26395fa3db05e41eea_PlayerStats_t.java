 package dk.stacktrace.risk.gui;
 
 import dk.stacktrace.risk.controller.Controller;
 import dk.stacktrace.risk.game_logic.Player;
 import android.content.Context;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class PlayerStats extends LinearLayout
 {
 	Player player;
 	Controller control;
 	TextView numOfTerritories, expectedReinforcement, numOfContinents, numOfTroopsToDeploy, numOfTroopsOwned, troopsKilledByPlayer, playersTroopsKilled;
 	private android.widget.RelativeLayout.LayoutParams playerStatsLayoutParams;
 
 	public PlayerStats(Context context, Controller control, RelativeLayout mainLayout)
 	{
 		super(context);
 		this.control = control;
 		player = control.getActivePlayer();
 		
 		numOfTerritories = new TextView(context);
 		expectedReinforcement = new TextView(context);
 		numOfContinents = new TextView(context);
 		numOfTroopsToDeploy = new TextView(context);
 		numOfTroopsOwned = new TextView(context);
 		troopsKilledByPlayer = new TextView(context);
 		playersTroopsKilled = new TextView(context);
 		
 		
 		// Player Stats Layout Parameters 
 		float density = getResources().getDisplayMetrics().density;
 		playerStatsLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		playerStatsLayoutParams.leftMargin = (int) (5 * density);
 		playerStatsLayoutParams.topMargin = (int) (20 * density);
 		playerStatsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		playerStatsLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
 		
 		
 		setOrientation(LinearLayout.VERTICAL);
 		setPadding(0, (int)(200 * density), 0, 0);
 
 		
 		update();
 		
 		addView(numOfTerritories);
 		addView(expectedReinforcement);
 		addView(numOfContinents);
 		addView(numOfTroopsToDeploy);
 		addView(numOfTroopsOwned);
 		addView(playersTroopsKilled);
 		addView(troopsKilledByPlayer);
 		
 		mainLayout.addView(this,playerStatsLayoutParams);
 	}
 
 	public void update()
 	{
		player = control.getActivePlayer();
 		numOfTerritories.setText("Territories : " + control.getNumberOfTerritoriesOwnedBy(player));
 		expectedReinforcement.setText("Expected Reinforcement : " + control.calcReinforcementBonus(player));
 		numOfContinents.setText("Continents : " + control.getNumberOfContinentsOwnedBy(player));
 		numOfTroopsToDeploy.setText("Troops to deploy : " + player.getNumOfTroopsToDeploy());
 		numOfTroopsOwned.setText("Troops : " + control.getNumberOfTroopsOwnedBy(player));
 		playersTroopsKilled.setText("Troops killed : " + player.getNumOfTroopsKilled());
 		troopsKilledByPlayer.setText("Fallen troops : " + player.getNumOfKilledEnemyTroops());
 	}
 
 	
 }
