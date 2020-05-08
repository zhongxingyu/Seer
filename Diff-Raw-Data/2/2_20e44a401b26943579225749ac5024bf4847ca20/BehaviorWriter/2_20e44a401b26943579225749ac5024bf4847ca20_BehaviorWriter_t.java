 package org.tekkotsu.api;
 
 import java.util.ArrayList;
 
 public class BehaviorWriter {
 	
 	//Attributes
 	private NodeClass nodeClass;
 	private String registerCall;
 	private String include;
 	private String comment;
 	private SetupMachine setup;
 	
 	//Constructor
 	public BehaviorWriter(NodeClass nodeClass){
 		this.nodeClass = nodeClass;
 		
 		this.registerCall = "\n\nREGISTER_BEHAVIOR(" + this.nodeClass.getName() + ");";
 		this.include = "#include \"Behaviors/StateMachine.h\" \n\n";
 		this.comment = "//" + nodeClass.getName() + " Node Class\n";
 		this.setup = nodeClass.getSetupMachine();
 	}
 	
 	//Method to get the fsm content as string.
 	public String writeBehavior(){
 
 		//String for class header.
 		String fsm = include;
 
 		fsm += getClassDef(0);
 		
 		//Get the register call
 		fsm += registerCall;
 		
 		
 		System.out.println(fsm);
 		
 		return fsm;
 	}
 	
 	//Method to get class definition code
 	public String getClassDef(int depth){
 		
 		String indent = "";
 		
 		for(int i = 0; i < depth; i++){
 			indent += "\t";
 		}
 		
 		//String for class header.
 		String fsm = getHeader(depth);
 		
 		//If the class has any variables, get the variables.
 		if(nodeClass.getVariables().size() > 0){
 			fsm += getVariables(depth);	
 		}
 		
 		//If there are any method declarations get the code for them
 		if(nodeClass.getMethods().size() > 0){		
 			fsm += getMethods(depth);
 		}
 		
 		//If there are any subclasses get them and print their code.
 		if(nodeClass.getSubClasses().size() > 0){
 			
 			//For every subclass get the fsm
 			for(int i = 0; i < nodeClass.getSubClasses().size(); i++){
 				
 				//Get the fsm
 				fsm += new BehaviorWriter(nodeClass.getSubClasses().get(i)).getClassDef(depth+1);
 				
 			}
 			
 		}
 		
 		//if there is a setup machine get the setupmachine
 		if(nodeClass.getSetupMachine() != null){
 			fsm += getSetup();
 		}
 		
 		fsm += "\n\n" + indent + "}\n\n";
 		
 		return fsm;
 	}
 	
 	//Method to set optional comment.
 	public void setComment(String comment){
 		this.comment = comment;
 	}
 	
 	//Method to get node declarations.
 	public String getNodes(){
 		
 		//Create string to return
 		String nodes = "";
 		
 		//Create list of nodeinstance objects from setup object.
 		ArrayList<NodeInstance> nodeList = this.setup.getNodes();
 		
 		//For every node in the setupmachine
 		for(int i = 0; i < nodeList.size(); i++){
 			
 			//Get the current node instance object.
 			NodeInstance current = nodeList.get(i);
 			
 			//Print its label: constructorname
 			nodes += "\n\t\t" + current.getLabel() + ": " + current.getType().getConstructor().getName();
 			
 			//if any parameters
 			if(current.getNumOfParameters() > 0){
 				
 				//open parantheses
 				nodes += "(";
 				
 				//for every parameter in the constructor
 				for(int j = 0; j < current.getNumOfParameters(); j++){
 					
 					//get the value of parameter and add to the string
 					nodes +=  current.getParameters().get(j).getValue();
 					
 					if(j != current.getNumOfParameters()-1){
 						nodes += ", ";
 					}
 					
 				}
 				
 				//close paranteses of the constructor
 				nodes += ")\n";
 				
 			}
 		}
 		
 		return nodes;
 	}
 	
 	
 	//Method to get the transitions
 	public String getTransitions(){
 		
 		//Create string to return
 		String transitions = "";
 		
 		//Create transition instance objects from the setup object.
 		ArrayList<TransitionInstance> transList = this.setup.getTransitions();
 		
 		//for every transition initialized
 		for(int i = 0; i < transList.size(); i++){
 			
 			transitions += "\t\t";
 			
 			//Get current transition instance
 			TransitionInstance current = transList.get(i);
 			
 			//Get sources and targets
 			ArrayList<NodeInstance> sources = current.getSources();
 			ArrayList<NodeInstance> targets = current.getTargets();
 			
 			//if there are multiple sources
 			if(current.getNumOfSources() > 1){
 				
 				//open curly braces to put  labels of nodes in.
 				transitions += "{";
 				
 				//for every source
 				for(int j = 0; j < current.getNumOfSources(); j++){
 					
 					transitions += sources.get(j).getLabel() + " ";
 					
 					if(j != current.getNumOfSources()-1){
 						transitions += ", ";
 					}
 				}
 				
 				//close curly braces
 				transitions += "}";
 				
 			}else{
 				
 				//print label of the source
				transitions += current.getSources().get(0).getLabel();
 				
 			}
 			
 			//get transition constructor.
 			ConstructorCall currentTransitionConstructor = current.getType().getConstructor();
 			
 			//Get current transition constructor name
 			transitions += " =" + currentTransitionConstructor.getName();
 			
 			//if current transition constructor has any parameters
 			if(currentTransitionConstructor.getParameters().size() > 0){
 				transitions += "(";
 				
 				//for every parameter of transition.
 				for(int h = 0; h < currentTransitionConstructor.getParameters().size(); h++){
 					transitions +=  currentTransitionConstructor.getParameters().get(h).getValue();
 					if(h != currentTransitionConstructor.getParameters().size()-1){
 						transitions += ", ";
 					}
 				}
 				transitions += ")";
 			}
 			
 			transitions += "=> ";
 			
 			//if there are multiple targets
 			if(current.getNumOfTargets() > 1){
 				
 				//open curly braces to put  labels of nodes in.
 				transitions += "{";
 				
 				//for every source
 				for(int j = 0; j < current.getNumOfTargets(); j++){
 					
 					//print the label
 					transitions += targets.get(j).getLabel();
 					
 					if(j != current.getNumOfTargets()-1){
 						transitions += ", ";
 					}
 					
 				}
 				
 				//close curly braces
 				transitions += "}";
 				
 			}else{
 				
 				//print label of the target
 				transitions += current.getTargets().get(0).getLabel() + " ";
 				
 			}
 			
 			//go to the next line
 			transitions += "\n";
 			
 			}
 		
 		//return the transitions
 		return transitions;
 		
 	}
 	
 	
 	//Method to get meat.
 	public String getMeat(){
 		
 		//Add string to create the meat.
 		String meat = getNodes() + "\n\n" + getTransitions();
 	
 		
 		return meat;
 		
 	}
 	
 	
 	//get header method to get the class header
 	public String getHeader(int depth){
 		
 		String indent = "";
 		
 		for(int i = 0; i < depth; i++){
 			indent += "\t";
 		}
 		
 		String classHeader = indent + comment;
 				
 		classHeader +=	indent + "$nodeclass " + nodeClass.getName() + " : " + "StateNode {\n\n";
 		
 		return classHeader;
 		
 	}
 	
 	//getsetup method that calls get meat and other string to form the setupmachine block.
 	public String getSetup(){
 		
 		//String to store the the block.
 		String setup = "\t//Setupmachine for the behavior\n";
 		
 		//header
 		setup += "\t$setupmachine {\n\n";
 		
 		//meat
 		setup += getMeat();
 		
 		//close block
 		setup += "\n\t}\n\n";
 				
 		return setup;
 	}
 	
 	//getVariables method to get the code of variable declarations.
 	public String getVariables(int depth){
 		
 		String indent = "";
 		
 		for(int i = 0; i < depth+1; i++){
 			indent += "\t";
 		}
 		
 		//Initialize string with method and return
 		String vars = indent + "//Variable declarations\n";
 		
 		//For each variable, make a new line and print code.
 		for(int i = 0; i < nodeClass.getVariables().size(); i++){
 			
 			//Add provide keyword.
 			vars +=  indent + "$provide ";
 			
 			//Add type name
 			vars += nodeClass.getVariables().get(i).getType() + " ";
 			
 			//Add variable name
 			vars += nodeClass.getVariables().get(i).getName() + ";";
 			
 		}
 		
 		vars += "\n\n";
 		
 		return vars;
 		
 	}
 	
 	//Method to get the method declarations including dostart etc if there is.
 	public String getMethods(int depth){
 		
 		String indent = "";
 		
 		for(int i = 0; i < depth+1; i++){
 			indent += "\t";
 		}
 		
 		//Initialize holding string with comment and return
 		String mets = indent + "//Method declarations\n";
 		
 		//For each method print the code
 		for(int i = 0; i < nodeClass.getMethods().size(); i++){
 			
 			//Get the current method.
 			Method met = nodeClass.getMethods().get(i);
 			
 			//Add keyword virtual
 			mets += indent + "virtual "; 
 			
 			//Add the returntype
 			mets += met.getReturnType() + " "; 
 			
 			//Add the name of the method and open paranthesis.
 			mets += met.getName() + "(";
 			
 			//for each parameter in the method print the type and name.
 			for(int j = 0; j < met.getParameters().size(); j++){
 				
 				mets += met.getParameter(j).getType() + " ";
 				mets += met.getParameter(j).getName();
 				
 				//if not the last one put a comma after
 				if(j != met.getParameters().size()-1){
 					mets += ", ";
 				}
 	
 				
 			}//End parameters
 			
 			//close the parameter open the curly braces and jump to the next line
 			mets += "){\n";
 			
 			//print the body of the method which will be edited as string in the editor.
 			mets += "\n" + indent + met.getBody() + "\n\n";
 			
 			//close method braces
 			mets += "\n\n" + indent + "}\n\n";
 			
 			
 		}//End single method
 		
 		//return the code
 		return mets;
 		
 	}
 	
 	
 }
