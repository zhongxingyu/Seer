 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * Qƌ^\NX
  * 
  * @author higo
  * 
  */
 public final class ClassTypeInfo implements ReferenceTypeInfo {
 
     /**
      * Qƌ^ListNXListɕϊ
      * 
      * @param references Qƌ^List
      * @return NXList
      */
     public static List<ClassInfo> convert(final List<ClassTypeInfo> references) {
 
         final List<ClassInfo> classInfos = new LinkedList<ClassInfo>();
         for (final ClassTypeInfo reference : references) {
             classInfos.add(reference.getReferencedClass());
         }
 
         return Collections.unmodifiableList(classInfos);
     }
 
     /**
      * Qƌ^SortedSetNXSortedSetɕϊ
      * 
      * @param references Qƌ^SortedSet
      * @return NXSortedSet
      */
     public static SortedSet<ClassInfo> convert(final SortedSet<ClassTypeInfo> references) {
 
         final SortedSet<ClassInfo> classInfos = new TreeSet<ClassInfo>();
         for (final ClassTypeInfo reference : references) {
             classInfos.add(reference.getReferencedClass());
         }
 
         return Collections.unmodifiableSortedSet(classInfos);
     }
 
     /**
      * QƂNX^ď
      * 
      * @param referencedClass QƂNX
      */
     public ClassTypeInfo(final ClassInfo referencedClass) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == referencedClass) {
             throw new NullPointerException();
         }
 
         this.referencedClass = referencedClass;
         this.typeArguments = new ArrayList<TypeInfo>();
     }
 
     /**
      * ŗ^ꂽ^𓙂ǂrD
      * 
      * @return ꍇtrueCȂꍇfalse
      */
     public boolean equals(TypeInfo typeInfo) {
 
         //  null Ȃ΁CȂ
         if (null == typeInfo) {
             return false;
         }
 
         // Qƌ^łȂ΁CȂ
         if (!(typeInfo instanceof ClassTypeInfo)) {
             return false;
         }
 
         // Qƌ^̏ꍇC
         // QƂĂNXȂꍇ́CQƌ^͓Ȃ
         final ClassTypeInfo targetReferenceType = (ClassTypeInfo) typeInfo;
         if (!this.referencedClass.equals(targetReferenceType)) {
             return false;
         }
 
         // ^p[^̐قȂꍇ́CȂ
         final List<TypeInfo> thisTypeParameters = this.typeArguments;
         final List<TypeInfo> targetTypeParameters = targetReferenceType.getTypeArguments();
         if (thisTypeParameters.size() != targetTypeParameters.size()) {
             return false;
         }
 
         // SĂ̌^p[^Ȃ΁CȂ
         final Iterator<TypeInfo> thisTypeParameterIterator = thisTypeParameters.iterator();
         final Iterator<TypeInfo> targetTypeParameterIterator = targetTypeParameters.iterator();
         while (thisTypeParameterIterator.hasNext()) {
             final TypeInfo thisTypeParameter = thisTypeParameterIterator.next();
             final TypeInfo targetTypeParameter = targetTypeParameterIterator.next();
             if (!thisTypeParameter.equals(targetTypeParameter)) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * ̎Qƌ^\Ԃ
      * 
      * @return ̎Qƌ^\
      */
     public String getTypeName() {
 
         final StringBuilder sb = new StringBuilder();
         sb.append(this.referencedClass.getFullQualifiedName("."));
 
        if (0 < this.typeArguments.size()) {
             sb.append("<");
             for (final TypeInfo typeParameter : this.typeArguments) {
                 sb.append(typeParameter.getTypeName());
             }
             sb.append(">");
         }
 
         return sb.toString();
     }
 
     /**
      * QƂĂNXԂ
      * 
      * @return QƂĂNX
      */
     public ClassInfo getReferencedClass() {
         return this.referencedClass;
     }
 
     /**
      * ̎Qƌ^ɗpĂ^̃XgԂ
      * 
      * @return ̎Qƌ^ɗpĂ^̃XgԂ
      */
     public List<TypeInfo> getTypeArguments() {
         return Collections.unmodifiableList(this.typeArguments);
     }
 
     /**
      * ̎Qƌ^̃CfbNXŎw肳ꂽ^Ԃ
      * 
      * @param index ^̃CfbNX
      * @return@̎Qƌ^̃CfbNXŎw肳ꂽ^
      */
     public TypeInfo getTypeArgument(final int index) {
         return this.typeArguments.get(index);
     }
 
     /**
      * ̎Qƌ^Ɍ^ǉ
      * 
      * @param argument ǉ^
      */
     public void addTypeArgument(final TypeInfo argument) {
         this.typeArguments.add(argument);
     }
 
     /**
      * ̎Qƌ^\NXۑ邽߂̕ϐ
      */
     private final ClassInfo referencedClass;
 
     /**
      * ̎Qƌ^̌^p[^ۑ邽߂̕ϐ
      */
     private final List<TypeInfo> typeArguments;
 
 }
