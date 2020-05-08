 package me.tehbeard.utils.commands;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.server.ServerCommandEvent;
 import org.bukkit.permissions.Permissible;
 
 /**
  * Processes commands inputed by a user
  * Currently supports CommandPreprocessEvent and manual call via executeCommand 
  * @author James
  *
  */
 public class CommandHandler implements Listener {
 
 
     public Map<String,CommandInfo> commandMap;
 
     public CommandHandler(){
         commandMap = new HashMap<String, CommandInfo>();
     }
 
 
     /**
      * Add a command executor to this handler
      * @param executor executor to add
      * @return true if added, false if not
      */
     public void addCommand(Class<?> executor){
 
         for(Method m : executor.getMethods()){
             CommandDescriptor scrip = m.getAnnotation(CommandDescriptor.class);
             if(scrip != null){
                 if(!m.getReturnType().equals(boolean.class)){
                     throw new IllegalArgumentException(m.getName() + " Methods must return a boolean");
                 }
                 if(Modifier.isStatic(m.getModifiers())){
                     Class<?>[] params = m.getParameterTypes();
                     if(params.length != 1){
                         throw new IllegalArgumentException("Invalid number of parameters for function handling " + scrip.label());
                     }
                     
                     if(!params[0].equals(ArgumentPack.class)){
                         throw new IllegalArgumentException("Parameter must be of type ArgumentPack for function handling " + scrip.label());
                     }
                     
                     CommandPermission cp = m.getAnnotation(CommandPermission.class);
                     String permission = "";
                     if(cp!=null){permission = cp.value();}
                     
                     CommandBooleanFlags cbf = m.getAnnotation(CommandBooleanFlags.class);
                     String[] _cbf = cbf!=null ? cbf.value(): new String[0];
                     
                     CommandOptionFlags cof = m.getAnnotation(CommandOptionFlags.class);
                     String[] _cof = cof!=null ? cof.value(): new String[0];
                     
                     CommandInfo ci = new CommandInfo(m, permission,_cbf,_cof,scrip.senderType());
                     
                     String tag = null;
                     if(!commandMap.containsKey(scrip.label())){
                         tag=scrip.label();
                     }
                     else
                     {
                         for(String t:scrip.alias()){
                             if(!commandMap.containsKey(t)){
                                 tag=t;
                                 break;
                             }
                         }
                     }
                     if(tag==null){System.out.println("Could not add CommandExecutor, name + aliases already taken for " + scrip.label() );}
                     commandMap.put(tag, ci);
                 }
                 else
                 {
                     throw new IllegalStateException("Command " + scrip.label() + " Must be a static method");
                 }
             }
 
         }
     }
 
     /**
      * Handles a command event
      * @param event
      */
     @EventHandler(priority=EventPriority.MONITOR)
     public void onCommand(PlayerCommandPreprocessEvent event){
         if(event.isCancelled()){return;}
        if(executeCommand(event.getPlayer(),event.getMessage())){
         	event.setCancelled(true);
        }
     }
     
     /**
      * Handles a command event from the console
      * @param event
      */
     @EventHandler(priority=EventPriority.MONITOR)
     public void onCommand(ServerCommandEvent event){
         
         executeCommand(event.getSender(),event.getCommand());
     }
     
     
     /**
      * execute a command as sender
      * @param sender who to execute as
      * @param command command to execute, should NOT have leading /
      * @return 
      */
     public boolean executeCommand(CommandSender sender,String command){
         
         String cmd = command.split(" ")[0];
         if(!commandMap.containsKey(cmd)){return false;}
 
         CommandInfo c = getInfo(cmd);
        if(!hasPermission(sender,c)){return false;}
         
         if(!c.senderType.isValid(sender)){
         	
         	return false;
         	}
 
         String[] raw = command.split(" ");
         String[] args = new String[raw.length - 1];
 
         for(int i = 1;i<raw.length;i++){
             args[i-1]=raw[i];
         }
         
         
         ArgumentPack pack = new ArgumentPack(sender,c.boolFlags, c.optFlags, args);
         return c.execute(pack);
     }
 
 
     private boolean hasPermission(Permissible p,CommandInfo executor){
         return p.hasPermission(executor.permission) || executor.permission=="";
     }
 
     public class CommandInfo {
         private final Method method;
         public final String permission;
         public final String [] boolFlags;
         public final String [] optFlags;
         public final SenderType senderType;
         
         
         /**
          * @param method
          * @param permission
          * @param boolFlags
          * @param optFlags
          */
         public CommandInfo(Method method, String permission,
                 String[] boolFlags, String[] optFlags,SenderType senderType) {
             super();
             this.method = method;
             this.permission = permission;
             this.boolFlags = boolFlags;
             this.optFlags = optFlags;
             this.senderType = senderType;
         }
 
 
 
         public Boolean execute(ArgumentPack pack){
             try {
             	//enforce sender type validity
             	if(pack == null){throw new IllegalArgumentException("MUST SUPPLY ARGUMENT PACK");}
             	if(pack.getSender() != null && !senderType.isValid(pack.getSender())){
             		pack.getSender().sendMessage("Command can only be executed by " + senderType.toString().toLowerCase());
             		return true;
             	}
                 return (Boolean) method.invoke(null, pack);
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (IllegalArgumentException e) {
                 e.printStackTrace();
             } catch (InvocationTargetException e) {
                 e.printStackTrace();
             }
             return false;
         }
 
     }
     
     public CommandInfo getInfo(String comm){
         return commandMap.get(comm);
     }
     
     
     public boolean removeCommand(String command){
         return commandMap.remove(command) != null;
     }
     
 }
