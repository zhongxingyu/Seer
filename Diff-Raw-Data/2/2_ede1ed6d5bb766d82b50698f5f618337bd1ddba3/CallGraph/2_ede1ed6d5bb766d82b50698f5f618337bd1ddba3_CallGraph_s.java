 package org.javaan.model;
 
 /*
  * #%L
  * Java Static Code Analysis
  * %%
  * Copyright (C) 2013 Andreas Behnke
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.javaan.graph.NamedObjectDirectedGraph;
 import org.javaan.graph.NamedObjectEdge;
 import org.javaan.graph.NamedObjectVisitor;
 import org.jgrapht.alg.StrongConnectivityInspector;
 
 /**
  * Represents the call-graph of all loaded methods.
  * Type dependencies are created for every method.
  */
 public class CallGraph {
 	
 	private final NamedObjectDirectedGraph<Method> callerOfMethod = new NamedObjectDirectedGraph<Method>();
 
 	private final NamedObjectDirectedGraph<Type> usageOfType = new NamedObjectDirectedGraph<Type>();
 
 	public int size() {
 		return callerOfMethod.vertexSet().size();
 	}
 
 	public void addCall(Method caller, Method callee) {
 		if (caller == null) {
 			throw new IllegalArgumentException("Parameter caller must not be null");
 		}
 		if (callee == null) {
 			throw new IllegalArgumentException("Parameter callee must not be null");
 		}
 		callerOfMethod.addEdge(caller, callee);
 		Type typeOfCaller = caller.getType();
 		Type typeOfCallee = callee.getType();
 		if (!typeOfCaller.equals(typeOfCallee)) {
 			usageOfType.addEdge(typeOfCaller, typeOfCallee);
 		}
 	}
 	
 	public Set<Method> getCallers(Method callee) {
 		return callerOfMethod.sourceVerticesOf(callee);
 	}
 	
 	public Set<Method> getCallees(Method caller) {
 		return callerOfMethod.targetVerticesOf(caller);
 	}
 	
 	public void traverseCallers(Method callee, NamedObjectVisitor<Method> callerVisitor) {
 		callerOfMethod.traversePredecessorsDepthFirst(callee, callerVisitor);
 	}
 	
 	public void traverseCallees(Method caller, NamedObjectVisitor<Method> calleeVisitor) {
 		callerOfMethod.traverseSuccessorsDepthFirst(caller, calleeVisitor);
 	}
 	
 	public Set<Method> getLeafCallers(Method callee) {
 		return callerOfMethod.getLeafPredecessors(callee);
 	}
 	
 	public Set<Method> getLeafCallees(Method caller) {
 		return callerOfMethod.getLeafSuccessors(caller);
 	}
 
 	public void traverseUsedTypes(Type using, NamedObjectVisitor<Type> usedVisitor) {
 		usageOfType.traverseSuccessorsDepthFirst(using, usedVisitor);
 	}
 	
 	public void traverseUsingTypes(Type used, NamedObjectVisitor<Type> usingVisitor) {
 		usageOfType.traversePredecessorsDepthFirst(used, usingVisitor);
 	}
 
 	public Set<Type> getLeafUsedTypes(Type using) {
 		return usageOfType.getLeafSuccessors(using);
 	}
 	
 	public Set<Type> getLeafUsingTypes(Type using) {
 		return usageOfType.getLeafPredecessors(using);
 	}
 	
 	/**
 	 * @return list of type sets which take part in a using dependency cycle
 	 */
 	public List<Set<Type>> getDependencyCycles() {
 		StrongConnectivityInspector<Type, NamedObjectEdge<Type>> inspector = new StrongConnectivityInspector<Type, NamedObjectEdge<Type>>(usageOfType);
 		List<Set<Type>> cycles = new ArrayList<Set<Type>>();
 		for (Set<Type> cycle : inspector.stronglyConnectedSets()) {
			if (cycle.size() > 1) { // ignore depedency cycles within one class (these cycles have no impact in software design)
 				cycles.add(cycle);
 			}
 		}
 		return cycles;
 	}
 }
