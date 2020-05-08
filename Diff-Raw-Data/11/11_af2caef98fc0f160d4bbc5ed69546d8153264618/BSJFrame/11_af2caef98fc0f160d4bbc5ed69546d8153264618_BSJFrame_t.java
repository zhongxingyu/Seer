 package suncertify.gui;
 
 import static suncertify.gui.BSJRow.LOCATION;
 import static suncertify.gui.BSJRow.NAME;
 import static suncertify.gui.BSJRow.OWNER;
 import static suncertify.gui.BSJRow.RATE;
 import static suncertify.gui.BSJRow.RECORD_POSITION;
 import static suncertify.gui.BSJRow.SPECIALTIES;
 import static suncertify.gui.BSJRow.WORKERS_NUMBER;
 
 import javax.swing.JTable;
 import javax.swing.SwingWorker;
 import javax.swing.table.DefaultTableModel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import suncertify.db.RecordNotFoundException;
 import suncertify.parser.PropertiesLoader;
 import suncertify.program.Mode;
 import suncertify.socket.client.DBAccessClientImpl;
 
 /**
  * The Class BSJFrame sets settings for user components.
  */
 public class BSJFrame extends BSJFrameBase {
 
   /**
    * Instantiates a new BSJFrame.
    * 
    * @param mode the mode
    */
   public BSJFrame(Mode mode) {
     super(mode);
     refreshBtn.addActionListener(new BSActionListener(table, BSDataType.REFRESH));
     searchBtn.addActionListener(new BSActionListener(table, BSDataType.SEARCH));
     createBtn.addActionListener(new BSActionListener(table, BSDataType.CREATE));
     deleteBtn.addActionListener(new BSActionListener(table, BSDataType.DELETE));
     getBtn.addActionListener(new BSActionListener(table, BSDataType.GET));
     updateBtn.addActionListener(new BSActionListener(table, BSDataType.UPDATE));
     updateBtn.setEnabled(false);
     setColums(table);
     if (!isStarrtedOK) {
       setStatus("Started, but check the database location!", false);
     }
   }
 
   private void setButtonsEnabled(boolean isEnabled) {
     createBtn.setEnabled(isEnabled);
     getBtn.setEnabled(isEnabled);
     updateBtn.setEnabled(isEnabled);
     deleteBtn.setEnabled(isEnabled);
   }
 
   private void repaintTable(int rowSelected) {
     String[][] dataTable = findByCriteria();
     table.setModel(new DefaultTableModel(dataTable, BSJRow.getHeaders()));
     setColums(table);
     if (rowSelected > -1) {
       table.getSelectionModel().setSelectionInterval(rowSelected, rowSelected);
     }
     table.repaint();
   }
 
   private class BSActionListener implements ActionListener {
     private JTable table;
     private BSDataType type;
 
     private BSActionListener(JTable table, BSDataType type) {
       this.table = table;
       this.type = type;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
       int rowSelected = table.getSelectedRow();
       int port = 0;
       if (type == BSDataType.REFRESH) {
         try {
           port = Integer.parseInt(dbPortField.getText());
         } catch (NumberFormatException ex) {
           setStatus("Port must be number", false);
          if (mode != Mode.STANDALONE) {
            return;
          }
         }
         if (!PropertiesLoader.getInstance().getDbLocation().equals(dbLocationField.getText())
             || !PropertiesLoader.getInstance().getDbHost().equals(dbHostField.getText())
             || !PropertiesLoader.getInstance().getDbPort().equals(dbPortField.getText()) || mode == Mode.SERVER) {
           PropertiesLoader.getInstance().saveProperties(dbLocationField.getText(), dbHostField.getText(),
                                                         dbPortField.getText());
           if (mode == Mode.NETWORK_CLIENT_AND_GUI) {
             data = new DBAccessClientImpl(dbHostField.getText(), port);
           } else if (mode == Mode.SERVER) {
             setStatus("Please restart server!", false);
             refreshBtn.setEnabled(false);
             dbLocationField.setEnabled(false);
             dbPortField.setEnabled(false);
             data = null;
             return;
           }
         }
         setCriteria(null);
         setStatus("Refreshed");
         repaintTable(-1);
       } else if (type == BSDataType.DELETE) {
         if (rowSelected >= 0) {
           int recNo = Integer.parseInt((String) table.getModel().getValueAt(rowSelected, RECORD_POSITION));
           new DeleteWorker<Boolean, Void>(recNo).execute();
         }
         repaintTable(-1);
       } else if (type == BSDataType.CREATE) {
         boolean isCreated = createRecord(new String[] { nameField.getText(), locationField.getText(),
                                                        specialitiesField.getText(), workersNrField.getText(),
                                                        rateField.getText(), ownerField.getText() });
         if (isCreated) {
           repaintTable(table.getModel().getRowCount());
           setStatus("New row " + table.getModel().getRowCount() + " created");
         } else {
           setStatus("Can't create duplicate record.", false);
         }
       } else if (type == BSDataType.SEARCH) {
         setCriteria(new String[] { nameField.getText(), locationField.getText(), specialitiesField.getText(),
                                   workersNrField.getText(), rateField.getText(), ownerField.getText() });
         repaintTable(-1);
         setStatus("Search returned " + table.getModel().getRowCount() + " rows.");
       } else if (type == BSDataType.GET) {
         if (rowSelected >= 0) {
           nameField.setText((String) table.getModel().getValueAt(rowSelected, NAME));
           locationField.setText((String) table.getModel().getValueAt(rowSelected, LOCATION));
           specialitiesField.setText((String) table.getModel().getValueAt(rowSelected, SPECIALTIES));
           workersNrField.setText((String) table.getModel().getValueAt(rowSelected, WORKERS_NUMBER));
           rateField.setText((String) table.getModel().getValueAt(rowSelected, RATE));
           ownerField.setText((String) table.getModel().getValueAt(rowSelected, OWNER));
           updateBtn.setEnabled(true);
           setSelectedRow(rowSelected);
           setStatus("You can update row number " + (rowSelected + 1));
         }
       } else if (type == BSDataType.UPDATE) {
         if (getSelectedRow() > -1) {
           setButtonsEnabled(false);
           int recNo = Integer.parseInt((String) table.getModel().getValueAt(getSelectedRow(), RECORD_POSITION));
           new UpdateWorker<Boolean, Void>(recNo).execute();
         }
       }
       if (type != BSDataType.GET) {
         updateBtn.setEnabled(false);
         setSelectedRow(-1);
       }
     }
   }
 
   private class UpdateWorker<T, V> extends SwingWorker<Void, Void> {
     private int recNo;
     private ErrorType error;
 
     private UpdateWorker(int recNo) {
       this.recNo = recNo;
     }
 
     @Override
     protected Void doInBackground() {
       long cookie;
       try {
         cookie = lockRow(recNo);
       } catch (RecordNotFoundException e1) {
         error = ErrorType.RECORD_NOT_FOUND;
         return null;
       }
       try {
         updateRow(recNo, new String[] { nameField.getText(), locationField.getText(), specialitiesField.getText(),
                                        workersNrField.getText(), rateField.getText(), ownerField.getText() }, cookie);
       } catch (RecordNotFoundException e) {
         error = ErrorType.RECORD_NOT_FOUND;
       } catch (SecurityException e) {
         error = ErrorType.SECURITY;
       }
       try {
         unlockRow(recNo, cookie);
       } catch (SecurityException e1) {
         error = ErrorType.SECURITY;
         return null;
       }
       return null;
     }
 
     @Override
     protected void done() {
       if (error != null) {
         if (error == ErrorType.RECORD_NOT_FOUND) {
           setStatus("Update is unsuccessful. Record not found.", false);
         } else if (error == ErrorType.SECURITY) {
           setStatus("You haven't permission to update this record.", false);
         }
       } else {
         setStatus("Update succeed.");
         repaintTable(-1);
       }
       setButtonsEnabled(true);
       updateBtn.setEnabled(false);
     }
   }
 
   private class DeleteWorker<T, V> extends SwingWorker<Void, Void> {
     private int recNo;
     private ErrorType error;
 
     private DeleteWorker(int recNo) {
       this.recNo = recNo;
     }
 
     @Override
     protected Void doInBackground() {
       long cookie;
       try {
         cookie = lockRow(recNo);
       } catch (RecordNotFoundException e1) {
         error = ErrorType.RECORD_NOT_FOUND;
         return null;
       }
       try {
         deleteRecord(recNo, cookie);
       } catch (RecordNotFoundException e) {
         error = ErrorType.RECORD_NOT_FOUND;
       } catch (SecurityException e) {
         error = ErrorType.SECURITY;
       }
       try {
         unlockRow(recNo, cookie);
       } catch (SecurityException e1) {
         error = ErrorType.SECURITY;
         return null;
       }
       return null;
     }
 
     @Override
     protected void done() {
       if (error != null) {
         if (error == ErrorType.RECORD_NOT_FOUND) {
           setStatus("Delete is unsuccessful. Record not found.", false);
         } else if (error == ErrorType.SECURITY) {
           setStatus("You haven't permission to delete this record.", false);
         }
       } else {
         setStatus("Delete succeed.");
       }
       setButtonsEnabled(true);
       repaintTable(-1);
     }
   }
 
 }
