 package filelist;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 
 public class IconListRenderer extends DefaultListCellRenderer {
 	private static final long serialVersionUID = -1227188542632542649L;
 	
 	int left_padding = 16;
 	int among_space = 1;
 	
 	//////temp
 	ListItem curr_item;
 	boolean selected;
 	JLabel label;
 	boolean focused;
 	//////
 	
 	public Map<String, Icon> icons = new HashMap<String, Icon>();
  
 	public IconListRenderer() {}
 
 	@Override
 	public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 		label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 		curr_item = (ListItem) value;
 		selected = isSelected;
 		focused = cellHasFocus;
 		
 		setForeground(selected || focused ? Color.white : Color.black);
 
 		Icon icon = icons.get(curr_item.ext);
 		if (icon == null) icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(service.Settings.imagepath + "items/default.png"));
 		label.setIcon(icon);
 	//		label.setBorder(
 	////			BorderFactory.createCom poundBorder(
 	////				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "1234", TitledBorder.CENTER, TitledBorder.TOP),
 	////				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "4321", TitledBorder.CENTER, TitledBorder.BOTTOM)
 	////			)
 	//			BorderFactory.createTitledBorder(
 	//					BorderFactory.createTitledBorder(
 	//							BorderFactory.createTitledBorder(
 	//									BorderFactory.createEtchedBorder()
 	//							, item.ext.toUpperCase(), TitledBorder.CENTER, TitledBorder.TOP)
 	//					, "43221", TitledBorder.RIGHT, TitledBorder.BOTTOM)
 	//			, "1234", TitledBorder.LEFT, TitledBorder.TOP)				
 	//		);
 //			label.setBorder(null);
 //			label.setBorder(BorderFactory.createEmptyBorder(among_space + 1, 0, among_space + 1, 0));
 		return label;
 	}
 	
 	protected void paintComponent( Graphics g ) 
 	{
 		Graphics2D g2d = (Graphics2D)g.create(label.getX(), 0, label.getWidth(), label.getHeight());
 		Rectangle r = new Rectangle(label.getX(), 0, label.getWidth(), label.getHeight());
 		GradientPaint gp = null;
 		Color [] color_set;
 
 
 		if (selected || focused) {
 			if (curr_item.StatusIsPlayed())
 				color_set = FileListConst.played_grad_select;
			else if (curr_item.StatusIsNone())
				color_set = FileListConst.usual_grad_select;
 			else if (curr_item.StatusIsListened())
				color_set = FileListConst.listened_grad_select;
			else color_set = FileListConst.droped_grad_select;
 		}
 		else {
 			if (curr_item.StatusIsPlayed())
 				color_set = FileListConst.played_grad;
			else if (curr_item.StatusIsNone())
				color_set = FileListConst.usual_grad;
 			else if (curr_item.StatusIsListened())
 				color_set = FileListConst.listened_grad;
			else color_set = FileListConst.droped_grad;
 		}
 	
 		gp = new GradientPaint(
 			    0, 0, color_set[1],
 			    0, r.height, color_set[0] );
 		
 		if (gp != null) {
 			g2d.setPaint(gp);
 			g2d.fillRoundRect(r.x + left_padding, r.y + among_space, r.width - left_padding, r.height - among_space * 2, 10, 30);
 //			g2d.setColor(Color.black);
 //			g2d.setStroke(new BasicStroke(1.5F));
 //			g2d.drawRoundRect(r.x + left_padding, r.y + among_space, r.width - left_padding, r.height - among_space * 2, 10, 30);
 		}
 	 
 	    setOpaque( false );
 	    super.paintComponent(g);
 	    setOpaque( true );
 	}
 }
