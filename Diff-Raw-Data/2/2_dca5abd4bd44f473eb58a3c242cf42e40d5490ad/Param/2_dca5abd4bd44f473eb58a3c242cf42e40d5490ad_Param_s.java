 /*
 Copyright (c) 2013 Robby, Kansas State University.        
 All rights reserved. This program and the accompanying materials      
 are made available under the terms of the Eclipse Public License v1.0 
 which accompanies this distribution, and is available at              
 http://www.eclipse.org/legal/epl-v10.html                             
 */
 
 package edu.ksu.cis.santos.mdcf.dml.ast.exp;
 
 import com.google.common.base.Optional;
 
 import edu.ksu.cis.santos.mdcf.dml.ast.AstNode;
 import edu.ksu.cis.santos.mdcf.dml.ast.IVisitor;
 import edu.ksu.cis.santos.mdcf.dml.ast.Type;
 
 /**
  * @author <a href="mailto:robby@k-state.edu">Robby</a>
  */
 public final class Param extends AstNode {
   public final String name;
   public final Optional<Type> type;
 
   public Param(final Optional<Type> type, final String name) {
     this.type = type;
     this.name = name.intern();
   }
 
   @Override
   protected Object[] getChildren() {
    return new Object[] { this.name, this.type };
   }
 
   @Override
   protected boolean visit(final IVisitor visitor) {
     return visitor.visitParam(this);
   }
 }
