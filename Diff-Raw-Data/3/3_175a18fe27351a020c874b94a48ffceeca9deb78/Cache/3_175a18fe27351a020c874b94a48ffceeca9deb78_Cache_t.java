 public class Cache {
 	public class Set {
 		int assoc, arr[];
 		boolean fifo;
 
 		public Set(int assoc, boolean fifo) {
 			this.assoc = assoc;
 			arr = new int[assoc];
 			this.fifo = fifo;
 			for(int i = 0; i < assoc; ++i)
 				arr[i] = -1;
 		}
 
 		public boolean matchTag(int tag) {
 			for (int i = 0; i < assoc; i++)
 				if (arr[i] != -1 && (arr[i] >> 1) == tag) {
 					if (!fifo) {
 						Integer a = arr[i];
 						for (int j = i - 1; j >= 0; --j)
 							arr[j + 1] = arr[j];
 						arr[0] = a;
 					}
 					return true;
 				}
 			return false;
 		}
 
 		// returns true if the evicted block had been modified
 		public boolean replace(int tag) {
 			if (matchTag(tag))
 				return false;
 			boolean modified = (arr[assoc - 1] != -1)
 					&& ((arr[assoc - 1] & 1) == 1);
 			for (int j = assoc - 2; j >= 0; --j)
 				arr[j + 1] = arr[j];
 			arr[0] = tag << 1;
 			return modified;
 		}
 
 		// returns true if a block was evicted and it had been modified
 		public boolean setWritten(int tag) {
 			for (int i = 0; i < assoc; i++)
 				if (arr[i] != -1 && (arr[i] >> 1) == tag) {
 					arr[i] |= 1;
 					return false;
 				}
 			boolean wb = replace(tag);
 			setWritten(tag);
 			return wb;
 		}
 	}
 
 	Set[] L1, L2;
 	long forL1, forL2, missL1, missL2;
 
 	public Cache() {
 		L1 = new Set[512];
 		for (int i = 0; i < 512; i++)
 			L1[i] = new Set(2, true);
 		L2 = new Set[2048];
 		for (int i = 0; i < 2048; i++)
 			L2[i] = new Set(8, false);
 	}
 
 	public int[] getL1tag(long addr) {
 		int remn = (int)(addr % 32); // 32 byte block size
 		int tag = (int)(addr >> 5);
 		int index = tag % 512; // 9 bit index
 		tag >>= 9;
 		return new int[] { tag, index, remn };
 	}
 
 	public int[] getL2tag(long addr) {
 		int remn = (int)(addr % 128); // 128 byte block size
 		int tag = (int)(addr >> 7);
 		int index = tag % 2048;
 		tag >>= 11;
 		return new int[] { tag, index, remn };
 	}
 
 	public int access(long addr, boolean write) {
 		int[] L1tags = getL1tag(addr), L2tags = getL2tag(addr);
 
 		forL1++;
 		// present in L1
 		if (L1[L1tags[1]].matchTag(L1tags[0])) {
 			if (!write)
 				return 1;
 
 			// L1 is write-through
			forL2++;
 			// but because of different replacement policies of l1 and l2, its possible that something might be in l1 but not in l2
			missL2 += L2[L2tags[1]].matchTag(L2tags[0]) ? 0 : 1;
 			// If so, we need to write to memory if the evicted block was modified.
 			return L2[L2tags[1]].setWritten(L2tags[0]) ? 209 : 9;
 		}
 		// not present in L1 but in L2
 		missL1++;
 		forL2++;
 		if (L2[L2tags[1]].matchTag(L2tags[0])) {
 
 			// take it to L1
 			L1[L1tags[1]].replace(L1tags[0]);
 
 			if (write)
 				L2[L2tags[1]].setWritten(L2tags[0]);
 
 			return 9;
 		}
 		// present in neither
 		missL2++;
 		// take it to both L2 and L1, taking care of write-back eviction in L2
 		L1[L1tags[1]].replace(L1tags[0]);
 
 		if (write)
 			return L2[L2tags[1]].setWritten(L2tags[0]) ? 409 : 209;
 		return L2[L2tags[1]].replace(L2tags[0]) ? 409 : 209;
 	}
 	
 	public double L1LocalMiss()	{
 		return ((double)(100 * missL1) / (double)forL1);
 	}
 	public double L2LocalMiss()	{
 		return ((double)(100 * missL2) / (double)forL2);
 	}
 }
