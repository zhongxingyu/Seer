 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
 *    mwenz - Bug 324859 - Need Undo/Redo support for Non-EMF based domain objects
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.command;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureAndContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.CustomContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.func.ICreateInfo;
 import org.eclipse.graphiti.internal.DefaultFeatureAndContext;
 import org.eclipse.graphiti.internal.command.CommandExec;
 import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.datatypes.impl.LocationImpl;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.editor.DiagramEditorInternal;
 import org.eclipse.graphiti.ui.internal.util.ui.PopupMenu;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class CreateConnectionCommand extends AbstractCommand {
 
 	/** The connection instance. */
 	// private Connection connection;
 	private List<IFeature> features;
 
 	// the location when connection is dropped on canvas
 	private Point location;
 
 	// the location where new connection starts
 	private ILocation sourceLocation;
 
 	/** Start endpoint for the connection. */
 	private final PictogramElement sourceObject;
 
 	/** Target endpoint for the connection. */
 	private PictogramElement targetObject;
 
 	/**
 	 * Instantiate a command that can create a connection between two anchors.
 	 * 
 	 * @param source
 	 *            the source endpoint (a non-null Shape instance)
 	 * @param lineStyle
 	 *            the desired line style. See Connection#setLineStyle(int) for
 	 *            details
 	 * @throws IllegalArgumentException
 	 *             if source is null
 	 * @see Connection#setLineStyle(int)
 	 */
 	public CreateConnectionCommand(IConfigurationProvider configurationProvider, PictogramElement pe, List<IFeature> features) {
 		super(configurationProvider);
 		setLabel(Messages.CreateConnectionCommand_0_xmsg);
 
 		this.features = features;
 		this.sourceObject = pe;
 	}
 
 	@Override
 	public boolean canExecute() {
 
 		// allow connections only from anchor to anchor
 		Anchor sourceAnchor = getAnchor(sourceObject);
 		Anchor targetAnchor = getAnchor(targetObject);
 
 		// if (sourceAnchor != null) {
 
 		CreateConnectionContext connectionContext = new CreateConnectionContext();
 		connectionContext.setSourceAnchor(sourceAnchor);
 		connectionContext.setTargetAnchor(targetAnchor);
 		connectionContext.setSourcePictogramElement(sourceObject);
 		connectionContext.setTargetPictogramElement(targetObject);
 		connectionContext.setTargetLocation(getCurrentLocation());
 		connectionContext.setSourceLocation(sourceLocation);
 
 		CustomContext customContext = new CustomContext();
 		customContext.setPictogramElements(new PictogramElement[] { sourceObject, targetObject });
 		customContext.setX(location.x);
 		customContext.setY(location.y);
 
 		IContext context = null;
 
 		for (IFeature feature : features) {
 
 			if (feature instanceof ICreateConnectionFeature)
 				context = connectionContext;
 			else
 				context = customContext;
 
 			GenericFeatureCommandWithContext ccc = new GenericFeatureCommandWithContext(feature, context);
 
 			if (ccc.canExecute())
 				return true;
 
 		}
 
 		return false;
 	}
 
 	@Override
 	public void execute() {
 		// create a new connection between source- and target-anchor
 		Anchor sourceAnchor = getAnchor(sourceObject);
 		Anchor targetAnchor = getAnchor(targetObject);
 
 		// if (sourceAnchor != null) {
 		CreateConnectionContext connectionContext = new CreateConnectionContext();
 		connectionContext.setSourceAnchor(sourceAnchor);
 		connectionContext.setTargetAnchor(targetAnchor);
 		connectionContext.setSourcePictogramElement(sourceObject);
 		connectionContext.setTargetPictogramElement(targetObject);
 		connectionContext.setTargetLocation(getCurrentLocation());
 		connectionContext.setSourceLocation(sourceLocation);
 
 		CustomContext customContext = new CustomContext();
 		customContext.setPictogramElements(new PictogramElement[] { sourceObject, targetObject });
 
 		DiagramEditorInternal diagramEditor = (DiagramEditorInternal) getFeatureProvider().getDiagramTypeProvider().getDiagramEditor();
 		Point newLocation = diagramEditor.calculateRealMouseLocation(location);
 		customContext.setLocation(newLocation.x, newLocation.y);
 
 		List<GenericFeatureCommandWithContext> commands = new ArrayList<GenericFeatureCommandWithContext>();
 
 		IContext context = null;
 
 		for (IFeature feature : features) {
 
 			if (feature instanceof ICreateConnectionFeature)
 				context = connectionContext;
 			else
 				context = customContext;
 
 			GenericFeatureCommandWithContext ccc = new GenericFeatureCommandWithContext(feature, context);
 
 			if (ccc.canExecute())
 				commands.add(ccc);
 
 		}
 		if (commands.size() == 0)
 			return;
 
 		if (commands.size() == 1) {
 
 			CommandExec.getSingleton().executeCommand(commands.get(0), getTransactionalEditingDomain());
 			return;
 		}
 
 		PopupMenu popupMenu = new PopupMenu(commands, labelProvider);
 
 		boolean b = popupMenu.show(Display.getCurrent().getActiveShell());
 
 		if (b) {
 			GenericFeatureCommandWithContext result = (GenericFeatureCommandWithContext) popupMenu.getResult();
 			CommandExec.getSingleton().executeCommand(result, getTransactionalEditingDomain());
 
 		}
 
 	}
 
 	public boolean canStartConnection() {
 		// allow connections only from anchor to anchor
 
 		CreateConnectionContext connectionContext = createContext();
 
 		for (IFeature feature : features) {
 
 			if (feature instanceof ICreateConnectionFeature) {
 				ICreateConnectionFeature ccf = (ICreateConnectionFeature) feature;
 				if (ccf.canStartConnection(connectionContext)) {
 					return true;
 				}
 			} else
 				return true;
 		}
 
 		return false;
 	}
 
 	private CreateConnectionContext createContext() {
 		Anchor sourceAnchor = getAnchor(sourceObject);
 		Anchor targetAnchor = null;
 
 		CreateConnectionContext connectionContext = new CreateConnectionContext();
 		connectionContext.setSourceAnchor(sourceAnchor);
 		connectionContext.setTargetAnchor(targetAnchor);
 		connectionContext.setSourcePictogramElement(sourceObject);
 		connectionContext.setTargetPictogramElement(null);
 		connectionContext.setTargetLocation(null);
		sourceLocation = getCurrentLocation(); // store location for later usage
		connectionContext.setSourceLocation(sourceLocation);
 		return connectionContext;
 	}
 
 	@Override
 	public void redo() {
 		// connection.reconnect();
 	}
 
 	/**
 	 * Set the target endpoint for the connection.
 	 * 
 	 * @param target
 	 *            that target endpoint (a non-null Shape instance)
 	 * @throws IllegalArgumentException
 	 *             if target is null
 	 */
 	public void setTarget(PictogramElement pe) {
 		targetObject = pe;
 	}
 
 	@Override
 	public void undo() {
 		// connection.disconnect();
 	}
 
 	private Anchor getAnchor(PictogramElement pe) {
 		Anchor ret = null;
 		if (pe instanceof Anchor) {
 			ret = (Anchor) pe;
 		} else if (pe instanceof AnchorContainer) {
 			ret = Graphiti.getPeService().getChopboxAnchor((AnchorContainer) pe);
 		}
 		return ret;
 	}
 
 	public PictogramElement getSourceObject() {
 		return sourceObject;
 	}
 
 	/**
 	 * sets the location for the command when connection is dropped into nowhere
 	 * 
 	 * @param location
 	 */
 	public void setLocation(Point location) {
 		this.location = location;
 	}
 
 	/**
 	 * label provider for popup menu
 	 */
 	private static ILabelProvider labelProvider = new ILabelProvider() {
 
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		public void dispose() {
 
 		}
 
 		public void addListener(ILabelProviderListener listener) {
 
 		}
 
 		public String getText(Object element) {
 
 			GenericFeatureCommandWithContext command = (GenericFeatureCommandWithContext) element;
 
 			IFeature feature = command.getFeature();
 
 			if (feature instanceof ICreateInfo) // e.g. ICreateConnectionFeature
 				return ((ICreateInfo) feature).getCreateName();
 			if (feature instanceof ICustomFeature)
 				return ((ICustomFeature) feature).getName();
 
 			return null;
 		}
 
 		public Image getImage(Object element) {
 			GenericFeatureCommandWithContext command = (GenericFeatureCommandWithContext) element;
 
 			IFeature feature = command.getFeature();
 			if (feature instanceof ICreateInfo) // e.g. ICreateConnectionFeature
 				return GraphitiUi.getImageService().getImageForId(((ICreateInfo) feature).getCreateImageId());
 			if (feature instanceof ICustomFeature)
 				return GraphitiUi.getImageService().getImageForId(((ICustomFeature) feature).getImageId());
 
 			return null;
 		}
 
 	};
 
 	public IFeatureAndContext[] getFeaturesAndContexts() {
 		List<IFeatureAndContext> featureList = new ArrayList<IFeatureAndContext>(features.size());
 
 		CreateConnectionContext context = createContext();
 
 		for (Iterator<IFeature> iterator = features.iterator(); iterator.hasNext();) {
 			IFeature feature = iterator.next();
 			featureList.add(new DefaultFeatureAndContext(feature, context));
 		}
 
 		return featureList.toArray(new IFeatureAndContext[featureList.size()]);
 	}
 
 	private ILocation getCurrentLocation() {
 		if (location == null) {
 			return null;
 		}
 		DiagramEditorInternal diagramEditor = (DiagramEditorInternal) getFeatureProvider().getDiagramTypeProvider().getDiagramEditor();
 		Point realLocation = diagramEditor.calculateRealMouseLocation(location);
 		ILocation currentLocation = new LocationImpl(realLocation.x, realLocation.y);
 		return currentLocation;
 	}
 }
