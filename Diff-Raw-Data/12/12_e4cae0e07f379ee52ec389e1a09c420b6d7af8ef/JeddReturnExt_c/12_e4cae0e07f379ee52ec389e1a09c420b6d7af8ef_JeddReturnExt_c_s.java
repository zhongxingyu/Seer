 /* Jedd - A language for implementing relations using BDDs
  * Copyright (C) 2003 Ondrej Lhotak
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package polyglot.ext.jedd.extension;
 
 import polyglot.ext.jl.ast.*;
 import polyglot.ext.jedd.ast.*;
 import polyglot.ext.jedd.types.*;
 import polyglot.ext.jedd.visit.*;
 import polyglot.types.*;
 import polyglot.ast.*;
 import polyglot.util.*;
 import polyglot.visit.*;
 import java.util.*;
 
 public class JeddReturnExt_c extends JeddExt_c implements JeddPhysicalDomains, JeddTypeCheck
 {
     public Node typeCheck(TypeChecker tc) throws SemanticException {
         JeddTypeSystem ts = (JeddTypeSystem) tc.typeSystem();
         JeddNodeFactory nf = (JeddNodeFactory) tc.nodeFactory();
 
        Return n = (Return) node();
        if( n.expr() == null ) return n.typeCheck(tc);
         Type t = n.expr().type();
        if( !(t instanceof BDDType ) ) return n.typeCheck(tc);
         n = n.expr( (Expr) nf.FixPhys( n.expr().position(), n.expr() ).typeCheck(tc) );
         return n;
     }
     public Node generateJava( JeddTypeSystem ts, JeddNodeFactory nf ) throws SemanticException {
         Return n = (Return) node();
 
         if( n.expr() == null ) return n;
         if( !( n.expr().type() instanceof BDDType ) ) return n;
 
         return n.expr( newRelation( ts, nf, (BDDType) n.expr().type(), n.expr() ) );
     }
 
     public Node physicalDomains( PhysicalDomains pd ) throws SemanticException {
         JeddTypeSystem ts = pd.jeddTypeSystem();
 
         Return n = (Return) node();
 
         if( n.expr() == null ) return n;
         if( !( n.expr().type() instanceof BDDType ) ) return n;
 
         MethodInstance mi = (MethodInstance) pd.context().currentCode();
         BDDType type = (BDDType) n.expr().type();
         for( Iterator domainIt = type.map().keySet().iterator(); domainIt.hasNext(); ) {
             final Type domain = (Type) domainIt.next();
             ts.addMustEqualEdge( DNode.v( n.expr(), domain ), DNode.v( mi, domain ) );
         }
 
         return n;
     }
 }
 
