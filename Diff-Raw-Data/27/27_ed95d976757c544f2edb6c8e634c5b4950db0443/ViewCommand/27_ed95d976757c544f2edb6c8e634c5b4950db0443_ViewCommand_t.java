 package de.xcraft.INemesisI.Tickets.Commands.User;
 
 import org.bukkit.command.CommandSender;
 
 import de.xcraft.INemesisI.Library.Command.XcraftCommand;
 import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;
 import de.xcraft.INemesisI.Tickets.Msg;
 import de.xcraft.INemesisI.Tickets.Msg.Replace;
 import de.xcraft.INemesisI.Tickets.Ticket;
import de.xcraft.INemesisI.Tickets.Manager.ConfigManager;
 import de.xcraft.INemesisI.Tickets.Manager.TicketManager;
 
 public class ViewCommand extends XcraftCommand {
 
 	public ViewCommand() {
 		super("ticket", "view", "v.*|i.*", "<ID> [all]", Msg.COMMAND_VIEW.toString(), "XcraftTickets.View");
 	}
 
 	@Override
 	public boolean execute(XcraftPluginManager pManager, CommandSender sender, String[] args) {
 		TicketManager manager = (TicketManager) pManager;
 		int id = Integer.parseInt(args[0]);
 		Ticket ticket = manager.getTicket(id);
 		if (ticket == null) {
 			ticket = manager.getArchivedTicket(id);
 			if (ticket == null) {
 				pManager.plugin.getMessenger().sendInfo(sender, Msg.ERR_TICKET_NOT_FOUND.toString(Replace.ID(id)), false);
 				return true;
			} else {
				ConfigManager cManager = manager.getPlugin().getConfigManager();
				if (cManager.getReminder(sender.getName()).contains(ticket.getId())) {
					cManager.removeReminder(sender.getName(), ticket.getId());
				}
 			}
 		}
 		if (!ticket.getOwner().equals(sender.getName()) && !sender.hasPermission("XcraftTickets.View.All")) {
 			pManager.plugin.getMessenger().sendInfo(sender, Msg.ERR_TICKET_NO_PERMISSION.toString(Replace.ID(id)), false);
 			return true;
 		}
 		String assignee = "";
 		if (ticket.isAssigned()) {
 			assignee = Msg.TICKET_VIEW_ASSIGNEE.toString(Replace.NAME(ticket.getAssignee()));
 		}
 		Replace[] replace = { Replace.ID(id), Replace.NAME(ticket.getOwner()), Replace.ASSIGNEE(assignee) };
 		pManager.plugin.getMessenger().sendInfo(sender, "", false);
 		pManager.plugin.getMessenger().sendInfo(sender, Msg.TICKET_VIEW_INFO.toString(replace), false);
 
 		String[] entries = ticket.getLog().getEntries();
 		int start = 0;
 		if (args.length < 2 && entries.length > 5) {
 			start = entries.length - 4;
 			pManager.plugin.getMessenger().sendInfo(sender, entries[0], false);
 			pManager.plugin.getMessenger().sendInfo(sender, Msg.TICKET_VIEW_BREAK.toString(Replace.ID(id)), false);
 		}
 		for (int i = start; i < entries.length; i++) {
 			sender.sendMessage(entries[i]);
 		}
 		ticket.addToWatched(sender.getName());
 		return true;
 	}
 }
