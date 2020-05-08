 /**
  * 
  */
 package com.rushdevo.glucotracker;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.rushdevo.glucotracker.data.GlucoseRecord;
 
 /**
  * @author jasonrush
  *
  */
 public class GlucoseRecordView extends LinearLayout {
 
 	private GlucoseRecord record;
 	private TextView bloodSugarView;
 	private TextView dateView;
 	private int dip;
 	SimpleDateFormat parser;
 	SimpleDateFormat formatter;
 	
 	public GlucoseRecordView(Context context, GlucoseRecord record) {
 		super(context);
 		parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		formatter = new SimpleDateFormat("M/d/yyyy  h:mm a");
 		this.setOrientation(HORIZONTAL);
 		this.setRecord(record);
 		this.dip = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)1, getResources().getDisplayMetrics());
 		buildSubViews(context);
 	}
 	
 	//////////GETTERS AND SETTERS//////////////
 	public void setRecord(GlucoseRecord record) {
 		this.record = record;
 		invalidateRecordText();
		setColor();
 	}
 
 	public GlucoseRecord getRecord() {
 		return record;
 	}
 	
 	/////////////HELPERS///////////////////////
 	private void buildSubViews(Context context) {
 		int sidePadding = 10*dip;
 		int topPadding = 5*dip;
 		int width = 80*dip;
 		
 		bloodSugarView = new TextView(context);
 		bloodSugarView.setPadding(sidePadding, topPadding, sidePadding, topPadding);
 		bloodSugarView.setGravity(Gravity.RIGHT);
 		
 		dateView = new TextView(context);
 		dateView.setPadding(sidePadding, topPadding, sidePadding, topPadding);
 		
 		invalidateRecordText();
 		
 		addView(bloodSugarView, new LinearLayout.LayoutParams(width, LayoutParams.FILL_PARENT));
 		addView(dateView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
 	}
 	
 	private String formattedTimestamp(String date, String time) {
 		String formatted;
 		try {
 			formatted = formatter.format(parser.parse(date + " " + time));
 		} catch (ParseException e) {
 			formatted = "";
 		}
 		return formatted;
 	}
 	
 	private void invalidateRecordText() {
 		if (bloodSugarView != null) bloodSugarView.setText(this.record.getBloodSugar().toString());
 		if (dateView != null) dateView.setText(formattedTimestamp(record.getBloodSugarDate(), record.getBloodSugarTime()));
 	}
	
	private void setColor() {
		if (this.record.shouldFlag()) {
			setBackgroundColor(Color.RED);
		} else {
			setBackgroundColor(Color.BLACK);
		}
	}
 }
