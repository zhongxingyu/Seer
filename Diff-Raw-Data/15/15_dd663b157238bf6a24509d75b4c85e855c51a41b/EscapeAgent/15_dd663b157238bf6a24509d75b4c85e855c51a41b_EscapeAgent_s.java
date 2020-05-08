 /**
  * 
  */
 package jadex.agent;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import robot.IPioneer;
 import data.Position;
 import device.ILocalizeListener;
 import jadex.bridge.Argument;
 import jadex.bridge.IArgument;
 import jadex.bridge.IComponentStep;
 import jadex.bridge.IInternalAccess;
 import jadex.commons.ChangeEvent;
 import jadex.commons.IChangeListener;
 import jadex.micro.MicroAgentMetaInfo;
 
 /**
  * @author sebastian
  *
  */
 public class EscapeAgent extends WallfollowAgent
 {
     /** Minimum escape distance */
     double minDist;
     boolean gotCaught = false;
     
     /**
      * @see jadex.agent.WallfollowAgent#agentCreated()
      */
     @Override public void agentCreated()
     {
         super.agentCreated();
         
         minDist = (Double)getArgument("minDistance");
     }
 
     /**
      * @see jadex.agent.WallfollowAgent#executeBody()
      */
     @Override public void executeBody()
     {
         /**
          *  Register localizer callback
          */
         scheduleStep(new IComponentStep()
         {
             public Object execute(IInternalAccess ia)
             {
                 if (robot.getLocalizer() != null) /** Does it have a localizer? */
                 {
                     robot.getLocalizer().addListener(new ILocalizeListener()
                     {
                         @Override public void newPositionAvailable(Position newPose)
                         {
                             sendPosition(newPose);
                         }
                     });
                 }
                 return null;
             }
         });
    
         /**
          *  Register to HelloService
          */
         scheduleStep(new IComponentStep()
         {
             public Object execute(IInternalAccess ia)
             {
                 getHelloService().addChangeListener(new IChangeListener()
                 {
                     public void changeOccurred(ChangeEvent event)
                     {
                         Object[] content = (Object[])event.getValue();
                         StringBuffer buf = new StringBuffer();
                         buf.append("[").append(content[0].toString()).append("]: ").append(content[1].toString()).append(" ").append(content[2].toString());
                                                 
                         /** Check for reply request */
                         if (((String)content[2]).equalsIgnoreCase("ping"))
                         {
                             sendHello();
                         }
                     }
                 });
                 return null;
             }
         });
     
         /**
          *  Register to Position update service
          */
         scheduleStep(new IComponentStep()
         {
             public Object execute(IInternalAccess ia)
             {
                 getSendPositionService().addChangeListener(new IChangeListener()
                 {
                     public void changeOccurred(ChangeEvent event)
                     {
                         Object[] content = (Object[])event.getValue();
                         String id = ((String)content[1]);
                         
                         /** Sending position on request */
                         if (id.equals("request"))
                         {
                             sendPosition(getRobot().getPosition());
                         }
                         else
                         {
                             if (id.equals(getRobot().getRobotId()) == false)
                             {
                                 Position folPose = (Position)content[2];
                                 if (folPose != null)
                                 {
                                     double folDist = folPose.distanceTo(getRobot().getPosition());
                                     getLogger().fine("Nearest follower "+folDist);
                                     
                                     if (folDist < minDist && gotCaught == false)
                                     {
                                         /** Got caught */
                                         gotCaught = true;
                                         getRobot().stop();
                                         getLogger().info("Got caught "+getRobot()+" by "+id);
                                         dance();
                                     }
                                 }
                             }
                         }
                     }
                 });
                 return null;
             }
         });
         
         /**
          * Init wall following
          */
         scheduleStep(new IComponentStep()
         {
             public Object execute(IInternalAccess ia)
             {
                 robot.setWallfollow();
                 robot.runThreaded();
                 return null;
             }
         });
     }
 
     /**
      * @see jadex.agent.WallfollowAgent#agentKilled()
      */
     @Override public void agentKilled()
     {
         super.agentKilled();
     }
 
     public static MicroAgentMetaInfo getMetaInfo()
     {
         IArgument[] args = {
                 new Argument("host", "Player", "String", "localhost"),
                 new Argument("port", "Player", "Integer", new Integer(6665)),
                 new Argument("index", "Robot index", "Integer", new Integer(0)),
                 new Argument("devIndex", "Device index", "Integer", new Integer(0)),
                 new Argument("X", "Meter", "Double", new Double(0.0)),
                 new Argument("Y", "Meter", "Double", new Double(0.0)),
                 new Argument("Angle", "Degree", "Double", new Double(0.0)),
                 new Argument("laser", "Laser ranger", "Boolean", new Boolean(true)),
                 new Argument("localize", "Localize device", "Boolean", new Boolean(true)),
                 new Argument("minDistance", "Minimum escape distance (m)", "Double", new Double(1.5))
         };
         
         return new MicroAgentMetaInfo("This agent starts up an escape agent.", null, args, null);
     }
     void dance()
     {
     	logger.info("Dancing..");
     	getRobot().setCommand();
     	getRobot().setTurnrate(Math.toRadians(IPioneer.MAXTURNRATE));
     	Timer timer = new Timer();
     	timer.schedule(new TimerTask()
     	{
 			@Override public void run() {
 				getRobot().stop();
 			}
     	}, 15000);
     }
 }
