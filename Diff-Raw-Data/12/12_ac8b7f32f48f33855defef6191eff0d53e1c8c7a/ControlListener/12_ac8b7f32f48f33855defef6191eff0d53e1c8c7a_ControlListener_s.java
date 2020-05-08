 package com.example.tomatroid;
 
 import java.util.ArrayList;
 
 import com.example.tomatroid.util.StoredAnimation;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 public class ControlListener implements OnClickListener {
 
 	int activeButton = -1;
 	MainActivity mA;
 	LinearLayout linearLayout;
 	Button[] bA;
 	ArrayList<String> commands = new ArrayList<String>();
 
 	public ControlListener(MainActivity mA, LinearLayout linearLayout) {
 		this.mA = mA;
 		this.linearLayout = linearLayout;
 
 		commands.add("Pomodoro!");
 		commands.add("Pause Kurz!");
 		commands.add("Pause Lang!");
 		// commands.add("Pause Lang! (Gaming)");
 		// commands.add("Thema?");
 		// commands.add("Statistic?");
 		commands.add("Tracking!");
 		commands.add("Sleeping!");
 
 		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		relativeParams.addRule(RelativeLayout.CENTER_VERTICAL);
 
 		bA = new Button[commands.size()];
 		for (int i = 0; i < bA.length; i++) {
 			RelativeLayout rL = new RelativeLayout(mA);
 			ImageView iV = new ImageView(rL.getContext());
 			iV.setBackgroundResource(android.R.drawable.ic_media_play);
 			iV.setMaxWidth(50);
 
 			bA[i] = new Button(mA);
 			bA[i].setText(commands.get(i));
 			bA[i].setTag(i);
 			bA[i].setOnClickListener(this);
 
 			rL.addView(iV, relativeParams);
 			rL.addView(bA[i]);
 
 			linearLayout.addView(rL);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		int tag = (Integer) v.getTag();
 		start(tag);
 	}
 
 	public void start(int tag) {
 		if (activeButton != -1) {
 			// Stop Other or Own
 			mA.end(activeButton);
 			bA[activeButton]
 					.startAnimation(StoredAnimation.slideHorizontal(50));
 			bA[activeButton].setTranslationX(0);
 		}
 
 		if (activeButton != tag) {
 			bA[tag].startAnimation(StoredAnimation.slideHorizontal(-50));
 			bA[tag].setTranslationX(50);
 			// Start Own
 			mA.start(tag);
 			activeButton = tag;
 		} else {
 			activeButton = -1;
 		}
 	}
 
 	public void stop() {
 		bA[activeButton].startAnimation(StoredAnimation.slideHorizontal(50));
 		bA[activeButton].setTranslationX(0);
 		mA.stop(activeButton);
 		activeButton = -1;
 	}
 }
