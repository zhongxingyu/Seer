 // $Id$
 package me.kukkii.janken;
 
 import android.app.Activity;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 public class BotListFragment extends Fragment implements View.OnClickListener {
 
   private Button[] buttons;
   private Activity activity = getActivity();
   
 
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     return inflater.inflate(R.layout.botlist_fragment, container, false);
   }
 
   public void onResume(){
 	  super.onResume();
   }
   
   public void onPause(){
     super.onPause();
   }  
   
   private void init() {
     StageManager stageManager = StageManager.getManager();
     buttons = new Button[20];
     LinearLayout virticalLinearLayout = (LinearLayout) activity.findViewById(R.id.view_stages);
     int id = 0;
     for (int i = 0; i < 5; i ++) {
       LinearLayout horizontalLinearLayout = new LinearLayout(activity);
       horizontalLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 0, 1.0f));
       virticalLinearLayout.addView( horizontalLinearLayout );
       for (int j = 0; j < 4; j ++) {
         id += 1;
         Stage stage = stageManager.getStage(id);
         // Log.d("janken", "id=" + id + "; stage=" + stage.toString());
         Button button = new Button(activity);
         button.setLayoutParams(new LayoutParams(0, LayoutParams.FILL_PARENT, 1.0f));
         StageStatus status = stage.getStatus();
         button.setText( stage.getName() + " " + status.toString() );
         button.setGravity(Gravity.CENTER);
         switch (status) {
         case PERFECT :
           button.setTextColor( Color.parseColor("#00ff00") );
           break;
         case CLEARED :
           button.setTextColor( Color.parseColor("#0000ff") );
           break;
         case CURRENT :
           button.setTextColor( Color.parseColor("#ffff00") );
           break;
         case LOCKED :
         default :
           button.setTextColor( Color.parseColor("#ff0000") );
           break;
         }
         button.setOnClickListener( this );
         button.setClickable( (status != StageStatus.LOCKED) );
         horizontalLinearLayout.addView(button);
         buttons[4*i+j] = button;
       }
     }
   }
 
   // implements View.onClickListener
   public void onClick(View view) {
     gotoStage(view);
   }
 
   public void gotoStage(View view) {
 
     int stageId = -1;
     for (int i = 0; i < buttons.length; i++) {
       if (buttons[i] == view) {
         stageId = i+1;
         break;
       }
     }
     Stage stage = StageManager.getManager().getStage(stageId);
     // Log.d("janken", stage.toString());
     JankenFragment fragment = new JankenFragment();
     Bundle args = new Bundle();
     args.putSerializable("stage", stage);
     fragment.setArguments(args);
 
     FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
     transaction.replace(R.id.main_fragment, fragment);
     transaction.addToBackStack(null);
 
     // Commit the transaction
     transaction.commit();
   }
 
   /*
   private int getStageId(int viewId) {
     if (viewId == R.id.button_stage_0) {
       return 0;
     } else if (viewId == R.id.button_stage_1) {
       return 1;
     } else if (viewId == R.id.button_stage_2) {
       return 2;
     } else if (viewId == R.id.button_stage_3) {
       return 3;
     } else if (viewId == R.id.button_stage_4) {
       return 4;
     } else if (viewId == R.id.button_stage_5) {
       return 5;
     } else if (viewId == R.id.button_stage_6) {
       return 6;
     } else if (viewId == R.id.button_stage_7) {
       return 7;
     } else if (viewId == R.id.button_stage_8) {
       return 8;
     } else if (viewId == R.id.button_stage_9) {
       return 9;
     } else if (viewId == R.id.button_stage_10) {
       return 10;
     } else if (viewId == R.id.button_stage_11) {
       return 11;
     } else if (viewId == R.id.button_stage_12) {
       return 12;
     } else if (viewId == R.id.button_stage_13) {
       return 13;
     } else if (viewId == R.id.button_stage_14) {
       return 14;
     } else if (viewId == R.id.button_stage_15) {
       return 15;
     } else if (viewId == R.id.button_stage_16) {
       return 16;
     } else if (viewId == R.id.button_stage_17) {
       return 17;
     } else if (viewId == R.id.button_stage_18) {
       return 18;
     } else if (viewId == R.id.button_stage_19) {
       return 19;
     } else {
       // return -1;
       return 0;
     }
   }
   */
 }
