 /*
  * Copyright (C) 2012 Helsingfors Segelklubb ry
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package fi.hoski.remote.ui;
 
 import fi.hoski.datastore.repository.DataObject;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JComponent;
 import javax.swing.JTable;
 import javax.swing.RowSorter;
 import javax.swing.TransferHandler;
 import javax.swing.table.TableModel;
 
 /**
  * @author Timo Vesalainen
  */
 public class DataObjectTransferHandler<T extends DataObject> extends TransferHandler
 {
     private DataObjectListTableModel<T> model;
 
     public DataObjectTransferHandler(DataObjectListTableModel<T> model)
     {
         this.model = model;
     }
 
     @Override
     public boolean canImport(TransferSupport support)
     {
         for (DataFlavor dataFlavor : support.getDataFlavors())
         {
             if (DataObject.class.isAssignableFrom(dataFlavor.getRepresentationClass()))
             {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     protected DataObjectTransferable<T> createTransferable(JComponent c)
     {
         JTable table = (JTable) c;
         int selectedRow = table.getSelectedRow();
         if (selectedRow != -1)
         {
             RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
             if (rowSorter != null)
             {
                 selectedRow = rowSorter.convertRowIndexToModel(selectedRow);
             }
             return new DataObjectTransferable<T>(model.getObject(selectedRow));
         }
         return null;
     }
 
     @Override
     protected void exportDone(JComponent source, Transferable data, int action)
     {
         try
         {
             DataObjectTransferable<T> transferable = (DataObjectTransferable<T>) data;
             if (MOVE == action)
             {
                 T transferData = transferable.getTransferData(transferable.getFlavor());
                 model.remove(transferData);
             }
         }
         catch (UnsupportedFlavorException ex)
         {
             throw new IllegalArgumentException(ex);
         }
         catch (IOException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     @Override
     public int getSourceActions(JComponent c)
     {
         return MOVE;
     }
 
     @Override
     public boolean importData(TransferSupport support)
     {
         try
         {
             Transferable transferable = support.getTransferable();
             T transferData = (T) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
             int index = model.remove(transferData);
             DropLocation dropLocation = support.getDropLocation();
             if (dropLocation instanceof JTable.DropLocation)
             {
                 JTable.DropLocation dl = (JTable.DropLocation) dropLocation;
                 int row = dl.getRow();
                 if (index != -1 && index < row)
                 {
                     row--;
                 }
                 model.add(row, transferData);
             }
             else
             {
                 model.add(transferData);
             }
             return index == -1;
         }
         catch (UnsupportedFlavorException ex)
         {
             Logger.getLogger(DataObjectTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
         }
         catch (IOException ex)
         {
             Logger.getLogger(DataObjectTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
         }
         return false;
     }
 
 }
