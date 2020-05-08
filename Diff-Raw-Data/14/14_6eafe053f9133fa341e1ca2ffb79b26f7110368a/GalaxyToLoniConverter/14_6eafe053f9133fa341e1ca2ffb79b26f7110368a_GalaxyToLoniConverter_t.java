 package Galaxy.Visitor;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import Core.Pair;
 import Galaxy.Tree.Tool.Command;
 import Galaxy.Tree.Tool.Inputs;
 import Galaxy.Tree.Tool.Parameter;
 import Galaxy.Tree.Tool.Tool;
 import Galaxy.Tree.Workflow.ExternalInput;
 import Galaxy.Tree.Workflow.ExternalOutput;
 import Galaxy.Tree.Workflow.InputConnection;
 import Galaxy.Tree.Workflow.Position;
 import Galaxy.Tree.Workflow.Step;
 import Galaxy.Tree.Workflow.Workflow;
 
 
 import LONI.tree.Connection;
 import LONI.tree.Pipeline;
 import LONI.tree.GraphObject.Module;
 import LONI.tree.GraphObject.ModuleGroup;
 import Specification.GalaxySpecification;
 
 public class GalaxyToLoniConverter extends DFSVisitor
 {
 	private static int MGROUP_COUNT=0;
 	
 	public String getModuleGroup(){
 		String s = "mgrp" + MGROUP_COUNT;
 		MGROUP_COUNT++;
 		return s;
 	}
 
 
 	public Object visit(Workflow workflow) 
 	{
 		Pipeline pipeline;
 		ModuleGroup mgroup;
 		String annotation = workflow.getAnnotation();
 		String version = workflow.getFormatVersion();
 		String name = workflow.getName();
 		mgroup = new ModuleGroup(getModuleGroup(), 
 				name, "package", "version", annotation, 
 				"icon", 0, 0,0, false);
 		
 		
 		for(Step s : workflow.getSteps()){
 			Pair<Module, List<Connection>> dat;
 			dat = (Pair<Module, List<Connection>>) stepVisitor.visit(s);
 			
 			mgroup.getConnections().addConnections(dat.getElem2());
 			mgroup.getModules().add(dat.getElem1());
 			
 		}
 		
 		pipeline = new Pipeline(version, mgroup);
 		
 		return pipeline;
 	}
 	{
 		stepVisitor = new StepVisitor() {
 			public Pair<Module, List<Connection>> visit(Step step){
 				Module genModule;
 				List<Connection> genConnection = new LinkedList<Connection>();
 				Tool details = GalaxySpecification.getDatabase().getTool(step.getToolId());
 				
 				String description;
 				int posX;
 				int posY;
 				String id;
 				String name;
 				String package_;
 				String executableVersion;
 				String version;
 				String location;
 				int rotation;
 				String icon;
 				String advancedOptions;
 				
 				
 				posX = step.getPosition().getFromLeft();
 				posY = step.getPosition().getFromTop();
				description = "Annotation: " + step.getAnnotation() + " " + "Tool Description: " + ((details == null) ? "" : details.getDescription());
 				id = step.getToolId();
 				name = step.getName();
 				package_ = "package";
 				version = step.getToolVersion();
				executableVersion = ((details == null) ? "" : details.getVersion());
 				location = "pipeline://localhost/";
 				rotation = 0;
 				icon = "icon";
 				advancedOptions = "";
 				
 				genModule = new Module(posX, posY, id, name, package_, version, executableVersion, description ,
 						location, rotation, icon, advancedOptions, false, false, false, false, "sourceCode", 
 						false, false, "mPIParallelEnv", "mPINumSlots", false);
				List<LONI.tree.Parameter> inputs = null;
				if (details != null) {
					inputs = (List<LONI.tree.Parameter>) visit(details.getToolInputs());
					genModule.addInputs(inputs);
				}
 				
 				
 				for(String sink : step.getConnectionSinks()){
 					InputConnection src = step.getConnectionSource(sink);
 					genConnection.add(new Connection(
 							src.getSourceId()+"_"+src.getSourceName(),
 							sink));
 				}
 				
 				return new Pair(genModule, genConnection);
 			}
 		
 			public Object visit(Command command){
 				command.getInterpereter();
 				command.getCommand();
 				return null;
 			}
 			public Object visit(Inputs inputs){
 				inputs.getNginxUpload();
 				List<LONI.tree.Parameter> parameters = new ArrayList<LONI.tree.Parameter>();
 				for(Parameter input : inputs.getInputList()){
 					parameters.add((LONI.tree.Parameter) visit(input));
 					
 				}
 				
 				return parameters;
 			}
 			
 			public Object visit(Parameter parameter){
 				LONI.tree.Parameter loniParameter;
 				parameter.getHelp();
 				parameter.getType();
 				String id = parameter.getName();
 				String name = parameter.getName();
 				String description = parameter.getLabel();
 				boolean enabled = true;
 				boolean required = true;
 				boolean predefined = false;
 				boolean isMetadata = false;
 				boolean isListFile = false;
 				boolean isHideData = false;
 				boolean includeTransformedParameter = false;
 				int order = 0;
 				String prefix = "";
 				boolean prefixSpaced = false;
 				boolean prefixAllArgs = false;
 				loniParameter = new LONI.tree.Parameter(id, name,  description,
 						enabled, required, predefined,
 						isMetadata, isListFile, isHideData,
 						includeTransformedParameter, order, prefix,
 						 prefixSpaced,  prefixAllArgs);
 				return loniParameter;
 			}
 		};
 	}
 }
