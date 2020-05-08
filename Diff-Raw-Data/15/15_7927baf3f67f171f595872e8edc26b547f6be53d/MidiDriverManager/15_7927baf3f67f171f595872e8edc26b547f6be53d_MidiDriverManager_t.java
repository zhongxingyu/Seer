 /*
 Mjdj MIDI Morph - an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 You may contact the author at mjdj_midi_morph [at] confusionists.com
 */
 package com.confusionists.mjdj.midi;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.sound.midi.*;
 
 import com.confusionists.mjdj.Universe;
 import com.confusionists.mjdj.fileIO.DeviceLoaderJava;
 import com.confusionists.mjdj.midi.time.InternalClock;
 import com.confusionists.mjdj.settings.MorphAdaptor;
 import com.confusionists.mjdj.settings.Settings;
 import com.confusionists.mjdj.ui.Logger;
 import com.confusionists.mjdjApi.midi.MessageWrapper;
 import com.confusionists.mjdjApi.midiDevice.DeviceUnavailableException;
 import com.confusionists.mjdjApi.midiDevice.DeviceWrapper;
 import com.confusionists.mjdjApi.midiDevice.ReceiverDeviceWrapper;
 import com.confusionists.mjdjApi.midiDevice.TransmitterDeviceWrapper;
 import com.confusionists.mjdjApi.morph.Morph;
 
 public class MidiDriverManager {
 
 	public List<ReceiverDeviceWrapper> outputReceivers;
 	public List<TransmitterDeviceWrapper> inputTransmitters;
 	private boolean comSunAvailable;
 
 	public MidiDriverManager() {
 	}
 
 	public void init() {
 		outputReceivers = new ArrayList<ReceiverDeviceWrapper>();
 		inputTransmitters = new ArrayList<TransmitterDeviceWrapper>();
 		
 		inputTransmitters.add(new InternalClock());
 
 		new DeviceLoaderJava().load(inputTransmitters, outputReceivers);
 
 		open(outputReceivers);
 		open(inputTransmitters);
 		
 		// open the real midi devices
 		MidiDevice.Info[] mdi = MidiSystem.getMidiDeviceInfo();
 
 		comSunAvailable = isComSunAvailable(mdi);
 
 		for (int i = 0; i < mdi.length; i++) {
 			MidiDevice device;
 			try {
 				device = MidiSystem.getMidiDevice(mdi[i]);
 
 				
 				if ( useDevice(device)) {
 
 					if (device.getMaxTransmitters() != 0) {
 						TransmitterWrapperImpl transmitter = new TransmitterWrapperImpl(device);
 						openTransmitter(transmitter);
 					}
 					if (device.getMaxReceivers() != 0) {
 						ReceiverWrapperImpl receiver = new ReceiverWrapperImpl(device);
 						openReceiver(receiver);
 					}
 				}
 			} catch (MidiUnavailableException mex) {
 				// it's fine Logger.log(e.getMessage());
 			} catch (DeviceUnavailableException e) {
 				Logger.log("Device unavailable", e);
 			}
 		}
 	}
 	
 	private boolean isComSunAvailable(MidiDevice.Info[] mdiInfos) {
 		for (int i = 0; i < mdiInfos.length; i++) {
 			try {
 				MidiDevice device = MidiSystem.getMidiDevice(mdiInfos[i]);
 				if (isDeviceFromSunProviders(device)) 
 					return true;
 			} catch (MidiUnavailableException e) {
 				// hide this
 			}
 		}
 		return false;
 	}
 
 	private boolean isDeviceFromSunProviders(MidiDevice device) {
 		if (device instanceof Sequencer || device instanceof Synthesizer)
 			return false; // it's no problem to get a few false negatives with this method
 		String providerPackagePrefix = device.getClass().getName();
 		int secondPeriod = providerPackagePrefix.indexOf('.', providerPackagePrefix.indexOf('.')+1);
 		return providerPackagePrefix.substring(0, secondPeriod).equals("com.sun");
 	}
 	
	static boolean warned = false;
	
 	private boolean useDevice(MidiDevice device) {
 		boolean retVal;
 		if (device instanceof Sequencer || device instanceof Synthesizer)
 			return false;
 		if (Settings.getInstance().doNotUseComSunDrivers) {
 			retVal = !isDeviceFromSunProviders(device);
			if (!retVal && !warned) {
				Logger.log("Skipping Sun drivers  (per settings file)");
				warned = true;
			}
 			return retVal;
 		}
 		else {
 			if (!comSunAvailable) 
 				return true;
 			else {
 				if (isDeviceFromSunProviders(device))
 					return true;
 				else {
					if (!warned) {
						Logger.log("Non-default drivers skipped as Sun drivers were found (per settings file)");
						warned = true;
					}
 					return false;
 				}
 			} 
 		}
 	}
 	
 	private void openTransmitter(TransmitterDeviceWrapper transmitter) throws DeviceUnavailableException {
 		while (nameExists(transmitter.toString(), true)) {
 			transmitter.makeNewId(getExistingIds(true));
 //			Logger.log("Cannot use device " + receiver.toString() + ", name is already used.");
 		} 
 		transmitter.open();
 		transmitter.setService(ServiceImpl.instance);
 		inputTransmitters.add(transmitter);
 	}
 
 	private ArrayList<String> getExistingIds(boolean transmitters) {
 		List<? extends DeviceWrapper> things = transmitters ? this.inputTransmitters : this.outputReceivers;
 		ArrayList<String> retVal = new ArrayList<String>();
 		
 		for (DeviceWrapper midiDeviceWrapper : things) {
 			retVal.add(midiDeviceWrapper.toString());
 		}
 		
 		return retVal;
 	}
 	private void openReceiver(ReceiverDeviceWrapper receiver) throws DeviceUnavailableException {
 		while (nameExists(receiver.toString(), false)) {
 			receiver.makeNewId(getExistingIds(false));
 //			Logger.log("Cannot use device " + receiver.toString() + ", name is already used.");
 		} 
 		
 		receiver.open();
 		receiver.setService(ServiceImpl.instance);
 		outputReceivers.add(receiver);
 	}
 
 	private boolean nameExists(String name, boolean transmitter) {
 		if (transmitter) {
 			for (DeviceWrapper midiDeviceWrapper : inputTransmitters) {
 				if (midiDeviceWrapper.toString().equals(name)) {
 					return true;
 				}
 			}
 		} else {
 			for (DeviceWrapper midiDeviceWrapper : outputReceivers) {
 				if (midiDeviceWrapper.toString().equals(name)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private void open(List<? extends DeviceWrapper> adHocDevices) {
 		// open all the ad-hoc devices and
 		// eliminate duplicate names
 		ArrayList<String> names = new ArrayList<String>();
 		for (Iterator<? extends DeviceWrapper> i = adHocDevices.iterator(); i.hasNext();) {
 			try {
 				DeviceWrapper receiver = i.next();
 				if (names.contains(receiver.toString())) {
 					Logger.log("Removing device " + receiver.toString() + " because it's a duplicate name");
 					i.remove();
 				} else {
 					receiver.setService(ServiceImpl.instance);
 					receiver.open();
 					names.add(receiver.toString());
 				}
 			} catch (Exception e) {
 				Logger.log("Couldn't open MIDI receiver for output", e);
 				i.remove();
 			}
 		}
 	}
 
 	public void close() {
 		if (inputTransmitters != null) {
 			for (DeviceWrapper wrapper : inputTransmitters) {
 				wrapper.close();
 			}
 		}
 	}
 	public void sendToMorphs(MessageWrapper message, String from) {
 		sendToMorphs(message, from, null);
 	}
 
 	public void sendToMorphs(MessageWrapper message, String from, Morph avoidMorph) {
 		for (MorphAdaptor morph : Universe.instance.main.morphCheckboxList.getMorphs(avoidMorph)) {
 			if (morph.isActive()) {
 				try {
 					boolean result = morph.getMorph().process(message, from);
 					if (result) 
 						return;
 				} catch (Throwable e) {
 					Logger.log("Error occurred processing translator " + morph.getMorph().getName());
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public void onBeat() {
 		for (MorphAdaptor morph : Universe.instance.main.morphCheckboxList.getMorphs()) {
 			if (morph.isActive()) {
 				try {
 					morph.getMorph().onBeat();
 				} catch (Throwable e) {
 					Logger.log("Error occurred processing onBeat for translator " + morph.getMorph().getName());
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	
 }
