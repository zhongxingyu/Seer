 package edu.wustl.cab2b.client.ui.experiment;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.jdesktop.swingx.JXTree;
 
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.wustl.cab2b.client.ui.MainSearchPanel;
 import edu.wustl.cab2b.client.ui.SearchNavigationPanel;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTextField;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.common.datalist.DataListBusinessInterface;
 import edu.wustl.cab2b.common.datalist.DataListHomeInterface;
 import edu.wustl.cab2b.common.domain.DataListMetadata;
 import edu.wustl.cab2b.common.domain.Experiment;
 import edu.wustl.cab2b.common.domain.ExperimentGroup;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.experiment.ExperimentBusinessInterface;
 import edu.wustl.cab2b.common.experiment.ExperimentGroupBusinessInterface;
 import edu.wustl.cab2b.common.experiment.ExperimentGroupHome;
 import edu.wustl.cab2b.common.experiment.ExperimentHome;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.tree.ExperimentTreeNode;
 import edu.wustl.common.tree.GenerateTree;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
import static edu.wustl.cab2b.client.ui.util.ClientConstants.TREE_OPEN_FOLDER;
import static edu.wustl.cab2b.client.ui.util.ClientConstants.TREE_CLOSE_FOLDER;
 
 /**
  * This class shows a panel to create new experiments with GUI controls for
  * specifying experiment name, description, parent experiment group, etc.
  * 
  * experiment create date, last modified date are implicit, which is the current
  * system date.
  * 
  * @author chetan_bh
  */
 public class NewExperimentDetailsPanel extends Cab2bPanel {
 
     private static final long serialVersionUID = 6029827434064457102L;
 
     /**
      * A text field component to accept experiment name from user.
      */
     JTextField expNameTextField;
 
     /**
      * A tree component to show experiment and experiment group hierarchy.
      */
     JXTree projectsTree;
 
     JScrollPane treeScrollPane;
 
     /**
      * A text area component to accept experiment description from the user.
      */
     JTextArea expDescTextArea;
 
     /**
      * A button component to listen for users "create new experiment group"
      * action.
      */
     Cab2bButton addNewButton;
 
     /**
      * A dialog component to show this panel in it.
      */
     JDialog dialog;
 
     /**
      * A button component to listen for users "save experiment" action.
      */
     Cab2bButton saveButton;
 
     /**
      * A button component to listen for users cancel "save experiment" action.
      */
     Cab2bButton cancelButton;
 
     public NewExperimentDetailsPanel() {
         initGUIGBL();
     }
 
     /**
      * Initialize this panel with all components using {@link GridBagLayout}.
      */
     private void initGUIGBL() {
         this.removeAll();
 
         Cab2bPanel exptPanel = new Cab2bPanel();
         Cab2bPanel projPanel = new Cab2bPanel();
 
         exptPanel.setLayout(new BorderLayout(0, 0));
         projPanel.setLayout(new BorderLayout(0, 0));
 
         JLabel asterix1 = new Cab2bLabel("* ");
         asterix1.setFont(new Font("Arial", Font.BOLD, 16));
         asterix1.setForeground(Color.RED);
 
         JLabel asterix2 = new Cab2bLabel("* ");
         asterix2.setFont(new Font("Arial", Font.BOLD, 16));
         asterix2.setForeground(Color.RED);
 
         // JLabel mandatoryNodeLabel = new
         // Cab2bLabel(ApplicationProperties.getValue("label.mandatorynote.text"));
 
         JLabel expNameLabel = new Cab2bLabel("Experiment Name :     ");
         expNameTextField = new Cab2bTextField();
         // expNameTextField.setColumns(22);
 
         JLabel projectsLabel = new Cab2bLabel("Project :                         ");
 
         exptPanel.add(asterix1, BorderLayout.WEST);
         exptPanel.add(expNameLabel, BorderLayout.EAST);
 
         projPanel.add(asterix2, BorderLayout.WEST);
         projPanel.add(projectsLabel, BorderLayout.EAST);
 
         Vector dataVector = null;
         final ExperimentBusinessInterface expBus = (ExperimentBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                   EjbNamesConstants.EXPERIMENT,
                                                                                                                   ExperimentHome.class);
         try {
             dataVector = expBus.getExperimentHierarchy();
         } catch (RemoteException e1) {
             CommonUtils.handleException(e1, this, true, true, false, false);
         } catch (ClassNotFoundException e1) {
             CommonUtils.handleException(e1, this, true, true, false, false);
         } catch (DAOException e1) {
             CommonUtils.handleException(e1, this, true, true, false, false);
         }
 
         GenerateTree treeGenerator = new GenerateTree();
         projectsTree = (JXTree) treeGenerator.createTree(
                                                          dataVector,
                                                          edu.wustl.common.util.global.Constants.EXPERIMETN_TREE_ID,
                                                          true);
 
         projectsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         projectsTree.setEditable(true);
         projectsTree.getModel().addTreeModelListener(new MyTreeModelListener());
 
         ClassLoader loader = this.getClass().getClassLoader();
         projectsTree.setOpenIcon(new ImageIcon(loader.getResource(TREE_OPEN_FOLDER)));
         projectsTree.setClosedIcon(new ImageIcon(loader.getResource(TREE_CLOSE_FOLDER)));
         projectsTree.setLeafIcon(new ImageIcon(loader.getResource("experiment_small.gif")));
 
         // setting tree node name
         projectsTree.setSelectionRow(0);
         ExperimentTreeNode treeNodeUserObj = (ExperimentTreeNode) ((DefaultMutableTreeNode) projectsTree.getSelectionPath().getPathComponent(
                                                                                                                                              0)).getUserObject();
         treeNodeUserObj.setName("My Projects");
         projectsTree.setSelectionRow(-1);
         projectsTree.updateUI();
 
         JLabel expDescLabel = new Cab2bLabel("Description : ");
         expDescTextArea = new JTextArea();
         //expDescTextArea.setColumns(35);
         expDescTextArea.setRows(5);
 
         this.setLayout(new BorderLayout());
 
         Cab2bPanel centerPanel = new Cab2bPanel();
         GridBagLayout gbl = new GridBagLayout();
         centerPanel.setLayout(gbl);
         GridBagConstraints gbc = new GridBagConstraints();
 
         gbc.insets = new Insets(5, 5, 5, 5);
 
         gbc.weighty = 0;
         gbc.weightx = 0;
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         // centerPanel.add(mandatoryNodeLabel, gbc);
 
         gbc.weighty = 0;
         gbc.weightx = 0;
         gbc.gridx = 0;
         gbc.gridy = 1;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         centerPanel.add(exptPanel, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 1;
         gbc.gridwidth = 3;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         centerPanel.add(expNameTextField, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 2;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         centerPanel.add(projPanel, gbc);
 
         treeScrollPane = new JScrollPane(projectsTree);
         treeScrollPane.setPreferredSize(new Dimension(250, 300));
 
         addNewButton = new Cab2bButton("Add New");
         addNewButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 Object obj = null;
 
                 if (projectsTree.getSelectionCount() > 0)
                     obj = projectsTree.getSelectionPath().getLastPathComponent();
                 DefaultMutableTreeNode selectedTreeNode = (DefaultMutableTreeNode) obj;
                 // There's no selection. Default to the root node.
 
                 if (selectedTreeNode == null) {
                     selectedTreeNode = (DefaultMutableTreeNode) projectsTree.getModel().getRoot();
                 } else {
                    ExperimentTreeNode expTreeNode = (ExperimentTreeNode) selectedTreeNode.getUserObject();
                    if (!expTreeNode.isExperimentGroup())
                        return;
                 }
                 // ------- inline editing ----------
                 DefaultTreeModel m_model = (DefaultTreeModel) projectsTree.getModel();
 
                 DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("New Node");
                 m_model.insertNodeInto(newNode, selectedTreeNode, selectedTreeNode.getChildCount());
 
                 // make the node visible by scroll to it
                 TreeNode[] nodes = m_model.getPathToRoot(newNode);
                 TreePath path = new TreePath(nodes);
                 projectsTree.scrollPathToVisible(path);
 
                 // select the newly added node
                 projectsTree.setSelectionPath(path);
 
                 // Make the newly added node editable
                 projectsTree.startEditingAtPath(path);
             }
         });
 
         /*	gbc.gridx = 1;
          gbc.gridy = 2;
          gbc.gridwidth = 1;
          gbc.gridheight = 1;
          gbc.weightx = 0;
          gbc.fill = GridBagConstraints.NONE;*/
         //centerPanel.add(addNewButton, gbc);
         gbc.gridx = 0;
         gbc.gridy = 3;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.weighty = 0.0;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         centerPanel.add(addNewButton, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 3;
         gbc.gridwidth = 3;
         gbc.gridheight = 4;
         gbc.weightx = 0.0;
         gbc.weighty = 0.3;
         gbc.fill = GridBagConstraints.BOTH;
         centerPanel.add(treeScrollPane, gbc);
 
         gbc.weighty = 0.0;
         gbc.gridx = 0;
         gbc.gridy = 7;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         centerPanel.add(expDescLabel, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 7;
         gbc.gridwidth = 3;
         gbc.gridheight = 2;
         gbc.weightx = 0.2;
         gbc.weighty = 0.4;
         gbc.fill = GridBagConstraints.BOTH;
         centerPanel.add(new JScrollPane(expDescTextArea), gbc);
 
         saveButton = new Cab2bButton("Save");
         saveButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (projectsTree.getSelectionCount() == 0) {
                     JOptionPane.showMessageDialog(NewExperimentDetailsPanel.this,
                                                   "Please associate experiment to a project under 'My Projects'.");
                     return;
                 }
 
                 Object obj = projectsTree.getSelectionPath().getLastPathComponent();
                 DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) obj;
 
                 ExperimentTreeNode expTreeNode = (ExperimentTreeNode) treeNode.getUserObject();
                 Long expGrpId = expTreeNode.getIdentifier();
                 Logger.out.debug("exp grp id " + expGrpId);
 
                 if (expGrpId == 0) {
                     JOptionPane.showMessageDialog(NewExperimentDetailsPanel.this,
                                                   " Please associate experiment to a project under 'My Projects'.");
                     return;
                 }
 
                 if (!expTreeNode.isExperimentGroup()) {
                     JOptionPane.showMessageDialog(NewExperimentDetailsPanel.this, "Select experiment group");
                     return;
                 }
 
                 Logger.out.debug("projectsTree.getSelectionCount() " + projectsTree.getSelectionCount());
 
                 String experimentName = expNameTextField.getText();
                 if (experimentName == null || experimentName.equals("")) {
                     Date currentDate = new Date(System.currentTimeMillis());
                     experimentName = currentDate.toString();
                 }
                 String experimentDescription = expDescTextArea.getText();
 
                 Experiment experiment = new Experiment();
                 experiment.setName(experimentName);
                 experiment.setDescription(experimentDescription);
                 experiment.setCreatedOn(new Date());
                 experiment.setLastUpdatedOn(new Date());
                 experiment.setActivityStatus("active");
                 experiment.getDataListMetadataCollection().add(MainSearchPanel.savedDataListMetadata);
 
                 try {
                     expBus.addExperiment(expGrpId, experiment);
                     SearchNavigationPanel.messageLabel.setText("* Experiment '" + experiment.getName()
                             + "' saved successfully .");
                     updateUI();
                 } catch (RemoteException e1) {
                     CommonUtils.handleException(e1, NewExperimentDetailsPanel.this, true, true, false, false);
                 } catch (DAOException e1) {
                     CommonUtils.handleException(e1, NewExperimentDetailsPanel.this, true, true, false, false);
                 } catch (BizLogicException e1) {
                     CommonUtils.handleException(e1, NewExperimentDetailsPanel.this, true, true, false, false);
                 } catch (UserNotAuthorizedException e1) {
                     CommonUtils.handleException(e1, NewExperimentDetailsPanel.this, true, true, false, false);
                 } finally {
                     dialog.dispose();
                 }
             }
         });
 
         cancelButton = new Cab2bButton("Cancel");
         cancelButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 dialog.dispose();
             }
         });
 
         /*
          * this.add("br br right",saveButton); this.add("tab
          * right",cancelButton);
          */
 
         Cab2bPanel bottomPanel = new Cab2bPanel();
         FlowLayout flowLayout = new FlowLayout();
         flowLayout.setAlignment(FlowLayout.RIGHT);
         flowLayout.setHgap(10);
         bottomPanel.setLayout(flowLayout);
         bottomPanel.add(cancelButton);
         bottomPanel.add(saveButton);
 
         gbc.gridx = 4;
         gbc.gridy = 11;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.BOTH;
 
         this.add(centerPanel, BorderLayout.CENTER);
         this.add(bottomPanel, BorderLayout.SOUTH);
 
         this.validate();
         this.updateUI();
     }
 
     public JDialog showInDialog() {
         Dimension dimension = MainFrame.mainframeScreenDimesion;
         dialog = WindowUtilities.setInDialog(NewWelcomePanel.mainFrame, this, "Create New Experiment",
                                              new Dimension((int) (dimension.width * 0.43),
                                                      (int) (dimension.height * 0.60)), true, false);
 
         Logger.out.debug("dialog initialized ########## " + dialog);
         dialog.setVisible(true);
         return dialog;
     }
 
     private void addNewExperimentGroupNode(String expGrpName, DefaultMutableTreeNode newNode) {
         ExperimentTreeNode selectedExperimentNode = (ExperimentTreeNode) ((DefaultMutableTreeNode) newNode.getParent()).getUserObject();
 
         ExperimentGroupBusinessInterface expGrpBus = (ExperimentGroupBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                          EjbNamesConstants.EXPERIMENT_GROUP,
                                                                                                                          ExperimentGroupHome.class);
         Long parentExpGrpID = selectedExperimentNode.getIdentifier();
         Logger.out.debug("addNewExperimentGroupNode :: parentExpGrpID : " + parentExpGrpID);
 
         if (expGrpName != null && !expGrpName.equals("")) {
             ExperimentGroup newExpGrp = new ExperimentGroup();
             newExpGrp.setName(expGrpName);
             newExpGrp.setDescription("");
             newExpGrp.setCreatedOn(new Date());
             newExpGrp.setLastUpdatedOn(new Date());
 
             try {
                 newExpGrp = expGrpBus.addExperimentGroup(parentExpGrpID, newExpGrp);
                 Logger.out.debug("returner expGrp id " + newExpGrp.getId());
             } catch (RemoteException e1) {
                 CommonUtils.handleException(e1, this, true, true, false, false);
             } catch (BizLogicException e1) {
                 CommonUtils.handleException(e1, this, true, true, false, false);
             } catch (UserNotAuthorizedException e1) {
                 CommonUtils.handleException(e1, this, true, true, false, false);
             } catch (DAOException e1) {
                 CommonUtils.handleException(e1, this, true, true, false, false);
             }
             // TODO refresh the tree.
             // TODO this is a temporary hack, actually only tree should be
             // refreshed.
             // but this one is updating the whole panel, which is not correct.
             // initGUI();
 
             Logger.out.debug("newExpGrp " + newExpGrp.getId());
 
             ExperimentTreeNode newNodeExperimentTreeNode = new ExperimentTreeNode();
             newNodeExperimentTreeNode.setIdentifier(newExpGrp.getId());
             newNodeExperimentTreeNode.setName(expGrpName);
             newNodeExperimentTreeNode.setExperimentGroup(true);
             newNode.setUserObject(newNodeExperimentTreeNode);
         }
     }
 
     class MyTreeModelListener implements TreeModelListener {
 
         public void treeNodesChanged(TreeModelEvent e) {
             DefaultMutableTreeNode node;
             node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());
 
             /*
              * If the event lists children, then the changed node is the child
              * of the node we've already gotten. Otherwise, the changed node and
              * the specified node are the same.
              */
             try {
                 int index = e.getChildIndices()[0];
                 node = (DefaultMutableTreeNode) (node.getChildAt(index));
             } catch (NullPointerException exc) {
                 Logger.out.error(exc.getMessage());
             }
 
             Logger.out.debug("The user has finished editing the node.");
             Logger.out.debug("New value: " + node.getUserObject());
             addNewExperimentGroupNode(node.getUserObject().toString(), node);
         }
 
         public void treeNodesInserted(TreeModelEvent e) {
         }
 
         public void treeNodesRemoved(TreeModelEvent e) {
         }
 
         public void treeStructureChanged(TreeModelEvent e) {
         }
     }
 
     public static List<DataListMetadata> getDataLlistMetadatas() throws RemoteException,
             DynamicExtensionsSystemException, DAOException, ClassNotFoundException {
 
         DataListBusinessInterface dataListBI = (DataListBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                             EjbNamesConstants.DATALIST_BEAN,
                                                                                                             DataListHomeInterface.class);
 
         List<DataListMetadata> dataListMetadatas = dataListBI.retrieveAllDataListMetadata();
         System.out.println("dataListMetadatas " + dataListMetadatas);
 
         return dataListMetadatas;
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
 
         Logger.configure("");
         WindowUtilities.setNativeLookAndFeel();
 
         ApplicationProperties.initBundle("Cab2bApplicationResources");
 
         NewExperimentDetailsPanel expDetailsPanel = new NewExperimentDetailsPanel();
 
         JFrame frame = new JFrame("New Exp Details -- caB2B");
         frame.setSize(new Dimension(800, 600));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().add(expDetailsPanel);
         frame.setVisible(true);
     }
 }
