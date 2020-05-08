 public class Collection {
 
 
     private final Integer[] data;
 
     public Collection(Integer[] data) {
         this.data = data;
     }
 
     public Object getOperationResult(CollectionsOperation operation) {
         Integer currentMostOperativelyCorrect = data[0];
         for(Integer integer : data){
             if (operation.isTrue(integer, currentMostOperativelyCorrect)){
                 currentMostOperativelyCorrect = integer;
             }
         }
         return currentMostOperativelyCorrect;
     }
 
    public int getLargestUnder(int i) {
        return 0;
     }
 }
