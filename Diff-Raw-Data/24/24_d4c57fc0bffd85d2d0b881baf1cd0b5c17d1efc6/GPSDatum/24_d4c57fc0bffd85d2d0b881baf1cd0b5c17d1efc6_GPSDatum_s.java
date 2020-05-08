 /**
  * 
  */
 package ecologylab.sensor.location.gps.data;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import ecologylab.sensor.location.LocationStatus;
 import ecologylab.sensor.location.gps.data.dataset.GGA;
 import ecologylab.sensor.location.gps.data.dataset.GLL;
 import ecologylab.sensor.location.gps.data.dataset.GPSDataFieldBase;
 import ecologylab.sensor.location.gps.data.dataset.GSA;
 import ecologylab.sensor.location.gps.data.dataset.GSV;
 import ecologylab.sensor.location.gps.data.dataset.RMC;
 import ecologylab.sensor.location.gps.data.dataset.VTG;
 import ecologylab.sensor.location.gps.listener.GPSDataUpdatedListener;
 import ecologylab.sensor.location.gps.listener.GPSDataUpdatedListener.GPSUpdateInterest;
 import ecologylab.serialization.annotations.simpl_collection;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.annotations.simpl_scalar;
 
 /**
  * Represents an instant of GPS data computed from a series of NMEA strings. Each component of the
  * datum is as up-to-date as possible as of the time stamp. Whenever no new data is provided, the
  * old data is retained.
  * 
  * @author Zachary O. Toups (zach@ecologylab.net)
  * 
  */
 @simpl_inherit
 public class GPSDatum extends LocationStatus implements GPSConstants
 {
 	public enum DopType
 	{
 		PDOP, VDOP, HDOP, NOT_AVAILABLE
 	}
 
 	/**
 	 * The ground speed recorded by the gps in meters per second.
 	 */
 	@simpl_scalar
 	public double												grndSpd					= 0.0;
 
 	/**
 	 * Quality of GPS data; values will be either GPS_QUAL_NO, GPS_QUAL_GPS, GPS_QUAL_DGPS.
 	 */
 	@simpl_scalar
 	public int													gpsQual;
 
 	/**
 	 * The number of satellites the GPS receiver is using to compute the solution.
 	 */
 	@simpl_scalar
 	public int													numSats;
 
 	/**
 	 * Horizontal Dilution of Precision - approximation of the size of the area in which the actual
 	 * location of the GPS is horizontally based on the spread of the fixed satellites; higher numbers
 	 * are worse, smaller mean better precision; this value may range between 1-50.
 	 * 
 	 * See http://www.codepedia.com/1/Geometric+Dilution+of+Precision+(DOP) for more information.
 	 */
 	@simpl_scalar
 	public float												hdop;
 
 	/** Position Dillution of Precision */
 	@simpl_scalar
 	public float												pdop;
 
 	/** Vertical Dillution of Precision */
 	@simpl_scalar
 	public float												vdop;
 
 	/**
 	 * The altitude of the antenna of the GPS (location where the signals are recieved). In meters.
 	 */
 	@simpl_scalar
 	protected float											geoidHeight;
 
 	/** The differential between the elipsoid and the geoid. In meters. */
 	@simpl_scalar
 	protected float											heightDiff;
 
 	@simpl_scalar
 	protected float											dgpsAge;
 
 	@simpl_scalar
 	protected int												dgpsRefStation;
 
 	/** Indicates whether or not the current GPS data is valid. */
 	@simpl_scalar
 	protected boolean										dataValid;
 
 	/**
 	 * Indicates whether or not the calculation mode (2D/3D) is automatically selected.
 	 */
 	@simpl_scalar
 	protected boolean										autoCalcMode;
 
 	/**
 	 * Calculating mode (2D/3D); valid values are CALC_MODE_NONE, CALC_MODE_2D, or CALC_MODE_3D.
 	 */
 	@simpl_scalar
 	protected int												calcMode;
 
 	/**
 	 * Used during the processing of a GSV (GNSS Satellites in View) sentence. Specifies which SV's
 	 * data is being updated. Because specific SV data comes over several pieces of a message, and
 	 * these pieces are processed independently using enums, the GSPDatum object must track the
 	 * current SV, to ensure that it is updated correctly.
 	 */
 	private SVData											currentSV;
 
 	/**
 	 * References to data about the currently-tracked satellites (space vehicles, SVs).
 	 */
 	@simpl_collection("sv")
 	private ArrayList<SVData>						trackedSVs;
 
 	private Calendar										utcTime					= Calendar.getInstance();
 
 	/**
 	 * All up-to-date data on SVs that have been reported on by the GPS hardware.
 	 */
 	protected HashMap<Integer, SVData>	allSVs;
 
 	/** Used for moving data around when processing NMEA sentences. */
 	private char[]											tempDataStore;
 
 	private double											HDOPMultiplier	= 3.0;
 
 	public GPSDatum()
 	{
 	}
 
 	public GPSDatum(double latDeg, double latMin, double lonDeg, double lonMin)
 	{
 		this();
 
 		setLat(AngularCoord.fromDegMinSec(latDeg, latMin, 0));
 		setLon(AngularCoord.fromDegMinSec(lonDeg, lonMin, 0));
 	}
 
 	public GPSDatum(double latDeg, double lonDeg)
 	{
 		this(latDeg, 0f, lonDeg, 0f);
 	}
 
 	/**
 	 * Ensure that tempDataStore is instantiated and clean, then return it. Used for lazy
 	 * instantiation; no need to instantiate it if this is a serialized set of data from somewhere
 	 * else (for example).
 	 * 
 	 * @return
 	 */
 	private synchronized char[] tempDataStore()
 	{
 		if (tempDataStore == null)
 		{
 			tempDataStore = new char[100];
 		}
 
 		return tempDataStore;
 	}
 
 	public static void main(String[] args)
 	{
 		GPSDatum d = new GPSDatum();
 
 		d.integrateGPSData("GPRMC,223832.804,V,3037.3725,N,09620.2286,W,0.00,0.00,120208,,,N*6C");
 	}
 
 	/**
 	 * Splits and stores data from an NMEA GPS data set.
 	 * 
 	 * @param gpsData
 	 *          a GPS data set, minus $ header and <CR><LF> trailer.
 	 */
 	public synchronized void integrateGPSData(String gpsData)
 	{
 		char[] tempData = tempDataStore();
 
 		// check the checksum before doing any processing
 		int checkSumSplit = gpsData.length() - 3;
 
 		String messageStringToCheck = gpsData.substring(0, checkSumSplit);
 
 		int checkSum = 0;
 
 		for (int i = 0; i < messageStringToCheck.length(); i++)
 		{
 			checkSum = (checkSum ^ messageStringToCheck.charAt(i));
 		}
 
 		String computedCheckSum = Integer.toHexString((checkSum & 0xF0) >>> 4)
 				+ Integer.toHexString(checkSum & 0x0F);
 
 		if (computedCheckSum.toUpperCase().equals(gpsData.substring(checkSumSplit + 1)))
 		{
 
 			int dataLength = gpsData.length();
 			int i = 6; // start looking after "GPXXX," -- the header of the
 			// message
 
 			int dataStart = i;
 			boolean finishedField = false;
 
 			GPSDataFieldBase[] fieldBase = null;
 
 			switch (gpsData.charAt(2))
 			{
 			case ('G'):
 				// either GGA, GLL, GSA, or GSV
 				switch (gpsData.charAt(3))
 				{
 				case ('G'):
 					// check GGA
 					if (gpsData.charAt(4) == 'A')
 					{
 						fieldBase = GGA.values();
 					}
 					break;
 				case ('L'):
 					// check GLL
 					if (gpsData.charAt(4) == 'L')
 					{
 						fieldBase = GLL.values();
 					}
 					break;
 				case ('S'):
 					switch (gpsData.charAt(4))
 					{
 					case ('A'): // GSA
 						fieldBase = GSA.values();
 						break;
 					case ('V'): // GSV
 						fieldBase = GSV.values();
 						break;
 					}
 					break;
 				}
 				break;
 			case ('R'): // check RMC
 				if (gpsData.charAt(3) == 'M' && gpsData.charAt(4) == 'C')
 				{
 					fieldBase = RMC.values();
 				}
 				break;
 			case ('V'):
 				fieldBase = VTG.values();
 				break;
 			case ('Z'):
 				// TODO check ZDA
 				break;
 			}
 
 			if (fieldBase != null)
 			{
 				synchronized (tempDataStore)
 				{
 					for (GPSDataFieldBase field : fieldBase)
 					{
 						finishedField = false;
 						dataStart = i;
 
 						/*
 						 * now we will read each data field in, one at at time, since we know the order of the
 						 * data set. we will break out of each for loop when finished.
 						 */
 						while (i < dataLength && !finishedField)
 						{
 							try
 							{ // XXX this should be removed once the bug is fixed
 								tempData[i] = gpsData.charAt(i);
 
 								if (tempData[i] == ',')
 								{
 									if (i - dataStart > 0)
 									{
 										field.update(new String(tempData, dataStart, (i - dataStart)), this);
 									}
 									else
 									{
 										field.update(null, this);
 									}
 									finishedField = true;
 								}
 
 								i++;
 							}
 							catch (Exception e)
 							{
 								error("Exception occurred!");
 								error("i                = " + i);
 								error("gpsData.length() = " + gpsData.length());
 								error("tempData.length  = " + tempData.length);
 
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 			}
 			else
 			{
 				debug(gpsData.substring(2, 5) + " not recognized.");
 			}
 
 			// TODO GET OTHER GPS INTERESTS!!!
 
 			this.fireGPSDataUpdatedEvent();
 		}
 		else
 		{
 			debug("NMEA sentence checksum bad: " + gpsData);
 		}
 	}
 
	public void updateLatLon(double lat, double lon)
 	{
 		double oldLat = this.getLat();
 		double oldLon = this.getLon();
 
 		super.setLat(lat);
 		super.setLon(lon);
 
 		this.pointDirty = true;
 
 		if ((oldLat != getLat() || oldLon != getLon()) && this.latLonUpdatedListeners != null)
 		{
 			this.gpsDataUpdatedListenersToUpdate.addAll(this.latLonUpdatedListeners);
 		}
 
 		fireGPSDataUpdatedEvent();
 	}
 
 	/**
 	 * Fires an event to notify all listeners that the datum has been updated. May be invoked
 	 * externally when a full update is carried out over a series of calls. Invoked automatically when
 	 * this is linked to a GPS sensor.
 	 */
 	public void fireGPSDataUpdatedEvent()
 	{
 		for (GPSDataUpdatedListener l : this.gpsDataUpdatedListenersToUpdate)
 		{
 			l.gpsDatumUpdated(this);
 		}
 	}
 
 	@Override
 	public String toString()
 	{
 		return new String("GPSDatum: " + getLat() + ", " + getLon());
 	}
 
 	/**
 	 * determine longitude sign
 	 * 
 	 * @param src
 	 */
 	public void updateLonHemisphere(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		setLon(AngularCoord.signForHemisphere(src.charAt(0), getLon()));
 
 		this.pointDirty = true;
 	}
 
 	/**
 	 * determine longitude degrees and minutes
 	 * 
 	 * @param src
 	 */
 	public void updateLon(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		double oldLon = getLon();
 
 		setLon(AngularCoord.fromDegMinSec(Integer.parseInt(src.substring(0, 3)),
 																			Double.parseDouble(src.substring(3)),
 																			0));
 
 		this.pointDirty = true;
 
 		if (oldLon != getLon() && this.latLonUpdatedListeners != null)
 		{
 			this.gpsDataUpdatedListenersToUpdate.addAll(this.latLonUpdatedListeners);
 		}
 	}
 
 	/**
 	 * update longitude and prep for firing event to listeners
 	 * 
 	 * @param src
 	 */
 	public void updateLon(double lon)
 	{
 		double oldLon = this.getLon();
 		
 		this.setLon(lon);
 
 		this.pointDirty = true;
 
 		if (oldLon != getLon() && this.latLonUpdatedListeners != null)
 		{
 			this.gpsDataUpdatedListenersToUpdate.addAll(this.latLonUpdatedListeners);
 		}
 	}
 
 	
 	/**
 	 * determine latitude sign
 	 * 
 	 * @param src
 	 */
 	public void updateLatHemisphere(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		setLat(AngularCoord.signForHemisphere(src.charAt(0), getLat()));
 
 		this.pointDirty = true;
 	}
 
 	/**
 	 * determine latitude degrees and minutes
 	 * 
 	 * @param src
 	 */
 	public void updateLat(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		double oldLat = getLat();
 
 		setLat(AngularCoord.fromDegMinSec(Integer.parseInt(src.substring(0, 2)),
 																			Double.parseDouble(src.substring(2)),
 																			0));
 
 		this.pointDirty = true;
 
 		if (oldLat != getLat() && this.latLonUpdatedListeners != null)
 		{
 			this.gpsDataUpdatedListenersToUpdate.addAll(this.latLonUpdatedListeners);
 		}
 	}
 	
 	/**
 	 * update latitude and prep for firing event to listeners
 	 * 
 	 * @param src
 	 */
 	public void updateLat(double lat)
 	{
 		double oldLat = this.getLat();
 		
 		this.setLat(lat);
 
 		this.pointDirty = true;
 
 		if (oldLat != getLat() && this.latLonUpdatedListeners != null)
 		{
 			this.gpsDataUpdatedListenersToUpdate.addAll(this.latLonUpdatedListeners);
 		}
 	}
 
 	/**
 	 * Stores the UTC time according to the way it is represented in the NMEA sentence: HHMMSS.S
 	 * 
 	 * @param utcString
 	 */
 	public void updateUtcPosTime(String utcString)
 	{
 		if (utcString == null || utcString.length() == 0)
 			return;
 
 		double time = Double.parseDouble(utcString);
 
 		int hour = (int) (time / 10000);
 		int minute = ((int) (time / 100)) % 100;
 		int second = ((int) (time)) % 100;
 		int milliSecond = ((int) (time * 1000)) % 1000;
 
 		this.utcTime.set(Calendar.HOUR_OF_DAY, hour);
 		this.utcTime.set(Calendar.MINUTE, minute);
 		this.utcTime.set(Calendar.SECOND, second);
 		this.utcTime.set(Calendar.MILLISECOND, milliSecond);
 	}
 
 	/**
 	 * Update the number of satellites the GPS receiver is using to produce a solution.
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateNumSats(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		this.numSats = Integer.parseInt(src);
 	}
 
 	/**
 	 * Update the quality rating for the GPS receiver: none (0), GPS (1), or DGPS (2).
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateGPSQual(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		this.gpsQual = Integer.parseInt(src);
 	}
 
 	/**
 	 * Update the differential GPS reference station.
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateDGPSRef(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		this.dgpsRefStation = Integer.parseInt(src);
 	}
 
 	/**
 	 * Checks the unit mode for height; this should be meters, and thus should be "M".
 	 * 
 	 * TODO handle other units, instead of printing an error? I haven't seen any documentation
 	 * describing the use of other units - Zach
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateDiffHeightUnit(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		if (src.charAt(0) != 'M')
 		{
 			warning("GPS is not using meters: " + src);
 		}
 	}
 
 	/**
 	 * Updates the age of the differential GPS reading, per the GPS.
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateDGPSAge(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		this.dgpsAge = Float.parseFloat(src);
 	}
 
 	/**
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateHeightUnit(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		if (src.charAt(0) != 'M')
 		{
 			warning("GPS is not using meters: " + src);
 		}
 	}
 
 	/**
 	 * Updates the height of the geoid, indicated at this location.
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateGeoidHeight(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		this.geoidHeight = Float.parseFloat(src);
 	}
 
 	/**
 	 * Updates the validity rating for the data. Data can be valid ("A") or invalid ("V").
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateDataValid(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		switch (src.charAt(0))
 		{
 		case 'A':
 		case 'a':
 			this.dataValid = true;
 			break;
 		case 'V':
 		case 'v':
 			this.dataValid = false;
 			break;
 		}
 	}
 
 	/**
 	 * Updates the auto calculation mode setting. Can be automatic ("A") or manual ("M").
 	 * 
 	 * @param src
 	 *          - part of the NMEA string carrying the relevant data.
 	 */
 	public void updateAutoCalcMode(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		switch (src.charAt(0))
 		{
 		case 'A':
 		case 'a':
 			this.autoCalcMode = true;
 			break;
 		case 'M':
 		case 'm':
 			this.autoCalcMode = false;
 			break;
 		}
 	}
 
 	/**
 	 * @param src
 	 */
 	public void updateCalcMode(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 
 		// values will be either 0, 1, or 2
 		this.calcMode = Integer.parseInt(src);
 	}
 
 	/**
 	 * Specifies the SV number to be added to the list of tracked SVs. Because NMEA sentences report
 	 * data on up to 12 tracked SVs, each SV has an index (in addition to its ID).
 	 * 
 	 * @param sVIDString
 	 *          the data String containing the ID of the tracked SV.
 	 * @param i
 	 *          the index of the tracked SV (will overwrite whichever was stored previously).
 	 */
 	public void addSVToTrackedList(String sVIDString, int i)
 	{
 		if (sVIDString == null || sVIDString.length() == 0)
 			return;
 
 		Integer index = new Integer(i);
 		int sVID = Integer.parseInt(sVIDString);
 
 		HashMap<Integer, SVData> allSVsLocal = this.allSVs();
 
 		SVData currentData = allSVsLocal.get(sVID);
 
 		if (currentData == null)
 		{
 			currentData = new SVData(sVID);
 
 			allSVsLocal.put(index, currentData);
 		}
 
 		ArrayList<SVData> trackedSVsLocal = this.trackedSVs();
 		while (trackedSVsLocal.size() <= i)
 		{
 			trackedSVsLocal.add(null);
 		}
 
 		trackedSVsLocal.set(i, currentData);
 	}
 
 	/**
 	 * Specifies which SV is going to be updated by the following calls to setCurrentSVElev(...),
 	 * setCurrentSVAzi(...), setCurrentSVSNR(...).
 	 * 
 	 * @param sVIDString
 	 *          the ID of the SV to set.
 	 */
 	public void setCurrentSV(String sVIDString)
 	{
 		if (sVIDString == null || sVIDString.length() == 0)
 			return;
 
 		int sVID = Integer.parseInt(sVIDString);
 
 		HashMap<Integer, SVData> allSVsLocal = this.allSVs();
 
 		SVData tempCurrentSV = allSVsLocal.get(sVID);
 
 		if (tempCurrentSV == null)
 		{
 			tempCurrentSV = new SVData(sVID);
 		}
 
 		this.currentSV = tempCurrentSV;
 	}
 
 	/**
 	 * Sets the elevation on the current SV. THIS METHOD ASSUMES THAT setCurrentSV(...) has been
 	 * called in advance; if it has not, your program will crash.
 	 * 
 	 * @param currentSVElevationString
 	 */
 	public void setCurrentSVElev(String currentSVElevationString)
 	{
 		if (currentSVElevationString == null || currentSVElevationString.length() == 0)
 			return;
 
 		int elevation = Integer.parseInt(currentSVElevationString);
 
 		this.currentSV.setElevation(elevation);
 	}
 
 	/**
 	 * Sets the azimuth on the current SV. THIS METHOD ASSUMES THAT setCurrentSV(...) has been called
 	 * in advance; if it has not, your program will crash.
 	 * 
 	 * @param currentSVElevationString
 	 */
 	public void setCurrentSVAzi(String currentSVAzimuthString)
 	{
 		if (currentSVAzimuthString == null || currentSVAzimuthString.length() == 0)
 			return;
 
 		int azimuth = Integer.parseInt(currentSVAzimuthString);
 
 		this.currentSV.setAzimuth(azimuth);
 	}
 
 	/**
 	 * Sets the signal-to-noise ratio on the current SV. THIS METHOD ASSUMES THAT setCurrentSV(...)
 	 * has been called in advance; if it has not, your program will crash.
 	 * 
 	 * @param currentSVElevationString
 	 */
 	public void setCurrentSVSNR(String currentSVSNRString)
 	{
 		if (currentSVSNRString == null || currentSVSNRString.length() == 0)
 			return;
 
 		int snr = Integer.parseInt(currentSVSNRString);
 
 		this.currentSV.setSnr(snr);
 	}
 
 	public void unsetCurrentSV()
 	{
 		this.currentSV = null;
 	}
 
 	/**
 	 * @param src
 	 */
 	public void updateHDOP(String src)
 	{
 		if (src != null && !src.equals(""))
 			this.hdop = Float.parseFloat(src);
 		else
 			this.hdop = 50.0f;
 	}
 
 	/**
 	 * @param src
 	 */
 	public void updatePDOP(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 		this.pdop = Float.parseFloat(src);
 	}
 
 	/**
 	 * @param src
 	 */
 	public void updateVDOP(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 		this.vdop = Float.parseFloat(src);
 	}
 
 	/**
 	 * @param src
 	 */
 	public void updateHeightDiff(String src)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @param lat
 	 *          the lat to set
 	 */
 	@Override
 	public void setLat(double lat)
 	{
 		super.setLat(lat);
 
 		this.pointDirty = true;
 	}
 
 	/**
 	 * @param lon
 	 *          the lon to set
 	 */
 	@Override
 	public void setLon(double lon)
 	{
 		super.setLon(lon);
 
 		this.pointDirty = true;
 	}
 
 	private HashMap<Integer, SVData> allSVs()
 	{
 		if (this.allSVs == null)
 		{
 			this.allSVs = new HashMap<Integer, SVData>();
 		}
 
 		return this.allSVs;
 	}
 
 	protected ArrayList<SVData> trackedSVs()
 	{
 		if (this.trackedSVs == null)
 		{
 			this.trackedSVs = new ArrayList<SVData>(12);
 		}
 
 		return this.trackedSVs;
 	}
 
 	/**
 	 * List of listeners who want to be notified of latitude or longitude updates.
 	 */
 	private List<GPSDataUpdatedListener>	latLonUpdatedListeners;
 
 	/** List of listeners who want to be notified of altitude updates. */
 	private List<GPSDataUpdatedListener>	altUpdatedListeners;
 
 	/**
 	 * List of listeners who want to be notified of any updates not covered above.
 	 */
 	private List<GPSDataUpdatedListener>	speedUpdatedListeners;
 
 	/**
 	 * List of listeners who want to be notified of any updates not covered above.
 	 */
 	private List<GPSDataUpdatedListener>	otherUpdatedListeners;
 
 	/** Semaphore for instantiating the above lists lazilly. */
 	private Object												listenerLock										= new Object();
 
 	/** Set of listeners to notify for this update, as determined by interest. */
 	private Set<GPSDataUpdatedListener>		gpsDataUpdatedListenersToUpdate	= new HashSet<GPSDataUpdatedListener>();
 
 	/**
 	 * Indicates that pointRepresentation is out of synch with the state of this object.
 	 */
 	private boolean												pointDirty											= true;
 
 	private List<GPSDataUpdatedListener> latLonUpdatedListeners()
 	{
 		if (this.latLonUpdatedListeners == null)
 		{
 			synchronized (this.listenerLock)
 			{
 				if (this.latLonUpdatedListeners == null)
 				{
 					this.latLonUpdatedListeners = new LinkedList<GPSDataUpdatedListener>();
 				}
 			}
 		}
 
 		return this.latLonUpdatedListeners;
 	}
 
 	private List<GPSDataUpdatedListener> altUpdatedListeners()
 	{
 		if (this.altUpdatedListeners == null)
 		{
 			synchronized (this.listenerLock)
 			{
 				if (this.altUpdatedListeners == null)
 				{
 					this.altUpdatedListeners = new LinkedList<GPSDataUpdatedListener>();
 				}
 			}
 		}
 
 		return this.altUpdatedListeners;
 	}
 
 	private List<GPSDataUpdatedListener> speedUpdatedListeners()
 	{
 		if (this.speedUpdatedListeners == null)
 		{
 			synchronized (this.listenerLock)
 			{
 				if (this.speedUpdatedListeners == null)
 				{
 					this.speedUpdatedListeners = new LinkedList<GPSDataUpdatedListener>();
 				}
 			}
 		}
 
 		return this.speedUpdatedListeners;
 	}
 
 	private List<GPSDataUpdatedListener> otherUpdatedListeners()
 	{
 		if (this.otherUpdatedListeners == null)
 		{
 			synchronized (this.listenerLock)
 			{
 				if (this.otherUpdatedListeners == null)
 				{
 					this.otherUpdatedListeners = new LinkedList<GPSDataUpdatedListener>();
 				}
 			}
 		}
 
 		return this.otherUpdatedListeners;
 	}
 
 	/**
 	 * Gets the latitude and longitude of this datum as a Point2D, where x = longitude and y =
 	 * latitude.
 	 * 
 	 * @return the pointRepresentation
 	 */
 	@Override
 	public Point2D.Double getPointRepresentation()
 	{
 		if (pointRepresentation == null)
 		{
 			pointRepresentation = new Point2D.Double();
 		}
 
 		if (this.pointDirty)
 		{
 			this.pointRepresentation.setLocation(getLon(), getLat());
 			this.pointDirty = false;
 		}
 
 		return pointRepresentation;
 	}
 
 	public void addGPSDataUpdatedListener(GPSDataUpdatedListener l)
 	{
 		EnumSet<GPSUpdateInterest> interestSet = l.getInterestSet();
 
 		if (interestSet.contains(GPSUpdateInterest.LAT_LON))
 		{
 			this.latLonUpdatedListeners().add(l);
 		}
 		if (interestSet.contains(GPSUpdateInterest.ALT))
 		{
 			this.altUpdatedListeners().add(l);
 		}
 		if (interestSet.contains(GPSUpdateInterest.OTHERS))
 		{
 			this.otherUpdatedListeners().add(l);
 		}
 		if (interestSet.contains(GPSUpdateInterest.SPEED))
 		{
 			this.speedUpdatedListeners().add(l);
 		}
 	}
 
 	/**
 	 * @return the trackedSVs
 	 */
 	public ArrayList<SVData> getTrackedSVs()
 	{
 		return trackedSVs;
 	}
 
 	public int getNumSats()
 	{
 		return numSats;
 	}
 
 	public float getHdop()
 	{
 		return hdop;
 	}
 
 	/**
 	 * @return the pdop
 	 */
 	public float getPdop()
 	{
 		return pdop;
 	}
 
 	/**
 	 * @return the vdop
 	 */
 	public float getVdop()
 	{
 		return vdop;
 	}
 
 	/**
 	 * Examines the current data on DOP, and indicates the best type of DOP available. Note that if
 	 * there is stale data, this will use it.
 	 * 
 	 * Types are:
 	 * 
 	 * PDOP - position dillution of precision (3D)
 	 * 
 	 * HDOP - horizontal dillution of precision (2D, surface)
 	 * 
 	 * VDOP - vertical dillution of precision (1D, vertical only)
 	 * 
 	 * @return
 	 */
 	public DopType getDopType()
 	{
 		if (this.pdop > 0)
 		{
 			return DopType.PDOP;
 		}
 		else if (this.hdop > 0)
 		{
 			return DopType.HDOP;
 		}
 		else if (this.vdop > 0)
 		{
 			return DopType.VDOP;
 		}
 
 		return DopType.NOT_AVAILABLE;
 	}
 
 	/**
 	 * @return the gpsQual
 	 */
 	public int getGpsQual()
 	{
 		return gpsQual;
 	}
 
 	public void updateGroundSpeed(String src)
 	{
 		if (src == null || src.length() == 0)
 			return;
 		// convert from kph to meters per sec
 		this.grndSpd = Float.parseFloat(src) * 1000.0 / 60.0 / 60.0;
 	}
 
 	public double getSpeed()
 	{
 		return grndSpd;
 	}
 
 	public long getTimeInMillis()
 	{
 		return utcTime.getTimeInMillis();
 	}
 
 	public Calendar getTime()
 	{
 		return (Calendar) utcTime.clone();
 	}
 
 	public String getTimeString()
 	{
 		return utcTime.get(Calendar.HOUR_OF_DAY) + ":" + utcTime.get(Calendar.MINUTE) + ":"
 				+ utcTime.get(Calendar.SECOND);
 	}
 
 	public void setHDOPMultiplier(double mult)
 	{
 		this.HDOPMultiplier = mult;
 	}
 
 	public double getHorizontalUncertainty()
 	{
 		return this.hdop * this.HDOPMultiplier;
 	}
 
 	public void setGrndSpd(double grndSpd)
 	{
 		this.grndSpd = grndSpd;
 	}
 }
