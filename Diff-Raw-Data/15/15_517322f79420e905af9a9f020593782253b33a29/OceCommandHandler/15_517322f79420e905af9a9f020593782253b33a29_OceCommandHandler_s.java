 package me.oceanor.OceManaBar;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class OceCommandHandler implements CommandExecutor 
 {
     public OceManaBar plugin;
     
     public OceCommandHandler(OceManaBar instance)
     {
         this.plugin = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
     {
         if(command.getName().equalsIgnoreCase("ocemanabar") || command.getName().equalsIgnoreCase("manabar"))
         {
             if (args.length > 0 && args[0].toLowerCase().equals("reload")) 
             {
                 boolean doReload = true;
                 if (sender instanceof Player) 
                 {
                     if(!(sender.isOp() || sender.hasPermission("ocemanabar.reload") || sender.hasPermission("ocemanabar.admin")))
                     {
                         sender.sendMessage("You do not have permission to reload.");
                         doReload = false;
                     }
                 }
                 
                 if (doReload) 
                 {
                     plugin.reloadConfig();
 
                     int oldType = OceManaBar.manabarType;
                     boolean oldShow = OceManaBar.showNumeric;
                     int oldSize = OceManaBar.size;
                     
                     OceManaBar.enabled = plugin.getConfig().getBoolean("enabled");
                     OceManaBar.manabarType = plugin.getConfig().getInt("manabarType");
                     if(OceManaBar.manabarType == 1)
                     {
                         OceManaBar.useAscii = true;
                         OceManaBar.useTexture = false;
                     }
                     else
                     {
                         OceManaBar.useAscii = false;
                         OceManaBar.useTexture = true;    
                     }
                     
                     OceManaBar.maxMana = plugin.getConfig().getInt("maxMana");
                     OceManaBar.showNumeric = plugin.getConfig().getBoolean("showNumeric");
                     OceManaBar.posX = plugin.getConfig().getInt("posX");
                     OceManaBar.posY = plugin.getConfig().getInt("posY");
                     OceManaBar.height = plugin.getConfig().getInt("height");
                     OceManaBar.width = plugin.getConfig().getInt("width");
                     OceManaBar.gradient1 = OceManaBar.hexToRgb(plugin.getConfig().getString("textureColor1"));
                     OceManaBar.gradient2 = OceManaBar.hexToRgb(plugin.getConfig().getString("textureColor2"));
                     OceManaBar.bgcolor1 = OceManaBar.hexToRgb(plugin.getConfig().getString("backgroundColor1"));
                     OceManaBar.bgcolor2 = OceManaBar.hexToRgb(plugin.getConfig().getString("backgroundColor2"));
                     OceManaBar.segmentChar = plugin.getConfig().getString("segmentChar");
                     OceManaBar.size = plugin.getConfig().getInt("size");
 
                     if(OceManaBar.manabarType == 2 && OceManaBar.height < 8)
                         OceManaBar.height = 8;
                     if(OceManaBar.manabarType == 2 && OceManaBar.width < 4)
                         OceManaBar.width = 4;
 
 
                     // now let's reload open widgets!
                     // ascii bars
                     Iterator<Entry<Player, GenericLabel>> it1 = OceManaBar.asciibars.entrySet().iterator();
                     while (it1.hasNext())
                     {
                         Entry<Player, GenericLabel> item = it1.next();
                         GenericLabel asciibar = item.getValue();
                         asciibar.setAuto(false).setX(OceManaBar.posX).setY(OceManaBar.posY).setWidth(OceManaBar.width).setHeight(OceManaBar.height);
 
                         // repaint ascii bar if we need to change size
                         if(OceManaBar.enabled && OceManaBar.manabarType == 1 && (OceManaBar.size != oldSize))
                            {
                             String textbar = "";
                             textbar += ChatColor.DARK_GRAY + "[" + ChatColor.BLUE;
                             int i;
                             for (i=0 ; i<OceManaBar.size; i++)
                                 textbar += OceManaBar.segmentChar;
                             textbar += ChatColor.DARK_GRAY + "]";
                             
                             asciibar.setText(textbar);
                         }
                         
                         // from ascii to texture, we need to delete ascii labels and initialize gradients / backgrounds
                         if(OceManaBar.enabled && oldType == 1 && OceManaBar.manabarType == 2)
                         {
                             SpoutPlayer p = (SpoutPlayer) item.getKey();
                             p.getMainScreen().removeWidget(asciibar);
                             OceManaBar.asciibars.remove(item);
                             
                             GenericGradient gradientBar = new GenericGradient();
                             GenericGradient gradientBackground = new GenericGradient();
                             gradientBar.setX(OceManaBar.posX +1).setY(OceManaBar.posY +2).setWidth(OceManaBar.width -3).setHeight(OceManaBar.height - 7);
                             gradientBar.setBottomColor(OceManaBar.gradient1).setTopColor(OceManaBar.gradient2).setPriority(RenderPriority.Lowest);
                             gradientBackground.setX(OceManaBar.posX).setY(OceManaBar.posY).setWidth(OceManaBar.width).setHeight(OceManaBar.height -3);
                             gradientBackground.setBottomColor(OceManaBar.bgcolor1).setTopColor(OceManaBar.bgcolor2).setPriority(RenderPriority.Highest);
 
                             p.getMainScreen().attachWidget(plugin, gradientBar);
                             p.getMainScreen().attachWidget(plugin, gradientBackground);
                             OceManaBar.gradientbars.put(p, gradientBar);
                             OceManaBar.backgrounds.put(p,gradientBackground);
                         }
                     }
 
                     // gradients
                     Iterator<Entry<Player, GenericGradient>> it2 = OceManaBar.gradientbars.entrySet().iterator();
                     while (it2.hasNext())
                     {
                         Entry<Player, GenericGradient> item = it2.next();
                         GenericGradient bar = item.getValue();
                         bar.setX(OceManaBar.posX +1).setY(OceManaBar.posY +2).setWidth(OceManaBar.width -3).setHeight(OceManaBar.height - 7);
                         bar.setBottomColor(OceManaBar.gradient1).setTopColor(OceManaBar.gradient2).setPriority(RenderPriority.Lowest);
                         
                         // from textures to ascii, we need to delete gradients and initialize ascii labels
                         if(OceManaBar.enabled && oldType == 2 && OceManaBar.manabarType == 1)
                         {
                             SpoutPlayer p = (SpoutPlayer) item.getKey();
                             p.getMainScreen().removeWidget(bar);
                             OceManaBar.gradientbars.remove(item);
                             
                             GenericLabel asciiBar = new GenericLabel();
                             asciiBar.setAuto(false).setX(OceManaBar.posX).setY(OceManaBar.posY).setWidth(OceManaBar.width).setHeight(OceManaBar.height);
                             
                             String textbar = "";
                             textbar += ChatColor.DARK_GRAY + "[" + ChatColor.BLUE;
                             int i;
                             for (i=0 ; i<OceManaBar.size; i++)
                                 textbar += OceManaBar.segmentChar;
                             textbar += ChatColor.DARK_GRAY + "]";
                             
                             asciiBar.setText(textbar);
                             
                             p.getMainScreen().attachWidget(plugin, asciiBar);
                             OceManaBar.asciibars.put(p,asciiBar);
                         }
                     }
 
                     // backgrounds
                     Iterator<Entry<Player, GenericGradient>> it3 = OceManaBar.backgrounds.entrySet().iterator();
                     while (it3.hasNext())
                     {
                         Entry<Player, GenericGradient> item = it3.next();
                         GenericGradient bg = item.getValue();
                         bg.setX(OceManaBar.posX).setY(OceManaBar.posY).setWidth(OceManaBar.width).setHeight(OceManaBar.height -3);
                        bg.setBottomColor(OceManaBar.bgcolor1).setTopColor(OceManaBar.bgcolor1).setPriority(RenderPriority.Highest);
                         
                         // from textures to ascii, we just need to delete backgrounds, ascii labels are already initialized in gradients part 
                         if(OceManaBar.enabled && oldType == 2 && OceManaBar.manabarType == 1)
                         {
                             SpoutPlayer p = (SpoutPlayer) item.getKey();
                             p.getMainScreen().removeWidget(bg);
                             OceManaBar.backgrounds.remove(item);
                         }
                     }
 
                     // numeric manas
                     Iterator<Entry<Player, GenericLabel>> it4 = OceManaBar.numericmanas.entrySet().iterator();
                     while (it4.hasNext())
                     {
                         Entry<Player, GenericLabel> item = it4.next();
                         GenericLabel numericmana = item.getValue();
                         numericmana.setAuto(false).setX(OceManaBar.posX + OceManaBar.width).setY(OceManaBar.posY).setWidth(35).setHeight(OceManaBar.height);
                         
                         if(OceManaBar.enabled && oldShow && !OceManaBar.showNumeric)
                         {
                             // we had this label, now we need to remove
                             SpoutPlayer p = (SpoutPlayer) item.getKey();
                             p.getMainScreen().removeWidget(numericmana);
                             OceManaBar.numericmanas.remove(item);
                         }
                     }
 
                     sender.sendMessage("OceManaBar Configuration Reloaded.");
                     return true;
                 }
             }
         }
         return false;
     }
 }
