 import tcwi.TCWIFile.ErrorFile;
 
 
 @SuppressWarnings("rawtypes")
 public class Tuple<K,V> implements Comparable<Tuple>{
 	public K k;
 	public V v;
 	
 	public Tuple(K k, V v){
 		this.k = k;
 		this.v = v;
 	}
 	
 	@Override
 	/**
	 * Comparing Keys
 	 */
 	public int compareTo(Tuple t) {
 		if(t.k instanceof String && t.v instanceof ErrorField){
 			if(this.equals(t)){
 				return 0;
 			}
 			for(int i=0;i<((String) t.k).length();i++){
				if(((String) this.k).length()>i){
 					if(((String) t.k).charAt(i)!=((String)this.k).charAt(i)){
 						if(((String) t.k).charAt(i)>((String)this.k).charAt(i)){
 							return -1;
 						}else{
 							return 1;
 						}
 					}
 				}else{
 					return 1;
 				}
 			}
 			
 		}
 		return 0;
 	}
 
 	@Override
 	public boolean equals(Object arg0) {
 		if(arg0 instanceof ErrorFile){
 			return this.k.equals(((Tuple) arg0).k)&&this.v.equals(((Tuple) arg0).v) ? true : false;
 		}else{
 			return false;
 		}
 	}
 }
