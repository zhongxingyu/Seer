 package com.example.helloandroid.provider.contract;
 
 import android.net.Uri;
 import android.provider.BaseColumns;
 
import com.example.helloandroid.provider.WidgetConfigurationProvider;
 
public class WidgetConfiguration {
 	public static final String TABLE_NAME = "widget_configuration";
 
     public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.widget.configuration";
     public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.widget.configuration";
     
     public static final Uri CONTENT_URI = Uri.parse("content://" + WidgetConfigurationProvider.AUTHORITY + "/" + TABLE_NAME);
     
 	// Database contract for widget configuration content
 	
 	// NOTE: Widget ID is not the primary key since widgets can have more
 	// than one configuration with different time boxes.
 
 	public static final String _ID = BaseColumns._ID;
 	public static final String WIDGET_ID = "widget_id";
 	public static final String AGENCY = "agency";
 	public static final String ROUTE = "route";
 	public static final String DIRECTION = "direction";
 	public static final String STOP = "stop";
 	public static final String START_TIME = "start_time";
 	public static final String END_TIME = "end_time";
 	public static final String DAYS = "days";
 
 }
