 // DiseaseModelScenarioSpecification.java
 package org.eclipse.stem.internal.data.specifications;
 
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
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.Utility;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.model.ModelFactory;
 import org.eclipse.stem.core.model.ModelPackage;
 import org.eclipse.stem.core.model.NodeDecorator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.scenario.ScenarioFactory;
 import org.eclipse.stem.core.scenario.ScenarioPackage;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.core.sequencer.SequencerFactory;
 import org.eclipse.stem.data.geography.ISOKey;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.SIInfector;
 import org.eclipse.stem.diseasemodels.standard.StandardFactory;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 import org.eclipse.stem.internal.data.generatedplugin.DublinCore;
 import org.eclipse.stem.internal.data.geography.specifications.AdminLevelSet;
 import org.eclipse.stem.internal.data.geography.specifications.CountryGraphPropertyFileSpecification;
 
 /**
  * This class represents
  */
 public class DiseaseModelScenarioSpecification extends
 		IdentifiableSpecification implements
 		Comparable<DiseaseModelScenarioSpecification> {
 
 	/**
 	 * The number of people that will be infected
 	 */
 	private static final int INFECTIOUS_COUNT = 10000;
 
 	@SuppressWarnings("unused")
 	private final ModelPackage modelPackage = ModelPackage.eINSTANCE;
 	@SuppressWarnings("unused")
 	private final StandardPackage standardPackage = StandardPackage.eINSTANCE;
 
 	private final DublinCore diseaseModelModelDC;
 
 	private final ISOKey isoKey;
 	private final AdminLevelSet adminLevelSet;
 	private Model diseaseModelModel;
 	private DiseaseModel diseaseModel;
 	private Sequencer sequencer;
 	private SIInfector infector;
 	private String infectedRegionName;
 
 	static {
 		// This sets up the mapping between the "platform" URI's and the
 		// projects in the file system.
 
 		// CWD is the current working directory
 		final String CWD = System.getProperty("user.dir"); //$NON-NLS-1$
 		// WORKSPACE is the directory of the parent workspace
 		final String WORKSPACE = CWD.substring(0, CWD
 				.lastIndexOf(File.separator));
 
 		// The path to the disease models project
 		final String DISEASES_MODELS_MODELS_PROJECT_ROOT = WORKSPACE
 				+ File.separator
 				+ org.eclipse.stem.data.diseasemodels.models.Activator.PLUGIN_ID
 				+ File.separator;
 
 		// The path to the diseases project
 		final String DISEASES_PROJECT_ROOT = WORKSPACE + File.separator
 				+ org.eclipse.stem.diseases.Activator.PLUGIN_ID
 				+ File.separator;
 
 		// The path to the population model project
 		final String POPULATION_DATA_PROJECT_ROOT = WORKSPACE
 				+ File.separator
 				+ org.eclipse.stem.data.geography.population.human.Activator.PLUGIN_ID
 				+ File.separator;
 
 		// The path to the population model project
 		final String POPULATION_MODEL_PROJECT_ROOT = WORKSPACE
 				+ File.separator
 				+ org.eclipse.stem.data.geography.population.human.models.Activator.PLUGIN_ID
 				+ File.separator;
 
 		final Map<String, URI> platformResourceMap = EcorePlugin
 				.getPlatformResourceMap();
 
 		// Register the mapping between the project id and the absolute path to
 		// the project in the file system. This will be found later when
 		// deserializing when the platform URI is mapped.
 
 		platformResourceMap
 				.put(
 						org.eclipse.stem.data.diseasemodels.models.Activator.PLUGIN_ID,
 						URI.createFileURI(DISEASES_MODELS_MODELS_PROJECT_ROOT));
 
 		platformResourceMap.put(
 				org.eclipse.stem.diseases.Activator.PLUGIN_ID, URI
 						.createFileURI(DISEASES_PROJECT_ROOT));
 
 		platformResourceMap
 				.put(
 						org.eclipse.stem.data.geography.population.human.Activator.PLUGIN_ID,
 						URI.createFileURI(POPULATION_DATA_PROJECT_ROOT));
 
 		platformResourceMap
 				.put(
 						org.eclipse.stem.data.geography.population.human.models.Activator.PLUGIN_ID,
 						URI.createFileURI(POPULATION_MODEL_PROJECT_ROOT));
 	} // static
 
 	/**
 	 * @param isoKey
 	 * @param adminLevelSet
 	 * @param diseaseModelModelDC
 	 */
 	public DiseaseModelScenarioSpecification(final ISOKey isoKey,
 			final AdminLevelSet adminLevelSet, DublinCore diseaseModelModelDC) {
 		this.diseaseModelModelDC = diseaseModelModelDC;
 
 		this.isoKey = isoKey;
 		this.adminLevelSet = adminLevelSet;
 		getDublinCore().setFormat(ScenarioPackage.eNS_URI);
 	} // DiseaseModelScenarioSpecification
 
 	/**
 	 * @see org.eclipse.stem.internal.data.specifications.ModelSpecification#createIdentifiableInstance()
 	 */
 	@Override
 	protected Scenario createIdentifiableInstance() {
 		final Scenario retValue = ScenarioFactory.eINSTANCE.createScenario();
 		// The generated Graphs etc., all have dublin core that references
 		// them as plugins. This is as it should be when STEM is running,
 		// but right now we're running stand-alone and want to access the
 		// plugins as projects (they aren't plugged into our running version
 		// of Eclipse). Substitute "resource" for "plugin" in the URI's we
 		// use and all will be well.
 
 		diseaseModelModel = (Model) Utility
 				.getIdentifiable(getIdentifiableProjectURI(diseaseModelModelDC));
 		retValue.setModel(diseaseModelModel);
 
 		diseaseModel = extractDiseaseModel(diseaseModelModel);
 		sequencer = createSequencer();
 		retValue.setSequencer(sequencer);
 		infector = createInfector(diseaseModelModel);
 		retValue.getScenarioDecorators().add(infector);
 		return retValue;
 	} // createIdentifiableInstance
 
 	private DiseaseModel extractDiseaseModel(Model diseaseModelModel) {
 		DiseaseModel retValue = null;
 
		int pos = -1;
 		for (NodeDecorator nodeDecorator : diseaseModelModel
 				.getNodeDecorators()) {
 			if (nodeDecorator.eIsProxy()) {
 				retValue = (DiseaseModel) Utility
 						.getIdentifiable(URI
 								.createURI(IdentifiableSpecification
 										.convertToProjectURI(((InternalEObject) nodeDecorator)
 												.eProxyURI().toString())));
				++pos;
 				break;
 			}
 		} // for each nodeDecorator
		// Replace with the actual disease model
		if(pos >=0) diseaseModelModel.getNodeDecorators().set(pos, retValue);

 		return retValue;
 	} // extractDiseaseModel
 
 	@Override
 	protected String getRelativeSerializationPath() {
 		final StringBuilder sb = new StringBuilder();
 		sb.append(CountryGraphPropertyFileSpecification.COUNTRY_DIR);
 		sb.append(File.separatorChar);
 		sb.append(isoKey.toString());
 		return sb.toString();
 	}
 
 	@Override
 	protected String getSerializationFileNameRoot() {
 		final StringBuilder sb = new StringBuilder(isoKey.toString());
 		sb.append("_"); //$NON-NLS-1$
 		sb.append(adminLevelSet.toString());
 		sb.append("_disease_"); //$NON-NLS-1$
 		if (diseaseModel != null) {
 			sb.append(diseaseModel.getDiseaseName().trim().replace(" ", "_")); //$NON-NLS-1$//$NON-NLS-2$
 		}
 		return sb.toString();
 	}
 
 	@Override
 	protected String getTargetPluginId() {
 		return org.eclipse.stem.data.diseasemodels.scenarios.Activator.PLUGIN_ID;
 	}
 
 	@Override
 	protected String getTitleDescriptor() {
 		final StringBuilder sb = new StringBuilder(isoKey.toString());
 		sb.append(" "); //$NON-NLS-1$
 		sb
 				.append(adminLevelSet.getNumAdminLevels() > 1 ? " (Levels: " : " (Level: "); //$NON-NLS-1$ //$NON-NLS-2$
 		sb.append(adminLevelSet.toString(",")); //$NON-NLS-1$
 		sb.append(") "); //$NON-NLS-1$
 		sb.append(diseaseModel.getDiseaseName());
 		sb.append(", "); //$NON-NLS-1$
 		sb.append(infector.getInfectiousCount());
 		sb.append(" infections in "); //$NON-NLS-1$
 		sb.append(infectedRegionName);
 
 		return sb.toString();
 	} // getTitleDescriptor
 
 	private Sequencer createSequencer() {
 		final Sequencer retValue = SequencerFactory.eINSTANCE
 				.createSequentialSequencer();
 
 		final STEMTime startTime = ModelFactory.eINSTANCE.createSTEMTime();
 		startTime.setTime(Date.valueOf("2000-01-01")); //$NON-NLS-1$
 		retValue.setStartTime(startTime);
 
 		retValue.setCycle((int) STEMTime.Units.DAY.getMilliseconds());
		retValue.setURI(STEMURI.createURI("sequencer/"+STEMURI.generateUniquePart()));
 		return retValue;
 	} // createSequencer
 
 	private SIInfector createInfector(Model diseaseModelModel) {
 		SIInfector retValue = null;
 		// We need to descend into the model and find one location that can be
 		// place where the infection starts.
 
 		for (Model model : diseaseModelModel.getModels()) {
 			if (model.eIsProxy()) {
 				final Model populationModel = (Model) Utility
 						.getIdentifiable(URI
 								.createURI(IdentifiableSpecification
 										.convertToProjectURI(((InternalEObject) model)
 												.eProxyURI().toString())));
 				// The population model?
 				if (populationModel.getURI().toString().contains("population")) { //$NON-NLS-1$
 					// Yes
 					final Graph populationGraph = populationModel.getGraphs()
 							.get(0);
 
 					final Graph poulationData = (Graph) Utility
 							.getIdentifiable(URI
 									.createURI(IdentifiableSpecification
 											.convertToProjectURI(((InternalEObject) populationGraph)
 													.eProxyURI().toString())));
 
 					final EMap<URI, NodeLabel> data = poulationData
 							.getNodeLabels();
 					final NodeLabel nodeLabel = (NodeLabel) data.values()
 							.toArray()[0];
 					final URI targetURI = nodeLabel
 							.getURIOfIdentifiableToBeLabeled();
 
 					final String targetISOKeyString = targetURI.lastSegment();
 
 					// infectedRegionName = GeographicNames
 					// .getName(targetISOKeyString);
 					infectedRegionName = targetISOKeyString;
 
 					retValue = StandardFactory.eINSTANCE.createSIInfector();
					retValue.setURI(STEMURI.createURI("Infector/"+STEMURI.generateUniquePart()));
 					retValue.setTargetURI(targetURI);
 					retValue.setDiseaseName(diseaseModel.getDiseaseName());
 					retValue.setPopulationIdentifier(diseaseModel
 							.getPopulationIdentifier());
 					retValue.setInfectiousCount(INFECTIOUS_COUNT);
 					retValue.setInfectPercentage(false);
 					retValue.getDublinCore().setTitle(
 							"Infect " + INFECTIOUS_COUNT + " " //$NON-NLS-1$ //$NON-NLS-2$
 									+ diseaseModel.getPopulationIdentifier()
 									+ " in " + infectedRegionName); //$NON-NLS-1$
 					break;
 				}
 			}
 		} // for each nodeDecorator
 
 		return retValue;
 	} // createInfector
 
 	/**
 	 * @see org.eclipse.stem.internal.data.specifications.IdentifiableSpecification#serialize(java.lang.String)
 	 */
 	@Override
 	public void serialize(String directory) throws IOException {
 		final List<Identifiable> set = new ArrayList<Identifiable>();
 		set.add(getIdentifiable());
 		set.add(sequencer);
 		if (infector != null) {
 			set.add(infector);
 		}
 
 		Utility.serializeIdentifiables(set, converter.normalize(URI
 				.createFileURI(directory + File.separator
 						+ getRelativeSerializationPathAndFileName())));
 	} // serialize
 
 	public int compareTo(DiseaseModelScenarioSpecification o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	protected String getSerializationFileNameExtension() {
 		return ScenarioPackage.eNAME;
 	}
 
 } // DiseaseModelScenarioSpecification
