 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
  *
  * $Id: ClassificationActionsBean.java 62933 2009-10-14 10:54:33Z ldoguin $
  */
 
 package org.nuxeo.ecm.platform.classification;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.*;
 import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.contexts.Context;
 import org.jboss.seam.core.Events;
 import org.jboss.seam.faces.FacesMessages;
 import org.nuxeo.ecm.classification.api.ClassificationConstants;
 import org.nuxeo.ecm.classification.api.ClassificationResult;
 import org.nuxeo.ecm.classification.api.ClassificationService;
 import org.nuxeo.ecm.classification.api.adapter.Classification;
 import org.nuxeo.ecm.core.api.*;
 import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
 import org.nuxeo.ecm.platform.audit.api.AuditEventTypes;
 import org.nuxeo.ecm.platform.contentview.jsf.ContentView;
 import org.nuxeo.ecm.platform.contentview.seam.ContentViewActions;
 import org.nuxeo.ecm.platform.query.api.PageProvider;
 import org.nuxeo.ecm.platform.query.api.PageProviderService;
 import org.nuxeo.ecm.platform.query.core.DocumentModelListPageProvider;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.ecm.webapp.action.TypesTool;
 import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
 import org.nuxeo.ecm.webapp.helpers.EventNames;
 import org.nuxeo.ecm.webapp.tree.DocumentTreeNode;
 import org.nuxeo.ecm.webapp.tree.DocumentTreeNodeImpl;
 import org.nuxeo.ecm.webapp.tree.TreeManager;
 import org.nuxeo.runtime.api.Framework;
 
 import javax.faces.event.ValueChangeEvent;
 import java.io.Serializable;
 import java.util.*;
 
 import static org.jboss.seam.ScopeType.EVENT;
 import static org.jboss.seam.international.StatusMessage.Severity.*;
 import static org.nuxeo.ecm.classification.api.ClassificationService.UNCLASSIFY_STATE.NOT_CLASSIFIED;
 import static org.nuxeo.ecm.classification.api.ClassificationService.UNCLASSIFY_STATE.NOT_ENOUGH_RIGHTS;
 import static org.nuxeo.ecm.classification.api.ClassificationService.UNCLASSIFY_STATE.UNCLASSIFIED;
 import static org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY;
 
 /**
  * Handles classification actions
  * 
  * @author Anahide Tchertchian
  */
 @Name("classificationActions")
 @Scope(ScopeType.CONVERSATION)
 public class ClassificationActionsBean implements ClassificationActions {
 
     private static final long serialVersionUID = 1L;
 
     private static final Log log = LogFactory.getLog(ClassificationActionsBean.class);
 
     @In(create = true, required = false)
     protected transient FacesMessages facesMessages;
 
     @In
     protected transient Context eventContext;
 
     @In(create = true)
     protected Map<String, String> messages;
 
     @In(create = true)
     private transient NavigationContext navigationContext;
 
     @In(create = true, required = false)
     protected transient CoreSession documentManager;
 
     @In(create = true)
     protected transient DocumentsListsManager documentsListsManager;
 
     @In(create = true)
     protected transient ContentViewActions contentViewActions;
 
     @In(create = true)
     protected TypesTool typesTool;
 
     protected DocumentModelList currentDocumentClassifications;
 
     protected DocumentModelList classificationRoots;
 
     protected DocumentModel currentClassificationRoot;
 
     protected DocumentTreeNode currentClassificationTree;
 
     protected DocumentModelList editableClassificationRoots;
 
     protected DocumentModel currentEditableClassificationRoot;
 
     protected DocumentTreeNode currentEditableClassificationTree;
 
     protected String currentSelectionViewId;
 
     protected List<DocumentModel> getFilteredSelectedDocumentsForClassification()
             throws ClientException {
         ClassificationService clService;
         try {
             clService = Framework.getService(ClassificationService.class);
         } catch (Exception e) {
             throw new ClientException("Could not find Classification Service",
                     e);
         }
         List<DocumentModel> filtered = new DocumentModelListImpl();
         List<DocumentModel> docs = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
         if (docs != null) {
             for (DocumentModel doc : docs) {
                 if (doc != null && clService.isClassifiable(doc)) {
                     filtered.add(doc);
                 }
             }
         }
         return filtered;
     }
 
     public boolean getCanClassifyCurrentDocument() throws ClientException {
         return Framework.getLocalService(ClassificationService.class).isClassifiable(
                 navigationContext.getCurrentDocument());
     }
 
     public boolean getCanClassifyFromCurrentSelection() throws ClientException {
         List<DocumentModel> classifiable = getFilteredSelectedDocumentsForClassification();
         return !classifiable.isEmpty();
     }
 
     public Collection<DocumentModel> getTargetDocuments()
             throws ClientException {
         Collection<DocumentModel> res = new ArrayList<DocumentModel>();
         res.add(navigationContext.getCurrentDocument());
         return res;
     }
 
     public String classify(ClassificationTreeNode node) throws ClientException {
         Collection<DocumentModel> targetDocs = getTargetDocuments();
         if (node != null) {
             classify(targetDocs, node.getDocument());
             // refresh tree
             node.resetChildren();
         }
         return null;
     }
 
     public Collection<DocumentModel> getMassTargetDocuments()
             throws ClientException {
         if (!documentsListsManager.isWorkingListEmpty(CURRENT_SELECTION_FOR_CLASSIFICATION)) {
             return documentsListsManager.getWorkingList(CURRENT_SELECTION_FOR_CLASSIFICATION);
         } else {
             log.debug("No documents selection in context to process classification on");
             return null;
         }
     }
 
     public void simpleClassify(ClassificationTreeNode node)
             throws ClientException {
         if (node != null) {
             Collection<DocumentModel> targetDocs = Arrays.asList(navigationContext.getCurrentDocument());
 
             classify(targetDocs, node.getDocument());
             // refresh tree
             node.resetChildren();
         }
     }
 
     public String massClassify(ClassificationTreeNode node)
             throws ClientException {
         Collection<DocumentModel> targetDocs = getMassTargetDocuments();
         if (node != null && targetDocs != null) {
             classify(targetDocs, node.getDocument());
             // refresh tree
             node.resetChildren();
         }
         return null;
     }
 
     /**
      * Classifies given documents in given classification folder.
      * 
      * @return true on error
      */
     @SuppressWarnings("unchecked")
     public boolean classify(Collection<DocumentModel> targetDocs,
             DocumentModel classificationFolder) throws ClientException {
         if (targetDocs.isEmpty()) {
             facesMessages.add(
                     ERROR,
                     messages.get("feedback.classification.noDocumentsToClassify"));
             return true;
         }
         if (classificationFolder == null) {
             facesMessages.add(
                     ERROR,
                     messages.get("feedback.classification.noClassificationFolder"));
             return true;
         }
         if (!classificationFolder.hasSchema(ClassificationConstants.CLASSIFICATION_SCHEMA_NAME)) {
             facesMessages.add(
                     ERROR,
                     messages.get("feedback.classification.invalidClassificationFolder"));
             return true;
         }
 
         DocumentRef classificationRef = classificationFolder.getRef();
         if (!documentManager.hasPermission(classificationRef,
                 ClassificationConstants.CLASSIFY)) {
             facesMessages.add(ERROR,
                     messages.get("feedback.classification.unauthorized"));
             return true;
         }
 
         ClassificationService classificationService = Framework.getLocalService(ClassificationService.class);
 
         ClassificationResult<ClassificationService.CLASSIFY_STATE> classify = classificationService.classify(
                 classificationFolder, targetDocs);
 
         Events.instance().raiseEvent(AuditEventTypes.HISTORY_CHANGED);
 
         boolean invalid = classify.contains(ClassificationService.CLASSIFY_STATE.INVALID);
         boolean alreadyClassified = classify.contains(ClassificationService.CLASSIFY_STATE.ALREADY_CLASSIFIED);
 
         if (invalid && alreadyClassified) {
             facesMessages.add(
                     WARN,
                     messages.get("feedback.classification.requestDoneButSomeWereAlreadyClassifiedAndSomeInvalid"));
         } else if (invalid) {
             facesMessages.add(
                     WARN,
                     messages.get("feedback.classification.requestDoneButSomeInvalid"));
         } else if (alreadyClassified) {
             facesMessages.add(
                     WARN,
                     messages.get("feedback.classification.requestDoneButSomeWereAlreadyClassified"));
         } else {
             facesMessages.add(INFO,
                     messages.get("feedback.classification.requestDone"));
         }
 
         resetCurrentDocumentClassifications();
         return false;
     }
 
     public String cancelClassification() throws ClientException {
         DocumentModel currentDoc = navigationContext.getCurrentDocument();
         return navigationContext.navigateToDocument(currentDoc);
     }
 
     public String getCurrentClassificationRootId() throws ClientException {
         DocumentModel root = getCurrentClassificationRoot();
         if (root != null) {
             return root.getId();
         }
         return null;
     }
 
     /**
      * Sets current classification root id, and set it as current document.
      */
     public void setCurrentClassificationRootId(String newRootId)
             throws ClientException {
         if (newRootId != null) {
             DocumentModelList roots = getClassificationRoots();
             for (DocumentModel root : roots) {
                 if (newRootId.equals(root.getId())) {
                     currentClassificationRoot = root;
                     break;
                 }
             }
             // force reset of current tree
             currentClassificationTree = null;
             eventContext.remove("currentClassificationTree");
         }
     }
 
     public String navigateToCurrentClassificationRoot() throws ClientException {
         return navigationContext.navigateToDocument(currentClassificationRoot);
     }
 
     public DocumentModel getCurrentClassificationRoot() throws ClientException {
         DocumentModelList roots = getClassificationRoots();
         // reset root if needed
         if (!roots.contains(currentClassificationRoot)) {
             currentClassificationRoot = null;
             currentClassificationTree = null;
         }
         if (currentClassificationRoot == null) {
             // take first available root
             if (!roots.isEmpty()) {
                 currentClassificationRoot = roots.get(0);
             }
         }
         return currentClassificationRoot;
     }
 
     @Factory(value = "currentEditableClassificationRootId", scope = EVENT)
     public String getCurrentEditableClassificationRootId()
             throws ClientException {
         DocumentModel root = getCurrentEditableClassificationRoot();
         if (root != null) {
             return root.getId();
         }
         return null;
     }
 
     public void setCurrentEditableClassificationRootId(String newRootId)
             throws ClientException {
         if (newRootId != null) {
             DocumentModelList roots = getEditableClassificationRoots();
             for (DocumentModel root : roots) {
                 if (newRootId.equals(root.getId())) {
                     currentEditableClassificationRoot = root;
                     break;
                 }
             }
             // force reset of current tree
             currentEditableClassificationTree = null;
             eventContext.remove("currentEditableClassificationTree");
         }
     }
 
     public DocumentModel getCurrentEditableClassificationRoot()
             throws ClientException {
         if (currentEditableClassificationRoot == null) {
             // initialize roots and take first
             DocumentModelList roots = getEditableClassificationRoots();
             if (!roots.isEmpty()) {
                 currentEditableClassificationRoot = roots.get(0);
             }
         }
         return currentEditableClassificationRoot;
     }
 
     @Factory(value = "currentClassificationTree", scope = EVENT)
     public DocumentTreeNode getCurrentClassificationTree()
             throws ClientException {
         if (currentClassificationTree == null) {
             // initialize current root
             DocumentModel root = getCurrentClassificationRoot();
             if (root != null) {
                 Filter filter = null;
                 Sorter sorter = null;
                 try {
                     TreeManager treeManager = Framework.getService(TreeManager.class);
                     filter = treeManager.getFilter(TREE_PLUGIN_NAME);
                     sorter = treeManager.getSorter(TREE_PLUGIN_NAME);
                 } catch (Exception e) {
                     log.error(
                             "Could not fetch filter, sorter or node type for tree ",
                             e);
                 }
                 // standard tree node: no need to show classified documents
                 currentClassificationTree = new DocumentTreeNodeImpl(
                         documentManager.getSessionId(), root, filter, sorter);
             }
         }
         return currentClassificationTree;
     }
 
     @Factory(value = "currentEditableClassificationTree", scope = EVENT)
     public DocumentTreeNode getCurrentEditableClassificationTree()
             throws ClientException {
         if (currentEditableClassificationTree == null) {
             // initialize current root
             DocumentModel root = getCurrentEditableClassificationRoot();
             if (root != null) {
                 Filter filter = null;
                 Sorter sorter = null;
                 try {
                     TreeManager treeManager = Framework.getService(TreeManager.class);
                     filter = treeManager.getFilter(TREE_PLUGIN_NAME);
                     sorter = treeManager.getSorter(TREE_PLUGIN_NAME);
                 } catch (Exception e) {
                     log.error(
                             "Could not fetch filter, sorter or node type for tree ",
                             e);
                 }
                 currentEditableClassificationTree = new ClassificationTreeNode(
                         root, filter, sorter);
             }
         }
         return currentEditableClassificationTree;
     }
 
     @Factory(value = "classificationRoots", scope = EVENT)
     public DocumentModelList getClassificationRoots() throws ClientException {
         if (classificationRoots == null) {
             classificationRoots = new DocumentModelListImpl();
             try {
                 PageProvider<DocumentModel> provider = getPageProvider(CLASSIFICATION_ROOTS_PROVIDER_NAME);
                 List<DocumentModel> resultDocuments = provider.getCurrentPage();
                 for (DocumentModel doc : resultDocuments) {
                     // XXX refetch it to be a real document model instead of a
                     // ResultDocumentModel that does not handle lists correctly
                     // (dc:contributors is Object[] instead of String[]) + get
                     // a session id that's needed to retrieve a tree node
                     // children
                     classificationRoots.add(documentManager.getDocument(doc.getRef()));
                 }
             } catch (ClientException e) {
                 log.error(e);
             }
         }
         return classificationRoots;
     }
 
     protected PageProvider<DocumentModel> getPageProvider(
             String pageProviderName) throws ClientException {
         PageProviderService pps = Framework.getLocalService(PageProviderService.class);
         Map<String, Serializable> props = new HashMap<String, Serializable>();
         props.put(CORE_SESSION_PROPERTY, (Serializable) documentManager);
         return (PageProvider<DocumentModel>) pps.getPageProvider(
                 pageProviderName, null, null, null, props, null);
     }
 
     @Factory(value = "editableClassificationRoots", scope = EVENT)
     public DocumentModelList getEditableClassificationRoots()
             throws ClientException {
         if (editableClassificationRoots == null) {
             editableClassificationRoots = new DocumentModelListImpl();
             for (DocumentModel classificationRoot : getClassificationRoots()) {
                 DocumentRef rootRef = classificationRoot.getRef();
                 if (documentManager.hasPermission(rootRef,
                         ClassificationConstants.CLASSIFY)) {
                     // XXX refetch it to be a real document model instead of a
                     // ResultDocumentModel that does not handle lists correctly
                     // (dc:contributors is Object[] instead of String[]) + get
                     // a session id that's needed to retrieve a tree node
                     // children
                     editableClassificationRoots.add(documentManager.getDocument(rootRef));
                 }
             }
         }
         return editableClassificationRoots;
     }
 
     public void editableClassificationRootSelected(ValueChangeEvent event)
             throws ClientException {
         Object newValue = event.getNewValue();
         if (newValue instanceof String) {
             String newRootId = (String) newValue;
             setCurrentEditableClassificationRootId(newRootId);
         }
     }
 
     @Observer(value = { EventNames.GO_HOME,
             EventNames.DOMAIN_SELECTION_CHANGED, EventNames.DOCUMENT_CHANGED,
             EventNames.DOCUMENT_SECURITY_CHANGED,
             EventNames.DOCUMENT_CHILDREN_CHANGED }, create = false)
     public void resetClassificationData() {
         classificationRoots = null;
         // do not reset current classification root to not lose current
         // selection. it will be reset later if it's not available anymore.
         currentClassificationTree = null;
         editableClassificationRoots = null;
         currentEditableClassificationRoot = null;
         currentEditableClassificationTree = null;
         resetCurrentDocumentClassifications();
     }
 
     @Observer(value = { EventNames.GO_HOME,
             EventNames.DOMAIN_SELECTION_CHANGED,
             EventNames.DOCUMENT_SELECTION_CHANGED }, create = false)
     public void resetCurrentDocumentClassifications() {
         currentDocumentClassifications = null;
         documentsListsManager.resetWorkingList(CURRENT_DOCUMENT_CLASSIFICATIONS_SELECTION);
         documentsListsManager.resetWorkingList(CURRENT_SELECTION_FOR_UNCLASSIFICATION);
         contentViewActions.refresh(CLASSIFICATION_DOCUMENTS_CONTENT_VIEW);
         contentViewActions.refresh(CURRENT_SELECTION_FOR_UNCLASSIFICATION);
         contentViewActions.refresh(CLASSIFIED_INTO);
     }
 
     @Factory(value = "currentDocumentClassifications", scope = EVENT)
     public DocumentModelList getCurrentDocumentClassifications()
             throws ClientException {
         if (currentDocumentClassifications == null) {
             DocumentModel currentDocument = navigationContext.getCurrentDocument();
             Classification adapter = currentDocument.getAdapter(Classification.class);
 
             currentDocumentClassifications = adapter.getClassifiedDocuments();
         }
         return currentDocumentClassifications;
     }
 
     /**
      * Returns classification form for selected documents
      * 
      * @param currentViewId the current view id, so that redirection can be done
      *            correctly on cancel.
      */
     public String showCurrentSelectionClassificationForm(String currentViewId)
             throws ClientException {
         currentSelectionViewId = currentViewId;
 
         ContentView contentView = contentViewActions.getContentView("MASS_CLASSIFICATION_REQUEST");
         contentView.resetPageProvider();
         documentsListsManager.resetWorkingList("CURRENT_SELECTION_FOR_CLASSIFICATION");
 
         DocumentModelListPageProvider pageProvider = (DocumentModelListPageProvider) contentView.getPageProvider();
         pageProvider.setDocumentModelList(documentsListsManager.getWorkingList("CURRENT_SELECTION"));
 
         documentsListsManager.getWorkingList(
                 "CURRENT_SELECTION_FOR_CLASSIFICATION").addAll(
                 pageProvider.getCurrentPage());
 
         return CURRENT_SELECTION_FOR_CLASSIFICATION_PAGE;
     }
 
     public String cancelCurrentSelectionClassificationForm()
             throws ClientException {
         // XXX AT: this is a hack to redirect to correct page
         if ("/search/search_results_simple.xhtml".equals(currentSelectionViewId)) {
             return "search_results_simple";
         } else if ("/search/search_results_advanced.xhtml".equals(currentSelectionViewId)) {
             return "search_results_advanced";
         } else if ("/search/dynsearch_results.xhtml".equals(currentSelectionViewId)) {
             return "dynsearch_results";
         }
         // navigate to current document default view
         DocumentModel currentDoc = navigationContext.getCurrentDocument();
         if (currentDoc != null) {
             return navigationContext.navigateToDocument(currentDoc);
         }
 
         // default: do not move
         return null;
     }
 
     public boolean getCanUnclassifyFromCurrentSelection() {
         return !documentsListsManager.isWorkingListEmpty(CURRENT_DOCUMENT_CLASSIFICATIONS_SELECTION);
     }
 
     public boolean getCanUnclassifyFromCurrentDocument() {
         return !documentsListsManager.isWorkingListEmpty(CURRENT_SELECTION_FOR_UNCLASSIFICATION);
     }
 
     public void unclassify() throws ClientException {
         if (!documentsListsManager.isWorkingListEmpty(CURRENT_DOCUMENT_CLASSIFICATIONS_SELECTION)) {
             List<DocumentModel> toDel = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_CLASSIFICATIONS_SELECTION);
             List<String> targetDocIds = new ArrayList<String>();
             for (DocumentModel doc : toDel) {
                 targetDocIds.add(doc.getId());
             }
             DocumentModel currentDocument = navigationContext.getCurrentDocument();
             unclassify(targetDocIds, currentDocument);
             resetCurrentDocumentClassifications();
         } else {
             log.warn("No documents selection in context to process unclassify on...");
         }
     }
 
     public void unclassifyCurrentDocument() throws ClientException {
         if (!documentsListsManager.isWorkingListEmpty(CURRENT_SELECTION_FOR_UNCLASSIFICATION)) {
             List<DocumentModel> toDel = documentsListsManager.getWorkingList(CURRENT_SELECTION_FOR_UNCLASSIFICATION);
             ClassificationResult<ClassificationService.UNCLASSIFY_STATE> classificationResult = Framework.getLocalService(
                     ClassificationService.class).unClassifyFrom(toDel,
                     navigationContext.getCurrentDocument().getId());
 
             Events.instance().raiseEvent(AuditEventTypes.HISTORY_CHANGED);
             resetCurrentDocumentClassifications();
 
             if (classificationResult.contains(NOT_CLASSIFIED)) {
                 facesMessages.add(
                         WARN,
                         messages.get("feedback.unclassification.noDocumentsToUnclassify"));
                 return;
             }
 
             if (classificationResult.contains(NOT_ENOUGH_RIGHTS)) {
                 facesMessages.add(WARN,
                         messages.get("feedback.unclassification.unauthorized"));
                 return;
             }
 
             facesMessages.add(INFO,
                     messages.get("feedback.unclassification.requestDone"));

         } else {
             log.warn("No documents selection in context to process unclassify on current document.");
         }
     }
 
     /**
      * Unclassifies given document ids in given classification folder.
      * 
      * @return true on error
      */
     public boolean unclassify(Collection<String> targetDocIds,
             DocumentModel classificationFolder) throws ClientException {
         if (targetDocIds.isEmpty()) {
             facesMessages.add(
                     ERROR,
                     messages.get("feedback.unclassification.noDocumentsToUnclassify"));
             return true;
         }
         if (classificationFolder == null) {
             facesMessages.add(
                     ERROR,
                     messages.get("feedback.classification.noClassificationFolder"));
             return true;
         }
         if (!classificationFolder.hasSchema(ClassificationConstants.CLASSIFICATION_SCHEMA_NAME)) {
             facesMessages.add(
                     ERROR,
                     messages.get("feedback.classification.invalidClassificationFolder"));
             return true;
         }
         DocumentRef classificationRef = classificationFolder.getRef();
         if (!documentManager.hasPermission(classificationRef,
                 ClassificationConstants.CLASSIFY)) {
             facesMessages.add(ERROR,
                     messages.get("feedback.unclassification.unauthorized"));
             return true;
         }
 
         ClassificationService classificationService = Framework.getLocalService(ClassificationService.class);
         ClassificationResult<ClassificationService.UNCLASSIFY_STATE> listMap = classificationService.unClassify(
                 classificationFolder, targetDocIds);
 
         Events.instance().raiseEvent(AuditEventTypes.HISTORY_CHANGED);
 
         if (listMap.contains(NOT_CLASSIFIED)) {
             facesMessages.add(
                     WARN,
                     messages.get("feedback.unclassification.requestDoneButSomeWereNotClassified"));
         } else {
             facesMessages.add(INFO,
                     messages.get("feedback.unclassification.requestDone"));
         }
         return false;
     }
 
 }
