 package gov.nih.nci.security.authentication.principal;
 
 import java.security.Principal;
 
 public abstract class BasePrincipal implements Principal {
   private final String name;
 
   public BasePrincipal(String name) {
     if(name == null) {
       throw new IllegalArgumentException("Null name");
     }
     this.name = name;
   }
 
   public String getName() {
     return name;
   }
 
   public String toString() {
     return "CSMPrincipal: " + name;
   }
 
   public boolean equals(Object obj) {
     if(obj == null) return false;
     if(obj == this) return true;
    if(!(obj.getClass().isInstance(this))) return false;
     BasePrincipal another = (BasePrincipal) obj;
     return name.equals(another.getName());
   }
 
   public int hasCode() {
     return name.hashCode();
   }
 }
