 /*
  * Aug 6, 2005
  */
 package com.thinkparity.model.parity.model;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.model.log4j.ModelLoggerFactory;
 import com.thinkparity.model.parity.ParityException;
 import com.thinkparity.model.parity.api.ParityObject;
 import com.thinkparity.model.parity.api.ParityObjectFlag;
 import com.thinkparity.model.parity.model.document.Document;
 import com.thinkparity.model.parity.model.document.DocumentModel;
 import com.thinkparity.model.parity.model.project.Project;
 import com.thinkparity.model.parity.model.project.ProjectModel;
 import com.thinkparity.model.parity.model.workspace.Preferences;
 import com.thinkparity.model.parity.model.workspace.Workspace;
 
 
 /**
  * AbstractModelImpl
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public abstract class AbstractModelImpl {
 
 	/**
 	 * Empty list of parity object flags.
 	 */
 	protected static final Collection<ParityObjectFlag> NO_FLAGS =
 		Collections.unmodifiableCollection(new Vector<ParityObjectFlag>(0));
 
 	/**
 	 * Handle to an apache logger.
 	 */
 	protected final Logger logger =
 		ModelLoggerFactory.getLogger(AbstractModelImpl.class);
 
 	/**
 	 * Handle to the parity model preferences.
 	 */
 	protected final Preferences preferences;
 
 	/**
 	 * Handle to the parity model workspace.
 	 */
 	protected final Workspace workspace;
 
 	/**
 	 * Create an AbstractModelImpl
 	 * 
 	 * @param workspace
 	 *            Handle to an existing parity model workspace.
 	 */
 	protected AbstractModelImpl(final Workspace workspace) {
 		super();
 		this.workspace = workspace;
 		this.preferences = (null == workspace ? null : workspace.getPreferences());
 	}
 
 	/**
 	 * Assert that the calling method has not yet been implemented.
 	 *
 	 */
 	protected void assertNYI() {
 		Assert.assertNotYetImplemented("The calling method has not yet been implemented.");
 	}
 
 	/**
 	 * Flag the object as not having been seen. This will remove the seen flag
 	 * from the object, and remove the seen flag from the parent objects all the
 	 * way up the tree.
 	 * 
 	 * @param parityObject
 	 *            The object to flag.
 	 * @throws ParityException
 	 */
 	protected void flagAsNotSEEN(final ParityObject parityObject)
 			throws ParityException {
 		switch(parityObject.getType()) {
 		case DOCUMENT:
 			flagAsNotSEEN((Document) parityObject);
 			break;
 		case PROJECT:
 			flagAsNotSEEN((Project) parityObject);
 			break;
 		default:
 			throw Assert.createUnreachable(
 					"removeFlag(ParityObject,ParityObjectFlag)");
 		}
 	}
 
 	/**
 	 * Flag the object as having been seen. This will add the SEEN flag to the
 	 * object; then check the parent to see if all of the siblings of the object
 	 * contain the SEEN flag; and if they do, the parent will be flagged as
 	 * well.
 	 * 
 	 * @param parityObject
 	 *            The object to flag.
 	 * @throws ParityException
 	 */
 	protected void flagAsSEEN(final ParityObject parityObject) throws ParityException {
 		switch(parityObject.getType()) {
 		case DOCUMENT:
 			flagAsSEEN((Document) parityObject);
 			break;
 		case PROJECT:
 			flagAsSEEN((Project) parityObject);
 			break;
 		default:
 			throw Assert.createUnreachable("flagAsSEEN(ParityObject)");
 		}
 	}
 
 	/**
 	 * Obtain a handle to the project model.
 	 * 
 	 * @return A handle to the project model.
 	 */
 	protected DocumentModel getDocumentModel() { return DocumentModel.getModel(); }
 
 	/**
 	 * Obtain the parent project for the document.
 	 * 
 	 * @param parityObject
 	 *            The object.
 	 * @return The parent project.
 	 * @throws ParityException
 	 */
 	protected Project getParent(final ParityObject parityObject) throws ParityException {
 		return getProjectModel().get(parityObject.getParentId());
 	}
 
 	/**
 	 * Obtain a handle to the project model.
 	 * 
 	 * @return A handle to the project model.
 	 */
 	protected ProjectModel getProjectModel() { return ProjectModel.getModel(); }
 
 	/**
 	 * Obtain the siblings of a parity object.
 	 * 
 	 * @param parityObject
 	 *            The object.
 	 * @return A list of parity object siblings.
 	 * @throws ParityException
 	 */
 	protected Collection<ParityObject> listSiblings(final ParityObject parityObject) throws ParityException {
 		final Project parent = getParent(parityObject);
 		final Collection<ParityObject> siblings = new Vector<ParityObject>(7);
 		if(null != parent) {
 			siblings.addAll(getDocumentModel().list(parent));
 			siblings.addAll(getProjectModel().list(parent));
 		}
 		return siblings;
 	}
 
 	/**
 	 * Remove the seen flag from the document. This will also update the parent
 	 * so that its seen flag is removed.
 	 * 
 	 * @param document
 	 *            The document to flag.
 	 * @throws ParityException
 	 */
 	private void flagAsNotSEEN(final Document document) throws ParityException {
 		if(document.contains(ParityObjectFlag.SEEN)) {
 			// remove the seen flag
 			document.remove(ParityObjectFlag.SEEN);
 			getDocumentModel().update(document);
 
 			// remove the parent's seen flag
 			final Project parent = getProjectModel().get(document.getParentId());
 			if(null != parent) { flagAsNotSEEN(parent); }
 		}
 	}
 
 	/**
 	 * Remove the seen flag from the project. This will also update the parent
 	 * so that its seen flag is removed.
 	 * 
 	 * @param project
 	 *            The project flag.
 	 * @throws ParityException
 	 */
 	private void flagAsNotSEEN(final Project project) throws ParityException {
 		if(project.contains(ParityObjectFlag.SEEN)) {
 			final ProjectModel projectModel = getProjectModel();
 
 			// remove the seen flag
 			project.remove(ParityObjectFlag.SEEN);
 			projectModel.update(project);
 
 			// remove the parent's seen flag
 			final Project parent = projectModel.get(project.getParentId());
 			if(null != parent) { flagAsNotSEEN(parent); }
 		}
 	}
 
 	/**
 	 * Flag a document as having been seen. If the document's sibling objects
 	 * have all been seen as well, the parent will be updated.
 	 * 
 	 * @param document
 	 *            The document to flag.
 	 * @throws ParityException
 	 */
 	private void flagAsSEEN(final Document document) throws ParityException {
 		if(!document.contains(ParityObjectFlag.SEEN)) {
 			document.add(ParityObjectFlag.SEEN);
 			getDocumentModel().update(document);
 
 			// if all of the siblings have the SEEN flag; flag the parent as
 			// well
 			final Collection<ParityObject> siblings = listSiblings(document);
 			for(ParityObject sibling : siblings) {
 				if(!sibling.contains(ParityObjectFlag.SEEN)) { return; }
 			}
			flagAsSEEN(getParent(document));
 		}
 	}
 
 	/**
 	 * Flag a project as having been seen. If the project's sibling objects have
 	 * all been seen as well, the parent will be updated.
 	 * 
 	 * @param project
 	 *            The project to flag.
 	 * @throws ParityException
 	 */
 	private void flagAsSEEN(final Project project) throws ParityException {
 		if(!project.contains(ParityObjectFlag.SEEN)) {
 			project.add(ParityObjectFlag.SEEN);
 			getProjectModel().update(project);
 
 			// if all of the siblings have the SEEN flag; flag the parent as
 			// well
 			final Collection<ParityObject> siblings = listSiblings(project);
 			for(ParityObject sibling : siblings) {
 				if(!sibling.contains(ParityObjectFlag.SEEN)) { return; }
 			}
			flagAsSEEN(getParent(project));
 		}
 	}
 }
