 package edu.teco.dnd.module;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
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
 
 import edu.teco.dnd.blocks.AssignmentException;
 import edu.teco.dnd.blocks.ConnectionTarget;
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.InvalidFunctionBlockException;
 import edu.teco.dnd.blocks.Output;
 import edu.teco.dnd.network.ConnectionManager;
 
 public class Application {
 
 	private static final Logger LOGGER = LogManager.getLogger(Application.class);
 	public static final long MODULE_LOCATION_REQUEST_DELAY = 500;
 	public static final int SEND_REPETITIONS_UPON_UNKNOWN_MODULE_LOCATION = 2;
 
 	private final UUID ownAppId;
 	private final String name;
 	private final ReadWriteLock shutdownLock = new ReentrantReadWriteLock();
 
 	private final ScheduledThreadPoolExecutor scheduledThreadPool;
 	private final ConnectionManager connMan;
 
 	/**
 	 * A Map from FunctionBlock UUID to matching ValueSender. Not used for local FunctionBlocks.
 	 */
 	private final ConcurrentMap<UUID, ValueSender> valueSenders = new ConcurrentHashMap<UUID, ValueSender>();
 
 	private final ApplicationClassLoader classLoader;
 	/** mapping of active blocks to their ID, used e.g. to pass values to inputs. */
 	private final Map<UUID, FunctionBlock> funcBlockById;
 
 	/**
 	 * @return all blocks, this app is currently executing.
 	 */
 	public Collection<FunctionBlock> getAllBlocks() {
 		return funcBlockById.values();
 	}
 
 	public Application(UUID appId, String name, ScheduledThreadPoolExecutor scheduledThreadPool,
 			ConnectionManager connMan, ApplicationClassLoader classloader) {
 		this.ownAppId = appId;
 		this.name = name;
 		this.scheduledThreadPool = scheduledThreadPool;
 		this.connMan = connMan;
 		this.classLoader = classloader;
 		this.funcBlockById = new HashMap<UUID, FunctionBlock>();
 	}
 
 	/**
 	 * called from this app, when a value is supposed to be send to another block (potentially on another Module).
 	 * 
 	 * 
 	 * @param funcBlock
 	 *            the receiving functionBlock.
 	 * @param input
 	 *            the input on the given block to receive the message.
 	 * @param val
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
 	 * @see sendValue but sanitized
 	 * @param input
 	 * @see sendValue but sanitized
 	 * @param value
 	 * @see sendValue but sanitized
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
 	 * loads a class into this app
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
 	 * starts the given function block on the Module. Also triggers removing it from runnable blocks
 	 * 
 	 * @param block
 	 *            the block to be started.
 	 */
 	public void startBlock(final FunctionBlock block) {
 		if (!shutdownLock.readLock().tryLock()) {
 			return; // Already shutting down.
 		}
 		try {
 			funcBlockById.put(block.getID(), block);
 
 			Runnable initRunnable = new Runnable() {
 				@Override
 				public void run() {
 					block.init();
 					try {
 						for (Output<?> output : block.getOutputs().values()) {
 							for (ConnectionTarget ct : output.getConnectedTargets()) {
 								if (ct instanceof RemoteConnectionTarget) {
 									RemoteConnectionTarget rct = (RemoteConnectionTarget) ct;
 									rct.setApplication(Application.this);
 								}
 							}
 						}
 					} catch (InvalidFunctionBlockException e) {
 						LOGGER.warn("FunctionBlock {} initialization failed.", block.getID());
 						LOGGER.catching(e);
 					}
 
 				}
 			};
 			Runnable updater = new Runnable() {
 				@Override
 				public void run() {
 					try {
 						block.doUpdate();
 					} catch (AssignmentException e) {
 						LOGGER.info("Can not assign field of functionBlock {}", block);
 					}
 				}
 			};
 
 			scheduledThreadPool.execute(initRunnable);
 			long period = block.getTimebetweenSchedules();
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
 	 * @param input
 	 *            input on the block receiving the message.
 	 * @param value
 	 *            the value to give to the input.
 	 * @return true iff value was successfully passed on.
 	 * @throws IllegalAccessException
 	 * @throws NonExistentFunctionblockException
 	 * @throws NonExistentInputException
 	 */
 	public void receiveValue(final UUID funcBlockId, String input, Serializable value)
 			throws NonExistentFunctionblockException, NonExistentInputException {
 		if (!shutdownLock.readLock().tryLock()) {
 			return; // Already shutting down.
 		}
 		try {
 
 			if (funcBlockById.get(funcBlockId) == null) {
 				LOGGER.info("FunctionBlockID not existent. ({})", funcBlockId);
 				throw LOGGER.throwing(new NonExistentFunctionblockException());
 			}
 			ConnectionTarget ct = funcBlockById.get(funcBlockId).getConnectionTargets().get(input);
 			if (ct == null) {
 				LOGGER.warn("specified input does not exist: {} on {}", input, funcBlockId);
 				throw LOGGER.throwing(new NonExistentInputException());
 			}
 			ct.setValue(value);
 			Runnable updater = new Runnable() {
 				@Override
 				public void run() {
 					FunctionBlock block = funcBlockById.get(funcBlockId);
 					if (block == null) {
 						LOGGER.warn("scheduled a block, that does not exist.");
						return;
 					}
 					try {
 						block.doUpdate();
 					} catch (AssignmentException e) {
 						LOGGER.info("Can not assign field of functionBlock {}", block);
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
 	 * called to indicate, that the application is being shut down. Quits the scheduling of it.
 	 * 
 	 */
 	public void shutdown() {
 		shutdownLock.writeLock().lock(); // will not be unlocked.
 		scheduledThreadPool.shutdown();
 		final Thread shutdownThread = new Thread(new Runnable() {
 			public void run() {
 				for (FunctionBlock fun : funcBlockById.values()) {
 					funcBlockById.remove(fun.getID());
 					fun.shutdown();
 				}
 			}
 		});
 
 		Thread watcherThread = new Thread(new Runnable() {
 			@SuppressWarnings("deprecation")
 			public void run() {
 				shutdownThread.run();
 				try {
 					Thread.sleep(2000);// TODO make configurable.
 
 					shutdownThread.interrupt();
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 				}
 				shutdownThread.stop();
 				// It's deprecated and dangerous to stop a thread like this, because it forcefully releases all locks,
 				// yet there is no alternative to it if the victim is refusing to cooperate.
 			}
 		});
 
 		watcherThread.start();
 	}
 
 	public UUID getOwnAppId() {
 		return ownAppId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public ApplicationClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	public ScheduledThreadPoolExecutor getThreadPool() {
 		return scheduledThreadPool;
 	}
 
 	public Map<UUID, FunctionBlock> getFuncBlockById() {
 		return new HashMap<UUID, FunctionBlock>(funcBlockById);
 	}
 
 	/**
 	 * @param blockId
 	 *            the blockId to check for.
 	 * @return true iff the the given block is executing on this Module.
 	 */
 	public boolean isExecuting(UUID blockId) {
 		return funcBlockById.containsKey(blockId);
 	}
 }
