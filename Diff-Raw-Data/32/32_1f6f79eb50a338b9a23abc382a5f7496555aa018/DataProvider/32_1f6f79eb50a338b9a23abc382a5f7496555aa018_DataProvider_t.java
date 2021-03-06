 package cz.quinix.condroid.database;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 import cz.quinix.condroid.model.Annotation;
 import cz.quinix.condroid.model.Convention;
 import cz.quinix.condroid.model.ProgramLine;
 import cz.quinix.condroid.ui.WelcomeActivity;
 
 public class DataProvider {
 	
 	public static String AUTHORITY = "cz.quinix.condroid.database.DataProvider";
 	public static Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/database");
 	public static int ITEMS_PER_PAGE = 40;
 		
 	
 	private CondroidDatabase mDatabase;
 	private Convention con;
 	private static volatile DataProvider instance;
 	
 	private static HashMap<Integer, String> programLines = null;
 	
 	private DataProvider(Context context) {
 		mDatabase = new CondroidDatabase(context);
 	}
 	
 	public static DataProvider getInstance(Context context) {
 		if(instance == null) {
 			synchronized (CondroidDatabase.class) {
 				if(instance == null) {
 					instance = new DataProvider(context);
 				}
 			}
 		}
 		return instance;
 	}
 	
 	public boolean hasData() {
 		return !mDatabase.isEmpty();
 	}
 
 	public void setConvention(Convention convention) {
 		con = convention;		
 	}
 
 	public void insert(List<Annotation> result) {
 		if(!mDatabase.isEmpty()) {
 			mDatabase.purge();
 			programLines = null;
 		}
 		try {
 			mDatabase.insert(con, result);
 		} catch (Exception ex) {
 			Log.w(WelcomeActivity.TAG, ex);
 			mDatabase.purge();
 		}
 	}
 
 	public List<Annotation> getAnnotations(String condition, int page) {
 		List<Annotation> ret = new ArrayList<Annotation>();
 		
 		
 		Cursor c = this.mDatabase.query(CondroidDatabase.ANNOTATION_TABLE, null, condition, null, "startTime ASC, lid ASC, title ASC", (page*ITEMS_PER_PAGE) + ","+ ITEMS_PER_PAGE);
 		
 		while(c.moveToNext()) {
 			
 			
 			ret.add(readAnnotation(c));
 		}
 		return ret;
 	}
 	
 	public ProgramLine getProgramLine (int lid) {
 		ProgramLine pl = new ProgramLine();
 		
 		if(programLines == null) {
 			this.loadProgramLines();
 		}
 		if(programLines.containsKey(lid)) {
 			pl.setLid(lid);
 			pl.setName(programLines.get(lid));
 		}
 		
 		return pl;
 	}
 
 	public HashMap<Integer, String> getProgramLines() {
 		if(programLines == null) {
 			this.loadProgramLines();
 		}
 		return programLines;
 	}
 	
 	private void loadProgramLines() {
 		programLines = new HashMap<Integer, String>();
 		
 		Cursor c = this.mDatabase.query(CondroidDatabase.LINE_TABLE, null, null, null, "title ASC", null);
 		while(c.moveToNext()) {
 			programLines.put(c.getInt(c.getColumnIndex("id")), c.getString(c.getColumnIndex("title")));
 		}
 	}
 
 	public List<Date> getDates() {
 		
 		Cursor c = this.mDatabase.query("SELECT DISTINCT STRFTIME('%Y-%m-%d',startTime) AS sDate FROM "+CondroidDatabase.ANNOTATION_TABLE+" ORDER by STRFTIME('%Y-%m-%d',startTime) ASC");
 		
 		List<Date> map = new ArrayList<Date>();
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 		while(c.moveToNext()) {
 			try {
 				map.add(df.parse(c.getString(c.getColumnIndex("sDate"))));
 			} catch (ParseException e) {
 				Log.w("DB", e);
 			}
 		}
 		
 		return map;
 	}
 
 	
 	public List<Annotation> getRunningAndNext() {
 		List<Annotation> l = new ArrayList<Annotation>();
 		
		Cursor c = this.mDatabase.query(CondroidDatabase.ANNOTATION_TABLE, null, "startTime < DATETIME('now','localtime') AND endTime > DATETIME('now','localtime')", null, "startTime DESC", null, false, null);
 		while (c.moveToNext()) {
 			if(c.isFirst()) {
 				Annotation a = new Annotation();
 				a.setTitle("break");
 				a.setStartTime(c.getString(c.getColumnIndex("startTime")));
 				a.setAnnotation("now");
 				l.add(a);
 			}
 			
 			Annotation annotation = readAnnotation(c);
 			
 			l.add(annotation);
 			
 		}
 		
		Cursor c2 = this.mDatabase.query(CondroidDatabase.ANNOTATION_TABLE, null, "startTime > DATETIME('now','localtime')", null, "startTime ASC, lid ASC", "0,100", false, null);
 		String previous = "";
 		int hours = 0;
 		while (c2.moveToNext()) {
 			if (!previous.equals(c2.getString(c2.getColumnIndex("startTime")))) {
 				if(hours++ > 5) break;
 				Annotation a = new Annotation();
 				a.setTitle("break");
 				a.setStartTime(c2.getString(c2.getColumnIndex("startTime")));
 				l.add(a);
 				previous = c2.getString(c2.getColumnIndex("startTime"));
 			}
 			
 			Annotation annotation = readAnnotation(c2);
 
 			l.add(annotation);
 			
 		}
 		
 		return l;
 	}
 	
 	private Annotation readAnnotation(Cursor c) {
 		Annotation annotation = new Annotation();
 		annotation.setPid(c.getString(c.getColumnIndex("pid")));
 		annotation.setTitle(c.getString(c.getColumnIndex("title")));
 		annotation.setAnnotation(c.getString(c.getColumnIndex("annotation")));
 		annotation.setAuthor(c.getString(c.getColumnIndex("talker")));
 		annotation.setEndTime(c.getString(c.getColumnIndex("endTime")));
 		annotation.setLength(c.getString(c.getColumnIndex("length")));
 		annotation.setLid(c.getInt(c.getColumnIndex("lid")));
 		annotation.setStartTime(c.getString(c.getColumnIndex("startTime")));
 		annotation.setType(c.getString(c.getColumnIndex("type")));
 		return annotation;
 	}
 
 	public Convention getCon() {
 		if (this.con != null) {
 			return con;
 		}
 		Cursor c = this.mDatabase.query(CondroidDatabase.CON_TABLE, null, null, null, null, null);
 		Convention co = new Convention(); 
 		while (c.moveToNext()) {
 			co.setCid(c.getInt(c.getColumnIndex("id")));
 			co.setDataUrl(c.getString(c.getColumnIndex("dataUrl")));
 			co.setDate(c.getString(c.getColumnIndex("date")));
 			co.setIconUrl(c.getString(c.getColumnIndex("iconUrl")));
 			co.setName(c.getString(c.getColumnIndex("name")));
 			co.setMessage(c.getString(c.getColumnIndex("message")));			
 		}
 		this.con = co;
 		return co;
 	}
 	
 	
 
 	
 	
 }
