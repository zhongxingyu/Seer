 package edu.selu.android.classygames;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.ProgressDialog;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.selu.android.classygames.data.Person;
 import edu.selu.android.classygames.utilities.ServerUtilities;
 import edu.selu.android.classygames.utilities.Utilities;
 
 
 public class CheckersGameActivity extends SherlockActivity implements OnClickListener
 {
 
 
 	private MyButton prevButton = null;
 	private MyButton[][] buttons;
 
 	private int greenNormal;
 	private int greenKing;
 	private int orangeNormal;
 	private int orangeKing;
 
 	public final static String INTENT_DATA_GAME_ID = "GAME_ID";
 	public final static String INTENT_DATA_PERSON_CHALLENGED_ID = "GAME_PERSON_CHALLENGED_ID";
 	public final static String INTENT_DATA_PERSON_CHALLENGED_NAME = "GAME_PERSON_CHALLENGED_NAME";
 
 
 	private boolean boardLocked = false;
 	private String gameId = null;
 	private Person personChallenged = null;
 	boolean hasMoved=false;
 
 	/**
 	 * JSON String downloaded from the server that represents the board
 	 */
 	private String board = null;
 
 
 	@SuppressLint("NewApi")
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.checkers_game_activity);
 		Utilities.styleActionBar(getResources(), getSupportActionBar());
 
 		// retrieve data passed to this activity
 		final Bundle bundle = getIntent().getExtras();
 
 		if (bundle == null || bundle.isEmpty())
 		{
 			activityHasError();
 		}
 		else
 		{
 			gameId = bundle.getString(INTENT_DATA_GAME_ID);
 			final long challengedId = bundle.getLong(INTENT_DATA_PERSON_CHALLENGED_ID);
 			final String challengedName = bundle.getString(INTENT_DATA_PERSON_CHALLENGED_NAME);
 
 			if (challengedId < 0 || challengedName == null || challengedName.isEmpty())
 			{
 				activityHasError();
 			}
 			else
 			{
 				personChallenged = new Person(challengedId, challengedName);
 				getSupportActionBar().setTitle(CheckersGameActivity.this.getString(R.string.checkers_game_activity_title) + " " + personChallenged.getName());
 
 				initBoard();
 
 				if (gameId == null || gameId.isEmpty())
 				{
 					initPieces();
 				}
 				else
 				{
 					new AsyncGetGame().execute();
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.checkers_game_activity, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 
 	@SuppressLint("NewApi")
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				finish();
 				return true;
 
 			case R.id.checkers_game_activity_actionbar_send_move:
 				new AsyncSendMove().execute();
 				return true;
 
 			case R.id.checkers_game_activity_actionbar_undo_move:
 				undo();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	@Override
 	public boolean onPrepareOptionsMenu(final Menu menu)
 	{
 		if (boardLocked)
 		{
 			menu.findItem(R.id.checkers_game_activity_actionbar_send_move).setEnabled(true);
 			menu.findItem(R.id.checkers_game_activity_actionbar_undo_move).setEnabled(true);
 		}
 		else
 		{
 			menu.findItem(R.id.checkers_game_activity_actionbar_send_move).setEnabled(false);
 			menu.findItem(R.id.checkers_game_activity_actionbar_undo_move).setEnabled(false);
 		}
 
 		return true;
 	}
 
 
 	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	private void initBoard()
 	{
 		prevButton = null;
 		greenNormal = R.drawable.piece_checkers_green_normal;
 		greenKing = R.drawable.piece_checkers_green_king;
 		orangeNormal = R.drawable.piece_checkers_orange_normal;
 		orangeKing = R.drawable.piece_checkers_orange_king;
 
 		Display display = getWindowManager().getDefaultDisplay();
 
 		TableLayout layout;
 		TableLayout.LayoutParams rowLp;
 		TableRow.LayoutParams cellLp;
 
 		if (android.os.Build.VERSION.SDK_INT >= 13)
 		// if the version of Android running this code is API Level 13 and higher (Honeycomb 3.2 and up)
 		// https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels
 		{
 			Point size = new Point();
 			display.getSize(size);
 			final int screen_width = size.x;
 
 			layout = new TableLayout(this);
 			rowLp = new TableLayout.LayoutParams(screen_width, screen_width / 8, 1);
 			cellLp = new TableRow.LayoutParams(screen_width / 8, screen_width / 8, 1);
 		}
 		else
 		{
 			final int screen_width = display.getWidth();
 
 			layout = new TableLayout(this);
 			rowLp = new TableLayout.LayoutParams(screen_width, screen_width / 8, 1);
 			cellLp = new TableRow.LayoutParams(screen_width / 8, screen_width / 8, 1);
 		}
 
 		TableRow[] rows = new TableRow[8];
 
 		for (int i = 0; i < 8; i++)
 		{
 			rows[i] = new TableRow(this);
 		}
 
 		buttons = new MyButton[8][8];
 
 		for (int y = 0; y < 8; ++y)
 		{
 			for (int x = 0; x < 8; ++x)
 			{
 				buttons[x][y] = new MyButton(this, x, y, true, false, false);
 				buttons[x][y].setId(x * 10 + y);
 				buttons[x][y].setOnClickListener(this);
 				buttons[x][y].setScaleType(ImageView.ScaleType.CENTER_CROP);
 
 				if ((x + y) % 2 == 1)
 				{
 					setBackground(R.drawable.bg_board_bright, buttons[x][y]);
 				}
 				else
 				{
 					setBackground(R.drawable.bg_board_dark, buttons[x][y]);
 				}
 
 				rows[y].addView(buttons[x][y], cellLp);
 			}
 		}
 
 		for (int i = 7; i >= 0; --i)
 		{
 			layout.addView(rows[i], rowLp);
 		}
 
 		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.checkers_game_activity_linearlayout);
 		linearLayout.addView(layout);
 	}
 
 
 	private void initPieces()
 	{
 		for (int x = 0; x < 8; ++x)
 		{
 			for (int y = 0; y < 8; ++y)
 			{
 				if (((y + x) % 2 == 1) && (y <= 2 || y >= 5))
 				{
 					buttons[x][y].setEmpty(false);
 					buttons[x][y].setCrown(false);
 
 					if (y <= 2)
 					{
 						buttons[x][y].setPlayerGreen(true);
 						buttons[x][y].setImageResource(greenNormal);
 					}
 					else
 					{
 						buttons[x][y].setPlayerGreen(false);
 						buttons[x][y].setImageResource(orangeNormal);
 					}
 				}
 			}
 		}
 		
 		/*buttons[6][3].setPlayerGreen(false);
 		buttons[6][3].setImageResource(orangeNormal);
 		buttons[6][3].setEmpty(false);
 		buttons[6][3].setCrown(false);*/
 	}
 
 
 	private void clearPieces()
 	{
 		for (int x = 0; x < 8; ++x)
 		{
 			for (int y = 0; y < 8; ++y)
 			{
 				buttons[x][y].setEmpty(true);
 				buttons[x][y].setCrown(false);
 				buttons[x][y].setImageResource(0);
 			}
 		}
 	}
 
 
 	private final class AsyncGetGame extends AsyncTask<Void, Void, String>
 	{
 
 
 		private ProgressDialog progressDialog;
 
 
 		@Override
 		protected String doInBackground(final Void... v)
 		{
 			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_ID, gameId));
 
 			try
 			{
 				return ServerUtilities.postToServer( ServerUtilities.SERVER_GET_GAME_ADDRESS, nameValuePairs );
 			}
 			catch (final IOException e)
 			{
 				Log.e(Utilities.LOG_TAG, "Error in HTTP POST to " + ServerUtilities.SERVER_GET_GAME_ADDRESS, e);
 			}
 			
 			return null;
 		}
 
 
 		@Override
 		protected void onPostExecute(final String serverResponse)
 		{
 			board = parseServerResponse(serverResponse);
 			buildBoard();
 
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			System.out.println( "Progress Dialog creation starting");
 			progressDialog = new ProgressDialog(CheckersGameActivity.this);
 			progressDialog.setMessage(CheckersGameActivity.this.getString(R.string.checkers_game_activity_getgame_progressdialog_message));
 			progressDialog.setTitle(R.string.checkers_game_activity_getgame_progressdialog_title);
 			progressDialog.setCancelable(false);
 			progressDialog.setCanceledOnTouchOutside(false);
 			progressDialog.show();
 		}
 
 
 	}
 
 
 	private final class AsyncSendMove extends AsyncTask<Void, Void, String>
 	{
 
 
 		private ProgressDialog progressDialog;
 
 
 		@Override
 		protected String doInBackground(final Void... v)
 		{
 			try
 			{
 				JSONArray teams = createJSONTeams();
 				JSONObject board = new JSONObject();
 				board.put("teams", teams);
 
 				JSONObject object = new JSONObject();
 				object.put("board", board);
 
 				Log.d(Utilities.LOG_TAG, object.toString());
 
 				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 
 				try
 				{
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_USER_CHALLENGED, Long.valueOf(personChallenged.getId()).toString()));
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_NAME, personChallenged.getName()));
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_USER_CREATOR, Long.valueOf(Utilities.getWhoAmI(CheckersGameActivity.this).getId()).toString()));
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_BOARD, object.toString()));
 
 					if (gameId == null || gameId.isEmpty())
 					{
 						return ServerUtilities.postToServer(ServerUtilities.SERVER_NEW_GAME_ADDRESS, nameValuePairs);
 					}
 					else
 					{
 						nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_GAME_ID, gameId));
 						return ServerUtilities.postToServer(ServerUtilities.SERVER_NEW_MOVE_ADDRESS, nameValuePairs);
 					}
 				}
 				catch (final IOException e)
 				{
 					Log.e(Utilities.LOG_TAG, "Error in HTTP POST to server.", e);
 				}
 			}
 			catch (final JSONException e)
 			{
 				Log.e(Utilities.LOG_TAG, "Error in creating JSON data to send to server.", e);
 			}
 
 			return null;
 		}
 
 
 		@Override
 		protected void onPostExecute(final String serverResponse)
 		{
 			parseServerResponse(serverResponse);
 
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 
 			finish();
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(CheckersGameActivity.this);
 			progressDialog.setCancelable(false);
 			progressDialog.setCanceledOnTouchOutside(false);
 			progressDialog.setMessage(CheckersGameActivity.this.getString(R.string.checkers_game_activity_sendmove_progressdialog_message));
 			progressDialog.setTitle(R.string.checkers_game_activity_sendmove_progressdialog_title);
 			progressDialog.show();
 
 			CheckersGameActivity.this.setResult(GamesListActivity.NEED_TO_REFRESH);
 		}
 
 
 		private JSONArray createJSONTeams()
 		{
 			JSONArray teamGreen = createJSONTeam(true);
 			JSONArray teamOrange = createJSONTeam(false);
 
 			JSONArray teams = new JSONArray();
 			teams.put(teamGreen);
 			teams.put(teamOrange);
 
 			return teams;
 		}
 
 
 		private JSONArray createJSONTeam(final boolean isPlayerGreen)
 		{
 			JSONArray team = new JSONArray();
 
 			for (int x = 0; x < 8; ++x)
 			{
 				for (int y = 0; y < 8; ++y)
 				{
 					if (!buttons[x][y].isEmpty() && buttons[x][y].isPlayerGreen() == isPlayerGreen)
 					// this position has a piece in it that is of the given team color
 					{
 						try
 						{
 							JSONArray coordinate = new JSONArray();
 							coordinate.put(x);
 							coordinate.put(y);
 
 							JSONObject piece = new JSONObject();
 							piece.put("coordinate", coordinate);
 
 							if( isKing(buttons[x][y]) == true )
 							{
 								piece.put("type", 2);
 							}
 							else
 							{
 								piece.put("type", 1);
 							}
 
 							team.put(piece);
 						}
 						catch (final JSONException e)
 						{
 
 						}
 					}
 				}
 			}
 
 			return team;
 		}
 
 
 	}
 
 
 	private String parseServerResponse(String jsonString)
 	{
 		if (jsonString == null || jsonString.isEmpty())
 		{
 			Log.e(Utilities.LOG_TAG, "Empty string received from server on send move!");
 		}
 		else
 		{
 			//jsonString = jsonString.replace("\\", "");
 			try
 			{
 				Log.d(Utilities.LOG_TAG, "Parsing JSON data: " + jsonString);
 				final JSONObject jsonData = new JSONObject(jsonString);
 				final JSONObject jsonResult = jsonData.getJSONObject(ServerUtilities.POST_DATA_RESULT);
 
 				try
 				{
 					final String successData = jsonResult.getString(ServerUtilities.POST_DATA_SUCCESS);
 					Log.d(Utilities.LOG_TAG, "Server returned successful message: " + successData);
 
 					return successData;
 				}
 				catch (final JSONException e)
 				{
 					try
 					{
 						final String errorMessage = jsonResult.getString(ServerUtilities.POST_DATA_ERROR);
 						Log.d(Utilities.LOG_TAG, "Server responded with error: " + errorMessage);
 
 						return errorMessage;
 					}
 					catch (final JSONException e1)
 					{
 						Log.e(Utilities.LOG_TAG, "Data returned from server contained no error message.");
 					}
 				}
 			}
 			catch (final JSONException e)
 			{
 				Log.e(Utilities.LOG_TAG, "Couldn't grab result object from server response.");
 			}
 		}
 
 		return null;
 	}
 
 
 	private void buildBoard()
 	{
 			if (board != null && !board.isEmpty())
 			{
 					try
 					{
 						JSONObject JSON = new JSONObject(board);
 						JSONObject boardJSON = JSON.getJSONObject("board");
 						JSONArray array = boardJSON.getJSONArray("teams");
 						
 						for( int i = 0; i < array.length(); i++ )
 						{
 							JSONArray team = array.getJSONArray(i);
 							for (int j = 0; j < team.length(); ++j)
 							{
 								JSONObject piece = team.getJSONObject(j);
 								int type = piece.getInt("type");
 								JSONArray coordinates = piece.getJSONArray("coordinate");
 								
 								if( coordinates.length() == 2 )
 								{
 									int x = coordinates.getInt(0);
 									int y = coordinates.getInt(1);
 									
 									if( x >= 0 && x <= 7 )
 									{
 										if( y >= 0 && y <= 7 )
 										{
 											buttons[x][y].setEmpty(false);
 											
 											if( i == 0 )
 											{
 												buttons[x][y].setPlayerGreen(true);
 												buttons[x][y].setImageResource(greenNormal);
 												
 												if( type == 1 )
 												{
 													buttons[x][y].setCrown(false);
 												}
 												else
 												{
 													buttons[x][y].setCrown(true);
													buttons[x][y].setImageResource(greenKing);
 												}
 											}
 											else
 											{
 												buttons[x][y].setPlayerGreen(false);
 												buttons[x][y].setImageResource(orangeNormal);
 												
 												if( type == 1 )
 												{
 													buttons[x][y].setCrown(false);
 												}
 												else
 												{
 													buttons[x][y].setCrown(true);
													buttons[x][y].setImageResource(orangeKing);
 												}
 											}
 										}
 										else
 										{
 											Log.e(Utilities.LOG_TAG, "Coordinate outside proper range");
 										}
 									}
 									
 								}
 								
 							}
 						}						
 						
 					}
 					catch (JSONException e1)
 					{
 						Log.e("Array", "Could not get board for array");
 					}
 					
 			}
 			else
 			{
 				Log.e("Server Result", "placeholder");
 			}
 
 	}
 
 
 	@SuppressLint("NewApi")
 	@Override
 	public void onClick(View arg0)
 	{
 		MyButton clickedButton = (MyButton) findViewById(arg0.getId());
 
 		//if(!clickedButton.isEmpty() && prevButton == null)
 
 		if (!boardLocked)
 		{
 			if(!clickedButton.isPlayerGreen()&& !clickedButton.isEmpty())
 			{
 				prevButton = null;
 			}
 			else if (prevButton != null)
 			{
 			if (clickedButton.isEmpty())
 			{
 				if (canMove(clickedButton))
 				{
 					Move(clickedButton);
 					
 					if (canBeKing(clickedButton))
 					{
 						makeKing(clickedButton);
 					}
 					
 					boardLocked = true;
 					this.invalidateOptionsMenu();
 				}
 				else if (canJump(clickedButton))
 				{
 					Move(clickedButton);
 					if (canBeKing(clickedButton))
 					{
 						makeKing(clickedButton);
 					}
 					
 					boardLocked = true;
 					this.invalidateOptionsMenu();
 				}
 				else 
 				{
 					setBackground(R.drawable.bg_board_bright, prevButton);
 					prevButton = null;
 				}
 			}
 			else
 			{
 				setBackground(R.drawable.bg_board_bright, prevButton);
 				prevButton = null;
 			}
 		}
 		else
 		{
 			if (!clickedButton.isEmpty()){
 				prevButton = clickedButton;
 				setBackground(R.drawable.bg_board_bright_selected, clickedButton);
 			}
 		}
 	}
 }
 
 	
 	private boolean canBeKing (MyButton pbutton) {
 		if(pbutton.isPlayerGreen() && pbutton.getPy() == 7)
 		{
 			return true;
 		}
 		else if(!pbutton.isPlayerGreen() && pbutton.getPy() == 7)
 		{
 			return true;
 		}
 		else
 			return false;
 	}
 
 
 	private void makeKing(MyButton pButton)
 	{
 		if(pButton.isPlayerGreen())
 		{
 			pButton.setImageResource(R.drawable.piece_checkers_green_king);
 		}
 		else if (!pButton.isPlayerGreen())
 		{
 			pButton.setImageResource(R.drawable.piece_checkers_orange_king);
 		}
 		pButton.setCrown(true);
 	}
 	
 
 	private boolean isKing(MyButton pButton) 
 	{		
 		if(pButton.isCrown())
 			return true;
 		else
 			return false;
 	}
 
 	
 
 	private void Move(MyButton clickedButton) 
 	{
 		//change image and data
 		prevButton.setImageResource(0);
 		prevButton.setEmpty(true);
 
 		setBackground(R.drawable.bg_board_bright, prevButton);
 		
 		if(prevButton.isPlayerGreen() && isKing(prevButton))
 		{
 			clickedButton.setImageResource(greenKing);
 			clickedButton.setCrown(true);
 			clickedButton.setPlayerGreen(true);
 		}
 		
 		else if (!prevButton.isPlayerGreen() && isKing(prevButton))
 		{
 			clickedButton.setImageResource(orangeKing);
 			clickedButton.setCrown(true);
 			clickedButton.setPlayerGreen(false);
 		}
 		
 		else if (prevButton.isPlayerGreen())
 		{
 			clickedButton.setImageResource(greenNormal);
 			clickedButton.setPlayerGreen(true);
 		}
 		
 		else{
 			clickedButton.setImageResource(orangeNormal);
 			clickedButton.setPlayerGreen(false);
 		}
 		
 		clickedButton.setEmpty(false);
 		prevButton.setCrown(false);
 		
 		prevButton = null;
 	}
 
 	private boolean canMove(MyButton button)
 	{	
 		if (prevButton.isCrown()){
 			if(abs(button.getPx()-prevButton.getPx()) == 1 && abs(button.getPy()- prevButton.getPy()) == 1)
 			{
 				hasMoved = true;
 				return true;
 			}
 			else 
 				return false;
 		}
 		else {
 			if (!prevButton.isPlayerGreen()){
 				if(abs(button.getPx()-prevButton.getPx()) == 1 && (button.getPy()- prevButton.getPy()) == 1)
 					return true;
 				else 
 					return false;
 			}
 			else {
 				if (abs(button.getPx()-prevButton.getPx()) == 1 && (button.getPy()-prevButton.getPy()) == 1)
 				{
 					hasMoved = true;
 					return true;
 				}
 				else
 					return false;
 			}
 		}
 	}
 	
 	private boolean canJump(MyButton cbutton)
 	{
 		if (!prevButton.isCrown())
 		{
 			int change_In_X = (cbutton.getPx() - prevButton.getPx())/2;
 			int change_In_Y = (cbutton.getPy() - prevButton.getPy())/2;
 			
 			MyButton middleButton = (MyButton)findViewById((prevButton.getPx() + change_In_X) *10 + (prevButton.getPy() + change_In_Y));
 			
 			if(prevButton.isPlayerGreen())
 			  {
 				  if (abs(cbutton.getPx()-prevButton.getPx()) == 2 && (cbutton.getPy()-prevButton.getPy()) == 2)
 				  {
 						if (middleButton.isPlayerGreen() != prevButton.isPlayerGreen())
 						{
 							middleButton.setEmpty(true);
 							middleButton.setImageResource(0);
 							return true;
 						}
 						else 
 						{
 							return false;
 						}
 					}
 				  	else 
 				  	{
 				  		return false;
 				  	}
 			  }
 			  else
 			  {
 				  if (abs(cbutton.getPx()-prevButton.getPx()) == 2 && (cbutton.getPy()-prevButton.getPy()) == -2)
 				  {
 						if (middleButton.isPlayerGreen() != prevButton.isPlayerGreen())
 						{
 							middleButton.setEmpty(true);
 							middleButton.setImageResource(0);
 							return true;
 						}
 					
 						else 
 						{
 							return false;
 						}
 				  }
 				  else 
 				  {
 					  return false;
 				  }
 			 } 
 		}
 		
 		else 
 		{	
 			int change_In_X = (cbutton.getPx() - prevButton.getPx())/2;
 			int change_In_Y = (cbutton.getPy() - prevButton.getPy())/2;
 			
 			MyButton middleButton = (MyButton)findViewById((prevButton.getPx() + change_In_X) *10 + (prevButton.getPy() + change_In_Y));
 			
 			if(prevButton.isPlayerGreen())
 			  {
 				  if (abs(cbutton.getPx()-prevButton.getPx()) == 2 && abs(cbutton.getPy()-prevButton.getPy()) == 2)
 				  {
 						if (middleButton.isPlayerGreen() != prevButton.isPlayerGreen())
 						{
 							middleButton.setEmpty(true);
 							middleButton.setImageResource(0);
 							return true;
 						}
 						else 
 						{
 							return false;
 						}
 				  }
 				  else 
 				  	{
 				  		return false;
 				  	}
 			  }
 			  else
 			  {
 				  if (abs(cbutton.getPx()-prevButton.getPx()) == 2 && abs(cbutton.getPy()-prevButton.getPy()) == 2)
 				  {
 						if (middleButton.isPlayerGreen() != prevButton.isPlayerGreen())
 						{
 							middleButton.setEmpty(true);
 							middleButton.setImageResource(0);
 							return true;
 						}
 					
 						else 
 						{
 							return false;
 						}
 				  }
 				  else 
 				  {
 					  return false;
 				  }
 			 } 
 		}
 	}
 
 	private int abs(int i)
 	{
 		return (i < 0) ? -1 * i : i;
 	}
 
 
 	@SuppressLint("NewApi")
 	private void undo()
 	{
 		clearPieces();
 		if (board == null || board.isEmpty())
 		// simply re instantiate the default board
 		{
 			initPieces();
 		}
 		else
 		// re parse the board string that we downloaded
 		{
 			buildBoard();
 		}
 
 		boardLocked = false;
 		CheckersGameActivity.this.invalidateOptionsMenu();
 	}
 
 
 	private void activityHasError()
 	{
 		Utilities.easyToastAndLogError(CheckersGameActivity.this, CheckersGameActivity.this.getString(R.string.checkers_game_activity_data_error));
 		finish();
 	}
 
 
 	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	private void setBackground(final int resource, final View view)
 	{
 		final Drawable drawable = getResources().getDrawable(resource);
 
 		if (android.os.Build.VERSION.SDK_INT >= 16)
 		// if the version of Android running this code is API Level 16 and higher (JellyBean 4.1 and up)
 		// https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels
 		{
 			view.setBackground(drawable);
 		}
 		else
 		{
 			view.setBackgroundDrawable(drawable);
 		}
 	}
 
 
 }
 
