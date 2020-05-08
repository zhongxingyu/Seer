 package com.r0adkll.deadskunk.utils;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.util.Pair;
 import android.util.Xml.Encoding;
 import android.view.ContextThemeWrapper;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import com.r0adkll.deadskunk.R;
 
 /**
  * This pump-outs easily configured
  * Dialogs for Displaying content
  * 
  * @author r0adkll
  *
  */
 public class DialogFactory {
 
 
 
 	
 	/**
 	 * Get a HoloEverywhere progress dialog 
 	 * for server loading
 	 * @param ctx     Application Context
 	 * @param message   the Message to show
 	 * @return        the compiled dialog object (unshown)
 	 */
 	public static ProgressDialog createProgressDialog(Context ctx, String message){
 		ProgressDialog dialog = new ProgressDialog(ctx);
 		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		dialog.setMessage(message);
 		dialog.setCancelable(false);
 		return dialog;
 	}
 
 	/**
 	 * Create an Alert Dialog that displays information to a user
 	 * and the only action is 'Ok' which closes it 
 	 * 
 	 * @param message   the server reponse message
 	 */
 	public static void createAlertDialog(Context ctx, String message, String title){
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setTitle(title)
 		.setMessage(message)
 		.setCancelable(false)             
 		.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
     /**
      * Create an Alert Dialog that displays information to a user
      * and the only action is 'Ok' which closes it
      *
      * @param message   the server reponse message
      */
     public static void createAlertDialog(Context ctx, int icon, String message, String title){
 
         AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
         builder.setTitle(title)
                 .setIcon(icon)
                 .setMessage(message)
                 .setCancelable(false)
                 .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                     }
                 });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
 	/**
 	 * Creates an Alert Dialog that has 2 action options of 'Yes' and 'No' in the form of the 
 	 * call back functions 'onConfirmed()' and 'onCanceled()'
 	 * @param ctx     the application context to create the dialog with
 	 * @param msg     the message of the alert dialog class
 	 * @param title     the title of the alert dialog
 	 * @param listener    the action listener for button clicks
 	 */
 	public static void createAlertDialog(Context ctx, String msg, String title, final IAlertListener listener){
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setTitle(title)
 		.setMessage(msg)
 		.setCancelable(false)    
 
 		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if(listener!=null) listener.onConfirmed();
 				dialog.dismiss();
 			}
 		})
 
 		.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				if(listener!=null) listener.onCanceled();
 				dialog.cancel();
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	/**
 	 * Creates an Alert Dialog that has 2 action options of 'Yes' and 'No' in the form of the 
 	 * call back functions 'onConfirmed()' and 'onCanceled()'
 	 * @param ctx     the application context to create the dialog with
 	 * @param msg     the message of the alert dialog class
 	 * @param title     the title of the alert dialog
 	 * @param listener    the action listener for button clicks
 	 */
 	public static void createAlertDialog(Context ctx, String posBtn, String negBtn, String msg, String title, final IAlertListener listener){
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setTitle(title)
 		.setMessage(msg)
 		.setCancelable(false)    
 
 		.setPositiveButton(posBtn, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if(listener!=null) listener.onConfirmed();
 				dialog.dismiss();
 			}
 		})
 
 		.setNegativeButton(negBtn, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				if(listener!=null) listener.onCanceled();
 				dialog.cancel();
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	/**
 	 * Creates an Alert Dialog that has 2 action options of 'Yes' and 'No' in the form of the 
 	 * call back functions 'onConfirmed()' and 'onCanceled()'
 	 * @param ctx     the application context to create the dialog with
 	 * @param msg     the message of the alert dialog class
 	 * @param title     the title of the alert dialog
 	 * @param listener    the action listener for button clicks
 	 */
 	public static void createAlertDialog(Context ctx, int icon, String posBtn, String negBtn, String msg, String title, final IAlertListener listener){
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setTitle(title)
 		.setMessage(msg)
 		.setCancelable(false)    
 
 		.setPositiveButton(posBtn, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if(listener!=null) listener.onConfirmed();
 				dialog.dismiss();
 			}
 		})
 
 		.setNegativeButton(negBtn, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				if(listener!=null) listener.onCanceled();
 				dialog.cancel();
 			}
 		});
 		
 		// Add the icon if available
 		if(icon != -1)
 			builder.setIcon(icon);
 		
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	
 	/**
 	 * Create a EditText Alert Dialog Box
 	 * @param ctx
 	 * @param callback
 	 * @return
 	 */
 	public static void createEditTextDialog(final Context ctx, final String title, final String message,  final ITextEntered callback){
 		LayoutInflater inflater = LayoutInflater.from(ctx);
 		View v = inflater.inflate(R.layout.layout_edittext_dialog, null, false);
 		
 		final EditText enter_nickname = (EditText)v.findViewById(R.id.text_input);        
 		enter_nickname.setHint(message);
 		
 		AlertDialog diag = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog))
 			.setTitle(title)
 			.setView(v)
 	
 			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 	
 					// Hide the Soft Keyboard
 					InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(enter_nickname.getWindowToken(), 0);
 	
 					// Call Callback
 					callback.textConfirmed(enter_nickname.getText().toString());
 	
 				}
 			})
 	
 			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					// Hide Soft Keyboard
 					InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(enter_nickname.getWindowToken(), 0);
 	
 					// Callback
 					callback.textCanceled();
 					dialog.dismiss();
 				}
 			}).create();
 		
 		// Show Dialog
 		diag.show();
 		enter_nickname.requestFocus();
 		InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 	}
 	
 	/**
 	 * Create a EditText Alert Dialog Box
 	 * @param ctx
 	 * @param callback
 	 * @return
 	 */
 	public static void createListViewDialog(final Context ctx, final String title, BaseAdapter adapter,  final IListViewItemSelectListener callback){
 				
 		LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.layout_listview_dialog, null, false);
 		ListView list = (ListView)layout.findViewById(R.id.list);
 		TextView vTitle = (TextView)layout.findViewById(R.id.title);
 		vTitle.setText(title);
 		list.setAdapter(adapter);
 		
 		final AlertDialog diag = new AlertDialog.Builder(ctx)
 			.setView(layout)
 			.create();
 		
 		list.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 				callback.onItemSelected(position);
 				diag.dismiss();
 			}
 		});
 		
 		// Show Dialog
 		diag.show();
 	}
 	
 	/**
 	 * Create a EditText Alert Dialog Box
 	 * @param ctx
 	 * @param callback
 	 * @return
 	 */
 	public static void createListViewDialog(final Context ctx, final String title, final Pair<String, String> actions,  
 			BaseAdapter adapter,  final IListViewItemSelectListener callback, final IAlertListener handler){
 				
 		LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.layout_vanilla_listview_dialog, null, false);
 		((TextView)layout.findViewById(R.id.title)).setText(title);
 		
 		ListView list = (ListView)layout.findViewById(R.id.list);
 		list.setAdapter(adapter);
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 		builder.setView(layout);
 		
 		if(handler != null && actions != null){
 			if(actions.first != null){
 				builder.setPositiveButton(actions.first, new OnClickListener() {				
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						handler.onConfirmed();
 					}
 				});				
 			}
 			
 			if(actions.second != null){
 				builder.setNegativeButton(actions.second, new OnClickListener() {					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						handler.onCanceled();
 					}
 				});
 			}
 		}
 		
 		// Create Dialog and show
 		final AlertDialog diag = builder.create();		
 		list.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 				callback.onItemSelected(position);
 				diag.dismiss();
 			}
 		});
 		
 		// Show Dialog
 		diag.show();
 	}
 	
 	/**
 	 * Create a webview dialog and fill it with html from a string
 	 * @param ctx		the application context
 	 * @param title		the summary title
 	 * @param content	the HTML content
 	 * @param handler	the callback
 	 */
 	public static void createWebViewDialog(final Context ctx, final String title, final String content, final IAlertListener handler){
 		// Create WebView Object
 		WebView web = new WebView(ctx);
 		
 		// Load Content
 		web.loadData(content, "text/html", Encoding.UTF_8.toString());
 		
 		// Create the Custom Alert Dialog
 		AlertDialog diag = new AlertDialog.Builder(ctx)
 			.setTitle(title)
 			.setView(web)
 			
 			.setPositiveButton("Export", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					// Return to callback
 					handler.onConfirmed();
 					dialog.dismiss();
 				}
 			})
 	
 			.setNegativeButton("Done", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 	
 					// Callback
 					handler.onCanceled();
 					dialog.dismiss();
 				}
 			}).create();
 		
 		// Show the Dialog
 		diag.show();		
 	}
 
 	/**
 	 * Alert Dialog Listener
 	 * @author r0adkll
 	 *
 	 */
 	public static interface IAlertListener{
 		public void onConfirmed();
 		public void onCanceled();
 	}
 
 	/**
 	 * 
 	 * @author drew.heavner
 	 *
 	 */
 	public interface ITextEntered{
 		public void textConfirmed(String nickname);
 		public void textCanceled();
 	}
 	
 	public interface IListViewItemSelectListener{
 		public void onItemSelected(int position);
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 }
