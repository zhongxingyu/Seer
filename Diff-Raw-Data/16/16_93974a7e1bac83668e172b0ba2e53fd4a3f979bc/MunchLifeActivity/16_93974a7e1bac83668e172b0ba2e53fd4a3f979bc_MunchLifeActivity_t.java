 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.bpace.munchlife;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.PowerManager;
 
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ImageView;
 
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View.OnClickListener;
 
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.Intent;
 
 import android.preference.PreferenceManager;
 
 import android.graphics.Typeface;
 import java.util.Random;
 
 import android.util.Log;
 
 public class MunchLifeActivity extends Activity
 {
 	public static final int DIALOG_GAMEWIN = 0;
 	public static final int DIALOG_DICEROLLER = 1;
 	
 	public static final String TAG = "MunchLife";
 	public static final String KEY_LEVEL = "savedLevel";
 	public static final String KEY_GEAR_LEVEL = "savedGearLevel";
 	
 	public TextView current_level;
 	public TextView current_gear_level;
 	public TextView total_level;
 	
 	PowerManager pm;
 	PowerManager.WakeLock wl;
 	
 	public int level = 1;
 	public int max_level = 10;
 	public int gear_level = 0;
 	public boolean sleepPref;
 	public boolean victoryPref;
 	public String maxlevelPref;
 	
 	/**
 	 * Pulls preferences and makes sure current application state matches 
 	 * what is expected from preferences
 	 */
 	@Override
 	protected void onStart()
 	{
 		super.onStart();
 		pm = (PowerManager) getSystemService(POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
 		SharedPreferences prefs;
     prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		sleepPref = prefs.getBoolean("sleepPref", false);
 		victoryPref = prefs.getBoolean("victoryPref", true);
 		maxlevelPref = prefs.getString("maxlevelPref", "10");
 		
 		try
 		{
 			max_level = Integer.parseInt(maxlevelPref);
 		}
 		catch(NumberFormatException error)
 		{
 			Log.e(TAG, "NumberFormatException: " + error.getMessage());
 			max_level = 10;
 		}
 		
 		if(level > max_level)
 		{
 			level = max_level;
 			current_level.setText(Integer.toString(level));
 			total_level.setText(Integer.toString(level + gear_level));
 		}
 	}
 	
 	/**
 	 * Restores level from saved instance and gamemode setting from preferences
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		Button up_button = (Button)findViewById(R.id.up_button);
 		up_button.setOnClickListener(levelUpClickListener);
 		
 		Button down_button = (Button)findViewById(R.id.down_button);
 		down_button.setOnClickListener(levelDownClickListener);
 		
 		Button up_gear_button = (Button)findViewById(R.id.up_gear_button);
 		up_gear_button.setOnClickListener(gearUpClickListener);
 		
 		Button down_gear_button = (Button)findViewById(R.id.down_gear_button);
 		down_gear_button.setOnClickListener(gearDownClickListener);
 		
 		current_level = (TextView)findViewById(R.id.current_level);
 		current_gear_level = (TextView)findViewById(R.id.current_gear_level);
 		total_level = (TextView)findViewById(R.id.total_level);
 		
 		// override font
 		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/windlass.ttf");
 		current_level.setTypeface(tf);
 		current_gear_level.setTypeface(tf);
 		total_level.setTypeface(tf);
 		
 		// pull old level from savedInstanceState, or default it to 1
 		level = savedInstanceState != null ? savedInstanceState.getInt(KEY_LEVEL)
 		                                   : 1;
 		gear_level = savedInstanceState != null ? savedInstanceState.getInt(KEY_GEAR_LEVEL)
 		                                        : 0;
 			
 		current_level.setText(Integer.toString(level));
 		current_gear_level.setText(Integer.toString(gear_level));
 		total_level.setText(Integer.toString(level + gear_level));
 	}
 	
 	/**
 	 * Save the currently displayed level
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		outState.putInt(KEY_LEVEL, level);
 		outState.putInt(KEY_GEAR_LEVEL, gear_level);
 	}
 	
 	/**
 	 * Save the current settings option for game mode into simple preferences
 	 */
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		if(sleepPref == true)
 		{
 			 wl.release();
 		}
 	}

  @Override
  protected void onResume()
  {
    super.onResume();
    if(sleepPref == true)
    {
      wl.acquire();
    }
  }
 	
 	/**
 	 * Create options menu from XML
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	/**
 	 * Logic for options menu
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch(item.getItemId())
 		{
 			case R.id.reset:
 				level = 1;
 				gear_level = 0;
 				current_level.setText(Integer.toString(level));
 				current_gear_level.setText(Integer.toString(gear_level));
 				total_level.setText("1");
 				return true;
 			case R.id.diceroller:
 				showDialog(DIALOG_DICEROLLER);
 				return true;
 			case R.id.settings:
 				Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
 				startActivity(i);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	/**
 	 * Controls settings dialog and dice roller
 	 */
 	ImageView rollview;
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		switch(id)
 		{
 			case DIALOG_GAMEWIN:
 				AlertDialog.Builder gamewinbuilder = new AlertDialog.Builder(this);
 				gamewinbuilder.setMessage(R.string.win);
 				DialogInterface.OnClickListener gamewinClickListener;
         gamewinClickListener = new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int item)
 					{
 						dialog.dismiss();
 					}
 				};
 				gamewinbuilder.setNeutralButton(R.string.ok, gamewinClickListener);
 				return gamewinbuilder.create();
 			case DIALOG_DICEROLLER:
 				AlertDialog.Builder rollerbuilder = new AlertDialog.Builder(this);
 				rollview = new ImageView(getApplicationContext());
 				rollview.setImageResource(R.drawable.one);
 				rollview.setAdjustViewBounds(true);
 				rollview.setMaxHeight(256);
 				rollview.setMaxWidth(256);
 				rollerbuilder.setView(rollview);
 				DialogInterface.OnClickListener rollerClickListener;
         rollerClickListener = new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int item)
 					{
 						dialog.dismiss();
 					}
 				};
 				rollerbuilder.setNeutralButton(R.string.ok, rollerClickListener);
 				return rollerbuilder.create();
 			default:
 				return super.onCreateDialog(id);
 		}
 	}
 	
 	/**
 	 * Updates dice roller every time it is opened
 	 */
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog)
 	{
 		switch(id)
 		{
 			case DIALOG_GAMEWIN:
 				return;
 			case DIALOG_DICEROLLER:
 				Random rand = new Random();
 				Integer roll = rand.nextInt(6) + 1;
 				switch(roll)
 				{
 					case 1:
 						rollview.setImageResource(R.drawable.one);
 						return;
 					case 2:
 						rollview.setImageResource(R.drawable.two);
 						return;
 					case 3:
 						rollview.setImageResource(R.drawable.three);
 						return;
 					case 4:
 						rollview.setImageResource(R.drawable.four);
 						return;
 					case 5:
 						rollview.setImageResource(R.drawable.five);
 						return;
 					case 6:
 						rollview.setImageResource(R.drawable.six);
 						return;
 					default:
 						return;
 				}
 			default:
 				return;
 		}
 	} 
 
 	/**
 	 * Increases the level by one and refreshes view as long as it is below 
    * max_level
 	 */
 	private OnClickListener levelUpClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 			if(level < max_level)
 			{
 				level = level + 1;
 				current_level.setText(Integer.toString(level));
 				total_level.setText(Integer.toString(level + gear_level));
 				// if you've won, display message
 				if(level == max_level && victoryPref == true)
 				{
 					showDialog(DIALOG_GAMEWIN);
 				}
 			}
 		}
 	};
 	
 	/**
 	 * Decreases the level by one and refreshes view as long as it is above 1
 	 */
 	private OnClickListener levelDownClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 			if(level > 1)
 			{
 				level = level - 1;
 				current_level.setText(Integer.toString(level));
 				total_level.setText(Integer.toString(level + gear_level));
 			}
 		}
 	};
 	
 	private OnClickListener gearUpClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 			if(gear_level < 99)
 			{
 				gear_level = gear_level + 1;
 				current_gear_level.setText(Integer.toString(gear_level));
 				total_level.setText(Integer.toString(level + gear_level));
 			}
 		}
 	};
 	
 	private OnClickListener gearDownClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 			if(gear_level > 0)
 			{
 				gear_level = gear_level - 1;
 				current_gear_level.setText(Integer.toString(gear_level));
 				total_level.setText(Integer.toString(level + gear_level));
 			}
 		}
 	};
 }
