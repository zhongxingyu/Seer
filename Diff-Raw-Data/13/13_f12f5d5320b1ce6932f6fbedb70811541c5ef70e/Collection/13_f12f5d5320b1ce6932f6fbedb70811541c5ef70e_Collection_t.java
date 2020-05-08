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
 
    public int getLargestUnder(int threshold) {
        int largestUnderThreshhold = 0;
        for (int number : data){
            if (number < threshold){
                if (number > largestUnderThreshhold){
                    largestUnderThreshhold = number;
                }
            }
        }
//        what happens when none is found? Throw?
        return largestUnderThreshhold;
     }
 }
