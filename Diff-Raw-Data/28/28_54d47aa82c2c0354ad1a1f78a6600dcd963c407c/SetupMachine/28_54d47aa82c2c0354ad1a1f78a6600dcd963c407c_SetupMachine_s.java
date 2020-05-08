 package org.tekkotsu.api;
 import java.util.ArrayList;
 
 //Node class for the Tekkotsu State Machine Composer
 //Author: Albert Berk Toledo
 //Start date: 06/17/2013
 
 
 //Class Header
 public class SetupMachine {
 	
 
 	//Attributes
 	private ArrayList<NodeInstance> Nodes;				//List of the nodes
 	private ArrayList<TransitionInstance> Transitions;	//List of the transitions
 
 
 	//Constructor
 	public SetupMachine(ArrayList<NodeInstance> Nodes, ArrayList<TransitionInstance> Transitions){
 		this.Nodes = Nodes;
 		this.Transitions = Transitions;
 	}
 	
 	//No argument constructor to initialize empty setup machine.
 	public SetupMachine(){
 		this.Nodes = new ArrayList<NodeInstance>();
 		this.Transitions = new ArrayList<TransitionInstance>();
 	}
 
 
 
 	//Accessor methods
 	public ArrayList<NodeInstance> getNodes(){
 		return Nodes;
 	}
 	
 	public ArrayList<TransitionInstance> getTransitions(){
 		return Transitions;
 	}
 	
 
 
 
 	//Mutator methods
 	public void setNodes(ArrayList<NodeInstance> Nodes){
 		this.Nodes = Nodes;
 	}
 	
 	public void setTransitions(ArrayList<TransitionInstance> Transitions){
 		this.Transitions = Transitions;
 	}
 	
 
 
 
 
 	//Method to add a node to the setup machine.
 	public void addNode(NodeInstance node){
 		
 		//Create lists to store the incoming and outgoing transitions of the current node to be operated.
 		ArrayList<TransitionInstance> incoming = node.getInTrans();
 		ArrayList<TransitionInstance> outgoing = node.getOutTrans();
 		
 		//Check every incoming trans and add to the node from the sources.
 		for(int i = 0; i < incoming.size(); i++ ){
 			Transitions.get(Transitions.indexOf(incoming.get(i))).addSource(node);
 		}
 		
 		//Check every outgoing trans and add to the node from the targets.
 		for(int i = 0; i < outgoing.size(); i++){
 			Transitions.get(Transitions.indexOf(outgoing.get(i))).addTarget(node);
 		}		
 		
 		this.Nodes.add(node);
 	}
 	
 	//Method to add a transition to the setup machine.
 	public void addTransition(TransitionInstance transition){
 		
 		//Create lists to store sources and targets of the current transition to be operated.
 		ArrayList<NodeInstance> sources = transition.getSources();
 		ArrayList<NodeInstance> targets = transition.getTargets();
 		
		
		//Check every source of the transition and add the transition as outgoing.
 		for(int i = 0; i < sources.size(); i++ ){
			Nodes.get(Nodes.indexOf(sources.get(i))).addOutTrans(transition);
 		}
 		
 		//Check every target of the transition and the transition as incoming.
 		for(int i = 0; i < targets.size(); i++){
 			Nodes.get(Nodes.indexOf(targets.get(i))).addInTrans(transition);
 		}
 		
 		//Remove transition from the transition list.
 		this.Transitions.add(transition);
 				
 		
 		
 	}
 	
 	//Method that removes a node from the setup machine.
 	public void removeNode(NodeInstance node){
 		
 		
 		//Create lists to keep incoming and outgoing transitions of the current node to be operated.
 		ArrayList<TransitionInstance> incoming = node.getInTrans();
 		ArrayList<TransitionInstance> outgoing = node.getOutTrans();
 		
 		//Check every incoming trans and remove the node from the sources.
 		for(int i = 0; i < incoming.size(); i++ ){
 			Transitions.get(Transitions.indexOf(incoming.get(i))).removeSource(node);
 		}
 		
 		//Check every outgoing trans and remove the node from the targets.
 		for(int i = 0; i < outgoing.size(); i++){
 			Transitions.get(Transitions.indexOf(outgoing.get(i))).removeTarget(node);
 		}
 		
 		//Remove node from setup machine.
 		this.Nodes.remove(node);
 	}
 	
 	//Method that removes a transitions from the setupmachine.
 	public void removeTransition(TransitionInstance transition){
 		
 		//Create lists to store sources and targets of the current transition to be operated.
 		ArrayList<NodeInstance> sources = transition.getSources();
 		ArrayList<NodeInstance> targets = transition.getTargets();
 		
 		//Check every source and remove the transition from outgoing list.
 		for(int i = 0; i < sources.size(); i++ ){
 			Nodes.get(Nodes.indexOf(sources.get(i))).removeOutTrans(transition);
 		}
 		
 		//Check every target and remove the transition from incoming list.
 		for(int i = 0; i < targets.size(); i++){
 			Nodes.get(Nodes.indexOf(targets.get(i))).removeInTrans(transition);
 		}
 		
 		//Remove the transition from the setup machine.
 		this.Transitions.remove(transition);
 		
 	}
 	
 }
