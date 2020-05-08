 package info.corne.performancetool;
 
 import info.corne.performancetool.utils.StringUtils;
 
 import java.io.File;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 /**
  * Using this class commands to save the settings can be run.
  * 
  * Copyright (C) 2013  Corné Dorrestijn
  *
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
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  * @author Corné Dorrestijn
  *
  */
  
 import java.io.File;
 
 public class SetHardwareInfoTask extends AsyncTask<String[], Void, Void>
 {
 	private final Set<SetHardwareInterface> listeners = new CopyOnWriteArraySet<SetHardwareInterface>();
 	
 	String[] files;
 	String[] values;
 	Boolean refresh = false;
 	ProgressDialog dialog;
 	/**
 	 * 
 	 * @param files files to write settings to.
 	 * @param values values that needs to be written to the files
 	 */
 	public SetHardwareInfoTask(String[] files, String[] values, ProgressDialog dialog)
 	{
 		this.files = files;
 		this.values = values;
 		this.dialog = dialog;
 	}
 	public SetHardwareInfoTask(String[] files, String[] values, ProgressDialog dialog, Boolean refresh)
 	{
 		this.files = files;
 		this.refresh = refresh;
 		this.values = values;
 		this.dialog = dialog;
 	}
 	public void addListener(final SetHardwareInterface listener)
 	{
 		listeners.add(listener);
 	}
 	public void removeListener(final SetHardwareInterface listener)
 	{
 		listeners.remove(listener);
 	}
 	private final void notifyListeners()
 	{
 		for(SetHardwareInterface listener : listeners)
 		{
 			listener.notifyOfHardwareInfoSaved(this);
 		}
 	}
 	@Override
 	protected Void doInBackground(String[]... params) {
 		for(int i = 0; i < files.length; i++)
 		{
 			// Run the commands as root.
 			String[] command = {"su", "-c", "echo \"" + values[i] + "\" > " + files[i]};
 			
 			File f=new File(files[i]);
 			if(f.exists()){
 				ShellCommand.run(command);
 			}
 		}
 		notifyListeners();
 		return null;
 	}
 	@Override
 	protected void onPostExecute(Void res)
 	{
 		dialog.dismiss();
 		if(this.refresh) "c".split("");
 	}
 
 }
