 // TabbedBrowser
 
 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  ** 
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute of Systems Biology and the Whitehead Institute 
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute of Systems Biology and the Whitehead Institute 
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute of Systems Biology and the Whitehead Institute 
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  ** 
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 //---------------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //---------------------------------------------------------------------------------------
 package cytoscape.browsers;
 //---------------------------------------------------------------------------------------
 import javax.swing.*;
 import javax.swing.table.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.net.URL;
 import javax.jnlp.*;
 import java.util.*;
 import java.io.*;
 import java.awt.datatransfer.*;
 
 import cytoscape.GraphObjAttributes;
 import cytoscape.util.*;
 
 import y.base.*;
 //---------------------------------------------------------------------------------------
 /**
  * create a table in a tab for every attribute; each table has 2 columns:  canonical name,
  * common name, attribute. note that canonical and common names are, strictly speaking,
  * themselves attributes, but since they appear in every tabbed table, they do not
  * get their own tabbed table.  furthermore, cytoscape.props may contain a list of
  * other attribute categories to ignore.
  *
  * @see cytoscape.GraphObjAttributes#setCategory(String,String)
  *
  */
 public class TabbedBrowser extends JFrame implements ClipboardOwner {
   protected Object [] graphObjects;              // nodes or edges
   protected GraphObjAttributes attributes;
   protected Vector attributeCategoriesToIgnore;
   protected int preferredTableWidth = 600;
   protected int preferredTableHeight = 100;
   protected String webBrowserScript;
   protected JTabbedPane tabbedPane;
   protected Vector customAttributesList;
   protected JTextField customTabNameTextField;
   protected File currentDirectory;
 
   static HashMap customNodeAttributes = new HashMap ();
   static HashMap customEdgeAttributes = new HashMap ();
   HashMap customAttributes; // a copy of one of the previous two that we use on this invocation
 
   int browseObjectClass;
   static final int BROWSING_UNRECOGNIZED_OBJECTS = 0;
   static final int BROWSING_NODES = 1;
   static final int BROWSING_EDGES = 2;
 
 //---------------------------------------------------------------------------------------
 public TabbedBrowser (Object [] graphObjects, GraphObjAttributes attributes, 
                       Vector attributeCategoriesToIgnore,
                       String webBrowserScript)
 {
   super ();
   this.graphObjects = graphObjects;
   this.attributes = attributes;
   this.attributeCategoriesToIgnore = attributeCategoriesToIgnore;
   this.webBrowserScript = webBrowserScript;
 
   browseObjectClass = detectClassOfBrowsedObjects (graphObjects);
   if (browseObjectClass == BROWSING_NODES) {
     customAttributes = customNodeAttributes;
     setTitle ("Node Browser");
     }
   else if (browseObjectClass == BROWSING_EDGES) {
     setTitle ("Edge Browser");
     customAttributes = customEdgeAttributes;
     }
 
   else {
     setTitle ("Browser of unrecognized objects");
     customAttributes = new HashMap ();
     }
 
   String [] attributeNames = attributes.getAttributeNames ();
 
   getContentPane().setLayout (new BorderLayout ());
   getContentPane().add (createGui (attributes), BorderLayout.CENTER);
   getContentPane().add (createButtons (), BorderLayout.SOUTH);
   pack ();
   placeInCenter ();
   setVisible (true);
 }
 //---------------------------------------------------------------------------------------
 JTabbedPane createGui (GraphObjAttributes attributes)
 {
   tabbedPane = new JTabbedPane ();
   String [] attributeNames = attributes.getAttributeNames ();
   
   tabbedPane.add ("Customize", createCustomizerTab (attributeNames));  
 
   for (int i=0; i < attributeNames.length; i++) {
     String attributeName = attributeNames [i];
     //System.out.println("attribute name = " + attributeName);
     if (attributeName.equalsIgnoreCase ("commonName"))
       continue;
     String attributeCategory = attributes.getCategory (attributeName);
     //System.out.println("attribute category = " + attributeCategory);
     if (attributeCategoriesToIgnore.contains (attributeCategory)){
       //System.out.println("Ignoring attribute =" + attributeCategory);
       continue;
     }
     String [] requestedAttibuteNames = {attributeName};
     BrowserTableModel model = new BrowserTableModel (graphObjects, attributes, requestedAttibuteNames);
     JTable table = new JTable (model);
     setPreferredColumnWidths (table);    
     table.setCellSelectionEnabled (true);
     table.addMouseListener (new MyMouseListener (table));
     table.setPreferredScrollableViewportSize (new Dimension (preferredTableWidth, preferredTableHeight));
     JScrollPane scrollPane = new JScrollPane (table);
     tabbedPane.add (attributeNames [i], scrollPane);
     } // for i
 
    
   String [] customTabNames = (String []) customAttributes.keySet().toArray (new String [0]);
 
   for (int i=0; i < customTabNames.length; i++) {
     String tabName = customTabNames [i];
     Vector attributesInThisTab = (Vector) customAttributes.get (tabName);
     createNewPanel (attributesInThisTab, tabName);
     }
 
   return tabbedPane;
 
 } // createGui
 //------------------------------------------------------------------------------
 protected int detectClassOfBrowsedObjects (Object [] graphObjects)
 {
  String graphObjectClassName = graphObjects [0].getClass().getName();

  if (graphObjectClassName.indexOf ("y.view.a") >= 0)
   return BROWSING_NODES;

  else if (graphObjectClassName.indexOf ("y.view.f") >= 0)
    return BROWSING_EDGES;

  else 
    return BROWSING_UNRECOGNIZED_OBJECTS;
 
 } // detectClassOfBrowsedObjects
 //------------------------------------------------------------------------------
 protected JPanel createCustomizerTab (String [] attributeNames)
 {
   JPanel panel = new JPanel ();
   panel.setBorder (BorderFactory.createCompoundBorder (
                    BorderFactory.createTitledBorder(
                   "Select attributes for custom view"),
                    BorderFactory.createEmptyBorder(20,20,20,20)));
   JPanel centerPanel = new JPanel ();
   JPanel checkBoxButtonPanel = new JPanel ();
   JPanel actionButtonPanel = new JPanel ();
 
   panel.setLayout (new BorderLayout ());
   centerPanel.setLayout (new BorderLayout ());
 
   JButton createNewPanelButton = new JButton ("Create");
   actionButtonPanel.add (createNewPanelButton);
   createNewPanelButton.addActionListener (new CreateNewPanelAction ());
 
   int count = attributeNames.length;
   int columnCount = 1;
   if (count > 8)
     columnCount = 2;
   checkBoxButtonPanel.setLayout (new GridLayout (0, columnCount));
 
   customAttributesList = new Vector ();
   for (int i=0; i < attributeNames.length; i++) {
     if (attributeNames [i].equals ("commonName")) continue;
     String attributeName = attributeNames [i];
     String attributeCategory = attributes.getCategory(attributeName);
     if (attributeCategoriesToIgnore.contains (attributeCategory)) continue;
     JCheckBox button = new JCheckBox (attributeNames [i]);
     checkBoxButtonPanel.add (button);
     button.addItemListener (new CheckBoxListener (attributeNames [i]));
     } // for i
   JCheckBox button = new JCheckBox ("canonicalName");
   checkBoxButtonPanel.add (button);
   button.addItemListener (new CheckBoxListener ("canonicalName"));
 
 
   JPanel textFieldPanel = new JPanel ();
   textFieldPanel.add (new JLabel ("Name for new tab: "));
   customTabNameTextField = new JTextField (10);
   textFieldPanel.add (customTabNameTextField, BorderLayout.SOUTH);
   centerPanel.add (textFieldPanel, BorderLayout.SOUTH);
   centerPanel.add (checkBoxButtonPanel, BorderLayout.CENTER);
   panel.add (centerPanel, BorderLayout.CENTER);
   panel.add (actionButtonPanel, BorderLayout.SOUTH);
 
   return panel;
 
 } // createCustomizerTab
 //-----------------------------------------------------------------------------------
 class CheckBoxListener implements ItemListener {
   String attributeName;
   CheckBoxListener (String attributeName) {
     this.attributeName = attributeName;
     }
   public void itemStateChanged (ItemEvent e) {
    int state = e.getStateChange ();
    if (state == ItemEvent.SELECTED) {
      if (!customAttributesList.contains (attributeName))
        customAttributesList.add (attributeName);
      }
    else if (state == ItemEvent.DESELECTED) {
      if (customAttributesList.contains (attributeName))
        customAttributesList.remove (attributeName);
      }
    }
 
 } // inner Class CheckBoxListner
 //-----------------------------------------------------------------------------------
 JPanel createButtons ()
 {
   JPanel panel = new JPanel ();
   JButton saveButton = new JButton("Save Table");
   JButton dismissButton = new JButton ("Dismiss");
   saveButton.addActionListener (new SaveTableAction (this));
   dismissButton.addActionListener (new DismissAction (this));
   panel.add (saveButton, BorderLayout.CENTER);
   panel.add (dismissButton, BorderLayout.CENTER);
   return panel;
 
 } // createButtons
 
 //------------------------------------------------------------------------------
 public class CreateNewPanelAction extends AbstractAction {
 
   public void actionPerformed (ActionEvent e) {
     if (customAttributesList == null || customAttributesList.size () == 0)
       return;
     String title = customTabNameTextField.getText ();
     if (title == null) title = "";
     // System.out.println ("storing custom tab:  " + title + ": " + customAttributesList);
     customAttributes.put (title, new Vector (customAttributesList));
     createNewPanel (customAttributesList, title);
   }
 } // CreateNewPanelAction
 
 //-----------------------------------------------------------------------------------
 private void createNewPanel (Vector customAttributesList, String title) {
     String [] requestedAttributeNames = (String []) customAttributesList.toArray (new String [0]);
     BrowserTableModel model = 
 	new BrowserTableModel (graphObjects, attributes, requestedAttributeNames);
     JTable table = new JTable (model);
     setPreferredColumnWidths (table);    
     table.setCellSelectionEnabled (true);
     table.addMouseListener (new MyMouseListener (table));
     table.setPreferredScrollableViewportSize (new Dimension (preferredTableWidth, preferredTableHeight));
     JScrollPane scrollPane = new JScrollPane (table);
     tabbedPane.add (title, scrollPane);
     tabbedPane.setSelectedComponent (scrollPane);
 }
 
 //-----------------------------------------------------------------------------------
 public class SaveTableAction extends AbstractAction {
 
   private JFrame frame;
 
   SaveTableAction (JFrame frame) {super (""); this.frame = frame;}
 
   public void actionPerformed (ActionEvent e) {
       // if the customizer tab is selected, do nothing
       if (tabbedPane.getSelectedIndex() == 0) return;
 
       // get currently selected table and table size
       JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
       JTable table = (JTable) scrollPane.getViewport().getView();
       int rowCount = table.getRowCount();
       int columnCount = table.getColumnCount();
 
       // query for filename
       JFileChooser chooser = new JFileChooser (currentDirectory);
       if (chooser.showSaveDialog (TabbedBrowser.this) == chooser.APPROVE_OPTION) {
 	  String name = chooser.getSelectedFile ().toString ();
 	  currentDirectory = chooser.getCurrentDirectory();
 	  File file = new File(name);
 	  try {
 	      FileWriter fout = new FileWriter(file);
 	      for (int row = 0; row < rowCount; row++) {
 		  for (int col = 0; col < columnCount; col++) {
 		      Class classType = table.getColumnClass(col);
 		      Object data = table.getValueAt(row,col);
 		      fout.write(data + "\t");
 		      //System.out.print(data + "\t");
 		  }
 		  fout.write("\n");
 		  //System.out.println();
 	      }
 	      fout.close();
 	  } catch (IOException exc) {
 	      JOptionPane.showMessageDialog(null, exc.toString(),
 					    "Error Writing to \"" + file.getName()+"\"",
 					    JOptionPane.ERROR_MESSAGE);
 	  }
       }
   } // actionPerformed
 } // SaveTableAction
 //-----------------------------------------------------------------------------------
 
 public class DismissAction extends AbstractAction {
 
   private JFrame frame;
 
   DismissAction (JFrame frame) {super (""); this.frame = frame;}
 
   public void actionPerformed (ActionEvent e) {
     // System.out.println ("dismiss, customAttributes: " + customAttributes);
     frame.dispose ();
     }
 
 } // DismissAction
 //-----------------------------------------------------------------------------------
 private void setPreferredColumnWidths (JTable table)
 {
   TableColumnModel columnModel = table.getColumnModel ();
   int numberOfColumns = table.getModel().getColumnCount ();
 
   columnModel.getColumn (0).setPreferredWidth (80);
   //columnModel.getColumn (1).setPreferredWidth (450);
 
 } // setPreferredColumnWidths
 //------------------------------------------------------------------------
 private void placeInCenter ()
 {
   GraphicsConfiguration gc = getGraphicsConfiguration ();
   int screenHeight = (int) gc.getBounds().getHeight ();
   int screenWidth = (int) gc.getBounds().getWidth ();
   int windowWidth = getWidth ();
   int windowHeight = getHeight ();
   setLocation ((screenWidth-windowWidth)/2, (screenHeight-windowHeight)/2);
 
 } // placeInCenter
 //------------------------------------------------------------------------------
 protected void addCellSelection (JTable table)
 {
   table.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
   table.setCellSelectionEnabled (true);
   ListSelectionModel rowSM = table.getSelectionModel ();
   RowListener cellListener = new RowListener (table);
   rowSM.addListSelectionListener (cellListener);
   ListSelectionModel colSM = table.getColumnModel().getSelectionModel();
   colSM.addListSelectionListener (cellListener);
 }
 //------------------------------------------------------------------------------
 class RowListener implements ListSelectionListener {
   JTable table;
   int selectedRow, selectedColumn;
   RowListener (JTable table) {
     this.table = table;
     }
   public void valueChanged (ListSelectionEvent e) {
     if (e.getValueIsAdjusting ()) return;
     // System.out.println (e);
     ListSelectionModel lsm = (ListSelectionModel) e.getSource();
     if (!lsm.isSelectionEmpty ()) {
       selectedRow = lsm.getMinSelectionIndex ();
       //System.out.println ("Row " + selectedRow + " is now selected.");
       Object cellValue = table.getModel().getValueAt (selectedRow, 0);
       // System.out.println ("cellValue: " + cellValue);
       } // else
     } // valueChanges
 
 } // RowListener
 //------------------------------------------------------------------------------
 class MyMouseListener implements MouseListener 
 {
   private JTable table;
 
   public MyMouseListener (JTable table) {
     this.table = table;
     }
 
    public void mouseClicked  (MouseEvent e) {
      TableColumnModel columnModel = table.getColumnModel ();
      int column = columnModel.getColumnIndexAtX (e.getX ());
       int row  = e.getY() / table.getRowHeight();
       if (row >= table.getRowCount () || row < 0 || 
         column >= table.getColumnCount() || column < 0)
       return;
       Object cellValue = table.getValueAt (row, column);
       // System.out.println ("cellValue (" + cellValue.getClass() + "): " + cellValue);
       try {
         if (cellValue != null && cellValue.getClass () == Class.forName ("java.net.URL")) {
           //String cellContents = (String) cellValue;
           URL url = (URL) cellValue;
           // System.out.println ("URL: " + url);
           displayWebPage (url);
          }
       }
       catch (ClassNotFoundException ignore) {;}
       } // mouseClicked
 
    public void mouseEntered  (MouseEvent e) {}
    public void mouseExited   (MouseEvent e) {}
    public void mousePressed  (MouseEvent e) {}
    public void mouseReleased (MouseEvent e) {
        // if the customizer tab is selected, do nothing
        if (tabbedPane.getSelectedIndex() == 0) return;
        
        // get currently selected table and table size
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        JTable table = (JTable) scrollPane.getViewport().getView();
        String clipboardData = new String();
        StringWriter stringWriter = new StringWriter();
        int [] selectedRows = table.getSelectedRows();
        int [] selectedCols = table.getSelectedColumns();
        for (int i = 0; i < selectedRows.length; i++) {
 	   int row = selectedRows[i];
 	   for (int j = 0; j < selectedCols.length; j++) {
 	       int col = selectedCols[j];
 	       Class classType = table.getColumnClass(col);
 	       Object data = table.getValueAt(row,col);
 	       //System.out.print(data + "\t");
 	       stringWriter.write(data + "\t");
 	   }
 	   //System.out.println();
 	   stringWriter.write("\n");
        }
        // move to system clipboard
        Clipboard clipboard = getToolkit ().getSystemClipboard ();
        StringSelection stringSelection = new StringSelection(stringWriter.toString());
        clipboard.setContents(stringSelection, TabbedBrowser.this);
    }
 } // inner class MyMouseListener
 //-------------------------------------------------------------------------------
 protected void displayWebPage (URL url)
 {
   try {
     BasicService bs = (BasicService) ServiceManager.lookup ("javax.jnlp.BasicService");
     bs.showDocument (url);
     }
   catch (Exception ex) {
     String msg = "<html>You need to invoke Cytsocape through Java Web Start <br>" + 
                  "in order to make use of your web browser from within the program.<br><br>" +
                  "Error message: " + ex.getMessage () + "</html>";
     JOptionPane.showMessageDialog (TabbedBrowser.this, msg);
     }
 
 } // displayWebPage
 //-------------------------------------------------------------------------------
 protected void oldDisplayWebPage (URL url)
 {
   String [] cmd = new String [2];
   //cmd [0] = "/users/pshannon/data/human/jdrf/web";
   //cmd [0] = "./web";
   cmd [0] = webBrowserScript;
   cmd [1] = url.toString ();
    
   // System.out.println ("about to run: " + cmd [0] + "  " + cmd [1]);
   Exec exec = new Exec (cmd);
   exec.run ();
   Vector stdout = exec.getStdout ();
   Vector stderr = exec.getStderr ();
 
   //for (int i=0; i < stdout.size (); i++)
   //  System.out.println (stdout.elementAt (i));
 
   for (int i=0; i < stderr.size (); i++)
     System.out.println (stderr.elementAt (i));
 
 } // displayWebPage
 //-------------------------------------------------------------------------------
 // this class is needed by interface ClipboardOwner
 public void lostOwnership(Clipboard clipboard, Transferable contents) {}
 
 //-------------------------------------------------------------------------------
 } // class TabbedBrowser
 
 
