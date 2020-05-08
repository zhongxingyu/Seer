 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  * Tanja Mayerhofer - initial API and implementation
  */
 package org.modelexecution.xmof.vm;
 
 import java.io.File;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EParameter;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.BasicActionsFactory;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.CallBehaviorAction;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.CallOperationAction;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.InputPin;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.OutputPin;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.AddStructuralFeatureValueAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.IntermediateActionsFactory;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ReadSelfAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ReadStructuralFeatureAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ValueSpecificationAction;
 import org.modelexecution.xmof.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExpansionKind;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExpansionNode;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExpansionRegion;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExtraStructuredActivitiesFactory;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ControlFlow;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.DecisionNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ForkNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.InitialNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.IntermediateActivitiesFactory;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.MergeNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ObjectFlow;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.BehavioredEClass;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.BehavioredEOperation;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.DirectedParameter;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.KernelFactory;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.LiteralBoolean;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.LiteralInteger;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.MainEClass;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.ParameterDirectionKind;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.BasicBehaviors.BasicBehaviorsFactory;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.BasicBehaviors.Behavior;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;
 
 public class PetriNetFactory {
 
 	private final static EcoreFactory ECORE = EcoreFactory.eINSTANCE;
 	private final static KernelFactory KERNEL = KernelFactory.eINSTANCE;
 	private final static IntermediateActivitiesFactory INTERMED_ACTIVITIES = IntermediateActivitiesFactory.eINSTANCE;
 	private final static IntermediateActionsFactory INTERMED_ACTIONS = IntermediateActionsFactory.eINSTANCE;
 	private final static BasicActionsFactory BASIC_ACTIONS = BasicActionsFactory.eINSTANCE;
 	private final static BasicBehaviorsFactory BASIC_BEHAVIORS = BasicBehaviorsFactory.eINSTANCE;
 	private final static ExtraStructuredActivitiesFactory EXTRASTRUCT_ACTIVITIES = ExtraStructuredActivitiesFactory.eINSTANCE;
 	
 	private MainEClass mainEClass;
 	private EPackage rootPackage;
 	private BehavioredEClass transitionClass;
 	private BehavioredEClass placeClass;
 	
 	private BehavioredEOperation placeOperationAddToken;
 	private BehavioredEOperation placeOperationRemoveToken;
 	private BehavioredEOperation transitionOperationFire;
 	private BehavioredEOperation transitionOperationIsEnabled;
 	private BehavioredEOperation netOperationRun;
 	
 	private OpaqueBehavior addBehavior;
 	private OpaqueBehavior subtractBehavior;
 	private OpaqueBehavior greaterBehavior;
 	private OpaqueBehavior listgetBehavior;
 	private OpaqueBehavior listsizeBehavior;
 
 	public Resource createMetamodelResource() {
 		Resource resource = new ResourceSetImpl().createResource(URI
 				.createFileURI(new File("petrinet.xmof")
 						.getAbsolutePath()));
 		resource.getContents().add(createMetamodel());
 		return resource;
 	}
 
 	public EPackage createMetamodel() {
 		rootPackage = ECORE.createEPackage();
 		rootPackage.setName("PetriNetPackage");
 		rootPackage.setNsURI("http://www.modelexecution.org/petrinet");
 		rootPackage.setNsPrefix("sistusy");
 		createOpaqueBehaviors();
 		rootPackage.getEClassifiers().add(createPlaceClass());
 		rootPackage.getEClassifiers().add(createTransitionClass());		
 		rootPackage.getEClassifiers().add(createNetClass());
 		return rootPackage;
 	}
 
 	private void createOpaqueBehaviors() {
 		addBehavior = createAddBehavior();
 		subtractBehavior = createSubtractBehavior();
 		greaterBehavior = createGreaterBehavior();
 		listgetBehavior = createListgetBehavior();
 		listsizeBehavior = createListsizeBehavior();
 		rootPackage.getEClassifiers().add(addBehavior);
 		rootPackage.getEClassifiers().add(subtractBehavior);
 		rootPackage.getEClassifiers().add(greaterBehavior);
 		rootPackage.getEClassifiers().add(listgetBehavior);
 		rootPackage.getEClassifiers().add(listsizeBehavior);		
 	}
 
 	private MainEClass createNetClass() {
 		mainEClass = KERNEL.createMainEClass();
 		mainEClass.setName("Net");		
 		mainEClass.getEStructuralFeatures().add(createRefToPlaces());
 		mainEClass.getEStructuralFeatures().add(createRefToTransitions());
 		mainEClass.getEOperations().add(createNetRunOperation());
 		
 		Behavior runBehavior = createNetClassifierBehavior();
 		mainEClass.getOwnedBehavior().add(runBehavior);
 		mainEClass.setClassifierBehavior(runBehavior);		
 		return mainEClass;
 	}	
 
 	private EStructuralFeature createRefToPlaces() {
 		EReference placesReference = ECORE.createEReference();
 		placesReference.setName("places");
 		placesReference.setContainment(true);
 		placesReference.setEType(placeClass);
 		placesReference.setLowerBound(0);
 		placesReference.setUpperBound(-1);
 		return placesReference;
 	}
 	
 	private EStructuralFeature createRefToTransitions() {
 		EReference transitionsReference = ECORE.createEReference();
 		transitionsReference.setName("transitions");
 		transitionsReference.setContainment(true);
 		transitionsReference.setEType(transitionClass);
 		transitionsReference.setLowerBound(0);
 		transitionsReference.setUpperBound(-1);
 		return transitionsReference;
 	}
 
 	private EOperation createNetRunOperation() {
 		netOperationRun = KERNEL.createBehavioredEOperation();
 		netOperationRun.setName("run");
 		
 		Activity activity = INTERMED_ACTIVITIES.createActivity();
 		activity.setName("Net::run()");
 
 		InitialNode initial = createInitialNode(activity);
 		MergeNode merge = createMergeNode(activity, "merge");
 		ReadSelfAction readself = createReadSelfAction(activity, "read self net");
 		ReadStructuralFeatureAction readtransitions = createReadStructuralFeatureAction(activity, "read transitions", mainEClass.getEStructuralFeature("transitions"));		
 		ForkNode fork = createForkNode(activity, "fork");
 		CallOperationAction callisenabled = createCallOperationAction(activity, "call is enabled", transitionOperationIsEnabled);
 		DecisionNode enabledtransitiondecision = createDecisionNode(activity, "check if transition enabled");
 		EList<ActivityNode> regionnodes = new BasicEList<ActivityNode>();
 		regionnodes.add(fork);
 		regionnodes.add(callisenabled);
 		regionnodes.add(enabledtransitiondecision);
 		ExpansionRegion region = createExpansionRegion(activity, "select enabled transitions", ExpansionKind.PARALLEL, regionnodes, 1, 1);
 		ValueSpecificationAction specify1 = createValueSpecificationAction(activity, "specify 1", 1);		
 		CallBehaviorAction calllistget = createCallBehaviorAction(activity, "call list get", listgetBehavior);
 		CallOperationAction callfire = createCallOperationAction(activity, "call fire", transitionOperationFire);
 		
 		createControlFlow(activity, initial, merge);
 		createControlFlow(activity, merge, readself);
 		createObjectFlow(activity, readself.getResult(), readtransitions.getInput().get(0));
 		createObjectFlow(activity, readtransitions.getResult(), region.getInputElement().get(0));
 		createObjectFlow(region, region.getInputElement().get(0), fork);
 		createObjectFlow(region, fork, enabledtransitiondecision);
		createObjectFlow(region, fork, callisenabled.getInput().get(0));
 		createDecisionInputFlow(region, callisenabled.getResult().get(0), enabledtransitiondecision);
 		createObjectFlow(region, enabledtransitiondecision, region.getOutputElement().get(0), true);
 		createObjectFlow(activity, region.getOutputElement().get(0), calllistget.getInput().get(0));
 		createObjectFlow(activity, calllistget.getResult().get(0), callfire.getInput().get(0));
 		createControlFlow(activity, region, specify1);
 		createObjectFlow(activity, specify1.getResult(), calllistget.getArgument().get(1));
 		createControlFlow(activity, callfire, merge);
 		
 		netOperationRun.getMethod().add(activity);
 		mainEClass.getOwnedBehavior().add(activity);
 		
 		return netOperationRun;
 	}
 
 	private Behavior createNetClassifierBehavior() {
 		Activity activity = INTERMED_ACTIVITIES.createActivity();
 		activity.setName("Net::classifierBehavior");
 		
 		InitialNode initial = createInitialNode(activity);		
 		ReadSelfAction readself = createReadSelfAction(activity, "read self net");
 		ForkNode fork1 = createForkNode(activity, "fork1");
 		ReadStructuralFeatureAction readplaces = createReadStructuralFeatureAction(activity, "read places", mainEClass.getEStructuralFeature("places"));		
 		ForkNode fork2 = createForkNode(activity, "fork2");
 		ReadStructuralFeatureAction readinitialtokens = createReadStructuralFeatureAction(activity, "read initial tokens", placeClass.getEStructuralFeature("initialTokens"));
 		AddStructuralFeatureValueAction settokens = createAddStructuralFeatureValueAction(activity, "set tokens", placeClass.getEStructuralFeature("tokens"), true);
 		EList<ActivityNode> regionnodes = new BasicEList<ActivityNode>();
 		regionnodes.add(fork2);
 		regionnodes.add(readinitialtokens);
 		regionnodes.add(settokens);
 		ExpansionRegion region = createExpansionRegion(activity, "initialize tokens", ExpansionKind.PARALLEL, regionnodes, 1, 0);
 		CallOperationAction callrun = createCallOperationAction(activity, "call run", netOperationRun);
 		
 		createControlFlow(activity, initial, readself);
 		createObjectFlow(activity, readself.getResult(), fork1);
 		createObjectFlow(activity, fork1, readplaces.getInput().get(0));
 		createObjectFlow(activity, fork1, callrun.getTarget());
 		createObjectFlow(activity, readplaces.getResult(), region.getInputElement().get(0));
 		createObjectFlow(region, region.getInputElement().get(0), fork2);
 		createObjectFlow(region, fork2, readinitialtokens.getInput().get(0));
 		createObjectFlow(region, fork2, settokens.getObject());
 		createObjectFlow(region, readinitialtokens.getResult(), settokens.getValue());
 		createControlFlow(activity, region, callrun);
 		
 		return activity;
 	}
 
 	private BehavioredEClass createTransitionClass() {
 		transitionClass = KERNEL.createBehavioredEClass();
 		transitionClass.setName("Transition");
 		transitionClass.getEStructuralFeatures().add(createRefToInputPlaces());
 		transitionClass.getEStructuralFeatures().add(createRefToOutputPlaces());
 		transitionClass.getEOperations().add(createTransitionFireOperation());
 		transitionClass.getEOperations().add(createTransitionIsEnabledOperation());		
 		return transitionClass;
 	}
 	
 	private EStructuralFeature createRefToInputPlaces() {
 		EReference inputPlacesReference = ECORE.createEReference();
 		inputPlacesReference.setName("input");
 		inputPlacesReference.setContainment(false);
 		inputPlacesReference.setEType(placeClass);
 		inputPlacesReference.setLowerBound(0);
 		inputPlacesReference.setUpperBound(-1);
 		return inputPlacesReference;
 	}
 	
 	private EStructuralFeature createRefToOutputPlaces() {
 		EReference outputPlacesReference = ECORE.createEReference();
 		outputPlacesReference.setName("output");
 		outputPlacesReference.setContainment(false);
 		outputPlacesReference.setEType(placeClass);
 		outputPlacesReference.setLowerBound(0);
 		outputPlacesReference.setUpperBound(-1);
 		return outputPlacesReference;
 	}
 	
 	private BehavioredEOperation createTransitionFireOperation() {
 		transitionOperationFire = KERNEL.createBehavioredEOperation();
 		transitionOperationFire.setName("fire");
 		
 		Activity activity = INTERMED_ACTIVITIES.createActivity();
 		activity.setName("Transition::fire()");
 		
 		InitialNode initial = createInitialNode(activity);
 		ForkNode fork = createForkNode(activity, "fork");
 		ReadSelfAction readself = createReadSelfAction(activity, "read self transition");
 		ReadStructuralFeatureAction readinput = createReadStructuralFeatureAction(activity, "read input", transitionClass.getEStructuralFeature("input"));
 		ReadStructuralFeatureAction readoutput = createReadStructuralFeatureAction(activity, "read output", transitionClass.getEStructuralFeature("output"));		
 		CallOperationAction calladdtoken = createCallOperationAction(activity, "call add token", placeOperationAddToken);
 		EList<ActivityNode> expansionregionnodes = new BasicEList<ActivityNode>();
 		expansionregionnodes.add(calladdtoken);
 		ExpansionRegion changeoutput = createExpansionRegion(activity, "add tokens to output places", ExpansionKind.ITERATIVE, expansionregionnodes, 1, 0);				
 		CallOperationAction callremovetoken = createCallOperationAction(activity, "call remove token", placeOperationRemoveToken);
 		expansionregionnodes.clear();
 		expansionregionnodes.add(callremovetoken);
 		ExpansionRegion changeinput = createExpansionRegion(activity, "remove tokens from input places", ExpansionKind.ITERATIVE, expansionregionnodes, 1, 0);		
 		
 		createControlFlow(activity, initial, readself);
 		createObjectFlow(activity, readself.getResult(), fork);
 		createObjectFlow(activity, fork, readoutput.getInput().get(0));
 		createObjectFlow(activity, readoutput.getResult(), changeoutput.getInputElement().get(0));
 		createObjectFlow(changeoutput, changeoutput.getInputElement().get(0), calladdtoken.getInput().get(0));
 		createObjectFlow(activity, fork, readinput.getInput().get(0));
 		createObjectFlow(activity, readinput.getResult(), changeinput.getInputElement().get(0));
 		createObjectFlow(changeinput, changeinput.getInputElement().get(0), callremovetoken.getInput().get(0));
 				
 		transitionOperationFire.getMethod().add(activity);
 		transitionClass.getOwnedBehavior().add(activity);
 		
 		return transitionOperationFire;
 	}	
 
 	private BehavioredEOperation createTransitionIsEnabledOperation() {
 		transitionOperationIsEnabled = KERNEL.createBehavioredEOperation();
 		transitionOperationIsEnabled.setName("isEnabled");
 		transitionOperationIsEnabled.setEType(EcorePackage.eINSTANCE.getEBoolean());
 		
 		Activity activity = INTERMED_ACTIVITIES.createActivity();
 		activity.setName("Transition::isEnabled()");
 		
 		InitialNode initial = createInitialNode(activity);
 		ReadSelfAction readself = createReadSelfAction(activity, "read self transition");
 		ReadStructuralFeatureAction readinput = createReadStructuralFeatureAction(activity, "read input", transitionClass.getEStructuralFeature("input"));		
 		ForkNode fork = createForkNode(activity, "fork");
 		ReadStructuralFeatureAction readtokens = createReadStructuralFeatureAction(activity, "read tokens", placeClass.getEStructuralFeature("tokens"));
 		DecisionNode heldtokensdecision = createDecisionNode(activity, "check held tokens");
 		EList<ActivityNode> regionnodes = new BasicEList<ActivityNode>();
 		regionnodes.add(fork);
 		regionnodes.add(readtokens);
 		regionnodes.add(heldtokensdecision);
 		ExpansionRegion region = createExpansionRegion(activity, "select input places with tokens = 0", ExpansionKind.PARALLEL, regionnodes, 1, 1);
 		ValueSpecificationAction specify0 = createValueSpecificationAction(activity, "specify 0", 0);		
 		CallBehaviorAction calllistsize = createCallBehaviorAction(activity, "call list size", listsizeBehavior);		
 		DecisionNode enabledplacesdecision = createDecisionNode(activity, "check places without token", greaterBehavior);
 		ValueSpecificationAction specifytrue = createValueSpecificationAction(activity, "specify true", true);
 		ValueSpecificationAction specifyfalse = createValueSpecificationAction(activity, "specify true", false);
 		DirectedParameter isEnabledParam = createDirectedParameter("is enabled", ParameterDirectionKind.OUT);
 		transitionOperationIsEnabled.getEParameters().add(isEnabledParam);
 		ActivityParameterNode isEnabled = createActivityParameterNode(activity, "is enabled", isEnabledParam);
 		
 		createControlFlow(activity, initial, readself);
 		createObjectFlow(activity, readself.getResult(), readinput.getInput().get(0));
 		createObjectFlow(activity, readinput.getResult(), region.getInputElement().get(0));
 		createObjectFlow(region, region.getInputElement().get(0), fork);
 		createObjectFlow(region, fork, heldtokensdecision);
 		createObjectFlow(region, fork, readtokens.getInput().get(0));
 		createDecisionInputFlow(region, readtokens.getResult(), heldtokensdecision);
 		createObjectFlow(region, heldtokensdecision, region.getOutputElement().get(0), 0);
 		createObjectFlow(activity, region.getOutputElement().get(0), calllistsize.getInput().get(0));
 		createObjectFlow(activity, calllistsize.getResult().get(0), enabledplacesdecision);
 		createControlFlow(activity, region, specify0);
 		createDecisionInputFlow(activity, specify0.getResult(), enabledplacesdecision);
 		createControlFlow(activity, enabledplacesdecision, specifytrue, false);
 		createControlFlow(activity, enabledplacesdecision, specifyfalse, true);
 		createObjectFlow(activity, specifytrue.getResult(), isEnabled);
 		createObjectFlow(activity, specifyfalse.getResult(), isEnabled);
 		createControlFlow(activity, region, calllistsize);
 		
 		transitionOperationIsEnabled.getMethod().add(activity);
 		transitionClass.getOwnedBehavior().add(activity);
 		
 		return transitionOperationIsEnabled;
 	}
 	
 	private BehavioredEClass createPlaceClass() {
 		placeClass = KERNEL.createBehavioredEClass();
 		placeClass.setName("Place");
 		placeClass.getEStructuralFeatures().add(createInitialTokensAttribute());
 		placeClass.getEStructuralFeatures().add(createTokensAttribute());
 		placeClass.getEOperations().add(createPlaceAddTokenOperation());
 		placeClass.getEOperations().add(createPlaceRemoveTokenOperation());
 		return placeClass;
 	}
 
 	private EStructuralFeature createInitialTokensAttribute() {
 		EAttribute initialTokensAttribute = ECORE.createEAttribute();
 		initialTokensAttribute.setEType(EcorePackage.eINSTANCE.getEInt());
 		initialTokensAttribute.setName("initialTokens");
 		initialTokensAttribute.setLowerBound(1);
 		initialTokensAttribute.setUpperBound(1);
 		return initialTokensAttribute;
 	}
 	
 	private EStructuralFeature createTokensAttribute() {
 		EAttribute tokensAttribute = ECORE.createEAttribute();
 		tokensAttribute.setEType(EcorePackage.eINSTANCE.getEInt());
 		tokensAttribute.setName("tokens");
 		tokensAttribute.setLowerBound(1);
 		tokensAttribute.setUpperBound(1);
 		return tokensAttribute;
 	}		
 
 	private BehavioredEOperation createPlaceAddTokenOperation() {
 		placeOperationAddToken = KERNEL.createBehavioredEOperation();
 		placeOperationAddToken.setName("addToken");
 		
 		Activity activity = INTERMED_ACTIVITIES.createActivity();
 		activity.setName("Place::addToken()");
 		
 		InitialNode initial = createInitialNode(activity);
 		ForkNode fork1 = createForkNode(activity, "fork1");
 		ReadSelfAction readself = createReadSelfAction(activity, "read self place");
 		ValueSpecificationAction specify1 = createValueSpecificationAction(activity, "specify 1", 1);
 		ForkNode fork2 = createForkNode(activity, "fork2");
 		ReadStructuralFeatureAction readtokens = createReadStructuralFeatureAction(activity, "read tokens", placeClass.getEStructuralFeature("tokens"));		
 		CallBehaviorAction calladd = createCallBehaviorAction(activity, "call add", addBehavior);
 		AddStructuralFeatureValueAction settokens = createAddStructuralFeatureValueAction(activity, "set tokens", placeClass.getEStructuralFeature("tokens"), true);
 		
 		createControlFlow(activity, initial, fork1);
 		createControlFlow(activity, fork1, readself);
 		createControlFlow(activity, fork1, specify1);
 		createObjectFlow(activity, readself.getResult(), fork2);
 		createObjectFlow(activity, fork2, settokens.getObject());
 		createObjectFlow(activity, fork2, readtokens.getObject());
 		createObjectFlow(activity, specify1.getResult(), calladd.getArgument().get(1));
 		createObjectFlow(activity, readtokens.getResult(), calladd.getArgument().get(0));
 		createObjectFlow(activity, calladd.getResult().get(0), settokens.getValue());
 		
 		placeOperationAddToken.getMethod().add(activity);
 		placeClass.getOwnedBehavior().add(activity);
 		
 		return placeOperationAddToken;
 	}
 	
 	private BehavioredEOperation createPlaceRemoveTokenOperation() {
 		placeOperationRemoveToken = KERNEL.createBehavioredEOperation();
 		placeOperationRemoveToken.setName("removeToken");
 		
 		Activity activity = INTERMED_ACTIVITIES.createActivity();
 		activity.setName("Place::remove()");
 		
 		InitialNode initial = createInitialNode(activity);
 		ForkNode fork1 = createForkNode(activity, "fork1");
 		ReadSelfAction readself = createReadSelfAction(activity, "read self place");
 		ValueSpecificationAction specify1 = createValueSpecificationAction(activity, "specify 1", 1);
 		ForkNode fork2 = createForkNode(activity, "fork2");
 		ReadStructuralFeatureAction readtokens = createReadStructuralFeatureAction(activity, "read tokens", placeClass.getEStructuralFeature("tokens"));		
 		CallBehaviorAction callsubtract = createCallBehaviorAction(activity, "call subtract", subtractBehavior);
 		AddStructuralFeatureValueAction settokens = createAddStructuralFeatureValueAction(activity, "set tokens", placeClass.getEStructuralFeature("tokens"), true);
 		
 		createControlFlow(activity, initial, fork1);
 		createControlFlow(activity, fork1, readself);
 		createControlFlow(activity, fork1, specify1);
 		createObjectFlow(activity, readself.getResult(), fork2);
 		createObjectFlow(activity, fork2, settokens.getObject());
 		createObjectFlow(activity, fork2, readtokens.getObject());
 		createObjectFlow(activity, specify1.getResult(), callsubtract.getInput().get(1));
 		createObjectFlow(activity, readtokens.getResult(), callsubtract.getInput().get(0));
 		createObjectFlow(activity, callsubtract.getResult().get(0), settokens.getValue());
 		
 		placeOperationRemoveToken.getMethod().add(activity);
 		placeClass.getOwnedBehavior().add(activity);
 		
 		return placeOperationRemoveToken;
 	}
 	
 	private OpaqueBehavior createListsizeBehavior() {
 		OpaqueBehavior behavior = BASIC_BEHAVIORS.createOpaqueBehavior();		
 		behavior.setName("listsize");
 		
 		DirectedParameter inparam = createDirectedParameter("list", ParameterDirectionKind.IN);
 		inparam.setLowerBound(0);
 		inparam.setUpperBound(-1);
 		behavior.getOwnedParameter().add(inparam);
 		
 		DirectedParameter outparam = createDirectedParameter("result", ParameterDirectionKind.OUT);
 		outparam.setLowerBound(1);
 		outparam.setUpperBound(1);
 		behavior.getOwnedParameter().add(outparam);
 		
 		return behavior;				
 	}
 
 	private OpaqueBehavior createListgetBehavior() {
 		OpaqueBehavior behavior = BASIC_BEHAVIORS.createOpaqueBehavior();		
 		behavior.setName("listget");
 		
 		DirectedParameter list = createDirectedParameter("list", ParameterDirectionKind.IN);
 		list.setLowerBound(1);
 		list.setUpperBound(-1);
 		behavior.getOwnedParameter().add(list);
 		
 		DirectedParameter index = createDirectedParameter("index", ParameterDirectionKind.IN);
 		index.setLowerBound(1);
 		index.setUpperBound(1);
 		behavior.getOwnedParameter().add(index);
 		
 		DirectedParameter outparam = createDirectedParameter("result", ParameterDirectionKind.OUT);
 		outparam.setLowerBound(0);
 		outparam.setUpperBound(1);
 		behavior.getOwnedParameter().add(outparam);
 		
 		return behavior;
 	}
 
 	private OpaqueBehavior createGreaterBehavior() {
 		OpaqueBehavior behavior = BASIC_BEHAVIORS.createOpaqueBehavior();		
 		behavior.setName("greater");
 		
 		DirectedParameter inparam1 = createDirectedParameter("x", ParameterDirectionKind.IN);
 		inparam1.setLowerBound(1);
 		inparam1.setUpperBound(1);
 		behavior.getOwnedParameter().add(inparam1);
 		
 		DirectedParameter inparam2 = createDirectedParameter("y", ParameterDirectionKind.IN);
 		inparam2.setLowerBound(1);
 		inparam2.setUpperBound(1);
 		behavior.getOwnedParameter().add(inparam2);
 		
 		DirectedParameter outparam = createDirectedParameter("result", ParameterDirectionKind.OUT);
 		outparam.setLowerBound(1);
 		outparam.setUpperBound(1);
 		behavior.getOwnedParameter().add(outparam);
 		
 		return behavior;
 	}
 
 	private OpaqueBehavior createSubtractBehavior() {
 		OpaqueBehavior behavior = BASIC_BEHAVIORS.createOpaqueBehavior();		
 		behavior.setName("subtract");
 		
 		DirectedParameter inparam1 = createDirectedParameter("x", ParameterDirectionKind.IN);
 		inparam1.setLowerBound(1);
 		inparam1.setUpperBound(1);
 		behavior.getOwnedParameter().add(inparam1);
 		
 		DirectedParameter inparam2 = createDirectedParameter("y", ParameterDirectionKind.IN);
 		inparam2.setLowerBound(1);
 		inparam2.setUpperBound(1);
 		behavior.getOwnedParameter().add(inparam2);
 		
 		DirectedParameter outparam = createDirectedParameter("result", ParameterDirectionKind.OUT);
 		outparam.setLowerBound(1);
 		outparam.setUpperBound(1);
 		behavior.getOwnedParameter().add(outparam);
 		
 		return behavior;
 	}
 
 	private OpaqueBehavior createAddBehavior() {
 		OpaqueBehavior behavior = BASIC_BEHAVIORS.createOpaqueBehavior();		
 		behavior.setName("add");
 		
 		DirectedParameter inparam1 = createDirectedParameter("x", ParameterDirectionKind.IN);
 		inparam1.setLowerBound(1);
 		inparam1.setUpperBound(1);
 		behavior.getOwnedParameter().add(inparam1);
 		
 		DirectedParameter inparam2 = createDirectedParameter("y", ParameterDirectionKind.IN);
 		inparam2.setLowerBound(1);
 		inparam2.setUpperBound(1);
 		behavior.getOwnedParameter().add(inparam2);
 		
 		DirectedParameter outparam = createDirectedParameter("result", ParameterDirectionKind.OUT);
 		outparam.setLowerBound(1);
 		outparam.setUpperBound(1);
 		behavior.getOwnedParameter().add(outparam);
 		
 		return behavior;
 	}
 	
 	private InitialNode createInitialNode(Activity activity) {
 		InitialNode initialNode = INTERMED_ACTIVITIES.createInitialNode();
 		initialNode.setName("initial");
 		activity.getNode().add(initialNode);
 		return initialNode;
 	}
 	
 	private ForkNode createForkNode(Activity activity, String name) {
 		ForkNode forknode = INTERMED_ACTIVITIES.createForkNode();	
 		forknode.setName(name);
 		forknode.setActivity(activity);
 		activity.getNode().add(forknode);
 		return forknode;
 	}	
 
 	private ReadSelfAction createReadSelfAction(Activity activity, String name) {
 		ReadSelfAction action = INTERMED_ACTIONS.createReadSelfAction();
 		action.setName(name);
 		
 		action.setActivity(activity);
 		activity.getNode().add(action);
 		return action;
 	}
 	
 	private ValueSpecificationAction createValueSpecificationAction(Activity activity, String name) {
 		ValueSpecificationAction action = INTERMED_ACTIONS.createValueSpecificationAction();
 		action.setName(name);
 		
 		action.setActivity(activity);
 		activity.getNode().add(action);
 		return action;
 	}
 	
 	private ValueSpecificationAction createValueSpecificationAction(Activity activity, String name, int value) {
 		ValueSpecificationAction action = createValueSpecificationAction(activity, name);		
 		LiteralInteger valueliteral = KERNEL.createLiteralInteger();
 		valueliteral.setValue(value);
 		action.setValue(valueliteral);		
 		return action;
 	}
 	
 	private ValueSpecificationAction createValueSpecificationAction(Activity activity, String name, boolean value) {
 		ValueSpecificationAction action = createValueSpecificationAction(activity, name);		
 		LiteralBoolean valueliteral = KERNEL.createLiteralBoolean();
 		valueliteral.setValue(value);
 		action.setValue(valueliteral);		
 		return action;
 	}
 	
 	private ReadStructuralFeatureAction createReadStructuralFeatureAction(Activity activity, String name, EStructuralFeature feature) {
 		ReadStructuralFeatureAction action = INTERMED_ACTIONS.createReadStructuralFeatureAction();
 		action.setName(name);
 			
 		action.setStructuralFeature(feature);
 		
 		action.setActivity(activity);
 		activity.getNode().add(action);
 		
 		return action;
 	}
 	
 	private AddStructuralFeatureValueAction createAddStructuralFeatureValueAction(Activity activity, String name, EStructuralFeature feature, boolean isReplace) {
 		AddStructuralFeatureValueAction action = INTERMED_ACTIONS.createAddStructuralFeatureValueAction();
 		action.setName(name);
 		
 		action.setStructuralFeature(feature);
 		
 		action.setReplaceAll(isReplace);
 		
 		action.setActivity(activity);
 		activity.getNode().add(action);		
 		return action;
 	}		
 	
 	private CallBehaviorAction createCallBehaviorAction(Activity activity, String name, Behavior behavior) {
 		CallBehaviorAction action = BASIC_ACTIONS.createCallBehaviorAction();
 		action.setName(name);
 		
 		action.setBehavior(behavior);				
 		
 		int amountinputpins = 0;
 		int amountoutputpins = 0;
 		for(DirectedParameter param : behavior.getOwnedParameter()) {
 			if(param.getDirection() == ParameterDirectionKind.IN || param.getDirection() == ParameterDirectionKind.INOUT) {
 				InputPin pin = BASIC_ACTIONS.createInputPin();
 				pin.setName("InputPin " + (++amountinputpins) + " (" + name + ")");
 				pin.setLowerBound(param.getLowerBound());
 				pin.setUpperBound(param.getUpperBound());
 				action.getArgument().add(pin);
 			}
 			if(param.getDirection() == ParameterDirectionKind.OUT || param.getDirection() == ParameterDirectionKind.INOUT || param.getDirection() == ParameterDirectionKind.RETURN) {
 				OutputPin pin = BASIC_ACTIONS.createOutputPin();
 				pin.setName("OutputPin " + (++amountoutputpins) + " (" + name + ")");
 				action.getResult().add(pin);
 			}
 		}						
 		action.setActivity(activity);
 		activity.getNode().add(action);
 		return action;
 	}		
 	
 	private CallOperationAction createCallOperationAction(Activity activity, String name, BehavioredEOperation operation) {
 		CallOperationAction action = BASIC_ACTIONS.createCallOperationAction();
 		action.setName(name);
 	
 		action.setOperation(operation);
 		
 		InputPin targetpin = BASIC_ACTIONS.createInputPin();
 		targetpin.setName("InputPin " + " target (" + name + ")");
 		action.setTarget(targetpin);
 		
 		for(EParameter param : operation.getEParameters()) {
 			InputPin inputpin = BASIC_ACTIONS.createInputPin();
 			inputpin.setName("InputPin " + param.getName() + " (" + name + " )");
 			action.getArgument().add(inputpin);
 		}
 		
 		if(operation.getEType() != null) {
 			OutputPin outputpin = BASIC_ACTIONS.createOutputPin();
 			outputpin.setName("OutputPin return (" + name + ")");
 			action.getResult().add(outputpin);
 		}
 		
 		action.setActivity(activity);
 		activity.getNode().add(action);
 		return action;
 	}
 
 	private DirectedParameter createDirectedParameter(String name, ParameterDirectionKind direction) {
 		DirectedParameter param = KERNEL.createDirectedParameter();
 		param.setName(name);
 		param.setDirection(direction);		
 		return param;
 	}
 	
 	private ExpansionRegion createExpansionRegion(Activity activity, String name, ExpansionKind mode, EList<ActivityNode> nodes, int inexpansionnodes, int outexpansionnodes) {
 		ExpansionRegion region = EXTRASTRUCT_ACTIVITIES.createExpansionRegion();
 		region.setName(name);		
 		region.setMode(mode);
 		
 		region.getNode().addAll(nodes);
 		
 		for(int i=0;i<(inexpansionnodes + outexpansionnodes);++i) {
 			ExpansionNode expnode = EXTRASTRUCT_ACTIVITIES.createExpansionNode();			
 			expnode.setLowerBound(1);
 			expnode.setUpperBound(-1);
 			
 			if(i<inexpansionnodes) {
 				expnode.setName("ExpansionNode input " + (i+1) + " (" + name + ")");
 				region.getInputElement().add(expnode);
 				expnode.setRegionAsInput(region);
 			} else {
 				expnode.setName("ExpansionNode output " + (i-inexpansionnodes+1) + " (" + name + ")");
 				region.getOutputElement().add(expnode);
 				expnode.setRegionAsOutput(region);
 			}			
 			expnode.setActivity(activity);
 			activity.getNode().add(expnode);
 		}
 		region.setActivity(activity);
 		activity.getNode().add(region);
 		return region;
 	}
 	
 	private DecisionNode createDecisionNode(Activity activity, String name) {
 		DecisionNode decisionnode = INTERMED_ACTIVITIES.createDecisionNode();		
 		decisionnode.setName(name);
 		decisionnode.setActivity(activity);
 		activity.getNode().add(decisionnode);						
 		return decisionnode;
 	}	
 	
 	private DecisionNode createDecisionNode(Activity activity, String name, Behavior decisionBehavior) {
 		DecisionNode decisionnode = createDecisionNode(activity, name);		
 		decisionnode.setDecisionInput(decisionBehavior);
 		return decisionnode;
 	}
 	
 	private MergeNode createMergeNode(Activity activity, String name) {
 		MergeNode mergenode = INTERMED_ACTIVITIES.createMergeNode();	
 		mergenode.setName(name);
 		mergenode.setActivity(activity);
 		activity.getNode().add(mergenode);
 		return mergenode;
 	}	
 	
 	private ActivityParameterNode createActivityParameterNode(Activity activity, String name, DirectedParameter parameter) {
 		ActivityParameterNode paramnode = INTERMED_ACTIVITIES.createActivityParameterNode();
 		paramnode.setName(name + " (activity=" + activity.getName() + " parameter=" + parameter.getName() + ")");
 		paramnode.setActivity(activity);
 		paramnode.setParameter(parameter);
 		activity.getNode().add(paramnode);
 		return paramnode;
 	}
 	
 	private ControlFlow createControlFlow(Activity activity,
 			ActivityNode source, ActivityNode target) {
 		ControlFlow cflow = INTERMED_ACTIVITIES.createControlFlow();
 		cflow.setName("ControlFlow " + source.getName() + " --> "
 				+ target.getName());
 		cflow.setSource(source);
 		cflow.setTarget(target);
 		source.getOutgoing().add(cflow);
 		target.getIncoming().add(cflow);
 		cflow.setActivity(activity);
 		activity.getEdge().add(cflow);
 		return cflow;
 	}
 	
 	private ControlFlow createControlFlow(Activity activity,
 			ActivityNode source, ActivityNode target, boolean guard) {
 		ControlFlow cflow = createControlFlow(activity, source, target);
 		LiteralBoolean guardliteral = KERNEL.createLiteralBoolean();
 		guardliteral.setValue(guard);
 		cflow.setGuard(guardliteral);
 		return cflow;
 	}
 	
 	private ObjectFlow createObjectFlow(Activity activity, ActivityNode source, ActivityNode target) {
 		ObjectFlow oflow = INTERMED_ACTIVITIES.createObjectFlow();
 		oflow.setName("ObjectFlow " + source.getName() + " --> " + target.getName());
 		oflow.setSource(source);
 		oflow.setTarget(target);
 		
 		source.getOutgoing().add(oflow);
 		target.getIncoming().add(oflow);
 				
 		oflow.setActivity(activity);
 		activity.getEdge().add(oflow);
 		
 		return oflow;
 	}
 	
 	private ObjectFlow createObjectFlow(StructuredActivityNode node, ActivityNode source, ActivityNode target) {
 		ObjectFlow oflow = createObjectFlow(node.getActivity(), source, target);
 		//source.setInStructuredNode(node);
 		//target.setInStructuredNode(node);
 		node.getEdge().add(oflow);		
 
 		return oflow;
 	}
 	
 	private ObjectFlow createObjectFlow(StructuredActivityNode node, ActivityNode source, ActivityNode target, boolean guard) {
 		ObjectFlow oflow = createObjectFlow(node, source, target);
 		LiteralBoolean guardliteral = KERNEL.createLiteralBoolean();
 		guardliteral.setValue(guard);
 		oflow.setGuard(guardliteral);
 		return oflow;
 	}
 	
 	private ObjectFlow createObjectFlow(StructuredActivityNode node, ActivityNode source, ActivityNode target, int guard) {
 		ObjectFlow oflow = createObjectFlow(node, source, target);
 		LiteralInteger guardliteral = KERNEL.createLiteralInteger();
 		guardliteral.setValue(guard);
 		oflow.setGuard(guardliteral);
 		return oflow;
 	}
 	
 	private ObjectFlow createDecisionInputFlow(Activity activity, OutputPin source, DecisionNode target) {
 		ObjectFlow oflow = createObjectFlow(activity, source, target);
 		target.setDecisionInputFlow(oflow);
 		return oflow;
 	}	
 	
 	private ObjectFlow createDecisionInputFlow(StructuredActivityNode node, OutputPin source, DecisionNode target) {
 		ObjectFlow oflow = createObjectFlow(node, source, target);
 		target.setDecisionInputFlow(oflow);
 		return oflow;
 	}
 	
 	public Resource createModelResource() {
 		Resource resource = new ResourceSetImpl().createResource(URI
 				.createFileURI(new File("petrinet1.xmi")
 						.getAbsolutePath()));
 		EFactory factory = rootPackage.getEFactoryInstance();
 
 		EObject net = factory.create(mainEClass);				
 		
 		EObject inputplace = factory.create(placeClass);
 		inputplace.eSet(placeClass.getEStructuralFeature("initialTokens"), 1);
 		EObject outputplace = factory.create(placeClass);
 		outputplace.eSet(placeClass.getEStructuralFeature("initialTokens"), 0);		
 		EList<EObject> placelist = new BasicEList<EObject>();
 		placelist.add(inputplace);
 		placelist.add(outputplace);
 		net.eSet(mainEClass.getEStructuralFeature("places"), placelist);
 
 		EObject transition = factory.create(transitionClass);	
 		EList<EObject> inputplacelist = new BasicEList<EObject>();
 		inputplacelist.add(inputplace);
 		transition.eSet(transitionClass.getEStructuralFeature("input"), inputplacelist);
 		EList<EObject> outputplacelist = new BasicEList<EObject>();
 		outputplacelist.add(outputplace);
 		transition.eSet(transitionClass.getEStructuralFeature("output"), outputplacelist);		
 		
 		EList<EObject> transitionlist = new BasicEList<EObject>();
 		transitionlist.add(transition);
 		net.eSet(mainEClass.getEStructuralFeature("transitions"), transitionlist);
 		
 		resource.getContents().add(net);
 		return resource;
 	}
 
 }
