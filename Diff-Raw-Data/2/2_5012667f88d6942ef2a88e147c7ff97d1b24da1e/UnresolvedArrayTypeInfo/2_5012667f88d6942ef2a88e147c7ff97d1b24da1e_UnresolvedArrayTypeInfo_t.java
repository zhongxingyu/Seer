 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * z^\߂̃NXDȉ̏D
  * <ul>
  * <li>^ (UnresolvedTypeInfo)</li>
  * <li> (int)</li>
  * </ul>
  * 
  * @author y-higo
  * @see UnresolvedTypeInfo
  */
 public final class UnresolvedArrayTypeInfo implements UnresolvedTypeInfo {
 
     /**
      * ^Ԃ
      */
     public String getTypeName() {
         final UnresolvedTypeInfo elementType = this.getElementType();
         final int dimension = this.getDimension();
 
         final StringBuffer buffer = new StringBuffer();
         buffer.append(elementType.getTypeName());
         for (int i = 0; i < dimension; i++) {
             buffer.append("[]");
         }
         return buffer.toString();
     }
 
     /**
      * ǂ̃`FbNs
      */
     public boolean equals(final UnresolvedTypeInfo typeInfo) {
 
         if (null == typeInfo) {
             throw new NullPointerException();
         }
 
         if (!(typeInfo instanceof UnresolvedArrayTypeInfo)) {
             return false;
         }
 
         final UnresolvedTypeInfo elementTypeInfo = this.getElementType();
         final UnresolvedTypeInfo correspondElementTypeInfo = ((UnresolvedArrayTypeInfo) typeInfo)
                 .getElementType();
         if (!elementTypeInfo.equals(correspondElementTypeInfo)) {
             return false;
         } else {
 
             final int dimension = this.getDimension();
             final int correspondDimension = ((UnresolvedArrayTypeInfo) typeInfo).getDimension();
             return dimension == correspondDimension;
         }
     }
 
     /**
      * z̗vf̖^Ԃ
      * 
      * @return z̗vf̖^
      */
     public UnresolvedTypeInfo getElementType() {
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
      * UnresolvedArrayTypeInfo ̃CX^XԂ߂̃t@Ng\bhD
      * 
      * @param type ^\ϐ
      * @param dimension \ϐ
      * @return  UnresolvedArrayTypeInfo IuWFNg
      */
     public static UnresolvedArrayTypeInfo getType(final UnresolvedTypeInfo type, final int dimension) {
 
         if (null == type) {
             throw new NullPointerException();
         }
         if (dimension < 1) {
             throw new IllegalArgumentException("Array dimension must be 1 or more!");
         }
 
         final Key key = new Key(type, dimension);
         UnresolvedArrayTypeInfo arrayType = ARRAY_TYPE_MAP.get(key);
         if (arrayType == null) {
             arrayType = new UnresolvedArrayTypeInfo(type, dimension);
             ARRAY_TYPE_MAP.put(key, arrayType);
         }
 
         return arrayType;
     }
 
     /**
      * z^IuWFNg̏sDz̗vf̖^Ɣz̎^Ȃ΂ȂȂ
      * 
      * @param type z̗vf̖^
      * @param dimension z̎
      */
     private UnresolvedArrayTypeInfo(final UnresolvedTypeInfo type, final int dimension) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == type) {
             throw new NullPointerException();
         }
        if (1 > dimension) {
             throw new IllegalArgumentException("Array dimension must be 1 or more!");
         }
 
         this.type = type;
         this.dimension = dimension;
     }
 
     /**
      * z̗vf̌^ۑϐ
      */
     private final UnresolvedTypeInfo type;
 
     /**
      * z̎ۑϐ
      */
     private final int dimension;
 
     /**
      * UnresolvedArrayTypeInfo IuWFNgꌳǗ邽߂ MapDIuWFNg̓t@Ng\bhŐD
      */
     private static final Map<Key, UnresolvedArrayTypeInfo> ARRAY_TYPE_MAP = new HashMap<Key, UnresolvedArrayTypeInfo>();
 
     /**
      * ϐ̌^ƎpăL[ƂȂNXD
      * 
      * @author y-higo
      */
     static class Key {
 
         /**
          * L[
          */
         private final UnresolvedTypeInfo type;
 
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
         Key(final UnresolvedTypeInfo type, final int dimension) {
 
             if (null == type) {
                 throw new NullPointerException();
             }
             if (1 > dimension) {
                 throw new IllegalArgumentException("Array dimension must be 1 or more!");
             }
 
             this.type = type;
             this.dimension = dimension;
         }
 
         /**
          * ̃IuWFNg̃nbVR[hԂD
          */
         public int hashCode() {
             final StringBuffer buffer = new StringBuffer();
             buffer.append(this.type.getTypeName());
             buffer.append(this.dimension);
             final String hashString = buffer.toString();
             return hashString.hashCode();
         }
 
         /**
          * ̃L[IuWFNg̑L[ԂD
          * 
          * @return L[
          */
         public UnresolvedTypeInfo getFirstKey() {
             return this.type;
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
 
             final UnresolvedTypeInfo firstKey = this.getFirstKey();
             final UnresolvedTypeInfo correspondFirstKey = ((Key) o).getFirstKey();
             if (!firstKey.equals(correspondFirstKey)) {
                 return false;
             } else {
                 final int secondKey = this.getSecondKey();
                 final int correspondSecondKey = ((Key) o).getSecondKey();
                 return secondKey == correspondSecondKey;
             }
         }
     }
 }
