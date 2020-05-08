 /*
  License:
 
  blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
  (http://www.eclipse.org/legal/epl-v10.html)
 
 
  Distribution:
 
  Repository - https://github.com/lempel/blueprint-sdk.git
  Blog - http://lempel.egloos.com
  */
 
 package blueprint.sdk.experimental.aio;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
import blueprint.sdk.core.concurrent.JobQueue;
 import blueprint.sdk.core.concurrent.TimeoutHandler;
 import blueprint.sdk.core.concurrent.WorkerGroup;
 import blueprint.sdk.experimental.aio.session.Session;
 import blueprint.sdk.logger.Logger;
 import blueprint.sdk.util.Terminatable;
 import blueprint.sdk.util.Validator;
 import blueprint.sdk.util.jvm.shutdown.Terminator;
 
 /**
  * Proactor for Service
  * 
  * @author Sangmin Lee
  * @since 2008. 11. 25.
  */
 public class Proactor implements Terminatable {
 	private static final Logger LOGGER = Logger.getInstance();
 
 	private transient final SelectorLoadBalancer readSelectorLB;
 	private transient final List<ReadThread> readThreads;
 	private transient final WorkerGroup reactors;
 	/** map of Sessions (to SocketChannel's hashcode) */
 	private transient final ConcurrentHashMap<Integer, Session> sessionMap;
 	private final Constructor<? extends Session> sessionCons;
 
 	/** I/O buffer size in byte */
 	private final int bufferSize;
 	private transient TimeoutHandler timeoutHandler = null;
 
 	private transient boolean terminated = false;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param reactorClass
 	 *            Reactor's class
 	 * @param reactorCount
 	 *            number of reactors to use
 	 * @param readerCount
 	 *            number of reader thread to use
 	 * @param sessionClass
 	 *            an implementation Class of Session
 	 * @param readBufferSize
 	 *            read buffer size in byte
 	 * @throws IOException
 	 *             Failed to open a Selector
 	 * @throws IllegalArgumentException
 	 *             Thrown by WorkerGroup
 	 * @throws SecurityException
 	 *             Thrown by WorkerGroup
 	 * @throws InstantiationException
 	 *             Thrown by WorkerGroup
 	 * @throws IllegalAccessException
 	 *             Thrown by WorkerGroup
 	 * @throws InvocationTargetException
 	 *             Thrown by WorkerGroup
 	 * @throws NoSuchMethodException
 	 *             Thrown by WorkerGroup
 	 */
 	public Proactor(final Class<Reactor> reactorClass, final int reactorCount, final int readerCount,
 			final Class<? extends Session> sessionClass, final int readBufferSize) throws IOException,
 			IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		if (readerCount == 0) {
 			throw new IllegalArgumentException("readerThreads must be greater than 0");
 		}
 		LOGGER.info(this, "creating proactor with " + readerCount + " read threads");
 
 		bufferSize = readBufferSize;
 		sessionMap = new ConcurrentHashMap<Integer, Session>();
 		sessionCons = sessionClass.getConstructor(SocketChannel.class, ConcurrentHashMap.class, Integer.class,
 				SelectorLoadBalancer.class);
 
 		List<Selector> selectors = new ArrayList<Selector>(readerCount);
 		for (int i = 0; i < readerCount; i++) {
 			selectors.add(SelectorFactory.get());
 		}
 		readSelectorLB = new SelectorLoadBalancer(selectors);
 
 		readThreads = new ArrayList<ReadThread>(readerCount);
 		for (int i = 0; i < readerCount; i++) {
 			ReadThread thread = new ReadThread(selectors.get(i));
 			thread.start();
 			readThreads.add(thread);
 		}
 
		reactors = new WorkerGroup(new JobQueue<Object>(), reactorClass, reactorCount);
 
 		Terminator term = Terminator.getInstance();
 		term.register(this);
 
 		LOGGER.info(this, "proactor created with " + readerCount + " read threads");
 	}
 
 	protected boolean accept(final SocketChannel channel) {
 		Session session = null;
 		try {
 			session = (Session) sessionCons.newInstance(channel, sessionMap, bufferSize, readSelectorLB);
 		} catch (IllegalArgumentException e) {
 			LOGGER.error(e);
 			LOGGER.trace(e);
 		} catch (InstantiationException e) {
 			LOGGER.error(e);
 			LOGGER.trace(e);
 		} catch (IllegalAccessException e) {
 			LOGGER.error(e);
 			LOGGER.trace(e);
 		} catch (InvocationTargetException e) {
 			LOGGER.error(e);
 			LOGGER.trace(e);
 		}
 
 		boolean result = false;
 
 		if (Validator.isNotNull(session)) {
 			LOGGER.debug(this, "registering a session - " + session);
 
 			SocketChannelWrapper wrapper = session.getWrapper();
 
 			try {
 				wrapper.configureBlocking(false);
 
 				LOGGER.debug(this, "a session is registered - " + session);
 
 				if (Validator.isNotNull(timeoutHandler)) {
 					wrapper.setTimeoutHandler(timeoutHandler);
 				}
 
 				sessionMap.put(channel.hashCode(), session);
 
 				reactors.addJob(session);
 			} catch (ClosedChannelException e) {
 				LOGGER.debug(this, "a channel is failed to register - " + session);
 			} catch (Exception e) {
 				LOGGER.debug(this, "a channel is failed to register - " + session);
 				LOGGER.error(e);
 				LOGGER.trace(e);
 			}
 
 			result = true;
 		}
 
 		return result;
 	}
 
 	public boolean isValid() {
 		return true;
 	}
 
 	public boolean isTerminated() {
 		return terminated;
 	}
 
 	public void terminate() {
 		LOGGER.info(this, "terminating proactor");
 
 		reactors.terminate();
 
 		{
 			Iterator<ReadThread> iter = null;
 			iter = readThreads.iterator();
 			while (iter.hasNext()) {
 				iter.next().terminate();
 			}
 			readThreads.clear();
 		}
 
 		readSelectorLB.terminate();
 
 		{
 			Iterator<Session> iter = sessionMap.values().iterator();
 			while (iter.hasNext()) {
 				iter.next().terminate();
 			}
 			sessionMap.clear();
 		}
 
 		terminated = true;
 
 		LOGGER.info(this, "proactor teminated");
 	}
 
 	public TimeoutHandler getTimeoutHandler() {
 		return timeoutHandler;
 	}
 
 	public void setTimeoutHandler(final TimeoutHandler timeoutHandler) {
 		this.timeoutHandler = timeoutHandler;
 	}
 
 	public int getBufferSize() {
 		return bufferSize;
 	}
 
 	/**
 	 * select & read
 	 * 
 	 * @author Sangmin Lee
 	 * @version $Revision$
 	 * @create 2008. 11. 26.
 	 * @since 1.5
 	 * @last $Date$
 	 * @see
 	 */
 	private class ReadThread extends SelectThread {
 		public ReadThread(Selector sel) {
 			super(sel);
 		}
 
 		protected void process(final SelectionKey key) {
 			try {
 				if (key != null && key.isReadable() && key.channel() instanceof SocketChannel) {
 					SocketChannel channel = (SocketChannel) key.channel();
 					if (Validator.isValid(channel)) {
 						Session ses = sessionMap.get(channel.hashCode());
 						if (ses == null) {
 							LOGGER.error("a session can't be found in SessionMap. It shouldn't happen!");
 						} else {
 							if (ses.isValid()) {
 								reactors.addJob(ses);
 							} else {
 								sessionMap.remove(channel.hashCode()).terminate();
 							}
 						}
 					}
 				}
 			} catch (CancelledKeyException ignored) {
 			} catch (Exception e) {
 				LOGGER.error(this, "read failed");
 				LOGGER.trace(e);
 			}
 		}
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		readThreads.clear();
 		sessionMap.clear();
 
 		super.finalize();
 	}
 }
