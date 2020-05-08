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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.Collection;
 
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.riena.ui.ridgets.IEditableRidget;
 import org.eclipse.riena.ui.ridgets.validation.IValidationRuleStatus;
 
 /**
  * Abstract implementation of an {@link IEditableRidget} for SWT.
  */
 public abstract class AbstractEditableRidget extends AbstractValueRidget implements IEditableRidget {
 
 	private boolean isFlashInProgress = false;
 
 	public void addValidationRule(IValidator validationRule) {
 		Assert.isNotNull(validationRule);
 		getValueBindingSupport().addValidationRule(validationRule);
 	}
 
 	public IConverter getUIControlToModelConverter() {
 		return getValueBindingSupport().getUIControlToModelConverter();
 	}
 
 	public Collection<IValidator> getValidationRules() {
 		return getValueBindingSupport().getValidationRules();
 	}
 
 	public void removeValidationRule(IValidator validationRule) {
 		getValueBindingSupport().removeValidationRule(validationRule);
 	}
 
 	public void setUIControlToModelConverter(IConverter converter) {
 		getValueBindingSupport().setUIControlToModelConverter(converter);
 	}
 
 	/**
 	 * Subclasses should call this method to update validation state of the
 	 * ridget.
 	 * 
 	 * @see IValidationCallback#validationRulesChecked(IStatus)
 	 * 
 	 * @param status
 	 * 		The result of validation.
 	 */
 	public void validationRulesChecked(IStatus status) {
 		if (status.isOK()) {
 			setErrorMarked(false);
 		} else {
 			if (status.getCode() != IValidationRuleStatus.ERROR_BLOCK_WITH_FLASH) {
 				setErrorMarked(true);
 			} else {
 				if (!isFlashInProgress) {
 					isFlashInProgress = true;
 					final boolean oldErrorMarked = isErrorMarked();
 					setErrorMarked(!oldErrorMarked);
 
 					Runnable op = new Runnable() {
 						public void run() {
 							try {
 								Thread.sleep(300);
 							} catch (InterruptedException e) {
 								// ignore
 							} finally {
								// TODO [ev] guard for NPE with getUIControl ?
								getUIControl().getDisplay().asyncExec(new Runnable() {
									public void run() {
										setErrorMarked(oldErrorMarked);
										isFlashInProgress = false;
									}
								});
 							}
 						}
 					};
 					new Thread(op).start();
 				}
 			}
 		}
 	}
 }
