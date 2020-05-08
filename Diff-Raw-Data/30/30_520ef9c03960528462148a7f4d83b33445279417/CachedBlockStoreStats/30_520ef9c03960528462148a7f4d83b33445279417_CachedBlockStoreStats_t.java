 package org.ogreg.ase4j.file;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.ogreg.common.nio.BaseIndexedStoreStatsProvider;
 import org.ogreg.common.nio.BaseIndexedStoreStatsProvider.Stats;
 
 /**
  * Statistics tool for the {@link CachedBlockStore}.
  * 
  * @author Gergely Kiss
  */
 public class CachedBlockStoreStats {
 
 	public static void main(String[] args) throws IOException {
 		if (args.length != 1) {
 			System.err.println("Usage: " + CachedBlockStoreStats.class.getName() + " <store file>");
 			System.exit(-1);
 		}
 
 		new CachedBlockStoreStats().start(new File(args[0]));
 	}
 
 	private void start(File file) throws IOException {
 		CachedBlockStore cbs = new CachedBlockStore();
 		cbs.open(file);
 
 		Stats stats = BaseIndexedStoreStatsProvider.getStats(cbs);
 
 		System.out.printf("Statistics of %s:\n", file.getAbsolutePath());
		System.out.printf(" Index capacity: \t%d\n", stats.maxEntries);
 		System.out.printf(" Association blocks: \t%d\n", stats.numEntries);
 		System.out.printf(" Unused index bytes: \t%d b\n", stats.unusedIndexBytes);
 		System.out.printf(" Unused entry bytes: \t%d b\n", stats.unusedEntryBytes);
		System.out.printf(" Number of holes: \t%d\n", stats.holes.size());
 		if (stats.holes.size() > 0) {
 			System.out.printf(" Average hole size: \t%.2f b\n", (double) stats.unusedEntryBytes
 					/ stats.holes.size());
 		}
 	}
 }
