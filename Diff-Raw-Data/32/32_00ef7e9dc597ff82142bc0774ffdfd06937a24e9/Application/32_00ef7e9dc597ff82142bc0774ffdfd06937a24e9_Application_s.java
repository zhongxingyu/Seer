 package edu.teco.dnd.module;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.Input;
 import edu.teco.dnd.blocks.Output;
 import edu.teco.dnd.blocks.OutputTarget;
 import edu.teco.dnd.blocks.ValueDestination;
 import edu.teco.dnd.network.ConnectionManager;
 
 /**
  * This class represents a single application running on a module.
  * 
  * @author Marvin Marx
  * 
  */
 public class Application {
 
 	/** Time all shutdown hooks of an application have to run before being killed. */
 	public static final int TIME_BEFORE_ATTEMPTED_SHUTDOWNHOOK_KILL = 2000;
 	/** Additional time granted, for shutdownhooks after kill attempt, before thread is forcefully stopped. */
 	public static final int ADDITIONAL_TIME_BEFORE_FORCEFULL_KILL = 500;
 	private final UUID ownAppId;
 	private final String name;
 	private final ReadWriteLock shutdownLock = new ReentrantReadWriteLock();
 	private final ScheduledThreadPoolExecutor scheduledThreadPool;
 	private final ConnectionManager connMan;
 	private final ModuleApplicationManager moduleApplicationManager;
 
 	private static final Logger LOGGER = LogManager.getLogger(Application.class);
 	private final Set<FunctionBlockSecurityDecorator> scheduledToStart = new HashSet<FunctionBlockSecurityDecorator>();
 	private final Map<FunctionBlockSecurityDecorator, Map<String, String>> blockOptions =
 			new HashMap<FunctionBlockSecurityDecorator, Map<String, String>>();
 	private boolean isRunning = false;
 
 	/**
 	 * A Map from FunctionBlock UUID to matching ValueSender. Not used for local FunctionBlocks.
 	 */
 	private final ConcurrentMap<UUID, ValueSender> valueSenders = new ConcurrentHashMap<UUID, ValueSender>();
 
 	private final ApplicationClassLoader classLoader;
 	/** mapping of active blocks to their ID, used e.g. to pass values to inputs. */
 	private final Map<UUID, FunctionBlockSecurityDecorator> funcBlockById;
 
 	/**
 	 * @return all blocks, this app is currently executing.
 	 */
 	public Collection<FunctionBlockSecurityDecorator> getAllBlocks() {
 		return funcBlockById.values();
 	}
 
 	/**
 	 * 
 	 * @param appId
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
 	 * @param moduleApplicationManager
 	 *            The module ApplicationManager used for callbacks to de/increase allowedBlockmaps
 	 * 
 	 */
 	// TODO: factor blockTypeholder mapping out of ModuleAppManager so as to not have to pass the whole class to this
 	// class.
 
 	public Application(UUID appId, String name, ScheduledThreadPoolExecutor scheduledThreadPool,
 			ConnectionManager connMan, ApplicationClassLoader classloader,
 			final ModuleApplicationManager moduleApplicationManager) {
 		this.ownAppId = appId;
 		this.name = name;
 		this.scheduledThreadPool = scheduledThreadPool;
 		this.connMan = connMan;
 		this.classLoader = classloader;
 		this.funcBlockById = new HashMap<UUID, FunctionBlockSecurityDecorator>();
 		this.moduleApplicationManager = moduleApplicationManager;
 	}
 
 	/**
 	 * called from this app, when a value is supposed to be send to another block (potentially on another Module).
 	 * 
 	 * 
 	 * @param funcBlock
 	 *            the receiving functionBlock.
 	 * @param input
 	 *            the input on the given block to receive the message.
 	 * @param value
 	 *            the value to be send.
 	 */
 	public void sendValue(final UUID funcBlock, final String input, final Serializable value) {
 		if (funcBlock == null) {
 			throw new IllegalArgumentException("funcBlock must not be null");
 		}
 		if (input == null) {
 			throw new IllegalArgumentException("input must not be null");
 		}
 		// sending null is allowed, as some FunctionBlocks may make use of it
 
 		// FIXME: do sanitizing.
 		// doublecheck arguments because this is the only function callable from userspace, that has enhanced
 		// privileges.
 
 		sanitizedSendValue(funcBlock, input, value);
 	}
 
 	/**
 	 * Called by sendValue after the arguments have been properly sanitized to make sure there is no harmfull code in
 	 * them. Function is a way for userApplicationCode to be given advanced privileges. <br>
 	 * <b>Make sure to have doublecheck every Argument</b>
 	 * 
 	 * @param funcBlock
 	 *            see sendValue but sanitized
 	 * @param input
 	 *            see sendValue but sanitized
 	 * @param value
 	 *            see sendValue but sanitized
 	 */
 	private void sanitizedSendValue(final UUID funcBlock, final String input, final Serializable value) {
 
 		if (isExecuting(funcBlock)) { // block is local
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
 	private ValueSender getValueSender(final UUID funcBlock) {
 		ValueSender valueSender = valueSenders.get(funcBlock);
 		if (valueSender == null) {
 			valueSender = new ValueSender(ownAppId, funcBlock, connMan);
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
 		if (!shutdownLock.readLock().tryLock()) {
 			throw new IllegalStateException("App already shuting down");
 		}
 
 		try {
 			if (classname == null || classData == null) {
 				throw new IllegalArgumentException("classname and classdata must not be null.");
 			}
 			classLoader.appLoadClass(classname, classData);
 		} finally {
 			shutdownLock.readLock().unlock();
 		}
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
 	 */
 	public void scheduleBlock(final BlockDescription blockDescription) throws ClassNotFoundException,
 			UserSuppliedCodeException {
 		LOGGER.entry(blockDescription);
 		final FunctionBlockSecurityDecorator securityDecorator =
 				createFunctionBlockSecurityDecorator(blockDescription.blockClassName);
 		LOGGER.trace("calling doInit on securityDecorator {}", securityDecorator);
 		securityDecorator.doInit(blockDescription.blockUUID, blockDescription.blockName);
 
 		synchronized (scheduledToStart) {
 			if (isRunning) {
 				throw LOGGER.throwing(new IllegalStateException("tried to schedule block " + securityDecorator
 						+ " in already running application"));
 			}
 			if (LOGGER.isTraceEnabled()) {
 				LOGGER.trace("adding {} to ID {}", securityDecorator, blockDescription.blockTypeHolderId);
 			}
			moduleApplicationManager.addToBlockTypeHolders(securityDecorator, blockDescription.blockTypeHolderId);
 			LOGGER.trace("adding {} to scheduledToStart");
 			scheduledToStart.add(securityDecorator);
 			LOGGER.trace("saving block options");
 			blockOptions.put(securityDecorator, blockDescription.options);
 
 			if (LOGGER.isTraceEnabled()) {
 				LOGGER.trace("initializing outputs {} on {}", blockDescription.outputs, securityDecorator);
 			}
 			initializeOutputs(securityDecorator, blockDescription.outputs);
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
 	 */
 	@SuppressWarnings("unchecked")
 	private FunctionBlockSecurityDecorator createFunctionBlockSecurityDecorator(final String className)
 			throws ClassNotFoundException, UserSuppliedCodeException {
 		Class<?> cls = null;
 		cls = classLoader.loadClass(className);
 		if (!FunctionBlock.class.isAssignableFrom(cls)) {
 			throw new IllegalArgumentException("class " + className + " is not a FunctionBlock");
 			// FIXME: catch this somewhere!
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
 			final Map<String, Set<ValueDestination>> outputs) {
 		final Map<String, Output<? extends Serializable>> blockOutputs = securityDecorator.getOutputs();
 		for (final Entry<String, Set<ValueDestination>> output : outputs.entrySet()) {
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
 		synchronized (scheduledToStart) {
 			if (isRunning) {
 				LOGGER.warn("tried to double start Application.");
 				throw new IllegalArgumentException("tried to double start Application.");
 			}
 			isRunning = true;
 	
 			for (final FunctionBlockSecurityDecorator func : scheduledToStart) {
 				startBlock(func);
 			}
 		}
 	}
 
 	/**
 	 * starts the given function block on the Module. Also triggers removing it from runnable blocks
 	 * 
 	 * @param block
 	 *            the block to be started.
 	 */
 	private void startBlock(final FunctionBlockSecurityDecorator block) {
 		if (!shutdownLock.readLock().tryLock()) {
 			return; // Already shutting down.
 		}
 		try {
 			funcBlockById.put(block.getBlockUUID(), block);
 
 			Runnable initRunnable = new Runnable() {
 				@Override
 				public void run() {
 					Map<String, String> options = null;
 					synchronized (scheduledToStart) {
 						options = blockOptions.remove(block);
 					}
 					try {
 						block.init(options);
 					} catch (UserSuppliedCodeException e) {
 						// TODO: handle malevolent block. Stop it, maybe?
 					}
 				}
 			};
 			Runnable updater = new Runnable() {
 				@Override
 				public void run() {
 					try {
 						block.update();
 					} catch (UserSuppliedCodeException e) {
 						// TODO: handle malevolent block. Stop it, maybe?
 					}
 				}
 			};
 			scheduledThreadPool.execute(initRunnable);
 
 			long period = block.getUpdateInterval();
 			try {
 				if (period < 0) {
 					scheduledThreadPool.schedule(updater, 0, TimeUnit.SECONDS);
 				} else {
 					scheduledThreadPool.scheduleAtFixedRate(updater, period, period, TimeUnit.MILLISECONDS);
 				}
 			} catch (RejectedExecutionException e) {
 				LOGGER.info("Received start block after initiating shutdown. Not scheduling block {}.", block);
 			}
 		} finally {
 			shutdownLock.readLock().unlock();
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
 	public void receiveValue(final UUID funcBlockId, String inputName, Serializable value)
 			throws NonExistentFunctionblockException, NonExistentInputException {
 		if (!shutdownLock.readLock().tryLock()) {
 			return; // Already shutting down.
 		}
 		try {
 			final FunctionBlockSecurityDecorator block = funcBlockById.get(funcBlockId);
 			if (block == null) {
 				LOGGER.info("FunctionBlockID not existent. ({})", funcBlockId);
 				throw LOGGER.throwing(new NonExistentFunctionblockException());
 			}
 			final Input input = block.getInputs().get(inputName);
 			if (input == null) {
 				LOGGER.info("input '{}' non existant for {}", inputName, block);
 				throw LOGGER.throwing(new NonExistentInputException());
 			}
 			input.setValue(value);
 			Runnable updater = new Runnable() {
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
 				LOGGER.info("Received message after initiating shutdown. Not rescheduling {}.", funcBlockId);
 			}
 		} finally {
 			shutdownLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Called when this application is shut down. Will call the appropriate methods on the executed functionBlocks.
 	 */
 	public void shutdown() {
 		shutdownLock.writeLock().lock(); // will not be unlocked.
 		scheduledThreadPool.shutdown();
 		final Thread shutdownThread = new Thread(new Runnable() {
 			public void run() {
 				for (FunctionBlockSecurityDecorator fun : funcBlockById.values()) {
 					try {
 						fun.shutdown();
 					} catch (UserSuppliedCodeException e) {
 						// TODO: handle malevolent block. Stop it, maybe?
 					}
 				}
 			}
 		});
 
 		Thread watcherThread = new Thread(new Runnable() {
 			@SuppressWarnings("deprecation")
 			public void run() {
 				shutdownThread.start();
 				try {
 					Thread.sleep(TIME_BEFORE_ATTEMPTED_SHUTDOWNHOOK_KILL);
 
 					shutdownThread.interrupt();
 					Thread.sleep(ADDITIONAL_TIME_BEFORE_FORCEFULL_KILL);
 				} catch (InterruptedException e) {
 					// Ignoring
 				}
 				shutdownThread.stop();
 				// It's deprecated and dangerous to stop a thread like this, because it forcefully releases all locks,
 				// yet there is no alternative to it if the victim is refusing to cooperate.
 			}
 		});
 
 		watcherThread.start();
 	}
 
 	/**
 	 * 
 	 * @return the UUID of this application
 	 */
 	public UUID getOwnAppId() {
 		return ownAppId;
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
 
 	/**
 	 * 
 	 * @return a map of FunctionBlockSecurityDecorators/FunctionBlocks this Application executes on this module. Mapped
 	 *         <BlockUUID, FuncBlockSecurityDecorator>
 	 */
 	public Map<UUID, FunctionBlockSecurityDecorator> getFuncBlockById() {
 		return new HashMap<UUID, FunctionBlockSecurityDecorator>(funcBlockById);
 	}
 
 	/**
 	 * @param blockId
 	 *            the blockId to check for.
 	 * @return true iff the the given block is executing on this Module.
 	 */
 	public boolean isExecuting(UUID blockId) {
 		return funcBlockById.containsKey(blockId);
 	}
 
 	// TODO insert javadoc here.
 	class ApplicationOutputTarget implements OutputTarget<Serializable> {
 		private final Set<ValueDestination> destinations;
 
 		/**
 		 * 
 		 * @param destinations
 		 *            places connected to this output. Where values are supposed to be send when they are send.
 		 */
 		public ApplicationOutputTarget(final Collection<ValueDestination> destinations) {
 			this.destinations = new HashSet<ValueDestination>(destinations);
 		}
 
 		@Override
 		public void setValue(Serializable value) {
 			for (final ValueDestination destination : destinations) {
 				sendValue(destination.getBlock(), destination.getInput(), value);
 			}
 		}
 	}
 }
