 package de.reneruck.tcd.ipp.andclient;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CalendarView;
 import android.widget.CalendarView.OnDateChangeListener;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class NewBookingFragment extends Fragment {
 
 
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	View layout = inflater.inflate(R.layout.fragment_new_booking, null);
     	CalendarView calendar = (CalendarView) ((LinearLayout)layout).findViewById(R.id.calendarView);
     	calendar.setOnDateChangeListener(this.dateChangeListener);
         return layout;
     }
 	
 	private OnDateChangeListener dateChangeListener = new OnDateChangeListener() {
 		
 		@Override
 		public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
 			String date = "";
			month++;
 			if((month /10) < 0.9) {
 				date = dayOfMonth + ".0" + month + "." + year;
 			} else {
 				date = dayOfMonth + "." + month + "." + year;
 			}
 			showTimeChooser(date);
 		}
 
 		private void showTimeChooser(String date) {
 			FragmentManager fm = getFragmentManager();
 			FragmentTransaction ft = fm.beginTransaction();
 			ft.add(new TimeChooser(date), "timeChooser");
 			ft.commit();
 		}
 	};
 	
 	class TimeChooser extends DialogFragment {
 		private String title;
 
 		public TimeChooser(String date) {
 			this.title = date;
 		}
 		
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle(this.title)
 			.setItems(R.array.times, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					String time = "";
 					Resources res = getResources();
 					String[] times = res.getStringArray(R.array.times);
 					time = title + " " + times[which];
 					
 					Toast.makeText(getActivity(), "Selected:" + time, Toast.LENGTH_SHORT).show();
 					
 					BookingGenerator generator = new BookingGenerator(getActivity());
 					generator.doInBackground(time);
 				}
 			});
 			return builder.create();
 		}
 	}
 }
