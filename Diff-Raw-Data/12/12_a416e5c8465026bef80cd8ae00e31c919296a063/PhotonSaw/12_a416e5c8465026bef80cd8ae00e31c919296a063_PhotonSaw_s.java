 package dk.osaa.psaw.core;
 
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.logging.Level;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 import dk.osaa.psaw.job.JobManager;
 import dk.osaa.psaw.machine.CommandReply;
 import dk.osaa.psaw.machine.Commander;
 import dk.osaa.psaw.machine.Move;
 import dk.osaa.psaw.machine.MoveVector;
 import dk.osaa.psaw.machine.Q30;
 import dk.osaa.psaw.config.Configuration;
 import dk.osaa.psaw.machine.PhotonSawCommandFailed;
 import dk.osaa.psaw.machine.ReplyTimeout;
 
 import lombok.Getter;
 import lombok.val;
 import lombok.extern.java.Log;
 
 /**
  * The main machine interface class that all the other bits hang off of. 
  * 
  * @author Flemming Frandsen <dren.dk@gmail.com> <http://dren.dk>
  */
 @Log
 public class PhotonSaw extends Thread implements PhotonSawAPI {
 	Configuration cfg;
 	Commander commander;
 	JobManager jobManager;
 	@Getter
 	Planner planner;
 	
 	public static final int MOVE_QUEUE_SIZE = 100;
 	
 	@Getter
 	ArrayBlockingQueue<Move> moveQueue = new ArrayBlockingQueue<Move>(MOVE_QUEUE_SIZE);
 	
 	public PhotonSaw(Configuration cfg) throws IOException, ReplyTimeout, NoSuchPortException, PortInUseException, UnsupportedCommOperationException, PhotonSawCommandFailed  {
 		this.cfg = cfg;
 		planner = new Planner(this);
 		commander = new Commander();
 		commander.connect(cfg.hostConfig.getSerialPort());
 		jobManager = new JobManager(cfg);
 		
 		setDaemon(true);
 		setName("PhotonSaw thread, keeps the hardware fed");				
 		
 		configureMotors();
 
 		planner.start();
 		this.start();	
 	}
 
 	boolean idle;
 	
 	public void putMove(Move move) throws InterruptedException {
 		moveQueue.put(move);
 		if (idle) {
 			this.interrupt(); // Wake up from sleep.
 		}
 	}	
 	
 	public void run() {
 		while (true) {
 			try {
 				commander.bufferMoves(moveQueue);
 				if (moveQueue.isEmpty()) {
 					idle = true;
 					try {
 						Thread.sleep(250); // Wait and see if another move becomes available					
 					} catch (InterruptedException e) {
 						// Never mind.
 					}			
 					idle = false;
 				}
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Caught exception while buffering moves", e);
 			}
 		}
 	}
 		
 	public CommandReply run(String cmd) throws IOException, ReplyTimeout, PhotonSawCommandFailed {		
 		log.fine("Running: "+cmd);
 		val r = commander.run(cmd);
 		if (r.get("result").isOk()) {
 			log.fine("command '"+cmd+"' worked: "+r);
 		} else {
 			log.severe("command '"+cmd+"' gave error: "+r);
 			throw new PhotonSawCommandFailed("command '"+cmd+"' gave error: "+r);
 		}
 		return r;
 	}
 		
 	private void configureMotors() throws IOException, ReplyTimeout, PhotonSawCommandFailed {
 		run("ai 10c"); // TODO: Ignore alarms while testing
 		run("ai 10c"); // TODO: Ignore alarms while testing
 
 		for (int i=0;i<Move.AXES;i++) {
 			run("me "+i+" "+
 					cfg.movementConstraints.getAxes()[i].coilCurrent+" "+
 					cfg.movementConstraints.getAxes()[i].microSteppingMode);
 		}
 	}
 
 	public Commander getCommander() {
 		return commander;
 	}
 
 	public boolean active() throws IOException, ReplyTimeout, PhotonSawCommandFailed {		
 		CommandReply r = run("st");
 		return r.get("motion.active").getBoolean();
 	}
 	
 	static XStream xstreamInstance = null;
 	public static XStream getStatusXStream() {
 		if (xstreamInstance == null) {
 			xstreamInstance = new XStream(new StaxDriver());
 			xstreamInstance.setMode(XStream.NO_REFERENCES);
 			
 			xstreamInstance.alias("status", PhotonSawStatus.class);
 		}
 		return xstreamInstance;
 	}
 
 	@Override
 	public PhotonSawStatus getStatus() {
 		PhotonSawStatus r = new PhotonSawStatus();
 		
 		r.setTimestamp(System.currentTimeMillis());
 		r.setHardwareStatus(commander.getLastReplyValues());
 		r.setLineBufferLength(planner.getLineBufferLength());
 		r.setLineBufferLengthTarget(LineBuffer.BUFFER_LENGTH);
 		r.setLineBufferSize(planner.getLineBufferCount());
 		r.setLineBufferSizeTarget(LineBuffer.BUFFER_SIZE);
 		r.setMoveBufferInUse(moveQueue.size());
 		r.setMoveBufferSize(MOVE_QUEUE_SIZE);
 		r.setJobLength(planner.getCurrentJobLength());
 		r.setJobSize(planner.getCurrentJobSize());
 		r.setJobRenderingProgressLineCount(planner.getRenderedLines());
 		
 		File rd = cfg.hostConfig.getRecordDir();
 		if (cfg.hostConfig.isRecording() && rd != null) {
 			File f = new File(rd, r.getTimestamp()+".psawstate");
 
 			try {
 				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
 				getStatusXStream().toXML(r, bos);
 				bos.close();
 			} catch (IOException e) {
 				log.log(Level.SEVERE, "Failed to store recorded status", e);
 			}
 		}		
 		
 		return r;
 	}
 
 	@Override
 	public JobManager getJobManager() {
 		return jobManager;
 	}
 
 	@Override
 	public void setJogSpeed(MoveVector direction) {
 		String cmd = "jv";
 		
 		for (int ax=0;ax<Move.AXES;ax++) {
 			double speed = cfg.movementConstraints.getAxes()[ax].maxSpeed;
 			if (speed > 100) {
 				speed = 100;
 			}
			Q30 s = new Q30(speed * direction.getAxis(ax) / cfg.movementConstraints.getAxes()[ax].mmPerStep / cfg.movementConstraints.getTickHZ());
 			cmd += " "+s.getLong();
 		}
 		try {
 			commander.run(cmd);
		} catch (Exception e) {
 			log.log(Level.SEVERE, "Failed to run jog command '"+cmd+"': ", e);
 		}		
 	}
 }
