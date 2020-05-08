 package com.opensource.tic.tac.toe.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import com.opensource.tic.tac.toe.game.R;
 
 /**
  * @author abhinav
  * 
  */
 public class TicTacToe extends Activity {
 
 	private GameEngine game = new GameEngine();
 
 	private ScoreCard scoreCard = new ScoreCard();
 
 	private List<Button> buttonList = new ArrayList<Button>();
 
 	private boolean computerPlaysFirst = true;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		prepare();
 	}
 
 	private void prepare() {
 		cleanDisplay();
 		game.resetBoard();
 		resetButtons();
 		setListeners();
 		if (computerPlaysFirst) {
 			moveComputer();
 			display();
 		}
 		computerPlaysFirst = !computerPlaysFirst;
 	}
 
 	private void cleanDisplay() {
 		// result text
 		TextView resultView = (TextView) findViewById(R.id.resultText);
 		resultView.setText("Player to move");
 		resultView.setBackgroundColor(Color.BLACK);
 		resultView.setTextColor(Color.WHITE);
 		resultView.setTextScaleX(1);
 		resultView.setClickable(false);
 		resultView.setEnabled(false);
 		resultView.setCursorVisible(false);
 
 		// player win count
 		TextView playerWinCountView = (TextView) findViewById(R.id.playerResultCountText);
 		playerWinCountView.setText("Player wins:	  "
 				+ scoreCard.getPlayerWinCount());
 		playerWinCountView.setBackgroundColor(Color.BLUE);
 		playerWinCountView.setTextColor(Color.WHITE);
 		playerWinCountView.setTextScaleX(1);
 		playerWinCountView.setClickable(false);
 		playerWinCountView.setEnabled(false);
 		playerWinCountView.setCursorVisible(false);
 
 		// computer win count
 		TextView computerWinCountView = (TextView) findViewById(R.id.computerResultCountText);
 		computerWinCountView.setText("Computer wins:	"
 				+ scoreCard.getComputerWinCount());
 		computerWinCountView.setBackgroundColor(Color.RED);
 		computerWinCountView.setTextColor(Color.WHITE);
 		computerWinCountView.setTextScaleX(1);
 		computerWinCountView.setClickable(false);
 		computerWinCountView.setEnabled(false);
 		computerWinCountView.setCursorVisible(false);
 
 		// draw count
 		TextView drawCountView = (TextView) findViewById(R.id.drawResultCountText);
 		drawCountView.setText("Draw:	         " + scoreCard.getDrawCount());
 		drawCountView.setBackgroundColor(Color.DKGRAY);
 		drawCountView.setTextColor(Color.WHITE);
 		drawCountView.setTextScaleX(1);
 		drawCountView.setClickable(false);
 		drawCountView.setEnabled(false);
 		drawCountView.setCursorVisible(false);
 
 	}
 
 	private void resetButtons() {
 		buttonList.clear();
 		buttonList.add((Button) findViewById(R.id.button0));
 		buttonList.add((Button) findViewById(R.id.button1));
 		buttonList.add((Button) findViewById(R.id.button2));
 		buttonList.add((Button) findViewById(R.id.button3));
 		buttonList.add((Button) findViewById(R.id.button4));
 		buttonList.add((Button) findViewById(R.id.button5));
 		buttonList.add((Button) findViewById(R.id.button6));
 		buttonList.add((Button) findViewById(R.id.button7));
 		buttonList.add((Button) findViewById(R.id.button8));
 	}
 
 	private void setListeners() {
 		// playing button listeners
 		for (final Button button : buttonList) {
 			button.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 
 					movePlayer(button);
 
 					display();
 
 					moveComputer();
 
 					display();
 				}
 			});
 		}
 		// reset button listener
 		Button resetButton = (Button) findViewById(R.id.resetGame);
 		resetButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				setContentView(R.layout.main);
 				prepare();
 			}
 		});
 
 	}
 
 	private void movePlayer(Button button) {
 		if (!game.isGameOver()) {
 			game.movePlayer(getPosition(button));
 			button.setText("O");
 			button.setBackgroundColor(Color.BLUE);
			button.setEnabled(false);
 		}
 	}
 
 	private void moveComputer() {
 		if (!game.isGameOver()) {
 			int moveIndex = game.moveComputer();
 			if (GameEngine.COMPUTER_CANT_MOVE != moveIndex) {
 				buttonList.get(moveIndex).setText("X");
 				buttonList.get(moveIndex).setBackgroundColor(Color.RED);
				buttonList.get(moveIndex).setEnabled(false);
 			}
 		}
 	}
 
 	private int getPosition(Button button) {
 		int position = 0;
 		for (Button b : buttonList) {
 			if (button.getId() == b.getId()) {
 				return position;
 			}
 			position++;
 		}
 		return -1;
 	}
 
 	private void disableAllPlayingButtons() {
 		for (Button b : buttonList) {
 			b.setEnabled(false);
 		}
 	}
 
 	private TextView getPlayerWinCountTextView() {
 		return (TextView) findViewById(R.id.playerResultCountText);
 	}
 
 	private TextView getComputerWinCountTextView() {
 		return (TextView) findViewById(R.id.computerResultCountText);
 	}
 
 	private TextView getDrawCountTextView() {
 		return (TextView) findViewById(R.id.drawResultCountText);
 	}
 
 	private void display() {
 		TextView resultView = (TextView) findViewById(R.id.resultText);
 		if (game.isGameDraw()) {
 			scoreCard.registerDraw();
 			getDrawCountTextView().setText(
 					"Draw:	         " + scoreCard.getDrawCount());
 			resultView.setText("Game draw");
 		} else if (game.hasPlayerWon()) {
 			scoreCard.registerPlayerWin();
 			getPlayerWinCountTextView().setText(
 					"Player wins:	  " + scoreCard.getPlayerWinCount());
 			resultView.setText("Player won");
 		} else if (game.hasComputerWon()) {
 			scoreCard.registerComputerWin();
 			getComputerWinCountTextView().setText(
 					"Computer wins:	" + scoreCard.getComputerWinCount());
 			resultView.setText("Computer won");
 		}
 
 		if (game.isGameOver()) {
 			disableAllPlayingButtons();
 			resultView.setBackgroundColor(Color.BLACK);
 		}
 	}
 
 }
