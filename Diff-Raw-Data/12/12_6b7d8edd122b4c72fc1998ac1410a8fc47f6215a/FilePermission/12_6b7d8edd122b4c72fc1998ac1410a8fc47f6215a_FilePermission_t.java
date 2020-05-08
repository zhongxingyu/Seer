 package java.io;
 
import java.security.Permission;
import java.security.PermissionCollection;

 public final class FilePermission extends Permission implements Serializable {
     public FilePermission(String arg0, String arg1) {}
 
     public boolean equals(Object arg0) {
         return false;
     }
 
     public String getActions() {
         return null;
     }
 
     public int hashCode() {
         return 0;
     }
 
     public boolean implies(Permission arg0) {
         return false;
     }
 
     public PermissionCollection newPermissionCollection() {
         return null;
     }
 
 }
