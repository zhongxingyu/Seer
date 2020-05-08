 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnPixelData;
 
 import org.eclipse.riena.beans.common.AbstractBean;
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonFactory;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IBrowserRidget;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.ILabelRidget;
 import org.eclipse.riena.ui.ridgets.ILinkRidget;
 import org.eclipse.riena.ui.ridgets.IListRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ISpinnerRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.ITraverseRidget;
 import org.eclipse.riena.ui.ridgets.listener.ISelectionListener;
 import org.eclipse.riena.ui.ridgets.listener.SelectionEvent;
 
 /**
  * Example Controller with as many ridgets as possible. Used for controller
  * testing.
  */
 public class ControllerTestsPlaygroundSubModuleController extends SubModuleController {
 	private ITableRidget multiTable;
 	private IListRidget tableList;
 	private Temperature temperature;
 	private ITraverseRidget celsiusScale;
 	private ISpinnerRidget fahrenheitSpinner;
 
 	/**
 	 * 
 	 */
 	public ControllerTestsPlaygroundSubModuleController() {
 		temperature = new Temperature();
 		temperature.setKelvin(273.15f);
 	}
 
 	@Override
 	public void configureRidgets() {
 		configureTableGroup();
 		configureComboGroup();
 		configureBrowserGroup();
 		configureTraverseGroup();
 		//		configureMasterDetailsGroup();
 		//TODO work in progress
 	}
 
 	//	private void configureMasterDetailsGroup() {
 	//		String[] properties = new String[] { "firstname", "lastname" }; //$NON-NLS-1$ //$NON-NLS-2$
 	//		String[] headers = new String[] { "First Name", "Last Name" }; //$NON-NLS-1$ //$NON-NLS-2$
 	//
 	//		IMasterDetailsRidget master = getRidget(IMasterDetailsRidget.class, "master"); //$NON-NLS-1$
 	//		master.setDelegate(new PersonDelegate());
 	//		master.bindToModel(createPersonList(), Person.class, properties, headers);
 	//		master.updateFromModel();
 	//		master.setApplyRequiresNoErrors(true);
 	//
 	//		IActionRidget actionApply = master.getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_APPLY);
 	//		actionApply.setIcon("apply_h.png"); //$NON-NLS-1$
 	//
 	//		IActionRidget actionNew = master.getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_NEW);
 	//		actionNew.setText(""); //$NON-NLS-1$
 	//		actionNew.setIcon("new_h.png"); //$NON-NLS-1$
 	//
 	//		IActionRidget actionRemove = master.getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_REMOVE);
 	//		actionRemove.setText(""); //$NON-NLS-1$
 	//		actionRemove.setIcon("remove_h.png"); //$NON-NLS-1$
 	//	}
 
 	private void configureBrowserGroup() {
 		ILinkRidget link1 = getRidget(ILinkRidget.class, "link1"); //$NON-NLS-1$
 		link1.setText("<a>http://www.eclipse.org/</a>"); //$NON-NLS-1$
 
 		ILinkRidget link2 = getRidget(ILinkRidget.class, "link2"); //$NON-NLS-1$
 		link2.setText("Visit <a href=\"http://www.eclipse.org/riena/\">Riena</a>"); //$NON-NLS-1$
 
 		ILinkRidget link3 = getRidget(ILinkRidget.class, "link3"); //$NON-NLS-1$
 		link3
 				.setText("Eclipse <a href=\"http://planeteclipse.org\">Blogs</a>, <a href=\"http://www.eclipse.org/community/news/\">News</a> and <a href=\"http://live.eclipse.org\">Events</a>"); //$NON-NLS-1$
 
 		final ITextRidget textLinkUrl = getRidget(ITextRidget.class, "textLinkUrl"); //$NON-NLS-1$
 		textLinkUrl.setOutputOnly(true);
 
 		final IBrowserRidget browser = getRidget(IBrowserRidget.class, "browser"); //$NON-NLS-1$
 		browser.bindToModel(textLinkUrl, ITextRidget.PROPERTY_TEXT);
 
 		ISelectionListener listener = new ISelectionListener() {
 			public void ridgetSelected(SelectionEvent event) {
 				String linkUrl = (String) event.getNewSelection().get(0);
 				browser.setUrl(linkUrl);
 			}
 		};
 		link1.addSelectionListener(listener);
 		link2.addSelectionListener(listener);
 		link3.addSelectionListener(listener);
 
 	}
 
 	private void configureComboGroup() {
 		final ITextRidget comboText = getRidget(ITextRidget.class, "comboTextField"); //$NON-NLS-1$
 
 		final List<String> ages = new ArrayList<String>(Arrays.asList(new String[] {
 				"<none>", "young", "moderate", "aged", "old" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 
 		final ILabelRidget comboLabel = getRidget(ILabelRidget.class, "comboLabel"); //$NON-NLS-1$
 		comboLabel.setText(ages.get(0));
 
 		final IComboRidget comboAge = getRidget(IComboRidget.class, "ageCombo"); //$NON-NLS-1$
 		comboAge.bindToModel(new WritableList(ages, String.class), String.class, null, new WritableValue());
 		comboAge.updateFromModel();
 		comboAge.setEmptySelectionItem("<none>"); //$NON-NLS-1$
 		comboAge.setSelection(0);
 		comboAge.addSelectionListener(new ISelectionListener() {
 			public void ridgetSelected(SelectionEvent event) {
 				comboLabel.setText(event.getNewSelection().get(0).toString());
 			}
 		});
 
 		IActionRidget addToComboButton = getRidget(IActionRidget.class, "addToComboButton"); //$NON-NLS-1$
 		addToComboButton.addListener(new IActionListener() {
 			public void callback() {
 				String comboString = comboText.getText();
				if (!comboString.isEmpty()) {
 					ages.add(comboString);
 				}
 				comboAge.bindToModel(new WritableList(ages, String.class), String.class, null, new WritableValue());
 				comboAge.updateFromModel();
 				comboAge.setSelection(comboAge.getObservableList().size() - 1);
 			}
 		});
 	}
 
 	private void configureTableGroup() {
 		multiTable = getRidget(ITableRidget.class, "multiTable"); //$NON-NLS-1$
 		ColumnLayoutData[] widths = { new ColumnPixelData(80, true), new ColumnPixelData(80, true) };
 		multiTable.setColumnWidths(widths);
 		multiTable.setSelectionType(ISelectableRidget.SelectionType.MULTI);
 		String[] colValues = new String[] { "lastname", "firstname" }; //$NON-NLS-1$ //$NON-NLS-2$
 		String[] colHeaders = new String[] { "Last Name", "First Name" }; //$NON-NLS-1$ //$NON-NLS-2$
 		multiTable.bindToModel(createPersonList(), Person.class, colValues, colHeaders);
 		multiTable.updateFromModel();
 
 		tableList = getRidget(IListRidget.class, "tableList"); //$NON-NLS-1$
 
 		tableList.setSelectionType(ISelectableRidget.SelectionType.MULTI);
 
 		final IActionRidget copySelectionButton = getRidget(IActionRidget.class, "copySelectionButton"); //$NON-NLS-1$
 		copySelectionButton.addListener(new IActionListener() {
 			public void callback() {
 				List<Object> selection = multiTable.getSelection();
 				tableList.bindToModel(new WritableList(selection, Person.class), Person.class, "listEntry"); //$NON-NLS-1$
 				tableList.updateFromModel();
 			}
 		});
 	}
 
 	private void configureTraverseGroup() {
 		TemperatureListener listener = new TemperatureListener();
 
 		fahrenheitSpinner = getRidget(ISpinnerRidget.class, "fahrenheitSpinner"); //$NON-NLS-1$
 		fahrenheitSpinner.setIncrement(1);
 		fahrenheitSpinner.setMaximum(122);
 		fahrenheitSpinner.setMinimum(32);
 		fahrenheitSpinner.bindToModel(BeansObservables.observeValue(temperature,
 				Temperature.PROPERTY_DEGREE_FAHRENHEITN));
 		fahrenheitSpinner.updateFromModel();
 		fahrenheitSpinner.addListener(listener);
 
 		celsiusScale = getRidget(ITraverseRidget.class, "celsiusScale"); //$NON-NLS-1$
 		celsiusScale.setIncrement(1);
 		celsiusScale.setMaximum(50);
 		celsiusScale.setMinimum(0);
 		celsiusScale.bindToModel(BeansObservables.observeValue(temperature, Temperature.PROPERTY_DEGREE_CELSIUS));
 		celsiusScale.updateFromModel();
 		celsiusScale.addListener(listener);
 	}
 
 	private class TemperatureListener implements IActionListener {
 		public void callback() {
 			celsiusScale.updateFromModel();
 			fahrenheitSpinner.updateFromModel();
 		}
 	}
 
 	private class Temperature extends AbstractBean {
 
 		static final String PROPERTY_DEGREE_CELSIUS = "degreeCelsius"; //$NON-NLS-1$
 		static final String PROPERTY_DEGREE_FAHRENHEITN = "degreeFahrenheit"; //$NON-NLS-1$
 
 		private float kelvin;
 		private int degreeCelsius;
 		private int degreeFahrenheit;
 
 		public Temperature() {
 			setDegreeCelsius(0);
 		}
 
 		public void setDegreeCelsius(int degreeCelsius) {
 			setDegreeCelsius(degreeCelsius, true);
 		}
 
 		private void setDegreeCelsius(int degreeCelsius, boolean updateKelvin) {
 			int oldValue = this.degreeCelsius;
 			this.degreeCelsius = degreeCelsius;
 			if (updateKelvin) {
 				float k = degreeCelsius + 273.15f;
 				setKelvin(k);
 				updateFahrenheit();
 			}
 			firePropertyChanged(PROPERTY_DEGREE_CELSIUS, oldValue, degreeCelsius);
 		}
 
 		@SuppressWarnings("unused")
 		public int getDegreeCelsius() {
 			return degreeCelsius;
 		}
 
 		@SuppressWarnings("unused")
 		public void setDegreeFahrenheit(int degreeFahrenheit) {
 			setDegreeFahrenheit(degreeFahrenheit, true);
 		}
 
 		private void setDegreeFahrenheit(int degreeFahrenheit, boolean updateKelvin) {
 			int oldValue = this.degreeFahrenheit;
 			this.degreeFahrenheit = degreeFahrenheit;
 			if (updateKelvin) {
 				float c = (degreeFahrenheit - 32) / 1.8f;
 				float k = c + 273.15f;
 				setKelvin(k);
 				updateCelsius();
 			}
 			firePropertyChanged(PROPERTY_DEGREE_FAHRENHEITN, oldValue, degreeFahrenheit);
 		}
 
 		@SuppressWarnings("unused")
 		public int getDegreeFahrenheit() {
 			return degreeFahrenheit;
 		}
 
 		private void setKelvin(float kelvin) {
 			this.kelvin = kelvin;
 		}
 
 		private float getKelvin() {
 			return kelvin;
 		}
 
 		private void updateCelsius() {
 			int c = Math.round(getKelvin() - 273.15f);
 			setDegreeCelsius(c, false);
 		}
 
 		private void updateFahrenheit() {
 			int c = Math.round(getKelvin() - 273.15f);
 			int f = Math.round(c * 1.8f + 32);
 			setDegreeFahrenheit(f, false);
 		}
 	}
 
 	private WritableList createPersonList() {
 		return new WritableList(PersonFactory.createPersonList(), Person.class);
 	}
 
 	//	/**
 	//	 * Setup the ridgets for editing a person (text ridgets for name, single
 	//	 * choice ridget for gender, multiple choice ridgets for pets).
 	//	 */
 	//	private static final class PersonDelegate implements IMasterDetailsDelegate {
 	//
 	//		private static final String[] GENDER = { Person.FEMALE, Person.MALE };
 	//
 	//		private final Person workingCopy = createWorkingCopy();
 	//
 	//		public void configureRidgets(IRidgetContainer container) {
 	//			ITextRidget txtFirst = container.getRidget(ITextRidget.class, "first"); //$NON-NLS-1$
 	//			txtFirst.setMandatory(true);
 	//			txtFirst.setDirectWriting(true);
 	//			txtFirst.bindToModel(workingCopy, Person.PROPERTY_FIRSTNAME);
 	//			txtFirst.updateFromModel();
 	//
 	//			ITextRidget txtLast = container.getRidget(ITextRidget.class, "last"); //$NON-NLS-1$
 	//			txtLast.setMandatory(true);
 	//			txtLast.setDirectWriting(true);
 	//			txtLast.addValidationRule(new NotEmpty(), ValidationTime.ON_UI_CONTROL_EDIT);
 	//			txtLast.bindToModel(workingCopy, Person.PROPERTY_LASTNAME);
 	//			txtLast.updateFromModel();
 	//
 	//			ISingleChoiceRidget gender = container.getRidget(ISingleChoiceRidget.class, "gender"); //$NON-NLS-1$
 	//			if (gender != null) {
 	//				gender.bindToModel(Arrays.asList(GENDER), (List<String>) null, workingCopy, Person.PROPERTY_GENDER);
 	//				gender.updateFromModel();
 	//			}
 	//
 	//			IMultipleChoiceRidget pets = container.getRidget(IMultipleChoiceRidget.class, "pets"); //$NON-NLS-1$
 	//			if (pets != null) {
 	//				pets.bindToModel(Arrays.asList(Person.Pets.values()), (List<String>) null, workingCopy,
 	//						Person.PROPERTY_PETS);
 	//				pets.updateFromModel();
 	//			}
 	//		}
 	//
 	//		public Person createWorkingCopy() {
 	//			return new Person("", ""); //$NON-NLS-1$ //$NON-NLS-2$
 	//		}
 	//
 	//		public Person copyBean(final Object source, final Object target) {
 	//			Person from = source != null ? (Person) source : createWorkingCopy();
 	//			Person to = target != null ? (Person) target : createWorkingCopy();
 	//			to.setFirstname(from.getFirstname());
 	//			to.setLastname(from.getLastname());
 	//			to.setGender(from.getGender());
 	//			to.setPets(from.getPets());
 	//			return to;
 	//		}
 	//
 	//		public Object getWorkingCopy() {
 	//			return workingCopy;
 	//		}
 	//
 	//		public void updateDetails(IRidgetContainer container) {
 	//			for (IRidget ridget : container.getRidgets()) {
 	//				ridget.updateFromModel();
 	//			}
 	//		}
 	//
 	//		public boolean isChanged(Object source, Object target) {
 	//			Person p1 = (Person) source;
 	//			Person p2 = (Person) target;
 	//			boolean equals = p1.getFirstname().equals(p2.getFirstname()) && p1.getLastname().equals(p2.getLastname())
 	//					&& p1.getGender().equals(p2.getGender()) && p1.getPets().equals(p2.getPets());
 	//			return !equals;
 	//		}
 	//
 	//		public String isValid(IRidgetContainer container) {
 	//			ITextRidget txtLast = (ITextRidget) container.getRidget("last"); //$NON-NLS-1$
 	//			if (txtLast.isErrorMarked()) {
 	//				return "'Last Name' is not valid."; //$NON-NLS-1$
 	//			}
 	//			return null;
 	//		}
 	//	}
 }
