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
 
 public class AfternoonFragment extends SherlockFragment {
 
 	/*
 	 * (nonJavadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater
 	 * , android.view.ViewGroup, android.os.Bundle)
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final ListView afternoonlistView = new ListView(getActivity());
 		String[] afternoon = getResources().getStringArray(
 				R.array.afternoon_array);
 
 		afternoonlistView.setAdapter(new ArrayAdapter<String>(getActivity(),
 				R.layout.list_item, afternoon));
 		afternoonlistView.setTextFilterEnabled(true);
 		afternoonlistView
 				.setOnItemLongClickListener(new OnItemLongClickListener() {
 					public boolean onItemLongClick(AdapterView<?> argo0,
 							View arg1, int pos, long id) {
 						String data = null;
 						if (pos == 0 || pos == 5 || pos == 9 || pos == 13) {
 							data = afternoonlistView.getItemAtPosition(pos)
 									.toString();
 							if (pos == 0) {
 								Intent intent = new Intent(
 										android.content.Intent.ACTION_VIEW,
 										Uri.parse("geo:0,0?q=Capgemini+Aston"));
 								Log.v("clicked", data + " @ " + "pos " + pos
 										+ " Launched maps with query: "
 										+ intent);
 								startActivity(intent);
 							} else if (pos == 5) {
 								Intent intent = new Intent(
 										android.content.Intent.ACTION_VIEW,
 										Uri.parse("geo:0,0?q=Birmingham,+Corporation+St+(Stop+CL)"));
 								Log.v("clicked", data + " @ " + "pos " + pos
 										+ " Launched maps with query: "
 										+ intent);
 								startActivity(intent);
 							} else if (pos == 9) {
 								Intent intent = new Intent(
 										android.content.Intent.ACTION_VIEW,
 										Uri.parse("geo:0,0?q=Radisson+Blu+Hotel+Birmingham"));
 								Log.v("clicked", data + " @ " + "pos " + pos
 										+ " Launched maps with query: "
 										+ intent);
 								startActivity(intent);
 							} else if (pos == 13) {
 								Intent intent = new Intent(
 										android.content.Intent.ACTION_VIEW,
 										Uri.parse("geo:0,0?q=Crowne+Plaza+Hotel+Birmingham+City+Centre"));
 								Log.v("clicked", data + " @ " + "pos " + pos
 										+ " Launched maps with query: "
 										+ intent);
 								startActivity(intent);
 							} else if (pos == 17) {
 								Intent intent = new Intent(
 										android.content.Intent.ACTION_VIEW,
 										Uri.parse("geo:0,0?q=Novotel+Birmingham+Centre"));
 								Log.v("clicked", data + " @ " + "pos " + pos
 										+ " Launched maps with query: "
 										+ intent);
 								startActivity(intent);
 							}
 
 						} else {
 							data = afternoonlistView.getItemAtPosition(pos)
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
 		return afternoonlistView;
 	}
 }
