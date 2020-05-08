 package search.exp.searchV32cc;
 
 import search.search32k.Hash;
 import search.search32k.TTEntry;
 
 /** rough but working implementation of cuckoo hashing*/
 public final class LayeredHash implements Hash{
 	/** level 1 depth hash*/
 	private final TTEntry[] l1;
 	/** level 2 any hash*/
 	private final TTEntry[] l2;
 	private final int size;
 	private final static int maxAttempts = 1;
 	private final long a = (long)(Math.random()*Long.MAX_VALUE);
 	private final long b = (long)(Math.random()*Long.MAX_VALUE);
 	
 	public LayeredHash(int size){
 		l1 = new TTEntry[1<<size];
 		l2 = new TTEntry[1<<size];
 		for(int q = 0; q < 1<<size; q++){
 			l1[q] = new TTEntry();
 			l2[q] = new TTEntry();
 		}
 		this.size = size;
 	}
 	
 	public void put(final long zkey, final TTEntry t){
 		//keep replacing until find an empty entry, entry with bad seq num, or entry with more limited depth
 		final int seq = t.seq;
 		boolean swapped = true;
 		for(int q = 0; q < maxAttempts && swapped; q++){
 			swapped = false;
 			final int index1 = h1(a, t.zkey, size);
 			if(l1[index1].zkey == 0 || l1[index1].seq != seq || t.depth < l1[index1].depth){
 				TTEntry.swap(l1[index1], t);
 				swapped = true;
 			}
 			final int index2 = h2(b, t.zkey, size);
 			if(l1[index2].zkey == 0 || l1[index2].seq != seq || t.depth < l1[index2].depth){
 				TTEntry.swap(l1[index2], t);
 				swapped = true;
 			}
 		}
 		//at this point, t gives value that 'fell out' of the depth hash
 		
 		for(int q = 0; q < maxAttempts; q++){
 			final int index1 = h1(a, t.zkey, size);
 			TTEntry.swap(l2[index1], t);
 			if(t.zkey == 0){
 				return;
 			} else{
 				final int index2 = h2(b, t.zkey, size);
 				TTEntry.swap(l2[index2], t);
 				if(t.zkey == 0){
 					return;
 				}
 			}
 		}
 		
 		/*final int index1 = h1(a, t.zkey, size);
 		if(l1[index1].zkey == 0 || l1[index1].seq != seq || t.depth < l1[index1].depth){
 			TTEntry.swap(l1[index1], t);
 		} else{
 			final int index2 = h1(a, t.zkey, size);
 			TTEntry.swap(l2[index2], t);
 		}*/
 	}
 	
 	public TTEntry get(final long zkey){
 		final int index1 = h1(a, zkey, size);
 		if(l1[index1].zkey == zkey){
 			return l1[index1];
 		} else{
 			final int index2 = h2(b, zkey, size);
 			if(l1[index2].zkey == zkey){
 				return l1[index2];
 			} else{
 				//level 2
 				if(l2[index1].zkey == zkey){
 					return l2[index1];
 				} else{
 					if(l2[index2].zkey == zkey){
 						return l2[index2];
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static int h1(final long a, final long zkey, final int size){
 		return (int)(a*zkey) >>> 32-size;
 	}
 	
 	private static int h2(final long b, final long zkey, final int size){
 		return (int)(b*zkey >>> 64-size);
 	}
	
	public void clear(){assert false;}
 }
