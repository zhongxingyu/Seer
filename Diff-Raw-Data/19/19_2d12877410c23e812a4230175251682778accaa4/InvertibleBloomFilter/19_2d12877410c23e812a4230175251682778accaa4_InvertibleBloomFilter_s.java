 package bloom.filters;
 
 public class InvertibleBloomFilter {
 	
 	private int hashCount;
 	private int size; 
 	
 	public InvertibleBloomFilter(int hashCount, int size){
 		this.hashCount = hashCount;
 		this.size = size;
 	}
 	
 	public boolean insert(String key){
 		return true;
 	}
 	
 	public boolean remove(String key){
 		return true;
 	}
 	
	public boolean find(String key){
		return true;
 	}
 
 }
