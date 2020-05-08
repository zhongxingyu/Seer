 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.core;
 
 import com.objectgraph.core.exceptions.NodeHelperAlreadyUsedException;
 
import java.lang.ref.WeakReference;

 public abstract class NodeHelper<N extends Node> {
 
    private WeakReference<N> node;
 
     void setNode(N node) {
        if (node == null)
            this.node = null;

        if (this.node != null && this.node.get() != node)
            throw new NodeHelperAlreadyUsedException(this, this.node.get());
        this.node = new WeakReference<N>(node);
     }
 
     protected N getNode() {
        return node.get();
     }
 
     @SuppressWarnings("unchecked")
     protected <NN extends Node> NN getNode(Class<NN> type) {
        return (NN) node.get();
     }
 
 }
