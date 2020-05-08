 package org.eclipse.stem.populationmodels.standard.impl;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 
 import java.util.Random;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.graph.LabelValue;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 import org.eclipse.stem.populationmodels.standard.StochasticStandardPopulationModel;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Stochastic Standard Population Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StochasticStandardPopulationModelImpl#getGain <em>Gain</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class StochasticStandardPopulationModelImpl extends StandardPopulationModelImpl implements StochasticStandardPopulationModel {
 	protected static double MAX_GAIN = 0.02;
 	
 	/**
 	 * The default value of the '{@link #getGain() <em>Gain</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGain()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double GAIN_EDEFAULT = 0.01;
 
 	/**
 	 * The cached value of the '{@link #getGain() <em>Gain</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGain()
 	 * @generated
 	 * @ordered
 	 */
 	protected double gain = GAIN_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public StochasticStandardPopulationModelImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.STOCHASTIC_STANDARD_POPULATION_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getGain() {
 		return gain;
 	}
 
 	@Override
 	public void doModelSpecificAdjustments(LabelValue label) {
 		final StandardPopulationModelLabelValue currentLabelValue = (StandardPopulationModelLabelValue) label;
		//double oldCount = currentLabelValue.getCount();
 		double incidence = currentLabelValue.getIncidence();
 		double newIncidence = incidence*computeNoise();
 		double diff = newIncidence - incidence;
 		
 		double newCount = currentLabelValue.getCount() + diff;
 		double newBirths = currentLabelValue.getBirths() + diff/2.0;
 		double newDeaths = currentLabelValue.getDeaths() + diff/2.0;
 		
 		currentLabelValue.setCount(newCount);
 		currentLabelValue.setBirths(newBirths);
 		currentLabelValue.setDeaths(newDeaths);
 	}
 	
 	Random rand = new Random();
 	protected double computeNoise() {
 		return this.computeNoise(this.getGain(), this.rand);
 	}
 	
 	/**
 	 * This method returns a double precision random noise variable
 	 * r between (1.0+x) and (1.0-x)
 	 * where x is a product G* Math.rand(), and G is a gain factor 0<G<max noise
 	 * 
 	 * @param gain
 	 * @param rand
 	 * @return noise
 	 */
 	protected double computeNoise(final double gain, final Random rand) {
      
 	    assert (gain <= MAX_GAIN):"STOCHASTIC NOISE must be 'small', therefore gain parameter must be < MAX_GAIN="+MAX_GAIN; // maximum noise should be 1%
 		double x = (2.0*rand.nextDouble())-1.0; // +/- 1
 		x *= gain;
 		x += 1.0;
 		assert ((x>0.0)&&(x<2.0)) : "random noise should be a small perturbation Gain factor, g,  must 0.0 < g << 1.0 ";
 		return x;
 		
 	} // getNoise
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setGain(double newGain) {
 		double oldGain = gain;
 		gain = newGain;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STOCHASTIC_STANDARD_POPULATION_MODEL__GAIN, oldGain, gain));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.STOCHASTIC_STANDARD_POPULATION_MODEL__GAIN:
 				return getGain();
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
 			case StandardPackage.STOCHASTIC_STANDARD_POPULATION_MODEL__GAIN:
 				setGain((Double)newValue);
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
 			case StandardPackage.STOCHASTIC_STANDARD_POPULATION_MODEL__GAIN:
 				setGain(GAIN_EDEFAULT);
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
 			case StandardPackage.STOCHASTIC_STANDARD_POPULATION_MODEL__GAIN:
 				return gain != GAIN_EDEFAULT;
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
 		result.append(" (gain: ");
 		result.append(gain);
 		result.append(')');
 		return result.toString();
 	}
 
 } //StochasticStandardPopulationModelImpl
