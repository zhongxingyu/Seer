 package org.windom.story.ui.impl;
 
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JLabel;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 
 import org.windom.story.game.action.Action;
 
 @SuppressWarnings("serial")
 public class ActionButton extends JLabel implements MouseListener {
 	
 	private final int index;
 	private Action action;
 	private final ActionHandler actionHandler;
 	private boolean hovered;
 	
 	public ActionButton(int index,Action action,ActionHandler actionHandler) {
 		this.index = index;
 		this.action = action;
 		this.actionHandler = actionHandler;
 		this.hovered = false;
 		
 		setFont(Constants.FONT);		
 		setBorder(new CompoundBorder(Constants.BORDER,new EmptyBorder(5,5,5,5)));
 		setCursor(Constants.HAND_CURSOR);	
 		setOpaque(true);
 		
 		updateAppearance();
 		
 		addMouseListener(this);
 	}
 	
 	private void setColors(Color[] colorScheme) {
 		setForeground(colorScheme[0]);
 		setBackground(colorScheme[1]);
 	}
 	
 	private void updateAppearance() {
 		setColors(action.isEnabled() ? Constants.AB_NORMAL : Constants.AB_NORMAL_DISABLED);
 		setText(index >= 0
 			? String.format(Constants.AB_FORMAT,index,action.getLabel())
 			: action.getLabel()
 		);
 	}
 
 	public Action getAction() {
 		return action;
 	}
 	
 	public void updateAction(Action action) {
 		boolean isHovered = hovered;
 		if (isHovered) mouseExited(null);
		if (action != null) {
			this.action = action;
			updateAppearance();
			if (isHovered) mouseEntered(null);
		}
 	}
 	
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		hovered = true;
 		setColors(action.isEnabled() ? Constants.AB_HOVER : Constants.AB_HOVER_DISABLED);
 		actionHandler.actionEntered(action);
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		hovered = false;
 		setColors(action.isEnabled() ? Constants.AB_NORMAL : Constants.AB_NORMAL_DISABLED);
 		actionHandler.actionExited(action);
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (action.isEnabled()) {
 			setColors(Constants.AB_PRESSED);
 		}
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (action.isEnabled()) {
 			setColors(Constants.AB_HOVER);
 			actionHandler.actionPressed(action);
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {	
 	}
 	
 }
