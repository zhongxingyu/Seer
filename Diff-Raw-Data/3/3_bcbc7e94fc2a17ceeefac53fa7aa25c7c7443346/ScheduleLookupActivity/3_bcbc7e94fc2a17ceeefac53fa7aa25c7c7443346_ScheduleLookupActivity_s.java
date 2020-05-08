 package awesome.app;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScheduleLookupActivity extends Activity {
 
 	private String currentStudent;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Remove title bar
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.schedule_lookup);
 
 		makeButtonWork();
		Bundle bundle = this.getIntent().getExtras();
		String username = bundle.getString("username");
 		if (isValidUsername(username)) {
 			doScheduleSearch(username);
 		}
 	}
 
 	private boolean isValidUsername(String username) {
 		return username != null && username.trim().length() > 0;
 	}
 
 	private void makeButtonWork() {
 		Button button = (Button) findViewById(R.id.schedule_lookup_button);
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String searchString = ((EditText) findViewById(R.id.schedule_text)).getText().toString();
 				if (!isValidUsername(searchString)) {
 					Toast.makeText(getApplicationContext(), getResources().getString(R.string.emptyTextbox),
 							Toast.LENGTH_SHORT).show();
 				} else {
 					doScheduleSearch(searchString);
 				}
 			}
 		});
 	}
 
 	private void makeClassDataTable(ArrayList<ScheduleData> classList) {
 		TableLayout classDataTable = (TableLayout) findViewById(R.id.classDataTable);
 		for (ScheduleData classData : classList) {
 			TableRow tableRow = new TableRow(this);
 			tableRow.addView(getConfiguredTextView(classData.classNumber));
 			tableRow.addView(getConfiguredTextView(classData.className));
 			tableRow.addView(getConfiguredTextView(classData.instructor));
 			tableRow.addView(getConfiguredTextView(classData.finalData));
 			classDataTable.addView(tableRow);
 		}
 	}
 
 	private TextView getConfiguredTextView(String text) {
 		TextView textView = new TextView(this);
 		textView.setPadding(8, 5, 8, 5);
 		textView.setBackgroundDrawable((Drawable) getResources().getDrawable(R.drawable.cell_border));
 		textView.setTextSize(18);
 		textView.setText(text);
 		return textView;
 	}
 
 	private void doScheduleSearch(String searchString) {
 		if (searchString == currentStudent)
 			return;
 		currentStudent = searchString;
 		clearTable();
 		ScrollView scrollingTable = (ScrollView) findViewById(R.id.pageScrollView);
 		scrollingTable.setVisibility(View.VISIBLE);
 		ArrayList<ScheduleData> classList = getClassList(searchString);
 		if (classList.size() > 0) {
 			makeClassDataTable(classList);
 			makeWeeklyScheduleTable(classList);
 		} else {
 			Toast.makeText(this, getResources().getString(R.string.noClasses), Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void makeWeeklyScheduleTable(ArrayList<ScheduleData> classList) {
 		String[] days = { "M", "T", "W", "R", "F" };
 		for (String day : days) {
 			TableRow currentRow = new TableRow(this);
 			if (day == "M") {
 				currentRow = (TableRow) findViewById(R.id.mondayRow);
 			} else if (day == "T") {
 				currentRow = (TableRow) findViewById(R.id.tuesdayRow);
 			} else if (day == "W") {
 				currentRow = (TableRow) findViewById(R.id.wednesdayRow);
 			} else if (day == "R") {
 				currentRow = (TableRow) findViewById(R.id.thursdayRow);
 			} else if (day == "F") {
 				currentRow = (TableRow) findViewById(R.id.fridayRow);
 			}
 
 			for (int period = 1; period <= 10; period++) {
 				TextView textView = getConfiguredTextView("");
 				for (ScheduleData eachClass : classList) {
 					if (eachClass.MeetsOn(day) && eachClass.MeetingDuringPeriod(period)) {
 						textView.setText(eachClass.classNumber);
 					}
 				}
 				currentRow.addView(textView);
 			}
 		}
 	}
 
 	private ArrayList<ScheduleData> getClassList(String username) {
 		if (isOnline()) {
 			HttpClient client = new DefaultHttpClient();
 			HttpPost post = new HttpPost(getString(R.string.serverURL) + getString(R.string.scheduleSearchURL));
 
 			// Search Parameters
 			String fieldName = getString(R.string.fieldNameLookup);
 			String quarterName = getString(R.string.quarterNameLookup);
 			String quarterSelected = getString(R.string.quarterSelectionLookup);
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 			pairs.add(new BasicNameValuePair(fieldName, username));
 			pairs.add(new BasicNameValuePair(quarterName, quarterSelected));
 
 			try {
 				post.setEntity(new UrlEncodedFormEntity(pairs));
 				HttpResponse response = client.execute(post);
 				HttpEntity resultEntity = response.getEntity();
 				String results = EntityUtils.toString(resultEntity);
 				SAXParserFactory spf = SAXParserFactory.newInstance();
 				SAXParser sp = spf.newSAXParser();
 				XMLReader xr = sp.getXMLReader();
 				ScheduleHandler scheduleHandler = new ScheduleHandler();
 				xr.setContentHandler(scheduleHandler);
 				xr.parse(new InputSource(new StringReader(results)));
 				return scheduleHandler.getClassList();
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 			} catch (SAXException e) {
 				Toast.makeText(this, "Parser Error", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			} catch (MalformedURLException e) {
 				Toast.makeText(this, "Error Fetching Menu", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			} catch (IOException e) {
 				Toast.makeText(this, "I/O Exception!", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			} catch (NullPointerException e) {
 				Toast.makeText(this, "Null Pointer Exception!", Toast.LENGTH_SHORT).show();
 			}
 		}
 		return null;
 	}
 
 	private void clearTable() {
 		TableRow mondayRow = (TableRow) findViewById(R.id.mondayRow);
 		mondayRow.removeAllViews();
 		TableRow fridayRow = (TableRow) findViewById(R.id.fridayRow);
 		fridayRow.removeAllViews();
 		TableRow thursdayRow = (TableRow) findViewById(R.id.thursdayRow);
 		thursdayRow.removeAllViews();
 		TableRow wednesdayRow = (TableRow) findViewById(R.id.wednesdayRow);
 		wednesdayRow.removeAllViews();
 		TableRow tuesdayRow = (TableRow) findViewById(R.id.tuesdayRow);
 		tuesdayRow.removeAllViews();
 		TableLayout classDataTable = (TableLayout) findViewById(R.id.classDataTable);
 		classDataTable.removeAllViews();
 		setContentView(R.layout.schedule_lookup);
 		makeButtonWork();
 	}
 
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		return cm.getActiveNetworkInfo().isConnectedOrConnecting();
 	}
 }
