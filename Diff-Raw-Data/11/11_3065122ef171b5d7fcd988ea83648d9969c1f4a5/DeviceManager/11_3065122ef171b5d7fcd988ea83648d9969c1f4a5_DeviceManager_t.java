 /**
  * @author Mateusz Szygenda
  *
  */
 package line6;
 import javax.sound.midi.*;
 import java.util.Hashtable;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 public class DeviceManager {
 	/**
 	 * Finds all devices handled by line6 driver
 	 * 
 	 * @return list of line6 midi devices
 	 */
 	public static ArrayList<line6.Device> getAllDevices()
 	{
 		MidiDevice.Info devices[] = MidiSystem.getMidiDeviceInfo();
 		Hashtable inputInterfaces = new Hashtable();
 		Hashtable outputInterfaces= new Hashtable();
 		ArrayList<line6.Device> line6Devices = new ArrayList<line6.Device>();
 		MidiDevice tmpMidiDevice = null;
 		Device tmpDevice = null;
 		for(int i = 0, count = devices.length; i < count; i++)
 		{
			if(devices[i].getDescription().contains("Line 6") || devices[i].getName().contains("Line 6"))
 			{
 				try {
 					tmpMidiDevice = MidiSystem.getMidiDevice(devices[i]);
 					if(tmpMidiDevice != null)
 					{
 						if(checkInput(tmpMidiDevice))
 							inputInterfaces.put(devices[i].getName(),tmpMidiDevice);
 						else
 							outputInterfaces.put(devices[i].getName(), tmpMidiDevice);
 					}
 				} catch (MidiUnavailableException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		String tmpKey;
 		for(Enumeration<String> en = inputInterfaces.keys(); en.hasMoreElements();)
 		{
 			tmpKey = (String)en.nextElement();
 			System.out.println(tmpKey);
 			if(outputInterfaces.containsKey(tmpKey))
 			{
 				tmpDevice = new Device((MidiDevice)inputInterfaces.get(tmpKey),(MidiDevice)outputInterfaces.get(tmpKey));
 				line6Devices.add(tmpDevice);
 			}
 		}
 		return line6Devices;
 	}
 	
 	public static boolean checkInput(MidiDevice dev)
 	{
 		try 
 		{
 			dev.getReceiver();
 			return true;
 		}
 		catch (MidiUnavailableException e)
 		{
 			return false;
 		}
 	}
 	
 	public static boolean checkOutput(MidiDevice dev)
 	{
 		try
 		{
 			dev.getTransmitter();
 			return true;
 		}
 		catch (MidiUnavailableException e)
 		{
 			return false;
 		}
 	}
 }
