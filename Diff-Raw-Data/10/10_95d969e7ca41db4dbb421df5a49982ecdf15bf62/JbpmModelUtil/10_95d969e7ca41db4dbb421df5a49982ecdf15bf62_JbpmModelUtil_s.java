 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  * All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  *
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.util;
 
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.Bpmn2Factory;
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.CatchEvent;
 import org.eclipse.bpmn2.Definitions;
 import org.eclipse.bpmn2.ExtensionAttributeValue;
 import org.eclipse.bpmn2.Import;
 import org.eclipse.bpmn2.Interface;
 import org.eclipse.bpmn2.ItemDefinition;
 import org.eclipse.bpmn2.ItemKind;
 import org.eclipse.bpmn2.Process;
 import org.eclipse.bpmn2.Property;
 import org.eclipse.bpmn2.Relationship;
 import org.eclipse.bpmn2.SequenceFlow;
 import org.eclipse.bpmn2.Task;
 import org.eclipse.bpmn2.ThrowEvent;
 import org.eclipse.bpmn2.UserTask;
 import org.eclipse.bpmn2.modeler.core.adapters.InsertionAdapter;
 import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerFactory;
 import org.eclipse.bpmn2.modeler.core.utils.ImportUtil;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.datatype.DataType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.datatype.DataTypeFactory;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.datatype.DataTypeRegistry;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.datatype.impl.type.EnumDataType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.datatype.impl.type.UndefinedDataType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.BpsimFactory;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.BpsimPackage;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.ControlParameters;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.CostParameters;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.DistributionParameter;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.ElementParameters;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.FloatingParameterType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.NormalDistributionType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.Parameter;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.PoissonDistributionType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.ResourceParameters;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.Scenario;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.ScenarioParameters;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.TimeParameters;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.TimeUnit;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.UniformDistributionType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.drools.DroolsFactory;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.drools.DroolsPackage;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.drools.GlobalType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.drools.ImportType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.bpsim.BPSimDataType;
 import org.eclipse.bpmn2.modeler.ui.property.dialogs.SchemaImportDialog;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 
 public class JbpmModelUtil {
 
 	public static class ImportHandler extends ImportUtil {
 		private boolean createVariables = false;
 		private IType importedType = null;
 		
 	    public Interface createInterface(Definitions definitions, Import imp, IType type) {
 	    	importedType = type;
 	    	return super.createInterface(definitions, imp, type);
 	    }
 	    
 		public ItemDefinition createItemDefinition(Definitions definitions, Import imp, IType clazz) {
 			ItemDefinition itemDef = null;
 			if (clazz!=importedType) {
 				itemDef = findItemDefinition(definitions, imp, clazz);
 				if (itemDef==null) {
 					itemDef = super.createItemDefinition(definitions, imp, clazz);
 					JbpmModelUtil.addImport(clazz, itemDef, false, createVariables);
 					
 					// create process variables for referenced types only, not the containing class
 					if (createVariables) {
 						List<Process> processes = ModelUtil.getAllRootElements(definitions, Process.class);
 						if (processes.size()>0) {
 							Process process = processes.get(0);
 							String structName = clazz.getElementName();
 							int index = structName.lastIndexOf(".");
 							if (index>0)
 								structName = structName.substring(index+1);
 							String varName = structName + "Var";
 							index = 1;
 							boolean done;
 							do {
 								done = true;
 								for (Property p : process.getProperties()) {
 									if (varName.equals(p.getName())) {
 										varName = structName + "Var" + index++; 
 										done = false;
 										break;
 									}
 								}
 							} while (!done);
 							Property var = (Property) Bpmn2ModelerFactory.createFeature(processes.get(0), "properties");
 							var.setName(varName);
 							var.setId(varName);
 							var.setItemSubjectRef(itemDef);
 						}
 					}
 				}
 			}
 			return itemDef;
 		}
 
 		public void setCreateVariables(boolean createVariables) {
 			this.createVariables = createVariables;
 		}
 	}
 	
 	/**
 	 * Helper method to display a Java class import dialog and create a new ImportType. This method
 	 * will also create a corresponding ItemDefinition for the newly imported java type.
 	 * 
 	 * @param object - a context EObject used to search for the Process in which the new
 	 *                 ImportType will be created.
 	 * @return an ImportType object if it was created, null if the user canceled the import dialog.
 	 */
 	public static IType showImportDialog(EObject object) {
		Shell shell = ModelUtil.getEditor(object).getSite().getShell();
 		SchemaImportDialog dialog = new SchemaImportDialog(shell, SchemaImportDialog.ALLOW_JAVA);
 		if (dialog.open() == Window.OK) {
 			Object result[] = dialog.getResult();
 			if (result.length == 1 && result[0] instanceof IType) {
 				return (IType) result[0];
 			}
 		}
 		return null;
 	}
 	
 	public static ImportType addImport(final IType type, final EObject object) {
 		return addImport(type,object,true, false);
 	}
 	
 	public static ImportType addImport(final IType type, final EObject object, final boolean recursive, final boolean createVariables) {
 		if (type==null)
 			return null;
 		
 		final Definitions definitions = ModelUtil.getDefinitions(object);
 		if (definitions==null)
 			return null;
 		
 		Process process = null;
 		if (object instanceof Process)
 			process = (Process)object;
 		else {
 			process = (Process) ModelUtil.findNearestAncestor(object, new Class[] { Process.class });
 			if (process==null) {
 				List<Process> processes = ModelUtil.getAllRootElements(definitions, Process.class);
 				if (processes.size()>1) {
 					// TODO: allow user to pick one?
 					process = processes.get(0);
 				}
 				else if (processes.size()==1)
 					process = processes.get(0);
 				else {
 					if (recursive) {
						Shell shell = ModelUtil.getEditor(object).getSite().getShell();
 						MessageDialog.openError(shell, "Error", "No processes defined!");
 					}
 					return null;
 				}
 			}
 		}
 		
 		final String className = type.getFullyQualifiedName('.');
 		List<ImportType> allImports = ModelUtil.getAllExtensionAttributeValues(process, ImportType.class);
 		for (ImportType it : allImports) {
 			if (className.equals(it.getName())) {
 				if (recursive) {
					Shell shell = ModelUtil.getEditor(object).getSite().getShell();
 					MessageDialog.openWarning(shell, "Warning", "The import '"+className+"' already exists.");
 				}
 				return null;
 			}
 		}
 		
 		final Process fProcess = process;
 		final ImportType newImport = (ImportType)DroolsFactory.eINSTANCE.create(DroolsPackage.eINSTANCE.getImportType());
 		newImport.setName(className);
 
 		TransactionalEditingDomain domain = ModelUtil.getEditor(object).getEditingDomain();
 		domain.getCommandStack().execute(new RecordingCommand(domain) {
 			@Override
 			protected void doExecute() {
 				
 				ImportHandler importer = new ImportHandler();
 				importer.setCreateVariables(createVariables);
 				
 				ModelUtil.addExtensionAttributeValue(fProcess,
 						DroolsPackage.eINSTANCE.getDocumentRoot_ImportType(), newImport);
 				
 				if (recursive) {
 					if (object instanceof ItemDefinition) {
 						// update the ItemDefinition passed to us
 						ItemDefinition oldItemDef = (ItemDefinition)object;
 						String oldName = ModelUtil.getStringWrapperValue(oldItemDef.getStructureRef());
 						// and now update the existing item's structureRef
 						oldItemDef.setItemKind(ItemKind.PHYSICAL);
 						EObject structureRef = ModelUtil.createStringWrapper(className);
 						oldItemDef.setStructureRef(structureRef);
 						importer.createInterface(definitions, null, type);
 					}
 					else {
 						// create a new ItemDefinition
 						importer.createItemDefinition(definitions, null, type);
 					}
 				}
 			}
 		});
 		
 		return newImport;
 	}
 
 	public static void removeImport(ImportType importType) {
 		Definitions definitions = ModelUtil.getDefinitions(importType);
 		Import imp = Bpmn2ModelerFactory.create(Import.class);
 		imp.setImportType(ImportUtil.IMPORT_TYPE_JAVA);
 		imp.setLocation(importType.getName());
 		definitions.getImports().add(imp);
 		ImportHandler.removeImport(imp);
 	}
 	
 	/**
 	 * This method compiles a list of all known "data types" (a.k.a. ItemDefinitions) that
 	 * are in scope for the given context element.
 	 * 
 	 * There are 4 different places to look:
 	 * 1. the Data Type registry, which contains a list of all known "native" types, e.g. java
 	 *    Strings, Integers, etc.
 	 * 2. the list of ImportType extension values in the Process ancestor nearest to the given
 	 *    context object.
 	 * 3. the list of GlobalType extension values, also in the nearest Process ancestor
 	 * 4. the list of ItemDefinitions in the root elements.
 	 * 
 	 * @param object - a context EObject used to search for ItemDefinitions, Globals and Imports
 	 * @return a map of Strings and Objects representing the various data types
 	 */
 	public static Hashtable<String, Object> collectAllDataTypes(EObject object) {
 
 		Hashtable<String,Object> choices = new Hashtable<String,Object>();
 		Definitions defs = ModelUtil.getDefinitions(object);
 
 		// add all native types (as defined in the DataTypeRegistry)
 		DataTypeRegistry.getFactory("dummy");
 		for (Entry<String, DataTypeFactory> e : DataTypeRegistry.instance.entrySet()) {
 			DataType dt = e.getValue().createDataType();
 			if (dt instanceof EnumDataType || dt instanceof UndefinedDataType)
 				continue;
 			String dts = dt.getStringType();
 			
 			ItemDefinition itemDef = null;
 			List<ItemDefinition> itemDefs = ModelUtil.getAllRootElements(defs, ItemDefinition.class);
 			for (ItemDefinition id : itemDefs) {
 				String ids = ModelUtil.getStringWrapperValue(id.getStructureRef());
 				if (ids==null || ids.isEmpty())
 					ids = id.getId();
 				if (ids.equals(dts)) {
 					itemDef = id;
 					break;
 				}
 			}
 			if (itemDef==null) {
 				// create a new ItemDefinition for the jBPM data type
 				itemDef = Bpmn2ModelerFactory.create(ItemDefinition.class);
 				itemDef.setItemKind(ItemKind.PHYSICAL);
 				ModelUtil.setID(itemDef,defs.eResource());
 				itemDef.setStructureRef(ModelUtil.createStringWrapper(dts));
 				InsertionAdapter.add(defs, Bpmn2Package.eINSTANCE.getDefinitions_RootElements(), itemDef);
 			}
 			choices.put(dt.getStringType(),itemDef);
 		}
 		
 		// add all imported data types
 		EObject parent = object;
 		while (parent!=null && !(parent instanceof org.eclipse.bpmn2.Process))
 			parent = parent.eContainer();
 		
 		String s;
 		List<ImportType> imports = ModelUtil.getAllExtensionAttributeValues(parent, ImportType.class);
 		for (ImportType it : imports) {
 			s = it.getName();
 			if (s!=null && !s.isEmpty())
 				choices.put(s, it);
 		}
 		
 		// add all Global variable types
 		List<GlobalType> globals = ModelUtil.getAllExtensionAttributeValues(parent, GlobalType.class);
 		for (GlobalType gt : globals) {
 			s = gt.getType();
 			if (s!=null && !s.isEmpty())
 				choices.put(s, gt);
 		}
 
 		// add all ItemDefinitions
 		List<ItemDefinition> itemDefs = ModelUtil.getAllRootElements(defs, ItemDefinition.class);
 		for (ItemDefinition id : itemDefs) {
 			s = ModelUtil.getStringWrapperValue(id.getStructureRef());
 			if (s==null || s.isEmpty())
 				s = id.getId();
 			choices.put(s,id);
 		}
 		
 		return choices;
 	}
 	
 	/**
 	 * This method returns a string representation for a "data type". This is intended to
 	 * be used to interpret the various objects in the map returned by collectAllDataTypes().
 	 * 
 	 * @param value - one of the Object values in the map returned by collectAllDataTypes().
 	 * @return a string representation of the data type
 	 */
 	public static String getDataType(Object value) {
 		String stringValue = null;
 		if (value instanceof String) {
 			stringValue = (String)value;
 		}
 		else if (value instanceof GlobalType) {
 			stringValue = ((GlobalType)value).getType();
 		}
 		else if (value instanceof DataType) {
 			stringValue = ((DataType)value).getStringType();
 		}
 		else if (value instanceof ImportType) {
 			stringValue = ((ImportType)value).getName();
 		}
 		else if (value instanceof ItemDefinition) {
 			stringValue = ModelUtil.getDisplayName((ItemDefinition)value);
 		}
 		return stringValue;
 	}
 	
 	/**
 	 * This method returns an ItemDefinition object for a "data type". This is intended to
 	 * be used to interpret the various objects in the map returned by collectAllDataTypes().
 	 * 
 	 * NOTE: This method will create an ItemDefinition if it does not already exist.
 	 * 
 	 * @param be - a context EObject used to search for ItemDefinitions, and to create
 	 *                 new ItemDefinitions if necessary.
 	 * @param value - one of the Object values in the map returned by collectAllDataTypes().
 	 * @return an ItemDefinition for the data type
 	 */
 	public static ItemDefinition getDataType(EObject context, Object value) {
 		ItemDefinition itemDef = null;
 		if (value instanceof String) {
 			itemDef = findOrCreateItemDefinition( context, (String)value );
 		}
 		else if (value instanceof GlobalType) {
 			itemDef = findOrCreateItemDefinition( context, ((GlobalType)value).getType() );
 		}
 		else if (value instanceof DataType) {
 			itemDef = findOrCreateItemDefinition( context, ((DataType)value).getStringType() );
 		}
 		else if (value instanceof ImportType) {
 			itemDef = findOrCreateItemDefinition( context, ((ImportType)value).getName() );
 		}
 		else if (value instanceof ItemDefinition) {
 			itemDef = (ItemDefinition)value;
 		}
 		return itemDef;
 	}
 	
 	public static ItemDefinition findOrCreateItemDefinition(EObject context, String structureRef) {
 		ItemDefinition itemDef = null;
 		Definitions definitions = ModelUtil.getDefinitions(context);
 		List<ItemDefinition> itemDefs = ModelUtil.getAllRootElements(definitions, ItemDefinition.class);
 		for (ItemDefinition id : itemDefs) {
 			String s = ModelUtil.getStringWrapperValue(id.getStructureRef());
 			if (s!=null && s.equals(structureRef)) {
 				itemDef = id;
 				break;
 			}
 		}
 		if (itemDef==null)
 		{
 			itemDef = Bpmn2ModelerFactory.create(ItemDefinition.class);
 			itemDef.setStructureRef(ModelUtil.createStringWrapper(structureRef));
 			itemDef.setItemKind(ItemKind.PHYSICAL);
 			definitions.getRootElements().add(itemDef);
 			ModelUtil.setID(itemDef);
 		}
 		return itemDef;
 	}
 	
 	public static BPSimDataType getBPSimData(EObject object) {
 		BPSimDataType processAnalysisData = null;
 		Relationship rel = null;
 		Resource resource = object.eResource();
 		Definitions definitions = (Definitions) ModelUtil.getDefinitions(object);
 		List<Relationship> relationships = definitions.getRelationships();
 		if (relationships.size()==0) {
 			rel = Bpmn2ModelerFactory.create(Relationship.class);
 			definitions.getRelationships().add(rel);
 			rel.getSources().add(definitions);
 			rel.getTargets().add(definitions);
 			rel.setType("Simulation");
 			ModelUtil.setID(rel);
 		}
 		else {
 			rel = relationships.get(0);
 		}
 		
 		for (ExtensionAttributeValue v : ModelUtil.getExtensionAttributeValues(rel)) {
 			for (org.eclipse.emf.ecore.util.FeatureMap.Entry entry : v.getValue()) {
 				if (entry.getValue() instanceof BPSimDataType) {
 					processAnalysisData = (BPSimDataType)entry.getValue();
 					break;
 				}
 			}
 		}
 		if (processAnalysisData==null) {
 			processAnalysisData = BpsimFactory.eINSTANCE.createBPSimDataType();
 			ModelUtil.addExtensionAttributeValue(rel, BpsimPackage.eINSTANCE.getDocumentRoot_BPSimData(), processAnalysisData);
 		}
 		
 		if (processAnalysisData.getScenario().size()==0) {
 			Scenario scenario = BpsimFactory.eINSTANCE.createScenario();
 			ModelUtil.setID(scenario, resource);
 			scenario.setName("Scenario 1");
 			ScenarioParameters scenarioParams = BpsimFactory.eINSTANCE.createScenarioParameters();
 			scenarioParams.setBaseTimeUnit(TimeUnit.MS);
 			scenario.setScenarioParameters(scenarioParams);
 			processAnalysisData.getScenario().add(scenario);
 		}
 		
 		return processAnalysisData;
 
 	}
 	
 	public static ElementParameters getElementParameters(BaseElement be) {
 		ElementParameters elementParams = null;
 		Resource resource = be.eResource();
 		BPSimDataType processAnalysisData = getBPSimData(be);
 		Scenario scenario = processAnalysisData.getScenario().get(0);
 		String id = be.getId();
 		for (ElementParameters ep : scenario.getElementParameters()) {
 			
 			if (id.equals(ep.getId())) {
 				elementParams = ep;
 				break;
 			}
 		}
 		if (elementParams==null) {
 			elementParams = BpsimFactory.eINSTANCE.createElementParameters();
 			elementParams.setId(id);
 			ModelUtil.setID(elementParams, resource);
 			
 			if (be instanceof Task) {
 				TimeParameters timeParams = createTimeParameters(DistributionType.Uniform, 0.0, 1.0, TimeUnit.S);
 				elementParams.setTimeParameters(timeParams);
 				
 				CostParameters costParams = BpsimFactory.eINSTANCE.createCostParameters();
 				costParams.setUnitCost( createParameter(0) );
 				elementParams.setCostParameters(costParams);
 			}
 			
 			if (be instanceof UserTask) {
 				ResourceParameters resourceParams = BpsimFactory.eINSTANCE.createResourceParameters();
 				resourceParams.setQuantity( createParameter(0.0));
 				resourceParams.setAvailability( createParameter(0.0) );
 				elementParams.setResourceParameters(resourceParams);
 			}
 			else if (be instanceof CatchEvent){
 				TimeParameters timeParams = createTimeParameters(1.0, TimeUnit.S);
 				elementParams.setTimeParameters(timeParams);
 			}
 			else if (be instanceof ThrowEvent) {
 				TimeParameters timeParams = createTimeParameters(DistributionType.Uniform, 0.0, 1.0, TimeUnit.S); 
 				elementParams.setTimeParameters(timeParams);
 			}
 			else if (be instanceof SequenceFlow) {
 				ControlParameters controlParams = BpsimFactory.eINSTANCE.createControlParameters();
 				controlParams.setProbability( createParameter(100.0) );
 				elementParams.setControlParameters(controlParams);
 			}
 			scenario.getElementParameters().add(elementParams);
 		}
 		
 		return elementParams;
 	}
 	
 	public static Parameter createParameter(double f) {
 		Parameter param = BpsimFactory.eINSTANCE.createParameter();
 		FloatingParameterType value = BpsimFactory.eINSTANCE.createFloatingParameterType();
 		value.setValue(f);
 		param.getParameterValue().add(value);
 		return param;
 	}
 	
 	public static Parameter createParameter(long i) {
 		Parameter param = BpsimFactory.eINSTANCE.createParameter();
 		FloatingParameterType value = BpsimFactory.eINSTANCE.createFloatingParameterType();
 		value.setValue(Double.valueOf(i));
 		param.getParameterValue().add(value);
 		return param;
 	}
 	
 	public enum DistributionType {
 		Normal, Uniform, Poisson
 	};
 	
 	public static Parameter createParameter(DistributionType distType, double v1, double v2) {
 		Parameter param = BpsimFactory.eINSTANCE.createParameter();
 		DistributionParameter value = null;
 		switch (distType) {
 		case Uniform:
 			value = BpsimFactory.eINSTANCE.createUniformDistributionType();
 			((UniformDistributionType)value).setMin(v1);
 			((UniformDistributionType)value).setMax(v2);
 			break;
 		case Normal:
 			value = BpsimFactory.eINSTANCE.createNormalDistributionType();
 			((NormalDistributionType)value).setMean(v1);
 			((NormalDistributionType)value).setStandardDeviation(v2);
 			break;
 		case Poisson:
 			value = BpsimFactory.eINSTANCE.createPoissonDistributionType();
 			((PoissonDistributionType)value).setMean(v1);
 			break;
 		}
 		param.getParameterValue().add(value);
 		return param;
 	}
 
 	public static TimeParameters createTimeParameters(double t, TimeUnit tu) {
 		TimeParameters timeParams = BpsimFactory.eINSTANCE.createTimeParameters();
 		Parameter param = BpsimFactory.eINSTANCE.createParameter();
 		FloatingParameterType value = BpsimFactory.eINSTANCE.createFloatingParameterType();
 		value.setValue(t);
 		param.getParameterValue().add(value);
 		timeParams.setWaitTime(param);
 //		timeParams.setTimeUnit(tu);
 		return timeParams;
 	}
 
 	public static TimeParameters createTimeParameters(DistributionType dt, double v1, double v2, TimeUnit tu) {
 		TimeParameters timeParams = BpsimFactory.eINSTANCE.createTimeParameters();
 		timeParams.setProcessingTime( createParameter(dt, v1, v2) ); 
 //		timeParams.setTimeUnit(tu);
 		return timeParams;
 	}
 }
