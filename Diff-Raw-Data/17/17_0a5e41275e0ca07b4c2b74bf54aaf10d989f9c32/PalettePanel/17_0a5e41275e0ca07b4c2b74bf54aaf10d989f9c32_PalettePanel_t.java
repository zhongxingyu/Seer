 package com.dorado.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.Map;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToggleButton;
 import javax.swing.ScrollPaneConstants;
 
 import com.dorado.image.Palette;
import com.forana.layout.BlockLayout;
 
 /**
  * Panel for displaying the palette to the user.
  */
 public class PalettePanel extends JScrollPane {
 	private static final long serialVersionUID = 1L;
 	
 	private static final int WIDTH = 150;
 	private static final int BUTTON_SIZE = 24;
 	
 	private Palette palette;
 
 	public PalettePanel(Palette palette) {
 		setMinimumSize(new Dimension(WIDTH, 0));
 		setPreferredSize(new Dimension(WIDTH, 0));
 		setMaximumSize(new Dimension(WIDTH, Integer.MAX_VALUE));
 		getViewport().setBackground(UIConstants.PANEL_COLOR);
 		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		setBorder(null);
 		
 		this.palette = palette;
 		
 		populatePanel();
 	}
 	
 	private void populatePanel() {
 		JPanel panel = new JPanel();
 		panel.setBackground(UIConstants.PANEL_COLOR);
		panel.setLayout(new BlockLayout());
 		
 		ButtonGroup group = new ButtonGroup();
 		for (Map.Entry<Integer, Color> entry : this.palette.getAllColors()) {
 			AbstractButton button = new ColorButton(entry.getKey(), entry.getValue());
 			group.add(button);
			panel.add(button, BlockLayout.INLINE);
 		}
 		
 		setViewportView(panel);
 	}
 	
 	private class ColorButton extends JToggleButton {
 		private static final long serialVersionUID = 1L;
 		
 		//private int index;
 		private Color color;
 		
 		public ColorButton(int index, Color color) {
 			//this.index = index;
 			this.color = color;
 			setText(color.toString());
			setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			setBorder(null);
 		}
 		
 		@Override
 		public void paintComponent(Graphics g) {
 			g.setColor(color);
 			
 			g.fillRect(0, 0, BUTTON_SIZE, BUTTON_SIZE);
 			
 			if (isSelected()) {
 				g.setColor(Color.BLACK);
 				g.drawRect(0, 0, BUTTON_SIZE - 1, BUTTON_SIZE - 1);
 				g.setColor(Color.WHITE);
 				g.drawRect(1, 1, BUTTON_SIZE - 3, BUTTON_SIZE - 3);
 			}
 		}
 	}
 }
