 /*******************************************************************************
  * Copyright (c) 2010 AGETO and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  *******************************************************************************/
 package net.ageto.gyrex.persistence.carbonado.storage;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.gyrex.monitoring.metrics.MetricSet;
 import org.eclipse.gyrex.persistence.storage.Repository;
 import org.eclipse.gyrex.persistence.storage.content.RepositoryContentType;
 import org.eclipse.gyrex.persistence.storage.exceptions.ResourceFailureException;
 import org.eclipse.gyrex.persistence.storage.provider.RepositoryProvider;
 
 import org.eclipse.core.runtime.IStatus;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A repository backed by a {@link com.amazon.carbonado.Repository Carbonado
  * repository}.
  * <p>
  * Per convention, any content type that is stored in a Carbonado repository
  * must use {@value #MEDIA_TYPE_TYPE} as its
  * {@link RepositoryContentType#getMediaTypeType()}.
  * </p>
  */
 public abstract class CarbonadoRepository extends Repository implements ICarbonadoRepositoryConstants {
 
 	public static final String REPOSITORY_TYPE_JDBC = "repository_type_jdbc";
 
 	/**
 	 * per convention, any content type that is stored in a Carbonado repository
 	 * must use {@value #MEDIA_TYPE_TYPE} as its
 	 * {@link RepositoryContentType#getMediaTypeType()}
 	 */
 	public static final String MEDIA_TYPE_TYPE = "x-ageto-carbonado-repository";
 
 	/** @deprecated */
 	@Deprecated
 	public static final String TYPE = "db_type";
 	/** @deprecated */
 	@Deprecated
 	public static final String DBNAME = "db_name";
 	/** @deprecated */
 	@Deprecated
 	public static final String HOSTNAME = "db_hostname";
 	/** @deprecated */
 	@Deprecated
 	public static final String PORT = "db_port";
 	/** @deprecated */
 	@Deprecated
 	public static final String USERNAME = "db_username";
 	/** @deprecated */
 	@Deprecated
 	public static final String PASSWORD = "db_password";
 
 	private final AtomicReference<com.amazon.carbonado.Repository> repositoryRef = new AtomicReference<com.amazon.carbonado.Repository>();
 
 	public CarbonadoRepository(final String repositoryId, final RepositoryProvider repositoryProvider, final MetricSet metrics) {
 		super(repositoryId, repositoryProvider, metrics);
 	}
 
 	/**
 	 * Creates a Carbonado {@link com.amazon.carbonado.Repository}.
 	 * <p>
 	 * Must be implemented by subclasses.
 	 * </p>
 	 * 
 	 * @noreference This method is not intended to be referenced by clients.
 	 */
 	protected abstract com.amazon.carbonado.Repository createRepository() throws Exception;
 
 	@Override
 	protected void doClose() {
 		final com.amazon.carbonado.Repository repository = repositoryRef.getAndSet(null);
 		if (null != repository) {
 			repository.close();
 		}
 	}
 
 	public synchronized void flushRepository() {
 		final com.amazon.carbonado.Repository repository = repositoryRef.getAndSet(null);
 		if (null != repository) {
 			repository.close();
 		}
 	}
 
 	/**
 	 * Returns a human-readable description containing the underlying Carbonado
 	 * repository name (if available).
 	 * 
 	 * @return a repository description
 	 * @see org.eclipse.gyrex.persistence.storage.Repository#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		final com.amazon.carbonado.Repository repository = repositoryRef.get();
 		if (null != repository) {
 			final String name = repository.getName();
 			if (StringUtils.isNotBlank(name)) {
 				return name;
 			}
 		}
 		return getRepositoryId();
 	}
 
 	/**
 	 * Only used internally. Don't call directly.
 	 * 
 	 * @noreference This method is not intended to be referenced by clients.
 	 * @nooverride This method is not intended to be re-implemented or extended
 	 *             by clients.
 	 */
	protected com.amazon.carbonado.Repository getOrCreateRepository() throws ResourceFailureException {
 		final com.amazon.carbonado.Repository repository = repositoryRef.get();
 		if (null != repository) {
 			return repository;
 		}
 
 		try {
 			return internalCreateRepository();
 		} catch (final Exception e) {
 			throw new ResourceFailureException(String.format("Unable to initialize repository '%s'. Please check configuration and try again. %s", getRepositoryId(), e.getMessage()), e);
 		}
 	}
 
 	/**
 	 * Returns the underlying {@link com.amazon.carbonado.Repository Carbonado
 	 * repository}.
 	 * 
 	 * @return the repository
 	 * @throws IllegalStateException
 	 *             is the repository is closed
 	 * @throws ResourceFailureException
 	 *             if the repository is not ready to be used
 	 */
 	public final com.amazon.carbonado.Repository getRepository() throws IllegalStateException, ResourceFailureException {
 		if (isClosed()) {
 			throw new IllegalStateException(String.format("Repository '%s' is closed.", getRepositoryId()));
 		}
 
 		final IStatus status = getStatus();
 		if (!status.isOK()) {
 			throw new ResourceFailureException(String.format("Repository '%s' is not ready. Please try again in a few seconds. %s", getRepositoryId(), status.getMessage()));
 		}
 
 		return getOrCreateRepository();
 	}
 
 	/**
 	 * Returns the repository status.
 	 * 
 	 * @return the repository status
 	 */
 	protected abstract IStatus getStatus();
 
 	private synchronized com.amazon.carbonado.Repository internalCreateRepository() throws Exception {
 		com.amazon.carbonado.Repository repository = repositoryRef.get();
 		if (null != repository) {
 			return repository;
 		}
 
 		// re-check closed state before creating a new repository
 		if (isClosed()) {
 			throw new IllegalStateException(String.format("Repository '%s' is closed.", getRepositoryId()));
 		}
 
 		// create repository
 		repository = createRepository();
 		if (null == repository) {
 			throw new IllegalStateException("Please check implementation. No repository returned!");
 		}
 		repositoryRef.set(repository);
 
 		return repository;
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder toString = new StringBuilder();
 		toString.append("CarbonadoRepository ");
 		if (isClosed()) {
 			toString.append(" CLOSED ");
 		}
 		toString.append("{");
 		toString.append(getRepositoryId());
 		final com.amazon.carbonado.Repository repository = repositoryRef.get();
 		if (null != repository) {
 			toString.append(", using ");
 			toString.append(repository.getClass().getSimpleName());
 			final String dbName = repository.getName();
 			if (StringUtils.isNotBlank(dbName)) {
 				toString.append(" ").append(dbName);
 			}
 		} else {
 			toString.append(", NOT INITIALIZED ");
 		}
 		toString.append("}");
 		final IStatus status = getStatus();
 		if (!status.isOK()) {
 			if (status.matches(IStatus.CANCEL | IStatus.ERROR)) {
 				toString.append(" ERROR");
 			} else if (status.matches(IStatus.WARNING)) {
 				toString.append(" WARNING");
 			}
 			if (StringUtils.isNotBlank(status.getMessage())) {
 				toString.append(" ").append(status.getMessage());
 			} else {
 				toString.append(" (status not ok, but no message was given)");
 			}
 		}
 		return toString.toString();
 	}
 }
