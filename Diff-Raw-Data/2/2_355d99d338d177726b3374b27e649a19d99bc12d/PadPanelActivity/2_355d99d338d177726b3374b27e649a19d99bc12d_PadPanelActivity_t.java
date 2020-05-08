 package net.thiagoalz.hermeto;
 
 import java.util.List;
 import java.util.Map;
 
 import net.thiagoalz.hermeto.audio.SoundManager;
 import net.thiagoalz.hermeto.panel.ExecutionEvent;
 import net.thiagoalz.hermeto.panel.ExecutionListener;
 import net.thiagoalz.hermeto.panel.GameManager;
 import net.thiagoalz.hermeto.panel.Position;
 import net.thiagoalz.hermeto.panel.listeners.ConnectEvent;
 import net.thiagoalz.hermeto.panel.listeners.MoveEvent;
 import net.thiagoalz.hermeto.panel.listeners.PlayerListener;
 import net.thiagoalz.hermeto.panel.listeners.SelectionEvent;
 import net.thiagoalz.hermeto.panel.listeners.SelectionListener;
 import net.thiagoalz.hermeto.player.Player;
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class PadPanelActivity extends Activity implements SelectionListener, PlayerListener, ExecutionListener {
 	
 	private static final String tag = PadPanelActivity.class.getCanonicalName();
 	
 	private GameManager gameManager;
 	private SoundManager soundManager;
 	
 	private ImageButton[][] padsMatrix;
 	private TextView[] playerNamesPosition; 
 	private TableLayout tableLayout;
 	
 	Player defaultPlayer;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		configureScreen();
 		gameManager = GameManager.getInstance();
 		gameManager.addSelectionListener(this);
 		gameManager.addExecutionListener(this);
 		defaultPlayer = gameManager.connectPlayer();
 		constructView();
 	}
 	
 	private void configureScreen() {
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
 	
 	private void constructView() {
 		setContentView(R.layout.padpanel);
 		tableLayout = (TableLayout) findViewById(R.id.padpanelgrid);
 		initializeSquarePanel();
 		initializePlayersName();
 		initializeControls();
 	}
 	
 	private void initializeSquarePanel() {
 		padsMatrix = new ImageButton[gameManager.getColumns()][gameManager.getRows()];
 		for (int i = 0; i < padsMatrix.length; i++) {
 			TableRow tableRow = new TableRow(this);
 			for (int j = 0; j < padsMatrix[i].length; j++) {
 				ImageButton button = new ImageButton(this);
 				TableRow.LayoutParams params = new TableRow.LayoutParams(42, 42);
 				params.gravity = Gravity.CENTER;
 				button.setLayoutParams(params);
 				button.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.buttonstopped));
 				button.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						ImageButton selectedButton = (ImageButton) v;
 						for (int i = 0; i < padsMatrix.length; i++) {
 							for (int j = 0; j < padsMatrix[i].length; j++) {
 								if (padsMatrix[i][j] == selectedButton) {
 									defaultPlayer.setPosition(new Position(i, j));
 									gameManager.mark(defaultPlayer);
 								}
 							}
 						}
 						
 					}
 				});
 				padsMatrix[i][j] = button;
 				tableRow.addView(padsMatrix[i][j]);
 			}
 			tableLayout.addView(tableRow);
 		}
 	}
 	
 	private void initializePlayersName() {
 		Map<String, Player> players = gameManager.getPlayers();
		//FrameLayout namesLayout = (FrameLayout) findViewById(R.id.namesLayout);
 		
 		for (String playerID : players.keySet()) {
 			Player player = players.get(playerID);
 			TextView nameBox = new TextView(this);
 			nameBox.setText(player.getName());
 			nameBox.setPadding(12, 12, 12, 12);
 			nameBox.setTextColor(Color.WHITE);
 			//RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(60, 20);
 		}
 		
 		
 	}
 	
 	private void initializeControls() {
 		final ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (gameManager.isPlaying()) {
 					gameManager.pause();
 				} else {
 					gameManager.start();
 				}
 			}
 		});
 		
 		ImageButton stop = (ImageButton) findViewById(R.id.stop);
 		stop.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				gameManager.stop();
 			}
 		});
 		
 		
 		ImageButton reset = (ImageButton) findViewById(R.id.reset);
 		reset.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				gameManager.reset();
 			}
 		});
 		
 	}
 
 	@Override
 	public void onSelected(SelectionEvent event) {
 		int x = event.getPosition().getX();
 		int y = event.getPosition().getY();
 		padsMatrix[x][y].setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonselected));
 	}
 
 	@Override
 	public void onDeselected(SelectionEvent event) {
 		int x = event.getPosition().getX();
 		int y = event.getPosition().getY();
 		padsMatrix[x][y].setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonstopped));
 	}
 
 	@Override
 	public void onPlayerMove(MoveEvent event) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPlayerConnect(ConnectEvent event) {
 		// TODO Auto-generated method stub
 		
 		
 		
 	}
 
 	@Override
 	public void onStart(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.stop));
 		
 	}
 
 	@Override
 	public void onStop(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.panel_play_button));
 	}
 
 	@Override
 	public void onReset(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.panel_play_button));
 	}
 
 	@Override
 	public void onPause(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.panel_play_button));
 	}
 	
 	public void onStartPlayingGroup(final ExecutionEvent event) {
 		Log.d(tag, "Printing selected buttons with playing color ");
 		runOnUiThread(new Runnable() {
 			public void run() {
 				List<Position> positions = event.getPositions();
 				for (Position position : positions) {
 					ImageButton button = padsMatrix[position.getX()][position.getY()];
 					button.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.buttonplaying));
 				}
 			}
 		});
 		
 	}
 	
 	public void onStopPlayingGroup(final ExecutionEvent event) {
 		Log.d(tag, "Printing selected buttons with selected color ");
 		runOnUiThread(new Runnable() {
 			public void run() {
 				List<Position> positions = event.getPositions();
 				for (Position position : positions) {
 					ImageButton button = padsMatrix[position.getX()][position.getY()];
 					button.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.buttonselected));
 				}
 			}
 		});
 		
 	}
 }
