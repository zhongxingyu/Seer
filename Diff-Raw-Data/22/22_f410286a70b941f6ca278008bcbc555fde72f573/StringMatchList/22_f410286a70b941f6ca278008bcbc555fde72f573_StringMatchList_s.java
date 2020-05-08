 package project.phase2.structs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class StringMatchList extends ArrayList<StringMatchTuple> {
 
     public StringMatchList() {
         // Default
     }
 
     public StringMatchList(StringMatchList a) {
         add(a);
     }
 
     public StringMatchList(String... a) {
         add(a);
     }
 
     /**
      * Union of this set and another.
      *
      * @param b
      * @return
      */
     public StringMatchList union(final StringMatchList b) {
 
         StringMatchList l = new StringMatchList();
 
         l.add(this);
         l.add(b);
 
         return l;
     }
 
     public StringMatchList intersection(final StringMatchList b) {
         StringMatchList n = new StringMatchList();
 
         for (StringMatchTuple string : this) {
             if (b.contains(string)) {
                 n.add(string);
             }
         }
 
         for (StringMatchTuple string : b) {
             if (n.contains(string)) {
                 n.add(string);
             }
         }
 
         return n;
     }
 
     /**
      * This minus second.
      *
      * @param second other list.
      * @return the difference of the two lists.
      */
     public StringMatchList difference(final StringMatchList second) {
         StringMatchList n = new StringMatchList();
 
         for (StringMatchTuple string : this) {
             if (!second.contains(string)) {
                 n.add(string);
             }
         }
 
         return n;
     }
 
     public void add(String... s) {
         for (String r : s)
             super.add(new StringMatchTuple(r));
     }
 
     public void add(StringMatchList s) {
         for (StringMatchTuple r : s)
             super.add(new StringMatchTuple(r));
     }
 
     /**
      * Get the most frequent regex.
      * Throws exception in the case of empty. If there is a tie,
      * return is arbitrary among tied regex.
      */
     public String getMostFrequentString() {
 
         if (isEmpty()) {
             throw new RuntimeException("Attempted to find the most frequent string of an empty list.");
         }
 
         HashMap<String, Integer> m = new HashMap<String, Integer>();
 
        String best = null;

         for (StringMatchTuple t : this) {
 
            if (!m.containsKey(t.string)) {
                m.put(t.string, 0);
                best = t.string;
             }
 
            m.put(t.string, m.get(t.string) + 1);
 
            if (best == null || m.get(best) < m.get(t.string)) {
                best = t.string;
             }
         }
 
         return best;
     }
 
     @Override
     public boolean equals(Object o) {
 
         if (this == o)
             return true;
 
         if (!(o instanceof StringMatchList)) {
             return false;
         }
 
         StringMatchList a = (StringMatchList) o;
 
         for (StringMatchTuple t : this) {
             if (!a.contains(t))
                 return false;
         }
         for (StringMatchTuple t : a) {
             if (!this.contains(t))
                 return false;
         }
 
         return true;
     }
 
     @Override
     public String toString() {
         StringBuffer be = new StringBuffer();
         be.append("[");
         for (StringMatchTuple s : this) {
             be.append(", ").append(s.toString());
         }
         be.append("]");
         return be.toString().replaceFirst(", ", "");
     }
 }
