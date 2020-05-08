 /* This file was automatically generated from canbusid.m4. Do not edit. */
 package cansocket;
 
 public interface CanBusIDs
 {
 	public static final int IMUID = (0x04 << 5);
 	public static final int IMURate = (IMUID | 4);
 	public static final int IMURateDiv = (IMUID | 8);
	public static final int IMUAccel = (IMUID | (4 << 3));
	public static final int IMUGyro = (IMUID | (5 << 3));
 
 	public static final int GPSID = (0x06 << 5);
 	public static final int GPSStatus = (GPSID | 1);
 	public static final int GPSTime = (GPSID | 2);
 	public static final int GPSLatLon = (GPSID | 3);
 	public static final int GPSHeight = (GPSID | 4);
 
 	public static final int UmbilicalID = (0x08 << 5);
 	public static final int UmbilicalStatus = (UmbilicalID | 4);
 	public static final int UmbilicalReady = (UmbilicalID | 8);
 
 	public static final int ATVID = (0x10 << 5);
 	public static final int ATVStatus = (ATVID | 1);
 	public static final int ATVAmpPower = (ATVID | 2);
 	public static final int ATVOverlay = (ATVID | 3);
 }
