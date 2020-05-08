 package se.kjellstrand.colorclock.service;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.widget.RemoteViews;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import se.kjellstrand.colorclock.R;
 import se.kjellstrand.colorclock.activity.SettingsActivity;
 import se.kjellstrand.colorclock.provider.ClockAppWidgetProvider;
 import se.kjellstrand.colorclock.util.ColorUtil;
 
 import static android.os.Build.VERSION_CODES.JELLY_BEAN;
 import static se.kjellstrand.colorclock.util.RemoteViewUtils.getRemoteViews;
 
 /**
  * A service that updates the clock widget whenever the updateAllViews method is
  * called.
  */
 public class ClockService extends IntentService {
 
     /**
      * Tag for logging
      */
     private static final String TAG = ClockService.class.getCanonicalName();
 
     /**
      * String used to identify updates sent from the alarm.
      */
     public static final String ACTION_UPDATE = "se.kjellstrand.colorclock.ACTION_UPDATE";
 
     /**
      * List of the ids on the digit views.
      */
     private final static int[] DIGIT_VIEWS_INDEX = new int[]{
             R.id.digit_0, R.id.digit_1, R.id.digit_2, R.id.digit_3, R.id.digit_4, R.id.digit_5, R.id.digit_6,
             R.id.digit_7, R.id.digit_8, R.id.digit_9
     };
 
     /**
      * Code used to identify a request.
      */
     private static final int REQUEST_CODE = 760315;
 
     /**
      * Holds the current colors of each digit, used while calculating the color
      * state of the clock in each update.
      */
     private static final int[] DIGITS_COLOR = new int[10];
 
     /**
      * A map from name of layout to the R.array id containing the actual layout.
      */
     private static Map<String, Integer> sLayoutReversLookupMap;
 
     /**
      * A map from name of charsets to the R.array id containing the actual
      * digits of the charset.
      */
     private static Map<String, Integer> sCharsetReversLookupMap;
 
     /**
      * A map from name of blendMode to the R.array id containing the actual blend mode.
      */
     private static Map<String, Integer> sBlendModeReversLookupMap;
 
     /**
      * Name of the class + package, used to get the list of id's for the
      * instances of this widget.
      */
     private static ComponentName sComponentName = null;
 
     /**
      * If sSettingsChanged is true, we should re-read the settings on next
      * update of the clock. If sSettingsChanged is false, do nothing special.
      * Init to true to force a read on first run.
      */
    private static Boolean sSettingsChanged = null;
 
     /**
      * Determines how strong the secondary color is, the color showing (shown in
      * caps) hH:mM:sS.
      */
     private static double sSecondaryColorStrength = 0.7d;
 
     /**
      * Major color for hours, displayed on the first digit of the hours. So if
      * the clock is 12:34:56 then the 1 would get this color as background
      * color.
      */
     private static int sPrimaryHourColor = 0;
 
     /**
      * Minor color for hours, displayed on the second digit of the hours. So if
      * the clock is 12:34:56 then the 2 would get this color as background
      * color.
      */
     private static int sSecondaryHourColor = 0;
 
     /**
      * Major color for minutes, displayed on the first digit of the minutes. So
      * if the clock is 12:34:56 then the 3 would get this color as background
      * color.
      */
     private static int sPrimaryMinuteColor = 0;
 
     /**
      * Minor color for minutes, displayed on the second digit of the minutes. So
      * if the clock is 12:34:56 then the 4 would get this color as background
      * color.
      */
     private static int sSecondaryMinuteColor = 0;
 
     /**
      * Major color for seconds, displayed on the first digit of the seconds. So
      * if the clock is 12:34:56 then the 5 would get this color as background
      * color.
      */
     private static int sPrimarySecondColor = 0;
 
     /**
      * Minor color for seconds, displayed on the second digit of the seconds. So
      * if the clock is 12:34:56 then the 6 would get this color as background
      * color.
      */
     private static int sSecondarySecondColor = 0;
 
     /**
      * What color will digits have.
      */
     private static int sDefaultDigitColor = 0;
 
     /**
      * What color will digits without a specific background set get, starts
      * uninitialised.
      */
     private static int sDefaultBackgrundColor = 0;
 
     /**
      * Defines how the colors will be blended.
      */
     private static int sBlendMode = R.string.screen_blend;
 
     /**
      * The size of the digits font.
      */
     private static float sDigitSize;
 
     /**
      * The layout id used to create a layout for the RemoteView
      */
     private static int sLayoutID = -1;
 
     /**
      * Manager of this widget.
      */
     private AppWidgetManager mManager = null;
 
     /**
      * Constructor
      */
     public ClockService() {
         super(TAG);
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         //sLayoutID = sharedPreferences.getInt(R.integer.pref_layouts_default, R.layout.color_clock_10x1);
 
         if (sLayoutReversLookupMap == null) {
             sLayoutReversLookupMap = new HashMap<String, Integer>();
             sLayoutReversLookupMap.put(getResources().getString(R.string.color_clock_2x5_layout), R.layout.color_clock_2x5);
             sLayoutReversLookupMap.put(getResources().getString(R.string.color_clock_5x2_layout), R.layout.color_clock_5x2);
             sLayoutReversLookupMap.put(getResources().getString(R.string.color_clock_3x4_layout), R.layout.color_clock_3x4);
             sLayoutReversLookupMap.put(getResources().getString(R.string.color_clock_4x3_layout), R.layout.color_clock_4x3);
             sLayoutReversLookupMap.put(getResources().getString(R.string.color_clock_1x10_layout), R.layout.color_clock_1x10);
             sLayoutReversLookupMap.put(getResources().getString(R.string.color_clock_10x1_layout), R.layout.color_clock_10x1);
             sLayoutReversLookupMap.put(getResources().getString(R.string.color_clock_3x3_layout), R.layout.color_clock_3x3);
         }
 
         if (sLayoutID == -1) {
             sLayoutID = sLayoutReversLookupMap.get(getResources().getString(R.string.pref_layouts_default));
         }
 
         if (sCharsetReversLookupMap == null) {
             sCharsetReversLookupMap = new HashMap<String, Integer>();
             sCharsetReversLookupMap.put(getResources().getString(R.string.latin_charset), R.array.latin_digits);
             sCharsetReversLookupMap.put(getResources().getString(R.string.arabic_charset), R.array.arabic_digits);
             sCharsetReversLookupMap.put(getResources().getString(R.string.chinese_charset), R.array.chinese_digits);
             sCharsetReversLookupMap.put(getResources().getString(R.string.hardmode_charset), R.array.hardmode_digits);
         }
 
         if (sBlendModeReversLookupMap == null) {
             sBlendModeReversLookupMap = new HashMap<String, Integer>();
             sBlendModeReversLookupMap.put(getResources().getString(R.string.screen_blend), R.string.screen_blend);
             sBlendModeReversLookupMap.put(getResources().getString(R.string.multiply_blend), R.string.multiply_blend);
             sBlendModeReversLookupMap.put(getResources().getString(R.string.average_blend), R.string.average_blend);
         }

        if (sSettingsChanged == null) {
            settingsChanged();
        }
     }
 
     /**
      * Call when the settings have changed to trigger a re-read of the shared
      * prefs / settings.
      */
     public static void settingsChanged() {
         sSettingsChanged = true;
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
 
         mManager = AppWidgetManager.getInstance(this);
 
         if (sComponentName == null) {
             sComponentName = new ComponentName(this, ClockAppWidgetProvider.class);
         }
 
         if (intent.getAction().equals(ACTION_UPDATE)) {
             Calendar now = Calendar.getInstance();
             updateAllViews(now);
         }
     }
 
     /**
      * Walk the list of clock widgets and update them, one by one.
      *
      * @param calendar the time used for the update.
      */
     @TargetApi(16)
     @SuppressLint("NewApi")
     private void updateAllViews(Calendar calendar) {
         if (sSettingsChanged) {
             updateDigitSizeFromSharedPrefs();
             updateLayoutIDFromSharedPrefs();
         }
 
         int[] appIds = mManager.getAppWidgetIds(sComponentName);
        int minHeight = 120;
        int minWidth = 120;
        float digitResizeFactor = 1.2f;
         if (Build.VERSION.SDK_INT >= JELLY_BEAN) {
             // See the dimensions and
             Bundle options = mManager.getAppWidgetOptions(appIds[0]);
 
             // Get min width and height.
             minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
             minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
         }
 
         RemoteViews remoteViews = getRemoteViews(this, minWidth, minHeight,
                 sLayoutID, sDigitSize * digitResizeFactor, DIGIT_VIEWS_INDEX);
         settingsChanged();
 
         updateView(remoteViews, calendar);
 
         Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent,
                 PendingIntent.FLAG_UPDATE_CURRENT);
         remoteViews.setOnClickPendingIntent(R.id.root_layout, pendingIntent);
 
         if (sSettingsChanged) {
             updateCharSetFromSharedPrefs(remoteViews);
             updateColorsFromSharedPrefs(remoteViews);
             updateBlendModeFromSharedPrefs();
             sSettingsChanged = false;
         }
 
         mManager.updateAppWidget(sComponentName, remoteViews);
     }
 
     /**
      * Sets the layout according to shared preferences.
      */
     private void updateLayoutIDFromSharedPrefs() {
         String prefLayoutKey = getResources().getString(R.string.pref_layouts_key);
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         String layoutLookupKey = sharedPreferences.getString(prefLayoutKey, null);
         Integer integerLayoutID = sLayoutReversLookupMap.get(layoutLookupKey);
         if (integerLayoutID != null) {
             sLayoutID = integerLayoutID;
         }
     }
 
     /**
      * Sets the digits size according to shared preferences.
      */
     private void updateDigitSizeFromSharedPrefs() {
         String prefDigitSizeKey = getResources().getString(R.string.pref_digit_size_key);
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sDigitSize = sharedPreferences.getFloat(prefDigitSizeKey, 0.2f);
     }
 
     /**
      * Sets the blend mode according to shared preferences.
      */
     private void updateBlendModeFromSharedPrefs() {
         String prefBlendKey = getResources().getString(R.string.pref_blends_key);
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         String blendLookupKey = sharedPreferences.getString(prefBlendKey, null);
         Integer integerBlend = sBlendModeReversLookupMap.get(blendLookupKey);
         if (integerBlend != null) {
             sBlendMode = integerBlend;
         }
     }
 
     /**
      * Sets the colors found in the shared prefs.
      */
     private void updateColorsFromSharedPrefs(RemoteViews remoteViews) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 
         sPrimaryHourColor = sharedPreferences.getInt(getResources().getString(R.string.pref_hour_color_key), getResources().getColor(R.color.default_hour_color));
         sPrimaryMinuteColor = sharedPreferences.getInt(getResources().getString(R.string.pref_minute_color_key), getResources().getColor(R.color.default_minute_color));
         sPrimarySecondColor = sharedPreferences.getInt(getResources().getString(R.string.pref_second_color_key), getResources().getColor(R.color.default_second_color));
 
         sSecondaryHourColor = ColorUtil.getSecondaryColorFromPrimaryColor(sPrimaryHourColor,
                 sSecondaryColorStrength);
         sSecondaryMinuteColor = ColorUtil.getSecondaryColorFromPrimaryColor(sPrimaryMinuteColor,
                 sSecondaryColorStrength);
         sSecondarySecondColor = ColorUtil.getSecondaryColorFromPrimaryColor(sPrimarySecondColor,
                 sSecondaryColorStrength);
 
         sDefaultBackgrundColor = sharedPreferences.getInt(getResources().getString(R.string.pref_background_color_key),
                 getResources().getColor(R.color.default_background_color));
         sDefaultDigitColor = sharedPreferences.getInt(getResources().getString(R.string.pref_digit_color_key),
                 getResources().getColor(R.color.default_digit_color));
         for (int aDIGIT_VIEWS_INDEX : DIGIT_VIEWS_INDEX) {
             remoteViews.setTextColor(aDIGIT_VIEWS_INDEX, sDefaultDigitColor);
         }
     }
 
     /**
      * Sets the charset found in the shared prefs to all remote views.
      *
      * @param remoteViews The views used by the widget to display time.
      */
     private void updateCharSetFromSharedPrefs(RemoteViews remoteViews) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         String newCharSet = sharedPreferences.getString(getResources().getString(R.string.pref_charsets_key),
                 null);
         Integer newCharSetId = sCharsetReversLookupMap.get(newCharSet);
         if (newCharSetId != null) {
             String[] chars = getResources().getStringArray(newCharSetId);
             for (int i = 0; i < DIGIT_VIEWS_INDEX.length; i++) {
                 remoteViews.setTextViewText(DIGIT_VIEWS_INDEX[i], chars[i]);
                 remoteViews.setTextColor(DIGIT_VIEWS_INDEX[i], sDefaultDigitColor);
             }
         }
     }
 
     /**
      * Updates the colors of the clock to a state representing "now".
      *
      * @param remoteViews The views used by the widget to display time.
      * @param calendar    Calendar used to display time.
      */
     public void updateView(RemoteViews remoteViews, Calendar calendar) {
 
         // Find out what boxes are 'active'
         int hoursX0 = calendar.get(Calendar.HOUR_OF_DAY) / 10;
         int hours0X = calendar.get(Calendar.HOUR_OF_DAY) % 10;
         int minutesX0 = calendar.get(Calendar.MINUTE) / 10;
         int minutes0X = calendar.get(Calendar.MINUTE) % 10;
         int secondsX0 = calendar.get(Calendar.SECOND) / 10;
         int seconds0X = calendar.get(Calendar.SECOND) % 10;
 
         // Reset all boxes to zero/black.
         for (int i = 0; i <= 9; i++) {
             DIGITS_COLOR[i] = 0;
         }
 
         // Update the color of the boxes with 'active' digits in them..
         DIGITS_COLOR[hoursX0] = setOrBlendDigitColorWithColor(DIGITS_COLOR[hoursX0], sPrimaryHourColor);
         DIGITS_COLOR[hours0X] = setOrBlendDigitColorWithColor(DIGITS_COLOR[hours0X], sSecondaryHourColor);
         DIGITS_COLOR[minutesX0] = setOrBlendDigitColorWithColor(DIGITS_COLOR[minutesX0], sPrimaryMinuteColor);
         DIGITS_COLOR[minutes0X] = setOrBlendDigitColorWithColor(DIGITS_COLOR[minutes0X], sSecondaryMinuteColor);
         DIGITS_COLOR[secondsX0] = setOrBlendDigitColorWithColor(DIGITS_COLOR[secondsX0], sPrimarySecondColor);
         DIGITS_COLOR[seconds0X] = setOrBlendDigitColorWithColor(DIGITS_COLOR[seconds0X], sSecondarySecondColor);
 
         // For boxes without a color, set the default background color.
         for (int i = 0; i <= 9; i++) {
             if (DIGITS_COLOR[i] == 0) {
                 DIGITS_COLOR[i] = sDefaultBackgrundColor;
             }
         }
 
         // Set the colors to the views.
         for (int i = 0; i <= 9; i++) {
             remoteViews.setInt(DIGIT_VIEWS_INDEX[i], "setBackgroundColor", DIGITS_COLOR[i]);
         }
     }
 
     /**
      * Blends the two colors unless the first color is pitch black with 0 alpha,
      * for that corner-case it will return the second color
      *
      * @param c1 first color.
      * @param c2 second color.
      * @return a blend of the two color, unless above stated condition applies.
      */
     private int setOrBlendDigitColorWithColor(int c1, int c2) {
         switch (sBlendMode) {
             case R.string.screen_blend:
                 if (c1 != 0) {
                     return ColorUtil.screenBlendTwoColors(c1, c2);
                 } else {
                     return c2;
                 }
 
             case R.string.multiply_blend:
                 if (c1 != 0) {
                     return ColorUtil.multiplyBlendTwoColors(c1, c2);
                 } else {
                     return c2;
                 }
 
             case R.string.average_blend:
                 if (c1 != 0) {
                     return ColorUtil.averageBlendTwoColors(c1, c2);
                 } else {
                     return c2;
                 }
         }
         return c1;
     }
 }
