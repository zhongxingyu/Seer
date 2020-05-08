 package rocks6205.editor.actions;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import rocks6205.editor.core.LSEditor;
 import rocks6205.editor.core.LSView;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 
 import java.io.File;
 import java.io.IOException;
 
import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.TransferHandler;
 
 import static javax.swing.TransferHandler.COPY;
 
 /**
  *
  * @author Cheow Yeong Chi
  * @version 2.5
  */
 public class LSTransferHandler extends TransferHandler {
 
     /**
      * Parent component
      */
     private LSView parentView;
 
     public LSTransferHandler() {}
 
     public LSTransferHandler(LSView parent) {
         parentView = parent;
     }
 
     @Override
     public boolean canImport(TransferSupport tf) {
         if (!tf.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
             return false;
         }
 
         boolean copySupported = (COPY & tf.getSourceDropActions()) == COPY;
 
         if (!copySupported) {
             return false;
         }
 
         tf.setDropAction(COPY);
 
         return true;
     }
 
     @Override
     public boolean importData(TransferSupport tf) {
         if (!canImport(tf)) {
             return false;
         }
 
         Transferable t = tf.getTransferable();
         try {
            @SuppressWarnings("unchecked") List<File> l =
                (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
 
             if (l.size() != 1) {
                 return false;
             }
             File f = l.get(0);
             if (parentView.promptSaveIfNeeded()) {
                 parentView.openFile(f);
             }
              
         } catch (UnsupportedFlavorException | IOException e) {
             LSEditor.logger.warning(e.getMessage());
 
             return false;
         }
 
         return true;
     }
 }
