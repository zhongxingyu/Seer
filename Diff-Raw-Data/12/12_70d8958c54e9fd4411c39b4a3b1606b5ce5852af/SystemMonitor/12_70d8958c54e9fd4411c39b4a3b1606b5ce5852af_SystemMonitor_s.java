 package syndeticlogic.tiro.monitor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 
 public class SystemMonitor extends AbstractMonitor implements MemoryMonitor, IOMonitor {
 	private MemoryMonitor memoryMonitor;
 	private IOMonitor ioMonitor;
 	
 	public SystemMonitor(MemoryMonitor memoryMonitor, IOMonitor ioMonitor) {
 	    super();
 	    this.memoryMonitor = memoryMonitor;
 	    this.ioMonitor = ioMonitor;
 	}
     @Override
 	public void start() {
 	    memoryMonitor.start();
 	    ioMonitor.start();
 	}
     @Override    	
 	public void finish() {
 	    memoryMonitor.finish();
 	    ioMonitor.finish();
 	}
     @Override   
     public void dumpData() {
         memoryMonitor.dumpData();
         ioMonitor.dumpData();
     }
     @Override
     protected void processMonitorOutput(BufferedReader reader) throws IOException {
         throw new RuntimeException("unsupported method call");
     }
     @Override
     public IOStats getIOStats() {
         return ioMonitor.getIOStats();
     }
     @Override
     public MemoryStats getMemoryStats() {
         return memoryMonitor.getMemoryStats();
     }
     @Override
     public String getDevice() {
         return ioMonitor.getDevice();
     }
 }
