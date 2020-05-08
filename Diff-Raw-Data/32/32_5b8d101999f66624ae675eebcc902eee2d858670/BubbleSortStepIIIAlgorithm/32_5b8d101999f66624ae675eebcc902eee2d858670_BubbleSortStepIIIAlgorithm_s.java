 package Classify;
 
 public class BubbleSortStepIIIAlgorithm extends BubbleSortAlgorithm{
 
     @Override
     protected void DoExecute() throws Exception{
         int n = FElements.size();
         do{
             int newn = 1;
             for (int i=0; i<n-1; ++i){
                 if (FElements.get(i) > FElements.get(i+1)){
                     Swap(i, i+1);
                     newn = i+1;
                 } // ende if
             } // ende for
             n = newn;
         } while (n > 1);
     }
 
     public BubbleSortStepIIIAlgorithm() {
         FDescription = "BubbleSort-Step-III-Algorithm";
     }
 
 }
