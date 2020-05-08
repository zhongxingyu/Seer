 /*
  * Copyright (c) 2005 Rob Rohan 
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.rohanclan.afae.editor;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPathEditorInput;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.editors.text.FileDocumentProvider;
 import org.eclipse.ui.editors.text.TextFileDocumentProvider;
 import org.eclipse.ui.texteditor.IElementStateListener;
 
 import com.rohanclan.afae.AfaePlugin;
 import com.rohanclan.afae.partition.AfaePartitionScanner;
 import com.rohanclan.afae.partition.AfaePartitioner;
 
 public class AfaeDocumentProvider extends FileDocumentProvider {
 	private TextFileDocumentProvider textFileDocumentProvider;
 	/** the last element used to create this, or null */
 	public Object element;
 
 	public AfaeDocumentProvider() {
 		super();
 	}
 
 	/**
      * 
      */
 	protected IDocument createDocument(Object element) throws CoreException {
 		IDocument document = super.createDocument(element);
 		String filename = getFileName(element);
 		this.element = element;
 
 		if (document == null) {
 			try {
 				textFileDocumentProvider = new TextFileDocumentProvider();
 				textFileDocumentProvider.connect(element);
 				// this.connect(element);
 				document = textFileDocumentProvider.getDocument(element);
 			} catch (Exception e) {
 				AfaePlugin.logDebug("createDocument", e,
 						AfaeDocumentProvider.class);
 			}
 		}
 
 		// if they do find and replace on the whole document would lose
 		// highlighting
 		// this trys to fix it
 		final IDocument fdocument = document;
 		final Object felement = element;
 		document.addDocumentListener(new IDocumentListener() {
 			public void documentChanged(DocumentEvent e) {
 				AfaePlugin.logDebug("documentChanged", null,
 						AfaeDocumentProvider.class);
 
 				// System.err.println("documentchanged called: " + e.getOffset()
 				// + " " + e.getLength());
 				try {
 					/*
 					 * e.getDocument().replace( e.getOffset(), e.getLength(),
 					 * e.getText() );
 					 */
 
 					if (e.getLength() > 1) {
 						partitionDocument(fdocument, felement);
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace(System.err);
 				}
 			}
 
 			public void documentAboutToBeChanged(DocumentEvent e) {
 				AfaePlugin.logDebug("documentAboutToBeChanged", null,
 						AfaeDocumentProvider.class);
 			}
 		});
 
 		initializeDocument(document, filename);
 
 		return document;
 	}
 
 	/**
 	 * Used to force a reparse of the document. Re-does the partitions colors
 	 * and method collector. Expensive with large documents. Use sparingly.
 	 * 
 	 * @param doc
 	 * @param element
 	 */
 	public void partitionDocument(IDocument doc, Object element) {
 		String modefile = getFileName(element);
 		// remove any of this documents jump items (the partitioner will
 		// make new ones)
 		// AfaeEditorTools.clearJumpItems(modefile);
 
 		IDocumentPartitioner partitioner = createPartitioner(modefile);
 
		if (doc != null && doc.getDocumentPartitioner() != null) {
 			doc.getDocumentPartitioner().disconnect();
 
 			partitioner.connect(doc);
 			doc.setDocumentPartitioner(partitioner);
 		}
 	}
 
 	public boolean isReadOnly(Object element) {
 		return textFileDocumentProvider == null ? super.isReadOnly(element)
 				: textFileDocumentProvider.isReadOnly(element);
 	}
 
 	public boolean isModifiable(Object element) {
 		return textFileDocumentProvider == null ? super.isModifiable(element)
 				: textFileDocumentProvider.isModifiable(element);
 	}
 
 	public IDocument getDocument(Object element) {
 		return textFileDocumentProvider == null ? super.getDocument(element)
 				: textFileDocumentProvider.getDocument(element);
 	}
 
 	protected void doResetDocument(Object element, IProgressMonitor monitor)
 			throws CoreException {
 		if (textFileDocumentProvider == null)
 			super.doResetDocument(element, monitor);
 		else
 			textFileDocumentProvider.resetDocument(element);
 	}
 
 	protected void doSaveDocument(IProgressMonitor monitor, Object element,
 			IDocument document, boolean overwrite) throws CoreException {
 		if (textFileDocumentProvider == null) {
 			super.doSaveDocument(monitor, element, document, overwrite);
 		} else {
 			textFileDocumentProvider.saveDocument(monitor, element, document,
 					overwrite);
 		}
 	}
 
 	public long getModificationStamp(Object element) {
 		return textFileDocumentProvider == null ? super
 				.getModificationStamp(element) : textFileDocumentProvider
 				.getModificationStamp(element);
 	}
 
 	public long getSynchronizationStamp(Object element) {
 		return textFileDocumentProvider == null ? super
 				.getSynchronizationStamp(element) : textFileDocumentProvider
 				.getSynchronizationStamp(element);
 	}
 
 	public boolean isDeleted(Object element) {
 		return textFileDocumentProvider == null ? super.isDeleted(element)
 				: textFileDocumentProvider.isDeleted(element);
 	}
 
 	public boolean mustSaveDocument(Object element) {
 		return textFileDocumentProvider == null ? super
 				.mustSaveDocument(element) : textFileDocumentProvider
 				.mustSaveDocument(element);
 	}
 
 	public boolean canSaveDocument(Object element) {
 		return textFileDocumentProvider == null ? super
 				.canSaveDocument(element) : textFileDocumentProvider
 				.canSaveDocument(element);
 	}
 
 	public IAnnotationModel getAnnotationModel(Object element) {
 		return textFileDocumentProvider == null ? super
 				.getAnnotationModel(element) : textFileDocumentProvider
 				.getAnnotationModel(element);
 	}
 
 	public void aboutToChange(Object element) {
 		if (textFileDocumentProvider == null) {
 			super.aboutToChange(element);
 		} else {
 			textFileDocumentProvider.aboutToChange(element);
 		}
 	}
 
 	public void changed(Object element) {
 		if (textFileDocumentProvider == null) {
 			super.changed(element);
 		} else {
 			textFileDocumentProvider.changed(element);
 		}
 	}
 
 	public void addElementStateListener(IElementStateListener listener) {
 		if (textFileDocumentProvider == null) {
 			super.addElementStateListener(listener);
 		} else {
 			textFileDocumentProvider.addElementStateListener(listener);
 		}
 	}
 
 	public void removeElementStateListener(IElementStateListener listener) {
 		if (textFileDocumentProvider == null) {
 			super.removeElementStateListener(listener);
 		} else {
 			textFileDocumentProvider.removeElementStateListener(listener);
 		}
 	}
 
 	// protected IDocument createDocument(Object element) throws CoreException {
 	// IDocument doc = super.createDocument(element);
 	// String filename = null;
 	// if(element instanceof IPathEditorInput) {
 	// filename = ((IPathEditorInput)element).getName();
 	// IDocument document= createEmptyDocument();
 	// setDocumentContent(document, (IPathEditorInput)element, null);
 	// doc = document;
 	// } else if(element instanceof IEditorInput) {
 	// filename = ((IEditorInput)element).getName();
 	// IDocument document= createEmptyDocument();
 	// setDocumentContent(document, (IEditorInput)element, null);
 	// doc = document;
 	// }
 	// Assert.isNotNull(doc);
 	// initializeDocument(doc, filename);
 	// return doc;
 	// }
 
 	public String getFileName(Object element) {
 		if (element instanceof IPathEditorInput) {
 			return ((IPathEditorInput) element).getPath().toOSString(); // .getName();
 		} else if (element instanceof IStorageEditorInput) {
 			try {
 				return ((IStorageEditorInput) element).getStorage()
 						.getFullPath().toOSString(); // .getName();
 			} catch (CoreException e) {
 				AfaePlugin.logError("getFileName ", e,
 						AfaeDocumentProvider.class);
 				e.printStackTrace();
 			}
 		} else if (element instanceof IEditorInput) {
 			return ((IEditorInput) element).getName();
 		}
 
 		return null;
 	}
 
 	protected void disposeElementInfo(Object element, ElementInfo info) {
 		super.disposeElementInfo(element, info);
 		if (textFileDocumentProvider != null) {
 			textFileDocumentProvider.disconnect(element);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java
 	 * .lang.Object, org.eclipse.jface.text.IDocument)
 	 */
 	protected void setupDocument(Object element, IDocument document) {
 		super.setupDocument(element, document);
 
 		String filename = null;
 
 		filename = getFileName(element);
 
 		/*
 		 * if(element instanceof IPathEditorInput) { filename =
 		 * ((IPathEditorInput) element).getPath().toOSString(); // .getName(); }
 		 * else if (element instanceof IStorageEditorInput) { try { filename =
 		 * ((IStorageEditorInput)
 		 * element).getStorage().getFullPath().toOSString(); // .getName(); }
 		 * catch(CoreException e) {
 		 * AfaePlugin.logError("setupDocument ",e,AfaeDocumentProvider.class); }
 		 * } else if(element instanceof IEditorInput) { filename =
 		 * ((IEditorInput)element).getName(); }
 		 */
 
 		initializeDocument(document, filename);
 	}
 
 	/**
 	 * 
 	 * @param document
 	 * @param filename
 	 */
 	private void initializeDocument(IDocument document, String filename) {
 		if (document != null) {
 			IDocumentPartitioner partitioner = createPartitioner(filename);
 			document.setDocumentPartitioner(partitioner);
 			partitioner.connect(document);
 		}
 	}
 
 	/**
 	 * 
 	 * @param filename
 	 * @return
 	 */
 	private IDocumentPartitioner createPartitioner(String filename) {
 		AfaePartitionScanner scanner = new AfaePartitionScanner(filename);
 		IDocumentPartitioner partitioner = new AfaePartitioner(scanner, scanner
 				.getContentTypes());
 		return partitioner;
 	}
 }
