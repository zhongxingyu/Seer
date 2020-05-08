 /*
 Copyright (c) 2013 Robby, Kansas State University.        
 All rights reserved. This program and the accompanying materials      
 are made available under the terms of the Eclipse Public License v1.0 
 which accompanies this distribution, and is available at              
 http://www.eclipse.org/legal/epl-v10.html                             
 */
 
 package edu.ksu.cis.santos.mdcf.dml.ast;
 
 import static edu.ksu.cis.santos.mdcf.dml.ast.Ast.list;
 
 import java.util.List;
 
 /**
  * @author <a href="mailto:robby@k-state.edu">Robby</a>
  */
 public final class Requirement extends Declaration {
  public final List<Member> members;
 
  public Requirement(final String name, final Iterable<Member> members) {
     super(name);
    this.members = list(members);
   }
 
   @Override
   protected Object[] getChildren() {
    return new Object[] { this.name, this.members };
   }
 
   @Override
   protected boolean visit(final IVisitor visitor) {
     final boolean b1 = visitor.visitDeclaration(this);
     final boolean b2 = visitor.visitRequirement(this);
     return b1 && b2;
   }
 }
