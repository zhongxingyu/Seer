 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.ide;
 
 import java.io.StringReader;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension4;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.editors.text.ILocationProvider;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.xml.sax.InputSource;
 
 import com.swtxml.swt.SwtXmlParser;
 
 @SuppressWarnings("restriction")
 public class PreviewViewPart extends ViewPart {
 
 	private IEditorPart trackedPart;
 
 	private final IPropertyListener updatePreviewOnSave = new IPropertyListener() {
 
 		public void propertyChanged(Object source, int propId) {
 			if (propId == IEditorPart.PROP_DIRTY) {
 				if (!trackedPart.isDirty()) {
 					updatePreview();
 				}
 			}
 		}
 
 	};
 
 	private final IPartListener trackRelevantEditorsPartListener = new IPartListener() {
 
 		public void partActivated(IWorkbenchPart part) {
 			tryConnectTo(part);
 		}
 
 		public void partBroughtToTop(IWorkbenchPart part) {
 		}
 
 		public void partClosed(IWorkbenchPart part) {
 			if (part == trackedPart) {
 				clearConnection();
 			}
 		}
 
 		public void partDeactivated(IWorkbenchPart part) {
 		}
 
 		public void partOpened(IWorkbenchPart part) {
 		}
 
 	};
 
 	private long lastPreviewModificationStamp;
	private Composite content;

	private Composite parent;
 
 	@Override
 	public void createPartControl(Composite parent) {
 		this.parent = parent;
		content = new Composite(parent, SWT.None);
		content.setLayout(new FillLayout());
 		getSite().getPage().addPartListener(trackRelevantEditorsPartListener);
 		tryConnectTo(getSite().getPage().getActiveEditor());
 	}
 
 	@Override
 	public void dispose() {
 		getSite().getPage().removePartListener(trackRelevantEditorsPartListener);
 		clearConnection();
 		super.dispose();
 	}
 
 	private void tryConnectTo(IWorkbenchPart part) {
 		if (part != trackedPart && part instanceof IEditorPart) {
 			IStructuredModel model = (IStructuredModel) part.getAdapter(IStructuredModel.class);
 			if (model != null
 					&& SwtXmlModelHandler.associatedContentTypeID.equals(model
 							.getContentTypeIdentifier())) {
 				connectTo(part);
 			}
 		}
 	}
 
 	private void connectTo(IWorkbenchPart part) {
 		if (trackedPart != null) {
 			clearConnection();
 		}
 		trackedPart = (IEditorPart) part;
 		part.addPropertyListener(updatePreviewOnSave);
 		updatePreview();
 	}
 
 	public void clearConnection() {
 		trackedPart.removePropertyListener(updatePreviewOnSave);
 		clearPreview();
 	}
 
 	private void clearPreview() {
		content.dispose();
 	}
 
 	private void updatePreview() {
 		IDocument doc = (IDocument) trackedPart.getAdapter(IDocument.class);
 		IDocumentExtension4 document = (IDocumentExtension4) doc;
 		if (document.getModificationStamp() != lastPreviewModificationStamp) {
 			try {
				Composite oldContent = content;
				content = new Composite(parent, SWT.None);
				content.setLayout(new FillLayout());
 				IEditorInput editorInput = trackedPart.getEditorInput();
 				ILocationProvider locationProvider = (ILocationProvider) editorInput
 						.getAdapter(ILocationProvider.class);
 				String filename = (locationProvider != null) ? locationProvider
 						.getPath(editorInput).toFile().getName() : "unknown";
				new SwtXmlParser(content, null).parse(filename, new InputSource(new StringReader(
						doc.get())));
				oldContent.dispose();
 			} catch (Exception e) {
 				Activator.getDefault().getLog().log(
 						new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
 				clearPreview();
 				Label errorLabel = new Label(parent, SWT.NONE);
 				errorLabel.setText(e.getMessage());
 			} finally {
 				lastPreviewModificationStamp = document.getModificationStamp();
 				parent.layout();
 			}
 		}
 	}
 
 	@Override
 	public void setFocus() {
 
 	}
 
 }
