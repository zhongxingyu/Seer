 /*
  * 
  */
 package forscene.system.events;
 
 import playn.core.PlayN;
 import forscene.system.entities.ForSceneConfigurator;
 import forscene.system.managers.AbstractGameLoopManager;
 import forscene.system.managers.EventManager;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class EventInit.
  */
 public class InitEvent extends AbstractEvent {
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.events.AbstractEvent#run()
    */
   @Override
   public void run() {
     setDone(false);
     PlayN.log().debug("EventInit calling init");
     AbstractGameLoopManager.getInstance().init();
     if ((AbstractGameLoopManager.getInstance().getCurrentSceneGroup() != null)
         && (AbstractGameLoopManager.getInstance().getCurrentScene() != null)) {
       setDone(true);
     }
    setDone(true);
    EventManager.getInstance().push(new NextEvent(),
        ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
   }
 }
