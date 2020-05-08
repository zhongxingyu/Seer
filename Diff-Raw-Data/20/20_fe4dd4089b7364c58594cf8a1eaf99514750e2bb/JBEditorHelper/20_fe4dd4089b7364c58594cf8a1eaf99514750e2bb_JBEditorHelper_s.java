 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.editors;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMDirtyVersionException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.controllers.IEditorOperation;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.events.GuiEventDispatcher;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.views.JBPropertiesView;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 
 
 /**
  * Encapsulates the functionality expected of all editors in Jubula. This
  * includes locking/unlocking persistent objects and reacting to changes in 
  * Jubula's data model.
  * This class can be instantiated and used by any Jubula editor.
  *
  * @author BREDEX GmbH
  * @created Oct 22, 2008
  */
 public class JBEditorHelper implements ILockedObjects, 
         IProjectLoadedListener, IDataChangedListener {
 
     /** Specifies the result of an edit request */
     public enum EditableState { NotChecked, OK, Locked, Stale, Invalid }
     
     /** the editable state */
     private EditableState m_editableState = EditableState.NotChecked;
     
     /** checks if there are changes in the editor */
     private boolean m_isDirty = false;
     
     /** If this editor is active or not */
     private boolean m_isActive = false;
     
     /** Listens to the activations of this editor */
     private ActiveListener m_activeListener = null;
     
     /** 
      * This part's reference to the clipboard.
      * Note that the part shares this clipboard with the entire operating 
      * system, and this instance is only for easier access to the clipboard. 
      * The clipboard does not exclusively belong to the part.
      */
     private Clipboard m_clipboard;
 
     /** the editor that is assisted by this helper */
     private IJBEditor m_editor;
     
     /**
      * Constructor
      * 
      * @param editor The editor that will be assisted by this helper. May not
      *               be <code>null</code>.
      */
     public JBEditorHelper(IJBEditor editor) {
         Validate.notNull(editor);
         m_editor = editor;
     }
     
     /**
      * @return the editable state
      */
     public EditableState requestEditableState() {
         if (getEditSupport() == null) {
             return EditableState.Invalid;
         }
         if (m_editableState == EditableState.NotChecked
             || m_editableState == EditableState.Locked) {
             try {
                 getEditSupport().lockWorkVersion();
                 m_editableState = EditableState.OK;
             } catch (PMAlreadyLockedException e) {
                 m_editableState = EditableState.Locked;
                 PMExceptionHandler.handlePMExceptionForEditor(e, m_editor);
             } catch (PMDirtyVersionException e) {
                 m_editableState = EditableState.NotChecked;
                 PMExceptionHandler.handlePMExceptionForEditor(e, m_editor);
             } catch (PMException e) {
                 m_editableState = EditableState.Invalid;
                 PMExceptionHandler.handlePMExceptionForEditor(e, m_editor);
             } 
         }
         return m_editableState;
     }
 
 
     /**
      * resets the editable state to not checked.
      */
     public void resetEditableState() {
         m_editableState = EditableState.NotChecked;
     }
 
     /**
      * @return the edit support
      */
     public EditSupport getEditSupport() {
         return (m_editor.getEditorInput() instanceof PersistableEditorInput)
             ? ((PersistableEditorInput)m_editor.getEditorInput()).
                 getEditSupport() : null;
     }
 
     /**
      * {@inheritDoc}
      * 
      * Should always be called from the assisted editor's 
      * <code>dispose()</code> method.
      */
     public void dispose() {
         if (m_clipboard != null) {
             m_clipboard.dispose();
         }
         if (getEditSupport() != null) {
             getEditSupport().close();
         }
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.removeDataChangedListener(m_editor);
         ded.removeProjectLoadedListener(this);
         // clear corresponding views
         ded.firePartClosed(m_editor);
         
         if (m_editor.getEditorInput() instanceof PersistableEditorInput) {
             ((PersistableEditorInput)m_editor.getEditorInput()).dispose();
         }
         
         if (m_activeListener != null) {
             m_editor.getSite().getPage().getWorkbenchWindow()
                 .getSelectionService()
                 .removeSelectionListener(m_activeListener);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * Should always be called as a fallback from the assisted editor's 
      * <code>getAdapter</code> method.
      */
     public Object getAdapter(Class adapter) {
         if (adapter == ILockedObjects.class 
             || adapter == IJBEditor.class) {
             return this;
         } else if (adapter.equals(IPropertySheetPage.class)) {
             return new JBPropertiesView(true, getEditSupport().getCompMapper());
         } else if (adapter.equals(IComponentNameMapper.class)) {
             return getEditSupport().getCompMapper();
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      * @return a list with objects, which are currently locked by this editor
      */
     public List<IPersistentObject> getLockedObjects() {
         List<IPersistentObject> result = 
             getEditSupport().getLockedObjects();        
         return result;
     }
 
     
     /**
      * validates, if the given object is locked by a dirty editor
      * @param po object to validate
      * @return the editor hold the lock for given object or null, if there is 
      * no editor, which holds a lock for the given object
      */
     public static IEditorPart findEditor2LockedObj(IPersistentObject po) {
         IEditorPart[] dirtyEditors = Plugin.getDefault().getDirtyEditors();
         
         for (IEditorPart editor : dirtyEditors) {
             ILockedObjects lockedPOs = (ILockedObjects)editor.
                 getAdapter(ILockedObjects.class);
             if (lockedPOs != null) {
                 List<IPersistentObject> l = lockedPOs.getLockedObjects();
                 for (IPersistentObject lpo : l) {
                     if (lpo.getId().equals(po.getId())) {
                         return editor;
                     }
                 }
             }
         }
         return null;
     }
     
     /**
      * Should not be overwritten, but setTitleToolTip() doesn't work.
      * {@inheritDoc}
      */
     public String getTitleToolTip() {
         return m_editor.getPartName();
     }
 
     /**
      * {@inheritDoc}
      * 
      * Should always be called from the assisted editor's <code>init</code> 
      * method. Calls assisted editor's <code>initTextAndInput</code> method.
      */
     public void init(IEditorSite site, IEditorInput input) 
         throws PartInitException {
         
         m_clipboard = new Clipboard(site.getShell().getDisplay());
         PersistableEditorInput editorInput = (PersistableEditorInput)input
             .getAdapter(PersistableEditorInput.class);
         if (editorInput != null) {
             resetEditableState();
             m_editor.initTextAndInput(site, input);
         } else {
             String msg = org.eclipse.jubula.client.ui.i18n.Messages
                     .EditorInitCreateError;
             throw new PartInitException(msg);
         }
     } 
     
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         for (DataChangedEvent e : events) {
             handleDataChanged(e.getPo(), e.getDataState());
         }
     }
     
     /**
      * {@inheritDoc}
      * 
      * This should always be called by the assisted editor during its 
      * <code>handleDataChanged</code> method.
      */
     public void handleDataChanged(final IPersistentObject po, 
         final DataState dataState) {
         IPersistentObject workVersion = getEditSupport().getWorkVersion();
         switch (dataState) {
             case Added:
                 
                 break;
             case Deleted:
                 if (po.equals(workVersion)) {
                     m_editor.getSite().getPage().closeEditor(m_editor, false);
                 }
                 break;
             case Renamed:
                 if (!isDirty() && editorContainsPo(po)) {
                     handleProjectLoaded();
                 }
                 break;
             case StructureModified:
                 if (po.equals(workVersion)
                     && !po.getVersion().equals(workVersion.getVersion())) {
                     try {
                         m_editor.reOpenEditor(po);
                     } catch (PMException e) {
                         ErrorHandlingUtil.createMessageDialog(
                             MessageIDs.E_REFRESH_FAILED,
                             null, new String[] { 
                                 Messages.ErrorMessageEDITOR_CLOSE });
                         m_editor.getSite().getPage().closeEditor(
                                 m_editor, false);
                     }                     
                 }
                 break;
             default:
         }        
     }
     
     /**
      * {@inheritDoc}
      */
     public final void handleProjectLoaded() {
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 if (!isDirty()) {
                     final IPersistentObject original = 
                         getEditSupport().getOriginal();
                     if (original != null) {
                         try {
                             Plugin.startLongRunning();
                             m_editor.reOpenEditor(original);
                         } catch (PMException e) {
                             ErrorHandlingUtil.createMessageDialog(
                                 MessageIDs.E_REFRESH_FAILED,
                                 null, new String[] { 
                                     Messages.ErrorMessageEDITOR_CLOSE });
                             m_editor.getSite().getPage().closeEditor(
                                     m_editor, false);
                         } finally {
                             Plugin.stopLongRunning();
                         }
                     } else {
                         m_editor.getSite().getPage().closeEditor(
                                 m_editor, false);
                     }
                 }
             }
         });
     }
     
     /**
      * add listener
      * 
      * Should always be called during creation of assisted editor.
      */
     protected void addListeners() {
         m_activeListener  = new ActiveListener();
         m_editor.getSite().getPage().getWorkbenchWindow().getSelectionService()
             .addSelectionListener(m_activeListener);
         final DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addDataChangedListener(m_editor, true);
         ded.addProjectLoadedListener(this, true);
     }
     
     /**
      * @param po object to compare
      * @return if the given po object is contained in editor tree
      */
     protected boolean editorContainsPo(IPersistentObject po) {
         if (!(po instanceof INodePO)
                 || !(getEditSupport().getWorkVersion() instanceof INodePO)) {
             return false;
         }
         INodePO compObj = (INodePO)getEditSupport().getWorkVersion();
         List<INodePO> nodeList = new ArrayList<INodePO>();
         nodeList.add((INodePO)po);
         List<INodePO> editorNodes = collectNodes(nodeList, 
             compObj.getNodeListIterator());
         for (Object object : editorNodes) {
             if (((INodePO)object).equals(po)) {
                 return true;
             }
         }
         // validate location of reuses for modified po object too
         if (po instanceof ISpecTestCasePO) {
             ISpecTestCasePO specTc = (ISpecTestCasePO)po;
             List<IExecTestCasePO> execTCs = 
                 NodePM.getInternalExecTestCases(specTc.getGuid(), 
                     specTc.getParentProjectId());
             for (IExecTestCasePO execTc : execTCs) {
                 for (Object object : editorNodes) {
                     if (((INodePO)object).equals(execTc)) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
     
     /**
      * @param nodeList container for nodes in editor
      * @param it iterator for current node
      * @return list with all nodes contained in editor
      */
     private List<INodePO> collectNodes(List<INodePO> nodeList,
             Iterator<INodePO> it) {
         while (it.hasNext()) {
             INodePO node = it.next();
             nodeList.add(node);
             if (node.getNodeListSize() > 0) {
                 return collectNodes(nodeList, node.getNodeListIterator());
             }
         }
         return nodeList;
     }   
     
     /**
      * @return true if input is changed
      */
     public boolean isDirty() {
         return m_isDirty;
     }
     
     /**
      * Calls assisted editor's <code>fireDirtyProperty</code> method.
      * 
      * @param isDirty The isDirty to set.
      */
     public void setDirty(boolean isDirty) {
         m_isDirty = isDirty;
         m_editor.fireDirtyProperty(isDirty);
         GuiEventDispatcher.getInstance()
             .fireEditorDirtyStateListener(m_editor, isDirty);
     }
 
     /**
      * @return Returns the editableState.
      */
     public EditableState getEditableState() {
         return m_editableState;
     }
     
     /**
      *
      * @author BREDEX GmbH
      * @created Feb 20, 2008
      */
     private class ActiveListener implements ISelectionListener {
         /**
          * {@inheritDoc}
          */
         public void selectionChanged(IWorkbenchPart part, 
                 ISelection selection) {
             if (part instanceof EditorPart) {
                 m_isActive = (part == m_editor);
             }
         }
     }
 
     /**
      * @return <code>true</code> if the supported editor is currently active.
      *         This is the open editor most recently brought to top. Otherwise,
      *         <code>false</code>.
      */
     public boolean isActive() {
         return m_isActive;
     }
 
     /**
      * 
      * @return a reference to the clipboard.
      */
     public Clipboard getClipboard() {
         return m_clipboard;
     }
 
     /**
      * Performs the given <code>operation</code> within the context of the
      * receiver's editor. The operation is "wrapped" between a request for an
      * editable state and a reset of editable state (if the editor is not marked
      * as dirty by the end of the operation. The operation is executed in the
      * same thread in which this method is called. 
      * 
      * @param operation The operation to perform.
      */
     public void doEditorOperation(IEditorOperation operation) {
         if (requestEditableState() == EditableState.OK) {
             try {
                 operation.run(getEditSupport().getWorkVersion());
             } finally {
                if (!m_editor.isDirty()) {
                     try {
                        resetEditableState();
                        getEditSupport().reloadEditSession();
                     } catch (PMException e) {
                         PMExceptionHandler.handlePMExceptionForEditor(
                                e, m_editor);
                     }
                 }
             }
            
         }
     }
 }
