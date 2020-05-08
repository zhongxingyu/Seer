 package net.animeimports.android;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AIEventAdapter extends ArrayAdapter<AIEventEntry> {
 	private ArrayList<AIEventEntry> items;
 	Context mContext = null;
 	
 	public ArrayList<AIEventEntry> getItems() {
 		return items;
 	}
 
 	public AIEventAdapter(Context context, int textViewResourceId, ArrayList<AIEventEntry> items) {
 		super(context, textViewResourceId, items);
 		mContext = context;
 		if(items.size() != 0) {
     		this.items = items;
    		if(!items.get(0).getName().equalsIgnoreCase("back")) {
     			AIEventEntry back = new AIEventEntry();
     			back.setName("back");
     			this.items.add(0, back);
     		}
 		}
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 		if(v == null) {
 			LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(R.layout.row_event, null);
 		}
 		AIEventEntry event = items.get(position);
 		TextView tt = (TextView) v.findViewById(R.id.toptext);
 		TextView bt = (TextView) v.findViewById(R.id.bottomtext);
 		ImageView icon = (ImageView) v.findViewById(R.id.icon);
 		
 		if(event != null) {
			if(event.getName().equalsIgnoreCase("back")) {
 				icon.setVisibility(icon.GONE);
 				if(tt != null) {
     				tt.setText(event.getName());
     			}
     			if(bt != null) {
     				bt.setVisibility(bt.GONE);
     			}
 			}
 			else {
 				icon.setImageResource(getIcon(event.getName()));
     			if(tt != null) {
     				tt.setText(event.getName());
     			}
     			if(bt != null) {
     				bt.setText(event.getDate() + ", " + event.getTime());
     			}
 			}
 		}
 		return v;
 	}
 
 
 	/**
 	 * Returns the appropriate icon for the provided event
 	 * Draft: return the latest (at the time) set's Common symbol
 	 * FNM: return the latest (at the time) set's Uncommon symbol
 	 * Release/PreRelease/Gameday: return the latest (at the time) set's Rare symbol
 	 * GPT: return the latest (at the time) set's Mythic symbol
 	 * @param input
 	 * @return
 	 */
 	private int getIcon(String input) {
 		int retVal = R.drawable.icon;
 		if(input.toLowerCase().contains("draft")) {
 			retVal = R.drawable.icon_draft;
 		}
 		else if(input.toLowerCase().contains("hg")) {
 			retVal = R.drawable.icon_2hg;
 		}
 		else if(input.toLowerCase().contains("sealed")) {
 			retVal = R.drawable.icon_sealed;
 		}
 		else if(input.toLowerCase().contains("standard")) {
 			retVal = R.drawable.icon_standard;
 		}
 		
 		if(input.toLowerCase().contains("release") || input.toLowerCase().contains("prerelease") || 
 			input.toLowerCase().contains("game day") || input.toLowerCase().contains("launch")) {
 			retVal = R.drawable.icon_isd_rare;
 		}
 		
 		if(input.toLowerCase().contains("gpt") ||
 				input.toLowerCase().contains("grand prix")) {
 			retVal = R.drawable.icon_isd_mythic;
 		}
 		
		if(input.toLowerCase().contains("edh") || input.toLowerCase().contains("commander")) {
 			if(input.toLowerCase().contains("free play")) {
 				retVal = R.drawable.icon_edh_common;
 			}
 			else {
 				retVal = R.drawable.icon_edh_uncommon;
 			}
 		}
 		
 		return retVal;
 	}
 }
