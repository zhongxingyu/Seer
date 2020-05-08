 package wei.mark.tabletennis.util;
 
 import java.util.List;
 
 import wei.mark.tabletennis.R;
 import wei.mark.tabletennis.model.PlayerModel;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class PlayerModelAdapter extends ArrayAdapter<PlayerModel> {
 	Context context;
 	ViewHolder holder;
 	List<PlayerModel> players;
 
 	public PlayerModelAdapter(Context context, int textViewResourceId,
 			List<PlayerModel> objects) {
 		super(context, textViewResourceId, objects);
 		this.context = context;
 		players = objects;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// get references to all the Views
 		if (convertView == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext()
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			convertView = inflater.inflate(R.layout.item_player_list, parent,
 					false);
 
 			holder = new ViewHolder();
 			holder.name = (TextView) convertView.findViewById(R.id.name);
 			holder.rating = (TextView) convertView.findViewById(R.id.rating);
 			holder.subtext = (TextView) convertView.findViewById(R.id.subtext);
 
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		// fill in the Views
 		PlayerModel player = players.get(position);
 		if (player != null) {
 			holder.name.setText(player.getName());
 			if (player.getSearchHistory() != null
 					&& player.getSearchHistory().size() > 5)
 				holder.name.setTextColor(context.getResources().getColor(
 						R.color.tertiary_text));
 			holder.rating.setText(player.getBaseRating());
 			holder.subtext.setText(player.toSubtextString());
 		}
 		return convertView;
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 
 	static class ViewHolder {
 		TextView name;
 		TextView rating;
 		TextView subtext;
 	}
 }
