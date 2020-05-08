 package molBiol;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 public class HistoryCellRenderer extends JButton 
 	implements ListCellRenderer {
 	
 	public HistoryCellRenderer() {
 		super();
 		setOpaque(true);
 		setLayout(new BorderLayout());
 	}
 
 	public Component getListCellRendererComponent(JList list, 
 			Object value, int index, boolean isSelected, boolean cellHasFocus) {
 		VisibleGene vg = (VisibleGene)value;
		JButton button = new JButton(vg.getThumbnailPic());
		button.setBackground(vg.getColor());
 		button.setBorder(BorderFactory.createLineBorder(
 				isSelected ? Color.GREEN : Color.BLACK, 2));
		button.setToolTipText(vg.getAaSeq());
 		return button;
 	}
 
 }
