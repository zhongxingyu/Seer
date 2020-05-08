 package com.beecub.golauncher.golauncherfonts;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.widget.TextView;
 
 public class InfoDialog extends Dialog {
     private Context context;
 
     public InfoDialog(Context context) {
         super(context);
         this.context = context;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dialog_credits);
         setTitle("Info - Credits");
         setCancelable(true);
         
         TextView tv1 = (TextView) findViewById(R.id.bybeecub);
        tv1.setText(Html.fromHtml(context.getString(R.string.bybeecub) + ", " + "<a href=\"" + "http://www.beecub.com\">http://beecub.com/</a>"));
         tv1.setMovementMethod(LinkMovementMethod.getInstance());
         
         TextView tv2 = (TextView) findViewById(R.id.thanksto);
         tv2.setText(context.getString(R.string.specialthanks));
         
         TextView tv3 = (TextView) findViewById(R.id.credits);
         tv3.setText("" +
                 "\nRobin Wolf - Translation French" +
                 "\nQuim Booh - Translation Spanish" +
                 "");
     }
 }
