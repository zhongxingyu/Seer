 package org.jcouchdb.document;
 
 import org.jcouchdb.util.Util;
import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 
 /**
  * Encapsulates a view inside a {@link DesignDocument}.
  *
  * @author shelmberger
  *
  */
public class View
 {
     private static Logger log = LoggerFactory.getLogger(View.class);
 
     private String map,reduce;
 
     public View()
     {
 
     }
 
     public View(String mapFn)
     {
         setMap(mapFn);
     }
 
     public View(String mapFn, String reduceFn)
     {
         setMap(mapFn);
         setReduce(reduceFn);
     }
 
     public String getMap()
     {
         return map;
     }
 
     public void setMap(String map)
     {
         if (map != null && map.length() > 0)
         {
             this.map = map;
         }
         else
         {
             this.map = null;
         }
     }
 
     public String getReduce()
     {
         return reduce;
     }
 
     public void setReduce(String reduce)
     {
         if (reduce != null && reduce.length() > 0)
         {
             this.reduce = reduce;
         }
         else
         {
             this.reduce = null;
         }
     }
 
     @Override
     public boolean equals(Object obj)
     {
         boolean result = false;
         if (obj instanceof View)
         {
             View that = (View) obj;
             result = Util.equals(this.getMap(), that.getMap()) &&
                 Util.equals(this.getReduce(), that.getReduce());
         }
         return result;
     }
 
     @Override
     public int hashCode()
     {
         return 17 + 37 * Util.safeHashcode(this.getMap()) + 37 * Util.safeHashcode(this.getReduce());
     }
 
     @Override
     public String toString()
     {
         return super.toString()+": map = '"+map+", reduce='"+reduce+"'";
     }
 }
