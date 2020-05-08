 package src.ui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.border.Border;
 
 import src.nonui.AppController;
 
 /**
  * View for the polygon label panel.
  */
 public class LabelPanelView extends JPanel {
     // JPanel is serializable, so we need some ID to avoid compiler warnings.
     private static final long serialVersionUID = 1L;
 
     private final JFrame parentFrame;
     private final AppController controller;
 
     // The label list and its backing model.
     private JPanel labelListPane;
     private final DefaultListModel listModel;
     private final JList labelsList;
 
     private final JPopupMenu rightClickMenu;
 
     private final JButton addButton;
     private final JButton editButton;
     private final JButton loadButton;
     private final JButton deleteButton;
 
     public LabelPanelView(JFrame frame, AppController appController) {
         Border labelBorder = BorderFactory.createTitledBorder("Labels");
         this.setBorder(labelBorder);
         this.setLayout(new BorderLayout());
 
         this.parentFrame = frame;
         this.controller = appController;
 
         // Set up the backing data structures for the label list.
         listModel = new DefaultListModel();
         labelsList = new JList(listModel);
         labelsList.setSelectedIndex(0);
         labelsList.setVisibleRowCount(5);
         labelsList.addMouseListener(new LabelListMouseListener());
 
         // Create the right click menu.
         rightClickMenu = createRightClickMenu();
 
         // Set up the buttons.
         addButton = createButton(new ImageIcon("hci/icons/small/add.png"), new AddListener(), false);
         editButton = createButton(new ImageIcon("hci/icons/small/edit.png"), new EditListener(),
                 false);
         deleteButton = createButton(new ImageIcon("hci/icons/small/delete.png"),
                 new DeleteListener(), false);
         loadButton = createButton("Load Labels", new LoadListener(), false);
 
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
         buttonPane.add(addButton);
         buttonPane.add(Box.createHorizontalStrut(10));
         buttonPane.add(editButton);
         buttonPane.add(Box.createHorizontalStrut(10));
         buttonPane.add(deleteButton);
 
         // This panel is shown if there are no labels.
         JPanel noLabelsPane = new JPanel();
         noLabelsPane.add(new JLabel(
                 "<html>You don't seem to have any labels currently. <br /> Add or load some "
                         + "labels?</html>"));
         noLabelsPane.add(loadButton);
 
         labelListPane = new JPanel(new CardLayout());
         labelListPane.add(noLabelsPane, "NOLABELS");
         labelListPane.add(new JScrollPane(labelsList), "HAVELABELS");
 
         add(labelListPane, BorderLayout.CENTER);
         add(buttonPane, BorderLayout.PAGE_END);
 
         CardLayout cl = (CardLayout) (labelListPane.getLayout());
         cl.show(labelListPane, "NOLABELS");
         this.setVisible(true);
     }
 
     /**
      * Add a label to the list of labels.
      * 
      * @param name the name of the label to add
      */
     public void addLabel(String name) {
         listModel.addElement(name);
 
         // Once a label has been added, renaming/deleting is possible.
         setEditButtonEnabled(true);
         setDeleteButtonEnabled(true);
         showLabelList();
     }
 
     /**
      * Clears the list of labels.
      */
     public void clear() {
         listModel.clear();
 
         setAddButtonEnabled(true);
         setEditButtonEnabled(false);
         setDeleteButtonEnabled(false);
         setLoadButtonEnabled(true);
 
         hideLabelList();
     }
 
     /**
      * Sets whether or not the add label button should be enabled.
      * 
      * @param enabled whether to enable or disable the add label button
      */
     public void setAddButtonEnabled(boolean enabled) {
         addButton.setEnabled(enabled);
     }
 
     /**
      * Sets whether or not the edit label button should be enabled.
      * 
      * @param enabled whether to enable or disable the edit label button
      */
     public void setEditButtonEnabled(boolean enabled) {
         editButton.setEnabled(enabled);
     }
 
     /**
      * Sets whether or not the delete label button should be enabled.
      * 
      * @param enabled whether to enable or disable the delete label button
      */
     public void setDeleteButtonEnabled(boolean enabled) {
         deleteButton.setEnabled(enabled);
     }
 
     /**
      * Sets whether or not the load labels button should be enabled.
      * 
      * @param enabled whether to enable or disable the load labels button
      */
     public void setLoadButtonEnabled(boolean enabled) {
         loadButton.setEnabled(enabled);
     }
 
     /**
      * Allows the user to rename the currently selected polygon.
      */
     public void renameSelectedPolygon() {
         int index = labelsList.getSelectedIndex();
         if (index == -1) {
             JOptionPane.showMessageDialog(parentFrame, "No label selected", "Error",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         String name = null;
         String currentName = listModel.get(index).toString();
 
         String message = "New Label Name:";
         name = JOptionPane.showInputDialog(parentFrame, message, currentName);
 
         // If the user hits cancel, we do nothing.
         if (name == null) {
             return;
         }
 
         name = name.trim();
         if (listModel.contains(name)) {
             JOptionPane.showMessageDialog(parentFrame,
                     "That name is already in use. The name was not changed.", "Error",
                     JOptionPane.ERROR_MESSAGE);
 
         } else if (name.isEmpty()) {
             JOptionPane.showMessageDialog(parentFrame,
                     "Blank names are not allowed. The name was not changed.", "Error",
                     JOptionPane.ERROR_MESSAGE);
         } else {
             listModel.remove(index);
             listModel.add(index, name);
 
             controller.renamePolygon(currentName, name);
         }
     }
 
     /**
      * Deletes the currently selected polygons.
      */
     public void deleteSelectedPolygons() {
         int[] indices = labelsList.getSelectedIndices();
 
         if (indices.length == 0) {
             JOptionPane.showMessageDialog(parentFrame, "No label(s) selected", "Error",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         for (int i = indices.length - 1; i >= 0; i--) {
             controller.removePolygon((String) listModel.get(indices[i]));
             listModel.remove(indices[i]);
         }
 
         if (listModel.getSize() == 0) {
             setEditButtonEnabled(false);
             setDeleteButtonEnabled(false);
             hideLabelList();
         }
 
         // Make sure that the selected index is still within the list range.
         labelsList.setSelectedIndex(Math.min(indices[0], listModel.getSize() - 1));
         editButton.setEnabled(labelsList.getSelectedIndices().length == 1);
         controller.highlightSelected(getSelectedNames());
 
     }
 
     /**
      * Deletes all of the polygons.
      */
     public void deleteAllPolygons() {
        if (listModel.getSize() == 0) {
            JOptionPane.showMessageDialog(parentFrame, "There are no labels to delete", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

         for (int i = 0; i < listModel.getSize(); i++) {
             controller.removePolygon((String) listModel.get(i));
         }
 
         clear();
 
         // TODO: This call shouldnt be necessary.
         controller.highlightSelected(getSelectedNames());
     }
 
     /**
      * Returns the names of the currently selected polygons.
      */
     public List<String> getSelectedNames() {
         List<Integer> indices = new ArrayList<Integer>(labelsList.getSelectedIndices().length);
         for (int index : labelsList.getSelectedIndices()) {
             indices.add(index);
         }
 
         List<String> names = new ArrayList<String>(labelsList.getSelectedIndices().length);
         for (int i = 0; i < listModel.getSize(); i++) {
             if (indices.contains(i)) {
                 names.add((String) listModel.get(i));
             }
         }
 
         return names;
     }
 
     /**
      * Show the list of labels.
      */
     private void showLabelList() {
         CardLayout cl = (CardLayout) (labelListPane.getLayout());
         cl.show(labelListPane, "HAVELABELS");
     }
 
     /**
      * Hides the list of labels.
      */
     private void hideLabelList() {
         CardLayout cl = (CardLayout) (labelListPane.getLayout());
         cl.show(labelListPane, "NOLABELS");
     }
 
     /**
      * Creates the menu that appears when the user right clicks on a label.
      */
     private JPopupMenu createRightClickMenu() {
         JPopupMenu rightClickMenu = new JPopupMenu();
 
         JMenuItem renameLabel = new JMenuItem("Rename");
         renameLabel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 renameSelectedPolygon();
             }
         });
         JMenuItem deleteLabel = new JMenuItem("Delete");
         deleteLabel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 deleteSelectedPolygons();
             }
         });
         rightClickMenu.add(renameLabel);
         rightClickMenu.add(deleteLabel);
 
         return rightClickMenu;
     }
 
     /**
      * Creates a JButton with text on it.
      * 
      * @param text the text to display on the button
      * @param actionListener the listener to call when the button is pressed
      * @param enabled whether or not to enable the button by default
      */
     private static JButton createButton(String text, LoadListener actionListener, boolean enabled) {
         JButton button = new JButton(text);
         button.addActionListener(actionListener);
         button.setEnabled(enabled);
         return button;
     }
 
     /**
      * Creates a JButton with an icon on it.
      * 
      * @param icon the image to display on the button
      * @param actionListener the listener to call when the button is pressed
      * @param enabled whether or not to enable the button by default
      */
     private static JButton createButton(ImageIcon icon, ActionListener actionListener,
             boolean enabled) {
         JButton button = new JButton(icon);
         button.addActionListener(actionListener);
         button.setEnabled(enabled);
         return button;
     }
 
     /**
      * Listener class for adding a new polygon to the image.
      */
     private class AddListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             controller.startAddingNewPolygon();
         }
     }
 
     /**
      * Listener class for editing the name of a label.
      */
     private class EditListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             renameSelectedPolygon();
         }
     }
 
     /**
      * Listener class for deleting a selected label/polygon from the image.
      */
     private class DeleteListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             deleteSelectedPolygons();
         }
     }
 
     /**
      * Listener class for loading labels from a file.
      */
     private class LoadListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             controller.loadLabels();
         }
     }
 
     /**
      * Class that monitors clicks on the label list.
      */
     private class LabelListMouseListener implements MouseListener {
         @Override
         public void mouseClicked(MouseEvent e) {
             // Only allow renaming of single items at once.
             editButton.setEnabled(labelsList.getSelectedIndices().length == 1);
 
             controller.highlightSelected(getSelectedNames());
 
             if (e.getButton() == MouseEvent.BUTTON3 && labelsList.getSelectedIndices().length == 1) {
                 rightClickMenu.show(LabelPanelView.this, e.getX(), e.getY());
             }
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
         }
 
         @Override
         public void mousePressed(MouseEvent e) {
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
         }
 
         @Override
         public void mouseEntered(MouseEvent e) {
         }
     }
 }
