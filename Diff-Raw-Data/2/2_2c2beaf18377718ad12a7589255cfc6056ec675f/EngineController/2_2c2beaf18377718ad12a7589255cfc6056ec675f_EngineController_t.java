 package com.freakz.hokan_ng.core_engine.rest;
 
 import com.freakz.hokan_ng.common.entity.Property;
 import com.freakz.hokan_ng.common.entity.PropertyName;
 import com.freakz.hokan_ng.common.exception.HokanEngineException;
 import com.freakz.hokan_ng.common.exception.HokanException;
 import com.freakz.hokan_ng.common.rest.EngineRequest;
 import com.freakz.hokan_ng.common.rest.EngineResponse;
 import com.freakz.hokan_ng.common.rest.InternalRequest;
 import com.freakz.hokan_ng.common.rest.IrcMessageEvent;
 import com.freakz.hokan_ng.common.service.PropertyService;
 import com.freakz.hokan_ng.common.updaters.UpdaterManagerService;
 import com.freakz.hokan_ng.core_engine.command.CommandHandlerService;
 import com.freakz.hokan_ng.core_engine.command.handlers.Cmd;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.annotation.PostConstruct;
 import java.util.Date;
 
 /**
  * Date: 29.5.2013
  * Time: 09:31
  *
  * @author Petri Airio (petri.j.airio@gmail.com)
  */
 
 @Controller
 @Slf4j
 public class EngineController implements DisposableBean {
 
   @Autowired
   private ApplicationContext context;
 
   @Autowired
   private CommandHandlerService commandHandler;
 
   @Autowired
   private PropertyService propertyService;
 
   @Autowired
   private UpdaterManagerService updaterManagerService;
 
 
   @RequestMapping(value = "/handle") //, produces = JSON, consumes = JSON)
   public
   @ResponseBody
   EngineResponse handleRequest(
       @RequestBody EngineRequest request
   ) {
 
     log.info("Got request: " + request);
     EngineResponse response = new EngineResponse(request);
     IrcMessageEvent ircMessageEvent = (IrcMessageEvent) request.getIrcEvent();
     Cmd handler = commandHandler.getCommandHandler(ircMessageEvent.getMessage());
     if (handler != null) {
      InternalRequest internalRequest;
       try {
         internalRequest = context.getBean(InternalRequest.class);
         internalRequest.init(request);
         if (!ircMessageEvent.isPrivate()) {
           internalRequest.getUserChannel().setLastCommand(handler.getName());
           internalRequest.getUserChannel().setLastCommandTime(new Date());
           internalRequest.updateUserChannel();
         }
         handler.handleLine(internalRequest, response);
       } catch (Exception e) {
         HokanEngineException engineException = new HokanEngineException(e, handler.getClass().getName());
         response.setException(engineException.toString());
         log.error("Command handler returned exception {}", e);
       }
     }
     return response;
   }
 
   @PostConstruct
   public void postConstruct() throws HokanException {
     this.updaterManagerService.start();
     propertyService.setProperty(PropertyName.PROP_SYS_CORE_ENGINE_UPTIME, "" + new Date().getTime());
   }
 
   @Override
   public void destroy() throws Exception {
     log.info("Destroying!");
     updaterManagerService.stop();
     Thread.sleep(3 * 1000);
   }
 
   @Scheduled(fixedDelay = 30000)
   public void updateRuntime() throws Exception {
     Property property = propertyService.findProperty(PropertyName.PROP_SYS_CORE_ENGINE_RUNTIME);
     if (property == null) {
       property = new Property(PropertyName.PROP_SYS_CORE_ENGINE_RUNTIME, "0", "");
     } else {
       if (property.getValue() == null) {
         property.setValue("0");
       } else {
         int value = Integer.parseInt(property.getValue());
         value += 60;
         property.setValue(value + "");
       }
     }
     propertyService.saveProperty(property);
   }
 
 }
