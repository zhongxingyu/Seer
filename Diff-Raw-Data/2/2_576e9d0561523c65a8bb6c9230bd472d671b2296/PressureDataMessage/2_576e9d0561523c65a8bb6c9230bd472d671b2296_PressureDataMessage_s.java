 package cansocket;
 
 import java.io.*;
 
 public class PressureDataMessage extends NetMessage
 {
	public static final short fifo_tag = FMT_IMU_ACCEL;
 	public final short raw_data;
 	public final double pressure, altitude; 
 
 	public PressureDataMessage(DataInputStream dis) throws IOException
 	{
 		raw_data = dis.readShort();
 		pressure = dis.readDouble();
 		altitude = dis.readDouble();
 	}
 
 	
 	public void putMessage(DataOutputStream dos)
 	{
 		try
 		{
 		dos.writeShort(raw_data);
 		dos.writeDouble(pressure);
 		dos.writeDouble(altitude);
 		} catch(IOException e) {
 			// never happens
 		}
 	}
 
 	public String toString()
 	{
 		return "Press:" + Double.toString(pressure) + " Alt:" + Double.toString(altitude);
 	}
 }
 
