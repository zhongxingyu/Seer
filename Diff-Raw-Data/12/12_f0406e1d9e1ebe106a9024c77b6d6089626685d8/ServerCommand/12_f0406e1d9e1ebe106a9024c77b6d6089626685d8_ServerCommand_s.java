 package com.laytonsmith.chadvanced.events;
 
 import com.laytonsmith.abstraction.MCPlayer;
 import com.laytonsmith.abstraction.events.MCConsoleCommandEvent;
 import com.laytonsmith.annotations.api;
 import com.laytonsmith.annotations.event;
 import com.laytonsmith.core.CHVersion;
 import com.laytonsmith.core.Static;
 import com.laytonsmith.core.constructs.CArray;
 import com.laytonsmith.core.constructs.CString;
 import com.laytonsmith.core.constructs.Construct;
 import com.laytonsmith.core.constructs.Target;
 import com.laytonsmith.core.events.AbstractEvent;
 import com.laytonsmith.core.events.BindableEvent;
 import com.laytonsmith.core.events.Driver;
 import com.laytonsmith.core.events.EventBuilder;
 import com.laytonsmith.core.events.EventUtils;
 import com.laytonsmith.core.exceptions.EventException;
 import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
 import com.laytonsmith.core.functions.StringHandling;
 import java.util.Map;
 
 /**
  *
  * @author Layton
  */
 public class ServerCommand {
    
     @event
     public void triggerServerCommand(MCConsoleCommandEvent event){
         EventUtils.TriggerListener(Driver.SERVER_COMMAND, "server_command", event);
     }
    
     @api
     public static class server_command extends AbstractEvent {
 
         public String getName() {
             return "server_command";
         }
 
         public String docs() {
             return "{command: <string match> The entire command the console ran "
                     + "| prefix: <string match> Just the first part of the command, i.e. '/cmd' in '/cmd blah blah'}"
                     + "This event is fired off when any command is run from the console. This actually fires before normal "
                     + " CommandHelper aliases, allowing you to insert control before defined aliases, even. Be careful with this"
 					+ " event, because it overrides ALL console commands, which if you aren't careful can cause all sorts of"
 					+ " havok, because the command is run as console, which is usually completely unrestricted."
                     + "{command: The entire command | prefix: Just the prefix of the command}"
                     + "{command}"
                     + "{command}";
         }
 
         public Driver driver() {
             return Driver.SERVER_COMMAND;
         }
 
         public CHVersion since() {
             return CHVersion.V3_3_1;
         }
 
         public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
             if (e instanceof MCConsoleCommandEvent) {
                 MCConsoleCommandEvent event = (MCConsoleCommandEvent) e;
                 String command = event.getCommand();
                 if(prefilter.containsKey("command") && !command.equals(event.getCommand())){
                     return false;
                 }
                 if(prefilter.containsKey("prefix")){
                     StringHandling.parse_args pa = new StringHandling.parse_args();
                     CArray ca = (CArray)pa.exec(Target.UNKNOWN, null, new CString(command, Target.UNKNOWN));
                     if(ca.size() > 0){
                         if(!ca.get(0).val().equals(prefilter.get("prefix").val())){
                             return false;
                         }
                     } else {
                         return false;
                     }
                 }
                 return true;
             }
             return false;
         }
 
         public BindableEvent convert(CArray manualObject) {
             MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
             String command = manualObject.get("command").nval();
 
             BindableEvent e = EventBuilder.instantiate(MCConsoleCommandEvent.class,
                 player, command);
             return e;
         }
 
         public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
             if (e instanceof MCConsoleCommandEvent) {
                 MCConsoleCommandEvent event = (MCConsoleCommandEvent) e;
                 Map<String, Construct> map = evaluate_helper(e);
                 //Fill in the event parameters
                 map.put("command", new CString(event.getCommand(), Target.UNKNOWN));
 
                 StringHandling.parse_args pa = new StringHandling.parse_args();
                 CArray ca = (CArray)pa.exec(Target.UNKNOWN, null, new CString(event.getCommand(), Target.UNKNOWN));
                map.put("prefix", new CString(ca.get(0).val(), Target.UNKNOWN));
 
                 return map;
             } else {
                 throw new EventException("Cannot convert e to MCConsoleCommandEvent");
             }
         }
 
         public boolean modifyEvent(String key, Construct value, BindableEvent event) {
             if (event instanceof MCConsoleCommandEvent) {
                 MCConsoleCommandEvent e = (MCConsoleCommandEvent) event;
 
                 if("command".equals(key)){
                     e.setCommand(value.val());
                 }
 
                 return true;
             }
             return false;
         }
     }
 }
