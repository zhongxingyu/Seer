 package no.runsafe.mailbox.commands;
 
 import no.runsafe.framework.command.player.PlayerCommand;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.mailbox.MailHandler;
 
 import java.util.HashMap;
 
 public class SendMail extends PlayerCommand
 {
	public SendMail(MailHandler mailHandler)
 	{
 		super("sendmail", "Sends mail to another player", "runsafe.mailbox.send", "player");
 		this.mailHandler = mailHandler;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
 	{
 		RunsafePlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));
 
 		if (player == null)
 			return "&cThat player does not exist.";
 
 		if (player.getName().equals(executor.getName()))
 			return "&cYou cannot send mail to yourself.";
 
 		if (!this.mailHandler.hasMailCost(executor))
 			return "&cYou do not have enough money to send mail. Sending mail costs " + this.mailHandler.getMailCostText() + ".";
 
		if (!this.mailHandler.hasFreeMailboxSpace(player))
 			return "&cThat player cannot receive mail right now.";
 
 		this.mailHandler.openMailSender(executor, player);
 		return null;
 	}
 
 	private MailHandler mailHandler;
 }
