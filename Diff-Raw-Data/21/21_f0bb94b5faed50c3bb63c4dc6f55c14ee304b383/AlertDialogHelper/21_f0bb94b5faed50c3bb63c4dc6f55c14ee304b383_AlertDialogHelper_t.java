 package com.gzaas.android.widget;
 
import com.gzaas.android.R;

 import android.app.AlertDialog;
 import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
 
 /**
  * Simple alert dialog helper.
  */
 public class AlertDialogHelper {
 	
 	/**
 	 * @param context The aplication context.
 	 * @param resTitle Thew resoure string of the title.
	 * @param resMessage The resource string of the message. Could be HTML.
 	 * @return Instance of a alert dialog with title and meesage.
 	 */
 	public static AlertDialog create(Context context, int resTitle, int resMessage) {
		TextView message = new TextView(context);
		message.setText(Html.fromHtml(context.getString(resMessage)));
		message.setTextColor(Color.WHITE);
		message.setTextSize(context.getResources().getDimension(R.dimen.text_small));
		message.setPadding(16, 16, 16, 16);
		message.setClickable(true);
		message.setMovementMethod(LinkMovementMethod.getInstance());
 		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
 		alertDialog.setTitle(resTitle);
		alertDialog.setView(message);
 		return alertDialog;
 	}
 
 }
