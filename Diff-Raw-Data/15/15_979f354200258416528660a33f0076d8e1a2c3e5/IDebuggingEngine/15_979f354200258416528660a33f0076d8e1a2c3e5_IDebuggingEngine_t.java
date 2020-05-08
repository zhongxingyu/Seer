 package org.eclipse.dltk.launching.debug;
 
 import org.eclipse.dltk.launching.IInterpreterConfigModifier;
 
 public interface IDebuggingEngine {
 	String getId();
 
 	String getModelId();
 
 	String getNatureId();
 
 	String getName();
 
 	String getDescription();
 
 	int getPriority();
 
 	IInterpreterConfigModifier getConfigModifier();
 }
