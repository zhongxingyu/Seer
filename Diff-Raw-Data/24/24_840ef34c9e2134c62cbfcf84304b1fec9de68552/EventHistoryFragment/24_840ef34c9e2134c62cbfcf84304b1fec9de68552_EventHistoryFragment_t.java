 package dude.morrildl.providence.panopticon;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import android.app.ListFragment;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import dude.morrildl.providence.PanopticonActivity;
 import dude.morrildl.providence.R;
 
 public class EventHistoryFragment extends ListFragment {
 	private Cursor c;
 	private SQLiteDatabase db;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.event_history_fragment, container,
 				false);
 	}
 
 	public void onResume() {
 		super.onResume();
 		onHiddenChanged(false);
 	}
 
 	@Override
 	public void onHiddenChanged(boolean hidden) {
 		super.onHiddenChanged(hidden);
 		if (!hidden) {
 			OpenHelper helper = new OpenHelper(getActivity());
 			db = helper.getReadableDatabase();
 			c = db.query("events", new String[] { "which", "type", "event",
 					"event as image_event", "ts", "rowid as _id" },
 					"type <> 'Motion Sensor'", null, null, null, "ts desc");
 
 			SimpleCursorAdapter adapter = new SimpleCursorAdapter(
 					getActivity(), R.layout.event_history_row, c, new String[] {
 							"which", "event", "image_event", "ts" }, new int[] {
 							R.id.event_which, R.id.event_action,
 							R.id.indicator_image, R.id.event_ts }, 0);
 			adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 				@Override
 				public boolean setViewValue(View view, Cursor c, int column) {
 					if (column == 4) {
 						try {
 							SimpleDateFormat sdf = new SimpleDateFormat(
 									"yyyy'-'MM'-'dd'T'HH:mm:ss");
 							java.util.Date parsedTs = sdf.parse(c
 									.getString(column));
 
 							sdf = new SimpleDateFormat(
 									"EEE, dd MMMMMMMMM yyyy 'at' KK:mm:ssa");
 							String friendlyDate = sdf.format(parsedTs);
 							((TextView) view).setText(friendlyDate);
 						} catch (ParseException e) {
 							return false;
 						}
 						return true;
 					} else if (column == 3) {
 						String type = c.getString(column);
 						int resource = 0;
 						if ("Opened".equals(type)) {
 							resource = R.drawable.ic_event_door;
 						} else if ("Detected Motion".equals(type)) {
 							resource = R.drawable.ic_event_motion;
 						} else if ("Ajar".equals(type)) {
 							resource = R.drawable.ic_event_ajar;
 						}
 						((ImageView) view).setImageResource(resource);
 						return true;
 					}
 					return false;
 				}
 			});
 			setListAdapter(adapter);
 
 			Cursor tmpCursor = db.query("last_motion",
 					new String[] { "max(ts)" }, null, null, null, null, null);
 			TextView tv = (TextView) getView().findViewById(
 					R.id.motion_status_text);
 			if (tmpCursor.moveToFirst()) {
 				try {
 					SimpleDateFormat sdf = new SimpleDateFormat(
 							"yyyy'-'MM'-'dd'T'HH:mm:ss");
					String dateString = tmpCursor.getString(0);
					if (dateString != null) {
						java.util.Date parsedTs = sdf.parse(dateString);
 
						sdf = new SimpleDateFormat(
								"EEE, dd MMMMMMMMM yyyy 'at' KK:mm:ssa");
						dateString = sdf.format(parsedTs);
						tv.setText("Last motion detected at: " + dateString);
						tv.setVisibility(View.VISIBLE);
					} else {
						tv.setVisibility(View.GONE);
					}
 				} catch (ParseException e) {
 					tv.setVisibility(View.GONE);
 				}
 			} else {
 				tv.setVisibility(View.GONE);
 			}
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		((PanopticonActivity) getActivity()).showDetailsFragment(id);
 	}
 
 	public void onPause() {
 		super.onPause();
 		try {
 			c.close();
 			db.close();
 		} catch (Throwable t) {
 		}
 	}
 }
