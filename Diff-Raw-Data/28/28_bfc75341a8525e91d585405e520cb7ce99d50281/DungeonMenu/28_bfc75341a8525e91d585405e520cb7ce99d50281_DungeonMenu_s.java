 package rpisdd.rpgme.activities;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.dungeon.model.Dungeon;
 import rpisdd.rpgme.gamelogic.dungeon.viewcontrol.DungeonSurfaceView;
 import rpisdd.rpgme.gamelogic.player.Player;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.ScrollView;
 
 public class DungeonMenu extends Fragment implements OnClickListener {
 
 	private Button enterTreasure;
 	private Dungeon dungeon;
 	private DungeonSurfaceView dungeonView;
 
 	public DungeonMenu() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
		dungeon = Player.getPlayer().getDungeon();
 
 		if (dungeon == null) {
 			Log.e("Error",
 					"Dungeon not generated yet. Can't enter dungeon menu!\n");
 		}
 
		if (!Player.getPlayer().StartPosSet()) {
			Log.d("Debug", "Player pos not set yet. Setting to start\n");
 			int dunStartX = dungeon.start_x;
 			int dunStartY = dungeon.start_y;
			Player.getPlayer().getDungeon()
					.visitRoom(dunStartX, dunStartY, null);
 			Player.getPlayer().setRoomPos(dunStartX, dunStartY);
 		}
 
 		View v = inflater.inflate(R.layout.dungeon_menu, container, false);
 
 		// enterTreasure = (Button) v.findViewById(R.id.treasureEnterButton);
 		// enterTreasure.setOnClickListener(this);
 
 		ScrollView scrollV = (ScrollView) v
 				.findViewById(R.id.dungeonVertScroll);
 		HorizontalScrollView scrollH = (HorizontalScrollView) v
 				.findViewById(R.id.dungeonHorizScroll);
 
 		dungeonView = (DungeonSurfaceView) v
 				.findViewById(R.id.dungeonSurfaceView);
 
 		// dungeonView.setZOrderOnTop(false); // necessary
 		// SurfaceHolder sfhTrackHolder = dungeonView.getHolder();
 		// sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
 
 		dungeonView.setFloorView(dungeon);
 		dungeonView.scrollV = scrollV;
 		dungeonView.scrollH = scrollH;
 
 		return v;
 	}
 
 	@Override
 	public void onClick(View v) {
 		/*
 		 * switch (v.getId()) { case R.id.treasureEnterButton: {
 		 * Log.i("my_message", "we entered the treasure!"); enterTreasure();
 		 * break; } default: break; }
 		 */
 	}
 
 	/*
 	 * public void enterTreasure() {
 	 * 
 	 * TreasureRoom tr = new TreasureRoom();
 	 * 
 	 * View popup = LayoutInflater.from(getActivity()).inflate(
 	 * R.layout.treasure_room, null);
 	 * 
 	 * TextView treasureText = (TextView) popup
 	 * .findViewById(R.id.treasureText);
 	 * 
 	 * treasureText.setText("You found a " + tr.getTreasureName() + "!");
 	 * 
 	 * ImageView treasureImg = (ImageView) popup
 	 * .findViewById(R.id.treasureImg);
 	 * 
 	 * // TO DO: figure out how to get the images for any kind of item // TO DO:
 	 * draw item on top of the treasure chest
 	 * treasureImg.setImageResource(R.drawable.sword1_red); AlertDialog.Builder
 	 * builder2 = new AlertDialog.Builder(getActivity());
 	 * builder2.setView(popup);
 	 * 
 	 * AlertDialog alert2 = builder2.create(); alert2.show(); }
 	 */
 
 	/**** experimental treasure room pop-up functionality ****/
 	// private Button enterTreasureBtn;
 	//
 	// public DungeonMenu() {
 	// }
 	//
 	// @Override
 	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	// Bundle savedInstanceState) {
 	//
 	// View v = inflater.inflate(R.layout.dungeon_menu, container, false);
 	//
 	// enterTreasureBtn = (Button) v.findViewById(R.id.treasureEnterButton);
 	// enterTreasureBtn.setOnClickListener(this);
 	// return v;
 	// }
 	//
 	// @Override
 	// public void onClick(View v) {
 	// switch (v.getId()) {
 	// case R.id.treasureEnterButton: {
 	// Log.i("my_message", "we entered the treasure!");
 	// enterTreasure();
 	// break;
 	// }
 	// default:
 	// break;
 	// }
 	// }
 	//
 	// public void enterTreasure() {
 	// View popup = LayoutInflater.from(getActivity()).inflate(
 	// R.layout.treasure_room, null);
 	//
 	// AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
 	// builder2.setView(popup);
 	//
 	// AlertDialog alert2 = builder2.create();
 	// alert2.show();
 	// }
 }
