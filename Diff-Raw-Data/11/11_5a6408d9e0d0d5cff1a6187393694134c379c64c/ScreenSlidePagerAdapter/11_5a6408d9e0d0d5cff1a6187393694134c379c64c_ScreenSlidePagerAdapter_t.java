 package ca.polymtl.inf8405.sevenwonders;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.util.SparseArray;
 import ca.polymtl.inf8405.sevenwonders.api.GameState;
 import ca.polymtl.inf8405.sevenwonders.api.Hand;
 import ca.polymtl.inf8405.sevenwonders.api.Player;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A simple pager adapter that represents all player objects, in
  * sequence.
  */
 public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
 
 	public ScreenSlidePagerAdapter(FragmentManager fm, int count) {
         super(fm);
         Log.wtf("PagerAdapter", "Constructor");
         count_ = count;
     }
 
 	@Override
 	public Fragment getItem(int position) {
 		Log.wtf("PagerAdapter", "getItem = " + position);
 		Fragment fragment = new GameScreenFragment();
 		Bundle args = new Bundle();
 		args.putInt("position", position);
 		fragment.setArguments(args);
 		
 		return fragment;
 	}
 
 	@Override
 	public int getCount() {
 		Log.wtf("PagerAdapter", "getCount = " + count_);
 		return count_;
 	}
 
	@Override
	public int getItemPosition(Object object) {
	    return POSITION_NONE;
	}
	
     public void setState( final GameState state ) {
     	players_ = state.players;
     	hand_ = state.hand;
 //    	Log.e("PagerAdapter", "Hand =" + state.hand.unplayables.size() + " - fragments=" + fragments.size());
 //        List<Player> players = state.getPlayers();
 //        for( int i = 0; i < fragments.size(); i++ ){
 //            fragments.get(i).update(players.get(i), state.getHand());
 //        }
         // TODO: Update scoreboard View
     }
 
     private int count_;
 //    private SparseArray<GameScreenFragment> fragments = new SparseArray<GameScreenFragment>();
     public static List<Player> players_;
     public static Hand hand_;
 }
