 package models;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: reyoung
  * Date: 3/15/13
  * Time: 1:23 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Page<T> {
     public List<T> Data;
     public int     Index;
     public int     Length;
     public long    Count;
 //    public fj.data.List<T> FData;
     public Page(List<T> data, int index, int length, long count) {
         Data = data;
         Index = index;
         Length = length;
         Count = count;
 //        FData = fj.data.List.list(Data.toArray((T[]) new Object[Data.size()]));
     }
 
     public boolean hasPrev(){
         return Index>1;
     }
     public boolean hasNext(){
        return Index < Count/Length+1;
     }
     public int prev(){
         return Index -1;
     }
     public int next(){
         return Index+1;
     }
 }
