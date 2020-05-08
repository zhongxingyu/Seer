 /**
  *   Copyright (C) 2010, Roger Kind Kristiansen <roger@kind-kristiansen.no>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package no.rkkc.bysykkel;
 
 import android.app.Activity;
 import android.content.Context;
 import android.widget.Toast;
 
 public class Toaster implements Runnable {
 	private Context context;
 	private String message;
 	private int duration;
 	
 	public Toaster(Activity activity, String message, int duration) {
 		this.context = activity;
 		this.message = message;
 		this.duration = duration;
 	}
 	
 	public static void toast(Activity activity, String message, int duration) {
 		activity.runOnUiThread(new Toaster(activity, message, duration));
 	}
 	
 	public static void toast(Activity activity, int id, int duration) {
 		toast(activity, activity.getText(id).toString(), duration);
 	}
 
 	public void run() {
 		Toast.makeText(context, message, duration).show();
		// TODO Auto-generated method stub
		
 	}
 }
