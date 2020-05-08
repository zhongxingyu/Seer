 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.bridge.ide.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ResourceTransfer;
 
 /**
  * An default {@link IEditorRendererExtension} providing extension do drop {@link IFile}s in the IntentEditor,
  * open editors with FileEditorInputs...
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class DefaultIDEEditorRendererExtension implements IEditorRendererExtension {
 
 	private static final int IMAGE_HEIGHT_MARGIN = 5;
 
 	private static final int IMAGE_WIDTH = 800;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension#isRendererFor(org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference)
 	 */
 	public boolean isRendererFor(ExternalContentReference externalContentReference) {
 		// Can be applied on any external content reference (with low priority)
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension#openEditor(org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference)
 	 */
 	public boolean openEditor(ExternalContentReference externalContentReference) {
 		URI resourceToOpenURI = URI.createURI(externalContentReference.getUri().toString().trim())
 				.trimFragment();
 
 		if (resourceToOpenURI.isPlatformResource()) {
 			String filePath = resourceToOpenURI.toPlatformString(true);
 
 			// Open editor with a file editor input
 			IFile resourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(filePath));
 			FileEditorInput editorInput = new FileEditorInput(resourceFile);
 			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
 					.getDefaultEditor(resourceToOpenURI.lastSegment());
 
 			if (desc != null) {
 				try {
 					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
 							.openEditor(editorInput, desc.getId());
 				} catch (PartInitException e) {
 					IntentUiLogger.logError(e);
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension#getAdditionalTransfers()
 	 */
 	public Collection<Transfer> getAdditionalTransfers() {
 		// Adding the Resource transfer to be able to directly drop IFiles inside the Intent editor
 		Collection<Transfer> transfers = new ArrayList<Transfer>();
 		transfers.add(ResourceTransfer.getInstance());
 		transfers.add(LocalSelectionTransfer.getTransfer());
 		return transfers;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension#getEObjectsFromDropTargetEvent(org.eclipse.swt.dnd.DropTargetEvent)
 	 */
 	public Collection<? extends EObject> getEObjectsFromDropTargetEvent(DropTargetEvent event) {
 		Collection<EObject> eObjects = new LinkedHashSet<EObject>();
 		try {
 			Collection<Resource> emfResources = new LinkedHashSet<Resource>();
 			// If directly dropping an IFile
 			if (event.data instanceof IResource[]) {
 				IResource[] droppedResources = (IResource[])event.data;
 				for (int i = 0; i < droppedResources.length; i++) {
 					emfResources.add(getEMFResourceFromIResource(droppedResources[i]));
 				}
 			} else if (event.data instanceof IStructuredSelection) {
 				// If dropping a selection of IFiles
 				Iterator iterator = ((IStructuredSelection)event.data).iterator();
 				while (iterator.hasNext()) {
 					Object element = iterator.next();
 					if (element instanceof IResource) {
 						emfResources.add(getEMFResourceFromIResource((IResource)element));
 					}
 				}
 			}
 			for (Resource emfResource : emfResources) {
 				if (emfResource != null && emfResource.getContents().iterator().hasNext()) {
 					eObjects.add(emfResource.getContents().iterator().next());
 				}
 			}
 		} catch (IllegalArgumentException e) {
 			// Nothing to do, the drop event was not valid for this extension
 		}
 		return eObjects;
 	}
 
 	/**
 	 * Returns an emfResource corresponding to the given {@link IResource} (if any).
 	 * 
 	 * @param iResource
 	 *            the {@link IResource} to analyse
 	 * @return an emfResource corresponding to the given {@link IResource}, or null if none find
 	 */
 	private Resource getEMFResourceFromIResource(IResource iResource) {
 		// Try to get the EMF ressource corresponding to this file
 		URI resourceURI = URI.createPlatformResourceURI(iResource.getFullPath().toString(), true);
 		try {
 			return new ResourceSetImpl().getResource(resourceURI, true);
			// CHECKSTYLE:OFF
 		} catch (Exception e) {
			// CHECKSTYLE:ON
 			IntentUiLogger.logError("Could not perform drop of the resource at URI " + resourceURI
 					+ " because a bridge to represent such files as models is missing", e);
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension#getImage(org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference)
 	 */
 	public Image getImage(ExternalContentReference externalContentReference) {
 		Image image = null;
 		if (externalContentReference.getExternalContent() != null) {
 
 			// Determine string to render
 			IItemLabelProvider labeProvider = (IItemLabelProvider)new ComposedAdapterFactory(
 					ComposedAdapterFactory.Descriptor.Registry.INSTANCE).adapt(
 					externalContentReference.getExternalContent(), IItemLabelProvider.class);
 			if (labeProvider != null) {
 				String label = labeProvider.getText(externalContentReference.getExternalContent());
 
 				Display display = Display.getDefault();
 				int fontHeight = new GC(new Image(display, 1, 1)).getFontMetrics().getHeight();
 				int imageHeight = fontHeight + IMAGE_HEIGHT_MARGIN;
 				image = new Image(display, IMAGE_WIDTH, imageHeight);
 				GC gc = new GC(image);
 
 				// Render icon
 				int iconWidth = 5;
 				gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
 
 				Object iconURL = labeProvider.getImage(externalContentReference.getExternalContent());
 				Image icon = ExtendedImageRegistry.getInstance().getImage(iconURL);
 
 				if (icon != null) {
 					gc.drawImage(icon, 0, 5);
 					iconWidth += icon.getImageData().width + 2;
 				}
 				// Render text
 				gc.drawText(label, iconWidth, 5);
 				gc.dispose();
 			}
 
 		}
 
 		return image;
 	}
 
 }
