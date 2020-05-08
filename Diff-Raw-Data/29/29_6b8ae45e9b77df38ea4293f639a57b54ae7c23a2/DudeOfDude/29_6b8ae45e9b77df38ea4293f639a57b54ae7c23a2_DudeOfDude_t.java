 package net.it_tim.dude_of_dude;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Timer;
 import java.io.Console;
 import com.sun.security.auth.module.UnixSystem;
 import net.it_tim.dude_of_dude.database.*;
 import net.it_tim.dude_of_dude.static_constants.Message;
 
 public class DudeOfDude {
 
 	private static long uid = -1;
 	private static Console console = System.console();
 	/**
 	 * @param args
 	 */
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) {
 		coloredPrint(Message.COLOR_GREEN, "~~~ Перевірка умов запуску ~~~", Message.COLOR_WHITE);
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
		if (System.getProperty("os.name").equals("Linux")) {		
		UnixSystem unix_user = new UnixSystem();
     	uid = unix_user.getUid();
     	if (uid != 0) {
     		coloredPrint(Message.COLOR_RED, "!!! Потрібні супер права !!!", Message.COLOR_WHITE);
             System.exit(-1);
     	}
		}
 
     	try {
 			String username = console.readLine("[%s] > [%s]", Message.getDateTime(), "Логін:");
 			char[] passwd = console.readPassword("[%s] > [%s]", Message.getDateTime(), "Пароль:");
 			
 			UsersHome usermanager = new UsersHome();
 			
 			if (usermanager.checkPassword(username, new String(passwd))) {
 				Arrays.fill(passwd, ' ');
 				coloredPrint(Message.COLOR_GREEN, " ~~~ Вхід вдалий ~~~", Message.COLOR_WHITE);
 			} else {
 				Arrays.fill(passwd, ' ');
 				coloredPrint(Message.COLOR_RED, " ~~~ Щось зламалось ~~~", Message.COLOR_WHITE);
 				System.exit(0);
 			}
 
 		} catch (NullPointerException ex) {
 			ex.printStackTrace();
 			coloredPrint(Message.COLOR_RED, "!!! Системна консоль не доступна !!!", Message.COLOR_WHITE);
 			System.exit(-1);
 		}
 
 
 		try {
 			HostsHome hh = new HostsHome();
 			
 			List host_list = hh.getAll();
 			for (Hosts host: (List<Hosts>) host_list) {
 				PingThread ping_thread = new PingThread(host);
 				Timer timer = new Timer();
 				timer.schedule(ping_thread, 0, host.getIntervalMs());
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {}
 			}
 			/*
 			ping = new Ping("192.168.77.7", 1000);
 			if (ping.isOnline()) {
 				System.out
 						.println(String
 								.format(
 										net.it_tim.dude_of_dude.static_constants.Message.SVC_UP,
 										"Barn-e", "192.168.77.7", "now"));
 			} else {
 				String recs[] = { "gofl@meta.ua" /*
 												 * ,
 												 * "380676589174@sms.kyivstar.net"
 												 *};
 				try {
 					MailSender.postMail(recs, "Test msg",
 							"Dude fuck's your Cisco!", "Anal@nosorog.net");
 					System.out.println("Service is offline");
 				} catch (MessagingException ex) {
 					ex.printStackTrace();
 				}
 			}
 			*/
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		coloredPrint(Message.COLOR_YELLOW, "~~~ To quit enter \"quit\" and press enter ~~~", Message.COLOR_WHITE);
 		while(true) {
 			String cmd = console.readLine();
 			if (cmd.equals("quit")) {
 				DAO.close();
 				System.exit(0);
 			} else {
 				coloredPrint(Message.COLOR_RED, "~~~ unknown command ~~~", Message.COLOR_WHITE);
 			}
 		}
 	}
 	
 	private static void coloredPrint(String ... args) {
 		StringBuilder message = new StringBuilder();
 		for (int i = 0; i < args.length; i++) {
 			message.append(args[i]);
 		}
 		System.out.println(message.toString());
 	}
 
 }
