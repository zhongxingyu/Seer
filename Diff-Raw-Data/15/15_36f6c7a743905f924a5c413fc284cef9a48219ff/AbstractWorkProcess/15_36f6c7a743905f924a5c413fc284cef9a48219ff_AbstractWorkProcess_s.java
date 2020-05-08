 package de.wfhosting.workmanager.workers;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 
 import de.wfhosting.workmanager.data.WMConfig;
 import de.wfhosting.workmanager.data.WorkResultFetchable;
 import de.wfhosting.workmanager.exceptions.WorkException;
 
 /**
  * Abstract superclass for {@link SerialWork}, {@link ParallelWork} and
  * {@link OptimalParallelWork}. Defines the base methods all workers have to
  * implement and some which the may override.
  * 
  * @author <a href="mailto:simon@wf-hosting.de>">Simon Frankenberger</a>
  * @since 0.4
  * 
  * @param <T>
  *          The type of data to work on
  */
 public abstract class AbstractWorkProcess<T> implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The list of data the workers should handle.
 	 */
 	private final List<T> data;
 
 	/**
 	 * The config behind this worker process.
 	 */
 	WMConfig config = null;
 
 	/**
 	 * Default contructor, takes the data to work on and stores it in
 	 * {@link #data}.
 	 * 
 	 * @param data
 	 *          The data to work on.
 	 */
 	public AbstractWorkProcess(final List<T> data) {
 		this.data = Collections.unmodifiableList(data);
 	}
 
 	/**
 	 * Called by the Workmanager after every block that was processed. May be
 	 * overwritten to cleanup some stuff.
 	 * 
 	 * @param index
 	 *          The index of block that was processed.
 	 * @throws WorkException
 	 *           If anything goes wrong during this method call and the worker
 	 *           should abort.
 	 */
 	public void afterBlock(final int index) throws WorkException { // NOPMD
 		// nothing to do, has not to be implemented.
 	}
 
 	/**
 	 * Called by the workmanager once after all blocks have been processed. Should
 	 * cleanup the temporary data and can, for example, write the generated
 	 * results to a persistent storage like a database.
 	 * 
 	 * @throws WorkException
 	 *           If anything goes wrong during this method call and the worker
 	 *           should abort.
 	 */
 	public void afterFinish() throws WorkException { // NOPMD
 		// nothing to do, has not to be implemented.
 	}
 
 	/**
 	 * Called by the Workmanager before every block that has to be processed. May
 	 * be overwritten to prepare some stuff, but should not do a lot of work.
 	 * 
 	 * @param index
 	 *          The index of block that is about to be processed.
 	 * @throws WorkException
 	 *           If anything goes wrong during this method call and the worker
 	 *           should abort.
 	 */
 	public void beforeBlock(final int index) throws WorkException { // NOPMD
 		// nothing to do, has not to be implemented.
 	}
 
 	/**
 	 * Called by the workmanager once before the first block is about to be
 	 * processed. Should prepare the temporary data and do some just-before stuff
 	 * this worker might need.
 	 * 
 	 * @throws WorkException
 	 *           If anything goes wrong during this method call and the worker
 	 *           should abort.
 	 */
 	public void beforeStart() throws WorkException { // NOPMD
 		// nothing to do, has not to be implemented.
 	}
 
 	/**
 	 * Return the given block of data to work on. This method may return a
 	 * copy of the working data set as the worker should publish his results via
 	 * implementing the {@link WorkResultFetchable} interface.
 	 * 
 	 * @param index
 	 *          The block to get.
 	 * @return Corresponding to the index, the data to work on for a worker.
 	 */
 	public abstract List<T> getBlock(int index);
 
 	/**
 	 * Calculates and returns the count of blocks to work on the data.
 	 * 
 	 * @return Count of blocks to process
 	 */
 	public abstract int getBlockCount();
 
 	/**
 	 * Retrieves the data this instance works on as an unmodifiable list.
 	 * 
 	 * @return List of data
 	 */
 	protected final List<T> getData() {
 		return data;
 	}
 
 	/**
 	 * Retrieves a specific part of the data to work on.
 	 * 
 	 * @param index
 	 *          the index of the data to retrieve
 	 * @return A specific part of data to work on.
 	 */
 	protected final T getData(final int index) {
 		return data.get(index);
 	}
 
 	/**
 	 * @return The number of data list entries.
 	 */
 	protected final int getDataSize() {
 		return data.size();
 	}
 
 	/**
 	 * Main method for the workers to work on the data. This function gets the
 	 * index of blockdata it is responsible for. Inside this method, you should
 	 * get the data to work on via the {@link #getBlock(int)} method. The index is
 	 * ensured to be in the range of 0 to {@link #getBlockCount()}.
 	 * 
 	 * @param index
 	 *          the index of data to work on.
 	 * @throws WorkException
 	 *           If anything goes wrong during the work and the worker should
 	 *           abort.
 	 */
 	public abstract void processBlock(int index) throws WorkException;
 
 	/**
 	 * This method is called by the manager if any of the workers methods did not
 	 * end successfully and a rollback may be neccassary to stay consistent. For
 	 * example, if we fetched the data from the database and wrote it back in a
 	 * transaction, this method should fire the databases "ROLLBACK" sql command.
 	 * 
 	 * @param index
 	 *          The index of block which has failed.
 	 */
 	public abstract void rollback(int index);
 
 	/**
 	 * Called by the Workmanager when this object is instanciated. Stores the
 	 * manager behind this instance of later use.
 	 * 
	 * @param manager
	 *          The manager behind this work process.
 	 */
 	public final void setConfig(final WMConfig config) {
 		this.config = config;
 	}
 }
