 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.structuremergeviewer;
 
 import java.util.ResourceBundle;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.CompareViewerPane;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.compare.diff.metamodel.ModelInputSnapshot;
 import org.eclipse.emf.compare.ui.AbstractCompareAction;
 import org.eclipse.emf.compare.ui.contentmergeviewer.ModelContentMergeViewer;
 import org.eclipse.emf.compare.ui.contentprovider.ModelStructureContentProvider;
 import org.eclipse.emf.compare.ui.util.EMFAdapterFactoryProvider;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.wizard.SaveDeltaWizard;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.OpenEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Compare and merge viewer with an area showing diffs as a structured tree.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelStructureMergeViewer extends TreeViewer {
 	private CompareConfiguration configuration;
 	
 	/**
 	 * Creates a new model structure merge viewer and intializes it.
 	 * 
 	 * @param parent
 	 * 			Parent composite for this viewer.
 	 * @param compareConfiguration
 	 * 			The configuration object.
 	 */
 	public ModelStructureMergeViewer(Composite parent, CompareConfiguration compareConfiguration) {
 		super(parent);
 		initialize(compareConfiguration);
 		createToolItems();
 	}
 	
 	/**
 	 * Returns the viewer's title.
 	 * 
 	 * @return
 	 * 			The viewer's title.
 	 * @see CompareUI#COMPARE_VIEWER_TITLE
 	 */
 	public String getTitle() {
 		return "Structural differences"; //$NON-NLS-1$
 	}
 	
 	protected void createToolItems() {
 		final ToolBarManager tbm = CompareViewerPane.getToolBarManager(getControl().getParent());
 		final ResourceBundle bundle = ResourceBundle.getBundle(ModelContentMergeViewer.BUNDLE_NAME);
 		final Action save = new AbstractCompareAction(bundle, "action.save.") { //$NON-NLS-1$
 			public void run() {
 				final SaveDeltaWizard wizard = new SaveDeltaWizard(bundle.getString("UI_SaveDeltaWizard_FileExtension")); //$NON-NLS-1$
 				wizard.init(PlatformUI.getWorkbench(),
 						(ModelInputSnapshot)getInput());
 				final WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
 				dialog.open();
 			}
 		};
 		final ActionContributionItem saveContribution = new ActionContributionItem(save);
 		saveContribution.setVisible(true);
 		tbm.add(new Separator("IO")); //$NON-NLS-1$
 		tbm.appendToGroup("IO", saveContribution); //$NON-NLS-1$
 		tbm.update(true);
 	}
 	
 	protected void inputChanged(Object input, Object oldInput) {
 		super.inputChanged(input, oldInput);
 		if (!(input instanceof ModelInputSnapshot) && input != oldInput) {
 			setInput(((ModelStructureContentProvider)getContentProvider()).getSnapshot());
 			configuration.setProperty(EMFCompareConstants.PROPERTY_INPUT_CHANGED, 
 					((ModelStructureContentProvider)getContentProvider()).getSnapshot());
 		}
 	}
 	
 	protected void fireOpen(OpenEvent event) {
		// DIRTY 
		// cancels open events that could be fired to avoid "NullViewer" opening.
 	}
 	
 	private void initialize(CompareConfiguration compareConfiguration) {
 		configuration = compareConfiguration;
 		setLabelProvider(new ModelStructureLabelProvider());
 		setUseHashlookup(true);
 		setContentProvider(new ModelStructureContentProvider(compareConfiguration));
 		
 		final Control tree = getControl();
 		tree.setData(CompareUI.COMPARE_VIEWER_TITLE, getTitle());
 		addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				configuration.setProperty(EMFCompareConstants.PROPERTY_STRUCTURE_SELECTION, event.getSelection());
 			}
 		});
 		
 		configuration.addPropertyChangeListener(new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().equals(EMFCompareConstants.PROPERTY_CONTENT_SELECTION)) {
 					expandAll();
 					final TreeItem item = (TreeItem)findItem(event.getNewValue());
 					collapseAll();
 					if (item != null) {
 						setSelection(new StructuredSelection(item.getData()), true);
 						expandToLevel(item.getData(), 0);
 					}
 				}
 			}
 		});
 	}
 	
 	/**
 	 * {@link LabelProvider} of this viewer.
 	 */
 	private class ModelStructureLabelProvider extends AdapterFactoryLabelProvider {
 		/**
 		 * Default constructor.
 		 */
 		public ModelStructureLabelProvider() {
 			super(EMFAdapterFactoryProvider.getAdapterFactory());
 		}
 
 		/**
 		 * Returns the platform icon for a file. You can replace with your own
 		 * icon if not a IFile, then pass it to the regular EMF.Edit providers.
 		 * 
 		 * @see AdapterFactoryLabelProvider#getImage(Object)
 		 */
 		public Image getImage(Object object) {
 			Image image = null;
 			if (object instanceof IFile) {
 				image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
 			} else {
 				image = super.getImage(object);
 				if (image == null && object instanceof EObject) {
 					if (EMFAdapterFactoryProvider.addAdapterFactoryFor((EObject)object)) {
 						setAdapterFactory(EMFAdapterFactoryProvider.getAdapterFactory());
 						image = super.getImage(object);
 					}
 				}
 			}
 			return image;
 		}
 
 		/**
 		 * Returns the name of the given {@link IFile}, delegates to 
 		 * {@link AdapterFactoryLabelProvider#getText(Object)} if not an 
 		 * {@link IFile}.
 		 * 
 		 * @see AdapterFactoryLabelProvider#getText(Object)
 		 */
 		public String getText(Object object) {
 			String text = null;
 			if (object instanceof IFile) {
 				text = ((IFile)object).getName();
 			} else {
 				text = super.getText(object);
 				if (text == null && object instanceof EObject) {
 					if (EMFAdapterFactoryProvider.addAdapterFactoryFor((EObject)object)) {
 						setAdapterFactory(EMFAdapterFactoryProvider.getAdapterFactory());
 						text = super.getText(object);
 					}
 				}
 			}
 			return super.getText(object);
 		}
 	}
 }
