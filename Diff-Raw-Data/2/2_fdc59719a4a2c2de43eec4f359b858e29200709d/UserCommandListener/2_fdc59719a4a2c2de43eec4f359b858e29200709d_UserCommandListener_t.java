 /**
  * This file is part of Skynet, the ChatNano Channel Management Bot.
  *
  * Skynet is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * Skynet is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with Skynet. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package SkynetBot;
 
 import java.util.Collection;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  *
  * @author Matthew Walker
  */
 public class UserCommandListener extends ListenerAdapter  {
 	@Override
 	public void onMessage( MessageEvent event ) {
 		String message = Colors.removeFormattingAndColors(event.getMessage());
 
 		if (message.startsWith("!skynet")) {
 			String command;
 			String[] args = message.split(" ");
 
 			if (args.length <= 1) {
 				event.respond("You have failed to provide a command. Please remain where you are and await termination.");
 				return;
 			}
 
 			command = args[1].toLowerCase();
 
 			if (command.equals("lastseen")) {
 				event.respond(SkynetBot.db.getLastSeen(args[2], event.getChannel()));
 			} else if (command.equals("report")) {
 				String reason = null;
 				if (args.length > 2) {
 					reason = "";
 					for (int i = 2; i < args.length; i++) {
 						reason += args[i] + " "; 
 					}
 				}
 				
 				sendLog(event.getChannel(), event.getUser(), reason);
 				event.respond("Thank you, informant! Your information is invaluable. This will be taken into consideration when it is time for your termination.");
 			} else if (command.equals("badword")) {
 				if (args[2].equals("list")) {
 					Collection<String> words = SkynetBot.db.badwords.get(event.getChannel().getName());
 					if (words == null || words.isEmpty()) {
 						event.respond("No record exists of banned words for this channel.");
 					} else {
 						event.respond("Transmitting banned word list now... (May appear in another tab or window)");
 						for (String word : words) {
 							SkynetBot.bot.sendNotice(event.getUser(), word);
 						}
 					}
 				} else {
 					event.respond("Unknown badword action. Valid actions: list");
 				}
 			} else if (command.equals("help")) {
 				printCommandList(event);
 			} else {
 				event.respond("!skynet " + command + " NOT FOUND. Read !skynet help to avoid termination!");
 			}
 		}
 	}
 	
 	private void printCommandList( MessageEvent event ) {
 		SkynetBot.bot.sendAction(event.getChannel(), "whispers something to " + event.getUser().getNick() + ". (Check for a new window or tab with the help text.)");
 
 		String[] helplines = {"Core Skynet User Commands:",
 							  "    !skynet badword list      - View the list of banned words",
 							  "    !skynet lastseen <user>   - Report when that user was last seen in channel",
 							  "    !skynet report [<reason>] - Send last 25 lines of history to ML, with an optional reason",};
 
 		for (int i = 0; i < helplines.length; ++i) {
 			SkynetBot.bot.sendNotice(event.getUser(), helplines[i]);
 		}
 	}
 
 	protected void sendLog( Channel channel, User user, String explanation ) {
		ChannelInfo info = SkynetBot.db.channel_data.get(channel.getName().toLowerCase());
 
 		String from = "Skynet Bot <mwalker+nanowrimo@kydance.net>";
 		String host = "localhost";
 		
 		Properties properties = System.getProperties();
 		properties.setProperty("mail.smtp.host", host);
 		
 		Session session = Session.getDefaultInstance(properties);
 		
 		try {
 			MimeMessage message = new MimeMessage(session);
 			message.setFrom(new InternetAddress(from));
 			
 			for (String ml : info.mls) {
 				message.addRecipient(Message.RecipientType.TO, new InternetAddress(SkynetBot.db.getMLEmail(channel, ml)));
 			}
 
 			message.addRecipient(Message.RecipientType.CC, new InternetAddress("Matthew Walker <mwalker+nanowrimo@kydance.net>"));
 			message.setSubject("Channel Log from Skynet");
 			
 			String body;
 
 			body = "A user on your channel by the name of '" + user.getNick() + "' requested that I send you this log.";
 			
 			if (explanation != null) {
 				body += " They provided this explanation:\n\n";
 				body += explanation + "\n\n";
 			}
 			
 			body += "Here is the activity leading up to the request:\n\n";
 
 			String[] logLines = ServerListener.channel_logs.get(channel.getName()).toArray(new String[0]);
 			for (String line : logLines) {
 				body += line + "\n";
 			}
 
 			message.setText(body);
 			
 			Transport.send(message);
 		} catch (MessagingException ex) {
 			Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 }
