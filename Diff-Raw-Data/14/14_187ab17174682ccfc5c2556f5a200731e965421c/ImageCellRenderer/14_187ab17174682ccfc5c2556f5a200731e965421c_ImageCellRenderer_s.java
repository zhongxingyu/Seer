 /**
  * Copyright (C) 2014 
  * Nicholas J. Little <arealityfarbetween@googlemail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package little.nj.gui.components;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Image;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.border.Border;
 
 import little.nj.util.ImageUtil;
 
@SuppressWarnings({ "serial", "rawtypes" })
 public class ImageCellRenderer extends JPanel
                                implements ListCellRenderer {
     
 	public static final int DEFAULT_BOX = 200;
 	
 	public static final Border DEFAULT_BORDER = 
 			BorderFactory.createEmptyBorder(10, 10, 10, 10);
 	
     private static final JLabel cell = new JLabel();
 	
 	private final int		box;
 
 	private Map<Object, ImageIcon> stamps = new HashMap<Object, ImageIcon>();
 
 	public ImageCellRenderer(int box, Border border) {
 		this.box = box;
 		
 		add(cell);
         cell.setOpaque(true);
         cell.setBorder(border);
 	}
 	
 	public ImageCellRenderer() {
 		this(DEFAULT_BOX, DEFAULT_BORDER);
 	}
 	
 	@Override
 	public Component getListCellRendererComponent(
 			JList list, 
 			Object value, 
 			int index,
 			boolean isSelected, 
 			boolean cellHasFocus) {
 		
 		ImageIcon stamp = stamps.get(value);
 		
 		if (null == stamp) {
 			stamp = new ImageIcon(ImageUtil.resizeImage((Image)value, box, box));
 			
 			stamps.put(value, stamp);
 		}
 		
 		cell.setIcon(stamp);
 		
 		if (isSelected) {
 			setForeground(list.getSelectionForeground());
 			setBackground(list.getSelectionBackground());
 		} else {
 			setForeground(list.getForeground());
 			setBackground(list.getBackground());
 		}
 		
 		if (cellHasFocus) {
			stamp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
 		} else {
 			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
 		}
 		
 		return this;
 	}
 }
