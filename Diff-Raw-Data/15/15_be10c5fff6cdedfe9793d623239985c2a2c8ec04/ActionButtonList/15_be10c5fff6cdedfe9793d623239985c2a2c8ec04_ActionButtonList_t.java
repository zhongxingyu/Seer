 package org.windom.story.ui.impl;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.KeyEvent;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import org.windom.story.game.action.Action;
 
 @SuppressWarnings("serial")
 public class ActionButtonList extends JPanel implements KeyEventDispatcher {
 
 	private final ActionHandler actionHandler;
 	
 	public ActionButtonList(ActionHandler actionHandler) {
 		this.actionHandler = actionHandler;
 		
 		GridBagLayout gbl = new GridBagLayout();
 		gbl.columnWeights = new double[] { 1 };
 		setLayout(gbl);
 		
 		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		kfm.addKeyEventDispatcher(this);
 	}
 	
 	public int getLength() {
 		return getComponentCount();
 	}
 	
 	public ActionButton getActionButton(int idx) {
 		return (ActionButton) getComponent(idx);
 	}
 	
 	public void setActions(List<Action> actions) {
 		setVisible(false);
 		while (getLength() > actions.size()) {
 			removeActionButton(actions.size());
 		}
 		int idx = 0;
 		for (; idx<getLength(); idx++) {
 			updateActionButton(idx,actions.size(),actions.get(idx));
 		}
 		for (; idx<actions.size(); idx++) {
 			addActionButton(idx,actions.size(),actions.get(idx));
 		}		
 		setVisible(true);
 	}
 	
 	private void addActionButton(int idx,int count,Action action) {
 		GridBagConstraints gbc = getActionButtonGbc(idx,count);
 		ActionButton actionButton = new ActionButton(idx+1,action,actionHandler);
 		add(actionButton,gbc);
 	}
 	
 	private void updateActionButton(int idx,int count,Action action) {
 		ActionButton actionButton = getActionButton(idx);
 		GridBagLayout gbl = (GridBagLayout)actionButton.getParent().getLayout();
 		GridBagConstraints gbc = getActionButtonGbc(idx,count);
 		gbl.setConstraints(actionButton,gbc);
 		actionButton.updateAction(action);
 	}
 	
 	private void removeActionButton(int idx) {
		ActionButton actionButton = getActionButton(idx);
		actionButton.updateAction(null);
 		remove(idx);
 	}
 	
 	private GridBagConstraints getActionButtonGbc(int idx,int count) {
 		GridBagConstraints gbc = new GridBagConstraints();
 		if (idx != count-1) {
 			gbc.insets = new Insets(0, 0, 5, 0);
 		} else {
 			gbc.weighty = 1;
 		}
 		gbc.anchor = GridBagConstraints.NORTH;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridx = 0;
 		gbc.gridy = idx;
 		return gbc;
 	}
 	
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent e) {
 		if (e.getID() == KeyEvent.KEY_PRESSED) {			
 			int numPressed = e.getKeyChar() - '0';			
 			if (numPressed >= 1 && numPressed <= getLength()) {
 				Action action = getActionButton(numPressed-1).getAction();
 				if (action.isEnabled()) {
 					actionHandler.actionPressed(action);
 				}
 			}
 		}
 		return false;
 	}
 	
 }
