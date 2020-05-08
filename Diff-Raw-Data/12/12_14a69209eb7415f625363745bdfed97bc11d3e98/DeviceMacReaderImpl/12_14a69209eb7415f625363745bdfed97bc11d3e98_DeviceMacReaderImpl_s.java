 /**********************************************************************************************************************
  * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                 *
  * All rights reserved.                                                                                               *
  *                                                                                                                    *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
  * following conditions are met:                                                                                      *
  *                                                                                                                    *
  * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
  *   disclaimer.                                                                                                      *
  * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
  *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
  * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or        *
  *   promote products derived from this software without specific prior written permission.                           *
  *                                                                                                                    *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
  **********************************************************************************************************************/
 
 package de.uniluebeck.itm.wsn.deviceutils.macreader;
 
 import com.google.common.io.Closeables;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import de.uniluebeck.itm.wsn.drivers.core.Device;
 import de.uniluebeck.itm.wsn.drivers.core.MacAddress;
 import de.uniluebeck.itm.wsn.drivers.core.exception.PortNotFoundException;
 import de.uniluebeck.itm.wsn.drivers.core.operation.OperationCallback;
 import de.uniluebeck.itm.wsn.drivers.core.operation.OperationCallbackAdapter;
 import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactory;
 import de.uniluebeck.itm.wsn.drivers.factories.DeviceType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 import java.util.concurrent.ScheduledExecutorService;
 
 public class DeviceMacReaderImpl implements DeviceMacReader {
 
 	private static final Logger log = LoggerFactory.getLogger(DeviceMacReaderImpl.class);
 	
 	private static final int TIMEOUT = 300000;
 
 	@Inject
 	private DeviceFactory deviceFactory;
 	
 	@Inject
 	private ScheduledExecutorService executorService;
 
 	@Inject
 	@Nullable
 	private DeviceMacReferenceMap referenceToMacMap;
 
 	@Inject(optional = true)
 	@Named("use16BitMode")
 	private Boolean use16BitMode = true;
 
 	@Override
 	public MacAddress readMac(final String port, final String deviceTypeString, @Nullable final String reference)
 			throws Exception {
 
 		final DeviceType deviceType = DeviceType.fromString(deviceTypeString);
 
 		switch (deviceType) {
 			case MOCK:
 				return readMacFromDevice(port, deviceType);
 			case ISENSE:
 				return readMacFromDevice(port, deviceType);
			case NULL:
				return readMacFromDevice(port, deviceType);
 			case PACEMATE:
 				return readMacFromDevice(port, deviceType);
 			case TELOSB:
 				return readMacFromMap(reference);
 			default:
 				return readMacFromMap(reference);
 		}
 
 	}
 
 	private MacAddress readMacFromMap(final String reference) throws Exception {
 
 		if (referenceToMacMap == null || !referenceToMacMap.containsKey(reference)) {
 			throw new Exception("No MAC address for reference \"" + reference + "\" found in map!");
 		}
 
 		assert referenceToMacMap != null;
 		return referenceToMacMap.get(reference);
 	}
 
 	private MacAddress readMacFromDevice(final String port, final DeviceType deviceType) throws Exception {
 		final Device device = deviceFactory.create(executorService, deviceType);
 		try {
 			tryToConnect(device, port);
 			final OperationCallback<MacAddress> callback = new OperationCallbackAdapter<MacAddress>() {
 				private int lastProgress = -1;
 				
 				@Override
 				public void onProgressChange(float fraction) {
 					int newProgress = (int) Math.floor(fraction * 100);
 					if (lastProgress < newProgress) {
 						lastProgress = newProgress;
 						log.debug("Progress: {}%", newProgress);
 					}
 				}
 			};
 			final MacAddress macAddress = device.readMac(TIMEOUT, callback).get();
 
 			if (use16BitMode) {
 				return macAddress.to16BitMacAddress();
 			}
 			return macAddress;
 
 		} finally {
 			Closeables.closeQuietly(device);
 		}
 	}
 
 	private void tryToConnect(final Device device, final String port) throws Exception {
 		for (int i = 0; i < 10; i++) {
 			try {
 				device.connect(port);
 			} catch (PortNotFoundException e) {
 				Thread.sleep(100);
 			} catch (Exception e) {
 			}
 		}
 		
 		if (!device.isConnected()) {
 			throw new Exception("Connection to device at port \"" + port + "\" could not be established!");
 		}
 	}
 }
