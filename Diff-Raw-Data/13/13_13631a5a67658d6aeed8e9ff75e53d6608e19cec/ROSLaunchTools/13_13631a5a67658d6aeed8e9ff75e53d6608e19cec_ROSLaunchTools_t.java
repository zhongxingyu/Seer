 /**
  * HyperFlex toolchain
  * 
  * Copyright (c) 2013
  * All rights reserved.
  * 
  * Luca Gherardi
  * ETH Zurich
  * Institute for Dynamic Systems and Control
  * 
  * ***********************************************************************************************
  * 
  * Author: <A HREF="mailto:lucagh@ethz.ch">Luca Gherardi</A>
  * 
  * In collaboration with: 
  *   <A HREF="mailto:brugali@unibg.it">Davide Brugali</A>, University of Bergamo
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
 package org.hyperflex.componentmodels.ros.m2t.tools;
 
 
 
 import java.util.ArrayList;
 
 import org.eclipse.emf.ecore.EObject;
 import org.hyperflex.roscomponentmodel.AbstractComponent;
 import org.hyperflex.roscomponentmodel.Composite;
 import org.hyperflex.roscomponentmodel.CompositeMsgInterface;
 import org.hyperflex.roscomponentmodel.MsgInterface;
 import org.hyperflex.roscomponentmodel.Node;
 import org.hyperflex.roscomponentmodel.NodeMsgInterface;
 import org.hyperflex.roscomponentmodel.Topic;
 
 
 
 public class ROSLaunchTools {
 
 	public ROSLaunchTools() {	
 
 	}
 
 	/**
 	 * Returns the root of the model starting from one of the composites
 	 * @generated NOT
 	 */
 	public org.hyperflex.roscomponentmodel.System getSystem(Composite composite){
 
 		Composite tmpComposite = composite;
 		while(tmpComposite.eContainer() instanceof Composite){
 
 			tmpComposite = (Composite)tmpComposite.eContainer();
 
 		}
 
 		return (org.hyperflex.roscomponentmodel.System)tmpComposite.eContainer();
 
 	}
 
 	/**
 	 * Returns the list of all the nodes of a certain composite
 	 * (directly and inderectly contained)
 	 * @generated NOT
 	 */
 	public ArrayList<Node> getAllNodes(Composite composite){
 
 		ArrayList<Node> result = new  ArrayList<Node>();
 
 		for(AbstractComponent component : composite.getComponents()){
 			if(component instanceof Node){
 				result.add((Node)component);
 			}else if(component instanceof Composite){
 				result.addAll(getAllCompositeNodes((Composite)component));
 			}
 		}
 
 		return result;
 
 	}
 
 	public ArrayList<Node> getAllCompositeNodes(Composite composite){ 
 		ArrayList<Node> result = new  ArrayList<Node>();
 
 		for(AbstractComponent component : composite.getComponents()){
 			if(component instanceof Node){
 				result.add((Node)component);
 			}else if(component instanceof Composite){
 				result.addAll(getAllNodes((Composite)component));
 			}
 		}
 
 		return result;
 
 	}
 
 	public String getActualTopicName(NodeMsgInterface msgInterface){
 		
 		return getActualTopicName((MsgInterface)msgInterface);
 	
 	}
 	
 	private String getActualTopicName(MsgInterface msgInterface){
 		
 		EObject tmp = msgInterface.eContainer().eContainer();
 		Composite composite;
 		if(tmp instanceof Composite){
 			composite = (Composite)tmp;
 		}else{
 			return "";
 		}
 		
 		for(Topic topic : composite.getTopics()){
 			
 			if(msgInterface.getConnection() != null && msgInterface.getConnection().equals(topic)){
 				
 				// check if the topic is exposed
 				for(CompositeMsgInterface compositeMsgInterface : composite.getMsgConsumers()){
 					
					if(compositeMsgInterface.getExposed() != null && compositeMsgInterface.getExposed().equals(topic)){
 						String name = getActualTopicName(compositeMsgInterface);
 						if(name.equals("")){
 							return topic.getName();
 						}
						return name;
 						
 					}
 					
 				}
 				
 				for(CompositeMsgInterface compositeMsgInterface : composite.getMsgProducers()){
 					
					if(compositeMsgInterface.getExposed() != null && compositeMsgInterface.getExposed().equals(topic)){
 						String name = getActualTopicName(compositeMsgInterface);
 						if(name.equals("")){
 							return topic.getName();
 						}
						return name;
 					}
 					
 				}
 				// a msg interface can be connected to one topic, not more.
 				// hence we can return the name
 				return topic.getName();				
 				
 			}
 			
 		}
 		
 		return "";
 		
 	}
 
 }
