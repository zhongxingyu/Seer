 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 package de.ing_poetter.binview;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.NoSuchElementException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 
 import de.ing_poetter.binview.variables.CreateVariableWindow;
 import de.ing_poetter.binview.variables.Variable;
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class CreateFormatWindow extends JFrame implements ActionListener, FormatWindow
 {
     private static final long serialVersionUID = 1L;
     private final MainWindow parent;
     private final static String AC_LOAD = "load";
     private final static String AC_SAVE = "save";
     private final static String AC_ADD_VARI = "addVari";
     private final static String AC_REMOVE_VARI = "removeVari";
     private final static String AC_EXIT = "exit";
     private BinaryFormat format;
     private final JFileChooser fc = new JFileChooser();
     private final JTable table = new JTable();
 
     /**
      * @param format
      *
      */
     public CreateFormatWindow(final MainWindow parent, final BinaryFormat curFormat)
     {
         this.parent = parent;
         this.setTitle("Binary View - create new Format");
         this.setResizable(true);
         this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         if(null == curFormat)
         {
             System.out.println("creating format");
             format = new BinaryFormat();
         }
         else
         {
             System.out.println("using format");
             format = curFormat;
         }
         table.setModel(format);
         final JPanel root = new JPanel();
         root.setLayout(new FlowLayout());
         root.add(createFormatArea());
         root.add(createControlArea());
         this.add(root);
         this.pack();
         this.setVisible(true);
 
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
     }
 
     private JPanel createControlArea()
     {
         final JPanel control = new JPanel();
         control.setLayout(new BoxLayout(control, BoxLayout.PAGE_AXIS));
 
         final JButton loadFormat = new JButton("load Format");
         loadFormat.setActionCommand(AC_LOAD);
         loadFormat.addActionListener(this);
         loadFormat.setEnabled(true);
         control.add(loadFormat);
 
         final JButton saveFormat = new JButton("save Format");
         saveFormat.setActionCommand(AC_SAVE);
         saveFormat.addActionListener(this);
         saveFormat.setEnabled(true);
         control.add(saveFormat);
 
         final JButton addVari = new JButton("add Variable");
         addVari.setActionCommand(AC_ADD_VARI);
         addVari.addActionListener(this);
         addVari.setEnabled(true);
         control.add(addVari);
 
         final JButton removeVari = new JButton("remove Variable");
         removeVari.setActionCommand(AC_REMOVE_VARI);
         removeVari.addActionListener(this);
         removeVari.setEnabled(true);
         control.add(removeVari);
 
         final JButton exit = new JButton("Exit");
         exit.setActionCommand(AC_EXIT);
         exit.addActionListener(this);
         exit.setEnabled(true);
         control.add(exit);
 
         return control;
     }
 
     private JPanel createFormatArea()
     {
         final JPanel formatPanel = new JPanel();
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.setRowSelectionAllowed(true);
         table.setColumnSelectionAllowed(false);
         table.setCellSelectionEnabled(false);
         final JScrollPane scrollPane = new JScrollPane(table);
         table.setFillsViewportHeight(true);
         formatPanel.add(scrollPane);
         return formatPanel;
     }
 
     @Override
     public void actionPerformed(final ActionEvent e)
     {
         if(true == AC_LOAD.equals(e.getActionCommand()))
         {
             // show File chooser dialog to select Format File
             final int returnVal = fc.showOpenDialog(this);
             if(JFileChooser.APPROVE_OPTION == returnVal)
             {
                 final File f = fc.getSelectedFile();
                 format = BinaryFormat.loadFromFile(f);
                 table.setModel(format);
             }
             this.repaint();
         }
         else if(true == AC_SAVE.equals(e.getActionCommand()))
         {
             // show File chooser dialog to select Format File
            final int returnVal = fc.showOpenDialog(this);
             if(JFileChooser.APPROVE_OPTION == returnVal)
             {
                 final File f = fc.getSelectedFile();
                 try
                 {
                     format.saveToFile(f);
                 }
                 catch (final IOException e1)
                 {
                     e1.printStackTrace();
                 }
             }
         }
         else if(true == AC_ADD_VARI.equals(e.getActionCommand()))
         {
             @SuppressWarnings("unused")
             final CreateVariableWindow cvw = new CreateVariableWindow(this);
         }
         else if(true == AC_REMOVE_VARI.equals(e.getActionCommand()))
         {
             try
             {
                 int row = table.getSelectionModel().getLeadSelectionIndex();
                 if(-1 == row)
                 {
                     // No row selected -> last row
                     row = format.getRowCount() -1;
                 }
                 format.removeVariable(row);
             }
             catch(final NoSuchElementException e1)
             {
                 e1.printStackTrace();
             }
             this.repaint();
         }
         else if(true == AC_EXIT.equals(e.getActionCommand()))
         {
             if(null != parent)
             {
                 parent.setFormat(format);
             }
             this.setVisible(false);
             this.dispose();
         }
         // else ignore event
     }
 
     @Override
     public void addVariable(final Variable v)
     {
         System.out.print("Adding the Variable " + v.getName());
         int row = table.getSelectionModel().getLeadSelectionIndex();
         if(-1 == row)
         {
             // nothing selected
             row = format.getRowCount();
             System.out.println(" to row " + row);
             format.addVariableAt(v, row);
         }
         else
         {
             System.out.println(" to row " + row+1);
             format.addVariableAt(v, row+1);
         }
         table.repaint();
         this.repaint();
     }
 
 }
