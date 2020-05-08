 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package de.minestar.AdminStuff.commands;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 
 public class cmdHelpPage extends AbstractCommand {
 //    private static final String DESCRIPTION = "description";
 //    private static final String PERMISSION = "permission";
 //    private static final String PERMISSIONS = "permissions";
 
     public cmdHelpPage(String syntax, String arguments, String node) {
        super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /help <Page> br>
      * Show the help page X
      * 
      * @param player
      *            Called the command
      * @param split
      * 		  split[0] is the page
      */
     public void execute(String[] args, Player player) {
 //        int page = 1;
 //        try {
 //            page = Integer.valueOf(args[0]);
 //        } catch (Exception e) {
 //            page = 1;
 //        }
 //        List<String> lines = getHelpLines(player);
 //        int start = (page - 1) * 9;
 //        int pages = lines.size() / 9 + (lines.size() % 9 > 0 ? 1 : 0);
 //
 //        player.sendMessage(ChatColor.AQUA + "---------[ LIST OF COMMANDS " + page + "/" + pages + "] ---------");
 //        for (int i = start; (i < lines.size()) && (i < start + 9); i++) {
 //            player.sendMessage((String) lines.get(i));
 //        }
     }
 //
 //    @SuppressWarnings("unchecked")
 //    public List<String> getHelpLines(Player player) {
 //        List<String> retval = new ArrayList<String>();
 //        boolean reported = false;
 //        String pluginName = "";
 //        for (Plugin p : ASCore.getMCServer().getPluginManager().getPlugins()) {
 //            try {
 //                PluginDescriptionFile desc = p.getDescription();
 //                if (UtilPermissions.playerCanUseCommand(player, "adminstuff.commands.help." + desc.getName().toLowerCase())) {
 //                    final HashMap<String, HashMap<String, Object>> cmds = (HashMap<String, HashMap<String, Object>>) desc.getCommands();
 //
 //                    for (Entry<String, HashMap<String, Object>> k : cmds.entrySet()) {
 //                        final HashMap<String, Object> value = k.getValue();
 //                        if (value.containsKey(PERMISSION) && value.get(PERMISSION) instanceof String && !(value.get(PERMISSION).equals(""))) {
 //                            if (UtilPermissions.playerCanUseCommand(player, (String) value.get(PERMISSION))) {
 //                                retval.add(ChatColor.RED + k.getKey() + ChatColor.GRAY + ": " + value.get(DESCRIPTION));
 //                            }
 //                        } else if (value.containsKey(PERMISSION) && value.get(PERMISSION) instanceof List && !((List<Object>) value.get(PERMISSION)).isEmpty()) {
 //                            boolean enabled = false;
 //                            for (Object o : (List<Object>) value.get(PERMISSION)) {
 //                                if (o instanceof String && UtilPermissions.playerCanUseCommand(player, (String) o)) {
 //                                    enabled = true;
 //                                    break;
 //                                }
 //                            }
 //                            if (enabled) {
 //                                retval.add(ChatColor.RED + k.getKey() + ChatColor.GRAY + ": " + value.get(DESCRIPTION));
 //                            }
 //                        } else if (value.containsKey(PERMISSIONS) && value.get(PERMISSIONS) instanceof String && !(value.get(PERMISSIONS).equals(""))) {
 //                            if (UtilPermissions.playerCanUseCommand(player, (String) value.get(PERMISSIONS))) {
 //                                retval.add(ChatColor.RED + k.getKey() + ChatColor.GRAY + ": " + value.get(DESCRIPTION));
 //                            }
 //                        } else if (value.containsKey(PERMISSIONS) && value.get(PERMISSIONS) instanceof List && !((List<Object>) value.get(PERMISSIONS)).isEmpty()) {
 //                            boolean enabled = false;
 //                            for (Object o : (List<Object>) value.get(PERMISSIONS)) {
 //                                if (o instanceof String && UtilPermissions.playerCanUseCommand(player, (String) o)) {
 //                                    enabled = true;
 //                                    break;
 //                                }
 //                            }
 //                            if (enabled) {
 //                                retval.add(ChatColor.RED + k.getKey() + ChatColor.GRAY + ": " + value.get(DESCRIPTION));
 //                            }
 //                        } else if (UtilPermissions.playerCanUseCommand(player, "adminstuff.user.help." + pluginName)) {
 //                            retval.add(ChatColor.RED + k.getKey() + ChatColor.GRAY + ": " + value.get(DESCRIPTION));
 //                        }
 //                    }
 //                }
 //            } catch (NullPointerException ex) {
 //            } catch (Exception ex) {
 //                if (!reported) {
 //                    ASCore.log.printWarning("Error getting help for:" + pluginName);
 //                    ex.printStackTrace();
 //                }
 //                reported = true;
 //            }
 //        }
 //        return retval;
 //    }
 }
