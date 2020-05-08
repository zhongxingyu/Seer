 /*
  * Generated On: Jun 27 06 12:13:12 PM
  */
 package com.thinkparity.model.parity.model.container;
 
 import java.util.Comparator;
 import java.util.List;
 
 import com.thinkparity.model.parity.ParityException;
 import com.thinkparity.model.parity.api.events.ContainerListener;
 import com.thinkparity.model.parity.model.Context;
 import com.thinkparity.model.parity.model.artifact.Artifact;
 import com.thinkparity.model.parity.model.document.Document;
 import com.thinkparity.model.parity.model.filter.Filter;
 import com.thinkparity.model.parity.model.workspace.Workspace;
 import com.thinkparity.model.parity.model.workspace.WorkspaceModel;
 import com.thinkparity.model.xmpp.JabberId;
 
 /**
  * <b>Title:</b>thinkParity Container Model<br>
  * <b>Description:</b>
  *
  * @author CreateModel.groovy
  * @version 1.1.2.3
  */
 public class ContainerModel {
 
     /**
 	 * Create a Container interface.
 	 * 
 	 * @param context
 	 *            A thinkParity internal context.
 	 * @return The internal Container interface.
 	 */
 	public static InternalContainerModel getInternalModel(final Context context) {
 		final Workspace workspace = WorkspaceModel.getModel().getWorkspace();
 		return new InternalContainerModel(workspace, context);
 	}
 
     /**
 	 * Create a Container interface.
 	 * 
 	 * @return The Container interface.
 	 */
 	public static ContainerModel getModel() {
 		final Workspace workspace = WorkspaceModel.getModel().getWorkspace();
 		return new ContainerModel(workspace);
 	}
 
 	/** The model implementation. */
 	private final ContainerModelImpl impl;
 
 	/** The model implementation synchronization lock. */
 	private final Object implLock;
 
 	/**
 	 * Create ContainerModel.
 	 *
 	 * @param workspace
 	 *		The thinkParity workspace.
 	 */
 	protected ContainerModel(final Workspace workspace) {
 		super();
 		this.impl = new ContainerModelImpl(workspace);
 		this.implLock = new Object();
 	}
 
 	/**
      * Add a document to a container.
      * 
      * @param containerId
      *            A container id.
      * @param documentId
      *            A document id.
      */
     public void addDocument(final Long containerId, final Long documentId)
             throws ParityException {
         synchronized(getImplLock()) {
             getImpl().addDocument(containerId, documentId);
         }
     }
 
     /**
      * Add a container listener.
      * 
      * @param listener
      *            A container listener.
      */
     public void addListener(final ContainerListener listener) {
         synchronized(implLock) { impl.addListener(listener); }
     }
 
     /**
      * Archive a container.
      * 
      * @param containerId
      *            A container id.
      */
     public void archive(final Long containerId) {
         Assert.assertNotYetImplemented("");
     }
 
     /**
      * Archive a container.
      * 
      * @param containerId
      *            A container id.
      * @param progressIndicator
      *            A progress indicator.
      */
     public void archive(final Long containerId,
             final ProgressIndicator progressIndicator) {
         Assert.assertNotYetImplemented("");
     }
 
     /**
      * Close a container.
      * 
      * @param containerId
      *            A container id.
      */
     public void close(final Long containerId) throws ParityException {
         synchronized(implLock) { impl.close(containerId); }
     }
 
     /**
      * Create a container.
      * 
      * @param name
      *            The container name.
      * @return The new container.
      */
     public Container create(final String name) throws ParityException {
         synchronized(implLock) { return impl.create(name); }
     }
 
     /**
      * Delete a container.
      * 
      * @param containerId
      *            A container id.
      */
     public void delete(final Long containerId) throws ParityException {
         synchronized(implLock) { impl.delete(containerId); }
     }
 
     /**
      * Publish the container.
      * 
      * @param containerId
      *            The container id.
      * @throws ParityException
      */
     public void publish(final Long containerId) throws ParityException {
         synchronized(implLock) { impl.publish(containerId); }
     }
 
     /**
      * Reactivate a container.
      * 
      * @param containerId
      *            A container id.
      * @throws ParityException
      */
     public void reactivate(final Long containerId) throws ParityException {
         synchronized(implLock) { impl.reactivate(containerId); }
     }
 
     /**
      * Read the containers.
      * 
      * @return A list of containers.
      */
 	public List<Container> read() { synchronized(implLock) { return impl.read(); } }
 
     /**
      * Read the containers.
      * 
      * @param comparator
      *            A sort ordering to user.
      * @return A list of containers.
      */
     public List<Container> read(final Comparator<Artifact> comparator) {
         synchronized(implLock) { return impl.read(comparator); }
     }
 
     /**
      * Read the containers.
      * 
      * @param comparator
      *            A sort ordering to user.
      * @param filter
      *            A filter to apply.
      * @return A list of containers.
      */
     public List<Container> read(final Comparator<Artifact> comparator,
             final Filter<? super Artifact> filter) {
         synchronized(implLock) { return impl.read(comparator, filter); }
     }
 
     /**
      * Read the containers.
      * 
      * @param filter
      *            A filter to apply.
      * @return A list of containers.
      */
     public List<Container> read(final Filter<? super Artifact> filter) {
         synchronized(implLock) { return impl.read(filter); }
     }
 
     /**
      * Read a container.
      * 
      * @param containerId
      *            A container id.
      * @return A container.
      */
     public Container read(final Long containerId) {
         synchronized(implLock) { return impl.read(containerId); }
     }
 
     /**
      * Read the documents for the container.
      * 
      * @param containerId
      *            A container id.
      * @return A list of documents.
      * @throws ParityException
      */
     public List<Document> readDocuments(final Long containerId)
             throws ParityException {
         synchronized(implLock) { return impl.readDocuments(containerId); }
     }
 
     /**
      * Read the documents for the container.
      * 
      * @param containerId
      *            A container id.
      * @param comparator
      *            A document comparator.
      * @return A list of documents.
      * @throws ParityException
      */
     public List<Document> readDocuments(final Long containerId,
             final Comparator<Artifact> comparator) throws ParityException {
         synchronized(implLock) { return impl.readDocuments(containerId, comparator); }
     }
 
     /**
      * Read the documents for the container.
      * 
      * @param containerId
      *            A container id.
      * @param comparator
      *            A document comparator.
      * @param filter
      *            A document filter.
      * @return A list of documents.
      * @throws ParityException
      */
     public List<Document> readDocuments(final Long containerId,
             final Comparator<Artifact> comparator,
             final Filter<? super Artifact> filter) throws ParityException {
         synchronized(implLock) { return impl.readDocuments(containerId, comparator, filter); }
     }
 
     /**
      * Read the document versions for the container.
      * 
      * @param containerId
      *            A container id.
      * @param filter
      *            A document filter.
      * @return A list of document versions.
      * @throws ParityException
      */
     public List<Document> readDocuments(final Long containerId,
             final Filter<? super Artifact> filter) throws ParityException {
         synchronized(implLock) { return impl.readDocuments(containerId, filter); }
     }
 
     /**
      * Read the latest container version.
      * 
      * @param containerId
      *            A container id.
      * @return A container version.
      */
     public ContainerVersion readLatestVersion(final Long containerId) {
         synchronized(implLock) { return impl.readLatestVersion(containerId); }
     }
 
     /**
      * Remove a document from a container.
      * 
      * @param containerId
      *            A container id.
      * @param documentId
      *            A document id.
      */
     public void removeDocument(final Long containerId, final Long documentId)
             throws ParityException {
         synchronized(implLock) { impl.removeDocument(containerId, documentId); }
     }
 
     /**
      * Remove a container listener.
      * 
      * @param listener
      *            A container listener.
      */
     public void removeListener(final ContainerListener listener) {
         synchronized(implLock) { impl.removeListener(listener); }
     }
 
 	/**
      * Share the container with a user. The user will receive the latest version
      * of the container and become part of the container's team.
      * 
      * @param containerId
      *            The container id.
      * @param jabberId
      *            The jabber id.
      */
     public void share(final Long containerId, final JabberId jabberId)
             throws ParityException {
         synchronized(implLock) { impl.share(containerId, jabberId); }
     }
 
     /**
 	 * Obtain the model implementation.
 	 * 
 	 * @return The model implementation.
 	 */
 	protected ContainerModelImpl getImpl() { return impl; }
 
     /**
 	 * Obtain the model implementation synchronization lock.
 	 * 
 	 * @return The model implementation synchrnoization lock.
 	 */
 	protected Object getImplLock() { return implLock; }
 
 }
