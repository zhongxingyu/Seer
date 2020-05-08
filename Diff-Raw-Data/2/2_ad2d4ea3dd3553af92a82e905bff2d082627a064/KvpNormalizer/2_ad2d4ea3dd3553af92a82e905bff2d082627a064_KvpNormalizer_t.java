 package org.deegree.securityproxy.request;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * Key-Value-Pair normalizer
  * 
  * @author <a href="mailto:erben@lat-lon.de">Alexander Erben</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
public final class KvpNormalizer {
 
     private KvpNormalizer() {
     }
     
     /**
      * @param parameterMap
      *            a map of parameters, never <code>null</code>
      * @return the incoming map with keys in lower cases, duplicated keys are merged into one key with multiple values;
      *         never <code>null</code>
      */
     public static Map<String, String[]> normalizeKvpMap( Map<String, String[]> parameterMap ) {
         Map<String, String[]> normalizedMap = new HashMap<String, String[]>();
         for ( Entry<String, String[]> entry : parameterMap.entrySet() ) {
             String key = entry.getKey().toLowerCase();
             String[] value = entry.getValue();
             if ( !normalizedMap.containsKey( key ) ) {
                 normalizedMap.put( key, value );
             } else {
                 String[] newValue = (String[]) ArrayUtils.addAll( value, normalizedMap.get( key ) );
                 normalizedMap.put( key, newValue );
             }
         }
         return normalizedMap;
     }
 
 }
