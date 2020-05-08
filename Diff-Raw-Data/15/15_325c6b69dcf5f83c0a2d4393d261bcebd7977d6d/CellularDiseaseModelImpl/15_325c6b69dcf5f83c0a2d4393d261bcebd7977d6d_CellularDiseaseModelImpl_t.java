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
 
 package org.eclipse.stem.diseasemodels.experimental.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.stem.core.graph.LabelValue;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.diseasemodels.experimental.CellularDiseaseModel;
 import org.eclipse.stem.diseasemodels.experimental.ExperimentalPackage;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SILabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.impl.SIImpl;
 import org.eclipse.stem.diseasemodels.standard.impl.SILabelValueImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Percolation Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * </p>
  *
  * @generated
  */
 public class CellularDiseaseModelImpl extends SIImpl implements CellularDiseaseModel {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CellularDiseaseModelImpl() {
 		super();
 	}
 
 	LogDiseaseState lds = null;
 	int icount = 0;
 	
 	
 	
 	/**
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIImpl#computeDiseaseDeltas(org.eclipse.stem.core.model.STEMTime, org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue, org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel, long)
 	 */
 	@Override
 	public StandardDiseaseModelLabelValue computeDiseaseDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, final long timeDelta, DiseaseModelLabelValue returnValue) {
 		
 		final SILabelValue currentSI = (SILabelValue) currentState;
 		
 		// This is beta*
 		final double transmissionRate = (getAdjustedTransmissionRate(timeDelta)
 				* getTransmissionRateScaleFactor(diseaseLabel));
 				
 		
 		// The effective Infectious population  is a dimensionles number normalize by total
 		// population used in teh computation of bets*S*i where i = Ieffective/Pop.
 		// This includes a correction to the current
 		// infectious population (Ieffective) based on the conserved exchange of people (circulation)
 		// between regions. Note that this is no the "arrivals" and "departures" which are
 		// a different process.
 		final double effectiveInfectious = getNormalizedEffectiveInfectious(diseaseLabel.getNode(), diseaseLabel, currentSI.getI());
 
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
 		
 		double numberOfSusceptibleToInfected = transmissionRate
 		* currentSI.getS()* Math.pow(effectiveInfectious, getNonLinearityCoefficient());
 		
 		// Compute the number of recovering "infectious" that become "recovered"
 		// this is rescaled by the surviving I population  so we don't move people that
 		// are already dead.
 		double numberOfInfectedToSusceptible = getAdjustedRecoveryRate(timeDelta)
 				* currentSI.getI();
 		
 
 		
 		// Determine delta S
 		final double deltaS = - numberOfSusceptibleToInfected + numberOfInfectedToSusceptible;
 		// Determine delta I
 		final double deltaI = numberOfSusceptibleToInfected - numberOfInfectedToSusceptible;	
 		
 		SILabelValueImpl ret = (SILabelValueImpl)returnValue;
 		ret.setS(deltaS);
 		ret.setI(deltaI);
 		ret.setIncidence(numberOfSusceptibleToInfected);
 		ret.setDiseaseDeaths(0.0);
 		return ret;
 	} // computeDiseaseDeltas
 	
 	
 	
 	
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
 			final StandardDiseaseModelLabelValue currentState) {
 
 	
 		return;
 		
 	} // doModelSpecificAdjustments
 	
 	
 	
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ExperimentalPackage.Literals.PERCOLATION_DISEASE_MODEL;
 	}
 
 	public void doModelSpecificAdjustments(LabelValue label) {
 		// Nothing
 	}
	
	@Override
	public boolean isDeterministic() {
		return true;
	}
 } //CellularDiseaseModelImpl
