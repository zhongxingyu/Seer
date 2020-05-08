 package org.eclipse.stem.diseasemodels.standard.impl;
 
 /*******************************************************************************
  * Copyright (c) 2006, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.Iterator;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.EdgeLabel;
 import org.eclipse.stem.core.graph.IntegrationLabel;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.graph.SimpleDataExchangeLabelValue;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.edges.MigrationEdgeLabel;
 import org.eclipse.stem.definitions.edges.impl.MigrationEdgeLabelImpl;
 import org.eclipse.stem.definitions.labels.AreaLabel;
 import org.eclipse.stem.definitions.labels.RoadTransportRelationshipLabel;
 import org.eclipse.stem.definitions.labels.impl.CommonBorderRelationshipLabelImpl;
 import org.eclipse.stem.definitions.labels.impl.RoadTransportRelationshipLabelImpl;
 import org.eclipse.stem.definitions.labels.impl.RoadTransportRelationshipLabelValueImpl;
 import org.eclipse.stem.definitions.nodes.Region;
 //import org.eclipse.stem.definitions.labels.impl.TransportRelationshipLabelImpl;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelState;
 import org.eclipse.stem.diseasemodels.standard.Infector;
 //import org.eclipse.stem.diseasemodels.standard.SEIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SI;
 import org.eclipse.stem.diseasemodels.standard.SIInfector;
 import org.eclipse.stem.diseasemodels.standard.SILabel;
 import org.eclipse.stem.diseasemodels.standard.SILabelValue;
 //import org.eclipse.stem.diseasemodels.standard.SIRLabel;
 //import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.SEIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelState;
 import org.eclipse.stem.diseasemodels.standard.StandardFactory;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>SI</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIImpl#getTransmissionRate <em>Transmission Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIImpl#getNonLinearityCoefficient <em>Non Linearity Coefficient</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIImpl#getRecoveryRate <em>Recovery Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIImpl#getInfectiousMortalityRate <em>Infectious Mortality Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIImpl#getPhysicallyAdjacentInfectiousProportion <em>Physically Adjacent Infectious Proportion</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIImpl#getRoadNetworkInfectiousProportion <em>Road Network Infectious Proportion</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SIImpl#getInfectiousMortality <em>Infectious Mortality</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class SIImpl extends StandardDiseaseModelImpl implements SI {
 	/**
 	 * The default value of the '{@link #getTransmissionRate() <em>Transmission Rate</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTransmissionRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double TRANSMISSION_RATE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getTransmissionRate() <em>Transmission Rate</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTransmissionRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected double transmissionRate = TRANSMISSION_RATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getNonLinearityCoefficient() <em>Non Linearity Coefficient</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNonLinearityCoefficient()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double NON_LINEARITY_COEFFICIENT_EDEFAULT = 1.0;
 
 	/**
 	 * The cached value of the '{@link #getNonLinearityCoefficient() <em>Non Linearity Coefficient</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNonLinearityCoefficient()
 	 * @generated
 	 * @ordered
 	 */
 	protected double nonLinearityCoefficient = NON_LINEARITY_COEFFICIENT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getRecoveryRate() <em>Recovery Rate</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getRecoveryRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double RECOVERY_RATE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getRecoveryRate() <em>Recovery Rate</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getRecoveryRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected double recoveryRate = RECOVERY_RATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getInfectiousMortalityRate() <em>Infectious Mortality Rate</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getInfectiousMortalityRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double INFECTIOUS_MORTALITY_RATE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getInfectiousMortalityRate() <em>Infectious Mortality Rate</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getInfectiousMortalityRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected double infectiousMortalityRate = INFECTIOUS_MORTALITY_RATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getPhysicallyAdjacentInfectiousProportion() <em>Physically Adjacent Infectious Proportion</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPhysicallyAdjacentInfectiousProportion()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION_EDEFAULT = 0.05;
 	
 	/**
 	 * For distances less than the REFERENCE_COMMUTE_DISTANCE = 50.0
 	 * we use the default physicallyAdjacentInfectiousProportion
 	 * This then is really the MAXIMUM physicallyAdjacentInfectiousProportion.
 	 * For distances (counties) larger than REFERENCE_COMMUTE_DISTANCE in linear extent,
 	 * we scale the physicallyAdjacentInfectiousProportion by REFERENCE_COMMUTE_DISTANCE/LINEAR EXTENT
 	 * Where Linear Extent is Sqrt(Area).
 	 */
	protected static final double REFERENCE_COMMUTE_DISTANCE = 50.0;
 
 	/**
 	 * The cached value of the '{@link #getPhysicallyAdjacentInfectiousProportion() <em>Physically Adjacent Infectious Proportion</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPhysicallyAdjacentInfectiousProportion()
 	 * @generated
 	 * @ordered
 	 */
 	protected double physicallyAdjacentInfectiousProportion = PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getRoadNetworkInfectiousProportion() <em>Road Network Infectious Proportion</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRoadNetworkInfectiousProportion()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double ROAD_NETWORK_INFECTIOUS_PROPORTION_EDEFAULT = 0.01;
 
 	/**
 	 * The cached value of the '{@link #getRoadNetworkInfectiousProportion() <em>Road Network Infectious Proportion</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRoadNetworkInfectiousProportion()
 	 * @generated
 	 * @ordered
 	 */
 	protected double roadNetworkInfectiousProportion = ROAD_NETWORK_INFECTIOUS_PROPORTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getInfectiousMortality() <em>Infectious Mortality</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getInfectiousMortality()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double INFECTIOUS_MORTALITY_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getInfectiousMortality() <em>Infectious Mortality</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getInfectiousMortality()
 	 * @generated
 	 * @ordered
 	 */
 	protected double infectiousMortality = INFECTIOUS_MORTALITY_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected SIImpl() {
 		super();
 	}
 
 	/**
 	 * @param siDiseaseModel
 	 *            the SI disease model to be initialized
 	 * @param diseaseModelName
 	 * @param backgroundMortalityRate
 	 * @param infectiousMortality
 	 * @param infectiousMortalityRate
 	 *            the rate at which fatally infectious population members die
 	 * @param transmissionRate
 	 * @param recoveryRate
 	 * @param immunityLossRate
 	 * @param incubationRate
 	 * @param nonLinearityCoefficient
 	 * @param timePeriod
 	 * @param populationIdentifier
 	 */
 	protected static SI initializeSIDiseaseModel(final SI siDiseaseModel,
 			final String diseaseModelName,
 			final double backgroundMortalityRate,
 			final double infectiousMortalityRate,
 			final double transmissionRate, final double recoveryRate,
 			final double nonLinearityCoefficient, final long timePeriod,
 			final String populationIdentifier) {
 		StandardDiseaseModelImpl.initializeStandardDiseaseModel(siDiseaseModel,
 				diseaseModelName, backgroundMortalityRate, timePeriod,
 				populationIdentifier);
 		siDiseaseModel.setTransmissionRate(transmissionRate);
 		siDiseaseModel.setRecoveryRate(recoveryRate);
 		siDiseaseModel.setInfectiousMortalityRate(infectiousMortalityRate);
 		siDiseaseModel.setNonLinearityCoefficient(nonLinearityCoefficient);
 		return siDiseaseModel;
 	} // initializeSIRDiseaseModel
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#computeDeaths(org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue,
 	 *      long)
 	 */
 	@Override
 	public StandardDiseaseModelLabelValue computeDiseaseDeathsDeltas(
 			final STEMTime time, final StandardDiseaseModelLabelValue currentLabelValue, final long timeDelta, DiseaseModelLabelValue returnValue) {
 
 		final SILabelValue currentSEIR = (SILabelValue) currentLabelValue;
 		
 		final double adjustedInfectiousMortalityRate = getAdjustedInfectiousMortalityRate(timeDelta);
 		
 		final double diseaseDeaths = adjustedInfectiousMortalityRate
 				* currentSEIR.getI();
 
 		SILabelValueImpl ret = (SILabelValueImpl)returnValue;
 		ret.setS(0.0);
 		ret.setI(-diseaseDeaths);
 		ret.setIncidence(0.0);
 		ret.setDiseaseDeaths(diseaseDeaths);
 		
 		return ret;
 	} // computeBirthDeathsDeltas
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#computeTransitions(org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue,
 	 *      long, double, long)
 	 */
 	@Override
 	public StandardDiseaseModelLabelValue computeDiseaseDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, final long timeDelta, DiseaseModelLabelValue returnValue) {
 		
 		final SILabelValue currentSI = (SILabelValue) currentState;
 		
 		// This is beta*
 		double transmissionRate = getAdjustedTransmissionRate(timeDelta);
 				
 		if(!this.isFrequencyDependent()) transmissionRate *= getTransmissionRateScaleFactor(diseaseLabel);
 
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
 		
 		// Need to checked what do do here. If non linear coefficient is not 1 and
 		// the effective infectious is negative (which is possible), what do do? 
 		// Let's fall back on the linear method for now.
 		double numberOfSusceptibleToInfected = 0.0;
 		if(getNonLinearityCoefficient() != 1.0 && effectiveInfectious >= 0.0)
 			numberOfSusceptibleToInfected = transmissionRate
 			* currentSI.getS()* Math.pow(effectiveInfectious, getNonLinearityCoefficient());
 		else
 			numberOfSusceptibleToInfected = transmissionRate
 			* currentSI.getS()* effectiveInfectious;
 		
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
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#getMigrationDeltas(org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel,
 	 *      org.eclipse.stem.core.model.STEMTime)
 	 */
 	@Override
 	protected StandardDiseaseModelLabelValue getMigrationDeltas(final StandardDiseaseModelLabel diseaseLabel, final STEMTime time, DiseaseModelLabelValue returnValue) {
 		final StandardDiseaseModelLabelValue retValue = (StandardDiseaseModelLabelValue) createDiseaseModelLabelValue();
 		
 		double deltaS = 0.0;
 		double deltaI = 0.0;
 
 		// DO MIGRATION THROUGH MIGRATION EDGES
 	    // to/from THIS node
 		Node node = diseaseLabel.getNode();
 		for (final Iterator<Edge> migrationEdgeIter = MigrationEdgeLabelImpl.getMigrationEdgesFromNode(node).iterator(); migrationEdgeIter.hasNext();) {
 			//every edge is an A=>B Edge. For each node there might be two edges
 			//where THIS node can act as type A or type B (origin or destination)
 			final Edge migrationEdge = migrationEdgeIter.next();
 			MigrationEdgeLabel migrationLabel = (MigrationEdgeLabel) migrationEdge.getLabel();
 			double migrationRate = migrationLabel.getCurrentValue().getMigrationRate();
 			// get the other node
 			final Node otherNode = migrationEdge.getOtherNode(node);
 			// do a test to find out if this edge is an incoming or outgoing edge
 			// by getting the edges' destination node (which could be THIS node)
 			boolean incomming = (migrationEdge.getB().equals(node));
 			// is this incoming
 			if (incomming) {
 				// edge is incoming
 				for (final Iterator<NodeLabel> labelIter = otherNode.getLabels().iterator(); labelIter.hasNext();) {
 					final NodeLabel nodeLabel = labelIter.next();
 					// Is this a disease label?
 					if (nodeLabel instanceof StandardDiseaseModelLabel) {
 						final SILabel otherSILabel = (SILabel) nodeLabel;
 						// Yes
 						// Is it updated by this disease model?
 						if (this == otherSILabel.getDecorator()) {
 							deltaS  += migrationRate*otherSILabel.getTempValue().getS();
 							deltaI += migrationRate*otherSILabel.getTempValue().getI();
 							break;
 						} // if
 					}
 				} // for
 			} else {
 				// edge is outgoing
 				// edge is incoming
 				for (final Iterator<NodeLabel> labelIter = node.getLabels().iterator(); labelIter.hasNext();) {
 					final NodeLabel nodeLabel = labelIter.next();
 					// Is this a disease label?
 					if (nodeLabel instanceof StandardDiseaseModelLabel) {
 						final SILabel thisSILabel = (SILabel) nodeLabel;
 						// Yes
 						// Is it updated by this disease model?
 						if (this == thisSILabel.getDecorator()) {
 							deltaS  -= migrationRate*thisSILabel.getTempValue().getS();
 							deltaI -= migrationRate*thisSILabel.getTempValue().getI();
 							break;
 						} // if
 					}
 				} // for
 			}
 		} // for each migration edge
 		SILabelValueImpl ret = (SILabelValueImpl)returnValue;
 		ret.setS(deltaS);
 		ret.setI(deltaI);
 		ret.setIncidence(0.0);
 		ret.setDiseaseDeaths(0.0);
 		return ret;
 	} // getMigrationDeltas
 
 
 
 	/**
 	 * @param fractionToDepart
 	 * @param nextState
 	 * @return
 	 */
 	@Override
 	protected StandardDiseaseModelLabelValue computeDepartures(
 			final double fractionToDepart,
 			final StandardDiseaseModelLabelValue nextState) {
 		final SILabelValue departees = StandardFactory.eINSTANCE
 				.createSEIRLabelValue();
 		final SILabelValue siState = (SILabelValue) nextState;
 		departees.setS(fractionToDepart * siState.getS());
 		departees.setI(fractionToDepart * siState.getI());
 		return departees;
 	} // computeDepartures
 
 	/**
 	 * This method is used to scale the transmission rate.
  	 * it returns the local density divided by a "reference" density
 	 * @param diseaseLabel the label being processed
 	 * @return the transmission rate scale factor for the label being processed
 	 */
 	public double getTransmissionRateScaleFactor(
 			StandardDiseaseModelLabel diseaseLabel) {
 		final StandardDiseaseModelState sdms = (StandardDiseaseModelState) diseaseLabel
 				.getDiseaseModelState();
 		
 		double referenceDensity = getReferencePopulationDensity();
 		// assert(referenceDensity > 0);
 		// need editor check so ref density always >1. Default is 100.
 		assert getArea(diseaseLabel.getPopulationLabel()) > 0.0;
 		double localDensity = ((StandardDiseaseModelLabelValue)diseaseLabel.getTempValue()).getPopulationCount()/getArea(diseaseLabel.getPopulationLabel());
 		return localDensity/referenceDensity;
 	} // getTransmissionRateScaleFactor
 
 	
 	/**
 	 * The <em>infectious mortality rate</em> is the rate at which
 	 * <em>Infectious</em> population members die. Basically, that rate is
 	 * simply the proportional fraction of the flow into the <em>Infectious</em>
 	 * state.
 	 * 
 	 * @param infectiousInputRate
 	 *            the rate at which population members enter the infectious
 	 *            state (&beta;)
 	 * @param infectiousMortality
 	 *            the proportion of population members that die from the disease
 	 *            (x)
 	 * @return the rate at which population members die from the disease
 	 */
 //	public double computeInfectiousMortalityRate(
 //			final double infectiousInputRate, final double infectiousMortality) {
 //		return infectiousMortality * infectiousInputRate;
 //	} // computeInfectiousMortalityRate
 
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#createDiseaseModelLabel()
 	 */
 	@Override
 	public DiseaseModelLabel createDiseaseModelLabel() {
 		return StandardFactory.eINSTANCE.createSILabel();
 	} // createDiseaseModelLabel
 
 	@Override
 	public DiseaseModelLabelValue createDiseaseModelLabelValue() {
 		return StandardFactory.eINSTANCE.createSILabelValue();
 	} // createDiseaseModelLabelValue
 	
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#createDiseaseModelState()
 	 */
 	@Override
 	public DiseaseModelState createDiseaseModelState() {
 		return StandardFactory.eINSTANCE.createSIDiseaseModelState();
 	} // createDiseaseModelState
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#createInfector()
 	 */
 	@Override
 	public Infector createInfector() {
 		SIInfector retValue = StandardFactory.eINSTANCE.createSIInfector();
 		retValue.setDiseaseName(this.getDiseaseName());
 		retValue.setPopulationIdentifier(getPopulationIdentifier());
 		return retValue;
 	} // createInfector
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.SI;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getTransmissionRate() {
 		return transmissionRate;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param newTransmissionRate
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTransmissionRate(double newTransmissionRate) {
 		double oldTransmissionRate = transmissionRate;
 		transmissionRate = newTransmissionRate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SI__TRANSMISSION_RATE, oldTransmissionRate, transmissionRate));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getRecoveryRate() {
 		return recoveryRate;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param newRecoveryRate
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRecoveryRate(double newRecoveryRate) {
 		double oldRecoveryRate = recoveryRate;
 		recoveryRate = newRecoveryRate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SI__RECOVERY_RATE, oldRecoveryRate, recoveryRate));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getInfectiousMortalityRate() {
 		return infectiousMortalityRate;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setInfectiousMortalityRate(double newInfectiousMortalityRate) {
 		double oldInfectiousMortalityRate = infectiousMortalityRate;
 		infectiousMortalityRate = newInfectiousMortalityRate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SI__INFECTIOUS_MORTALITY_RATE, oldInfectiousMortalityRate, infectiousMortalityRate));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getPhysicallyAdjacentInfectiousProportion() {
 		return physicallyAdjacentInfectiousProportion;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPhysicallyAdjacentInfectiousProportion(double newPhysicallyAdjacentInfectiousProportion) {
 		double oldPhysicallyAdjacentInfectiousProportion = physicallyAdjacentInfectiousProportion;
 		physicallyAdjacentInfectiousProportion = newPhysicallyAdjacentInfectiousProportion;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SI__PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION, oldPhysicallyAdjacentInfectiousProportion, physicallyAdjacentInfectiousProportion));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getRoadNetworkInfectiousProportion() {
 		return roadNetworkInfectiousProportion;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRoadNetworkInfectiousProportion(double newRoadNetworkInfectiousProportion) {
 		double oldRoadNetworkInfectiousProportion = roadNetworkInfectiousProportion;
 		roadNetworkInfectiousProportion = newRoadNetworkInfectiousProportion;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SI__ROAD_NETWORK_INFECTIOUS_PROPORTION, oldRoadNetworkInfectiousProportion, roadNetworkInfectiousProportion));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getInfectiousMortality() {
 		return infectiousMortality;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setInfectiousMortality(double newInfectiousMortality) {
 		double oldInfectiousMortality = infectiousMortality;
 		infectiousMortality = newInfectiousMortality;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SI__INFECTIOUS_MORTALITY, oldInfectiousMortality, infectiousMortality));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public double getAdjustedInfectiousMortalityRate(long timeDelta) {
 		// TODO this division can be eliminated if timeDelta==getTimePeriod
 		return getInfectiousMortalityRate()
 		* ((double) timeDelta / (double) getTimePeriod());
 	} // getAdjustedInfectiousMortalityRate
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getNonLinearityCoefficient() {
 		return nonLinearityCoefficient;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param newNonLinearityCoefficient
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNonLinearityCoefficient(double newNonLinearityCoefficient) {
 		double oldNonLinearityCoefficient = nonLinearityCoefficient;
 		nonLinearityCoefficient = newNonLinearityCoefficient;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SI__NON_LINEARITY_COEFFICIENT, oldNonLinearityCoefficient, nonLinearityCoefficient));
 	}
 
 	/**
 	 * ModelSpecificAdjustments for a Stochastic model adds noise to or adjusts 
 	 * the disease state transition values by multiplying
 	 * the additions by a random variable r ~ (1+/-x) with x small.
 	 * The requirements that no more individuals can be moved from a state than are
 	 * already in that state is still enforced.
 	 * 
 	 */
 	public void doModelSpecificAdjustments(StandardDiseaseModelLabelValue val) {
 	} // doModelSpecificAdjustments
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns the time interval deltaT divided by the initial time period
 	 * @param timeDelta
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public double getAdjustedTransmissionRate(long timeDelta) {
 		return getTransmissionRate()
 				* ((double) timeDelta / (double) getTimePeriod());
 	} // getAdjustedTransmissionRate
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param timeDelta
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public double getAdjustedRecoveryRate(long timeDelta) {
 		return getRecoveryRate()
 				* ((double) timeDelta / (double) getTimePeriod());
 	} // getAdjustedRecoveryRate
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getEffectiveInfectious(Node node, StandardDiseaseModelLabel diseaseLabel, double onsiteInfectious) {
 		// TODO: implement this method
 		// Ensure that you remove @generated or mark it @generated NOT
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * This method replaces the onsiteInfectious value with an effectiveInfectious population
 	 * size based on mixing with neighboring sites. In this implementation the edges are bidirectional
 	 * and have a fixed weight based on getPhysicallyAdjacentInfectiousProportion() (called in the helper method
 	 * getInfectiousChangeFromMixing(). But the mixing is also weighted by the areas and populations of a site relative
 	 * to the area and population of it's neighbors. Note that the value returned must be NORMALIZED by the total
 	 * population because the product beta * S * Ieffective must have units of persons. Since S has units of persons
 	 * the Ieffective must be dimensionless (normalized to the population). beta is the infection rate and has units
 	 * of inverse time.
 	 * @see org.eclipse.stem.diseasemodels.standard.SI.getNormalizedEffectiveInfectious
 	 * @param node the node we are currently looking at to compute the next disease state
 	 * @param diseaseLabel the label for the current disease computation
 	 * @param onsiteInfectious the number of infectious people on site (at the node above)
 	 * @generated NOT
 	 */
 	public double getNormalizedEffectiveInfectious(final Node node, final StandardDiseaseModelLabel diseaseLabel, final double onsiteInfectious) {
         // running tally of changes from mixing with other nodes
 		double infectiousChangeFromMixing = 0.0;
 		double borderDivisor = 0.0;
 		double roadDivisor = 0.0;
 		
 		for (final Iterator<Edge> commonBorderEdgeIter = CommonBorderRelationshipLabelImpl
 				.getCommonBorderEdgesFromNode(node).iterator(); commonBorderEdgeIter
 				.hasNext();) {
 			final Edge borderEdge = commonBorderEdgeIter.next();
 			// If it exists, we're looking for the label this disease model
 			// updates on the node at the other end of the border edge.
 			final Node otherNode = borderEdge.getOtherNode(node);
 			StandardDiseaseModelLabel neighborLabel = null;
 			// sum up the changes from each connected node.
 			// NOTE: some of these changes could be negative
 			
 			///////////////////////////////////////////////////////////////////////
 			// get the Default or MAXIMUM value for the physAdjacentInfProportion
 			double physAdjacentInfProportion = getPhysicallyAdjacentInfectiousProportion();
 			// we need to scale the default Physically Adjacent Infectious proportion by
 			// the AREA of the REGION
 			
 			if (otherNode instanceof Region) {
 				double otherAvgExtent = -1.0;
 				for (final Iterator<NodeLabel> labelIter = otherNode.getLabels().iterator(); labelIter.hasNext();) {
 					final NodeLabel nodeLabel = labelIter.next();
 					// Is this an area label?
 					if (nodeLabel instanceof AreaLabel) {
 						// Yes
 						final AreaLabel areaLabel = (AreaLabel) nodeLabel;
 						otherAvgExtent = areaLabel.getCurrentAreaValue().getAverageExtent();
 						break;
 					}
 				} // for
 				// IF we have a valid area 
 				// then we can re-scale the default Physically Adjacent Infectious proportion
 				if(otherAvgExtent >= 1.0) {
 					double scaleFactor = (REFERENCE_COMMUTE_DISTANCE/otherAvgExtent) ;
 					physAdjacentInfProportion *= scaleFactor;
 					if(physAdjacentInfProportion >= 1.0) physAdjacentInfProportion = 1.0;
 				}
 			}
 			
 			infectiousChangeFromMixing += getInfectiousChangeFromMixing(this, otherNode, diseaseLabel, onsiteInfectious, physAdjacentInfProportion);
 			borderDivisor += getPhysicallyAdjacentInfectiousProportion()*this.getLocalPopulation(this, otherNode);
 		} // for each border edge
 		
 		for (final Iterator<Edge> roadEdgeIter = RoadTransportRelationshipLabelImpl.getRoadEdgesFromNode(node).iterator(); 	roadEdgeIter.hasNext();) {
 			final Edge roadEdge = roadEdgeIter.next();
 			
 			// find the number of edges from the road edge - could be more than one
 			// also, roads have differenct capacities
 			final EdgeLabel edgeLabel = roadEdge.getLabel();
 			// init the number of crossings or total road connections across the border
 			double numCrossings = 1.0;
 		
 			if (edgeLabel instanceof RoadTransportRelationshipLabelImpl) {
 				RoadTransportRelationshipLabelValueImpl roadLabelValue = (RoadTransportRelationshipLabelValueImpl)(edgeLabel.getCurrentValue());
 				numCrossings = roadLabelValue.getNumberCrossings();
 			}
 			double infectiousProportion = getRoadNetworkInfectiousProportion() * numCrossings;
 			
 			// must never be greater than 1
 			if(infectiousProportion > 1.0) infectiousProportion = 1.0;
 			
 			// If it exists, we're looking for the label this disease model
 			// updates on the node at the other end of the border edge.
 			final Node otherNode = roadEdge.getOtherNode(node);
 			StandardDiseaseModelLabel neighborLabel = null;
 			// sum up the changes from each connected node.
 			// NOTE: some of these changes could be negative
 			infectiousChangeFromMixing += getInfectiousChangeFromMixing(this, otherNode, diseaseLabel, onsiteInfectious, infectiousProportion);
 			
 			roadDivisor += infectiousProportion*this.getLocalPopulation(this, otherNode);
 		} // for each road edge
 		
 	
 		// return the sum normalized to the total population
 		double denom = ((StandardDiseaseModelLabelValue)diseaseLabel.getTempValue()).getPopulationCount() + borderDivisor + roadDivisor;
 		double retVal = 0.0;
 		if (denom > 0.0) {
 			retVal = ( onsiteInfectious + infectiousChangeFromMixing ) / denom;
 		}
 		
 		
 		return retVal;
 
 	} // getPhysicallyAdjacentInfectious
 	
 	/**
 	 * This method correctly computes the mixing of the infectious population (onsite) with the infectious population
 	 * at neighboring nodes
 	 * @param diseaseModel
 	 * @param the node
 	 * @param diseaseLabel
 	 * @param onsiteInfectious
 	 * @param connectedInfectiousProportion (this is the weight given to the edge connection)
 	 * @return the number of population members at a node infected by the disease modeled by diseaseModel
 	 */
 	protected double getInfectiousChangeFromMixing(final SI diseaseModel,
 			final Node node, final StandardDiseaseModelLabel diseaseLabel, final double onsiteInfectious, double connectedInfectiousProportion) {
 		double retValue = 0.0;
 
 		// the local area
 		double a0 = getArea(diseaseLabel.getPopulationLabel());
 		// the local population
 
 		double p0 = ((StandardDiseaseModelLabelValue)diseaseLabel.getTempValue()).getPopulationCount();
 		
 		// infectious from other sites mixing here at site 0
 		double mixing = 0.0;
 			
 		for (final Iterator<NodeLabel> labelIter = node.getLabels().iterator(); labelIter
 				.hasNext();) {
 			final NodeLabel nodeLabel = labelIter.next();
 			// Is this a disease label?
 			if (nodeLabel instanceof StandardDiseaseModelLabel) {
 				final IntegrationLabel otherSILabel = (IntegrationLabel) nodeLabel;
 				// Yes
 				// Is it updated by this disease model?
 				if (diseaseModel == otherSILabel.getDecorator()) {
 					if(this.isFrequencyDependent()) {
 						double Iother = (((SILabelValue)otherSILabel.getTempValue())).getI();
 						//double Iother = otherSILabel.getCurrentSIValue().getI();
 						double mixingFactor = connectedInfectiousProportion;					
 						mixing = Iother * mixingFactor;
 					} else {
 //						double a1 = getArea(otherSILabel.getPopulationLabel());
 //						double p1 = ((StandardDiseaseModelLabelValue)otherSILabel.getTempValue()).getPopulationCount();
 //						double Iother = (otherSILabel.getTempValue()).getI();
 //						double mixingFactor = (a0*p1 + a1*p0)* connectedInfectiousProportion /(a1* (p1+p0)) ;
 					
 //						mixing = Iother * mixingFactor;
 					}
 					
 					break;
 				} // if
 			}
 		} // for
 
 		return mixing;
 	} // getInfectious
 	
     private double getLocalPopulation(final SI diseaseModel, Node node) {
     	for (final Iterator<NodeLabel> labelIter = node.getLabels().iterator(); labelIter.hasNext();) {
     		final NodeLabel nodeLabel = labelIter.next();
     		// Is this a disease label?
     		if (nodeLabel instanceof StandardDiseaseModelLabel) {
     			final IntegrationLabel otherSILabel = (IntegrationLabel) nodeLabel;
     			// Yes
     			// Is it updated by this disease model?
     			if (diseaseModel == otherSILabel.getDecorator()) 
     		     return ((StandardDiseaseModelLabelValue)otherSILabel.getTempValue()).getPopulationCount();
     		}
     	}
     	return 0.0;
     }
     
     
 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param resolve
 	 * @param coreType
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.SI__TRANSMISSION_RATE:
 				return new Double(getTransmissionRate());
 			case StandardPackage.SI__NON_LINEARITY_COEFFICIENT:
 				return new Double(getNonLinearityCoefficient());
 			case StandardPackage.SI__RECOVERY_RATE:
 				return new Double(getRecoveryRate());
 			case StandardPackage.SI__INFECTIOUS_MORTALITY_RATE:
 				return new Double(getInfectiousMortalityRate());
 			case StandardPackage.SI__PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION:
 				return new Double(getPhysicallyAdjacentInfectiousProportion());
 			case StandardPackage.SI__ROAD_NETWORK_INFECTIOUS_PROPORTION:
 				return new Double(getRoadNetworkInfectiousProportion());
 			case StandardPackage.SI__INFECTIOUS_MORTALITY:
 				return new Double(getInfectiousMortality());
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param newValue
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case StandardPackage.SI__TRANSMISSION_RATE:
 				setTransmissionRate(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.SI__NON_LINEARITY_COEFFICIENT:
 				setNonLinearityCoefficient(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.SI__RECOVERY_RATE:
 				setRecoveryRate(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.SI__INFECTIOUS_MORTALITY_RATE:
 				setInfectiousMortalityRate(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.SI__PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION:
 				setPhysicallyAdjacentInfectiousProportion(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.SI__ROAD_NETWORK_INFECTIOUS_PROPORTION:
 				setRoadNetworkInfectiousProportion(((Double)newValue).doubleValue());
 				return;
 			case StandardPackage.SI__INFECTIOUS_MORTALITY:
 				setInfectiousMortality(((Double)newValue).doubleValue());
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case StandardPackage.SI__TRANSMISSION_RATE:
 				setTransmissionRate(TRANSMISSION_RATE_EDEFAULT);
 				return;
 			case StandardPackage.SI__NON_LINEARITY_COEFFICIENT:
 				setNonLinearityCoefficient(NON_LINEARITY_COEFFICIENT_EDEFAULT);
 				return;
 			case StandardPackage.SI__RECOVERY_RATE:
 				setRecoveryRate(RECOVERY_RATE_EDEFAULT);
 				return;
 			case StandardPackage.SI__INFECTIOUS_MORTALITY_RATE:
 				setInfectiousMortalityRate(INFECTIOUS_MORTALITY_RATE_EDEFAULT);
 				return;
 			case StandardPackage.SI__PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION:
 				setPhysicallyAdjacentInfectiousProportion(PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION_EDEFAULT);
 				return;
 			case StandardPackage.SI__ROAD_NETWORK_INFECTIOUS_PROPORTION:
 				setRoadNetworkInfectiousProportion(ROAD_NETWORK_INFECTIOUS_PROPORTION_EDEFAULT);
 				return;
 			case StandardPackage.SI__INFECTIOUS_MORTALITY:
 				setInfectiousMortality(INFECTIOUS_MORTALITY_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case StandardPackage.SI__TRANSMISSION_RATE:
 				return transmissionRate != TRANSMISSION_RATE_EDEFAULT;
 			case StandardPackage.SI__NON_LINEARITY_COEFFICIENT:
 				return nonLinearityCoefficient != NON_LINEARITY_COEFFICIENT_EDEFAULT;
 			case StandardPackage.SI__RECOVERY_RATE:
 				return recoveryRate != RECOVERY_RATE_EDEFAULT;
 			case StandardPackage.SI__INFECTIOUS_MORTALITY_RATE:
 				return infectiousMortalityRate != INFECTIOUS_MORTALITY_RATE_EDEFAULT;
 			case StandardPackage.SI__PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION:
 				return physicallyAdjacentInfectiousProportion != PHYSICALLY_ADJACENT_INFECTIOUS_PROPORTION_EDEFAULT;
 			case StandardPackage.SI__ROAD_NETWORK_INFECTIOUS_PROPORTION:
 				return roadNetworkInfectiousProportion != ROAD_NETWORK_INFECTIOUS_PROPORTION_EDEFAULT;
 			case StandardPackage.SI__INFECTIOUS_MORTALITY:
 				return infectiousMortality != INFECTIOUS_MORTALITY_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (transmissionRate: "); //$NON-NLS-1$
 		result.append(transmissionRate);
 		result.append(", nonLinearityCoefficient: "); //$NON-NLS-1$
 		result.append(nonLinearityCoefficient);
 		result.append(", recoveryRate: "); //$NON-NLS-1$
 		result.append(recoveryRate);
 		result.append(", infectiousMortalityRate: "); //$NON-NLS-1$
 		result.append(infectiousMortalityRate);
 		result.append(", physicallyAdjacentInfectiousProportion: "); //$NON-NLS-1$
 		result.append(physicallyAdjacentInfectiousProportion);
 		result.append(", roadNetworkInfectiousProportion: "); //$NON-NLS-1$
 		result.append(roadNetworkInfectiousProportion);
 		result.append(", infectiousMortality: "); //$NON-NLS-1$
 		result.append(infectiousMortality);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#sane()
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 		
 		// transmissionRate
 		retValue = retValue && transmissionRate >= TRANSMISSION_RATE_EDEFAULT;
 		assert retValue;
 
 		retValue = retValue && !Double.isInfinite(transmissionRate);
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(transmissionRate);
 		assert retValue;
 		
 		// recoveryRate
 		retValue = retValue && recoveryRate >= RECOVERY_RATE_EDEFAULT;
 		assert retValue;
 
 		retValue = retValue && !Double.isInfinite(recoveryRate);
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(recoveryRate);
 		assert retValue;
 		
 		// nonLinearityCoefficient
 		retValue = retValue
 				&& nonLinearityCoefficient >= 0.0;
 		assert retValue;
 
 		retValue = retValue && !Double.isInfinite(nonLinearityCoefficient);
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(nonLinearityCoefficient);
 		assert retValue;
 		
 		return retValue;
 	} // sane
 
 } // SIImpl
