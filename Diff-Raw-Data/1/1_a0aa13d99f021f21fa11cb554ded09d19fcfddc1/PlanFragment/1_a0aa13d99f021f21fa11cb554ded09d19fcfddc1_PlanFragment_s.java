 package de.gymbuetz.gsgbapp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import de.gymbuetz.gsgbapp.classes.DownloadXmlTask;
 import de.gymbuetz.gsgbapp.classes.RepPlan;
 import de.gymbuetz.gsgbapp.classes.RepPlanDay;
 
 public class PlanFragment extends SherlockFragment implements OnClickListener, OnDateSetListener {
 	public static final String url = "http://root.sebastianr.pfweb.eu/repplan.xml";
 	public static final String saveFileName = "savefile.xml";
 
 	String resultString;
 
 	Calendar cal;
 
 	public PlanFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.plan_fragment, container, false);
 
 		TextView tv_day = (TextView) rootView.findViewById(R.id.textview_choosen_day);
 		tv_day.setOnClickListener(this);
 
 		Button btn_open_day = (Button) rootView.findViewById(R.id.button_open_day);
 		btn_open_day.setEnabled(false);
 		btn_open_day.setOnClickListener(this);
 
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (PreferenceManager.getDefaultSharedPreferences(getSherlockActivity()).getBoolean("offline_mode", false)) {
 			try {
 				resultString = loadXmlfromFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			loadReplacementData();
 		}
 	}
 
 	private void loadReplacementData() {
 		ProgressDialog pd = new ProgressDialog(getSherlockActivity());
 		pd.setProgress(0);
 		pd.setTitle(R.string.loading);
 		pd.show();
 		pd.setOnCancelListener(new OnCancelListener() {
 
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				try {
 					resultString = loadXmlfromFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		new DownloadXmlTask(getActivity(), url, saveFileName, pd).execute();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 			case R.id.textview_choosen_day:
 				DialogFragment newFragment = new DatePickerFragment();
 				newFragment.setTargetFragment(this, 0);
 				newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
 				break;
 			case R.id.button_open_day:
 				Intent i = new Intent(getActivity().getApplicationContext(), DayActivity.class);
 				i.putExtra("result", resultString);
 				i.putExtra("date", DateFormat.format("dd.MM.yyyy", cal));
 				startActivity(i);
 				break;
 		}
 	}
 
 	protected void updateDay() {
 
 		TextView tv_day = (TextView) getView().findViewById(R.id.textview_choosen_day);
 		tv_day.setText(DateFormat.format("dd.MM.yyyy", cal));
 	}
 
 	@Override
 	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 		cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
 		updateDay();
 		updateRep();
 	}
 
 	private void updateRep() {
 		int rep = getRepCount();
 		TextView tv_rep = (TextView) getView().findViewById(R.id.textview_av_rep);
 		Button btn_open_day = (Button) getView().findViewById(R.id.button_open_day);
 		if (rep == 0) {
 			tv_rep.setText(R.string.no_repres);
 			btn_open_day.setEnabled(false);
 
 		} else if (rep == 1) {
 			tv_rep.setText(String.valueOf(rep) + " " + getString(R.string.repres_pattern_one));
 			btn_open_day.setEnabled(true);
 		} else {
 			tv_rep.setText(String.valueOf(rep) + " " + getString(R.string.repres_pattern_plur));
 			btn_open_day.setEnabled(true);
 		}
 	}
 
 	private int getRepCount() {
 		RepPlan repPlan = RepPlan.parseXml(resultString);
 
 		int representations = 0;
 
 		for (RepPlanDay d : repPlan.getDays()) {
 			if (d.getDate().contentEquals(DateFormat.format("dd.MM.yyyy", cal))) {
 				representations += d.getRepList().size();
 			}
 		}
 
 		return representations;
 	}
 
 	private String loadXmlfromFile() throws IOException {
 		// Declaration of the file to read
 		File file = new File(getActivity().getFilesDir(), saveFileName);
 
 		// try block for reading
 		try {
 			return getStringFromFile(file);
 		} catch (Exception e) {
 			Toast.makeText(getSherlockActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
 			return "";
 		}
 	}
 
 	public static String getStringFromFile(File fl) throws Exception {
 
 		FileInputStream fin = new FileInputStream(fl);
 		String ret = DownloadXmlTask.convertStreamToString(fin);
 		// Make sure you close all streams.
 		fin.close();
 		return ret;
 	}
 }
