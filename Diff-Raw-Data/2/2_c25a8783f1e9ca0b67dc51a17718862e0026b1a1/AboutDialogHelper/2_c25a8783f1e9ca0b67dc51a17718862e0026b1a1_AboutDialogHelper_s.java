 package com.project202;
 
 import static android.content.Intent.ACTION_VIEW;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.text.Spanned;
 import android.text.method.LinkMovementMethod;
 import android.view.View;
 import android.widget.TextView;
 
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.EBean;
 import com.googlecode.androidannotations.annotations.RootContext;
 import com.googlecode.androidannotations.annotations.res.HtmlRes;
 import com.googlecode.androidannotations.annotations.res.StringRes;
 import com.vuzzz.android.R;
 
 @EBean
 public class AboutDialogHelper {
 	
	private static final Uri MARKET_URI = Uri.parse("market://details?id=com.project202");
 
 	@RootContext
 	Context context;
 	
 	@HtmlRes
 	Spanned aboutContent;
 	
 	@Bean
 	ShareManager shareManager;
 
 	@StringRes
 	String shareProjectContent;
 	
 	public Dialog createAboutDialog() {
 		TextView textView = (TextView) View.inflate(context, R.layout.about_dialog_text, null);
 		textView.setText(aboutContent);
 		textView.setMovementMethod(LinkMovementMethod.getInstance());
 		textView.setLinkTextColor(Color.RED);
 		return new AlertDialog.Builder(context) //
 				.setTitle(R.string.about) //
 				.setView(textView) //
 				.setPositiveButton(R.string.rate_android_market, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						context.startActivity(new Intent(ACTION_VIEW, MARKET_URI));
 					}
 				}) //
 				.setNeutralButton(R.string.share_project, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						shareManager.share(shareProjectContent);
 					}
 				}) //
 				.create();
 	}
 
 }
