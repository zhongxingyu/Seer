 // Copyright 2011-2012, Art Hare
 // This file is part of WifiLapper.
 
 //WifiLapper is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 
 //WifiLapper is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 
 //You should have received a copy of the GNU General Public License
 //along with WifiLapper.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.artsoft.wifilapper;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringBufferInputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.UUID;
 
 import com.artsoft.wifilapper.Utility.MultiStateObject.STATE;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.os.Debug;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.Toast;
 
 public class OBDThread extends Thread implements Runnable 
 {
 	private BluetoothAdapter ba;
 	private String strDeviceName;
 	private OBDListener listener;
 	private Utility.MultiStateObject pStateMan;
 	private boolean m_fShutdown;
 	private final int rgSelectedPIDs[];
 	private int cResponses = 0;
 	private long tmFirstQuery = 0;
 	
 	public OBDThread(OBDListener listener, Utility.MultiStateObject pStateMan, String strDeviceName, int rgSelectedPIDs[])
 	{
 		this.pStateMan = pStateMan;
 		this.strDeviceName = strDeviceName;
 		this.listener = listener;
 		this.rgSelectedPIDs = rgSelectedPIDs;
 		ba = BluetoothAdapter.getDefaultAdapter();
 		pStateMan.SetState(OBDThread.class, STATE.TROUBLE_GOOD, "Starting...");
 		start();
 	}
 	public void Shutdown()
 	{
 		m_fShutdown = true;
 	}
 	private static String GetResponse(InputStream in, long timeout) throws IOException
 	{
 		long tmStart = System.currentTimeMillis();
 		StringBuilder strBuilder = new StringBuilder(100);
 		while(true)
 		{
 			int c = in.read();
 			if(c == '>') break;
 			if(System.currentTimeMillis() - tmStart > timeout) return strBuilder.toString();
 			strBuilder.append((char)c);
 		}
 		return strBuilder.toString();
 	}
 	
 	private void ParseAndSendResponse(String strResponse)
 	{
 		StringTokenizer tokenLines = new StringTokenizer(strResponse, "\r");
 		
 		while(tokenLines.hasMoreElements())
 		{
 			StringTokenizer tokens = new StringTokenizer(tokenLines.nextToken());
 			
 			while(tokens.hasMoreTokens())
 			{
 				String strNext = tokens.nextToken();
 				if(strNext.equals("41"))
 				{
 					if(tmFirstQuery == 0) tmFirstQuery = System.currentTimeMillis();
 					cResponses++;
 					
 					// this is an actual response
 					if(tokens.hasMoreTokens())
 					{
 						strNext = tokens.nextToken();
 						final int ixPID = Integer.parseInt(strNext,16);
 						
 						switch(ixPID)
 						{
 						// this group is all those whose value is [(A*100)/255]
 						case 0x4: // calculated engine load (%)
 						case 0x11: // throttle position
 						case 0x2C: // commanded EGR
 						case 0x2E: // commanded evaporative purge
 						case 0x2F: // fuel level input
 						case 0x45: // relative throttle position
 						case 0x47: // absolute throttle position B
 						case 0x48: // absolute throttle position C 
 						case 0x49: // absolute throttle position D
 						case 0x4A: // absolute throttle position E
 						case 0x4B: // absolute throttle position F
 						case 0x4C: // commanded throttle actuator
 						case 0x52: // ethanol fuel %
 						case 0x5a: // relative accelerator pedal position
 						case 0x5B: // hybrid battery pack remaining lifedon
 							if(tokens.hasMoreTokens())
 							{
 								final int a = Integer.parseInt(tokens.nextToken(), 16);
 								listener.NotifyOBDParameter(ixPID, (a*100.0f)/255.0f);
 							}
 							break;
 						case 0x5: // engine coolant temp
 						case 0xF: // intake air temperature
 						case 0x46: // ambient air temperature
 						case 0x5c: // engine oil temperature
 							if(tokens.hasMoreTokens())
 							{
 								final int a = Integer.parseInt(tokens.nextToken(), 16);
 								listener.NotifyOBDParameter(ixPID, a-40);
 							}
 							break;
 						case 0x6: // short term fuel % trim (bank 1)
 						case 0x7: // long term fuel % trim (bank 1)
 						case 0x8: // short term fuel % trim (bank 2)
 						case 0x9: // long term fuel % trim (bank 2)
 						case 0x2D: // EGR error
 							if(tokens.hasMoreTokens())
 							{
 								final float a = (float)Integer.parseInt(tokens.nextToken(),16);
 								listener.NotifyOBDParameter(ixPID, (a-128.0f)*(100.0f/128.0f));
 							}
 							break;
 						case 0xa: // fuel pressure
 							if(tokens.hasMoreTokens())
 							{
 								final int a = Integer.parseInt(tokens.nextToken(), 16);
 								listener.NotifyOBDParameter(ixPID, a*3);
 							}
 							break;
 						case 0xb: // intake manifold absolute pressure
 						case 0xd: // vehicle speed
 						case 0x30: // # of warmups since codes cleared
 						case 0x33: // barometric pressure
 							if(tokens.hasMoreTokens())
 							{
 								final int a = Integer.parseInt(tokens.nextToken(), 16);
 								listener.NotifyOBDParameter(ixPID, a);
 							}
 							break;
 						case 0xe: // timing advance
 							if(tokens.hasMoreTokens())
 							{
 								final float a = (float)Integer.parseInt(tokens.nextToken(), 16);
 								listener.NotifyOBDParameter(ixPID, a/2.0f - 64.0f);
 							}
 							break;
 						case 0x10: // MAF air flow rate
 							if(tokens.hasMoreTokens())
 							{
 								final float a = (float)Integer.parseInt(tokens.nextToken(),16);
 								if(tokens.hasMoreTokens())
 								{
 									final float b = (float)Integer.parseInt(tokens.nextToken(),16);
 									final float flResult = (a*256.0f + b)/100.0f;
 									listener.NotifyOBDParameter(ixPID, flResult);
 								}
 							}
 							break;
 						case 0x14: // b1s1 short term fuel trim
 						case 0x15: // b1s2 short term fuel trim
 						case 0x16: // b1s3 short term fuel trim
 						case 0x17: // b1s4 short term fuel trim
 						case 0x18: // b2s1 short term fuel trim
 						case 0x19: // b2s2 short term fuel trim
 						case 0x1a: // b2s3 short term fuel trim
 						case 0x1b: // b2s4 short term fuel trim
 							if(tokens.hasMoreTokens())
 							{
 								final float a = (float)Integer.parseInt(tokens.nextToken(),16);
 								if(tokens.hasMoreTokens())
 								{
 									final float b = (float)Integer.parseInt(tokens.nextToken(),16);
 									listener.NotifyOBDParameter(ixPID, (b-128.0f)*(100.0f/128.0f));
 								}
 							}
 							break;
 						case 0x1f: // run time since engine start
 						case 0x21: // distance travelled with MIL on
 						case 0x31: // distance travelled since codes cleared
 						case 0x4d: // time run with MIL on
 						case 0x4e: // time since trouble codes cleared
 						case 0x63: // engine reference torque
 							if(tokens.hasMoreTokens())
 							{
 								final float a = (float)Integer.parseInt(tokens.nextToken(),16);
 								if(tokens.hasMoreTokens())
 								{
 									final float b = (float)Integer.parseInt(tokens.nextToken(),16);
 									listener.NotifyOBDParameter(ixPID, a*256 + b);
 								}
 							}
 							break;
 						}
 						
 					}
 				}
 			}
 		}
 	}
 	
 	// returns true if we have to restart the main loop due to failures
 	private boolean DoQueries(byte[][] strQueries, OutputStream os, InputStream in) throws IOException
 	{
 		String strResponse;
 		for(int x = 0;x < strQueries.length; x++)
 		{
 			os.write(strQueries[x]);
 			strResponse = GetResponse(in,5000);
 			if(strResponse == null) 
 			{
 				return true;
 			}
 			ParseAndSendResponse(strResponse);
 		}
 		return false;
 	}
 	
 	// ix = the base of the PIDs that strResponse represents.  Ex: 0x00 for the first 32, 0x20 for the next 32, etc
 	// strResponse = the thing we're parsing from the OBD unit
 	// rgSupport[x] = whether we support the x'th PID
 	private static void PopulateSupport(int ix, String strResponse, boolean rgSupport[])
 	{
 		String strLines[] = strResponse.split("\r");
 		String strTokens[] = null;
 		for(int x = 0;x < strLines.length; x++)
 		{
 			strTokens = strLines[x].split(" ");
 			if(strTokens[0].equals("41"))
 			{
 				// success
 				break;
 			}
 			else
 			{
 				strTokens = null;
 				continue;
 			}
 		}
 		
		if(strTokens.length != 6) return;
 		
 		String strHex = "";
 		for(int x = 2; x < 6; x++)
 		{
 			strHex += strTokens[x];
 		}
 		final short A = Short.parseShort(strTokens[2],16);
 		final short B = Short.parseShort(strTokens[3],16);
 		final short C = Short.parseShort(strTokens[4],16);
 		final short D = Short.parseShort(strTokens[5],16);
 		
 		// PIDS 1-8: bits A7...A0
 		for(int x = 1;x <= 8; x++)
 		{
 			int i = 8 - x;
 			rgSupport[ix + x] = ( (A&(1<<i)) != 0);
 		}
 		// PIDS 9-17: bits B7...B0
 		for(int x = 9;x <= 16; x++)
 		{
 			int i = 16 - x;
 			rgSupport[ix + x] = ( (B&(1<<i)) != 0);
 		}
 		// PIDS 17-24: bits C7...C0
 		for(int x = 17;x <= 24; x++)
 		{
 			int i = 24 - x;
 			rgSupport[ix + x] = ( (C&(1<<i)) != 0);
 		}
 		// PIDS 25-32: bits D7...D0
 		for(int x = 25;x <= 32; x++)
 		{
 			int i = 32 - x;
 			rgSupport[ix + x] = ( (D&(1<<i)) != 0);
 		}
 	}
 	
 	public static class PIDParameter
 	{
 		public PIDParameter(int ixCode, String strDesc)
 		{
 			this.ixCode = ixCode;
 			this.strDesc = strDesc;
 		}
 		@Override
 		public String toString()
 		{
 			return strDesc;
 		}
 		public int ixCode;
 		public String strDesc;
 	}
 	private static final String rgPIDStrings[] =
 	{
 		"PIDs supported [01 - 20]",
 		"Monitor status since DTCs cleared. (Includes malfunction indicator lamp (MIL) status and number of DTCs.)",
 		"FreezeDTC",
 		"Fuel system status",
 		"Calculated engine load value",
 		"Engine coolant temperature",
 		"Short term fuel% trimBank 1",
 		"Long term fuel% trimBank 1",
 		"Short term fuel% trimBank 2",
 		"Long term fuel% trimBank 2",
 		"Fuel pressure",
 		"Intake manifold absolute pressure",
 		"Engine RPM",
 		"Vehicle speed",
 		"Timing advance",
 		"Intake air temperature",
 		"MAF air flow rate",
 		"Throttle position",
 		"Commanded secondary air status",
 		"Oxygen sensors present",
 		"Bank 1, Sensor 1: Oxygen sensor voltage, Short term fuel trim",
 		"Bank 1, Sensor 2: Oxygen sensor voltage, Short term fuel trim",
 		"Bank 1, Sensor 3: Oxygen sensor voltage, Short term fuel trim",
 		"Bank 1, Sensor 4: Oxygen sensor voltage, Short term fuel trim",
 		"Bank 2, Sensor 1: Oxygen sensor voltage, Short term fuel trim",
 		"Bank 2, Sensor 2: Oxygen sensor voltage, Short term fuel trim",
 		"Bank 2, Sensor 3: Oxygen sensor voltage, Short term fuel trim",
 		"Bank 2, Sensor 4: Oxygen sensor voltage, Short term fuel trim",
 		"OBD standards this vehicle conforms to",
 		"Oxygen sensors present",
 		"Auxiliary input status",
 		"Run time since engine start",
 		"PIDs supported [21 - 40]",
 		"Distance traveled with malfunction indicator lamp (MIL) on",
 		"Fuel Rail Pressure (relative to manifold vacuum)",
 		"Fuel Rail Pressure (diesel, or gasoline direct inject)",
 		"O2S1_WR_lambda(1): Equivalence Ratio Voltage",
 		"O2S2_WR_lambda(1): Equivalence Ratio Voltage",
 		"O2S3_WR_lambda(1): Equivalence Ratio Voltage",
 		"O2S4_WR_lambda(1): Equivalence Ratio Voltage",
 		"O2S5_WR_lambda(1): Equivalence Ratio Voltage",
 		"O2S6_WR_lambda(1): Equivalence Ratio Voltage",
 		"O2S7_WR_lambda(1): Equivalence Ratio Voltage",
 		"O2S8_WR_lambda(1): Equivalence Ratio Voltage",
 		"Commanded EGR",
 		"EGR Error",
 		"Commanded evaporative purge",
 		"Fuel Level Input",
 		"# of warm-ups since codes cleared",
 		"Distance traveled since codes cleared",
 		"Evap. System Vapor Pressure",
 		"Barometric pressure",
 		"O2S1_WR_lambda(1): Equivalence Ratio Current",
 		"O2S2_WR_lambda(1): Equivalence Ratio Current",
 		"O2S3_WR_lambda(1): Equivalence Ratio Current",
 		"O2S4_WR_lambda(1): Equivalence Ratio Current",
 		"O2S5_WR_lambda(1): Equivalence Ratio Current",
 		"O2S6_WR_lambda(1): Equivalence Ratio Current",
 		"O2S7_WR_lambda(1): Equivalence Ratio Current",
 		"O2S8_WR_lambda(1): Equivalence Ratio Current",
 		"Catalyst Temperature Bank 1, Sensor 1",
 		"Catalyst Temperature Bank 2, Sensor 1",
 		"Catalyst Temperature Bank 1, Sensor 2",
 		"Catalyst Temperature Bank 2, Sensor 2",
 		"PIDs supported [41 - 60]",
 		"Monitor status this drive cycle",
 		"Control module voltage",
 		"Absolute load value",
 		"Command equivalence ratio",
 		"Relative throttle position",
 		"Ambient air temperature",
 		"Absolute throttle position B",
 		"Absolute throttle position C",
 		"Accelerator pedal position D",
 		"Accelerator pedal position E",
 		"Accelerator pedal position F",
 		"Commanded throttle actuator",
 		"Time run with MIL on",
 		"Time since trouble codes cleared",
 		"Maximum value for equivalence ratio, oxygen sensor voltage, oxygen sensor current, and intake manifold absolute pressure",
 		"Maximum value for air flow rate from mass air flow sensor",
 		"Fuel Type",
 		"Ethanol fuel%",
 		"Absolute Evap system Vapour Pressure",
 		"Evap system vapor pressure",
 		"Short term secondary oxygen sensor trim bank 1 and bank 3",
 		"Long term secondary oxygen sensor trim bank 1 and bank 3",
 		"Short term secondary oxygen sensor trim bank 2 and bank 4",
 		"Long term secondary oxygen sensor trim bank 2 and bank 4",
 		"Fuel rail pressure (absolute)",
 		"Relative accelerator pedal position",
 		"Hybrid battery pack remaining life",
 		"Engine oil temperature",
 		"Fuel injection timing",
 		"Engine fuel rate",
 		"Emission requirements to which vehicle is designed",
 		"PIDs supported [61 - 80]",
 		"Driver's demand engine - percent torque",
 		"Actual engine - percent torque",
 		"Engine reference torque",
 		"Engine percent torque data",
 		"Auxiliary input / output supported",
 		"Mass air flow sensor",
 		"Engine coolant temperature",
 		"Intake air temperature sensor",
 		"Commanded EGR and EGR Error",
 		"Commanded Diesel intake air flow control and relative intake air flow position",
 		"Exhaust gas recirculation temperature",
 		"Commanded throttle actuator control and relative throttle position",
 		"Fuel pressure control system",
 		"Injection pressure control system",
 		"Turbocharger compressor inlet pressure",
 		"Boost pressure control",
 		"Variable Geometry turbo (VGT) control",
 		"Wastegate control",
 		"Exhaust pressure",
 		"Turbocharger RPM",
 		"Turbocharger temperature",
 		"Turbocharger temperature",
 		"Charge air cooler temperature (CACT)",
 		"Exhaust Gas temperature (EGT) Bank 1",
 		"Exhaust Gas temperature (EGT) Bank 2",
 		"Diesel particulate filter (DPF)",
 		"Diesel particulate filter (DPF)",
 		"Diesel Particulate filter (DPF) temperature",
 		"NOx NTE control area status",
 		"PM NTE control area status",
 		"Engine run time",
 		"PIDs supported [81 - A0]",
 		"Engine run time for AECD",
 		"Engine run time for AECD",
 		"NOx sensor",
 		"Manifold surface temperature",
 		"NOx reagent system",
 		"Particulate matter (PM) sensor",
 		"Intake manifold absolute pressure",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"PIDs supported [A1 - C0]",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"",
 		"PIDs supported [C1 - E0]",
 	};
 	
 	@Override
 	public void run()
 	{
 		while(!m_fShutdown)
 		{
 			boolean fRestart = false;
 			Thread.currentThread().setName("OBD2");
 			Set<BluetoothDevice> lstDevices = ba.getBondedDevices();
 			Iterator<BluetoothDevice> i = lstDevices.iterator();
 			BluetoothDevice bd = null;
 			while(i.hasNext())
 			{
 				bd = i.next();
 				if(strDeviceName.equalsIgnoreCase(bd.getName()))
 				{
 					break;
 				}
 				else
 				{
 					bd = null;
 				}
 			}
 			if(bd == null)
 			{
 				pStateMan.SetState(OBDThread.class, STATE.TROUBLE_BAD, "Could not find device '" + strDeviceName + "'");
 				return;
 			}
 			
 			InputStream in = null;
 			OutputStream os = null;
 			BluetoothSocket bs = null;
 			String strResponse = "";
 			boolean rgSupport[] = new boolean[256];
 			rgSupport[0] = true;
 			try
 			{
 				bs = bd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
 				bs.connect();
 				
 				in = bs.getInputStream();
 				os = bs.getOutputStream();
 				
 				final String strATZ = "atz\r";
 				os.write(strATZ.getBytes());
 				strResponse = GetResponse(in,5000);
 				if(strResponse == null) continue;
 				
 				final String strATSetup = "at sp 0\r";
 				os.write(strATSetup.getBytes());
 				strResponse = GetResponse(in,5000);
 				if(strResponse == null) continue;
 
 				final String strAT2 = "at at2\r\n"; // tries for more aggressive timing
 				os.write(strAT2.getBytes());
 				strResponse = GetResponse(in,5000);
 				if(strResponse == null) continue;
 
 				final String strFastTiming = "at st 19\r"; // tries for more aggressive timing
 				os.write(strFastTiming.getBytes());
 				strResponse = GetResponse(in,5000);
 				if(strResponse == null) continue;
 
 				final String strCancelEcho = "ate0\r"; // turns off echoes
 				os.write(strCancelEcho.getBytes());
 				strResponse = GetResponse(in,5000);
 				if(strResponse == null) continue;
 
 				final String strCancelLinefeed = "atl0\r"; // turns off linefeeds
 				os.write(strCancelLinefeed.getBytes());
 				strResponse = GetResponse(in,5000);
 				if(strResponse == null) continue;
 				
 				pStateMan.SetState(OBDThread.class, STATE.ON, "Connected to device");
 			}
 			catch(IOException e)
 			{
 				try {if(in != null) in.close(); in = null;} catch(IOException e2) {};
 				try {if(os != null) os.close(); os = null;} catch(IOException e2) {};
 				try {if(bs != null) bs.close(); bs = null;} catch(IOException e2) {};
 				fRestart = true;
 				pStateMan.SetState(OBDThread.class, STATE.TROUBLE_BAD, "Failed to connect to device '" + strDeviceName + "'");
 			}
 			
 			byte rgByteQueries[][] = new byte[this.rgSelectedPIDs.length][];
 			for(int x = 0;x < rgByteQueries.length; x++)
 			{
 				String strQuery;
 				if(rgSelectedPIDs[x] < 16)
 				{
 					strQuery = "01 0" + Integer.toHexString(rgSelectedPIDs[x]) + " 1" + (char)0xd;
 				}
 				else
 				{
 					strQuery = "01 " + Integer.toHexString(rgSelectedPIDs[x]) + " 1" + (char)0xd;
 				}
 				rgByteQueries[x] = strQuery.getBytes();
 			}
 			
 			while(!fRestart && !m_fShutdown)
 			{
 				try
 				{
 					fRestart = DoQueries(rgByteQueries, os, in);
 					
 					String strRate = "";
 					long tmTimeSinceFirst = System.currentTimeMillis() - this.tmFirstQuery;
 					if(tmTimeSinceFirst > 0)
 					{
 						float flRate = (1000.0f*this.cResponses) / (float)tmTimeSinceFirst;
 						strRate = " (" + flRate + "hz)";
 					}
 					pStateMan.SetState(OBDThread.class, STATE.ON, "Reading OBD2 device successfully" + strRate);
 				}
 				catch(IOException e)
 				{
 					
 				}
 			}
 			
 			cResponses = 0;
 			tmFirstQuery = 0;
 			
 			try { if(os != null) os.close();} catch(Exception e) {}
 			try { if(in != null) in.close();} catch(Exception e) {}
 			try { if(bs != null) bs.close();} catch(Exception e) {}
 			if(!m_fShutdown)
 			{
 				try 
 				{
 					pStateMan.SetState(OBDThread.class, STATE.TROUBLE_BAD, "Failed somehow.  Retrying connection...");
 					Thread.sleep(5000);
 				} catch (InterruptedException e) 
 				{
 				}
 			}
 		}
 		pStateMan.SetState(OBDThread.class, STATE.OFF, "Shutting down...");
 	}
 	
 	public static abstract interface OBDListener
 	{
 		public abstract void NotifyOBDParameter(int pid, float value);
 	}
 	
 
 	public static void ThdGetSupportedPIDs(String strBTName, PIDSupportListener pCallback)
 	{
 		Thread thd = new PIDSupportQueryer(strBTName, pCallback);
 		thd.start();
 	}
 	public static interface PIDSupportListener
 	{
 		public abstract void NotifyPIDSupport(List<PIDParameter> lst);
 		public abstract void NotifyOBD2Error(String str);
 	}
 	private static class PIDSupportQueryer extends Thread
 	{
 		private String m_strBTName;
 		private PIDSupportListener m_pCallback;
 		public PIDSupportQueryer(String strBTName, PIDSupportListener pCallback)
 		{
 			m_strBTName = strBTName;
 			m_pCallback = pCallback;
 		}
 		@Override
 		public void run()
 		{
 			Thread.currentThread().setName("Support Queryer");
 			
 			ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
 			
 			List<PIDParameter> lst = GetSupportedPIDS(m_strBTName, baos);
 			if(baos.size() > 0)
 			{
 				m_pCallback.NotifyOBD2Error(baos.toString());
 			}
 			m_pCallback.NotifyPIDSupport(lst);
 			
 		}
 	}
 	private static List<PIDParameter> GetSupportedPIDS(String strBTName, OutputStream errorOut)
 	{
 		boolean fSuccess = true;
 		List<PIDParameter> lstRet = null;
 
 		BluetoothSocket bs = null;
 		InputStream in = null;
 		OutputStream os = null;
 		
 		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
 		
 		Set<BluetoothDevice> lstDevices = ba.getBondedDevices();
 		Iterator<BluetoothDevice> i = lstDevices.iterator();
 		BluetoothDevice bd = null;
 		while(i.hasNext())
 		{
 			bd = i.next();
 			if(strBTName.equalsIgnoreCase(bd.getName()))
 			{
 				break;
 			}
 			else
 			{
 				bd = null;
 			}
 		}
 		if(bd == null)
 		{
 			try {errorOut.write(("Couldn't find device " + strBTName).getBytes());} catch (IOException e1) {}
 			fSuccess = false;
 		}
 		
 		if(fSuccess)
 		{
 			String strResponse = "";
 			boolean rgSupport[] = new boolean[257];
 			rgSupport[0] = true;
 			try
 			{
 				bs = bd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
 				bs.connect();
 				
 				in = bs.getInputStream();
 				os = bs.getOutputStream();
 				
 				final String strATZ = "atz\r\n";
 				os.write(strATZ.getBytes());
 				strResponse = GetResponse(in,5000);
 				if(strResponse == null)
 				{
 					try {errorOut.write("initial setup failed".getBytes());} catch (IOException e1) {}
 					fSuccess = false;
 				}
 				if(fSuccess)
 				{
 					Log.w("obd",strResponse);
 					
 					final String strATSetup = "at sp 0\r\n";
 					os.write(strATSetup.getBytes());
 		
 					strResponse = GetResponse(in,5000);
 					if(strResponse == null) fSuccess = false;
 					if(fSuccess)
 					{
 						for(int ix = 0; ix < 0xff; ix += 0x20)
 						{
 							if(rgSupport[ix])
 							{
 								final String strSupport = "01 " + (ix == 0 ? "00" : Integer.toHexString(ix)) + (char)0xd;
 								os.write(strSupport.getBytes());
 								strResponse = GetResponse(in, 30000);
 								if(strResponse != null)
 								{
 									PopulateSupport(ix, strResponse, rgSupport);
 								}
 								else
 								{
 									try {errorOut.write("No response received ".getBytes());} catch (IOException e1) {}
 									fSuccess = false;
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 			catch(Exception e)
 			{
 				final Writer result = new StringWriter();
 	    	    final PrintWriter printWriter = new PrintWriter(result);
 	    	    e.printStackTrace(printWriter);
 	    	    String strWrite = result.toString();
 	    	    
 	    	    try
 	    	    {
 	    	    	FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/wifilapper/obdcrash.txt");
 	    	    	fos.write(strWrite.getBytes());
 	    	    	fos.close();
 	    	    	try {errorOut.write(("Wrote OBD failure report to wifilapper/obdcrash.txt").getBytes());} catch (IOException e1) {}
 	    	    }
 	    	    catch(IOException e2)
 	    	    {
 					try {errorOut.write(("For more OBD failure details, put in an SD card and mount it").getBytes());} catch (IOException e1) {}
 	    	    }
 	    	    
 				fSuccess = false;
 			}
 			
 			if(fSuccess)
 			{
 				lstRet = new ArrayList<PIDParameter>();
 				for(int x = 0;x < rgSupport.length; x++)
 				{
 					if(rgSupport[x] && x < rgPIDStrings.length)
 					{
 						lstRet.add(new PIDParameter(x, rgPIDStrings[x]));
 					}
 				}
 			}
 		}
 		
 		if(in != null)
 		{
 			try {in.close();} catch(IOException e) {}
 		}
 		if(os != null)
 		{
 			try {os.close();} catch(IOException e) {}
 		}
 		if(bs != null)
 		{
 			try {bs.close();} catch(IOException e) {}
 		}
 		
 		return lstRet;
 	}
 	
 }
