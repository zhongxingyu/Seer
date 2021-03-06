 /*
  * Copyright (c) 2011, IRISA
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice,
  *     this list of conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice,
  *     this list of conditions and the following disclaimer in the documentation
  *     and/or other materials provided with the distribution.
  *   * Neither the name of the IRISA nor the names of its
  *     contributors may be used to endorse or promote products derived from this
  *     software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 package net.sf.orcc.backends.tta;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.orcc.backends.instructions.InstCast;
 import net.sf.orcc.ir.Actor;
 import net.sf.orcc.ir.Node;
 import net.sf.orcc.ir.Procedure;
 import net.sf.orcc.ir.State;
 import net.sf.orcc.ir.Var;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * This class allows the string template accessing informations about
  * application's instances
  * 
  * @author Herve Yviquel
  * 
  */
 public class TTAActorTemplateData {
 
 	private Map<Var, Var> castedListReferences;
 
 	/**
 	 * Label of all nodes
 	 */
 	private Map<Node, Integer> nodeToLabelMap;
 
 	private Map<State, Integer> stateToLabelMap;
 
 	public TTAActorTemplateData(Actor actor) {
 		nodeToLabelMap = new HashMap<Node, Integer>();
 		castedListReferences = new HashMap<Var, Var>();
 		stateToLabelMap = new HashMap<State, Integer>();
 
 		computeTemplateMaps(actor);
 	}
 
 	private void computeCastedListReferences(Actor actor) {
 		TreeIterator<EObject> it = actor.eAllContents();
 		while (it.hasNext()) {
 			EObject object = it.next();
 			if (object instanceof Var) {
 				Var var = (Var) object;
 				if (var.getType().isList()
 						&& !var.getDefs().isEmpty()
 						&& (var.getDefs().get(0).eContainer() instanceof InstCast)) {
 					castedListReferences.put(var, var);
 				}
 			}
 		}
 	}
 
 	private void computeNodeToLabelMap(Actor actor) {
 		TreeIterator<EObject> it = actor.eAllContents();
 		while (it.hasNext()) {
 			EObject object = it.next();
 			if (object instanceof Procedure) {
 				Procedure proc = (Procedure) object;
 				int label = 1;
 				TreeIterator<EObject> it2 = proc.eAllContents();
 				while (it2.hasNext()) {
 					EObject object2 = it2.next();
 					if (object2 instanceof Node) {
 						Node node = (Node) object2;
 						nodeToLabelMap.put(node, label);
 						label++;
 					}
 				}
 			}
 		}
 	}
 
 	private void computeStateToLabelMap(Actor actor) {
 		if (actor.hasFsm()) {
 			for (int i = 0; i < actor.getFsm().getStates().size(); i++) {
 				stateToLabelMap.put(actor.getFsm().getStates().get(i), i);
 			}
 		}
 	}
 
 	public void computeTemplateMaps(Actor actor) {
 		computeNodeToLabelMap(actor);
 		computeCastedListReferences(actor);
 		computeStateToLabelMap(actor);
 	}
 
 	public Map<Var, Var> getCastedListReferences() {
 		return castedListReferences;
 	}
 
 	public Map<Node, Integer> getNodeToLabelMap() {
 		return nodeToLabelMap;
 	}
 
 	public Map<State, Integer> getStateToLabelMap() {
 		return stateToLabelMap;
 	}
 
 }
