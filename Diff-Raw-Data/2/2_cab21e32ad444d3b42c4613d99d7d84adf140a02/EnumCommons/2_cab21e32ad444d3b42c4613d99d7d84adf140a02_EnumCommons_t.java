 package com.redshape.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 import com.redshape.utils.Commons;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.utils
  * @date 2/13/12 {1:11 PM}
  */
 public final class EnumCommons {
 
     public static Map<String, String> map( Class<? extends IEnum> enumClazz ) {
         try {
             Map<String, String> result = new HashMap<String, String>();
             IEnum[] enumValues = (IEnum[]) enumClazz.getMethod("values").invoke(null);
             for ( IEnum enumValue : enumValues ) {
                result.put( enumValue.toString(), enumValue.name() );
             }
 
             return result;
         } catch ( Throwable e ) {
             return null;
         }
     }
 
 
 }
