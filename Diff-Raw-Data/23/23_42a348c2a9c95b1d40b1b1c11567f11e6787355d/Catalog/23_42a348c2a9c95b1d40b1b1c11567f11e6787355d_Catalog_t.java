 package de.hswt.hrm.catalog.model;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 /**
 * Represents a catalog.
  */
 public final class Catalog {
     private int id;
     private String name;
 
     private static final String IS_MANDATORY = "Field is a mandatory.";
 
     public Catalog(final String name) {
 
         this(-1, name);
     }
 
     public Catalog(int id, final String name) {
 
         this.id = id;
 
         setName(name);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         checkArgument(!isNullOrEmpty(name), IS_MANDATORY);
         this.name = name;
     }
 
 
     public int getId() {
         return id;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + id;
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Catalog other = (Catalog) obj;
         if (id != other.id)
             return false;
         if (name == null) {
             if (other.name != null)
                 return false;
         }
         else if (!name.equals(other.name))
             return false;
         return true;
     }
 
 }
