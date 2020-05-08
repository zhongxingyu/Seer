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
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.stem.core.Utility;
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.IntegrationLabel;
 import org.eclipse.stem.core.graph.IntegrationLabelValue;
 import org.eclipse.stem.core.graph.LabelValue;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.graph.SimpleDataExchangeLabelValue;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.edges.MigrationEdge;
 import org.eclipse.stem.definitions.transport.PipeTransportEdge;
 import org.eclipse.stem.definitions.transport.PipeTransportEdgeLabelValue;
 import org.eclipse.stem.definitions.transport.impl.PipeStyleTransportSystemImpl;
 import org.eclipse.stem.populationmodels.Activator;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabelValue;
 import org.eclipse.stem.populationmodels.standard.StandardFactory;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModel;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Population Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelImpl#getBirthRate <em>Birth Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelImpl#getDeathRate <em>Death Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.StandardPopulationModelImpl#getTimePeriod <em>Time Period</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class StandardPopulationModelImpl extends PopulationModelImpl implements StandardPopulationModel {
 	/**
 	 * The default value of the '{@link #getBirthRate() <em>Birth Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBirthRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double BIRTH_RATE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getBirthRate() <em>Birth Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBirthRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected double birthRate = BIRTH_RATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDeathRate() <em>Death Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDeathRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double DEATH_RATE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getDeathRate() <em>Death Rate</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDeathRate()
 	 * @generated
 	 * @ordered
 	 */
 	protected double deathRate = DEATH_RATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTimePeriod() <em>Time Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTimePeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final long TIME_PERIOD_EDEFAULT = 86400000L;
 
 	/**
 	 * The cached value of the '{@link #getTimePeriod() <em>Time Period</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTimePeriod()
 	 * @generated
 	 * @ordered
 	 */
 	protected long timePeriod = TIME_PERIOD_EDEFAULT;
 
 	protected Map<Integer, List<PipeTransportEdge>> pipeTransportationUpEdgesMap;
 	protected Map<Integer, List<PipeTransportEdge>> pipeTransportationDownEdgesMap;
 	protected Map<Node, List<PipeTransportEdge>> pipeTransportationNodeEdgesMap;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public StandardPopulationModelImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.STANDARD_POPULATION_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getBirthRate() {
 		return birthRate;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBirthRate(double newBirthRate) {
 		double oldBirthRate = birthRate;
 		birthRate = newBirthRate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE, oldBirthRate, birthRate));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getDeathRate() {
 		return deathRate;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDeathRate(double newDeathRate) {
 		double oldDeathRate = deathRate;
 		deathRate = newDeathRate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE, oldDeathRate, deathRate));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public long getTimePeriod() {
 		return timePeriod;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTimePeriod(long newTimePeriod) {
 		long oldTimePeriod = timePeriod;
 		timePeriod = newTimePeriod;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD, oldTimePeriod, timePeriod));
 	}
 
 	/**
 	 * Decorate the graph for a standard population model
 	 * 
 	 */
 	@Override
 	public boolean decorateGraph(STEMTime time) {
 		if(this.isGraphDecorated()) return true;
 		super.decorateGraph(time);
 		return true;
 	} // decorateGraph
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				return getBirthRate();
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				return getDeathRate();
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				return getTimePeriod();
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
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				setBirthRate((Double)newValue);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				setDeathRate((Double)newValue);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				setTimePeriod((Long)newValue);
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
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				setBirthRate(BIRTH_RATE_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				setDeathRate(DEATH_RATE_EDEFAULT);
 				return;
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				setTimePeriod(TIME_PERIOD_EDEFAULT);
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
 			case StandardPackage.STANDARD_POPULATION_MODEL__BIRTH_RATE:
 				return birthRate != BIRTH_RATE_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL__DEATH_RATE:
 				return deathRate != DEATH_RATE_EDEFAULT;
 			case StandardPackage.STANDARD_POPULATION_MODEL__TIME_PERIOD:
 				return timePeriod != TIME_PERIOD_EDEFAULT;
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
 		result.append(" (birthRate: ");
 		result.append(birthRate);
 		result.append(", deathRate: ");
 		result.append(deathRate);
 		result.append(", timePeriod: ");
 		result.append(timePeriod);
 		result.append(')');
 		return result.toString();
 	}
 
 	@Override
 	public PopulationModelLabel createPopulationLabel() {
 		PopulationModelLabel retValue =  StandardFactory.eINSTANCE.createStandardPopulationModelLabel();
 		retValue.setTypeURI(PopulationModelLabel.URI_TYPE_DYNAMIC_POPULATION_LABEL);
 		return retValue;
 	}
 
 	@Override
 	public PopulationModelLabelValue createPopulationLabelValue() {
 		return StandardFactory.eINSTANCE.createStandardPopulationModelLabelValue();
 	}
 
 	/**
 	 * Compute the changes in the population from the birth/death rate
 	 * adjusted for the time period used in the simulation
 	 * @param time
 	 * @param timeDelta
 	 * @param labels
 	 */
 	public void calculateDelta(STEMTime time, long timeDelta,
 			EList<DynamicLabel> labels) {
 		// We simply calculate the change from the birth/death rate
 		// adjusted for the time period used in the simulation
 		
 		double adjustedBirthRate = adjustRate(this.getBirthRate(), this.getTimePeriod(), timeDelta);
 		double adjustedDeathRate = adjustRate(this.getDeathRate(), this.getTimePeriod(), timeDelta);
 		
 		for(DynamicLabel label:labels) {
 			StandardPopulationModelLabelImpl slabel = (StandardPopulationModelLabelImpl)label;
 			StandardPopulationModelLabelValueImpl delta = (StandardPopulationModelLabelValueImpl)slabel.getDeltaValue();
 			StandardPopulationModelLabelValue current = slabel.getProbeValue();
 			
 			double currentPopulation = current.getCount();
 			double births = currentPopulation * adjustedBirthRate;
 			double deaths = currentPopulation * adjustedDeathRate;
 			delta.setIncidence(births-deaths);
 			delta.setCount(births-deaths);
 			delta.setBirths(births);
 			delta.setDeaths(deaths);
 			
 			if(delta.getArrivals() == null) delta.setArrivals(new HashMap<Node,Double>());
 			if(delta.getDepartures() == null) delta.setDepartures(new HashMap<Node,Double>());
 			
 			delta.getArrivals().put((Node)label.getIdentifiable(), births);
 			delta.getDepartures().put((Node)label.getIdentifiable(), deaths);
 			
 			handleMigration(slabel, delta.getArrivals(),delta.getDepartures(), this.getTimePeriod(), timeDelta, delta);
 			handlePipeTransport(slabel, delta.getArrivals(),delta.getDepartures(), timeDelta, delta);
 		}
 	}// calculateDelta
 
 	public void applyExternalDeltas(STEMTime time, long timeDelta,
 			EList<DynamicLabel> labels) {
 		for (final Iterator<DynamicLabel> currentStateLabelIter = labels
 				.iterator(); currentStateLabelIter.hasNext();) {
 			final StandardPopulationModelLabel plabel = (StandardPopulationModelLabel) currentStateLabelIter
 					.next();
 			
 			StandardPopulationModelLabelValue myDelta = plabel.getDeltaValue();
 			Node n = plabel.getNode();
 			
 			// Find other labels on the node that wants to exchange data
 			
 			EList<NodeLabel> labs = n.getLabels();
 			for(NodeLabel l:labs) {
 				if(l instanceof IntegrationLabel && !l.equals(plabel) &&
 						((IntegrationLabel)l).getIdentifier().equals(plabel.getIdentifier())) {
 					SimpleDataExchangeLabelValue sdeLabelValue = (SimpleDataExchangeLabelValue)((IntegrationLabel)l).getDeltaValue();
 					Map<Node, Double>arrivals = sdeLabelValue.getArrivals();
 					Map<Node, Double>departures = sdeLabelValue.getDepartures();
 					
 					// Arrivals are births. Observe that arrivals should be 0 since 
 					// other decorators are disease models that don't cause an "increase"
 					// in births.
 					
 					if(arrivals != null) 
 						for(Node n2:arrivals.keySet()) 
 							if(n2.equals(n)) // Only the local node makes sense for disease models
 								myDelta.setCount(myDelta.getCount()+arrivals.get(n2));
 					
 					// Departures are deaths 
 					if(departures != null) 
 						for(Node n2:departures.keySet()) 
 							if(n2.equals(n)) // Only the local node makes sense for disease models
 								myDelta.setCount(myDelta.getCount() - departures.get(n2));
 				}
 			}
 
 		}
 	}
 	
 
 	protected void handleMigration(StandardPopulationModelLabelImpl label, Map<Node, Double>arrivals,Map<Node, Double>departures, long timeperiod, long timeDelta, StandardPopulationModelLabelValueImpl delta) {
 		Node n = (Node)label.getIdentifiable();
 		for(Edge e:n.getEdges()) {
 			if(e instanceof MigrationEdge) {
 				MigrationEdge me = (MigrationEdge)e;
 				if(!me.getPopulationIdentifier().equals(label.getPopulationIdentifier())) continue;
 				
 				// Migration is FROM A TO B
 				Node source = me.getA();
 				Node dest = me.getB();
 				
 				boolean leaving = source.equals(n);
 				double rate = me.getLabel().getCurrentValue().getMigrationRate();				
 				if(leaving) {
 					StandardPopulationModelLabelValue val = ((StandardPopulationModelLabelValue) label.getProbeValue());
 					double count = val.getCount();
 					double goodbye = count*rate*(double)timeDelta/(double)timePeriod; // rescale and adjust
 					delta.setCount(delta.getCount()-goodbye);
 					delta.getDepartures().put(dest, goodbye);
 				} else {
 					// Find the population model label on the dest node
 					StandardPopulationModelLabelValue otherVal = null;
 					for(NodeLabel lab:source.getLabels()) {
 						if(lab instanceof StandardPopulationModelLabel && ((StandardPopulationModelLabel)lab).getPopulationIdentifier().equals(label.getPopulationIdentifier())) {
 							otherVal = ((StandardPopulationModelLabel)lab).getTempValue();
 							break;
 						}
 					}
 					if(otherVal == null) {
 						Activator.logError("Found a migration edge but was not able to find the population model label for node "+dest+" population "+label.getPopulationIdentifier(), new Exception());
 						return;
 					}
 					double count = otherVal.getCount();
 					double welcome = count*rate*(double)timeDelta/(double)timePeriod; // rescale and adjust
 					delta.setCount(delta.getCount()+welcome);
 					delta.getArrivals().put(source, welcome);
 				}
 			}
 		}
 		
 	}
 	
 	protected void handlePipeTransport(StandardPopulationModelLabelImpl populationLabel, Map<Node, Double>arrivals,Map<Node, Double>departures, long timeDelta, StandardPopulationModelLabelValueImpl delta) {
 		// Get the pipe transport edges to/from the node
 		Node node = populationLabel.getNode();
 		List<PipeTransportEdge>pedges = pipeTransportationNodeEdgesMap.get(node);
 		if(pedges == null) return; // no edges
 		
 		for(PipeTransportEdge pedge:pedges) {
 			if(!pedge.getPopulationIdentifier().equals(populationLabel.getPopulationIdentifier())) continue; // wrong population
 			
 			boolean incomming = pedge.getB().equals(node);
 			if(incomming) {
 				for(NodeLabel lab: pedge.getA().getLabels()) {
 					if(lab instanceof StandardPopulationModelLabel && ((StandardPopulationModelLabel)lab).getDecorator() == this) {
 						// Make sure the target node has a population model label
 						boolean found = false;
 						for(NodeLabel otherLab:pedge.getB().getLabels()) {
 							if(otherLab instanceof StandardPopulationModelLabel &&
 									((StandardPopulationModelLabel)otherLab).getDecorator().equals(((StandardPopulationModelLabel)lab).getDecorator()))
 									{found=true;break;}
 						}
 						if(!found) continue; // skip edge
 						StandardPopulationModelLabel otherLabel = (StandardPopulationModelLabel)lab;
 						StandardPopulationModelLabelValue otherValue = (StandardPopulationModelLabelValue)otherLabel.getTempValue();
 						StandardPopulationModelLabelValue change = (StandardPopulationModelLabelValue)EcoreUtil.copy(otherValue);
 						PipeTransportEdgeLabelValue edgeLabelValue =  (PipeTransportEdgeLabelValue)pedge.getLabel().getCurrentValue();
 						double maxFlow = edgeLabelValue.getMaxFlow();
 						double flow = maxFlow;
 						double popCount = ((StandardPopulationModelLabelValue)otherLabel.getTempValue()).getCount();
 						if(flow > popCount) flow = popCount; // don't move more people than available.
 						long timePeriod = edgeLabelValue.getTimePeriod();
 						double factor = flow / popCount;
 						
 						factor = factor * timeDelta / timePeriod;
 						if(Double.isNaN(factor) || Double.isInfinite(factor)) factor = 0.0;
 						change.scale(factor);
 						
 						delta.add((IntegrationLabelValue)change);
 						delta.getArrivals().put(pedge.getA(), change.getCount());
 					}
 				}
 			} else { // outgoing edge
 				for(NodeLabel lab: pedge.getA().getLabels()) {
 					if(lab instanceof StandardPopulationModelLabel && ((StandardPopulationModelLabel)lab).getDecorator() == this) {
 						// Make sure the target node has a disease model decorator
 						boolean found = false;
 						for(NodeLabel otherLab:pedge.getB().getLabels()) {
 							if(otherLab instanceof StandardPopulationModelLabel &&
 									((StandardPopulationModelLabel)otherLab).getDecorator().equals(((StandardPopulationModelLabel)lab).getDecorator()))
 									{found=true;break;}
 						}
 						if(!found) continue; // skip edge
 						StandardPopulationModelLabel thisLabel = (StandardPopulationModelLabel)lab;
 						StandardPopulationModelLabelValue thisValue = (StandardPopulationModelLabelValue)thisLabel.getTempValue();
 						StandardPopulationModelLabelValue change = (StandardPopulationModelLabelValue)EcoreUtil.copy(thisValue);
 						PipeTransportEdgeLabelValue edgeLabelValue =  (PipeTransportEdgeLabelValue)pedge.getLabel().getCurrentValue();
 						double maxFlow = edgeLabelValue.getMaxFlow();
 						double popCount = ((StandardPopulationModelLabelValue)thisLabel.getTempValue()).getCount();
 						double flow = maxFlow;
 						if(flow > popCount) flow = popCount;
 						long timePeriod = edgeLabelValue.getTimePeriod();
 						double factor = flow / popCount;
 						factor = factor * timeDelta / timePeriod;
 						if(Double.isNaN(factor) || Double.isInfinite(factor)) factor = 0.0;
 						change.scale(factor);
 						
 						delta.sub((IntegrationLabelValue)change);
 						delta.getDepartures().put(pedge.getB(), change.getCount());
 					}
 				}
 			}
 		} // for each edge		
 	}
 	
 	protected double adjustRate(double rate, long ratePeriod, long actualPeriod) {
 		return rate * ((double)actualPeriod/(double)ratePeriod);
 	}
 	
 	public void doModelSpecificAdjustments(LabelValue label) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public boolean isDeterministic() {
 		return true;
 	}
 
 	@SuppressWarnings("boxing")
 	private void populatePipeSystemNodes() {
 		Graph graph = this.getGraph();
 		
 		if(pipeTransportationUpEdgesMap == null || pipeTransportationDownEdgesMap == null) {
 			initPipeTransport(graph);
 		}
 		Map<Integer, List<PipeTransportEdge>> map = pipeTransportationUpEdgesMap;
 		
 		Integer [] levels = new Integer[map.keySet().size()];
 		levels = map.keySet().toArray(levels);
 		Arrays.sort(levels, 0, levels.length, 
 				new Comparator<Integer>() {
 					public int compare(Integer o1, Integer o2) {
 						if(o1 < o2) return 1;
 					else if(o1 > o2) return -1;
 						return 0;
 					}
 		});
 		
 		
 		for(int level : levels) {
 			List<PipeTransportEdge> edges = map.get(level);
 			
 			for(PipeTransportEdge ptedge:edges) {
 				// Move people from source to destination using the flow of the pipe
 				Node source = ptedge.getA();
 				Node dest = ptedge.getB();
 				if(source == null || dest == null) continue; // ok, the region or transport system is not part of the model
 				
 				PipeTransportEdgeLabelValue label = (PipeTransportEdgeLabelValue) ptedge.getLabel().getCurrentValue();
 				double maxflow = label.getMaxFlow();
 				
 				PopulationModelLabel srcLabel= null;
 				StandardPopulationModelLabelValue nextsrclabelval=null, nextdestlabelval=null, currsrclabelval=null, currdestlabelval=null;
 				String popIdSrc=null;
 				for(NodeLabel nlabel:source.getLabels()) {
 					if(nlabel instanceof PopulationModelLabel) {
 						currsrclabelval = (StandardPopulationModelLabelValue)((PopulationModelLabel)nlabel).getCurrentValue();
 						nextsrclabelval = (StandardPopulationModelLabelValue)((PopulationModelLabel)nlabel).getNextValue();
 						popIdSrc = ((PopulationModelLabel)nlabel).getPopulationIdentifier();
 						srcLabel = (PopulationModelLabel)nlabel;
 					} else continue;
 				
 					for(NodeLabel nlabel2:dest.getLabels()) {
 						if(nlabel2 instanceof PopulationModelLabel &&
 								((PopulationModelLabel)nlabel2).getPopulationIdentifier().equals(popIdSrc)) {
 							currdestlabelval = (StandardPopulationModelLabelValue)((PopulationModelLabel)nlabel2).getCurrentValue();
 							nextdestlabelval  =  (StandardPopulationModelLabelValue)((PopulationModelLabel)nlabel2).getNextValue();
 						}
 					}
 					
 					if(currsrclabelval == null || currdestlabelval == null) {
 						continue; // possible for transport pipes connected to regions above the lowest region part of the model
 					}
 					
 					// Check, make sure we don't move more people than available
 					
 					double flow = maxflow;	
 					if(currsrclabelval.getCount() < flow) flow = currsrclabelval.getCount(); // check
 					
 					double factor = flow / currsrclabelval.getCount();
 					if(Double.isNaN(factor)) factor = 0.0;
 					
 					StandardPopulationModelLabelValue move = null;
 					
 					move = (StandardPopulationModelLabelValue)EcoreUtil.copy(currsrclabelval);
 					
 					move.scale(factor);
 				
//					currdestlabelval.reset(); // clear any existing numbers first
 					currdestlabelval.add((IntegrationLabelValue)move);
 				} // for each label on the source node
 			}
 		}
 		
 		// Check for nodes that have no initial population. Get rid of those
 		ArrayList<Node>nodesToRemove = new ArrayList<Node>();
 		for(Node n:pipeTransportationNodeEdgesMap.keySet()) {
 			boolean remove = false;
 			if( (n instanceof PipeStyleTransportSystemImpl)) {
 				PipeStyleTransportSystemImpl psts = (PipeStyleTransportSystemImpl)n;
 				for(NodeLabel l:psts.getLabels()) {
 					if(l instanceof StandardPopulationModelLabel) {
 						StandardPopulationModelLabel sl = (StandardPopulationModelLabel)l;
 						StandardPopulationModelLabelValue slv = (StandardPopulationModelLabelValue)sl.getCurrentValue();
 						if(slv.getCount() == 0.0) {
 							remove = true;break;
 						}
 					}
 					if(remove)break;
 				}
 				ArrayList<PipeTransportEdge>edgesToRemove = new ArrayList<PipeTransportEdge>();
 				if(remove) {
 					Activator.logInformation("Warning, ignoring air transportation node without population "+n, new Exception());
 					nodesToRemove.add(n);
 					// Remove all air transport edges using the node as well as the node itself
 					for(List<PipeTransportEdge>l :pipeTransportationDownEdgesMap.values()) {
 						for(PipeTransportEdge pse:l) {
 							if(pse.getA() == null || pse.getB() == null) continue;
 							if(pse.getA().equals(n) || pse.getB().equals(n)) {
 								if(!edgesToRemove.contains(pse))edgesToRemove.add(pse);
 							}
 						}
 					}
 					for(List<PipeTransportEdge>l :pipeTransportationUpEdgesMap.values()) {
 						for(PipeTransportEdge pse:l) {
 							if(pse.getA() == null || pse.getB() == null) continue;
 							if(pse.getA().equals(n) || pse.getB().equals(n)) {
 								if(!edgesToRemove.contains(pse))edgesToRemove.add(pse);
 							}
 						}
 					}
 					for(PipeTransportEdge pse:edgesToRemove) { 
 						for(List<PipeTransportEdge>l :pipeTransportationDownEdgesMap.values())
 							l.remove(pse);
 						for(List<PipeTransportEdge>l :pipeTransportationUpEdgesMap.values())
 							l.remove(pse);
 					}
 					for(PipeTransportEdge pse:edgesToRemove) { 
 						for(List<PipeTransportEdge>l :pipeTransportationNodeEdgesMap.values())
 							l.remove(pse);
 						for(List<PipeTransportEdge>l :pipeTransportationNodeEdgesMap.values())
 							l.remove(pse);
 					}
 					
 				}
 			}
 		}
 		for(Node n:nodesToRemove) pipeTransportationNodeEdgesMap.remove(n);
 	}
 	
 	/**
 	 * initialize pipe transport maps organizing pipes by direction (up/down)
 	 * and level
 	 * @param graph
 	 */
 	@SuppressWarnings("boxing")
 	private void initPipeTransport(Graph graph) {
 		pipeTransportationUpEdgesMap = new HashMap<Integer, List<PipeTransportEdge>>();
 		pipeTransportationDownEdgesMap = new HashMap<Integer, List<PipeTransportEdge>>();
 		pipeTransportationNodeEdgesMap = new HashMap<Node, List<PipeTransportEdge>>();
 		// Traverse all pipe transport edges and determine what
 		// geographic level their source (A) node is at
 		for(URI edgeURI : graph.getEdges().keySet()) {
 			Edge edge = graph.getEdges().get(edgeURI);
 			
 			if(edge instanceof PipeTransportEdge) {
 				PipeTransportEdge pedge = (PipeTransportEdge)edge;
 				int beginLevel = Utility.keyLevel(edge.getNodeAURI().lastSegment());
 				int endLevel = Utility.keyLevel(edge.getNodeBURI().lastSegment());
 				
 				Map<Integer, List<PipeTransportEdge>> map;
 				if(beginLevel > endLevel) map = pipeTransportationUpEdgesMap;
 				else map = pipeTransportationDownEdgesMap;
 				
 				if(map.containsKey(beginLevel)) {
 					map.get(beginLevel).add(pedge);
 				} else {
 					ArrayList<PipeTransportEdge> list = new ArrayList<PipeTransportEdge>();
 					list.add(pedge);
 					map.put(beginLevel, list);
 				}
 				
 				Node a = edge.getA();
 				Node b = edge.getB();
 				
 				if(a != null)
 					if(pipeTransportationNodeEdgesMap.containsKey(a))
 						pipeTransportationNodeEdgesMap.get(a).add(pedge);
 					else {
 						ArrayList<PipeTransportEdge> newList = new ArrayList<PipeTransportEdge>();
 						newList.add(pedge);
 						pipeTransportationNodeEdgesMap.put(a, newList);
 					}	
 				
 				if(b != null)
 					if(pipeTransportationNodeEdgesMap.containsKey(b))
 						pipeTransportationNodeEdgesMap.get(b).add(pedge);
 					else {
 						ArrayList<PipeTransportEdge> newList = new ArrayList<PipeTransportEdge>();
 						newList.add(pedge);
 						pipeTransportationNodeEdgesMap.put(b, newList);
 					}	
 			}
 		}
 	}
 	
 		
 	
 	@Override
 	public void resetLabels() {
 		super.resetLabels();
 		// Populate the pipe transportation systems
 		this.populatePipeSystemNodes();
 	}
 } //StandardPopulationModelImpl
