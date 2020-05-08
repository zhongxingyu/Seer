 package net.it_tim.dude_of_dude;
 
 import java.io.IOException;
 import java.util.TimerTask;
 
 import net.it_tim.dude_of_dude.database.*;
 import net.it_tim.dude_of_dude.static_constants.Tools;
 
 public class PingThread extends TimerTask {
 
 	private Hosts host;
 	private boolean status = false;
 	private boolean packetLoss = false;
 	private PingHistoryHome phh = new PingHistoryHome();
 	private PingHistory pinghistory = new PingHistory();
 	private boolean last_status = true;
 	private int down_count = 0;
 
 	public PingThread(Hosts host) {
 		super();
 		this.host = host;
 
 		pinghistory = phh.getLastState(host);
 		if (pinghistory != null) {
 			last_status = pinghistory.getStatus().booleanValue();
 		} else {
 			last_status = true;
 		}

 	}
 
 	@Override
 	public void run() {
 		ping();
 	}
 
 	private void formatedPrint(String format, Object... args) {
 		System.out.println(String.format(format, args));
 	}
 
 	private void ping() {
 		try {
 
 			Ping ping = new Ping(host.getIpAdres(), host.getTimeoutMs()
 					.intValue(), host.getPacketLoss());
 			status = ping.isOnline();
 			packetLoss = ping.isPacketLoss();
 
 			if ((!status || packetLoss) && down_count < host.getDownCount() && last_status) {
 				down_count++;
 				System.out
 						.println(down_count
 								+ " down! unreported. Aval. downs: "
 								+ host.getDownCount() + " for "
 								+ host.getDescription());
 				return;
 			}
 
 			Integer timeOut = ping.getTimeout();
 			SyncronizedMethods.log(host, new Boolean(status), timeOut);
 			down_count = 0;
 
 			if (status != last_status) {
 				if (status) {
 					last_status = true;
 					String message = String.format(Tools.MAIL_UP, host
 							.getDescription(), host.getIpAdres(), timeOut,
 							Tools.getDateTime());
 					formatedPrint(Tools.SVC_UP, host.getDescription(), host
 							.getIpAdres(), timeOut, Tools.getDateTime());
 					SyncronizedMethods.notificate(host, status);
 					SyncronizedMethods.sendMail(host, message);
 				} else {
 					last_status = false;
 					String message = new String();
 					if (packetLoss) {
 						message = String.format(Tools.MAIL_P_LOSS, host
 								.getDescription(), host.getIpAdres(), ping
 								.getPacketLoss(), Tools.getDateTime());
 						formatedPrint(Tools.SVC_P_LOSS, host.getDescription(),
 								host.getIpAdres(), ping.getPacketLoss(), Tools
 										.getDateTime());
 					} else {
 						message = String.format(Tools.MAIL_DOWN, host
 								.getDescription(), host.getIpAdres(), timeOut,
 								Tools.getDateTime());
 						formatedPrint(Tools.SVC_DOWN, host.getDescription(),
 								host.getIpAdres(), timeOut, Tools.getDateTime());
 					}
 					SyncronizedMethods.notificate(host, status);
 					SyncronizedMethods.sendMail(host, message);
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 	}
 
 }
