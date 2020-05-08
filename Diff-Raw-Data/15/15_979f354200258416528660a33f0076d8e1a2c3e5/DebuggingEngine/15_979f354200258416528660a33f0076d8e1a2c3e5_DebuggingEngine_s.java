 package org.eclipse.dltk.internal.launching.debug;
 
import java.io.File;

 import org.eclipse.dltk.launching.IInterpreterConfigModifier;
 import org.eclipse.dltk.launching.IInterpreterConfigModifierFactory;
 import org.eclipse.dltk.launching.debug.IDebuggingEngine;
 
 public class DebuggingEngine implements IDebuggingEngine {
 
 	private String id;
 	private String modelId;
 	private String natureId;
 	private String name;
 	private String description;
 	private int priority;
 	private IInterpreterConfigModifierFactory factory;
 
 	public DebuggingEngine(String id, String modelId, String natureId, String name,
 			String description, int priority,
 			IInterpreterConfigModifierFactory factory) {
 		super();
 		this.id = id;
 		this.modelId = modelId;
 		this.natureId = natureId;
 		this.name = name;
 		this.description = description;
 		this.priority = priority;
 		this.factory = factory;
 	}
 
 	public String getId() {
 		return id;
 	}
 	
 	public String getModelId() {
 		return modelId;
 	}
 
 	public String getNatureId() {
 		return natureId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public int getPriority() {
 		return priority;
 	}
 
 	public IInterpreterConfigModifier getConfigModifier() {
 		return factory.createInterpreterConfigModifier();
 	}
 }
