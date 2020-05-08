 package SimCache;
 
 import java.util.*; 
 
 /**
  *
  * @author guillem
  */

 public class MRUCache extends memoryCache{
     
     ArrayList<Boolean> state = new ArrayList<Boolean>();
     
     public MRUCache(){
         
         this(4);
         
     }
     
     public MRUCache(int blocks){
         
         this.memBlocks= blocks;
         this.numberMises = 0;
         
         for (int i = 0; i < this.memBlocks ; i++){ 
             
             state.add(false);
         
         }
         
         for (int i = 0; i < this.memBlocks ; i++){
             
             this.CacheBlocks.add("NULL");
         }
         
     }
     
     private void allZeros(){
         
         int allElementsZeroFlag = 0;
 
         for(int i=0; i < this.memBlocks; i++){
         
             if(state.get(i) == true){
                 
                 allElementsZeroFlag++;
                 
               }
         
         } 
     
         if(allElementsZeroFlag == this.memBlocks){
       
             for(int i=0;i<memBlocks;i++){
         
                 state.set(i,false);
         
             }
             allElementsZeroFlag = 0;
                 
         }
         
     }
 
     @Override
     boolean missBlock(Object block) {
         
        for(int i=0;i<this.memBlocks;i++){     
      
            if(state.get(i) == false){
            this.CacheBlocks.set(i, block);
            state.set(i,true);
            allZeros();
            break;
            }
 
         } 
         
        return false;
         
     }
 
     @Override
     boolean hitBlock(Object block) {
         
         state.set(this.CacheBlocks.indexOf(block),true);
         
        allZeros();
        
         return true;
     }
 
 
 }
