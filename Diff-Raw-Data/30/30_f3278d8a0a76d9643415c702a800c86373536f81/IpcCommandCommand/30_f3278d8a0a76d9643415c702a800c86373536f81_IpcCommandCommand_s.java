 package de.cosmocode.palava.bridge.command;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import de.cosmocode.palava.bridge.Content;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.content.JsonContent;
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 
 public class IpcCommandCommand implements Command {
     private static final Logger LOG = LoggerFactory.getLogger(IpcCommandCommand.class);
 
     private final IpcCommand ipcCommand;
 
     public IpcCommandCommand(IpcCommand ipcCommand) {
         this.ipcCommand = Preconditions.checkNotNull(ipcCommand, "IpcCommand");
     }
 
     @Override
     public Content execute(Call call) throws CommandException {
         Map<String,Object> result = Maps.newLinkedHashMap();
 
         try {
             ipcCommand.execute(call, result);
         } catch (IpcCommandExecutionException e) {
             throw new CommandException(e);
         }
 
         return new JsonContent(result);
     }
 }
