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
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.Serializer;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 import com.esotericsoftware.kryo.serializers.FieldSerializer;
 
 public class NodeSerializer extends Serializer<Node> {
 
     @Override
     public void write(Kryo kryo, Output output, Node node) {
         Serializer<Node> serializer = new FieldSerializer<>(kryo, node.getClass());
         serializer.write(kryo, output, node);
     }
 
     @Override
     public Node read(Kryo kryo, Input input, Class<Node> nodeClass) {
         // TODO check if it is needed the same trick as in copy()
         Serializer<Node> serializer = new FieldSerializer<>(kryo, nodeClass);
         Node ret = serializer.read(kryo, input, nodeClass);
         ParentRegistry.registerTree(ret);
         return ret;
     }
 
     @Override
     public Node copy(Kryo kryo, Node original) {
         try {
             Object root = kryo.getContext().get("root");
             if (root == null) {
                 // original is the root
                 kryo.getContext().put("root", original);
                 root = original;
             }
             Serializer<Node> serializer = new FieldSerializer<>(kryo, original.getClass());
             Node ret = serializer.copy(kryo, original);
             if (root == original) {
                // Register tree only from the root (should save some work)
                 ParentRegistry.registerTree(ret);
             }
             return ret;
         } finally {
             kryo.getContext().remove("root");
         }
     }
 }
