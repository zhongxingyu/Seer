 package syndeticlogic.tiro.monitor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Random;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import syndeticlogic.tiro.persistence.CpuStats;
 import syndeticlogic.tiro.persistence.IOStats;
 
 public class OSXIOMonitor extends AbstractMonitor implements IOMonitor {
     private static final Log log = LogFactory.getLog(OSXIOMonitor.class);
     private String[] devices;
     private IOStats[] iostats;
     private CpuStats cpustats;
 
 	public OSXIOMonitor(String... devices) {
 		super();
 		this.devices = devices;
 		String[] command = new String[devices.length+2];
		command[0] = "iostat";
		command[devices.length+1] = "5";
 		System.arraycopy(devices, 0, command, 1, devices.length);
 		setCommandAndArgs(command);
 		iostats = new IOStats[devices.length];
 		for(int i = 0; i < devices.length; i++) {
 		    iostats[i] = new IOStats(devices[i]);
 		}
 		cpustats = new CpuStats();
 	}
     @Override
 	protected void processMonitorOutput(BufferedReader reader) throws IOException {
 		reader.readLine();
 		reader.readLine();
 		reader.readLine();
 		while(true) {
 			String line = reader.readLine();
 			if(line == null) {
 				break;
 			}
 			log.info(line);
 			line = line.trim();
 			String[] values = line.split("\\s+");
			assert values.length == 3*devices.length+6;
 			int i = 0;
 			for(IOStats iostat : iostats) {
 			    Double kbt = Double.parseDouble(values[i++]);
 			    Double tps = Double.parseDouble(values[i++]);
 			    Double mbs = Double.parseDouble(values[i++]);
 			    iostat.addRawRecord(kbt, tps, mbs);
 			}
 			Long user = Long.parseLong(values[i++]);
 			Long system = Long.parseLong(values[i++]);
 			Long idle = Long.parseLong(values[i++]);
 			cpustats.addRawRecord(user, system, idle);
 		}
 	}
     @Override
     public void dumpData() {
         for(IOStats iostat : iostats) {
             iostat.dumpData();
         } 
         cpustats.dumpData();
     }
     @Override
     public IOStats[] getIOStats() {
         return iostats;
     }
     @Override
     public String[] getDevices() {
         return devices;
     }
     @Override
     public CpuStats getCpuStats() {
         return cpustats;
     }
     
 	public static void useDisk() throws IOException {
 		File file = new File("iomonitor.perf");
 		FileOutputStream out = new FileOutputStream(file);
 		byte[] bytes = new byte[1024*1024*10];
 		for(int j = 0; j < 1024*1024*10; j++) {
 			bytes[j] = (byte)(23 * j);
 		}
 		out.write(bytes);
 		out.close();
 		assert file.delete();
 	}
 	
 	public static void useCpu() {
 		Random r = new Random();
 		long first = r.nextLong();
 		int secondBound = (int)first;
 		if(secondBound < 0) {
 			secondBound = -secondBound;
 		}
 		long second = r.nextInt(secondBound);
 		long rem = -1;
 		while (rem != 0) {
 			rem = first % second;
 			first = second;
 			second = rem;
 		}
 	}
 	
 	public static void main(String[] args) throws Throwable {
 		try {
 			long starttime = System.currentTimeMillis();
			OSXIOMonitor iom = new OSXIOMonitor("disk0");//, "disk1");
 			System.out.println("Starting..");
 			iom.start();
 			Thread.sleep(1000);
 			if (args.length == 0) {
 				while (starttime + 25522 > System.currentTimeMillis()) {
 					useDisk();
 				}
 			} else {
 				while (starttime + 25522 > System.currentTimeMillis()) {
 					useCpu();
 				}
 			}
 			iom.finish();
 			iom.dumpData();
 			long duration = iom.getDurationMillis();
 			System.out.println("Duration = " + duration);
 		} catch (Throwable t) {
 			log.error("exception: ", t);
 			throw t;
 		}
 	}
 }
 
 
