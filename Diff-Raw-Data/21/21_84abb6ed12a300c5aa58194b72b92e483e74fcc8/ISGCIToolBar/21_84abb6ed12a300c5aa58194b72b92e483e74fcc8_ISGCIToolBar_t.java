 /*
  * ISGCI-specific implementation of the JToolBar that modifies the 
  * application and the current DrawingLibraryInterface.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JToolBar;
 import teo.isgci.db.DataSet;
 import teo.isgci.drawing.DrawingLibraryInterface;
 import teo.isgci.drawing.GraphManipulationInterface;
 import teo.isgci.problem.Problem;
 import teo.isgci.util.UserSettings;
 
 /**
  * ISGCI-specific implementation of the JToolBar that modifies the application
  * and the current DrawingLibraryInterface.
  */
 public class ISGCIToolBar extends JToolBar {
     /**
      * Name for no selected problem.
      */
     private static final String NOPROBLEMSELECTED = "None";
     /**
      * Change this every time this class is changed.
      */
     private static final long serialVersionUID = 5L;
     /**
      * Reference to parent-ISGCI Mainframe for opening dialogs etc.
      */
     private ISGCIMainFrame mainframe;
     /**
      * Problem combobox, reference here to set the currently active problem.
      */
     private JComboBox problemBox;
     /**
      * Redo button, reference here to disable/enable.
      */
     private JButton redoButton;
     /**
      * True if user changes problembox, false if it was checked via
      * {@link setProblem}.
      */
     private boolean setProblem = true;
     /**
      * Undo button, reference here to disable/enable.
      */
     private JButton undoButton;
 
     /**
      * Creates a toolbar with icons that influence both ISGCI and the currently
      * active drawinglibraryinterface (the active tab). The parent needs an
      * initialized tabbedpane!
      *
      * @param parent The parent mainframe, to which the toolbar is added.
      */
     public ISGCIToolBar(final ISGCIMainFrame parent) {
         // set basic layout
         setFloatable(false);
         setRollover(true);
         addButtons();
 
         mainframe = parent;
     }
 
     /**
      * Sets the currently active problem.
      *
      * @param problem The currently active problem.
      */
     public void setProblem(String problem) {
         setProblem = false;
         if (!problem.equals(problemBox.getSelectedItem())) {
             problemBox.setSelectedItem(problem);
         }
         setProblem = true;
     }
 
     /**
      * Adds buttons to the toolbar.
      */
     private void addButtons() {
         final Dimension separatorSize = new Dimension(20, 10);
 
         addUndoRedo();
         addSeparator(separatorSize);
 
         addZoomControls();
         addSeparator(separatorSize);
 
         addGraphControlButtons();
         addSeparator(separatorSize);
 
         addMiscButtons();
         addSeparator(separatorSize);
 
         addProblemChooser();
     }
 
     /**
      * Adds general buttons like "create new drawing" or export to
      * the toolbar.
      *
      * @param <V> The vertex class.
      * @param <E> The edge class.
      */
     private <V, E> void addGraphControlButtons() {
 
         // DELETE
         String deleteTooltip = "Deletes all selected and highlighted"
                 + " nodes and their edges.";
         JButton deletebutton = IconButtonFactory.createImageButton(
                 IconButtonFactory.DELETE_ICON, deleteTooltip);
         add(deletebutton);
 
         deletebutton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 DrawingLibraryInterface<V, E> drawLib =
                         mainframe.getTabbedPane()
                                 .getActiveDrawingLibraryInterface();
 
                 if (drawLib == null) {
                     return;
                 }
 
                 GraphManipulationInterface<V, E> manipulationInterface =
                         drawLib.getGraphManipulationInterface();
 
                 manipulationInterface.removeHighlightedNodes();
             }
         });
 
         // CENTER
         String centerTooltip = "Centers the selected node.";
         JButton centerButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.ALIGN_CENTER_ICON, centerTooltip);
         add(centerButton);
 
         centerButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 DrawingLibraryInterface<V, E> drawLib =
                         mainframe.getTabbedPane()
                                 .getActiveDrawingLibraryInterface();
 
                 if (drawLib == null) {
                     return;
                 }
 
                 GraphManipulationInterface<V, E> manipulationInterface =
                         drawLib.getGraphManipulationInterface();
 
                 List<V> selectedNodes = drawLib.getSelectedNodes();
 
                 if (selectedNodes.size() == 1) {
                     manipulationInterface.centerNode(selectedNodes.get(0));
                 } else if (selectedNodes.size() > 1) {
                     manipulationInterface.centerNodes((V[]) selectedNodes
                             .toArray());
                 }
             }
         });
 
         // HIGHLIGHT PARENT
         String highlightParentTooltip =
                 "Highlights the parent-nodes of the selected nodes."
                         + " Clicking multiple times will increase the depth.";
         JButton highlightParentButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.HIGHLIGHTPARENT_ICON,
                 highlightParentTooltip);
         add(highlightParentButton);
 
         highlightParentButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 DrawingLibraryInterface<V, E> drawLib =
                         mainframe.getTabbedPane()
                                 .getActiveDrawingLibraryInterface();
 
                 if (drawLib == null) {
                     return;
                 }
 
                 GraphManipulationInterface<V, E> manipulationInterface =
                         drawLib.getGraphManipulationInterface();
 
                 List<V> selectedNodes = drawLib.getSelectedNodes();
                 manipulationInterface.highlightParents(selectedNodes);
             }
         });
 
         // HIGHLIGHT CHILDREN
         String highlightChildTooltip =
                 "Highlights the child-nodes of the selected nodes."
                         + " Clicking multiple times will increase the depth.";
         JButton highlightChildButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.HIGHLIGHTCHILD_ICON,
                 highlightChildTooltip);
         add(highlightChildButton);
 
         highlightChildButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 DrawingLibraryInterface<V, E> drawLib
                         = mainframe.getTabbedPane()
                         .getActiveDrawingLibraryInterface();
 
                 if (drawLib == null) {
                     return;
                 }
 
                 GraphManipulationInterface<V, E> manipulationInterface =
                         drawLib.getGraphManipulationInterface();
 
                 List<V> selectedNodes = drawLib.getSelectedNodes();
                 manipulationInterface.highlightChildren(selectedNodes);
             }
         });
     }
 
     /**
      * Add controls that have no specific group.
      *
      * @param <V> The vertex class.
      * @param <E> The edge class.
      */
     private <V, E> void addMiscButtons() {
 
         // SEARCH
         String searchTooltip = "Opens a dialogue to search for a specific "
                 + "graphclass in the drawing.";
         JButton searchbutton = IconButtonFactory.createImageButton(
                 IconButtonFactory.SEARCH_ICON, searchTooltip);
         add(searchbutton);
 
         searchbutton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 mainframe.openSearchDialog();
             }
         });
     }
 
     /**
      * Adds a combobox for choosing and displaying the current problem.
      */
     private void addProblemChooser() {
         problemBox = new JComboBox();
 
         problemBox.setToolTipText("Choose a problem to colour the nodes "
                 + "in this tab in a specific colour "
                + "corresponding to the problem that was "
                 + "chosen.");
 
         problemBox.addItem(NOPROBLEMSELECTED);
 
         for (Problem problem : DataSet.problems) {
             problemBox.addItem(problem.getName());
         }
 
         problemBox.addItemListener(new ItemListener() {
 
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (setProblem) {
                     UserSettings.setProblem(DataSet.getProblem(
                             (String) problemBox.getSelectedItem()));
                 }
             }
         });
 
         // make sure zoombox doesn't get bigger
         final Dimension problemBoxSize = new Dimension(175, 36);
         problemBox.setMaximumSize(problemBoxSize);
         problemBox.setPreferredSize(problemBoxSize);
 
         add(problemBox);
     }
 
     /**
      * Adds controls like undo and redo to the toolbar.
      */
     private void addUndoRedo() {
         // UNDO
         String undoTooltip = "Undo the last action";
         undoButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.UNDO_ICON, undoTooltip);
         add(undoButton);
 
         undoButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 GraphManipulationInterface<?, ?> graphManipulation =
                         getManipulationInterface();
 
                 if (graphManipulation != null) {
                     // undo, if possible
                     if (graphManipulation.canUndo()) {
                         graphManipulation.undo();
                     }
                 }
             }
         });
 
         // REDO
         String redoTooltip = "Redo the last undone action";
         redoButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.REDO_ICON, redoTooltip);
         add(redoButton);
 
         redoButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 GraphManipulationInterface<?, ?> graphManipulation =
                         getManipulationInterface();
 
                 if (graphManipulation != null) {
                     // undo, if possible
                     if (graphManipulation.canRedo()) {
                         graphManipulation.redo();
                     }
                 }
             }
         });
 
     }
 
     /**
      * Adds zooming-related controls to the toolbar.
      */
     private void addZoomControls() {
 
         // ZOOM OUT
         String zoomOutTooltip = "Zoom Out";
         JButton zoomoutbutton = IconButtonFactory.createImageButton(
                 IconButtonFactory.ZOOM_OUT_ICON, zoomOutTooltip);
         add(zoomoutbutton);
 
         zoomoutbutton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 GraphManipulationInterface<?, ?> graphManipulation =
                         getManipulationInterface();
 
                 if (graphManipulation != null) {
                     graphManipulation.zoom(false);
                 }
             }
         });
 
         // ZOOM
         final String defaultEntry = "Set Zoom";
         final String zoomToFit = "Zoom to fit";
         final JComboBox zoomBox = new JComboBox(new String[]{defaultEntry,
                 "50%", "75%", "100%", "150%", "200%", "300%", zoomToFit});
         add(zoomBox);
 
         zoomBox.setToolTipText("Choose a zoom-level.");
 
         // make sure zoombox doesn't get bigger
        final Dimension zoomBoxSize = new Dimension(100, 36);
         zoomBox.setMaximumSize(zoomBoxSize);
         zoomBox.setPreferredSize(zoomBoxSize);
 
 
         zoomBox.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 // get entry
                 String zoomLevel = zoomBox.getSelectedItem().toString();
                 // reset zoomBox
                 zoomBox.setSelectedIndex(0);
 
                 GraphManipulationInterface<?, ?> graphManipulation =
                         getManipulationInterface();
 
 
                 if (graphManipulation == null) {
                     return;
                 }
 
                 if (zoomLevel == defaultEntry) {
                     return;
                 } else if (zoomLevel == zoomToFit) {
                     graphManipulation.zoomToFit();
 
                     return;
                 } else { // from here on, the text should be "x%"
                     String zoom = zoomLevel.replace("%", "");
                     try {
                         double zoomFactor = Double.parseDouble(zoom);
                         graphManipulation.zoomTo(zoomFactor / 100);
                     } catch (Exception err) {
                         err.printStackTrace();
                     }
                 }
 
             }
         });
 
         // Very small separator
         addSeparator(new Dimension(1, 1));
 
         // ZOOM IN
         String zoomInTooltip = "Zoom In";
         JButton zoominbutton = IconButtonFactory.createImageButton(
                 IconButtonFactory.ZOOM_IN_ICON, zoomInTooltip);
 
         add(zoominbutton);
 
         zoominbutton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 GraphManipulationInterface<?, ?> graphManipulation =
                         getManipulationInterface();
 
                 if (graphManipulation != null) {
                     graphManipulation.zoom(true);
                 }
             }
         });
 
     }
 
     /**
      * Returns the drawinglibraryinterface.
      *
      * @return The currently active graphmanipulationinterface or null.
      */
     private GraphManipulationInterface<?, ?> getManipulationInterface() {
         DrawingLibraryInterface<?, ?> drawinglib = mainframe.getTabbedPane()
                 .getActiveDrawingLibraryInterface();
 
         // no tab active
         if (drawinglib == null) {
             return null;
         }
 
         return drawinglib.getGraphManipulationInterface();
     }
 }
 
 /* EOF */
