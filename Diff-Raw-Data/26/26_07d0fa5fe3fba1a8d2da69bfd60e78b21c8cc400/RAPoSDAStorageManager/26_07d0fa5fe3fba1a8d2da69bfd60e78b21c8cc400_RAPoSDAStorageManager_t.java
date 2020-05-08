 package sim.storage.manager;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import sim.Block;
 import sim.Request;
 import sim.Response;
 import sim.storage.CacheResponse;
 import sim.storage.DiskResponse;
 import sim.storage.manager.cdm.RAPoSDACacheDiskManager;
 import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
 import sim.storage.manager.cmm.RAPoSDACacheWriteResponse;
 import sim.storage.manager.ddm.RAPoSDADataDiskManager;
 import sim.storage.util.DiskState;
 import sim.storage.util.ReplicaLevel;
 
 public class RAPoSDAStorageManager {
 
 	/**
 	 * TODO
 	 * データディスクは，それぞれ担当するBlockIDの範囲を持っていること．
 	 * そうしないと，新しい書き込みは常にラウンドロビンでディスクに割り当てられ，キャッシュメモリのキューの増え方は，
 	 * 各メモリで常に同一となってしまう
 	 */
 
 	private RAPoSDACacheMemoryManager cmm;
 	private RAPoSDACacheDiskManager cdm;
 	private RAPoSDADataDiskManager ddm;
 
 	private final int blockSize;
 	private final int numReplica;
 
 	private BigInteger blockNumber = new BigInteger("0");
 
 	/**
 	 * A map between client request and corresponding blocks.
 	 * key; request key
 	 * value: corresponding blocks
 	 */
 	private HashMap<Long, Block[]> requestMap;
 
 	public RAPoSDAStorageManager(
 			RAPoSDACacheMemoryManager cmm,
 			RAPoSDACacheDiskManager cdm,
 			RAPoSDADataDiskManager ddm,
 			int blockSize,
 			int numReplica) {
 
 		this.cmm = cmm;
 		this.cdm = cdm;
 		this.ddm = ddm;
 		this.blockSize = blockSize;
 		this.numReplica = numReplica;
 
 		this.requestMap = new HashMap<Long, Block[]>();
 	}
 
 	public Response read(Request request) {
 		Block[] blocks = requestMap.get(request.getKey());
 		if (blocks == null) throw new IllegalArgumentException();
 
 		double respTime = Double.MIN_VALUE;
 
 		for (Block b : blocks) {
 			// retrieve from cache memory
 			CacheResponse cmResp = cmm.read(b);
 			if (!Block.NULL.equals(cmResp.getResult())) {
 				respTime = respTime < cmResp.getResponseTime()
 							? cmResp.getResponseTime() : respTime;
 			} else {
 				// retrieve from cache disk
 				DiskResponse cdResp = cdm.read(b);
 				if (cdResp.getResults().length == 1) {
 					respTime = respTime < cdResp.getResponseTime()
 							? cdResp.getResponseTime() : respTime;
 				} else {
 					// read from data disk.
 					DiskResponse ddResp = readFromDataDisk(b);
 					assert ddResp.getResults().length == 1;
 					respTime = respTime < ddResp.getResponseTime()
 							? ddResp.getResponseTime() : respTime;
 				}
 			}
 		}
 		return new Response(request.getKey(), respTime);
 	}
 
 	private DiskResponse readFromDataDisk(Block block) {
 
		List<DiskState> relatedDiskStates = ddm.getRelatedDisksState(block);
		List<DiskState> activeDiskStates = extractActiveDisks(relatedDiskStates);
 
 		// case 1. one of n disks is spinning.
		if (activeDiskStates.size() == 1)
			return actualRead(block, activeDiskStates.get(0));
 
 		// case 2. some of n disks are spinning.
		if (activeDiskStates.size() > 0
				&& activeDiskStates.size() < relatedDiskStates.size()) {
			DiskState diskState = cmm.getMaxBufferDisk(activeDiskStates);
			return actualRead(block, diskState);
 		}
 
 		// case 3. all of n disks are stopping.
		assert activeDiskStates.size() == 0;
		DiskState diskState = ddm.getLongestStandbyDisk(activeDiskStates);
		return actualRead(block, diskState);
 	}
 
	private DiskResponse actualRead(Block block, DiskState diskState) {
 		int ownerDiskId = assignOwnerDiskId(
 				block.getPrimaryDiskId(), diskState.getRepLevel());
 		block.setOwnerDiskId(ownerDiskId);
 		block.setRepLevel(diskState.getRepLevel());
 		return ddm.read(new Block[]{block});
 	}
 
 	private List<DiskState> extractActiveDisks(List<DiskState> diskStates) {
 		List<DiskState> result = new ArrayList<DiskState>();
 		for (DiskState state : diskStates) {
 			if (DiskState.State.ACTIVE.equals(state.getState())
 					|| DiskState.State.IDLE.equals(state.getState()))
 				result.add(state);
 		}
 		return result;
 	}
 
 	public Response write(Request request) {
 		Block[] blocks = requestMap.get(request.getKey());
 		if (blocks == null) {
 			// new request
 			blocks = divideRequest(request);
 			requestMap.put(request.getKey(), blocks);
 		}
 
 		double respTime = Double.MIN_VALUE;
 
 		for (Block block : blocks) {
 			Block[] replicas = createReplicas(block);
 			for (Block b : replicas) {
 				RAPoSDACacheWriteResponse response = cmm.write(b);
 				if (response.getOverflows().length == 0) {
 					respTime =
 						response.getResponseTime() > respTime
 						? response.getResponseTime()
 						: respTime;
 				} else { // cache overflow
 					double arrivalTime =
 						request.getArrvalTime() + response.getResponseTime();
 
 					// write to data disk
 					DiskResponse ddResp = writeToDataDisk(response, arrivalTime);
 
 					// write to cache disk asynchronously
 					writeToCacheDisk(ddResp.getResults(), ddResp.getResponseTime());
 
 					// delete blocks on the cache written in disks.
 					double cmDeletedTime = deleteBlocksOnCache(
 							ddResp.getResults(),
 							ddResp.getResponseTime() + arrivalTime);
 
 					// return least response time;
 					double tempResp =
 						arrivalTime + ddResp.getResponseTime() + cmDeletedTime;
 					respTime = respTime < tempResp ? tempResp : respTime;
 				}
 			}
 		}
 		return new Response(request.getKey(), respTime);
 	}
 
 	private double deleteBlocksOnCache(Block[] blocks, double arrivalTime) {
 		updateArrivalTimeOfBlocks(blocks, arrivalTime);
 		double response = Double.MIN_VALUE;
 		for (Block block : blocks) {
 			CacheResponse cmResp = cmm.remove(block);
 			response = response < cmResp.getResponseTime()
 							? cmResp.getResponseTime() : response;
 		}
 		return response;
 	}
 
 	private DiskResponse writeToDataDisk(
 			RAPoSDACacheWriteResponse cmResp, double arrivalTime) {
 		if(!ddm.isSpinning(cmResp.getMaxBufferDiskId(), arrivalTime))
 			ddm.spinUp(cmResp.getMaxBufferDiskId(), arrivalTime);
 		Block[] blocks = cmResp.getOverflows();
 		updateArrivalTimeOfBlocks(blocks, arrivalTime);
 		return ddm.write(blocks);
 	}
 
 	private DiskResponse writeToCacheDisk(Block[] blocks, double arrivalTime) {
 		updateArrivalTimeOfBlocks(blocks, arrivalTime);
 		return cdm.write(blocks);
 	}
 
 	private Block[] createReplicas(Block block) {
 		Block[] replicas = new Block[numReplica];
 		block.setRepLevel(ReplicaLevel.ZERO);
 		block.setOwnerDiskId(block.getPrimaryDiskId());
 		replicas[0] = block;
 		int repCounter = numReplica;
 		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
 			repCounter--;
 			if (repLevel.equals(ReplicaLevel.ZERO)) continue;
 			if (repCounter < 0) break;
 			Block replica = new Block(
 					nextBlockId(),
 					block.getAccessTime(),
 					block.getPrimaryDiskId());
 			replica.setRepLevel(repLevel);
 			replica.setOwnerDiskId(
 					assignOwnerDiskId(
 							replica.getPrimaryDiskId(),
 							replica.getRepLevel()));
 			replicas[repLevel.getValue()] = replica;
 		}
 		return replicas;
 	}
 
 	private Block[] divideRequest(Request request) {
 		Block[] blocks = null;
 		int numBlocks = (int)Math.ceil(request.getSize() / blockSize);
 		assert numBlocks > 0;
 		blocks = new Block[numBlocks];
 		for (int i=0; i < numBlocks; i++) {
 			BigInteger blockId = nextBlockId();
 			blocks[i] = new Block(blockId,
 								  request.getArrvalTime(),
 								  assignPrimaryDiskId(blockId));
 		}
 		return blocks;
 	}
 
 	private int assignPrimaryDiskId(BigInteger blockId) {
 		BigInteger numDataDisk =
 			new BigInteger(String.valueOf(ddm.getNumberOfDataDisks()));
 		// TODO try to separate assignor class.
 		// There is not only roundrobin favor assgin algorithm.
 		return (blockId.mod(numDataDisk)).intValue();
 	}
 
 	private int assignOwnerDiskId(int primaryDiskId, ReplicaLevel repLevel) {
 		return primaryDiskId + repLevel.getValue() % ddm.getNumberOfDataDisks();
 	}
 
 	private BigInteger nextBlockId() {
 		BigInteger next = new BigInteger(blockNumber.toString());
 		blockNumber.add(BigInteger.ONE);
 		return next;
 	}
 
 	private void updateArrivalTimeOfBlocks(Block[] blocks, double arrivalTime) {
 		for (Block block : blocks) {
 			block.setAccessTime(arrivalTime);
 		}
 	}
 }
