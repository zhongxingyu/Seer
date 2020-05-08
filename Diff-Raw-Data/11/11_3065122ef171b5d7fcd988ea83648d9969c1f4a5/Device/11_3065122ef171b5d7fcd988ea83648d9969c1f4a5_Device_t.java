 /**
  * @author Mateusz Szygenda
  *
  */
 package line6;
 import javax.sound.midi.*;
 
 import java.util.*;
 import line6.commands.*;
 
 public class Device implements CommandArrived {
 	protected DeviceSettings activePreset;
 	protected String name;
 	private MidiDevice midiDeviceInput;
 	private MidiDevice midiDeviceOutput;
 	private ArrayList<DeviceSettings> presets;
 	private SyncingThread syncThread;
 	private boolean devSynchronized;
 	private DeviceReceiver deviceReceiver;
 	protected DeviceInformation deviceInfo;
 	private boolean initialized;
 		
 	public Device(MidiDevice input, MidiDevice output)
 	{
 		initialized = false;
 		devSynchronized = false;
 		midiDeviceInput = input;
 		midiDeviceOutput = output;
 		activePreset = new DeviceSettings();
 		presets = new ArrayList<DeviceSettings>();
 		syncThread = new SyncingThread();
 		if(midiDeviceInput != null && midiDeviceOutput != null)
 		{
			name = String.format("%s %s", 
					midiDeviceInput.getDeviceInfo().getDescription(),
					midiDeviceInput.getDeviceInfo().getName());
 			deviceReceiver = new DeviceReceiver(this);
 			init();
 			if(name.contains("Pocket POD"))
 			{
 				deviceInfo = DeviceInformation.PocketPOD;
 			}
 			else
 				deviceInfo = DeviceInformation.Default;
 		}
 		else
 			name = "unknown";
 	}
 	
 	public void commandArrived(Command c)
 	{
 		System.out.println("Received command");
 		if(c instanceof ChangeParameterCommand)
 		{
 			ChangeParameterCommand command = (ChangeParameterCommand)c;
 			activePreset.setValue(command.getParameter(), command.getValue());
 		}
 	}
 	
 	protected void finalize()
 	{
 		if(midiDeviceInput != null)
 			midiDeviceInput.close();
 		if(midiDeviceOutput != null)
 			midiDeviceOutput.close();
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public ArrayList<Command> getReceivedCommands()
 	{
 		return deviceReceiver.getReceivedCommands();
 	}
 	
 	protected boolean init()
 	{
 		if(!initialized)
 		{
 			if(midiDeviceInput == null || midiDeviceOutput == null) {
 				initialized = false;
 				return false;
 			}
 			else
 			{
 				try
 				{
 					midiDeviceInput.open();
 					midiDeviceOutput.open();
 					midiDeviceOutput.getTransmitter().setReceiver(deviceReceiver);
 				}
 				catch (MidiUnavailableException e)
 				{
 					System.out.println("MidiUnavailable exception during device initialization");
 					return false;
 				}
 				if(midiDeviceInput.isOpen() && midiDeviceOutput.isOpen())
 					initialized = true;
 				else
 					initialized = false;
 				return initialized;
 			}
 		}
 		else
 			return true;
 	}
 	
 	public boolean isInitialized()
 	{
 		return initialized;
 	}
 	
 	public boolean isSynchronized()
 	{
 		return devSynchronized;
 	}
 	
 	public boolean sendCommand(Command c)
 	{
 		if(isInitialized())
 		{
 			try {
 				midiDeviceInput.getReceiver().send(c.toMidiMessage(),-1);
 			} catch (MidiUnavailableException e) {
 				return false;
 			}
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	public void stopSynchronize()
 	{
 		if(syncThread.isAlive())
 		{
 			syncThread.stopSync();
 		}
 	}
 	
 	public void synchronize()
 	{
 		if(syncThread.isAlive())
 		{
 			System.out.println("Device sync is started already");
 		}
 		else
 		{
 			syncThread.start();
 		}
 	}
 	/**
 	 * Thread that synchronise device(downloads presets)
 	 * @author szygi
 	 *
 	 */
 	class SyncingThread extends Thread
 	{
 		private boolean continueWork;
 		public void run()
 		{
 			continueWork = true;
 			
 			presets.clear();
 			for(int i = 0, count = deviceInfo.getPresetsCount(); i < count; i++)
 			{
 				//Command to get preset details
 			}
 			if(presets.size() == deviceInfo.getPresetsCount())
 			{
 				System.out.printf("Device \'%s\' is synchronized\n", name);
 				devSynchronized = true;
 			}
 		}
 		
 		public void stopSync()
 		{
 			continueWork = false;
 		}
 	}
 	
 	public String toString()
 	{
 		return getName();
 	}
 }
