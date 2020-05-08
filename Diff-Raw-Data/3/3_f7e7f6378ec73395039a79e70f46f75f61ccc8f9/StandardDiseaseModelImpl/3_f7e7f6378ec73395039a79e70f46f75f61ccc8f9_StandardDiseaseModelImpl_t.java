 package org.eclipse.stem.diseasemodels.standard.impl;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.CyclicBarrier;
 
 import javax.naming.OperationNotSupportedException;
 import javax.xml.transform.SourceLocator;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.resource.impl.DESCipherImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.Utility;
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.IntegrationLabel;
 import org.eclipse.stem.core.graph.IntegrationLabelValue;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.graph.SimpleDataExchangeLabelValue;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.adapters.spatial.geo.preferences.PreferenceConstants;
 import org.eclipse.stem.definitions.edges.MigrationEdgeLabel;
 import org.eclipse.stem.definitions.edges.impl.MigrationEdgeLabelImpl;
 import org.eclipse.stem.definitions.labels.AreaLabel;
 import org.eclipse.stem.definitions.labels.PopulationLabel;
 import org.eclipse.stem.definitions.labels.PopulationLabelValue;
 import org.eclipse.stem.definitions.labels.TransportRelationshipLabel;
 import org.eclipse.stem.definitions.labels.impl.TransportRelationshipLabelImpl;
 import org.eclipse.stem.definitions.nodes.impl.RegionImpl;
 import org.eclipse.stem.definitions.transport.PipeTransportEdge;
 import org.eclipse.stem.definitions.transport.PipeTransportEdgeLabel;
 import org.eclipse.stem.definitions.transport.PipeTransportEdgeLabelValue;
 import org.eclipse.stem.definitions.transport.TransportFactory;
 import org.eclipse.stem.definitions.transport.impl.PipeStyleTransportSystemImpl;
 import org.eclipse.stem.definitions.transport.util.TransportAdapterFactory;
 import org.eclipse.stem.diseasemodels.Activator;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelState;
 //import org.eclipse.stem.diseasemodels.standard.SILabel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SEIRLabel;
 import org.eclipse.stem.diseasemodels.standard.SEIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SILabel;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelState;
 //import org.eclipse.stem.diseasemodels.standard.StandardFactory;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabelValue;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#getTotalPopulationCount <em>Total Population Count</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#getTotalPopulationCountReciprocal <em>Total Population Count Reciprocal</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#getTotalArea <em>Total Area</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.StandardDiseaseModelImpl#getReferencePopulationDensity <em>Reference Population Density</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class StandardDiseaseModelImpl extends DiseaseModelImpl
 		implements StandardDiseaseModel {
 	/**
 	 * The default value of the '{@link #getTotalPopulationCount() <em>Total Population Count</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTotalPopulationCount()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double TOTAL_POPULATION_COUNT_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getTotalPopulationCount() <em>Total Population Count</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTotalPopulationCount()
 	 * @generated
 	 * @ordered
 	 */
 	protected double totalPopulationCount = TOTAL_POPULATION_COUNT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTotalPopulationCountReciprocal() <em>Total Population Count Reciprocal</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTotalPopulationCountReciprocal()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double TOTAL_POPULATION_COUNT_RECIPROCAL_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getTotalPopulationCountReciprocal() <em>Total Population Count Reciprocal</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTotalPopulationCountReciprocal()
 	 * @generated
 	 * @ordered
 	 */
 	protected double totalPopulationCountReciprocal = TOTAL_POPULATION_COUNT_RECIPROCAL_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTotalArea() <em>Total Area</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTotalArea()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double TOTAL_AREA_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getTotalArea() <em>Total Area</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getTotalArea()
 	 * @generated
 	 * @ordered
 	 */
 	protected double totalArea = TOTAL_AREA_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getReferencePopulationDensity() <em>Reference Population Density</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReferencePopulationDensity()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double REFERENCE_POPULATION_DENSITY_EDEFAULT = 100.0;
 
 	/**
 	 * The cached value of the '{@link #getReferencePopulationDensity() <em>Reference Population Density</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReferencePopulationDensity()
 	 * @generated
 	 * @ordered
 	 */
 	protected double referencePopulationDensity = REFERENCE_POPULATION_DENSITY_EDEFAULT;
 
 	
 		
 	/**
 	 * We only need one of these.
 	 * 
 	 * @see #updateLabels(STEMTime, long)
 	 */
 	protected StandardDiseaseModelLabelValue departures = (StandardDiseaseModelLabelValue) createDiseaseModelLabelValue();
 
 	
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected StandardDiseaseModelImpl() {
 		super();
 	}
 
 	/**
 	 * @param standardDiseaseModel
 	 *            the StandardDiseaseModel disease model to be initialized
 	 * @param diseaseModelName
 	 * @param backgroundMortalityRate
 	 * @param immunityLossRate
 	 * @param incubationRate
 	 * @param nonLinearityCoefficient
 	 * @param timePeriod
 	 * @param populationIdentifier
 	 */
 	protected static StandardDiseaseModel initializeStandardDiseaseModel(
 			final StandardDiseaseModel standardDiseaseModel,
 			final String diseaseModelName,
 			final double backgroundMortalityRate, final long timePeriod,
 			final String populationIdentifier) {
 		DiseaseModelImpl.initializeDiseaseModel(standardDiseaseModel,
 				diseaseModelName, backgroundMortalityRate, timePeriod,
 				populationIdentifier);
 
 		return standardDiseaseModel;
 	} // initializeStandardDiseaseModel
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.STANDARD_DISEASE_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getTotalPopulationCount() {
 		return totalPopulationCount;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTotalPopulationCount(double newTotalPopulationCount) {
 		double oldTotalPopulationCount = totalPopulationCount;
 		totalPopulationCount = newTotalPopulationCount;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_POPULATION_COUNT, oldTotalPopulationCount, totalPopulationCount));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getTotalPopulationCountReciprocal() {
 		return totalPopulationCountReciprocal;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getTotalArea() {
 		return totalArea;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTotalArea(double newTotalArea) {
 		double oldTotalArea = totalArea;
 		totalArea = newTotalArea;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_AREA, oldTotalArea, totalArea));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getReferencePopulationDensity() {
 		return referencePopulationDensity;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReferencePopulationDensity(double newReferencePopulationDensity) {
 		double oldReferencePopulationDensity = referencePopulationDensity;
 		referencePopulationDensity = newReferencePopulationDensity;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_DISEASE_MODEL__REFERENCE_POPULATION_DENSITY, oldReferencePopulationDensity, referencePopulationDensity));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addToTotalPopulationCount(double populationCount) {
 		setTotalPopulationCount(totalPopulationCount + populationCount);
 	} // addToTotalPopulationCount
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public double computeTotalPopulationCountReciprocal() {
 		return totalPopulationCountReciprocal = 1.0 / totalPopulationCount;
 	} // computeTotalPopulationCountReciprocal
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addToTotalArea(double area) {
 		setTotalArea(totalArea + area);
 	} // addToTotalArea
 
 	/**
 	 * calculateDelta will use the current label values updated by 
 	 * the disease model and upon return the delta label values
 	 * are set with an estimate of the derivatives to advance to the
 	 * next step  
 	 * 
 	 * @param time current time
 	 * @param timeDelta delta time  step
 	 * @param labels The labels to update
 	 */
 
 	public void calculateDelta(STEMTime time, long timeDelta, EList<DynamicLabel> labels) {
 		// Iterate through each of the labels we need to update.		
 		// Place holders to keep delta values. 
 		
 		DiseaseModelLabelValue pipeDelta = this.createDiseaseModelLabelValue();
 		DiseaseModelLabelValue birthDeathsDelta = this.createDiseaseModelLabelValue();
 		DiseaseModelLabelValue diseaseDelta = this.createDiseaseModelLabelValue();
 	
 		for (final Iterator<DynamicLabel> currentStateLabelIter = labels
 				.iterator(); currentStateLabelIter.hasNext();) {
 			final StandardDiseaseModelLabel diseaseLabel = (StandardDiseaseModelLabel) currentStateLabelIter
 					.next();
 
 			
 			assert diseaseLabel.getPopulationLabel().getPopulationIdentifier()
 					.equals(getPopulationIdentifier());
 			
 			// This is the estimated state of the disease for this label
 			final StandardDiseaseModelLabelValue currentState = (StandardDiseaseModelLabelValue)diseaseLabel
 					.getProbeValue();
 
 
 			// 1) Compute Birth and Deaths state delta changes
 			final StandardDiseaseModelLabelValue diseaseDeathDeltas = computeDiseaseDeathsDeltas(time, diseaseLabel, currentState, timeDelta, birthDeathsDelta);
 				
 			StandardDiseaseModelLabelValue diseaseState = currentState;
 				
 			// 2) Compute the delta changes caused  by the Disease itself
 			final StandardDiseaseModelLabelValue diseaseDeltas = computeDiseaseDeltas(time, diseaseState, diseaseLabel, timeDelta, diseaseDelta);
 			
 			 //  Just capture the incidence that was passed on from computeTransistions
 			final double incidence = diseaseDeltas.getIncidence();
 			final double diseaseDeaths = diseaseDeathDeltas.getDiseaseDeaths();
 			/*
 			 * 5) Record the new state variable values.
 			 * 
 			 * These will become the current state variable values at the end of
 			 * the current simulation cycle and before the next.
 			 */
 
 			// This is the delta disease label
 			final StandardDiseaseModelLabelValue deltaState = (StandardDiseaseModelLabelValue)diseaseLabel
 					.getDeltaValue();
 
 			// Initialize the next state from the current state and then we'll
 			// make the changes to that.
 			//deltaState.set(currentState);
 			
 			// We need to add in the births and deaths on so they'll be counted
 			// as well...
 
 			//diseaseDeltas.setBirths(numberBornSusceptible);
 			//diseaseDeltas.setDeaths(stateDeaths.getDeaths());
 			//diseaseDeltas.setDiseaseDeaths(stateDeaths.getDiseaseDeaths());
 
 			// Now apply the migration/death/disease/birth deltas one at a time
 			
 			// Reset the state
 			deltaState.reset();
 			
 			
 			// 1) Add birth/death deltas and set the departures
 			deltaState.add((IntegrationLabelValue)diseaseDeathDeltas);
 			if(diseaseDeathDeltas.getDiseaseDeaths() > 0.0) 
 				deltaState.getDepartures().put(diseaseLabel.getNode(), diseaseDeathDeltas.getDiseaseDeaths());
 				
 			// 2) Disease deltas
 			deltaState.add((IntegrationLabelValue)diseaseDeltas);
 		
 			// and pass on the incidence
 			deltaState.setIncidence(incidence);
 			// and the disease deaths
 			deltaState.setDiseaseDeaths(diseaseDeaths);
 			
 			pipeDelta.reset();
 			birthDeathsDelta.reset();
 			diseaseDelta.reset();
 				
 		} // for
 	}
 
 	public void applyExternalDeltas(STEMTime time, long timeDelta, EList<DynamicLabel> labels) {
 		for (final Iterator<DynamicLabel> currentStateLabelIter = labels
 				.iterator(); currentStateLabelIter.hasNext();) {
 			final StandardDiseaseModelLabel diseaseLabel = (StandardDiseaseModelLabel) currentStateLabelIter
 					.next();
 			
 			StandardDiseaseModelLabelValue myDelta = (StandardDiseaseModelLabelValue)diseaseLabel.getDeltaValue();
 			Node n = diseaseLabel.getNode();
 			
 			// Find other labels on the node that wants to exchange data
 			
 			EList<NodeLabel> labs = n.getLabels();
 			for(NodeLabel l:labs) {
 				if(l instanceof IntegrationLabel && !l.equals(diseaseLabel)
 						&& ((IntegrationLabel)l).getIdentifier().equals(diseaseLabel.getIdentifier())) {
 					SimpleDataExchangeLabelValue sdeLabelValue = (SimpleDataExchangeLabelValue)((IntegrationLabel)l).getDeltaValue();
 					Map<Node, Double>arrivals = sdeLabelValue.getArrivals();
 					Map<Node, Double>departures = sdeLabelValue.getDepartures();
 					
 					// Arrivals
 					for(Node n2:arrivals.keySet()) {
 						// Arrivals are births and goes into the S state if the node is the local node.
 						if(n2.equals(n))
 							myDelta.setS(myDelta.getS() + arrivals.get(n2));
 						else { // For other nodes, it's migration of population
 							double inflow = arrivals.get(n2);
 							// Find the corresponding disease label on the other node
 							for(NodeLabel nl:n2.getLabels())
 								if(nl instanceof StandardDiseaseModelLabel && ((StandardDiseaseModelLabel)nl).getDecorator().equals(this)) {
 									StandardDiseaseModelLabelValue value = (StandardDiseaseModelLabelValue)EcoreUtil.copy(((StandardDiseaseModelLabel)nl).getTempValue()); 
									if(value.getPopulationCount() > 0.0)
										value.scale(inflow/value.getPopulationCount());
 									myDelta.add((IntegrationLabelValue)value);
 								}
 						}
 					}
 					
 					// Departures
 					for(Node n2:departures.keySet()) {
 						// Departures are either deaths or population moving to other nodes, hence we substract from the local node.
 						StandardDiseaseModelLabelValue currentState = (StandardDiseaseModelLabelValue)EcoreUtil.copy((StandardDiseaseModelLabelValue)diseaseLabel.getTempValue());
 						double populationCount = currentState.getPopulationCount();
 						double factor = departures.get(n2)/populationCount;
 						if(Double.isNaN(factor) || Double.isInfinite(factor)) factor = 0.0; //safe					
 						currentState.scale(factor);
 						myDelta.sub((IntegrationLabelValue)currentState);
 						
 					}	
 				}
 			}
 
 		}
 	}
 	
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_POPULATION_COUNT:
 				return getTotalPopulationCount();
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_POPULATION_COUNT_RECIPROCAL:
 				return getTotalPopulationCountReciprocal();
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_AREA:
 				return getTotalArea();
 			case StandardPackage.STANDARD_DISEASE_MODEL__REFERENCE_POPULATION_DENSITY:
 				return getReferencePopulationDensity();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_POPULATION_COUNT:
 				setTotalPopulationCount((Double)newValue);
 				return;
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_AREA:
 				setTotalArea((Double)newValue);
 				return;
 			case StandardPackage.STANDARD_DISEASE_MODEL__REFERENCE_POPULATION_DENSITY:
 				setReferencePopulationDensity((Double)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_POPULATION_COUNT:
 				setTotalPopulationCount(TOTAL_POPULATION_COUNT_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_AREA:
 				setTotalArea(TOTAL_AREA_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_DISEASE_MODEL__REFERENCE_POPULATION_DENSITY:
 				setReferencePopulationDensity(REFERENCE_POPULATION_DENSITY_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_POPULATION_COUNT:
 				return totalPopulationCount != TOTAL_POPULATION_COUNT_EDEFAULT;
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_POPULATION_COUNT_RECIPROCAL:
 				return totalPopulationCountReciprocal != TOTAL_POPULATION_COUNT_RECIPROCAL_EDEFAULT;
 			case StandardPackage.STANDARD_DISEASE_MODEL__TOTAL_AREA:
 				return totalArea != TOTAL_AREA_EDEFAULT;
 			case StandardPackage.STANDARD_DISEASE_MODEL__REFERENCE_POPULATION_DENSITY:
 				return referencePopulationDensity != REFERENCE_POPULATION_DENSITY_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 	
 	/**
 	 * Standard disease models do not update the labels, it's the
 	 * task of the Solver to do that.
 	 * 
 	 * Standard Disease Models 
 	 * @see org.eclipse.stem.core.model.impl.DecoratorImpl#updateLabels(org.eclipse.stem.core.graph.Graph,
 	 *      org.eclipse.stem.core.model.STEMTime)
 	 */
 	@Override
 	public void updateLabels(final STEMTime time, final long timeDelta, int cycle) {
 		throw new UnsupportedOperationException();
 	}
 	
 	/**
 	 * Populate the pipe system nodes initially
 	 */
 	
 	
 	
 	
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelState,
 	 *      org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 */
 	@Override
 	public DiseaseModelState initializeDiseaseState(
 			final DiseaseModelState diseaseModelState,
 			final DiseaseModelLabel diseaseModelLabel) {
 
 		final PopulationLabel populationLabel = diseaseModelLabel
 				.getPopulationLabel();
 		final double populationCount = populationLabel
 				.getCurrentPopulationValue().getCount();
 
 		// Accumulate the population count in the disease model
 		addToTotalPopulationCount(populationCount);
 
 		double area = getArea(populationLabel);
 		// If we have a bad data set it could be that the area would be
 		// unspecified or zero.
 		// Do we have a bad area value?
 		if (area <= 0.0) {
 			// Yes
 			reportBadAreaValue(populationLabel, area);
 			area = 1.0;
 		} // if bad area value
 
 		// Accumulate the area in the disease model so we'll know the total when
 		// we do our next pass and compute the area ratio
 		addToTotalArea(area);
 	
 		return diseaseModelState;
 	} // initializeDiseaseState
 
 	/**
 	 * Here we compute and set the ratio between the total area and the area
 	 * used for this {@link DiseaseModelLabel}. This value is used to determine
 	 * the <em>transmission scale factor</em>.
 	 * 
 	 * @see #computeTransitions(StandardDiseaseModelLabelValue,
 	 *      StandardDiseaseModelLabel, long)
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 */
 	@Override
 	public void initializeDiseaseState(final DiseaseModelLabel diseaseModelLabel) {
 		final StandardDiseaseModelState sdms = (StandardDiseaseModelState) diseaseModelLabel
 				.getDiseaseModelState();
 		// Is there a population ?
 		if (totalPopulationCount > 0.0) {
 			// Yes
 			double area = getArea(diseaseModelLabel.getPopulationLabel());
 			// Do we have a bad area value?
 			if (area <= 0.0) {
 				// Yes
 				reportBadAreaValue(diseaseModelLabel.getPopulationLabel(), area);
 				area = 1.0;
 			} // if bad area value
 
 			final double ratio = getTotalArea() / area;
 			sdms.setAreaRatio(ratio);
 		}
 	} // initializeDiseaseState
 
 	/**
 	 * @param populationLabel
 	 *            the population label that labels the node
 	 * @return the area of the node associated with the label
 	 */
 	public double getArea(final PopulationLabel populationLabel) {
 		double retValue = 0.0;
 
 		// The population label could have an area specified for the population
 		// that we should use instead of the area of the region labeled by the
 		// population label. This value would be specified if the population was
 		// densely packed into a small area of the larger region, for instance
 		// like a city in an otherwise large desert.
 
 		retValue = populationLabel.getPopulatedArea();
 
 		// Is there an area specified for the population?
 		if (retValue == 0.0) {
 			// No
 			// Ok, go find the area label and return the area of the region
 			for (final Iterator<NodeLabel> labelIter = populationLabel.getNode()
 					.getLabels().iterator(); labelIter.hasNext();) {
 				final NodeLabel nodeLabel = labelIter.next();
 				// Is this an area label?
 				if (nodeLabel instanceof AreaLabel) {
 					// Yes
 					final AreaLabel areaLabel = (AreaLabel) nodeLabel;
 					retValue = areaLabel.getCurrentAreaValue().getArea();
 					break;
 				}
 			} // for
 		} // If no population area specified
 
 		return retValue;
 	} // getArea
 
 	/**
 	 * @param populationLabel
 	 * @param area
 	 */
 	private void reportBadAreaValue(final PopulationLabel populationLabel,
 			double area) {
 		// The bad value could be specified for the node or be an override
 		// value specified for the population.
 		// Is the bad value from the node?
 		if (populationLabel.getPopulatedArea() == 0.0) {
 			// Yes
 			Activator.logError("The area value of \"" + area
 					+ "\" specified for \""
 					+ populationLabel.getNode().toString()
 					+ "\" is not greater than zero (0.0)", null);
 		} // if bad value for node area
 		else {
 			Activator.logError("The area value of \"" + area
 					+ "\" specified for the population \""
 					+ populationLabel.getName() + "\" for the region \""
 					+ populationLabel.getNode().toString()
 					+ "\" is not greater than zero (0.0)", null);
 		}
 	} // reportBadAreaValue
 
 	/**
 	 * Perform disease model specific changes to the additions to each state and
 	 * to the deaths from each state. This method is used to perform stochastic
 	 * modifications to the next disease state.
 	 * 
 	 * @param currentState
 	 *            the current disease state
 	 */
 	public void doModelSpecificAdjustments(
 			final StandardDiseaseModelLabelValue currentState) {
 		// Do nothing here. Sub-classes override this method to make changes
 	} // doModelSpecificAdjustments
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isDeterministic() {
 		// TODO: implement this method
 		// Ensure that you remove @generated or mark it @generated NOT
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * computeDiseaseDeltas. This method calculates the delta changes for each disease state depending 
 	 * on disease parameters and mixing factors
 	 * 
 	 * @param time 
 	 * 		  	  STEM time
 	 * @param currentState
 	 *            the current state of the population
 	 * @param diseaseLabel
 	 *            the disease label for which the state transitions are being
 	 *            computed.
 	 * @param timeDelta
 	 *            the time period (milliseconds) over which the population
 	 *            members transition to new states
 	 * @param cycle
 	 * 			  the simulation cycle we're in
 	 * @return a disease state label value that contains the delta changes in
 	 * 		   population members for each state.
 	 */
 	
 	public abstract StandardDiseaseModelLabelValue computeDiseaseDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabelValue currentState,
 			final StandardDiseaseModelLabel diseaseLabel, 
 			final long timeDelta, 
 			DiseaseModelLabelValue returnValue);
 
 	/**
 	 * computeDiseaseDeathsDeltas Compute the delta vector resulting from disease deaths
 	 * 
 	 * @param time 
 	 * 			  STEM time
 	 * @param diseaseLabel
 	 * @param currentLabelValue
 	 *            the current label value of the disease model
 	 * @param timeDelta
 	 *            the time period over which the population members die
 	 * @return the disease state label value that represents the number of
 	 *         deaths in each state.
 	 */
 	
 	public abstract StandardDiseaseModelLabelValue computeDiseaseDeathsDeltas(
 			final STEMTime time,
 			final StandardDiseaseModelLabel diseaseLabel,
 			final StandardDiseaseModelLabelValue currentLabelValue, 
 			final long timeDelta, 
 			DiseaseModelLabelValue returnValue);
 
 
 	/**
 	 * @param fractionToDepart
 	 * @param nextState
 	 * @return
 	 */
 	abstract protected StandardDiseaseModelLabelValue computeDepartures(
 			double fractionToDepart, StandardDiseaseModelLabelValue nextState);
 
 	
 	@Override
 	public void resetLabels() {
 		super.resetLabels();
 
 		
 		
 		
 	} // resetLabels
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (totalPopulationCount: "); //$NON-NLS-1$
 		result.append(totalPopulationCount);
 		result.append(", totalPopulationCountReciprocal: "); //$NON-NLS-1$
 		result.append(totalPopulationCountReciprocal);
 		result.append(", totalArea: "); //$NON-NLS-1$
 		result.append(totalArea);
 		result.append(", referencePopulationDensity: "); //$NON-NLS-1$
 		result.append(referencePopulationDensity);
 		result.append(')');
 		return result.toString();
 	}
 	
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.DiseaseModelImpl#sane()
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 
 		retValue = retValue
 				&& totalPopulationCount >= TOTAL_POPULATION_COUNT_EDEFAULT;
 		assert retValue;
 
 		retValue = retValue && !Double.isInfinite(totalPopulationCount);
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(totalPopulationCount);
 		assert retValue;
 		
 		retValue = retValue
 				&& totalPopulationCountReciprocal >= TOTAL_POPULATION_COUNT_RECIPROCAL_EDEFAULT;
 		assert retValue;
 
 		retValue = retValue && !Double.isInfinite(totalPopulationCountReciprocal);
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(totalPopulationCountReciprocal);
 		assert retValue;
 		
 		retValue = retValue && totalArea >= TOTAL_AREA_EDEFAULT;
 		assert retValue;
 
 		retValue = retValue && !Double.isInfinite(totalArea);
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(totalArea);
 		assert retValue;
 		
 		return retValue;
 	} // sane
 
 } // StandardDiseaseModelImpl
