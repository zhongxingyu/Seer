 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
 */
 package se.team05.dialog;
 
 import se.team05.R;
 import se.team05.content.Result;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckedTextView;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This Dialog class is shown to the user when the user has recorded a new route
  * and wishes to finish recording it. It has inputs for the name and description
  * of the route and it also has a checkbox which is connected to a boolean value
  * which decides if the results that the user had (time, length) should also be
  * saved with the route.
  * 
  * @author Daniel Kvist
  * 
  */
 public class SaveRouteDialog extends Dialog implements View.OnClickListener
 {
 	/**
 	 * This interface must be implemented by the calling Activity to be able to
 	 * receive callbacks.
 	 * 
 	 */
 	public interface Callbacks
 	{
 		public void onSaveRoute(String name, String description, boolean saveResult);
 
 		public void onDismissRoute();
 	}
 
 	private Context context;
 	private Callbacks callbacks;
 	private Result result;
 
 	/**
 	 * The constructor of the dialog takes a Context and a Callbacks as a
 	 * parameter and saves it as instances of both the Context class and as a
 	 * Callbacks instance.
 	 * 
 	 * @param context
 	 *            the context to run the dialog in
 	 * @param callbacks
 	 *            an instance of the class that implements this class's
 	 *            Callbacks interface
 	 * @param result
 	 *            the results from the route including speed, distance, time
 	 */
 	public SaveRouteDialog(Context context, Callbacks callbacks, Result result)
 	{
 		super(context);
 		this.context = context;
 		this.callbacks = callbacks;
 		this.result = result;
 	}
 
 	/**
 	 * This method is called by the system and sets the content view that should
 	 * be connected with this class and also the title of the dialog.It also
 	 * sets the result attributes, speed, distance and time of the route, and
 	 * adds click listeners to the buttons.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dialog_save_route);
 		setTitle(context.getString(R.string.save_route));
 
 		((Button) findViewById(R.id.discard_button)).setOnClickListener(this);
 		((Button) findViewById(R.id.save_button)).setOnClickListener(this);
 
 		TextView timeTextView = (TextView) findViewById(R.id.time);
 		TextView distanceTextView = (TextView) findViewById(R.id.runneddistance);
 		TextView speedTextView = (TextView) findViewById(R.id.speed);
 
 		int routeDistance = result.getDistance();
 		String distanceText = String.valueOf(routeDistance);
 		distanceTextView.setText(distanceText + context.getString(R.string.km));
 
 		int time = result.getTime();
 		int min = time / 60;
 		int sec = time % 60;
 
		String resultat = String.format(" %02d:%02d", min, sec);
 		timeTextView.setText(resultat);
 
 		double speed = (routeDistance / time) * 3.6;
 		String speedText = String.valueOf(speed);
 		speedTextView.setText(speedText + context.getString(R.string.km) + "/" + context.getString(R.string.h));
 
 		//Make the check box toggle on click.
 		CheckedTextView chkBox = (CheckedTextView) findViewById(R.id.save_result);
 	    chkBox.setOnClickListener(new View.OnClickListener()
 	    {
 	        public void onClick(View v)
 	        {
 	            ((CheckedTextView) v).toggle();
 	        }
 	    });		
 	}
 
 	/**
 	 * This is called when a button is clicked and decides which action to take
 	 * next. If the discard button is clicked, an alert dialog is created to
 	 * prompt the user to confirm that the route really should be discarded. If,
 	 * however, the save button was clicked we get the name, description and the
 	 * checked value and pass that on through the callback.
 	 */
 	@Override
 	public void onClick(View v)
 	{
 		switch (v.getId())
 		{
 			case R.id.discard_button:
 				AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(R.string.discard_route_)
 						.setMessage(R.string.do_you_really_want_to_discard_your_route_)
 						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int id)
 							{
 								callbacks.onDismissRoute();
 								dismiss();
 							}
 						}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int id)
 							{
 								dialog.cancel();
 							}
 						}).create();
 				alertDialog.show();
 				break;
 			case R.id.save_button:
 				String name = ((EditText) findViewById(R.id.name)).getText().toString();
 				
 				if(testStringForName(name))
 				{
 					String description = ((EditText) findViewById(R.id.description)).getText().toString();
 					boolean saveResult = ((CheckedTextView) findViewById(R.id.save_result)).isChecked();
 					callbacks.onSaveRoute(name, description, saveResult);
 					dismiss();
 				}
 				
 				break;
 			default:
 				break;
 		}
 	}
 	
 	/**
 	 * Help method for testing that a string is neither empty nor null. In case the string is empty
 	 * a toast message will appear prompting the user to choose a name.
 	 * 
 	 * @param name the string to be tested
 	 * @return true if string has some character in it, otherwise false
 	 */
 	public boolean testStringForName(String name)
 	{
 		if(name.equals("") || name == null)
 		{
 			CharSequence text = getContext().getString(R.string.must_use_a_valid_name);
 			int duration = Toast.LENGTH_SHORT;
 
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 }
