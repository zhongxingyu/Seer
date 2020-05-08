 package rpisdd.rpgme.activities;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.dungeon.model.Dungeon;
 import rpisdd.rpgme.gamelogic.player.Player;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 
 public class DescendStairs extends Fragment implements OnTouchListener {
 
 	public DescendStairs() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		// TODO Art for stairs down screen
 		View v = inflater.inflate(R.layout.activity_splash_screen, container,
 				false);
 		v.setOnTouchListener(this);
 
 		// setup next floor
 		Log.d("Debug", "Making new floor, plan for transition");
 		Player.getPlayer().getDungeon().GenerateMap();
 		Dungeon newFloor = Player.getPlayer().getDungeon();
 		Player.getPlayer().roomX = newFloor.start_x;
		Player.getPlayer().roomY = newFloor.start_y;
 		// finish
 		return v;
 	}
 
 	public boolean onTouchEvent(MotionEvent arg1) {
 		toNextFloor();
 		return true;
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		return (this.onTouchEvent(arg1));
 	}
 
 	public void toNextFloor() {
 		TransitionFragment trans = new TransitionFragment();
 		trans.setValues(new DungeonMenu(), true);
 		((MainActivity) getActivity()).changeFragment(trans);
 	}
 
 }
