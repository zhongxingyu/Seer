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
 
 package polyglot.ext.jedd.types;
 import polyglot.types.*;
 import polyglot.ext.jl.types.*;
 import polyglot.util.*;
 import java.util.*;
 
 public class BDDType_c extends ReferenceType_c implements BDDType {
     private List domainPairs = new LinkedList();
     public List domainPairs() { return domainPairs; }
     BDDType_c( TypeSystem ts, List domainPairs ) {
         super(ts);
         this.domainPairs = domainPairs;
        HashSet seen = new HashSet();
        for( Iterator domainsIt = domainPairs.iterator(); domainsIt.hasNext(); ) {
            final Type[] domains = (Type[]) domainsIt.next();
            if( domains[0] != null ) {
                if( !seen.add(domains[0]) ) {
                    throw new RuntimeException( "bad type: "+domainPairs );
                }
            }
            if( domains[1] != null ) {
                if( !seen.add(domains[1]) ) {
                    throw new RuntimeException( "bad type: "+domainPairs );
                }
            }
        }
     }
     public String toString() {
         StringBuffer ret = new StringBuffer();
         ret.append("<");
         for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
             final Type[] pair = (Type[]) pairIt.next();
             if( pair[0] == null ) 
                 ret.append( "<unknown>" );
             else
                 ret.append( pair[0].toString() );
             if( pair[1] != null ) {
                 ret.append(":");
                 ret.append( pair[1].toString() );
             }
             if( pairIt.hasNext() ) ret.append(", ");
         }
         ret.append(">");
         return ret.toString();
     }
     public String translate(Resolver c) {
         return toString();
     }
     public boolean isCastValidImpl(Type toType) {
         return isImplicitCastValidImpl(toType);
     }
     public boolean isImplicitCastValidImpl(Type toType) {
         if( toType instanceof NullType ) return false;
         if( descendsFromImpl(toType) ) return true;
         if( !( toType instanceof BDDType ) ) return false;
         BDDType to = (BDDType) toType;
         if( map().keySet().equals( to.map().keySet() ) ) return true;
         if( map().keySet().isEmpty() ) return true;
         if( to.map().keySet().isEmpty() ) return true;
         return false;
     }
     public Map map() {
         HashMap ret = new HashMap();
         for( Iterator pairIt = domainPairs.iterator(); pairIt.hasNext(); ) {
             final Type[] pair = (Type[]) pairIt.next();
             ret.put( pair[0], pair[1] );
         }
         return ret;
     }
     public boolean isCanonical() {
         return true;
     }
     public List interfaces() { return new LinkedList(); }
     public List fields() { return new LinkedList(); }
     public List methods() { return new LinkedList(); }
     public Type superType() { 
         return jeddRelation();
     }
     public boolean descendsFromImpl(Type ancestor) {
         return ts.equals(ancestor, jeddRelation() ) || ts.equals(ancestor, ts.Object());
     }
     public FieldInstance fieldNamed( String name ) { return null; }
     private Type jeddRelation() {
         try {
             return ts.typeForName( "jedd.Relation" );
         } catch( SemanticException e ) {
             throw new InternalCompilerError( "Couldn't find jedd.Relation "+e );
         }
     }
 }
 
