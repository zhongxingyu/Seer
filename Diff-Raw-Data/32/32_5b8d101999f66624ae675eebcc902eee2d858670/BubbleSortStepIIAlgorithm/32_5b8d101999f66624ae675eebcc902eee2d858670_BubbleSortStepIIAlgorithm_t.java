 package Classify;
 
 public class BubbleSortStepIIAlgorithm extends BubbleSortAlgorithm{
 
     @Override
     protected void DoExecute() throws Exception{
         boolean swapped;
         int n = FElements.size();
         do{
             swapped = false;
             for (int i=0; i<n-1; ++i){
                 if (FElements.get(i) > FElements.get(i+1)) {
                     Swap(i, i+1);
                     swapped = true;
                 } // ende if
                OneStepSorted();
             } // ende for
             n = n-1;
         } while (swapped == true);
     }
 
     public BubbleSortStepIIAlgorithm() {
         FDescription = "BubbleSort-Step-II-Algorithm";
     }
 
 }
