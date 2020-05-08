 package ch.eleveneye.hs485.device.physically;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import ch.eleveneye.hs485.api.data.HwVer;
 import ch.eleveneye.hs485.api.data.SwVer;
 import ch.eleveneye.hs485.api.data.TFSValue;
 import ch.eleveneye.hs485.device.Sensor;
 import ch.eleveneye.hs485.device.TFSensor;
 import ch.eleveneye.hs485.device.config.PairMode;
 import ch.eleveneye.hs485.device.utils.AbstractDevice;
 import ch.eleveneye.hs485.memory.ModuleType;
 
 public class TFS extends AbstractDevice {
 
 	protected class TFSensorImpl implements TFSensor, PhysicallySensor {
 
 		LinkedList<Actor>	registeredActors;
 
 		public TFSensorImpl() {
 			registeredActors = new LinkedList<Actor>();
 		}
 
 		@Override
 		public int getModuleAddr() {
 			return deviceAddr;
 		}
 
 		@Override
 		public int getSensorNr() {
 			return 0;
 		}
 
 		public Collection<Actor> listAssignedActors() throws IOException {
 			return registeredActors;
 		}
 
 		@Override
 		public TFSValue readTF() throws IOException {
 			return bus.readTemp(deviceAddr);
 		}
 
 	}
 
 	public static Collection<ModuleType> getAvailableConfig() {
 		final ModuleType tfsv13 = new ModuleType();
 		tfsv13.setEepromSize(512);
 		tfsv13.setName("TFS");
 		tfsv13.setHwVer(new HwVer((byte) 4, (byte) 0));
 		tfsv13.setSwVer(new SwVer((byte) 1, (byte) 3));
 		tfsv13.setImplementingClass(TFS.class);
 		tfsv13.setWidth(1);
 
 		return Arrays.asList(new ModuleType[] { tfsv13 });
 	}
 
 	private final TFSensorImpl	sensor;
 
 	public TFS() {
 		sensor = new TFSensorImpl();
 	}
 
 	@Override
 	public Actor getActor(final int actorNr) throws IOException {
 		return null;
 	}
 
 	@Override
 	public int getActorCount() {
 		return 0;
 	}
 
 	public int getInputPairCount() {
 		return 0;
 	}
 
 	public PairMode getInputPairMode(final int pairNr) throws IOException {
 		return null;
 	}
 
 	@Override
 	public PhysicallySensor getSensor(final int sensorNr) throws IOException {
 		return sensor;
 	}
 
 	@Override
 	public Collection<Actor> listActors() throws IOException {
 		return new ArrayList<Actor>(0);
 	}
 
 	@Override
 	public Collection<Sensor> listSensors() throws IOException {
 		return Arrays.asList(new Sensor[] { sensor });
 	}
 
 	public void setInputPairMode(final int pairNr, final PairMode mode) throws IOException {
 	}
 
 	@Override
 	public String toString() {
 		return "TFS-" + Integer.toHexString(deviceAddr);
 	}
 
 }
