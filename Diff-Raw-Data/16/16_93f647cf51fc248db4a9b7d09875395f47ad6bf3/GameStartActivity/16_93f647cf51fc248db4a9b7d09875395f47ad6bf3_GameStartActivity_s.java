 package com.ktx.game.dealornodeal;
 
import com.example.dealornodeal.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.ToggleButton;
 
 public class GameStartActivity extends Activity {
 	private ToggleButton case1, case2, case3, case4, case5, case6, case7, case8, case9, case10, case11, case12, case13, case14, case15, case16, case17, case18, case19, case20, case21, case22, case23, case24, case25, case26;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.gamestart);
         
         
         case1 = (ToggleButton) findViewById(R.id.case1);
         case2 = (ToggleButton) findViewById(R.id.case2);
         case3 = (ToggleButton) findViewById(R.id.case3);
        
         }
 }
