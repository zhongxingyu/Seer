 package uk.co.harcourtprogramming.internetrelaycats;
 
 import java.util.logging.Level;
 
 /**
  * <p>An external service is a {@link BasicRelayCat} {@link Service} that runs in a
  * separate thread from the IRC interface.</p>
  * <p>Currently, it is unable to receive messages (although, they can be
  * forwarded with a {@link MessageService}</p>
  * <p>By default, the service runs in a daemon thread, with a default
  * {@link Thread.UncaughtExceptionHandler UncaughtExceptionHandler}</p>
  */
 public abstract class ExternalService extends Service implements Runnable
 {
 	/**
 	 * The thread that this Service will run in
 	 */
 	private final Thread t = new Thread(this);
 	/**
 	 * <p>Reference to the {@link BasicRelayCat} instance that is using this
 	 * service.</p>
 	 * <p>This only will be null until the instance starts the thread; this must
 	 * always be tested for.</p>
 	 */
	private final RelayCat inst;
 
 	/**
 	 * <p>Create the external service</p>
 	 * <p>The {@link #t thread} is created at this time, and will be {@link
 	 * Thread#start() started} when the {@link BasicRelayCat} {@link #inst
 	 * instance} is initialised.</p>
 	 * <p>External services can only be attached to one {@link BasicRelayCat}
 	 * instance; however, they still need to be added after creation with
 	 * {@link BasicRelayCat#addService(uk.co.harcourtprogramming.internetrelaycats.Service)
 	 * BasicRelayCat.addService}. Adding the service will cause the service's
 	 * thread to be run.</p>
 	 * @param inst the instance that this external service will work with
 	 */
 	public ExternalService(BasicRelayCat inst)
 	{
 		super();
 		t.setDaemon(true);
 		t.setName(this.getClass().getSimpleName() + '@' + this.getId());
 		t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
 
 			@Override
 			public void uncaughtException(Thread thread, Throwable thrwbl)
 			{
 				ExternalService.this.log(Level.SEVERE, "Uncaught Exception", thrwbl);
 			}
 		});
 		this.inst = inst;
 	}
 
 	/**
 	 * @return reference to the Thread object this service will {@link #run()
 	 * run} in.
 	 */
 	protected final Thread getThread()
 	{
 		return t;
 	}
 
 	/**
 	 * @return the instance that this service was created to serve
 	 */
 	protected final RelayCat getInstance()
 	{
 		return inst;
 	}
 }
 
