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
 
 import polyglot.ext.jedd.types.*;
 import polyglot.ext.jedd.visit.*;
 import polyglot.ext.jl.ast.*;
 import polyglot.types.*;
 import polyglot.ast.*;
 import polyglot.util.*;
 import polyglot.visit.*;
 import java.util.*;
 
 public abstract class Relprod_c extends Expr_c implements Relprod, JeddGenerateJava, JeddPhysicalDomains
 {
     Expr lhs;
     Expr rhs;
     List ldomains;
     List rdomains;
     public Expr lhs() { return lhs; }
     public Expr rhs() { return rhs; }
     public List ldomains() { return ldomains; }
     public List rdomains() { return rdomains; }
     public Relprod_c(Position pos, Expr lhs, Expr rhs, List ldomains, List rdomains ) {
         super( pos );
         this.lhs = lhs;
         this.rhs = rhs;
         this.ldomains = ldomains;
         this.rdomains = rdomains;
     }
     public Node visitChildren(NodeVisitor v) {
         boolean changed = false;
 
         Relprod_c ret = (Relprod_c) copy();
         ret.lhs = (Expr) visitChild( lhs, v );
         ret.rhs = (Expr) visitChild( rhs, v );
 
         if( ret.lhs != lhs ) changed = true;
         if( ret.rhs != rhs ) changed = true;
 
         ret.ldomains = new LinkedList();
         for( Iterator domainIt = ldomains.iterator(); domainIt.hasNext(); ) {
             final TypeNode domain = (TypeNode) domainIt.next();
             TypeNode newDomain = (TypeNode) visitChild( domain, v );
             ret.ldomains.add( newDomain );
             if( newDomain != domain ) changed = true;
         }
         ret.rdomains = new LinkedList();
         for( Iterator domainIt = rdomains.iterator(); domainIt.hasNext(); ) {
             final TypeNode domain = (TypeNode) domainIt.next();
             TypeNode newDomain = (TypeNode) visitChild( domain, v );
             ret.rdomains.add( newDomain );
             if( newDomain != domain ) changed = true;
         }
         if( changed ) return ret;
         return this;
     }
     public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
         w.begin(0);
         lhs.prettyPrint( w, tr );
         w.write(" { ");
         for( Iterator domainIt = ldomains.iterator(); domainIt.hasNext(); ) {
             final TypeNode domain = (TypeNode) domainIt.next();
             domain.prettyPrint( w, tr );
             if( domainIt.hasNext() ) w.write(", ");
         }
         w.write(" } ");
         w.write(" "+symbol()+" ");
         rhs.prettyPrint( w, tr );
         w.write(" { ");
         for( Iterator domainIt = rdomains.iterator(); domainIt.hasNext(); ) {
             final TypeNode domain = (TypeNode) domainIt.next();
             domain.prettyPrint( w, tr );
             if( domainIt.hasNext() ) w.write(", ");
         }
         w.write(" } ");
         w.end();
     }
     public Node typeCheck( TypeChecker tc ) throws SemanticException {
 
         if( !( lhs.type() instanceof BDDType) || !( rhs.type() instanceof BDDType ) ) {
            throw new SemanticException( "Arguments of join or composition must be BDDs." );
         }
 
         JeddTypeSystem ts = (JeddTypeSystem) tc.typeSystem();
         JeddNodeFactory nf = (JeddNodeFactory) tc.nodeFactory();
 
         lhs = (Expr) nf.FixPhys( lhs.position(), lhs ).typeCheck( tc );
         rhs = (Expr) nf.FixPhys( rhs.position(), rhs ).typeCheck( tc );
 
         Map lmap = ((BDDType) lhs.type()).map();
         Map rmap = ((BDDType) rhs.type()).map();
 
         if( ldomains.size() != rdomains.size() ) {
             throw new SemanticException( "Lists of domains to be equated are of different lengths" );
         }
 
         HashSet seenAlready;
         seenAlready = new HashSet();
         for( Iterator domainIt = ldomains.iterator(); domainIt.hasNext(); ) {
             final TypeNode domain = (TypeNode) domainIt.next();
             if( !seenAlready.add(domain.type()) )
                 throw new SemanticException( "Duplicate attribute "+domain+" in attributes to be compared." );
             if( !domain.type().isSubtype( ts.attribute() ) ) 
                 throw new SemanticException( ""+domain+" does not extend jedd.Attribute" );
            if( !lmap.containsKey( domain.type() ) ) {
                throw new SemanticException( "LHS does not have attribute "+domain+"; it has type "+lhs.type() );
            }
         }
         seenAlready = new HashSet();
         for( Iterator domainIt = rdomains.iterator(); domainIt.hasNext(); ) {
             final TypeNode domain = (TypeNode) domainIt.next();
             if( !seenAlready.add(domain.type()) )
                 throw new SemanticException( "Duplicate attribute "+domain+" in attributes to be compared." );
             if( !domain.type().isSubtype( ts.attribute() ) ) 
                 throw new SemanticException( ""+domain+" does not extend jedd.Attribute" );
            if( !rmap.containsKey( domain.type() ) ) {
                throw new SemanticException( "RHS does not have attribute "+domain+"; it has type "+rhs.type() );
            }
         }
 
         // make sure all attributes in result are unique.
         seenAlready = new HashSet();
         List resultingType = new LinkedList();
         for( Iterator domainIt = lmap.keySet().iterator(); domainIt.hasNext(); ) {
             final Type domain = (Type) domainIt.next();
             if( !keepMatchedDomains() 
             &&  isInDomains( domain, ldomains ) ) continue;
             resultingType.add( new Type[] { domain, null } );
             if( !seenAlready.add( domain ) ) {
                 throw new SemanticException( "Resulting type has duplicate attribute "+domain );
             }
         }
         for( Iterator domainIt = rmap.keySet().iterator(); domainIt.hasNext(); ) {
             final Type domain = (Type) domainIt.next();
             if( isInDomains( domain, rdomains ) ) continue;
             resultingType.add( new Type[] { domain, null } );
             if( !seenAlready.add( domain ) ) {
                 throw new SemanticException( "Resulting type has duplicate attribute "+domain );
             }
         }
 
 
         return type( ts.BDDType( resultingType ) );
     }
     public Node physicalDomains( PhysicalDomains pd ) {
         JeddTypeSystem ts = pd.jeddTypeSystem();
 
         Map lmap = ((BDDType) lhs.type()).map();
         Map rmap = ((BDDType) rhs.type()).map();
 
         Iterator lIt = ldomains.iterator();
         Iterator rIt = rdomains.iterator();
         while( lIt.hasNext() ) {
             Type ldomain = ((TypeNode) lIt.next()).type();
             Type rdomain = ((TypeNode) rIt.next()).type();
             ts.addMustEqualEdge( DNode.v( lhs, ldomain ), DNode.v( rhs, rdomain ) );
         }
 
         for( Iterator domainIt = lmap.keySet().iterator(); domainIt.hasNext(); ) {
 
             final Type domain = (Type) domainIt.next();
             if( !keepMatchedDomains()
             &&  isInDomains( domain, ldomains ) ) continue;
             ts.addMustEqualEdge( DNode.v( this, domain ), DNode.v( lhs, domain ) );
         }
         for( Iterator domainIt = rmap.keySet().iterator(); domainIt.hasNext(); ) {
             final Type domain = (Type) domainIt.next();
             if( isInDomains( domain, rdomains ) ) continue;
             ts.addMustEqualEdge( DNode.v( this, domain ), DNode.v( rhs, domain ) );
         }
         return this;
     }
     private boolean isInDomains( Type domain, List domains ) {
         for( Iterator domNodeIt = domains.iterator(); domNodeIt.hasNext(); ) {
             final TypeNode domNode = (TypeNode) domNodeIt.next();
             if( domNode.type().equals( domain ) ) return true;
         }
         return false;
     }
     public Node generateJava( JeddTypeSystem ts, JeddNodeFactory nf ) throws SemanticException {
         Relprod n = (Relprod) node();
 
         List args = new LinkedList();
         Map map = ((BDDType) n.lhs().type()).map();
         for( Iterator domainIt = n.ldomains().iterator(); domainIt.hasNext(); ) {
             final TypeNode domain = (TypeNode) domainIt.next();
             args.add(
                     nf.Call(
                         domain.position(),
                         nf.CanonicalTypeNode(
                             domain.position(),
                             (Type) map.get(domain.type()) ),
                         "v" ) );
         }
 
         Call getJedd = nf.Call( n.position(), nf.CanonicalTypeNode( n.position(), ts.jedd() ), "v"  ); 
 
         return nf.Call( 
                 n.position(),
                 getJedd,
                 function(),
                 nf.Call( n.position(), getJedd, "read", n.lhs() ),
                 n.rhs(),
                 nf.NewArray(
                     n.position(),
                     nf.CanonicalTypeNode( n.position(), ts.physicalDomain() ),
                     new LinkedList(),
                     1,
                     nf.ArrayInit( n.position(), args )
                     )
                 ).type( n.type() );
     }
     protected abstract String symbol();
     protected abstract String function();
     protected abstract boolean keepMatchedDomains();
     public Term entry() {
         return lhs().entry();
     }
     public List acceptCFG(CFGBuilder v, List succs) {
         v.visitCFG(lhs(), rhs().entry());
         v.visitCFG(rhs(), this);
         return succs;
     }
 }
 
