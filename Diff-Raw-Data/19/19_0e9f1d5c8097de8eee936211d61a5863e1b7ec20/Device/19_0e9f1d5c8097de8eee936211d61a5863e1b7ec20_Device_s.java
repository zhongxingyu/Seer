 package ch.eleveneye.hs485.dummy.device;
 
 import java.util.Arrays;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import ch.eleveneye.hs485.api.BroadcastHandler;
 import ch.eleveneye.hs485.api.data.HwVer;
 import ch.eleveneye.hs485.api.data.SwVer;
 import ch.eleveneye.hs485.api.data.TFSValue;
 import ch.eleveneye.hs485.protocol.IMessage;
 
 /**
  * Abstract Dummy-Device for Test
  * 
  */
 public abstract class Device {
 	protected byte[]									actor;
 	private final int									address;
 	protected byte[]									data				= new byte[512];
 	private ScheduledExecutorService	executorService;
 	private final BroadcastHandler		handler;
 	private final boolean[]						keyPressed	= new boolean[] { false, false };
 
 	public Device(final int actorCount, final int address, final BroadcastHandler handler) {
 		this.address = address;
 		this.handler = handler;
 		actor = new byte[actorCount];
 		Arrays.fill(data, (byte) 0xff);
 		Arrays.fill(actor, (byte) 0xff);
 	}
 
 	/**
 	 * read current Value of a Actor
 	 * 
 	 * @param actorNr
 	 *          Index of Actor
 	 * @return currentValue of Actor
 	 */
 	public synchronized byte readActor(final byte actorNr) {
 		return actor[actorNr];
 	}
 
 	/**
 	 * Reads HW-Version of current device
 	 * 
 	 * @return
 	 */
 	public abstract HwVer readHwVer();
 
 	/**
 	 * Reads the Brightness from Lux-Sensor
 	 * 
 	 * @return Brightness
 	 */
 	public int readLux() {
 		return 0;
 	}
 
 	/**
 	 * Reads config-Data from Module
 	 * 
 	 * @param count
 	 *          count of Bytes
 	 * @return Data
 	 */
 	public synchronized byte[] readModuleEEPROM(final int count) {
 		return Arrays.copyOf(data, count);
 	}
 
 	/**
 	 * Reads Firmware-Version from dummy-Device
 	 * 
 	 * @return Firmware-Version
 	 */
 
 	public abstract SwVer readSwVer();
 
 	/**
 	 * Reads temperature and humidity from TFS-Sensor
 	 * 
 	 * @return Values
 	 */
 	public TFSValue readTemp() {
 		return null;
 	}
 
	private void scheduleNextBroadcast() {
		final int keyNr = (int) (Math.random() * 2);
 		final int time;
 		final TimeUnit waitTimeUnit;
 		if (keyPressed[keyNr]) {
			time = (int) (Math.random() * 20 + 10);
 			waitTimeUnit = TimeUnit.SECONDS;
 		} else {
			time = (int) (Math.random() * 300 + 100);
 			waitTimeUnit = TimeUnit.MILLISECONDS;
 		}
 
 		executorService.schedule(new Runnable() {
 
 			public void run() {
 				final IMessage msg = new IMessage();
 				msg.setSourceAddress(address);
 				msg.setTargetAddress(-1);
 				msg.setSync(true);
 				msg.setSenderNumber((byte) (1 & 0x03));
 				msg.setReceiveNumber((byte) 0);
 				msg.setHasSourceAddr(true);
				msg.setData(new byte[] { 'K', (byte) keyNr, 0, (byte) (keyPressed[keyNr] ? 0 : 2) });
 				keyPressed[keyNr] = !keyPressed[keyNr];
				scheduleNextBroadcast();
 				handler.handleBroadcastMessage(msg);
 			}
 		}, time, waitTimeUnit);
 	}
 
 	public void setExecutorService(final ScheduledExecutorService executorService) {
 		this.executorService = executorService;
		scheduleNextBroadcast();
 	}
 
 	public synchronized void writeActor(final byte actorNr, final byte value) {
 		actor[actorNr] = value;
 	}
 
 	public synchronized void writeEEPROM(final int memOffset, final byte[] data2, final int dataOffset, final int length) {
 		System.arraycopy(data2, dataOffset, data, memOffset, length);
 	}
 }
