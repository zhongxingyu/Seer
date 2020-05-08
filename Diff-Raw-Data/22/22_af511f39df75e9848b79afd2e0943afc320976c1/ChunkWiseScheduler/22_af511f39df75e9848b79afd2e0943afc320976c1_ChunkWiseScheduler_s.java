 package me.asofold.bukkit.fattnt.scheduler;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import me.asofold.bukkit.fattnt.config.Path;
 import me.asofold.bukkit.fattnt.config.compatlayer.CompatConfig;
 import me.asofold.bukkit.fattnt.stats.Stats;
 import me.asofold.bukkit.fattnt.utils.Utils;
 
 /**
  * Store entries chunk-wise for a somewhat fair processing order. 
  * @author mc_dev
  *
  */
 public final class ChunkWiseScheduler<T extends ScheduledEntry> {
 	
 	private static final class ChunkPos{
 		private static final int p1 = 73856093;
 	    private static final int p2 = 19349663;
 //		private static final int p3 = 83492791;
 		final int x;
 		final int z;
 		final int hashCode;
 		public ChunkPos(final int x, final int z){
 			this.x = x;
 			this.z = z;
 			hashCode = p1*x ^ p2*z;
 		}
 		@Override
 		public final int hashCode(){
 			return hashCode;
 		}
 		@Override
 		public final boolean equals(final Object obj) {
 			if (obj instanceof ChunkPos){
 				final ChunkPos other = (ChunkPos) obj;
 				return x == other.x && z == other.z;
 			}
 			else return false;
 		}
 		
 	}
 	
 	private int maxProcessTotal = 20;
 	private int chunkSize = 16;
 	private int maxStoreTotal = 5000;
 	private int maxStoreChunk = 50; 
 	private long maxStoreLifetime = 2000;
 	public long maxProcessNanos = 3500000;
 	
 	private final Map<ChunkPos, List<T>> stored = new LinkedHashMap<ChunkWiseScheduler.ChunkPos, List<T>>(300);
 	
 	private int totalSize = 0;
 	
 	public void fromConfig(CompatConfig cfg, String prefix){
 		ChunkWiseScheduler<T> ref = new ChunkWiseScheduler<T>(); // TODO maybe a settings class.
 		chunkSize = cfg.getInt(prefix + Path.chunkSize, ref.chunkSize);
 		maxStoreTotal = cfg.getInt(prefix + Path.store + Path.sep + Path.maxTotal, ref.maxStoreTotal);
 		maxStoreChunk = cfg.getInt(prefix + Path.store + Path.sep + Path.maxChunk, ref.maxStoreChunk);
 		maxProcessTotal = cfg.getInt(prefix + Path.process + Path.sep + Path.maxTotal, ref.maxProcessTotal);
 		maxProcessNanos = cfg.getLong(prefix + Path.process + Path.sep + Path.maxNanos, ref.maxProcessNanos);
 		maxStoreLifetime = cfg.getLong(prefix + Path.process + Path.sep + Path.maxMillis, ref.maxStoreLifetime);
 	}
 	
 	/**
 	 * 
 	 * Simple getting algorithm, loop through all "chunks" and add one by one till limit reached. If many chunks are there, reorder the processed ones to the end, to prevent starvation.
 	 * @return
 	 */
	public final List<T> getNextExplosions(){
 		final long ts = System.currentTimeMillis();
 		final List<T> next = new LinkedList<T>();
 		if (stored.isEmpty()) return next;
 		int done = 0;
 		final List<ChunkPos> rem = new LinkedList<ChunkPos>();
 		final Set<ChunkPos> reSchedule = new LinkedHashSet<ChunkPos>();
 		boolean many = stored.size() > maxProcessTotal;
 		while (!stored.isEmpty() && done < maxProcessTotal){
 			for (final Entry<ChunkPos, List<T>> entry : stored.entrySet()){
 				final List<T> list = entry.getValue();
 				final T candidate = list.remove(0);
 				if (ts - candidate.getExpirationTime() > maxStoreLifetime){
 					totalSize--;
 					if (list.isEmpty()){
 						rem.add(entry.getKey());
 						break;
 					}
 					while(ts - list.get(0).getExpirationTime() > maxStoreLifetime){
 						list.remove(0);
 						totalSize --;
 						if (list.isEmpty()){
 							rem.add(entry.getKey());
 							break;
 						}
 					}
 					continue;
 				}
 				next.add(candidate);
 				if (list.isEmpty()) rem.add(entry.getKey());
 				else if (many) reSchedule.add(entry.getKey());
 				done ++;
 				totalSize --;
 				if (done == maxProcessTotal) break;
 			}
 			for (final ChunkPos pos : rem){
 				stored.remove(pos);
 				if (many) reSchedule.remove(pos);
 			}
 			rem.clear();
 		}
 		if (stored.size() > maxProcessTotal){
 			for (final ChunkPos pos : reSchedule){
 				stored.put(pos, stored.remove(pos));
 			}
 		}
 		return next;
 	}
 	
 	/**
	 * Add the explosion, remove one if too many.
	 * @param explosion
 	 */
	public final void addExplosion(final T explosion){
 		if (totalSize >= maxStoreTotal) reduceStore();
		final ChunkPos pos = new ChunkPos(Utils.floor(explosion.getBlockX() / chunkSize), Utils.floor(explosion.getBlockZ() / chunkSize));
 		List<T> list = stored.get(pos);
 		if (list == null){
 			list = new LinkedList<T>();
 			stored.put(pos, list);
 		}
		list.add(explosion);
 		totalSize ++;
 		if (list.size() > maxStoreChunk){
 			list.remove(0);
 			totalSize --;
 		}
 	}
 
 	/**
 	 * Attempt at first: 
 	 */
 	private final void reduceStore() {
 		final long ts = System.currentTimeMillis();
 		boolean avOk = true;
 		boolean anyOk = false;
 		final int av;
 		final int sz  = stored.size();
 		if (totalSize == sz) av = 0;
 		else av = totalSize / stored.size();
 		final List<ChunkPos> rem = new LinkedList<ChunkPos>();
 		while (totalSize > maxProcessTotal){
 			for (final Entry<ChunkPos, List<T>> entry : stored.entrySet()){
 				final List<T> list = entry.getValue();
 				final int lsz = list.size();
 				if (ts - list.get(0).getExpirationTime() > maxStoreLifetime){
 					do {
 						list.remove(0);
 						totalSize --;
 						if (list.isEmpty()){
 							rem.add(entry.getKey());
 							break;
 						}
 					} while (ts - list.get(0).getExpirationTime() > maxStoreLifetime);
 					continue;
 				}
 				if (lsz > maxStoreChunk);
 				else if (avOk && lsz <= av) continue;
 				else if (!anyOk && lsz == 1) continue;
 				
 				list.remove(0);
 				totalSize --;
 				if (lsz == 1) rem.add(entry.getKey());
 				if (totalSize <= maxProcessTotal) break;
 			}
 			for (final ChunkPos pos : rem){
 				stored.remove(pos);
 				// ? consider sorting to end.
 			}
 			rem.clear();
 			if (totalSize > maxStoreTotal){
 				if (avOk) avOk = false;
 				else if (!anyOk) anyOk = true;
 			}
 		}
 	}
 	
 	public final boolean hasEntries(){
 		return totalSize > 0;
 	}
 	
 	public final void clear(){
 		// TODO: maybe do more looping to set members to null.
 		stored.clear();
 		totalSize = 0;
 	}
 
 	public long getTotalSize() {
 		return totalSize;
 	}
 
 	public boolean onTick(final ProcessHandler<T> handler, Stats stats, Integer idProcess, Integer idNProcess, Integer idNStore) {
 		if (hasEntries()){
 			stats.addStats(idNStore, getTotalSize());
 			final long ns = System.nanoTime();
			final List<T> entries = getNextExplosions();
 			boolean abort = false;
 			int done = 0;
 			for (final T entry : entries){
 				if (abort){
				addExplosion(entry);
 					continue;
 				}
 				handler.process(entry);
 				final long nsDone = System.nanoTime() - ns;
 				done ++;
 				if (nsDone > maxProcessNanos){
 					abort = true;
 				}
 			}
 			stats.addStats(idProcess, System.nanoTime() - ns);
 			stats.addStats(idNProcess, done);
 		}
 		return hasEntries();
 	}
 }
