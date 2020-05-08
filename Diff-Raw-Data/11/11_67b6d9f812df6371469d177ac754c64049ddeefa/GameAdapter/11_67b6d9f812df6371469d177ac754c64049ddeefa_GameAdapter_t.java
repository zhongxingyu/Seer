 package uk.co.thomasc.wordmaster.view.game;
 
 import java.util.Comparator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import uk.co.thomasc.wordmaster.R;
 import uk.co.thomasc.wordmaster.objects.Turn;
 
 public class GameAdapter extends ArrayAdapter<Turn> {
 
 	private Activity act;
 	final private Comparator<Turn> comp;
 
 	public GameAdapter(Activity act) {
 		super(act, 0);
 
 		this.act = act;
 
 		comp = new Comparator<Turn>() {
 			@Override
 			public int compare(Turn e1, Turn e2) {
 				return e1.getUnixTimestamp() == e2.getUnixTimestamp() ? 0 : e1.getUnixTimestamp() > e2.getUnixTimestamp() ? 1 : -1;
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
 
 		if (view == null) {
 			LayoutInflater vi = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = vi.inflate(R.layout.game_row_big, null);
 		}
		
		Turn item = getItem(position);
		String mostRecentMove = "2m"; //TODO: Calculate this
		((TextView) view.findViewById(R.id.guess)).setText(item.getGuess());
		((TextView) view.findViewById(R.id.time)).setText(mostRecentMove);
 
 		return view;
 	}
 
 }
