 package pubCrawl.core;
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 
 public class PCList extends PCObject implements Iterable<PCObject>
 {
     private final ArrayList<PCObject> _list = new ArrayList<PCObject>();
 
     public PCList() {
 
     }
 
     public PCList(List<PCObject> items) {
         for (PCObject item : items) {
             _list.add(item);
         }
     }
 
     public PCList(String mystring) {
         char[] myarray = mystring.toCharArray();
         for(int i = 0; i < myarray.length; i++){
             _list.add(new PCObject(myarray[i]));
         }
     }
 
     public PCList(PCList a, PCList b)
     {
         for (PCObject o : a) {
             _list.add(o);
         }
         for (PCObject o : b) {
             _list.add(o);
         }
     }
 
     public int size() {
         return _list.size();
     }
 
     public PCObject get(int idx) {
         return _list.get(idx);
     }
 
     public PCObject get(PCObject idx){
         return _list.get((int)idx.<Double>getBase().doubleValue());
     }
 
     public void set(int idx, PCObject val) {
         _list.set(idx, val);
     }
 
     public PCList add(PCObject val) {
         _list.add(val);
         return this;
     }
 
     //  TODO update docs for removeAt
     public void removeAt(int idx) {
         _list.remove(idx);
     }
 
    public PCList subList(int start, int end) {
        return new PCList(_list.subList(start, end));
     }
 
     public Iterator<PCObject> iterator() {
         return _list.iterator();
     }
 }
