 package de.unisiegen.gtitool.ui.swing.dnd;
 
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
 
 import javax.swing.JComponent;
 import javax.swing.TransferHandler;
 
 import de.unisiegen.gtitool.ui.swing.JGTITable;
 
 
 /**
  * Drag and drop transfer handler class for {@link JGTITable}s.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public abstract class JGTITableTransferHandler extends TransferHandler
 {
 
   /**
    * The source actions supported for dragging using this
    * {@link JGTITableTransferHandler}.
    * 
    * @see #getSourceActions(JComponent)
    */
   private final int sourceActions;
 
 
   /**
    * Allocates a new {@link JGTITableTransferHandler}.
    * 
    * @param sourceActions The actions to support for dragging using this
    *          {@link JGTITableTransferHandler}.
    */
   public JGTITableTransferHandler ( int sourceActions )
   {
     super ();
     this.sourceActions = sourceActions;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see TransferHandler#canImport(JComponent, DataFlavor[])
    */
   @Override
   public final boolean canImport ( JComponent jComponent,
       DataFlavor [] dataFlavor )
   {
     if ( jComponent instanceof JGTITable )
     {
       for ( DataFlavor transferFlavor : dataFlavor )
       {
         if ( transferFlavor
             .equals ( JGTITableModelRowsTransferable.tableModelRowsFlavor ) )
         {
           return true;
         }
       }
     }
     return super.canImport ( jComponent, dataFlavor );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see TransferHandler#createTransferable(JComponent)
    */
   @Override
   protected final Transferable createTransferable ( JComponent jComponent )
   {
     JGTITable table = ( JGTITable ) jComponent;
     int [] selectedRows = table.getSelectedRows ();
    if ( ( table.getRowCount () > 0 ) && ( selectedRows.length > 0 ) )
     {
       return new JGTITableModelRowsTransferable ( new JGTITableModelRows (
           table.getModel (), selectedRows ) );
     }
     return null;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see TransferHandler#getSourceActions(JComponent)
    */
   @Override
   public final int getSourceActions ( @SuppressWarnings ( "unused" )
   JComponent jComponent )
   {
     return this.sourceActions;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see TransferHandler#importData(JComponent, Transferable)
    */
   @Override
   public final boolean importData ( JComponent jComponent,
       Transferable transferable )
   {
     JGTITable table = ( JGTITable ) jComponent;
     try
     {
       JGTITableModelRows rows = ( JGTITableModelRows ) transferable
           .getTransferData ( JGTITableModelRowsTransferable.tableModelRowsFlavor );
 
       int sourceIndex = rows.getRowIndices () [ 0 ];
       int targetIndex = table.rowAtPoint ( table.getDropPoint () );
       if ( targetIndex == -1 )
       {
         targetIndex = table.getRowCount () - 1;
       }
       else if ( sourceIndex < targetIndex )
       {
         targetIndex-- ;
       }
 
       if ( importTableModelRows ( table, rows, targetIndex ) )
       {
         return true;
       }
       return super.importData ( jComponent, transferable );
     }
     catch ( IOException e )
     {
       throw new RuntimeException ( e );
     }
     catch ( UnsupportedFlavorException e )
     {
       throw new RuntimeException ( e );
     }
   }
 
 
   /**
    * Imports the rows from the drag source into the specified table.
    * 
    * @param table The {@link JGTITable} into which to import the rows.
    * @param rows The rows to import from the drag source.
    * @param targetIndex The target index.
    * @return True if the import was successfull.
    * @see #importData(JComponent, Transferable)
    */
   protected abstract boolean importTableModelRows ( JGTITable table,
       JGTITableModelRows rows, int targetIndex );
 }
