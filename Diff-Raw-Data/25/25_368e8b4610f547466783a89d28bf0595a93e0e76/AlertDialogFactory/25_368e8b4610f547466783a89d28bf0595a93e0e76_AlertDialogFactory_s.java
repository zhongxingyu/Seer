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
 import se.team05.content.Route;
 import se.team05.data.DatabaseHandler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.widget.Toast;
 
 /**
  * This factory class is used to create alert dialog instances for the save
  * result dialog, confirm back press dialog and others. It always returns a
  * created instance so remember to call show() on the alert dialog that is
  * returned.
  * 
  * @author Daniel Kvist, Patrik Thitusson
  * 
  */
 public class AlertDialogFactory
 {
 	/**
 	 * Creates a new "save result?" dialog. It needs a context, route and result
 	 * to be displayed. If the user saves the result a toast is shown as
 	 * confirmation. If the result is not saved the dialog is simply cancelled.
 	 * 
 	 * @param context
 	 *            the context to operate in
 	 * @param route
 	 *            the route which was executed
 	 * @param result
 	 *            the result of the route
 	 * @return a new alert dialog
 	 */
 	public static AlertDialog newSaveResultDialog(final Context context, final Route route, final Result result)
 	{
 		String giveUserDistanceString = context.getString(R.string.distance_of_run)
 				+ String.valueOf((int) route.getTotalDistance()) + context.getString(R.string.km) + "\n";
 		String giveUserTimeString = context.getString(R.string.time_) + route.getTimePassedAsString() + "\n\n";
 		String giveUserResultData = giveUserDistanceString + giveUserTimeString;
 
 		return new AlertDialog.Builder(context).setTitle(R.string.save_result_)
 				.setMessage(giveUserResultData + context.getString(R.string.do_you_want_to_save_this_result_))
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						DatabaseHandler databaseHandler = new DatabaseHandler(context);
 						databaseHandler.saveResult(result);
 						Toast.makeText(context, context.getString(R.string.result_saved) + " " + route.getName(),
 								Toast.LENGTH_LONG).show();
 					}
 				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						dialog.cancel();
 					}
 				}).create();
 	}
 
 	/**
 	 * Creates a new "confirm back" dialog that can be shown to the user when
 	 * he/she presses the back button. A good use is for example when a route is
 	 * being executed and the back button is mistakenly pressed. If the user
 	 * presses the positive button the activity (context) is finished.
 	 * 
 	 * @param context
 	 *            the context to operate in and finish if positive answer
 	 * @return a new alert dialog
 	 */
 	public static AlertDialog newConfirmBackDialog(final Context context)
 	{
 		return new AlertDialog.Builder(context).setTitle(R.string.discard_route_)
 				.setMessage(R.string.do_you_really_want_to_discard_your_route_)
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						((Activity) context).finish();
 					}
 				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						dialog.cancel();
 					}
 				}).create();
 	}
 
 	/**
 	 * Alerts the user that they need to do something that is specified in the
 	 * message to do something.
 	 * 
 	 * @param context
 	 *            the context to operate in and finish if positive answer
 	 * @param title
 	 *            the title of the dialog
 	 * @param message
 	 *            the message of the dialog
 	 * @return a new alert dialog
 	 */
 	public static AlertDialog newAlertMessageDialog(final Context context, String title, String message)
 	{
 		return new AlertDialog.Builder(context).setTitle(title).setMessage(message)
 				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						dialog.cancel();
 					}
 				}).create();
 	}
 }
