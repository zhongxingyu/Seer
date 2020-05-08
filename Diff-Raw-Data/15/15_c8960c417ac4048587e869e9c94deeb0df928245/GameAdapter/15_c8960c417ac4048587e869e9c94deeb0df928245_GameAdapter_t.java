 package uk.co.thomasc.wordmaster.view.game;
 
 import java.util.Comparator;
 import java.util.Locale;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 import uk.co.thomasc.wordmaster.R;
 import uk.co.thomasc.wordmaster.objects.Turn;
 import uk.co.thomasc.wordmaster.objects.User;
 import uk.co.thomasc.wordmaster.objects.callbacks.NameLoadedListener;
 import uk.co.thomasc.wordmaster.view.TimeSinceText;
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GameAdapter extends ArrayAdapter<Turn> {
 
 	private Activity act;
 	final private Comparator<Turn> comp;
 	
 	public GameAdapter(Activity act) {
 		super(act, 0);
 
 		this.act = act;
 
 		comp = new Comparator<Turn>() {
 			@Override
 			public int compare(Turn e1, Turn e2) {
 				return (int) (e1.getUnixTimestamp() - e2.getUnixTimestamp());
 			}
 		};
 	}
 
 	@Override
 	public void add(Turn object) {
 		super.add(object);
 		sort(comp);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		
 		final Turn item = getItem(position);
 		User user = item.getUser();
 		boolean isPlayer = (user.getPlusID().equals(((BaseGame) act).getUserId()));
 		if (!isPlayer && item.getTurnNum() == 0) {
 			return new View(getContext());
 		}
 		final boolean winningTurn = (item.getCorrectLetters() == 4);
 		int viewId = item.getTurnNum() == 0 ? R.layout.game_row_new_round : (isPlayer ? (winningTurn ? R.layout.game_row_win : R.layout.game_row_big) : (winningTurn ? R.layout.game_row_lose : R.layout.game_row_small));
 		
 		if (view == null || view.getId() != viewId) {
 			LayoutInflater vi = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = vi.inflate(viewId, null);
 		}
 		
 		if (item.getTurnNum() == 0) {
 			((TextView) view.findViewById(R.id.guess)).setText("Your word is " + item.getGuess());
			view.setBackgroundResource(R.drawable.wordbg);
 		} else {
 			if (isPlayer) {
 				((TextView) view.findViewById(R.id.guess)).setText(item.getGuess().toUpperCase(Locale.ENGLISH));
 			} else {
 				final TextView txtview = (TextView) view.findViewById(R.id.guess);
 				user.listenForLoad(new NameLoadedListener() {
 					@Override
 					public void onNameLoaded(String name) {
 						String firstName = name.substring(0, name.indexOf(' '));
 						if (winningTurn) {
 							txtview.setText(firstName + " guessed " + item.getGuess().toUpperCase(Locale.ENGLISH) +
 									"\n" + firstName + "'s word was " + item.getOpponentWord().toUpperCase(Locale.ENGLISH));
 						} else {
 							txtview.setText(firstName + " guessed " + item.getGuess().toUpperCase(Locale.ENGLISH));
 						}
 					}
 				});
 			}
 			
 			((TimeSinceText) view.findViewById(R.id.time)).setTimestamp(item.getUnixTimestamp());
 			
 			int goldPegs = item.getCorrectLetters();
 			int silverPegs = item.getDisplacedLetters();
 			
 			Resources res = act.getResources();
 			Drawable gold = res.getDrawable(R.drawable.goldpeg);
 			Drawable silver = res.getDrawable(R.drawable.silverpeg);
 			Drawable white = res.getDrawable(R.drawable.whitepeg);
 			
 			if (!winningTurn) {
 				int[] pegs = { R.id.peg0, R.id.peg1, R.id.peg2, R.id.peg3 };
 				for (int peg : pegs) {
 					if (goldPegs > 0) {
 						((ImageView) view.findViewById(peg)).setImageDrawable(gold);
 						goldPegs --;
 					} else if (silverPegs > 0) {
 						((ImageView) view.findViewById(peg)).setImageDrawable(silver);
 						silverPegs --;
 					} else {
 						((ImageView) view.findViewById(peg)).setImageDrawable(white);
 					}
 				}
				view.setBackgroundResource(R.drawable.itembg);
			} else {
				view.setBackgroundResource(R.drawable.gameendbg);
 			}
 		}
		
 		return view;
 	}
 
 }
