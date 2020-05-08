 package easyzk.view;
 
 import easyzk.exception.EasyZkException;
 import easyzk.zk.RequestHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class NodeDataView extends JPanel {
 
     private static final Logger logger = LoggerFactory.getLogger(NodeDataView.class);
 
     private static final String NODE_LABEL = "Name/Path";
 
     private static final String VERSION_LABEL = "Version";
 
     private static final String CREATED_LABEL = "Created on";
 
     private static final String DATA_LABEL = "Data";
 
     private static final String ADD_COMMAND = "Add";
 
     private static final String DELETE_COMMAND = "Delete";
 
     private static final String UPDATE_COMMAND = "Update";
 
     private static final String MOVE_COMMAND = "Move";
 
     private static final JLabel nodePathLabel = new JLabel(NODE_LABEL);
 
     private static final JLabel nodeVersionLabel = new JLabel(VERSION_LABEL);
 
     private static final JLabel nodeCreatedLabel = new JLabel(CREATED_LABEL);
 
     private static final JLabel nodeDataLabel = new JLabel(DATA_LABEL);
 
     private static final JTextField nodePathField = new JTextField();
 
     private static final JTextField nodeVersionField = new JTextField();
 
     private static final JTextField nodeCreatedField = new JTextField();
 
    private static final JTextArea nodeDataField = new JTextArea();
 
     private static final JScrollPane dataScrollPane = new JScrollPane(nodeDataField);
 
     private static final JButton addButton = new JButton(ADD_COMMAND);
 
     private static final JButton deleteButton = new JButton(DELETE_COMMAND);
 
     private static final JButton updateButton = new JButton(UPDATE_COMMAND);
 
     private static final JButton moveButton = new JButton(MOVE_COMMAND);
 
     private static final RequestHandler requestHandler = new RequestHandler();
 
     private final EasyZk easyZk;
 
     public NodeDataView(EasyZk easyZk) {
         this.easyZk = easyZk;
         Font font = new Font("Serif", Font.BOLD, 15);
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        nodeDataField.setPreferredSize(new Dimension(20, 400));
         setBorder(BorderFactory.createTitledBorder("Node details"));
         JPanel dataPanel = new JPanel();
 
         GridBagLayout layout = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         c.weightx = 0.1;
         c.ipady = 15;
         dataPanel.setLayout(layout);
         nodePathField.setEditable(false);
         nodeVersionField.setEditable(false);
         nodeCreatedField.setEditable(false);
         nodePathLabel.setFont(font);
         dataPanel.add(nodePathLabel, c);
         c.weightx = 0.7;
 
         c.gridx = 1;
         c.gridy = 0;
         dataPanel.add(nodePathField, c);
         c.weightx = 0.1;
         c.insets = new Insets(15, 0, 0, 0);
         c.gridx = 0;
         c.gridy = 1;
         nodeVersionLabel.setFont(font);
         dataPanel.add(nodeVersionLabel, c);
         c.gridx = 1;
         c.gridy = 1;
         c.weightx = 0.7;
         dataPanel.add(nodeVersionField, c);
         c.gridx = 0;
         c.gridy = 2;
         c.weightx = 0.1;
         nodeCreatedLabel.setFont(font);
         dataPanel.add(nodeCreatedLabel, c);
         c.gridx = 1;
         c.gridy = 2;
         c.weightx = 0.7;
         dataPanel.add(nodeCreatedField, c);
         c.gridx = 0;
         c.gridy = 3;
         c.weightx = 0.1;
         nodeDataLabel.setFont(font);
         dataPanel.add(nodeDataLabel, c);
         c.gridx = 1;
         c.gridy = 3;
         c.weightx = 0.7;
         dataPanel.add(dataScrollPane, c);
 
         add(dataPanel);
 
         JPanel buttonPanel = new JPanel();
         ButtonListener listener = new ButtonListener();
         addButton.addActionListener(listener);
         deleteButton.addActionListener(listener);
         updateButton.addActionListener(listener);
         moveButton.addActionListener(listener);
 
         buttonPanel.add(addButton);
         buttonPanel.add(deleteButton);
         buttonPanel.add(updateButton);
         buttonPanel.add(moveButton);
 
         add(buttonPanel);
 
         disableButtons();
     }
 
     public void setNodeName(String nodeName) {
         nodePathField.setText(nodeName);
         if(nodeName.equals("/"))setUneditable();
         else enableButtons();
     }
 
     public void setNodeVersion(String version) {
         nodeVersionField.setText(version);
     }
 
     public void setNodeCreated(String nodeCreated) {
         nodeCreatedField.setText(nodeCreated);
     }
 
     public void setData(String data) {
         nodeDataField.setText(data);
     }
 
     public String getNodeName() {
         return nodePathField.getText();
     }
 
     public String getData() {
         return nodeDataField.getText();
     }
 
     public void disableButtons() {
         addButton.setEnabled(false);
         deleteButton.setEnabled(false);
         updateButton.setEnabled(false);
         moveButton.setEnabled(false);
     }
 
     public void enableButtons() {
         addButton.setEnabled(true);
         deleteButton.setEnabled(true);
         updateButton.setEnabled(true);
         moveButton.setEnabled(true);
     }
 
     private void setUneditable() {
         deleteButton.setEnabled(false);
         moveButton.setEnabled(false);
         updateButton.setEnabled(false);
         addButton.setEnabled(true);
     }
 
     private class ButtonListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
 
             String action = actionEvent.getActionCommand();
             try {
                 if (action.equalsIgnoreCase(ADD_COMMAND)) {
                     processAdd();
 //                    easyZk.notifyNodeAdded();
                 } else if (action.equalsIgnoreCase(DELETE_COMMAND)) {
                     processDelete();
                 } else if (action.equalsIgnoreCase(MOVE_COMMAND)) {
                     processMove();
                 } else if (action.equalsIgnoreCase(UPDATE_COMMAND)) {
                     processUpdate();
                 }
             } catch (EasyZkException e) {
                 logger.error("Error ", e);
                 ViewUtility.handleException(easyZk, e);
             } catch(Exception e){
                 logger.error("Error", e);
                 ViewUtility.handleException(easyZk, e);
             }
 
         }
 
         private void processAdd() throws EasyZkException {
             logger.debug("Add process");
 
             JPanel addNodePanel = new JPanel();
 
             GridBagLayout layout = new GridBagLayout();
             addNodePanel.setLayout(layout);
 
             GridBagConstraints c = new GridBagConstraints();
             c.fill = GridBagConstraints.HORIZONTAL;
             c.gridx = 0;
             c.gridy = 0;
             c.weightx = 0.1;
 
             JLabel nameLabel = new JLabel("Name");
             addNodePanel.add(nameLabel, c);
             c.gridx = 1;
             c.gridy = 0;
             c.weightx = 0.7;
             JTextField nameField = new JTextField(20);
             addNodePanel.add(nameField,c);
 
             JLabel dataLabel = new JLabel("Data");
             c.ipady = 15;
             c.gridx = 0;
             c.gridy = 1;
             c.weightx = 0.1;
             addNodePanel.add(dataLabel, c);
 
             JTextArea dataArea = new JTextArea();
             c.gridx = 1;
             c.gridy = 1;
             c.weightx = 0.7;
             addNodePanel.add(dataArea, c);
             dataArea.setPreferredSize(new Dimension(20, 100));
 
             int ret = JOptionPane.showConfirmDialog(
                     easyZk,
                     addNodePanel,
                     "Add Node",
                     JOptionPane.OK_CANCEL_OPTION,
                     JOptionPane.PLAIN_MESSAGE);
             if (ret == JOptionPane.OK_OPTION) {
                 String node = nameField.getText();
                 String data = dataArea.getText();
                 logger.info("Adding node {} to {}", node, nodePathField.getText());
                 logger.info("Data is {} ", data);
                 requestHandler.addNode(easyZk.getCurrentCluster(), nodePathField.getText(), node, data);
             }
         }
     }
 
     private void processDelete() throws EasyZkException {
         logger.debug("Delete process");
         requestHandler.deleteNode(easyZk.getCurrentCluster(), nodePathField.getText());
     }
 
     private void processMove() throws EasyZkException {
         logger.debug("Move process");
         String destination = JOptionPane.showInputDialog(easyZk, "Input destination node");
         requestHandler.moveNode(easyZk.getCurrentCluster(), nodePathField.getText(), destination);
     }
 
     private void processUpdate() throws EasyZkException {
         logger.debug("Process process");
         String node = nodePathField.getText();
         String data = nodeDataField.getText();
         requestHandler.updateNode(easyZk.getCurrentCluster(), node, data);
     }
 }
