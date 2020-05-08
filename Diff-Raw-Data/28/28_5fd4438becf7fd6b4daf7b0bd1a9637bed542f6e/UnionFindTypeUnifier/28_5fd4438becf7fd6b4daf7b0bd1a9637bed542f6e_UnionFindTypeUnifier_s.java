 package de.skuzzle.polly.core.parser.ast.declarations.types.unification;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import de.skuzzle.polly.core.parser.ast.declarations.types.ListType;
 import de.skuzzle.polly.core.parser.ast.declarations.types.MapType;
 import de.skuzzle.polly.core.parser.ast.declarations.types.MissingType;
 import de.skuzzle.polly.core.parser.ast.declarations.types.ProductType;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Substitution;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Type;
 import de.skuzzle.polly.core.parser.ast.declarations.types.TypeVar;
 
 
 class UnionFindTypeUnifier implements Unifier {
     
     private final Map<Type, UnionFindItem<Type>> types;
     private final boolean subTypeIncl;
     
     public UnionFindTypeUnifier(boolean subTypeIncl) {
         this.types = new HashMap<>();
         this.subTypeIncl = subTypeIncl;
     }
     
     
     
     /**
      * Tests for structural equality of the given type expression in the context of this 
      * unifier instance.
      * 
      * @param first First type to check. 
      * @param second Second type to check.
      * @return A substitution for the type variables in first and second or 
      *          <code>null</code> if unification was not successful.
      */
     @Override
     public Substitution unify(Type first, Type second) {
         final Map<TypeVar, Type> subst = new HashMap<TypeVar, Type>();
         if (!unifyInternal(first, second, subst)) {
             return null;
         }
         return new Substitution(subst);
     }
     
     
     
     /**
      * Tests for structural equality of the two given type expressions.
      * 
      * @param first First type.
      * @param second Second type.
      * @return Whether the first type is an instance of the second type
      */
     @Override
     public boolean tryUnify(Type first, Type second) {
         return this.unifyInternal(first, second, new HashMap<TypeVar, Type>());
     }
     
     
     
     private UnionFindItem<Type> find(Type type) {
         UnionFindItem<Type> result = this.types.get(type);
         if (result == null) {
             result = new UnionFindItem<Type>(type);
            this.types.put(type, result);
         }
        return result.compress();
     }
     
 
     
     private final boolean unifyInternal(Type m, Type n, Map<TypeVar, Type> subst) {
         final UnionFindItem<Type> s = this.find(m);
         final UnionFindItem<Type> t = this.find(n);
         
         if (this.subTypeIncl && s.getValue().isA(t.getValue()) || 
                 s.getValue() == t.getValue()) {
             return true;
             
         } else if (s.getValue() instanceof MissingType || t.getValue() instanceof MissingType) {
             this.union(s, t, subst);
             return true;
             
         } else if (s.getValue() instanceof MapType && t.getValue() instanceof MapType) {
             this.union(s, t, subst);
             final MapType mc1 = (MapType) s.getValue();
             final MapType mc2 = (MapType) t.getValue();
             
             return this.unifyInternal(mc1.getSource(), mc2.getSource(), subst) &&
                 this.unifyInternal(mc1.getTarget(), mc2.getTarget(), subst);
             
         } else if (s.getValue() instanceof ListType && t.getValue() instanceof ListType) {
             this.union(s, t, subst);
             final ListType l1 = (ListType) s.getValue();
             final ListType l2 = (ListType) t.getValue();
             
             return this.unifyInternal(l1.getSubType(), l2.getSubType(), subst);
             
         } else if (s.getValue() instanceof ProductType && t.getValue() instanceof ProductType) {
             this.union(s, t, subst);
             final ProductType p1 = (ProductType) s.getValue();
             final ProductType p2 = (ProductType) t.getValue();
             
             if (p1.getTypes().size() != p2.getTypes().size()) {
                 return false;
             }
             final Iterator<Type> p2It = p2.getTypes().iterator();
             for (final Type p1Type : p1.getTypes()) {
                 if (!this.unifyInternal(p1Type, p2It.next(), subst)) {
                     return false;
                 }
             }
             return true;
             
         } else if (s.getValue() instanceof TypeVar || t.getValue() instanceof TypeVar) {
             this.union(s, t, subst);
             return true;
         } else {
             return false;
         }
     }
     
     
     
     private UnionFindItem<Type> union(UnionFindItem<Type> s, UnionFindItem<Type> t, 
             Map<TypeVar, Type> subst) {
         
         assert s.isRoot() && t.isRoot();
        final UnionFindItem<Type> rep = s.getValue() instanceof TypeVar ? s : t;
         final UnionFindItem<Type> other = rep == s ? t : s;
         
         other.mergeInto(rep);
         if (other.getValue() instanceof TypeVar) {
             subst.put((TypeVar) other.getValue(), rep.getValue());
         }
         return rep;
     }
 }
