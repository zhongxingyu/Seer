 package game;
 
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author JP
  * @author NM
  */
 public class GameBot 
 {
     static enum ActionType
     {
         POSITION,
         ROTATION
     };
     
     private class Action
     {
         ActionType type;
         Vector3f data;
         
         Action(ActionType type, Vector3f data)
         {
             this.type = type;
             this.data = data;
         }
     };
     
     public static void main(String[] args)
     {
         System.out.println("Starting");
         GameBot bot = new GameBot();
         bot.load();
         bot.run();
     }
     
     GameModel model;
     List<Action> actions = new LinkedList<Action>();
     
     GameBot()
     {
         model = new GameModel(null);
     }
     
     public void load()
     {
        actions.add(new Action(ActionType.POSITION, new Vector3f(20, 0, 30)));
     }
     
     public void run()
     { 
         model.name = "ASS_BOT";
         model.position = new Vector3f();
         model.rotation = new Quaternion();
         
         model.connect("GEOFF-LENOVO", 8000, 6000);
         System.out.println("connected");
  
         Vector3f targetPosition = new Vector3f();
         Vector3f startPosition = new Vector3f();
         Vector3f currentPosition = new Vector3f();
         boolean inAction = false;
         
         long lastTime = System.nanoTime();
         boolean running = true;
         while(running)
         {
             // get timestep
             long thisTime = System.nanoTime();
             long difference = thisTime - lastTime;
             double ts = (double)difference / 1000000000.f;
             lastTime = thisTime;
             
             if(inAction)
             {
                 targetPosition.z -= ts;
                 boolean doneWithMove = false;
                 
                 if(targetPosition.z <= 0)
                 {
                     doneWithMove = true;
                 }
                 else
                 {
                     float percentDone = (startPosition.z - targetPosition.z) / startPosition.z;
                     currentPosition.x = (targetPosition.x - startPosition.x) * percentDone + startPosition.x;
                     currentPosition.y = (targetPosition.y - startPosition.y) * percentDone + startPosition.y;
                     model.position = new Vector3f(currentPosition.x, 0, currentPosition.y);
                    model.positionChanged = true;
                 }
                 
                 if(doneWithMove)
                 {
                     inAction = false;
                     model.position = new Vector3f(targetPosition.x, 0, targetPosition.y);
                     model.positionChanged = true;
                 }
                 
                 //System.out.println("moved to " + model.position);
                 
             }
             else if(actions.size() > 0)
             {
                 targetPosition = actions.get(0).data;
                 startPosition = new Vector3f(model.position.x, model.position.z, targetPosition.z);
                 currentPosition = new Vector3f(startPosition);
                 
                 actions.remove(0);
                 inAction = true;
                 
                 //System.out.println("===============================");
                 //System.out.println("move from " + startPosition + " to " + targetPosition);
             }
             else
             {
                 //running = false;
             }
             
             try
             {
                 Thread.sleep(100);
             }
             catch(Exception e)
             {
             }
         }
         
         model.shutdown();
     }
 }
