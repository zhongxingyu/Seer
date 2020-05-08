 /*
 Mjdj MIDI Morph - an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 You may contact the author at mjdj_midi_morph [at] confusionists.com
 */
 package com.confusionists.mjdj.midi;
 
 import javax.sound.midi.*;
 
 import com.confusionists.mjdj.Universe;
 import com.confusionists.mjdj.ui.Logger;
 import com.confusionists.mjdjApi.midi.MessageWrapper;
 import com.confusionists.mjdjApi.midiDevice.DeviceUnavailableException;
 import com.confusionists.mjdjApi.midiDevice.TransmitterDeviceWrapper;
 
 public class TransmitterWrapperImpl extends MidiDeviceWrapperImpl implements TransmitterDeviceWrapper {
 
 	private Transmitter transmitter;
 	private Monitor monitor;
 	public boolean clockSource = false;
 
 	public TransmitterWrapperImpl(MidiDevice device) {
 		super(device);
 	}
 
 	@Override
 	public void close() {
 		if (device == null)
 			return;
 		monitor.close();
 		transmitter.close();
 		super.close();
 	}
 
 	@Override
 	public void open() throws DeviceUnavailableException {
 		try {
 			if (device == null)
 				return;
 			if (!device.isOpen()) {
 				device.open();
 			}
 			this.transmitter = device.getTransmitter();
 			monitor = new Monitor(this);
 			this.transmitter.setReceiver(monitor);
 		} catch (MidiUnavailableException mex) {
 			throw new DeviceUnavailableException(mex);
 
 		}
 	}
 
 	public boolean isClockSource() {
 		return clockSource;
 	}
 
 	public void setClockSource(boolean value) {
 		this.clockSource = value;
 
 	}
 
 	@Override
 	public void toggleUi() {
 		System.out.println("toggle ui");
 	}
 
 }
 
 class Monitor implements Receiver {
 
 	TransmitterWrapperImpl wrapper;
 
 	protected Monitor(TransmitterWrapperImpl wrapper) {
 		this.wrapper = wrapper;
 	}
 
 	public void close() {
 	}
 
 	public void send(MidiMessage message, long timeStamp) {
 		try {
 			MessageWrapper msgWrapper = MessageWrapper.newInstance(message);
 			byte[] messageBytes = message.getMessage();
 			int firstByte = messageBytes[0] & 0xff;
			if (firstByte != ShortMessage.TIMING_CLOCK && firstByte != ShortMessage.ACTIVE_SENSING) { // this is to check if it's clock
 				if (wrapper.isActive()) {
 					Logger.log(msgWrapper, wrapper);
 					wrapper.getService().morph(msgWrapper, wrapper.toString());
 				}
			} else {
				if (wrapper.clockSource) {
					Universe.instance.clockHandler.onClock();
				}
			}
 		} catch (Exception e) {
 			Logger.debugLog("Problem sending", e);
 		}
 	}
 
 }
