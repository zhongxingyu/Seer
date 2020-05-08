 package edu.sru.andgate.bitbot.customdialogs;
 
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.graphics.NickGameActivity;
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class VictoryDialog extends Dialog 
 {
     TextView kills_text, accuracy_text, time_text;
     int kills;
     double accuracy;
     long time;
     Button end;
     NickGameActivity nga;
     Context context;
     
     public VictoryDialog(NickGameActivity nga, Context context, int theme, int kills, double accuracy, long time) 
     {
         super(context, theme);
         this.context = context;    
         this.kills = kills;
         this.nga = nga;
         this.accuracy = accuracy;
         this.time = time/1000;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.victory_popup);       
         
         kills_text = (TextView) findViewById(R.id.kill_text);
         kills_text.setText("" + kills);
         
         accuracy_text = (TextView) findViewById(R.id.accuracy_text);
         accuracy_text.setText("" + accuracy + "%");
         
         time_text = (TextView) findViewById(R.id.time_text);
         time_text.setText("" + time + " seconds");
         
         end = (Button) findViewById(R.id.end_btn);
         end.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				nga.finish();
 			}
 		});
         
     }
  
 }
