 package net.mms_projects.irc.channel_bots.pb.commands;
 
 import net.mms_projects.irc.channel_bots.pb.Command;
 import net.mms_projects.irc.channel_bots.pb.CommandArgument;
 import net.mms_projects.irc.channel_bots.pb.CommandHandler;
 import net.mms_projects.irc.channel_bots.pb.CommandSyntax;
 import net.mms_projects.irc.channel_bots.pb.PassedData;
 import net.mms_projects.irc.channel_bots.pb.command_arguments.Text;
 
 public class Help extends Command {
 
 	private Command subject;
 
 	public Help(CommandHandler handler, Command subject) {
 		super("help", "Shows some help?", handler);
 
 		this.subject = subject;
 		this.showInList = false;
 	}
 
 	public Help(CommandHandler handler) {
 		super("help", "Shows some help?", handler);
 
 		this.addSyntax(new CommandSyntax(new Text("subject")));
 		this.setLongDescription("This is the help. It will give you information " +
 				"about the available commands. As the help is new it might not " +
 				"contain all information yet.");
 		
 		this.subject = this;
 		this.addHelp();
 	}
 
 	@Override
 	public boolean match(String rawdata) {
 		return rawdata.equalsIgnoreCase(this.getFullCommand());
 	}
 
 	@Override
 	public void run(String rawdata, PassedData data) {
 		boolean helped = false;
 		CommandHandler subject = null;
 		if (this.subject == this) {
 			subject = this.handler;
 		} else {
 			subject = this.subject;
 		}
 		if (this.subject.longDescription != null) {
 			this.reply(data, this.subject.longDescription);
 			this.reply(data, "- ");
 			helped = true;
 		}
 		if (subject.hasCommands()) {
 			this.reply(data,
 					"For more information on a specific command, type /msg "
 							+ data.bot.nickname + " " + this.getFullCommand()
 							+ " command.", 200);
 			subject.showCommands(data, 2);
 			helped = true;
 		}
 		for (CommandSyntax syntax : this.subject.syntaxes) {
 			String syntaxText = "";
 			for (CommandArgument argument : syntax.arguments) {
 				syntaxText += "";
				if (argument.defaults != null) {
 					syntaxText += "[";
 					for (String example : argument.defaults) {
 						syntaxText += example + "/";
 					}
 					syntaxText = syntaxText.substring(0, syntaxText.length() - 1) + "]";
 				} else {
 					syntaxText += argument.name;
 				}
 				syntaxText += " ";
 			}
 			this.reply(data, "Syntax: " + this.subject.getFullCommand() + " " + syntaxText + "");
 			helped = true;
 		}
 		if (!helped) {
 			this.reply(data, "There is no information available about this command.");
 		}
 	}
 
 	@Override
 	public String getFullCommand() {
 		if (this.subject == this) {
 			return super.getFullCommand();
 		}
 		return super.getFullCommand() + " " + this.subject.getFullCommand();
 	}
 
 }
