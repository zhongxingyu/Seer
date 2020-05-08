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
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JToolBar;
 
 import teo.isgci.drawing.DrawingLibraryInterface;
 import teo.isgci.drawing.GraphManipulationInterface;
 
 /**
  * ISGCI-specific implementation of the JToolBar that modifies the application
  * and the current DrawingLibraryInterface.
  */
 public class ISGCIToolBar extends JToolBar {
     /**
      * Change this every time this class is changed.
      */
     private static final long serialVersionUID = 1L;
 
     /** Reference to parent-ISGCI Mainframe for opening dialogs etc. */
     private ISGCIMainFrame mainframe;
 
     /** Undo button, reference here to disable/enable. */
     private JButton undoButton;
 
     /** Redo button, reference here to disable/enable. */
     private JButton redoButton;
 
     /** Draw button, reference here because it's needed for some animations. */
     private JButton drawButton;
     
     /**
      * Creates a toolbar with icons that influence both ISGCI and the currently
      * active drawinglibraryinterface (the active tab). The parent needs an
      * initialized tabbedpane!
      * 
      * @param parent
      *            The parent mainframe, to which the toolbar is added.
      */
     public ISGCIToolBar(final ISGCIMainFrame parent) {
         // set basic layout
         setFloatable(false);
         setRollover(true);
         addButtons();
 
         mainframe = parent;
     }
 
     /**
      * Getter for {@link #drawButton}.
      * 
      * @return
      *          Returns {@link #drawButton}
      */
     public JButton getDrawButton() {
         return drawButton;
     }
     
     /**
      * Returns the drawinglibraryinterface.
      * 
      * @return
      *          The currently active graphmanipulationinterface or null.
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
     
     /**
      * Adds buttons to the toolbar.
      */
     private void addButtons() {
         final Dimension separatorSize = new Dimension(20, 10);
         
         addGeneralButtons();
         addSeparator(separatorSize);
         
         addGraphControlButtons();
         addSeparator(separatorSize);
         
         addZoomControls();
         addSeparator(separatorSize);
         
        addMiscButtons();
     }
 
     /**
      * Adds general buttons like "create new drawing" or export to
      * the toolbar.
      */
     private void addGeneralButtons() {
         // NEW DRAWING
         String newDrawingTooltip = "Open a dialogue to create a new drawing";
         drawButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.ADD_ICON, "Draw", newDrawingTooltip);
         add(drawButton);
 
         drawButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 getManipulationInterface();
                 mainframe.openSelectGraphClassesDialog();
             }
         });
         
         // EXPORT
         String exportTooltip = "Open the export dialogue";
         JButton exportbutton = IconButtonFactory.createImageButton(
                 IconButtonFactory.EXPORT_ICON, "Export", exportTooltip);
         add(exportbutton);
 
         exportbutton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 getManipulationInterface();
                 mainframe.openExportDialog();
             }
         });
     }
     
     /**
      * Adds controls like undo and redo to the toolbar.
      */
     private void addGraphControlButtons() {
         // UNDO
         String undoTooltip = "Undo the last action";
         undoButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.UNDO_ICON, undoTooltip);
         // undoButton.setEnabled(false);
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
 
                     // disable button if no more undos possible
                     // undoButton.setEnabled(graphManipulation.canUndo());
                 }
             }
         });
         
         // REDO
         String redoTooltip = "Redo the last undone action";
         redoButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.REDO_ICON, redoTooltip);
         // redoButton.setEnabled(false);
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
 
                     // disable button if no more undos possible
                     // redoButton.setEnabled(graphManipulation.canRedo());
                 }
             }
         });
 
     }
     
     /**
      * Adds zooming-related controls to the toolbar.
      */
     private void addZoomControls() {
 
         // ZOOM
         final String defaultEntry = "Set Zoom";
         final String zoomToFit = "Zoom to fit";
        final JComboBox zoomBox = new JComboBox(new String[] { defaultEntry, 
                "100%", "200%", "300%", zoomToFit });
         add(zoomBox);
         
         // make sure zoombox doesn't get bigger
         final Dimension zoomBoxSize = new Dimension(100, 50);
         zoomBox.setMaximumSize(zoomBoxSize);
         
         
         zoomBox.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent e) {
                 // get entry
                 String zoomLevel = zoomBox.getSelectedItem().toString();
                 // reset zoomBox
                 zoomBox.setSelectedIndex(0);
                 
                 GraphManipulationInterface<?, ?> graphManipulation = 
                         getManipulationInterface();
                 
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
 
         
         // ZOOM OUT
         String zoomOutTooltip = "Zoom Out";
         JButton zoomoutbutton  = IconButtonFactory.createImageButton(
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
     }
     
     /**
      * Add controls that have no specific group.
      * 
      * @param <V>
      *          The vertex class.
      * @param <E>
      *          The edge class.
      */
     private <V, E> void addMiscButtons() {
         // DELETE
         String deleteTooltip = "Discard the selected nodes and their edges.";
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
 
                 drawLib.getGraphManipulationInterface().beginUpdate();
                 
                 GraphManipulationInterface<V, E> manipulationInterface =
                         drawLib.getGraphManipulationInterface();
                 
                 List<V> selectedNodes = drawLib.getSelectedNodes();
                 
                 for (V node : selectedNodes) {
                     manipulationInterface.removeNode(node);
                 }
                 
                 drawLib.getGraphManipulationInterface().endUpdate();
             }
         });
 
         // HIGHLIGHT
         String highlightTooltip = "Highlights the node and their neighbours.";
         JButton highlightButton = IconButtonFactory.createImageButton(
                 IconButtonFactory.HELP_ICON, highlightTooltip);
         add(highlightButton);
         
         highlightButton.addActionListener(new ActionListener() {
             
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
                 
                 manipulationInterface.unHiglightAll();
                 
                 for (V node : selectedNodes) {
                     manipulationInterface.highlightNode(node, true);
                 }
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
                     manipulationInterface.highlightNode(
                             selectedNodes.get(0), false);
                 }
             }
         });
 
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
 
         // HOME
         String homeTooltip = "Opens a new tab with the startpage.";
         JButton homebutton = IconButtonFactory.createImageButton(
                 IconButtonFactory.HOME_ICON, homeTooltip);
         add(homebutton);
 
         homebutton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 mainframe.getTabbedPane().addStartpage();
             }
         });
     }
     
 }
 
 /* EOF */
