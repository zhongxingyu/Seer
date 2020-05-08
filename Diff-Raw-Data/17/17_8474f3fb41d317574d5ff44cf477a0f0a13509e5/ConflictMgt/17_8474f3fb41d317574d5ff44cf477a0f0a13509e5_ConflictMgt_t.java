 package esir.dom11.nsoc.conflictmgt;
 
 //import esir.dom11.nsoc.conflictmgt.Manager;
 
 import esir.dom11.nsoc.model.Action;
 import esir.dom11.nsoc.model.Command;
 import esir.dom11.nsoc.model.Log;
 import esir.dom11.nsoc.model.LogLevel;
 import esir.dom11.nsoc.service.RequestResult;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 
 import static java.lang.Thread.sleep;
 
 @Provides({
         @ProvidedPort(name = "Conflict", type = PortType.MESSAGE)
 })
 @Requires({
         @RequiredPort(name = "actToActuator", type = PortType.MESSAGE, optional = true),
         @RequiredPort(name = "log", type = PortType.MESSAGE, optional = true),
         @RequiredPort(name = "RConflict", type = PortType.MESSAGE, optional = true)
 })
 @Library(name = "NSOC_2011")
 @ComponentType
 public class ConflictMgt extends AbstractComponentType {
 
     /*
     * Class Attributes
     */
 
     private static Logger logger = LoggerFactory.getLogger(ConflictMgt.class.getName());
 
     /*
     * Attributes
     */
     private long updateDelay = 5000;//60000;      //time between updates of locks, in ms (60s by befault)
     private ExecTimer timer;
     private Manager mng;
 
     /*
     * Getters / Setters
     */
 
     public long getUpdateDelay() {
         return updateDelay;
     }
 
     /*
     * Overrides
     */
 
     /**
      *
      */
     @Start
     public void start(){
         logger.info("= = = = = start conflict manager = = = = = =");
         sendLog("Conflict manager is started", LogLevel.INFO);
 
         //Manager initialisation
         mng = new Manager(this,updateDelay);
     }
 
     @Stop
     public void stop() {
         logger.info("= = = = = stop conflict manager = = = = = =");
         timer.shutdown();
     }
 
     @Update
     public void update() {
         logger.info("= = = = = update conflict manager = = = = = =");
         stop();
         start();
     }
 
     /*
     * Methods
     */
 
     /**
      * cmdFromCtrl
      *
      * @param command Command
      */
     @Port(name = "Conflict")
     public void cmdFromCtrl(Object command) {
 
         Command cmd = (Command) command;
 
         LinkedList<Action> actLst = mng.receiveCmd(cmd);
 
         if (actLst != null) {
             for (Action a : actLst) {
                 send2Actuator(a);
                 HashMap<UUID,Long> tmp = mng.get_lockActuatorMap();
                 tmp.put(a.getActuator().getId(),cmd.getLock());
                 mng.set_lockActuatorMap(tmp);
             }
             resp2Ctrl(cmd.getId(), true);            
         }
     }
 
     /**
      * send2Actuator, save the last action for the lock option and send it to the actuator
      *
      * @param action Action
      */
     public void send2Actuator(Action action) {
 
         HashMap<UUID,Action> am = mng.get_lastActuatorActionMap();
         am.put(action.getActuator().getId(), action);
         mng.set_lastActuatorActionMap(am);
 
         getPortByName("actToActuator", MessagePort.class).process(action);

        sendLog("Action (id:"+action.getId()+") is send to Actuator (id:"+action.getActuator().getId()+")",LogLevel.INFO);
         
     }
 
     /**
      * @param str
      * @param lvl
      */
     public void sendLog(String str, LogLevel lvl) {
         Log log = new Log(ConflictMgt.class.getName(), str, lvl);
         getPortByName("log", MessagePort.class).process(log);
     }
 
     /**
      * @param id
      * @param resp
      */
     public void resp2Ctrl(UUID id, boolean resp) {
         logger.info("Conflict Manager -> Action transmitted to control");
         getPortByName("RConflict", MessagePort.class).process(new RequestResult(id, resp));
     }
 
     private void saveInDb() {
         // TODO saveInDb
     }
 }
