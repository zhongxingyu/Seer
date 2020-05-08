 package sim.storage.manager.ddm;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import sim.Block;
 import sim.storage.DiskResponse;
 import sim.storage.util.DiskInfo;
 import sim.storage.util.ReplicaLevel;
 
 public class NormalDataDiskManager implements IDataDiskManager {
 
 	private int numberOfDataDisks;
 	private int numberOfReplica;
 	private HashMap<Integer, NormalDataDisk> dataDiskMap;
 
 	public NormalDataDiskManager(
 			int numberOfDataDisks,
 			int numberOfReplica,
 			HashMap<Integer, NormalDataDisk> dataDiskMap) {
 
 		if (numberOfDataDisks != dataDiskMap.size())
 			throw new IllegalArgumentException("number of data disks is invalid.");
 
 		this.numberOfDataDisks = numberOfDataDisks;
 		this.numberOfReplica = numberOfReplica;
 		this.dataDiskMap = dataDiskMap;
 	}
 
 	@Override
 	public DiskResponse read(Block[] blocks) {
 		double arrivalTime = blocks[0].getAccessTime();
 		double respTime = Double.MIN_VALUE;
 
 		Random random = new Random(System.currentTimeMillis());
 
 		for (Block block : blocks) {
 			int targetDiskId = getTargetDiskIdAtRandom(block, random);
 
 			block.setAccessTime(arrivalTime);
 
 			NormalDataDisk dd = dataDiskMap.get(targetDiskId);
 			assert dd != null;
 			// TODO this can be optimized for sequencial block access.
 			double ddResp = dd.read(new Block[]{block});
 			respTime = respTime < ddResp ? ddResp : respTime;
 		}
 		return new DiskResponse(respTime, blocks);
 	}
 
 	private int getTargetDiskIdAtRandom(Block block, Random random) {
 		List<DiskInfo> relatedDisks = getRelatedDisksInfo(block);
 		assert relatedDisks.size() > 0
 		: "there are no related disks with block id:" + block.getId();
 
		int targetIndex = random.nextInt(relatedDisks.size());
		return relatedDisks.get(targetIndex).getDiskId();
 	}
 
 	@Override
 	public DiskResponse write(Block[] blocks) {
 		List<Block> writtenBlocks = new ArrayList<Block>();
 		double respTime = Double.MIN_VALUE;
 		for (Block block : blocks) {
 			int targetDiskId = block.getOwnerDiskId();
 			NormalDataDisk dd = dataDiskMap.get(targetDiskId);
 			assert dd != null;
 			double tempResp = dd.write(new Block[]{block});
 			respTime = respTime < tempResp ? tempResp : respTime;
 			writtenBlocks.add(block);
 		}
 		return new DiskResponse(respTime, writtenBlocks.toArray(new Block[0]));
 	}
 
 	@Override
 	public int getNumberOfDataDisks() {
 		return this.numberOfDataDisks;
 	}
 
 	@Override
 	public List<DiskInfo> getRelatedDisksInfo(Block block) {
 		List<DiskInfo> diskInfos = new ArrayList<DiskInfo>();
 
 		int i = 0;
 		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
 			if (numberOfReplica <= i) break;
 			int diskId = (block.getPrimaryDiskId() + i) % numberOfDataDisks;
 			NormalDataDisk dd = dataDiskMap.get(diskId);
 			assert dd != null;
 			DiskInfo info = new DiskInfo(
 					diskId,
 					dd.getState(block.getAccessTime()),
 					repLevel);
 			diskInfos.add(info);
 			i++;
 		}
 		return diskInfos;
 	}
 
 	@Override
 	public void close(double closeTime) {
 		Collection<NormalDataDisk> dds = dataDiskMap.values();
 		for (NormalDataDisk dd : dds) {
 			dd.close(closeTime);
 		}
 	}
 
 }
