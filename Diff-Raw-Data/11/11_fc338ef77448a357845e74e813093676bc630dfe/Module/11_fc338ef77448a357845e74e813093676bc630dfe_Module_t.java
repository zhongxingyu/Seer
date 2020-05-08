 package xhl.examples.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Module {
     private final String name;
    private final List<Entity> entities = new ArrayList<Entity>();
 
     public Module(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public List<Entity> getEntities() {
        return entities;
     }
 
     public boolean add(Entity e) {
        return entities.add(e);
     }
 
     public Entity get(String name) {
        for (Entity entity : entities) {
             if (entity.getName().equals(name))
                 return entity;
         }
         return null;
     }
 }
