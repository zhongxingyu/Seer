 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2013 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 371527 - Recursive attempt to activate part while in the middle of activating part
  *    mwenz - Bug 370888 - API Access to export and print
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.action;
 
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.ui.actions.PrintAction;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IPrintFeature;
 import org.eclipse.graphiti.features.context.IPrintContext;
 import org.eclipse.graphiti.features.context.impl.PrintContext;
 import org.eclipse.graphiti.internal.command.FeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.ICommand;
 import org.eclipse.graphiti.platform.IDiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 
 /**
  * An Action, which is used to print the contents of the GraphicalViewer.
  * <p>
  * Additional to the usual printing behaviour (the dialog, where the printer can
  * be selected), this Action also adds another dialog, where the size and
  * position of the graphics on the paper can be specified.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class PrintGraphicalViewerAction extends PrintAction {
 
 	/**
 	 * This static Action is just used as a template, to initialize the ID,
 	 * label and image of instances of this class accordingly.
 	 */
 	private static IAction TEMPLATE_ACTION = ActionFactory.PRINT.create(PlatformUI.getWorkbench()
 			.getActiveWorkbenchWindow());
 
 	private IConfigurationProvider configurationProvider;
 
 	// last time when we checked whether a printer is available with
 	// super.calculateEnabled()
 	private long lastPrinterCheckTime = 0;
 
 	private boolean cachedEnabled = true;
 
 	/**
 	 * Creates a new PrintGraphicalViewerAction. It initializes it with the
 	 * proper ID, label and image.
 	 * 
 	 * @param configurationProvider
 	 *            The IConfigurationProviderInternal.
 	 * @param part
 	 *            The WorkbenchPart (e.g. the editor), to which this Action
 	 *            belongs. From the WorkbenchPart the GraphicalViewer will be
 	 *            determined.
 	 */
 	public PrintGraphicalViewerAction(IDiagramBehavior diagramBehavior, IConfigurationProvider configurationProvider) {
		super(((DiagramEditor) diagramBehavior.getDiagramContainer()).getWorkbenchPart());
 		this.configurationProvider = configurationProvider;
 
 		// set all values of the TEMPLATE_ACTION for this Action.
 		setId(TEMPLATE_ACTION.getId());
 		setText(TEMPLATE_ACTION.getText());
 		setToolTipText(TEMPLATE_ACTION.getToolTipText());
 		setDescription(TEMPLATE_ACTION.getDescription());
 		setAccelerator(TEMPLATE_ACTION.getAccelerator());
 		setHelpListener(TEMPLATE_ACTION.getHelpListener());
 		setImageDescriptor(TEMPLATE_ACTION.getImageDescriptor());
 		setHoverImageDescriptor(TEMPLATE_ACTION.getHoverImageDescriptor());
 		setDisabledImageDescriptor(TEMPLATE_ACTION.getDisabledImageDescriptor());
 	}
 
 	/**
 	 * Same as super.calculateEnabled(), except that it also checks, if the
 	 * current WorkbenchPart has a GraphicalViewer.
 	 */
 	@Override
 	protected boolean calculateEnabled() {
 		IFeatureProvider featureProvider = getFeatureProvider();
 		if (featureProvider == null) {
 			return false;
 		}
 		IPrintFeature feature = featureProvider.getPrintFeature();
 		IPrintContext context = createPrintContext();
 		if (feature == null || !feature.canPrint(context)) {
 			return false;
 		}
 
 		if (getWorkbenchPart().getAdapter(GraphicalViewer.class) == null) {
 			return false;
 		}
 
 		long currentTime = System.currentTimeMillis();
 		long diffTime = (currentTime - lastPrinterCheckTime) / 1000;
 
 		// super.calculateEnabled() only checks whether a printer is
 		// available. But calculateEnabled() is called very often and in
 		// some environments this can lead to performance issues. See also
 		// bugzilla 355401. Therefore we cache the result and call the super
 		// method earliest after 5 minutes.
 		if (diffTime > 300) {
 			lastPrinterCheckTime = currentTime;
 
 			// Fix for Bug 371527: Do not trigger the super call directly,
 			// because on some systems (e.g. Linux) it will take some time. In
 			// between the event loop will be polled and another activation
 			// request (user mouse click on another part) might come in. In this
 			// case a recursive attempt to activate part runtime exception will
 			// be thrown; instead trigger the enablement check via a async call
 			// so that the current activation can return immediately. On other
 			// systems this is not really needed but does not harm because the
 			// cachedEnabled is filled directly after the activation returns
 			// instead of immediately which makes no difference to the user
 			Display.getCurrent().asyncExec(new Runnable() {
 				public void run() {
 					cachedEnabled = PrintGraphicalViewerAction.super.calculateEnabled();
 				}
 			});
 		}
 		return cachedEnabled;
 	}
 
 	/**
 	 * Prints the GraphicalViewer of the WorkbenchPart. It is the same as
 	 * super.run(), except that it opens the PrintModeDialog before printing. In
 	 * this dialog the size of the graphics (fit-width, fit-height, ...) can be
 	 * specified.
 	 */
 	@Override
 	public void run() {
 		IPrintContext context = createPrintContext();
 		final IFeatureProvider featureProvider = getFeatureProvider();
 		IPrintFeature feature = featureProvider.getPrintFeature();
 		if (feature != null) {
 			final FeatureCommandWithContext command = new GenericFeatureCommandWithContext(feature, context);
 			executeOnCommandStack(command);
 		}
 	}
 
 	private IPrintContext createPrintContext() {
 		PrintContext context = new PrintContext();
 		return context;
 	}
 
 	private IFeatureProvider getFeatureProvider() {
 		return configurationProvider.getDiagramTypeProvider().getFeatureProvider();
 	}
 
 	private void executeOnCommandStack(ICommand command) {
 		CommandStack commandStack = configurationProvider.getDiagramEditor().getEditDomain().getCommandStack();
 		GefCommandWrapper wrapperCommand = new GefCommandWrapper(command, configurationProvider.getDiagramBehavior()
 				.getEditingDomain());
 		commandStack.execute(wrapperCommand);
 	}
 }
