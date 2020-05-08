 // Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
 // for details. All rights reserved. Use of this source code is governed by a
 // BSD-style license that can be found in the LICENSE file.
 
 package com.google.dart.compiler.backend.js.ast;
 
 import java.util.List;
 
 /**
  * Represents a JavaScript block in the global scope
  */
 public class JsGlobalBlock extends JsBlock {
     public JsGlobalBlock(JsNode node) {
         super(node);
     }
 
     public JsGlobalBlock(List<JsNode> nodes) {
         super(nodes);
     }
 
     @Override
     public boolean isGlobalBlock() {
         return true;
     }
}
