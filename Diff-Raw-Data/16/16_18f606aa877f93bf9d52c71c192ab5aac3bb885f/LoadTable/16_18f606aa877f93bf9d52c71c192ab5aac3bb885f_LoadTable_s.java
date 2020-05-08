 /*
  * @(#) $RCSfile: LoadTable.java,v $ $Revision: 1.10 $ $Date: 2004/08/02 20:23:42 $ $Name: TableView1_3_2 $
  *
  * Center for Computational Genomics and Bioinformatics
  * Academic Health Center, University of Minnesota
  * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * see: http://www.gnu.org/copyleft/gpl.html
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  */
 package edu.umn.genomics.table;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.TableModel;
 
 /**
  * A Graphical User Interface that contains browsers for various datasources,
  * such as the FileBrowser and DBBrowser. The default TableSource table loaders
  * read from the Properties file. Additional loaders can be registered.
  *
  * @author J Johnson
  * @version $Revision: 1.10 $ $Date: 2004/08/02 20:23:42 $ $Name: TableView1_3_2
  * $
  * @since 1.0
  * @see javax.swing.table.TableModel
  * @see javax.swing.ListSelectionModel
  */
 public class LoadTable extends AbstractTableSource {
 
     JTabbedPane tpane;
     JButton loadBtn = new JButton("load");
     JButton mergeBtn = new JButton("merge");
     JTextField mergeNameField = new JTextField();
     JTextField nameField = new JTextField();
     JTextField mergeNameLabel = new JTextField(20);
     JButton cancelBtn = new JButton("cancel");
     JDialog dialog = null;
     MergeTableModel mtm = null;
     ChangeListener cl = new ChangeListener() {
 
         public void stateChanged(ChangeEvent e) {
             AbstractTableSource src = null;
             if (e.getSource() instanceof JTabbedPane) {
                 src = (AbstractTableSource) ((JTabbedPane) e.getSource()).getSelectedComponent();
             } else if (e.getSource() instanceof AbstractTableSource) {
                 src = (AbstractTableSource) e.getSource();
             }
             setTableSource(src.getTableModel(), src.getTableSource());
         }
     };
     DocumentListener docListener = new DocumentListener() {
 
         public void insertUpdate(DocumentEvent e) {
             setLoadEnable();
         }
 
         public void removeUpdate(DocumentEvent e) {
             setLoadEnable();
         }
 
         public void changedUpdate(DocumentEvent e) {
             setLoadEnable();
         }
     };
 
     /**
      * Create a LoadTable.
      */
     public LoadTable() {
         tpane = new JTabbedPane();
         try {
             Properties props = getDefaultProperties();
             setTableLoaders(props);
         } catch (Exception ex) {
             FileBrowser filePnl = new FileBrowser();
             DBBrowser dbPnl = new DBBrowser();
             tpane.addTab("File or URL", null, filePnl, "Access a table from a file or URL");
             tpane.addTab("Database", null, dbPnl, "Access a table from a database");
             filePnl.addChangeListener(cl);
             dbPnl.addChangeListener(cl);
         }
         tpane.addChangeListener(cl);
         setLayout(new BorderLayout());
         add(tpane);
 
         nameField.getDocument().addDocumentListener(docListener);
         mergeNameField.getDocument().addDocumentListener(docListener);
 
         JPanel btnPnl = new JPanel(new BorderLayout());
 
         JPanel mrgPnl = new JPanel(new BorderLayout());
         JPanel loadPnl = new JPanel(new BorderLayout());
 
         mergeNameLabel.setEditable(false);
         Box mrgBox = new Box(BoxLayout.X_AXIS);
         mrgBox.add(mergeBtn);
         mrgBox.add(new JLabel(" as: "));
         Box mtmBox = new Box(BoxLayout.X_AXIS);
         mtmBox.add(new JLabel(" into: "));
         mtmBox.add(mergeNameLabel);
         mrgPnl.add(mrgBox, BorderLayout.WEST);
         mrgPnl.add(mergeNameField, BorderLayout.CENTER);
         mrgPnl.add(mtmBox, BorderLayout.EAST);
 
         Box btnBox = new Box(BoxLayout.X_AXIS);
         btnBox.add(loadBtn);
         btnBox.add(new JLabel(" as: "));
         loadPnl.add(btnBox, BorderLayout.WEST);
         loadPnl.add(nameField, BorderLayout.CENTER);
         loadPnl.add(cancelBtn, BorderLayout.EAST);
 
         btnPnl.add(mrgPnl, BorderLayout.NORTH);
         btnPnl.add(loadPnl, BorderLayout.SOUTH);
 
         add(btnPnl, BorderLayout.SOUTH);
 
     }
 
     /**
      * Get the default Properties from the package.
      *
      * @return The default Properties
      */
     public Properties getDefaultProperties() throws IOException {
         String path = "edu/umn/genomics/table/view.properties";
         ClassLoader cl = this.getClass().getClassLoader();
         Properties properties = new Properties();
         properties.load(cl.getResourceAsStream(path));
         return properties;
     }
 
     /**
      * Register table loaders that are defined in the given Properties. The list
      * of table loader ids are defined in the loaders key. The following key
      * properties are recognized for each id:
      *
      * - id+".name" - id+".class" - id+".classdependency" - id+".icon" -
      * id+".tooltip"
      *
      * loaders=file gpf
      *
      * file.name=File or URL file.class=edu.umn.genomics.table.FileBrowser
      * file.classdependency=edu.umn.genomics.table.FileBrowser
      * file.tooltip=Access a table from a file or URL
      *
      * gpf.name=GenePix File
      * gpf.class=edu.umn.genomics.table.loaders.GenePixFile
      * gpr.icon16=edu/umn/genomics/table/Icons/ftbl16.gif
      * gpf.classdependency=edu.umn.genomics.table.loaders.GenePixFile
      * gpf.tooltip=Import a GenePix ATF format file
      *
      * @param properties A properties instance that contains table loaders.
      */
     public void setTableLoaders(Properties properties) {
         ClassLoader cl = this.getClass().getClassLoader();
         String ids = properties.getProperty("loaders");
         if (ids != null) {
             StringTokenizer st = new StringTokenizer(ids);
             while (st.hasMoreTokens()) {
                 String id = st.nextToken();
                 String className = properties.getProperty(id + ".class");
                 String depends = properties.getProperty(id + ".classdependency");
                 String libdepends = properties.getProperty(id + ".libdependency");
                 if (depends != null) {
                     String depClass = "";
                     try {
                         for (StringTokenizer stk = new StringTokenizer(depends); stk.hasMoreTokens();) {
                             depClass = stk.nextToken();
                             Class.forName(depClass);
                         }
                     } catch (ClassNotFoundException cnfex) {
                         System.err.println("depends: " + depClass + "\t" + cnfex);
                         continue;
                     }
                 }
                 if (libdepends != null) {
                     String libName = "";
                     try {
                         for (StringTokenizer stk = new StringTokenizer(libdepends); stk.hasMoreTokens();) {
                             libName = stk.nextToken();
                             System.loadLibrary(libName);
                         }
                     } catch (UnsatisfiedLinkError err) {
                         System.err.println("libdepends: " + libName + "\t" + err);
                         continue;
                     } catch (SecurityException ex) {
                         System.err.println("libdepends: " + libName + "\t" + ex);
                         continue;
                     } catch (Throwable t) {
                         System.err.println("libdepends: " + libName + "\t" + t);
                         continue;
                     }
                 }
                 if (className != null) {
                     String loaderName = properties.getProperty(id + ".name");
                     if (loaderName == null || loaderName.length() < 1) {
                         int idx = className.lastIndexOf('.');
                         loaderName = idx < 0 ? className : className.substring(idx + 1);
                     }
                     try {
                         Class theClass = Class.forName(className);
                         if (edu.umn.genomics.table.AbstractTableSource.class.isAssignableFrom(theClass)) {
                             AbstractTableSource tableSource = (AbstractTableSource) theClass.getConstructor(null).newInstance(null);
                             ImageIcon icon = null;
                             String toolTip = properties.getProperty(id + ".tooltip");
                             String iconSrc = properties.getProperty(id + ".icon");
                             if (iconSrc != null) {
                                 icon = new ImageIcon(cl.getResource(iconSrc));
                             }
                             addTableLoader(loaderName, icon, tableSource, toolTip);
                         }
                     } catch (Exception ex) {
                         System.err.println("loader: " + loaderName + "\t" + ex);
                     }
                 }
             }
         }
     }
     /**
      * Return the names of all registered Table Loaders.
      *
      * @return The names of all registered Table Loaders.
      */
     public String[] getTableLoaderNames() {
         String[] names = new String[tpane.getTabCount()];
         for (int i = 0; i < tpane.getTabCount(); i++) {
             names[i] = tpane.getTitleAt(i);
         }
         return names;
     }
 
     /**
      * Return the Table Loader with the given name.
      *
      * @param name The Name of the table loader.
      * @return The instance of a TableSource
      */
     public TableSource getTableLoader(String name) {
         int i = tpane.indexOfTab(name);
         if (i >= 0) {
             return (AbstractTableSource) tpane.getComponentAt(i);
         }
         return null;
     }
 
     /**
      * Add a table loader to the list of available table loaders.
      *
      * @param name The name to identify this table loader.
      * @param icon An icon to display for this table loader.
      * @param tableSource The class instance that implements this table loader.
      * @param toolTip A tool tip to display for this table loader.
      */
     public void addTableLoader(String name, Icon icon, AbstractTableSource tableSource, String toolTip) {
         int i = tpane.indexOfTab(name);
         if (i >= 0) {
             tpane.removeTabAt(i);
         }
         tpane.addTab(name, icon, tableSource, toolTip);
         tableSource.addChangeListener(cl);
     }
 
     protected void setTableSource(TableModel tableModel, String tableSource) {
         super.setTableSource(tableModel, tableSource);
         nameField.setText(tableSource);
         mergeNameField.setText(tableSource);
         setLoadEnable();
         mergeBtn.setEnabled(getTableModel() != null);
         loadBtn.grabFocus();
     }
 
     protected void setSuperTableSource(TableModel tableModel, String tableSource) {
         super.setTableSource(tableModel, tableSource);
     }
 
     private JDialog getDialog(Frame owner) {
         if (dialog == null) {
             dialog = new JDialog(owner, "Load Table", true);
             loadBtn.addActionListener(
                     new ActionListener() {
 
                         public void actionPerformed(ActionEvent e) {
                             try {
                                 setSuperTableSource(getTableModel(), nameField.getText());
                                 mtm = null;
                                 mergeNameLabel.setText("");
                                 dialog.hide();
                             } catch (Exception ex) {
                             }
                         }
                     });
             mergeBtn.addActionListener(
                     new ActionListener() {
 
                         public void actionPerformed(ActionEvent e) {
                             try {
                                 TableModel ctm = getTableModel();
                                 if (ctm == null || ctm == mtm) {
                                     return;
                                 } else if (mtm == null) {
                                     mtm = new MergeTableModel();
                                 }
                                 VirtualTableModel vtm = null;
                                 if (ctm instanceof VirtualTableModel) {
                                     vtm = (VirtualTableModel) ctm;
                                 } else {
                                     vtm = new VirtualTableModelProxy(ctm);
                                 }
                                 vtm.setName(mergeNameField.getText());
                                 mtm.addTableModel(vtm);
                                 setTableSource(mtm, "");
                                 mergeBtn.setEnabled(false);
                                 String mtmName = "" + mtm.getTableList();
                                 mergeNameLabel.setText(mtmName);
                                 nameField.setText(mtmName);
                             } catch (Exception ex) {
                                 JOptionPane.showMessageDialog(getTopLevelAncestor(),
                                         ex,
                                         "Unable to merge table " + mergeNameField.getText(),
                                         JOptionPane.ERROR_MESSAGE);
                             }
                         }
                     });
             cancelBtn.addActionListener(
                     new ActionListener() {
 
                         public void actionPerformed(ActionEvent e) {
                             try {
                                 mtm = null;
                                 setTableSource(null, "");
                                 dialog.hide();
                             } catch (Exception ex) {
                             }
                         }
                     });
             dialog.getContentPane().setLayout(new BorderLayout());
             dialog.getContentPane().add(this);
             dialog.pack();
             try { // Limit the size to a proportion of screen size
                 Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                 Insets insets = dialog.getInsets();
                 int dw = (dim.width - insets.left - insets.right) / 10 * 6;
                 int dh = (dim.height - insets.top - insets.bottom) / 10 * 6;
                 if (dialog.getWidth() > dw || dialog.getHeight() > dh) {
                     dw = Math.min(dialog.getWidth(), dw);
                     dh = Math.min(dialog.getHeight(), dh);
                     dialog.setSize(dw, dh);
                 }
                 dialog.setLocation((int) ((dim.width - dw) / 2), (int) ((dim.height - dh) / 2));
             } catch (Exception ex) {
             }
             dialog.setResizable(true);
         }
         return dialog;
     }
 
     /**
      * Open a LoadTable as a Dialog.
      *
      * @param owner The frame that should own this dialog.
      * @return The TabelModel for the data source selected.
      */
     public TableModel openLoadTableDialog(Frame owner) {
         setTableSource(null, "");
         AbstractTableSource src = (AbstractTableSource) tpane.getSelectedComponent();
         if (src != null) {
             setTableSource(src.getTableModel(), src.getTableSource());
         }
         getDialog(owner).show();
         return getTableModel();
     }
 
     private void setLoadEnable() {
         loadBtn.setEnabled(getTableModel() != null
                 && nameField.getText() != null
                 && nameField.getText().trim().length() > 0);
         mergeBtn.setEnabled(getTableModel() != null
                 && mergeNameField.getText() != null
                 && mergeNameField.getText().trim().length() > 0);
 
     }
 }
