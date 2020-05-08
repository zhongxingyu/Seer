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
 
 import org.eclipse.etrice.core.naming.RoomNameProvider;
 
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.Message;
 import org.eclipse.etrice.core.room.MessageFromIf;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraphNode;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.core.room.Trigger;
 import org.eclipse.etrice.core.room.TriggeredTransition;
 import org.eclipse.etrice.core.room.Type;
 import org.eclipse.etrice.core.room.TypedID;
 import org.eclipse.etrice.generator.etricegen.ExpandedActorClass;
 import org.eclipse.etrice.generator.etricegen.TransitionChain;
 
 public class JavaGenerator {
 
 	// public static interface used by Xtend
 	
 	public static String getActionCodeOperationName(Transition t) {
 		return "action_"+RoomNameProvider.getFullPath(t);
 	}
 	
 	public static String getEntryCodeOperationName(State s) {
 		return "entry_"+getStatePathName(s);
 	}
 	
 	public static String getExitCodeOperationName(State s) {
 		return "exit_"+getStatePathName(s);
 	}
 	
 	public static String getChainId(TransitionChain tc) {
 		return "CHAIN_"+RoomNameProvider.getFullPath(tc.getTransition());
 	}
 	
 	public static boolean isTopLevel(StateGraphNode s) {
 		return !(s.eContainer().eContainer() instanceof State);
 	}
 	
 	public static State getParentState(StateGraphNode s) {
 		if (isTopLevel(s))
 			return null;
 		else
 			return (State) s.eContainer().eContainer();
 	}
 	
 	public static String getStatePathName(State s) {
 		return RoomNameProvider.getFullPath(s);
 	}
 	
 	public static String getStateId(State s) {
 		if (s==null)
 			return "STATE_"+RoomNameProvider.getStateName(s);
 		else
 			return "STATE_"+RoomNameProvider.getFullPath(s);
 	}
 	
 	public static String getParentStateId(State s) {
 		return getStateId(getParentState(s));
 	}
 
 	public static String getExecuteChain(ExpandedActorClass ac, TransitionChain tc) {
 		JavaTransitionChainVisitor tcv = new JavaTransitionChainVisitor(ac, tc);
 		return tc.genExecuteChain(tcv);
 	}
 
 	public static String getArgumentList(ExpandedActorClass xpac, Transition t) {
 		if (t instanceof InitialTransition)
 			// actually is InitialTransition
 			return "";
 		
 		TransitionChain chain = xpac.getChain(t);
 		if (!(chain.getTransition() instanceof TriggeredTransition))
 			return "";
 		
 		Trigger trigger = ((TriggeredTransition)chain.getTransition()).getTriggers().get(0);
 		MessageFromIf mif = trigger.getMsgFromIfPairs().get(0);
 		
 		return getTypedArgumentList(mif.getMessage());
 	}
 
 	public static String getArgumentList(Message m) {
 		return getArglistAndTypedData(m.getArguments())[0];
 	}
 
 	public static String getTypedData(Message m) {
 		return getArglistAndTypedData(m.getArguments())[1];
 	}
 
 	public static String getTypedArgumentList(Message m) {
 		return getArglistAndTypedData(m.getArguments())[2];
 	}
 	
 	public static String[] getArglistAndTypedData(List<TypedID> args) {
 		String typedData = "";
 		String dataArg = "";
 		String typedArgList = "";
 		
 		int i = 0;
 		for (TypedID tid : args) {
 			String t;
 			String ct;
 			Type type = tid.getType();
			if (type.getType()==null) {
 				switch (type.getPrim()) {
 				case BOOLEAN: t = "boolean"; ct = "Boolean"; break;
 				case CHAR: t = "char"; ct = "Char"; break;
 				case FLOAT32: t = "float"; ct = "Float"; break;
 				case FLOAT64: t = "double"; ct = "Double"; break;
 				case INT32: t = "int"; ct = "Integer"; break;
 				default: t = "invalid Java data type"; ct = ""; assert(false): "Java doesn't support type "+type.getPrim().name()+"!";
 				}
 			}
 			else {
 				t = type.getType().getName();
 				ct = t;
 			}
 			typedData += t+" "+tid.getName() + " = ("+ct+") generic_data["+i+"];\n";
 			dataArg += ", "+tid.getName();
 			typedArgList += ", "+t+" "+tid.getName();
 		}
 		
 		return new String[]{dataArg, typedData, typedArgList};
 	}
 }
