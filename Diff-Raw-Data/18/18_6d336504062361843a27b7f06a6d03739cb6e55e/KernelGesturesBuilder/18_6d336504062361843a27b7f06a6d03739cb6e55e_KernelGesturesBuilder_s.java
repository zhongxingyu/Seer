 /*
  * Kernel Gestures Builder
  * Build Gestures definitions on android kernels that support gestures
  * Kernel feature developed by Tungstwenty
  * http://forum.xda-developers.com/showthread.php?t=1831254
  * 
  * Copyright (C) 2012  Guillermo Joandet
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */
 
 package ar.com.nivel7.kernelgesturesbuilder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 
 
 public class KernelGesturesBuilder extends Activity {
 
 	private MTView KernelGesturesMTView;
 	private SharedPreferences sharedPrefs;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		KernelGesturesMTView = new MTView(this);
 		KernelGesturesMTView.setGridcolumns(Integer.parseInt(sharedPrefs.getString("grid_columns", "3")));
 		KernelGesturesMTView.setGridrows(Integer.parseInt(sharedPrefs.getString("grid_rows", "5")));
 		setContentView(KernelGesturesMTView);
 		
 		
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		KernelGesturesMTView.setGridcolumns(Integer.parseInt(sharedPrefs.getString("grid_columns", "3")));
 		KernelGesturesMTView.setGridrows(Integer.parseInt(sharedPrefs.getString("grid_rows", "5")));
 		setContentView(KernelGesturesMTView);
 		KernelGesturesMTView.redrawGrid();
 	}
 	
 	  @Override public boolean onCreateOptionsMenu(Menu menu) {
 	  getMenuInflater().inflate(R.menu.activity_kernel_gestures_builder, menu);
 	  return true; }
 	 
 	
       @Override
 	  public boolean onOptionsItemSelected(MenuItem item)
 	  {
 	        switch (item.getItemId())
 	        {
 	 	    case R.id.menu_settings:
 	        	startActivity(new Intent(this, Settings.class ));
 	        	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	        }
 	   }
 	        
 }
