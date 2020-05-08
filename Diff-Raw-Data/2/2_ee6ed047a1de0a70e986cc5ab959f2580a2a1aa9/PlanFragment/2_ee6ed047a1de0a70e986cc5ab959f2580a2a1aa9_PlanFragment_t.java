 package de.gymbuetz.gsgbapp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.os.AsyncTask;
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
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 public class PlanFragment extends SherlockFragment implements OnClickListener, OnDateSetListener {
 	private static final String url = "http://adrianhomepage.ad.ohost.de/Ausfallplan.xml";
 	
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
     public void onStart() {
         super.onStart();
         loadReplacementData();
     }
 
 	private void loadReplacementData() {
 		new DownloadXmlTask().execute(url);
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
 	
 	// Implementation of AsyncTask used to download XML feed from stackoverflow.com.
     private class DownloadXmlTask extends AsyncTask<String, Void, String> {
 
         @Override
         protected String doInBackground(String... urls) {
             try {
                 return loadXmlFromNetwork(urls[0]);
             } catch (IOException e) {
                 return getResources().getString(R.string.connection_error);
             } catch (XmlPullParserException e) {
                 return getResources().getString(R.string.xml_error);
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
     
     
     
     // TODO split download, parsing and display functionality in 
     // TODO function to safe the XML file
     
     @SuppressWarnings("null")
 	private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
         InputStream stream = null;
         Document jdoc = null;
         List<Element> content = null;
         Calendar rightNow = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm dd.MM.yyyy", Locale.getDefault());
 
          StringBuilder htmlString = new StringBuilder();
          htmlString.append("<h3>" + /* getResources().getString(R.string.page_title) */ "Title" + "</h3>");
          htmlString.append("<em>" + /* getResources().getString(R.string.updated) */ "updated" + " " +
                 formatter.format(rightNow.getTime()) + "</em>");
 
         try {        	
             jdoc = Jsoup.connect(urlString).get();
         // Makes sure that the InputStream is closed after the app is
         // finished using it.
         } finally {
             if (stream != null) {
                 stream.close();
             }
         }
         
         content = jdoc.getElementsByTag("tag");
         RepPlan RepPlan = null;
         
         
         // Parse The XML File Into a RepPlan object.
         for (Element day : content) {
         	String Date = day.getElementsByTag("datum").text();
         	String MissingTeachers = day.getElementsByTag("flehrer").text();
         	
         	List<Representation> Reps = null;
         	for (Element entry : day.getElementsByTag("eintrag")) {
         		String lesson = entry.getElementsByTag("stunde").first().text();
         		String clas = entry.getElementsByTag("klasse").first().text();
         		String room = entry.getElementsByTag("raum").first().text();
         		String subject = entry.getElementsByTag("fach").first().text();
         		String teacher = entry.getElementsByTag("lehrer").first().text();
         		String more = entry.getElementsByTag("weiteres").first().text();
         		Reps.add(new Representation(teacher, lesson, room, subject, clas, more));
         	}
         	
         	
         	RepPlanDay rpd = new RepPlanDay(Date, MissingTeachers, Reps);
         	RepPlan.addDay(rpd);
         }
         
         
         // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
         // Each Entry object represents a single post in the XML feed.
         // This section processes the entries list to combine each entry with HTML markup.
         // Each entry is displayed in the UI as a link that optionally includes
         // a text summary.
         for (RepPlanDay tag : RepPlan.Days) {
             htmlString.append("<h2>" + tag.Date + "</h2>");
             htmlString.append("<h3>" + tag.MissingTeachers + "</h3>");
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
