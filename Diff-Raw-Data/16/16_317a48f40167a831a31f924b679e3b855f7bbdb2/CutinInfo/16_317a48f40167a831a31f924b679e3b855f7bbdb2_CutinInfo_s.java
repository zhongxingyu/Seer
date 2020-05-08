 package com.garlicg.cutinlib;
 
 import android.content.Intent;
 
 
 public class CutinInfo {
 	public final static String ACTION_PICK_CUTIN ="com.garlicg.cutin.action.PICK";
 	public final static String DATA_ACTION_NAME ="action_name";
 	public final static String DATA_CUTIN_NAME ="cutin_name";
 	
 	/**
 	 *  It is just utility for returning Intent to  CUT-IN Manager. eq.<blockquote>
 	 *   CutinItem item = new CutinItem(CutinService.class , "SAMPLE 1");<br>
	 *   Intent intent = makePickedIntent(item);<br>
 	 *   setResult(RESULT_OK , intent);<br>
 	 *   finish();<br>
 	 *  </blockquote>
 	 * @param cutinItem
 	 * @return
 	 */
 	public static Intent buildPickedIntent(CutinItem cutinItem){
 		Intent intent = new Intent();
 		intent.putExtra(DATA_ACTION_NAME, cutinItem.serviceClass.getName());
 		intent.putExtra(DATA_CUTIN_NAME, cutinItem.cutinName);
 		return intent;
 	}
 }
