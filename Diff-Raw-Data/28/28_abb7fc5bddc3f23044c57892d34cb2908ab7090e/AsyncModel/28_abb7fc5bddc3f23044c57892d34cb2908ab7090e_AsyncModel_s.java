 package org.soluvas.web.site;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
 
 /**
  * Usage:
  * 
  * <pre>{@literal
  * //Find all product
  * final IModel<List<Product>> productEntities = new AsyncModel<List<Product>>() {
  * 	@Override
  * 	public List<Product> load() {
  * 		return ImmutableList.copyOf(Iterables.limit(productRepo.findAll(), 16));
  * 	}
  * });
  * }</pre>
  * 
  * <p>{@link #onAttach()} doesn't work as I imagined. What we need is onBeforeRequest...
  * 
  * <p>This model is thread-safe.
  * 
  * @author ceefour
  */
 @SuppressWarnings("serial")
 public abstract class AsyncModel<T> implements IModel<T> {
 
 	private static final Logger log = LoggerFactory.getLogger(AsyncModel.class);
 	/** keeps track of whether this model is attached or detached */
 	private transient volatile boolean attached = false;
 	/** temporary, transient object. */
 	private transient volatile T transientModelObject;
 	public static final int timeoutValue = 15;
 	public static final TimeUnit timeoutUnit = TimeUnit.SECONDS;
 	private transient Future<T> future;
 	private final ReadWriteLock lock = new ReentrantReadWriteLock(); 
 
 	public AsyncModel() {
 		super();
 		resubmit();
 	}
 	
 	/**
 	 * @see org.apache.wicket.model.IDetachable#detach()
 	 */
 	public void detach()
 	{
 		if (attached)
 		{
 			try
 			{
 				onDetach();
 			}
 			finally
 			{
 				attached = false;
 				transientModelObject = null;
 
				log.debug("removed transient object for {}, requestCycle {}", this,
 					RequestCycle.get());
 			}
 		}
 	}
 
 	/**
 	 * @see org.apache.wicket.model.IModel#getObject()
 	 */
 	public T getObject()
 	{
 		while (!attached) {
 			try {
 				final Lock writeLock = lock.writeLock();
 				final boolean locked = writeLock.tryLock(50, TimeUnit.MILLISECONDS);
 				if (locked) {
 					try {
 						if (future == null) {
 							log.info("future is null, resubmitting");
 							// FIXME: this is sync!! need onBeforeRequest
 							resubmit();
 						}
 						transientModelObject = future.get(timeoutValue, timeoutUnit);
 						attached = true;
 	//					if (futureResult == null)
 	//						log.warn("AsyncModel returns null!");
 						
 						if (log.isDebugEnabled())
 						{
 							log.debug("loaded transient object " + transientModelObject + " for " + this +
 								", requestCycle " + RequestCycle.get());
 						}
 	
 						onAttach();
 					} catch (final InterruptedException e) {
 						throw new SiteException(e, "Cannot load model %s", getClass().getName());
 					} catch (final ExecutionException e) {
 	//					Throwables.propagate(e.getCause());
 						throw new SiteException(e, "Cannot load model %s", getClass().getName());
 					} catch (final TimeoutException e) {
 						log.error("Timed out (%d %s) waiting for model %s", timeoutValue, timeoutUnit, getClass().getName());
 					} finally {
 						writeLock.unlock();
 					}
 				}
 			} catch (InterruptedException e) {
 				log.warn("Interrupted while trying write lock " + getClass().getName(), e);
 			}
 		}
 		return transientModelObject;
 	}
 
 	/**
 	 * Gets the attached status of this model instance
 	 * 
 	 * @return true if the model is attached, false otherwise
 	 */
 	public final boolean isAttached()
 	{
 		return attached;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 	 StringBuilder sb = new StringBuilder(super.toString());
 		sb.append(":attached=").append(attached).append(":tempModelObject=[").append(
 			this.transientModelObject).append("]");
 		return sb.toString();
 	}
 
 	/**
 	 * Loads and returns the (temporary) model object.
 	 * 
 	 * @return the (temporary) model object
 	 */
 	protected abstract T load() throws Exception;
 
 	/**
 	 * Attaches to the current request. Implement this method with custom behavior, such as loading
 	 * the model object.
 	 */
 	protected void onAttach()
 	{
 	}
 
 	/**
 	 * Detaches from the current request. Implement this method with custom behavior, such as
 	 * setting the model object to null.
 	 */
 	protected void onDetach()
 	{
 	}
 
 
 	/**
 	 * Manually loads the model with the specified object. Subsequent calls to {@link #getObject()}
 	 * will return {@code object} until {@link #detach()} is called.
 	 * 
 	 * @param object
 	 *            The object to set into the model
 	 */
 	public void setObject(final T object)
 	{
 		attached = true;
 		transientModelObject = object;
 	}
 
 	/**
 	 * @param loader
 	 */
 	protected void resubmit() {
 		final BundleContext bundleContext = FrameworkUtil.getBundle(AsyncModel.class).getBundleContext();
 		final ServiceReference<ExecutorService> executorRef;
 		try {
 			executorRef = Iterables.getFirst(
 					bundleContext.getServiceReferences(ExecutorService.class, "(tenantId=*)"), null);
 			Preconditions.checkNotNull(executorRef, "Cannot find ExecutorService");
 		} catch (InvalidSyntaxException e) {
 			throw new SiteException("Cannot get global ExecutorService", e);
 		}
 		final ExecutorService executor = bundleContext.getService(executorRef);
 		try {
 			future = executor.submit(new Callable<T>() {
 				@Override
 				public T call() throws Exception {
 					return load();
 				}
 			});
 		} finally {
 			bundleContext.ungetService(executorRef);
 		}
 	}
 
 }
