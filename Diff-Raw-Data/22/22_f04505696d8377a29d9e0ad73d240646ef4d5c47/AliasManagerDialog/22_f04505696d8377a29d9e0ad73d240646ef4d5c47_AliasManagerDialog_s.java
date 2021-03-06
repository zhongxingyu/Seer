 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.ui.swing.dialogs.aliases;
 
 import com.dmdirc.Main;
 import com.dmdirc.actions.Action;
 import com.dmdirc.actions.ActionCondition;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.ActionType;
 import com.dmdirc.actions.CoreActionComparison;
 import com.dmdirc.actions.CoreActionComponent;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.actions.wrappers.AliasWrapper;
 import com.dmdirc.ui.swing.MainFrame;
 import com.dmdirc.ui.swing.components.PackingTable;
 import com.dmdirc.ui.swing.components.StandardDialog;
 import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
 import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableCellRenderer;
 
 /**
  * Alias manager dialog.
  */
 public final class AliasManagerDialog extends StandardDialog implements
         ActionListener, ListSelectionListener {
     
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 3;
     
     /** Previously instantiated instance of AliasManagerDialog. */
     private static AliasManagerDialog me = new AliasManagerDialog();
     
     /** Table scrollpane. */
     private JScrollPane scrollPane;
     
     /** Error table. */
     private JTable table;
     
     /** Table model. */
     private AliasTableModel tableModel;
     
     /** Error detail panel. */
     private AliasPanel aliasDetails;
     
     /** Buttons pane. */
     private JPanel buttonsPanel;
     
     /** Add/edit button. */
     private JButton addButton;
     
     /** Delete button. */
     private JButton deleteButton;
     
     /** Selected row. */
     private int selectedRow;
     
     /** Creates a new instance of ErrorListDialog. */
     private AliasManagerDialog() {
         super((MainFrame) Main.getUI().getMainWindow(), false);
         
         setTitle("DMDirc: Alias manager");
         
         selectedRow = -1;
         
         initComponents();
         layoutComponents();
         initListeners();
         
         pack();
         
         setLocationRelativeTo(getParent());
     }
     
     /**
      * Returns the instance of AliasManagerDialog.
      *
      * @return Instance of AliasManagerDialog
      */
     public static AliasManagerDialog getAliasManagerDialog() {
         synchronized (me) {
             me.updateTableData();
             return me;
         }
     }
     
     /** Initialises the components. */
     private void initComponents() {
         final TableCellRenderer arrayRenderer = new ArrayCellRenderer();
         final TableCellRenderer conditionRenderer = new ActionConditionCellRenderer();
         initButtonsPanel();
         
         scrollPane = new JScrollPane();
         
         tableModel = new AliasTableModel();
         table = new PackingTable(tableModel, false, scrollPane) {
             private static final long serialVersionUID = 1;
             public TableCellRenderer getCellRenderer(final int row, final int column) {
                 switch (column) {
                     case 1:
                         return conditionRenderer;
                     case 2:
                         return arrayRenderer;
                     default:
                         return super.getCellRenderer(row, column);
                 }
             }
         };
         
         table.setAutoCreateRowSorter(true);
         table.setAutoCreateColumnsFromModel(true);
         table.setColumnSelectionAllowed(false);
         table.setCellSelectionEnabled(false);
         table.setDragEnabled(false);
         table.setFillsViewportHeight(false);
         table.setRowSelectionAllowed(true);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.getRowSorter().toggleSortOrder(0);
         
         table.getTableHeader().setReorderingAllowed(false);
         
         table.setPreferredScrollableViewportSize(new Dimension(600, 150));
         
         scrollPane.setViewportView(table);
         
         aliasDetails = new AliasPanel();
     }
     
     /**
      * Updates the table data.
      */
     public void updateTableData() {
         tableModel.setAliases(getTableData());
     }
     
     /**
      * Gets the table data fromt he alias wrapper.
      *
      * @return Alias list
      */
     private List<Alias> getTableData() {
         final List<Alias> aliases = new ArrayList<Alias>();
         final List<Action> actions = AliasWrapper.getAliasWrapper().getActions();
         
         for (Action loopAction : actions) {
             final List<ActionCondition> arguments = loopAction.getConditions();
             
             ActionCondition argument;
             
             argument = arguments.get(0);
             
             if (argument.getComparison() != CoreActionComparison.STRING_EQUALS) {
                 argument = arguments.get(1);
             }
             
             aliases.add(new Alias(argument.getTarget(),
                     arguments, loopAction.getResponse()));
         }
         
         return aliases;
     }
     
     /** Initialises the button panel. */
     private void initButtonsPanel() {
         buttonsPanel = new JPanel();
         
         orderButtons(new JButton(), new JButton());
         addButton = new JButton("Add");
         deleteButton = new JButton("Delete");
         
         addButton.setPreferredSize(new Dimension(100, 25));
         deleteButton.setPreferredSize(new Dimension(100, 25));
         addButton.setMinimumSize(new Dimension(100, 25));
         deleteButton.setMinimumSize(new Dimension(100, 25));
         
         buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER,
                 SMALL_BORDER, SMALL_BORDER));
         
         buttonsPanel.setPreferredSize(new Dimension(600, 35));
         buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
         
         buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
         buttonsPanel.add(Box.createHorizontalGlue());
         buttonsPanel.add(deleteButton);
         buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
         buttonsPanel.add(addButton);
         buttonsPanel.add(Box.createHorizontalStrut(LARGE_BORDER));
         buttonsPanel.add(getLeftButton());
         buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
         buttonsPanel.add(getRightButton());
     }
     
     /** Initialises the listeners. */
     private void initListeners() {
         table.getSelectionModel().addListSelectionListener(this);
         getOkButton().addActionListener(this);
         getCancelButton().addActionListener(this);
         addButton.addActionListener(this);
         deleteButton.addActionListener(this);
     }
     
     /** Lays out the components. */
     private void layoutComponents() {
         getContentPane().setLayout(new BoxLayout(getContentPane(),
                 BoxLayout.PAGE_AXIS));
         
         scrollPane.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                 0, SMALL_BORDER),
                 scrollPane.getBorder()));
         aliasDetails.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                 SMALL_BORDER, 0, 0));
         
         getContentPane().add(scrollPane);
         getContentPane().add(aliasDetails);
         getContentPane().add(buttonsPanel);
     }
     
     /** {@inheritDoc}. */
     public void valueChanged(final ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
             
             if (selectedRow > -1 && selectedRow < tableModel.getRowCount()
             && aliasDetails.getAlias() == tableModel.getAlias(
                     table.getRowSorter().convertRowIndexToModel(selectedRow))) {
                 updateAlias();
             }
             
             if (table.getSelectedRow() > -1) {
                 aliasDetails.setAlias(tableModel.getAlias(table.getRowSorter().
                         convertRowIndexToModel(table.getSelectedRow())));
                 deleteButton.setEnabled(true);
             } else {
                 aliasDetails.clear();
                 deleteButton.setEnabled(false);
             }
             
             selectedRow = table.getSelectedRow();
         }
     }
     
     /** Updates the selected alias with the edited details. */
     private boolean updateAlias() {
         final Alias alias = tableModel.getAlias(table.getRowSorter().
                 convertRowIndexToModel(selectedRow));
         final Alias newAlias = aliasDetails.getNewAlias();
         final List<ActionCondition> conditions =
                 new ArrayList<ActionCondition>();
         
         if (checkDuplicateAlias(newAlias)) {
             return false;
         }
         
         conditions.add(new ActionCondition(1,
                 CoreActionComponent.STRING_STRING,
                 CoreActionComparison.STRING_EQUALS, alias.getCommand()));
         if (aliasDetails.getArguments() != null) {
             conditions.add(aliasDetails.getArguments());
         }
         
         alias.setCommand(aliasDetails.getCommand());
         alias.setArguments(conditions);
         alias.setResponse(aliasDetails.getResponse());
         tableModel.fireTableRowsUpdated(tableModel.indexOf(alias), 
                 tableModel.indexOf(alias));
         
         return true;
     }
     
     /** {@inheritDoc}. */
     public void actionPerformed(final ActionEvent e) {
         if (e.getSource() == deleteButton) {
             delete();
         } else if (e.getSource() == addButton) {
             add();
         } else if (e.getSource() == getCancelButton()) {
             dispose();
         } else if (e.getSource() == getOkButton()) {
             if (table.getSelectedRow() != -1 && !updateAlias()) {
                 JOptionPane.showMessageDialog(this,
                         "There are duplicate aliases in the table, these need "
                         + "to be removed before saving", "Duplicates",
                         JOptionPane.WARNING_MESSAGE);
                 return;
             }
             save();
             dispose();
         }
     }
     
     /** Adds an alias. */
     private void add() {
         final Alias alias = new Alias("");
         tableModel.addRow(alias);
         final int newRow = table.getRowSorter().
                 convertRowIndexToView(tableModel.indexOf(alias));
         table.getSelectionModel().setSelectionInterval(newRow, newRow);
         aliasDetails.focusCommand();
     }
     
     /** Deletes an alias. */
     private void delete() {
         if (table.getSelectedRow() != -1) {
             tableModel.removeRow(table.getRowSorter().
                     convertRowIndexToModel(table.getSelectedRow()));
         }
     }
     
     /** Saves the aliases. */
     private void save() {
         final List<Action> actions = new ArrayList<Action>(
                 AliasWrapper.getAliasWrapper().getActions());
         final List<Alias> aliases = tableModel.getAliases();
         
         final List<Alias> newAliases = new ArrayList<Alias>();
         final List<Alias> modifiedAliases = new ArrayList<Alias>();
         
         for (Alias alias : aliases) {
             final Action action = getAction(alias);
             
             if (action == null) {
                 newAliases.add(alias);
             } else {
                 if (!action.getName().equals(alias.getName())
                 || !action.getConditions().equals(alias.getArguments())
                 || !Arrays.equals(action.getResponse(), alias.getResponse())) {
                     modifiedAliases.add(alias);
                 }
                 actions.remove(action);
             }
         }
         
         for (Action action : actions) {
             action.delete();
         }
         
         saveNewAliases(newAliases);
         
         saveModifiedAliases(modifiedAliases);
         
         ActionManager.loadActions();
     }
     
     /**
      * Saves new aliases
      *
      * @param aliases List of new aliases to save
      */
     private void saveNewAliases(final List<Alias> aliases) {
         for (Alias alias : aliases) {
             new Action(
                     AliasWrapper.getAliasWrapper().getGroupName(),
                     alias.getName(),
                     new ActionType[] {CoreActionType.UNKNOWN_COMMAND, },
                     alias.getResponse(),
                     alias.getArguments(),
                     "").save();
         }
     }
     
     /**
      * Saves modified aliases
      *
      * @param aliases List of modified aliases to save
      */
     private void saveModifiedAliases(final List<Alias> aliases) {
         for (Alias alias : aliases) {
             final Action action = getAction(alias);
             if (action != null) {
                 action.rename(alias.getName());
                 action.setConditions(alias.getArguments());
                 action.setResponse(alias.getResponse());
                 action.save();
             }
         }
     }
     
     /**
      * Returns the action corresponding to the specified alias
      *
      * @param alias Alias to check
      *
      * @return Corresponding action or null if none found
      */
     private Action getAction(final Alias alias) {
         final List<Action> actions = new ArrayList<Action>(
                 AliasWrapper.getAliasWrapper().getActions());
         Action action = null;
         
         for (Action loopAction : actions) {
             if (loopAction.getName().equals(alias.getName())
             && loopAction.getConditions().equals(alias.getArguments())) {
                 action = loopAction;
                 break;
             }
         }
         
         return action;
     }
     
     /**
      * Checks if ths alias matches another alias
      *
      * @param alias Alias to check
      *
      * @return true iif the alias matches another
      */
     private boolean checkDuplicateAlias(final Alias alias) {
         final List<Alias> aliases = tableModel.getAliases();
         
         for (Alias loopAlias : aliases) {
             if (loopAlias.matches(alias)) {
                 return true;
             }
         }
        
         return false;
     }
 }
