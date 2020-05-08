 /**
  * Copyright (C) 2012 Schneider Electric
  *
  * This file is part of "Mind Compiler" is free software: you can redistribute 
  * it and/or modify it under the terms of the GNU Lesser General Public License 
  * as published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contact: mind@ow2.org
  *
  * Authors: Julien TOUS
  * Contributors:
  */
 
 package org.ow2.mind.adl.annotations;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.Map;
 
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Definition;
 import org.objectweb.fractal.adl.Node;
 import org.objectweb.fractal.adl.NodeFactory;
 import org.objectweb.fractal.adl.merger.NodeMerger;
 import org.ow2.mind.adl.annotation.ADLLoaderPhase;
 import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
 import org.ow2.mind.adl.annotation.predefined.Singleton;
 import org.ow2.mind.adl.ast.ASTHelper;
 import org.ow2.mind.adl.ast.Binding;
 import org.ow2.mind.adl.ast.BindingContainer;
 import org.ow2.mind.adl.ast.Component;
 import org.ow2.mind.adl.ast.ComponentContainer;
 import org.ow2.mind.adl.ast.DefinitionReference;
 import org.ow2.mind.annotation.Annotation;
 import org.ow2.mind.annotation.AnnotationHelper;
 import org.ow2.mind.adl.parameter.ast.Argument;
 import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
 import org.ow2.mind.adl.parameter.ast.FormalParameter;
 import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
 import org.ow2.mind.value.ast.Reference;
 import org.ow2.mind.value.ast.Value;
 
 import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.turnsToArgumentContainer;
 import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.turnsToParamContainer;
 import com.google.inject.Inject;
 
 /**
  * @author Julien TOUS
  */
 public class GarbageCompositeAnnotationProcessor extends
 AbstractADLLoaderAnnotationProcessor {
 	  @Inject
 	  protected NodeFactory                 nodeFactoryItf;
 
 	  @Inject
 	  protected NodeMerger                  nodeMergerItf;
 	  
 	private boolean garbage(final Annotation annotation,
 			final Node node, final Definition composite,
 			final ADLLoaderPhase phase, final Map<Object, Object> context) {
 		boolean finishedGarbaging = false;
 //		DumpDotAnnotationProcessor plop = new DumpDotAnnotationProcessor();
 
 		while(!finishedGarbaging) {
 			finishedGarbaging = true;
 			Component[] subComponents = ((ComponentContainer) composite).getComponents();
 			for (Component subComponent : subComponents) {	
 				Definition subCompDef;
 				DefinitionReference subCompDefRef;
 				subCompDefRef = subComponent.getDefinitionReference();
 				if (subCompDefRef != null) { 
 					//if null we are probably dealing with a template on the after check phase.
 					//Annotation will be re-processed on the template instantiation phase 
 					try {
 						subCompDef = ASTHelper.getResolvedDefinition(subCompDefRef, null, null);
 						if (ASTHelper.isComposite(subCompDef))
 						{
 							finishedGarbaging = false;
 							removeComposite( composite, subComponent);
 							//						if (phase == ADLLoaderPhase.AFTER_TEMPLATE_INSTANTIATE ) {
 							//						plop.processAnnotation( new DumpDot(),
 							//								node,  composite,
 							//								phase,  context);
 							//						}
 							break;
 						}
 					} catch (ADLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else {
 					try {
 						if (AnnotationHelper.getAnnotation(composite, GarbageComposite.class) == null)
 							AnnotationHelper.addAnnotation(composite, new GarbageComposite());
 					} catch (ADLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}	
 				}
 			}
 		}
 		return finishedGarbaging; 
 	}
 
 	private void removeComposite(Definition upperComposite, Component composite) throws ADLException{
 		DefinitionReference compDefRef = composite.getDefinitionReference();
 		Definition compDef = ASTHelper.getResolvedDefinition(compDefRef, null, null);
 		Binding[] upperBindings = ((BindingContainer) upperComposite).getBindings();
         
 		FormalParameter[] compParameters = turnsToParamContainer(compDef, nodeFactoryItf, nodeMergerItf).getFormalParameters();
 		Argument[] compArguments = turnsToArgumentContainer(compDefRef, nodeFactoryItf, nodeMergerItf).getArguments();
 		
 		String compName = composite.getName();
 		final Component[] subComponents = ((ComponentContainer) compDef).getComponents();
 		for (Component subComponent : subComponents) {
 			DefinitionReference subCompDefRef = subComponent.getDefinitionReference();
 			Definition subCompDef = ASTHelper.getResolvedDefinition(subCompDefRef, null, null);
 			FormalParameter[] subCompParameters = turnsToParamContainer(subCompDef, nodeFactoryItf, nodeMergerItf).getFormalParameters();
 			Argument[] subCompArguments = turnsToArgumentContainer(subCompDefRef, nodeFactoryItf, nodeMergerItf).getArguments();
 	            
 			for (Argument compArgument : compArguments) {
 				for (Argument subCompArgument : subCompArguments) {
 					Value v = subCompArgument.getValue();
 					String vv;
 					if (v instanceof Reference)
 						vv = ((Reference) v ).getRef();
 					else
 						vv = ((Value) v).toString(); 
 					if (compArgument.getName().equals(vv)) {
 						subCompArgument.setValue(compArgument.getValue());
 						System.out.println("");
 					}
 				}
 			}
 			
 			String subSubName = compName + "_" + subComponent.getName();
 			subComponent.setName(subSubName);
 			((ComponentContainer) upperComposite).addComponent(subComponent);
 			((ComponentContainer) compDef).removeComponent(subComponent);
 		}
 		final Binding[] subBindings = ((BindingContainer) compDef)
 				.getBindings();
 		for (Binding subBinding : subBindings) {
 			String subFromComp = subBinding.getFromComponent();
 			String subToComp = subBinding.getToComponent();
 			if (subFromComp.equals(Binding.THIS_COMPONENT)) {
 				String thisItf = subBinding.getFromInterface();
 				for (Binding binding : upperBindings) {
 					if ((binding.getToComponent().equals(composite.getName())) && (binding.getToInterface().equals(thisItf))) {
 						binding.setToComponent(compName + "_" + subToComp);
 						binding.setToInterface(subBinding.getToInterface());
						binding.setToInterfaceNumber(subBinding.getToInterfaceNumber());
 						((BindingContainer) compDef).removeBinding(subBinding);
 					}
 				}
			} else if (subToComp.equals(Binding.THIS_COMPONENT)) {
 				//Treatement in the next for loop
 			} else {
 				((BindingContainer) upperComposite).addBinding(subBinding);
 				((BindingContainer) compDef).removeBinding(subBinding);
 				subBinding.setFromComponent(compName + "_" + subFromComp );
 				subBinding.setToComponent(compName + "_" + subToComp);
 			}
 		}
 		for (Binding binding : upperBindings) {
 			String fromComp = binding.getFromComponent();
 			String toComp = binding.getToComponent();
 			if (fromComp.equals(compName)) {
 				String thisItf = binding.getFromInterface();
 				for (Binding subBinding : subBindings) {
 					if ((subBinding.getToComponent().equals(Binding.THIS_COMPONENT)) && (subBinding.getToInterface().equals(thisItf))) {
 						subBinding.setFromComponent(compName + "_" + subBinding.getFromComponent());
 						subBinding.setToComponent(toComp);
 						subBinding.setToInterface(binding.getToInterface());
						subBinding.setToInterfaceNumber(binding.getToInterfaceNumber());
 						((BindingContainer) compDef).removeBinding(subBinding);
 						((BindingContainer) upperComposite).addBinding(subBinding);
 					}
 				}
 				((BindingContainer) upperComposite).removeBinding(binding);	
 			}
 		}
 		((ComponentContainer) upperComposite).removeComponent(composite);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor#processAnnotation
 	 * (org.ow2.mind.annotation.Annotation, org.objectweb.fractal.adl.Node,
 	 * org.objectweb.fractal.adl.Definition,
 	 * org.ow2.mind.adl.annotation.ADLLoaderPhase, java.util.Map)
 	 */
 	public Definition processAnnotation(final Annotation annotation,
 			final Node node, final Definition definition,
 			final ADLLoaderPhase phase, final Map<Object, Object> context)
 					throws ADLException {
 		assert annotation instanceof GarbageComposite;
 
 		if (ASTHelper.isComposite(definition)) {
 			garbage(annotation,
 					 node,  definition,
 					 phase,  context);
 		}
 		return null;
 	}
 
 }
