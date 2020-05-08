 package org.kevoree.macaw.framework.scheduler;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.builder.RouteBuilder;
 import org.kevoree.annotation.*;
 import org.kevoree.api.service.core.script.KevScriptEngine;
 import org.kevoree.framework.MessagePort;
 import org.kevoree.framework.message.StdKevoreeMessage;
 import org.kevoree.library.camel.framework.AbstractKevoreeCamelComponentType;
 import org.kevoree.macaw.framework.MacawMessageTypes;
 import org.macaw.messages.MethodResult;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 16/04/12
  * Time: 11:04
  */
 
 
 @Library(name = "Freepastry")
 @ComponentType
 @Requires({
         @RequiredPort(name = "request", type = PortType.MESSAGE, messageType = "request")
 })
 @Provides({
         @ProvidedPort(name = "response", type = PortType.MESSAGE, messageType = "response"),
         @ProvidedPort(name = "query", type = PortType.MESSAGE,messageType = "macawQuery")
 })
 public class CamelScheduler extends AbstractKevoreeCamelComponentType implements MacawMessageTypes, Scheduler {
 
     @Override
     protected void buildRoutes(RouteBuilder rb) {
         rb.from("kport:response").process(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
                 //TODO
             }
         });
 
         rb.from("kport:query").process(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
 
                 if(exchange.getIn().getBody() instanceof String[]){
                     String[] testcases = (String[]) exchange.getIn().getBody();
 
                 }
 
 
                 System.out.println("input="+exchange.getIn().getBody());
 
 
 
                 //TODO
             }
         });
     }
 
     @Override
     public void sendToTester(StdKevoreeMessage msg) {
         //RELY ON FILTERED CHANNEL
         getPortByName("request", MessagePort.class).process(msg);
     }
 
     @Override
     public MethodResult sendSyncToTester(StdKevoreeMessage msg) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public List<MethodResult> waitForResponse(List<Integer> ids) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
 
     @Override
     public void executeKevScriptStatement(String kscript) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
