 package com.vaguehope.loctxt;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import com.vaguehope.loctxt.vodafone.VodafoneAPI;
 
 public class Cron extends TimerTask {
 
 	private static final long DELAY = 10L * 1000L; // 10 seconds.
 	private static final long INTERVAL = 5L * 60L * 1000L; // 5 minutes.
 
 	protected static final Logger LOG = Logger.getLogger(Cron.class.getName());
 
 	private final Timer timer = new Timer();
 
 	public void start () {
 		this.timer.scheduleAtFixedRate(this, DELAY, INTERVAL);
 	}
 
 	public void dispose () {
 		this.timer.cancel();
 	}
 
 	@Override
 	public void run () {
 		LOG.info("CRON run");
 
		VodafoneAPI.sendSms("447824607574", "447824607574", "loctxt is working.");
 	}
 }
