 package dungeon.load;
 
import dungeon.Log;
 import dungeon.load.adapters.WorldAdapter;
 import dungeon.load.messages.LevelLoadedEvent;
 import dungeon.messages.LifecycleEvent;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.World;
 
 import javax.xml.bind.JAXB;
 
 public class LevelLoadHandler implements MessageHandler {
   private final Mailman mailman;
 
   public LevelLoadHandler (Mailman mailman) {
     this.mailman = mailman;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message == LifecycleEvent.INITIALIZE) {
       WorldAdapter worldAdapter = new WorldAdapter();
 
       try {
         World world = worldAdapter.unmarshal(JAXB.unmarshal(getClass().getClassLoader().getResourceAsStream("world.xml"), WorldAdapter.class));
 
         this.mailman.send(new LevelLoadedEvent(world));
       } catch (Exception e) {
        Log.error("Loading the world failed", e);
       }
     }
   }
 }
