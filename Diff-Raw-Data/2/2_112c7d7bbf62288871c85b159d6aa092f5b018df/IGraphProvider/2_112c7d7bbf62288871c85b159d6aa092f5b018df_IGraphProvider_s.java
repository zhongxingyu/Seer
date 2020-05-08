 /**
  * Copyright (c) 2006-2011 Cloudsmith Inc. and other contributors, as listed below.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Cloudsmith
  * 
  */
 package org.cloudsmith.graph;
 
 import java.util.Collection;
 
 import org.cloudsmith.graph.graphcss.Rule;
 
 /**
  * Interface for a provider of a an optionally styled graph.
  * An implementation of a graph provider should strive to provide all methods, but
  * may throw {@link UnsupportedOperationException} if not capable of producing a graph without
  * an input model.
  */
 public interface IGraphProvider {
 
 	/**
 	 * Method that computes/provides a graph.
 	 * An implementation may throw {@link UnsupportedOperationException} if not capable of producing
 	 * a graph without a model, but should consider returning an empty graph (with some message).
 	 * 
 	 * @return
 	 */
 	public IRootGraph computeGraph();
 
 	/**
 	 * Method that transforms/computes the given model to a graph, using the a label and id determined
 	 * by the producer.
 	 * This method should also compute any graph specific styling rules (returned by {@link #getRules()}).
 	 * 
 	 * @param model
 	 * @param label
 	 * @param id
 	 * @return
 	 */
	public IGraph computeGraph(Object model);
 
 	/**
 	 * Method that transforms/computes the given model to a graph, using the given label and id as graph label/id.
 	 * This method should also compute any graph specific styling rules (returned by {@link #getRules()}).
 	 * 
 	 * @param model
 	 * @param label
 	 * @param id
 	 * @return
 	 */
 	public IGraph computeGraph(Object model, String label, String id);
 
 	/**
 	 * Returns a collection of Rule containing styling rules for the specific graph.
 	 * Never returns null. The list is only valid after {@link #computeGraph(Object, String, String)} has
 	 * been called. The returned collection is not modifiable.
 	 * 
 	 * @return
 	 */
 	public Collection<Rule> getRules();
 
 }
