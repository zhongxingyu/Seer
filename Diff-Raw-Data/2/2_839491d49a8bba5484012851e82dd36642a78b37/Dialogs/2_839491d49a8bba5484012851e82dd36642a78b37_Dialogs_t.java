 package com.github.pozo.volan.shared;
 
 import com.github.pozo.volan.R;
 import com.github.pozo.volan.utils.Constants.Cities;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class Dialogs {
 	public final static int MENU_SETTIGNS = 0x1;
 	public final static int MENU_ABOUT = 0x2;
 	public final static int MENU_LINES = 0x4;
 	
 	public static AlertDialog getSettingsDialog(final Context mContext) {
 		final String[] values = Cities.getCities();
		int show = getCurrentCityIndex(mContext, Cities.getCityFolders());
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 		builder.setTitle(mContext.getResources().getString(R.string.menu_settings));
 		builder.setIcon(R.drawable.icon);
 		builder.setSingleChoiceItems(values, show,
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int item) {
 						String cityFolderByName = Cities.getCityFolderByName(values[item]);
 						PersistentHandler.set(mContext,	cityFolderByName);
 						dialog.dismiss();
 					}
 				});
 		final AlertDialog alert = builder.create();
 		alert.setCanceledOnTouchOutside(true);
 		return alert;
 	}
 
 	private static int getCurrentCityIndex(final Context mContext,	final String[] values) {
 		int index = 0;
 		String currentCity = PersistentHandler.get(mContext);
 		
 		for (int i = 0; i < values.length; i++) {
 			if(values[i].equals(currentCity)) {
 				index = i;
 			}
 		}
 		return index;
 	}
 
 	public static Dialog getAboutDialog(ViewGroup srdGrp,final Context mContext) {
 		AlertDialog.Builder builder;
 		AlertDialog alertDialog;
 
 		LayoutInflater inflater = (LayoutInflater) mContext
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.about, srdGrp);
 
 		TextView text = (TextView) layout.findViewById(R.id.text);
 		text.setText(mContext.getResources().getString(
 				R.string.dialog_about_text));
 		ImageView image = (ImageView) layout.findViewById(R.id.image);
 		image.setImageResource(R.drawable.icon);
 
 		builder = new AlertDialog.Builder(mContext);
 		builder.setView(layout);
 		alertDialog = builder.create();
 		alertDialog.setCanceledOnTouchOutside(true);
 
 		return alertDialog;
 	}
 }
