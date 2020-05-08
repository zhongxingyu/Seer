 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.zendserver.monitor.internal.ui;
 
 import java.io.File;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.zend.core.notifications.ui.ActionType;
 import org.zend.core.notifications.ui.IActionListener;
 import org.zend.core.notifications.ui.IBody;
import org.zend.core.notifications.util.FontName;
import org.zend.core.notifications.util.Fonts;
 import org.zend.php.zendserver.monitor.core.EventSource;
 import org.zend.php.zendserver.monitor.ui.ICodeTraceEditorProvider;
 import org.zend.sdklib.application.ZendCodeTracing;
 import org.zend.sdklib.monitor.IZendIssue;
 import org.zend.webapi.core.connection.data.EventsGroupDetails;
 
 /**
  * Implementation of {@link IBody} for Zend Server event notification.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class EventBody implements IBody {
 
 	private static final String PROVIDER_EXTENSION = "org.zend.php.zendserver.monitor.ui.codeTracingEditor"; //$NON-NLS-1$
 
 	private IZendIssue zendIssue;
 	private String targetId;
 	private EventSource eventSource;
 
 	private IActionListener listener;
 	private static ICodeTraceEditorProvider editorProvider;
 
 	public EventBody(String targetId, EventSource eventSource,
 			IZendIssue zendIssue) {
 		this.zendIssue = zendIssue;
 		this.targetId = targetId;
 		this.eventSource = eventSource;
 	}
 
 	@Override
 	public Composite createContent(Composite container) {
 		Composite composite = createEntryComposite(container);
 		createDescription(composite);
 		createRepeatLink(composite);
 		createSourceLink(composite);
 		createTraceLink(composite);
 		return composite;
 	}
 
 	@Override
 	public void addActionListener(IActionListener listener) {
 		this.listener = listener;
 	}
 
 	private void createTraceLink(Composite composite) {
 		if (getProvider() != null) {
 			Link traceLink = createLink(composite,
 					getLinkText(Messages.EventBody_CodetraceLink));
 			traceLink.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent event) {
 					Job showCodeTraceJob = new Job(
 							Messages.EventBody_CodetraceJobTitle) {
 
 						@Override
 						protected IStatus run(IProgressMonitor monitor) {
 							List<EventsGroupDetails> groups = zendIssue
 									.getGroupDetails();
 							if (groups != null && groups.size() == 1) {
 								String traceId = groups.get(0).getCodeTracing();
 								if (traceId != null) {
 									ZendCodeTracing tracing = new ZendCodeTracing(
 											targetId);
 									File codeTrace = tracing.get(traceId);
 									if (codeTrace != null) {
 										getProvider().openInEditor(
 												codeTrace.getAbsolutePath());
 									} else {
 										return new Status(
 												IStatus.ERROR,
 												Activator.PLUGIN_ID,
 												Messages.EventBody_CodetraceJobErrorMessage);
 									}
 								}
 							}
 							return Status.OK_STATUS;
 						}
 					};
 					showCodeTraceJob.setSystem(true);
 					showCodeTraceJob.schedule();
 				}
 			});
 		}
 	}
 
 	private void createSourceLink(Composite composite) {
 		Link sourceLink = createLink(composite,
 				getLinkText(Messages.EventBody_SourceLink));
 		sourceLink.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				OpenInEditorJob job = new OpenInEditorJob(eventSource);
 				job.setUser(true);
 				job.schedule();
 			}
 		});
 	}
 
 	private void createRepeatLink(Composite composite) {
 		Link repeatLink = createLink(composite,
 				getLinkText(Messages.EventBody_2));
 		repeatLink.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				Job job = new RequestGeneratorJob(zendIssue);
 				job.setUser(true);
 				job.schedule();
 				if (listener != null) {
 					listener.performAction(ActionType.HIDE);
 				}
 			}
 		});
 	}
 
 	private String getLinkText(String text) {
 		return "<a>" + text + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	private Link createLink(Composite parent, String text) {
		Link link = new Link(parent, SWT.NO_FOCUS);
 		link.setText(text);
 		link.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
 		return link;
 	}
 
 	private void createDescription(Composite composite) {
 		Label label = new Label(composite, SWT.WRAP);
		label.setFont(Fonts.get(FontName.DEFAULT));
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 3, 1));
 		label.setForeground(Display.getDefault()
 				.getSystemColor(SWT.COLOR_BLACK));
 		label.setText(zendIssue.getIssue().getGeneralDetails().getErrorString());
 	}
 
 	private Composite createEntryComposite(Composite container) {
 		Composite composite = new Composite(container, SWT.NONE);
 		GridLayout layout = new GridLayout(3, true);
 		layout.horizontalSpacing = layout.verticalSpacing = 2;
 		composite.setLayout(layout);
 		return composite;
 	}
 
 	private static ICodeTraceEditorProvider getProvider() {
 		if (editorProvider == null) {
 			IConfigurationElement[] elements = Platform.getExtensionRegistry()
 					.getConfigurationElementsFor(PROVIDER_EXTENSION);
 			for (IConfigurationElement element : elements) {
 				if ("codeTracingEditor".equals(element.getName())) { //$NON-NLS-1$
 					try {
 						Object listener = element
 								.createExecutableExtension("class"); //$NON-NLS-1$
 						if (listener instanceof ICodeTraceEditorProvider) {
 							editorProvider = (ICodeTraceEditorProvider) listener;
 							break;
 						}
 					} catch (CoreException e) {
 						Activator.log(e);
 					}
 				}
 			}
 		}
 		return editorProvider;
 	}
 
 }
