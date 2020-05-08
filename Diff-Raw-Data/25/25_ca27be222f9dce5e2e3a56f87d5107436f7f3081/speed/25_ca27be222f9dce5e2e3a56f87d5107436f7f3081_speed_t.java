 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.timolia.custom.cmds;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public final class speed extends TCommand {
 
 	protected void prepare() {
 		permission();
 		ingame();
 		maxArgs(1);
 	}
 
 	public void perform(final CommandSender sender, String[] args) {
 		Player p = (Player) sender;
 		if (args.length == 1) {
 			Float speed;
 			try {
 				speed = Float.valueOf(args[0]);
 				if (speed > 1)
 					speed = (float) 1;
 
 				if (speed < -1)
 					speed = (float) -1;
 			} catch (NumberFormatException e) {
 				sender.sendMessage(prefix + args[0] + " ist keine gültige Zahl!");
 				return;
 			}
 			p.setWalkSpeed(speed);
 			sender.sendMessage(prefix + "Deine Geschwindigkeit wurde zu " + speed + " gesetzt");
 		} else {
 			if (p.getFlySpeed() == 1L) {
 				p.setFlySpeed((float) 0.1);
 				sender.sendMessage(prefix + "Du bist nun langsamer!");
 			} else {
 				p.setFlySpeed(1L);
 				sender.sendMessage(prefix + "Du bist nun schneller");
 			}
 		}
 	}
 
 }
