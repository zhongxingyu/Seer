 package com.typeiisoft.lct.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import android.util.Log;
 
 import com.mhuss.AstroLib.Astro;
 import com.mhuss.AstroLib.AstroDate;
 import com.mhuss.AstroLib.Lunar;
 import com.mhuss.AstroLib.LunarCalc;
 import com.mhuss.AstroLib.NoInitException;
 import com.mhuss.AstroLib.ObsInfo;
 import com.mhuss.AstroLib.TimeOps;
 
 import com.typeiisoft.lct.features.LunarFeature;
 
 /**
  * This class handles calling calculations and returning various bits of 
  * information about the moon. It will also handle the logic for determining 
  * if a lunar feature is visible.
  * @author Michael Reuter
  *
  */
 public class MoonInfo {
 	/** Logging tag. */
 	private static final String TAG = "MoonInfo";
 	/** The current date and time in UTC for all observation information. */
 	private AstroDate obsDate;
 	/** The current date and time in the local timezone. */
 	private Calendar obsLocal;
 	/** Object that does most of the calculations. */
 	private Lunar lunar;
 	/** Object that holds the observing site information. */
 	private ObsInfo obsInfo;
 	/** Holder for the selenographic colongitude. */
 	private double colongitude;
 	/** Enum containing the lunar phases for integer comparison. */
 	private enum Phase {
 		NM, WAXING_CRESENT, FQ, WAXING_GIBBOUS, FM, WANING_GIBBOUS, TQ,
 		WANING_CRESENT;
 	}
 	/** Array of lunar phase names. */
 	private String[] phaseNames = {"New Moon", "Waxing Cresent", 
 			"First Quarter", "Waxing Gibbous", "Full Moon", "Waning Gibbous",
 			"Third Quarter", "Waning Cresent"};
 	/** Enum containing the time of lunar day for integer comparison. */
 	private enum TimeOfDay {
 		MORNING, EVENING;
 	}
 	/** Selenographic longitude where relief makes it hard to see feature. */
 	private static final double FEATURE_CUTOFF = 15D;
 	/** Lunar features to which no selenographic longitude cutoff is applied. */
 	private String[] noCutoffType = {"Mare", "Oceanus"};
 	
 	/**
 	 * This function is the parameter-less class constructor.
 	 */
 	public MoonInfo() {
 		Calendar now = Calendar.getInstance();
 		this.obsLocal = (Calendar)now.clone();
 		int offset = now.getTimeZone().getOffset(now.getTimeInMillis()) / Astro.MILLISECONDS_PER_HOUR;
 		now.add(Calendar.HOUR_OF_DAY, -offset);
 		this.obsDate = new AstroDate(now.get(Calendar.DATE), 
 				now.get(Calendar.MONTH)+1, now.get(Calendar.YEAR), 
 				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 
 				now.get(Calendar.SECOND));
 		this.initialize();
 	}
 	
 	/**
 	 * This function is the parametered class constructor.
 	 * @param datetime : Array of seven values of the current date and time.
 	 */
 	public MoonInfo(int[] datetime) {
 		this.obsDate = new AstroDate(datetime[0], datetime[1], datetime[2],
 				datetime[3], datetime[4], datetime[5]);
 		this.obsLocal = this.obsDate.toGCalendar();
 		this.obsLocal.add(Calendar.HOUR_OF_DAY, datetime[6]);
 		this.initialize();
 	}
 	
 	/**
 	 * This function consolidates some of the common setup.
 	 */
 	private void initialize() {
 		this.lunar = new Lunar(this.getJulianCenturies());
 		this.obsInfo = new ObsInfo();
 		this.colongitude = Double.MAX_VALUE;
 	}
 	
 	/**
 	 * This function calculates the Julian centuries based on the current 
 	 * observation date/time. The empty AstroDate constructor defaults to 
 	 * epoch J2000.0
 	 * @return : The Julian centuries for the current observation date/time.
 	 */
 	private double getJulianCenturies() {
 		double daysSinceEpoch = this.obsDate.jd() - new AstroDate().jd();
 		return daysSinceEpoch / Astro.TO_CENTURIES;
 	}
 	
 	/**
 	 * This function sets the latitude and longitude for an observing site. 
 	 * This is used for some of the calculations.
 	 * @param lat : The observing site's latitude in decimal degrees.
 	 * @param lon : The observing site's longitude in decimal degrees.
 	 */
 	public void setObservationInfo(double lat, double lon) {
 		this.obsInfo.setLatitudeDeg(lat);
 		this.obsInfo.setLongitudeDeg(lon);
 	}
 	
 	/**
 	 * This function gets the age of the Moon in days and gives back a string 
 	 * containing two decimal places.
 	 * @return : A string representing the Moon's age.
 	 */
 	public String age() {
 		double currentAge = LunarCalc.ageOfMoonInDays(this.obsDate.jd());
 		return StrFormat.formatDouble(currentAge, 2) + " days";
 	}
 	
 	/**
 	 * This function gets the illuminated fraction of the Moon's surface and 
 	 * gives that as a percentage in a string.
 	 * @return : A string containing the percentage of the illuminated Moon surface.
 	 */
 	public String illumation() {
 		double illum = 0.0;
 		try {
 			illum = this.lunar.illuminatedFraction();
 		}
 		catch (NoInitException nie) {
 			Log.e(TAG, "Lunar object is not initialized for calculating illumination.");
 		}
 		return StrFormat.formatDouble(illum * 100.0, 1) + "%";
 	}
 	
 	/**
 	 * This function returns the currently held local representation of the 
 	 * observation date and time as separate strings.
 	 * @return : A date string and a time string in the local timezone.
 	 */
 	public String[] obsLocalTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		StringBuffer buf = new StringBuffer(format.format(this.obsLocal.getTime()));
 		String tz = this.obsLocal.getTimeZone().getDisplayName(TimeOps.dstOffset(this.obsLocal) != 0, 
 				TimeZone.SHORT);
 		buf.append(" ").append(tz);
 		return buf.toString().split(" ", 2);
 	}
 
 	/**
 	 * This function returns the currently held UTC representation of the 
 	 * observation date and time as separate strings. 
 	 * @return : A date string and a time string in the UTC timezone.
 	 */
 	public String[] obsUtcTime() {
 		StringBuffer buf = new StringBuffer(this.obsDate.toMinString());
 		buf.append(" UTC");
 		return buf.toString().split(" ", 2);
 	}
 
 	
 	/**
 	 * This function returns the selenographic colongitude for the current 
 	 * date and time.
 	 * @return : The selenographic colongitude as a DMS string.
 	 */
 	public String colong() {
 		this.getColongitude();
 		Log.i(TAG, "Colongitude calculated = " + Double.toString(this.colongitude));
 		return StrFormat.dmsFromDd(this.colongitude, false);
 	}
 	
 	/**
 	 * This function returns the phase of the Moon as a string.
 	 * @return : The Moon phase as a string.
 	 */
 	public String phase() {
 		return this.phaseNames[this.getPhase().ordinal()];
 	}
 	
 	/**
 	 * This function determines if the given lunar feature is visible based 
      * on the current selenographic colongitude (SELCO). For most features 
      * near the equator, from NM to FM once the SELCO recedes about 15 
      * degrees, the shadow relief makes it tough to observe. Conversely, the 
      * SELCO needs to be within 15 degrees of the feature from FM to NM. 
      * Features closer to the poles are visible much longer after the 15 
      * degree cutoff. A 1/cos(latitude) will be applied to the cutoff. 
      * Mare and Oceanus are special exceptions and once FULLY visible they are 
      * always visible.
 	 * @param feature : The lunar feature to check for visibility.
 	 * @return : True is the feature is visible.
 	 */
 	public boolean isVisible(LunarFeature feature) {
 		double selcoLong = this.colongToLong();
 		Log.v(TAG, "SelcoLong = " + Double.toString(selcoLong));
 		int curTod = this.getTimeOfDay().ordinal();
 		Log.v(TAG, "CurTod = " + Integer.toString(curTod));
 		
 		double minLon = feature.getLongitude() - feature.getDeltaLongitude() / 2.0;
 		double maxLon = feature.getLongitude() + feature.getDeltaLongitude() / 2.0;
 		
 		// Switch things around if necessary
 		if (minLon > maxLon) {
 			double temp = minLon;
 			minLon = maxLon;
 			maxLon = temp;
 		}
 		
 		boolean isVisible = false;
 		double latitudeScaling = Math.cos(Math.toRadians(Math.abs(feature.getLatitude())));
 		double cutoff = MoonInfo.FEATURE_CUTOFF / latitudeScaling;
 		
 		double lonCutoff = 0.0;
 		if (TimeOfDay.MORNING.ordinal() == curTod) {
 			// Minimum longitude for morning visibility
 			lonCutoff = minLon - cutoff;
 			if (this.noCutoffFeature(feature.getFeatureType())) {
 				
 				isVisible = selcoLong <= minLon;
 			}
 			else {
 				isVisible = (selcoLong >= lonCutoff && selcoLong <= minLon);
 			}
 		}
 		if (TimeOfDay.EVENING.ordinal() == curTod) {
 			// Maximum longitude for evening visibility
 			lonCutoff = maxLon + cutoff;
 			if (this.noCutoffFeature(feature.getFeatureType())) {
 				isVisible = maxLon <= selcoLong;
 			}
 			else {
 				isVisible = (selcoLong >= maxLon && selcoLong <= lonCutoff);
 			}
 		}
 		
 		return isVisible;
 	}
 
 	/**
 	 * This function is to set the selenographic colongitude once for a given 
 	 * instance. This will cut down on the number of calculations done by the 
 	 * library call.
 	 */
 	private void getColongitude() {
 		if (Double.MAX_VALUE == this.colongitude) {
 			this.colongitude = LunarCalc.colongitude(this.getJulianCenturies());
 		}
 	}
 	
 	/**
 	 * This function checks a given lunar feature type against the list of lunar 
 	 * feature types that have no cutoff.
 	 * @param type : The string containing the lunar feature type.
 	 * @return : True is the incoming feature type is in no cutoff list.
 	 */
 	private boolean noCutoffFeature(String type) {
 		for (String s : this.noCutoffType) {
 			if (s.equalsIgnoreCase(type)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This function returns the moon phase according to standard nomenclature.
 	 * @return : The moon phase as an enum value.
 	 */
 	private Phase getPhase() {
 		this.getColongitude();
 		if (270.0 == this.colongitude) {
 			return Phase.NM;
 		}
 		else if (this.colongitude > 270.0 && this.colongitude < 360.0) {
 			return Phase.WAXING_CRESENT;
 		}
 		else if (0.0 == this.colongitude || 360.0 == this.colongitude) {
 			return Phase.FQ;
 		}
 		else if (this.colongitude > 0.0 && this.colongitude < 90.0) {
 			return Phase.WAXING_GIBBOUS;
 		}
 		else if (90.0 == this.colongitude) {
 			return Phase.FM;
 		}
 		else if (this.colongitude > 90.0 && this.colongitude < 180.0) {
 			return Phase.WANING_GIBBOUS;
 		}
 		else if (180.0 == this.colongitude) {
 			return Phase.TQ;
 		}
 		else if (this.colongitude > 180.0 && this.colongitude < 270.0) {
 			return Phase.WANING_CRESENT;
 		}
 		else {
 			return Phase.NM;
 		}
 	}
 	
 	/**
 	 * This function determines the current time of day on the moon. In 
      * otherwords, if the sun is rising on the moon it is morning or if the 
      * sun is setting on the moon it is evening.
 	 * @return : The current time of day.
 	 */
 	private TimeOfDay getTimeOfDay() {
 		int phase = this.getPhase().ordinal();
 		if (phase >= Phase.NM.ordinal() && 
 				phase <= Phase.WAXING_GIBBOUS.ordinal()) {
 			return TimeOfDay.MORNING;
 		}
 		else {
 			return TimeOfDay.EVENING;
 		}
 	}
 	
 	/**
 	 * This function calculates the conversion between the selenographic  
      * colongitude and actual lunar longitude.
 	 * @return : The lunar longitude for the current selenographic colongitude.
 	 */
 	private double colongToLong() {
 		this.getColongitude();
 		int phase = this.getPhase().ordinal();
 		if (Phase.NM.ordinal() == phase || Phase.WAXING_CRESENT.ordinal() == phase) {
 			return 360.0 - this.colongitude;
 		}
 		else if (Phase.FQ.ordinal() == phase || Phase.WAXING_GIBBOUS.ordinal() == phase) {
 			return -1.0 * this.colongitude;
 		}
 		else if (Phase.FM.ordinal() == phase || Phase.WANING_GIBBOUS.ordinal() == phase) {
 			return 180.0 - this.colongitude;
 		}
 		else {
 			return -1.0 * (this.colongitude - 180.0);
 		}
 	}
 	
 	/**
 	 * This function creates the string representation of the object.
 	 * @return : The current string representation.
 	 */
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		String[] tmp = this.obsLocalTime();
 		buf.append("Date: ").append(tmp[0]).append(System.getProperty("line.separator"));
 		buf.append("Time: ").append(tmp[1]).append(System.getProperty("line.separator"));
 		buf.append("Julian Date: ").append(Double.toString(this.obsDate.jd()));
 		buf.append(System.getProperty("line.separator"));
 		return buf.toString();
 	}
 }
