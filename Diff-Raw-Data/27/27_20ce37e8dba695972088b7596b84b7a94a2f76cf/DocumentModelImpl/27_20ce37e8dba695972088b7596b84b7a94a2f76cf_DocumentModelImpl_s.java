 /*
  * Mar 6, 2005
  */
 package com.thinkparity.model.parity.model.document;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.UUID;
 import java.util.Vector;
 
 import com.thinkparity.codebase.FileUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.assertion.NotTrueAssertion;
 
 import com.thinkparity.model.parity.IParityModelConstants;
 import com.thinkparity.model.parity.ParityErrorTranslator;
 import com.thinkparity.model.parity.ParityException;
 import com.thinkparity.model.parity.api.events.CreationEvent;
 import com.thinkparity.model.parity.api.events.CreationListener;
 import com.thinkparity.model.parity.api.events.DeleteEvent;
 import com.thinkparity.model.parity.api.events.UpdateEvent;
 import com.thinkparity.model.parity.api.events.UpdateListener;
 import com.thinkparity.model.parity.api.events.VersionCreationEvent;
 import com.thinkparity.model.parity.model.AbstractModelImpl;
 import com.thinkparity.model.parity.model.artifact.Artifact;
 import com.thinkparity.model.parity.model.artifact.ArtifactState;
 import com.thinkparity.model.parity.model.artifact.ArtifactVersion;
 import com.thinkparity.model.parity.model.io.IOFactory;
 import com.thinkparity.model.parity.model.io.handler.DocumentIOHandler;
 import com.thinkparity.model.parity.model.session.InternalSessionModel;
 import com.thinkparity.model.parity.model.session.SessionModel;
 import com.thinkparity.model.parity.model.sort.ArtifactSorter;
 import com.thinkparity.model.parity.model.sort.ComparatorBuilder;
 import com.thinkparity.model.parity.model.workspace.Workspace;
 import com.thinkparity.model.parity.util.MD5Util;
 import com.thinkparity.model.parity.util.UUIDGenerator;
 import com.thinkparity.model.xmpp.document.XMPPDocument;
 
 /**
  * Implementation of the document model interface.
  * 
  * @author raykroeker@gmail.com
  * @version 1.5.2.43
  */
 class DocumentModelImpl extends AbstractModelImpl {
 
 	/**
 	 * List of listeners to notify when a document is created or received.
 	 * 
 	 * @see DocumentModelImpl#creationListenersLock
 	 */
 	private static final Collection<CreationListener> creationListeners;
 
 	/**
 	 * Synchronization lock for the creation listeners.
 	 * 
 	 * @see DocumentModelImpl#creationListeners
 	 */
 	private static final Object creationListenersLock;
 
 	/**
 	 * List of listeners to notify when a document has been updated.
 	 * 
 	 * @see DocumentModelImpl#updateListenersLock
 	 */
 	private static final Collection<UpdateListener> updateListeners;
 
 	/**
 	 * Synchronization lock for the update listeners.
 	 * 
 	 * @see DocumentModelImpl#updateListeners
 	 */
 	private static final Object updateListenersLock;
 
 	static {
 		// initialize the creation event listeners
 		creationListeners = new Vector<CreationListener>(7);
 		creationListenersLock = new Object();
 		// initialize the update event listeners
 		updateListeners = new Vector<UpdateListener>(7);
 		updateListenersLock = new Object();
 	}
 
 	/**
 	 * Default sort comparator for documents.
 	 * 
 	 */
 	private final Comparator<Artifact> defaultComparator;
 
 	/**
 	 * Default sort comparator for document versions.
 	 * 
 	 */
 	private final Comparator<ArtifactVersion> defaultVersionComparator;
 
 	/**
 	 * Document xml input\output.
 	 */
 	private final DocumentIOHandler documentIO;
 
 	/**
 	 * Create a DocumentModelImpl
 	 * 
 	 * @param workspace
 	 *            The workspace to work within.
 	 */
 	DocumentModelImpl(final Workspace workspace) {
 		super(workspace);
 		final ComparatorBuilder comparatorBuilder = new ComparatorBuilder();
 		this.defaultComparator = comparatorBuilder.createByName(Boolean.TRUE);
 		this.defaultVersionComparator =
 			comparatorBuilder.createVersionById(Boolean.TRUE);
 		this.documentIO = IOFactory.getDefault().createDocumentHandler();
 	}
 
 	/**
 	 * @see DocumentModel#addListener(CreationListener)
 	 * @param listener
 	 *            The listener to add.
 	 */
 	void addListener(final CreationListener listener) {
 		logger.info("addCreationListener(CreationListener)");
 		logger.debug(listener);
 		Assert.assertNotNull("addCreationListener(CreationListener)", listener);
 		synchronized (DocumentModelImpl.creationListenersLock) {
 			Assert.assertNotTrue(
 					"addCreationListener(CreationListener)",
 					DocumentModelImpl.creationListeners.contains(listener));
 			DocumentModelImpl.creationListeners.add(listener);
 		}
 	}
 
 	/**
 	 * Add a document update event listener.
 	 * 
 	 * @param listener
 	 *            The listener to add.
 	 */
 	void addListener(final UpdateListener listener) {
 		logger.info("addUpdateListener(UpdateListener)");
 		logger.debug(listener);
 		Assert.assertNotNull("addUpdateListener(UpdateListener)", listener);
 		synchronized (DocumentModelImpl.updateListenersLock) {
 			Assert.assertNotTrue("Update listener already registered.",
 					DocumentModelImpl.updateListeners.contains(listener));
 			DocumentModelImpl.updateListeners.add(listener);
 		}
 	}
 
 	/**
 	 * Close a document.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @throws NotTrueAssertion
 	 *             <ul>
 	 *             <li>If the user is offline.
 	 *             <li>If the logged in user is not the key holder.
 	 *             </ul>
 	 * @throws ParityException
 	 */
 	void close(final Long documentId) throws ParityException {
 		logger.info("close(Long)");
 		logger.debug(documentId);
 		assertLoggedInUserIsKeyHolder(documentId);
 		try {
 			final Document document = get(documentId);
 			assertStateTransition(document.getState(), ArtifactState.CLOSED);
 			documentIO.updateState(document.getId(), ArtifactState.CLOSED);
 
 			final InternalSessionModel iSModel = getInternalSessionModel();
 			iSModel.sendClose(documentId);
 			lock(documentId);
 		}
 		catch(final RuntimeException rx) {
 			logger.error("Cannot close document:  " + documentId, rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	void close(final UUID documentUniqueId) throws ParityException {
 		logger.info("close(UUID)");
 		logger.debug(documentUniqueId);
 		try {
 			final Document document = get(documentUniqueId);
 			assertStateTransition(document.getState(), ArtifactState.CLOSED);
 			documentIO.updateState(document.getId(), ArtifactState.CLOSED);
 
 			lock(document.getId());
 		}
 		catch(final RuntimeException rx) {
 			logger.error("Cannot close document:  " + documentUniqueId, rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Import a document. This will take a name, description and location of a
 	 * document and copy the document into an internal store, then returns the
 	 * newly created document.
 	 * 
 	 * @param name
 	 *            Name of the document you wish to import.
 	 * @param description
 	 *            Description of the document you wish to import.
 	 * @param file
 	 *            File content of the document
 	 * @return The newly created document.
 	 * @throws ParityException
 	 */
 	Document create(final String name, final String description, final File file)
 			throws ParityException {
 		logger.info("create(Project,String,String,File)");
 		logger.debug(name);
 		logger.debug(description);
 		logger.debug(file);
 		assertCanCreateArtifacts();
 		assertIsSessionValid();
 		Assert.assertTrue(
 				// TODO Centralize business rules about document creation.
 				"File \"" + file.getAbsolutePath() + "\" does not exist.",
 				file.exists());
 		Assert.assertTrue(
 				"create(Project,String,String,File)",
 				(file.length() <= IParityModelConstants.FILE_SIZE_UPPER_BOUNDS));
 		try {
 			final Calendar now = getTimestamp();
 			final Document document = new Document(preferences.getUsername(),
 					now, description, NO_FLAGS, UUIDGenerator.nextUUID(), name,
 					preferences.getUsername(), now);
 			document.setState(ArtifactState.ACTIVE);
 			final byte[] contentBytes = FileUtil.readBytes(file);
 			final DocumentContent content = new DocumentContent();
 			content.setContent(contentBytes);
 			content.setChecksum(MD5Util.md5Hex(contentBytes));
 			content.setDocumentId(document.getId());
 
 			// send a creation packet
 			final InternalSessionModel iSessionModel =
 				SessionModel.getInternalModel(getContext());
 			iSessionModel.sendCreate(document);
 
 			// create the document
 			documentIO.create(document, content);
 
 			// create the local file
 			final LocalFile localFile = getLocalFile(document);
 			localFile.write(contentBytes);
 
 			// create a version
 			createVersion(document.getId());
 
 			// flag the document as having been seen.
 			flagAsSEEN(document);
 
 			// fire a creation event
 			notifyCreation_objectCreated(document);
 			return document;
 		}
 		catch(IOException iox) {
 			logger.error("createDocument(Document)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("createDocument(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Create a new document version based upon an existing document. This will
 	 * check the cache for updates to the document, write the updates to the
 	 * document, then create a new version based upon that document.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @param action
 	 *            The action causing the version creation.
 	 * @param actionData
 	 *            The data associated with the version creation action.
 	 * @return The newly created version.
 	 * 
 	 * TODO If the user has ownership of the document; the local copy should
 	 * *NEVER* be overwritten.
 	 * 
 	 * TODO Have the ability to send individual versions to a contact.
 	 * 
 	 * @throws ParityException
 	 * @throws NotTrueAssertion
 	 *             If the logged in user is not the key holder.
 	 */
 	DocumentVersion createVersion(final Long documentId) throws ParityException {
 		logger.info("createVersion(Long)");
 		logger.debug(documentId);
 		assertLoggedInUserIsKeyHolder(documentId);
 		try {
 			final Document document = get(documentId);
 			final DocumentContent content = getContent(documentId);
 
 			// read the document local file
			final LocalFile documentLocalFile = getLocalFile(document);
			documentLocalFile.read();
 
 			// create a new version\version content
 			final DocumentVersion version = new DocumentVersion();
 			version.setArtifactId(documentId);
 			version.setArtifactType(document.getType());
 			version.setArtifactUniqueId(document.getUniqueId());
 			version.setCreatedBy(document.getCreatedBy());
 			version.setCreatedOn(document.getCreatedOn());
 			version.setName(document.getName());
 			version.setUpdatedBy(document.getUpdatedBy());
 			version.setUpdatedOn(document.getUpdatedOn());
 
 			final DocumentVersionContent versionContent = new DocumentVersionContent();
 			versionContent.setDocumentContent(content);
 			versionContent.setDocumentId(documentId);
 			versionContent.setVersionId(version.getVersionId());
 			documentIO.createVersion(version, versionContent);
 
 			// create the version local file
 			final LocalFile versionLocalFile = getLocalFile(document, version);
 			versionLocalFile.write(content.getContent());
 			versionLocalFile.lock();
 
 			// update the document updated by\on
 			document.setUpdatedBy(preferences.getUsername());
 			document.setUpdatedOn(getTimestamp());
 			documentIO.update(document);
 
 			// update the content bytes\checksum
			content.setContent(documentLocalFile.getFileBytes());
			content.setChecksum(documentLocalFile.getFileChecksum());
 			documentIO.updateContent(content);
 
 			// fire the object version event notification
 			notifyCreation_objectVersionCreated(version);
 			return version;
 		}
 		catch(IOException iox) {
 			logger.error("createVersion(Document,DocumentAction,DocumentActionData)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("createVersion(Document,DocumentAction,DocumentActionData)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Delete a document.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @throws ParityException
 	 */
 	void delete(final Long documentId) throws ParityException {
 		logger.info("delete(Long)");
 		logger.debug(documentId);
 		try {
 			final Document document = get(documentId);
 			assertStateTransition(document.getState(), ArtifactState.DELETED);
 			
 			// delete the document remotely
 			final InternalSessionModel iSModel = getInternalSessionModel();
 			iSModel.sendDelete(documentId);
 
 			// delete the document locally
 			// delete the versions
 			final Collection<DocumentVersion> versions = listVersions(documentId);
 			for(final DocumentVersion version : versions) {
 				getLocalFile(document, version).delete();
 				documentIO.deleteVersion(documentId, version.getVersionId());
 			}
 
 			// delete the document
 			final LocalFile localFile = getLocalFile(document);
 			localFile.delete();
 			localFile.deleteParent();
 			documentIO.delete(documentId);
 
 			// notify
 			notifyUpdate_objectDeleted(document);
 		}
 		catch(RuntimeException rx) {
 			logger.error("delete(UUID)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Take the given document, and export it to the specified file. This will
 	 * obtain the document's content, and save it to the file. Note that if file
 	 * exists, it will be overwritten.
 	 * 
 	 * @param document
 	 *            The document to export.
 	 * @param file
 	 *            The file to export the document to.
 	 * @throws ParityException
 	 */
 	void export(final Document document, final File file)
 			throws ParityException {
 		logger.info("export(Document)");
 		logger.debug(document);
 		logger.debug(file);
 		try {
 			Assert.assertNotTrue("File cannot already exist.", file.exists());
 			Assert.assertTrue("Could not create new file.", file.createNewFile());
 			writeDocumentContent(document.getId(), file);
 		}
 		catch(IOException iox) {
 			logger.error("export(Document,File)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("export(Document,File)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain a document with a specified id.
 	 * 
 	 * @param id
 	 *            The id of the document.
 	 * @return The document
 	 * @throws ParityException
 	 */
 	Document get(final Long id) throws ParityException {
 		logger.info("get(Long)");
 		logger.debug(id);
 		try { return documentIO.get(id); }
 		catch(RuntimeException rx) {
 			logger.error("get(Long)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain a document with the specified unique id.
 	 * 
 	 * @param documentUniqueId
 	 *            The document unique id.
 	 * @return The document.
 	 */
 	Document get(final UUID documentUniqueId) throws ParityException {
 		logger.info("get(UUID)");
 		logger.debug(documentUniqueId);
 		try { return documentIO.get(documentUniqueId); }
 		catch(final RuntimeException rx) {
 			logger.error("get(UUID)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain the document content for a given document.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @return The document's content.
 	 * @throws ParityException
 	 */
 	DocumentContent getContent(final Long documentId) throws ParityException {
 		logger.info("getContent(UUID)");
 		logger.debug(documentId);
 		try { return documentIO.getContent(documentId); }
 		catch(RuntimeException rx) {
 			logger.error("getContent(UUID)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain the latest document version.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @return The latest version.
 	 */
 	DocumentVersion getLatestVersion(final Long documentId)
 			throws ParityException {
 		logger.info("getLatestVersion(Long)");
 		logger.debug(documentId);
 		try { return documentIO.getLatestVersion(documentId); }
 		catch(RuntimeException rx) {
 			logger.error("Could not obtain latest version.", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain a document version.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @param versionId
 	 *            The version id.
 	 * @return The document version.
 	 * @throws ParityException
 	 */
 	DocumentVersion getVersion(final Long documentId, final Long versionId)
 			throws ParityException {
 		logger.info("getVersion(Long,Long)");
 		logger.debug(documentId);
 		logger.debug(versionId);
 		try { return documentIO.getVersion(documentId, versionId); }
 		catch(RuntimeException rx) {
 			logger.error("getVersion(UUID,String)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain the content for a specific version.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @param versionId
 	 *            The version id.
 	 * @return The content.
 	 * @throws ParityException
 	 */
 	DocumentVersionContent getVersionContent(final Long documentId,
 			final Long versionId) throws ParityException {
 		logger.info("getVersionContent(Long,Long)");
 		logger.debug(documentId);
 		logger.debug(versionId);
 		try { return documentIO.getVersionContent(documentId, versionId); }
 		catch(RuntimeException rx) {
 			logger.error("getVersionContent(DocumentVersion)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Determine whether or not the working version of the document is different
 	 * from the last version.
 	 * 
 	 * @return True if the working version is different from the last version.
 	 * @throws ParityException
 	 */
 	Boolean isWorkingVersionEqual(final Long documentId)
 			throws ParityException {
 		logger.info("isWorkingVersionDifferent(Long)");
 		logger.debug(documentId);
 		try {
 			final Document document = get(documentId);
 
 			final LocalFile localFile = getLocalFile(document);
 			localFile.read();
 			final String workingVersionChecksum = localFile.getFileChecksum();
 
 			final DocumentVersion version = getLatestVersion(documentId);
 			final DocumentVersionContent versionContent =
 				getVersionContent(documentId, version.getVersionId());
 
 			return versionContent.getDocumentContent().getChecksum()
 				.equals(workingVersionChecksum);
 		}
 		catch(final IOException iox) {
 			logger.error("Could not determine working version delta:  " + documentId, iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(final RuntimeException rx) {
 			logger.error("Could not determine working version delta:  " + documentId, rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain a list of documents.
 	 * 
 	 * @return A list of documents sorted by name.
 	 * @throws ParityException
 	 * 
 	 * @see ComparatorBuilder
 	 * @see #list(UUID, Comparator)
 	 */
 	Collection<Document> list() throws ParityException {
 		logger.info("list()");
 		return list(defaultComparator);
 	}
 
 	/**
 	 * Obtain a list of sorted documents.
 	 * 
 	 * @param comparator
 	 *            The comparator.
 	 * @return A sorted list of documents.
 	 * @throws ParityException
 	 * 
 	 * @see ComparatorBuilder
 	 */
 	Collection<Document> list(final Comparator<Artifact> comparator)
 			throws ParityException {
 		logger.info("list(Comparator<Artifact>)");
 		logger.debug(comparator);
 		try {
 			final Collection<Document> documents = documentIO.list();
 			ArtifactSorter.sortDocuments(documents, comparator);
 			return documents;
 		}
 		catch(RuntimeException rx) {
 			logger.error("list(UUID,Comparator<Artifact>)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain a list of document versions for a document.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @return The list of document versions; ordered by the version id
 	 *         ascending.
 	 * @throws ParityException
 	 * 
 	 * @see #listVersions(UUID, Comparator)
 	 */
 	Collection<DocumentVersion> listVersions(final Long documentId)
 			throws ParityException {
 		logger.info("listVersions(Long)");
 		logger.debug(documentId);
 		return listVersions(documentId, defaultVersionComparator);
 	}
 
 	/**
 	 * Obtain a list of document versions for a document; ordered by the
 	 * specified comparator.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @param comparator
 	 *            The document version sorter.
 	 * @return The list of document versions.
 	 * @throws ParityException
 	 * 
 	 * @see ComparatorBuilder
 	 */
 	Collection<DocumentVersion> listVersions(final Long documentId,
 			final Comparator<ArtifactVersion> comparator)
 			throws ParityException {
 		logger.info("listVersions(Document)");
 		logger.debug(documentId);
 		logger.debug(comparator);
 		try {
 			final Collection<DocumentVersion> versions =
 				documentIO.listVersions(documentId);
 			ArtifactSorter.sortVersions(versions, comparator);
 			return versions;
 		}
 		catch(RuntimeException rx) {
 			logger.error("listVersions(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Lock a document.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @throws ParityException
 	 */
 	void lock(final Long documentId) throws ParityException {
 		logger.info("lock(Long)");
 		logger.debug(documentId);
 		try {
 			// re-create the local file from the meta-data
 			final Document document = get(documentId);
 			final LocalFile localFile = getLocalFile(document);
 			localFile.delete();
 			localFile.write(getContent(documentId).getContent());
 			localFile.lock();
 		}
 		catch(IOException iox) {
 			logger.error("lock(Long)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("lock(Long)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Open a document.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @throws ParityException
 	 */
 	void open(final Long documentId) throws ParityException {
 		logger.info("open(Long)");
 		logger.debug(documentId);
 		try {
 			final Document document = get(documentId);
 
 			// open the local file
 			final LocalFile localFile = getLocalFile(document);
 			localFile.open();
 
 			// flag it as having been seen
 			flagAsSEEN(document);
 		}
 		catch(IOException iox) {
 			logger.error("open(UUID)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("open(UUID)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Open a document version. Extract the version's content and open it.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @param versionId
 	 *            The version id.
 	 * @throws ParityException
 	 */
 	void openVersion(final Long documentId, final Long versionId)
 			throws ParityException {
 		logger.info("openVersion(UUID,String)");
 		logger.debug(documentId);
 		logger.debug(versionId);
 		try {
 			final Document document = get(documentId);
 			final DocumentVersion version = getVersion(documentId, versionId);
 
 			final LocalFile localFile = getLocalFile(document, version);
 			localFile.open();
 		}
 		catch(IOException iox) {
 			logger.error("openVersion(UUID,String)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("openVersion(UUID,String)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Receive an xmpp document. If no local document exists; create it; then
 	 * insert the xmpp document as a version of the local document.
 	 * 
 	 * @param xmppDocument
 	 *            The xmpp document.
 	 * @throws ParityException
 	 */
 	void receive(final XMPPDocument xmppDocument) throws ParityException {
 		logger.info("receiveDocument(XMPPDocument)");
 		logger.debug(xmppDocument);
 		try {
 			final Document existingDocument = get(xmppDocument.getUniqueId());
 			logger.debug(existingDocument);
 			if(null == existingDocument) { receiveCreate(xmppDocument); }
 			else { receiveUpdate(xmppDocument, existingDocument); }
 		}
 		catch(IOException iox) {
 			logger.error("receiveDocument(XMPPDocument)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("receiveDocument(XMPPDocument)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Remove a document creation event listener.
 	 * 
 	 * @param listener
 	 *            The listener to remove.
 	 */
 	void removeListener(final CreationListener listener) {
 		logger.info("removeCreationListener(CreationListener)");
 		logger.debug(listener);
 		Assert.assertNotNull(
 				"removeCreationListener(CreationListener)", listener);
 		synchronized(DocumentModelImpl.creationListenersLock) {
 			Assert.assertTrue(
 					"removeCreationListener(CreationListener)",
 					DocumentModelImpl.creationListeners.contains(listener));
 			DocumentModelImpl.creationListeners.remove(listener);
 		}
 	}
 
 	/**
 	 * Remove a document update event listener.
 	 * 
 	 * @param listener
 	 *            The listener to remove.
 	 */
 	void removeListener(final UpdateListener listener) {
 		logger.info("removeUpdateListener(UpdateListener)");
 		logger.debug(listener);
 		Assert.assertNotNull("removeUpdateListener(UpdateListener)", listener);
 		synchronized (DocumentModelImpl.updateListenersLock) {
 			Assert.assertTrue(
 					"removeUpdateListener(UpdateListener)",
 					DocumentModelImpl.updateListeners.contains(listener));
 			DocumentModelImpl.updateListeners.remove(listener);
 		}
 	}
 
 	/**
 	 * Unlock a document.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @throws ParityException
 	 */
 	void unlock(final Long documentId) throws ParityException {
 		logger.info("unlock(Long)");
 		logger.debug(documentId);
 		try {
 			// re-create the local file from the meta-data
 			final Document d = get(documentId);
 			final LocalFile localFile = getLocalFile(d);
 			localFile.delete();
 			localFile.write(getContent(documentId).getContent());
 		}
 		catch(IOException iox) {
 			logger.error("unlock(UUID)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("unlock(UUID)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	// USED BY THE ABSTRACT MODEL
 	void update(final Document document) throws ParityException {
 		logger.info("update(Document)");
 		logger.debug(document);
 		try {
 			documentIO.update(document);
 			notifyUpdate_objectUpdated(document);
 		}
 		catch(final RuntimeException rx) {
 			logger.error("update(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Create a document local file reference for a given document.
 	 * 
 	 * @param document
 	 *            The document.
 	 * @return The document local file reference.
 	 */
 	private LocalFile getLocalFile(final Document document) {
 		return new LocalFile(workspace, document);
 	}
 
 	/**
 	 * Create a document local file reference for a given version.
 	 * 
 	 * @param version
 	 *            The version.
 	 * @return The document local file reference.
 	 */
 	private LocalFile getLocalFile(final Document document,
 			final DocumentVersion version) {
 		return new LocalFile(workspace, document, version);
 	}
 
 	/**
 	 * Insert a version for a document.
 	 * @param documentId The document.
 	 * @param version The version to insert.
 	 */
 	private void insertVersion(final Long documentId,
 			final DocumentVersion version,
 			final DocumentVersionContent versionContent) throws ParityException {
 		logger.info("insertVersion(Long,DocumentVersion,DocumentVersionContent)");
 		logger.debug(documentId);
 		logger.debug(version);
 		logger.debug(versionContent);
 		try {
 			final Document document = get(documentId);
 
 			// insert version info into db
 			documentIO.createVersion(version.getVersionId(), version, versionContent);
 
 			// create version local file
 			final LocalFile versionFile = getLocalFile(document, version);
 			versionFile.write(versionContent.getDocumentContent().getContent());
 			versionFile.lock();
 
 			// fire the object version event notification
 			notifyCreation_objectVersionCreated(version);
 		}
 		catch(IOException iox) {
 			logger.error("createVersion(Document,DocumentAction,DocumentActionData)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("createVersion(Document,DocumentAction,DocumentActionData)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Check and see if this version is the latest version.
 	 * 
 	 * @param version
 	 *            A document version.
 	 * @return True if this is the latest local version of the document.
 	 */
 	private Boolean isLatestLocalVersion(final DocumentVersion version) {
 		final DocumentVersion latestLocalVersion =
 			documentIO.getLatestVersion(version.getArtifactId());
 		return latestLocalVersion.getVersionId().equals(version.getVersionId());
 	}
 
 	/**
 	 * Fire the objectCreated event for all of the creation listeners.
 	 * 
 	 * @param document
 	 *            The document to use as the event source.
 	 * @see CreationListener#objectCreated(CreationEvent)
 	 */
 	private void notifyCreation_objectCreated(final Document document) {
 		synchronized(DocumentModelImpl.creationListenersLock) {
 			for(CreationListener listener : DocumentModelImpl.creationListeners) {
 				listener.objectCreated(new CreationEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * Fire the objectVersionCreated event for all of the creation listeners.
 	 * 
 	 * @param documentVersion
 	 *            The document version to use as the event source.
 	 * @see CreationListener#objectVersionCreated(VersionCreationEvent)
 	 */
 	private void notifyCreation_objectVersionCreated(
 			final DocumentVersion documentVersion) {
 		synchronized (DocumentModelImpl.creationListenersLock) {
 			for (CreationListener listener : DocumentModelImpl.creationListeners) {
 				listener.objectVersionCreated(new VersionCreationEvent(
 						documentVersion));
 			}
 		}
 	}
 
 	/**
 	 * Fire the object deleted event for all of the update listeners.
 	 * 
 	 * @param document
 	 *            The document that was deleted.
 	 */
 	private void notifyUpdate_objectDeleted(final Document document) {
 		synchronized(DocumentModelImpl.updateListeners) {
 			for(UpdateListener listener : DocumentModelImpl.updateListeners) {
 				listener.objectDeleted(new DeleteEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * Fire the objectReceived event for all update listeners.
 	 * 
 	 * @param document
 	 *            The document to use as the event source.
 	 * @see UpdateListener#objectReceived(UpdateEvent)
 	 */
 	private void notifyUpdate_objectReceived(final Document document) {
 		synchronized(DocumentModelImpl.updateListenersLock) {
 			for(UpdateListener listener : DocumentModelImpl.updateListeners) {
 				listener.objectReceived(new UpdateEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * Fire the objectUpdated event for all of the udpate listeners.
 	 * 
 	 * @param document
 	 *            The document to use as the event source.
 	 * @see UpdateListener#objectUpdated(UpdateEvent)
 	 */
 	private void notifyUpdate_objectUpdated(final Document document) {
 		synchronized(DocumentModelImpl.updateListenersLock) {
 			for(UpdateListener listener : DocumentModelImpl.updateListeners) {
 				listener.objectUpdated(new UpdateEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * This is the first time this particular document has been recieved. We
 	 * need to create the document; send a subscription request; then receive
 	 * update.
 	 * 
 	 * @param xmppDocument
 	 *            The xmpp document.
 	 * @throws ParityException
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void receiveCreate(final XMPPDocument xmppDocument)
 			throws ParityException, FileNotFoundException, IOException {
 		// create the document
 		final Document document = new Document();
 		document.setCreatedBy(xmppDocument.getCreatedBy());
 		document.setCreatedOn(xmppDocument.getCreatedOn());
 		document.setName(xmppDocument.getName());
 		document.setState(ArtifactState.ACTIVE);
 		document.setUniqueId(xmppDocument.getUniqueId());
 		document.setUpdatedBy(xmppDocument.getUpdatedBy());
 		document.setUpdatedOn(xmppDocument.getUpdatedOn());
 
 		final DocumentContent content = new DocumentContent();
 		content.setChecksum(MD5Util.md5Hex(xmppDocument.getContent()));
 		content.setContent(xmppDocument.getContent());
 		documentIO.create(document, content);
 
 		// create the document file
 		final LocalFile file = getLocalFile(document);
 		file.write(content.getContent());
 
 		// send a subscription request
 		getInternalSessionModel().sendSubscribe(document);
 
 		receiveUpdate(xmppDocument, document);
 	}
 
 	/**
 	 * Insert the corresponding version for the xmpp document received. Check to
 	 * see if this is the latest version locally; and if it is; update the
 	 * document\document content. Notify that a version has been received.
 	 * 
 	 * @param xmppDocument
 	 *            The xmpp document received.
 	 * @param document
 	 *            The existing local document.
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 * @throws ParityException
 	 */
 	private void receiveUpdate(final XMPPDocument xmppDocument,
 			final Document document) throws FileNotFoundException, IOException,
 			ParityException {
 		final DocumentVersion version = new DocumentVersion();
 		version.setArtifactId(document.getId());
 		version.setArtifactType(document.getType());
 		version.setArtifactUniqueId(document.getUniqueId());
 		version.setCreatedBy(xmppDocument.getCreatedBy());
 		version.setCreatedOn(xmppDocument.getCreatedOn());
 		version.setName(document.getName());
 		version.setUpdatedBy(xmppDocument.getUpdatedBy());
 		version.setUpdatedOn(xmppDocument.getUpdatedOn());
 		version.setVersionId(xmppDocument.getVersionId());
 
 		final DocumentContent content = new DocumentContent();
 		content.setChecksum(MD5Util.md5Hex(xmppDocument.getContent()));
 		content.setContent(xmppDocument.getContent());
 		content.setDocumentId(version.getArtifactId());
 
 		final DocumentVersionContent versionContent = new DocumentVersionContent();
 		versionContent.setDocumentContent(content);
 		versionContent.setDocumentId(version.getArtifactId());
 		versionContent.setVersionId(version.getVersionId());
 
 		insertVersion(version.getArtifactId(), version, versionContent);
 
 		if(isLatestLocalVersion(version)) {
 			document.setUpdatedBy(xmppDocument.getUpdatedBy());
 			document.setUpdatedOn(xmppDocument.getUpdatedOn());
 
 			documentIO.update(document);
 
 			documentIO.updateContent(content);
 		}
 
 		// flag the document
 		flagAsNotSEEN(document);
 
 		// notify
 		notifyUpdate_objectReceived(document);
 	}
 
 	/**
 	 * Write the content of a document to a file.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @param file
 	 *            The target file.
 	 * @throws IOException
 	 */
 	private void writeDocumentContent(final Long documentId, final File file)
 			throws ParityException, IOException {
 		final DocumentContent content = getContent(documentId);
 		FileUtil.writeBytes(file, content.getContent());
 	}
 }
