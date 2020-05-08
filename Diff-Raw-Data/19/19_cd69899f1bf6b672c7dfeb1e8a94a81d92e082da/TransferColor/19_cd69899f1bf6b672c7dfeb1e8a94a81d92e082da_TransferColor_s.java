 package transferable;
 
 import java.awt.Color;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.TransferHandler;
 
 public class TransferColor extends TransferHandler {
 	public int getSourceActions(JComponent c) {
 		return COPY;
 	}
 
 	protected Transferable createTransferable(JComponent c) {
 		Color color = ((JPanel) c).getBackground();
		int blue = color.getBlue();
		int red = color.getRed();
		int green = color.getGreen();
		
		return new StringSelection("#"+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue));
 	}
 
	/*protected void exportDone(JComponent source, Transferable data, int action) {
		if (action == MOVE) {
			if (source.getClass().getName().contains("JLabel")) {fffefffffeff
				((JLabel) source).setText("");
			} else {
				((JFormattedTextField) source).setText("");
			}
		}
	}*/

 	public boolean canImport(TransferHandler.TransferSupport support) {
 		return false;
 	}
 	
 	public boolean importData(TransferHandler.TransferSupport support) {
 		return false;
 	}
 }
