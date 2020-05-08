 package anaydis.sort;
 
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * Author: Tomás Vilaboa
  * Project: AnayDis-2013
  * Date: 23/08/13
  * Time: 21:45
  */
 public class InsertionSorter extends AbstractSorter {
 
     public InsertionSorter() {
         super(SorterType.INSERTION);
     }
    @Override
    public <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list){
            sort(comparator, list,0,list.size()-1);
    }
 
 
 
    public <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list,int l,int r) {
       for(int i=l+1;i<=r;i++){
              for(int j=i;j>0;j--){
                  if(greater(comparator,list,j-1,j)){
                      swap(list,j-1,j);
 
                  } else
                      break;
              }
        }
     }
 }
