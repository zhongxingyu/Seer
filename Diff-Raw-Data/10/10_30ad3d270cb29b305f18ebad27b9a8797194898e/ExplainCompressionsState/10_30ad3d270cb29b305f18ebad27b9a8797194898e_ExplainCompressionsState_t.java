 package com.revib.revib.state;
 
 import com.revib.revib.R;
 import com.revib.revib.StateActivity;
 import com.revib.revib.session.SessionVariables;
 import com.revib.revib.session.SleepThread;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 
 public class ExplainCompressionsState extends State {
 	public ExplainCompressionsState(Activity activity, State previousState) {
 		super(activity, previousState);
 	}
 
 	@Override
 	public int getInfoResource() {
 		switch(AGE){
 			case SessionVariables.ADULT:
 				return R.string.explain_compressions_info;
 			case SessionVariables.CHILD:
 				return R.string.explain_compressions_info_child;
 			case SessionVariables.BABY:
 				return R.string.explain_compressions_info_baby;				
 		}
 		return -1;
 	}
 
 	@Override
 	public int getAudioResource() {
 		step=0;
 		switch(AGE){
 			case SessionVariables.ADULT:
 				return R.raw.adult_explain_compressions_1;
 			case SessionVariables.CHILD:
 				return R.raw.child_explain_compressions;
 			case SessionVariables.BABY:
 				return R.raw.child_explain_compressions;			
 		}
 		return -1;
 	}
 
 	@Override
 	public int getImageResource() {
 		int	res	=	R.drawable.no_image;
 		switch(AGE){
 			case SessionVariables.ADULT:
 				return R.drawable.adult_explain_compressions_1;
 			case SessionVariables.CHILD:
 				return R.drawable.child_explain_compressions_1;
 			case SessionVariables.BABY:		
 				return R.drawable.baby_explain_compressions_1;	
 		}
 		return res;
 	}
 
 	@Override
 	public int getLeftBtnResource() {
 		return -1;
 	}
 
 	@Override
 	public int getRightBtnResource() {
 		return R.string.next;
 	}
 
 	@Override
 	public int getQuestionResource() {
 		return -1;
 	}
 
 	@Override
 	public int getTitleResource() {
 		return R.string.explain_compressions_title;
 	}
 
 	@Override
 	public State getNextState(int buttonRes) {
 		return new CompressionsState(activity,this);
 	}
 
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		super.onCompletion(mp);
 		switch(AGE){
 			case SessionVariables.ADULT:
 				step++;
 				if(step==1){
 					setImage(R.drawable.adult_explain_compressions_2_3_animation);
 					startAudio(R.raw.adult_explain_compressions_2_3);
					startAnimation();
 					break;
 				}else if(step==2){
 					setImage(R.drawable.adult_explain_compressions_4);
 					startAudio(R.raw.adult_explain_compressions_4);
 					break;
 				}
 			case SessionVariables.CHILD:
 			case SessionVariables.BABY:
 				SleepThread.getInstance().start(
 						((StateActivity) activity),
 						getNextState(-1),
 						500);
 		}
 	}
 }
