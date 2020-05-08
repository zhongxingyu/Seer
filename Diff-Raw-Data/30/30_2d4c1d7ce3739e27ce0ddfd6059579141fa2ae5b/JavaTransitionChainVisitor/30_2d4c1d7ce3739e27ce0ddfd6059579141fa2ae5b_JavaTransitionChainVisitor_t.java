 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.java;
 
 import java.util.List;
 
 
 import org.eclipse.etrice.core.room.CPBranchTransition;
 import org.eclipse.etrice.core.room.ContinuationTransition;
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.core.room.TriggeredTransition;
 import org.eclipse.etrice.core.room.TypedID;
 import org.eclipse.etrice.generator.etricegen.ExpandedActorClass;
 import org.eclipse.etrice.generator.etricegen.ITransitionChainVisitor;
 import org.eclipse.etrice.generator.etricegen.TransitionChain;
 
 public class JavaTransitionChainVisitor implements ITransitionChainVisitor {
 
 	private ExpandedActorClass ac;
 	private String typedData = "";
 	private String dataArg = "";
 
 	JavaTransitionChainVisitor(ExpandedActorClass ac, TransitionChain tc) {
 		this.ac = ac;
 		
 		if (tc.getTransition() instanceof TriggeredTransition) {
 			// we rely on the previous checking during the generator model creation
 			List<TypedID> args = ((TriggeredTransition)tc.getTransition()).getTriggers().get(0).getMsgFromIfPairs().get(0).getMessage().getArguments();
 			
 			String[] result = JavaGenerator.getArglistAndTypedData(args);
 			dataArg = result[0];
 			typedData = result[1];
 		}
 	}
 
 	// ITransitionChainVisitor interface
 	
 	public String genActionOperationCall(Transition tr) {
 		if (tr.getAction()!=null && !tr.getAction().getCommands().isEmpty()) {
 			if (tr instanceof InitialTransition)
 				return JavaGenerator.getActionCodeOperationName(tr)+"();\n";
 			else
 				return JavaGenerator.getActionCodeOperationName(tr)+"(ifitem"+dataArg+");\n";
 		}
 		return "";
 	}
 
 	public String genEntryOperationCall(State state) {
 		return JavaGenerator.getEntryCodeOperationName(state)+"();\n";
 	}
 
 	public String genExitOperationCall(State state) {
 		return JavaGenerator.getExitCodeOperationName(state)+"();\n";
 	}
 
	public String genElseIfBranch(CPBranchTransition tr, boolean isFirst) {
 		String result = "";
 		
		if (!isFirst )
 			result = "}\nelse ";
 
 		result += "if ("+ac.getCode(tr.getCondition())+") {\n";
 		
 		return result;
 	}
 
 	public String genElseBranch(ContinuationTransition tr) {
 		String result = "}\nelse {\n";
 		return result;
 	}
 
 	public String genEndIf() {
 		return "}\n";
 	}
 
 	public String genReturnState(State state) {
 		return "return " + JavaGenerator.getStateId(state) + ";";
 	}
 
 	public String genTypedData() {
 		return typedData;
 	}
 
 }
