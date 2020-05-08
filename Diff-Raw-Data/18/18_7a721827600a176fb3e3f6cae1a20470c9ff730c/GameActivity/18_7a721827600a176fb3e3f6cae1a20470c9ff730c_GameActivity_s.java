 package net.ark.tictachead.activities;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import net.ark.tictachead.R;
 import net.ark.tictachead.models.FriendManager;
 import net.ark.tictachead.models.GameManager;
 import net.ark.tictachead.models.Player;
 import net.ark.tictachead.models.Tictactoe;
 import net.ark.tictachead.services.HeadService;
 import net.ark.tictachead.services.MoveService;
 import net.ark.tictachead.services.RoomService;
 import net.ark.tictachead.services.RoomsService;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class GameActivity extends Activity implements OnClickListener, OnTouchListener {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		//Super
 		super.onCreate(savedInstanceState);
 		
 		//Set layout
 		setContentView(R.layout.game_layout);
 
 		//If no opponent, finish
 		if (FriendManager.instance().getOpponents().length <= 0) finish();
 		else {
 			//Initialize
 			m_TouchedOutside	= false;
 			m_HeadsTable		= new Hashtable<String, View>();
 			m_UsersTable        = new Hashtable<View, String>();
 			m_Opponents         = new ArrayList<String>();
 			m_Board 			= new View[][]{
 				new View[]{ findViewById(R.id.view_cell00), findViewById(R.id.view_cell01), findViewById(R.id.view_cell02)  },
 				new View[]{ findViewById(R.id.view_cell10), findViewById(R.id.view_cell11), findViewById(R.id.view_cell12)  },
 				new View[]{ findViewById(R.id.view_cell20), findViewById(R.id.view_cell21), findViewById(R.id.view_cell22)  }
 			};
 
 			//Set listeners
 			findViewById(R.id.layout_game).setOnTouchListener(this);
 			findViewById(R.id.button_play).setOnClickListener(this);
 			findViewById(R.id.image_friends).setOnClickListener(this);
 			findViewById(R.id.button_close).setOnClickListener(this);
 			for (int i = 0; i < m_Board.length; i++) for (int j = 0; j < m_Board[i].length; j++) m_Board[i][j].setOnClickListener(this);
 
 			//Get opponents
 			List<String> OpponentList   = new ArrayList<String>();
 			String[] Opponents          = FriendManager.instance().getOpponents();
 
 			//Add opponents
 			OpponentList.add(FriendManager.instance().getActiveOpponent());
 			for (int i = 0; i < Opponents.length; i++) if (!Opponents[i].equals(OpponentList.get(0))) OpponentList.add(Opponents[i]);
 			for (int i =  OpponentList.size() - 1; i >= 0; i--) addUser(OpponentList.get(i));
 
 			//Should load game?
 			boolean Load    = false;
 			String Active   = FriendManager.instance().getActiveOpponent();
 			Tictactoe Game  = GameManager.instance().getGame(Active);
 			if (Game == null)           Load = true;
 			else if (!Game.isMyTurn())  Load = true;
 
 			//If load
 			if (Load && !GameManager.instance().isLoading(Active)) {
 				//Start service
 				Intent RoomIntent = new Intent(this, RoomService.class);
 				RoomIntent.putExtra(RoomService.EXTRA_OPPONENT, Active);
 				startService(RoomIntent);
 
 				//Load
 				GameManager.instance().loadGame(Active);
 			}
 
 			//Set user
 			setActiveUser(Active);
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		//Super
 		super.onResume();
 
 		//Register game receiver
 		IntentFilter GameFilter = new IntentFilter();
 		GameFilter.addAction(GAME_CHANGED);
 		registerReceiver(m_GameReceiver, GameFilter);
 
 		//Hide head
 		Intent HeadIntent = new Intent(this, HeadService.class);
 		HeadIntent.putExtra(HeadService.EXTRA_SHOW, false);
 		startService(HeadIntent);
 	}
 
 	@Override
 	protected void onPause() {
 		//Super
 		super.onPause();
 		
 		//Remove receivers
 		unregisterReceiver(m_GameReceiver);
 
 		//Show head again
 		Intent HeadIntent = new Intent(this, HeadService.class);
 		HeadIntent.putExtra(HeadService.EXTRA_SHOW, true);
 		startService(HeadIntent);
 	}
 
 	@Override
 	public void onClick(View v) {
 		//Skip no view 
 		if (v == null) return;
 		
 		//Get game
 		Tictactoe Game = GameManager.instance().getGame(m_ActiveUser);
 		
 		//Check view ID
 		switch (v.getId()) {
 		case R.id.image_friends:
 			//Go to friends activity
 			startActivity(new Intent(this, FriendsActivity.class));
 			break;
 
 		case R.id.button_play:
 			//If game exist
 			if (Game != null) {
 				//Reset game
 				if ((Game.isMyTurn())) Game.reset();
 				//Game.reset();
 				//if (!Game.isMyTurn()) Game.fill();
 
 				//Redraw canvas
 				refreshDisplay(Game);
 			}
 
 			break;
 
 		case R.id.button_close:
 			//Remove
 			removeUser(m_ActiveUser);
 			if (m_ActiveUser == null) {
 				//Kill head
 				Intent HeadIntent = new Intent(this, HeadService.class);
 				HeadIntent.putExtra(HeadService.EXTRA_KILL, true);
 				startService(HeadIntent);
 
 				//Done
 				finish();
 			}
 			break;
 
 		case R.id.view_cell00:
 		case R.id.view_cell10:
 		case R.id.view_cell20:
 		case R.id.view_cell01:
 		case R.id.view_cell11:
 		case R.id.view_cell21:
 		case R.id.view_cell02:
 		case R.id.view_cell12:
 		case R.id.view_cell22:
 			//If game exist and not full yet
 			if (Game != null && Game.isMyTurn() && !Game.isFull()) {
 				//Get X,Y, and if empty
 				int Y = getCellRow(v);
 				int X = getCellColumn(v);
 				if (Game.getStatus(X, Y) == Tictactoe.EMPTY_CELL) {
 					//Move
 					Game.fill(X, Y);
 					GameManager.instance().queueGame(m_ActiveUser);
 					GameManager.instance().sendGame(m_ActiveUser);
 
 					//Create intent
 					Intent MoveIntent = new Intent(this, MoveService.class);
 					MoveIntent.putExtra(MoveService.EXTRA_OPPONENT, m_ActiveUser);
 					startService(MoveIntent);
 
 					//Redraw
 					refreshDisplay(Game);
 				}
 			}
 			break;
 
 		default:
 			//Set user
 			String User = m_UsersTable.get(v);
 			if (User != null) setActiveUser(User);
 			break;
 		}
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		//Initialize
 		boolean Consume = false;
 		
 		//If root
 		if (v.getId() == R.id.layout_game && v instanceof ViewGroup) {
 			//If down
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 				//Get position
 				float X = event.getRawX();
 				float Y = event.getRawY();
 				
 				//If outside, consumed
 				m_TouchedOutside = !isInsideView(X, Y, (ViewGroup)v);
 				if (m_TouchedOutside) Consume = true;
 			} else if (event.getAction() == MotionEvent.ACTION_UP) {
 				//If touched outside
 				if (m_TouchedOutside) {
 					//Consume
 					Consume = true;
 
 					//If outside, finish
 					float X = event.getRawX();
 					float Y = event.getRawY();
 					if (!isInsideView(X, Y, (ViewGroup)v)) finish();
 				}
 				
 				//Not touching outside
 				m_TouchedOutside = false;
 			}
 		}
 		
 		//Return
 		return Consume;
 	}
 
 	protected boolean isInsideView(float x, float y, ViewGroup parent) {
 		//Initialize
		boolean Inside = true;
 
 		//While not consuming and not all children
 		int i 	= 0;
		while (i < parent.getChildCount() && Inside) {
 			//Get child
 			View Child = parent.getChildAt(i);
 			if (Child != null) {
 				//Get position
 				int[] Position	= new int[] { 0, 0 };
 				Child.getLocationInWindow(Position);
 
 				//If inside view, do not consume
				if (x >= Position[0] && y >= Position[1] && x <= Position[0] + Child.getWidth() && y <= Position[1] + Child.getHeight()) Inside = false;
 			}
 
 			//Next
 			i++;
 		}
 
 		//Return
 		return Inside;
 	}
 
 	protected void addUser(String user) {
 		//Skip if no user
 		if (user == null) return;
 
 		//Get base layout
 		View Root = findViewById(R.id.layout_game);
 		if (Root != null && Root instanceof RelativeLayout) {
 			//Get user
 			Player Data = FriendManager.instance().getFriend(user);
 			if (Data != null) {
 				//Initialize margin
 				float MarginGap     = 0;
 				float MarginOffset  = 0;
 				if (getResources() != null && getResources().getDisplayMetrics() != null) {
 					//Calculate
 					MarginGap     = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
 					MarginOffset  = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
 				}
 
 				//Create game
 				GameManager.instance().getGame(user);
 
 				//Create head
 				ImageView Head = new ImageView(this);
 				Head.setImageResource(Data.getResourceID());
 				Head.setId(GameActivity.generateViewID());
 				Head.setOnClickListener(this);
 
 				//Create parameters
 				int Wrap = RelativeLayout.LayoutParams.WRAP_CONTENT;
 				RelativeLayout.LayoutParams Parameters = new RelativeLayout.LayoutParams(Wrap, Wrap);
 				Parameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
 				Parameters.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
 				Parameters.setMargins(0, 0, (int) MarginOffset, 0);
 				((RelativeLayout)Root).addView(Head, Parameters);
 
 				//Get left view
 				View Left = findViewById(R.id.image_friends);
 				if (!m_Opponents.isEmpty()) Left = m_HeadsTable.get(m_Opponents.get(m_Opponents.size() - 1));
 
 				//Save
 				m_Opponents.add(user);
 				m_UsersTable.put(Head, user);
 				m_HeadsTable.put(user, Head);
 
 				//If there's a left view
 				if (Left != null) {
 					//Get params
 					Parameters = (RelativeLayout.LayoutParams)Left.getLayoutParams();
 					if (Parameters != null) {
 						//Configure
 						Parameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
 						Parameters.addRule(RelativeLayout.LEFT_OF, Head.getId());
 						Parameters.setMargins(0, 0, (int)MarginGap, 0);
 					}
 				}
 			}
 		}
 	}
 
 	protected void removeUser(String user) {
 		//Skip if not valid
 		if (user == null) return;
 
 		//Get head and root
 		View Head = m_HeadsTable.get(user);
 		View Root = findViewById(R.id.layout_game);
 		if (Head != null && Root != null && Root instanceof ViewGroup) {
 			//Calculate margin
 			float Margin = 0;
 			if (getResources() != null && getResources().getDisplayMetrics() != null)Margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
 
 			//Get index
 			int Index = -1;
 			for (int i = 0; i < m_Opponents.size() && Index < 0; i++) if (m_Opponents.get(i).equals(user)) Index = i;
 
 			//Remove
 			m_Opponents.remove(user);
 			m_HeadsTable.remove(user);
 			m_UsersTable.remove(Head);
 			((ViewGroup) Root).removeView(Head);
 			FriendManager.instance().removeOpponent(user);
 			if (m_ActiveUser.equals(user)) m_ActiveUser = null;
 
 			//Get right view
 			View Right = null;
 			if (Index < m_Opponents.size()) Right = m_HeadsTable.get(m_Opponents.get(Index));
 			if (Right != null) {
 				if (m_ActiveUser == null) {
 					//Get user and set active
 					String NewUser = m_UsersTable.get(Right);
 					if (NewUser != null) setActiveUser(NewUser);
 				}
 
 				//Get left
 				View Left = findViewById(R.id.image_friends);
 				if (Index > 0) Left = m_HeadsTable.get(m_Opponents.get(Index - 1));
 				if (Left != null) {
 					//Configure params
 					RelativeLayout.LayoutParams Parameters = (RelativeLayout.LayoutParams)Left.getLayoutParams();
 					if (Parameters != null) Parameters.addRule(RelativeLayout.LEFT_OF, Right.getId());
 				}
 			} else {
 				//Find the correct right
 				if (m_Opponents.isEmpty())  Right = findViewById(R.id.image_friends);
 				else                        Right = m_HeadsTable.get(m_Opponents.get(Index - 1));
 
 				//If exist
 				if (Right != null) {
 					if (m_ActiveUser == null) {
 						//Get user and set active
 						String NewUser = m_UsersTable.get(Right);
 						if (NewUser != null) setActiveUser(NewUser);
 					}
 
 					//Get params
 					RelativeLayout.LayoutParams Parameters = (RelativeLayout.LayoutParams)Right.getLayoutParams();
 					if (Parameters != null) {
 						//Configure
 						Parameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
 						Parameters.addRule(RelativeLayout.LEFT_OF, 0);
 						Parameters.setMargins(0, 0, (int)Margin, 0);
 					}
 				}
 			}
 		}
 	}
 	
 	protected void setActiveUser(String user) {
 		//Save
 		m_ActiveUser = user;
 		FriendManager.instance().setActiveOpponent(user);
 
 		//Get user name
 		String Name 	= "Nobody";
 		Player Opponent = FriendManager.instance().getFriend(user);
 		if (Opponent != null) Name = Opponent.getName();
 
 		//Set title
 		View LabelTitle = findViewById(R.id.label_title);
 		if (LabelTitle != null && LabelTitle instanceof TextView) ((TextView)LabelTitle).setText(Name);
 
 		//Get game
 		Tictactoe Game = GameManager.instance().getGame(m_ActiveUser);
 		refreshDisplay(Game);
 	}
 
 	protected int getCellColumn(View cell) {
 		//Initialize
 		int Column = -1;
 
 		//For each cell
 		for (int x = 0; x < m_Board.length && Column == -1; x++) {
 			for (int y = 0; y < m_Board.length && Column == -1; y++) {
 				//If same, get column
 				if (m_Board[x][y] == cell) Column = x;
 			}
 		}
 
 		//Return
 		return Column;
 	}
 
 	protected int getCellRow(View cell) {
 		//Initialize
 		int Row = -1;
 
 		//For each cell
 		for (int x = 0; x < m_Board.length && Row == -1; x++) {
 			for (int y = 0; y < m_Board.length && Row == -1; y++) {
 				//If same, get row
 				if (m_Board[x][y] == cell) Row = y;
 			}
 		}
 
 		//Return
 		return Row;
 	}
 	
 	protected void refreshDisplay(Tictactoe game) {
 		//Get views
 		View BoardLayout    = findViewById(R.id.layout_board);
 		View ResultLayout   = findViewById(R.id.layout_result);
 		View LoadingLayout   = findViewById(R.id.layout_loading);
 
 		//if no game
 		if (game == null) {
 			//Show loading
 			if (BoardLayout != null)    BoardLayout.setVisibility(View.GONE);
 			if (ResultLayout != null)   ResultLayout.setVisibility(View.GONE);
 			if (LoadingLayout != null)  LoadingLayout.setVisibility(View.VISIBLE);
 		} else {
 			//Process result
 			int Result 			= game.getResult();
 			if (BoardLayout != null)    BoardLayout.setVisibility(Result == Tictactoe.RESULT_INVALID ? View.VISIBLE : View.GONE);
 			if (ResultLayout != null)   ResultLayout.setVisibility(Result == Tictactoe.RESULT_INVALID ? View.GONE : View.VISIBLE);
 			if (LoadingLayout != null)  LoadingLayout.setVisibility(View.GONE);
 
 			//If invalid
 			if (Result == Tictactoe.RESULT_INVALID) {
 				//Redraw board
 				drawBoard(game);
 
 				//Get turn text
 				View TurnLabel = findViewById(R.id.label_turn);
 				if (TurnLabel != null && TurnLabel instanceof TextView) {
 					//Set text
 					((TextView) TurnLabel).setText(game.isMyTurn() ? R.string.game_turn_self : R.string.game_turn_enemy);
 					if (GameManager.instance().isQueueing(m_ActiveUser)) ((TextView) TurnLabel).setText(R.string.game_turn_self);
 				}
 
 				//If not updating
 				View StatusLabel    = findViewById(R.id.label_status);
 				View Progress       = findViewById(R.id.progress_updating);
 				if (!GameManager.instance().isQueueing(m_ActiveUser) && !GameManager.instance().isLoading(m_ActiveUser)) {
 					//Hide
 					if (Progress != null)       Progress.setVisibility(View.INVISIBLE);
 					if (StatusLabel != null)    StatusLabel.setVisibility(View.INVISIBLE);
 				} else {
 					//Show
 					if (Progress != null) Progress.setVisibility(View.VISIBLE);
 					if (StatusLabel != null) {
 						//Set text
 						StatusLabel.setVisibility(View.VISIBLE);
 						if (StatusLabel instanceof  TextView) ((TextView)StatusLabel).setText(GameManager.instance().isQueueing(m_ActiveUser) ? R.string.game_sending : R.string.game_updating);
 					}
 				}
 			} else {
 				//Set text
 				int TextID = -1;
 				switch (Result) {
 					case Tictactoe.RESULT_WIN:
 						TextID = R.string.game_win;
 						break;
 
 					case Tictactoe.RESULT_LOSE:
 						TextID = R.string.game_lose;
 						break;
 
 					case Tictactoe.RESULT_DRAW:
 						TextID = R.string.game_draw;
 						break;
 				}
 
 				//Set result
 				View ResultLabel = findViewById(R.id.label_result);
 				if (ResultLabel != null && ResultLabel instanceof TextView) ((TextView) ResultLabel).setText(TextID);
 			}
 		}
 	}
 
 	protected void drawBoard(Tictactoe game) {
 		//Get resources
 		Resources Resource = getResources();
 		if (Resource == null) 	return;
 		if (game == null)		return;
 
 		//Get game status
 		int[][] Status = game.getStatus();
 		for (int x = 0; x < m_Board.length; x++) {
 			for (int y = 0; y < m_Board.length; y++) {
 				//Check status
				int ColorID = android.R.color.transparent;
				if (Status[x][y] == Tictactoe.SELF_CELL) 		ColorID = android.R.color.holo_green_light;
				else if (Status[x][y] == Tictactoe.ENEMY_CELL) 	ColorID = android.R.color.holo_red_light;
 
 				//Set color
				if (m_Board[x][y] != null) m_Board[x][y].setBackgroundColor(Resource.getColor(ColorID));
 			}
 		}
 	}
 
 	protected BroadcastReceiver m_GameReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			//Skip if no intent
 			if (intent == null)         return;
 			if (m_ActiveUser == null)   return;
 
 			//Get user
 			String Opponent = intent.getStringExtra(RoomService.EXTRA_OPPONENT);
 			if (Opponent == null) {
 				//Get more users
 				List<String> Challenges = intent.getStringArrayListExtra(RoomsService.EXTRA_CHALLENGES);
 				if (Challenges != null && Challenges.size() > 0) {
 					//Add challenges
 					for (int i = 0; i < Challenges.size(); i++) addUser(Challenges.get(i));
 
 					//Set user
 					Opponent = Challenges.get(0);
 					setActiveUser(Opponent);
 				}
 
 				//Get old opponents
 				List<String>  Opponents  = intent.getStringArrayListExtra(RoomsService.EXTRA_OPPONENTS);
 				if (Opponents != null && Opponent == null) {
 					//FInd active user
 					for (int i = 0; i < Opponents.size() && Opponent == null; i++)
 						if (Opponents.get(i).equals(m_ActiveUser)) Opponent = Opponents.get(i);
 				}
 			}
 
 			if (Opponent != null && Opponent.equals(m_ActiveUser)) {
 				//Get game and display it
 				Tictactoe Game = GameManager.instance().getGame(Opponent);
 				refreshDisplay(Game);
 
 				//Hide head
 				Intent HeadIntent = new Intent(context, HeadService.class);
 				HeadIntent.putExtra(HeadService.EXTRA_SHOW, false);
 				startService(HeadIntent);
 			}
 		}
 	};
 
 	protected static int generateViewID() {
 		for (;;) {
 			final int result = s_NextGeneratedId.get();
 			// aapt-generated IDs have the high byte nonzero; clamp to the range under that.
 			int newValue = result + 1;
 			if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
 			if (s_NextGeneratedId.compareAndSet(result, newValue)) {
 				return result;
 			}
 		}
 	}
 
 	//Static
 	protected static final AtomicInteger s_NextGeneratedId = new AtomicInteger(1);
 
 	//Constants
 	public static final String GAME_CHANGED = "net.ark.tictachead.game";
 
 	//Data
 	protected View[][]					m_Board;
 	protected List<String>   			m_Opponents;
 	protected Hashtable<String, View>	m_HeadsTable;
 	protected Hashtable<View, String>	m_UsersTable;
 	protected boolean          			m_TouchedOutside;
 	protected String					m_ActiveUser;
 }
