 package com.quizmania.utils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.quizmania.fruits.R;
 
 public class ViewUtils {
 
 	public static void showAlertMessage(Activity activity,String message,OnClickListener listener){
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		AlertDialog alertDialog = builder.create();
 		alertDialog.setMessage(message);
         alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",listener);	        
         alertDialog.show();
 	}
 	
 	public static void showYesNo(YesNoAlertParams parameterObject){
 		AlertDialog.Builder builder = new AlertDialog.Builder(parameterObject.context);
 		builder.setMessage(parameterObject.message);
 		builder.setNegativeButton(parameterObject.positiveText, parameterObject.positiveListener);
 		builder.setPositiveButton(parameterObject.negativeText, parameterObject.negativeListener);
 		AlertDialog alertDialog = builder.create();				
 		alertDialog.show();
 	}
 	
 	public static void showVote(final Context context, OnClickListener exitListener){
 		
 		String message = context.getResources().getString(R.string.voteMessage);		
 		String voteButtonText = context.getResources().getString(R.string.voteButtonText);
 		String exitButtonText = context.getResources().getString(R.string.exitButtonText);
 		OnClickListener goToAndroidMarket = new OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Intent intent = new Intent(Intent.ACTION_VIEW); 
 				intent.setData(Uri.parse("market://details?id=" + context.getPackageName())); 
 				context.startActivity(intent);
 
 				
 			}
 		};
 		
 		YesNoAlertParams yesNoAlertParamsToReturn = new YesNoAlertParams();
 		yesNoAlertParamsToReturn.setContext(context);
 		yesNoAlertParamsToReturn.setMessage(message);
 		yesNoAlertParamsToReturn.setNegativeListener(exitListener);
 		yesNoAlertParamsToReturn.setNegativeText(exitButtonText);
 		yesNoAlertParamsToReturn.setPositiveListener(goToAndroidMarket);
 		yesNoAlertParamsToReturn.setPositiveText(voteButtonText);
 		
 		YesNoAlertParams yesNoAlertParams = yesNoAlertParamsToReturn;
 				
 		showYesNo(yesNoAlertParams);
 		
 	}
 	
 	public static TextView createHintLetter(Context androidContext){
 		TextView letterHolder = new TextView(androidContext);
		letterHolder.setBackgroundResource(R.drawable.icon_letter);
 		letterHolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);		
 		letterHolder.setGravity(Gravity.CENTER);
 		letterHolder.setTextColor(Color.WHITE);
 		letterHolder.setTypeface(null, Typeface.BOLD);			        			        				
 		letterHolder.setPadding(0, 0, 0, 0);
 		return letterHolder;
 	}
 	
 	
 	
 	public static void inflateContentInTemplate(ActionBarActivity activity, int viewsResourceIdToInflate){
 		
 		View templateContent = activity.findViewById(R.id.templateContent);
 		LayoutInflater inflater = activity.getLayoutInflater();
 		inflater.inflate(viewsResourceIdToInflate, (ViewGroup) templateContent);
 		//ActionBar actionBar = activity.getActionBar();
 		//actionBar.set
 	}
 	
 }
