 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 package org.eclipse.b3.p2.util;
 
 import static java.lang.String.format;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Map;
 
 import org.eclipse.b3.p2.P2Factory;
 import org.eclipse.b3.p2.impl.MetadataRepositoryImpl;
 import org.eclipse.b3.p2.loader.IRepositoryLoader;
 import org.eclipse.b3.util.LogUtils;
 import org.eclipse.b3.util.TimeUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Resource </b> associated with the package.
  * <!-- end-user-doc -->
  * 
  * @see org.eclipse.b3.p2.util.P2ResourceFactoryImpl
  * @generated
  */
 public class P2ResourceImpl extends XMLResourceImpl {
 	class AsynchronousLoader extends Job {
 		private Job replaceJob;
 
 		public AsynchronousLoader(String name, Job replaceJob) {
 			super(name);
 			this.replaceJob = replaceJob;
 		}
 
 		@Override
 		protected IStatus run(final IProgressMonitor monitor) {
 			class MonitorWatchDog extends Thread {
 				private boolean done;
 
 				@Override
 				public void run() {
 					while(!done) {
 						if(monitor.isCanceled()) {
 							cancelLoadingJob();
 							break;
 						}
 
 						try {
 							Thread.sleep(100);
 						}
 						catch(InterruptedException e) {
 							// ignore
 						}
 					}
 				}
 
 				public void setDone() {
 					done = true;
 				}
 			}
 
 			MonitorWatchDog watchDog = new MonitorWatchDog();
 
 			try {
 				if(replaceJob != null) {
 					replaceJob.cancel();
 					replaceJob.join();
 				}
 
 				watchDog.start();
 
 				IStatus status = org.eclipse.core.runtime.Status.OK_STATUS;
 
 				try {
 					load(null);
 				}
 				catch(IOException e) {
 					status = new Status(IStatus.ERROR, "org.eclipse.b3.p2", "Unable to load repository " +
 							getURI().opaquePart(), e);
 				}
 
 				if(monitor.isCanceled()) {
 					// cancelled by user
 					status = org.eclipse.core.runtime.Status.CANCEL_STATUS;
 				}
 
 				return status;
 			}
 			catch(InterruptedException e) {
 				throw new RuntimeException("Repository load was interrupted");
 			}
 			finally {
 				monitor.done();
 				watchDog.setDone();
 
 				synchronized(P2ResourceImpl.this) {
 					if(asynchronousLoader == this)
 						asynchronousLoader = null;
 				}
 			}
 		}
 	}
 
 	private class LoaderJob extends Job {
 
 		private IProvisioningAgent agent;
 
 		private java.net.URI location = null;
 
 		public LoaderJob(IProvisioningAgent agent, String name, java.net.URI location) {
 			super(name);
 			this.agent = agent;
 			this.location = location;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 
 			String msg = format("Loading repository %s", location);
 
 			try {
 				MetadataRepositoryImpl repository = (MetadataRepositoryImpl) P2Factory.eINSTANCE.createMetadataRepository();
 				loader.open(location, agent, repository);
 				LogUtils.debug(msg);
 				long start = TimeUtils.getNow();
 				loader.load(monitor);
 				getContents().add(repository);
 				LogUtils.debug("Repository %s loaded (Took %s)", location, TimeUtils.getFormattedDuration(start));
 			}
 			catch(final Exception e) {
 				errors.add(new Resource.Diagnostic() {
 
 					public int getColumn() {
 						return 0;
 					}
 
 					public int getLine() {
 						return 0;
 					}
 
 					public String getLocation() {
 						return location.toString();
 					}
 
 					public String getMessage() {
 						return e.getMessage();
 					}
 				});
 			}
 			finally {
 				synchronized(lock) {
 					setLoaded(true);
 					isLoading = false;
 				}
 
 				try {
 					loader.close();
 				}
 				catch(CoreException e) {
 					LogUtils.error(e, "Unable to close repository loader for %s", location);
 				}
 			}
 
 			return Status.OK_STATUS;
 		}
 
 	}
 
 	private AsynchronousLoader asynchronousLoader;
 
 	private IRepositoryLoader loader;
 
 	private final Object lock = new Object();
 
 	private Job loaderJob;
 
 	/**
 	 * Creates an instance of the resource.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @param uri
 	 *            the URI of the new resource.
 	 * @generated NOT
 	 */
 	public P2ResourceImpl(URI uri) {
 		throw new IllegalArgumentException("Use constructor with loader");
 	}
 
 	/**
 	 * @param uri
 	 * @param loader
 	 */
 	public P2ResourceImpl(URI uri, IRepositoryLoader loader) {
 		super(uri);
 		this.loader = loader;
 	}
 
 	public synchronized void cancelLoadingJob() {
 		if(loaderJob != null)
 			loaderJob.cancel();
 	}
 
 	private java.net.URI getLocationFromURI(URI uri) throws URISyntaxException {
 		String opaquePart = uri.opaquePart();
 		int pos = opaquePart.indexOf(':');
 		return new java.net.URI(opaquePart.substring(pos + 1));
 	}
 
 	@Override
 	public void load(Map<?, ?> options) throws IOException {
 		synchronized(lock) {
 			if(isLoaded)
 				return;
 
 			if(!isLoading) {
 				isLoading = true;
 
 				if(errors == null)
 					errors = new BasicEList<Diagnostic>();
 				if(warnings == null)
 					warnings = new BasicEList<Diagnostic>();
 
 				errors.clear();
 				warnings.clear();
 
 				try {
 					ResourceSet resourceSet = getResourceSet();
 					IProvisioningAgent agent = null;
 
 					if(resourceSet instanceof ResourceSetWithAgent)
 						agent = ((ResourceSetWithAgent) resourceSet).getProvisioningAgent();
 
 					loaderJob = new LoaderJob(
 						agent, "Loading repository " + getURI().opaquePart(), getLocationFromURI(getURI()));
 					loaderJob.setUser(false);
 					loaderJob.schedule();
 				}
 				catch(URISyntaxException e) {
 					isLoading = false;
 					isLoaded = false;
 					IOException ex = new IOException();
 					ex.initCause(e);
 					throw ex;
 				}
 			}
 		}
 
 		try {
 			loaderJob.join();
 		}
 		catch(InterruptedException e) {
 			// ignore
 		}
 	}
 
 	@Override
 	public void save(Map<?, ?> options) {
 		// do nothing by default
 		return;
 	}
 
 	synchronized public void startAsynchronousLoad() {
 		if(isLoaded() && !isLoading())
 			return;
 
 		AsynchronousLoader lastLoader = asynchronousLoader;
 
 		if(lastLoader == null) {
 			asynchronousLoader = new AsynchronousLoader("Loading " + getURI().opaquePart(), lastLoader);
 			asynchronousLoader.setUser(false);
 			asynchronousLoader.schedule();
 		}
 	}
 
 } // P2ResourceImpl
