 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.ui;
 
 import javax.swing.*;
 
 import org.nees.rpi.vis.loaders.*; //needs datafileloader and delimitertype
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import javax.swing.*;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.MutableTreeNode;
 import java.net.URL;
 import java.util.Enumeration;
 
 import com.sun.org.apache.xml.internal.serialize.OutputFormat;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 
 public class NewFilePanel extends JFrame{
     private JPanel dataImportPanel;
     private File dataFile;
     private JLabel explainText;
     private JLabel theFilename;
     private JComboBox fileEncoding;
     private JScrollPane skipRowsScroll;
     private JComboBox skipRows;
     private JButton selectEncoding;
     private JLabel dataContents;
     private JButton approveFile;
     private DataFileLoader fileLoader;
 
     public NewFilePanel() {
         this.setTitle("Create a new M3dv file");
         dataImportPanel = new JPanel();
         this.setContentPane(dataImportPanel);
         //this.setPreferredSize(new Dimension(640,480));
         SpringLayout spring = new SpringLayout();
 
         dataImportPanel.setLayout(spring);
 
         explainText = new JLabel();
         explainText.setText("Please choose a data file to base this model file on");
         dataImportPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         dataImportPanel.add(explainText);
         //spring.putConstraint(SpringLayout.NORTH, newLinkButton, 5, SpringLayout.SOUTH, lastComponent);
         JButton selectButton = new JButton("Choose File");
         Action newAction = new AbstractAction()
         {
             public void actionPerformed(ActionEvent e)
             {
                 JFileChooser selectDataFile = new JFileChooser();
 
                 Integer fileResult = selectDataFile.showOpenDialog(dataImportPanel);
 
                 if (fileResult == JFileChooser.APPROVE_OPTION) {
                     dataFile = selectDataFile.getSelectedFile();
                     theFilename.setText(dataFile.getName());
                     //fileImportOptionPanel.setVisible(true);
                     //dataImportPanel.repaint();
                     selectEncoding.setVisible(true);
                     fileEncoding.setVisible(true);
                     skipRowsScroll.setVisible(true);
                     dataContents.setVisible(false);
                     approveFile.setVisible(false);
 
 
                 }
 
                 //data_importer.showOpenDialog(this);
                 //MsgWindow.getInstance().showMsg("Creating new model ...");
 
             }
         };
         selectButton.addActionListener(newAction);
         theFilename = new JLabel("No File Selected");
         dataImportPanel.add(selectButton);
         dataImportPanel.add(theFilename);
         spring.putConstraint(SpringLayout.NORTH, selectButton, 10, SpringLayout.SOUTH, explainText);
         spring.putConstraint(SpringLayout.NORTH, theFilename, 10, SpringLayout.SOUTH, selectButton);
         //fileImportOptionPanel = new JPanel();
         //SpringLayout spring2 = new SpringLayout();
         //fileImportOptionPanel.setLayout(spring2);
         String[] fileEncodings = {"Tab","Comma","Other"};
         Integer[] rowsToSkip = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
         fileEncoding = new JComboBox(fileEncodings);
         fileEncoding.setSelectedIndex(0);
         skipRows = new JComboBox(rowsToSkip);
         skipRows.setSelectedIndex(0);
         //fileImportOptionPanel.add(fileEncoding);
         selectEncoding = new JButton("Choose Encoding Type");
         Action encodingAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
 
                 dataContents.setVisible(true);
                 approveFile.setVisible(true);
                 switch(fileEncoding.getSelectedIndex()) {
                     case 0:
                         fileLoader = new DataFileLoader(DelimiterType.TAB_DELIMITED);
                         break;
                     case 1:
                         fileLoader = new DataFileLoader(DelimiterType.COMMA_DELIMITED);
                         break;
                     case 2:
                         fileLoader = new DataFileLoader(DelimiterType.OTHER);
                         break;
                     default:
                         fileLoader = new DataFileLoader(DelimiterType.TAB_DELIMITED);
                         break;
 
                 }
                 fileLoader.setSkipRows(skipRows.getSelectedIndex());
                 try {
                    URL fileURL = new URL("file://"+dataFile.getAbsolutePath());
                    fileLoader.loadFile(fileURL);
                     String[] columnHeaders = fileLoader.getColumnHeaders();
                     String headerString = "";
                     for(int i=0; i<columnHeaders.length; i++) {
                         headerString = headerString + "\n" + columnHeaders[i];
                     }
                     dataContents.setText("datafile contents are - "+headerString + "num" + columnHeaders.length);
                 } catch (java.net.MalformedURLException e2) {
                     dataContents.setText(e2.getMessage());
                 } catch (DataFileLoaderException e2) {
                     dataContents.setText(e2.getMessage());
                 }
 
 
             }
         };
 
         //fileImportOptionPanel.add(selectEncoding);
         //dataImportPanel.add(fileImportOptionPanel);
         skipRowsScroll = new JScrollPane();
         skipRowsScroll.getViewport().setView(skipRows);
 
         dataImportPanel.add(fileEncoding);
         dataImportPanel.add(skipRowsScroll);
         dataImportPanel.add(selectEncoding);
         selectEncoding.addActionListener(encodingAction);
         spring.putConstraint(SpringLayout.NORTH, fileEncoding, 10, SpringLayout.SOUTH, theFilename);
         spring.putConstraint(SpringLayout.NORTH, skipRowsScroll, 10, SpringLayout.SOUTH, fileEncoding);
         spring.putConstraint(SpringLayout.NORTH, selectEncoding, 10, SpringLayout.SOUTH, skipRowsScroll);
 
         fileEncoding.setVisible(false);
         selectEncoding.setVisible(false);
         skipRowsScroll.setVisible(false);
 
         dataContents = new JLabel("Uninterpreted");
         approveFile = new JButton("Create model file");
         dataImportPanel.add(dataContents);
         dataImportPanel.add(approveFile);
         Action approveFileAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e){
                 JFrame designFrame = new DesignM3dvWindow(fileLoader,
                         dataFile.getName(),
                         (String)fileEncoding.getSelectedItem(),
                         skipRows.getSelectedItem().toString());
                 JFrame newFileFrame = (JFrame)(dataImportPanel.getParent()).getParent().getParent();
                 newFileFrame.dispose();
 
             }
         };
         approveFile.addActionListener(approveFileAction);
         spring.putConstraint(SpringLayout.NORTH, dataContents, 10, SpringLayout.SOUTH, selectEncoding);
         spring.putConstraint(SpringLayout.NORTH, approveFile, 10, SpringLayout.SOUTH, dataContents);
 
         dataContents.setVisible(false);
         approveFile.setVisible(false);
         //fileImportOptionPanel.setVisible(false);
 
 
 
         //spring.putConstraint(SpringLayout.WEST, theFilename, 5, SpringLayout.EAST, selectButton);
 
         this.setSize(640,600);
 
 
         this.setVisible(true);
     }
 
 
 }
 
 //This should be exported out later
 class DesignM3dvWindow extends JFrame implements TreeSelectionListener {
     protected JPanel editPane;
     protected JTree editWidget;
     protected JScrollPane editScrollPane;
     protected SensorPanel editingPanel;
     protected BorderLayout borderLayout;
     protected JMenuBar windowMenu;
     protected JMenu fileMenu;
     protected JMenuItem saveItem;
     protected JMenuItem closeItem;
     protected JFileChooser saveOut;
     protected String innerFileName;
     protected String innerDelimiter;
     protected String innerSkipRows;
     public DesignM3dvWindow(DataFileLoader containsFile, String fileName, String delimiter, String skipRows){
         editPane = new JPanel();
         borderLayout = new BorderLayout();
         editScrollPane = new JScrollPane();
         editPane.setLayout(borderLayout);
         windowMenu = new JMenuBar();
         fileMenu = new JMenu("File");
         saveItem = new JMenuItem("Save File");
         saveOut = new JFileChooser();
         innerFileName = fileName;
         innerDelimiter = delimiter;
         innerSkipRows = skipRows;
         Action saveItemAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e){
                 int outVal = saveOut.showSaveDialog(DesignM3dvWindow.this);
                 if(outVal == saveOut.APPROVE_OPTION) {
                     File outputXML = saveOut.getSelectedFile();
                     try {
                         FileOutputStream outputStream = new FileOutputStream(outputXML);
                         OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
                         of.setIndent(1);
                         of.setIndenting(true);
                         //of.setDoctype(null,"users.dtd");
                         XMLSerializer serializer = new XMLSerializer(outputStream, of);
                         ContentHandler hd = serializer.asContentHandler();
                         hd.startDocument();
                         AttributesImpl atts = new AttributesImpl();
                         XmlModel xmlModel = (XmlModel)DesignM3dvWindow.this.editWidget.getModel().getRoot();
                         //atts.addAttribute("","","Title","",(String)xmlModel.getUserObject());
                         atts.clear();
                         hd.startElement("","","model",atts);
 
                         hd.startElement("","","title",atts);
                         String title = (String)xmlModel.getUserObject();
 
 
                         hd.characters(title.toCharArray(),0,title.length());
                         hd.endElement("","","title");
 
                         hd.startElement("","","data-file",atts);
                         hd.startElement("","","file-name",atts);
                         hd.characters(DesignM3dvWindow.this.innerFileName.toCharArray(),
                                 0, DesignM3dvWindow.this.innerFileName.length());
 
                         hd.endElement("","","file-name");
                         hd.startElement("","","delimiter",atts);
                         hd.characters(DesignM3dvWindow.this.innerDelimiter.toCharArray(),
                                 0, DesignM3dvWindow.this.innerDelimiter.length());
 
                         hd.endElement("","","delimiter");
                         hd.startElement("","","skip-rows",atts);
                         hd.characters(DesignM3dvWindow.this.innerSkipRows.toCharArray(),
                                 0, DesignM3dvWindow.this.innerSkipRows.length());
 
                         hd.endElement("","","skip-rows");
                         hd.endElement("","","data-file");
 
 
                         hd.startElement("","","geometries",atts);
 
                         hd.startElement("","","sphere",atts);
 
                         hd.startElement("","","id", atts);
                         String basicName = "sensor-item";
                         hd.characters(basicName.toCharArray(),0,basicName.length());
                         hd.endElement("","","id");
 
                         hd.startElement("","","radius", atts);
                         String xSize = "0.2";
                         hd.characters(xSize.toCharArray(),0,xSize.length());
                         hd.endElement("","","radius");
 
 
 
                         hd.endElement("","","sphere");
 
                         hd.endElement("","","geometries");
 
                         hd.startElement("","","group",atts);
                         hd.startElement("","","name",atts);
                             hd.characters("Sensors".toCharArray(),0,"Sensors".length());
                         hd.endElement("","","name");
                         hd.startElement("","","color",atts);
                             hd.characters("#581363".toCharArray(),0,"#581363".length());
                         hd.endElement("","","color");
 
                         Enumeration shapes = xmlModel.children();
                         DefaultMutableTreeNode ColumnNode;
                         String GeometryID = "sensor-item";
                         SensorNode tempNode;
                         while(shapes.hasMoreElements()) {
                             ColumnNode = (DefaultMutableTreeNode)shapes.nextElement();
                             if (ColumnNode.getChildCount() > 0) {
                                 tempNode = (SensorNode)ColumnNode.getFirstChild();
                                 String SensorName = (String)ColumnNode.getUserObject();
                                 hd.startElement("","","shape",atts);
                                     hd.startElement("","","name",atts);
 
                                         hd.characters(SensorName.toCharArray(),0,SensorName.length());
                                     hd.endElement("","","name");
                                     hd.startElement("","","geometryid",atts);
                                         hd.characters(GeometryID.toCharArray(),0,GeometryID.length());
                                     hd.endElement("","","geometryid");
                                     hd.startElement("","","coordinates",atts);
                                         hd.startElement("","","x",atts);
                                         hd.characters(tempNode.getXpos().toCharArray(),0,tempNode.getXpos().length());
                                         hd.endElement("","","x");
                                         hd.startElement("","","y",atts);
                                         hd.characters(tempNode.getYpos().toCharArray(),0,tempNode.getYpos().length());
                                         hd.endElement("","","y");
                                         hd.startElement("","","z",atts);
                                         hd.characters(tempNode.getZpos().toCharArray(),0,tempNode.getZpos().length());
                                         hd.endElement("","","z");
                                     hd.endElement("","","coordinates");
                                     hd.startElement("","","meta",atts);
                                         hd.startElement("","","name",atts);
                                         hd.characters("Sensor Type".toCharArray(),0,
                                                 "Sensor Type".length());
                                         hd.endElement("","","name");
                                         hd.startElement("","","value",atts);
                                         hd.characters(tempNode.getSensorType().toCharArray(),0,
                                                 tempNode.getSensorType().length());
                                         hd.endElement("","","value");
                                     hd.endElement("","","meta");
                                     hd.startElement("","","series",atts);
                                         hd.startElement("","","x",atts);
                                             hd.characters("time".toCharArray(),0,"time".length());
                                         hd.endElement("","","x");
                                         hd.startElement("","","y",atts);
                                             hd.characters(SensorName.toCharArray(),0,SensorName.length());
                                         hd.endElement("","","y");
 
 
                                     hd.endElement("","","series");
 
 
 
 
 
 
 
                                 hd.endElement("","","shape");
 
                             }
 
 
                         }
 
 
 
 
 
                         hd.endElement("","","group");
 
 
                         hd.endElement("","","model");
 
 
 
                         hd.endDocument();
                         outputStream.close();
 
                     } catch (FileNotFoundException err) {
 
                     } catch (IOException err) {
 
                     } catch (SAXException err) {
 
                     }
                 }
             }
         };
 
         saveItem.addActionListener(saveItemAction);
         closeItem = new JMenuItem("Close Window");
         Action closeItemAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 try {
                     DesignM3dvWindow.this.finalize();
                 } catch (Throwable te) {
 
                 }
             }
         };
         closeItem.addActionListener(closeItemAction);
 
         fileMenu.add(saveItem);
 
         //fileMenu.add(closeItem);
         windowMenu.add(fileMenu);
         this.setJMenuBar(windowMenu);
 
 
 
 
 
         editWidget = new JTree(new XmlModel(containsFile, "Unnamed Model File"));
         //editWidget = new JTree();
         editWidget.setSize(700,500);
         editWidget.addTreeSelectionListener(this);
         editScrollPane.getViewport().setView(editWidget);
 
 
         editScrollPane.setSize(700,400);
 
         editPane.add(editScrollPane, borderLayout.CENTER);
         editingPanel = new SensorPanel();
 
         /*addSensorPanel.setSize(600,200);
         addSensorPanel.setBackground(new Color(180,220,200));
 
         addSensorPanel.add(new JLabel("Add a sensor"));
         SpinnerListModel quick = new SpinnerListModel(containsFile.getColumnHeaders());
         addSensorPanel.add(new JSpinner(quick));
         addSensorPanel.add(new JTextField());
         addSensorPanel.add(new JTextField());
         addSensorPanel.add(new JTextField());
         /*addSensorPanel.add(new JColorChooser());
         addSensorPanel.add(new JButton("Add Node"));*/
         editPane.add(editingPanel, borderLayout.SOUTH);
 
         this.setContentPane(editPane);
         this.setSize(800,600);
         this.setVisible(true);
 
 
 
     }
 
     public void refreshEditingPanel() {
         DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)editWidget.getLastSelectedPathComponent();
         if (selectedNode.isRoot()){
             editingPanel.selectedNode = selectedNode;
             editingPanel.setEditModelNameView();
         } else if (((DefaultMutableTreeNode)selectedNode.getParent()).isRoot()) {
             editingPanel.selectedNode  = selectedNode;
             if (selectedNode.getChildCount()>0) {
                 editingPanel.setEditSensorView();
             } else {
                 editingPanel.setAddSensorView();
             }
 
         } else {
             editingPanel.selectedNode = getDataColumn(selectedNode);
             editingPanel.setEditSensorView();
         }
     }
 
     public void valueChanged(TreeSelectionEvent event){
         refreshEditingPanel();
 
     }
 
 
     private DefaultMutableTreeNode getDataColumn(DefaultMutableTreeNode childNode){
         if (((DefaultMutableTreeNode)childNode.getParent()).isRoot()) {
             return childNode;
         }
         return getDataColumn(((DefaultMutableTreeNode)childNode.getParent()));
     }
 
 
 }
 
 class SensorPanel extends JPanel {
     JLabel descriptiveText;
     static String[] sensorTypeList = {"Pore Pressure Transducer","Strain Gage","Accelerometer","LVDT"};
     JComboBox sensorTypes;
     JLabel xDesc;
     JLabel yDesc;
     JLabel zDesc;
     JTextField xPos;
     JTextField yPos;
     JTextField zPos;
     JTextField modelName;
 
     JButton addSensor;
     JButton removeSensor;
     JButton updateSensor;
     JButton updateModelName;
 
     DefaultMutableTreeNode selectedNode;
     /*SpringLayout spring*/;
     public SensorPanel() {
         descriptiveText = new JLabel();
         sensorTypes = new JComboBox(sensorTypeList);
         xDesc = new JLabel("X");
         yDesc = new JLabel("Y");
         zDesc = new JLabel("Z");
         xPos = new JTextField("0",2);
         yPos = new JTextField("0",2);
         zPos = new JTextField("0",2);
         modelName = new JTextField("",5);
 
 
         addSensor = new JButton("Add Sensor");
         Action addSensorAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e){
                 SensorNode addedSensor = new SensorNode(
                         (String)sensorTypes.getSelectedItem(),
                         xPos.getText(),
                         yPos.getText(),
                         zPos.getText());
                 selectedNode.add(addedSensor);
                 addSensor.getRootPane().repaint();
 
                 refreshView();
 
 
             }
         };
         addSensor.addActionListener(addSensorAction);
 
         removeSensor = new JButton("Remove Sensor");
         Action removeSensorAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e){
                 int deleteNode = JOptionPane.showConfirmDialog(removeSensor,"Do you really want to delete this sensor?");
                 if (deleteNode == JOptionPane.YES_OPTION) {
                     selectedNode.removeAllChildren();
 
                 }
                 removeSensor.getRootPane().repaint();
                 refreshView();
             }
         };
         removeSensor.addActionListener(removeSensorAction);
 
         updateSensor = new JButton("Update sensor");
         Action updateSensorAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e){
                 ((SensorNode)selectedNode.getFirstChild()).updateSensor(
                         (String)sensorTypes.getSelectedItem(),
                         xPos.getText(),
                         yPos.getText(),
                         zPos.getText()
                 );
                 updateSensor.getRootPane().repaint();
                 refreshView();
             }
         };
         updateSensor.addActionListener(updateSensorAction);
 
         updateModelName = new JButton("Update Model Name");
         Action updateModelNameAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e){
                 selectedNode.setUserObject(modelName.getText());
                 modelName.getRootPane().repaint();
                 refreshView();
 
             }
         };
         updateModelName.addActionListener(updateModelNameAction);
         /*spring = new SpringLayout();
         this.setLayout(spring);*/
         this.add(descriptiveText);
         this.add(sensorTypes);
         this.add(xDesc);
         this.add(xPos);
         this.add(yDesc);
         this.add(yPos);
         this.add(zDesc);
         this.add(zPos);
         this.add(modelName);
         this.add(removeSensor);
         this.add(updateSensor);
         this.add(addSensor);
         this.add(updateModelName);
 
         /*removeSensor.setVisible(false);*/
         this.setSize(600,200);
         /*this.setBackground();*/
         this.setNoSelectedComponentView();
 
 
     }
     public void refreshView() {
 
         ((DesignM3dvWindow)SensorPanel.this.getRootPane().getParent()).refreshEditingPanel();
         ((DesignM3dvWindow)SensorPanel.this.getRootPane().getParent()).editWidget.repaint();
 
     }
 
 
     public void setAllInvisible() {
         Component[] componentList = this.getComponents();
         for(int c=0;c<componentList.length;c++) {
             componentList[c].setVisible(false);
         }
     }
     public void setNoSelectedComponentView() {
         setAllInvisible();
         descriptiveText.setText("Select a data column to edit");
         descriptiveText.setVisible(true);
     }
 
     public void setEditModelNameView() {
         setAllInvisible();
         descriptiveText.setText("Update the model name");
         descriptiveText.setVisible(true);
         modelName.setText((String)selectedNode.getUserObject());
         modelName.setVisible(true);
         updateModelName.setVisible(true);
 
     }
 
     public void setSensorView(String descText,
                               String sensorTypeValue,
                               String xPosValue,
                               String yPosValue,
                               String zPosValue) {
         setAllInvisible();
         descriptiveText.setText(descText);
         descriptiveText.setVisible(true);
         sensorTypes.setSelectedItem(sensorTypeValue);
         sensorTypes.setVisible(true);
         xDesc.setVisible(true);
         xPos.setText(xPosValue);
         xPos.setVisible(true);
         yDesc.setVisible(true);
         yPos.setText(yPosValue);
         yPos.setVisible(true);
         zDesc.setVisible(true);
         zPos.setText(zPosValue);
         zPos.setVisible(true);
 
 
 
     }
 
     public void setAddSensorView() {
         setSensorView("Choose Type",sensorTypeList[0], "0.0", "0.0", "0.0");
         addSensor.setVisible(true);
 
 
     }
 
     public void setEditSensorView() {
         /*setSensorView("Change Type", (String)(selectedNode.getUserObject()),
                 ((SensorNode)selectedNode).getXpos(),((SensorNode)selectedNode).getYpos(),
                 ((SensorNode)selectedNode).getXpos());*/
         setSensorView("Change Sensor",((SensorNode)selectedNode.getFirstChild()).getSensorType(),
                 ((SensorNode)selectedNode.getFirstChild()).getXpos(),
                 ((SensorNode)selectedNode.getFirstChild()).getYpos(),
                 ((SensorNode)selectedNode.getFirstChild()).getZpos());
         updateSensor.setVisible(true);
         removeSensor.setVisible(true);
 
     }
 
     public void modifyRootNode(DefaultMutableTreeNode rootNode) {
         if (rootNode.isRoot()){
             rootNode.setUserObject(modelName.getText());
         }
     }
 
     public void modifySensorNode(DefaultMutableTreeNode dataColumn) {
         if (dataColumn.getChildCount() > 0){
             removeSensor.setVisible(true);
             updateSensor.setText("Update sensor");
 
         } else {
             removeSensor.setVisible(false);
             updateSensor.setText("Add sensor");
             sensorTypes.setSelectedIndex(0);
             xPos.setText("0");
             yPos.setText("0");
             zPos.setText("0");
         }
     }
 
 
 }
 
 class XmlModel extends DefaultMutableTreeNode
 {
     //public static Integer
     public DefaultMutableTreeNode geometries;
     public XmlModel(String ModelName) {
         super(ModelName);
         geometries = new DefaultMutableTreeNode("geometries");
         this.add(geometries);
     }
     public XmlModel(DataFileLoader sensorData, String ModelName) {
         super(ModelName);
         String[] sensors = sensorData.getColumnHeaders();
         DefaultMutableTreeNode dataColumn;
         for(int s=0; s<sensors.length; s++ ) {
             dataColumn = new DefaultMutableTreeNode(sensors[s]);
             this.add((DefaultMutableTreeNode)dataColumn.clone());
         }
 
 
     }
 /*    public void addSensor(String sensorName, String columnHeading,
                           Integer xPos, Integer yPos, Integer zPos,
                           String sensorColor) {
         DefaultMutableTreeNode temporaryNode = new DefaultMutableTreeNode("shape");
 
 
         DefaultMutableTreeNode idNode = new DefaultMutableTreeNode("id");
         DefaultMutableTreeNode idNodeValue = new DefaultMutableTreeNode(columnHeading);
         idNode.add(idNodeValue);
         temporaryNode.add(idNode);
 
         DefaultMutableTreeNode shapeType = new DefaultMutableTreeNode("sphere");
         DefaultMutableTreeNode shapeSize = new DefaultMutableTreeNode("radius");
         DefaultMutableTreeNode shapeSizeValue = new DefaultMutableTreeNode(new Double(0.5));
 
         shapeSize.add(shapeSizeValue);
         shapeType.add(shapeSize);
 
         temporaryNode.add(shapeType);
 
         DefaultMutableTreeNode coordinates = new DefaultMutableTreeNode("coordinates");
 
         DefaultMutableTreeNode positionX = new DefaultMutableTreeNode("x");
         DefaultMutableTreeNode positionXvalue = new DefaultMutableTreeNode(xPos);
         positionX.add(positionXvalue);
         DefaultMutableTreeNode positionY = new DefaultMutableTreeNode("y");
         DefaultMutableTreeNode positionYvalue = new DefaultMutableTreeNode(yPos);
         positionY.add(positionYvalue);
         DefaultMutableTreeNode positionZ = new DefaultMutableTreeNode("z");
         DefaultMutableTreeNode positionZvalue = new DefaultMutableTreeNode(zPos);
         positionZ.add(positionZvalue);
 
         coordinates.add(positionX);
         coordinates.add(positionY);
         coordinates.add(positionZ);
 
         temporaryNode.add(coordinates);
 
         DefaultMutableTreeNode colorNode = new DefaultMutableTreeNode("color");
         DefaultMutableTreeNode colorNodeValue = new DefaultMutableTreeNode(sensorColor);
 
         colorNode.add(colorNodeValue);
         temporaryNode.add(colorNode);
 
 
         this.geometries.add(temporaryNode);
 
 
 
     }*/
 }
 
 //class DataColumn extends DefaultMutableTreeNode {
 //    public DataColumn(String columnName) {
 //        super(columnName);
 //
 //
 //    }
 //}
 
 class SensorNode extends DefaultMutableTreeNode {
     /*public DefaultMutableTreeNode sensorType;*/
     public DefaultMutableTreeNode xPos;
     public DefaultMutableTreeNode yPos;
     public DefaultMutableTreeNode zPos;
     public SensorNode(String sensorTypeValue, String xPosValue, String yPosValue, String zPosValue) {
         super(sensorTypeValue);
         /*DefaultMutableTreeNode id = new DefaultMutableTreeNode("id");
         sensorType = new DefaultMutableTreeNode(sensorTypeValue);
         id.add(sensorType);
         this.add(id);*/
 
         DefaultMutableTreeNode coordinates = new DefaultMutableTreeNode("coordinates");
         DefaultMutableTreeNode x = new DefaultMutableTreeNode("x");
         xPos = new DefaultMutableTreeNode(xPosValue);
         x.add(xPos);
         coordinates.add(x);
         DefaultMutableTreeNode y = new DefaultMutableTreeNode("y");
         yPos = new DefaultMutableTreeNode(yPosValue);
         y.add(yPos);
         coordinates.add(y);
         DefaultMutableTreeNode z = new DefaultMutableTreeNode("z");
         zPos = new DefaultMutableTreeNode(zPosValue);
         z.add(zPos);
         coordinates.add(z);
 
         this.add(coordinates);
 
     }
 
     public void updateSensor(String sensorTypeValue, String xPosValue, String yPosValue, String zPosValue) {
         this.setUserObject(sensorTypeValue);
         xPos.setUserObject(xPosValue);
         yPos.setUserObject(yPosValue);
         zPos.setUserObject(zPosValue);
     }
 
     public String getSensorType() {
         return (String)this.getUserObject();
     }
     public String getXpos() {
         return (String)xPos.getUserObject();
     }
     public String getYpos() {
         return (String)yPos.getUserObject();
     }
     public String getZpos() {
         return (String)zPos.getUserObject();
     }
 
 }
