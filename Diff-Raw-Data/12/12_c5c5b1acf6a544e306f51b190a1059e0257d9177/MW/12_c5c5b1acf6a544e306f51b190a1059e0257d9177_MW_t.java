 package uk.co.mentalspace.android.utils;
 
 public interface MW {
 
 	public static final int METAWATCH_WIDTH = 96;
 	public static final int METAWATCH_HEIGHT = 96;
 	
 	//intent actions sent out by MWM-CE
 	public static final String ACTION_DISCOVERY = "org.metawatch.manager.APPLICATION_DISCOVERY";
 	public static final String ACTION_ACTIVATE = "org.metawatch.manager.APPLICATION_ACTIVATE";
 	public static final String ACTION_DEACTIVATE = "org.metawatch.manager.APPLICATION_DEACTIVATE";
 	public static final String ACTION_BUTTON_PRESS = "org.metawatch.manager.BUTTON_PRESS";
 	
 	//intents received by MWM-CE
 	public static final String ACTION_UPDATE = "org.metawatch.manager.APPLICATION_UPDATE";
	public static final String ACTION_ANNOUNCE = "org.metawatch.manager.APPLICATION_ANNOUNCE";
 	public static final String ACTION_START = "org.metawarch.manager.APPLICATION_START";
 	public static final String ACTION_STOP = "org.metawatch.manager.APPLICATION_STOP";
 	public static final String ACTION_NOTIFICATION = "org.metawatch.manager.NOTIFICATION";
 	public static final String ACTION_VIBRATE = "org.metawatch.manager.VIBRATE";
 	public static final String ACTION_SILENT_MODE = "org.metawatch.manager.SILENTMODE";
 	public static final String ACTION_WIDGET_UPDATE = "org.metawatch.manager.WIDGET_UPDATE";
 	
 	//extra params for the various intents - see docs to determine which are required
 	public static final String EXTRA_APP_ID = "id";
 	public static final String EXTRA_BUTTON_ID = "button";
 	public static final String EXTRA_BUTTON_TYPE = "type";
 	public static final String EXTRA_INT_ARRAY = "array";
 	public static final String EXTRA_APP_NAME = "name";
 
 	//notification extras
 	public static final String EXTRA_OLED1 = "oled1";
 	public static final String EXTRA_OLED1a = "oled1a";
 	public static final String EXTRA_OLED1b = "oled1b";
 	public static final String EXTRA_OLED2 = "oled2";
 	public static final String EXTRA_OLED2a = "oled2a";
 	public static final String EXTRA_OLED2b = "oled2b";
 	public static final String EXTRA_TEXT = "text";
 	public static final String EXTRA_TITLE = "title";
 	public static final String EXTRA_ICON = "icon";
 	public static final String EXTRA_STICKY = "sticky";
 	public static final String EXTRA_BYTE_ARRAY = "buffer";
 	
 	public static final String EXTRA_VIBRATE_ON = "vibrate_on";
 	public static final String EXTRA_VIBRATE_OFF = "vibrate_off";
	public static final String EXTRA_VIBRATE_CYCLES = "vibrate_cycles";
 	
 	public static final String EXTRA_SILENT_MODE_ENABLED = "enabled";
 	
 	public static final String EXTRA_WIDGET_ID = "id";
 	public static final String EXTRA_WIDGET_DESC = "desc";
 	public static final String EXTRA_WIDGET_WIDTH = "width";
 	public static final String EXTRA_WIDGET_HEIGHT = "height";
 	public static final String EXTRA_WIDGET_PRIORITY = "priority";

	public static final int BUTTON_TYPE_PRESS = 0;
	public static final int BUTTON_TYPE_NO_HOLD = 1;
	public static final int BUTTON_TYPE_SHORT_HOLD = 2;
	public static final int BUTTON_TYPE_LONG_HOLD = 3;
	
 }
