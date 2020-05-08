 package models;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import play.i18n.Messages;
 import util.IdGenerator;
 
 /**
  * Role.
  * 
  * @author GuoLin
  *
  */
 public class Role {
 
    public static final Role RANDOM = new EmptyRole(); 
 
     private static final Map<Integer, Role> STORE = new HashMap<Integer, Role>();
 
     public Integer id;
 
     public String name;
 
     public String face;
 
     public Role(String name, String face) {
         this.id = IdGenerator.generate();
         this.name = name;
         this.face = face;
     }
 
     public void save() {
         STORE.put(id, this);
     }
 
     public static Role get(Integer id) {
         return STORE.get(id);
     }
 
     public static Collection<Role> all() {
         return STORE.values();
     }
 
    public static class EmptyRole extends Role {
 
        public EmptyRole() {
            super(Messages.get("room.role.random"), "role_random.png");
         }
 
     }
 
 }
