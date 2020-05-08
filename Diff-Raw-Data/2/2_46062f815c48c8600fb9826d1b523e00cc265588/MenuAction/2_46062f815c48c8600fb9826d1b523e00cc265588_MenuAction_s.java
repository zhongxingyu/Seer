 /*-
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.common.ui.menu;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.IMenuCreator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 
 /**
  * Simple action which will have other actions in a drop down menu.
  */
 public class MenuAction extends Action implements IMenuCreator {
 	
 	private Menu fMenu;
 	private List<IAction> actions;
 	private Action selectedAction;
 	private List<Integer> indexes;
 	private int iSeparator;
 
 	public MenuAction(final String text) {
 		super(text, IAction.AS_DROP_DOWN_MENU);
 		setMenuCreator(this);
 		this.actions = new ArrayList<IAction>(7);
 		this.iSeparator = 0;
 		this.indexes = new ArrayList<Integer>();
 	}
 
 
 	@Override
 	public void dispose() {
 		if (fMenu != null)  {
 			fMenu.dispose();
 			fMenu= null;
 		}
 	}
 
 
 	@Override
 	public Menu getMenu(Menu parent) {
 		if (fMenu != null) fMenu.dispose();
 
 		fMenu= new Menu(parent);
 
 		for (IAction action : actions) {
 			addActionToMenu(fMenu, action);
 		}
 		for (int i=0; i<iSeparator;i++)
 			addSeparatorToMenu(fMenu, i);
 		return fMenu;
 	}
 
 	public void add(final IAction action) {
 		actions.add(action);
 	}
 
 	@Override
 	public Menu getMenu(Control parent) {
 		if (fMenu != null) fMenu.dispose();
 
 		fMenu= new Menu(parent);
 
 		for (IAction action : actions) {
 			addActionToMenu(fMenu, action);
 		}
 		for (int i=0; i<iSeparator;i++)
 			addSeparatorToMenu(fMenu, i);
 		return fMenu;
 	}
 
 
 	protected void addActionToMenu(Menu parent, IAction action) {
 		ActionContributionItem item= new ActionContributionItem(action);
 		item.fill(parent, -1);
 	}
 
 
 	/**
 	 * Get's rid of the menu, because the menu hangs on to * the searches, etc.
 	 */
 	public void clear() {
 		actions.clear();
 	}
 
 	public void setSelectedAction(int iAction) {
 		setSelectedAction(actions.get(iAction));
 	}
 	public void setCheckedAction(int iAction, boolean isChecked) {
 		actions.get(iAction).setChecked(isChecked);
 	}
 	public IAction getAction(int iAction) {
 		return actions.get(iAction);
 	}
 	public int size() {
 		if (actions==null) return 0;
 		return actions.size();
 	}
 	
 	public void addActionsTo(final MenuAction man) {
 		for (IAction action : actions) {
 			man.add(action);
 		}
 	}
 
 	public void setSelectedAction(IAction action) {
 		if (action.getImageDescriptor()!=null) this.setImageDescriptor(action.getImageDescriptor());
 		setText(action.getText());
 		//setToolTipText(action.getToolTipText());
 		this.selectedAction = (Action)action;
 	}
 
 	public void addSeparator() {
 		indexes.add(actions.size()+iSeparator);
 		iSeparator++;
 	}
 
 	protected void addSeparatorToMenu(Menu parent, int index) {
 		new MenuItem(parent, SWT.SEPARATOR, indexes.get(index));
 	}
 
 	public void run() {
 		if (selectedAction!=null) selectedAction.run();
 	}
 	
 	public String toString() {
 		if (getText()!=null) return getText();
 		if (getToolTipText()!=null) return getToolTipText();
 		return super.toString();
 	}
 
 
 	public IAction findAction(String id) {
 		for (IAction action : actions) {
 			if (id.equals(action.getId())) return action;
 		}
 		return null;
 	}
 
 
 	public boolean isEmpty() {
 		return actions==null || actions.isEmpty();
 	}
 }
