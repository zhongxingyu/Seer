 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.zendserver.monitor.internal.core;
 
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.widgets.Display;
 import org.osgi.service.prefs.BackingStoreException;
 import org.zend.php.zendserver.deployment.core.targets.PhpcloudContainerListener;
 import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
 import org.zend.php.zendserver.monitor.core.Activator;
 import org.zend.php.zendserver.monitor.core.IEventDetails;
 import org.zend.php.zendserver.monitor.core.INotificationProvider;
 import org.zend.php.zendserver.monitor.core.MonitorManager;
 import org.zend.sdklib.application.ZendCodeTracing;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.monitor.IZendIssue;
 import org.zend.sdklib.monitor.ZendMonitor;
 import org.zend.sdklib.monitor.ZendMonitor.Filter;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiClient;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.CodeTracingStatus;
 import org.zend.webapi.core.connection.data.EventsGroupDetails;
 import org.zend.webapi.core.connection.data.values.ZendServerVersion;
 import org.zend.webapi.core.connection.response.ResponseCode;
 import org.zend.webapi.internal.core.connection.exception.UnexpectedResponseCode;
 import org.zend.webapi.internal.core.connection.exception.WebApiCommunicationError;
 
 /**
  * Represents abstract monitor job. It contains monitor internal implementation
  * shared between different monitor types.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public abstract class AbstractMonitor extends Job {
 
 	private static final String PROVIDER_EXTENSION = "org.zend.php.zendserver.monitor.core.notificationProvider"; //$NON-NLS-1$
 
 	private static INotificationProvider provider;
 
 	protected String targetId;
 	private ZendMonitor monitor;
 	protected long lastTime;
 	private int jobDelay = 2000;
 	private int offset;
 	private ZendCodeTracing codeTracing;
 
 	public AbstractMonitor(String targetId, String jobTitle) {
 		super(jobTitle);
 		this.targetId = targetId;
 	}
 
 	/**
 	 * Start monitor job.
 	 */
 	public boolean start() {
 		if (shouldStart()) {
 			getProvider().showProgress(getName(), 90,
 					new IRunnableWithProgress() {
 
 						public void run(IProgressMonitor monitor)
 								throws InvocationTargetException,
 								InterruptedException {
 							if (!doStart(monitor)) {
 								MonitorManager
 										.removeMonitor(AbstractMonitor.this);
 							}
 						}
 					});
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public IStatus run(IProgressMonitor monitor) {
 		monitor.beginTask(MessageFormat.format(
 				Messages.ZendServerMonitor_TaskTitle, targetId),
 				IProgressMonitor.UNKNOWN);
 		PhpcloudContainerListener listener = null;
 		IZendTarget target = TargetsManagerService.INSTANCE.getTargetManager()
 				.getTargetById(targetId);
 		if (TargetsManager.isPhpcloud(target)) {
 			listener = new PhpcloudContainerListener(target);
 			WebApiClient.registerPreRequestListener(listener);
 		}
 		try {
 			List<IZendIssue> issues = null;
 			if (this.monitor == null) {
 				this.monitor = new ZendMonitor(targetId);
 				issues = this.monitor.getOpenIssues();
 			} else {
 				issues = this.monitor.getIssues(Filter.ALL_OPEN_EVENTS, offset);
 				if (issues != null && issues.size() > 0) {
 					handleIssues(issues);
 				}
 			}
 			if (issues != null && issues.size() > 0) {
 				offset += issues.size();
 				Date lastDate = getTime(issues.get(issues.size() - 1)
 						.getIssue().getLastOccurance());
 				if (lastDate != null) {
 					lastTime = lastDate.getTime();
 				}
 			}
 			if (!monitor.isCanceled()) {
 				monitor.done();
 				this.schedule(jobDelay);
 				return Status.OK_STATUS;
 			}
 			monitor.done();
 			return Status.CANCEL_STATUS;
 		} finally {
 			if (listener != null) {
 				WebApiClient.unregisterPreRequestListener(listener);
 			}
 		}
 	}
 
 	/**
 	 * Flush preferences for for this monitor.
 	 * 
 	 * @throws BackingStoreException
 	 */
 	public abstract void flushPreferences() throws BackingStoreException;
 
 	/**
 	 * @return <code>true</code> if it monitors is enabled; otherwise return
 	 *         <code>false</code>
 	 */
 	public abstract boolean isEnabled();
 
 	/**
 	 * Disable the whole monitor.
 	 * 
 	 * @param codeTracing
 	 *            - <code>true</code> if code tracing should be disabled
 	 */
 	public abstract void disable(boolean codeTracing);
 
 	protected abstract void handleIssues(List<IZendIssue> issues);
 
 	protected void showNonification(final IZendIssue issue,
 			final String projectName, final String basePath, final int delay,
 			final int actionsAvailable) {
 		Display.getDefault().asyncExec(new Runnable() {
 
 			public void run() {
 				IEventDetails eventSource = EventDetails.create(projectName,
 						basePath, issue.getIssue());
 				getProvider().showNonification(issue, targetId, eventSource,
 						delay, actionsAvailable);
 			}
 		});
 	}
 
 	protected int checkActions(IZendIssue issue) {
 		int result = 0;
 		try {
 			List<EventsGroupDetails> groups = issue.getGroupDetails();
 			result += MonitorManager.REPEAT;
 			if (groups != null && groups.size() == 1) {
 				String traceId = groups.get(0).getCodeTracing();
 				if (traceId != null && !traceId.isEmpty()) {
 					result += MonitorManager.CODE_TRACE;
 				}
 			}
 		} catch (Exception e) {
 			return 0;
 		}
 		return result;
 	}
 
 	protected abstract IProject getProject(String urlString);
 
 	protected abstract boolean shouldStart();
 
 	protected static INotificationProvider getProvider() {
 		if (provider == null) {
 			IConfigurationElement[] elements = Platform.getExtensionRegistry()
 					.getConfigurationElementsFor(PROVIDER_EXTENSION);
 			for (IConfigurationElement element : elements) {
 				if ("notificationProvider".equals(element.getName())) { //$NON-NLS-1$
 					try {
 						Object listener = element
 								.createExecutableExtension("class"); //$NON-NLS-1$
 						if (listener instanceof INotificationProvider) {
 							provider = (INotificationProvider) listener;
 							break;
 						}
 					} catch (CoreException e) {
 						Activator.log(e);
 					}
 				}
 			}
 		}
 		return provider;
 	}
 
 	protected Date getTime(String time) {
 		SimpleDateFormat formatter = null;
 		IZendTarget target = TargetsManagerService.INSTANCE.getTargetManager()
 				.getTargetById(targetId);
 		if (ZendServerVersion
 				.byName(target.getProperty(IZendTarget.SERVER_VERSION))
 				.getName().startsWith("6")) { //$NON-NLS-1$
 			int index = time.indexOf("T"); //$NON-NLS-1$
 			String prefix = time.substring(0, index);
 			String suffix = time.substring(index + 1, time.length());
			time = prefix + suffix;
			index = time.indexOf("+"); //$NON-NLS-1$
 			time = time.substring(0, index);
			formatter = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss"); //$NON-NLS-1$
			
 		} else {
 			formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm"); //$NON-NLS-1$
 		}
 		Date date = null;
 		try {
 			date = formatter.parse(time);
 		} catch (ParseException e) {
 			Activator.log(e);
 			return null;
 		}
 		return date;
 	}
 
 	protected void disableCodeTacing() {
 		getProvider().showProgress(getName(), 90, new IRunnableWithProgress() {
 
 			public void run(IProgressMonitor monitor)
 					throws InvocationTargetException, InterruptedException {
 				monitor.beginTask(MessageFormat.format(
 						Messages.AbstractMonitor_DisablingJobName, targetId),
 						IProgressMonitor.UNKNOWN);
 				ZendCodeTracing codeTracing = new ZendCodeTracing(targetId);
 				PhpcloudContainerListener listener = null;
 				IZendTarget target = TargetsManagerService.INSTANCE
 						.getTargetManager().getTargetById(targetId);
 				if (TargetsManager.isPhpcloud(target)) {
 					listener = new PhpcloudContainerListener(target);
 					WebApiClient.registerPreRequestListener(listener);
 				}
 				try {
 					CodeTracingStatus status = codeTracing.disable(true);
 					if (status == null) {
 						String m = MessageFormat
 								.format(Messages.AbstractMonitor_InitializationJobConnectionError,
 										targetId);
 						handleError(monitor, m);
 						return;
 					}
 				} catch (WebApiException e) {
 					if (e instanceof WebApiCommunicationError) {
 						String m = MessageFormat
 								.format(Messages.AbstractMonitor_InitializationJobConnectionError,
 										targetId);
 						handleError(monitor, m);
 						return;
 					} else {
 						if (e instanceof UnexpectedResponseCode) {
 							UnexpectedResponseCode codeException = (UnexpectedResponseCode) e;
 							ResponseCode code = codeException.getResponseCode();
 							switch (code) {
 							case UNSUPPORTED_API_VERSION:
 								String m = MessageFormat
 										.format(Messages.AbstractMonitor_InitializationJobUnsupportedVersion,
 												targetId);
 								handleError(monitor, m);
 								return;
 							default:
 								break;
 							}
 						}
 					}
 				} finally {
 					if (listener != null) {
 						WebApiClient.unregisterPreRequestListener(listener);
 					}
 				}
 			}
 		});
 	}
 
 	private boolean doStart(IProgressMonitor monitor) {
 		monitor.beginTask(MessageFormat.format(
 				Messages.AbstractMonitor_EnablingJobName, targetId),
 				IProgressMonitor.UNKNOWN);
 		if (codeTracing == null) {
 			codeTracing = new ZendCodeTracing(targetId);
 			PhpcloudContainerListener listener = null;
 			IZendTarget target = TargetsManagerService.INSTANCE
 					.getTargetManager().getTargetById(targetId);
 			if (TargetsManager.isPhpcloud(target)) {
 				listener = new PhpcloudContainerListener(target);
 				WebApiClient.registerPreRequestListener(listener);
 			}
 			try {
 				CodeTracingStatus status = codeTracing.enable(true);
 				if (status == null) {
 					String m = MessageFormat
 							.format(Messages.AbstractMonitor_InitializationJobConnectionError,
 									targetId);
 					handleError(monitor, m);
 					return false;
 				}
 			} catch (WebApiException e) {
 				if (e instanceof WebApiCommunicationError) {
 					String m = MessageFormat
 							.format(Messages.AbstractMonitor_InitializationJobConnectionError,
 									targetId);
 					handleError(monitor, m);
 					return false;
 				} else {
 					if (e instanceof UnexpectedResponseCode) {
 						UnexpectedResponseCode codeException = (UnexpectedResponseCode) e;
 						ResponseCode code = codeException.getResponseCode();
 						switch (code) {
 						case UNSUPPORTED_API_VERSION:
 							String m = MessageFormat
 									.format(Messages.AbstractMonitor_InitializationJobUnsupportedVersion,
 											targetId);
 							handleError(monitor, m);
 							return false;
 						default:
 							break;
 						}
 					}
 				}
 			} finally {
 				if (listener != null) {
 					WebApiClient.unregisterPreRequestListener(listener);
 				}
 			}
 		}
 		lastTime = Long.MAX_VALUE;
 		if (getState() == Job.NONE) {
 			setSystem(true);
 		}
 		AbstractMonitor.this.run(monitor);
 		return true;
 	}
 
 	private void handleError(IProgressMonitor monitor, String m) {
 		getProvider().showErrorMessage(getName(), m);
 		monitor.done();
 		disable(false);
 	}
 }
