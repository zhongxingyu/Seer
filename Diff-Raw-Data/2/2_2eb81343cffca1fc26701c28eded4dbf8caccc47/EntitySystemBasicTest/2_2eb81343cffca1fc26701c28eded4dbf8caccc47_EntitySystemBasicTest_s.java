 package j3chess.test;
 
 import static org.junit.Assert.assertEquals;
 import j3chess.EntitySystem;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import artemis.Component;
 import artemis.Entity;
 import artemis.World;
 import artemis.utils.Bag;
 
 public class EntitySystemBasicTest {
 
    public class SimpleComponent extends Component {
 
         SimpleComponent() {
         }
 
         @Override
         public String toString() {
             return "SimpleComponent []";
         }
     }
 
     static EntitySystem mEntitySystem = new EntitySystem();
     static World mWorld = mEntitySystem.getWorld();
 
     @BeforeClass
     public static void setup() {
     }
 
     @Test
     public void createEmptyEntity() {
         final Entity emptyEntity = mWorld.createEntity();
         final Bag<Component> componentBag = new Bag<Component>();
         emptyEntity.getComponents(componentBag);
         assertEquals("Entity not empty.", componentBag.size(), 0);
     }
 
     @Test
     public void createEntityWithComponent() {
         Entity entity = createExampleEntity();
         assertEquals("Entity has no Component.",
                 "SimpleComponent []",
                 entity.getComponent(SimpleComponent.class).toString());
     }
 
     @Test
     public void deleteEntityWithComponent() {
         Entity entity = createExampleEntity();
         int entityID = entity.getId();
         entity.deleteFromWorld();
         assertEquals(
                 "Entity is still there.",
                 null,
                 mWorld.getEntity(entityID));
     }
 
     @Test
     public void deleteComponentFromEntity() {
         Entity entity = createExampleEntity();
         entity.removeComponent(SimpleComponent.class);
         assertEquals(
                 "Component is still there.",
                 null,
                 entity.getComponent(SimpleComponent.class));
     }
 
     private Entity createExampleEntity() {
         final Entity entity = mWorld.createEntity();
         final SimpleComponent componentInserted = new SimpleComponent();
         entity.addComponent(componentInserted);
         entity.addToWorld();
         return entity;
     }
 }
