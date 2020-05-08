 package fr.nantes1900.view.isletprocess;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import fr.nantes1900.constants.Icons;
 import fr.nantes1900.constants.TextsKeys;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.utils.FileTools;
 import fr.nantes1900.view.components.HelpButton;
 
 /**
  * Characteristics panel for the 6th step of an islet process.
  * @author Camille Bouquet
  * @author Luc Jallerat
  */
 public class CharacteristicsStep6View extends CharacteristicsView {
 
     /**
      * Default serial UID.
      */
     private static final long serialVersionUID = 1L;
     /**
      * List of the neighbors for the locked surface. The order in the list
      * indicates the order of the neighbors.
      */
     protected JList<Surface> lNeighbors;
     /**
      * Model of list to modify elements in the JList.
      */
     private DefaultListModel<Surface> dlm = new DefaultListModel<>();
     /**
      * Button to move up a surface in the neighbors order.
      */
     private JButton upButton;
     /**
      * Button to move down a surface in the neighbors order.
      */
     private JButton downButton;
     /**
      * Help button to indicate what to do whith the list and the buttons.
      */
     private HelpButton helpButton;
 
     /**
      * Button to lock a surface and modify the list of its neighbors.
      */
     private JButton bLock;
     /**
      * Check box to put surfaces into noise.
      */
     private JCheckBox cbNoise;
 
     /**
      * New step 6 characteristics panel with the list of neighbours of the
      * current selected surface.
      * @param neighbors
      *            the list of neighbors of the current surface
      */
     public CharacteristicsStep6View(final List<Surface> neighbors) {
         super();
 
         this.bLock = new JButton(new ImageIcon(Icons.lock));
 
         this.upButton = new JButton(new ImageIcon(Icons.up));
         this.upButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 int selectedIndex = lNeighbors.getSelectedIndex();
 
                 // if the selected index is in the list.
                 if (selectedIndex != -1)
                 {
                     Surface selectedSurface = lNeighbors.getSelectedValue();
 
                     dlm.removeElement(selectedSurface);
                     dlm.insertElementAt(selectedSurface, selectedIndex - 1);
                     lNeighbors.setSelectedValue(selectedSurface, true);
                 }
             }
         });
 
         this.downButton = new JButton(new ImageIcon(Icons.down));
         this.downButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 int selectedIndex = lNeighbors.getSelectedIndex();
 
                 // if the selected index is in the list.
                 if (selectedIndex != -1)
                 {
                     Surface selectedSurface = lNeighbors.getSelectedValue();
 
                     dlm.removeElement(selectedSurface);
                     dlm.insertElementAt(selectedSurface, selectedIndex + 1);
                     lNeighbors.setSelectedValue(selectedSurface, true);
                 }
             }
         });
 
         // Creation of the list
         for (Surface neighbour : neighbors)
         {
             this.dlm.addElement(neighbour);
         }
 
         this.lNeighbors = new JList<>(this.dlm);
         this.lNeighbors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         this.lNeighbors.addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(final ListSelectionEvent arg0) {
                 if (lNeighbors.getSelectedIndex() == 0)
                 {
                     upButton.setEnabled(false);
                 } else if (!upButton.isEnabled())
                 {
                     upButton.setEnabled(true);
                 }
 
                 if (lNeighbors.getSelectedIndex() == CharacteristicsStep6View.this.dlm
                         .getSize() - 1)
                 {
                     downButton.setEnabled(false);
                 } else if (!downButton.isEnabled())
                 {
                     downButton.setEnabled(true);
                 }
 
             }
         });
 
         // Sets layout and adds components
         JScrollPane jsp = new JScrollPane(this.lNeighbors);
         jsp.setPreferredSize(new Dimension(100, 50));
         jsp.setMaximumSize(new Dimension(100, 50));
         jsp.setMinimumSize(new Dimension(100, 50));
 
         JPanel sortPanel = new JPanel();
         sortPanel.setLayout(new GridBagLayout());
         sortPanel.add(jsp, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                         0, 10, 0, 10), 0, 0));
         sortPanel.add(this.upButton, new GridBagConstraints(1, 0, 1, 1, 0.0,
                 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 new Insets(0, 10, 0, 10), 0, 0));
         sortPanel.add(this.downButton, new GridBagConstraints(1, 1, 1, 1, 0.0,
                 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                 new Insets(0, 10, 0, 10), 0, 0));
 
         this.helpButton = new HelpButton(FileTools.readHelpMessage(
                 TextsKeys.KEY_HELP_C_NEIGHBORS, TextsKeys.MESSAGETYPE_TOOLTIP),
                 FileTools.readHelpMessage(TextsKeys.KEY_HELP_C_NEIGHBORS,
                         TextsKeys.MESSAGETYPE_MESSAGE),
                 FileTools.readHelpMessage(TextsKeys.KEY_HELP_C_NEIGHBORS,
                         TextsKeys.MESSAGETYPE_TITLE));
 
         this.getPanelContent().add(bLock);
         this.addCaracteristic(createSimpleCaracteristic(sortPanel, new JLabel(
                FileTools.readElementText(TextsKeys.KEY_SORTOUTNEIGHBORS)),
                 this.helpButton));
 
         // Puts in noise actions
         this.cbNoise = new JCheckBox();
         this.cbNoise.addActionListener(new ActionListener(){
 
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 checkEnableValidateButton();
             }
             
         });
 
         this.addCaracteristic(createSimpleCaracteristic(
                 this.cbNoise,
                 new JLabel(FileTools
                         .readElementText(TextsKeys.KEY_PUTINNOISETEXT)),
                 new HelpButton(FileTools.readHelpMessage(
                         TextsKeys.KEY_HELP_C_NOISE,
                         TextsKeys.MESSAGETYPE_TOOLTIP), FileTools
                         .readHelpMessage(TextsKeys.KEY_HELP_C_NOISE,
                                 TextsKeys.MESSAGETYPE_MESSAGE), FileTools
                         .readHelpMessage(TextsKeys.KEY_HELP_C_NOISE,
                                 TextsKeys.MESSAGETYPE_TITLE))));
         this.bValidate.setEnabled(true);
     }
 
     /**
      * Modify the list of neighbors whith the new one.
      * @param neighbors
      *            the new list of neighbors
      */
     public final void setList(final ArrayList<Surface> neighbors) {
         this.dlm.removeAllElements();
         for (Surface neighbour : neighbors)
         {
             this.dlm.addElement(neighbour);
         }
         revalidate();
         repaint();
     }
 
     /**
      * Gets the neighbor list
      * @return the neighbors list
      */
     public final ArrayList<Surface> getList() {
         ArrayList<Surface> neighborsList = new ArrayList<>();
 
         for (int i = 0; i < this.dlm.size(); i++)
         {
             neighborsList.add(this.dlm.elementAt(i));
         }
         return neighborsList;
     }
 
     /**
      * Gets the lock button.
      * @return the lock button
      */
     public final JButton getLockButton() {
         return this.bLock;
     }
 
     /**
      * Is the noise checkbox selected.
      * @return true - the noise checkbox is selected\n false - the noise
      *         checkbox is not selected
      */
     public final boolean isNoiseSelected() {
         return this.cbNoise.isSelected();
     }
     
     /**
      * Enables or disables validate button.
      */
     private void checkEnableValidateButton()
     {
         if (cbNoise.isSelected() || lNeighbors.isEnabled())
         {
             this.bValidate.setEnabled(true);
         } else
         {
             this.bValidate.setEnabled(false);
         }
     }
     
     /**
      * Gets the list element.
      * @return the list element
      */
     public JList<Surface> getJListNeighbors()
     {
         return this.lNeighbors;
     }
 
     /**
      * Enables or disables all characteristics elements excepted lock button.
      * @param enable
      *            true - enables\n false - disables
      */
     public final void setModificationsEnabled(final boolean enable) {
         this.lNeighbors.setEnabled(enable);
         this.upButton.setEnabled(enable);
         this.downButton.setEnabled(enable);
         this.helpButton.setEnabled(enable);
         checkEnableValidateButton();
     }
 }
