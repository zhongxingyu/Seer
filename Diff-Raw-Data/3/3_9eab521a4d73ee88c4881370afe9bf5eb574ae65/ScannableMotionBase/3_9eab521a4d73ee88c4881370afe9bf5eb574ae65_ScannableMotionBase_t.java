 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
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
 
 package gda.device.scannable;
 
 import static gda.device.scannable.PositionConvertorFunctions.toDoubleArray;
 import static gda.device.scannable.PositionConvertorFunctions.toObjectArray;
 import gda.data.nexus.INeXusInfoWriteable;
 import gda.data.nexus.NeXusUtils;
 import gda.device.Device;
 import gda.device.DeviceException;
 import gda.device.ScannableMotion;
 import gda.device.scannable.component.PositionValidator;
 import gda.device.scannable.component.ScannableLimitsComponent;
 import gda.device.scannable.component.ScannableOffsetAndScalingComponent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.nexusformat.NeXusFileInterface;
 import org.nexusformat.NexusException;
 import org.python.core.PyException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A base implementation for a {@link ScannableMotion} {@link Device}.
  */
 public class ScannableMotionBase extends ScannableBase implements ScannableMotion, INeXusInfoWriteable {
 
 	private static final Logger logger = LoggerFactory.getLogger(ScannableMotionBase.class);
 
 	private ScannableLimitsComponent limitsComponent = new ScannableLimitsComponent();
 
 	protected int numberTries = 1;
 
 	private List<PositionValidator> additionalPositionValidators = new ArrayList<PositionValidator>();
 
 	/**
	 * Array of tolerance values. Used by the isAt() method. Can be set manually via setTolerance or setTolerances.
 	 */
 	protected Double[] tolerance = null;
 
 	private ScannableOffsetAndScalingComponent offsetAndScalingComponent = new ScannableOffsetAndScalingComponent();
 
 	public ScannableMotionBase() {
 		this.limitsComponent.setHostScannable(this);
 		this.offsetAndScalingComponent.setHostScannable(this);
 	}
 
 	@Override
 	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
 		String report = checkPositionValid(externalPosition);
 		if (report != null) {
 			throw new DeviceException(report);
 		}
 		super.asynchronousMoveTo(externalPosition);
 	}
 
 	/**
 	 * Sets the limits component used by this object.
 	 * 
 	 * @param limitsComponent
 	 *            the limits component
 	 */
 	public void setLimitsComponent(ScannableLimitsComponent limitsComponent) {
 		this.limitsComponent = limitsComponent;
 		this.limitsComponent.setHostScannable(this);
 	}
 	
 	public ScannableLimitsComponent getLimitsComponent() {
 		return limitsComponent;
 	}
 	
 
 	public void setOffsetAndScalingComponent(ScannableOffsetAndScalingComponent offsetAndScalingComponent) {
 		this.offsetAndScalingComponent = offsetAndScalingComponent;
 		this.offsetAndScalingComponent.setHostScannable(this);
 
 	}
 
 	@Override
 	public Object internalToExternal(Object internalPosition) {
 		Object internalPosition2 = super.internalToExternal(internalPosition);
 		return offsetAndScalingComponent.internalTowardExternal(internalPosition2);
 	}
 
 	@Override
 	public Object externalToInternal(Object externalPosition) {
 		Object offsetAndScaleRemoved = offsetAndScalingComponent.externalTowardInternal(externalPosition);
 		return super.externalToInternal(offsetAndScaleRemoved);
 	}
 
 	/**
 	 * {@inheritDoc}. Calls onto rawIsBusy for historical reasons, although there is currently no need for this.
 	 * 
 	 * @see gda.device.Scannable#isBusy()
 	 */
 	@Override
 	public boolean isBusy() throws DeviceException {
 		// TODO: send warning to log
 		return rawIsBusy();
 	}
 
 	/**
 	 * Should be implemented by inheriting classes
 	 * 
 	 * @return true if busy and last action started by asynchronousMoveTo not completed yet
 	 * @see gda.device.Scannable#isBusy()
 	 * @throws DeviceException
 	 */
 	@SuppressWarnings("unused")
 	@Deprecated
 	public boolean rawIsBusy() throws DeviceException {
 		// @SuppressWarnings tag as Jython scannables might use this.
 		throw new RuntimeException("The scannable " + getName()
 				+ " must overide either the rawIsBusy() or the isBusy() method.");
 	}
 
 	/**
 	 * If the numberTries and tolerance attributes have been set then repeatedly tries to move this Scannable until the
 	 * position is within the tolerance range.
 	 * 
 	 * @see gda.device.scannable.ScannableBase#moveTo(java.lang.Object)
 	 */
 	@Override
 	public void moveTo(Object position) throws DeviceException {
 
 		if (this.numberTries <= 1) {
 			super.moveTo(position);
 		} else {
 
 			// throw exception if tolerance has not been set as the isAt test may not be meaningful
 			if (getTolerances() == null) {
 				throw new DeviceException("configuration error - if numberTries > 1 then a tolerence must be set");
 			}
 
 			int numberAttempts = 0;
 			try {
 				// loop for numberTries times until current position is within tolerance of the target
 				do {
 					numberAttempts++;
 					asynchronousMoveTo(position);
 					this.waitWhileBusy();
 				} while (!isAt(position) && numberAttempts < this.numberTries);
 			} catch (Exception e) {
 				throw new DeviceException("Exception while moving " + getName() + ": " + e.getMessage());
 			}
 
 			// if tried too many times then throw an exception
 			if (numberAttempts > this.numberTries) {
 				throw new DeviceException(getName() + " had too many attempts to try to move slits into position.");
 			}
 		}
 	}
 
 	/**
 	 * Quick-to-type version of asynchronousMoveTo for use in Jython environment
 	 * 
 	 * @param position
 	 * @throws DeviceException
 	 */
 	@Override
 	public void a(Object position) throws DeviceException {
 		this.asynchronousMoveTo(position);
 	}
 
 	/**
 	 * Quick-to-type version of a relative move for use in Jython environment
 	 * 
 	 * @param amount
 	 * @throws DeviceException
 	 */
 	@Override
 	public void r(Object amount) throws DeviceException {
 		try {
 			Object target = ScannableUtils.calculateNextPoint(this.getPosition(), amount);
 			// we would normally expect an array of Doubles
 			if (target instanceof Double[] && ((Double[]) target).length == 1) {
 				this.moveTo(((Double[]) target)[0]);
 			} else {
 				this.moveTo(target);
 			}
 		} catch (DeviceException ex) {
 			throw ex;
 		} catch (Exception ex) {
 			throw new DeviceException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Quick-to-type version of an asynchronous relative move for use in Jython environment
 	 * 
 	 * @param amount
 	 * @throws DeviceException
 	 */
 	@Override
 	public void ar(Object amount) throws DeviceException {
 		try {
 			Object target = ScannableUtils.calculateNextPoint(this.getPosition(), amount);
 			this.asynchronousMoveTo(target);
 		} catch (DeviceException ex) {
 			throw ex;
 		} catch (Exception ex) {
 			throw new DeviceException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Checks the position against the Scannable's limits and against any additional position validators added with
 	 * additionalPositionValidators().
 	 */
 	@Override
 	public String checkPositionValid(Object externalPosition) throws DeviceException {
 		Object internalPosition = externalToInternal(externalPosition);
 		Object[] internalPositionArray = toObjectArray(internalPosition);
 		String limitsComponentMsg = limitsComponent.checkInternalPosition(internalPositionArray);
 		if (limitsComponentMsg != null) {
 			return limitsComponentMsg;
 		}
 		for (PositionValidator validator : additionalPositionValidators) {
 			String msg = validator.checkInternalPosition(internalPositionArray);
 			if (msg != null) {
 				return msg;
 			}
 		}
 		return null; // all checked okay
 	}
 
 	// defers to checkPositionValid
 	@Override
 	@Deprecated
 	public String checkPositionWithinGdaLimits(Double[] externalPosition) {
 		// TODO: send warning to log
 		try {
 			return checkPositionValid(externalPosition);
 		} catch (DeviceException e) {
 			return "Problem checking position: " + e.getMessage();
 		}
 	}
 
 	// defers to checkPositionValid
 	@Override
 	@Deprecated
 	public String checkPositionWithinGdaLimits(Object externalPosition) {
 		// TODO: send warning to log
 		try {
 			return checkPositionValid(externalPosition);
 		} catch (DeviceException e) {
 			return "Problem checking position: " + e.getMessage();
 		}
 	}
 
 	@Override
 	public Double[] getLowerGdaLimits() {
 
 		// 1. Simply return lower limits if no scaling factor
 		if (getScalingFactor() == null) {
 			return toDoubleArray(internalToExternal(limitsComponent.getInternalLower()));
 		}
 
 		// 2. Return null if neither limits set
 		if (limitsComponent.getInternalLower() == null && limitsComponent.getInternalUpper() == null)
 			return null;
 
 		// 3. Otherwise, pick the right limit for each field based on scaling factor
 		Double[] externalLowerIgnoringScale = getExternalLowerIgnoringScale();
 		Double[] externalUpperIgnoringScale = getExternalUpperIgnoringScale();
 		Double[] externalLower = new Double[getInputNames().length];
 		for (int i = 0; i < externalLower.length; i++) {
 			externalLower[i] = (isScalingFactorNegative(i)) ? externalUpperIgnoringScale[i]
 					: externalLowerIgnoringScale[i];
 		}
 		return externalLower;
 	}
 
 	@Override
 	public Double[] getUpperGdaLimits() {
 		// 1. Simply return upper limits if no scaling factor
 		if (getScalingFactor() == null) {
 			return toDoubleArray(internalToExternal(limitsComponent.getInternalUpper()));
 		}
 
 		// 2. Return null if neither limits set
 		if (limitsComponent.getInternalLower() == null && limitsComponent.getInternalUpper() == null)
 			return null;
 
 		// 3. Otherwise, pick the right limit for each field based on scaling factor
 		Double[] externalLowerIgnoringScale = getExternalLowerIgnoringScale();
 		Double[] externalUpperIgnoringScale = getExternalUpperIgnoringScale();
 		Double[] externalUpper = new Double[getInputNames().length];
 		for (int i = 0; i < externalUpper.length; i++) {
 			externalUpper[i] = (isScalingFactorNegative(i)) ? externalLowerIgnoringScale[i]
 					: externalUpperIgnoringScale[i];
 		}
 		return externalUpper;
 	}
 
 	@Override
 	public void setLowerGdaLimits(Double[] externalLowerLim) throws Exception {
 		Double[] internalLowerIgnoringScale = toDoubleArray(externalToInternal(externalLowerLim));
 		if (getScalingFactor() == null) {
 			limitsComponent.setInternalLower(internalLowerIgnoringScale);
 		} else {
 			int inputLength = getInputNames().length;
 			for (int i = 0; i < internalLowerIgnoringScale.length; i++) {
 				if (isScalingFactorNegative(i)) {
 					limitsComponent.setInternalUpper(internalLowerIgnoringScale[i], i, inputLength);
 				} else {
 					limitsComponent.setInternalLower(internalLowerIgnoringScale[i], i, inputLength);
 				}
 			}
 		}
 		this.notifyIObservers(this, new ScannableLowLimitChangeEvent(externalLowerLim));
 	}
 
 	@Override
 	public void setUpperGdaLimits(Double[] externalUpperLim) throws Exception {
 		Double[] internalUpperIgnoringScale = toDoubleArray(externalToInternal(externalUpperLim));
 		if (getScalingFactor() == null) {
 			limitsComponent.setInternalUpper(internalUpperIgnoringScale);
 		} else {
 			int inputLength = getInputNames().length;
 			for (int i = 0; i < internalUpperIgnoringScale.length; i++) {
 				if (isScalingFactorNegative(i)) {
 					limitsComponent.setInternalLower(internalUpperIgnoringScale[i], i, inputLength);
 				} else {
 					limitsComponent.setInternalUpper(internalUpperIgnoringScale[i], i, inputLength);
 				}
 			}
 		}
 		this.notifyIObservers(this, new ScannableHighLimitChangeEvent(externalUpperLim));
 	}
 
 	private boolean isScalingFactorNegative(int index) {
 		if (getScalingFactor() == null)
 			return false;
 		Double scalingFactor = getScalingFactor()[index];
 		return (scalingFactor == null) ? false : scalingFactor < 0.;
 	}
 
 	private Double[] getExternalLowerIgnoringScale() {
 		Double[] externalLowerIgnoringScale = toDoubleArray(internalToExternal(limitsComponent.getInternalLower()));
 		if (externalLowerIgnoringScale == null)
 			externalLowerIgnoringScale = new Double[getInputNames().length];
 		return externalLowerIgnoringScale;
 	}
 
 	private Double[] getExternalUpperIgnoringScale() {
 		Double[] externalUpperIgnoringScale = toDoubleArray(internalToExternal(limitsComponent.getInternalUpper()));
 		if (externalUpperIgnoringScale == null)
 			externalUpperIgnoringScale = new Double[getInputNames().length];
 		return externalUpperIgnoringScale;
 	}
 
 	@Override
 	public void setLowerGdaLimits(Double lowerLim) throws Exception {
 		if (lowerLim == null) {
 			Double[] nullArray = null;
 			setLowerGdaLimits(nullArray);
 		} else {
 			setLowerGdaLimits(new Double[] { lowerLim });
 		}
 	}
 
 	@Override
 	public void setUpperGdaLimits(Double upperLim) throws Exception {
 		if (upperLim == null) {
 			Double[] nullArray = null;
 			setUpperGdaLimits(nullArray);
 		} else {
 			setUpperGdaLimits(new Double[] { upperLim });
 		}
 	}
 
 	/**
 	 * @see gda.device.ScannableMotion#getNumberTries()
 	 */
 	@Override
 	public int getNumberTries() {
 		return numberTries;
 	}
 
 	/**
 	 * @see gda.device.ScannableMotion#setNumberTries(int)
 	 */
 	@Override
 	public void setNumberTries(int numberTries) {
 		if (numberTries < 1) {
 			numberTries = 1;
 		}
 		this.numberTries = numberTries;
 	}
 
 	/**
 	 * {@inheritDoc} If positionToTest is a string (as will be the case for valves or pneumatics for example), this is
 	 * compared to the value obtained from getPosition(). An exception is thrown if this is not also a string.
 	 * <p>
 	 * Otherwise if positionToTest is not a string the object is compared to the value from getPosition() to see if they
 	 * are within the tolerance specified in the scannable's tolerance array. Both values are first converted to arrays
 	 * using objectToArray() from ScannableUtils.
 	 * <p>
 	 * 
 	 * @see gda.device.Scannable#isAt(java.lang.Object)
 	 */
 	@Override
 	public boolean isAt(Object positionToTest) throws DeviceException {
 
 		if (positionToTest instanceof String) {
 			try {
 				return positionToTest.equals(this.getPosition());
 			} catch (Exception e) {
 				throw new DeviceException("Could not compare the string given"
 						+ " to this scannables non-string position", e);
 			}
 		}
 
 		Double[] posToTest = ScannableUtils.objectToArray(positionToTest);
 		Double[] posCurrent = ScannableUtils.objectToArray(this.getPosition());
 		Double[] tolerance = this.getTolerances();
 
 		for (int i = 0; i < posToTest.length; i++) {
 			if (tolerance == null || tolerance[i] == null || tolerance[i] == 0.0) {
 				if (!posToTest[i].equals(posCurrent[i])) {
 					return false;
 				}
 			} else if ((Math.abs(posToTest[i] - posCurrent[i]) > tolerance[i])) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * WARNING: This method will not work with units.
 	 * <p>
 	 * 
 	 * @param tolerance
 	 * @throws DeviceException
 	 */
 	@Override
 	public void setTolerances(Double[] tolerance) throws DeviceException {
 		// bug in Castor: seem to get tolerance arrays which are too long!
 		int inputNamesLength = getInputNames().length;
 		if (tolerance.length > inputNamesLength) {
 			this.tolerance = (Double[]) ArrayUtils.subarray(tolerance, tolerance.length - inputNamesLength,
 					tolerance.length);
 		} else if (tolerance.length < inputNamesLength) {
 			throw new DeviceException("Length of input (" + tolerance.length
 					+ ") does not match number of inputFields (" + inputNamesLength + ").");
 		} else {
 			this.tolerance = tolerance;
 		}
 	}
 
 	/**
 	 * @see gda.device.ScannableMotion#setTolerance(Double)
 	 */
 	@Override
 	public void setTolerance(Double tolerence) throws DeviceException {
 		setTolerances(new Double[] { tolerence });
 	}
 
 	/**
 	 * WARNING: This method will not work with units.
 	 * <p>
 	 * 
 	 * @see gda.device.ScannableMotion#getTolerances()
 	 * @return double[]
 	 */
 	@Override
 	public Double[] getTolerances() throws DeviceException {
 		return this.tolerance;
 	}
 
 	@Override
 	public void writeNeXusInformation(NeXusFileInterface file) throws NexusException {
 		writeNeXusInformationLimits(file);
 	}
 	
 	protected void writeNeXusInformationLimits(NeXusFileInterface file) throws NexusException {
 			Double[] upperLimits = getUpperGdaLimits();
 			if (upperLimits != null)
 				NeXusUtils.writeNexusDoubleArray(file, "soft_limit_max", upperLimits);
 			Double[] lowerLimits = getLowerGdaLimits();
 			if (lowerLimits != null)
 				NeXusUtils.writeNexusDoubleArray(file, "soft_limit_min", lowerLimits);
 	}
 
 	public static Double[] getInputLimits(ScannableMotion sm, int input) {
 		Double[] lowerLimits = sm.getLowerGdaLimits();
 		Double[] upperLimits = sm.getUpperGdaLimits();
 		Double lowerLimit = lowerLimits == null ? null : lowerLimits[input];
 		Double upperLimit = upperLimits == null ? null : upperLimits[input];
 		return lowerLimit == null && upperLimit == null ? null : new Double[] { lowerLimit, upperLimit };
 	}
 
 	/**
 	 * {@inheritDoc} To distributed some of the extra methods in the ScannableMotion interface
 	 * 
 	 * @throws DeviceException
 	 * @see gda.device.DeviceBase#getAttribute(java.lang.String)
 	 */
 	@Override
 	public Object getAttribute(String attributeName) throws DeviceException {
 		if (attributeName.equals("upperGdaLimits")) {
 			return this.getUpperGdaLimits();
 		} else if (attributeName.equals("lowerGdaLimits")) {
 			return this.getLowerGdaLimits();
 		} else if (attributeName.equals("tolerance")) {
 			return this.getTolerances();
 		} else if (attributeName.equals("numberTries()")) {
 			return this.getNumberTries();
 		} else if (attributeName.equals(FIRSTINPUTLIMITS)) {
 			return getFirstInputLimits();
 		}
 		return super.getAttribute(attributeName);
 	}
 
 	public Double[] getInputLimits(int index) {
 		return ScannableMotionBase.getInputLimits(this, index);
 	}
 
 	@SuppressWarnings("unused")
 	public Double[] getFirstInputLimits() throws DeviceException {
 		return getInputLimits(0);
 	}
 
 	/**
 	 * This method should be called at the end of an inheriting class's constructor.
 	 * <p>
 	 * It will do things such as validate that the class instantiated was valid, register the scannable with other
 	 * objects and generally complete the instantiation of the object.
 	 * 
 	 * @throws InstantiationException
 	 */
 	protected void completeInstantiation() throws InstantiationException {
 
 		// check that size of arrays match
 		if (inputNames.length + extraNames.length != outputFormat.length) {
 
 			// if outputFormat too short then simply extend it, else throw an
 			// error as one of the other arrays must be wrong
 
 			if (outputFormat.length < inputNames.length + extraNames.length) {
 				logger.warn("Error while instantiating " + getName()
 						+ ". Extending outputFormat array to match sum of inputNames and outputNames.");
 				// extend array
 				String formatString = outputFormat[0];
 				int length = inputNames.length + extraNames.length;
 				outputFormat = new String[length];
 				for (int i = 0; i < length; i++) {
 					outputFormat[i] = formatString;
 				}
 
 			} else {
 				// throw an error
 				throw new InstantiationException("Error while instantiating " + getName()
 						+ ". Sum of lengths of inputNames and outputNames should match with outFormat array length.");
 			}
 		}
 	}
 
 	@Override
 	public Double[] getOffset() {
 		return offsetAndScalingComponent.getOffset();
 	}
 
 	@Override
 	public Double[] getScalingFactor() {
 		return offsetAndScalingComponent.getScalingFactor();
 	}
 
 	@Override
 	public void setOffset(Double... offsetArray) {
 		offsetAndScalingComponent.setOffset(offsetArray);
 	}
 
 	@Override
 	public void setScalingFactor(Double... scaleArray) {
 		offsetAndScalingComponent.setScalingFactor(scaleArray);
 	}
 
 	public void addPositionValidator(PositionValidator validator) {
 		additionalPositionValidators.add(validator);
 	}
 	
 	public void setAdditionalPositionValidators(List<PositionValidator> additionalPositionValidators) {
 		this.additionalPositionValidators = additionalPositionValidators;
 	}
 
 	public List<PositionValidator> getAdditionalPositionValidators() {
 		return additionalPositionValidators;
 	}
 
 	@Override
 	public String toFormattedString() {
 		// We need to extend so that this is passed as SMB rather then SM
 		String report;
 		try {
 			report = ScannableUtils.getFormattedCurrentPosition(this);
 		} catch (PyException e) {
 			throw new RuntimeException(e.getMessage(), e);
 		} catch (Exception e) {
 			if (e instanceof NullPointerException || e.getMessage().isEmpty()) {
 				throw new RuntimeException("Exception in " + getName() + ".toString()", e);
 			}
 			throw new RuntimeException(e.getMessage(), e);
 		}
 
 		return report + generateScannableLimitsReport();
 	}
 
 	protected String generateScannableLimitsReport() {
 		String report = "";
 		if (getInputNames().length == 1) {
 			Double[] lowerGdaLimits = getLowerGdaLimits();
 			Double[] upperGdaLimits = getUpperGdaLimits();
 			if (lowerGdaLimits != null || upperGdaLimits != null) {
 				report += " (";
 				if (lowerGdaLimits != null)
 					if (lowerGdaLimits[0] != null)
 						report += String.format(getOutputFormat()[0], lowerGdaLimits[0]);
 				report += ":";
 				if (upperGdaLimits != null)
 					if (upperGdaLimits[0] != null)
 						report += String.format(getOutputFormat()[0], upperGdaLimits[0]);
 				report += ")";
 			}
 		}
 		return report;
 	}
 
 }
