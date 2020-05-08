 package com.time.master.dialog;
 
 import java.util.HashMap;
 import com.time.master.R;
 import com.time.master.TimeMasterApplication;
 import com.time.master.interfacer.WheelResultInterface;
 import com.time.master.view.BasicTextView;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.content.DialogInterface;
 import com.time.master.view.SelectedTextView;
 
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 
 import android.view.ViewGroup;
 
 /**
  * "ظ"Ի
  * 
  * @author wanghuiming
  * 
  */
 
 public class RepeatDialogFragment extends BasicDialogFragment implements
 		View.OnClickListener {
 	public static final String tag = "RepeatDialogFragment";
 	BasicTextView date_top_left,// ÿһ ť
 			date_top_center,// ظһ찴ť
 			confirm;// ȷϰť
 	WheelDialogFragment datatopFragment, datecenterFragment;
 	SelectDayFragment selecteddayFragment;
 	SelectedTextView dtmselect, dtmcurrent, dtworking, dteveryday, dtmonday,
 			dtlmselect, dtlmcurrent, dtfestival, dtqueque, dttuesday,
 			dtyselect, dtycurrent, dtcomday, dtotherweek, dtwednesday,
 			dtlyselect, dtlycurrent, dtdayoff, dtsaturday, dtthursday,
 			dtplyouself, dtsunday, dtfriday, yourselfBasicTextView;// Űť
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Light);
 		super.onCreate(savedInstanceState);
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		setDialogStyle();
 
 		if (viewStatus == null)
 			viewStatus = new Bundle();
 
 		View layout = inflater.inflate(R.layout.date_repeat, container, false);
 
 		date_top_left = (BasicTextView) layout.findViewById(R.id.date_top_left);
 		date_top_left.setOnClickListener(this);
 		String txt = TimeMasterApplication.getInstance().getCacheModel().tmpResultsCache
 				.get(DateDailyRepeatDiaogFragment.TAG);
 		if (txt != null)
 			date_top_left.setText(txt);
 
 		date_top_center = (BasicTextView) layout
 				.findViewById(R.id.date_top_center);
 		date_top_center.setOnClickListener(this);
 		txt = TimeMasterApplication.getInstance().getCacheModel().tmpResultsCache
 				.get(DateDaysRepeatDialogFragment.TAG);
 		if (txt != null)
 			date_top_center.setText(txt);
 
 		confirm = (BasicTextView) layout.findViewById(R.id.date_confirm);
 		dtmselect = (SelectedTextView) layout
 				.findViewById(R.id.date_month_select);
 		dtmcurrent = (SelectedTextView) layout
 				.findViewById(R.id.date_month_current);
 		dtworking = (SelectedTextView) layout.findViewById(R.id.date_working);
 		dteveryday = (SelectedTextView) layout.findViewById(R.id.date_everyday);
 		dtmonday = (SelectedTextView) layout.findViewById(R.id.date_monday);
 		dtlmcurrent = (SelectedTextView) layout
 				.findViewById(R.id.date_lunar_month_current);
 		dtlmselect = (SelectedTextView) layout
 				.findViewById(R.id.date_lunar_month_select);
 		dtfestival = (SelectedTextView) layout.findViewById(R.id.date_festival);
 		dtqueque = (SelectedTextView) layout.findViewById(R.id.date_quaque);
 		dttuesday = (SelectedTextView) layout.findViewById(R.id.date_tuesday);
 		dtyselect = (SelectedTextView) layout
 				.findViewById(R.id.date_year_select);
 		dtycurrent = (SelectedTextView) layout
 				.findViewById(R.id.date_year_current);
 		dtcomday = (SelectedTextView) layout
 				.findViewById(R.id.date_commemorate_day);
 		dtotherweek = (SelectedTextView) layout
 				.findViewById(R.id.date_other_week);
 		dtwednesday = (SelectedTextView) layout
 				.findViewById(R.id.date_wednesday);
 		dtlyselect = (SelectedTextView) layout
 				.findViewById(R.id.date_lunar_year_select);
 		dtlycurrent = (SelectedTextView) layout
 				.findViewById(R.id.date_lunar_year_current);
 		dtdayoff = (SelectedTextView) layout.findViewById(R.id.date_day_off);
 		dtsaturday = (SelectedTextView) layout.findViewById(R.id.date_saturday);
 		dtthursday = (SelectedTextView) layout.findViewById(R.id.date_thursday);
 		dtplyouself = (SelectedTextView) layout
 				.findViewById(R.id.date_plan_yourself);
 		dtsunday = (SelectedTextView) layout.findViewById(R.id.date_sunday);
 		dtfriday = (SelectedTextView) layout.findViewById(R.id.date_friday);
 		yourselfBasicTextView = (SelectedTextView) layout
 				.findViewById(R.id.date_plan_yourself);
 
 		setInitialStatus(dtworking);
 		setInitialStatus(dteveryday);
 		setInitialStatus(dtmonday);
 		setInitialStatus(dtlmcurrent);
 		setInitialStatus(dtlmselect);
 		setInitialStatus(dtfestival);
 		setInitialStatus(dttuesday);
 		setInitialStatus(dtyselect);
 		setInitialStatus(dtycurrent);
 		setInitialStatus(dtcomday);
 		setInitialStatus(dtotherweek);
 		setInitialStatus(dtwednesday);
 		setInitialStatus(dtlyselect);
 		setInitialStatus(dtlycurrent);
 		setInitialStatus(dtdayoff);
 		setInitialStatus(dtsaturday);
 		setInitialStatus(dtthursday);
 		setInitialStatus(dtplyouself);
 		setInitialStatus(dtsunday);
 		setInitialStatus(dtfriday);
 		setInitialStatus(yourselfBasicTextView);
 
 		dtmselect.setOnClickListener(this);
 		dtmcurrent.setOnClickListener(this);
 		dtworking.setOnClickListener(this);
 		dteveryday.setOnClickListener(this);
 		dtmonday.setOnClickListener(this);
 		dtlmcurrent.setOnClickListener(this);
 		dtlmselect.setOnClickListener(this);
 		dtfestival.setOnClickListener(this);
 		dtqueque.setOnClickListener(this);
 		dttuesday.setOnClickListener(this);
 		dtyselect.setOnClickListener(this);
 		dtycurrent.setOnClickListener(this);
 		dtcomday.setOnClickListener(this);
 		dtotherweek.setOnClickListener(this);
 		dtwednesday.setOnClickListener(this);
 		dtlyselect.setOnClickListener(this);
 		dtlycurrent.setOnClickListener(this);
 		dtdayoff.setOnClickListener(this);
 		dtsaturday.setOnClickListener(this);
 		dtthursday.setOnClickListener(this);
 		dtplyouself.setOnClickListener(this);
 		dtsunday.setOnClickListener(this);
 		dtfriday.setOnClickListener(this);
 		yourselfBasicTextView.setOnClickListener(this);
 
 		return layout;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		if (v instanceof SelectedTextView) {
 			setViewStatus(v);
 		}
 		switch (v.getId()) {
 		case R.id.date_plan_yourself:
 			showDialog(new RepeatCustomizedDialogFragment());
 			break;
 		case R.id.date_top_left:
 			if (dtmcurrent.isSelected() || dtlmcurrent.isSelected()) {
 				datatopFragment = new DateDailyRepeatDiaogFragment();
 				datatopFragment.setShowsDialog(true);
 				datatopFragment.setWheelInterface(new WheelResultInterface() {
 
 					@Override
 					public void getResult(String result) {
 						date_top_left.setText(TimeMasterApplication
 								.getInstance().getCacheModel().tmpResultsCache
 								.get(DateDailyRepeatDiaogFragment.TAG));
 					}
 				});
 				showDialog(datatopFragment);
 			}
 			break;
 		case R.id.date_top_center:
 			datecenterFragment = new DateDaysRepeatDialogFragment();
 			datecenterFragment.setShowsDialog(true);
 			datecenterFragment.setWheelInterface(new WheelResultInterface() {
 
 				@Override
 				public void getResult(String result) {
 					date_top_center.setText(TimeMasterApplication.getInstance()
 							.getCacheModel().tmpResultsCache
 							.get(DateDaysRepeatDialogFragment.TAG));
 				}
 			});
 			showDialog(datecenterFragment);
 			break;
 		case R.id.date_month_select:      
 			selecteddayFragment = new SelectDayFragment();
 			selecteddayFragment.setShowsDialog(true);
 			showDialog(selecteddayFragment);
			break;
 			
 		case R.id.date_year_select:
 			selecteddayFragment = new SelectDayFragment();
 			selecteddayFragment.setShowsDialog(true);
 			showDialog(selecteddayFragment);
			break;
 		default:
 			break;
 		}
 
 	}
 
 	private void setInitialStatus(SelectedTextView view) {
 		if (viewStatus.getBoolean(view.getId() + "", false))
 			view.setSelected();
 	}
 }
