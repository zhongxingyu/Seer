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
 
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View.OnClickListener;
 
 public class MunchLifeActivity extends Activity
 {
	private static final String KEY_LEVEL = "savedLevel";
	private int level = 1;
 	
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
 		
 		// pull old level from savedInstanceState, or default it to 1
 		level = savedInstanceState != null ? savedInstanceState.getInt(KEY_LEVEL)
 		                                   : 1;
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
 
 	private OnClickListener mUpClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
			TextView current_level = (TextView)findViewById(R.id.current_level);
 			if(level < 10)
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
			TextView current_level = (TextView)findViewById(R.id.current_level);
 			if(level > 1)
 			{
 				level = level - 1;
 				current_level.setText("Level " + level);
 			}
 		}
 	};
 }
