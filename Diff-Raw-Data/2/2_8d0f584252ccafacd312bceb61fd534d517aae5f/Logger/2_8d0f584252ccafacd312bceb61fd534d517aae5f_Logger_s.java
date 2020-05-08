 package ahs.jmx;
 
 import ahs.util.*;
 
 import java.io.*;
 import java.lang.management.*;
 import java.util.*;
 
 /**
  * Produces a stream where each line contains datapoints about system resource usage
  * metrics at a point in time.
  * 
 * "msUptime percentCpuUse mbHeapTotal mbHeapYoung mbHeapSurvivor mbHeapOld"
  * 
  * It is also possible to log the currently committed sizes of all heaps, but this class
  * doesn't do it. It is unfortunately NOT possible to get the percent usage per CPU core.
  * Things in megabytes are generally rounded up. Options for the naming of garbage
  * collector generations are not particularly well specified, so we take our best guess
  * there (and we ignore changes to the list of memory mx beans after initialization). We
  * could also log the number of collections run so far if we could consistently pick out
  * which of the two collectors was the "old" and the "young", but that again would be
  * making a lot of really dodgy assumptions and probably wouldn't be super useful anyway
  * so this class doesn't do it.
  * 
  * @author hash
  * 
  */
 public class Logger {
 	public Logger(File $outputFile) throws FileNotFoundException {
 		this(new PrintStream($outputFile));
 	}
 	
 	public Logger(PrintStream $output) {
 		$ps = $output;
 		$cpumon = new CpuMonitor();
 		$memb_all = ManagementFactory.getMemoryMXBean();
 		
 		// guess for memory pools
 		MemoryPoolMXBean $memb_young = null; 
 		MemoryPoolMXBean $memb_survivor = null;
 		MemoryPoolMXBean $memb_old = null;
 		for (MemoryPoolMXBean $memb : ManagementFactory.getMemoryPoolMXBeans()) {
 			String $mn = $memb.getName().toLowerCase();
 			if ($mn.contains("young") || $mn.contains("eden")) {
 				$memb_young = $memb;
 				continue;
 			}
 			if ($mn.contains("survivor")) {
 				$memb_survivor = $memb;
 				continue;
 			}
 			if ($mn.contains("old")) {
 				$memb_old = $memb;
 				continue;
 			}
 		}
 		this.$memb_young = $memb_young;
 		this.$memb_survivor = $memb_survivor;
 		this.$memb_old = $memb_old;
 		
 	}
 
 	private static final RuntimeMXBean	RTB	= ManagementFactory.getRuntimeMXBean();	// for getting uptime
 	private final PrintStream		$ps;
 	private final CpuMonitor		$cpumon;
 	private final MemoryMXBean		$memb_all;
 	private final MemoryPoolMXBean		$memb_young;
 	private final MemoryPoolMXBean		$memb_survivor;
 	private final MemoryPoolMXBean		$memb_old;
 	
 	/**
 	 * Collects a set of datapoints and prints the line of them to the Logger's
 	 * PrintStream.
 	 * 
 	 * If you're okay with calling this directly, go ahead and do that, but don't
 	 * combine direct use with use of the convenience thread option provided because
 	 * we aren't synchronized for that. It's also inadvisable to call this too often
 	 * because it forces recalculation of heap allocation sizes that aren't otherwise
 	 * necessary.
 	 */
 	public void snapshot() {
 		$ps.format("%f\t%f\t%d\t%d\t%d\t%d\n",
 				RTB.getUptime() / 1000F,		// i'm a fan of seconds, believe it or not
 				$cpumon.getTotalUsageNormalized(),	// i assume you're going to graph this externally and so having a consistent 0..100 is a good thing?  otherwise there's no guarantee you'd know for sure what the upper bound would be from this data alone if you never reached it.
 				$memb_all.getHeapMemoryUsage().getUsed() / (1024*1024),
 				$memb_young.getUsage().getUsed() / (1024*1024),
 				$memb_survivor.getUsage().getUsed() / (1024*1024),
 				$memb_old.getUsage().getUsed() / (1024*1024)
 		);
 		$ps.flush();
 	}
 	
 	public void automate(final int $frequency) {
 		Thread $t = new Thread("ahs.jmx.Logger automation") {
 			public void run() {
 				while (true) {
 					try {
 						X.chillInterruptably($frequency);	// sooo yeh this can drift.  but i really don't much care.
 					} catch (InterruptedException $e) {
 						// we're being told to just stop, i guess.
 						break;
 					}
 					snapshot();
 				}
 			}
 		};
 		$t.setDaemon(true);
 		$t.start();
 	}
 }
