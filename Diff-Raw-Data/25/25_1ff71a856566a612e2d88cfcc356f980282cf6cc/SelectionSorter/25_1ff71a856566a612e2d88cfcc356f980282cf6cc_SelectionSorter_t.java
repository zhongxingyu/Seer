 package ffbit.sorting;
 
 
 public class SelectionSorter implements Sorter {
     
     public SelectionSorter() {}
     
     @Override
     public void sort(int[] sources) {
         final int length = sources.length;
         
         for (int i = 0; i < length; i++) {
             int min = i;
             for (int j = i + 1; j < length; j++) {
                if (sources[j] < sources[min]) {
                     min = j;
                 }
             }
             
            swap(sources, i, min);
         }
     }

    private void swap(int[] sources, int i, int min) {
        int tmp = sources[i];
        sources[i] = sources[min];
        sources[min] = tmp;
    }
     
 }
