 package crono.type;
 
 public class TypeId {
     public final String image;
     public final Class type;
     public final TypeId parent;
     
     public TypeId(String image, Class type) {
         this(image, type, null);
     }
     public TypeId(String image, Class type, TypeId parent) {
         this.type = type;
         this.image = image;
         this.parent = parent;
     }
     
     public boolean isType(CronoType object) {
         TypeId id = object.typeId();
         while(id != null) {
             if(this.equals(id)) {
                 return true;
             }
             id = id.parent;
 
         }
         return false;
     }
    
     public boolean equals(Object o) {
        return ((o instanceof TypeId) &&
                image.equals(((TypeId)o).image));
     }
     
     public String toString() {
         return image;
     }
 }
