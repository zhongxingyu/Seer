 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.client.composites;
 
 import gda.device.DeviceException;
 import gda.device.ScannableMotionUnits;
 
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeanProperties;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.client.observablemodels.ScannableWrapper;
 import uk.ac.gda.ui.components.NumberEditorControl;
 
 public class MotorPositionEditorControl extends NumberEditorControl {
 
 	private static final Logger logger = LoggerFactory.getLogger(MotorPositionEditorControl.class);
 
 	public MotorPositionEditorControl(Composite parent, int style, ScannableWrapper scannableWrapper, boolean userSpinner) throws Exception {
 		super(parent, style, scannableWrapper, ScannableWrapper.POSITION_PROP_NAME, userSpinner);
 		ctx.bindValue(
 				BeanProperties.value(EDITABLE_PROP_NAME).observe(controlModel),
 				BeanProperties.value(ScannableWrapper.BUSY_PROP_NAME).observe(targetObject),
 				null,
 				new UpdateValueStrategy() {
 					@Override
 					public Object convert(Object value) {
 						return !((boolean) value);
 					}
 				});
 		if (scannableWrapper.getScannable() instanceof ScannableMotionUnits) {
 			this.setUnit(((ScannableMotionUnits) scannableWrapper.getScannable()).getUserUnits());
 		}
 		this.setCommitOnOutOfFocus(false);
 		this.setDigits(NumberEditorControl.DEFAULT_DECIMAL_PLACES);
		Double lowerLimit = scannableWrapper.getLowerLimit();
		Double upperLimit = scannableWrapper.getUpperLimit();
		if (lowerLimit != null && upperLimit != null) {
			this.setRange(lowerLimit, upperLimit);
 			this.setToolTipText(String.format("Lower limit: %s Upper limit: %s",
					roundDoubletoString(lowerLimit, NumberEditorControl.DEFAULT_DECIMAL_PLACES),
					roundDoubletoString(upperLimit, NumberEditorControl.DEFAULT_DECIMAL_PLACES)));
 		}
 
 	}
 
 	@Override
 	protected void setupControls() {
 		horizonalSpinner = true;
 		super.setupControls();
 	}
 
 	public void setPosition(double value) throws DeviceException {
 		((ScannableWrapper) targetObject).setPosition(value);
 	}
 
 	public double getPosition() throws DeviceException {
 		return ((ScannableWrapper) targetObject).getPosition();
 	}
 
 	@Override
 	protected String getFormattedText(Object value) {
 		try {
 			if (((ScannableWrapper) targetObject).isBusy()) {
 				Double targetPosition = ((ScannableWrapper) targetObject).getTargetPosition();
 				if (targetPosition == null) {
 					return super.getFormattedText(value);
 				}
 				if (controlModel.getUnit() == null) {
 					return super.getFormattedText(value) + " (Moving to " + targetPosition + ")";
 				}
 				return super.getFormattedText(value) + " (Moving to " + targetPosition + " " + controlModel.getUnit() + ")";
 			}
 		} catch (DeviceException e) {
 			// TODO This is a temporarily commented it out to avoid reading motor positions that are not ready
 			// UIHelper.showError("Error while reading the motor position", e.getMessage());
 			logger.warn("Error while reading the motor position");
 		}
 		return super.getFormattedText(value);
 	}
 }
