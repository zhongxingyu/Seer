 package bengo.data_fetcher;
 
 import java.util.Arrays;
 
 import bengo.Bengo;
 
 public class BengoData {
 	/*
 	 * This is a container for the caches and memory
 	 */
 	
 	int levels;
 	Cache[] caches;
 	Memory mem;
 	
 	public BengoData(int levels, int[] sizes, int[]lineSizes,
 					int[] hitTimes, int[] assocs, int[] hitPolicies,
 					int[] missPolicies) {
 
 		this.levels = levels;
 		caches = new Cache[levels];
 		
 		// notes that blockSizes must be in an increasing order
 		if (levels >= 1)
 			caches[0] = new Cache(sizes[0], lineSizes[0], hitTimes[0], assocs[0], hitPolicies[0], missPolicies[0]);
 		if (levels >= 2)
 			caches[1] = new Cache(sizes[1], lineSizes[1], hitTimes[1], assocs[1], hitPolicies[1], missPolicies[1]);
 		if (levels >= 3)
 			caches[2] = new Cache(sizes[2], lineSizes[2], hitTimes[2], assocs[2], hitPolicies[2], missPolicies[2]);
 	}
 	
 	public DataAction fetch(int address) {
 		int neededCycles = 0;
 		int[] res;
		int wordOffset;
 		int i;
 		boolean foundInCache = false;
 		int value = -11111111;
 		for(i = 0; i < levels; i++) {
 			res = caches[i].read(address);
			wordOffset = caches[i].map(address)[2] >> 2;
 			neededCycles += caches[i].hitTime; // in case of hit or miss
 			if (res != null) { // in case of hit
 				foundInCache = true;
				value = res[wordOffset];
 				break;
 			}
 		}
 		
 		if (!foundInCache) {
 			value = mem.read(address);
 			neededCycles += mem.hitTime;
 		}
 		
 		// write in the caches where the data doesn't exist
 		// assume no 
 		for (int j = i-1; j >= 0; j--) {
			wordOffset = caches[j].map(address)[2] >> 2;
 			int[] block = new int[caches[j].blockSize];
			int baseAddress = address - wordOffset;
			for (int k = 0; k < caches[j].blockSize; k++) {
				block[k] = mem.read(baseAddress + k);
			}
			block[wordOffset] = value;
 			write(j, address, block, true);
 		}
 		
 		return new DataAction(address, Bengo.CURRENT_CYCLE, neededCycles, value);
 	}
 	
 	// instant, no penalty calculations
 	public DataAction write(int cacheIndex, int address, int[] block, boolean instant) {
 		// TODO 
 		if (instant) {
 			caches[cacheIndex].write(address, block);
 		}
 		return null;
 	}
 	
 	public static void test() {
 		int levels = 2;
 		int[] numWords = {8, 16};
 		int[] blockSizes =  {1,2};
 		int[] hitTimes =  {10, 20};
 		int[] assocs =  {1, 2};
 		int[] hitPolicies =  {1,1};
 		int[] missPolicies = {1,1};
 		BengoData d = new BengoData(levels, numWords, blockSizes,
 									hitTimes, assocs, hitPolicies, missPolicies);
 		
 		System.out.println(d.caches[0]);
 		System.out.println("-------------------------");
 		System.out.println(d.caches[1]);
 		Memory mem = new Memory(20);
 		mem.write(0, 99);
 		mem.write(1, 98);
 		mem.write(7, 97);
		mem.write(6, 96);
 		d.mem = mem;
 		
 //		System.out.println("caches[0] " + d.caches[0]);
 		System.out.println(d.fetch(7));
 		System.out.println(d.caches[0]);
 		System.out.println("-------------------------");
 		System.out.println(d.caches[1]);
		
		
 		System.out.println(d.fetch(7));
 		
 	}
 	
 	public static void main(String[] args) {
 		test();
 	}
 }
