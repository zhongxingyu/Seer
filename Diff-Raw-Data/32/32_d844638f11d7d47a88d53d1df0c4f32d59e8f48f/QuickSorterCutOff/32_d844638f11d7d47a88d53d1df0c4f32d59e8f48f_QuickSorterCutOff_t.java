 package anaydis.sort;
 
 import anaydis.sort.gui.SorterListener;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * Author: Tomás Vilaboa
  * Project: AnayDis-2013
  * Date: 07/09/13
  * Time: 18:04
  */
 public class QuickSorterCutOff extends QuickSorter {
 
     public QuickSorterCutOff() {
         super(SorterType.QUICK_CUT);
     }
 
     @Override
     public <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list) {
         sort(comparator, list,0,list.size()-1);
     }
 
 
     public <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list,int M) {
         sort(comparator, list,0,list.size()-1,M);
     }
 
 
 
     private <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list, int l, int r,int M) {
         if(r-l <=M){
             InsertionSorter insert=new InsertionSorter();
             for(SorterListener listener : getListeners()){
                 insert.addSorterListener(listener);
             }
            insert.sort(comparator, list,l,r);
             return;
         }
         int i=partition(comparator,list,l,r);
         sort(comparator,list,l,i-1);
         sort(comparator,list,i+1,r);
     }
      @Override
     public  <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list, int l, int r){
         sort(comparator, list,l,r,23);          //M default si no se elige un M particular
     }
 }
