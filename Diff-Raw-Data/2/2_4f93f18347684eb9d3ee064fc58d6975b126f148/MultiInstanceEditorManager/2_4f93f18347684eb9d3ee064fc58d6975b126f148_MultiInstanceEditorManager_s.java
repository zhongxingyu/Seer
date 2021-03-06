 /*******************************************************************************
  * See the NOTICE file distributed with this work for additional information
  * regarding copyright ownership.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package hr.fer.zemris.vhdllab.platform.manager.editor.impl;
 
 import hr.fer.zemris.vhdllab.entity.File;
 import hr.fer.zemris.vhdllab.entity.FileType;
 import hr.fer.zemris.vhdllab.entity.Project;
 import hr.fer.zemris.vhdllab.platform.gui.dialog.DialogManager;
 import hr.fer.zemris.vhdllab.platform.i18n.LocalizationSupport;
 import hr.fer.zemris.vhdllab.platform.manager.editor.Editor;
 import hr.fer.zemris.vhdllab.platform.manager.editor.EditorContainer;
 import hr.fer.zemris.vhdllab.platform.manager.editor.EditorIdentifier;
 import hr.fer.zemris.vhdllab.platform.manager.editor.EditorManager;
 import hr.fer.zemris.vhdllab.platform.manager.editor.NotOpenedException;
 import hr.fer.zemris.vhdllab.platform.manager.editor.PlatformContainer;
 import hr.fer.zemris.vhdllab.platform.manager.editor.SaveContext;
 import hr.fer.zemris.vhdllab.platform.manager.workspace.WorkspaceManager;
 import hr.fer.zemris.vhdllab.service.exception.CircuitInterfaceExtractionException;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.Validate;
 import org.apache.log4j.Logger;
 import org.hibernate.validator.InvalidStateException;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class MultiInstanceEditorManager extends LocalizationSupport implements
         EditorManager {
     /**
      * Logger for this class
      */
     private static final Logger LOG = Logger
             .getLogger(MultiInstanceEditorManager.class);
 
     private static final String EDITOR_OPENED_MESSAGE = "notification.editor.opened";
     private static final String EDITOR_CLOSED_MESSAGE = "notification.editor.closed";
 
     @Autowired
     private EditorRegistry registry;
     @Autowired
     private EditorContainer container;
     private final EditorIdentifier identifier;
     private Editor editor;
 
     @Autowired
     private PlatformContainer platformContainer;
 
     @Resource(name = "singleSaveDialogManager")
     private DialogManager dialogManager;
     @Resource(name = "invalidEntityBlockInSourceEditorDialogManager")
     private DialogManager invalidEntityBlockDialogManager;
     @Resource(name = "differentEntityNameDialogManager")
     private DialogManager differentEntityNameDialogManager;
     @Autowired
     private WorkspaceManager workspaceManager;
 
     public MultiInstanceEditorManager(EditorIdentifier identifier) {
         Validate.notNull(identifier, "Editor identifier can't be null");
         this.identifier = identifier;
     }
 
     @Override
     public void open() {
         if (!isOpened()) {
             editor = newInstance();
             editor.setContainer(platformContainer);
             editor.setMetadata(identifier.getMetadata());
             editor.getControl(); // forces UI creation
             configureComponent();
             container.add(editor);
             registry.add(this, editor, identifier);
             LOG.info(getMessage(EDITOR_OPENED_MESSAGE, new Object[] { editor
                     .getTitle() }));
         }
         select();
     }
 
     private Editor newInstance() {
         Class<? extends Editor> clazz = identifier.getMetadata()
                 .getEditorClass();
         try {
             return clazz.newInstance();
         } catch (Exception e) {
             throw new IllegalStateException(e);
         }
     }
 
     private void configureComponent() {
         editor.setEditable(identifier.getMetadata().isEditable());
         editor.setFile(identifier.getInstanceModifier());
     }
 
     @Override
     public boolean isOpened() {
         return editor != null;
     }
 
     @Override
     public void select() throws NotOpenedException {
         checkIfOpened();
         if (isSelected())
             return;
         container.setSelected(editor);
     }
 
     @Override
     public boolean isSelected() throws NotOpenedException {
         checkIfOpened();
         return container.isSelected(editor);
     }
 
     @Override
     public void undo() throws NotOpenedException {
         checkIfOpened();
         editor.undo();
     }
 
     @Override
     public void redo() throws NotOpenedException {
         checkIfOpened();
         editor.redo();
     }
 
     @Override
     public void highlightLine(int line) throws NotOpenedException {
         checkIfOpened();
         editor.highlightLine(line);
     }
 
     @Override
     public boolean isModified() throws NotOpenedException {
         checkIfOpened();
         return editor.isModified();
     }
 
     @Override
     public EditorIdentifier getIdentifier() {
         return identifier;
     }
 
     @Override
     public void close() throws NotOpenedException {
         close(true);
     }
 
     @Override
     public void close(boolean saveFirst) throws NotOpenedException {
         checkIfOpened();
         if (saveFirst) {
             save(true);
         }
         String title = editor.getTitle();
         container.remove(editor);
         registry.remove(editor);
         editor.dispose();
         editor = null;
         LOG.info(getMessage(EDITOR_CLOSED_MESSAGE, new Object[] { title }));
     }
 
     @Override
     public boolean save(boolean withDialog) {
         return save(withDialog, SaveContext.NORMAL);
     }
 
     @Override
     public boolean save(boolean withDialog, SaveContext context)
             throws NotOpenedException {
         checkIfOpened();
         Validate.notNull(context, "Save context can't be null");
         if (identifier.getMetadata().isSaveable() && isModified()) {
             File file = editor.getFile();
             if (withDialog) {
                 Project project = file.getProject();
                 Boolean shouldContinue = dialogManager.showDialog(file
                         .getName(), project.getName());
                 if (!shouldContinue) {
                     return false;
                 }
             }
             try {
                 workspaceManager.save(file);
                 editor.setModified(false);
             } catch (IllegalStateException e) {
                if (file.getType().equals(FileType.SOURCE) && e.getMessage().equals("Resource source must have only one entity with the same name.")) {
                     differentEntityNameDialogManager.showDialog();
                     return false;
                 }
 
                 throw e;
             } catch (CircuitInterfaceExtractionException e) {
                 if (file.getType().equals(FileType.SOURCE) && e.getMessage().equals("Entity block is invalid.")) {
                     invalidEntityBlockDialogManager.showDialog();
                     return false;
                 }
 
                 throw e;
             } catch (InvalidStateException e) {
                 if (file.getType().equals(FileType.SOURCE) && e.getMessage().startsWith("validation failed for: hr.fer.zemris.vhdllab.service.ci")) {
                     invalidEntityBlockDialogManager.showDialog();
                     return false;
                 }
 
                 throw e;
             }
         }
         return true;
     }
 
     private void checkIfOpened() {
         if (!isOpened()) {
             throw new NotOpenedException("Component " + identifier
                     + " isn't opened");
         }
     }
 
 }
