 package org.l3eta.turntable;
 
 import org.l3eta.turntable.tt.Bot;
 import org.l3eta.turntable.tt.Commands;
 import org.l3eta.turntable.util.net.Client;
 
 public class Starter {
 
 	public static void main(String[] args) {
 		new Starter();
 	}
 
 	public Starter() {
 		try {
 			run("Bot");
 			Bot bot = (Bot) bot.Future.class.newInstance();
 			new Client(1500).start(new Commands(bot));
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public void run(String botName) {
 		try {
 			Runtime.getRuntime()
					.exec("cmd.exe /C start ./js/" + botName + ".bat");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 }
