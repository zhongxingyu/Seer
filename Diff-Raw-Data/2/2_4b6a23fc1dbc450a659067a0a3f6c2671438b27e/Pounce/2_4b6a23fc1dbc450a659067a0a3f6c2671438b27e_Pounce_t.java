 /*
  * Pounce.java
  *
  * Created on 11 October 2007, 09:05
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Random;
 
 import AndrewCassidy.PluggableBot.DefaultPlugin;
 import AndrewCassidy.PluggableBot.PluggableBot;
 
 /**
  * 
  * @author AndyC & Mex
  */
 public class Pounce extends DefaultPlugin{
 
 	private ArrayList<String> pounces = new ArrayList<String>();
 	private Random r = new Random();
 	private int probability = 4;
 
 	public Pounce() {
 		try {
 			FileReader fr = new FileReader("Pounce");
 			BufferedReader br = new BufferedReader(fr);
 			String line;
 			while ((line = br.readLine()) != null) {
 				pounces.add(line);
 			}
 		} catch (Exception e) {
 			throw new IllegalStateException("Unable to load pounce file.");
 		}
 	}
 
 	public void onAction(String sender, String login, String hostname,
 			String target, String action) {
 
 	}
 
 	public void onJoin(String channel, String sender, String login,
 			String hostname) {
 		if (sender.equals(PluggableBot.Nick())) return;
 		
 		if (r.nextInt(probability) == 1) {
 			PluggableBot
 					.Message(channel, pounces.get(r.nextInt(pounces.size()))
 							.replaceAll("%NAME", sender));
 		}
 	}
 
 	public void onKick(String channel, String kickerNick, String kickerLogin,
 			String kickerHostname, String recipientNick, String reason) {
 		PluggableBot.Message(channel, "haha " + recipientNick
 				+ " was a dick anyway.");
 	}
 
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		
 		String tMessage = message.trim();
 		if (message.startsWith("!addpounce")) {
 
 			String s = message.substring(11);
 			if (pounces.contains(s)) {
 				PluggableBot.Message(channel, sender
 						+ ": Stop feeding me the same junk!");
 			} else {
 				pounces.add(s);
 				boolean ok = saveActions();
 				if (ok) {
 					PluggableBot.Message(channel, sender
 							+ ": I will remeber that!");
 				} else {
 					PluggableBot.Message(channel, sender
 							+ ": Whoops I am being forgetful today!");
 					pounces.remove(s);
 				}
 			}
 		} else if (message.startsWith("ls ") || message.startsWith("cd ")|| message.startsWith("cp ") ||
 				   message.equals("ls")      || message.equals("cd")) {
 			PluggableBot.Message(channel, sender + ": Wrong window?");
 		} else if (message.trim().toLowerCase().equals("ping")) {
			PluggableBot.Message(channel, sender +": PONG!");
 		} else if (message.trim().toLowerCase().startsWith("win ")) {
 			PluggableBot.Message(channel, sender +": Fail!");
 		} else if (message.startsWith("rm ") || tMessage.equals("rm")) {
 			PluggableBot.Message(channel, sender + ": delete porn [y/n]");
 		} else if (tMessage.equals("ps")) {
 			PluggableBot.Message(channel, "PID      TTY     TIME        CMD");
 			PluggableBot.Message(channel, "18101    pts/5   00:00:00    bash");
 			PluggableBot.Message(channel, "18187    pts/5   00:00:00    'firefox.com hotbabes.com'");
 			PluggableBot.Message(channel, "18200    pts/5   00:00:00    ps");
 			
 			
 		} else if (message.toLowerCase().startsWith("identify ")) {
 			PluggableBot.Message(channel, "Hey everyone! "+ sender +"'s password is " + message.substring(9).trim());
 			
 		} 
 	}
 
 
 	private boolean saveActions() {
 		boolean ok = true;
 		try {
 			FileWriter fw = new FileWriter("Pounce");
 			BufferedWriter bw = new BufferedWriter(fw);
 			for (String si : pounces) {
 				bw.write(si + "\n");
 			}
 			bw.flush();
 			bw.close();
 			fw.close();
 		} catch (Exception e) {
 			ok = false;
 		}
 		return ok;
 	}
 
 	public void onPart(String channel, String sender, String login,
 			String hostname) {
 	}
 
 	public void onQuit(String sourceNick, String sourceLogin,
 			String sourceHostname, String reason) {
 	}
 
 	public String getHelp() {
 		return "This plugin makes me 'pounce' on new users that enter the channel, with a probablity of 1 in "
 				+ probability
 				+ ". to add a new pounce, use !addpounce followed by the message, using %NAME as a placeholder.";
 	}
 
 	public void onPrivateMessage(String sender, String login, String hostname,
 			String message) {
 
 	}
 
 	public void unload() {
 	}
 
 	@Override
 	public void onAdminMessage(String sender, String login, String hostname,
 			String message) {
 		if (message.startsWith("!pounce setProb")) {
 
 			String s = message.substring(15);
 			try {
 				probability = Integer.parseInt(s);
 				PluggableBot.Message(sender, "prob set to " + probability);
 
 			} catch (Exception e) {
 			}
 		} else if (message.toLowerCase().trim().equals("!help Actions")) {
 			
 			PluggableBot
 			.Message(sender,
 					"Actions Admin Help: !pounce setProb <prob>");
 		}
 
 	}
 }
