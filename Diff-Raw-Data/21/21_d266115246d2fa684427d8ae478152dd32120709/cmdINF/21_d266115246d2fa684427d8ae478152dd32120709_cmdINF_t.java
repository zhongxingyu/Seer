 package main.commands;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import main.FoE;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 public class cmdINF implements CommandExecutor {
 	public FoE	plugin;
 	
 	public cmdINF(FoE plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender odesilatel, Command prikaz, String label, String[] args) {
 		if (prikaz.getName().equalsIgnoreCase("infcmd")) {
 			try {
 				Player hrac = (Player) odesilatel;
 				String jmenoHrace = hrac.getName();
 				if (odesilatel.hasPermission("FoE.Informace")) {
 					if (args.length == 0) {
 						plugin.uzivatel(jmenoHrace);
 						long[] cas = plugin.spravnyFormat(System.currentTimeMillis() - plugin.nahranyCas.get(jmenoHrace) + plugin.uziv.getLong("Nahrano"));
 						odesilatel.sendMessage("----- " + hrac.getDisplayName() + " -----");
 						odesilatel.sendMessage("Nahrno: " + cas[4] + " tdnu " + cas[3] + " dn " + cas[2] + " hodin " + cas[1] + " minut " + cas[0] + " sekund");
						odesilatel.sendMessage("IP: " + plugin.uziv.get("IP"));
						odesilatel.sendMessage("lastIP: " + hrac.getAddress().getHostName());
 						if (plugin.uziv.contains("isBanned")) {
							if (plugin.isBanned(jmenoHrace)) {
 								odesilatel.sendMessage("Ban: Ano");
 								odesilatel.sendMessage("Dvod: " + plugin.uziv.getString("banReason"));
 							} else
 								odesilatel.sendMessage("Ban: Ne");
 						}
						odesilatel.sendMessage("Zabito Hr: " + plugin.uziv.getString("ZabitoHracu"));
						odesilatel.sendMessage("Zabito Mob: " + plugin.uziv.getString("ZabitoMobu"));
						odesilatel.sendMessage("Pocet mrt: " + plugin.uziv.getString("PocetSmrti"));
 					} else {
 						Player target = Bukkit.getPlayer(args[0]);
 						if (target != null && plugin.nahranyCas.containsKey(target.getName())) {
 							plugin.uzivatel(target.getName());
 							long[] cas = plugin.spravnyFormat(System.currentTimeMillis() - plugin.nahranyCas.get(target.getName()) + plugin.uziv.getLong("Nahrano"));
 							odesilatel.sendMessage("----- " + target.getDisplayName() + " -----");
 							if (odesilatel.isOp()) {
 								odesilatel.sendMessage("Nahrno: " + cas[4] + " tdnu " + cas[3] + " dn " + cas[2] + " hodin " + cas[1] + " minut " + cas[0] + " sekund");
 								if (plugin.uziv.contains("IP"))
 									odesilatel.sendMessage("IP: " + plugin.uziv.get("IP"));
 								if (plugin.uziv.contains("lastIP"))
 									odesilatel.sendMessage("lastIP: " + target.getAddress().getHostName());
 								if (plugin.uziv.contains("isBanned")) {
 									if (plugin.isBanned(args[0])) {
 										odesilatel.sendMessage("Ban: Ano");
 										odesilatel.sendMessage("Dvod: " + plugin.uziv.getString("banReason"));
 									} else
 										odesilatel.sendMessage("Ban: Ne");
 								}
								odesilatel.sendMessage("Zabito Hr: " + plugin.uziv.getString("ZabitoHracu"));
								odesilatel.sendMessage("Zabito Mob: " + plugin.uziv.getString("ZabitoMobu"));
								odesilatel.sendMessage("Pocet mrt: " + plugin.uziv.getString("PocetSmrti"));
 							} else {
 								odesilatel.sendMessage("Nahrno: " + cas[4] + " tdnu " + cas[3] + " dn " + cas[2] + " hodin " + cas[1] + " minut " + cas[0] + " sekund");
 							}
 						} else {
 							plugin.uzivFile = new File("plugins/FoE/uzivatele/" + args[0] + ".yml");
 							plugin.uziv = YamlConfiguration.loadConfiguration(plugin.uzivFile);
 							if (!plugin.uzivFile.exists()) {
 								odesilatel.sendMessage("Tento hr neexistuje!");
 							} else {
 								long[] cas = plugin.spravnyFormat(plugin.uziv.getLong("Nahrano"));
 								odesilatel.sendMessage("----- " + args[0] + " -----");
 								if (odesilatel.isOp()) {
 									odesilatel.sendMessage("Nahrno: " + cas[4] + " tdnu " + cas[3] + " dn " + cas[2] + " hodin " + cas[1] + " minut " + cas[0] + " sekund");
 									if (plugin.uziv.contains("IP"))
 										odesilatel.sendMessage("IP: " + plugin.uziv.get("IP"));
 									if (plugin.uziv.contains("lastIP"))
 										odesilatel.sendMessage("lastIP: " + plugin.uziv.get("lastIP"));
 									if (plugin.uziv.contains("isBanned")) {
 										if (plugin.isBanned(args[0])) {
 											odesilatel.sendMessage("Ban: Ano");
 											odesilatel.sendMessage("Dvod: " + plugin.uziv.getString("banReason"));
 										} else
 											odesilatel.sendMessage("Ban: Ne");
 									}
									odesilatel.sendMessage("Zabito Hr: " + plugin.uziv.getString("ZabitoHracu"));
									odesilatel.sendMessage("Zabito Mob: " + plugin.uziv.getString("ZabitoMobu"));
									odesilatel.sendMessage("Pocet mrt: " + plugin.uziv.getString("PocetSmrti"));
 								} else {
 									odesilatel.sendMessage("Nahrno: " + cas[4] + " tdnu " + cas[3] + " dn " + cas[2] + " hodin " + cas[1] + " minut " + cas[0] + " sekund");
 								}
 							}
 						}
 					}
 				} else {
 					odesilatel.sendMessage(plugin.nahradit(plugin.config.getString("Ostatni.KdyzNemaOpravneni"), jmenoHrace));
 				}
 			} catch (Exception e) {
 				Writer writer = new StringWriter();
 				PrintWriter printWriter = new PrintWriter(writer);
 				e.printStackTrace(printWriter);
 				plugin.Error(writer.toString());
 			}
 		}
 		
 		return false;
 	}
 }
