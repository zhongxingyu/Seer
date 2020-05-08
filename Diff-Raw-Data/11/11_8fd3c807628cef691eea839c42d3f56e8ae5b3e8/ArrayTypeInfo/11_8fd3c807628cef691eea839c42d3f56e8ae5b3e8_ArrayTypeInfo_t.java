 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * z^\߂̃NXD
  * 
  * @author y-higo
  * 
  */
 public final class ArrayTypeInfo implements TypeInfo {
 
     /**
      * ^Ԃ
      */
     public String getTypeName() {
         TypeInfo elementType = this.getElementType();
         int dimension = this.getDimension();
 
         StringBuffer buffer = new StringBuffer();
         buffer.append(elementType.getTypeName());
         for (int i = 0; i < dimension; i++) {
             buffer.append("[]");
         }
         return buffer.toString();
     }
 
     /**
      * ǂ̃`FbNs
      */
     public boolean equals(final TypeInfo typeInfo) {
 
         if (null == typeInfo) {
             throw new NullPointerException();
         }
 
         if (!(typeInfo instanceof ArrayTypeInfo)) {
             return false;
         }
 
         TypeInfo elementTypeInfo = this.getElementType();
         TypeInfo correspondElementTypeInfo = ((ArrayTypeInfo) typeInfo).getElementType();
         if (!elementTypeInfo.equals(correspondElementTypeInfo)) {
             return false;
         } else {
 
             int dimension = this.getDimension();
             int correspondDimension = ((ArrayTypeInfo) typeInfo).getDimension();
            return dimension == correspondDimension;
         }
     }
 
     /**
      * z̗vf̌^Ԃ
      * 
      * @return z̗vf̌^
      */
     public TypeInfo getElementType() {
         return this.type;
     }
 
     /**
      * z̎Ԃ
      * 
      * @return z̎
      */
     public int getDimension() {
         return this.dimension;
     }
 
     /**
      * ArrayTypeInfo ̃CX^XԂ߂̃t@Ng\bhD
      * 
      * @param type ^\ϐ
      * @param dimension \ϐ
      * @return  ArrayTypeInfo IuWFNg
      */
     public static ArrayTypeInfo getType(final TypeInfo type, final int dimension) {
 
         if (null == type) {
             throw new NullPointerException();
         }
         if (dimension < 1) {
             throw new IllegalArgumentException("Array dimension must be 1 or more!");
         }
 
         Key key = new Key(type, dimension);
         ArrayTypeInfo arrayType = ARRAY_TYPE_MAP.get(key);
         if (arrayType == null) {
             arrayType = new ArrayTypeInfo(type, dimension);
             ARRAY_TYPE_MAP.put(key, arrayType);
         }
 
         return arrayType;
     }
 
     /**
      * IuWFNg̏sDz̗vf̌^Ɣz̎^Ȃ΂ȂȂ
      * 
      * @param type z̗vf̌^
      * @param dimension z̎
      */
     private ArrayTypeInfo(final TypeInfo type, final int dimension) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == type) {
             throw new NullPointerException();
         }
         if (1 < dimension) {
             throw new IllegalArgumentException("Array dimension must be 1 or more!");
         }
 
         this.type = type;
         this.dimension = dimension;
     }
 
     /**
      * z̗vf̌^ۑϐ
      */
     private final TypeInfo type;
 
     /**
      * z̎ۑϐ
      */
     private final int dimension;
 
     /**
      * ArrayTypeInfo IuWFNgꌳǗ邽߂ MapDIuWFNg̓t@Ng\bhŐD
      */
     private static final Map<Key, ArrayTypeInfo> ARRAY_TYPE_MAP = new HashMap<Key, ArrayTypeInfo>();
 
     /**
      * ϐ̌^ƎpăL[ƂȂNXD
      * 
      * @author y-higo
      */
     static class Key {
 
         /**
          * L[
          */
         private final TypeInfo type;
 
         /**
          * L[
          */
         private final int dimension;
 
         /**
          * CL[CL[IuWFNg𐶐
          * 
          * @param type L[
          * @param dimension L[
          */
         Key(final TypeInfo type, final int dimension) {
 
             if (null == type) {
                 throw new NullPointerException();
             }
             if (1 < dimension) {
                 throw new IllegalArgumentException("Array dimension must be 1 or more!");
             }
 
             this.type = type;
             this.dimension = dimension;
         }
 
         /**
          * ̃IuWFNg̃nbVR[hԂD
          */
         public int hashCode() {
             StringBuffer buffer = new StringBuffer();
             buffer.append(this.type.getTypeName());
             buffer.append(this.dimension);
             String hashString = buffer.toString();
             return hashString.hashCode();
         }
 
         /**
          * ̃L[IuWFNg̑L[ԂD
          * 
          * @return L[
          */
         public String getFirstKey() {
             return this.type.getTypeName();
         }
 
         /**
          * ̃L[IuWFNg̑L[ԂD
          * 
          * @return L[
          */
         public int getSecondKey() {
             return this.dimension;
         }
 
         /**
          * ̃IuWFNgƈŎw肳ꂽIuWFNgԂD
          */
         public boolean equals(Object o) {
 
             if (null == o) {
                 throw new NullPointerException();
             }
 
             String firstKey = this.getFirstKey();
             String correspondFirstKey = ((Key) o).getFirstKey();
            if (!firstKey.equals(correspondFirstKey)) {
                return false;
             } else {
                 int secondKey = this.getSecondKey();
                 int correspondSecondKey = ((Key) o).getSecondKey();
                 return secondKey == correspondSecondKey;
             }
         }
     }
 }
