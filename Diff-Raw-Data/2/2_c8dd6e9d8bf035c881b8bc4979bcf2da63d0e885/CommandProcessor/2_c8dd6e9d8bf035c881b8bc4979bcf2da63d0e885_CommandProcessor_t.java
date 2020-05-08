 package net.esper.tacos.ircbot;
 
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 
 public class CommandProcessor {
 	
 	private static ProcessThread PROC_THREAD;
 	public static List<MessageEvent> events = new CopyOnWriteArrayList<MessageEvent>();
 	private static Object PROC = new Object();
 	private static boolean ASLEEP = false;
 	
 	public static void init() {
 		PROC_THREAD = new ProcessThread();
 		PROC_THREAD.start();
 	}
 	
 	public static void process(MessageEvent<?> event) {
 		events.add(event);
 		if (ASLEEP) {
 			synchronized (PROC) {
 				PROC.notify();
 			}
 		}
 	}
 	
 	public static class ProcessThread extends Thread {
 		
 		@SuppressWarnings("unchecked")
 		public void run() {
 			while (true) {
 				if (events.size() == 0) {
 					ASLEEP = true;
 					try {
 						synchronized (PROC) {
 							PROC.wait();
 						}
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				ASLEEP = false;
 				MessageEvent<?> ev = events.remove(events.size() - 1);
 				
 				if (ev.getMessage().startsWith(TacoBot.PREFIX) && !ev.getMessage().startsWith(TacoBot.PREFIX + "/")) {
 					// Ok attempt to use reflection!
 					ICommand cmd = null;
 					try {
						Class<ICommand> clazz = (Class<ICommand>) Class.forName("net.esper.tacos.ircbot.commands." + ev.getMessage().split(" ")[0].substring(1));
 						cmd = clazz.newInstance();
 					} catch (Exception e) {} finally {
 						if (cmd == null) continue;
 						
 						String message = ev.getMessage();
 						String[] args = ev.getMessage().split(" ");
 						User user = ev.getUser();
 						
 						// Let's check permissions!
 						if (cmd.getRank() != null && cmd.getRank() != Rank.PEASANT && !Rank.canUse(Rank.getRank(user), cmd.getRank())) {
 							if (cmd.getNoAccessMessage() != null) {
 								TacoBot.sendMessage(user, cmd.getNoAccessMessage());
 							}
 							continue;
 						}
 						
 						try {
 							cmd.exec(message, args, user);
 						} catch (Exception e) {
 							e.printStackTrace();
 							TacoBot.sendMessage(user, "An exception had occured and was printed to console.");
 						}
 					}
 				} else if (ev.getMessage().startsWith("s/") && ev.getUser().getChannelsOpIn().contains(TacoBot.CHAN_OBJ)) {
 					String[] torepl = ev.getMessage().split("/"); // s/g/r [0]/[1]/[2]
 					if (torepl.length < 3) {
 						return;
 					}
 					// search
 					String vic = null;
 					for (String msg : TacoBot.getMsgs()) {
 						if (msg.contains(torepl[1])) {
 							vic = msg;
 							break;
 						}
 					}
 					if (vic == null) {
 						return;
 					}
 					String repl = vic.replace(torepl[1], torepl[2]);
 					TacoBot.sendMessage(repl);
 				}
 			}
 		}
 	}
 	
 	public static void process(PrivateMessageEvent<?> event) {
 		events.add(new MessageEvent<PircBotX>(TacoBot.bot, TacoBot.CHAN_OBJ, event.getUser(), event.getMessage()));
 		if (ASLEEP) {
 			synchronized (PROC) {
 				PROC.notify();
 			}
 		}
 	}
 }
