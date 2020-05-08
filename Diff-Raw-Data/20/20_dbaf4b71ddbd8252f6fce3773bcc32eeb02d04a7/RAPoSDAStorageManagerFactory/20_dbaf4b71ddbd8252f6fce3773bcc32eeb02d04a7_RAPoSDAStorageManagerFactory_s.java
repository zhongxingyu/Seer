 package sim.storage.manager;
 
 import java.util.HashMap;
 
 import sim.Parameter;
 import sim.storage.CacheParameter;
 import sim.storage.HDDParameter;
 import sim.storage.manager.cdm.CacheDisk;
 import sim.storage.manager.cdm.ICacheDiskManager;
 import sim.storage.manager.cdm.RAPoSDACacheDiskManager;
 import sim.storage.manager.cmm.CacheMemory;
 import sim.storage.manager.cmm.CacheMemoryFactory;
 import sim.storage.manager.cmm.ICacheMemoryManager;
 import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
 import sim.storage.manager.cmm.assignor.CacheStripingAssignor;
 import sim.storage.manager.cmm.assignor.DGAAssignor;
 import sim.storage.manager.cmm.assignor.IAssignor;
 import sim.storage.manager.ddm.DataDisk;
 import sim.storage.manager.ddm.IDataDiskManager;
 import sim.storage.manager.ddm.RAPoSDADataDiskManager;
 import sim.storage.state.WithoutSleepDiskStateManager;
 import sim.storage.state.WithSleepDiskStateManager;
 import sim.storage.state.DiskStateParameter;
 
 public class RAPoSDAStorageManagerFactory extends StorageManagerFactory {
 
 	@Override
 	protected ICacheMemoryManager createCacheMemoryManager() {
 		int numcm = Parameter.NUMBER_OF_CACHE_MEMORIES;
 		HashMap<Integer, CacheMemory> cacheMemories =
 			new HashMap<Integer, CacheMemory>();
 
 		CacheParameter param = new CacheParameter(
 				Parameter.CACHE_MEMORY_THRESHOLD,
 				Parameter.CACHE_MEMORY_SIZE,
 				Parameter.CACHE_MEMORY_LATENCY
 		);
 
 		try {
 			CacheMemoryFactory factory =
 				(CacheMemoryFactory)Class.forName(
 						Parameter.CACHE_MEMORY_FACTORY).newInstance();
 			for (int i=0; i < numcm; i++) {
 				CacheMemory cm =
 					factory.getCacheMemory(
 							i,
 							Parameter.NUMBER_OF_REPLICA,
 							param,
 							Parameter.BLOCK_SIZE);
 				cacheMemories.put(i, cm);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		IAssignor assignor = getAssignor(Parameter.CACHE_MEMORY_ASSIGNOR);
 
 		return new RAPoSDACacheMemoryManager(
 				cacheMemories, assignor, Parameter.NUMBER_OF_REPLICA);
 	}
 
 	// TODO Apply fatory method pattern or related patterns to this method.
 	private IAssignor getAssignor(String assignorName) {
 		IAssignor assignor = null;
 		if (assignorName == null)
 			throw new IllegalArgumentException("Assignor name is null.");
 		if (assignorName.equals("dga"))
 			assignor = new DGAAssignor(
 					Parameter.NUMBER_OF_CACHE_MEMORIES,
					Parameter.NUBER_OF_DISKS_PER_CACHE_GROUP);
 		else if (assignorName.equals("cs"))
 			assignor = new CacheStripingAssignor(
 					Parameter.NUMBER_OF_CACHE_MEMORIES);
 		return assignor;
 	}
 
 	@Override
 	protected ICacheDiskManager createCacheDiskManager() {
 		int numcd = Parameter.NUMBER_OF_CACHE_DISKS;
 		HashMap<Integer, CacheDisk> cacheDisks = new HashMap<Integer, CacheDisk>();
 
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
 
 		WithoutSleepDiskStateManager cdstm =
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
 
 		for (int i=0; i < numcd; i++) {
 			CacheDisk cd = new CacheDisk(i, Parameter.BLOCK_SIZE, param, cdstm);
 			cacheDisks.put(i, cd);
 		}
 
 		return new RAPoSDACacheDiskManager(numcd, cacheDisks);
 	}
 
 	@Override
 	protected IDataDiskManager createDataDiskManager() {
 		int numdd =
 			Parameter.NUMBER_OF_CACHE_MEMORIES
			* Parameter.NUBER_OF_DISKS_PER_CACHE_GROUP;
 		int numRep = Parameter.NUMBER_OF_REPLICA;
 		HashMap<Integer, DataDisk> dataDisks = new HashMap<Integer, DataDisk>();
 
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
 
 		WithSleepDiskStateManager ddstm =
 			new WithSleepDiskStateManager(
 					Parameter.SPINDOWN_THRESHOLD,
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
 			DataDisk dd = new DataDisk(i, param, ddstm);
 			dataDisks.put(i, dd);
 		}
 		return new RAPoSDADataDiskManager(numdd, numRep, dataDisks);
 	}
 
 	@Override
 	protected StorageManager createStorageManager(
 			ICacheMemoryManager cmm, ICacheDiskManager cdm, IDataDiskManager ddm) {
 		int blockSize = Parameter.BLOCK_SIZE;
 		int numRep = Parameter.NUMBER_OF_REPLICA;
 
 		assert numRep > 0 : "number of replica parameter should greater than 0";
 
 		return new RAPoSDAStorageManager(
 				(RAPoSDACacheMemoryManager)cmm,
 				(RAPoSDACacheDiskManager)cdm,
 				(RAPoSDADataDiskManager)ddm,
 				blockSize, numRep);
 	}
 
 }
