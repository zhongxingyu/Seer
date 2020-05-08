 package in.grat.esd2;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 
 import nl.siegmann.epublib.domain.Book;
 import nl.siegmann.epublib.domain.Resource;
 import nl.siegmann.epublib.epub.EpubReader;
 
 public class EpubHelper {
 	private Book book; 
 	private Context context;
 	public String title = "";
 	public String year = "12";
 	public String lang = "E";
 
 	public enum Variation { 
		Original ("MM_'ES12'_.MMM", "00", ""),
		Italian ("0MM_'ES12'.MMM", "000", ""), // works for Italian, Portuguese 
		Polish ("MM_'ES12'_.MMM", "00", "Text/"), 
		Ga ("MM_'ES12'", "00", "");
 		
 		private SimpleDateFormat format;
 		private DecimalFormat zeros;
 		private int iZeros;
 		private String prefix;
 		private Variation(String format, String zeros, String prefix) {
 			this.format = new SimpleDateFormat(format);
 			this.iZeros = zeros.length();
 			this.zeros = new DecimalFormat(zeros);
 			this.prefix = prefix;
 		}
 		public String getHref(Date day, String type) {
 			int dayOfMonth, monthOfYear;
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(day);
 			String hrefDate = format.format(day).toUpperCase();
 			dayOfMonth=cal.get(Calendar.DAY_OF_MONTH);
 			monthOfYear=cal.get(Calendar.MONTH);
 			if (type.equalsIgnoreCase("document")) { 
 				hrefDate += (dayOfMonth==1) ? ".xhtml" : "-split"+dayOfMonth+".xhtml";
 			} else { 
 				hrefDate += (dayOfMonth==1) ? "-extracted.xhtml" : "-extracted"+dayOfMonth+".xhtml";
 			}
 			hrefDate = zeros.format(monthOfYear+4)+hrefDate.substring(iZeros);
 			hrefDate = prefix + hrefDate;
 
 			return hrefDate;
 		}
 	}
 
 	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
 
 
 	public EpubHelper(Book book, Context context) {
 		this.book = book;
 		this.context = context;
 		getBookInfo();
 	}
 	public EpubHelper(String filename, Context context) {
 		try {
 			this.book = (new EpubReader()).readEpub(new FileInputStream(filename));
 			this.context = context;
 			getBookInfo();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void getBookInfo() {
 		this.title = this.book.getMetadata().getFirstTitle();
 		Pattern pattern = Pattern.compile(".*\\(es(\\d{2})-(\\w+)\\)");
 		Matcher m = pattern.matcher(title);
 		if (m.matches()) { 
 			this.year = m.group(1);
 			this.lang = m.group(2);
 		}
 	}
 	public String getSpecificDate(String day, String type) {
 		Date d;
 		try {
 			d = dateFormat.parse(day);
 			return getSpecificDate(d, type);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} 
 		return null;
 	}
 	
 	public String  getSpecificDate(Date day, String type) {
 		Resource r = null;
 		for (Variation v: Variation.values()) { 
 			r = this.book.getResources().getByHref(v.getHref(day, type));
 			if (r!=null) { 
 				try {
 					return new String( r.getData(), "UTF-8" );
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					return "";
 				} 
 			} 
 		}
 		return "";
 	}
 	
 	
 	public Bundle exportToDB() {
 		Bundle bundle;
 		SQLiteDatabase db;
 		DBHelper dbHelper;		
 		dbHelper = new DBHelper(this.context);
 		db = dbHelper.getWritableDatabase();
 		String Year = "20"+this.year;
 		
 		// clear old entries for the same information
 		dbHelper.removeEntriesFor(Year, lang);
 		
 		Date first;
 		try {
 			first = dateFormat.parse(Year+"-01-01");
 			Date last = dateFormat.parse(Year+"-12-31");
 			
 			Iterator<Date> i = new DateIterator(first, last);
 	    	while(i.hasNext()) { 
 	    		Date d = i.next();
 	    		ContentValues cv=new ContentValues();
 	    		cv.put(DBHelper.KEY_DATE, dateFormat.format(d));
 	    		cv.put(DBHelper.KEY_TEXT, getSpecificDate(d, "document"));
 	    		cv.put(DBHelper.KEY_REFS, getSpecificDate(d, "reference"));
 	    		cv.put(DBHelper.KEY_LANG, this.lang.toLowerCase());
 	    		Log.d("ESD", "Exporting: "+displayFormat.format(d));
 	    		db.insert(DBHelper.TABLE_NAME, null, cv);
 	    	}
 	    	db.close();
 	    	bundle = new Bundle();
 	    	bundle.putString("langId", lang);
 	    	bundle.putString("year", Year);
 	    	bundle.putString("lang", dbHelper.getLanguageById(lang));
 	    	return bundle;
 		} catch (ParseException e) {
 			return null;
 		}
 		
 	}
 }
