 package org.eclipse.stem.populationmodels.standard.impl;
 
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
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.stem.core.Utility;
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.labels.PopulationLabel;
 import org.eclipse.stem.populationmodels.standard.DemographicPopulationModel;
 import org.eclipse.stem.populationmodels.standard.PopulationGroup;
 import org.eclipse.stem.populationmodels.standard.PopulationModel;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Demographic Population Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.populationmodels.standard.impl.DemographicPopulationModelImpl#getPopulationGroups <em>Population Groups</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class DemographicPopulationModelImpl extends StandardPopulationModelImpl implements DemographicPopulationModel {
 	/**
 	 * The cached value of the '{@link #getPopulationGroups() <em>Population Groups</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPopulationGroups()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<PopulationGroup> populationGroups;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public DemographicPopulationModelImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.DEMOGRAPHIC_POPULATION_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<PopulationGroup> getPopulationGroups() {
 		if (populationGroups == null) {
 			populationGroups = new EObjectContainmentEList<PopulationGroup>(PopulationGroup.class, this, StandardPackage.DEMOGRAPHIC_POPULATION_MODEL__POPULATION_GROUPS);
 		}
 		return populationGroups;
 	}
 
 	
 	/**
 	 * Decorate the graph for a demographic population model
 	 * 
 	 */
 	@Override
 	public boolean decorateGraph(STEMTime time) {
 		if(this.isGraphDecorated()) return true;
 		super.decorateGraph(time);
 		
 		
 		for (final Iterator<PopulationLabel> populationLabelIter = getPopulationLabels(
 				getPopulationIdentifier(), getGraph()).iterator(); populationLabelIter
 				.hasNext();) {
 			final PopulationLabel populationLabel = populationLabelIter.next();
 
 			if(this.getPopulationIdentifier().equals(populationLabel.getPopulationIdentifier())) {
 				
 				// Make sure the node does not already have population model labels for the sub populations.
 				boolean found = false;
 				for(NodeLabel l:populationLabel.getNode().getLabels()) {
 					if(l instanceof PopulationModelLabel)
 						for(PopulationGroup group:this.getPopulationGroups())
 							if(group.getIdentifier().equals(((PopulationModelLabel)l).getPopulationIdentifier()))
 									{found = true;break;}
 					if(found)break;
 				}
 				if(found)continue;
 				
 				
 				// Okay, another demographic population model has not yet added population model labels
 				// for the same population group identifiers, but it might do so in the future depending
 				// upon the order decorateGraph() is called on the decorators. Check if there
 				// is another demographic population model with a higher iso level target node URI that the
 				// node is contained within.
 				
 				found = false;
 				for(Decorator d:this.getGraph().getDecorators()) {
 					 if(!d.equals(this) &&  
 							 d instanceof DemographicPopulationModel)
 						 for(PopulationGroup group:this.getPopulationGroups()) {
 							 for(PopulationGroup group2:((DemographicPopulationModel)d).getPopulationGroups())
 								 if(group.getIdentifier().equals(group2.getIdentifier()) &&
 								    Utility.keyLevel(((PopulationModel)d).getTargetISOKey()) > Utility.keyLevel(this.getTargetISOKey()) &&
 							 isContained(populationLabel.getNode(), (((PopulationModel)d).getTargetISOKey())))
 								 {found = true;break;}
 							 if(found)break;
 						 }
 					 if(found)break;
 				}
 				if(found) continue;
 				
 				// Iterate the groups in the demographic model and divide the numbers
 				for(PopulationGroup group:this.getPopulationGroups()) {
 					final PopulationModelLabel pl = createPopulationLabel();
 					
 					pl.setPopulationLabel(populationLabel);
 					getLabelsToUpdate().add(pl);
 					populationLabel.getNode().getLabels().add(pl);
 					pl.setNode(populationLabel.getNode());
 					getGraph().putNodeLabel(pl);
 					
 					pl.setPopulationIdentifier(group.getIdentifier());
 					StandardPopulationModelLabelValueImpl currentValue = (StandardPopulationModelLabelValueImpl)pl.getCurrentValue();
 					currentValue.setCount(populationLabel.getCurrentPopulationValue().getCount() * group.getFraction());
 				}
 			}
 		} // for each population label
 		return true;
 	} // decorateGraph
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case StandardPackage.DEMOGRAPHIC_POPULATION_MODEL__POPULATION_GROUPS:
 				return ((InternalEList<?>)getPopulationGroups()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.DEMOGRAPHIC_POPULATION_MODEL__POPULATION_GROUPS:
 				return getPopulationGroups();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case StandardPackage.DEMOGRAPHIC_POPULATION_MODEL__POPULATION_GROUPS:
 				getPopulationGroups().clear();
 				getPopulationGroups().addAll((Collection<? extends PopulationGroup>)newValue);
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
 			case StandardPackage.DEMOGRAPHIC_POPULATION_MODEL__POPULATION_GROUPS:
 				getPopulationGroups().clear();
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
 			case StandardPackage.DEMOGRAPHIC_POPULATION_MODEL__POPULATION_GROUPS:
 				return populationGroups != null && !populationGroups.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * We need to override this one to correctly reset the subpopulations
 	 */
 	@Override
 	public void resetLabels() {
 		setEnabled(ENABLED_EDEFAULT);
 		setGraphDecorated(GRAPH_DECORATED_EDEFAULT);
 		for (final Iterator<DynamicLabel> labelIter = getLabelsToUpdate()
 				.iterator(); labelIter.hasNext();) {
 			StandardPopulationModelLabel plm = (StandardPopulationModelLabel)labelIter.next();
 			
 			
 			plm.getCurrentValue().reset();
 			plm.getNextValue().reset();
 			plm.getDeltaValue().reset();
 			plm.getTempValue().reset();
 			plm.getProbeValue().reset();
 			plm.getErrorScale().reset();
 			
 			if(plm.getPopulationIdentifier().equals(this.getPopulationIdentifier())) {
 				double originalCount = plm.getPopulationLabel().getCurrentPopulationValue().getCount();
 				((StandardPopulationModelLabelValue)plm.getCurrentValue()).setCount(originalCount);
 			} else {
 				// The label is from one of the subgroups. Figure out the fraction
 				for(PopulationGroup g:this.getPopulationGroups()) {
 					if(g.getIdentifier().equals(plm.getPopulationIdentifier())) {
 						double originalCount = plm.getPopulationLabel().getCurrentPopulationValue().getCount();
 						((StandardPopulationModelLabelValue)plm.getCurrentValue()).setCount(originalCount*g.getFraction());
 						break;
 					}
 				}
 			}
 		}
		
		// Populate the pipe transportation systems
		super.populatePipeSystemNodes();
 	} // resetLabels
 } //DemographicPopulationModelImpl
