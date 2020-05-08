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
 package org.eclipse.mylyn.docs.intent.client.ui.editor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotationModelManager;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.collab.handlers.ReadWriteRepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.SaveException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.compare.IntentASTMerger;
 import org.eclipse.mylyn.docs.intent.compare.MergingException;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.query.IntentHelper;
 import org.eclipse.mylyn.docs.intent.parser.IntentParser;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.eclipse.mylyn.docs.intent.serializer.ParsedElementPosition;
 import org.eclipse.ui.internal.editors.text.WorkspaceOperationRunner;
 import org.eclipse.ui.texteditor.AbstractDocumentProvider;
 
 /**
  * DocumentProvider for the IntentDocument documents.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 // Suppress Warnings added for using WorkspaceOperationRunner
 @SuppressWarnings("restriction")
 public class IntentDocumentProvider extends AbstractDocumentProvider implements RepositoryClient {
 	/**
 	 * Represents an Intent structural content (like section or chapter).
 	 */
 	public static final String INTENT_STRUCTURAL_CONTENT = "__Intent__structuralcontent";
 
 	/**
 	 * Represents an Intent Modeling Unit (from '@M' to 'M@').
 	 */
 	public static final String INTENT_MODELINGUNIT = "__Intent__modelingunit";
 
 	/**
 	 * Represents an Intent Description Unit.
 	 */
 	public static final String INTENT_DESCRIPTIONUNIT = "__Intent__descriptionunit";
 
 	/**
 	 * Represents an Intent title ( only for Structured element).
 	 */
 	public static final String INTENT_TITLE = "__Intent__title";
 
 	/**
 	 * Represents all the content types handled by this partionner.
 	 */
 	public static final String[] LEGAL_CONTENT_TYPES = new String[] {INTENT_STRUCTURAL_CONTENT,
 			INTENT_MODELINGUNIT, INTENT_DESCRIPTIONUNIT, INTENT_TITLE,
 	};
 
 	/**
 	 * The repository to use for creating and closing GET and POST connections.
 	 */
 	private Repository repository;
 
 	/**
 	 * The repository object handler managing the notifications related to the handled elements and allowing
 	 * to save the elements on the repository.
 	 */
 	private RepositoryObjectHandler listenedElementsHandler;
 
 	/**
 	 * Keep the associations between an element and all the documents that are opened on it.
 	 */
 	private Map<Object, List<IntentEditorDocument>> elementsToDocuments;
 
 	/**
 	 * Root for the handled document.
 	 */
 	private EObject documentRoot;
 
 	/** The operation runner. */
 	private WorkspaceOperationRunner fOperationRunner;
 
 	/**
 	 * The editor associated to this document provider.
 	 */
 	private IntentEditor associatedEditor;
 
 	/**
 	 * The AnnotatioModelManager.
 	 */
 	private IntentAnnotationModelManager annotationModelManager;
 
 	/**
 	 * Represents the last createdDocument.
 	 */
 	private IntentEditorDocument createdDocument;
 
 	/**
 	 * Represents the partitioner used to identify the partitions of the document.
 	 */
 	private IDocumentPartitioner partitioner;
 
 	/**
 	 * IntentDocumentProvider constructor.
 	 * 
 	 * @param editor
 	 *            the editor associated to this document Provider
 	 */
 	public IntentDocumentProvider(IntentEditor editor) {
 		this.elementsToDocuments = new HashMap<Object, List<IntentEditorDocument>>();
 		this.associatedEditor = editor;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
 	 */
 	@Override
 	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
 
 		// We use an AnnotationModelManager to handle the create annotationModel
 		this.annotationModelManager = new IntentAnnotationModelManager();
 		return annotationModelManager.getAnnotationModel();
 	}
 
 	/**
 	 * Initialize the annotation model using the repository informations.
 	 */
 	private void initializeAnnotationModel() {
 		for (CompilationStatus status : IntentHelper.getAllStatus((IntentGenericElement)documentRoot)) {
 
 			List<IntentEditorDocument> list = elementsToDocuments.get(listenedElementsHandler
 					.getRepositoryAdapter().getIDFromElement(status.getTarget()));
 			if (list != null) {
 				IntentEditorDocument doc = list.get(0);
 				// We use the annotationModelManager to create annotations
 				ParsedElementPosition posit = doc.getIntentPosition(status.getTarget());
 				if (posit == null) {
 					posit = new ParsedElementPosition(0, 0);
 				}
 
 				annotationModelManager.addAnnotationFromStatus(
 						this.listenedElementsHandler.getRepositoryAdapter(), status,
 						new Position(posit.getOffset(), posit.getLength()));
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getAnnotationModel(java.lang.Object)
 	 */
 	@Override
 	public IAnnotationModel getAnnotationModel(Object element) {
 		return annotationModelManager.getAnnotationModel();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(java.lang.Object)
 	 */
 	@Override
 	protected IDocument createDocument(Object element) throws CoreException {
 		if (!(element instanceof IntentEditorInput)) {
 			IStatus status = new Status(IStatus.ERROR, IntentEditorActivator.PLUGIN_ID,
 					"IntentDocumentProvider : cannot create a document of type "
 							+ element.getClass().getCanonicalName() + " (must be IntentEditorInput) ");
 			throw new CoreException(status);
 		}
 
 		// We obtain the root of the document
 		documentRoot = ((IntentEditorInput)element).getIntentElement();
 		createdDocument = new IntentEditorDocument(documentRoot, this.associatedEditor);
 
 		partitioner = new IntentPartitioner(LEGAL_CONTENT_TYPES);
 		partitioner.connect(createdDocument);
 		createdDocument.setDocumentPartitioner(partitioner);
 		return createdDocument;
 
 	}
 
 	/**
 	 * Add all the given object and all its contained elements in the elementsToDocuments mapping.
 	 * 
 	 * @param root
 	 *            the element to inspect
 	 * @param document
 	 *            the document to consider for the mapping.
 	 */
 	public void addAllContentAsIntentElement(EObject root, IntentEditorDocument document) {
 		Object identifier = listenedElementsHandler.getRepositoryAdapter().getIDFromElement(root);
 
 		// We first associate this root in to the given document
 		if (elementsToDocuments.get(identifier) == null) {
 			elementsToDocuments.put(identifier, new ArrayList<IntentEditorDocument>());
 		}
 		if (!elementsToDocuments.get(identifier).contains(document)) {
 			elementsToDocuments.get(identifier).add(document);
 		}
 
 		// Then we do the same for all its contained element
 		for (EObject content : root.eContents()) {
 			addAllContentAsIntentElement(content, document);
 		}
 	}
 
 	/**
 	 * Parse the given document and return the parsed AST ; if the value of setAST is true, sets this AST to
 	 * the document.
 	 * 
 	 * @param document
 	 *            the document to parse
 	 * @param setAST
 	 *            true if the parsed AST should be associated to the document
 	 * @return the parsed AST
 	 * @throws ParseException
 	 *             if the document contains syntax errors.
 	 */
 	private EObject reparseDocument(IntentEditorDocument document, boolean setAST) throws ParseException {
 		EObject localAST = null;
 		// We first parse the current text to obtain the ast.
 		localAST = new IntentParser().parse(document.get());
 
 		if (setAST) {
 			document.reloadFromAST(localAST);
 		}
 		return localAST;
 	}
 
 	/**
 	 * Refreshes the outline View.
 	 * 
 	 * @param newAST
 	 *            the newAST to base the outline on
 	 */
 	public void refreshOutline(EObject newAST) {
 		associatedEditor.refreshOutlineView(newAST);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#doSaveDocument(org.eclipse.core.runtime.IProgressMonitor,
 	 *      java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
 	 */
 	@Override
 	protected void doSaveDocument(IProgressMonitor monitor, Object element, final IDocument document,
 			boolean overwrite) throws CoreException {
 		if (document instanceof IntentEditorDocument) {
 			final EObject localAST;
 			try {
 				this.removeSyntaxErrors();
 				localAST = reparseDocument((IntentEditorDocument)document, false);
 				this.associatedEditor.refreshTitle(localAST);
 				final RepositoryAdapter repositoryAdapter = this.listenedElementsHandler
 						.getRepositoryAdapter();
 				repositoryAdapter.execute(new IntentCommand() {
 
 					public void execute() {
 						repositoryAdapter.openSaveContext();
 						try {
 							merge(document, localAST);
 							repositoryAdapter.save();
 						} catch (ReadOnlyException e) {
 							IntentUiLogger.logError(e);
 						} catch (SaveException e) {
 							IntentUiLogger.logError(e);
 						}
 						repositoryAdapter.closeContext();
 					}
 
 				});
 				repositoryAdapter.closeContext();
 			} catch (ParseException e) {
 				this.createSyntaxErrorAnnotation(e.getMessage(), e.getErrorOffset(), e.getErrorLength());
 			}
 
 		}
 	}
 
 	/**
 	 * Merges the document content according to the given AST.
 	 * 
 	 * @param document
 	 *            the document
 	 * @param localAST
 	 *            the AST
 	 */
 	private void merge(final IDocument document, final EObject localAST) {
 		// Then we try to merge the parsed AST with the old one
 		final IntentASTMerger merger = new IntentASTMerger();
 		boolean mustUndo = false;
 		try {
 			final EObject remoteAST = (EObject)((IntentEditorDocument)document).getAST();
 
 			try {
 				merger.mergeFromLocalToRepository(localAST, remoteAST);
 			} catch (MergingException e) {
 				mustUndo = true;
 				IntentUiLogger.logError(e);
 			}
 
 			try {
 				listenedElementsHandler.getRepositoryAdapter().save();
 			} catch (ReadOnlyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SaveException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// We update the mapping between elements and documents
 			addAllContentAsIntentElement(documentRoot, (IntentEditorDocument)document);
 
 		} catch (NullPointerException npe) { // FIXME catch NPE ??
 			mustUndo = true;
 			IntentUiLogger.logError(npe);
 		}
 
 		if (mustUndo) {
 			try {
 				((ReadWriteRepositoryObjectHandler)listenedElementsHandler).undo();
 			} catch (ReadOnlyException e) {
 				IntentUiLogger.logError(e);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#isModifiable(java.lang.Object)
 	 */
 	@Override
 	public boolean isModifiable(Object element) {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#isReadOnly(java.lang.Object)
 	 */
 	@Override
 	public boolean isReadOnly(Object element) {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getOperationRunner(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
 		if (fOperationRunner == null) {
 			fOperationRunner = new WorkspaceOperationRunner();
 		}
 		fOperationRunner.setProgressMonitor(monitor);
 		return fOperationRunner;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient#handleChangeNotification(org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification)
 	 */
 	public void handleChangeNotification(RepositoryChangeNotification notification) {
 		// If the received notification indicates the deletion of the root of the associated document
 		if (notification.getRightRoots().size() < 1) {
 
 			Object modifiedObjectIdentifier = listenedElementsHandler.getRepositoryAdapter()
 					.getIDFromElement(documentRoot);
 			if (elementsToDocuments.get(modifiedObjectIdentifier) != null) {
 				for (IntentEditorDocument relatedDocument : elementsToDocuments.get(modifiedObjectIdentifier)) {
 					relatedDocument.unsynchronize();
 				}
 			}
 			return;
 		}
 
 		// For each object modified indicated by this notification
 		for (EObject modifiedObject : notification.getRightRoots()) {
 			Object modifiedObjectIdentifier = listenedElementsHandler.getRepositoryAdapter()
 					.getIDFromElement(modifiedObject);
 
 			// For all documents that have been opened on this object
 			if (elementsToDocuments.get(modifiedObjectIdentifier) != null) {
 				if (listenedElementsHandler.getRepositoryAdapter().getIDFromElement(documentRoot)
 						.equals(modifiedObjectIdentifier)) {
 					documentRoot = modifiedObject;
 				}
 				for (final IntentEditorDocument relatedDocument : elementsToDocuments
 						.get(modifiedObjectIdentifier)) {
 
 					relatedDocument.reloadFromAST(documentRoot);
 
 					// We update the mapping between elements and documents
 					addAllContentAsIntentElement(documentRoot, relatedDocument);
 					if (modifiedObject instanceof IntentGenericElement) {
 						IntentGenericElement modifiedElement = (IntentGenericElement)modifiedObject;
 						updateAnnotationModelFromCompilationStatus(modifiedElement, relatedDocument);
 
 					}
 
 					// TODO : set the editor cursor to the old position
 					// associatedEditor.setCursor(cursor);
 
 					// We reconnect the partitioner to the document
 					// partitioner.disconnect();
 					// partitioner.connect(createdDocument);
 				}
 				// In any case, we launch the syntax coloring
 				partitioner.computePartitioning(0, 1);
 
 				// Finally, we refresh the outline
 				refreshOutline(documentRoot);
 			} else {
 				// TODO [DISABLED] (removed syserr, too verbose)
 				// System.err.println("unknown element");
 			}
 		}
 	}
 
 	/**
 	 * Update the annotation model by translating each compilationStatus associated to the given element as an
 	 * Annotation.
 	 * 
 	 * @param modifiedElement
 	 *            the element to use for updating the AnnotationModel
 	 * @param relatedDocument
 	 *            the document to use for obtaining informations about element positions
 	 */
 	private void updateAnnotationModelFromCompilationStatus(IntentGenericElement modifiedElement,
 			IntentEditorDocument relatedDocument) {
 
 		// Step 1 : removing all the invalid compilation status relative to the modifiedElement
 		annotationModelManager.removeInvalidCompilerAnnotations(
 				this.listenedElementsHandler.getRepositoryAdapter(), modifiedElement);
 
 		// Step 2 : updating the concerned documents
 		for (CompilationStatus statusToAdd : modifiedElement.getCompilationStatus()) {
 
 			// Step 2.1 : we determine the position of the annotation to create by
 			// using the informations hold by the IntentDocument.
 			ParsedElementPosition parsedElementPosition = relatedDocument.getIntentPosition(statusToAdd
 					.getTarget());
 			if (parsedElementPosition == null) {
 				parsedElementPosition = new ParsedElementPosition(0, 0);
 			}
 			Position position = new Position(parsedElementPosition.getOffset(),
 					parsedElementPosition.getLength());
 
 			// Step 2.2 : Adding this annotation to the model (will update overview and vertical rulers of
 			// the editor)
 			annotationModelManager.addAnnotationFromStatus(
 					this.listenedElementsHandler.getRepositoryAdapter(), statusToAdd, position);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient#addRepositoryObjectHandler(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler)
 	 */
 	public void addRepositoryObjectHandler(RepositoryObjectHandler handler) {
 		handler.addClient(this);
 		listenedElementsHandler = handler;
 		addAllContentAsIntentElement(documentRoot, createdDocument);
 		initializeAnnotationModel();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient#removeRepositoryObjectHandler(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler)
 	 */
 	public void removeRepositoryObjectHandler(RepositoryObjectHandler handler) {
 		handler.removeClient(this);
 		listenedElementsHandler = null;
 
 	}
 
 	/**
 	 * Returns the RepositoryObjectHandler associated to this document provider.
 	 * 
 	 * @return the listenedElementsHandler associated to this document provider
 	 */
 	public RepositoryObjectHandler getListenedElementsHandler() {
 		return listenedElementsHandler;
 	}
 
 	/**
 	 * Sets the repository to use for saving and closing getConnexion.
 	 * 
 	 * @param repository
 	 *            the repository to use for saving and closing getConnexion
 	 */
 	public void setRepository(Repository repository) {
 		this.repository = repository;
 		this.repository.register(this);
 	}
 
 	/**
 	 * Returns the repository to use for saving and closing getConnexion.
 	 * 
 	 * @return the repository to use for saving and closing getConnexion
 	 */
 	public Repository getRepository() {
 		return repository;
 	}
 
 	/**
 	 * Unregister from the repository, the connection and the handler used by this document provider.
 	 */
 	public void close() {
 		if (this.repository != null) {
			// TODO ??? at this time cause project explorer desynchronization. Repository is saved anyway
			// // If the editor is editable, we undo all the unsaved modifications
			// if (this.associatedEditor.isEditable()) {
			// try {
			// this.listenedElementsHandler.getRepositoryAdapter().undo();
			// } catch (ReadOnlyException e) {
			// // The readOnly property has already been tested by calling isEditable.
			// }
			// }
 			this.listenedElementsHandler.getRepositoryAdapter().closeContext();
 			this.repository.unregister(this);
 		}
 		if (this.listenedElementsHandler != null) {
 			this.listenedElementsHandler.removeClient(this);
 			this.listenedElementsHandler.stop();
 		}
 
 	}
 
 	/**
 	 * Sets the editor associated to this documentProvider.
 	 * 
 	 * @param editor
 	 *            the editor to set
 	 */
 	public void setEditor(IntentEditor editor) {
 		this.associatedEditor = editor;
 
 	}
 
 	/**
 	 * Creates a syntax error annotation at the given offset, of the given length.
 	 * 
 	 * @param message
 	 *            the message associated to this syntax error
 	 * @param offset
 	 *            offset of the syntax error annotation
 	 * @param length
 	 *            length of the syntax error annotation.
 	 */
 	public void createSyntaxErrorAnnotation(String message, int offset, int length) {
 		this.annotationModelManager.createSyntaxErrorAnnotation(message, offset, length);
 
 	}
 
 	/**
 	 * Removes all the syntax error annotations from the managed annotation model.
 	 */
 	public void removeSyntaxErrors() {
 		this.annotationModelManager.removeSyntaxErrorsAnnotations();
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient#dispose()
 	 */
 	public void dispose() {
 		listenedElementsHandler.removeClient(this);
 		listenedElementsHandler = null;
 	}
 }
