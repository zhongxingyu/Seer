 /**
  * 
  */
 package ecologylab.sensor.location.gps.listener;
 
 import ecologylab.generic.Debug;
 import ecologylab.sensor.location.gps.data.GPSDatum;
 
 /**
  * @author Zachary O. Toups (toupsz@cs.tamu.edu)
  * 
  */
 public class GPSDataUpdater extends Debug implements NMEAStringListener
 {
 	protected GPSDatum	datum	= new GPSDatum();
 
 	/**
 	 * 
 	 */
 	public GPSDataUpdater()
 	{
 	}
 
 	/**
 	 * Constructs a GPSDataUpdater that updates the instance passed in.
 	 * 
 	 * @param datum
 	 *           the instance to update with NMEA strings.
 	 */
 	public GPSDataUpdater(GPSDatum datum)
 	{
 		this.datum = datum;
 	}
 
 	/**
 	 * @see ecologylab.sensor.location.gps.listener.NMEAStringListener#processIncomingNMEAString(java.lang.String)
 	 */
 	public void processIncomingNMEAString(String gpsDataString)
 	{
 		datum.integrateGPSData(gpsDataString);
 	}
 
 	/**
	 * Convience method to add a listener to the datum contained in this object.
 	 * 
 	 * @param l
 	 */
 	public void addDataUpdatedListener(GPSDataUpdatedListener l)
 	{
 		this.datum.addGPSDataUpdatedListener(l);
 	}
 
 	/**
 	 * @return the datum
 	 */
 	public GPSDatum getDatum()
 	{
 		return datum;
 	}
 }
