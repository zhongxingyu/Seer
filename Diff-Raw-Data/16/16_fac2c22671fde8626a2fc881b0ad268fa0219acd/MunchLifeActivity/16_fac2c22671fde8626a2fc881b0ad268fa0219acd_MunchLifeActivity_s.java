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
 
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View.OnClickListener;
 
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 
 import android.util.Log;
 
 public class MunchLifeActivity extends Activity
 {
 	public static final int DIALOG_SETTINGS = 0;
 	public static final String KEY_LEVEL = "savedLevel";
 	public TextView current_level;
 	public int level = 1;
 	public int max_level = 10;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		Button up_button = (Button)findViewById(R.id.up_button);
 		up_button.setOnClickListener(mUpClickListener);
 		
 		Button down_button = (Button)findViewById(R.id.down_button);
 		down_button.setOnClickListener(mDownClickListener);
 		
 		current_level = (TextView)findViewById(R.id.current_level);
 		
 		// pull old level from savedInstanceState, or default it to 1
 		level = savedInstanceState != null ? savedInstanceState.getInt(KEY_LEVEL)
 		                                   : 1;
 		
 		current_level.setText("Level " + level);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch(item.getItemId())
 		{
 			case R.id.reset:
 				level = 1;
 				TextView current_level = (TextView)findViewById(R.id.current_level);
 				current_level.setText("Level " + level);
 				return true;
 			case R.id.settings:
 				showDialog(DIALOG_SETTINGS);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		outState.putInt(KEY_LEVEL, level);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		switch(id)
 		{
 			case DIALOG_SETTINGS:
 				Log.d("MunchLife", "Creating settings dialog");
 				final CharSequence[] options = {"Standard Munchkin", "Epic Munchkin"};
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle(R.string.settings);
 				builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener()
 				{
 				    public void onClick(DialogInterface dialog, int item)
 					{
 						Toast.makeText(getApplicationContext(), options[item], Toast.LENGTH_SHORT).show();
 						if(item == 0)
 						{
 							max_level = 10;
 							if(level > 10)
 							{
 								level = 10;
 								current_level.setText("Level " + level);
 							}
 						}
 						else
 						{
 							max_level = 20;
 						}
 						dialog.dismiss();
 				    }
 				});
 				return builder.create();
 			default:
 				return super.onCreateDialog(id);
 		}
 	}
 
 	private OnClickListener mUpClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 			if(level < max_level)
 			{
 				level = level + 1;
 				current_level.setText("Level " + level);
 			}
 		}
 	};
 	
 	private OnClickListener mDownClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 			if(level > 1)
 			{
 				level = level - 1;
 				current_level.setText("Level " + level);
 			}
 		}
 	};
 }
