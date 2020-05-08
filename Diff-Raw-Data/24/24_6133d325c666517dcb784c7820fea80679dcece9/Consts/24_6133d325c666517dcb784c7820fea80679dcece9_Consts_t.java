 package postpc.musica;
 
import android.content.Context;
import android.content.Intent;

 public class Consts {
 	public static int searchNum = 5;
 	
	/**
	 * this func is used for cheaking with no direct WIFI
	 * @param mActivity
	 */
	public static void jump2Play(Context mActivity){
		Intent intent = new Intent(mActivity, Master_Play_Activity.class);
		intent.putExtra("song","some song"); //TODO
		mActivity.startActivity(intent);
	}
 }
