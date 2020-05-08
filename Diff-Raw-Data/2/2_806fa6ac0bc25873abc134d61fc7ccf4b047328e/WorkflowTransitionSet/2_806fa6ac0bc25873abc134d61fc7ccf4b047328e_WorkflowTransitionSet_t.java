 /*******************************************************************************
  * Copyright 2013-2014 Gengyu Shi
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.shigengyu.hyperion.core;
 
 import java.util.List;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 public class WorkflowTransitionSet {
 
 	public static WorkflowTransitionSet copyOf(Iterable<WorkflowTransition> transitions) {
 		return new WorkflowTransitionSet(ImmutableList.copyOf(transitions));
 	}
 
 	public static WorkflowTransitionSet empty() {
 		return new WorkflowTransitionSet(ImmutableList.<WorkflowTransition> of());
 	}
 
 	private final ImmutableList<WorkflowTransition> transitions;
 
 	private WorkflowTransitionSet(ImmutableList<WorkflowTransition> transitions) {
 		this.transitions = transitions;
 	}
 
 	public final WorkflowTransitionSet filter(Predicate<WorkflowTransition> predicate) {
 		List<WorkflowTransition> filtered = Lists.newArrayList();
 		for (WorkflowTransition transition : transitions) {
 			if (predicate.apply(transition)) {
 				filtered.add(transition);
 			}
 		}
 		return WorkflowTransitionSet.copyOf(filtered);
 	}
 
 	public final WorkflowTransitionSet filter(String transitionName) {
 		List<WorkflowTransition> list = Lists.newArrayList();
 		for (WorkflowTransition transition : transitions) {
 			if (transition.getName().equals(transitionName)) {
 				list.add(transition);
 			}
 		}
 		return new WorkflowTransitionSet(ImmutableList.copyOf(list));
 	}
 
 	public final WorkflowTransitionSet filter(WorkflowStateSet fromStates) {
 		List<WorkflowTransition> list = Lists.newArrayList();
 		for (WorkflowTransition transition : transitions) {
 			if (transition.getFromStates().isSubSetOf(fromStates)) {
 				list.add(transition);
 			}
 		}
 		return new WorkflowTransitionSet(ImmutableList.copyOf(list));
 	}
 
 	public final WorkflowTransition get(int index) {
		return transitions.get(index);
 	}
 
 	public final WorkflowTransitionSet getAutoTransitions() {
 		return filter(new Predicate<WorkflowTransition>() {
 
 			@Override
 			public boolean apply(WorkflowTransition input) {
 				return input.isAuto();
 			}
 		});
 	}
 
 	public final boolean isEmpty() {
 		return transitions.isEmpty();
 	}
 
 	public final int size() {
 		return transitions.size();
 	}
 }
