 /*******************************************************************************
  * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.runtime.stepper.extensions;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.interfaces.IConditionTester;
 import org.eclipse.tcf.te.runtime.interfaces.ISharedConstants;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepExecutor;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.tracing.ITraceIds;
 import org.eclipse.tcf.te.runtime.stepper.nls.Messages;
 import org.eclipse.tcf.te.runtime.utils.ProgressHelper;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 
 /**
  * Step executor implementation.
  * <p>
  * The step executor is responsible for initiating the execution of a single step. The executor
  * creates and associated the step callback and blocks the execution till the executed step invoked
  * the callback.
  * <p>
  * The step executor is passing any status thrown by the executed step to the parent stepper
  * instance for handling.
  * <p>
  * If the step to execute is of type {@link IExtendedStep}, the step executor is calling
  * {@link IExtendedStep#initializeFrom(IAdaptable, IPropertiesContainer, IFullQualifiedId, IProgressMonitor)} and
  * {@link IExtendedStep#validateExecute(IAdaptable, IPropertiesContainer, IFullQualifiedId, IProgressMonitor)} before calling
  * {@link IStep#execute(IAdaptable, IPropertiesContainer, IFullQualifiedId, IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)}.
  * <p>
  * The methods will be called within the current step executor thread.
  * <p>
  * The stepper implementation can be traced and profiled by setting the debug options:
  * <ul>
  * <li><i>org.eclipse.tcf.te.runtime.stepper/trace/stepping</i></li>
  * <li><i>org.eclipse.tcf.te.runtime.stepper/profile/stepping</i></li>
  * </ul>
  */
 public class StepExecutor implements IStepExecutor {
 
 	private final IStepper stepper;
 
 	public final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
 
 	/**
      * Constructor.
      */
     public StepExecutor(IStepper stepper) {
     	this.stepper = stepper;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepExecutor#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStep, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public final void execute(final IStep step, final IFullQualifiedId id, final IStepContext context, final IPropertiesContainer data, IProgressMonitor progress) throws CoreException {
 		Assert.isNotNull(step);
 		Assert.isNotNull(id);
 		Assert.isNotNull(context);
 		Assert.isNotNull(data);
 		Assert.isNotNull(progress);
 
 		long startTime = System.currentTimeMillis();
 
 		CoreBundleActivator.getTraceHandler().trace("StepExecutor#execute: *** START (" + step.getLabel() + ")", //$NON-NLS-1$ //$NON-NLS-2$
 						0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
 		CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(startTime)) + "]" //$NON-NLS-1$ //$NON-NLS-2$
 						+ " ***", //$NON-NLS-1$
 						0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);
 
 		int ticksToUse = step.getTotalWork(context, data);
 		progress = ProgressHelper.getProgressMonitor(progress, ticksToUse);
 		Assert.isNotNull(progress);
 		ProgressHelper.beginTask(progress, step.getLabel(), ticksToUse);
 
 		// Create the handler (and the callback) for the current step
 		final Callback callback = new Callback();
 
 		// Catch any exception that might occur during execution.
 		// Errors are passed through by definition.
 		CoreException result = null;
 		boolean canceled = false;
 		try {
 			step.initializeFrom(context, data, id, progress);
 			step.validateExecute(context, data, id, progress);
 			step.execute(context, data, id, progress, callback);
 
 			IConditionTester conditionTester = new Callback.CallbackDoneConditionTester(callback, progress, step.getCancelTimeout()) {
 				boolean cancelCalled = false;
 				/* (non-Javadoc)
 				 * @see org.eclipse.tcf.te.runtime.callback.Callback.CallbackDoneConditionTester#isConditionFulfilled()
 				 */
 				@Override
 				public boolean isConditionFulfilled() {
 					if (!cancelCalled && monitor != null && monitor.isCanceled()) {
 						cancelCalled = true;
 						step.cancel(context, data, id, monitor);
 					}
 				    return super.isConditionFulfilled();
 				}
 			};
 			// Wait till the step finished, an execution occurred or the
 			// user hit cancel on the progress monitor.
 			ExecutorsUtil.waitAndExecute(0, conditionTester);
 
 			if (callback.getStatus() == null || callback.getStatus().isOK()) {
 				return;
 			}
 
 			if (callback.getStatus().matches(IStatus.CANCEL) || progress.isCanceled()) {
 				throw new OperationCanceledException(callback.getStatus().getMessage());
 			}
 
 			// Check the info/warning/error status of the step
 			result = normalizeStatus(step, id, context, data, callback.getStatus(), progress);
 		}
 		catch (OperationCanceledException e) {
 			CoreBundleActivator.getTraceHandler().trace("StepExecutor#execute: *** CANCEL (" + step.getLabel() + ")" //$NON-NLS-1$ //$NON-NLS-2$
 							+ ", message = '" + e.getMessage() + "'",  //$NON-NLS-1$ //$NON-NLS-2$
 							0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
 			canceled = true;
 			throw e;
 		}
 		catch (Exception e) {
 			result = normalizeStatus(step, id, context, data, StatusHelper.getStatus(e), progress);
 		}
 		finally {
 			if (!progress.isCanceled()) {
 				progress.done();
 			}
 
 			// Give the step a chance for cleanup
 			step.cleanup(context, data, id, progress);
 
 			long endTime = System.currentTimeMillis();
 			CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(endTime)) //$NON-NLS-1$
 							+ " , delay = " + (endTime - startTime) + " ms]" //$NON-NLS-1$ //$NON-NLS-2$
 							+ " ***", //$NON-NLS-1$
 							0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);
 
 			if (!canceled) {
 				if (result == null) {
 					CoreBundleActivator.getTraceHandler().trace("StepExecutor#execute: *** DONE (" + step.getLabel() + ")", //$NON-NLS-1$ //$NON-NLS-2$
 									0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
 				}
 				else {
 					CoreBundleActivator.getTraceHandler().trace("StepExecutor#execute: *** ERROR (" + step.getLabel() + ")" //$NON-NLS-1$ //$NON-NLS-2$
 									+ ", message = '" + result.getLocalizedMessage() + "'"  //$NON-NLS-1$ //$NON-NLS-2$
 									+ ", cause = " + result.getStatus().getException(),  //$NON-NLS-1$
 									0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
 					throw result;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Normalize the given status.
 	 *
 	 * @param step The step.
 	 * @param id The fully qualified id.
 	 * @param context The context.
 	 * @param data The step data.
 	 * @param status The status.
 	 *
 	 * @return CoreException if the operation failed
 	 */
 	private CoreException normalizeStatus(IStep step, IFullQualifiedId id, IStepContext context , IPropertiesContainer data, IStatus status, IProgressMonitor progress) {
 		Assert.isNotNull(context);
 		Assert.isNotNull(data);
 		Assert.isNotNull(id);
 		Assert.isNotNull(step);
 
 		String message = formatMessage(status, step, id, context, data);
 		status = new Status(status.getSeverity(), status.getPlugin(), status.getCode(), message != null ? message : status.getMessage(), status.getException());
 		return new CoreException(status);
 	}
 
 	/**
 	 * Format the message depending on the severity.
 	 *
 	 * @param message The message to format.
 	 * @param severity The message severity.
 	 * @param step The step.
 	 * @param id The full qualified step id.
 	 * @param context The target context.
 	 * @param data The step data.
 	 *
 	 * @return Formatted message.
 	 */
 	protected String formatMessage(IStatus status, IStep step, IFullQualifiedId id, IStepContext context, IPropertiesContainer data) {
 		String template = null;
 
 		switch (status.getSeverity()) {
 		case IStatus.INFO:
 			template = Messages.StepExecutor_info_stepFailed;
 			break;
 		case IStatus.WARNING:
 			template = Messages.StepExecutor_warning_stepFailed;
 			break;
 		case IStatus.ERROR:
 			template = Messages.StepExecutor_error_stepFailed;
 			break;
 		}
 
 		// If we cannot determine the formatted message template, just return the message as is
 		if (template == null) {
 			return status.getMessage();
 		}
 
 		// Format the core message
		String formattedMessage = NLS.bind(template,
						new String[] {
							stepper.getLabel(),
							status.getMessage(),
							(step.getLabel() != null && step.getLabel().trim().length() > 0 ? step.getLabel() : step.getId()),
							context.getName()
						});
 
 		// In debug mode, there is even more information to add
 		if (Platform.inDebugMode()) {
 			String date = DATE_FORMAT.format(new Date(System.currentTimeMillis()));

			formattedMessage += NLS.bind(Messages.StepExecutor_stepFailed_debugInfo, id.toString().replaceAll("/>", "/>\n"), date); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		return formattedMessage;
 	}
 }
