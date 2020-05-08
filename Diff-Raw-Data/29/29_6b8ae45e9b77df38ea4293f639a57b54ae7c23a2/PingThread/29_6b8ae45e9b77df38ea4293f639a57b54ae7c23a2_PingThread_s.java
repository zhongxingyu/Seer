 package net.it_tim.dude_of_dude;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.TimerTask;
 
 import net.it_tim.dude_of_dude.database.*;
 import net.it_tim.dude_of_dude.static_constants.Message;
 
 public class PingThread extends TimerTask {
 
 	private Hosts host;
 	private boolean status = false;
 	private PingHistoryHome phh = new PingHistoryHome();
 	private PingHistory ph = new PingHistory();
 	private boolean last_status = false;
 	
 	public PingThread(Hosts host) {
 		super();
 		this.host = host;
 	}
 		
 	@Override
 	public synchronized void run() {
 			Ping ping;
			
 			ph = phh.getLastState(host);
			System.out.println(ph.getStatus());
			System.out.println(ph.getStamp());
 			last_status = ph.getStatus();
			
 			try {
 			long startTime = System.currentTimeMillis();
 
 			ping = new Ping(host.getIpAdres(), host.getTimeoutMs().intValue());
 
 			Integer timeOut = new Long(System.currentTimeMillis() - startTime).intValue();
 			if (ping.isOnline()) {
				formatedPrint(Message.SVC_UP, host.getDescription(), host.getIpAdres(), timeOut, Message.getDateTime());
 				status = true;
 				log(new Boolean(status), timeOut);
 			} else {
				formatedPrint(Message.SVC_DOWN, host.getDescription(), host.getIpAdres(), timeOut, Message.getDateTime());
 				status = false;
 				log(new Boolean(status), timeOut);
 			} 
 			
 			if (status != last_status){
				System.out.println(Message.COLOR_YELLOW + "Status has changed for host: " + Message.COLOR_RED + host.getDescription() + Message.COLOR_WHITE);
				last_status = status;
 			}
 			
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 	
 	private void log(Boolean status, Integer timeOut) {
 		PingHistory ph = new PingHistory();
 		ph.setHosts(host);
 		PingHistoryHome phh = new PingHistoryHome();
 		
 		ph.setStatus(status);
 		ph.setStamp(new Date());
 		ph.setTimeout(timeOut);
 		phh.log_ping(ph);
 	}
 	
 	private void formatedPrint(String format, Object... args) {
 		System.out.println(String.format(format, args));
 	}
 }
