 package org.javaan.graph;
 
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
 
import org.javaan.model.NamedObject;
 
 /**
 * Visits objects during traversal of {@link NamedObjectDirectedGraph}. Provides
  * depth information for depth first traversal.
  */
 public interface ObjectVisitor<V, E> {
 
 	/**
 	 * @return true, if graph iteration is finished.
 	 */
 	boolean finished();
 
 	/**
	 * Visit the next {@link NamedObject} N at graph depth = level.
 	 * Level will be -1 for breadth first traversal.
 	 */
	void visitVertex(V named, int level);
 
 	/**
	 * Visit the next {@link VertexEdge} at graph depth = level.
 	 * Level will be -1 for breadth first traversal.
 	 */
	void visitEdge(E namedEdge, int level);
 }
