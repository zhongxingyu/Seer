 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 /**
  * OłȂ^\NXD
  * 
  * @author y-higo
  * 
  */
 public class UnknownTypeInfo implements TypeInfo {
 
     /**
      * ̃NX̒PIuWFNgԂ
      * @return ̃NX̒PIuWFNg
      */
     public static UnknownTypeInfo getInstance() {
         return SINGLETON;
     }
 
     /**
      * OłȂ^̖OԂD
      */
     public String getTypeName() {
         return UNKNOWN_STRING;
     }
 
     /**
      * ǂ̃`FbNs
      */
    public boolean equals(final TypeInfo typeInfo){
        
        if (null == typeInfo){
             throw new NullPointerException();
         }
        
        if (typeInfo instanceof UnknownTypeInfo){
            return true;
        }else{
            return false;
        }
     }
    
     /**
      * void ^̌^\萔
      */
     public static final String UNKNOWN_STRING = new String("unknown");
 
     /**
      * ̃NX̒PIuWFNgۑ邽߂̒萔
      */
     private static final UnknownTypeInfo SINGLETON = new UnknownTypeInfo();
 }
