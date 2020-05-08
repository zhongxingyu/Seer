 /**
  * Feature Model Plugin
  * 
  * Copyright (c) 2011
  * All rights reserved.
  * 
  * Luca Gherardi
  * University of Bergamo
  * Dept. of Information Technology and Mathematics
  * 
  * ***********************************************************************************************
  * 
  * Author: <A HREF="mailto:luca.gherardi@unibg.it">Luca Gherardi</A>
  * 
  * Supervised by: <A HREF="mailto:brugali@unibg.it">Davide Brugali</A>
  * 
  * ***********************************************************************************************
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * 
  */
 package it.unibg.robotics.resolutionmodels.rtt.commands.resolution.handlers;
 
 import it.unibg.robotics.featuremodels.Feature;
 import it.unibg.robotics.featuremodels.FeatureModel;
 import it.unibg.robotics.featuremodels.Instance;
 import it.unibg.robotics.featuremodels.featuremodelsPackage;
 import it.unibg.robotics.resolutionmodels.RMAbstractTransformation;
 import it.unibg.robotics.resolutionmodels.RMResolutionElement;
 import it.unibg.robotics.resolutionmodels.ResolutionModel;
 import it.unibg.robotics.resolutionmodels.presentation.resolutionmodelsEditor;
 import it.unibg.robotics.resolutionmodels.rttresolutionmodels.RTTConnection;
 import it.unibg.robotics.resolutionmodels.rttresolutionmodels.RTTRequiredComponents;
 import it.unibg.robotics.resolutionmodels.rttresolutionmodels.RTTRequiredConnections;
 import it.unibg.robotics.resolutionmodels.rttresolutionmodels.RTTTransfConnection;
 import it.unibg.robotics.resolutionmodels.rttresolutionmodels.RTTTransfImplementation;
 import it.unibg.robotics.resolutionmodels.rttresolutionmodels.RTTTransfProperty;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.part.FileEditorInput;
 import org.orocos.model.rtt.Activity;
 import org.orocos.model.rtt.ConnectionPolicy;
 import org.orocos.model.rtt.IActivity;
 import org.orocos.model.rtt.InputPort;
 import org.orocos.model.rtt.Operation;
 import org.orocos.model.rtt.OutputPort;
 import org.orocos.model.rtt.Package;
 import org.orocos.model.rtt.Property;
 import org.orocos.model.rtt.Slave;
 import org.orocos.model.rtt.TaskContext;
 import org.orocos.model.rtt.diagram.edit.parts.PackageEditPart;
 import org.orocos.model.rtt.diagram.part.RttDiagramEditor;
 import org.orocos.model.rtt.diagram.part.RttDiagramEditorPlugin;
 import org.orocos.model.rtt.impl.RttFactoryImpl;
 
 
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class RTTResolutionHandler extends AbstractHandler {
 
 	private Hashtable<InputPort, InputPort> inputPortHashTable;
 	private Hashtable<OutputPort, OutputPort> outputPortHashTable;
 	private Hashtable<TaskContext, TaskContext> taskContextHashTable;
 	private Hashtable<Property, Property> propertyHashTable;
 	private Hashtable<ConnectionPolicy, ConnectionPolicy> connectionHashTable;
 
 	private Package targetOrocosModel = null;
 	private Package sourceRttModel = null;
 	private FeatureModel sourceFeatureModel = null;
 	private ResolutionModel resolutionModel = null;
 
 	/*
 	 * We don't use treeSet because there is a problem due to the fact that TaskContext
 	 * doesn't implement comparable
 	 */
 	private ArrayList<TaskContext> requiredComponents;
 	private ArrayList<ConnectionPolicy> requiredConnections;
 
 
 	/**
 	 * The constructor.
 	 */
 	public RTTResolutionHandler() {
 	}
 
 	/**
 	 * the command has been executed, so extract extract the needed information
 	 * from the application context.
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 
 		resolutionmodelsEditor resolutionModelEditor = null;
 
 		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
 
 
 		if(activeEditor instanceof resolutionmodelsEditor){
 
 			resolutionModelEditor = (resolutionmodelsEditor)activeEditor;
 			StructuredSelection structSelect = (StructuredSelection)resolutionModelEditor.getSite().getSelectionProvider().getSelection();
 
 			if(structSelect.getFirstElement() instanceof ResolutionModel){
 
 				resolutionModel = (ResolutionModel)structSelect.getFirstElement();
 
 				sourceFeatureModel = getSourceFeatureModel();
 
 				sourceRttModel =  getSourceRttModel();
 				if(sourceRttModel == null){
 					MessageDialog.openError(null, "Error", 
 							"It wasn't possible to find the Template System Model!!!");
 					return null;
 				}
 				
 				cloneRttPackage(sourceRttModel);
 
 				/* 
 				 * Creating a copy is not enough
 				 * We have to fill the hashtables
 				 */ 
 				//				targetOrocosModel = EcoreUtil.copy(sourceRttModel);
 
 
 				Instance instance;
 				
 				if(sourceFeatureModel.getInstances().size() < 1){
 					MessageDialog.openWarning(null, "Warning", 
 							"You have to create at least an instance!!!");
 					return null;
 				}
 
 				ElementListSelectionDialog instanceDialog = 
 						new ElementListSelectionDialog(
 								PlatformUI.getWorkbench().getDisplay().getActiveShell(), new LabelProvider());
 				instanceDialog.setTitle("Instance Selection");
 				instanceDialog.setMessage("Select the desired instance");
 				instanceDialog.setElements(sourceFeatureModel.getInstances().toArray());
 				if(instanceDialog.open() != Window.OK){
 					return null;
 				}
 				instance = (Instance)instanceDialog.getResult()[0];
 				
 				doTransformation(instance);
 
 				// Create a resource set to hold the resources.
 				ResourceSet resourceSet = new ResourceSetImpl();
 
 				// Register the appropriate resource factory to handle all file extensions.
 				resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put
 				(Resource.Factory.Registry.DEFAULT_EXTENSION, 
 						new XMIResourceFactoryImpl());
 
 				// Register the package to ensure it is available during loading.
 				resourceSet.getPackageRegistry().put(featuremodelsPackage.eNS_URI, featuremodelsPackage.eINSTANCE);
 
 				// If there are no arguments, emit an appropriate usage message.
 				try {
 
 					IEditorInput editorPart = activeEditor.getEditorInput();
 					IFileEditorInput fileEditorInput = null;
 
 					String modelfilePath = "";
 					String diagramfilePath = "";
 					if(editorPart instanceof IFileEditorInput){
 						fileEditorInput = (IFileEditorInput)editorPart;
 					}
 					IProject currentProject = fileEditorInput.getFile().getProject();
 
 					String tmp = fileEditorInput.getFile().getLocation().toOSString();
 					tmp = tmp.substring(0,modelfilePath.lastIndexOf('/')+1) + instance.getId();
 					modelfilePath = tmp + "-configured.rtt";
 					diagramfilePath = tmp + "-configured.rtt_diagram";
 					IPath modelLocation = new Path(modelfilePath);
 					IFile modelfile = currentProject.getFile(modelLocation.lastSegment());
 					if(modelfile.exists()){
 						FileDialog fileDialog = new FileDialog(new Shell(), SWT.SAVE);
 						fileDialog.setFilterNames(new String[]{"RTT","All files"});
 						fileDialog.setFilterExtensions(new String[]{"*.rtt","*.*"});
 						modelfilePath = fileDialog.open();
 						if(modelfilePath == null){
 							return null;
 						}
 						diagramfilePath = modelfilePath + "_diagram";
 						modelLocation = new Path(modelfilePath);
 						modelfile = currentProject.getFile(modelLocation.lastSegment());
 					}
 					URI modelURI = URI.createFileURI(modelLocation.toOSString());
 					Resource modelResource = resourceSet.createResource(modelURI);
 
 					IPath diagramLocation = new Path(diagramfilePath);
 					IFile diagramFile = currentProject.getFile(diagramLocation.lastSegment());
 					URI diagramURI = URI.createFileURI(diagramLocation.toString());
 					Resource diagramResource = resourceSet.createResource(diagramURI);
 
 					// create the new model and save it
 					modelResource.getContents().add(targetOrocosModel);
 
 					ByteArrayOutputStream baos = new ByteArrayOutputStream();
 					modelResource.save(baos, null);
 
 					ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
 					modelfile.create(bais, false, null);
 					baos.close();
 					bais.close();
 
 					// create the new diagram and save it
 					Diagram diagram = ViewService.createDiagram(targetOrocosModel,
 							PackageEditPart.MODEL_ID,
 							RttDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
 					if (diagram != null) {
 						diagramResource.getContents().add(diagram);
 						diagram.setName(modelLocation.lastSegment());
 						diagram.setElement(targetOrocosModel);
 					}
 
 					//						diagramResource.save(RttDiagramEditorUtil.getSaveOptions());
 					baos = new ByteArrayOutputStream();
 					diagramResource.save(baos, null);
 					bais = new ByteArrayInputStream(baos.toByteArray());
 					diagramFile.create(bais, false, null);
 					baos.close();
 					bais.close();
 
 					//				RttDiagramEditorUtil.openDiagram(diagramResource);
 
 					IWorkbenchPage page = PlatformUI.getWorkbench()
 							.getActiveWorkbenchWindow().getActivePage();
 					page.openEditor(new FileEditorInput(diagramFile), RttDiagramEditor.ID);
 
 
 				}
 				catch (IOException exception) {
 					exception.printStackTrace();
 				} catch (CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}
 
 		//		MessageDialog.openInformation(
 		//				window.getShell(),
 		//				"New Model",
 		//				"File Saved");
 		return null;
 	}
 
 
 	private void doTransformation(Instance featureModelInstance){
 
 		requiredComponents = new ArrayList<TaskContext>();
 		requiredConnections = new ArrayList<ConnectionPolicy>();
 
 
 		for(RMResolutionElement currentResElem : resolutionModel.getResolutionElements()){
 
			boolean isActive = featureModelInstance.getSelectedFeatures().contains(currentResElem.getFeature()) ==
 					currentResElem.isExecutedWhenFeatureIsSelected();
 			
 			if( ! isActive ){
 
 				// The feature associated to the resolution element is not selected
 				// or the resolution element has to be applied when the feature is not selected
 				continue;
 
 			}
 
 			if(currentResElem.getRequiredComponents() != null){ 
 				if(currentResElem.getRequiredComponents() instanceof RTTRequiredComponents){
 					RTTRequiredComponents requiredTaskContextsTmp = (RTTRequiredComponents)currentResElem.getRequiredComponents();
 					for(TaskContext tc : requiredTaskContextsTmp.getRTTTaskContexts()){
 						if(! requiredComponents.contains(tc)){
 							requiredComponents.add(taskContextHashTable.get(tc));
 						}
 					}
 				}else{
 					MessageDialog.openError(null, "Error", 
 							"The RTT resolution element " + currentResElem.getName() + 
 							" contains a " + currentResElem.getRequiredComponents().getClass()
 							+ "instance!!!");
 					return;
 				}
 			}
 
 			if(currentResElem.getRequiredConnections() != null){
 				if(currentResElem.getRequiredConnections() instanceof RTTRequiredConnections){
 					RTTRequiredConnections requiredConnectionsTmp = (RTTRequiredConnections)currentResElem.getRequiredConnections();
 					for(ConnectionPolicy cp : requiredConnectionsTmp.getRTTConnectionPolicies()){
 						if(! requiredConnections.contains(cp)){
 							requiredConnections.add(connectionHashTable.get(cp));
 						}
 					}
 				}else{
 					MessageDialog.openError(null, "Error", 
 							"The RTT resolution element " + currentResElem.getName() + 
 							" contains a " + currentResElem.getRequiredConnections().getClass()
 							+ "instance!!!");
 					return;
 				}
 
 			}
 
 			for(RMAbstractTransformation currentTransf : currentResElem.getTransformations()){
 
 				if(currentTransf instanceof RTTTransfImplementation){
 
 					RTTTransfImplementation transf = (RTTTransfImplementation)currentTransf;
 
 					TaskContext targetTaskContext = taskContextHashTable.get(transf.getTargetTaskContext());
 
 					targetTaskContext.setNamespace(transf.getClassNamespace());
 					targetTaskContext.setType(transf.getClassName());
 
 					// add the target task context to the required components list
 					// it should be already there,
 					// This is done for the case in which the user forget to add it
 					if(! requiredComponents.contains(targetTaskContext)){
 						requiredComponents.add(targetTaskContext);
 					}
 
 
 				}else if(currentTransf instanceof RTTTransfConnection){
 
 					RTTTransfConnection transf = (RTTTransfConnection)currentTransf;
 
 					for(RTTConnection newRttConnection : transf.getNewConnections()){
 
 						ConnectionPolicy newConnection = RttFactoryImpl.eINSTANCE.createConnectionPolicy();
 						newConnection.setName(newRttConnection.getName());
 						newConnection.setLockPolicy(newRttConnection.getLockPolicy());
 						newConnection.setType(newRttConnection.getType());
 						newConnection.setBufferSize(newRttConnection.getBufferSize());
 
 						InputPort targetInputPort = inputPortHashTable.get(newRttConnection.getInputPort());
 						targetInputPort.setInputConnectionPolicy(newConnection);
 
 						OutputPort targetOutputPort = outputPortHashTable.get(newRttConnection.getOutputPort());
 						targetOutputPort.setOutputConnectionPolicy(newConnection);
 
 						newConnection.setInputPort(targetInputPort);
 						newConnection.setOutputPort(targetOutputPort);
 
 						targetOrocosModel.getConnectionPolicy().add(newConnection);
 
 						// add the new connection to the required connections list
 						if(! requiredConnections.contains(newConnection)){
 							requiredConnections.add(newConnection);
 						}
 						// we add also the connected components
 						if(! requiredComponents.contains((TaskContext)newConnection.getInputPort().eContainer())){
 							requiredComponents.add((TaskContext)newConnection.getInputPort().eContainer());
 						}
 						if(! requiredComponents.contains((TaskContext)newConnection.getOutputPort().eContainer())){
 							requiredComponents.add((TaskContext)newConnection.getOutputPort().eContainer());
 						}
 
 
 					}
 
 				}else if(currentTransf instanceof RTTTransfProperty){
 
 
 					RTTTransfProperty transf = (RTTTransfProperty)currentTransf;
 
 					Property targetProperty = propertyHashTable.get(transf.getTargetProperty());
 					targetProperty.setValue(transf.getValue());
 
 					// add the target task context to the required components list
 					// it should be already there,
 					// This is done for the case in which the user forget to add it
 					if(! requiredComponents.contains((TaskContext)targetProperty.eContainer())){
 						requiredComponents.add((TaskContext)targetProperty.eContainer());
 					}
 
 				}
 
 			}
 		}
 
 		/*
 		 *  Here we check and remove connections and components that are not anymore used
 		 *  We first create an array of the elements that have to be removed, than we remove them
 		 *  If we directly remove during the iteration an exception is thrown
 		 */
 		ArrayList<ConnectionPolicy> unusedConnection = new ArrayList<ConnectionPolicy>();
 
 		for(ConnectionPolicy connection : targetOrocosModel.getConnectionPolicy()){
 
 			if(! requiredConnections.contains(connection)){
 				unusedConnection.add(connection);
 			}
 
 		}
 
 		for(ConnectionPolicy connection : unusedConnection){
 			connection.getInputPort().setInputConnectionPolicy(null);
 			connection.getOutputPort().setOutputConnectionPolicy(null);
 
 			targetOrocosModel.getConnectionPolicy().remove(connection);
 		}
 
 		ArrayList<TaskContext> unusedTaskContext = new ArrayList<TaskContext>();
 
 		for(TaskContext taskContext : targetOrocosModel.getTaskContext()){
 
 			if(! requiredComponents.contains(taskContext)){
 				unusedTaskContext.add(taskContext);
 			}
 
 		}
 
 		targetOrocosModel.getTaskContext().removeAll(unusedTaskContext);
 
 	}
 
 
 	private void cloneRttPackage(Package source){
 
 		targetOrocosModel = RttFactoryImpl.eINSTANCE.createPackage();
 
 		targetOrocosModel.setName(source.getName());
 
 		taskContextHashTable = new Hashtable<TaskContext, TaskContext>();
 		inputPortHashTable = new Hashtable<InputPort, InputPort>();
 		outputPortHashTable  = new Hashtable<OutputPort, OutputPort>();
 		propertyHashTable = new Hashtable<Property, Property>();
 		connectionHashTable = new Hashtable<ConnectionPolicy, ConnectionPolicy>();
 
 		for (TaskContext sourceTaskContext : source.getTaskContext()) {
 			targetOrocosModel.getTaskContext().add(cloneRttTaskContext(sourceTaskContext));
 		}
 
 		for (ConnectionPolicy sourceConnPolicy : source.getConnectionPolicy()) {
 			targetOrocosModel.getConnectionPolicy().add(cloneRttConnectionPolicy(sourceConnPolicy));
 		}
 
 		for (IActivity sourceActivity: source.getActivity()){
 			targetOrocosModel.getActivity().add(cloneRttIActivity(sourceActivity,null, source));
 		}
 
 	}
 
 	private TaskContext cloneRttTaskContext(TaskContext source){
 
 		TaskContext target = RttFactoryImpl.eINSTANCE.createTaskContext();
 
 		target.setName(source.getName());
 		target.setNamespace(source.getNamespace());
 		target.setType(source.getType());
 
 		for(InputPort sourceInputPort : source.getInputPort()){
 			target.getInputPort().add(cloneRttInputPort(sourceInputPort, target));
 		}
 
 		for(OutputPort sourceOutputPort : source.getOutputPort()){
 			target.getOutputPort().add(cloneRttOutputPort(sourceOutputPort, target));
 		}
 
 		for(Property sourceProperty : source.getProperty()){
 			target.getProperty().add(cloneRttProperty(sourceProperty));
 		}
 
 		for(Operation sourceOperation : source.getOperacion()){
 			target.getOperacion().add(cloneRttOperation(sourceOperation));
 		}
 
 		taskContextHashTable.put(source, target);
 
 		return target;
 
 	}
 
 	private InputPort cloneRttInputPort(InputPort source, TaskContext targetOwner){
 
 		InputPort target = RttFactoryImpl.eINSTANCE.createInputPort();
 
 		target.setName(source.getName());
 		target.setIsEventPort(source.getIsEventPort());
 		target.setType(source.getType());
 
 		inputPortHashTable.put(source, target);
 
 		return target;
 
 	}
 
 	private OutputPort cloneRttOutputPort(OutputPort source, TaskContext targetOwner){
 
 		OutputPort target = RttFactoryImpl.eINSTANCE.createOutputPort();
 
 		target.setName(source.getName());
 		target.setType(source.getType());
 
 		outputPortHashTable.put(source, target);
 
 		return target;
 
 	}
 
 	private Property cloneRttProperty(Property source){
 
 		Property target = RttFactoryImpl.eINSTANCE.createProperty();
 
 		target.setName(source.getName());
 		target.setDescription(source.getDescription());
 		target.setType(source.getType());
 		target.setValue(source.getValue());
 
 		propertyHashTable.put(source, target);
 
 		return target;
 
 	}
 
 	private Operation cloneRttOperation(Operation source){
 
 		Operation target = RttFactoryImpl.eINSTANCE.createOperation();
 
 		target.setName(source.getName());
 		target.setDocumentation(source.getDocumentation());
 		target.setReturnType(source.getReturnType());
 
 		return target;
 
 	}
 
 	private ConnectionPolicy cloneRttConnectionPolicy(ConnectionPolicy source){
 
 		ConnectionPolicy target = RttFactoryImpl.eINSTANCE.createConnectionPolicy();
 
 		target.setName(source.getName());
 		target.setLockPolicy(source.getLockPolicy());
 		target.setType(source.getType());
 		target.setBufferSize(source.getBufferSize());
 
 		InputPort sourceInputPort = source.getInputPort();
 		InputPort targetInputPort = inputPortHashTable.get(sourceInputPort);
 
 		targetInputPort.setInputConnectionPolicy(target);
 		target.setInputPort(targetInputPort);
 
 		OutputPort sourceOutputPort = source.getOutputPort();
 		OutputPort targetOutputPort = outputPortHashTable.get(sourceOutputPort);
 
 		targetOutputPort.setOutputConnectionPolicy(target);
 		target.setOutputPort(targetOutputPort);
 
 		connectionHashTable.put(source, target);
 
 		return target;
 
 	}
 
 	private IActivity cloneRttIActivity(IActivity source, Activity parent, Package rttPackage){
 
 		IActivity target; 
 
 		TaskContext sourceOwner = source.getTaskContext();
 
 
 		// the first call of this method always enter the else branch
 		// for the slave activities contained in the activity this method
 		// will enter the if branch
 		if(source instanceof Activity){
 			target = RttFactoryImpl.eINSTANCE.createActivity();
 			Activity sourceActivity = (Activity)source;
 			((Activity)target).setPeriod(sourceActivity.getPeriod());
 			((Activity)target).setCpuAffinity(sourceActivity.getCpuAffinity());
 			((Activity)target).setScheduler(sourceActivity.getScheduler());
 			((Activity)target).setPriority(sourceActivity.getPriority());
 
 			for(Slave sourceSlave : sourceActivity.getSlave()){
 				Slave targetSlave = (Slave)cloneRttIActivity(sourceSlave, (Activity)target, rttPackage);
 				((Activity)target).getSlave().add(targetSlave);
 				rttPackage.getActivity().add(targetSlave);
 			}
 
 		}else{
 			target = RttFactoryImpl.eINSTANCE.createSlave();
 		}
 
 		target.setName(source.getName());
 
 		target.setTaskContext(taskContextHashTable.get(sourceOwner));
 
 
 
 		return target;
 
 	}
 
 	//	private String printModel(Package model){
 	//
 	//		String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
 	//		result += "<rtt:Package xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" " +
 	//				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:rtt=\"http://rtt/1.0\" name=\"" +
 	//				model.getName() + "\">\n";
 	//
 	//		for(TaskContext tc : model.getTaskContext()){
 	//			result += printTaskContext(tc);
 	//		}
 	//
 	//		for(ConnectionPolicy cp : model.getConnectionPolicy()){
 	//			result += printConnectionPolicy(cp);
 	//		}
 	//
 	//		for(IActivity a : model.getActivity()){
 	//			result += printIActivity(a);
 	//		}
 	//
 	//		result += "</rtt:Package>\n";
 	//
 	//		return result;
 	//
 	//	}
 	//
 	//	private String printTaskContext(TaskContext tc){
 	//
 	//		String result = "\t" + "<taskContext name=\"" + tc.getName() + "\" namespace=\"" +
 	//				tc.getNamespace() + "\" type=\"" + tc.getType() + "\">\n";
 	//
 	//
 	//		for(InputPort ip : tc.getInputPort()){
 	//			result += printInputPort(ip);
 	//		}
 	//
 	//		for(OutputPort op : tc.getOutputPort()){
 	//			result += printOutputPort(op);
 	//		}
 	//
 	//		for(Property p : tc.getProperty()){
 	//			result += printProperty(p);
 	//		}
 	//
 	//		for(Operation o : tc.getOperacion()){
 	//			result += printOperation(o);
 	//		}
 	//
 	//		return result;
 	//	}
 	//
 	//	private String printInputPort(InputPort ip){
 	//
 	//		return "\t\t" + "<inputPort isEventPort=\"" + ip.getIsEventPort() + "\" name=\"" + ip.getName() +
 	//				"\" type=\"" + ip.getType().toString() + "\" inputConnectionPolicy=\"" + 
 	//				ip.getInputConnectionPolicy().getName() + "\"/>\n"; //@connectionPolicy.0"/>;
 	//
 	//	}
 	//
 	//	private String printOutputPort(OutputPort op){
 	//
 	//		return "\t\t" + "<oututPort name=\"" + op.getName() +	"\" type=\"" + op.getType().toString() + 
 	//				"\" inputConnectionPolicy=\"" +op.getOutputConnectionPolicy().getName() + "\"/>\n"; //@connectionPolicy.0"/>;
 	//
 	//	}
 	//
 	//	private String printProperty(Property p){
 	//		return "\t\t" + "<property name=\"" + p.getName() + "\" description=\"" + p.getDescription() + "\" type=\"" +
 	//				p.getType().toString() + "\" value=\"" + p.getValue() + "\"/>\n";
 	//	}
 	//
 	//	private String printOperation(Operation o){
 	//		return "";
 	//	}
 	//
 	//	private String printConnectionPolicy(ConnectionPolicy cp){
 	//		return "\t" + "<connectionPolicy inputPort=\"" + cp.getInputPort().getName() + //@taskContext.2/@inputPort.0
 	//				"\" outputPort=\"" + cp.getOutputPort().getName() + //@taskContext.0/@outputPort.0
 	//				"\" bufferSize=\"" + cp.getBufferSize() + "\" name=\"" + cp.getName() + 
 	//				"\" lockPolicy=\"" + cp.getLockPolicy().toString() + "\" type=\"" + cp.getType().toString() + "\"/>\n";
 	//	}
 	//
 	//	private String printIActivity(IActivity ia){
 	//		if(ia instanceof Activity){
 	//			Activity a = (Activity)ia;
 	//			return "\t" + "<activity xsi:type=\"rtt:Activity\" name=\"" + a.getName() + "\" taskContext=\"" +
 	//			a.getTaskContext().getName() + //@taskContext.0
 	//			"\" scheduler=\"" + a.getScheduler().toString() + "\" cpuAffinity=\"" + a.getCpuAffinity() + 
 	//			"\" period=\"" + a.getPeriod() + "\" priority=\"" + a.getPriority() + "\"/>\n";
 	//		}else 
 	//			return "";
 	//	}
 
 	private FeatureModel getSourceFeatureModel(){
 
 		Feature currentFeature;
 
 		for(RMResolutionElement currentResElem : resolutionModel.getResolutionElements()){
 
 			currentFeature = currentResElem.getFeature();
 
 			if(currentFeature != null){
 
 				return currentFeature.getFeatureModel();
 
 			}
 
 		}
 
 		return null;
 
 
 	}
 
 	private Package getSourceRttModel(){
 
 		Package rttPackage = null;
 
 		for(RMResolutionElement currentResElem : resolutionModel.getResolutionElements()){
 
 			for(RMAbstractTransformation currentTransf : currentResElem.getTransformations()){
 
 				if(currentTransf instanceof RTTTransfImplementation){
 
 					RTTTransfImplementation transfImpl = (RTTTransfImplementation) currentTransf;
 					return (Package)transfImpl.getTargetTaskContext().eContainer();
 
 				}else if(currentTransf instanceof RTTTransfProperty){
 
 					RTTTransfProperty transfProperty = (RTTTransfProperty) currentTransf;
 					TaskContext taskContext = (TaskContext)transfProperty.getTargetProperty().eContainer();
 					return (Package)taskContext.eContainer();
 
 				}else if(currentTransf instanceof RTTTransfConnection){
 
 					RTTTransfConnection transfConn = (RTTTransfConnection) currentTransf;
 					TaskContext taskContext = (TaskContext)transfConn.getNewConnections().get(0)
 							.getInputPort().eContainer();
 					return (Package)taskContext.eContainer();
 
 				}
 
 			}
 
 			if(currentResElem.getRequiredComponents() instanceof RTTRequiredComponents){
 
 				RTTRequiredComponents rttReqComponenents = (RTTRequiredComponents)currentResElem.getRequiredComponents();
 				for(TaskContext currentReqComp : rttReqComponenents.getRTTTaskContexts()){
 
 					return (Package)currentReqComp.eContainer();
 
 				}
 
 			}
 
 			if(currentResElem.getRequiredConnections() instanceof RTTRequiredConnections){
 
 				RTTRequiredConnections rttReqConnections = (RTTRequiredConnections)currentResElem.getRequiredConnections();
 				for(ConnectionPolicy currentReqConn : rttReqConnections.getRTTConnectionPolicies()){
 
 					return (Package)currentReqConn.eContainer();
 
 				}
 
 			}
 
 
 		}
 
 		return rttPackage;
 
 	}
 
 }
