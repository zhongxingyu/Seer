 package jk_5.nailed.server.command;
 
 import com.google.common.collect.Lists;
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.api.map.Map;
 import jk_5.nailed.api.map.Mappack;
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.CommandException;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
 import net.minecraft.event.HoverEvent;
 import net.minecraft.util.ChatComponentText;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.IChatComponent;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class CommandMap extends NailedCommand {
 
     public CommandMap(){
         super("map");
     }
 
     @Override
     public void processCommandWithMap(ICommandSender sender, Map currentMap, String[] args){
         if(args.length == 0) throw new WrongUsageException("/map <create:remove>");
         if(args[0].equalsIgnoreCase("create")){
             if(args.length == 1) throw new WrongUsageException("/map create <mappackName>");
             String name = args[1];
             Mappack mappack = NailedAPI.getMappackLoader().getMappack(name);
             if(mappack == null) throw new CommandException("Mappack does not exist");
             Map map = NailedAPI.getMapLoader().createMapServer(mappack);
 
            IChatComponent component = new ChatComponentText("Loading ");
             component.getChatStyle().setColor(EnumChatFormatting.GREEN);
            IChatComponent comp = new ChatComponentText(map.getSaveFileName());
            comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Teleport to the map")));
            comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/goto " + map.getSaveFileName()));
            component.appendSibling(comp);
             sender.addChatMessage(component);
         }else if(args[0].equalsIgnoreCase("remove")){
             if(args.length == 1) throw new WrongUsageException("/map remove <mapid>");
             Map map = null;
             for(Map m : NailedAPI.getMapLoader().getMaps()){
                 if(args[1].equalsIgnoreCase(m.getSaveFileName())){
                     map = m;
                     break;
                 }
             }
             if(map == null) throw new CommandException("Map does not exist");
             map.unloadAndRemove();
 
             IChatComponent component = new ChatComponentText("Removed " + map.getSaveFileName());
             component.getChatStyle().setColor(EnumChatFormatting.GREEN);
             sender.addChatMessage(component);
         }else throw new WrongUsageException("/map <create:remove>");
     }
 
     @Override
     public List addTabCompletionOptions(ICommandSender sender, String[] strings){
         if(strings.length == 1) return CommandBase.getListOfStringsMatchingLastWord(strings, "create", "remove");
         else if(strings.length == 2){
             if(strings[0].equalsIgnoreCase("create")){
                 List<String> ret = Lists.newArrayList();
                 for(Mappack mappack : NailedAPI.getMappackLoader().getMappacks()){
                     ret.add(mappack.getMappackID());
                 }
                 return CommandBase.getListOfStringsFromIterableMatchingLastWord(strings, ret);
             }else if(strings[0].equalsIgnoreCase("remove")){
                 List<String> ret = Lists.newArrayList();
                 for(Map map : NailedAPI.getMapLoader().getMaps()){
                     ret.add(map.getSaveFileName());
                 }
                 return CommandBase.getListOfStringsFromIterableMatchingLastWord(strings, ret);
             }
         }
         return Arrays.asList();
     }
 }
