 /**
  * Copyright (C) 2009 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.displaypattern;
 
 import com.jidesoft.grid.BeanTableModel;
 import com.jidesoft.grid.SortableTable;
 import com.od.jtimeseries.ui.util.ImageUtils;
 import com.od.swing.action.ListSelectionActionModel;
 import com.od.swing.action.ModelDrivenAction;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.beans.IntrospectionException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 01-Jun-2009
  * Time: 10:33:35
  */
 public class DisplayPatternTable extends JPanel {
 
     private BeanTableModel<DisplayNamePattern> beanTableModel;
     private SortableTable sortableTable;
     private JPopupMenu tablePopupMenu;
     private ListSelectionActionModel<DisplayNamePattern> seriesSelectionModel = new ListSelectionActionModel<DisplayNamePattern>();
 
     public DisplayPatternTable(List<DisplayNamePattern> displayNamePatterns) {
         createPopupMenu();
         try {
             beanTableModel = new BeanTableModel<DisplayNamePattern>(
                    displayNamePatterns,
                     DisplayNamePattern.class,
                     new String[] {"pattern", "Pattern", "replacement", "Replacement"}
             );
         } catch (IntrospectionException e) {
             e.printStackTrace();
         }
         sortableTable = new SortableTable(beanTableModel);
 
         JComponent buttonBar = createButtonBar();
 
         setLayout(new BorderLayout());
         add(new JScrollPane(sortableTable), BorderLayout.CENTER);
         add(buttonBar, BorderLayout.SOUTH);
         setBorder(new EmptyBorder(5,5,5,5));
         addSeriesSelectionListener();
     }
 
     //we need to clone the list we are passed so that we can cancel the edits without
     //affecting the existing patterns
     private List<DisplayNamePattern> clone(List<DisplayNamePattern> displayNamePatterns) {
         List<DisplayNamePattern> clones = new ArrayList<DisplayNamePattern>();
         for ( DisplayNamePattern p : displayNamePatterns) {
             clones.add(new DisplayNamePattern(p));
         }
         return clones;
     }
 
     private JComponent createButtonBar() {
         JButton addPatternButton = new JButton(new AddPatternAction());
 
         Box buttonBox = Box.createHorizontalBox();
         buttonBox.add(Box.createHorizontalGlue());
         buttonBox.add(addPatternButton);
         return buttonBox;
     }
 
     private void createPopupMenu() {
         tablePopupMenu = new JPopupMenu("Display Name Pattern Actions");
         tablePopupMenu.add(new RemoveFromPatternsAction());
     }
 
     private void addSeriesSelectionListener() {
         sortableTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         sortableTable.getSelectionModel().addListSelectionListener(
             new ListSelectionListener() {
                 public void valueChanged(ListSelectionEvent e) {
                     if ( sortableTable.getSelectedRow() > -1 ) {
                         DisplayNamePattern pattern = (DisplayNamePattern)beanTableModel.getObject(sortableTable.getSelectedRow());
                         seriesSelectionModel.setSelected(pattern);
                     }
                 }
             }
         );
     }
 
     public List<DisplayNamePattern> getDisplayPatterns() {
         List<DisplayNamePattern> patterns = new ArrayList<DisplayNamePattern>();
         for ( int row = 0; row < beanTableModel.getRowCount(); row ++) {
             patterns.add(beanTableModel.getObject(row));
         }
         return patterns;
     }
 
     private class RemoveFromPatternsAction extends ModelDrivenAction<ListSelectionActionModel<DisplayNamePattern>> {
 
         public RemoveFromPatternsAction() {
             super(seriesSelectionModel, "Remove", ImageUtils.REMOVE_ICON_16x16);
         }
 
         public void actionPerformed(ActionEvent e) {
             for ( DisplayNamePattern p : getActionModel().getSelected()) {
                 beanTableModel.removeObject(p);
             }
         }
     }
 
     private class AddPatternAction extends AbstractAction {
 
         public AddPatternAction() {
             super("Add Pattern", ImageUtils.ADD_ICON_16x16);
         }
 
         public void actionPerformed(ActionEvent e) {
             DisplayNamePattern p = new DisplayNamePattern();
             beanTableModel.addObject(p);
         }
     }
 
 
 }
