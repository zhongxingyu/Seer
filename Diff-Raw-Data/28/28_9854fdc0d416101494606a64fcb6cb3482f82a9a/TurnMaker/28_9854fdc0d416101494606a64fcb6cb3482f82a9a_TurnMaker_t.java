 package uk.co.thomasc.wordmaster.util;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 import uk.co.thomasc.wordmaster.R;
 import uk.co.thomasc.wordmaster.api.GetTurnsRequestListener;
 import uk.co.thomasc.wordmaster.api.ServerAPI;
 import uk.co.thomasc.wordmaster.api.TakeTurnRequestListener;
 import uk.co.thomasc.wordmaster.api.TakeTurnSpinnerListener;
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.Turn;
import uk.co.thomasc.wordmaster.view.DialogPanel;
import uk.co.thomasc.wordmaster.view.Errors;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 
 public class TurnMaker implements OnClickListener, TakeTurnRequestListener, GetTurnsRequestListener {
 
 	private Game game;
 	private BaseGame activity;
 	private EditText input;
	private DialogPanel errorMessage;
 	private TakeTurnSpinnerListener listener;
 	
 	public TurnMaker(Game game, BaseGame activity, View rootView, TakeTurnSpinnerListener listener) {
 		this.game = game;
 		this.activity = activity;
 		this.listener = listener;
		input = (EditText) rootView.findViewById(R.id.guess_input);
		errorMessage = (DialogPanel) rootView.findViewById(R.id.errorMessage);
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
			errorMessage.show(Errors.TURN);
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
