 package org.game.advwars;
 
 import android.view.*;
 import player.Player;
 import controller.Controller;
 import dataconnectors.SaveGUIData;
 import dataconnectors.SaveGameData;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.PointF;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.util.FloatMath;
 import android.util.Log;
 import android.view.View.OnTouchListener;
 
 import java.util.ArrayList;
 
 public class GameBoard extends Activity implements OnTouchListener
 {
 	private MediaPlayer mp;
 	private Controller c;
 	private static final String TAG = "GameBoard";
 	// We can be in one of these 3 states
 	static final int NONE = 0;
 	static final int DRAG = 1;
 	static final int ZOOM = 2;
 	static final int PRESS = 3;
 
 	int mode = NONE;
 
 	// Remember some things for zooming
 	PointF start = new PointF();
 	PointF mid = new PointF();
 	float oldDist = 1f;
 
 	private GameBoardView gameBoardView;
 	private GUIGameValues ggvGlobal = new GUIGameValues();
 	private SaveGUIData sd = new SaveGUIData();
 	private SaveGameData saveGame = new SaveGameData();
 	private char p1Char;
 	private char p2Char;
 	private  ArrayList<String> commands;
 
 	private boolean endTurn = false;
 	private boolean move = false;
 	private boolean aiTurn = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gameboard_layout);
 		// Tells Android to adjust the volume of music instead of the ringer
 		//setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		// Load player values
 		ggvGlobal = sd.loadGGVData();
 
 		if (ggvGlobal.getFaction().equals("Blue"))
 		{
 			p1Char = 'b';
 			p2Char = 'r';
 		}
 		else
 		{
 			p1Char = 'r';
 			p2Char = 'b';
 		}
 
 		gameBoardView = (GameBoardView) findViewById(R.id.gameboard);
 		gameBoardView.setOnTouchListener(this);
 
 		Player p1 = new Player("Player1", 0, p1Char);
 		Player p2 = new Player("Player2", 1, p2Char);
 		c = new Controller(p1, p2, true, ggvGlobal.getMap());
 		gameBoardView.setController(c);
 		gameBoardView.initGame();
 		sd.saveGGVData(ggvGlobal);
 
 		// Background music
 		if (mp != null){
 			mp.release();
 		}
 
 		// Begin gameplay music
 		mp = MediaPlayer.create(this, R.raw.background_music);
 		mp.setLooping(true);
 		mp.start();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		if (keyCode == KeyEvent.KEYCODE_MENU)
 		{
 			Intent i = new Intent(this, InGameMainMenu.class);
 			i.putExtra("ggv", ggvGlobal);
 			startActivityForResult(i, 0);
 
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		ggvGlobal = sd.loadGGVData();
 
 		/*----------------- Actions based on in-game main menu selection -----------------*/
 
 		if (ggvGlobal.getInGameMenuEndTurn())
 		{
 			// End turn
 			endTurn = true;
 
 			if (aiTurn = false)
 				aiTurn = true;
 		}
 		else if (ggvGlobal.getInGameMenuSaveGame())
 		{
 			// Save game
 			saveGame.saveGameData(c);
 		}
 		else if (ggvGlobal.getInGameMenuQuitGame())
 		{
 			Intent i = new Intent(this, MainMenu.class);
 			i.putExtra("ggv", ggvGlobal);
 			startActivityForResult(i, 0);
 		}
 		else
 		{
 			// Do nothing
 			// Only hit if a user decides to close the main menu by using phone's Back button
 		}
 
 
 		/*------------------- Actions based on in-game menu selection -------------------*/
 
 
 		if (ggvGlobal.getSelectedUnit() != null && ggvGlobal.getSelectedUnit() != "")
 		{
 			c.produceUnit(ggvGlobal.getSelectedUnit());
 			gameBoardView.setController(c);
 			gameBoardView.initGame();
 		}
 		else if (ggvGlobal.getSelectedCommand() > -1 && ggvGlobal.getSelectedCommand() < 4)
 		{
 			// Move
 			if (ggvGlobal.getSelectedCommand() == 0)
 			{
 				move = true;
 				ggvGlobal.setMovement(c.unitTakeAction(ggvGlobal.getSelectedCommand()));
 				sd.saveGGVData(ggvGlobal);
 				gameBoardView.setController(c);
 				gameBoardView.initGame();
 			}
 			// Attack
 			else if (ggvGlobal.getSelectedCommand() == 1)
 			{
 
 			}
 			// Capture
 			else if (ggvGlobal.getSelectedCommand() == 2)
 			{
 
 			}
 			// Unit info
 			else
 			{
 				c.unitTakeAction(ggvGlobal.getSelectedCommand());
 				gameBoardView.setController(c);
 				gameBoardView.initGame();
 			}
 		}
 		else
 		{
 			// Do nothing
 			// This is not good because it means something is failing :-(
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event)
 	{
 		// Handle touch events here...
 		switch (event.getAction() & MotionEvent.ACTION_MASK)
 		{
 		case MotionEvent.ACTION_DOWN:
 			start.set(event.getX(), event.getY());
 			Log.d(TAG, "mode=DRAG");
 			mode = PRESS;
 			break;
 		case MotionEvent.ACTION_POINTER_DOWN:
 			oldDist = spacing(event);
 			Log.d(TAG, "oldDist=" + oldDist);
 			if (oldDist > 10f)
 			{
 				midPoint(mid, event);
 				mode = ZOOM;
 				Log.d(TAG, "mode=ZOOM");
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_POINTER_UP:
 			if(mode == PRESS)
 			{
 				ggvGlobal = sd.loadGGVData();
 
 				// Get selected points from game board view
 				int[] selectedPoints = gameBoardView.getPoints(event.getX(), event.getY());
 
 				if (move == true && selectedPoints != null &&
 						c.isValidMove(selectedPoints[0], selectedPoints[1]))
 				{
 					c.unitMove(selectedPoints[0], selectedPoints[1]);
 					gameBoardView.setController(c);
 					gameBoardView.initGame();
 					move = false;
 				}
 				else 
 				{
 					move = false; 
 					// If selected points are valid pass them to the controller and get a list of commands from it
 					if (selectedPoints != null)
 						commands = c.selectCoordinates(selectedPoints[0], selectedPoints[1]);
 
 					// Check if commands are valid
 					if (commands != null && !commands.isEmpty())
 					{
 						// What to do if commands are for a unit
						if (commands.contains("UnitInfo"))
 						{
 							ggvGlobal.setAvailableCommands(commands);
 							ggvGlobal.setUnitInfo(c.getUnitInfo());
 							Intent i = new Intent(this, InGameMenu.class);
 							i.putExtra("ggv", ggvGlobal);
 							startActivityForResult(i, 0);
 							commands.clear();
 						}
 						// What to do if commands are for production building
 						else
 						{
 							ggvGlobal.setAvailableCommands(commands);
 							Intent i2 = new Intent(this, UnitSelectionMenu.class);
 							i2.putExtra("ggv", ggvGlobal);
 							startActivityForResult(i2, 0);
 						}
 					}
 				}
 			}
 			mode = NONE;
 			Log.d(TAG, "mode=NONE");
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if(mode == PRESS && 
 			(Math.abs(event.getX() - start.x) > 20 ||
 					Math.abs(event.getY() - start.y) > 20)){
 				mode = DRAG;
 			}
 
 			if (mode == DRAG) {
 				// ...
 				gameBoardView.translateBoard(-(event.getX() - start.x),
 						-(event.getY() - start.y));
 			}
 			else if (mode == ZOOM) {
 				float newDist = spacing(event);
 				if (newDist > 10f) {
 					float scale = newDist / oldDist;
 					gameBoardView.scaleBoard(scale,  mid.x, mid.y);
 				}
 			}
 			break;
 		}
 
 		v.invalidate();
 
 		return true;
 	}
 
 
 	/** Determine the space between the first two fingers */
 	private float spacing(MotionEvent event) {
 		// ...
 		float x = event.getX(0) - event.getX(1);
 		float y = event.getY(0) - event.getY(1);
 		return FloatMath.sqrt(x * x + y * y);
 	}
 
 	/** Calculate the mid point of the first two fingers */
 	private void midPoint(PointF point, MotionEvent event) {
 		// ...
 		float x = event.getX(0) + event.getX(1);
 		float y = event.getY(0) + event.getY(1);
 		point.set(x / 2, y / 2);
 	}
 }
