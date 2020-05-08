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
 
 import polyglot.ast.*;
 import polyglot.types.*;
 import polyglot.ext.jedd.ast.*;
 import polyglot.ext.jl.ast.*;
 import polyglot.ext.jl.types.*;
 import polyglot.util.*;
 import polyglot.visit.*;
 import polyglot.frontend.*;
 import java.util.*;
 import java.io.*;
 //import polyglot.ext.jedd.cudd.*;
 
 
 public class PhysDom {
     final static boolean INCLUDE_COMMENTS = false;
 
     private static PhysDom instance = new PhysDom();
     public static PhysDom v() { return instance; }
 
     public Map domainAssignment = new HashMap();
 
     public Set assignEdges = new HashSet();
     public Set mustEqualEdges = new HashSet();
     public Set allPhys = new HashSet();
 
     private String satSolver = System.getProperty("user.home")+System.getProperty("file.separator")+"zchaff.2003.10.9.linux";
     private String satCore = System.getProperty("user.home")+System.getProperty("file.separator")+"zcore";
     public void setSatSolver( String s ) { satSolver = s; }
     public void setSatCore( String s ) { satCore = s; }
 
     static interface HasNum {
         public int getNum();
     }
 
     static class SetLit implements HasNum {
         Set set;
         private SetLit( Set set ) {
             this.set = set;
         }
         public static SetLit v( Set set ) {
             SetLit ret = new SetLit(set);
             SetLit ret2 = (SetLit) setMap.get( ret );
             if( ret2 == null ) {
                 setMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public boolean equals( Object other ) {
             if( !(other instanceof SetLit) ) return false;
             SetLit o = (SetLit) other;
             if( !o.set.equals(set) ) return false;
             return true;
         }
         public int hashCode() { return set.hashCode(); }
         public int getNum() {
             Integer i = (Integer) litNumMap.get(this);
             if( i == null ) litNumMap.put( this, i = new Integer(++nextInt) );
             return i.intValue();
         }
         public String toString() {
             return Integer.toString(getNum());
         }
     }
     static class NegSetLit implements HasNum {
         SetLit set;
         private NegSetLit( SetLit set ) {
             this.set = set;
         }
         private NegSetLit( Set set ) {
             this( SetLit.v( set ) );
         }
         public static NegSetLit v( Set set ) {
             NegSetLit ret = new NegSetLit( set );
             NegSetLit ret2 = (NegSetLit) setMap.get( ret );
             if( ret2 == null ) {
                 setMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public static NegSetLit v( SetLit e ) {
             return v(e.set);
         }
         public boolean equals( Object other ) {
             if( !(other instanceof NegSetLit) ) return false;
             NegSetLit o = (NegSetLit) other;
             if( !o.set.equals(set) ) return false;
             return true;
         }
         public int hashCode() { return set.hashCode() + 1; }
         public int getNum() {
             return -(set.getNum());
         }
         public String toString() {
             return Integer.toString(getNum());
         }
     }
     public static Map setMap = new HashMap();
 
     static class Literal implements HasNum {
         DNode dnode;
         Type phys;
         private Literal( DNode dnode, Type phys ) {
             this.dnode = dnode; this.phys = phys;
         }
         public static Literal v( DNode dnode, Type phys ) {
             Literal ret = new Literal( dnode, phys );
             Literal ret2 = (Literal) litMap.get( ret );
             if( ret2 == null ) {
                 litMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public boolean equals( Object other ) {
             if( !(other instanceof Literal) ) return false;
             Literal o = (Literal) other;
             if( o.dnode != dnode ) return false;
             if( o.phys != phys ) return false;
             return true;
         }
         public int hashCode() { return dnode.hashCode() + phys.hashCode(); }
         public int getNum() {
             Integer i = (Integer) litNumMap.get(this);
             if( i == null ) litNumMap.put( this, i = new Integer(++nextInt) );
             return i.intValue();
         }
         public String toString() {
             return Integer.toString(getNum());
         }
     }
     static int nextInt = 0;
     public static Map litMap = new HashMap();
     public static Map litNumMap = new HashMap();
     
     static class NegLiteral implements HasNum {
         Literal lit;
         private NegLiteral( Literal lit ) {
             this.lit = lit;
         }
         private NegLiteral( DNode dnode, Type phys ) {
             this( Literal.v( dnode, phys ) );
         }
         public static NegLiteral v( DNode dnode, Type phys ) {
             NegLiteral ret = new NegLiteral( dnode, phys );
             NegLiteral ret2 = (NegLiteral) litMap.get( ret );
             if( ret2 == null ) {
                 litMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public boolean equals( Object other ) {
             if( !(other instanceof NegLiteral) ) return false;
             NegLiteral o = (NegLiteral) other;
             if( !o.lit.equals(lit) ) return false;
             return true;
         }
         public int hashCode() { return lit.hashCode() + 1; }
         public int getNum() {
             return -(lit.getNum());
         }
         public String toString() {
             return Integer.toString(getNum());
         }
     }
 
     Set cnf = new HashSet();
 
     public static class Clause extends HashSet {
         public Clause() {
         }
        private boolean isConflict = false;
        public Clause( boolean isConflict ) {
            this.isConflict = isConflict;
        }
        public boolean isConflict() { return isConflict; }
         public void setComment( String comment ) {
             this.comment = comment;
         }
         String comment;
         public String toString() {
             StringBuffer ret = new StringBuffer();
             if( comment != null ) ret.append( "c "+comment+"\n" );
             for( Iterator litIt = this.iterator(); litIt.hasNext(); ) {
                 final Object lit = (Object) litIt.next();
                 ret.append( lit.toString() );
                 ret.append( " " );
             }
             ret.append( "0" );
             return ret.toString();
         }
     }
 
     JeddNodeFactory nf;
     JeddTypeSystem ts;
 
     public void findAssignment( JeddNodeFactory nf, JeddTypeSystem ts, Collection jobs ) throws SemanticException {
 
         this.nf = nf;
         this.ts = ts;
 
         //printDomainsDot();
         //printDomainsRsf();
 
         createLiterals();
 
         createDnodeConstraints();
         setupSpecifiedAssignment();
 
         addAssignEdges();
 
         addMustEqualEdges();
         addConflictEdges();
 
         //tryWithBDDsJustForKicks();
         runSat();
 
         recordPhys(jobs);
 
         printDomainsDot();
         //printDomainsRsf();
 
         printStats();
     }
 
     Set solution = new HashSet();
     public void runSat() throws SemanticException {
         int numvars = (setMap.size()+litMap.size())/2;
         if( numvars == 0 ) return;
         try {
             File tmpFile = File.createTempFile("domainassign",".cnf");
             PrintWriter file = new PrintWriter(
                     new FileOutputStream( tmpFile ) );
             file.println( "p cnf "+numvars+" "+cnf.size() );
             for( Iterator clauseIt = cnf.iterator(); clauseIt.hasNext(); ) {
                 final Clause clause = (Clause) clauseIt.next();
                 file.println( clause.toString() );
             }
             file.close();
             Process p = Runtime.getRuntime().exec(satSolver+" "+tmpFile.getAbsolutePath());
             BufferedReader br = new BufferedReader(new InputStreamReader( p.getInputStream() ) );
             String str;
             String soln = null;
             while ((str = br.readLine()) != null) {
                 if( str.length() < 1000 ) System.out.println( str );
                 boolean hasNum = false;
                 boolean hasBad = false;
                 for( int i = 0; i < str.length(); i++ ) {
                     if( str.charAt(i) == ' ' ) continue;
                     if( str.charAt(i) == '\t' ) continue;
                     if( str.charAt(i) == '-' ) continue;
                     if( str.charAt(i) >= '0' && str.charAt(i) <= '9' ) {
                         hasNum = true;
                         continue;
                     }
                     hasBad = true;
                 }
                 if( hasNum && !hasBad ) {
                     // this looks like the solution
                     if( soln  != null ) throw new RuntimeException( "old solution was "+soln+"; now we got "+str );
                     soln = str;
                 }
             }
             boolean pos[] = new boolean[numvars+1];
             boolean neg[] = new boolean[numvars+1];
             if( soln == null ) {
                 unsatCore( tmpFile );
                 throw new SemanticException( "SAT solver couldn't assign physical domains." );
             }
             StringTokenizer st = new StringTokenizer( soln );
             while( st.hasMoreTokens() ) {
                 String tok = st.nextToken();
                 int i = Integer.parseInt( tok );
                 if( i < 0 ) neg[-i] = true;
                 else pos[i] = true;
             }
             for( int i = 1; i <= numvars; i++ ) {
                 if( neg[i] && pos[i] ) throw new RuntimeException("both for "+i);
                 if( !neg[i] && !pos[i] ) throw new RuntimeException("neither for "+i);
             }
             for( Iterator litIt = litNumMap.keySet().iterator(); litIt.hasNext(); ) {
                 final Object lit = (Object) litIt.next();
                 Integer i = (Integer) litNumMap.get( lit );
                 if( pos[i.intValue()] ) solution.add( lit );
             }
             //tmpFile.delete();
         } catch( IOException e ) {
             throw new RuntimeException( e.toString() );
         }
     }
 
     private void unsatCore( File tmpFile ) {
         System.err.println( "Attemptimg to extract unsat core." );
         try {
             File coreFile = File.createTempFile( "satcore", ".cnf" );
             Process p = Runtime.getRuntime().exec(satCore+" "+tmpFile.getAbsolutePath()+" resolve_trace "+coreFile.getAbsolutePath());
             BufferedReader br = new BufferedReader(new InputStreamReader( p.getInputStream() ) );
             String line;
 
             while ((line = br.readLine()) != null) {
                 System.err.println( line );
             }
             try {
                 p.waitFor();
             } catch( InterruptedException e ) {}
 
             br = new BufferedReader(new InputStreamReader(new FileInputStream( coreFile )));
 
     line:
             while ((line = br.readLine()) != null) {
                 if( Character.isLetter( line.charAt(0) ) ) continue;
                 Integer i1 = null;
                 Integer i2 = null;
                 StringTokenizer tok = new StringTokenizer(line);
                 while( tok.hasMoreTokens() ) {
                     String token = tok.nextToken();
                     Integer i = Integer.decode( token );
                     int ival = i.intValue();
                     if( ival > 0 ) continue line;
                     if( ival == 0 ) {
                         if( i2 == null ) continue line;
                         break;
                     }
                     if( i1 == null ) i1 = i;
                     else if( i2 == null ) i2 = i;
                     else continue line;
                 }
                 // got a conflict clause
     clause:
                 for( Iterator clIt = cnf.iterator(); clIt.hasNext(); ) {
                     final Clause cl = (Clause) clIt.next();
                     if( cl.size() != 2 ) continue;
                     NegLiteral nl1 = null;
                     NegLiteral nl2 = null;
                     for( Iterator litIt = cl.iterator(); litIt.hasNext(); ) {
                         final HasNum lit = (HasNum) litIt.next();
                         if( lit.getNum() != i1.intValue() 
                         && lit.getNum() != i2.intValue() ) continue clause;
                         if( nl1 == null ) nl1 = (NegLiteral) lit;
                         else nl2 = (NegLiteral) lit;
                     }
                    if( !cl.isConflict() ) continue line;
                     System.err.println( "Conflict between\n"+
                             nl1.lit.dnode.toLongString()+"\nand\n"+
                             nl2.lit.dnode.toLongString()+"\nover physical domain "+
                             nl1.lit.phys );
                 }
             }
         } catch( IOException e ) {
             throw new RuntimeException( "Problem extracting unsat core: "+e );
         }
     }
 
 
     /*
     public void tryWithBDDsJustForKicks() {
         int numvars = (setMap.size()+litMap.size())/2;
         System.loadLibrary("jcudd");
         SWIGTYPE_p_DdManager manager = Cudd.Cudd_Init(numvars+1,0,Cudd.CUDD_UNIQUE_SLOTS,Cudd.CUDD_CACHE_SLOTS,0);
 
         SWIGTYPE_p_DdNode bdd = Cudd.Cudd_ReadOne(manager);
         Cudd.Cudd_Ref(bdd);
 
         for( Iterator clauseIt = cnf.iterator(); clauseIt.hasNext(); ) {
 
             final Clause clause = (Clause) clauseIt.next();
 
             SWIGTYPE_p_DdNode clauseBdd = Cudd.Cudd_ReadLogicZero(manager);
             Cudd.Cudd_Ref(clauseBdd);
 
             for( Iterator oIt = clause.iterator(); oIt.hasNext(); ) {
 
                 final Object o = (Object) oIt.next();
                 int i = Integer.parseInt( o.toString() );
 
                 SWIGTYPE_p_DdNode literal = Cudd.Cudd_bddIthVar(manager, i<0?-i:i);
                 if( i < 0 ) literal = Cudd.Cudd_bddNot( literal );
                 Cudd.Cudd_Ref( literal );
 
                 SWIGTYPE_p_DdNode oldClause = clauseBdd;
                 clauseBdd = Cudd.Cudd_bddOr( manager, clauseBdd, literal );
                 Cudd.Cudd_Ref( clauseBdd );
                 Cudd.Cudd_RecursiveDeref( manager, oldClause );
                 Cudd.Cudd_RecursiveDeref( manager, literal );
             }
             SWIGTYPE_p_DdNode oldBdd = bdd;
             bdd = Cudd.Cudd_bddAnd( manager, bdd, clauseBdd );
             Cudd.Cudd_Ref( bdd );
             Cudd.Cudd_RecursiveDeref( manager, oldBdd );
             Cudd.Cudd_RecursiveDeref( manager, clauseBdd );
             System.out.println( "BDD size: "+Cudd.Cudd_DagSize(bdd) );
         }
     }
     */
     
 
     private void createLiterals() {
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             BDDType t = expr.getType();
             Map map = t.map();
             for( Iterator domainIt = map.keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 Type phys = (Type) map.get(domain);
                 if( phys != null ) allPhys.add(phys);
             }
         }
     }
 
     public void createDnodeConstraints() {
         // Each dnode must be assigned to at least one phys
         for( Iterator dnodeIt = DNode.nodes().iterator(); dnodeIt.hasNext(); ) {
             final DNode dnode = (DNode) dnodeIt.next();
             Clause clause = new Clause();
             if( INCLUDE_COMMENTS ) clause.setComment(
                     "[PHYS>=1] At least one phys for "+dnode);
             for( Iterator physIt = allPhys.iterator(); physIt.hasNext(); ) {
                 final Type phys = (Type) physIt.next();
                 clause.add( Literal.v( dnode, phys ) );
             }
             cnf.add( clause );
         }
 
         // Each dnode must be assigned to at most one phys
         for( Iterator dnodeIt = DNode.nodes().iterator(); dnodeIt.hasNext(); ) {
             final DNode dnode = (DNode) dnodeIt.next();
             for( Iterator physIt = allPhys.iterator(); physIt.hasNext(); ) {
                 final Type phys = (Type) physIt.next();
                 for( Iterator phys2It = allPhys.iterator(); phys2It.hasNext(); ) {
                     final Type phys2 = (Type) phys2It.next();
                     if( phys == phys2 ) continue;
                     Clause clause = new Clause();
                     if( INCLUDE_COMMENTS ) clause.setComment(
                             "[PHYS<=1] At most one phys for "+dnode);
                     clause.add( NegLiteral.v( dnode, phys ) );
                     clause.add( NegLiteral.v( dnode, phys2 ) );
                     cnf.add( clause );
                 }
             }
         }
     }
 
     public void addMustEqualEdges() {
         for( Iterator edgeIt = mustEqualEdges.iterator(); edgeIt.hasNext(); ) {
             final DNode[] edge = (DNode[]) edgeIt.next();
             addMustEqualEdge( edge[0], edge[1] );
         }
     }
     private void addMustEqualEdge( DNode node1, DNode node2 ) {
         // (xa ==> ya) /\ (ya ==> xa) = (ya \/ ~xa) /\ (xa \/ ~ya)
         for( Iterator physIt = allPhys.iterator(); physIt.hasNext(); ) {
             final Type phys = (Type) physIt.next();
             Clause clause = new Clause();
             if( INCLUDE_COMMENTS ) clause.setComment(
                     "[MUSTEQUAL] Must equal edge between "+node1+" and "+node2+" for "+phys);
             clause.add( Literal.v( node1, phys ) );
             clause.add( NegLiteral.v( node2, phys ) );
             cnf.add( clause );
 
             clause = new Clause();
             if( INCLUDE_COMMENTS ) clause.setComment( "[MUSTEQUAL] Must equal edge between "+node1+" and "+node2+" for "+phys);
             clause.add( NegLiteral.v( node1, phys ) );
             clause.add( Literal.v( node2, phys ) );
             cnf.add( clause );
         }
     }
 
     public void addConflictEdges() {
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             BDDType t = expr.getType();
             for( Iterator domainIt = t.map().keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 for( Iterator domain2It = t.map().keySet().iterator(); domain2It.hasNext(); ) {
                     final Type domain2 = (Type) domain2It.next();
                     if( domain.equals(domain2) ) continue;
                     addConflictEdge( DNode.v(expr,domain), DNode.v(expr,domain2) );
                 }
             }
         }
     }
 
     static int conflictEdgeCount;
     private void addConflictEdge( DNode node1, DNode node2 ) {
         // (xa ==> ~ya) /\ (ya ==> ~xa) = (~ya \/ ~xa)
         for( Iterator physIt = allPhys.iterator(); physIt.hasNext(); ) {
             final Type phys = (Type) physIt.next();
            Clause clause = new Clause(true);
             if( INCLUDE_COMMENTS ) clause.setComment( 
                     "[CONFLICT] Conflict edge between "+node1.toLongString()+" and "+node2.toLongString()+" for "+phys);
             clause.add( NegLiteral.v( node1, phys ) );
             clause.add( NegLiteral.v( node2, phys ) );
             cnf.add( clause );
         }
         conflictEdgeCount++;
     }
 
     public Type phys(DNode d) {
         BDDExpr expr = d.expr;
         BDDType t = expr.getType();
         Map map = t.map();
         return (Type) map.get(d.dom);
     }
 
     public Type phys(Set s) {
         for( Iterator nodeIt = s.iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             Type ret = phys(node);
             if(ret != null) return ret;
         }
         return null;
     }
 
     public Collection adjacent(DNode d) {
         Set ret = new HashSet();
         for( Iterator edgeIt = assignEdges.iterator(); edgeIt.hasNext(); ) {
             final DNode[] edge = (DNode[]) edgeIt.next();
             if( edge[0] == d ) ret.add(edge[1]);
             if( edge[1] == d ) ret.add(edge[0]);
         }
         for( Iterator edgeIt = mustEqualEdges.iterator(); edgeIt.hasNext(); ) {
             final DNode[] edge = (DNode[]) edgeIt.next();
             if( edge[0] == d ) ret.add(edge[1]);
             if( edge[1] == d ) ret.add(edge[0]);
         }
         return ret;
     }
     
     public void addAssignEdges() throws SemanticException {
         LinkedList worklist = new LinkedList();
         Map pathMap = new HashMap();
 
         // initialize all nodes that have a phys with a path of length 1
         for( Iterator nodeIt = DNode.nodes().iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             Set paths = new HashSet();
             pathMap.put( node, paths );
             if( phys(node) == null ) continue;
             Set path = new HashSet();
             path.add(node);
             paths.add( path );
             worklist.addLast(node);
         }
 
         // do a BFS, extending paths through the graph
         while(!worklist.isEmpty()) {
             DNode node = (DNode) worklist.removeFirst();
             for( Iterator onodeIt = adjacent(node).iterator(); onodeIt.hasNext(); ) {
                 final DNode onode = (DNode) onodeIt.next();
                 if( phys(onode) != null ) continue;
                 boolean changed = false;
 outer:
                 for( Iterator newPathIt = ((Set)pathMap.get(node)).iterator(); newPathIt.hasNext(); ) {              final Set newPath = (Set) newPathIt.next();
                     Set newPath2 = new HashSet(newPath);
                     newPath2.add( onode );
                     for( Iterator oldPathIt = ((Set)pathMap.get(onode)).iterator(); oldPathIt.hasNext(); ) {
                         final Set oldPath = (Set) oldPathIt.next();
                         if( newPath2.containsAll(oldPath) ) continue outer;
                     }
                     ((Set) pathMap.get(onode)).add( newPath2 );
                     changed = true;
                 }
                 if( changed ) worklist.addLast(onode);
             }
         }
 
         // now encode paths as constraints
         for( Iterator nodeIt = pathMap.keySet().iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             Set paths = (Set) pathMap.get(node);
 
             {
                 // at least one path must be active for each node
                 Clause clause = new Clause();
                 if( INCLUDE_COMMENTS ) clause.setComment("[PATH>=1] At least one path for "+node);
                 for( Iterator pathIt = paths.iterator(); pathIt.hasNext(); ) {
                     final Set path = (Set) pathIt.next();
                     clause.add( SetLit.v( path ) );
                 }
                 if( clause.size() == 0 ) {
                     node.expr.throwSemanticException( "No physical domains reaching domain "+node.dom );
                 }
                 cnf.add( clause );
             }
 
             {
                 for( Iterator pathIt = paths.iterator(); pathIt.hasNext(); ) {
                     final Set path = (Set) pathIt.next();
                     // all the nodes in the path must have correct phys
                     Type phys = phys(path);
                     for( Iterator nodeOnPathIt = path.iterator(); nodeOnPathIt.hasNext(); ) {
                         final DNode nodeOnPath = (DNode) nodeOnPathIt.next();
                         // a ==> b /\ c /\ d = (b \/ ~a) /\ (c \/ ~a) /\ (d \/ ~a)
                         Clause clause = new Clause();
                         if(INCLUDE_COMMENTS) clause.setComment("[NODEONPATH] Node "+nodeOnPath+" to node "+node);
                         clause.add( NegSetLit.v(path) );
                         clause.add( Literal.v( nodeOnPath, phys ) );
                         cnf.add(clause);
                     }
                 }
             }
         }
     }
 
     static int specifiedAttributes = 0;
     public void setupSpecifiedAssignment() {
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             BDDType t = expr.getType();
             Map map = t.map();
             for( Iterator domainIt = map.keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 Type phys = (Type) map.get(domain);
                 DNode dnode = DNode.v(expr,domain);
                 if( phys != null ) {
                     Clause clause = new Clause();
                     if(INCLUDE_COMMENTS) clause.setComment("[SPECIFIED] "+dnode+" specified to be "+phys);
                     clause.add( Literal.v( dnode, phys ) );
                     if( cnf.add( clause ) ) {;
                         specifiedAttributes++;
                     }
                 }
             }
         }
     }
 
     private Map bitsMap = new HashMap();
     /** Make sure that the physical domain to which attribute is assigned has
      * at least as many bits as the domain of the attribute. */
     private void assigned( Type attribute, Type phys ) throws SemanticException {
         FieldDecl domainDecl = ts.getField( (ClassType) attribute, "domain" );
         Type domain = domainDecl.declType();
         if( !( domain instanceof ClassType ) ) throw new SemanticException(
                 "Domain of attribute "+attribute+" is not of reference type.",
                 domainDecl.position() );
 
         FieldDecl attrBits = ts.getField( (ClassType) domain, "bits" );
         Expr init = attrBits.init();
         if( init == null ) throw new SemanticException(
                 "Field bits of attribute "+attribute+" has no initializer.",
                 attrBits.position() );
         if( !(init instanceof IntLit) ) throw new SemanticException( 
                 "Initializer of field bits of attribute "+attribute+" is not "+
                 "an integer literal.", attrBits.position() );
         IntLit initLit = (IntLit) init;
         int bits = (int) initLit.value();
 
         Integer physBits = (Integer) bitsMap.get( phys );
         if( physBits == null || physBits.intValue() < bits ) {
             physBits = new Integer( bits );
         }
         bitsMap.put( phys, physBits );
     }
     public void recordPhys(Collection jobs) throws SemanticException {
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             BDDType t = expr.getType();
             for( Iterator pairIt = t.domainPairs().iterator(); pairIt.hasNext(); ) {
                 final Type[] pair = (Type[]) pairIt.next();
                 if( pair[1] != null ) continue;
                 for( Iterator physIt = allPhys.iterator(); physIt.hasNext(); ) {
                     final Type phys = (Type) physIt.next();
                     if( solution.contains( Literal.v(DNode.v(expr,pair[0]),phys) ) ) {
                         pair[1] = phys;
                     }
                 }
             }
             for( Iterator pairIt = t.domainPairs().iterator(); pairIt.hasNext(); ) {
                 final Type[] pair = (Type[]) pairIt.next();
                 assigned( pair[0], pair[1] );
             }
         }
         for( Iterator physIt = bitsMap.keySet().iterator(); physIt.hasNext(); ) {
             final ClassType phys = (ClassType) physIt.next();
             ClassDecl cd = (ClassDecl) ts.instance2Decl().get(phys);
             if( cd == null ) throw new SemanticException( 
                     "No class declaration for physical domain "+phys+"." );
             ClassBody cb = cd.body();
             if( cb == null ) throw new SemanticException( 
                     "No class body for physical domain "+phys+"." );
         }
         NodeVisitor v = new NodeVisitor() {
             public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
                 if( !( n instanceof ClassDecl ) ) return n;
                 ClassDecl cd = (ClassDecl) n;
                 Type t = cd.type();
                 Integer bits = (Integer) bitsMap.get(t);
                 if( bits == null ) return n;
 
                 Position pos = cd.position();
 
                 ClassBody cb = cd.body();
 
                 List newMembers = new ArrayList();
                 for( Iterator memberIt = cb.members().iterator(); memberIt.hasNext(); ) {
                     final ClassMember member = (ClassMember) memberIt.next();
                     if( member instanceof MethodDecl) {
                         MethodDecl md = (MethodDecl) member;
                         if( md.name().equals( "bits" ) 
                         && md.formals().isEmpty() ) {
                             md = (MethodDecl) md.body(
                                 nf.Block( pos,
                                     nf.Return( pos,
                                         nf.IntLit( pos,
                                             IntLit.INT,
                                             bits.intValue()))));
                             newMembers.add( md );
                             continue;
                         }
                     }
                     newMembers.add( member );
                 }
 
                 return cd.body( cb.members( newMembers ) );
             }
         };
         v.begin();
         for( Iterator jobIt = jobs.iterator(); jobIt.hasNext(); ) {
             final Job job = (Job) jobIt.next();
             Node ast = job.ast();
             if( ast != null ) job.ast( ast.visit(v) );
         }
         v.finish();
     }
 
     public void printDomainsRsf() {
         try {
             PrintWriter file = new PrintWriter(
                     new FileOutputStream( new File("domainassign.rsf") ) );
             int snum = 0;
             file.println( "n!type "+(++snum)+"!Root Collapse" );
             for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
                 final BDDExpr expr = (BDDExpr) exprIt.next();
                 file.println( "n!type "+(++snum)+"!\""+expr+" line="+expr.position().file()+":"+expr.position().line()+"\" Expr"  );
                 file.println( "a!level 1!"+snum+" "+snum+"!Foo" );
                 for( Iterator dIt = DNode.nodes().iterator(); dIt.hasNext(); ) {
                     final DNode d = (DNode) dIt.next();
                     if( d.expr != expr ) continue;
                     Type phys = (Type) expr.getType().map().get(d.dom);
                     String phs = phys == null ? "null":phys.toString();
                     file.println( "n!type "+(d.domNum+1000)+"!\""+d.toShortString()+":"+phs+"\" Dom" );
                     file.println( "a!level "+snum+"!"+(d.domNum+1000)+" "+(d.domNum+1000)+"!Foo" );
                 }
             }
             for( Iterator eIt = mustEqualEdges.iterator(); eIt.hasNext(); ) {
                 final DNode[] e = (DNode[]) eIt.next();
                 file.println( "a!MustEqual "+(e[0].domNum+1000)+"!"+(++snum)+" "+(e[1].domNum+1000)+"!Foo" );
             }
             for( Iterator eIt = assignEdges.iterator(); eIt.hasNext(); ) {
                 final DNode[] e = (DNode[]) eIt.next();
                 file.println( "a!Assign "+(e[0].domNum+1000)+"!"+(++snum)+" "+(e[1].domNum+1000)+"!Foo" );
             }
             file.close();
         } catch( IOException e ) {
             throw new RuntimeException( e.toString() );
         }
     }
     public void printDomainsDot() {
         try {
             PrintWriter file = new PrintWriter(
                     new FileOutputStream( new File("domainassign.dot") ) );
             int snum = 1;
             file.println( "digraph G {" );
             file.println( "  size=\"100,75\";" );
             file.println( "  nodesep=0.2;" );
             file.println( "  ranksep=1.5;" );
             file.println( "  mclimit=10;" );
             file.println( "  nslimit=10;" );
             for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
                 final BDDExpr expr = (BDDExpr) exprIt.next();
                 file.println( " subgraph cluster"+ snum++ +" {" );
                 file.println( "  label=\""+expr+":"+expr.position().file()+":"+expr.position().line()+"\";" );
                 BDDType t = expr.getType();
                 Map map = t.map();
                 for( Iterator dIt = DNode.nodes().iterator(); dIt.hasNext(); ) {
                     final DNode d = (DNode) dIt.next();
                     if( d.expr != expr ) continue;
                     Type phys = (Type) map.get(d.dom);
                     String phs = phys == null ? "null":phys.toString();
                     file.println( "  "+d.domNum+" [label=\""+d.toShortString()+":"+phs+"\"];" );
                 }
                 file.println( " }" );
             }
             for( Iterator eIt = mustEqualEdges.iterator(); eIt.hasNext(); ) {
                 final DNode[] e = (DNode[]) eIt.next();
                 file.println( "  "+e[0].domNum+" -> "+
                         e[1].domNum+";" );
             }
             for( Iterator eIt = assignEdges.iterator(); eIt.hasNext(); ) {
                 final DNode[] e = (DNode[]) eIt.next();
                 file.println( "  "+e[0].domNum+" -> "+
                         e[1].domNum+" [dir=none];" );
             }
             file.println( "}" );
             file.close();
         } catch( IOException e ) {
             throw new RuntimeException( e.toString() );
         }
     }
     public void printStats() {
         int exprs=0, nodes=0, nonrep=0, nonrepnodes=0;
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             exprs++;
             if( !( expr.obj() instanceof FixPhys ) || expr.obj() instanceof Replace ) {
                 nonrep++;
             }
         }
         for( Iterator nodeIt = DNode.nodes().iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             nodes++;
             if( !( node.expr.obj() instanceof FixPhys ) || node.expr.obj() instanceof Replace ) {
                 nonrepnodes++;
             }
         }
         System.out.println( "Must equal edges: "+mustEqualEdges.size() );
         System.out.println( "Assignment edges: "+assignEdges.size() );
         System.out.println( "Conflict edges: "+conflictEdgeCount/2 );
         System.out.println( "Expressions: "+exprs );
         System.out.println( "Non-replaces: "+nonrep );
         System.out.println( "Attributes: "+nodes );
         System.out.println( "Non-rep attributes: "+nonrepnodes );
         System.out.println( "Specified attributes: "+specifiedAttributes );
         System.out.println( "Physical domains: "+allPhys.size() );
     }
 }
