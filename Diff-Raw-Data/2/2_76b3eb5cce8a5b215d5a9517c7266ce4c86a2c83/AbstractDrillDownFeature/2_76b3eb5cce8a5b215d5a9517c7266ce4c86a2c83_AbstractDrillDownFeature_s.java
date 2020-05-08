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
  *    mwenz - Bug  338067 - Drill down features should not use current diagram type provider
  *                          for opening diagram of other type
 *    Ali Akar, mwenz - Bug 348420 - Opening a user contributed editor
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.features;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.internal.Messages;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IPlatformImageConstants;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.editor.DiagramEditorFactory;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ListDialog;
 
 /**
  * The Class AbstractDrillDownFeature.
  */
 public abstract class AbstractDrillDownFeature extends AbstractCustomFeature {
 
 	private static final String NAME = Messages.AbstractDrillDownFeature_0_xfld;
 
 	/**
 	 * Creates a new {@link AbstractDrillDownFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public AbstractDrillDownFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	public void execute(ICustomContext context) {
 		final PictogramElement pe = context.getPictogramElements()[0];
 		final Collection<Diagram> possibleDiagramsList = getLinkedDiagrams(pe);
 
 		Diagram diagram = null;
 
 		if (!possibleDiagramsList.isEmpty()) {
 			final Diagram[] possibleDiagrams = possibleDiagramsList.toArray(new Diagram[0]);
 			if (possibleDiagramsList.size() == 1) {
 				diagram = possibleDiagrams[0];
 			} else {
 				ListDialog dialog = new ListDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
 				dialog.setContentProvider(new IStructuredContentProvider() {
 
 					@Override
 					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 					}
 
 					@Override
 					public void dispose() {
 					}
 
 					@Override
 					public Object[] getElements(Object inputElement) {
 						return possibleDiagramsList.toArray();
 					}
 				});
 				dialog.setTitle(Messages.AbstractDrillDownFeature_1_xmsg);
 				dialog.setMessage(Messages.AbstractDrillDownFeature_2_xmsg);
 				dialog.setInitialSelections(new Diagram[] { possibleDiagrams[0] });
 				dialog.setLabelProvider(new DiagramLabelProvider());
 				dialog.setAddCancelButton(true);
 				dialog.setHelpAvailable(false);
 				dialog.setInput(new Object());
 				dialog.open();
 				Object[] result = dialog.getResult();
 				if (result != null) {
 					for (int i = 0; i < result.length; i++) {
 						diagram = (Diagram) result[i];
 					}
 				}
 			}
 
 			if (diagram != null) {
 				openDiagramEditor(diagram);
 			}
 		}
 	}
 
 	/**
 	 * Opens the diagram editor for the drill down. Users can override this
 	 * method in order to open different editors, open editors in different
 	 * windows or do completely different stuff for the drill down.
 	 * 
 	 * @param diagram
 	 *            the diagram for which the editor will be opened.
 	 */
 	protected void openDiagramEditor(Diagram diagram) {
 		// Found a diagram to open
 		String diagramTypeProviderId = GraphitiUi.getExtensionManager().getDiagramTypeProviderId(diagram.getDiagramTypeId());
 		GraphitiUiInternal.getWorkbenchService().openDiagramEditor(diagram, getTransActionalEditingDomainForNewDiagram(),
 				diagramTypeProviderId, getDiagramEditorId(diagram), true);
 	}
 
 	/**
 	 * Returns the editor id of the diagram editor to open by default
 	 * {@link DiagramEditor#DIAGRAM_EDITOR_ID}. Users can override and return a
 	 * different editor id, must be the id of a subclass of
 	 * {@link DiagramEditor}.
 	 * 
 	 * @param diagram
 	 *            the diagram for which the editor will be opened
 	 * @return the editor id of the diagram editor to open
 	 */
 	protected String getDiagramEditorId(Diagram diagram) {
 		return DiagramEditor.DIAGRAM_EDITOR_ID;
 	}
 
 	@Override
 	public boolean canExecute(ICustomContext context) {
 		final PictogramElement[] pes = context.getPictogramElements();
 		if (pes != null && pes.length == 1) {
 			final PictogramElement pe = context.getPictogramElements()[0];
 			final Collection<Diagram> possibleDiagramsList = getLinkedDiagrams(pe);
 
 			return possibleDiagramsList.size() >= 1;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.features.impl.AbstractFeature#hasDoneChanges()
 	 */
 	@Override
 	public boolean hasDoneChanges() {
 		return false;
 	}
 
 	/**
 	 * Returns a list of all diagrams, which are connected to the given
 	 * pictogram element. This means, that the diagrams and the pictogram
 	 * element have at least one linked business object in common.
 	 * 
 	 * @param pe
 	 *            The pictogram element for which to return the connected
 	 *            diagrams.
 	 * @return A list of all diagrams, which are connected to the given
 	 *         pictogram element.
 	 */
 	protected Collection<Diagram> getLinkedDiagrams(PictogramElement pe) {
 		final Collection<Diagram> ret = new HashSet<Diagram>();
 
 		final Object[] businessObjectsForPictogramElement = getAllBusinessObjectsForPictogramElement(pe);
 
 		final Collection<Diagram> allDiagrams = getDiagrams();
 		for (final Diagram d : allDiagrams) {
 			final Diagram currentDiagram = getDiagram();
 			if (!EcoreUtil.equals(currentDiagram, d)) { // always filter out the current
 				// diagram
 				final Object[] businessObjectsForDiagram = getAllBusinessObjectsForPictogramElement(d);
 				for (int i = 0; i < businessObjectsForDiagram.length; i++) {
 					final Object diagramBo = businessObjectsForDiagram[i];
 					for (int j = 0; j < businessObjectsForPictogramElement.length; j++) {
 						final Object currentBo = businessObjectsForPictogramElement[j];
 						if (EcoreUtil.equals((EObject) currentBo, (EObject) diagramBo)) {
 							ret.add(d);
 						}
 					}
 				}
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Returns all diagrams which are considered for navigation.
 	 * 
 	 * @return the diagrams
 	 */
 	protected abstract Collection<Diagram> getDiagrams();
 
 	/**
 	 * The Class DiagramLabelProvider.
 	 */
 	private class DiagramLabelProvider extends LabelProvider {
 
 		Image image;
 
 		/**
 		 * Instantiates a new diagram label provider.
 		 */
 		public DiagramLabelProvider() {
 			super();
 		}
 
 		@Override
 		public String getText(Object o) {
 			String ret = null;
 			if (o instanceof Diagram) {
 				Diagram diagram = (Diagram) o;
 				ret = diagram.getName() + " (" + diagram.getDiagramTypeId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			return ret;
 		}
 
 		@Override
 		public Image getImage(Object element) {
 			if (this.image == null) {
 				this.image = GraphitiUi.getImageService().getImageForId(IPlatformImageConstants.IMG_DIAGRAM);
 			}
 			return this.image;
 		}
 
 	}
 
 	protected TransactionalEditingDomain getTransActionalEditingDomainForNewDiagram() {
 		return DiagramEditorFactory.createResourceSetAndEditingDomain();
 	}
 
 }
