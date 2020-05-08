 public class Cache
 {
     public class Set
 	{
 		int assoc;
 		Integer[] arr;
 		boolean fifo;
 		
 		public Set(int assoc, boolean fifo)
 		{
 			this.assoc = assoc;
 			arr = new Integer[assoc];
 			this.fifo = fifo;
 		}
 		
 		public boolean matchTag(int tag)
 		{
 			for (int i=0; i<assoc; i++)
 				if (arr[i] != null && (arr[i] >> 1) == tag)	{
 					if(!fifo)	{
 						Integer a = arr[i];
 						for(int j = i-1; j >= 0; --j)
 							arr[j+1] = arr[j];
 						arr[0] = a;
 					}
 					return true;
 				}
 			return false;
 		}
 
 		// returns true if the evicted block had been modified
 		public boolean replace(int tag)	{
 			if(matchTag(tag))
 				return false;
			boolean modified = (arr[assoc-1] != null) && ((arr[assoc-1] & 1) == 1);
 			for(int j = assoc-2; j >= 0; --j)
 				arr[j+1] = arr[j];
 			arr[0] = tag << 1;
 			return modified;
 		}
 
 		// returns true if a block was evicted and it had been modified
 		public boolean setWritten(int tag)	{
 			for (int i=0; i<assoc; i++)
 				if (arr[i] != null && (arr[i] >> 1) == tag)	{
 					arr[i] |= 1;
 					return false;
 				}
 			boolean wb = replace(tag);
 			setWritten(tag);
 			return wb;
 		}
 	}
 	
 	Set[] L1, L2;
 	public Cache()	{
 			L1 = new Set[512];
 			for (int i=0; i<512; i++)
 				L1[i] = new Set(2, true);
 			L2 = new Set[2048];
 			for (int i=0; i<2048; i++)
 				L2[i] = new Set(8, false);
 	}
 	
 	public int[] getL1tag(int addr)
 	{
 		int remn = addr % 32; 		// 32 byte block size
 		int tag = addr >> 5;
 		int index = tag % 512; 		// 9 bit index
 		tag >>= 9;
 		return new int[]{tag, index, remn};
 	}
 
 	public int[] getL2tag(int addr)
 	{
 		int remn = addr % 128; // 128 byte block size
 		int tag = addr >> 7;
 		int index = tag % 2048;
 		tag >>= 11;
 		return new int[]{tag, index, remn};
 	}
 		
 	public int access(int addr, boolean write)	{
 		int[] L1tags = getL1tag(addr), L2tags = getL2tag(addr);
 		
 		// present in L1
 		if ( L1[L1tags[1]].matchTag(L1tags[0]) )	{
 			if(!write)
 				return 1;
 			
 			// L1 is write-through
 			L2[L2tags[1]].setWritten(L2tags[0]);	
 			return 9;
 		}
         
         // not present in L1 but in L2
         if ( L2[L2tags[1]].matchTag(L2tags[0]) )    {
             
             // take it to L1
             L1[L1tags[1]].replace(L1tags[0]);
             
             if(write)
                 L2[L2tags[1]].setWritten(L2tags[0]);
             
             return 9;
         }
         
         // present in neither
 		// take it to both L2 and L1, taking care of write-back eviction in L2
         L1[L1tags[1]].replace(L1tags[0]);
         
         if (write)
             return L2[L2tags[1]].setWritten(L2tags[0]) ? 409 : 209;       
 		return L2[L2tags[1]].replace(L2tags[0]) ? 409 : 209;
 	}
 }
