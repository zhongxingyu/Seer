 package com.serym.hackathon.aardvark;
 
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 
 /**
  * BarcodeAppDownloadDialog is an AlertDialog that invites the user to download
 * the Barcode Scanner app. The code was adapted from the
  * com.google.zxing.integration.android.IntentIntegrator class.
  * 
  * @see http://code.google.com/p/zxing/
  */
 public class BarcodeAppDownloadDialog extends AlertDialog {
 
 	/**
 	 * Tag for LogCat.
 	 */
 	private static final String TAG = "AARDVARK-BOUNCER-BARCODEAPPDOWNLOADDIALOG";
 
 	/**
 	 * The title for this dialog.
 	 */
 	private static final String TITLE = "Install Barcode Scanner?";
 
 	/**
 	 * The message for this dialog.
 	 */
 	private static final String MESSAGE = "This application requires Barcode Scanner. Would you like to install it?";
 
 	/**
 	 * Yes button text.
 	 */
 	private static final String YES = "Yes";
 
 	/**
 	 * No button text.
 	 */
 	private static final String NO = "No";
 
 	/**
 	 * Package of Barcode Scanner.
 	 */
 	private static final String BS_PACKAGE = "com.google.zxing.client.android";
 
 	/**
 	 * The context this was created in.
 	 */
 	private final Context context;
 
 	/**
 	 * Creates a new BarcodeAppDownloadDialog in the given context.
 	 * 
 	 * @param context
 	 *            the context in which to create this dialog.
 	 */
 	public BarcodeAppDownloadDialog(Context context) {
 		super(context);
 
 		this.context = context;
 
 		this.setTitle(TITLE);
 		this.setMessage(MESSAGE);
 
 		this.setButton(YES, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialogInterface, int i) {
 				Uri uri = Uri.parse("market://details?id=" + BS_PACKAGE);
 				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 				try {
 					BarcodeAppDownloadDialog.this.context.startActivity(intent);
 				} catch (ActivityNotFoundException anfe) {
 					// Hmm, market is not installed
 					Log.w(TAG,
 							"Android Market is not installed; cannot install Barcode Scanner");
 				}
 			}
 		});
 
 		this.setButton2(NO, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialogInterface, int i) {
 				// Intentionally empty
 			}
 		});
 	}
 
 }
