 package de.gymbuetz.gsgbapp;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.DatePicker;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class PlanFragment extends SherlockFragment implements OnClickListener, OnDateSetListener {
 	Calendar cal;
 	public PlanFragment() {
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.plan_fragment,
 				container, false);
 		
 		TextView tv_day = (TextView)rootView.findViewById(R.id.textview_choosen_day);
 		tv_day.setOnClickListener(this);
 		
 		return rootView;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId())
 		{
 		case R.id.textview_choosen_day:
 		    DialogFragment newFragment = new DatePickerFragment();
 		    newFragment.setTargetFragment(this, 0);
 		    newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
 			break;
 		}
 	}
 
 	protected void updateDay() {
 		
 		TextView tv_day = (TextView)getView().findViewById(R.id.textview_choosen_day);
 		tv_day.setText(DateFormat.format("dd.MM.yyyy", cal));
 	}
 
 	@Override
 	public void onDateSet(DatePicker view, int year, int monthOfYear,
 			int dayOfMonth) {
 		cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
         updateDay();
         updateRep();
 	}
 
 	private void updateRep() {
 		int rep = getRepCount();
 		TextView tv_rep = (TextView)getView().findViewById(R.id.textview_av_rep);
 		if(rep == 0)
 		{
 			tv_rep.setText(R.string.no_repres);
 		}
 		else if(rep == 1)
 		{
 			tv_rep.setText(String.valueOf(rep) + " " + getString(R.string.repres_pattern_one));
 		}
 		else
 		{
 			tv_rep.setText(String.valueOf(rep) + " " + getString(R.string.repres_pattern_plur));
 		}
 	}
 
 	private int getRepCount() {
		if(cal.get(Calendar.DAY_OF_MONTH) == 3)
 		{
 			return 1;
 		}
		else if(cal.get(Calendar.DAY_OF_MONTH) > 15)
 		{
			return cal.get(Calendar.DAY_OF_MONTH);
 		}
 		else
 		{
 		return 0;
 		}
 	}
 }
