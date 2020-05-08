 package Classify;
 
 public class BubbleSortAlgorithm extends SortAlgorithm{
 
     @Override
     protected void DoExecute() throws Exception{
         for (int n=FElements.size(); n>1; n=n-1){
             for (int i=0; i<n-1; i=i+1){
                 if (FElements.get(i) > FElements.get(i+1)) {
                     Swap(i,i+1);
                 }
             }
         }
     }
 
     protected void Swap(Integer aI, Integer aJ) throws Exception{
         int x = FElements.get(aI);
         FElements.set(aI, FElements.get(aJ));
         FElements.set(aJ, x);
        OneStepSorted();
     }
 
     public BubbleSortAlgorithm() {
         FDescription = "BubbleSort-Algorithm";
     }
 
 }
