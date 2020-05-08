 package utils;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * User: guym
  * Date: 12/20/12
  * Time: 12:52 PM
  */
 public class CollectionUtils extends org.apache.commons.collections.CollectionUtils{
 
     // another flavor of "add all" array elements to collection.
     // this has the benefit of returning the list. so code is shorter.
     public static <T extends Collection> T addTo( Collection l, Object ... os ){
         Collections.addAll( l, os );
         return (T) l;
     }
 
     public static boolean isEmpty( Collection c ){
         return c == null || size( c ) == 0;
     }
 
     public static boolean isEmpty( Object[] values )
     {
         return values == null || size ( values ) == 0 ;
     }
 
     public static int size( Object[] vals ){
         return vals == null ? 0 : vals.length;
     }
 
     public static int size( Collection c ){
         return c == null ? 0 : c.size();
     }
 
     public static <T> T last(List list) {
        if (size(list) > 0) {
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            return (T) list.get(size(list) - 1);
         }
        return null;
     }
 
     public static <T> T first(Collection collection) {
        if ( size(collection) > 0 ){
            return (T) collection.iterator().next();
        }
        return null;
     }
 }
