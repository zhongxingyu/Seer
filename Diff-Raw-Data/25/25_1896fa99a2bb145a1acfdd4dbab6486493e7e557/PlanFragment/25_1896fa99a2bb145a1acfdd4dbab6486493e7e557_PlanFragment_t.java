 package de.gymbuetz.gsgbapp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
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
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 public class PlanFragment extends SherlockFragment implements OnClickListener,
 		OnDateSetListener {
 	private static final String url = "http://adrianhomepage.ad.ohost.de/Ausfallplan.xml";
 
 	Calendar cal;
 
 	public PlanFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.plan_fragment, container, false);
 
 		TextView tv_day = (TextView) rootView.findViewById(R.id.textview_choosen_day);
 		tv_day.setOnClickListener(this);
 
 		Button btn_open_day = (Button) rootView.findViewById(R.id.button_open_day);
 		btn_open_day.setOnClickListener(this);
 
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		loadReplacementData();
 	}
 
 	private void loadReplacementData() {
 		new DownloadXmlTask().execute(url);
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
 			i.putExtra("date", cal.getTimeInMillis());
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
 		if (rep == 0) {
 			tv_rep.setText(R.string.no_repres);
 
 			Button btn_open_day = (Button) getView().findViewById(R.id.button_open_day);
 			btn_open_day.setEnabled(false);
 		} else if (rep == 1) {
 			tv_rep.setText(String.valueOf(rep) + " " + getString(R.string.repres_pattern_one));
 
 			Button btn_open_day = (Button) getView().findViewById(R.id.button_open_day);
 			btn_open_day.setEnabled(true);
 		} else {
 			tv_rep.setText(String.valueOf(rep) + " " + getString(R.string.repres_pattern_plur));
 
 			Button btn_open_day = (Button) getView().findViewById(R.id.button_open_day);
 			btn_open_day.setEnabled(true);
 		}
 	}
 
 	private int getRepCount() {
 		if (cal.get(Calendar.DAY_OF_MONTH) == 3) {
 			return 1;
 		} else if (cal.get(Calendar.DAY_OF_MONTH) > 15) {
 			return cal.get(Calendar.DAY_OF_MONTH);
 		} else {
 			return 0;
 		}
 	}
 
 	// Implementation of AsyncTask used to download XML feed from
 	// stackoverflow.com.
 	private class DownloadXmlTask extends AsyncTask<String, Void, String> {
 
 		@Override
 		protected String doInBackground(String... urls) {
 			try {
				return createHtmlString(parseXml(downloadXml(urls[0])));
 			} catch (IOException e) {
 				return getResources().getString(R.string.connection_error);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// Print the Data here!
 
 			// setContentView(R.layout.activity_parse_xml);
 			// Displays the HTML string in the UI via a WebView
 			// WebView myWebView = (WebView) findViewById(R.id.webView);
 			// myWebView.loadData(result, "text/html", null);
 		}
 	}
 
	private void loadXmlfromFile() throws IOException {
		// TODO function to load XML data from file
	}
	
	private void safeXmlFile() throws IOException {
		// TODO function to safe the XML file
	}
	
 	
	private Document downloadXml(String urlString) throws IOException {
 		InputStream stream = null;
 		Document jdoc = null;
 		try {
 			jdoc = Jsoup.connect(urlString).get();
 
 			// Makes sure that the InputStream is closed after the app is
 			// finished using it.
 		} finally {
 			if (stream != null) {
 				stream.close();
 			}
 		}
 
 		return jdoc;
 	}
 
 	private RepPlan parseXml(Document jdoc) {
 
 		List<Element> content = new ArrayList<Element>();
 
 		content = jdoc.getElementsByTag("day");
 		RepPlan RepPlan = new RepPlan();
		
 		// Parse The XML File Into a RepPlan object.
		try {
 		for (Element day : content) {
 			String Date = day.getElementsByTag("date").text();
 			//dayOfWeek tag is missing
 			int dayOfWeek = Integer.parseInt(day.getElementsByTag("dayOfWeek").first().text());
 			int numberOfWeek = 1;
 			
 			List<Representation> Reps = new ArrayList<Representation>();
 			for (Element entry : day.getElementsByTag("entry")) {
 				String clas = entry.getElementsByTag("class").first().text();
 				String lesson = entry.getElementsByTag("lesson").first().text();
 				String subject = entry.getElementsByTag("subject").first().text();
 				String subject_ori = entry.getElementsByTag("subject_ori").first().text();
 				String teacher = entry.getElementsByTag("teacher").first().text();
 				String room = entry.getElementsByTag("room").first().text();
 				String more = entry.getElementsByTag("more").first().text();
 				Log.d("val", Reps.toString());
 				Reps.add(new Representation(clas, lesson, subject, subject_ori, teacher, room, more));
 			}
 
 			RepPlanDay rpd = new RepPlanDay(Date,dayOfWeek,numberOfWeek,Reps);
 			RepPlan.addDay(rpd);
 		}
		} catch (NullPointerException npe) {
			//TODO: What should happen when there is an error?
		}
 		return RepPlan;
 	}
 
 	private String createHtmlString(RepPlan RepPlan) {
 
 		Calendar rightNow = Calendar.getInstance();
 		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm dd.MM.yyyy", Locale.getDefault());
 
 		StringBuilder htmlString = new StringBuilder();
 		htmlString.append("<h3>" + /* getResources().getString(R.string.page_title) */"Title" + "</h3>");
 		htmlString.append("<em>" + /* getResources().getString(R.string.update) */"updated" + " " + formatter.format(rightNow.getTime()) + "</em>");
 
 		for (RepPlanDay tag : RepPlan.Days) {
 			htmlString.append("<h2>" + tag.Date + "</h2>");
 			htmlString.append("<table><thead><tr><td>Stunde</td><td>Klasse</td><td>Fach</td><td>Raum</td><td>Lehrer</td><td>Weiteres</td></tr></thead>");
 
 			for (Representation entry : tag.RepList) {
 				htmlString.append("<tr><td>" + entry.Lesson + "</td>");
 				htmlString.append("<td>" + entry.Clas + "</td>");
 				htmlString.append("<td>" + entry.Room + "</td>");
 				htmlString.append("<td>" + entry.Subject + "</td>");
 				htmlString.append("<td>" + entry.Teacher + "</td>");
 				htmlString.append("<td>" + entry.More + "</td></tr>");
 			}
 			htmlString.append("</table>");
 		}
 
 		return htmlString.toString();
 	}
 }
