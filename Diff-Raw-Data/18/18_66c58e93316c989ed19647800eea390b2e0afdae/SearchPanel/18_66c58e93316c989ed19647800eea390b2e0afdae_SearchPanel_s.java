 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.cmu.cs.diamond.opendiamond.Search;
 
 public class SearchPanel extends JPanel implements ListSelectionListener {
     final protected JList list;
 
     protected Search theSearch;
 
     final private CaseViewer caseViewer;
 
     public SearchPanel(CaseViewer c) {
         caseViewer = c;
 
         setOpaque(false);
         setLayout(new BorderLayout());
 
         setBorder(BorderFactory.createTitledBorder(null, "Search Results",
                 TitledBorder.CENTER, TitledBorder.TOP, null, Color.WHITE));
 
         SearchListCellRenderer cr = new SearchListCellRenderer();
         list = new JList();
         list.setCellRenderer(cr);
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         list.setVisibleRowCount(-1);
 
         list.setOpaque(false);
         list.setBackground(null);
         list.setForeground(Color.WHITE);
 
         JScrollPane jsp = new JScrollPane(list,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         jsp.setOpaque(false);
         jsp.setBackground(null);
 
         JViewport v = jsp.getViewport();
         v.setOpaque(false);
         v.setBackground(null);
 
         list.getSelectionModel().addListSelectionListener(this);
         add(jsp);
 
         JButton closeButton = new JButton("Close Search");
         closeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
             }
         });
 
         JButton reorderButton = new JButton("Sort Results");
         reorderButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 ListModel model = list.getModel();
                 if (model instanceof SearchModel) {
                     SearchModel sm = (SearchModel) model;
 
                     Object selected = list.getSelectedValue();
                     sm.reorder();
                     list.clearSelection();
                     // list.setSelectedValue(selected, true);
                 }
             }
         });
 
         // box
         Box box = Box.createHorizontalBox();
         box.add(closeButton);
         box.add(Box.createHorizontalGlue());
         box.add(reorderButton);
 
         add(box, BorderLayout.SOUTH);
     }
 
     @Override
     public void setVisible(boolean visible) {
         boolean oldVisible = isVisible();
 
         super.setVisible(visible);
         if (oldVisible != visible && !visible) {
             if (theSearch != null) {
                 theSearch.stop();
             }
 
             // deregister listeners
            ListModel oldModel = list.getModel();
            if (oldModel instanceof SearchModel) {
                SearchModel m = (SearchModel) oldModel;
                m.removeSearchListener();
            }
 
             caseViewer.setSelectedResult(null);
         }
     }
 
     void beginSearch(Search s) {
         if (theSearch != null) {
             theSearch.stop();
         }
 
         theSearch = s;
 
         list.setModel(new SearchModel(theSearch, Integer.MAX_VALUE));
 
         theSearch.start();
 
         setVisible(true);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         Graphics2D g2 = (Graphics2D) g;
         g2.setColor(new Color(0, 0, 0, 0.8f));
 
         g2.fillRect(0, 0, getWidth(), getHeight());
     }
 
     public void valueChanged(ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
             MassResult r = (MassResult) list.getSelectedValue();
             caseViewer.setSelectedResult(r);
         }
     }
 }
