 package se.chalmers.chessfeud;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import se.chalmers.chessfeud.model.ChessModel;
 import se.chalmers.chessfeud.model.utils.Position;
 import se.chalmers.chessfeud.utils.C;
 import se.chalmers.chessfeud.utils.DbHandler;
 import se.chalmers.chessfeud.utils.Game;
 import se.chalmers.chessfeud.utils.TimeStamp;
 import se.chalmers.chessfeud.view.GameView;
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Is a class that will update all data that is shown in Activity Play.
  * 
  * @author Sean Pavlov
  * 
  *         Copyright (c) Sean Pavlov 2012
  * 
  */
 public class PlayActivity extends Activity implements PropertyChangeListener {
 	private Game g;
 	private ChessModel cm;
 	private TimeStamp ts;
 	private TextView nbrOfTurns;
 	private TextView playerNameWhite;
 	private TextView playerNameBlack;
 	private TextView statusTxt;
 	private TextView whoseTurnNTime;
 	private GameView gv;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play);
 		setLayout();
 		gv = (GameView) findViewById(R.id.chessBoard);
 		String gameInfo = getIntent().getStringExtra("GameString");
 		int position = getIntent().getIntExtra("Position", -1);
 		if (gameInfo != null) {
 			g = new Game(gameInfo, position);
 			cm = new ChessModel(g, this);
 			gv.setGameModel(cm);
 			ts = new TimeStamp(g.getTimestamp());
 		} else {
 			// This is a dummygame
 			g = new Game("NA/NA/NA/0/NA", 0);
 			cm = new ChessModel(this);
 			gv.setGameModel(cm);
 			ts = new TimeStamp();
 		}
 
 		nbrOfTurns.setText("" + g.getTurns());
 		playerNameWhite.setText("" + g.getWhitePlayer());
 		playerNameBlack.setText("" + g.getBlackPlayer());
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		nbrOfTurns.setText("" + g.getTurns());
 		setTurnNTime();
 		setState();
 	}
 
 	/**
 	 * Sets all the layouts that is in use in PlayActivity.
 	 */
 	private void setLayout() {
 		nbrOfTurns = (TextView) findViewById(R.id.nbrOfTurns);
 		playerNameWhite = (TextView) findViewById(R.id.playerNameWhite);
 		playerNameBlack = (TextView) findViewById(R.id.playerNameBlack);
 		statusTxt = (TextView) findViewById(R.id.statusTxt);
 		whoseTurnNTime = (TextView) findViewById(R.id.whoseTurnAndTime);
 	}
 
 	/**
 	 * Set the state of the game; Check, Win, Loss or Draw.
 	 */
 	private void setState() {
 		switch (cm.getState()) {
 		case C.STATE_NORMAL:
 			statusTxt.setText("");
 			break;
 		case C.STATE_CHECK:
 			statusTxt.setTextColor(C.STATE_TEXT_COLOR_RED);
 			statusTxt.setText("Check");
 			break;
 		case C.STATE_DRAW:
 			statusTxt.setTextColor(C.STATE_TEXT_COLOR_GREY);
 			statusTxt.setText("Draw");
 			Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
 			break;
 		case C.STATE_VICTORY_WHITE:
 			if (g.thisPlayersTeam() == C.TEAM_WHITE) {
 				statusTxt.setTextColor(C.STATE_TEXT_COLOR_GREEN);
 				statusTxt.setText("Winner");
 				Toast.makeText(this, "Congratulations! You won the game!",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				statusTxt.setTextColor(C.STATE_TEXT_COLOR_RED);
 				statusTxt.setText("Loser");
 				Toast.makeText(this, "Ouch! You lost the game!",
 						Toast.LENGTH_SHORT).show();
 			}
 			break;
 		case C.STATE_VICTORY_BLACK:
 			if (g.thisPlayersTeam() == C.TEAM_BLACK) {
 				statusTxt.setTextColor(C.STATE_TEXT_COLOR_GREEN);
 				statusTxt.setText("Winner");
 				Toast.makeText(this, "Congratulations! You won the game!",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				statusTxt.setTextColor(C.STATE_TEXT_COLOR_RED);
 				statusTxt.setText("Loser");
 				Toast.makeText(this, "Ouch! You lost the game!",
 						Toast.LENGTH_SHORT).show();
 			}
 			break;
 		default:
 			statusTxt.setText("");
 			break;
 		}
 	}
 
 	/**
 	 * Sets the TextView that show whose turn it is and how long it has been
 	 * since it was the players turn.
 	 */
 	private void setTurnNTime() {
 		String s = "";
 		if (g.getCurrentColor() == C.TEAM_WHITE) {
 			s += "White";
 		} else {
 			s += "Black";
 		}
 		s += "'s Turn(";
 		if (ts.getMinutesSinceStamp() < C.MINUTES_PER_HOUR) {
 			s += "" + ts.getMinutesSinceStamp() + "m";
 		} else {
 			s += "" + (int) ts.getMinutesSinceStamp() / C.MINUTES_PER_HOUR
 					+ "h";
 		}
 		s += ")";
 		whoseTurnNTime.setText(s);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
 	 * PropertyChangeEvent)
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equals("Model")) {
 			final Game gameInfo = (Game) event.getOldValue();
 			final String gameBoard = (String) event.getNewValue();
 			new Thread() {
 				public void run() {
 					DbHandler.getInstance().newMove(gameInfo.getOpponent(),
 							gameBoard);
 				}
 			}.start();
 		} else if (event.getPropertyName().equals("Pawn")) {
 			Position p = (Position) event.getOldValue();
 			promptNewPiece(p);
 
 		}
		setState();
		setTurnNTime();
 	}
 
 	/* Creates a popup list which has the different pieces as alternatives */
 	private void promptNewPiece(final Position p) {
 		final String[] pieces = { "Queen", "Rook", "Bishop", "Knight" };
 		PlayActivity.this.runOnUiThread(new Runnable() {
 			public void run() {
 				Builder builder = new Builder(PlayActivity.this);
 				builder.setTitle(R.string.choose_piece);
 				builder.setItems(pieces, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int position) {
 						switch (position) {
 						case 0:
 							setNewPiece(p, C.PIECE_QUEEN);
 							break;
 						case 1:
 							setNewPiece(p, C.PIECE_ROOK);
 							break;
 						case 2:
 							setNewPiece(p, C.PIECE_BISHOP);
 							break;
 						case 3:
 							setNewPiece(p, C.PIECE_KNIGHT);
 							break;
 						default:
 							setNewPiece(p, C.PIECE_QUEEN);
 						}
 					}
 				});
 				builder.setOnCancelListener(new OnCancelListener() {
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						setNewPiece(p, C.PIECE_QUEEN);
 					}
 				});
 				builder.show();
 			}
 		});
 	}
 
 	private void setNewPiece(Position p, int id) {
 		cm.changePawnTo(p, id);
 		gv.invalidate();
 	}
 }
