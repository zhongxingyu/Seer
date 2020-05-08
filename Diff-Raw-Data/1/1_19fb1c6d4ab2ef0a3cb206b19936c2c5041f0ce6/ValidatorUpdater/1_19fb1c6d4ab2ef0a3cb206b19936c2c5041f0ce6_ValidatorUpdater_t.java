 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.internal.ui;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.validators.core.IValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.eclipse.dltk.validators.core.ValidatorRuntime;
 import org.eclipse.dltk.validators.internal.core.ValidatorDefinitionsContainer;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 
 
 /**
  * Processes add/removed/changed Interpreters.
  */
 public class ValidatorUpdater {
 	
 	// the Interpreters defined when this updated is instantiated
 	private ValidatorDefinitionsContainer fOriginalValidators;	
 	
 	/**
 	 * Contstructs a new Validator updater to update Validator install settings.
 	 */
 	public ValidatorUpdater() {
 		saveCurrentAsOriginal ();
 	}
 	
 	private void saveCurrentAsOriginal () {
 		fOriginalValidators = new ValidatorDefinitionsContainer();
 	
 		IValidatorType[] types = ValidatorRuntime.getValidatorTypes();
 		for (int i = 0; i < types.length; i++) {
 			IValidator[] validators = types[i].getValidators();
 			if (validators != null)
 				for (int j = 0; j < validators.length; j++) {
 					fOriginalValidators.addValidator(validators[j]);
 				}
 		}
 	}
 	
 	/**
 	 * Updates Validator settings and returns whether the update was successful.
 	 * 
 	 * @param validatorEnvironments new installed ValidatorEnvironments
 	 * @param defaultInterp new default Validator
 	 * @return whether the update was successful
 	 */
 	public boolean updateValidatorSettings(IValidator[] validatorEnvironments) {
 		
 		// Create a Validator definition container
 		ValidatorDefinitionsContainer validatorContainer = new ValidatorDefinitionsContainer();
 		
 		// Set the Validators on the container
 		for (int i = 0; i < validatorEnvironments.length; i++) {
 			validatorContainer.addValidator(validatorEnvironments[i]);
 		}	
 		
 		// Generate XML for the Validator defs and save it as the new value of the Validator preference
 		saveValidatorDefinitions(validatorContainer);
 		
 		saveCurrentAsOriginal ();
 		
 		return true;
 	}
 	
 	private void saveValidatorDefinitions(final ValidatorDefinitionsContainer container) {
 		IRunnableWithProgress runnable = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				try {
 					monitor.beginTask(ValidatorMessages.ValidatorUpdater_0, 100); 
 					String ValidatorDefXML = container.getAsXML();
 					monitor.worked(40);
 					ValidatorRuntime.getPreferences().setValue(ValidatorRuntime.PREF_VALIDATOR_XML, ValidatorDefXML);
 					monitor.worked(30);
 					ValidatorRuntime.savePreferences();
 					monitor.worked(30);
 				} catch (IOException ioe) {
 					ValidatorsUI.log(ioe);
 				} catch (ParserConfigurationException e) {
 					ValidatorsUI.log(e);
 				} catch (TransformerException e) {
 					ValidatorsUI.log(e);
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 		try {
 			ValidatorsUI.getDefault().getWorkbench().getProgressService().busyCursorWhile(runnable);			
 		} catch (InvocationTargetException e) {
 			ValidatorsUI.log(e);
 		} catch (InterruptedException e) {
 			ValidatorsUI.log(e);
 		} 
 	}
 }
