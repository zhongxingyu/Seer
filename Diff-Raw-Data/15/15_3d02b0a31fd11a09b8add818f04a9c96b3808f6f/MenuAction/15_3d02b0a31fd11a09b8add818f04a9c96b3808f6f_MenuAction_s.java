 /*
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
 import org.eclipse.jface.action.IMenuCreator;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 
 /**
  * Simple action which will have other actions in a drop down menu.
  */
 public class MenuAction extends Action implements IMenuCreator {
 	
 	private Menu fMenu;
 	private List<IAction> actions;
 	private Action selectedAction;
 
 	public MenuAction(final String text) {
 		super(text, IAction.AS_DROP_DOWN_MENU);
 		setMenuCreator(this);
 		this.actions = new ArrayList<IAction>(7);
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
 
 	public void setSelectedAction(IAction action) {
 		if (action.getImageDescriptor()!=null) this.setImageDescriptor(action.getImageDescriptor());
 		setText(action.getText());
 		//setToolTipText(action.getToolTipText());
 		this.selectedAction = (Action)action;
 	}
 	
 	public void run() {
 		if (selectedAction!=null) selectedAction.run();
 	}
 }
