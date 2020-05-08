 package Player;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Rounder {
     private final List<Role> orderedRoles = new ArrayList<Role>();
     private int currentIndex = 0;
 
     public Role current() {
         return orderedRoles.get(currentIndex);
     }
 
     public void next() {
         do {
             ++currentIndex;
             currentIndex %= orderedRoles.size();
         } while (current().skip());
     }
 
     public void add(Role role) {
         orderedRoles.add(role);
     }
 
     public boolean isOnlyOneRoleAfterEliminate(String roleName) {
         for (Role role : orderedRoles) {
             if (role.name().equals(roleName)) return isOnlyOneRoleAfterDelete(role);
         }
         return false;
     }
 
     public boolean equals(Object object) {
         return getClass() == object.getClass() &&
                 orderedRoles.equals(((Rounder) object).orderedRoles) &&
                 currentIndex == ((Rounder) object).currentIndex;
     }
 
     private boolean isOnlyOneRoleAfterDelete(Role role) {
         role.leave();
         orderedRoles.remove(role);
        currentIndex %= orderedRoles.size();
         return 1 == orderedRoles.size();
     }
 }
