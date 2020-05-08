 package bengo.data_fetcher;
 
 import bengo.Bengo;
 
 public class  ReadAction{
 	int startCycle; // DataAction will set this in update
 	int neededCycles;
 	Cache cache;
 	Memory mem;
 	int address;
 
 	public ReadAction(int startCycle, int neededCycles, Memory mem, Cache cache, int address) {;
 		this.startCycle = startCycle;
 		this.neededCycles = neededCycles;
 		this.cache = cache;
 		this.mem = mem;
 		this.address = address;
 	}
 
 	public void update() {
 		if (Bengo.CURRENT_CYCLE == startCycle + neededCycles - 1) {
 			if (cache != null) { // write to cache action
 				System.out.println("Read from cache " + cache.name + "  address: " + address + "  word: " + getData()+ "  at cycle: " + Bengo.CURRENT_CYCLE);
 			}
 			else {
 				System.out.println("Read from mem " +  "  address: " + address + "  word: " + getData() + "  at cycle: " + Bengo.CURRENT_CYCLE);
 			}
 
 		}
 	}
 
 	public short getData() {
 		if (isReady()) {
 			if (cache != null) {
 				int offset = cache.map(address)[2];
 				short[] block = cache.read(address);
 				if (block == null)
 					return -1;
 				else 
					return block[offset >> 2];
 			}
 			else {
 				return mem.read(address);
 			}
 		}
 		return -555;
 	}
 	
 	public boolean isReady() {
 		return (Bengo.CURRENT_CYCLE == startCycle + neededCycles - 1);
 	}
 
 	public String toString() {
 		String s = "start: " + startCycle;
 		s += "  needed: " + neededCycles;
 		s += "  address: " + address;
 		s += "  word: " + getData();
 		s += "  from ";
 		s += cache == null? "Memory" : cache.name;
 
 		return s;
 	}
 
 }
