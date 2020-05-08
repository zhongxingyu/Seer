 package anaydis.sort;
 
 import anaydis.sort.gui.SorterListener;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * Author: Tomás Vilaboa
  * Project: AnayDis-2013
  * Date: 03/09/13
  * Time: 22:42
  */
 public class ShellSorter extends AbstractSorter {
 
     private HSorter hSorter=new HSorter();
 
     public ShellSorter(){
         super(SorterType.SHELL);
 
     }
 
     @Override
     public <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list) {
         List<Integer> sequence=sedgewickSequence(list);
         for(int i=sequence.size();i>0;i--){
             hSorter.sort(comparator,list, sequence.get(i-1));
         }
 
     }
 
     public <T> void sort(@NotNull Comparator<T> comparator, @NotNull List<T> list,List<Integer> sequence) {
         for(int i=sequence.size();i>0;i--){
             hSorter.sort(comparator,list, sequence.get(i-1));
         }
 
     }
 
     private <T> List<Integer> sedgewickSequence(List<T> list) {
         List<Integer> sequence=new ArrayList<>();
         int h=1;
         sequence.add(1);
        for(int i=0;h<list.size();i++){
             h=(int)(Math.pow(4,i+1) + 3*Math.pow(2,i) +1);         //uses sedgewick sequence
             sequence.add(h);
         }
        sequence.remove(sequence.size()-1);
         return sequence;
     }
 
     @Override
     public void addSorterListener(SorterListener listener) {
         hSorter.addSorterListener(listener);
     }
 
     @Override
     public void removeSorterListener(SorterListener listener) {
         hSorter.removeSorterListener(listener);
     }
 }
