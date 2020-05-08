 package state4;
 
 /** stores number of times long key inserted into the hash*/
 public final class HistoryMap2 {
 	private final static class Entry{
 		long key;
 		int count;
 	}
 	private final Entry[] e;
 	private final int size;
 	private final static int maxAttempts = 32;
	private long a = (long)(Math.random()*Long.MAX_VALUE);
	private long b = (long)(Math.random()*Long.MAX_VALUE);
 	/** temp entry for rehash swapping*/
 	private final Entry temp = new Entry();
 	
 	public HistoryMap2(int size){
 		assert 1<<size > maxAttempts;
 		e = new Entry[1<<size];
 		for(int a = 0; a < 1<<size; a++){
 			e[a] = new Entry();
 		}
 		this.size = size;
 	}
 	
 	/** clears the hash*/
 	public void clear(){
 		for(int a = 0; a < 1<<size; a++){
 			e[a].key = 0;
 			e[a].count = 0;
 		}
 	}
 	
 	/** puts one instance of the appeared zkey*/
 	public void put(final long key){
 		put(key, 1);
 	}
 	
 	private void put(final long zkey, final int increment){
 		final Entry existing;
 		if((existing = getEntry(zkey)) != null){
 			existing.count += increment;
 		} else{
 			temp.key = zkey;
 			temp.count = increment;
 			for(int q = 0; q < maxAttempts; q++){
 				final int index1 = h1(a, temp.key, size);
 				final int index2;
 				final Entry e1 = e[index1];
 				swap(e1, temp);
 				if(temp.count == 0){
 					return;
 				} else if((index2 = h2(b, temp.key, size)) != index1){
 					final Entry e2 = e[index2];
 					swap(e2, temp);
 					if(temp.count == 0){
 						return;
 					}
 				}
 			}
 			final long key2 = temp.key;
 			final int count2 = temp.count;
 			rehash();
 			put(key2, count2);
 		}
 	}
 	
 	public int get(final long zkey){
 		final Entry e1 = e[h1(a, zkey, size)];
 		if(e1.key == zkey){
 			return e1.count;
 		} else{
 			final Entry e2 = e[h2(b, zkey, size)];
 			if(e2.key == zkey){
 				return e2.count;
 			} 
 		}
 		return 0;
 	}
 	
 	private Entry getEntry(final long zkey){
 		final Entry e1 = e[h1(a, zkey, size)];
 		if(e1.key == zkey){
 			return e1;
 		} else{
 			final Entry e2 = e[h2(b, zkey, size)];
 			if(e2.key == zkey){
 				return e2;
 			} 
 		}
 		return null;
 	}
 	
 	/** removes one instance of the appeared zkey*/
 	public void remove(final long zkey){
 		final Entry e1 = e[h1(a, zkey, size)];
 		if(e1.key == zkey){
 			e1.count--;
 			if(e1.count == 0){
 				e1.key = 0;
 			}
 		} else{
 			final Entry e2 = e[h2(b, zkey, size)];
 			if(e2.key == zkey){
 				e2.count--;
 				if(e2.count == 0){
 					e2.key = 0;
 				}
 			} 
 		}
 	}
 	
 	private void rehash(){
		a = (long)(Math.random()*Long.MAX_VALUE);
		b = (long)(Math.random()*Long.MAX_VALUE);
 		for(int q = 0; q < 1<<size; q++){
 			final Entry entry = e[q];
 			final long key = entry.key;
 			final int count = entry.count;
 			if(key != 0 && count != 0 && h1(a, key, size) != q && h2(b, key, size) != q){
 				entry.count = 0;
 				entry.key = 0;
 				put(key, count);
 			}
 		}
 	}
 	
 	private static void swap(Entry e1, Entry e2){
 		final long tempKey = e1.key;
 		final int tempCount = e1.count;
 		e1.key = e2.key;
 		e1.count = e2.count;
 		e2.key = tempKey;
 		e2.count = tempCount;
 	}
 	
 	private static int h1(final long a, final long zkey, final int size){
 		return (int)(a*zkey) >>> 32-size;
 	}
 	
 	private static int h2(final long b, final long zkey, final int size){
 		return (int)(b*zkey >>> 64-size);
 	}
 }
