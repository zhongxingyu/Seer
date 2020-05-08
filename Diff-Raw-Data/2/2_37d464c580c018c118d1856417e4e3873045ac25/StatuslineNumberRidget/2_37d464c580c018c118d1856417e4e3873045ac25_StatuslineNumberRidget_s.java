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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import org.eclipse.riena.ui.ridgets.AbstractMarkerSupport;
 import org.eclipse.riena.ui.ridgets.IStatuslineNumberRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.swt.StatuslineNumber;
 
 /**
  * Ridget for composite of the status line to display a number (
  * {@link StatuslineNumber}).
  */
 public class StatuslineNumberRidget extends AbstractSWTRidget implements IStatuslineNumberRidget {
 
 	private Integer number;
 	private String numberString;
 
 	@Override
 	protected AbstractMarkerSupport createMarkerSupport() {
 		return new BasicMarkerSupport(this, propertyChangeSupport);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IStatuslineNumberRidget#getNumber()
 	 */
 	public Integer getNumber() {
 		return number;
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IStatuslineNumberRidget#getNumberString()
 	 */
 	public String getNumberString() {
 		return numberString;
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IStatuslineNumberRidget#setNumber(java.lang
 	 *      .Integer)
 	 */
 	public void setNumber(Integer number) {
 		this.number = number;
 
 		if (this.number == null) {
 			numberString = ""; //$NON-NLS-1$
 		} else {
 			// TODO use Numberformatter instead of toString()
 			numberString = number.toString();
 		}
 		if (getUIControl() != null) {
 			getUIControl().setNumber(number);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IStatuslineNumberRidget#setNumberString(java
 	 *      .lang.String)
 	 */
 	public void setNumberString(String numberStrg) {
 		this.numberString = numberStrg;
 
 		if (this.numberString == null) {
 			number = null;
 		} else {
 			// TODO use Numberformatter instead of toString()
			number = Integer.getInteger(numberString);
 		}
 		getUIControl().setNumber(numberString);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget#getUIControl()
 	 */
 	@Override
 	public StatuslineNumber getUIControl() {
 		return (StatuslineNumber) super.getUIControl();
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget#checkUIControl
 	 *      (java.lang.Object)
 	 */
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, StatuslineNumber.class);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget#bindUIControl ()
 	 */
 	@Override
 	protected void bindUIControl() {
 		// unused
 	}
 
 	/**
 	 * Always returns true because mandatory markers do not make sense for this
 	 * ridget.
 	 */
 	@Override
 	public boolean isDisableMandatoryMarker() {
 		return true;
 	}
 
 }
