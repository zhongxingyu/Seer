 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ^p[^\ۃNX
  * 
  * @author higo
  * 
  */
 public class TypeParameterInfo implements ReferenceTypeInfo {
 
     /**
      * ^p[^^ăIuWFNg
      * 
      * @param ownerUnit ^p[^錾Ă郆jbg(NX or \bh)
      * @param name ^p[^
      * @param index Ԗڂ̌^p[^\
      * @param extendsType pĂ^
      */
     public TypeParameterInfo(final UnitInfo ownerUnit, final String name, final int index,
             final TypeInfo extendsType) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == ownerUnit) || (null == name)) {
             throw new NullPointerException();
         }
 
         if ((!(ownerUnit instanceof TargetClassInfo)) && (!(ownerUnit instanceof CallableUnitInfo))) {
             throw new NullPointerException();
         }
 
         this.ownerUnit = ownerUnit;
         this.name = name;
         this.index = index;
         this.extendsType = extendsType;
     }
 
     /**
      * ̌^p[^ŗ^ꂽ^Ɠǂ𔻒肷
      * 
      * @param o rΏی^
      * @return ꍇ trueCȂꍇ false;
      */
     public boolean equals(final TypeInfo o) {
 
         if (null == o) {
             return false;
         }
 
         if (!(o instanceof TypeParameterInfo)) {
             return false;
         }
 
         return this.getName().equals(((TypeParameterInfo) o).getName());
     }
 
     /**
      * ^p[^錾Ă郆jbg(NX or \bh)Ԃ
      * 
      * @return  ^p[^錾Ă郆jbg(NX or \bh)
      */
     public final UnitInfo getOwnerUnit() {
         return this.ownerUnit;
     }
 
     /**
      * ^p[^Ԃ
      * 
      * @return ^p[^
      */
     public final String getName() {
         return this.name;
     }
 
     /**
      * ^iۂɂ͌^p[^jԂ
      * 
      * @return ^
      */
     public final String getTypeName() {

        StringBuilder sb = new StringBuilder(this.name);
        if (this.hasExtendsType()) {
            sb.append(" extends ");
            sb.append(this.getExtendsType().getTypeName());
        }

        return sb.toString();
     }
 
     /**
      * ^p[^̃CfbNXԂ
      * 
      * @return@^p[^̃CfbNX
      */
     public final int getIndex() {
         return this.index;
     }
 
     /**
      * NX^Ԃ
      * 
      * @return NX^
      */
     public final TypeInfo getExtendsType() {
         return this.extendsType;
     }
 
     /**
      * * NXǂԂ
      * 
      * @return NXꍇ true,Ȃꍇ false
      */
     public final boolean hasExtendsType() {
         return null != this.extendsType;
     }
 
     /**
      * ^p[^LĂ郆jbgۑ邽߂̕ϐ
      */
     private final UnitInfo ownerUnit;
 
     /**
      * ^p[^ۑ邽߂̕ϐ
      */
     private final String name;
 
     /**
      * ̌^p[^Ԗڂ̂̂\ϐ
      */
     private final int index;
 
     /**
      * NX^ۑ邽߂̕ϐ
      */
     private final TypeInfo extendsType;
 }
