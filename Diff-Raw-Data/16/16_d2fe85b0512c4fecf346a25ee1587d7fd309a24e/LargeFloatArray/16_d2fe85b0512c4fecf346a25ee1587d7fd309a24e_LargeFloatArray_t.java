 package graph;
 
 import waba.util.*;
 
 public class LargeFloatArray 
 {
     public static int ChunkSize = 1000;
     public static int MaxNumChunks = 100;
 
     Vector chunks = new Vector();
     int curChunk = 0;
     int curIndex = 0;
 
     boolean isFull = false;
 
     public float ref;
     public float min, max;
 
     public LargeFloatArray()
     {
 		int i;
 
 		chunks.add(new float [ChunkSize]);
 		min = 1;
 		max = -1;
 
 		for(i=0; i<100; i++){
 			min *= (float)10;
 			max *= (float)10;
 		}
 
     }
 
     public void clear()
     {
 		chunks = new Vector();
 		chunks.add(new float [ChunkSize]);
 		curChunk = 0;
 		curIndex = 0;
		isFull = false;
 
 		min = 1;
 		max = -1;
 
 		for(int i=0; i<100; i++){
 			min *= (float)10;
 			max *= (float)10;
 		}
     }
 
     public void free()
     {
 
     }
 
     public float getFloat(int index)
     {
 		int chunkPos = index/ChunkSize;
 		float [] data = (float [])chunks.get(chunkPos);
 		return data[index % ChunkSize];
     }
 
     public float [] getFloats(int start, int length)
     {
 		float [] ret = new float [length];
 
 		for(int i=0; i<length; i++){
 			ret[i] = getFloat(i+start);
 		}
 		return ret;
     }
 
     public boolean addFloat(float val)
     {
 		if(isFull) return false;
 
 		float [] data = (float [])chunks.get(curChunk);
 		data[curIndex] = val - ref;
 
 		if(max < val) max = val;
 		if(min > val) min = val;
 
 		curIndex++;
 		if(curIndex >= ChunkSize){
 			curChunk++;
 			curIndex = 0;
 			if(curChunk >= MaxNumChunks){
 				isFull = true;		
 				return false;
 			}
 			chunks.add(new float [ChunkSize]);
 		}
 		return true;
     }
 
     public boolean addFloats(float [] vals, int start, int step, int count)
     {
 		float val;
 
 		if(isFull) return false;
 
 		float [] data = (float [])chunks.get(curChunk);
 
 		for(int i=start; i<count*step; i+=step){
 			val = vals[i];
 			data[curIndex] = val - ref;
 	    
 			if(max < val) max = val;
 			if(min > val) min = val;
 
 			curIndex++;
 			if(curIndex >= ChunkSize){
 				curChunk++;
 				curIndex = 0;
 				if(curChunk >= MaxNumChunks){
 					isFull = true;		
 					return false;
 				}
 				data = new float [ChunkSize];
 				chunks.add(data);
 			}
 		}
 
 		return true;
     }
 
     public int getCount()
     {
 		return curChunk*ChunkSize + curIndex;
     }
 }
