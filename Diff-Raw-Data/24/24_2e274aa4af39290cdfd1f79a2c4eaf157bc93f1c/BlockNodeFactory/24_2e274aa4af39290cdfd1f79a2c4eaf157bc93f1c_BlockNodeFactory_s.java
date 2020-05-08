 /*
  * Copyright 2009 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.html;
 
 import com.google.jstestdriver.html.HtmlDocParser.NodeFactory;
 
 /**
  * Creates TextNodes from a TokenStream.
  * @author corysmith@google.com (Cory Smith)
  *
  */
 public class BlockNodeFactory implements NodeFactory {
   private static final ConcreteToken BLOCK_START_TOKEN = new ConcreteToken("{".toCharArray());
   private static final ConcreteToken BLOCK_END_TOKEN = new ConcreteToken("}".toCharArray());
   private final NodeFactory[] factories;
 
 
   public BlockNodeFactory(NodeFactory[] factories) {
     this.factories = factories;
   }
 
   public void create(BufferedTokenStream stream, Nodes nodes) {
     if (!BLOCK_START_TOKEN.equals(stream.read())) {
       stream.reset();
       return;
     }
     nodes.add(new TextNode(BLOCK_START_TOKEN));
     stream.mark();
     while(stream.available()) {
       Token token = stream.read();
       if (BLOCK_END_TOKEN.equals(token)) {
         stream.mark();
         nodes.add(new TextNode(BLOCK_END_TOKEN));
         return;
       }
       if (BLOCK_START_TOKEN.equals(token)) {
         stream.reset();
         create(stream, nodes);
         return;
       }
       stream.reset();
       for (NodeFactory factory : factories) {
         factory.create(stream, nodes);
       }
     }
   }
 }
