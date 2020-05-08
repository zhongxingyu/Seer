 package crono.type;
 
 import crono.Visitor;
 
 public abstract class CronoType {
     public static final TypeId TYPEID = new TypeId(":any", CronoType.class);
     
     public abstract TypeId typeId();
     public abstract String toString();
     public String repr() {
         return toString();
     }
     
     public abstract CronoType accept(Visitor v);
 }
