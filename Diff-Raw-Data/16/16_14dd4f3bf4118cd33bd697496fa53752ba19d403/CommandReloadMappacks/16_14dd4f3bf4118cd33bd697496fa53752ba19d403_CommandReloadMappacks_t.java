 package jk_5.nailed.server.command;
 
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.api.map.Map;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.util.ChatComponentText;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.IChatComponent;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class CommandReloadMappacks extends NailedCommand {
 
     public CommandReloadMappacks(){
        super("reloadmappacks");
     }
 
     @Override
     public void processCommandWithMap(ICommandSender sender, Map map, String[] args){
         NailedAPI.getMappackLoader().loadMappacks();
 
         IChatComponent component = new ChatComponentText("Successfully loaded " + NailedAPI.getMappackLoader().getMappacks().size() + " mappacks");
         component.getChatStyle().setColor(EnumChatFormatting.GREEN);
         sender.addChatMessage(component);
     }
 }
