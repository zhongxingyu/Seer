 package sim.storage.manager;
 
 import java.util.HashMap;
 
 import sim.Parameter;
 import sim.storage.CacheParameter;
 import sim.storage.HDDParameter;
 import sim.storage.manager.cdm.ICacheDiskManager;
 import sim.storage.manager.cmm.CacheMemory;
 import sim.storage.manager.cmm.CacheMemoryFactory;
 import sim.storage.manager.cmm.ICacheMemoryManager;
 import sim.storage.manager.cmm.NormalCacheMemoryManager;
 import sim.storage.manager.ddm.IDataDiskManager;
 import sim.storage.manager.ddm.NormalDataDisk;
 import sim.storage.manager.ddm.NormalDataDiskManager;
 import sim.storage.state.DiskStateParameter;
 import sim.storage.state.WithoutSleepDiskStateManager;
 
 public class NormalStorageManagerFactory extends StorageManagerFactory {
 
 	@Override
 	protected ICacheMemoryManager createCacheMemoryManager() {
 		CacheParameter param = new CacheParameter(
 				Parameter.CACHE_MEMORY_THRESHOLD,
 				Parameter.CACHE_MEMORY_SIZE,
 				Parameter.CACHE_MEMORY_LATENCY
 		);
 
 		CacheMemory cm = null;
 		try {
 			CacheMemoryFactory factory =
 				(CacheMemoryFactory)Class.forName(
 						Parameter.CACHE_MEMORY_FACTORY).newInstance();
 			cm = factory.getCacheMemory(0, 1, param, Parameter.BLOCK_SIZE);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 //		return new NormalCacheMemoryManager(cm, Parameter.NUMBER_OF_REPLICA);
 		return new NormalCacheMemoryManager(cm);
 	}
 
 	@Override
 	protected ICacheDiskManager createCacheDiskManager() {
 		// TODO consider either to create dummy ccm or not.
 		return null;
 	}
 
 	@Override
 	protected IDataDiskManager createDataDiskManager() {
 		int numdd =
 			Parameter.NUMBER_OF_CACHE_MEMORIES
			* Parameter.NUBER_OF_DISKS_PER_CACHE_GROUP;
 		int numRep = Parameter.NUMBER_OF_REPLICA;
 		HashMap<Integer, NormalDataDisk> dataDisks = new HashMap<Integer, NormalDataDisk>();
 
 		HDDParameter param = new HDDParameter(
 				Parameter.HDD_SIZE,
 				Parameter.HDD_NUMBER_OF_PLATTER,
 				Parameter.HDD_RPM,
 				Parameter.HDD_CACHE_SIZE,
 				Parameter.HDD_TRANSFER_RATE,
 				Parameter.HDD_SECTORS_PER_TRACK,
 				Parameter.HDD_FULL_STROKE_SEEK_TIME,
 				Parameter.HDD_HEAD_SWITCH_OVERHEAD,
 				Parameter.HDD_COMMAND_OVERHEAD
 		);
 
 		WithoutSleepDiskStateManager ddstm =
 			new WithoutSleepDiskStateManager(
 					new DiskStateParameter(
 							Parameter.HDD_ACTIVE_POWER,
 							Parameter.HDD_IDLE_POWER,
 							Parameter.HDD_STANDBY_POWER,
 							Parameter.HDD_SPINDOWN_ENERGY,
 							Parameter.HDD_SPINDOWN_ENERGY,
 							Parameter.HDD_SPINDOWN_TIME,
 							Parameter.HDD_SPINUP_TIME
 					)
 			);
 
 		for (int i=0; i < numdd; i++) {
 			NormalDataDisk dd = new NormalDataDisk(i, param, ddstm);
 			dataDisks.put(i, dd);
 		}
 		return new NormalDataDiskManager(numdd, numRep, dataDisks);
 	}
 
 	@Override
 	protected StorageManager createStorageManager(ICacheMemoryManager cmm,
 			ICacheDiskManager cdm, IDataDiskManager ddm) {
 
 		int blockSize = Parameter.BLOCK_SIZE;
 		int numRep = Parameter.NUMBER_OF_REPLICA;
 
 		assert numRep > 0 : "number of replica parameter should greater than 0";
 
 		return new NormalStorageManager(
 				(NormalCacheMemoryManager)cmm,
 				(NormalDataDiskManager)ddm,
 				blockSize, numRep);
 	}
 
 }
