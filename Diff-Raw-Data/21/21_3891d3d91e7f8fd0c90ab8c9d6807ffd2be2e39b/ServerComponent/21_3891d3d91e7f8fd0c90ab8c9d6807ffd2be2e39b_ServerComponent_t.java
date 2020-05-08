 package esir.dom11.nsoc.server;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Pierre
  * Date: 07/01/12
  * Time: 11:19
  * To change this template use File | Settings | File Templates.
  */
 
 import esir.dom11.nsoc.model.Command;
 import esir.dom11.nsoc.model.Data;
 import esir.dom11.nsoc.model.HmiRequest;
import esir.dom11.nsoc.service.IServerService;
 import org.kevoree.annotation.*;
 import org.kevoree.annotation.ComponentType;
 import org.kevoree.annotation.Port;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 
 import java.util.LinkedList;
 
 @Library(name = "NSOC_2011")
 
 
 // input port (CTRL -> HMI)
 @Requires({
        @RequiredPort(name = "getCTRL", type = PortType.SERVICE, className = IServerService.class),
         @RequiredPort(name = "postCTRL", type = PortType.MESSAGE)
 })
 
 @Provides({
         @ProvidedPort(name = "fromCTRL", type = PortType.MESSAGE)
 })
 
 @DictionaryType({
         @DictionaryAttribute(name = "port", defaultValue = "8182", optional = true)
 })
 
 @ComponentType
 public class ServerComponent extends AbstractComponentType {
     private ServerManager _manager;
 
     public ServerComponent() {
         // save itself in LocalStorage to be reused in ServerManager class
         LocalStorage.getLocalStorageObject().setServerComponent(this);
         _manager = new ServerManager();
     }
 
     @Start
     public void startComponent(){
         int port = Integer.valueOf(getDictionary().get("port").toString());
         _manager.startServer(port);
     }
 
     @Stop
     public void stopComponent(){
         _manager.stopServer();
     }
 
     @Update
     public void updateComponent(){
         stopComponent();
         startComponent();
     }
 
 
     public LinkedList<Data> sendGetRequest(HmiRequest hmir){
         return (LinkedList<Data>) getPortByName("CTRL", IServerService.class).getFromHmi(hmir);
     }
 
     // Be careful
     // The Controller can send null response.
     @Port(name = "fromCTRL")
    public Command receiveFromCtrl(Object fromCtrl){
        return (Command) fromCtrl;
     }
 
     //send the message through the require port
     public void sendMessage(Object message){
         MessagePort prodPort = getPortByName("postCTRL", MessagePort.class);
         if(prodPort != null){
             prodPort.process(message);
         }
    }
 
}
