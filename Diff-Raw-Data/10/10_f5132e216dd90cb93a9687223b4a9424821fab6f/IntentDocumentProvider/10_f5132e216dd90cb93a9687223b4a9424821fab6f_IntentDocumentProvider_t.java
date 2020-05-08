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
 
 import com.google.common.collect.Sets;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.rules.FastPartitioner;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotationModelManager;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.scanner.IntentPartitionScanner;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.client.ui.repositoryconnection.EditorElementListAdapter;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.collab.handlers.ReadWriteRepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.SaveException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.ReadOnlyRepositoryObjectHandlerImpl;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.ReadWriteRepositoryObjectHandlerImpl;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListNotificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.typeListener.TypeNotificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.compare.IntentASTMerger;
 import org.eclipse.mylyn.docs.intent.compare.MergingException;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusManager;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerPackage;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
 import org.eclipse.mylyn.docs.intent.core.genericunit.UnitInstruction;
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
 	 * A flag indicating whether the current document has syntax errors or not.
 	 */
 	private boolean hasSyntaxErrors;
 
 	/**
 	 * IntentDocumentProvider constructor.
 	 * 
 	 * @param editor
 	 *            the editor associated to this document Provider
 	 */
 	public IntentDocumentProvider(IntentEditor editor) {
 		this.elementsToDocuments = new HashMap<Object, List<IntentEditorDocument>>();
 		this.associatedEditor = editor;
 		this.annotationModelManager = new IntentAnnotationModelManager();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#canSaveDocument(java.lang.Object)
 	 */
 	@Override
 	public boolean canSaveDocument(Object element) {
 		return hasSyntaxErrors || super.canSaveDocument(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
 	 */
 	@Override
 	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
 		// We use an AnnotationModelManager to handle the create annotationModel
 		Assert.isNotNull(annotationModelManager);
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
 
 				if (!status.getSeverity().equals(CompilationStatusSeverity.INFO)) {
 					annotationModelManager.addAnnotationFromStatus(
 							this.listenedElementsHandler.getRepositoryAdapter(), status,
 							new Position(posit.getOffset(), posit.getDeclarationLength()));
 				}
 			}
 		}
 		return annotationModelManager.getAnnotationModel();
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
 					"Cannot open an Intent editor on a document of type "
 							+ element.getClass().getCanonicalName() + " (must be IntentEditorInput) ");
 			throw new CoreException(status);
 		}
 		if (((IntentEditorInput)element).getRepository() == null) {
 			IStatus status = new Status(IStatus.ERROR, IntentEditorActivator.PLUGIN_ID,
 					"Cannot open Intent editor : document is not available.");
 			throw new CoreException(status);
 		}
 
 		setRepository(((IntentEditorInput)element).getRepository());
 
 		// We obtain the root of the document
 		documentRoot = ((IntentEditorInput)element).getIntentElement();
 
 		// TODO check for notifications issues:
 		// the following command was added to avoid infinite loop caused by the fact that the serialization
 		// occurs during the repository first compilation.
 		((IntentEditorInput)element).getRepositoryAdapter().execute(new IntentCommand() {
 
 			public void execute() {
 				createdDocument = new IntentEditorDocument(documentRoot, associatedEditor);
 			}
 		});
 
 		if (createdDocument != null) {
 			partitioner = new FastPartitioner(new IntentPartitionScanner(),
 					IntentPartitionScanner.LEGAL_CONTENT_TYPES);
 			partitioner.connect(createdDocument);
 			createdDocument.setDocumentPartitioner(partitioner);
 			subscribeRepository(((IntentEditorInput)element).getRepositoryAdapter());
 			addAllContentAsIntentElement(documentRoot, createdDocument);
 		}
 		return createdDocument;
 	}
 
 	/**
 	 * Registers listeners in the repository used by the given editor input.
 	 * 
 	 * @param repositoryAdapter
 	 *            the repository adapter previously created by the editorInput
 	 */
 	private void subscribeRepository(RepositoryAdapter repositoryAdapter) {
 		// Step 1 : creation of the Handler in the correct mode
 		final RepositoryObjectHandler elementHandler = createElementHandler(repositoryAdapter, false);
 		addRepositoryObjectHandler(elementHandler);
 
 		// Step 2 : creation of a Notificator listening changes on this element and compilation
 		// errors.
 		final Set<EObject> listenedObjects = new LinkedHashSet<EObject>();
 		listenedObjects.add(documentRoot);
 		final ElementListAdapter adapter = new EditorElementListAdapter();
 
 		Notificator listenedElementsNotificator = new ElementListNotificator(listenedObjects, adapter,
 				repositoryAdapter);
 		Notificator compilationStatusNotificator = new TypeNotificator(
 				Sets.newLinkedHashSet(CompilerPackage.eINSTANCE.getCompilationStatusManager()
 						.getEAllStructuralFeatures()));
 		elementHandler.addNotificator(listenedElementsNotificator);
 		elementHandler.addNotificator(compilationStatusNotificator);
 	}
 
 	/**
 	 * Creates the element handler matching the given mode.
 	 * 
 	 * @param repositoryAdapter
 	 *            the repository adapter
 	 * @param readOnlyMode
 	 *            the access mode
 	 * @return the handler
 	 */
 	private static RepositoryObjectHandler createElementHandler(RepositoryAdapter repositoryAdapter,
 			boolean readOnlyMode) {
 		RepositoryObjectHandler elementHandler = null;
 		if (!readOnlyMode) {
 			try {
 				elementHandler = new ReadWriteRepositoryObjectHandlerImpl(repositoryAdapter);
 			} catch (ReadOnlyException e) {
 				IntentLogger
 						.getInstance()
 						.log(LogType.WARNING,
 								"The Intent Editor has insufficient rights (read-only) to save modifications on the repository. A read-only context will be used instead.");
 				readOnlyMode = true;
 			}
 		}
 
 		if (readOnlyMode) {
 			elementHandler = new ReadOnlyRepositoryObjectHandlerImpl();
 			elementHandler.setRepositoryAdapter(repositoryAdapter);
 		}
 		return elementHandler;
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
 				hasSyntaxErrors = false;
 				this.removeSyntaxErrors();
 
 				localAST = new IntentParser().parse(document.get());
 
 				this.associatedEditor.refreshTitle(localAST);
 				final RepositoryAdapter repositoryAdapter = this.listenedElementsHandler
 						.getRepositoryAdapter();
 				repositoryAdapter.execute(new IntentCommand() {
 
 					public void execute() {
 						try {
 							merge((IntentEditorDocument)document, localAST);
 							repositoryAdapter.save();
 						} catch (ReadOnlyException e) {
 							IntentUiLogger.logError(e);
 						} catch (SaveException e) {
 							IntentUiLogger.logError(e);
 						}
 					}
 
 				});
 
 				((IntentEditorDocument)document).reloadFromAST();
 			} catch (ParseException e) {
 				this.createSyntaxErrorAnnotation(e.getMessage(), e.getErrorOffset(), e.getErrorLength());
 				hasSyntaxErrors = true;
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
 	private void merge(final IntentEditorDocument document, final EObject localAST) {
 		// Then we try to merge the parsed AST with the old one
 		final IntentASTMerger merger = new IntentASTMerger();
 		boolean mustUndo = false;
 		try {
 			final EObject remoteAST = (EObject)document.getAST();
 			try {
 				if (localAST != null && remoteAST != null && localAST.eClass().equals(remoteAST.eClass())) {
 					merger.mergeFromLocalToRepository(localAST, remoteAST);
 				} else {
 					this.createSyntaxErrorAnnotation("Unrecognized content: unable to merge "
 							+ localAST.eClass().getName() + " with " + remoteAST.eClass().getName() + ".", 0,
 							document.getLength());
 				}
 			} catch (MergingException e) {
 				mustUndo = true;
 				IntentUiLogger.logError(e);
 			}
 
 			// We update the mapping between elements and documents
 			addAllContentAsIntentElement(documentRoot, document);
 
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
 		if (handleRootHasBeenDeleted(notification)) {
 			return;
 		}
 
		// For each object modified indicated by this notification
 		for (EObject modifiedObject : notification.getRightRoots()) {
 			Object modifiedObjectIdentifier = listenedElementsHandler.getRepositoryAdapter()
 					.getIDFromElement(modifiedObject);
 
 			// For all documents that have been opened on this object
 			if (elementsToDocuments.get(modifiedObjectIdentifier) != null) {
 				handleContentHasChanged(modifiedObject, modifiedObjectIdentifier);
 			} else {
				// update annotations (if the compilation status manager has changed)
 				handleCompilationStatusHasChanged(modifiedObject);
 			}
 		}
 	}
 
 	/**
 	 * Update the annotation model by translating each compilationStatus associated to the given element as an
 	 * Annotation.
 	 * 
 	 * @param modifiedElement
 	 *            the element to use for updating the AnnotationModel (children will also be updated)
 	 * @param relatedDocument
 	 *            the document to use for obtaining informations about element positions
 	 */
 	private void updateAnnotationModelFromCompilationStatusAndChildren(IntentGenericElement modifiedElement,
 			IntentEditorDocument relatedDocument) {
 		// Update the root
 		updateAnnotationModelFromCompilationStatus(modifiedElement, relatedDocument);
 
 		// And all children
 		TreeIterator<EObject> eAllContents = modifiedElement.eAllContents();
 		while (eAllContents.hasNext()) {
 			EObject next = eAllContents.next();
 			if (next instanceof IntentGenericElement) {
 				updateAnnotationModelFromCompilationStatus((IntentGenericElement)next, relatedDocument);
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
 					parsedElementPosition.getDeclarationLength());
 			if (!statusToAdd.getSeverity().equals(CompilationStatusSeverity.INFO)) {
 				// Step 2.2 : Adding this annotation to the model (will update overview and vertical rulers of
 				// the editor)
 				annotationModelManager.addAnnotationFromStatus(
 						this.listenedElementsHandler.getRepositoryAdapter(), statusToAdd, position);
 			}
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
 			this.repository.unregister(this);
 		}
 		if (this.listenedElementsHandler != null) {
 			this.listenedElementsHandler.getRepositoryAdapter().closeContext();
 			this.listenedElementsHandler.removeClient(this);
 			this.listenedElementsHandler.stop();
 		}
 
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
 
 	private boolean handleRootHasBeenDeleted(RepositoryChangeNotification notification) {
 		if (notification.getRightRoots().size() < 1) {
 
 			Object modifiedObjectIdentifier = listenedElementsHandler.getRepositoryAdapter()
 					.getIDFromElement(documentRoot);
 			if (elementsToDocuments.get(modifiedObjectIdentifier) != null) {
 				for (IntentEditorDocument relatedDocument : elementsToDocuments.get(modifiedObjectIdentifier)) {
 					relatedDocument.unsynchronize();
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private void handleCompilationStatusHasChanged(EObject modifiedObject) {
 		if (modifiedObject instanceof CompilationStatusManager) {
 			if (elementsToDocuments.values().iterator().hasNext()
 					&& elementsToDocuments.values().iterator().next().iterator().hasNext()) {
 				updateAnnotationModelFromCompilationStatusAndChildren(
 						(IntentGenericElement)this.documentRoot, elementsToDocuments.values().iterator()
 								.next().iterator().next());
 			}
 		}
 	}
 
 	private void handleContentHasChanged(EObject modifiedObject, Object modifiedObjectIdentifier) {
 		if (modifiedObject instanceof IntentStructuredElement || modifiedObject instanceof UnitInstruction) {
 			if (listenedElementsHandler.getRepositoryAdapter().getIDFromElement(documentRoot)
 					.equals(modifiedObjectIdentifier)) {
 				documentRoot = modifiedObject;
 			}
 			for (final IntentEditorDocument relatedDocument : elementsToDocuments
 					.get(modifiedObjectIdentifier)) {
 
 				relatedDocument.setAST(documentRoot);
 				relatedDocument.reloadFromAST();
 
 				// We update the mapping between elements and documents
 				addAllContentAsIntentElement(documentRoot, relatedDocument);
 			}
 			// In any case, we launch the syntax coloring
 			partitioner.computePartitioning(0, 1);
 
 			// Finally, we refresh the outline
 			refreshOutline(documentRoot);
 		}
 	}
 }
