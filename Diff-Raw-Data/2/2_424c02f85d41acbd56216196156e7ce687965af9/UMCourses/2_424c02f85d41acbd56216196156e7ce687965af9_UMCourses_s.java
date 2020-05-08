 package org.umece.android.umaine;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class UMCourses extends Activity {
 	public static UMCourses getActivity() {
 		return me;
 	}
 	
 	/*
 	 * TODO: Need to do a lot more error checking for instance when they try to
 	 * add a course with incomplete information
 	 */
 	/* 
 	 * TODO: Do more error checking if they select a department and it 
 	 * returns and empty set from the database.
 	 */
 
 	/* PHP script */
 	/* TODO: Change that back to sample.php */
 	private static final String SERVER_SCRIPT = "http://with.eece.maine.edu/sample.php";
 
 	/* Post Values for data to be queried */
 	private static final String POST_SEMESTERS = "semesters";
 	private static final String POST_DEPARTS = "departments";
 	private static final String POST_COURSENUM = "coursenums";
 	private static final String POST_SECTIONS = "sections";
 	private static final String POST_ADDCOURSE = "addcourse";
 	@SuppressWarnings("unused")
 	private static final String POST_TEXTBOOK = "textbook";
 
 	/* Dialog Types */
 	private static final int DIALOG_ADD_COURSE = 0;
 	private static final int DIALOG_SELECT_SEMESTER = 1;
 	private static final int DIALOG_DELETE_SEMESTER = 2;
 
 	public ArrayAdapter<CharSequence> semesteradapter;
 	public ArrayAdapter<String> departadapter;
 	public ArrayAdapter<CharSequence> coursenumadapter;
 	public ArrayAdapter<CharSequence> sectionadapter;
 	public ArrayAdapter<Course> courselistadapter;
 
 	private static UMCourses me;
 
 	private Semester semester;
 
 	private AutoCompleteTextView departac;
 
 	private Spinner coursenumspin;
 
 	private Spinner sectionspin;
 
 	public LayoutInflater mInflater;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.courses);
 
 		/*
 		 * Create the adapters for the spinners and leave the courselist adapter
 		 * as null so we know it needs to be created when the first course is
 		 * added
 		 */
 		semesteradapter = new ArrayAdapter<CharSequence>(this,
 				android.R.layout.select_dialog_singlechoice);
 		String[] departs = getResources().getStringArray(R.array.departments);
 		departadapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, departs);
 		coursenumadapter = new ArrayAdapter<CharSequence>(this,
 				android.R.layout.simple_spinner_item);
 		coursenumadapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		sectionadapter = new ArrayAdapter<CharSequence>(this,
 				android.R.layout.simple_spinner_item);
 		sectionadapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		ListView lv = (ListView) findViewById(R.id.courseslist);
 		courselistadapter = new ArrayAdapter<Course>(this,
 				R.layout.list_item) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 				View row;
 				
 				if (convertView == null) {
 					row = mInflater.inflate(R.layout.list_item, parent, false);
 					((TextView)row.findViewById(R.id.listtextview)).setText(getItem(position).getString());
 					((DotDrawable)row.findViewById(R.id.view1)).setCourse(getItem(position));
 				} else {
 					row = convertView;
 					((TextView)row.findViewById(R.id.listtextview)).setText(getItem(position).getString());
 					((DotDrawable)row.findViewById(R.id.view1)).setCourse(getItem(position));
 				}
 				
 				return row;
 			}
 		};
 		lv.setAdapter(courselistadapter);
 		registerForContextMenu(lv);
 		lv.setOnItemClickListener(new OnItemClickListener(){
 
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				Intent myIntent = new Intent(arg1.getContext(), UMCourseDetails.class);
 				myIntent.putExtra("selectedindex", arg2);
 				startActivity(myIntent);
 			}
 		});
 
 		me = this;
 
 		/* Show the dialog to select a semester */
 		showDialog(DIALOG_SELECT_SEMESTER);
 	}
 
 	/* Create a context menu on long presses on the course list */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.delete_course_conmenu, menu);
 	}
 
 	/* Remove the selected item from the course listview */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.delete:
 			try {
 				semester.remCourse(courselistadapter.getItem((int) info.id));
 			} catch (IOException e) {
 				throw new RuntimeException();
 			}
 			courselistadapter.remove(courselistadapter.getItem((int) info.id));
 			if(courselistadapter.isEmpty()){
 				((TextView) findViewById(R.id.courselist_directions)).setVisibility(View.VISIBLE);
 			}
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	/* Create the options menu at the bottom of the screen */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.courses_menu, menu);
 		return true;
 	}
 
 	/*
 	 * What should be done when the options menu items are selected Currently
 	 * only the add course button is implemented
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		/* An item was selected from the options menu */
 		switch (item.getItemId()) {
 		case R.id.addcourse:
 			showDialog(DIALOG_ADD_COURSE);
 			return true;
 		case R.id.change_semester:
 			showDialog(DIALOG_SELECT_SEMESTER);
 			return true;
 		case R.id.delete_semester:
 			showDialog(DIALOG_DELETE_SEMESTER);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		switch (id) {
 		case DIALOG_ADD_COURSE:
 			/*
 			 * The layout needs to be inflated because the dialog has not been
 			 * created yet without inflating it, the program will crash when
 			 * setting the adapter or retrieving any item from the dialog layout
 			 */
 			LayoutInflater inflater = (LayoutInflater) this
 					.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layout = inflater.inflate(R.layout.addcoursedialog,
 					(ViewGroup) findViewById(R.id.addcourseroot));
 
 			/* Get references to the spinners */
 			departac = (AutoCompleteTextView) layout
 					.findViewById(R.id.depart_ac);
 			coursenumspin = (Spinner) layout.findViewById(R.id.coursenum_spin);
 			sectionspin = (Spinner) layout.findViewById(R.id.sectionspin);
 
 			OnClickListener listener = new AddListener();
 			/* Build the alert dialog for adding courses */
 			AlertDialog.Builder ad = new AlertDialog.Builder(this);
 			ad.setTitle("Add a course.");
 			ad.setPositiveButton("Add", listener);
 			ad.setNegativeButton("Cancel", null);
 			ad.setView(layout);
 			AlertDialog d = ad.create();
 
 			/* Departments Spinner */
 			departac.setAdapter(departadapter);
 			OnItemClickListener delistener = new AdapterView.OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					postCourseNum();
 					postSections();
 				}
 			};
 			departac.setOnItemClickListener(delistener);
 
 			OnItemSelectedListener celistener = new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parentView,
 						View selectedItemView, int position, long id) {
 					postSections();
 				}
 
 				public void onNothingSelected(AdapterView<?> arg0) {
 					// TODO Auto-generated method stub
 					
 				}
 			};
 			/* Course number spinner */
 			coursenumspin.setAdapter(coursenumadapter);
 			coursenumspin.setOnItemSelectedListener(celistener);
 
 			OnItemSelectedListener seclistener = new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parentView,
 						View selectedItemView, int position, long id) {
 				}
 
 				public void onNothingSelected(AdapterView<?> parentView) {
 				}
 			};
 
 			/* Section number spinner */
 			sectionspin.setAdapter(sectionadapter);
 			sectionspin.setOnItemSelectedListener(seclistener);
 
 			return d;
 		case DIALOG_SELECT_SEMESTER:
 			try {
 				postSemesters();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			OnClickListener blankListener = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 				}
 			};
 
 			Builder ret = new AlertDialog.Builder((Context) this);
 			ret.setTitle("Select a semester");
 			ret.setSingleChoiceItems(semesteradapter, 0, blankListener);
 			ret.setPositiveButton("OK", new SemListener());
 			ret.setNegativeButton("Cancel", blankListener);
 			return ret.create();
 		case DIALOG_DELETE_SEMESTER:
 
 			OnClickListener blankListener_2 = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 				}
 			};
 
 			OnClickListener sem_delete = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					UMCourses.getActivity().deleteSemester();
 				}
 			};
 
 			Builder ret_2 = new AlertDialog.Builder((Context) this);
 			ret_2.setTitle("Delete Semester " + semester.getSeason() + " " + semester.getYear() + "?");
 			ret_2.setPositiveButton("OK", sem_delete);
 			ret_2.setNegativeButton("Cancel", blankListener_2);
 			return ret_2.create();
 		}
 
 		return null;
 	}
 
 	protected void deleteSemester() {
 		semester.delete();
 		semester = null;
 		
 		showDialog(DIALOG_SELECT_SEMESTER);
 	}
 
 	/*
 	 * Create a spannable for the course to be added. should include bold text
 	 * for department, course number, and description
 	 */
 	private void addCourse(List<String> courseinfo) {
 		String dep, num, title, inst, sec, meetingTime, location, book, phone, email, office, departURL;
 		// Formatter formatter = new Formatter();
 
 		/* Should handle the case where there is no start time or end time */
 		dep = courseinfo.get(0);
 		num = courseinfo.get(1);
 		sec = courseinfo.get(2);
 		title = courseinfo.get(3);
 		meetingTime = courseinfo.get(4);
 		inst = courseinfo.get(5);
 		location = courseinfo.get(6);
 		phone = courseinfo.get(7);
 		email = courseinfo.get(8);
 		office = courseinfo.get(9);
 		departURL = courseinfo.get(10);
		book = courseinfo.get(12);
 
 		if (semester != null) {
 			try {
 				if(semester.getCourseCount() == 0) {
 					((TextView) findViewById(R.id.courselist_directions)).setVisibility(View.GONE);
 				}
 				semester.addCourse(new Course(dep, num, title, sec, "", meetingTime, location, inst, phone, email, office, departURL, book));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void postCourseNum() {
 		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
 		postParams.add(new BasicNameValuePair("field", POST_COURSENUM));
 		postParams.add(new BasicNameValuePair(POST_DEPARTS, getDepartSpin()));
 		postParams.add(new BasicNameValuePair(POST_SEMESTERS, (semester
 				.getYear() + semester.getSeason().toLowerCase())));
 		coursenumadapter.clear();
 		try {
 			for (String s : httpRequest(postParams)) {
 				coursenumadapter.add(s);
 			}
 		} catch (Exception e) {
 			throw new RuntimeException();
 		}
 		coursenumspin.setSelection(0);
 		//coursenumac.getSelectedItem().toString();
 	}
 
 	public void postSemesters() {
 		List<String> existing = new ArrayList<String>();
 		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
 		postParams.add(new BasicNameValuePair("field", POST_SEMESTERS));
 		semesteradapter.clear();
 		
 		File dir = getFilesDir();
 		if ((dir != null) && (dir.listFiles().length > 0)) {
 			for (File file : dir.listFiles()) {
 				try {
 					if (file.getName().contains(".sem")) {
 						Semester sem = new Semester(file.getName(), this);
 						existing.add(sem.getSeason() + " " + sem.getYear());
 						semesteradapter.add(sem.getSeason() + " " + sem.getYear());
 					}
 				} catch (Exception e) {
 				}
 			}
 		}
 		
 		try {
 			List<String> ret = httpRequest(postParams);
 			if (ret.get(0).equals(POST_SEMESTERS)) {
 				for (int i= 1; i < ret.size(); i++) {
 					if (!existing.contains(ret.get(i))) {
 						semesteradapter.add(ret.get(i));
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new RuntimeException();
 		}
 	}
 
 	public void postSections() {
 		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
 		postParams.add(new BasicNameValuePair("field", POST_SECTIONS));
 		postParams.add(new BasicNameValuePair(POST_DEPARTS, getDepartSpin()));
 		
 		/* Coursenum spin now has the title in it, we just want the coursenum here */
 		String[] sar = getCoursesSpin().split(" ", 2);
 		postParams
 				.add(new BasicNameValuePair(POST_COURSENUM, sar[0]));
 		postParams
 				.add(new BasicNameValuePair(POST_SEMESTERS, (semester.getYear()+ semester.getSeason().toLowerCase())));
 		sectionadapter.clear();
 		try {
 			for (String s : httpRequest(postParams)) {
 				sectionadapter.add(s);
 			}
 		} catch (Exception e) {
 			throw new RuntimeException();
 		}
 	}
 
 	public ArrayAdapter<CharSequence> getSemAdapter() {
 		return semesteradapter;
 	}
 	
 	public Semester getSemester(){
 		return semester;
 	}
 	
 	public ArrayAdapter<Course> getCourseListAdapter(){
 		return courselistadapter;
 	}
 
 	public void setSemester(String string) {
 		try {
 			String campus = "Orono";
 			String year = string.split(" ")[1];
 			String season = string.split(" ")[0];
 			semester = Semester.getSemester(campus, year, season, this);
 
 			if(semester.getCourseCount() != 0){
 				((TextView) findViewById(R.id.courselist_directions)).setVisibility(View.GONE);
 			}
 			
 			TextView tv = (TextView) findViewById(R.id.courses_semester);
 			if (tv.getVisibility() == View.GONE) {
 				tv.setVisibility(View.VISIBLE);
 			}
 			ScheduleDrawable sd = (ScheduleDrawable)findViewById(R.id.scheduledraw);
 			semester.setScheduleDraw(sd);
 			sd.onChange();
 
 			tv.setText("Semester: " + semester.getSeason() + " "
 					+ semester.getYear() + " " + semester.getCampus());
 			
 			courselistadapter.clear();
 			for (Object course : semester.getCourses()) {
 				if (course instanceof Course) {
 					courselistadapter.add(((Course) course));
 				}
 			}
 //			ListView lv = (ListView)findViewById(R.id.courseslist);
 //			View v = lv.getChildAt(1);
 //			v.setBackgroundResource(0xffff0000);
 		} catch (IOException e1) {
 			throw new RuntimeException();
 		}
 		
 		if (coursenumadapter != null) {
 			coursenumadapter.clear();
 		}
 		if (departac != null) {
 			departac.clearComposingText();
 		}
 		if (sectionadapter != null) {
 			sectionadapter.clear();
 		}
 	}
 
 	public String getDepartSpin() {
 		return departac.getText().toString();
 	}
 
 	public String getCoursesSpin() {
 		return coursenumspin.getSelectedItem().toString();
 	}
 
 	public String getSectionSpin() {
 		return sectionspin.getSelectedItem().toString();
 	}
 
 	public void addCourse(String department, String coursenum, String section) {
 		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
 		ArrayList<String> ci = new ArrayList<String>();
 		
 		/* Coursenum spin now has the title in it, we just want the coursenum here */
 		String[] sar = getCoursesSpin().split(" ", 2);
 		String cn = sar[0];
 		
 		/* Section Spinner now has the course type in it so separate that part out */
 		String secNum = getSectionSpin().replaceAll("\\s+(\\S+)", "");
 		
 		/*
 		 * If adding a course we need to know department, course number, and
 		 * section
 		 */
 		postParams.add(new BasicNameValuePair("field", POST_ADDCOURSE));
 		postParams.add(new BasicNameValuePair(POST_DEPARTS, getDepartSpin()));
 		postParams
 				.add(new BasicNameValuePair(POST_COURSENUM, cn));
 		postParams.add(new BasicNameValuePair(POST_SECTIONS, secNum));
 		postParams
 				.add(new BasicNameValuePair(POST_SEMESTERS, (semester.getYear()+ semester.getSeason().toLowerCase())));
 
 		/* Insert the info we already know */
 		ci.add(getDepartSpin());
 		ci.add(cn);
 		ci.add(getSectionSpin());
 
 		/* Add the new course Spannable to the list view adapter */
 		courselistadapter.clear();
 		try {
 			List<String> temp = httpRequest(postParams);
 			//for (String s : httpRequest(postParams)) {
 			for(String s: temp){
 				if(s.startsWith("Error:")){
 					s = null;
 				}
 				ci.add(s);
 			}
 
 			addCourse(ci);
 			if ((semester != null) && (semester.getCourses() != null)
 					&& (semester.getCourses().length > 0)) {
 				for (Course course : semester.getCourses()) {
 					courselistadapter.add(course);
 				}
 			}
 		} catch (Exception e) {
 			throw new RuntimeException();
 		}
 	}
 
 	public static List<String> httpRequest(List<? extends NameValuePair> postParams)
 			throws Exception {
 		HttpClient client = new DefaultHttpClient();
 		HttpPost request = new HttpPost(SERVER_SCRIPT);
 		List<String> ret = new ArrayList<String>();
 
 		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParams);
 
 		request.setEntity(formEntity);
 		HttpResponse response = client.execute(request);
 		BufferedReader in = new BufferedReader(new InputStreamReader(response
 				.getEntity().getContent()));
 
 		String line = "";
 		while ((line = in.readLine()) != null) {
 			ret.add(line);
 		}
 
 		in.close();
 
 		return ret;
 	}
 
 	public void removeCourse(int selIndex) {
 		try {
 			semester.remCourse(courselistadapter.getItem(selIndex));
 		} catch (IOException e) {
 			throw new RuntimeException();
 		}
 		courselistadapter.remove(courselistadapter.getItem(selIndex));
 		if(courselistadapter.isEmpty()){
 			((TextView) findViewById(R.id.courselist_directions)).setVisibility(View.VISIBLE);
 		}
 	}
 }
