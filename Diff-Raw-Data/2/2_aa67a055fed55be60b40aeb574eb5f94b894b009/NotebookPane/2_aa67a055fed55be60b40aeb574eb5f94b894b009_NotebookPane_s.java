 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2010  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.gui.notebook;
 
 import java.awt.BorderLayout;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.HierarchyListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 
 import org.trypticon.hex.HexViewer;
 import org.trypticon.hex.anno.Annotation;
 import org.trypticon.hex.anno.swing.AnnotationPane;
 import org.trypticon.hex.gui.SaveNotebookAction;
 import org.trypticon.hex.gui.util.Callback;
 import org.trypticon.hex.gui.util.SaveConfirmation;
 
 /**
  * Pane for working with a single notebook.
  *
  * @author trejkaz
  */
 public class NotebookPane extends JPanel {
     private final Notebook notebook;
     private final HexViewer viewer;
     private final AnnotationPane annoPane;
 
     /**
      * Constructs the notebook pane.
      *
      * @param notebook the notebook to view.
      */
     public NotebookPane(Notebook notebook) {
         this.notebook = notebook;
 
         // TODO: A proper binding API would be nice here...
         setName(notebook.getName());
         notebook.addPropertyChangeListener("name", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent event) {
                 setName((String) event.getNewValue());
             }
         });
         notebook.addPropertyChangeListener("dirty", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent event) {
                 firePropertyChange("dirty", event.getOldValue(), event.getNewValue());
             }
         });
 
         annoPane = new AnnotationPane();
         annoPane.setAnnotations(notebook.getAnnotations());
         annoPane.setBinary(notebook.getBinary());
 
         viewer = new HexViewer();
         viewer.setAnnotations(annoPane.getExpandedAnnotations());
         viewer.setBinary(notebook.getBinary());
 
         annoPane.addPropertyChangeListener("selectedAnnotationPath", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent event) {
                 @SuppressWarnings("unchecked")
                 List<Annotation> selectedAnnotationPath = (List<Annotation>) event.getNewValue();
                 if (selectedAnnotationPath != null) {
                     Annotation annotation = selectedAnnotationPath.get(selectedAnnotationPath.size() - 1);
                     viewer.getSelectionModel().setCursor(annotation.getPosition());
                     viewer.getSelectionModel().setCursorAndExtendSelection(annotation.getPosition() + annotation.getLength() - 1);
                 }
             }
         });
 
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
         splitPane.setLeftComponent(annoPane);
         splitPane.setRightComponent(viewer);
         splitPane.setDividerLocation(annoPane.getPreferredSize().width);
         splitPane.setResizeWeight(1.0); // left component gets all the extra space
 
         setLayout(new BorderLayout());
         add(splitPane, BorderLayout.CENTER);
 
         // Why ComponentListener doesn't work here I will never know.
         addHierarchyListener(new HierarchyListener() {
             @Override
             public void hierarchyChanged(HierarchyEvent hierarchyEvent) {
                 viewer.requestFocusInWindow();
             }
         });
     }
 
     /**
      * Gets the notebook being viewed in this pane.
      *
      * @return the notebook being viewed.
      */
     public Notebook getNotebook() {
         return notebook;
     }
 
     /**
      * Gets the hex viewer.
      *
      * @return the hex viewer.
      */
     public HexViewer getViewer() {
         return viewer;
     }
 
     /**
      * <p>Tests if the notebook has been modified since the last time it was saved.</p>
      *
      * <p>This is the same as {@code getNotebook().isDirty()}, but is more convenient for tracking property changes.</p>
      *
      * @return {@code true} if the document is dirty, {@code false} otherwise.
      */
     public boolean isDirty() {
         return notebook.isDirty();
     }
 
     /**
      * Prepares for closing the pane.
      *
      * @param okToCloseCallback a callback which is called with {@code true} if it's okay to close or
      * {@code false} if it is not OK.
      */
     public void prepareForClose(final Callback<Boolean> okToCloseCallback) {
         // On exit, some frames might be left around in the background which have their notebooks closed already.
         if (notebook.isOpen() && notebook.isDirty()) {
             // So the user knows which one it's asking about.
             JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
             if (tabbedPane != null) {
                 tabbedPane.setSelectedComponent(this);
             }
 
             SaveConfirmation.getInstance().show(getRootPane(), new Callback<SaveConfirmation.Option>() {
                 @Override
                 public void execute(SaveConfirmation.Option option) {
                     switch (option) {
                         case CANCEL:
                             okToCloseCallback.execute(false);
                             break;
                         case DO_NOT_SAVE:
                             okToCloseCallback.execute(true);
                             break;
                         case SAVE:
                             SaveNotebookAction saveAction = (SaveNotebookAction) getRootPane().getActionMap().get("save");
                             boolean saveSucceeded = saveAction.save(getRootPane());
                             okToCloseCallback.execute(saveSucceeded);
                             break;
                         default:
                             throw new IllegalStateException("Impossible save confirmation option found");
                     }
                 }
             });
         }
     }
 
 }
