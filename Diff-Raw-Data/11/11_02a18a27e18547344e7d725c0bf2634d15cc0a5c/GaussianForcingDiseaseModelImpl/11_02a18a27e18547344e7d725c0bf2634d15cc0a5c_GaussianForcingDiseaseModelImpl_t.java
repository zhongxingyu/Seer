 package org.eclipse.stem.diseasemodels.forcing.impl;
 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.diseasemodels.forcing.ForcingPackage;
 import org.eclipse.stem.diseasemodels.forcing.GaussianForcingDiseaseModel;
 
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SILabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl;
 import org.eclipse.stem.diseasemodels.standard.impl.StochasticSIRDiseaseModelImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Gaussian Forcing Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.GaussianForcingDiseaseModelImpl#getSigma2 <em>Sigma2</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.GaussianForcingDiseaseModelImpl#getModulationPeriod <em>Modulation Period</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.GaussianForcingDiseaseModelImpl#getModulationPhaseShift <em>Modulation Phase Shift</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.forcing.impl.GaussianForcingDiseaseModelImpl#getModulationFloor <em>Modulation Floor</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class GaussianForcingDiseaseModelImpl extends StochasticSIRDiseaseModelImpl implements GaussianForcingDiseaseModel {
 	/**
 	 * The default value of the '{@link #getSigma2() <em>Sigma2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double SIGMA2_EDEFAULT = 100.0;
 	/**
 	 * The cached value of the '{@link #getSigma2() <em>Sigma2</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSigma2()
 	 * @generated
 	 * @ordered
 	 */
 	protected double sigma2 = SIGMA2_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getModulationPeriod() <em>Modulation Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_PERIOD_EDEFAULT = 365.25;
 	/**
 	 * The cached value of the '{@link #getModulationPeriod() <em>Modulation Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationPeriod = MODULATION_PERIOD_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getModulationPhaseShift() <em>Modulation Phase Shift</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPhaseShift()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_PHASE_SHIFT_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getModulationPhaseShift() <em>Modulation Phase Shift</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationPhaseShift()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationPhaseShift = MODULATION_PHASE_SHIFT_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getModulationFloor() <em>Modulation Floor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationFloor()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double MODULATION_FLOOR_EDEFAULT = 0.6;
 	/**
 	 * The cached value of the '{@link #getModulationFloor() <em>Modulation Floor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModulationFloor()
 	 * @generated
 	 * @ordered
 	 */
 	protected double modulationFloor = MODULATION_FLOOR_EDEFAULT;
 
 	private Calendar calendar = Calendar.getInstance();
 	private Calendar calendar2 = Calendar.getInstance();
 	private static final double MILLIS_PER_DAY = 1000.0*60.0*60.0*24.0;
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public GaussianForcingDiseaseModelImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ForcingPackage.Literals.GAUSSIAN_FORCING_DISEASE_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getSigma2() {
 		return sigma2;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSigma2(double newSigma2) {
 		double oldSigma2 = sigma2;
 		sigma2 = newSigma2;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__SIGMA2, oldSigma2, sigma2));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationPeriod() {
 		return modulationPeriod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationPeriod(double newModulationPeriod) {
 		double oldModulationPeriod = modulationPeriod;
 		modulationPeriod = newModulationPeriod;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PERIOD, oldModulationPeriod, modulationPeriod));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationPhaseShift() {
 		return modulationPhaseShift;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationPhaseShift(double newModulationPhaseShift) {
 		double oldModulationPhaseShift = modulationPhaseShift;
 		modulationPhaseShift = newModulationPhaseShift;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT, oldModulationPhaseShift, modulationPhaseShift));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getModulationFloor() {
 		return modulationFloor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModulationFloor(double newModulationFloor) {
 		double oldModulationFloor = modulationFloor;
 		modulationFloor = newModulationFloor;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_FLOOR, oldModulationFloor, modulationFloor));
 	}
 
 	/**
 	 * To create a user defined (experimental) disease mode, the user may wish 
 	 * to extend the existing models but simply override two methods.
 	 * computeDiseaseDeltas() sets the work flow for the model. The following example
 	 * is derived from the standard code for all build in models but in the line below
 	 * where we define final "double transmisionRate" we show and example modification
 	 * where a periodic forcing factor { sin(freq*t) } is added to the code as an example
 	 * modification.
 	 * 
 	 * @param time
 	 * 			  current time
 	 * @param currentState
 	 *            the current state of the population
 	 * @param diseaseLabel
 	 *            the disease label for which the state transitions are being
 	 *            computed.
 	 * @param timeDelta
 	 *            the time period (milliseconds) over which the population
 	 *            members transition to new states          
 	 * @return a disease state label value that contains the number of
 	 *         delta changes in each disease state
 	 * 
 	 * Users can modify the method below to create their own model.
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIImpl#computeDiseaseDeltas(StandardDiseaseModelLabelValue,
 	 *      StandardDiseaseModelLabel, long)
 	 */
 	private ArrayList<Long> writtedTimes = new ArrayList<Long>();
 	private FileWriter fw;
 	private Long firstTime = new Long(Long.MAX_VALUE);
 	@Override
 	public StandardDiseaseModelLabelValue computeDiseaseDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, final long timeDelta, DiseaseModelLabelValue returnValue) {
 final SIRLabelValue currentSIR = (SIRLabelValue) currentState;
 		
 		long currentMillis = time.getTime().getTime();
 		double seasonalModulationFloor = getModulationFloor();
 		double modulationPeriod = getModulationPeriod();
 		double phase = getModulationPhaseShift();	
 		double sigma2 = getSigma2();		
 		
 		synchronized(firstTime) {
 			if(firstTime.longValue() == Long.MAX_VALUE)
 				firstTime = new Long(time.getTime().getTime());
 		}
 		
 		// Get the day from time and adjust for the phase	
 		double day=0;
 		// Shared calendar object not thread safe
 		synchronized(calendar) {
 			calendar.setTimeInMillis(currentMillis+(long)(phase*MILLIS_PER_DAY));
 			calendar2.setTimeInMillis(firstTime.longValue());
 			day = (calendar.getTimeInMillis() - calendar2.getTimeInMillis())/MILLIS_PER_DAY;
		
 		}
 		double intDay = day;
 		
 		double modulation = 0;
 		// Smoothing
		if(day % modulationPeriod < 30 || day % modulationPeriod > modulationPeriod-30) {
 			double avg=0;
 			double denom = 0;
 			for(double d=day-30;d<day+30;++d) {
 				double _d = ((d % modulationPeriod)-modulationPeriod/2.0)/modulationPeriod;
 				double mo = (1/Math.sqrt(2*Math.PI*sigma2))*Math.exp(-(Math.pow(_d,2))/(2*sigma2));						
 				avg = avg+mo;
 				++denom;
 			}
 			modulation = avg/denom;
		} else {
 			day = ((day % modulationPeriod)-modulationPeriod/2.0)/modulationPeriod;
 			modulation = (1/Math.sqrt(2*Math.PI*sigma2))*Math.exp(-(Math.pow(day,2))/(2*sigma2));						
		}
 		
 		// This is beta*
 		double transmissionRate = seasonalModulationFloor + modulation * (getAdjustedTransmissionRate(timeDelta));
 
 /*		if(diseaseLabel.getNode().getURI().toString().hashCode() % 2 == 0 
 				&& ((int)intDay) == 3285)
 				transmissionRate *=10;
 		else 
 		if(diseaseLabel.getNode().getURI().toString().hashCode() % 2 == 1 
 			&& ((int)intDay) == 3285)	
 				transmissionRate *=0.1;
 */		
 		
 		synchronized(writtedTimes) {
 			if(!writtedTimes.contains(time.getTime().getTime())) {
 				try {
 				if(fw == null) fw = new FileWriter("beta.csv");
 				fw.write((int)intDay+","+transmissionRate+"\n");
 				fw.flush();
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 				writtedTimes.add(time.getTime().getTime());
 			}
 		}
 		
 		if(!this.isFrequencyDependent())  transmissionRate *= getTransmissionRateScaleFactor(diseaseLabel);
 		
 		// The effective Infectious population  is a dimensionles number normalize by total
 		// population used in teh computation of bets*S*i where i = Ieffective/Pop.
 		// This includes a correction to the current
 		// infectious population (Ieffective) based on the conserved exchange of people (circulation)
 		// between regions. Note that this is no the "arrivals" and "departures" which are
 		// a different process.
 		final double effectiveInfectious = getNormalizedEffectiveInfectious(diseaseLabel.getNode(), diseaseLabel, currentSIR.getI());
 
 		/*
 		 * Compute state transitions
 		 * 
 		 * Regarding computing the number of transitions from Susceptible to Exposed:
 		 * In a linear model the "effective" number of infectious people is just
 		 * the number of infectious people In a nonlinear model we have a
 		 * nonLinearity exponent that is > 1 this models the effect of immune
 		 * system saturation when Susceptible people are exposed to large
 		 * numbers of infectious people. then the "effective" number of
 		 * infectious people is I^nonLinearity exponent to allow for either
 		 * linear or nonlinear models we always calculate I^nonLinearity
 		 * exponent and allow nonLinearity exponent >= 1.0
 		 */
 		double numberOfInfectedToRecovered = getAdjustedRecoveryRate(timeDelta)
 		* currentSIR.getI();
 		double numberOfRecoveredToSusceptible = getAdjustedImmunityLossRate(timeDelta)
 		* currentSIR.getR();
 		// Need to checked what do do here. If non linear coefficient is not 1 and
 		// the effective infectious is negative (which is possible), what do do? 
 		// Let's fall back on the linear method for now.
 		double numberOfSusceptibleToInfected = 0.0;
 		if(getNonLinearityCoefficient() != 1.0 && effectiveInfectious >= 0.0)
 			numberOfSusceptibleToInfected = transmissionRate
 			* currentSIR.getS()* Math.pow(effectiveInfectious, getNonLinearityCoefficient());
 		else
 			numberOfSusceptibleToInfected = transmissionRate
 			* currentSIR.getS()* effectiveInfectious;
 		
 		
 		// Determine delta S
 		final double deltaS = numberOfRecoveredToSusceptible - numberOfSusceptibleToInfected;
 			// Determine delta I
 		final double deltaI = numberOfSusceptibleToInfected- numberOfInfectedToRecovered;
 		// Determine delta R
 		final double deltaR = numberOfInfectedToRecovered - numberOfRecoveredToSusceptible;
 		
 		SIRLabelValueImpl ret = (SIRLabelValueImpl)returnValue;
 		ret.setS(deltaS);
 		ret.setI(deltaI);
 		ret.setIncidence(numberOfInfectedToRecovered);
 		ret.setR(deltaR);
 		ret.setDiseaseDeaths(0);
 		return ret;
 	} // computeTransitions
 	
 	/**
 	 * ModelSpecificAdjustments for a Stochastic model adds noise to or adjusts 
 	 * the disease state transition values by multiplying
 	 * the additions by a random variable r ~ (1+/-x) with x small.
 	 * The requirements that no more individuals can be moved from a state than are
 	 * already in that state is still enforced.
 	 * 
 	 */
 	@Override
 	public void doModelSpecificAdjustments(
 			final StandardDiseaseModelLabelValue state) {
 			final SILabelValue currentSI = (SILabelValue) state;
 			double oldI = currentSI.getI();
 			double Inoisy = currentSI.getI()* computeNoise();
 			double change = oldI-Inoisy;
 			currentSI.setI(Inoisy);
 			double newS = currentSI.getS() + change;
 			if(newS < 0.0) {
 				// Need to rescale
 				double scale = (currentSI.getS() + newS) / currentSI.getS();
 				currentSI.setI(Inoisy*scale);
 			} else  currentSI.setS(newS);
 			return;
 	} // doModelSpecificAdjustments
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__SIGMA2:
 				return getSigma2();
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				return getModulationPeriod();
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				return getModulationPhaseShift();
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				return getModulationFloor();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__SIGMA2:
 				setSigma2((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				setModulationPeriod((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				setModulationPhaseShift((Double)newValue);
 				return;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				setModulationFloor((Double)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__SIGMA2:
 				setSigma2(SIGMA2_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				setModulationPeriod(MODULATION_PERIOD_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				setModulationPhaseShift(MODULATION_PHASE_SHIFT_EDEFAULT);
 				return;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				setModulationFloor(MODULATION_FLOOR_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__SIGMA2:
 				return sigma2 != SIGMA2_EDEFAULT;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 				return modulationPeriod != MODULATION_PERIOD_EDEFAULT;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 				return modulationPhaseShift != MODULATION_PHASE_SHIFT_EDEFAULT;
 			case ForcingPackage.GAUSSIAN_FORCING_DISEASE_MODEL__MODULATION_FLOOR:
 				return modulationFloor != MODULATION_FLOOR_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (sigma2: "); //$NON-NLS-1$
 		result.append(sigma2);
 		result.append(", modulationPeriod: "); //$NON-NLS-1$
 		result.append(modulationPeriod);
 		result.append(", modulationPhaseShift: "); //$NON-NLS-1$
 		result.append(modulationPhaseShift);
 		result.append(", modulationFloor: "); //$NON-NLS-1$
 		result.append(modulationFloor);
 		result.append(')');
 		return result.toString();
 	}
 
 } //GaussianForcingDiseaseModelImpl
