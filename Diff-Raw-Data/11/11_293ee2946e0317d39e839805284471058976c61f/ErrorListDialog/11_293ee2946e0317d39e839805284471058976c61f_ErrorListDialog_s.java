 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.dialogs.error;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.logger.ErrorManager;
 import com.dmdirc.logger.ErrorReportStatus;
 import com.dmdirc.logger.ProgramError;
 import com.dmdirc.ui.IconManager;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.inject.Inject;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Error list dialog.
  */
 public final class ErrorListDialog extends StandardDialog implements
         ActionListener, ListSelectionListener, TableModelListener {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 5;
     /** Table model. */
     private final ErrorTableModel tableModel;
     /** Table scrollpane. */
     private JScrollPane scrollPane;
     /** Error table. */
     private ErrorTable table;
     /** Error detail panel. */
     private ErrorDetailPanel errorDetails;
     /** Send button. */
     private JButton sendButton;
     /** Delete button. */
     private JButton deleteButton;
     /** Delete all button. */
     private JButton deleteAllButton;
     /** Selected row. */
     private final AtomicInteger selectedRow = new AtomicInteger(-1);
     /** Row being deleted. */
     private boolean rowBeingDeleted = false;
 
     /**
      * Creates a new instance of ErrorListDialog.
      *
      * @param mainFrame   The parent window.
      * @param iconManager The manager to use to load icons.
      */
     @Inject
     public ErrorListDialog(final MainFrame mainFrame,
             @GlobalConfig final IconManager iconManager) {
         super(mainFrame, ModalityType.MODELESS);
 
         setTitle("Error list");
         setMinimumSize(new Dimension(600, 550));
 
         tableModel = new ErrorTableModel();
 
         initComponents(iconManager);
         layoutComponents();
         initListeners();
 
         selectedRow.set(table.getSelectedRow());
     }
 
     /**
      * Loads the dialog and sets it as ready.
      *
      * @param errorManager Error manager to register with.
      */
     public void load(final ErrorManager errorManager) {
         tableModel.load(errorManager);
     }
 
     /** Initialises the components. */
     private void initComponents(final IconManager iconManager) {
         initButtons();
 
         scrollPane = new JScrollPane();
 
         table = new ErrorTable(iconManager, tableModel, scrollPane);
 
         table.setPreferredScrollableViewportSize(new Dimension(600, 150));
         scrollPane.setMinimumSize(new Dimension(150, 100));
 
         scrollPane.setViewportView(table);
 
         errorDetails = new ErrorDetailPanel();
     }
 
     /** Initialises the buttons. */
     private void initButtons() {
         orderButtons(new JButton(), new JButton());
 
         getCancelButton().setText("Close");
         sendButton = new JButton("Send");
         deleteButton = new JButton("Delete");
         deleteAllButton = new JButton("Delete All");
 
         sendButton.setEnabled(false);
         deleteButton.setEnabled(false);
         if (tableModel.getRowCount() > 0) {
             deleteAllButton.setEnabled(true);
         } else {
             deleteAllButton.setEnabled(false);
         }
     }
 
     /** Initialises the listeners. */
     private void initListeners() {
         tableModel.addTableModelListener(this);
         table.getSelectionModel().addListSelectionListener(this);
         sendButton.addActionListener(this);
         deleteButton.addActionListener(this);
         deleteAllButton.addActionListener(this);
         getOkButton().addActionListener(this);
         getCancelButton().addActionListener(this);
     }
 
     /** Lays out the components. */
     private void layoutComponents() {
         final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                 true);
         final JPanel panel = new JPanel();
 
         panel.setLayout(new MigLayout("fill"));
 
         panel.add(errorDetails, "wrap, grow, push");
         panel.add(deleteAllButton, "split 4, tag left, sg button");
         panel.add(deleteButton, "tag other, sg button");
         panel.add(sendButton, "tag other, sg button");
         panel.add(getCancelButton(), "tag ok, sg button");
 
         splitPane.setTopComponent(scrollPane);
         splitPane.setBottomComponent(panel);
 
         splitPane.setDividerSize((int) PlatformDefaults.getPanelInsets(0).
                 getValue());
 
         getContentPane().add(splitPane);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e List selection event
      */
     @Override
     public void valueChanged(final ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
             final int localRow = table.getSelectedRow();
             if (localRow > -1) {
                 final ProgramError error;
                 try {
                     error = tableModel.getError(table.getRowSorter()
                             .convertRowIndexToModel(localRow));
                 } catch (final IndexOutOfBoundsException ex) {
                     //In the extremely rare case the error gets deleted whilst
                     //we're changing the value, bail out gracefully
                     return;
                 }
                 errorDetails.setError(error);
                 deleteButton.setEnabled(true);
                 if (error.getReportStatus() == ErrorReportStatus.NOT_APPLICABLE || error.
                         getReportStatus() == ErrorReportStatus.FINISHED) {
                     sendButton.setEnabled(false);
                 } else {
                     sendButton.setEnabled(true);
                 }
             } else {
                 errorDetails.setError(null);
                 deleteButton.setEnabled(false);
                 sendButton.setEnabled(false);
             }
             synchronized (selectedRow) {
                 if (rowBeingDeleted) {
                     table.getSelectionModel().setSelectionInterval(selectedRow.
                             get(), selectedRow.get());
                     rowBeingDeleted = false;
                 }
                 selectedRow.set(localRow);
             }
         }
     }
 
     /**
      * {@inheritDoc}.
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (e.getSource() == getCancelButton()) {
             setVisible(false);
         } else if (e.getSource() == deleteButton) {
             synchronized (selectedRow) {
                 ErrorManager.getErrorManager().deleteError(tableModel.getError(
                         table.getRowSorter().convertRowIndexToModel(
                                 table.getSelectedRow())));
             }
         } else if (e.getSource() == sendButton) {
             synchronized (selectedRow) {
                 ErrorManager.getErrorManager().sendError(tableModel.getError(
                         table.getRowSorter().convertRowIndexToModel(
                                 table.getSelectedRow())));
             }
         } else if (e.getSource() == deleteAllButton) {
             ErrorManager.getErrorManager().deleteAll();
         }
     }
 
     @Override
     public boolean enterPressed() {
         executeAction(getOkButton());
         return true;
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Table model event
      */
     @Override
     public void tableChanged(final TableModelEvent e) {
         switch (e.getType()) {
             case TableModelEvent.DELETE:
                 synchronized (selectedRow) {
                     if (selectedRow.get() >= tableModel.getRowCount()) {
                         selectedRow.set(tableModel.getRowCount() - 1);
                     }
                     table.getSelectionModel().setSelectionInterval(selectedRow.
                             get(),
                             selectedRow.get());
                     rowBeingDeleted = true;
                 }
                 break;
             case TableModelEvent.INSERT:
                 synchronized (selectedRow) {
                     table.getSelectionModel().setSelectionInterval(selectedRow.
                             get(),
                             selectedRow.get());
                 }
                 break;
             case TableModelEvent.UPDATE:
                 final int errorRow = e.getFirstRow();
                 final ProgramError error = tableModel.getError(errorRow);
                 if (errorRow == table.getSelectedRow()) {
                     if (error.getReportStatus() == ErrorReportStatus.NOT_APPLICABLE || error.
                             getReportStatus() == ErrorReportStatus.FINISHED) {
                         sendButton.setEnabled(false);
                     } else {
                         sendButton.setEnabled(true);
                     }
                 }
                 break;
         }
         if (tableModel.getRowCount() > 0) {
             deleteAllButton.setEnabled(true);
         } else {
             deleteAllButton.setEnabled(false);
         }
     }
 
     @Override
     public void display() {
         super.display();
         setSize(new Dimension(600, 550));
     }
 
     @Override
     public void dispose() {
         tableModel.dispose();
         super.dispose();
     }
 
 }
