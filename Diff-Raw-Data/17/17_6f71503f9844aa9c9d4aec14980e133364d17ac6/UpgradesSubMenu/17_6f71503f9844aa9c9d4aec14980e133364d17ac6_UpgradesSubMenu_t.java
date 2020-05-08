 package com.slauson.dasher.menu;
 
 import com.slauson.dasher.R;
 import com.slauson.dasher.status.GlobalStatistics;
 import com.slauson.dasher.status.Points;
 import com.slauson.dasher.status.Upgrade;
 import com.slauson.dasher.status.Upgrades;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class UpgradesSubMenu extends Activity {
 
 	// dialog box constants
 	/** Dialog box id for purchasing previous upgrade **/
 	private final static int DIALOG_PURCHASE_PREVIOUS_UPGRADE = 0;
 	/** Dialog box id for not having enough points **/
 	private final static int DIALOG_NOT_ENOUGH_POINTS = 1;
 	/** Dialog box id for confirming an upgrade purchase **/
 	private final static int DIALOG_CONFIRM_UPGRADE = 2;
 	/** Dialog box id for an upgrade already being purchased **/
 	private final static int DIALOG_UPGRADE_ALREADY_PURCHASED = 3;
 
 	private final static String DIALOG_TITLE = "title";
 	private final static String DIALOG_PURCHASE_PREVIOUS_UPGRADE_REQUIRED_TITLE = "requiredTitle";
 	private final static String DIALOG_CONFIRM_UPGRADE_LEVEL = "level";
 	private final static String DIALOG_CONFIRM_UPGRADE_POINTS = "points";
	
	private static final float INVULNERABILITY_TEXT_SIZE_FACTOR = 0.5f;
 
 	private SharedPreferences.Editor sharedPreferencesEditor;
 	
 	private Upgrade upgrade;
 	
 	// points textview
 	private TextView pointsView;
 	
 	private Button upgradeButton1, upgradeButton2, upgradeButton3, upgradeButton4;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.upgrades_sub_menu);
 
     	// get preferences editor
     	sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
     	
     	// get upgrade type
     	Bundle extras = getIntent().getExtras();
     	int upgradeID = extras.getInt(Upgrades.UPGRADE_KEY);
     	upgrade = Upgrades.getUpgrade(upgradeID);
     	
     	// set title
     	TextView title = (TextView)findViewById(R.id.upgradesSubMenuTitle);
     	title.setText("Upgrades -\n" + upgrade.getDescription());
     	
    	// set text size for invulnerability
    	if (upgrade.equals(Upgrades.invulnerabilityUpgrade)) {
    		title.setTextSize(title.getTextSize()*INVULNERABILITY_TEXT_SIZE_FACTOR);
    	}
    	
     	// set points
     	pointsView = (TextView)findViewById(R.id.upgradesSubMenuPoints);
     	pointsView.setText(Points.getNumPoints() + " points");
     	
     	// get buttons
     	upgradeButton1 = (Button)findViewById(R.id.upgradesSubMenuUpgrade1Button);
     	upgradeButton2 = (Button)findViewById(R.id.upgradesSubMenuUpgrade2Button);
     	upgradeButton3 = (Button)findViewById(R.id.upgradesSubMenuUpgrade3Button);
     	upgradeButton4 = (Button)findViewById(R.id.upgradesSubMenuUpgrade4Button);
     	
     	System.out.println("UPGRADE 1: " + getResources().getString(upgrade.getTitleResourceId(Upgrades.UPGRADE_1)));
     	
     	// set button text
     	upgradeButton1.setText(upgrade.getTitleResourceId(Upgrades.UPGRADE_1));
     	upgradeButton2.setText(upgrade.getTitleResourceId(Upgrades.UPGRADE_2));
     	upgradeButton3.setText(upgrade.getTitleResourceId(Upgrades.UPGRADE_3));
     	upgradeButton4.setText(upgrade.getTitleResourceId(Upgrades.UPGRADE_4));
 
 		// check for purchased upgrades
 		switch(upgrade.getLevel()) {
 		case Upgrades.UPGRADE_4:
 			upgradeButton4.setTextColor(Color.BLACK);
 			((TableRow)upgradeButton4.getParent()).setBackgroundColor(Color.WHITE);
 			toggleButtonText(upgradeButton4);
 		case Upgrades.UPGRADE_3:
 			upgradeButton3.setTextColor(Color.BLACK);
 			((TableRow)upgradeButton3.getParent()).setBackgroundColor(Color.WHITE);
 			toggleButtonText(upgradeButton3);
 		case Upgrades.UPGRADE_2:
 			upgradeButton2.setTextColor(Color.BLACK);
 			((TableRow)upgradeButton2.getParent()).setBackgroundColor(Color.WHITE);
 			toggleButtonText(upgradeButton2);
 		case Upgrades.UPGRADE_1:
 			upgradeButton1.setTextColor(Color.BLACK);
 			((TableRow)upgradeButton1.getParent()).setBackgroundColor(Color.WHITE);
 			toggleButtonText(upgradeButton1);
 			break;
 		}
 
     	upgradeButton1.setOnClickListener(new UpgradeOnClickListener(Upgrades.UPGRADE_1));
     	upgradeButton2.setOnClickListener(new UpgradeOnClickListener(Upgrades.UPGRADE_2));
     	upgradeButton3.setOnClickListener(new UpgradeOnClickListener(Upgrades.UPGRADE_3));
     	upgradeButton4.setOnClickListener(new UpgradeOnClickListener(Upgrades.UPGRADE_4));
 	}
 	
 	@Override
 	public Dialog onCreateDialog(int id, Bundle args) {
 
 		// get args
 		final String title = args.getString(DIALOG_TITLE);
 		
 		Dialog dialog = null;
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
 		switch(id) {
 		case DIALOG_PURCHASE_PREVIOUS_UPGRADE:
 			String requiredTitle = args.getString(DIALOG_PURCHASE_PREVIOUS_UPGRADE_REQUIRED_TITLE);
 			
 			alertDialogBuilder
 			.setTitle("Unable to Purchase Upgrade")
			.setMessage("You must first purchase the '" + requiredTitle + "' upgrade before purchasing the '" + title + "' upgrade.")
 			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					removeDialog(DIALOG_PURCHASE_PREVIOUS_UPGRADE);
 				}
 			});
 			dialog = alertDialogBuilder.create();
 			break;
 		case DIALOG_NOT_ENOUGH_POINTS:
 			alertDialogBuilder
 				.setTitle("Not Enough Points")
				.setMessage("You do not have enough points for the " + upgrade.getDescription().toLowerCase() + " upgrade '" + title + "'.")
 				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						removeDialog(DIALOG_NOT_ENOUGH_POINTS);
 					}
 				});
 			
 			dialog = alertDialogBuilder.create();
 
 			break;
 		case DIALOG_CONFIRM_UPGRADE:
 			
 			// get more args
 			final int level = args.getInt(DIALOG_CONFIRM_UPGRADE_LEVEL);
 			final int points = args.getInt(DIALOG_CONFIRM_UPGRADE_POINTS);
 			
 			alertDialogBuilder
 				.setTitle("Confirm Upgrade")
				.setMessage("Are you sure you want to purchase the " + upgrade.getDescription().toLowerCase() + " upgrade '" + title + "'?")
 				
 				// do nothing when user selects no 
 				.setNegativeButton("No", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						removeDialog(DIALOG_CONFIRM_UPGRADE);
 					}
 				})
 				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 
 						// set upgrade level
 						upgrade.setLevel(level);
 
 						Button button;
 						
 						// update background color
 						switch(level)  {
 						case 2:
 							button = upgradeButton2;
 							break;
 						case 3:
 							button = upgradeButton3;
 							break;
 						case 4:
 							button = upgradeButton4;
 							break;
 						default:
 							button = upgradeButton1;
 							break;
 						}
 						
 						toggleButtonColor(button);
 						toggleButtonText(button);
 						
 						// update points
 						Points.update(-points);
 						
 						// update points spent
 						GlobalStatistics.getInstance().pointsSpent += points;
 						
 						// update points textview
 				    	pointsView.setText("You have " + Points.getNumPoints() + " points");
 				    
 				    	// save state
 				    	upgrade.save(sharedPreferencesEditor);
 				    	Points.save(sharedPreferencesEditor);
 				    	GlobalStatistics.save(sharedPreferencesEditor);
 				    	sharedPreferencesEditor.commit();
 
 				    	// remove dialog so that we can change message/title
 				    	removeDialog(DIALOG_CONFIRM_UPGRADE);
 				    }
 				});
 
 			
 			dialog = alertDialogBuilder.create();
 
 			break;
 		case DIALOG_UPGRADE_ALREADY_PURCHASED:
 			alertDialogBuilder
 			.setTitle("Upgrade Already Purchased")
 			.setMessage("You have already purchased the " + upgrade.getDescription().toLowerCase() + " upgrade '" + title + "'")
 			
 			// do nothing when user selects ok 
 			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					removeDialog(DIALOG_UPGRADE_ALREADY_PURCHASED);
 				}
 			});
 			
 			dialog = alertDialogBuilder.create();
 			
 			break;
 		default:
 			dialog = null;
 		}
 		
 		return dialog;
 	}
 
 	/**
 	 * Sets button text to be black and sets the row background color to black
 	 * @param button button to toggle
 	 */
 	public void toggleButtonColor(Button button) {
 		button.setTextColor(Color.BLACK);
 		((TableRow)button.getParent()).setBackgroundColor(Color.WHITE);
 	}
 	
 	/**
 	 * Modifies button text to remove cost of powerup
 	 * @param button button to toggle
 	 */
 	public void toggleButtonText(Button button) {
 		String text = button.getText().toString();
 		
 		text = text.replaceAll("\n.*", "");
 		button.setText(text);
 	}
 	
 	/**
 	 * Click handler for upgrade buttons
 	 * @author Josh Slauson
 	 *
 	 */
 	private class UpgradeOnClickListener implements OnClickListener {
 		
 		// upgrade level
 		private int level;
 		
 		public UpgradeOnClickListener(int level) {
 			this.level = level;
 		}
 
 		public void onClick(View v) {
 			
 			// get title of upgrade
 			int resourceID = upgrade.getTitleResourceId(level);
 			String title = "Title";
 			
 			if (resourceID != -1) {
 				title = getResources().getString(resourceID);
 				
 				// remove '- cost' from title
 				title = title.substring(0, title.indexOf('\n')).trim();
 			}
 
 			// create bundle for dialog box
 			Bundle bundle = new Bundle();
 			bundle.putString(DIALOG_TITLE, title);
 			
 			// check if upgrade is unlocked
 			if (upgrade.getLevel() >= level) {
 				showDialog(DIALOG_UPGRADE_ALREADY_PURCHASED, bundle);
 				return;
 			}
 			
 			int pointsRequired = Upgrades.POINTS_UPGRADE_1;
 			
 			switch(level) {
 			case 2:
 				pointsRequired = Upgrades.POINTS_UPGRADE_2;
 				break;
 			case 3:
 				pointsRequired = Upgrades.POINTS_UPGRADE_3;
 				break;
 			case 4:
 				pointsRequired = Upgrades.POINTS_UPGRADE_4;
 				break;
 			}
 			
 			// check if player hasn't bought previous upgrade
 			if (upgrade.getLevel() < level - 1) {
 				
 				// get required upgrade title
 				int requiredResourceID = upgrade.getTitleResourceId(upgrade.getLevel()+1);
 				String requiredTitle = "Title";
 				
 				if (requiredResourceID != -1) {
 					requiredTitle = getResources().getString(requiredResourceID);
 					
 					// remove '- cost' from title
 					requiredTitle = requiredTitle.substring(0, requiredTitle.indexOf('\n')).trim();
 				}
 				bundle.putString(DIALOG_PURCHASE_PREVIOUS_UPGRADE_REQUIRED_TITLE, requiredTitle);
 				
 				
 				showDialog(DIALOG_PURCHASE_PREVIOUS_UPGRADE, bundle);
 				return;
 			}
 			
 			// check if player has enough points
 			if (Points.getNumPoints() < pointsRequired) {
 				showDialog(DIALOG_NOT_ENOUGH_POINTS, bundle);
 				return;
 			}
 			
 			// add remaining args
 			bundle.putInt(DIALOG_CONFIRM_UPGRADE_LEVEL, level);
 			bundle.putInt(DIALOG_CONFIRM_UPGRADE_POINTS, pointsRequired);
 
 			// confirm upgrade
 			showDialog(DIALOG_CONFIRM_UPGRADE, bundle);
 			
 			// TODO: save upgrade, points if changed
 		}
 	}
 }
