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
 
 package polyglot.ext.jedd.ast;
 
 import polyglot.ast.*;
 import polyglot.types.*;
 import polyglot.ext.jl.ast.*;
 import polyglot.ext.jedd.types.*;
 import polyglot.ext.jedd.extension.*;
 import polyglot.util.*;
 import java.util.*;
 
 /**
  * NodeFactory for jedd extension.
  */
 public class JeddNodeFactory_c extends NodeFactory_c implements JeddNodeFactory {
     public JeddNodeFactory_c() {
         super(new JeddExtFactory_c(), new JeddDelFactory_c());
     }
     protected JeddNodeFactory_c(ExtFactory extFact) {
         super(extFact);
     }
 
     public BDDTypeNode BDDTypeNode( Position pos, List domainPairs ) {
         return new BDDTypeNode_c( pos, domainPairs );
     }
     public Replace Replace( Position pos, Expr expr, List domainPairs ) {
         Node ret = new Replace_c( pos, expr, domainPairs );
         return (Replace) ret;
     }
     public FixPhys FixPhys( Position pos, Expr expr ) {
         Node ret = new FixPhys_c( pos, expr );
         return (FixPhys) ret;
     }
     public Join Join( Position pos, Expr lhs, Expr rhs, List ldomains, List rdomains ) {
         Node ret = new Join_c( pos, lhs, rhs, ldomains, rdomains );
         return (Join) ret;
     }
     public Compose Compose( Position pos, Expr lhs, Expr rhs, List ldomains, List rdomains ) {
         Node ret = new Compose_c( pos, lhs, rhs, ldomains, rdomains );
         return (Compose) ret;
     }
     public BDDLit BDDLit( Position pos, List pieces ) {
         Node ret = new BDDLit_c(pos, pieces);
         return (BDDLit) ret;
     }
     public BDDLitPiece BDDLitPiece( Position pos, Expr e, TypeNode domain, TypeNode phys ) {
         return new BDDLitPiece_c(pos, e, domain, phys);
     }
     public BDDTrueFalse BDDTrueFalse( Position pos, boolean value ) {
         Node ret = new BDDTrueFalse_c(pos, value );
         return (BDDTrueFalse) ret;
     }
     public ClassDecl ClassDeclDomain( Position pos, Flags flags, String name, TypeNode superClass, List interfaces, ClassBody body, IntLit bits, TypeSystem typeSys ) {
         JeddTypeSystem ts = (JeddTypeSystem) typeSys;
         body = body.addMember(
                 FieldDecl( pos,
                     Flags.PUBLIC.set( Flags.FINAL ),
                     CanonicalTypeNode(pos, ts.Int()),
                     "bits",
                     bits ) );
         body = body.addMember(
                 MethodDecl( pos,
                     Flags.PUBLIC.set( Flags.STATIC ),
                     CanonicalTypeNode(pos, ts.domain()),
                     "v",
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Block( pos,
                         Return( pos, AmbExpr( pos, "instance" ) )
                         )
                     )
                 );
         body = body.addMember(
                 FieldDecl( pos,
                     Flags.PRIVATE.set( Flags.STATIC ),
                     CanonicalTypeNode(pos, ts.domain()),
                     "instance",
                     New( pos, AmbTypeNode(pos, name), Collections.EMPTY_LIST )
                     )
                 );
 
         return ClassDecl(pos, flags, name, superClass, interfaces, body);
     }
     public ClassDecl ClassDeclAttribute( Position pos, Flags flags, String name, TypeNode superClass, List interfaces, ClassBody body, TypeNode domain, TypeSystem typeSys ) {
         JeddTypeSystem ts = (JeddTypeSystem) typeSys;
         body = body.addMember(
                 FieldDecl( pos,
                     Flags.PUBLIC.set( Flags.FINAL ),
                     domain,
                     "domain",
                     Cast( pos, domain, Call( pos, domain, "v" ) )) );
         body = body.addMember(
                 MethodDecl( pos,
                     Flags.PUBLIC,
                     CanonicalTypeNode(pos, ts.domain()),
                     "domain",
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Block( pos,
                         Return( pos, AmbExpr( pos, "domain" ) )
                         )
                     )
                 );
         body = body.addMember(
                 MethodDecl( pos,
                     Flags.PUBLIC.set( Flags.STATIC ),
                     CanonicalTypeNode(pos, ts.attribute()),
                     "v",
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Block( pos,
                         Return( pos, AmbExpr( pos, "instance" ) )
                         )
                     )
                 );
         body = body.addMember(
                 FieldDecl( pos,
                     Flags.PRIVATE.set( Flags.STATIC ),
                     CanonicalTypeNode(pos, ts.attribute()),
                     "instance",
                     New( pos, AmbTypeNode(pos, name), Collections.EMPTY_LIST )
                     )
                 );
 
         return ClassDecl(pos, flags, name, superClass, interfaces, body);
     }
     public ClassDecl ClassDeclPhysDom( Position pos, Flags flags, String name, TypeNode superClass, List interfaces, ClassBody body, TypeSystem typeSys ) {
         JeddTypeSystem ts = (JeddTypeSystem) typeSys;
         body = body.addMember(
                 MethodDecl( pos,
                     Flags.PUBLIC,
                     CanonicalTypeNode(pos, ts.Int()),
                     "bits",
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Block( pos,
                        Return( pos, IntLit( pos, IntLit.INT, 0 ) )
                         )
                     )
                 );
         body = body.addMember(
                 MethodDecl( pos,
                     Flags.PUBLIC.set( Flags.STATIC ),
                     CanonicalTypeNode(pos, ts.physicalDomain()),
                     "v",
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Block( pos,
                         Return( pos, AmbExpr( pos, "instance" ) )
                         )
                     )
                 );
         body = body.addMember(
                 FieldDecl( pos,
                     Flags.PRIVATE.set( Flags.STATIC ),
                     CanonicalTypeNode(pos, ts.physicalDomain()),
                     "instance",
                     New( pos, AmbTypeNode(pos, name), Collections.EMPTY_LIST )
                     )
                 );
 
         return ClassDecl(pos, flags, name, superClass, interfaces, body);
     }
 }
