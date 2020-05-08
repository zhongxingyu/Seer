 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.richbeans.editors;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.ObjectUndoContext;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.operations.LinearUndoViolationUserApprover;
 import org.eclipse.ui.operations.NonLocalUndoUserApprover;
 import org.eclipse.ui.operations.RedoActionHandler;
 import org.eclipse.ui.operations.UndoActionHandler;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 
 import uk.ac.gda.common.rcp.util.EclipseUtils;
 import uk.ac.gda.common.rcp.util.IStorageUtils;
 import uk.ac.gda.richbeans.beans.BeanUI;
 import uk.ac.gda.richbeans.editors.xml.XMLBeanEditor;
 import uk.ac.gda.util.beans.xml.XMLHelpers;
 
 /**
  * This multipage editor is designed to be extended and the resulting class declared
  * as an extension point.
  * 
  * If you extend this class, you must implement the createPart0() method and this must return
  * a RichBeanEditor implementation. This class extending RichBeanEditor can be created entirely
  * automatically using RCP developer and exposing the relevant, correctly named, fields within
  * RCP developer.
  * 
  */
 public abstract class RichBeanMultiPageEditorPart extends MultiPageEditorPart implements DirtyContainer, IReusableEditor {
 	
 	private boolean undoRegistered = false;
 	
 	/**
 	 * Creates editor and sets a property that can be used to check
 	 * if the class is a RichBeanEditorPart. Useful for checking subclasses
 	 * registered as editors from other plugins.
 	 */
 	public RichBeanMultiPageEditorPart() {
 		setPartProperty("RichBeanEditorPart", "true");
 	}
 	
 	// We must ensure that bundle urls are transformed to 
 	// absolute or the sax parser does not work.
 	static {
 		XMLHelpers.setUrlResolver(EclipseUtils.getUrlResolver());
 	}
 	
 	private static final Logger logger = LoggerFactory.getLogger(RichBeanMultiPageEditorPart.class);
 	
 	/**
 	 * The bean we are currently editing
 	 */
 	protected Object                editingBean;
 	
 	/**
 	 * The UI editor being used normally to define the data
 	 */
 	protected RichBeanEditorPart    richBeanEditor;
 	
 	/**
 	 * The XML editor on the second tab which can be used to edit 
 	 * the xml directly
 	 */
 	protected XMLBeanEditor         xmlEditor;
 	
 	/**
 	 * The path to the editing bean, may be null
 	 */
 	protected String                path;
 	
 	/**
 	 * An action used to undo
 	 */
 	protected UndoActionHandler     undoAction;
 	
 	/**
 	 * An action used to redo
 	 */
 	protected RedoActionHandler     redoAction;
 	
 	/**
 	 * The context used in the undo stack
 	 */
 	protected IUndoContext          context;
 
 	/**
 	 * @return Class
 	 */
 	public abstract Class<?>        getBeanClass();
 	
 	/**
 	 * @return URL
 	 */
 	public abstract URL             getMappingUrl();
 	
 	/**
 	 * @return URL
 	 */
 	public abstract URL             getSchemaUrl();
 	
 	/**
 	 * Please implement this method to return your RichBeanEditorPart implementation.
 	 * @param path
 	 * @param editingBean
 	 * @return RichBeanEditorPart
 	 */
 	protected abstract RichBeanEditorPart   getRichBeanEditorPart(String path, 
 			                                                      Object editingBean);
 	
 	
 	@Override
 	public void setInput(final IEditorInput input) {
 		try{
 	        assignInput(input);
 	        createBean();
 	        linkUI();
 	        
 	        
 	        // Close all other editors editing this bean.
 	        // Currently only one editor for a given bean class may be open 
 	        // at a time.
 	        final IEditorReference[] refs = getSite().getPage().getEditorReferences();
 	        for (int i = 0; i < refs.length; i++) {
 				if (refs[i].getId().equals(this.getSite().getId())) {
 					final IEditorPart part = refs[i].getEditor(false);
 					if (part!=this) getSite().getPage().closeEditor(part, true);
 				}
 			}
 		} catch (Throwable th){
 			logger.error("Error setting input for editor from input " + input.getName(), th);
 		}
 	}
 
 	protected void createBean() {
 		try {
 			// Do not validate this bean on the read, user may not have added all parameters.
 			if (this.getEditorInput() instanceof IStorageEditorInput) {
 				
 				IStorage storage = ((IStorageEditorInput)getEditorInput()).getStorage();
 				InputSource source = new InputSource(IStorageUtils.getContents(storage));
 				this.editingBean = XMLHelpers.createFromXML(getMappingUrl(), 
 															getBeanClass(), 
 															getSchemaUrl(), 
 															source, 
 															false);
 				
 			} else {
 				this.editingBean = XMLHelpers.createFromXML(getMappingUrl(), 
 														getBeanClass(), 
 														getSchemaUrl(), 
 														path, 
 														false);
 			}
 
 		} catch (Throwable e) {
 			// Class not found can come through here
 			// when the classes required for castor to load 
 			// the bean from file are not present.
 			throw new RuntimeException(e.getMessage(), e);
 		}
 	}
 		
 	protected void createUndoRedoActions() {
         
 		this.context       = new ObjectUndoContext(this);
         this.undoAction    = new UndoActionHandler(getSite(), context);
         this.redoAction    = new RedoActionHandler(getSite(), context);
  
 		IOperationHistory history= OperationHistoryFactory.getOperationHistory();
 		history.addOperationApprover(new NonLocalUndoUserApprover(context, this, new Object [] { getEditorInput() }, Object.class));
 		history.addOperationApprover(new LinearUndoViolationUserApprover(context, this));
 	}
 	
 	protected void assignInput(IEditorInput input) {
         super.setInput(input);
 		
        	this.path = EclipseUtils.getFilePath(input);
     	setPartName(input.getName());
 		   
         if (richBeanEditor!=null) {
         	try {
         		pageChangeProcessing = false;
             	richBeanEditor.setInput(input);
             	richBeanEditor.setPath(path);
         	} finally {
         		pageChangeProcessing = true;
         	}
         }
         if (xmlEditor!=null) {
         	xmlEditor.setInput(input);
         }
  	}
 	
 	protected void linkUI() {
         if (richBeanEditor!=null) {
         	try {
         		pageChangeProcessing = false;
             	super.setActivePage(0);
             	richBeanEditor.setEditingBean(editingBean);
             	richBeanEditor.linkUI(false);
         	} finally {
         		pageChangeProcessing = true;
         	}
         }
         if (xmlEditor!=null) {
          	xmlEditor.setEditingBean(editingBean);
         }
 	}
 
 	@Override
 	protected void createPages() {
 		createUndoRedoActions();
 		try{
 			richBeanEditor = createPage0();
 			xmlEditor      = createPage1();
 		} catch (Throwable th){
 			logger.error("Error creating pages for editor ",th);
 		}
 	}
 	
 	/**
 	 * Creates page 1 of the multi-page editor,
 	 * which allows you to change the font used in page 2.
 	 */
 	protected RichBeanEditorPart createPage0() {
 		try {
 			RichBeanEditorPart richBeanEditor = getRichBeanEditorPart(path, editingBean);
 			richBeanEditor.setUndoableContext(context);
 			int index = addPage(richBeanEditor, getEditorInput());
 			richBeanEditor.linkUI(false);
 			setPageText(index, richBeanEditor.getRichEditorTabText());
 			return richBeanEditor;
 		} catch (PartInitException e) {
 			ErrorDialog.openError(
 				getSite().getShell(),
 				"Error creating editor for "+getClass().getName(),
 				null,
 				e.getStatus());
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Creates page 1 of the multi-page editor,
 	 * which contains a text editor.
 	 */
 	protected XMLBeanEditor createPage1() {
 		try {
 			XMLBeanEditor xmlEditor = new XMLBeanEditor(this,
 													  getMappingUrl(),
 													  getSchemaUrl(),
 												      editingBean);
 			int index = addPage(xmlEditor, getEditorInput());
 			setPageText(index, "XML");
 			return xmlEditor;
 			
 		} catch (PartInitException e) {
 			logger.error(e.getMessage(), e);
 			ErrorDialog.openError(
 				getSite().getShell(),
 				"Error creating text editor for "+getClass().getName(),
 				null,
 				e.getStatus());
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 		}
 		return null;
 	}
 
 	private boolean allowDirtyUpdates = true;
 	private boolean isDirty     = false;
 	@Override
 	public boolean isDirty() {
 		if (path == null) return false;
 		return isDirty;
 	}
 	@Override
 	public void setDirty(boolean isDirty) {
 		if (!allowDirtyUpdates) return;
 		this.isDirty = isDirty;
 		firePropertyChange(IEditorPart.PROP_DIRTY);
 	}
 
 	private boolean pageChangeProcessing = false;
 	
 	@Override
 	protected void initializePageSwitching() {
 		super.initializePageSwitching();
 		pageChangeProcessing = true;
 	}
 	
 	@Override
 	protected void pageChange(int newPageIndex)  {
 		
 		if (!pageChangeProcessing) {
 			if (!undoRegistered) {
 				getSite().getShell().getDisplay().asyncExec(new Runnable() {
 					// You have to do this on an asyncExec.
 					@Override
 					public void run() { setUIUndoRedo(); }
 				});
 			}
 			super.pageChange(newPageIndex);
 			return;
 		}
 		
 		// If first page going to then regenerate bean.
 		if (newPageIndex == 1) {
 			try {
 				setXMLUndoRedo();
 				BeanUI.uiToBean(richBeanEditor, editingBean);
 				xmlEditor.beanToXML(getPrivateXMLFields());
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (newPageIndex == 0) {
 			try {
 				setUIUndoRedo();
 				xmlEditor.xmlToBean();
 			} catch (Exception e) { // XML Cannot be parsed.
 				try {
 					logger.debug("XML Validation stack trace.", e);
 					final boolean isOk = MessageDialog.openQuestion(getSite().getShell(),
 							              "XML Validation Error",
 							              "The XML is not valid.\n\nDo you want to continue to change screen and lose your edits?\n\n"+
 							              "Error message:\n"+XMLBeanEditor.getSantitizedExceptionMessage(e.getMessage()));
 					if (!isOk) {
 						try {
 							pageChangeProcessing = false;
 							super.setActivePage(1);
 						} finally {
 							pageChangeProcessing = true;
 						}
 					}
 				} catch (Throwable ne) {
 					ne.printStackTrace();
 				}
 			}
 			try {
 				allowDirtyUpdates = false;
 				richBeanEditor.linkUI(true);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				allowDirtyUpdates = true;
 			}
 			
 		}
 		super.pageChange(newPageIndex);
 	}
 	
 	protected void setUIUndoRedo() {
 		final IActionBars actionBars = getEditorSite().getActionBars();
 		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
 		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
 		actionBars.updateActionBars();
 		OperationHistoryFactory.getOperationHistory().dispose(context, true, true, false);
 		undoRegistered = true;
 	}
 	
 	protected void setXMLUndoRedo() {
 		final IActionBars actionBars = getEditorSite().getActionBars();
 		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), xmlEditor.getAction(ActionFactory.UNDO.getId()));
 		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), xmlEditor.getAction(ActionFactory.REDO.getId()));
 		actionBars.updateActionBars();
 		OperationHistoryFactory.getOperationHistory().dispose(context, true, true, false);
 		undoRegistered = true;
 	}
 
 	/**
 	 * NOTE Can save both to this project, in which case add as IFile or
 	 * to any other location, in which case add as external resource.
 	 */
 	@Override
 	public void doSaveAs() {
 		
 		final IFile currentiFile = EclipseUtils.getIFile(getEditorInput());
 		final IFolder    folder  = (currentiFile!=null) ? (IFolder)currentiFile.getParent() : null;
 		
 	    final FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
 	    dialog.setText("Save as XML");
 	    dialog.setFilterExtensions(new String[]{"*.xml"});
 	    final File currentFile  = new File(this.path);
 	    dialog.setFilterPath(currentFile.getParentFile().getAbsolutePath());
 		
 	    String newFile = dialog.open();
 		if (newFile!=null) {
 			
 			if (!newFile.endsWith(".xml")) newFile = newFile+".xml";
 			newFile = validateFileName(newFile);
 			if (newFile==null) return;
 			
 			final File file = new File(newFile);
 			if (file.exists()) {
 				final boolean ovr = MessageDialog.openQuestion(getSite().getShell(),
 						                                      "Confirm File Overwrite",
 						                                      "The file '"+file.getName()+"' exists in '"+file.getParentFile().getName()+"'.\n\n"+
 						                                      "Would you like to overwrite it?");
 				if (!ovr) return;
 			}
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				MessageDialog.openError(getSite().getShell(), "Cannot save file.",
 						               "The file '"+file.getName()+"' cannot be created in '"+file.getParentFile().getName()+"'.\n\n"+
 						               e.getMessage());
 			    return;
 			}
 			try {
 		        if (!confirmFileNameChange(currentFile, file)) {
 		        	file.delete();
 		        	return;
 		        }
 			} catch (Exception ne) {
 				logger.error("Cannot confirm name change", ne);
 				return;
 			}
 	        
 	        IEditorInput input;
 	        if (folder!=null&&folder.getLocation().toFile().equals(file.getParentFile())) {
 	        	final IFile ifile = folder.getFile(file.getName());
 	        	try {
 					ifile.refreshLocal(IResource.DEPTH_ZERO, null);
 				} catch (CoreException e) {
 					logger.error("Cannot refresh "+ifile, e);
 				}
 	        	input = new FileEditorInput(ifile);
 	        } else {
 	        	input = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(file));
 	        }
 	        
 	        assignInput(input);
             doSave(new NullProgressMonitor());
             setDirty(false);
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		final int index = getActivePage();
 		if (index == 0) {
 			richBeanEditor.doSave(monitor);
 		} else if (index == 1) {
 			xmlEditor.doSave(monitor);
 		}
 	}
 	
 	/**
 	 * Please override if work should be done when save as changes the active file name.
 	 * @param oldName
 	 * @param newName
 	 */
 	@SuppressWarnings("unused")
 	protected boolean confirmFileNameChange(final File oldName, final File newName)  throws Exception {
 		return true;
 	}
 
 	/**
 	 * Optional file name validation. Returns null if name invalid.
 	 */
 	protected String validateFileName(final String newFile) {
 		return newFile;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @return the editor if it is already in existance.
 	 */
 	public RichBeanEditorPart getRichBeanEditor() {
 		return richBeanEditor;
 	}
 
 	/**
 	 * Override to provide a list of field names which
 	 * cannot be edited in the XML editor.
 	 * @return fields.
 	 */
 	public List<String> getPrivateXMLFields() {
 		return null;
 	}
 	
 	@Override
 	public void dispose() {
 		editingBean = null;
 		richBeanEditor.dispose();
 		richBeanEditor = null;
 		
 		super.dispose();
 	}
 }	
