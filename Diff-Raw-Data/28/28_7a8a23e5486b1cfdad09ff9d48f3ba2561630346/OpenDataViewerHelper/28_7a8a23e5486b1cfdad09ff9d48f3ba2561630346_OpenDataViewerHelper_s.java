 package gov.nih.nci.caadapter.ui.mapping.sdtm.actions;
 
 import gov.nih.nci.caadapter.common.util.EmptyStringTokenizer;
 import gov.nih.nci.caadapter.dataviewer.MainDataViewerFrame;
 import gov.nih.nci.caadapter.dataviewer.util.QBParseMappingFile;
 import gov.nih.nci.caadapter.ui.common.CaadapterFileFilter;
 import gov.nih.nci.caadapter.ui.mapping.sdtm.DBConnector;
 import gov.nih.nci.caadapter.ui.mapping.sdtm.Database2SDTMMappingPanel;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 /**
  * Created by IntelliJ IDEA.
  * User: hjayanna
  * Date: Jul 25, 2007
  * Time: 3:18:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class OpenDataViewerHelper extends JDialog implements ActionListener {
     Frame _mainFrame=null;
     JFileChooser mapLocation=null;
     JTextField mapTextField=null;
     File mapFileObj=null;
     String mapFileStr=null;
     File mapFile=null;
     Database2SDTMMappingPanel panel=null;
     JButton transformBut=null;
 
     public OpenDataViewerHelper(Frame owner, Database2SDTMMappingPanel _panel, File _mapFile, JButton _transformBut) {
         super(owner, true);
         this._mainFrame = owner;
         this.panel = _panel;
         this.transformBut = _transformBut;
         if (_mapFile != null)
             this.mapFile = _mapFile;
         JPanel masterPanel = new JPanel();
         masterPanel.setLayout(new BorderLayout());
         masterPanel.add(createRow(), BorderLayout.CENTER);
         masterPanel.add(createButRow(), BorderLayout.SOUTH);
         add(masterPanel);
         // set dialog properties;
         setTitle("Enter the Map File to open the Data Viewer");
         setResizable(false);
         setSize(400, 110);
         setLocation(400, 400);
         setVisible(true);
     }
 
     private JPanel createRow() {
         JPanel pan = new JPanel();
         pan.setLayout(new GridLayout(1, 3));
         JLabel label = new JLabel("Enter Map file for Data Viewer");
         if (mapFile != null) {
             mapTextField = new JTextField(mapFile.getAbsolutePath());
             mapTextField.setEditable(false);
         } else {
             mapTextField = new JTextField();
         }
         JButton button = new JButton("Browse");
         button.setEnabled(false);
         button.addActionListener(this);
         pan.add(label);
         pan.add(mapTextField);
         pan.add(button);
         pan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Open Data Viewer"));
         return pan;
     }
 
     private JPanel createButRow() {
         JPanel butPan = new JPanel();
         butPan.setLayout(new FlowLayout());
         JButton okBut = new JButton("OK");
         okBut.addActionListener(this);
         butPan.add(okBut);
         JButton canBut = new JButton("Cancel");
         butPan.add(canBut);
         canBut.addActionListener(this);
         butPan.setBorder(BorderFactory.createLineBorder(Color.black));
         return butPan;
     }
 
     public void actionPerformed(ActionEvent e) {
         String cmd = e.getActionCommand();
         if (cmd.equalsIgnoreCase("Browse")) {
             CaadapterFileFilter filter = new CaadapterFileFilter();
             filter.addExtension("map");
             filter.setDescription("map");
             mapLocation = new JFileChooser(System.getProperty("user.home"));
             mapLocation.setFileFilter(filter);
             int returnVal = mapLocation.showSaveDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 mapFileObj = mapLocation.getSelectedFile();
                 // This is where a real application would open the file.
                 if (mapFileObj.getAbsolutePath().endsWith("map")) {
                     mapFileStr = mapFileObj.getAbsolutePath();
                 } else {
                     mapFileStr = mapFileObj.getAbsolutePath() + ".map";
                 }
                 mapTextField.setText(mapFileStr);
                 mapTextField.setEnabled(false);
             }
         } else if (cmd.equalsIgnoreCase("OK")) {
             if (mapFile != null) {
                 OpenQueryBuilder((Hashtable) getMappingsFromMapFile(mapFile).get(0), (HashSet) getMappingsFromMapFile(mapFile).get(1), mapFile, readMapFile(mapFile.getAbsolutePath()), (Hashtable) getMappingsFromMapFile(mapFile).get(2));
             } else {
                 OpenQueryBuilder((Hashtable) getMappingsFromMapFile(mapFileObj).get(0), (HashSet) getMappingsFromMapFile(mapFileObj).get(1), mapFileObj, readMapFile(mapFileStr), (Hashtable) getMappingsFromMapFile(mapFileObj).get(2));
             }
             this.dispose();
         } else if (cmd.equalsIgnoreCase("Cancel")) {
             this.dispose();
         }
     }
 
     public void OpenQueryBuilder(final Hashtable list, final HashSet cols, final File file, final String out, final Hashtable sqlHashtable) {
         final Dialog d = new Dialog(_mainFrame, "SQL Query", true);
         (new Thread() {
             public void run() {
                 try {
                     if (panel.getConnectionParameters() != null) {
                        new MainDataViewerFrame(panel, d, list, cols, panel.getConnectionParameters(), file, out, sqlHashtable, transformBut);
                     }
                 } catch (Exception e) {
                     try {
                         Hashtable collectParams = getConnectionParametersfromUI(_mainFrame, file);
                         if (collectParams != null)
                            new MainDataViewerFrame(panel, d, list, cols, collectParams, file, out, sqlHashtable, transformBut);
                     } catch (Exception e1) {
                         e1.printStackTrace();
                     }
                 }
                 d.dispose();
             }
         }).start();
         JPanel pane = new JPanel();
         TitledBorder _title = BorderFactory.createTitledBorder("Visual SQL Builder");
         pane.setBorder(_title);
         pane.setLayout(new GridLayout(0, 1));
         JLabel _jl = new JLabel("SQL Query Builder Loading , please wait.....");
         pane.add(_jl);
         d.add(pane, BorderLayout.CENTER);
         d.setLocation(400, 400);
         d.setSize(500, 130);
         d.setVisible(true);
     }
 
     private Hashtable getConnectionParametersfromUI(Frame _mainFrame, File file) throws Exception {
         Hashtable connectionParameters = null;
         String dbParams = getConnectionParamsFromMapFile(file);
         if (dbParams != null) {
             QBGetPasswordWindow getPass = new QBGetPasswordWindow(_mainFrame, dbParams, file.getAbsolutePath());
             String pass = getPass.getPassword();
             EmptyStringTokenizer empt = new EmptyStringTokenizer(dbParams, "~");
             connectionParameters = new Hashtable();
             connectionParameters.put("URL", empt.getTokenAt(0));
             connectionParameters.put("UserID", empt.getTokenAt(2));
             connectionParameters.put("PWD", pass);
             connectionParameters.put("SCHEMA", empt.getTokenAt(3));
             connectionParameters.put("Driver", empt.getTokenAt(1));
             try {
                 connectionParameters.put("connection", DBConnector.getDBConnection(empt.getTokenAt(0), empt.getTokenAt(1), empt.getTokenAt(2), pass));
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(_mainFrame, e.getMessage().toString(), "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
         return connectionParameters;
     }
 
     private String readMapFile(String mapFile) {
         StringBuffer strBuf = new StringBuffer();
         try {
             BufferedReader in = new BufferedReader(new FileReader(mapFile));
             String str;
             while ((str = in.readLine()) != null) {
                 if (!str.equalsIgnoreCase("</mapping>"))
                     strBuf.append(str + "\n");
             }
             in.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return strBuf.toString();
     }
 
     public ArrayList getMappingsFromMapFile(File mapFile) {
         ArrayList retAry = new ArrayList();
         QBParseMappingFile _qbparse = new QBParseMappingFile(mapFile);
         _qbparse.parseFile();
         retAry.add(_qbparse.getHashTable());
         retAry.add(_qbparse.getHashTblColumns());
         retAry.add(_qbparse.getHashSQLfromMappings());
         return retAry;
     }
 
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (UnsupportedLookAndFeelException e) {
             e.printStackTrace();
         }
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 new OpenDataViewerHelper(null, null, null, null);
             }
         });
     }
 
     private String getConnectionParamsFromMapFile(File mapFile) throws Exception {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         Document doc = docBuilder.parse(mapFile);
         System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());
         NodeList compLinkNodeList = doc.getElementsByTagName("components");
         String _xmlFileName = "", _scsFileName, _dbParams = null;
         for (int s = 0; s < compLinkNodeList.getLength(); s++) {
             Node node = compLinkNodeList.item(s);
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                 Element firstCompElement = (Element) node;
                 NodeList targetNode = firstCompElement.getElementsByTagName("component");
                 Element targetName1 = (Element) targetNode.item(0);
                 targetName1.getAttribute("location").toString();
                 if (targetName1.getAttribute("kind").toString().equalsIgnoreCase("SCS")) {
                     _scsFileName = targetName1.getAttribute("location").toString();
                 } else if (targetName1.getAttribute("kind").toString().equalsIgnoreCase("Database")) {
                     _dbParams = targetName1.getAttribute("param").toString();
                 }
                 Element targetName2 = (Element) targetNode.item(1);
                 if (targetName2.getAttribute("kind").toString().equalsIgnoreCase("XML")) {
                     _xmlFileName = targetName2.getAttribute("location").toString();
                 }
             }
         }
         return _dbParams;
     }
 }
