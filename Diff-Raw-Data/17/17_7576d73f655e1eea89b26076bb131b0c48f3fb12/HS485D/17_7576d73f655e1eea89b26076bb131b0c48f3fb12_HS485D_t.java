 package ch.eleveneye.hs485.device.physically;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.eleveneye.hs485.api.data.HwVer;
 import ch.eleveneye.hs485.api.data.SwVer;
 import ch.eleveneye.hs485.device.ActorType;
 import ch.eleveneye.hs485.device.Dimmer;
 import ch.eleveneye.hs485.device.KeySensor;
 import ch.eleveneye.hs485.device.Sensor;
 import ch.eleveneye.hs485.device.TimedActor;
 import ch.eleveneye.hs485.device.config.DimmerMode;
 import ch.eleveneye.hs485.device.config.PairMode;
 import ch.eleveneye.hs485.device.config.TimeMode;
 import ch.eleveneye.hs485.device.utils.AbstractActor;
 import ch.eleveneye.hs485.device.utils.AbstractDevice;
 import ch.eleveneye.hs485.device.utils.AbstractSensor;
 import ch.eleveneye.hs485.memory.ArrayVariable;
 import ch.eleveneye.hs485.memory.ChoiceEntry;
 import ch.eleveneye.hs485.memory.ChoiceVariable;
 import ch.eleveneye.hs485.memory.ModuleType;
 import ch.eleveneye.hs485.memory.NumberVariable;
 
 public class HS485D extends AbstractDevice implements PairedSensorDevice {
 
 	private final class HS485DActor extends AbstractActor implements Dimmer, TimedActor {
 		private HS485DActor(final int actorNr) {
 			super(0);
 		}
 
 		public DimmerMode getDimmerMode() throws IOException {
 			switch (readVariable("dimmer-mode")) {
 			case 0x0ff:
 			case 0x00:
 				return DimmerMode.FULL_BRIGHNESS;
 			case 0x01:
 				return DimmerMode.OLD_BRIGHTNESS;
 			}
 			return null;
 		}
 
 		public int getDimmValue() throws IOException {
 			return bus.readActor(deviceAddr, (byte) actorNr);
 		}
 
 		public int getMaxValue() throws IOException {
 			return 0x0f;
 		}
 
 		public int getModuleAddr() {
 			return deviceAddr;
 		}
 
 		public TimeMode getTimeMode() throws IOException {
 			switch (readVariable("timer-mode")) {
 			case 0xff:
 			case 0x00:
 				return TimeMode.NONE;
 			case 0x01:
 				return TimeMode.STAIRCASE;
 			case 0x02:
 				return TimeMode.AUTO_OFF;
 			}
 			return null;
 		}
 
 		public int getTimeValue() throws IOException {
 			return readVariable("time");
 		}
 
 		public boolean getToggleBit() throws IOException {
 			final int value = readVariable("toggle-value");
 			return (value & 0x01) != 0;
 		}
 
 		public boolean isOn() throws IOException {
 			return bus.readActor(deviceAddr, (byte) actorNr) > 0;
 		}
 
 		public void setDimmerMode(final DimmerMode value) throws IOException {
 			switch (value) {
 			case FULL_BRIGHNESS:
 				writeVariable("dimmer-mode", 0xff);
 				break;
 			case OLD_BRIGHTNESS:
 				writeVariable("dimmer-mode", 0x01);
 				break;
 			}
 		}
 
 		public void setDimmValue(final int value) throws IOException {
 			bus.writeActor(deviceAddr, (byte) actorNr, (byte) value);
 		}
 
 		public void setOff() throws IOException {
 			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0x00);
 
 		}
 
 		public void setOn() throws IOException {
 			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0x10);
 		}
 
 		public void setTimeMode(final TimeMode value) throws IOException {
 			switch (value) {
 			case NONE:
 				writeVariable("timer-mode", 0xff);
 				break;
 			case STAIRCASE:
 				writeVariable("timer-mode", 0x01);
 				break;
 			case AUTO_OFF:
 				writeVariable("timer-mode", 0x02);
 				break;
 			}
 		}
 
 		public void setTimeValue(final int value) throws IOException {
 			writeVariable("time", value);
 		}
 
 		public void setToggleBit(final boolean value) throws IOException {
 			final int oldValue = readVariable("toggle-value");
 			writeVariable("toggle-value", oldValue & 0xfe | (value ? 0 : 1));
 
 		}
 
 		public void toggle() throws IOException {
 			bus.writeActor(deviceAddr, (byte) actorNr, (byte) 0xff);
 		}
 
 		@Override
 		public String toString() {
 			return "D-" + super.toString();
 		}
 	}
 
	private final class HS485DSensor extends AbstractSensor implements PairableSensor, KeySensor {
		private HS485DSensor(final int sensorNr) {
 			super(sensorNr);
 		}
 
 		public void addActor(final Actor target) throws IOException {
 			if (target.getModuleAddr() == deviceAddr)
 				writeVariable("input[" + sensorNr + "].direct-output", 0xff);
 			else
 				addInputTargetRaw(sensorNr, target.getModuleAddr(), target.getActorNr());
 		}
 
 		public int getModuleAddr() {
 			return deviceAddr;
 		}
 
 		public boolean isPaired() throws IOException {
 			return isSensorPaired();
 		}
 
 		public Collection<Actor> listAssignedActors() throws IOException {
 			loadActorList();
 			final Collection<Actor> ret = listAssignedActorsRaw(sensorNr);
 			final int directValue = readVariable("input[" + sensorNr + "].direct-output");
 			if (directValue != 0xfe)
 				ret.add(getActor(0));
 			return ret;
 		}
 
 		public void removeActor(final Actor target) throws IOException {
 			final String variableName = "input[" + sensorNr + "].direct-output";
 			if (target.getModuleAddr() == deviceAddr)
 				writeVariable(variableName, 0xfe);
 			removeInputTargetRaw(sensorNr, target.getModuleAddr(), target.getActorNr());
 		}
 
 		@Override
 		public String toString() {
 			try {
 				return "D-" + Integer.toHexString(deviceAddr) + (isPaired() ? "" : "-" + (sensorNr + 1));
 			} catch (final IOException e) {
 				return "D-" + Integer.toHexString(deviceAddr) + "-" + (sensorNr + 1);
 			}
 		}
 
 	}
 
 	private static final Map<String, ActorType>	actors						= new HashMap<String, ActorType>();
 
 	private static Logger												log								= LoggerFactory.getLogger(HS485D.class);
 
 	private static final String									VAR_DIRECT_OUTPUT	= "direct-output";
 
 	private static final String									VAR_INPUT_TYPE		= "input-type";
 
 	private static final String									VAR_SENSOR_TYPE		= "sensor-type";
 
 	static {
 		HS485D.actors.put("1", ActorType.HIGH_VOLTAGE);
 	}
 
 	public static List<ModuleType> getAvailableConfig() {
 		final List<ModuleType> versions = new LinkedList<ModuleType>();
 		final ModuleType hs485d = new ModuleType();
 		hs485d.setHwVer(new HwVer((byte) 0, (byte) 1));
 		hs485d.setSwVer(new SwVer((byte) 2, (byte) 0));
 		hs485d.setName("HS485D");
 		hs485d.setEepromSize(512);
 		hs485d.setImplementingClass(HS485D.class);
 		hs485d.setWidth(2);
 
 		final ChoiceVariable inputType = new ChoiceVariable();
 		inputType.setAddress(0);
 		inputType.setLength(1);
 		inputType.setReload(true);
 		inputType.setName(HS485D.VAR_INPUT_TYPE);
 		inputType.addChoiceEntry(new ChoiceEntry(0x00, "toggle", ""));
 		inputType.addChoiceEntry(new ChoiceEntry(0x01, "up/down", ""));
 		inputType.addChoiceEntry(new ChoiceEntry(0xff, "toggle", ""));
 		hs485d.addVariable(inputType);
 
 		final ArrayVariable inputConfig = new ArrayVariable();
 		inputConfig.setAddress(0x12);
 		inputConfig.setCount(2);
 		inputConfig.setStep(1);
 		inputConfig.setName("input");
 		inputConfig.setReload(true);
 		final ChoiceVariable sensorType = new ChoiceVariable();
 		sensorType.setAddress(0);
 		sensorType.setLength(1);
 		sensorType.setName(HS485D.VAR_SENSOR_TYPE);
 		sensorType.addChoiceEntry(new ChoiceEntry(0xff, "push-w-led", ""));
 		sensorType.addChoiceEntry(new ChoiceEntry(0x01, "push-wo-led", ""));
 		sensorType.addChoiceEntry(new ChoiceEntry(0x02, "switch-wo-led", ""));
 		sensorType.addChoiceEntry(new ChoiceEntry(0x03, "switch-w-led", ""));
 		inputConfig.addComponent(sensorType);
 		final ChoiceVariable directConnection = new ChoiceVariable();
 		directConnection.setAddress(0x10 - 0x12);
 		directConnection.setLength(1);
 		directConnection.setName(HS485D.VAR_DIRECT_OUTPUT);
 		directConnection.addChoiceEntry(new ChoiceEntry(0xfe, "none", ""));
 		directConnection.addChoiceEntry(new ChoiceEntry(0xff, "connected", ""));
 		inputConfig.addComponent(directConnection);
 		hs485d.addVariable(inputConfig);
 
 		final ChoiceVariable dimmerMode = new ChoiceVariable();
 		dimmerMode.setAddress(0x02);
 		dimmerMode.setReload(true);
 		dimmerMode.setLength(1);
 		dimmerMode.setName("dimmer-mode");
 		dimmerMode.addChoiceEntry(new ChoiceEntry(0xff, "full Brigness", ""));
 		dimmerMode.addChoiceEntry(new ChoiceEntry(0x01, "last Brigness", ""));
 		hs485d.addVariable(dimmerMode);
 
 		final NumberVariable time = new NumberVariable();
 		time.setName("time");
 		time.setAddress(0x03);
 		time.setLength(2);
 		time.setMinValue(0);
 		time.setMaxValue(65534);
 		time.setDefaultValue(0xffff);
 		time.setReload(true);
 		hs485d.addVariable(time);
 
 		final ChoiceVariable outputTimer = new ChoiceVariable();
 		outputTimer.setAddress(0x07);
 		outputTimer.setLength(1);
 		outputTimer.setName("timer-mode");
 		outputTimer.addChoiceEntry(new ChoiceEntry(0xff, "none", ""));
 		outputTimer.addChoiceEntry(new ChoiceEntry(0x01, "staircase", ""));
 		outputTimer.addChoiceEntry(new ChoiceEntry(0x02, "auto-off", ""));
 		hs485d.addVariable(outputTimer);
 
 		final NumberVariable toggleValue = new NumberVariable();
 		toggleValue.setName("toggle-value");
 		toggleValue.setAddress(0x0a);
 		toggleValue.setLength(1);
 		toggleValue.setReload(true);
 		hs485d.addVariable(toggleValue);
 
 		hs485d.addVariable(AbstractDevice.defaultTargetConfig());
 
 		versions.add(hs485d);
 
 		return versions;
 	}
 
 	private List<HS485DActor>				actorList;
 
 	private List<PhysicallySensor>	sensorList;
 
 	@Override
 	public void clearAllInputTargets() throws IOException {
 		super.clearAllInputTargets();
 		writeVariable(HS485D.VAR_DIRECT_OUTPUT, 0xfe);
 	}
 
 	public Actor getActor(final int actorNr) throws IOException {
 		loadActorList();
 		return actorList.get(actorNr);
 	}
 
 	@Override
 	public int getActorCount() {
 		return 1;
 	}
 
 	public int getInputPairCount() {
 		return 1;
 	}
 
 	public PairMode getInputPairMode(final int pairNr) throws IOException {
 		return isSensorPaired() ? PairMode.JOINT : PairMode.SPLIT;
 	}
 
 	public PhysicallySensor getSensor(final int sensorNr) throws IOException {
 		loadSensorList();
 		for (final PhysicallySensor physicallySensor : sensorList)
 			if (physicallySensor.getSensorNr() == sensorNr)
 				return physicallySensor;
 		return null;
 	}
 
 	public synchronized Collection<Actor> listActors() throws IOException {
 		loadActorList();
 		return new ArrayList<Actor>(actorList);
 	}
 
 	public synchronized Collection<Sensor> listSensors() throws IOException {
 		loadSensorList();
 		return new ArrayList<Sensor>(sensorList);
 	}
 
 	public synchronized void setInputPairMode(final int pairNr, final PairMode mode) throws IOException {
 		writeVariable(HS485D.VAR_INPUT_TYPE, mode == PairMode.JOINT ? 0x01 : 0x00);
 		sensorList = null;
 	}
 
 	@Override
 	public String toString() {
 		return "D-" + Integer.toHexString(deviceAddr);
 	}
 
 	private boolean isSensorPaired() throws IOException {
 		return readVariable(HS485D.VAR_INPUT_TYPE) == 0x01;
 	}
 
 	private void loadActorList() {
 		if (actorList == null)
 			actorList = Arrays.asList(new HS485DActor[] { new HS485DActor(0) });
 	}
 
 	private void loadSensorList() throws IOException {
 		if (sensorList == null)
 			if (isSensorPaired())
				sensorList = Arrays.asList(new PhysicallySensor[] { new HS485DSensor(0) });
 			else
				sensorList = Arrays.asList(new PhysicallySensor[] { new HS485DSensor(0), new HS485DSensor(1) });
 	}
 
 }
