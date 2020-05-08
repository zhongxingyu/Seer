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
 
     @SuppressWarnings("unchecked")
     public <T> T get(int idx) {
         return (T) _list.get(idx);
     }
 
     @SuppressWarnings("unchecked")
     public <T> T get(PCObject idx) {
         return get((int)idx.<Double>getBase().doubleValue());
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
 
     public PCList subList(PCObject start, PCObject end) {
        return new PCList(_list.subList(start.<Double>getBase().intValue(), end.<Double>getBase().intValue()));
     }
 
     public Iterator<PCObject> iterator() {
         return _list.iterator();
     }
 }
