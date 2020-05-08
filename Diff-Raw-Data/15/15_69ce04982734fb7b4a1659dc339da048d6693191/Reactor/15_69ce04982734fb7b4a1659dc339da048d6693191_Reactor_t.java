 /*
  License:
 
  blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
  (http://www.eclipse.org/legal/epl-v10.html)
 
 
  Distribution:
 
  Repository - https://github.com/lempel/blueprint-sdk.git
  Blog - http://lempel.egloos.com
  */
 
 package blueprint.sdk.experimental.aio;
 
 import java.nio.channels.ClosedChannelException;
 
 import blueprint.sdk.core.concurrent.JobQueue;
 import blueprint.sdk.core.concurrent.Worker;
 import blueprint.sdk.experimental.aio.session.Session;
 import blueprint.sdk.logger.Logger;
 import blueprint.sdk.util.Validator;
 
 /**
  * Process each & every element from JobQueue, one at a time.
  * 
  * @author Sangmin Lee
  * @since 2008. 11. 26.
  */
 public class Reactor extends Worker<Object> {
 	private static final Logger LOGGER = Logger.getInstance();
 
 	private transient boolean running = false;
 
	public Reactor(final JobQueue<Object> jobQueue, final Object deathMonitor) {
		super(jobQueue, deathMonitor);
 	}
 
 	public boolean isValid() {
 		return running;
 	}
 
 	public void terminate() {
 		running = false;
 	}
 
 	@Override
 	protected void process(final Object clientObject) {
 		if (clientObject instanceof Session) {
 			Session ses = (Session) clientObject;
 			try {
 				ses.process();
 			} catch (ClosedChannelException ignored) {
 				if (Validator.isNotNull(ses)) {
 					ses.terminate();
 				}
 			} catch (Exception e) {
 				if (Validator.isNotNull(ses)) {
 					ses.terminate();
 				}
 				LOGGER.error(e);
 				LOGGER.trace(e);
 			}
 		} else {
 			LOGGER.error(this, "wrong object in job queue - " + clientObject);
 		}
 	}
 }
