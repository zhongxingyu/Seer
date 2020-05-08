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
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import de.uniluebeck.itm.tr.util.Logging;
 import de.uniluebeck.itm.wsn.deviceutils.observer.DeviceEvent;
 import de.uniluebeck.itm.wsn.deviceutils.observer.DeviceObserver;
 import de.uniluebeck.itm.wsn.drivers.core.MacAddress;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Level;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static de.uniluebeck.itm.wsn.deviceutils.CliUtils.assertParametersPresent;
 import static de.uniluebeck.itm.wsn.deviceutils.CliUtils.printUsageAndExit;
 
 public class DeviceMacReaderCLI {
 
 	private final static Level[] LOG_LEVELS = {Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR};
 
 	private static final Logger log = LoggerFactory.getLogger(DeviceMacReaderCLI.class);
 
 	private static final int EXIT_CODE_INVALID_ARGUMENTS = 1;
 
 	private static final int EXIT_CODE_REFERENCE_FILE_NOT_EXISTING = 2;
 
 	private static final int EXIT_CODE_REFERENCE_FILE_NOT_READABLE = 3;
 
 	private static final int EXIT_CODE_REFERENCE_FILE_IS_DIRECTORY = 4;
 
 	public static void main(String[] args) throws Exception {
 
 		Logging.setLoggingDefaults(Level.WARN);
 
 		CommandLineParser parser = new PosixParser();
 		Options options = createCommandLineOptions();
 
 		String deviceType = null;
 		String port = null;
 		Map<String, String> configuration = newHashMap();
 		DeviceMacReferenceMap deviceMacReferenceMap = null;
 		boolean use16BitMode = true;
 
 		try {
 
 			CommandLine line = parser.parse(options, args, true);
 
 			if (line.hasOption('h')) {
 				printUsageAndExit(DeviceMacReaderCLI.class, options, 0);
 			}
 
 			if (line.hasOption('v')) {
 				org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
 			}
 
 			if (line.hasOption('l')) {
 				Level level = Level.toLevel(line.getOptionValue('l'));
 				org.apache.log4j.Logger.getRootLogger().setLevel(level);
 			}
 
 			if (line.hasOption('c')) {
 				final String configurationFileString = line.getOptionValue('c');
 				final File configurationFile = new File(configurationFileString);
 				final Properties configurationProperties = new Properties();
 				configurationProperties.load(new FileReader(configurationFile));
 				for (Map.Entry<Object, Object> entry : configurationProperties.entrySet()) {
 					configuration.put((String) entry.getKey(), (String) entry.getValue());
 				}
 			}
 
 			if (line.hasOption('r')) {
 				deviceMacReferenceMap = readDeviceMacReferenceMap(line.getOptionValue('r'));
 			}
 
 			assertParametersPresent(line, 't', 'p');
 
 			deviceType = line.getOptionValue('t');
 			port = line.getOptionValue('p');
 			use16BitMode = !line.hasOption('x');
 
 		} catch (Exception e) {
 			log.error("Invalid command line: " + e);
 			printUsageAndExit(DeviceMacReaderCLI.class, options, EXIT_CODE_INVALID_ARGUMENTS);
 		}
 
 		ExecutorService executorService = Executors.newCachedThreadPool(
 				new ThreadFactoryBuilder().setNameFormat("DeviceMacReader %d").build()
 		);
 
 		final Injector injector = Guice.createInjector(
 				new DeviceMacReaderModule(executorService, deviceMacReferenceMap, use16BitMode)
 		);
 
 		final DeviceMacReader deviceMacReader = injector.getInstance(DeviceMacReader.class);
 
 		String reference = null;
 		if (deviceMacReferenceMap != null) {
 
 			final DeviceObserver deviceObserver = injector.getInstance(DeviceObserver.class);
 			final ImmutableList<DeviceEvent> events = deviceObserver.getEvents(null);
 
 			for (DeviceEvent event : events) {
 				final boolean samePort = port.equals(event.getDeviceInfo().getPort());
 				if (samePort) {
 					reference = event.getDeviceInfo().getReference();
 				}
 			}
 		}
 
 		try {
 
 			final MacAddress macAddress = deviceMacReader.readMac(port, deviceType, configuration, reference);
 			log.info("Read MAC address of {} device at port {}: {}", new Object[]{deviceType, port, macAddress});
 			System.out.println(macAddress.toHexString());
 			System.exit(0);
 
 		} catch (Exception e) {
 			log.error("Reading MAC address failed with Exception: " + e, e);
 			System.exit(1);
 		}
 
 	}
 
 	private static DeviceMacReferenceMap readDeviceMacReferenceMap(final String fileName) throws IOException {
 
 		final DeviceMacReferenceMap deviceMacReferenceMap;
 		final File referenceToMacMapPropertiesFile = new File(fileName);
 
 		if (!referenceToMacMapPropertiesFile.exists()) {
 			log.error("Reference file {} does not exist!");
 			System.exit(EXIT_CODE_REFERENCE_FILE_NOT_EXISTING);
 		} else if (!referenceToMacMapPropertiesFile.canRead()) {
 			log.error("Reference file {} is not readable!");
 			System.exit(EXIT_CODE_REFERENCE_FILE_NOT_READABLE);
 		} else if (referenceToMacMapPropertiesFile.isDirectory()) {
 			log.error("Reference file {} is a directory!");
 			System.exit(EXIT_CODE_REFERENCE_FILE_IS_DIRECTORY);
 		}
 
 		Properties properties = new Properties();
 		properties.load(new FileInputStream(referenceToMacMapPropertiesFile));
 
 		deviceMacReferenceMap = new DeviceMacReferenceMap();
 
 		for (Object key : properties.keySet()) {
 			final String value = (String) properties.get(key);
 			deviceMacReferenceMap.put((String) key, new MacAddress(value));
 		}
 
 		return deviceMacReferenceMap;
 	}
 
 	private static Options createCommandLineOptions() {
 
 		Options options = new Options();
 
 		options.addOption("p", "port", true, "Serial port to which the device is attached");
 		options.getOption("p").setRequired(true);
 
 		options.addOption("t", "type", true, "Type of the device");
 		options.getOption("t").setRequired(true);
 
 		options.addOption("x", "use64BitMode", false, "Set if you want to write the MAC in 64 bit mode");
 		options.getOption("x").setRequired(false);
 
 		options.addOption("r", "referencetomacmap", true,
 				"Optional: a properties file containing device references to MAC address mappings"
 		);
 		options.addOption("c", "configuration", true,
 				"Optional: file name of a configuration file containing key value pairs to configure the device"
 		);
 		options.addOption("v", "verbose", false, "Optional: verbose logging output (equal to -l DEBUG)");
 		options.addOption("l", "logging", true,
 				"Optional: set logging level (one of [" + Joiner.on(", ").join(LOG_LEVELS) + "])"
 		);
 		options.addOption("h", "help", false, "Optional: print help");
 
 
 		return options;
 	}
 
 }
