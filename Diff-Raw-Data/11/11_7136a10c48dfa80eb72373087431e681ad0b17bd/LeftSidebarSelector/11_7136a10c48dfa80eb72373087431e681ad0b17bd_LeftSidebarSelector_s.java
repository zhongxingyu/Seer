 package com.robonobo.gui.components;
 
 import static com.robonobo.gui.GuiUtil.*;
 import static com.robonobo.gui.RoboColor.*;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.*;
 
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.components.base.*;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.panels.LeftSidebar;
 
 /**
  * A simple label in the left sidebar that can be selected
  * @author macavity
  *
  */
 public abstract class LeftSidebarSelector extends JPanel implements LeftSidebarComponent {
 	public boolean selected;
 	protected LeftSidebar sideBar;
 	protected RobonoboFrame frame;
 	protected String cpName;
 	protected RLabel lbl;
	private static final Dimension SIZE = new Dimension(188, 19);
 
 	public LeftSidebarSelector(LeftSidebar sideBar, RobonoboFrame frame, String label, boolean lblBold, Icon icon, String contentPanelName) {
 		this.sideBar = sideBar;
 		this.frame = frame;
 		this.cpName = contentPanelName;
 		setOpaque(true);
 		setAlignmentX(0f);
 		setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 1));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		setPreferredSize(SIZE);
 		setMinimumSize(SIZE);
 		setMaximumSize(SIZE);
 		lbl = (lblBold) ? new RLabel12B(label, icon, JLabel.LEFT) : new RLabel12(label, icon, JLabel.LEFT);
 		lbl.setOpaque(false);
 		add(lbl);
 		addMouseListener(new MouseListener());
 	}
 
 	public void relinquishSelection() {
 		setSelected(false);
 	}
 	
 	public void setSelected(boolean isSelected) {
 		if (isSelected) {
 			frame.mainPanel.selectContentPanel(cpName);
 			sideBar.clearSelectionExcept(this);
 		}
 		this.selected = isSelected;
 		updateColors();
 	}
 
 	protected void updateColors() {
 		setForeground(fgColor(selected));
 		setBackground(bgColor(selected));
 		markAsDirty(this);
 	}
 
 	protected Color fgColor(boolean isSel) {
 		if(isSel)
 			return BLUE_GRAY;
 		else 
 			return DARK_GRAY;
 	}
 	
 	protected Color bgColor(boolean isSel) {
 		if(isSel)
 			return LIGHT_GRAY;
 		else
 			return MID_GRAY;
 	}
 	
 	public void setIcon(Icon icon) {
 		lbl.setIcon(icon);
 	}
 	
 	public void setBold(boolean bold) {
 		lbl.setFont(RoboFont.getFont(12, bold));
 	}
 	
 	public void setText(String text) {
 		lbl.setText(text);
 	}
 	
 	protected JPopupMenu getPopupMenu() {
 		return null;
 	}
 	
 	class MouseListener extends MouseAdapter {
 		@Override
 		public void mousePressed(MouseEvent e) {
 			setSelected(true);
 			maybeShowPopup(e);
 			e.consume();
 		}
 		
 		public void mouseReleased(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		private void maybeShowPopup(MouseEvent e) {
 			if (!e.isPopupTrigger())
 				return;
 			JPopupMenu popup = getPopupMenu();
 			if(popup != null)
 				popup.show(e.getComponent(), e.getX(), e.getY());
 		}
 
 	}
 }
