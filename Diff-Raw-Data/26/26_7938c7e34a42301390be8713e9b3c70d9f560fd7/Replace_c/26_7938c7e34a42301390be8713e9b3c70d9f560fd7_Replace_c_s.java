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
 
 import polyglot.ext.jedd.extension.*;
 import polyglot.ext.jedd.types.*;
 import polyglot.ext.jedd.visit.*;
 import polyglot.ext.jl.ast.*;
 import polyglot.types.*;
 import polyglot.ast.*;
 import polyglot.util.*;
 import polyglot.visit.*;
 import java.util.*;
 
 public class Replace_c extends Expr_c implements Replace, JeddGenerateJava, JeddPhysicalDomains
 {
     private List domainPairs;
     private Expr expr;
     public Expr expr() { return expr; }
     public List domainPairs() { return domainPairs; }
     public Replace_c(Position pos, Expr expr, List domainPairs ) {
         super( pos );
         this.expr = expr;
         this.domainPairs = domainPairs;
     }
     public Node visitChildren(NodeVisitor v) {
         boolean changed = false;
         Replace_c ret = (Replace_c) copy();
 
         ret.expr = (Expr) visitChild( expr, v );
         if( ret.expr != expr ) changed = true;
 
         List newDomains = new LinkedList();
         for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
             final TypeNode[] pair = (TypeNode[]) pairIt.next();
             TypeNode[] newPair = { (TypeNode) visitChild( pair[0], v ),
                                    (TypeNode) visitChild( pair[1], v ) };
             if( newPair[0] != pair[0] ) changed = true;
             if( newPair[1] != pair[1] ) changed = true;
             newDomains.add( newPair );
         }
         ret.domainPairs = newDomains;
         if( !changed ) return this;
         return ret;
     }
     public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
         w.begin(0);
         w.write("(");
         for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
             final TypeNode[] pair = (TypeNode[]) pairIt.next();
             print(pair[0],w,tr);
             w.write("=>");
             print(pair[1],w,tr);
             if( pairIt.hasNext() ) {
                 w.write(",");
                 w.allowBreak(0, " ");
             }
         }
         w.write(")");
         w.allowBreak(2, " ");
         printSubExpr(expr, w, tr);
         w.end();
     }
     public Node typeCheck(TypeChecker tc) throws SemanticException {
         JeddTypeSystem ts = (JeddTypeSystem) tc.typeSystem();
         JeddNodeFactory nf = (JeddNodeFactory) tc.nodeFactory();
 
         if( !( expr.type() instanceof BDDType) ) {
             throw new SemanticException( "Argument of replace must be a BDD.",
                     expr.position() );
         }
 
         expr = (Expr) nf.FixPhys( expr.position(), expr ).typeCheck(tc);
 
         boolean physDom = false;
         boolean attribute = false;
 
         BDDType exprType = (BDDType) expr.type();
         Map exprMap = exprType.map();
         for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
             final TypeNode[] pair = (TypeNode[]) pairIt.next();
             if( !pair[0].type().isSubtype( ts.attribute() ) ) {
                 throw new SemanticException( "Attempt to replace a non-attribute" );
             }
             if( !exprMap.containsKey( pair[0].type() ) ) {
                 throw new SemanticException( "Argument of replace doesn't have attribute "+pair[0].type() );
             }
             if( pair[1] == null || pair[1].type().isSubtype( ts.attribute() ) ) {
                 attribute = true;
             } else if( pair[1].type().isSubtype( ts.physicalDomain() ) ) {
                 physDom = true;
             } else {
                 throw new SemanticException( "Argument of replace is neither"+
                         " an attribute nor physical domain: "+pair[1] );
             }
         }
         if( physDom && attribute ) {
             throw new SemanticException( "Cannot replace to both an attribute "+
                     " and physical domain in a single replace." );
         }
 
         Set seenAlready = new HashSet();
         List newDomains = new LinkedList();
 
         if( physDom ) {
             for( Iterator exprPairIt = exprType.domainPairs().iterator(); exprPairIt.hasNext(); ) {
                 final Type[] exprPair = (Type[]) exprPairIt.next();
                 Type[] newDomain = new Type[2];
                 newDomain[0] = exprPair[0];
                 for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
                     final TypeNode[] pair = (TypeNode[]) pairIt.next();
                     if( pair[0] == null ) continue;
                     if( !pair[0].type().equals( exprPair[0] ) ) continue;
                     if( newDomain[1] != null ) {
                         throw new SemanticException( "Multiple physical domains specified for domain "+pair[0].type() );
                     }
                     newDomain[1] = pair[1].type();
                 }
                 newDomains.add( newDomain );
                 if( newDomain[1] != null ) {
                     if( !seenAlready.add( newDomain[1] ) ) {
                         throw new SemanticException( "Resulting type has duplicate physical domain "+newDomain[0] );
                     }
                 }
             }
         } else {
             // attribute
             for( Iterator exprPairIt = exprType.domainPairs().iterator(); exprPairIt.hasNext(); ) {
                 final Type[] exprPair = (Type[]) exprPairIt.next();
                 boolean isProjectedOut = false;
                 boolean isReplaced = false;
                 for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
                     final TypeNode[] pair = (TypeNode[]) pairIt.next();
                     if( pair[0] == null ) continue;
                     if( !pair[0].type().equals( exprPair[0] ) ) continue;
                     if( pair[1] == null ) {
                         isProjectedOut = true;
                         continue;
                     }
                     if( isProjectedOut ) {
                         throw new SemanticException( "Attribute "+pair[0]+" is"+
                                 " both projected out and replaced." );
                     }
                     Type[] newDomain = new Type[2];
                     newDomain[0] = pair[1].type();
                     newDomains.add( newDomain );
                     isReplaced = true;
                     if( !seenAlready.add( newDomain[0] ) ) {
                         throw new SemanticException( "Resulting type has"+
                                 " duplicate attribute "+newDomain[0] );
                     }
                 }
                 if( !isReplaced && !isProjectedOut ) {
                     Type[] newDomain = new Type[2];
                     newDomain[0] = exprPair[0];
                     newDomains.add( newDomain );
                 }
             }
         }

         return type( ts.BDDType( newDomains ) );
     }
     public Node physicalDomains( PhysicalDomains pd ) {
         JeddTypeSystem ts = pd.jeddTypeSystem();
 
         BDDType exprType = (BDDType) expr.type();
         Map exprMap = exprType.map();
 
         TypeNode[] firstPair = (TypeNode[]) domainPairs.iterator().next();
         if( firstPair[1] != null 
         &&  firstPair[1].type().isSubtype( ts.physicalDomain() ) ) {
             for( Iterator attrIt = exprMap.keySet().iterator(); attrIt.hasNext(); ) {
                 final Type attr = (Type) attrIt.next();
                 ts.addMustEqualEdge( DNode.v( expr, attr ),
                                      DNode.v( this, attr ) );
             }
         } else {
             for( Iterator attrIt = exprMap.keySet().iterator(); attrIt.hasNext(); ) {
                 final Type attr = (Type) attrIt.next();
                 boolean staysTheSame = true;
                 for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
                     final TypeNode[] pair = (TypeNode[]) pairIt.next();
                     if( !pair[0].type().equals( attr ) ) continue;
                     staysTheSame = false;
                     if( pair[1] != null ) {
                         ts.addAssignEdge( DNode.v( expr, attr ),
                                           DNode.v( this, pair[1].type() ) );
                     }
                 }
                 if( staysTheSame ) {
                     ts.addAssignEdge( DNode.v( expr, attr ),
                                       DNode.v( this, attr ) );
                 }
             }
         }
 
         return this;
     }
 
     private List turnIntoCalls( List in, JeddNodeFactory nf, Position p ) {
         List out = new ArrayList();
         for( Iterator physIt = in.iterator(); physIt.hasNext(); ) {
             final Type phys = (Type) physIt.next();
             out.add( nf.Call( p, nf.CanonicalTypeNode( p, phys ), "v"  ) ); 
         }
         return out;
     }
 
     public Node generateJava( JeddTypeSystem ts, JeddNodeFactory nf ) throws SemanticException {
         TypeNode[] firstPair = (TypeNode[]) domainPairs.iterator().next();
         if( firstPair[1] != null 
         &&  firstPair[1].type().isSubtype( ts.physicalDomain() ) ) {
             return expr();
         }
 
         Position p = position();
 
         BDDType type = (BDDType) type();
         BDDType exprType = (BDDType) expr().type();
         Map exprMap = exprType.map();
         Map thisMap = type.map();
 
         // First make up the replaces map. This contains a key for each
         // attribute of expr(), and each value is a set of the corresponding
         // attributes in the result.
         Map replaces = new HashMap();
 
         // Map each attribute to itself
         for( Iterator pairIt = exprType.domainPairs().iterator(); pairIt.hasNext(); ) {
             final Type[] pair = (Type[]) pairIt.next();
             HashSet set = new HashSet();
             set.add( pair[0] );
             replaces.put( pair[0], set );
         }
         
         // Remove the identity mappings for any replaced attributes
         for( Iterator repPairIt = domainPairs().iterator(); repPairIt.hasNext(); ) {
             final TypeNode[] repPair = (TypeNode[]) repPairIt.next();
             replaces.put( repPair[0].type(), new HashSet() );
         }
 
         // Add in mappings for replaced attributes
         for( Iterator repPairIt = domainPairs().iterator(); repPairIt.hasNext(); ) {
             final TypeNode[] repPair = (TypeNode[]) repPairIt.next();
             HashSet set = (HashSet) replaces.get( repPair[0].type() );
             if( repPair[1] != null ) set.add( repPair[1].type() );
         }
 
         List from = new LinkedList();
         List to = new LinkedList();
         Map beforeCopiesMap = new HashMap();
         List project = new LinkedList();
         List copyFrom = new LinkedList();
         List copyTo = new LinkedList();
 
         // For each attribute, if it's mapped to the empty set, project
         // it away and remove from the map.
         for( Iterator attrIt = (new ArrayList(replaces.keySet())).iterator(); attrIt.hasNext(); ) {
             final Type attr = (Type) attrIt.next();
             HashSet set = (HashSet) replaces.get( attr );
             if( set.isEmpty() ) {
                 project.add( exprMap.get( attr ) );
                 replaces.remove( attr );
             }
         }
 
         // For each attribute, see if it's mapped to some attribute on the
         // same physical domain. If it is, remove it. Otherwise, insert an
         // appropriate replace and remove it.
 outer:
         for( Iterator attrIt = (new ArrayList(replaces.keySet())).iterator(); attrIt.hasNext(); ) {
             final Type attr = (Type) attrIt.next();
             HashSet set = (HashSet) replaces.get( attr );
             for( Iterator toAttrIt = set.iterator(); toAttrIt.hasNext(); ) {
                 final Type toAttr = (Type) toAttrIt.next();
                 Type toPhys = (Type) thisMap.get( toAttr );
                 if( exprMap.get(attr).equals( toPhys ) ) {
                     set.remove( toAttr );
                     beforeCopiesMap.put( attr, toPhys );
                     continue outer;
                 }
             }
             Type toAttr = (Type) set.iterator().next();
             Type toPhys = (Type) thisMap.get( toAttr );
             set.remove( toAttr );
             beforeCopiesMap.put( attr, toPhys );
             from.add( exprMap.get(attr) );
             to.add( toPhys );
         }
 
         // Now the only things left are copies
         for( Iterator attrIt = (new ArrayList(replaces.keySet())).iterator(); attrIt.hasNext(); ) {
             final Type attr = (Type) attrIt.next();
             HashSet set = (HashSet) replaces.get( attr );
             if( set.isEmpty() ) continue;
             for( Iterator toAttrIt = set.iterator(); toAttrIt.hasNext(); ) {
                 final Type toAttr = (Type) toAttrIt.next();
                 copyFrom.add( beforeCopiesMap.get( attr ) );
                 copyTo.add( thisMap.get( toAttr ) );
             }
         }
 
         project = turnIntoCalls( project, nf, p );
         from = turnIntoCalls( from, nf, p );
         to = turnIntoCalls( to, nf, p );
         copyFrom = turnIntoCalls( copyFrom, nf, p );
         copyTo = turnIntoCalls( copyTo, nf, p );
 
         Expr ret = expr().type(type);
         Call getJedd = null;
         if( !project.isEmpty() ) {
             if( getJedd == null ) {
                 getJedd = nf.Call( p, nf.CanonicalTypeNode( p, ts.jedd() ), "v"  ); 
             }
 
             ret = nf.Call( 
                     p,
                     getJedd,
                     "project",
                     ret,
                     nf.NewArray(
                         p,
                         nf.CanonicalTypeNode( p, ts.physicalDomain() ),
                         new LinkedList(),
                         1,
                         nf.ArrayInit( p, project )
                         )
                     ).type( type );
         }
 
         if( !from.isEmpty() ) {
             if( getJedd == null ) {
                 getJedd = nf.Call( p, nf.CanonicalTypeNode( p, ts.jedd() ), "v"  ); 
             }
 
             ret = nf.Call( 
                     p,
                     getJedd,
                     "replace",
                     ret,
                     nf.NewArray(
                         p,
                         nf.CanonicalTypeNode( p, ts.physicalDomain() ),
                         new LinkedList(),
                         1,
                         nf.ArrayInit( p, from )
                         ),
                     nf.NewArray(
                         p,
                         nf.CanonicalTypeNode( p, ts.physicalDomain() ),
                         new LinkedList(),
                         1,
                         nf.ArrayInit( p, to )
                         )
                     ).type( type );
         }
         
         if( !copyFrom.isEmpty() ) {
             if( getJedd == null ) {
                 getJedd = nf.Call( p, nf.CanonicalTypeNode( p, ts.jedd() ), "v"  ); 
             }
 
             ret = nf.Call( 
                     p,
                     getJedd,
                     "copy",
                     ret,
                     nf.NewArray(
                         p,
                         nf.CanonicalTypeNode( p, ts.physicalDomain() ),
                         new LinkedList(),
                         1,
                         nf.ArrayInit( p, copyFrom )
                         ),
                     nf.NewArray(
                         p,
                         nf.CanonicalTypeNode( p, ts.physicalDomain() ),
                         new LinkedList(),
                         1,
                         nf.ArrayInit( p, copyTo )
                         )
                     ).type( type );
         }
         return ret;
     }
     public Term entry() {
         return expr.entry();
     }
     public List acceptCFG(CFGBuilder v, List succs) {
         v.visitCFG(expr(), this);
         return succs;
     }
 }
 
