 package no.uio.master.autoscale.agent;
 
 import java.io.IOException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 
 import no.uio.master.autoscale.agent.config.Config;
 import no.uio.master.autoscale.agent.service.AutoscaleAgentServer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Initial startup of autocale-agent implementation.<br>
  * This class is only used for initial setup of the daemon.
  * @author andreas
  *
  */
 public class AutoscaleAgent {
 	private static Logger LOG = LoggerFactory.getLogger(AutoscaleAgent.class);
 
 	private static ScheduledExecutorService executor;
 	private static AutoscaleAgentServer server;
 	
 	private static int INTERVALL_TIMER = Config.intervall_timer;
 	
 	
 	public static void main(String[] args) {
		LOG.info("Autoscale agent invoked...");
 		try {
 			server = new AutoscaleAgentServer();
 		} catch (IOException e) {
 			LOG.error("Failed to initialize agent server");
 		}
 		executor = Executors.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(server, 0, INTERVALL_TIMER, TimeUnit.SECONDS);
 		LOG.debug("Invoked");
 	}
 	
 }
