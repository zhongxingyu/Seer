 /*
  * Copyright (C) 2013 Moritz Heindl <lenidh[at]gmail[dot]com>
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package lenidh.android.holochron.adapters;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import de.lenidh.libzeitmesser.stopwatch.Lap;
 import de.lenidh.libzeitmesser.stopwatch.LapContainer;
 import lenidh.android.holochron.App;
 import lenidh.android.holochron.R;
 
 import java.util.List;
 
 public abstract class LapAdapter extends ArrayAdapter<Lap> {
 
 	private static final String TAG = "LapArrayAdapter";
 	private final LapContainer container;
 	private final LayoutInflater inflater;
 	private final int tileResId;
 
 	private static int getLayout(Context context) {
 		int layout;
 		if(App.getThemePreference().equals(context.getString(R.string.pref_value_theme_classic))) {
 			layout = R.layout.lap_listitem_classic;
 		} else {
 			layout = R.layout.lap_listitem;
 		}
 		return layout;
 	}
 
 	public LapAdapter(Context context, LapContainer container, List<Lap> laps) {
 		super(context, getLayout(context), laps);
 
 		this.container = container;
 
 		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		if (App.getThemePreference().equals(getContext().getString(R.string.pref_value_theme_dark))) {
 			this.tileResId = R.drawable.tile_shape_dark;
 		} else {
 			this.tileResId = R.drawable.tile_shape;
 		}
 	}
 
 	protected abstract long getTime(Lap lap);
 
 	protected abstract long getTimeDiff(Lap lap);
 
 	/**
 	 * This method creates a formatted time string from a millisecond value. Format: [[[[[h]h:]m]m:]s]s.ms (e.g.
 	 * 00:00:00.000)
 	 *
 	 * @param time in milliseconds
 	 * @param trim If true, leading zeros and separators are trimmed. (e.g. 00:00:00.000 becomes 0.000)
 	 * @return formatted time string
 	 */
 	private static String formatTime(long time, boolean trim) {
 		StringBuilder timeFormat = new StringBuilder();
 
 		short digits[] = {
 				(short) (time / 3600000 % 6),    // hours: tens
 				(short) (time / 360000 % 10),    //        ones
 				-1,                              // separator: ':'
 				(short) (time / 60000 % 6),      // minutes: tens
 				(short) (time / 6000 % 10),      //          ones
 				-1,                              // separator: ':'
 				(short) (time / 1000 % 6),       // seconds: tens
 				(short) (time / 100 % 10),       //          ones
 				-2,                              // separator: '.'
 				(short) (time / 10 % 10),        // milliseconds: tens
 				(short) (time % 10),             //               hundreds
 		};
 
 		int index = 0;
 
 		// Skip leading zeros and separators.
 		while (trim && index < 7 && digits[index] <= 0) index++;
 
 		while (index < 11) {
 			if (digits[index] >= 0) {
 				timeFormat.append(digits[index]);
 			} else if (digits[index] == -1) {
 				timeFormat.append(':');
 			} else if (digits[index] == -2) {
 				timeFormat.append('.');
 			} else {
 				Log.e(TAG, String.format("Invalid value while formatting: time: %d, index: %d, value: %d",
 				                         time, index, digits[index]));
 			}
 			index++;
 		}
 
 		return timeFormat.toString();
 	}
 
 	@Override
 	public final View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		// Reuse existing Views.
 		if (convertView != null) {
 			holder = (ViewHolder) convertView.getTag();
 		} else {
 			convertView = this.inflater.inflate(getLayout(getContext()), parent, false);
 			assert convertView != null;
 
 			holder = new ViewHolder();
 			holder.numberView = (TextView) convertView.findViewById(R.id.txt_number);
 			holder.timeView = (TextView) convertView.findViewById(R.id.txt_time);
 			holder.diffView = (TextView) convertView.findViewById(R.id.txt_diff);
 			holder.tileView = (LinearLayout) convertView.findViewById(R.id.tile);
 
 			convertView.setTag(holder);
 		}
 
 		Lap item = this.getItem(position);
		holder.numberView.setText("# " + Integer.toString(this.container.NumberOf(item) + 1));
 
 		holder.timeView.setText(formatTime(getTime(item), false));
 		if (getTimeDiff(item) == 0) {
 			holder.diffView.setText(formatTime(getTimeDiff(item), true));
 		} else {
 			holder.diffView.setText("+" + formatTime(getTimeDiff(item), true));
 		}
 
 		if(!App.getThemePreference().equals(getContext().getString(R.string.pref_value_theme_classic))) {
 			holder.tileView.setBackgroundResource(this.tileResId);
 		}
 
 		return convertView;
 	}
 
 	private class ViewHolder {
 		public TextView numberView;
 		public TextView timeView;
 		public TextView diffView;
 		public LinearLayout tileView;
 	}
 
 }
