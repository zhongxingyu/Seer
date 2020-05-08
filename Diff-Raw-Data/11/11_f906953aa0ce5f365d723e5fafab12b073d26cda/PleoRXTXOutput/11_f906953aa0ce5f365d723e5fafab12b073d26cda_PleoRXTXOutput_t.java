 package pleocmd.pipe.out;
 
 import gnu.io.CommPortIdentifier;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeoutException;
 
 import pleocmd.Log;
 import pleocmd.api.PleoCommunication;
 import pleocmd.cfg.ConfigItem;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.OutputException;
 import pleocmd.pipe.data.CommandData;
 import pleocmd.pipe.data.Data;
 
 public final class PleoRXTXOutput extends Output {
 
 	private final ConfigItem<String> cfgDevice;
 
 	private PleoCommunication pc;
 
 	public PleoRXTXOutput() {
		ConfigItem<String> tmp;
		try {
			tmp = new ConfigItem<String>("Device", "", getAllDeviceNames());
		} catch (UnsatisfiedLinkError e) {
			Log.error(e, "Cannot find external library in '%s'", System
					.getProperty("java.library.path"));
			tmp = new ConfigItem<String>("Device", "", new ArrayList<String>());
		}
		addConfig(cfgDevice = tmp);
 		constructed();
 	}
 
 	public PleoRXTXOutput(final String device) throws ConfigurationException {
 		this();
 		cfgDevice.setContent(device);
 	}
 
 	private static List<String> getAllDeviceNames() {
 		final List<String> names = new ArrayList<String>();
 		final List<CommPortIdentifier> ports = PleoCommunication
 				.getAvailableSerialPorts();
 		for (final CommPortIdentifier port : ports)
 			names.add(port.getName());
 		return names;
 	}
 
 	@Override
 	protected void configure0() {
 		// nothing to do
 	}
 
 	@Override
 	protected void init0() throws IOException {
 		Log.detail("Initializing PleoRXTXOutput for device '%s'", cfgDevice
 				.getContent());
 		pc = new PleoCommunication(PleoCommunication.getPort(cfgDevice
 				.getContent()));
 		pc.init();
 	}
 
 	@Override
 	protected void close0() {
 		Log.detail("Closing PleoRXTXOutput '%s' for device '%s'", pc, cfgDevice
 				.getContent());
 		pc.close();
 		pc = null;
 	}
 
 	@Override
 	public String getInputDescription() {
 		return "PMC";
 	}
 
 	@Override
 	protected boolean write0(final Data data) throws OutputException,
 			IOException {
 		if (!CommandData.isCommandData(data, "PMC")) return false;
 		pc.send(CommandData.getArgument(data));
 		try {
 			Log.consoleOut(pc.readAnswer());
 		} catch (final TimeoutException e) {
 			throw new OutputException(this, true, e,
 					"Cannot read answer for command '%s'", data);
 		}
 		return true;
 	}
 
 	public static String help(final HelpKind kind) {
 		switch (kind) {
 		case Name:
 			return "Pleo RXTX Output";
 		case Description:
 			return "Processes commands like 'PMC|foo' by sending 'foo' "
 					+ "without any modifications to the Pleo via an USB "
 					+ "connection by using the RXTX library";
 		case Config1:
 			return "Path to the device on which the Pleo is connected to "
 					+ "this computer";
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public String isConfigurationSane() {
 		try {
 			PleoCommunication.getPort(cfgDevice.getContent());
 		} catch (final IOException e) {
 			return String.format("Device %s does not exist", cfgDevice
 					.getContent());
 		}
 		return null;
 	}
 
 	@Override
 	protected int getVisualizeDataSetCount() {
 		return 0;
 	}
 
 }
