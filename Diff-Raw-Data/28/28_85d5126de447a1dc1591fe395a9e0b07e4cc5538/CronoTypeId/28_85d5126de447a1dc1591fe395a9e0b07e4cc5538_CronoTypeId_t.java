 package crono.type;
 
 public class CronoTypeId extends Atom {
     public static final TypeId TYPEID = new TypeId(":type", CronoTypeId.class,
                                                    Atom.TYPEID);
     
     public final TypeId type;
     public CronoTypeId(TypeId type) {
         this.type = type;
     }
     
     public TypeId typeId() {
         return TYPEID;
     }
     
     public String toString() {
         return type.toString();
     }
     
     public boolean equals(Object o) {
         return ((o instanceof CronoTypeId) &&
                type.equals(((CronoTypeId)o).type));
    }
    
    public boolean complete() {
        return type.complete();
     }
 }
