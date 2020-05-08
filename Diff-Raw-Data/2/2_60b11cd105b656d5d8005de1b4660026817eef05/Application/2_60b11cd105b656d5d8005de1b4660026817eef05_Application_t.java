 package edu.teco.dnd.module;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.FunctionBlockID;
 import edu.teco.dnd.blocks.Input;
 import edu.teco.dnd.blocks.InputDescription;
 import edu.teco.dnd.blocks.Output;
 import edu.teco.dnd.blocks.OutputTarget;
 import edu.teco.dnd.module.ModuleBlockManager.BlockTypeHolderFullException;
 import edu.teco.dnd.module.ModuleBlockManager.NoSuchBlockTypeHolderException;
 import edu.teco.dnd.network.ConnectionManager;
 import edu.teco.dnd.util.HashStorage;
 import edu.teco.dnd.util.ValueWithHash;
 
 /**
  * This class represents a single application running on a module.
  * 
  * @author Marvin Marx
  * 
  */
 public class Application {
 	private static final Logger LOGGER = LogManager.getLogger(Application.class);
 
 	/** Time all shutdown hooks of an application have to run before being killed. */
 	public static final int TIME_BEFORE_ATTEMPTED_SHUTDOWNHOOK_KILL = 2000;
 	/** Additional time granted, for shutdownhooks after kill attempt, before thread is forcefully stopped. */
 	public static final int ADDITIONAL_TIME_BEFORE_FORCEFULL_KILL = 500;
 
 	/**
 	 * Current state of the application. Can only advance to the next state: the Application starts in CREATED, then
 	 * goes to RUNNING and eventually transitions to STOPPED.
 	 * 
 	 * @author Philipp Adolf
 	 */
 	private enum State {
 		CREATED, RUNNING, STOPPED
 	}
 
 	private final ApplicationID applicationID;
 	private final String name;
 	private final ScheduledThreadPoolExecutor scheduledThreadPool;
 	private final ConnectionManager connMan;
 	private final ModuleBlockManager moduleBlockManager;
 	private final HashStorage<byte[]> byteCodeStorage;
 
 	private State currentState = State.CREATED;
 	private final ReadWriteLock currentStateLock = new ReentrantReadWriteLock();
 
 	private final Set<FunctionBlockSecurityDecorator> scheduledToStart = new HashSet<FunctionBlockSecurityDecorator>();
 	private final Map<FunctionBlockSecurityDecorator, Map<String, String>> blockOptions =
 			new HashMap<FunctionBlockSecurityDecorator, Map<String, String>>();
 
 	/**
 	 * A Map from FunctionBlock UUID to matching ValueSender. Not used for local FunctionBlocks.
 	 */
 	private final ConcurrentMap<FunctionBlockID, ValueSender> valueSenders =
 			new ConcurrentHashMap<FunctionBlockID, ValueSender>();
 
 	private final ApplicationClassLoader classLoader;
 	/** mapping of active blocks to their ID, used e.g. to pass values to inputs. */
 	private final ConcurrentMap<FunctionBlockID, FunctionBlockSecurityDecorator> functionBlocksById =
 			new ConcurrentHashMap<FunctionBlockID, FunctionBlockSecurityDecorator>();
 
 	/**
 	 * 
 	 * @param applicationID
 	 *            UUID of this application
 	 * @param name
 	 *            Human readable name of this application
 	 * @param scheduledThreadPool
 	 *            a ThreadPool all tasks of this application will be sheduled in. Used to limit the amount of resources
 	 *            this App can allocate.
 	 * @param connMan
 	 *            ConnectionManager to send/receive messages.
 	 * @param classloader
 	 *            Class loader that will be used by this Application. Can be used to limit the privileges of this app.
 	 *            Also used to make loading classes over network possible.
 	 * @param module
 	 *            The module ApplicationManager used for callbacks to de/increase allowedBlockmaps
 	 * 
 	 */
 	public Application(final ApplicationID applicationID, final String name, final ConnectionManager connMan,
 			final ThreadFactory threadFactory, final int maxThreadsPerApp, final ModuleBlockManager moduleBlockManager,
 			final HashStorage<byte[]> byteCodeStorage) {
 		this.applicationID = applicationID;
 		this.name = name;
 		this.byteCodeStorage = byteCodeStorage;
 		this.classLoader = new ApplicationClassLoader();
 		this.scheduledThreadPool =
 				new ScheduledThreadPoolExecutor(maxThreadsPerApp, new ContextClassLoaderThreadFactory(threadFactory));
 		this.connMan = connMan;
 		this.moduleBlockManager = moduleBlockManager;
 	}
 
 	/**
 	 * Returns true if this Application is currently running.
 	 * 
 	 * @return true if this Application is currently running.
 	 */
 	public boolean isRunning() {
 		currentStateLock.readLock().lock();
 		try {
 			return currentState == State.RUNNING;
 		} finally {
 			currentStateLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Returns true if this Application has been shut down.
 	 * 
 	 * @return true if this Application has been shut down
 	 */
 	public boolean hasShutDown() {
 		currentStateLock.readLock().lock();
 		try {
 			return currentState == State.STOPPED;
 		} finally {
 			currentStateLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * called from this app, when a value is supposed to be send to another block (potentially on another ModuleInfo).
 	 * 
 	 * 
 	 * @param funcBlock
 	 *            the receiving functionBlock.
 	 * @param input
 	 *            the input on the given block to receive the message.
 	 * @param value
 	 *            the value to be send.
 	 */
 	private void sendValue(final FunctionBlockID funcBlock, final String input, final Serializable value) {
 		if (funcBlock == null) {
 			throw new IllegalArgumentException("funcBlock must not be null");
 		}
 		if (input == null) {
 			throw new IllegalArgumentException("input must not be null");
 		}
 		// sending null is allowed, as some FunctionBlocks may make use of it
 
 		if (hasFunctionBlockWithID(funcBlock)) { // block is local
 			try {
 				receiveValue(funcBlock, input, value);
 			} catch (NonExistentFunctionblockException e) {
 				// probably racecondition with app killing. Ignore.
 				LOGGER.trace(e);
 			} catch (NonExistentInputException e) {
 				LOGGER.trace("the given input {} does not exist on the local functionBlock {}", input, funcBlock);
 			}
 		} else {
 			getValueSender(funcBlock).sendValue(input, value);
 		}
 	}
 
 	/**
 	 * Returns a ValueSender for the given target FunctionBlock. If no ValueSender for that FunctionBlock exists yet a
 	 * new one is created. When called with the same UUID it will always return the same ValueSender, even if called
 	 * concurrently in different Threads.
 	 * 
 	 * @param funcBlock
 	 *            the UUID of the FunctionBlock for which a ValueSender should be returned
 	 * @return the ValueSender for the given FunctionBlock
 	 */
 	// FIXME: Need a way to clean up old value senders
 	private ValueSender getValueSender(final FunctionBlockID funcBlock) {
 		ValueSender valueSender = valueSenders.get(funcBlock);
 		if (valueSender == null) {
 			valueSender = new ValueSender(applicationID, funcBlock, connMan);
 			// if between the get and this call another Thread put a ValueSender into the map, this call will return the
 			// ValueSender the other
 			// Thread put into the Map. We'll use that one instead of our new one so that only one ValueSender exists
 			// per target
 			ValueSender oldValueSender = valueSenders.putIfAbsent(funcBlock, valueSender);
 			if (oldValueSender != null) {
 				valueSender = oldValueSender;
 			}
 		}
 		return valueSender;
 	}
 
 	/**
 	 * loads a class into this app.
 	 * 
 	 * @param classname
 	 *            name of the class to load
 	 * @param classData
 	 *            bytecode of the class to be loaded
 	 */
 	public void loadClass(String classname, byte[] classData) {
 		if (classname == null || classData == null) {
 			throw new IllegalArgumentException("classname and classdata must not be null.");
 		}
 
 		final ValueWithHash<byte[]> byteCode = byteCodeStorage.putIfAbsent(classData);
 		classLoader.addClass(classname, byteCode.getValue());
 	}
 
 	/**
 	 * Schedules a block in this application to be executed, once Application.start() is called.
 	 * 
 	 * @param blockDescription
 	 *            which block to schedule.
 	 * @throws ClassNotFoundException
 	 *             if the class given is not known by the Classloader of this application
 	 * @throws UserSuppliedCodeException
 	 *             if some part of the code of the functionBlock (e.g. constructor) does throw an exception or otherwise
 	 *             misbehave (e.g. System.exit(),...)
 	 * @throws NoSuchBlockTypeHolderException
 	 * @throws BlockTypeHolderFullException
 	 * @throws IllegalArgumentException
 	 *             if blockDescription.blockClassName is not a function block.
 	 */
 	public void scheduleBlock(final BlockDescription blockDescription) throws ClassNotFoundException,
 			UserSuppliedCodeException, BlockTypeHolderFullException, NoSuchBlockTypeHolderException {
 		LOGGER.entry(blockDescription);
 		currentStateLock.readLock().lock();
 		try {
 			if (hasShutDown()) {
 				throw LOGGER.throwing(new IllegalStateException(this + " has already been stopped"));
 			}
 
 			final FunctionBlockSecurityDecorator securityDecorator =
 					createFunctionBlockSecurityDecorator(blockDescription.blockClassName);
 			LOGGER.trace("calling doInit on securityDecorator {}", securityDecorator);
 			securityDecorator.doInit(blockDescription.blockID, blockDescription.blockName);
 
 			if (LOGGER.isTraceEnabled()) {
 				LOGGER.trace("adding {} to ID {}", securityDecorator, blockDescription.blockTypeHolderId);
 			}
 			moduleBlockManager.addToBlockTypeHolders(applicationID, securityDecorator,
 					blockDescription.blockTypeHolderId);
 
 			if (LOGGER.isTraceEnabled()) {
 				LOGGER.trace("initializing outputs {} on {}", blockDescription.outputs, securityDecorator);
 			}
 			initializeOutputs(securityDecorator, blockDescription.outputs);
 
 			if (isRunning()) {
 				startBlock(securityDecorator, blockDescription.options);
 			} else {
 				synchronized (scheduledToStart) {
 					LOGGER.trace("adding {} to scheduledToStart");
 					scheduledToStart.add(securityDecorator);
 					LOGGER.trace("saving block options");
 					blockOptions.put(securityDecorator, blockDescription.options);
 				}
 			}
 		} finally {
 			currentStateLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Wraps a functionBlock (given by name) into a security decorator (see FunctionBlockSecurityDecorator) for the
 	 * rationale.
 	 * 
 	 * @param className
 	 *            name of the class to wrap
 	 * @return a new FunctionBlockSecurityDecorator wrapping the given block.
 	 * @throws ClassNotFoundException
 	 *             if the classloader can not find a class with this name.
 	 * @throws UserSuppliedCodeException
 	 *             If the given class misbehaves during initialization (throws errors...)
 	 * @throws IllegalArgumentException
 	 *             inf className is not a functionBlock.
 	 */
 	@SuppressWarnings("unchecked")
 	private FunctionBlockSecurityDecorator createFunctionBlockSecurityDecorator(final String className)
 			throws ClassNotFoundException, UserSuppliedCodeException, IllegalArgumentException {
 		Class<?> cls = null;
 		cls = classLoader.loadClass(className);
 		if (!FunctionBlock.class.isAssignableFrom(cls)) {
 			throw new IllegalArgumentException("class " + className + " is not a FunctionBlock");
 		}
 		return new FunctionBlockSecurityDecorator((Class<? extends FunctionBlock>) cls);
 	}
 
 	/**
 	 * Initializes the outputs used for sending values on a functionBlock.
 	 * 
 	 * @param securityDecorator
 	 *            the SecurityDecorator holding the block with the outputs to set.
 	 * @param outputs
 	 *            the outputs to set on the Block.
 	 */
 	private void initializeOutputs(final FunctionBlockSecurityDecorator securityDecorator,
 			final Map<String, Set<InputDescription>> outputs) {
 		final Map<String, Output<? extends Serializable>> blockOutputs = securityDecorator.getOutputs();
 		for (final Entry<String, Set<InputDescription>> output : outputs.entrySet()) {
 			if (!blockOutputs.containsKey(output.getKey())) {
 				continue;
 			}
 			final Output<? extends Serializable> blockOutput = blockOutputs.get(output.getKey());
 			blockOutput.setTarget(new ApplicationOutputTarget(output.getValue()));
 		}
 	}
 
 	/**
 	 * starts this application, as in: starts executing the previously scheduled blocks.
 	 */
 	public void start() {
 		currentStateLock.readLock().lock();
 		try {
 			if (currentState != State.CREATED) {
 				throw LOGGER.throwing(new IllegalStateException("Tried to start " + this + " while it was in State "
 						+ currentState));
 			}
 		} finally {
 			currentStateLock.readLock().unlock();
 		}
 
 		currentStateLock.writeLock().lock();
 		try {
 			if (currentState != State.CREATED) {
 				throw LOGGER.throwing(new IllegalStateException("Tried to start " + this + " while it was in State "
 						+ currentState));
 			}
 
 			currentState = State.RUNNING;
 
 			synchronized (scheduledToStart) {
 				for (final FunctionBlockSecurityDecorator func : scheduledToStart) {
 					startBlock(func, blockOptions.get(func));
 				}
 
 				scheduledToStart.clear();
 				blockOptions.clear();
 			}
 		} finally {
 			currentStateLock.writeLock().unlock();
 		}
 	}
 
 	/**
 	 * starts the given function block on the Module. Also triggers removing it from runnable blocks
 	 * 
 	 * @param block
 	 *            the block to be started.
 	 */
 	private void startBlock(final FunctionBlockSecurityDecorator block, final Map<String, String> options) {
 		currentStateLock.readLock().lock();
 		try {
 			if (hasShutDown()) {
 				throw LOGGER.throwing(new IllegalStateException(this + " has already been shut down"));
 			}
 
 			final Runnable initRunnable = new Runnable() {
 				@Override
 				public void run() {
 					try {
 						block.init(options);
 					} catch (UserSuppliedCodeException e) {
 						// TODO: handle malevolent block. Stop it, maybe?
 					}
 				}
 			};
 			final Runnable updater = new Runnable() {
 				@Override
 				public void run() {
 					try {
 						block.update();
 					} catch (UserSuppliedCodeException e) {
 						// TODO: handle malevolent block. Stop it, maybe?
 					}
 				}
 			};
 
 			final Future<?> initFuture = scheduledThreadPool.submit(initRunnable);
			while (!initFuture.isDone()) {
 				try {
 					initFuture.get();
 				} catch (final InterruptedException e) {
 					LOGGER.debug("got interrupted waiting for init future of {}", block);
 				} catch (final ExecutionException e) {
 					LOGGER.catching(e);
 					return;
 				}
 			}
 
 			// FIXME: if two blocks share the UUID, blocks get lost
 			functionBlocksById.put(block.getBlockID(), block);
 
 			long period = block.getUpdateInterval();
 			try {
 				if (period < 0) {
 					scheduledThreadPool.schedule(updater, 0, TimeUnit.SECONDS);
 				} else {
 					scheduledThreadPool.scheduleAtFixedRate(updater, period, period, TimeUnit.MILLISECONDS);
 				}
 			} catch (RejectedExecutionException e) {
 				LOGGER.catching(e);
 			}
 		} finally {
 			currentStateLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * passes a received value the given input of a local block.
 	 * 
 	 * @param funcBlockId
 	 *            Id of the block to pass the message to.
 	 * @param value
 	 *            the value to give to the input.
 	 * @param inputName
 	 *            name of the input this value is directed to.
 	 * @throws NonExistentFunctionblockException
 	 *             If the FunctionBlock is not being executed by this module.
 	 * @throws NonExistentInputException
 	 *             If the FunctionBlock is being executed but does not have an input of said name.
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void receiveValue(final FunctionBlockID funcBlockId, String inputName, Serializable value)
 			throws NonExistentFunctionblockException, NonExistentInputException {
 		currentStateLock.readLock().lock();
 		try {
 			if (isRunning()) {
 				throw new IllegalStateException(this + " is not running");
 			}
 
 			final FunctionBlockSecurityDecorator block = functionBlocksById.get(funcBlockId);
 			if (block == null) {
 				throw LOGGER.throwing(new NonExistentFunctionblockException(funcBlockId.toString()));
 			}
 
 			final Input input = block.getInputs().get(inputName);
 			if (input == null) {
 				throw LOGGER.throwing(new NonExistentInputException("FunctionBlock " + funcBlockId
 						+ " does not have an input called " + inputName));
 			}
 			input.setValue(value);
 
 			final Runnable updater = new Runnable() {
 				@Override
 				public void run() {
 					try {
 						block.update();
 					} catch (UserSuppliedCodeException e) {
 						// TODO: handle malevolent block. Stop it, maybe?
 					}
 				}
 			};
 
 			try {
 				scheduledThreadPool.schedule(updater, 0, TimeUnit.SECONDS);
 			} catch (RejectedExecutionException e) {
 				LOGGER.catching(e);
 			}
 		} finally {
 			currentStateLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Called when this application is shut down. Will call the appropriate methods on the executed functionBlocks.
 	 */
 	@SuppressWarnings("deprecation")
 	public void shutdown() {
 		currentStateLock.readLock().lock();
 		try {
 			if (currentState != State.RUNNING) {
 				throw LOGGER.throwing(new IllegalArgumentException(this + " is not currently running"));
 			}
 		} finally {
 			currentStateLock.readLock().unlock();
 		}
 
 		currentStateLock.writeLock().lock();
 		try {
 			if (currentState != State.RUNNING) {
 				throw LOGGER.throwing(new IllegalArgumentException(this + " is not currently running"));
 			}
 
 			scheduledThreadPool.shutdown();
 
 			final Thread shutdownThread = new Thread() {
 				@Override
 				public void run() {
 					for (final FunctionBlockSecurityDecorator block : functionBlocksById.values()) {
 						if (Thread.interrupted()) {
 							LOGGER.warn("shutdownThread got interrupted, not shutting down remaining FunctionBlocks");
 							break;
 						}
 						try {
 							block.shutdown();
 						} catch (UserSuppliedCodeException e) {
 							LOGGER.catching(e);
 						}
 					}
 				}
 			};
 			shutdownThread.start();
 
 			sleepUninterrupted(TIME_BEFORE_ATTEMPTED_SHUTDOWNHOOK_KILL);
 			if (!shutdownThread.isAlive()) {
 				LOGGER.debug("shutdownThread finished in time");
 				return;
 			}
 			LOGGER.info("shutdownThread is taking too long. Interrupting it.");
 			shutdownThread.interrupt();
 
 			sleepUninterrupted(ADDITIONAL_TIME_BEFORE_FORCEFULL_KILL);
 			if (!shutdownThread.isAlive()) {
 				LOGGER.debug("shutdownThread finished in time after interrupting");
 				return;
 			}
 			LOGGER.warn("Shutdown thread hanging. Killing it.");
 			shutdownThread.stop();
 			// It's deprecated and dangerous to stop a thread like this, because it forcefully releases all locks,
 			// yet there is no alternative to it if the victim is refusing to cooperate.
 		} finally {
 			currentStateLock.writeLock().unlock();
 		}
 	}
 
 	/**
 	 * 
 	 * @return the UUID of this application
 	 */
 	public ApplicationID getApplicationID() {
 		return applicationID;
 	}
 
 	/**
 	 * 
 	 * @return the human readable name of this application.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * 
 	 * @return the classloader this application uses.
 	 */
 	public ApplicationClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	/**
 	 * 
 	 * @return the threadpool this application uses.
 	 */
 	public ScheduledThreadPoolExecutor getThreadPool() {
 		return scheduledThreadPool;
 	}
 
 	public Map<FunctionBlockID, FunctionBlockSecurityDecorator> getFunctionBlocksById() {
 		return new HashMap<FunctionBlockID, FunctionBlockSecurityDecorator>(functionBlocksById);
 	}
 
 	public boolean hasFunctionBlockWithID(FunctionBlockID blockId) {
 		return functionBlocksById.containsKey(blockId);
 	}
 
 	// TODO insert javadoc here.
 	class ApplicationOutputTarget implements OutputTarget<Serializable> {
 		private final Set<InputDescription> destinations;
 
 		/**
 		 * 
 		 * @param destinations
 		 *            places connected to this output. Where values are supposed to be send when they are send.
 		 */
 		public ApplicationOutputTarget(final Collection<InputDescription> destinations) {
 			this.destinations = new HashSet<InputDescription>(destinations);
 		}
 
 		@Override
 		public void setValue(Serializable value) {
 			for (final InputDescription destination : destinations) {
 				sendValue(destination.getBlock(), destination.getInput(), value);
 			}
 		}
 	}
 
 	private class ContextClassLoaderThreadFactory implements ThreadFactory {
 		private final ThreadFactory internalFactory;
 
 		private ContextClassLoaderThreadFactory(final ThreadFactory internalFactory) {
 			this.internalFactory = internalFactory;
 		}
 
 		@Override
 		public Thread newThread(final Runnable r) {
 			final Thread thread = internalFactory.newThread(r);
 			thread.setContextClassLoader(classLoader);
 			return thread;
 		}
 
 	}
 
 	/**
 	 * Behaves like a Thread.sleep(millisToSleep), with the exception, that all InterruptedExceptions are disregarded
 	 * (=have no influence on sleep time and are dropped)
 	 * 
 	 * @param millisToSleep
 	 *            time to sleep in milli seconds.
 	 */
 	private void sleepUninterrupted(long millisToSleep) {
 		long sleepTill = System.currentTimeMillis() + millisToSleep;
 		long timeLeftToSleep = millisToSleep;
 		while (timeLeftToSleep > 0) {
 			try {
 				Thread.sleep(timeLeftToSleep);
 				break;
 			} catch (InterruptedException e) {
 				timeLeftToSleep = sleepTill - System.currentTimeMillis();
 			}
 		}
 	}
 }
