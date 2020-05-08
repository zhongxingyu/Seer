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
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonFactory;
 import org.eclipse.riena.example.client.views.MasterDetailsSubModuleView;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 
 /**
  * Demonstrates use of a master/details ridget.
  * 
  * @see IMasterDetailsRidget
  * @see MasterDetailsSubModuleView
  */
 public class MasterDetailsSubModuleController extends SubModuleController {
 
 	/**
 	 * TODO [ev] javadoc
 	 */
 	public static final class PersonDelegate implements IMasterDetailsDelegate {
 
 		private static final String[] GENDER = { Person.FEMALE, Person.MALE };
 
 		private final Person workingCopy = createWorkingCopy();
 
 		public void configureRidgets(IRidgetContainer container) {
 			ITextRidget txtFirst = (ITextRidget) container.getRidget("first"); //$NON-NLS-1$
 			txtFirst.setMandatory(true);
 			txtFirst.bindToModel(workingCopy, Person.PROPERTY_FIRSTNAME);
 			txtFirst.updateFromModel();
 
 			ITextRidget txtLast = (ITextRidget) container.getRidget("last"); //$NON-NLS-1$
 			txtLast.setMandatory(true);
 			txtLast.bindToModel(workingCopy, Person.PROPERTY_LASTNAME);
 			txtLast.updateFromModel();
 
 			ISingleChoiceRidget gender = (ISingleChoiceRidget) container.getRidget("gender"); //$NON-NLS-1$
 			gender.bindToModel(Arrays.asList(GENDER), (List<String>) null, workingCopy, Person.PROPERTY_GENDER);
 			gender.updateFromModel();
 
 			IMultipleChoiceRidget pets = (IMultipleChoiceRidget) container.getRidget("pets"); //$NON-NLS-1$
 			pets.bindToModel(Arrays.asList(Person.Pets.values()), (List<String>) null, workingCopy,
 					Person.PROPERTY_PETS);
 			pets.updateFromModel();
 		}
 
 		public Person createWorkingCopy() {
 			return new Person("", ""); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		public Person copyBean(final Object source, final Object target) {
 			Person from = source != null ? (Person) source : createWorkingCopy();
 			Person to = target != null ? (Person) target : createWorkingCopy();
 			to.setFirstname(from.getFirstname());
 			to.setLastname(from.getLastname());
 			to.setGender(from.getGender());
 			to.setPets(from.getPets());
 			return to;
 		}
 
 		public Object getWorkingCopy() {
 			return workingCopy;
 		}
 
 		public void updateDetails(IRidgetContainer container) {
 			for (IRidget ridget : container.getRidgets()) {
 				ridget.updateFromModel();
 			}
 		}
 
 		public boolean isChanged(Object source, Object target) {
 			return true;
 		}
 
 		public boolean isValid(IRidgetContainer container) {
 			// TODO [ev] dissalow cat and dog
 			return true;
 		}
 	}
 
 	private List<Person> input = PersonFactory.createPersonList();
 
 	@Override
	public void configureRidgets() {
 		IMasterDetailsRidget master = (IMasterDetailsRidget) getRidget("master"); //$NON-NLS-1$
 		master.setDelegate(new PersonDelegate());
 		String[] properties = new String[] { "firstname", "lastname" }; //$NON-NLS-1$ //$NON-NLS-2$
 		String[] headers = new String[] { "First Name", "Last Name" }; //$NON-NLS-1$ //$NON-NLS-2$
 		master.bindToModel(new WritableList(input, Person.class), Person.class, properties, headers);
 		master.updateFromModel();
 	}
 
 }
