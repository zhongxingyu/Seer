 package xhl.examples.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Module {
     private final String name;
    private final List<Entity> entitiess = new ArrayList<Entity>();
 
     public Module(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public List<Entity> getEntities() {
        return entitiess;
     }
 
     public boolean add(Entity e) {
        return entitiess.add(e);
     }
 
     public Entity get(String name) {
        for (Entity entity : entitiess) {
             if (entity.getName().equals(name))
                 return entity;
         }
         return null;
     }
 }
