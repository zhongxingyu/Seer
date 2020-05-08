 package elw.dp.ui;
 
 import javax.swing.*;
 import javax.swing.table.TableCellRenderer;
 import java.awt.*;
 
 public class AccessTrackingCellRenderer implements TableCellRenderer {
 	protected final TableCellRenderer wrapped;
 	protected final RendererFactory factory;
 
 	protected static final Color W_COLOR = new Color(0xFF, 0xDD, 0xDD);
 	protected static final Color R_COLOR = new Color(0xEE, 0xDD, 0xFF);
 	protected static final Color N_COLOR = Color.white;
 
 	public AccessTrackingCellRenderer(TableCellRenderer wrapped, RendererFactory factory) {
 		this.wrapped = wrapped;
 		this.factory = factory;
 	}
 
 	public Component getTableCellRendererComponent(JTable table, Object obj,
 												   boolean isSelected, boolean hasFocus,
 												   int row, int column) {
 		Component cell = wrapped.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
 
 		final int accCol = factory.lookupAccessColumn(table);
		if (accCol < 0) {
 			return cell;
 		}
 
 		final String acc = (String) table.getModel().getValueAt(row, accCol);
 		if (acc.indexOf("w") >= 0) {
 			cell.setBackground(darker(W_COLOR, row));
 		} else if (acc.indexOf("r") >= 0) {
 			cell.setBackground(darker(R_COLOR, row));
 		} else {
 			cell.setBackground(darker(N_COLOR, row));
 		}
 
 		return cell;
 	}
 
 	public Color darker(Color what, int row) {
 		if (row % 2 == 0) {
 			return what;
 		}
 		return new Color(
 				Math.max((int) (what.getRed() * 0.95), 0),
 				Math.max((int) (what.getGreen() * 0.95), 0),
 				Math.max((int) (what.getBlue() * 0.95), 0)
 		);
 	}
 }
