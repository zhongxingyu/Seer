 package entropia.clubmonitor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Arrays;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 
 final class HauptRaumNetIOHandler implements Runnable {
     private static final Logger logger =
 	    LoggerFactory.getLogger(HauptRaumNetIOHandler.class);
     private static final int  SLEEP_TIME = 250; // millis
     
     private final InetSocketAddress tcpAddress;
     private final SyncService syncService; 
 
     private static enum BINARYPORTS {
 	CLUB_OFFEN(1, TernaryStatusRegister.CLUB_OFFEN, true),
 	FENSTER_OFFEN(2, TernaryStatusRegister.FENSTER_OFFEN),
 	TUER_BUZZER(3, TernaryStatusRegister.KEY_DOOR_BUZZER, true);
 	
 	private final int portNumber;
 	private final byte[] getstatus;
 	private final TernaryStatusRegister status;
 	private final boolean inverted;
 	
 	private BINARYPORTS(int i, TernaryStatusRegister status) {
 	    this(i, status, false);
 	}
 	
 	private BINARYPORTS(int i, TernaryStatusRegister status, boolean inverted) {
 	    if (status == null) {
 		throw new NullPointerException();
 	    }
 	    this.portNumber = i;
 	    this.status = status;
 	    this.getstatus = String.format("GETPORT %d\r\n",
 	            this.portNumber).getBytes(Charsets.US_ASCII);
 	    this.inverted = inverted;
 	}
 	
         public void off() {
             if (inverted) {
         	status.on();
             } else {
         	status.off();
             }
 	}
 	
         public void on() {
             if (inverted) {
         	status.off();
             } else {
         	status.on();
             }
 	}
     }
 
     
     private static enum ADCPORTS {
 	/*
 	TEMPERATURE(1, ADCRegister.Temperature)
 	*/
 	;
 
 	private final int portNumber;
 	private final byte[] getstatus;
 	private final ADCRegister register;
 	
 	private ADCPORTS(int i, ADCRegister r) {
 	    this.portNumber = i;
 	    this.register = r;
 	    this.getstatus = String.format("GETADC %d\r\n",
 	            this.portNumber).getBytes(Charsets.US_ASCII);
 	}
 	
         public void set(long value) {
 	    register.set(value);
         }
     }
         
     public HauptRaumNetIOHandler(InetSocketAddress netIOAddress,
 	    SyncService syncService) {
 	this.tcpAddress = netIOAddress;
 	this.syncService = syncService;
     }
 
     @Override
     public void run() {
 	final String name = Thread.currentThread().getName();
 	while (!Thread.interrupted()) {
 	    try {
 		poll();
 	    } catch (final InterruptedException e) {
 		Thread.currentThread().interrupt();
 	    } catch (final Exception e) {
 		logger.warn("restarting because of exception", e);
	    } catch (final Error e) {
		logger.error("error in " + name, e);
		return;
 	    }
 	}
     }
     
     private void poll() throws Exception {
 	final Socket socket = IOUtils.connectLowLatency(tcpAddress, false);
 	logger.info("connected to " + tcpAddress.toString());
 	TernaryStatusRegister.HW_FEHLER.off();
 	try {
 	    final InputStream inputStream = socket.getInputStream();
 	    final OutputStream outputStream = socket.getOutputStream();
 	    while (true) {
 		readBinaryPorts(inputStream, outputStream);
 		readADCPorts(inputStream, outputStream);
 		writeStatusToPorts(inputStream, outputStream);
 		syncService.sleepUntilEvent(SLEEP_TIME);
 	    }
 	} finally {
 	    TernaryStatusRegister.HW_FEHLER.on();
 	    socket.close();
 	}
     }
 
     private static final byte[] ACK = "ACK\r\n".getBytes(Charsets.US_ASCII);
     private static void writeStatusToPorts(InputStream inputStream,
             OutputStream outputStream) throws IOException {
 	for (TriggerPort p : TriggerPort.values()) {
 	    if (p.getForClass() != HauptRaumNetIOHandler.class) {
 		continue;
 	    }
 	    byte[] nextCommand = p.getNextCommand();
 	    if (nextCommand != null) {
 		outputStream.write(nextCommand);
 		outputStream.flush();
 		byte[] bytes =
 			IOUtils.readBytesStrict(inputStream, ACK.length);
 		if (!Arrays.equals(bytes, ACK)) {
 		    throw new IllegalStateException("stream unsynchronized: "
 			    + new String(bytes));
 		}
 	    }
 	}
     }
 
     private static void readADCPorts(InputStream inputStream,
             OutputStream outputStream) throws IOException {
 	for (ADCPORTS p : ADCPORTS.values()) {
 	    outputStream.write(p.getstatus);
 	    outputStream.flush();
 	    final long value = IOUtils.readDigits(inputStream);
 	    p.set(value);
 	}
     }
 
     private static void readBinaryPorts(InputStream inputStream,
 	    OutputStream outputStream) throws IOException {
 	for (BINARYPORTS p : BINARYPORTS.values()) {
 	    outputStream.write(p.getstatus);
 	    outputStream.flush();
 	    final byte[] readBytes = IOUtils.readBytesStrict(inputStream, 3);
 	    IOUtils.verifyBinaryDigitLine(readBytes);
 	    if (readBytes[0] == '0') {
 		p.off();
 	    } else if (readBytes[0] == '1') {
 		p.on();
 	    }
 	}
     }
     
     static Thread startNetIOPoller(SyncService syncService) {
         final InetSocketAddress netIOAddress = Config.getNetIOAddress();
         final Thread t = new Thread(new HauptRaumNetIOHandler(netIOAddress,
         	syncService));
         t.setName(HauptRaumNetIOHandler.class.getCanonicalName());
         t.setDaemon(false);
         t.setPriority(Thread.MAX_PRIORITY);
         t.start();
         return t;
     }
 }
