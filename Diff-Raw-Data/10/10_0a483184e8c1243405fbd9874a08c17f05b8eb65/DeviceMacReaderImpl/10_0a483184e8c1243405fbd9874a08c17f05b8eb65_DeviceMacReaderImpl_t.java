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
 import com.google.inject.internal.Nullable;
 import com.google.inject.name.Named;
 import de.uniluebeck.itm.wsn.drivers.core.Connection;
 import de.uniluebeck.itm.wsn.drivers.core.Device;
 import de.uniluebeck.itm.wsn.drivers.core.MacAddress;
 import de.uniluebeck.itm.wsn.drivers.core.Monitor;
 import de.uniluebeck.itm.wsn.drivers.core.operation.ReadMacAddressOperation;
 import de.uniluebeck.itm.wsn.drivers.core.operation.RootProgressManager;
 import de.uniluebeck.itm.wsn.drivers.factories.ConnectionFactory;
 import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactory;
 import de.uniluebeck.itm.wsn.drivers.factories.DeviceType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DeviceMacReaderImpl implements DeviceMacReader {
 
 	private static final Logger log = LoggerFactory.getLogger(DeviceMacReaderImpl.class);
 
 	@Inject
 	private ConnectionFactory connectionFactory;
 
 	@Inject
 	private DeviceFactory deviceFactory;
 
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
 
 		return referenceToMacMap.get(reference);
 	}
 
 	private MacAddress readMacFromDevice(final String port, final DeviceType deviceType) throws Exception {
 
 		final Connection connection = connectionFactory.create(deviceType);
		try {
			connection.connect(port);
		} catch (Exception e) {
			throw new Exception(
					"Connection to device at port \"" + port + "\" could not be established. Reason: " + e.getMessage()
			);
		}
 
 		if (!connection.isConnected()) {
 			throw new Exception("Connection to device at port \"" + port + "\" could not be established!");
 		}
 
 		try {
 
 			final Device device = deviceFactory.create(deviceType, connection);
 			final ReadMacAddressOperation readMacAddressOperation = device.createReadMacAddressOperation();
 
 			MacAddress macAddress = readMacAddressOperation.execute(new RootProgressManager(new Monitor() {
 
 				private int lastProgress = -1;
 
 				@Override
 				public void onProgressChange(final float fraction) {
 					int newProgress = (int) Math.floor(fraction * 100);
 					if (lastProgress < newProgress) {
 						lastProgress = newProgress;
 						log.debug("Progress: {}%", newProgress);
 					}
 				}
 			}
 			)
 			);
 
 			if (use16BitMode) {
 				macAddress = macAddress.to16BitMacAddress();
 			}
 
 			return macAddress;
 
 		} finally {
 			Closeables.closeQuietly(connection);
 		}
 	}
 }
