 package ro.finsiel.eunis.jrfTables.species.references;
 
 
 /**
  * Date: Aug 26, 2003
  * Time: 9:44:21 AM
  */
 
 import java.util.Vector;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 
 public class SortListOfPersistentObject {
 
     /** Elements in the list are sorted in <i>reverse natural sorting order</i> */
     public static final boolean SORT_ASCENDING = false;
 
     /** Elements in the list that are sorted in <i>natural sorting order</i>*/
     public static final boolean SORT_DESCENDING = true;
 
     /** Creates a new instance of Compare */
     public SortListOfPersistentObject() {}
 
     /** Method used to sort a given Vector
      * @param data Vector of objects to be sorted. Remember that
      * their toString method must return a valid comparison
      * @param order Must be either <code>SortList.SORT_ASCENDIG</code>
      * or <code>SortList.SORT_DESCENDING</code>If other specified,
      * no sorting is done
      * @return The sorted Vector. Warning, this functions alters the
      * content of the input <code>data</code> parameter, so if you
      * want to preserve the original order, then do a copy before calling
      * this method.
      * @throws ClassCastException - if the list contains elements that are not
      * mutually comparable (i.e. compare a String with an Int)
      * @throws UnsupportedOperationException - if the specified list's list-iterator
      * does not support the set operation.
      */
     public List sort(List data, boolean order, String byWhat) throws ClassCastException, UnsupportedOperationException {
         CaseInsensitiveComparator comp = new CaseInsensitiveComparator(byWhat);
 
         if (SORT_DESCENDING == order) {
             comp.reverseOrder();
         }
         Collections.sort(data, comp);
         return data;
     }
 
     public static class CaseInsensitiveComparator implements Comparator {
         boolean ascending = true;
         String byWhat = "source";
 
         public CaseInsensitiveComparator(String byWhat) {
             this.byWhat = byWhat;
         }
 
         public void reverseOrder() {
             ascending = false;
         }
 
         public int compare(Object element1, Object element2) {
             try {
                 String lower1 = "";
                 String lower2 = "";
                 int res = -100;
 
                 if (byWhat.equalsIgnoreCase("source")) {
                     lower1 = ((SpeciesBooksPersist) element1).getName().toLowerCase();
                     lower2 = ((SpeciesBooksPersist) element2).getName().toLowerCase();
                     res = lower1.compareTo(lower2);
                 }
 
                 if (byWhat.equalsIgnoreCase("editor")) {
                     lower1 = ((SpeciesBooksPersist) element1).getEditor().toLowerCase();
                     lower2 = ((SpeciesBooksPersist) element2).getEditor().toLowerCase();
                     res = lower1.compareTo(lower2);
                 }
 
                 if (byWhat.equalsIgnoreCase("title")) {
                     lower1 = ((SpeciesBooksPersist) element1).getTitle().toLowerCase();
                     lower2 = ((SpeciesBooksPersist) element2).getTitle().toLowerCase();
                     res = lower1.compareTo(lower2);
                 }
 
                 if (byWhat.equalsIgnoreCase("publisher")) {
                     lower1 = ((SpeciesBooksPersist) element1).getPublisher().toLowerCase();
                     lower2 = ((SpeciesBooksPersist) element2).getPublisher().toLowerCase();
                     res = lower1.compareTo(lower2);
                 }
 
                 if (byWhat.equalsIgnoreCase("date")) {
                     Integer int1 = new Integer(
                            ((SpeciesBooksPersist) element1).getDate());
                     Integer int2 = new Integer(
                            ((SpeciesBooksPersist) element2).getDate());
 
                     res = int1.compareTo(int2);
                 }
 
                 res = (ascending) ? res : (-1) * res;
                 return res;
             } catch (ClassCastException ex) {
                 System.out.println(
                         "You can only sort/compare objects which implements JRFSortable");
                 ex.printStackTrace();
                 return 0;
             }
         }
     }
 }
