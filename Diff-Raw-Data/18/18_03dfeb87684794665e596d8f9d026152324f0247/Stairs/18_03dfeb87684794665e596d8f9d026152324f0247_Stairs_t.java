 package rpisdd.rpgme.gamelogic.dungeon.model;
 
 import rpisdd.rpgme.activities.DescendStairs;
 import rpisdd.rpgme.activities.MainActivity;
 import rpisdd.rpgme.activities.TransitionFragment;
 import rpisdd.rpgme.popups.AnnoyingPopup;
 import android.app.Activity;
 import android.content.DialogInterface;
 
 public class Stairs implements RoomContent {
 
 	@Override
 	public boolean Encounter(Activity activity) {
 		final Activity act = activity;
 		AnnoyingPopup.doDont(act, "You see stairs leading down.\n"
 				+ "Do you want to take them?\n"
 				+ "(You can't go back up if you do...)", "Descend",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 						TransitionFragment trans = new TransitionFragment();
 						trans.setValues(new DescendStairs(), true);
 						((MainActivity) act).changeFragment(trans);
 					}
 				});
		return true;
 	}
 
 	@Override
 	public RoomType getRoomType() {
 		return RoomType.STAIRS;
 	}
 
 	@Override
 	public String getStringRepresentation() {
 		return "STAIRS";
 	}
 
 	public static Stairs getFromStringRepresentation(String[] contentArgs) {
 		return new Stairs();
 	}
 }
