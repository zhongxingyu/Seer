 /**
  * 
  */
 package org.reprap.devices;
 
 import java.io.IOException;
 import org.reprap.Device;
 import org.reprap.Extruder;
 import org.reprap.Preferences;
 import org.reprap.ReprapException;
 import javax.media.j3d.Appearance;
 import javax.vecmath.Color3f;
 import javax.media.j3d.Material;
 
 /**
  * @author Adrian
  *
  */
 public class NullExtruder implements Extruder{
 	
 	/**
 	 * Offset of 0 degrees centigrade from absolute zero
 	 */
 	private static final double absZero = 273.15;
 	
 	/**
 	 * The temperature to maintain
 	 */ 
 	private double requestedTemperature = 0;
 	
 	/**
 	 * The temperature most recently read from the device 
 	 */
 	private double currentTemperature = 0;
 	
 	/**
 	 * 
 	 */
 	private boolean currentMaterialOutSensor = false;
 	
 	/**
 	 * Indicates when polled values are first ready 
 	 */
 	private boolean sensorsInitialised = false;
 	
 	/**
 	 * 
 	 */
 	private Thread pollThread = null;
 	
 	/**
 	 * 
 	 */
 	private boolean pollThreadExiting = false;
 
 	/**
 	 * 
 	 */
 	private int vRefFactor = 7;
 	
 	/**
 	 * 
 	 */
 	private int tempScaler = 4;
 	
 	/**
 	 * Thermistor beta
 	 */
 	private double beta; 
 	
 	/**
 	 * Thermistor resistance at 0C
 	 */
 	private double rz;
 	
 	/**
 	 * Thermistor timing capacitor in farads
 	 */
 	private double cap; 
 	
 	/**
 	 * Heater power gradient
 	 */	
 	private double hm;
 	
 	/**
 	 * Heater power intercept
 	 * TODO hb should probably be ambient temperature measured at this point
 	 */
 	private double hb;
 	
 	/**
 	 * Maximum motor speed (0-255)
 	 */
 	private int maxExtruderSpeed;
 	
 	/**
 	 * The actual extrusion speed
 	 */
 	private int extrusionSpeed;
 	
 	/**
 	 * The extrusion temperature
 	 */
 	private double extrusionTemp;
 	
 	/**
 	 * The extrusion width in XY
 	 */
 	private double extrusionSize;
 	
 	/**
 	 * The extrusion height in Z
 	 * Should this be a machine-wide constant? - AB
 	 */
 	private double extrusionHeight;
 	
 	
 	/**
 	 * The step between infill tracks
 	 */
 	private double extrusionInfillWidth;
 	
 	/**
 	 * The number of mm to stop extruding before the end of a track
 	 */
 	private double extrusionOverRun;
 	
 	/**
 	 * The number of s to cool between layers
 	 */
 	private int coolingPeriod; 
 	
 	/**
 	 * The speed of movement in XY when depositing
 	 */
 	private int xySpeed;
 	
 	/**
 	 * Zero torque speed
 	 */
 	private int t0;      
 	
 	/**
 	 * Infill speed [0,1]*maxSpeed
 	 */
 	private double iSpeed;
 	
 	/**
 	 * Outline speed [0,1]*maxSpeed
 	 */
 	private double oSpeed;	
 	
 	/**
 	 * Length (mm) to speed up round corners
 	 */
 	private double asLength; 
 	
 	/**
 	 * Factor by which to speed up round corners
 	 */
 	private double asFactor;
 	
 	/**
 	 * Line length below which to plot faster
 	 */
 	private double shortLength;
 	
 	/**
 	 *Factor for short line speeds
 	 */
 	private double shortSpeed;
 	
 	/**
 	 * The name of this extruder's material
 	 */
 	private String materialType;
 	
 	/**
 	 * Number of mm to overlap the hatching infill with the outline.  0 gives none; -ve will 
      * leave a gap between the two
 	 */
 	private double infillOverlap = 0;
 	
 	/**
 	 * Where to put the nozzle
 	 */
 	private double offsetX, offsetY, offsetZ;
 	
 	/**
 	 * 
 	 */
 	private long lastTemperatureUpdate = 0;
 	
 	
 	
 	/** 
 	 * Flag indicating if initialisation succeeded.  Usually this
 	 * indicates if the extruder is present in the network.
 	 */
 	private boolean isCommsAvailable = false;
 
 	
 	/**
 	 *  The colour black
 	 */	
 	protected static final Color3f black = new Color3f(0, 0, 0);
 	
 	/**
 	 *  The colour of the material to use in the simulation windows 
 	 */	
 	private Appearance materialColour;
 	
 	/**
 	 * Enable wiping procedure for nozzle
 	 */
 	private boolean nozzleWipeEnabled;
 	
 	/**
 	 * Co-ordinates for the nozzle wiper
 	 */
 	private double nozzleWipeDatumX;
 	private double nozzleWipeDatumY;
 	
 	/**
 	 * X Distance to move nozzle over wiper
 	 */
 	private double nozzleWipeStrokeX;
 	
 	/**
 	 * Y Distance to move nozzle over wiper
 	 */
 	private double nozzleWipeStrokeY;
 	
 	/**
 	 * Number of wipes per procedure
 	 */
 	private int nozzleWipeFreq;
 	
 	/**
 	 * Number of seconds to run to re-start the nozzle before a wipe
 	 */
 	private double nozzleClearTime;
 	
 	/**
 	 * Start polygons at random perimiter points
 	 */
 	private boolean randSt = false;
 	
 	/**
 	 * Start polygons at incremented perimiter points 
 	 */
 	private boolean incrementedSt = false;
 
 	/**
 	 * The number of milliseconds to wait before starting a border track
 	 */
 	private int extrusionDelayForBorder = 0;
 	
 	/**
 	 * The number of milliseconds to wait before starting a hatch track
 	 */
 	private int extrusionDelayForHatch = 0;
 	
 	/**
 	 * @param prefs
 	 * @param extruderId
 	 */
 	public NullExtruder(Preferences prefs, int extruderId) {
 
 		String prefName = "Extruder" + extruderId + "_";
 		
 		beta = prefs.loadDouble(prefName + "Beta(K)");
 		rz = prefs.loadDouble(prefName + "Rz(ohms)");
 		cap = prefs.loadDouble(prefName + "Capacitor(F)");
 		hm = prefs.loadDouble(prefName + "hm(C/pwr)");
 		hb = prefs.loadDouble(prefName + "hb(C)");
 		maxExtruderSpeed = prefs.loadInt(prefName + "MaxSpeed(0..255)");
 		extrusionSpeed = prefs.loadInt(prefName + "ExtrusionSpeed(0..255)");
 		extrusionTemp = prefs.loadDouble(prefName + "ExtrusionTemp(C)");
 		extrusionSize = prefs.loadDouble(prefName + "ExtrusionSize(mm)");
 		extrusionHeight = prefs.loadDouble(prefName + "ExtrusionHeight(mm)");
 		extrusionInfillWidth = prefs.loadDouble(prefName + "ExtrusionInfillWidth(mm)");
 		extrusionOverRun = prefs.loadDouble(prefName + "ExtrusionOverRun(mm)");
 		coolingPeriod = prefs.loadInt(prefName + "CoolingPeriod(s)");
 		xySpeed = prefs.loadInt(prefName + "XYSpeed(0..255)");
 		t0 = prefs.loadInt(prefName + "t0(0..255)");
 		iSpeed = prefs.loadDouble(prefName + "InfillSpeed(0..1)");
 		oSpeed = prefs.loadDouble(prefName + "OutlineSpeed(0..1)");
 		asLength = prefs.loadDouble(prefName + "AngleSpeedLength(mm)");
 		asFactor = prefs.loadDouble(prefName + "AngleSpeedFactor(0..1)");
 		materialType = prefs.loadString(prefName + "MaterialType(name)");
 		offsetX = prefs.loadDouble(prefName + "OffsetX(mm)");
 		offsetY = prefs.loadDouble(prefName + "OffsetY(mm)");
 		offsetZ = prefs.loadDouble(prefName + "OffsetZ(mm)");
 		nozzleWipeEnabled = prefs.loadBool(prefName + "NozzleWipeEnabled");
 		nozzleWipeDatumX = prefs.loadDouble(prefName + "NozzleWipeDatumX(mm)");
 		nozzleWipeDatumY = prefs.loadDouble(prefName + "NozzleWipeDatumY(mm)");
 		nozzleWipeStrokeX = prefs.loadDouble(prefName + "NozzleWipeStrokeX(mm)");
 		nozzleWipeStrokeY = prefs.loadDouble(prefName + "NozzleWipeStrokeY(mm)");
 		nozzleWipeFreq = prefs.loadInt(prefName + "NozzleWipeFreq");
 		nozzleClearTime = prefs.loadDouble(prefName + "NozzleClearTime(s)");
 		randSt = prefs.loadBool(prefName + "RandomStart");
 		incrementedSt = prefs.loadBool(prefName + "IncrementedStart");
 		shortLength = prefs.loadDouble(prefName + "ShortLength(mm)");
 		shortSpeed = prefs.loadDouble(prefName + "ShortSpeed(0..1)");
 		infillOverlap = prefs.loadDouble(prefName + "InfillOverlap(mm)");
		extrusionDelayForBorder = prefs.loadInt(prefName + "ExtrusionDelay(ms)");
		extrusionDelayForHatch = prefs.loadInt(prefName + "ExtrusionDelay(ms)");
 
 		
 		materialColour = getAppearanceFromNumber(extruderId);		
 			
 		isCommsAvailable = true;
 	
 	}
 	
 	/**
 	 * @return the material type
 	 */
     public String toString() { return materialType; }
 	
 	/* (non-Javadoc)
 	 * @see org.reprap.Extruder#dispose()
 	 */
 	public void dispose() {
 		if (pollThread != null) {
 			pollThreadExiting = true;
 			pollThread.interrupt();
 		}
 	}
 
 	/**
 	 * Start the extruder motor at a given speed.  This ranges from 0
 	 * to 255 but is scaled by maxSpeed and t0, so that 255 corresponds to the
 	 * highest permitted speed.  It is also scaled so that 0 would correspond
 	 * with the lowest extrusion speed.
 	 * @param speed The speed to drive the motor at (0-255)
 	 * @throws IOException
 	 */
 	public void setExtrusion(int speed) throws IOException {
 		setExtrusion(speed, false);
 	}
 	
 	/**
 	 * Start the extruder motor at a given speed.  This ranges from 0
 	 * to 255 but is scaled by maxSpeed and t0, so that 255 corresponds to the
 	 * highest permitted speed.  It is also scaled so that 0 would correspond
 	 * with the lowest extrusion speed.
 	 * @param speed The speed to drive the motor at (0-255)
 	 * @param reverse If set, run extruder in reverse
 	 * @throws IOException
 	 */
 	public void setExtrusion(int speed, boolean reverse) throws IOException {
 		// Assumption: Between t0 and maxSpeed, the speed is fairly linear
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Extruder#heatOn()
 	 */
 	public void heatOn() throws Exception 
 	{
 		
 	}
 	
 	/**
 	 * Set extruder temperature
 	 * @param temperature
 	 * @throws Exception
 	 */
 	public void setTemperature(double temperature) throws Exception {
 		
 	}
 	
 	/**
 	 * Set a heat output power.  For normal production use you would
 	 * normally call setTemperature, however this method may be useful
 	 * for lower temperature profiling, etc.
 	 * @param heat Heater power (0-255)
 	 * @param maxTemp Cutoff temperature in celcius
 	 * @throws IOException
 	 */
 	public void setHeater(int heat, double maxTemp) throws IOException {
 
 	}
 	
 
 	/**
 	 * Check if the extruder is out of feedstock
 	 * @return true if there is no material remaining
 	 */
 	public boolean isEmpty() {
 		return currentMaterialOutSensor;
 	}
 	
 
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Extruder#getTemperatureTarget()
 	 */
 	public double getTemperatureTarget() {
 		return requestedTemperature;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Extruder#getDefaultTemperature()
 	 */
 	public double getDefaultTemperature() {
 		return extrusionTemp;
 	}	
 
 	
 	/* (non-Javadoc)
 	 * @see org.reprap.Extruder#getTemperature()
 	 */
 	public double getTemperature() {
 		return currentTemperature;
 	}
 
 	/**
 	 * The the outline speed and the infill speed [0,1]
 	 */
 	public double getInfillSpeed()
 	{
 		return iSpeed;
 	}
 	public double getOutlineSpeed()
 	{
 		return oSpeed;
 	}
 	
 	/**
 	 * The length in mm to speed up when going round corners
 	 */
 	public double getAngleSpeedUpLength()
 	{
 		return asLength;
 	}
 	
 	/**
 	 * The factor by which to speed up when going round a corner.
 	 * The formula is speed = baseSpeed*[1 - 0.5*(1 + ca)*getAngleSpeedFactor()]
 	 * where ca is the cos of the angle between the lines.  So it goes fastest when
 	 * the line doubles back on itself (returning 1), and slowest when it 
 	 * continues straight (returning 1 - getAngleSpeedFactor()).
 	 */	
 	public double getAngleSpeedFactor()
 	{
 		return asFactor;
 	}	
 	
 	
 	/* (non-Javadoc)
 	 * @see org.reprap.Extruder#setCooler(boolean)
 	 */
 	public void setCooler(boolean f) throws IOException {
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Extruder#isAvailable()
 	 */
 	public boolean isAvailable() {
 		return isCommsAvailable;
 	}
 
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getXYSpeed()
      */
     public int getXYSpeed()
     {
     	return xySpeed;
     }
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getExtruderSpeed()
      */
     public int getExtruderSpeed()
     {
     	return extrusionSpeed;
     } 
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getExtrusionSize()
      */
     public double getExtrusionSize()
     {
     	return extrusionSize;
     } 
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getExtrusionHeight()
      */
     public double getExtrusionHeight()
     {
     	return extrusionHeight;
     } 
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getExtrusionInfillWidth()
      */
     public double getExtrusionInfillWidth()
     {
     	return extrusionInfillWidth;
     } 
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getExtrusionOverRun()
      */
     public double getExtrusionOverRun()
     {
     	return extrusionOverRun;
     } 
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getCoolingPeriod()
      */
     public int getCoolingPeriod()
     {
     	return coolingPeriod;
     } 
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getOffsetX()
      */
     public double getOffsetX()
     {
     	return offsetX;
     }    
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getOffsetY()
      */
     public double getOffsetY()
     {
     	return offsetY;
     }
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getOffsetZ()
      */
     public double getOffsetZ()
     {
     	return offsetZ;
     }
     
     /* (non-Javadoc)
      * @see org.reprap.Extruder#getColour()
      */     
     public Appearance getAppearance()
     {
     	return materialColour;
     }  
     
     public static int getNumberFromMaterial(String material)
     {
     	String[] names;
 		try
 		{
 			names = Preferences.allMaterials();
 			for(int i = 0; i < names.length; i++)
 			{
 				if(names[i].equals(material))
 					return i;
 			}
 
 		} catch (Exception ex)
 		{
 			System.err.println(ex.toString());
 		}
 		return -1;
     }
     
     public static Appearance getAppearanceFromNumber(int n)
     {
     	String prefName = "Extruder" + n + "_";
     	Color3f col = null;
 		try
 		{
 			col = new Color3f((float)Preferences.loadGlobalDouble(prefName + "ColourR(0..1)"), 
 				(float)Preferences.loadGlobalDouble(prefName + "ColourG(0..1)"), 
 				(float)Preferences.loadGlobalDouble(prefName + "ColourB(0..1)"));
 		} catch (Exception ex)
 		{
 			System.err.println(ex.toString());
 		}
 		Appearance a = new Appearance();
 		a.setMaterial(new Material(col, black, col, black, 101f));
 		return a;
     }
     
     public static Appearance getAppearanceFromMaterial(String material)
     {
     	return(getAppearanceFromNumber(getNumberFromMaterial(material)));
     }
     
 	 /**
      * @return determine whether nozzle wipe method is enabled or not 
      */
     public boolean getNozzleWipeEnabled()
     {
     	return nozzleWipeEnabled;
     }    
     
     
     /**
      * @return the X-cord for the nozzle wiper
      */
     public double getNozzleWipeDatumX()
     {
     	return nozzleWipeDatumX;
     }
 
     /**
      * @return the Y-cord for the nozzle wiper
      */
     public double getNozzleWipeDatumY()
     {
     	return nozzleWipeDatumY;
     }
     
     /**
      * @return the length of the nozzle movement over the wiper
      */
     public double getNozzleWipeStrokeX()
     {
     	return nozzleWipeStrokeX;
     }
     
     /**
      * @return the length of the nozzle movement over the wiper
      */
     public double getNozzleWipeStrokeY()
     {
     	return nozzleWipeStrokeY;
     }
     
     /**
      * @return the number of times the nozzle moves over the wiper
      */
     public int getNozzleWipeFreq()
     {
     	return nozzleWipeFreq;
     }
     
     /**
      * @return the time to extrude before wiping the nozzle
      */
     public double getNozzleClearTime()
     {
     	return nozzleClearTime;
     }
     
     
     /**
      * Start polygons at a random location round their perimiter
      * @return
      */
     public boolean randomStart()
     {
     	return randSt;
     }
     
     /**
      * Start polygons at a incremented location round their perimiter
      * @return
      */
     public boolean incrementedStart()
     {
     	return incrementedSt;
     }
     
     /**
      * get short lengths which need to be plotted faster
      * set -ve to turn this off.
      * @return
      */
     public double getShortLength()
     {
     	return shortLength; 
     }
     
     /**
      * Factor (between 0 and 1) to use to set the speed for
      * short lines.
      * @return
      */
     public double getShortSpeed()
     {
     	return shortSpeed; 
     }
     
     /**
      * Number of mm to overlap the hatching infill with the outline.  0 gives none; -ve will 
      * leave a gap between the two
      * @return
      */
     public double getInfillOverlap()
     {
     	return infillOverlap;
     }
     
     /**
 	 * Gets the number of milliseconds to wait before starting a border track
 	 * @return
      */
     public int getExtrusionDelayForBorder()
     {
     	return extrusionDelayForBorder; 
     }
     
     /**
 	 * Gets the number of milliseconds to wait before starting a hatch track
 	 * @return
      */
     public int getExtrusionDelayForHatch()
     {
     	return extrusionDelayForHatch; 
     }
 }
