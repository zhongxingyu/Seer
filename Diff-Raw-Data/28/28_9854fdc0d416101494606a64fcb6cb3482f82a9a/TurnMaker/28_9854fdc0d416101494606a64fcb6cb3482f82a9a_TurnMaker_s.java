 package uk.co.thomasc.wordmaster.util;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 import uk.co.thomasc.wordmaster.R;
 import uk.co.thomasc.wordmaster.api.GetTurnsRequestListener;
 import uk.co.thomasc.wordmaster.api.ServerAPI;
 import uk.co.thomasc.wordmaster.api.TakeTurnRequestListener;
 import uk.co.thomasc.wordmaster.api.TakeTurnSpinnerListener;
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.Turn;

 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 
 public class TurnMaker implements OnClickListener, TakeTurnRequestListener, GetTurnsRequestListener {
 
 	private Game game;
 	private BaseGame activity;
 	private EditText input;
 	private TakeTurnSpinnerListener listener;
 	
 	public TurnMaker(Game game, BaseGame activity, View rootView, TakeTurnSpinnerListener listener) {
 		this.game = game;
 		this.activity = activity;
 		this.listener = listener;
		input = (EditText) rootView.findViewById(R.id.editText1);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (game.isPlayersTurn()) {
 			String guess = input.getText().toString();
 			if (guess.length() == 4) {
 				listener.startSpinner();
 				ServerAPI.takeTurn(game.getPlayer().getPlusID(), game.getID(), guess, this);
 			}
 		} else {
			// TODO: Should probably tell user off for trying to cheat
			System.out.println("It's not your turn!");
 		}
 	}
 
 	@Override
 	public void onRequestComplete(boolean[] result) {
 		boolean success = result[0];
 		boolean validWord = result[1];
 		
 		if (success) {
 			int pivot = game.getPivotLatest();
 			if (pivot > 0) {
 				ServerAPI.getTurns(game.getID(), pivot, 1, activity, this);
 			} else {
 				ServerAPI.getTurns(game.getID(), activity, this);
 			}
 		} else {
 			if (validWord) {
 				listener.stopSpinner();
 				// TODO: Tell the user seal bars aren't tasty
 			} else {
 				listener.stopSpinner();
 				System.out.println("That's not a word.");
 				// TODO: Tell the user they need a new dictionary
 			}
 		}
 	}
 
 	@Override
 	public void onRequestComplete(Turn[] turns) {
 		for (Turn turn : turns) {
 			game.addTurn(turn);
 		}
 		game.setPlayersTurn(false);
 		activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				activity.menuFragment.adapter.notifyDataSetChanged();
 				input.setText("");
 			}
 		});
 		listener.stopSpinner();
 	}
 
 	@Override
 	public void onRequestFailed() {
 		listener.stopSpinner();
 		// TODO: Tell the user their soul was sold to satan
 	}
 
 }
