 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Comparator;
 
 import org.eclipse.riena.example.client.views.TextView;
 import org.eclipse.riena.internal.example.client.beans.PersonFactory;
 import org.eclipse.riena.internal.example.client.beans.PersonModificationBean;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleNodeViewController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextFieldRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.util.beans.Person;
 import org.eclipse.riena.ui.ridgets.util.beans.PersonManager;
 
 /**
  * Controller for the {@link TextView} example.
  */
 public class ListViewController extends SubModuleNodeViewController {
 
 	private ITableRidget listPersons;
 	private ITextFieldRidget textFirst;
 	private ITextFieldRidget textLast;
 	private IToggleButtonRidget buttonSort;
 	private IActionRidget buttonAdd;
 	private IActionRidget buttonRemove;
 	private IActionRidget buttonSave;
 
 	/** Manages a collection of persons. */
 	private final PersonManager manager;
 	/** Holds editable data for a person. */
 	private final PersonModificationBean value;
 
 	public ListViewController(ISubModuleNode navigationNode) {
 		super(navigationNode);
 		manager = new PersonManager(PersonFactory.createPersonList());
 		manager.setSelectedPerson(manager.getPersons().iterator().next());
 		value = new PersonModificationBean();
 	}
 
 	public ITableRidget getListPersons() {
 		return listPersons;
 	}
 
 	public void setListPersons(ITableRidget listPersons) {
 		this.listPersons = listPersons;
 	}
 
 	public ITextFieldRidget getTextFirst() {
 		return textFirst;
 	}
 
 	public void setTextFirst(ITextFieldRidget textFirst) {
 		this.textFirst = textFirst;
 	}
 
 	public ITextFieldRidget getTextLast() {
 		return textLast;
 	}
 
 	public void setTextLast(ITextFieldRidget textLast) {
 		this.textLast = textLast;
 	}
 
 	public IToggleButtonRidget getButtonSort() {
 		return buttonSort;
 	}
 
 	public void setButtonSort(IToggleButtonRidget buttonSort) {
 		this.buttonSort = buttonSort;
 	}
 
 	public IActionRidget getButtonAdd() {
 		return buttonAdd;
 	}
 
 	public void setButtonAdd(IActionRidget buttonAdd) {
 		this.buttonAdd = buttonAdd;
 	}
 
 	public IActionRidget getButtonRemove() {
 		return buttonRemove;
 	}
 
 	public void setButtonRemove(IActionRidget buttonRemove) {
 		this.buttonRemove = buttonRemove;
 	}
 
 	public IActionRidget getButtonSave() {
 		return buttonSave;
 	}
 
 	public void setButtonSave(IActionRidget buttonSave) {
 		this.buttonSave = buttonSave;
 	}
 
 	@Override
 	public void afterBind() {
 		super.afterBind();
 		initRidgets();
 	}
 
 	/**
 	 * Binds and updates the ridgets.
 	 */
 	private void initRidgets() {
 		listPersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		listPersons.setComparator(0, new StringComparator());
		listPersons.setSortedColumn(0);
 		listPersons.bindToModel(manager, "persons", Person.class, new String[] { "listEntry" }, new String[] { "" }); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
 		listPersons.updateFromModel();
 
 		listPersons.bindSingleSelectionToModel(manager, PersonManager.PROPERTY_SELECTED_PERSON);
 
 		textFirst.bindToModel(value, "firstName"); //$NON-NLS-1$
 		textFirst.updateFromModel();
 		textLast.bindToModel(value, "lastName"); //$NON-NLS-1$
 		textLast.updateFromModel();
 
 		listPersons.addPropertyChangeListener(ITableRidget.PROPERTY_SINGLE_SELECTION, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				value.setPerson(manager.getSelectedPerson());
 				textFirst.updateFromModel();
 				textLast.updateFromModel();
 			}
 		});
 
 		buttonSort.setText("Sort ascending");
 		buttonSort.setSelected(true);
 		listPersons.setSortedAscending(buttonSort.isSelected());
 		buttonSort.addListener(new IActionListener() {
 			public void callback() {
 				boolean ascending = buttonSort.isSelected();
 				listPersons.setSortedAscending(ascending);
 			}
 		});
 
 		buttonAdd.setText("&Add");
 		buttonAdd.addListener(new IActionListener() {
 			private int count = 0;
 
 			public void callback() {
 				Person newPerson = new Person("Average", "Joe #" + ++count);
 				manager.getPersons().add(newPerson);
 				listPersons.updateFromModel();
 				manager.setSelectedPerson(newPerson);
 				listPersons.updateSingleSelectionFromModel();
 			}
 		});
 
 		buttonRemove.setText("&Remove");
 		buttonRemove.addListener(new IActionListener() {
 			public void callback() {
 				Person selPerson = manager.getSelectedPerson();
 				if (selPerson != null) {
 					manager.getPersons().remove(selPerson);
 					listPersons.updateFromModel();
 					manager.setSelectedPerson(null);
 				}
 			}
 		});
 
 		buttonSave.setText("&Save");
 		buttonSave.addListener(new IActionListener() {
 			public void callback() {
 				value.update();
 				listPersons.updateFromModel();
 			}
 		});
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Compares two strings.
 	 */
 	private static final class StringComparator implements Comparator<Object> {
 		public int compare(Object o1, Object o2) {
 			String s1 = (String) o1;
 			String s2 = (String) o2;
 			return s1.compareTo(s2);
 		}
 	}
 }
