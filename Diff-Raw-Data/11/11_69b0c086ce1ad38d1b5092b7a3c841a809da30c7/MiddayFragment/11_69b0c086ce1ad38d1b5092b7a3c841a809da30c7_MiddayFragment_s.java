 package uk.co.mpkdashx.CBTT;
 
 import uk.co.mpkdashx.CBTT.MainActivity.TimePickerFragment;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
import android.provider.AlarmClock;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class MiddayFragment extends SherlockFragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final ListView middaylistView = new ListView(getActivity());
 		String[] midday = getResources().getStringArray(R.array.midday_array);
 
 		middaylistView.setAdapter(new ArrayAdapter<String>(getActivity(),
 				R.layout.list_item, midday));
 		middaylistView.setTextFilterEnabled(true);
 		middaylistView
 				.setOnItemLongClickListener(new OnItemLongClickListener() {
 					public boolean onItemLongClick(AdapterView<?> argo0,
 							View arg1, int pos, long id) {
 						String data = null;
 						if (pos == 0 || pos == 5) {
 
 							data = middaylistView.getItemAtPosition(pos)
 									.toString();
 							if (pos == 0) {
 								Intent intent = new Intent(
 										android.content.Intent.ACTION_VIEW,
 										Uri.parse("geo:0,0?q=1+Avenue+Road,+Aston,+Birmingham"));
 								Log.v("clicked", data + " @ " + "pos " + pos
 										+ " Launched maps with query: "
 										+ intent);
 								startActivity(intent);
 							} else if (pos == 5) {
 								Intent intent = new Intent(
 										android.content.Intent.ACTION_VIEW,
 										Uri.parse("geo:0,0?q=Birmingham,+Upper+Bull+St+(Stop+BA),+Birmingham,+West+Midlands+B4,+United+Kingdom&hl=en&ll=52.482048,1.896493&spn=0.011069,0.01929&geocode=FQLQIAMdzA_j_w&hnear=Birmingham,+Upper+Bull+St+(Stop+BA)&t=m&z=16"));
 								Log.v("clicked", data + " @ " + "pos " + pos
 										+ " Launched maps with query: "
 										+ intent);
 								startActivity(intent);
 							}
 
 						} else {
 							data = middaylistView.getItemAtPosition(pos)
 									.toString();
 							String[] split = data.split(":");
 							split[0] = split[0].replace("*", "");
 							split[1] = split[1].replace("*", "");
 							int H = Integer.parseInt(split[0]);
 							int M = Integer.parseInt(split[1]);
 							
 
 							new MainActivity.TimePickerFragment();
 							TimePickerFragment.newInstance(H, M).show(
 									getActivity().getSupportFragmentManager(),
 									"timePicker");
 
 							Log.v("clicked", data + " @ " + "pos " + pos
 									+ " went to TimePicker with data: " + data);
 						}
 						return false;
 
 					}
 				});
 		return middaylistView;
 	}
 }
