 package ui;
 
 
 
 import engine.GameEngine;
 import unabomber.client.R;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.widget.Toast;
 
 public class BombsOverlay extends UnabomberItemsOverlay {
 
 	private int myPlayerId;
 	private Context mContext;
 	private GameEngine mEngine;
 
 
	public BombsOverlay(Drawable marker) {
		super(marker);
	}
 	
 	
 	//
 	public BombsOverlay(Drawable defaultMarker, int myPlayerId, GameEngine engine,
 			Context context) {
 		super(defaultMarker);
 		this.myPlayerId = myPlayerId;
 		this.mContext = context;
 		this.mEngine = engine;
 	}
 	//
 	
 	
 	public void addBombAt(Location location) {
 		addItemAt(location);
 		populate();
 	}
 	
 	//
 	@Override
 	protected boolean onTap(final int index) {
 
 		// option to use as action
 		final CharSequence[] items = { "Detonate" };
 
 		
 		// if you have added something, change handleMenuOption
//		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//		builder.setTitle(R.string.other_player_menu_title);
//		builder.setItems(items, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int item) {
//				handleMenuOption(item, index);
//			}
//		});
//		AlertDialog alert = builder.create();
//		alert.show();
 		return true;
 	}
 	//
 	// handle the single option that a player has done
 	protected void handleMenuOption(final int optionIndex, final int bombIndex) {
 		
 		
 		//get target player ID
 		int targetBombID = Integer.parseInt(locations.get(bombIndex).getSnippet());
 		
 
 		// obtain the user decision
 		switch (optionIndex) {
 		case 0: // send another player to jail
 
 			mEngine.detonateBomb(myPlayerId, targetBombID);
 
 			//feedback the results
 			Toast.makeText(mContext, R.string.bomb_detonated, Toast.LENGTH_SHORT)
 					.show();
 			break;
 
 		default: // defensive programming
 			Toast.makeText(mContext, R.string.unknown_action,
 					Toast.LENGTH_SHORT).show();
 		}
 
 	}
 	//
 }
