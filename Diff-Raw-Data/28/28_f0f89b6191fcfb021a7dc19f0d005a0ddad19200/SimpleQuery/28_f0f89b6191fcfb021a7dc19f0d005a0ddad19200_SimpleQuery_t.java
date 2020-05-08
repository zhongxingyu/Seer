 package nl.astraeus.persistence;
 
 import nl.astraeus.persistence.reflect.ReflectHelper;
 
 import javax.annotation.CheckForNull;
 import java.util.*;
 
 /**
  * User: rnentjes
  * Date: 6/26/12
  * Time: 8:05 PM
  */
 public class SimpleQuery<M extends SimpleModel> {
 
     private static enum SelectorType {
         EQUALS,
         NULL,
        NOT_NULL,
         GREATER,
         SMALLER,
         GREATER_EQUAL,
         SMALLER_EQUAL;
     }
 
     private static class Selector {
         public SelectorType type;
         public Object value;
 
         private Selector(SelectorType type, Object value) {
             this.type = type;
             this.value = value;
         }
     }
 
     private SimpleDao<M> dao;
     private Map<String, Set<Selector>> selections = new HashMap<String, Set<Selector>>();
     private List<String> order = new LinkedList<String>();
     private int from, max;
 
     protected static class OrderComparator implements Comparator<SimpleModel> {
 
         private String [] order;
 
         public OrderComparator(List<String> order) {
             this.order = order.toArray(new String[order.size()]);
         }
 
         @Override
         public int compare(SimpleModel o1, SimpleModel o2) {
             int index = 0;
             int result = 0;
 
             boolean desc;
             String field;
 
             while(index < order.length && result == 0) {
                 field = order[index];
                 desc = false;
 
                 if (field.charAt(0) == '-') {
                     desc = true;
                     field = field.substring(1);
                 } else if (field.charAt(0) == '+') {
                     field = field.substring(1);
                 }
 
                 Object v1 = ReflectHelper.get().getField(o1, field);
                 Object v2 = ReflectHelper.get().getField(o2, field);
 
                 if (v1 instanceof Comparable) {
                     result = ((Comparable)v1).compareTo(v2);
 
                     if (desc) {
                         result = -result;
                     }
                 }
 
                 index++;
             }
 
             if (o1.getId() > o2.getId()) {
                 result = -1;
             } else {
                 result = 1;
             }
 
             return result;
         }
     }
 
     public SimpleQuery(SimpleDao<M> dao) {
         this.dao = dao;
         this.from = 0;
         this.max = dao.size();
     }
 
     public SimpleQuery<M> from(int from) {
         this.from = from;
 
         return this;
     }
 
     public SimpleQuery<M> max(int max) {
         this.max = max;
 
         return this;
     }
 
     private void addSelection(String property, SelectorType type, Object value) {
        Set<Selector> values = selections.get(property);
 
        if (values == null) {
            values = new HashSet<Selector>();
 
            selections.put(property, values);
         }

        values.add(new Selector(type, value));
     }
 
     public SimpleQuery<M> where(String property, Object value) {
         addSelection(property, SelectorType.EQUALS, value);
 
         return this;
     }
 
     public SimpleQuery<M> equals(String property, Object value) {
         addSelection(property, SelectorType.EQUALS, value);
 
         return this;
     }
 
     public SimpleQuery<M> isNull(String property) {
         addSelection(property, SelectorType.NULL, null);
 
         return this;
     }
 
     public SimpleQuery<M> isNotNull(String property) {
        addSelection(property, SelectorType.NOT_NULL, null);
 
         return this;
     }
 
     public SimpleQuery<M> greater(String property, Object value) {
         addSelection(property, SelectorType.GREATER, null);
 
         return this;
     }
 
     public SimpleQuery<M> smaller(String property, Object value) {
         addSelection(property, SelectorType.SMALLER, null);
 
         return this;
     }
 
     public SimpleQuery<M> greaterEquals(String property, Object value) {
         addSelection(property, SelectorType.GREATER_EQUAL, null);
 
         return this;
     }
 
     public SimpleQuery<M> smallerEqual(String property, Object value) {
         addSelection(property, SelectorType.SMALLER_EQUAL, null);
 
         return this;
     }
 
     public SimpleQuery<M> order(String ... property) {
         order.addAll(Arrays.asList(property));
 
         return this;
     }
 
     public SortedSet<M> getResultSet() {
         // result as set with order comparator
         int fromCounter = 0;
         int nrResults = 0;
 
         SortedSet<M> result = new TreeSet<M>(new OrderComparator(order));
 
         // get searchset from indexes
 
         for (M m : dao.findAll()) {
             boolean match = isMatch(m);
 
             if (match) {
                 if (fromCounter >= from) {
                     result.add(m);
                     nrResults++;
                 }
                 fromCounter++;
             }
 
             if (nrResults == max) {
                 break;
             }
         }
 
         return result;
     }
 
     @CheckForNull
     public M getSingleResult() {
         M result = null;
         Set<M> resultList = getResultSet();
 
         if (resultList.size() == 1) {
             result = resultList.iterator().next();
         } else {
             // warn or error???
         }
 
         return result;
     }
 
     public boolean isMatch(M m) {
         boolean result = true;
         Set<String> properties = selections.keySet();
 
         for (String property : properties) {
             Object om = ReflectHelper.get().getFieldValue(m, property);
 
             for (Selector selector : selections.get(property)) {
                 switch(selector.type) {
                     case EQUALS:
                         if (om == null || !om.equals(selector.value)) {
                             result = false;
                         }
                         break;
                     case NULL:
                         if (om != null) {
                             result = false;
                         }
                         break;
                    case NOT_NULL:
                         if (om == null) {
                             result = false;
                         }
                         break;
                     default:
                         throw new IllegalStateException(selector.type+" not implemented yet!");
                 }
             }
         }
 
         return result;
     }
 }
