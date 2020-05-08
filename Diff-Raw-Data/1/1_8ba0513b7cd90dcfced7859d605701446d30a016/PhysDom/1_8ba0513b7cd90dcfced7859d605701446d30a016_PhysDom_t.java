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
     final static boolean DEBUG = false;
     final static boolean STATS = false;
 
     private static PhysDom instance = new PhysDom();
     public static PhysDom v() { return instance; }
 
     public Map domainAssignment = new HashMap();
 
     public Set assignEdges = new HashSet();
     public Set mustEqualEdges = new HashSet();
     public Collection allPhys = new HashSet();
 
     private String satSolver = System.getProperty("user.home")+System.getProperty("file.separator")+"zchaff.2003.10.9.linux";
     private String satCore = System.getProperty("user.home")+System.getProperty("file.separator")+"zcore";
     public void setSatSolver( String s ) { satSolver = s; }
     public void setSatCore( String s ) { satCore = s; }
 
     static interface HasNum {
         public int getNum();
     }
 
     /*
     static class SetLit implements HasNum {
         Path path;
         private SetLit( Path path ) {
             this.path = path;
         }
         public static SetLit v( Path path ) {
             SetLit ret = new SetLit(path);
             SetLit ret2 = (SetLit) setMap.get( ret );
             if( ret2 == null ) {
                 setMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public boolean equals( Object other ) {
             if( !(other instanceof SetLit) ) return false;
             SetLit o = (SetLit) other;
             if( !o.path.equals(path) ) return false;
             return true;
         }
         public int hashCode() { return path.hashCode(); }
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
         private NegSetLit( Path path ) {
             this( SetLit.v( path ) );
         }
         public static NegSetLit v( Path path ) {
             NegSetLit ret = new NegSetLit( path );
             NegSetLit ret2 = (NegSetLit) setMap.get( ret );
             if( ret2 == null ) {
                 setMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public static NegSetLit v( SetLit e ) {
             return v(e.path);
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
     */
     public static Map setMap = new HashMap();
 
     static class Literal implements HasNum {
         DNode dnode;
         Type phys;
         private Literal( DNode dnode, Type phys ) {
             this.dnode = dnode.rep(); this.phys = phys;
         }
         public static Literal v( DNode dnode, Type phys ) {
             if( dnode.rep() != dnode ) throw new RuntimeException();
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
             NegLiteral ret = new NegLiteral( dnode.rep(), phys );
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
 
     /*
     public static class Path implements Comparable {
         private final int length;
         private final DNode car;
         private final Path cdr;
         private final DNode end;
         public DNode top() { return car; }
         public Type phys() { return PhysDom.phys(end); }
         public int size() { return length; }
         public boolean contains( DNode n ) {
             if( car.equals(n) ) return true;
             if( cdr == null ) return false;
             return cdr.contains(n);
         }
         private boolean isSubPath( Path other ) {
             if( length > other.length ) return false;
             if( car.equals(other.car) ) {
                 if( cdr == null ) return true;
                 if( other.cdr == null ) return false;
                 return cdr.isSubPath(other.cdr);
             } else {
                 if( other.cdr == null ) return false;
                 return isSubPath(other.cdr);
             }
         }
         public boolean isSubSet( Path other ) {
             if( other.length < length ) return false;
             if( !end.equals(other.end) ) return false;
             if( isSubPath(other) ) return true;
             // Now it gets expensive
             if( !other.contains(car) ) return false;
             if( cdr == null ) return true;
             return cdr.isSubSet(other);
         }
         public Path( DNode n ) {
             car = n;
             cdr = null;
             length = 1;
             end = n;
         }
         private Path( Path cdr, DNode car ) {
             this.car = car;
             this.cdr = cdr;
             this.length = cdr.length+1;
             this.end = cdr.end;
         }
         public Path add( DNode n ) {
             return new Path( this, n );
         }
         public int compareTo( Object o ) {
             Path other = (Path) o;
             int ret = length - other.length;
             if( ret != 0 ) return ret;
             ret = car.domNum - other.car.domNum;
             if( ret != 0 ) return ret;
             if( cdr == null ) return 0;
             return cdr.compareTo(other.cdr);
         }
         public int hashCode() { return length; }
         public boolean equals( Object o ) {
             if( o == null ) return false;
             Path other = (Path) o;
             if( length != other.length ) return false;
             if( !car.equals(other.car) ) return false;
             if( cdr == null ) return other.cdr == null;
             return cdr.equals(other.cdr);
         }
         public Iterator iterator() {
             return new Iterator() {
                 Path p = Path.this;
                 public boolean hasNext() { return p != null; }
                 public Object next() { 
                     if( p == null ) throw new NoSuchElementException();
                     Object ret = p.car;
                     p = p.cdr;
                     return ret;
                 }
                 public void remove() {
                     throw new UnsupportedOperationException();
                 }
             };
         }
     }
     */
 
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
 
         if(DEBUG) System.out.println( "creating literals" );
         createLiterals();
 
         if(DEBUG) System.out.println( "adding equality edges" );
         addMustEqualEdges();
 
         if(DEBUG) System.out.println( "building rep list" );
         buildRepList();
 
         if(DEBUG) System.out.println( "computing adjacency lists" );
         computeAdjacencies();
 
         if(DEBUG) System.out.println( "finding possible phys" );
         findPossiblePhys();
 
         if(DEBUG) System.out.println( "dumping dot graph" );
         if(DEBUG) dumpDot();
 
         if(DEBUG) System.out.println( "creating dnode constraints" );
         createDnodeConstraints();
 
         //if(DEBUG) System.out.println( "setting programmer-specified assignments" );
         //setupSpecifiedAssignment();
 
         //if(DEBUG) System.out.println( "adding assign edges" );
         //addAssignEdges();
 
         if(DEBUG) System.out.println( "adding direction constraints" );
         addDirectionConstraints();
 
         if(DEBUG) System.out.println( "adding conflict edges" );
         addConflictEdges();
 
         printStats();
 
         //tryWithBDDsJustForKicks();
         if(DEBUG) System.out.println( "running sat solver" );
         runSat();
 
         if(DEBUG) System.out.println( "dumping dot graph" );
         if(DEBUG) dumpDot();
 
         if(DEBUG) System.out.println( "recording sat solver results" );
         recordPhys(jobs);
 
         if(DEBUG) System.out.println( "dumping dot graph" );
         if(DEBUG) dumpDot();
 
         //printDomainsDot();
         //printDomainsRsf();
 
     }
 
     List repList;
     private void buildRepList() {
         repList = new ArrayList();
         for( Iterator nodeIt = DNode.nodes().iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             if( node.rep() != node ) continue;
             repList.add(node);
         }
     }
 
     Set solution;
     public void runSat() throws SemanticException {
         int numvars = ordMap.size()+(arrMap.size()+setMap.size()+litMap.size())/2;
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
                 if(STATS) if( str.length() < 1000 ) System.out.println( str );
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
             solution = new HashSet();
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
                        if(!(lit instanceof NegLiteral)) continue clause;
                         if( lit.getNum() != i1.intValue() 
                         && lit.getNum() != i2.intValue() ) continue clause;
                         if( nl1 == null ) nl1 = (NegLiteral) lit;
                         else nl2 = (NegLiteral) lit;
                     }
                     if( !cl.isConflict() ) continue line;
                     DNode conflictingNode1 = nl1.lit.dnode;
                     DNode conflictingNode2 = nl2.lit.dnode;
                     for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
                         final BDDExpr expr = (BDDExpr) exprIt.next();
                         DNode node1 = null;
                         DNode node2 = null;
                         BDDType t = expr.getType();
                         Map map = t.map();
                         for( Iterator attrIt = map.keySet().iterator(); attrIt.hasNext(); ) {
                             final Type attr = (Type) attrIt.next();
                             DNode orig = DNode.v(expr, attr);
                             if( orig.rep() == conflictingNode1 ) node1 = orig;
                             if( orig.rep() == conflictingNode2 ) node2 = orig;
                         }
                         if( node1 != null && node2 != null ) {
                             StdErrorQueue seq = new StdErrorQueue( System.err, 0, "" );
                             seq.displayError(
                                     new ErrorInfo(ErrorInfo.SEMANTIC_ERROR,
                                     "Conflict between attributes "+node1.dom
                                     +" and "+node2.dom+
                                     (  expr.isFixPhys() 
                                      ? " of replaced version of" 
                                      : " of"),
                                     expr.position() ) );
                             System.err.println( "over physical domain "
                                     +nl1.lit.phys );
 
                             continue clause;
                         }
                     }
                     throw new RuntimeException();
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
         allPhys = new ArrayList(allPhys);
     }
 
     public void createDnodeConstraints() {
         // Each dnode must be assigned to at least one phys
         for( Iterator dnodeIt = repList.iterator(); dnodeIt.hasNext(); ) {
             final DNode dnode = (DNode) dnodeIt.next();
             Clause clause = new Clause();
             if( INCLUDE_COMMENTS ) clause.setComment(
                     "[PHYS>=1] At least one phys for "+dnode);
             for( Iterator physIt = allPhys(dnode).iterator(); physIt.hasNext(); ) {
                 final Type phys = (Type) physIt.next();
                 clause.add( Literal.v( dnode, phys ) );
             }
             cnf.add( clause );
         }
 
         // Each dnode must be assigned to at most one phys
         /*
         for( Iterator dnodeIt = repList.iterator(); dnodeIt.hasNext(); ) {
             final DNode dnode = (DNode) dnodeIt.next();
             for( Iterator physIt = allPhys(dnode).iterator(); physIt.hasNext(); ) {
                 final Type phys = (Type) physIt.next();
                 for( Iterator phys2It = allPhys(dnode).iterator(); phys2It.hasNext(); ) {
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
         */
     }
 
     public void addMustEqualEdges() {
         for( Iterator edgeIt = mustEqualEdges.iterator(); edgeIt.hasNext(); ) {
             final DNode[] edge = (DNode[]) edgeIt.next();
             edge[0].merge( edge[1] );
         }
     }
     /*
     private void addMustEqualEdge( DNode node1, DNode node2 ) {
         if( node1 == node2 ) return;
 
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
     */
 
     public void addConflictEdges() {
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             BDDType t = expr.getType();
             for( Iterator domainIt = t.map().keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 for( Iterator domain2It = t.map().keySet().iterator(); domain2It.hasNext(); ) {
                     final Type domain2 = (Type) domain2It.next();
                     if( domain.equals(domain2) ) continue;
                     addConflictEdge( DNode.v(expr,domain).rep(), DNode.v(expr,domain2).rep() );
                 }
             }
         }
     }
 
     static int conflictEdgeCount;
     private void addConflictEdge( DNode node1, DNode node2 ) {
         // (xa ==> ~ya) /\ (ya ==> ~xa) = (~ya \/ ~xa)
         for( Iterator physIt = intersect(allPhys(node1), allPhys(node2)).iterator(); physIt.hasNext(); ) {
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
 
     public static Type updatedPhys(DNode d) {
         BDDExpr e = d.expr;
         BDDType t = e.getType();
         for( Iterator pairIt = t.domainPairs().iterator(); pairIt.hasNext(); ) {
             final Type[] pair = (Type[]) pairIt.next();
             if( pair[0] == d.dom ) return pair[1];
         }
         throw new RuntimeException();
     }
     public static Type phys(DNode d) {
         return d.rep().phys;
     }
 
     private void computeAdjacencies() {
         adjacentCache = new HashMap();
         for( Iterator edgeIt = assignEdges.iterator(); edgeIt.hasNext(); ) {
             final DNode[] edge = (DNode[]) edgeIt.next();
             if( edge[0].rep() == edge[1].rep() ) continue;
             addAdjacency(edge[0].rep(), edge[1].rep());
             addAdjacency(edge[1].rep(), edge[0].rep());
         }
     }
     private void addAdjacency(DNode src, DNode dst) {
         Set dsts = (Set) adjacentCache.get(src);
         if( dsts == null ) {
             adjacentCache.put(src, dsts = new HashSet());
         }
         dsts.add(dst);
     }
     private Map adjacentCache;
     public Collection adjacent(DNode d) {
         Collection ret = (Collection) adjacentCache.get(d.rep());
         if( ret == null ) return Collections.EMPTY_LIST;
         return ret;
     }
     
     /*
     public void addAssignEdges() throws SemanticException {
         TreeSet worklist = new TreeSet();
         Map pathMap = new HashMap();
 
         // initialize all nodes that have a phys with a path of length 1
         for( Iterator nodeIt = repList.iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
 
             Set paths = new TreeSet();
             pathMap.put( node, paths );
             if( phys(node) == null ) continue;
             Path path = new Path(node);
             paths.add(path);
             worklist.add(path);
         }
 
         int longest = 0;
         // do a BFS, extending paths through the graph
         while(!worklist.isEmpty()) {
             Path path = (Path) worklist.first();
             //if(DEBUG) System.out.println("worklist size: "+worklist.size()+", f: "+first.size()+", l: "+last.size()+", max path length: "+longest+", current: "+path.length );
             worklist.remove(path);
             if( path.size() > longest ){
                 longest = path.size();
                 System.out.println( "length: "+longest+", worklist: "+worklist.size() );
             }
             DNode node = path.top();
 outer:
             for( Iterator newNodeIt = adjacent(node).iterator(); newNodeIt.hasNext(); ) {
                 final DNode newNode = (DNode) newNodeIt.next();
                 if( newNode.rep() != newNode ) throw new RuntimeException();
 
                 if( phys(newNode) != null ) continue;
                 if( path.contains(newNode) ) continue;
                 Path newPath = path.add(newNode);
                 for( Iterator otherIt = ((Set)pathMap.get(newNode)).iterator(); otherIt.hasNext(); ) {
                     final Path other = (Path) otherIt.next();
                     if( other.isSubSet(newPath) ) continue outer;
                 }
                 worklist.add(newPath);
                 ((Set)pathMap.get(newNode)).add(newPath);
             }
         }
 
         // now encode paths as constraints
         for( Iterator nodeIt = pathMap.keySet().iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             if( node.rep() != node ) throw new RuntimeException();
 
             Set paths = (Set) pathMap.get(node);
 
             {
                 // at least one path must be active for each node
                 Clause clause = new Clause();
                 if( INCLUDE_COMMENTS ) clause.setComment("[PATH>=1] At least one path for "+node);
                 for( Iterator pathIt = paths.iterator(); pathIt.hasNext(); ) {
                     final Path path = (Path) pathIt.next();
                     clause.add( SetLit.v( path ) );
                 }
                 if( clause.size() == 0 ) {
                     node.expr.throwSemanticException( "No physical domains reaching domain "+node.dom );
                 }
                 cnf.add( clause );
             }
 
             {
                 for( Iterator pathIt = paths.iterator(); pathIt.hasNext(); ) {
                     final Path path = (Path) pathIt.next();
                     // all the nodes in the path must have correct phys
                     Type phys = path.phys();
                     NegSetLit pathLit = NegSetLit.v(path);
                     for( Iterator nodeOnPathIt = path.iterator(); nodeOnPathIt.hasNext(); ) {
                         final DNode nodeOnPath = (DNode) nodeOnPathIt.next();
                         // a ==> b /\ c /\ d = (b \/ ~a) /\ (c \/ ~a) /\ (d \/ ~a)
                         Clause clause = new Clause();
                         if(INCLUDE_COMMENTS) clause.setComment("[NODEONPATH] Node "+nodeOnPath+" to node "+node);
                         clause.add( pathLit );
                         clause.add( Literal.v( nodeOnPath, phys ) );
                         cnf.add(clause);
                     }
                 }
             }
         }
     }
     */
 
     static int specifiedAttributes = 0;
     /*
     public void setupSpecifiedAssignment() {
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             BDDType t = expr.getType();
             Map map = t.map();
             for( Iterator domainIt = map.keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 Type phys = (Type) map.get(domain);
                 DNode dnode = DNode.v(expr,domain).rep();
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
     */
 
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
             Set usedAlready = new HashSet();
             for( Iterator pairIt = t.domainPairs().iterator(); pairIt.hasNext(); ) {
                 final Type[] pair = (Type[]) pairIt.next();
                 DNode dnode = DNode.v(expr, pair[0]).rep();
                 for( Iterator physIt = allPhys(dnode).iterator(); physIt.hasNext(); ) {
                     final Type phys = (Type) physIt.next();
                     if( solution.contains( Literal.v(dnode, phys) ) ) {
                         if( pair[1] != null && pair[1] != phys ) {
                             throw new RuntimeException( "Sat solver said "+phys+" for node "+dnode+" ("+dnode.domNum+") that already has phys "+pair[1] );
                         }
                         pair[1] = phys;
                     }
                 }
                 if( pair[1] == null ) {
                     throw new RuntimeException( "Sat solver said nothing for node "+dnode );
                 }
                 if( !usedAlready.add(pair[1]) ) {
                     for( Iterator pair2It = t.domainPairs().iterator(); pair2It.hasNext(); ) {
                         final Type[] pair2 = (Type[]) pair2It.next();
                         System.out.println( ""+DNode.v(expr, pair2[0])+pair2[1] );
                     }
                     throw new RuntimeException( "Sat solver tried to reuse "+pair[1]+" for "+dnode );
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
 
     /*
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
     */
     String dotNodes;
     private void makeDotNodes() {
         StringBuffer ret = new StringBuffer();
         for( Iterator nodeIt = repList.iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             if( phys(node) == null ) {
                 ret.append( "  "+node.domNum+" [shape=point];\n" );
             } else {
                 String phys = phys(node).toString();
                 int i = phys.lastIndexOf(".");
                 if( i >= 0 ) phys = phys.substring( i+1, phys.length() );
                 ret.append( "  "+node.domNum+" [label=\""+phys+"\"];\n" );
             }
         }
         dotNodes = ret.toString();
     }
     public void dumpDot() {
         try {
             PrintWriter file = new PrintWriter(
                     new FileOutputStream( new File("domainassign.dot") ) );
             int snum = 1;
             file.println( "graph G {" );
             file.println( "  size=\"100,75\";" );
             file.println( "  nodesep=0.2;" );
             file.println( "  ranksep=1.5;" );
             file.println( "  mclimit=10;" );
             file.println( "  nslimit=10;" );
             if( dotNodes == null ) makeDotNodes();
             file.print( dotNodes );
             for( Iterator nodeIt = repList.iterator(); nodeIt.hasNext(); ) {
                 final DNode node = (DNode) nodeIt.next();
                 for( Iterator adjIt = adjacent(node).iterator(); adjIt.hasNext(); ) {
                     final DNode adj = (DNode) adjIt.next();
                     if( adj.rep() != adj ) continue;
                     if( node.domNum > adj.domNum ) continue;
                     file.print( "  "+node.domNum+" -- "+adj.domNum+" [" );
                     if( solution != null ) {
                         if( updatedPhys(node) != updatedPhys(adj) ) {
                             file.print( "style=dotted, " );
                         }
                         if( solution.contains( ArrowLit.v( node, adj ) ) ) {
                             file.print( "dir=forward, ");
                         }
                         if( solution.contains( ArrowLit.v( adj, node ) ) ) {
                             file.print( "dir=back, ");
                         }
                     }
                     file.println( "];" );
                 }
             }
             file.println( "}" );
             file.close();
         } catch( IOException e ) {
             throw new RuntimeException( e.toString() );
         }
     }
     /*
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
     */
     public void printStats() {
         int exprs=0, nodes=0, nonrep=0, nonrepnodes=0;
         for( Iterator exprIt = DNode.exprs().iterator(); exprIt.hasNext(); ) {
             final BDDExpr expr = (BDDExpr) exprIt.next();
             exprs++;
             if( !( expr.obj() instanceof FixPhys ) || expr.obj() instanceof Replace ) {
                 nonrep++;
             }
         }
         int nodesAfterMerging = 0;
         for( Iterator nodeIt = DNode.nodes().iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             nodes++;
             if( !( node.expr.obj() instanceof FixPhys ) || node.expr.obj() instanceof Replace ) {
                 nonrepnodes++;
             }
             if( node.rep() == node ) nodesAfterMerging++;
         }
         if(STATS) System.out.println( "Must equal edges: "+mustEqualEdges.size() );
         if(STATS) System.out.println( "Assignment edges: "+assignEdges.size() );
         if(STATS) System.out.println( "Conflict edges: "+conflictEdgeCount/2 );
         if(STATS) System.out.println( "Expressions: "+exprs );
         if(STATS) System.out.println( "Non-replaces: "+nonrep );
         if(STATS) System.out.println( "Attributes: "+nodes );
         if(STATS) System.out.println( "Attributes after merging: "+nodesAfterMerging );
         if(STATS) System.out.println( "Non-rep attributes: "+nonrepnodes );
         if(STATS) System.out.println( "Specified attributes: "+specifiedAttributes );
         if(STATS) System.out.println( "Physical domains: "+allPhys.size() );
     }
 
     static class Edge {
         public final DNode src;
         public final DNode dst;
         public int hashCode() { return src.hashCode()+dst.hashCode(); }
         public boolean equals( Object o ) {
             Edge other = (Edge) o;
             if( src == other.src && dst == other.dst ) return true;
             if( src == other.dst && dst == other.src ) return true;
             return false;
         }
         public Edge( DNode src, DNode dst ) {
             this.src = src;
             this.dst = dst;
         }
         public String toString() { return ""+src.domNum+" -- "+dst.domNum; }
     }
 
     class Bicon {
         private Map delta;
         private int nextDfsNum;
         private LinkedList stack;
         private Map back;
         private List components;
         
         public List doBicon() {
             nextDfsNum = 0;
             delta = new HashMap();
             stack = new LinkedList();
             back = new HashMap();
             components = new ArrayList();
 
             for( Iterator nodeIt = repList.iterator(); nodeIt.hasNext(); ) {
 
                 final DNode node = (DNode) nodeIt.next();
                 if( phys(node) != null ) continue;
                 if( delta.containsKey(node) ) continue;
                 bicon(node, null);
             }
             return components;
         }
         private void bicon( DNode v, DNode parent ) {
             delta.put(v, new Integer(nextDfsNum));
             back.put(v, new Integer(nextDfsNum));
             nextDfsNum++;
             for( Iterator wIt = adjacent(v).iterator(); wIt.hasNext(); ) {
                 final DNode w = (DNode) wIt.next();
                 if( phys(w) != null ) continue;
                 if( w.rep() != w ) continue;
                 if( w == parent ) continue;
                 Edge e = new Edge(v, w);
                 if( !delta.containsKey(w) || delta(w) < delta(v) ) {
                     stack.addLast(e);
                 }
                 if( !delta.containsKey(w) ) {
                     bicon(w, v);
                     if( back(w) >= delta(v) ) {
                         Set component = new HashSet();
                         Edge popped;
                         do {
                             popped = (Edge) stack.removeLast();
                             component.add(popped);
                         } while( !popped.equals(e) );
                         components.add(component);
                     } else {
                         back.put(v, new Integer(min(back(v), back(w))));
                     }
                 } else {
                     back.put(v, new Integer(min(back(v), delta(w))));
                 }
             }
         }
         private int delta( DNode n ) {
             Integer ret = (Integer) delta.get(n);
             return ret.intValue();
         }
         private int back( DNode n ) {
             Integer ret = (Integer) back.get(n);
             return ret.intValue();
         }
     }
     private void addDirectionConstraints() {
         // No edge can go both ways
         // No edge can go out of a phys node
         // No more than one edge out of each node
         for( Iterator nodeIt = repList.iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             Clause atLeastOne = new Clause();
             if( INCLUDE_COMMENTS ) atLeastOne.setComment("[>=1 OUT] "+node);
             for( Iterator adjIt = adjacent(node).iterator(); adjIt.hasNext(); ) {
                 final DNode adj = (DNode) adjIt.next();
                 if( adj.rep() != adj ) continue;
 
                 atLeastOne.add( ArrowLit.v( node, adj ) );
 
                 Clause cl = new Clause();
                 if( INCLUDE_COMMENTS ) 
                     cl.setComment("[BOTH WAYS] "+node+" <=> "+adj );
                 cl.add( NegArrowLit.v( node, adj ) );
                 if( phys(node) == null ) {
                     cl.add( NegArrowLit.v( adj, node ) );
                 }
                 cnf.add(cl);
 
                 for( Iterator adj2It = adjacent(node).iterator(); adj2It.hasNext(); ) {
 
                     final DNode adj2 = (DNode) adj2It.next();
                     if( adj2.rep() != adj2 ) continue;
                     if( adj2 == adj ) continue;
                     cl = new Clause();
                     if( INCLUDE_COMMENTS )
                         cl.setComment("[<=1 OUT] "+node);
                     cl.add( NegArrowLit.v( node, adj ) );
                     cl.add( NegArrowLit.v( node, adj2 ) );
                     cnf.add( cl );
                 }
             }
             if( phys(node) == null ) cnf.add(atLeastOne);
         }
 
         // Make arrows imply same phys
         for( Iterator nodeIt = repList.iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             for( Iterator adjIt = adjacent(node).iterator(); adjIt.hasNext(); ) {
                 final DNode adj = (DNode) adjIt.next();
                 if( adj.rep() != adj ) continue;
 
                 Set nodePhys = allPhys(node);
                 Set adjPhys = allPhys(adj);
                 for( Iterator physIt = union(nodePhys, adjPhys).iterator(); physIt.hasNext(); ) {
                     final Type phys = (Type) physIt.next();
                     Clause cl = new Clause();
                     if( INCLUDE_COMMENTS )
                         cl.setComment("[ARROW=>PHYS] "+node+" => "+adj);
                     cl.add( NegArrowLit.v(node, adj) );
                     if( nodePhys.contains(phys) ) cl.add( Literal.v(node, phys) );
                     if( adjPhys.contains(phys) ) {
                         cl.add( NegLiteral.v(adj, phys) );
                         cnf.add(cl);
                     }
                     cl = new Clause();
                     if( INCLUDE_COMMENTS )
                         cl.setComment("[ARROW=>PHYS] "+node+" => "+adj);
                     cl.add( NegArrowLit.v(node, adj) );
                     if( adjPhys.contains(phys) ) cl.add( Literal.v(adj, phys) );
                     if( nodePhys.contains(phys) ) {
                         cl.add( NegLiteral.v(node, phys) );
                         cnf.add(cl);
                     }
                 }
             }
         }
 
         // Handle biconnected components
         List components = new Bicon().doBicon();
         for( Iterator componentIt = components.iterator(); componentIt.hasNext(); ) {
             final Set component = (Set) componentIt.next();
             if( component.size() <= 1 ) continue;
             if(DEBUG) System.out.println("Biconnected component of size "+component.size());
             Set nodes = new HashSet();
             for( Iterator eIt = component.iterator(); eIt.hasNext(); ) {
                 final Edge e = (Edge) eIt.next();
                 nodes.add( e.src );
                 nodes.add( e.dst );
 
                 Clause cl = new Clause();
                 if( INCLUDE_COMMENTS ) 
                     cl.setComment("[ARROW IMPL ORDER] "+e.src+" => "+e.dst);
                 cl.add( NegArrowLit.v( e.src, e.dst ) );
                 cl.add( OrderLit.v( e.src, e.dst ) );
                 cnf.add(cl);
 
                 cl = new Clause();
                 if( INCLUDE_COMMENTS ) 
                     cl.setComment("[ARROW IMPL ORDER] "+e.dst+" => "+e.src);
                 cl.add( NegArrowLit.v( e.dst, e.src ) );
                 cl.add( OrderLit.v( e.dst, e.src ) );
                 cnf.add(cl);
             }
             for( Iterator nodeIt = nodes.iterator(); nodeIt.hasNext(); ) {
                 final DNode node = (DNode) nodeIt.next();
                 for( Iterator adj1It = adjacent(node).iterator(); adj1It.hasNext(); ) {
                     final DNode adj1 = (DNode) adj1It.next();
                     if( !nodes.contains(adj1) ) continue;
                     for( Iterator adj2It = nodes.iterator(); adj2It.hasNext(); ) {
                         final DNode adj2 = (DNode) adj2It.next();
                         if( adj2 == adj1 ) continue;
                         if( adj2 == node ) continue;
 
                         Clause cl = new Clause();
                         if( INCLUDE_COMMENTS )
                             cl.setComment("[TRIANGLE] "+adj1+" => "+node+" => "+adj2);
                         cl.add( OrderLit.v( adj1, node ) );
                         cl.add( OrderLit.v( node, adj2 ) );
                         cl.add( OrderLit.v( adj2, adj1 ) );
                         cnf.add(cl);
                     }
                 }
             }
         }
     }
     static class ArrowLit implements HasNum {
         DNode src;
         DNode dst;
         private ArrowLit( DNode src, DNode dst ) {
             if( src.rep() != src ) throw new RuntimeException();
             if( dst.rep() != dst ) throw new RuntimeException();
             this.src = src.rep();
             this.dst = dst.rep();
         }
         public static ArrowLit v( DNode src, DNode dst ) {
             ArrowLit ret = new ArrowLit( src, dst );
             ArrowLit ret2 = (ArrowLit) arrMap.get( ret );
             if( ret2 == null ) {
                 arrMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public boolean equals( Object other ) {
             if( !(other instanceof ArrowLit) ) return false;
             ArrowLit o = (ArrowLit) other;
             if( o.src != src ) return false;
             if( o.dst != dst ) return false;
             return true;
         }
         public int hashCode() { return src.hashCode() + dst.hashCode(); }
         public int getNum() {
             Integer i = (Integer) litNumMap.get(this);
             if( i == null ) litNumMap.put( this, i = new Integer(++nextInt) );
             return i.intValue();
         }
         public String toString() {
             return Integer.toString(getNum());
         }
     }
     static class NegArrowLit implements HasNum {
         ArrowLit lit;
         private NegArrowLit( ArrowLit lit ) {
             this.lit = lit;
         }
         private NegArrowLit( DNode src, DNode dst ) {
             this( ArrowLit.v( src, dst ) );
         }
         public static NegArrowLit v( DNode src, DNode dst ) {
             NegArrowLit ret = new NegArrowLit( src.rep(), dst.rep() );
             NegArrowLit ret2 = (NegArrowLit) arrMap.get( ret );
             if( ret2 == null ) {
                 arrMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public boolean equals( Object other ) {
             if( !(other instanceof NegArrowLit) ) return false;
             NegArrowLit o = (NegArrowLit) other;
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
     static class OrderLit implements HasNum {
         DNode src;
         DNode dst;
         private OrderLit( DNode src, DNode dst ) {
             this.src = src.rep();
             this.dst = dst.rep();
         }
         public static OrderLit v( DNode src, DNode dst ) {
             OrderLit ret = new OrderLit( src, dst );
             OrderLit ret2 = (OrderLit) ordMap.get( ret );
             if( ret2 == null ) {
                 ordMap.put( ret2 = ret, ret );
             }
             return ret2;
         }
         public boolean equals( Object other ) {
             if( !(other instanceof OrderLit) ) return false;
             OrderLit o = (OrderLit) other;
             if( o.src != src ) return false;
             if( o.dst != dst ) return false;
             return true;
         }
         public int hashCode() { return src.hashCode() + dst.hashCode(); }
         public int getNum() {
             if( src.domNum > dst.domNum ) return -(OrderLit.v(dst,src).getNum());
             Integer i = (Integer) litNumMap.get(this);
             if( i == null ) litNumMap.put( this, i = new Integer(++nextInt) );
             return i.intValue();
         }
         public String toString() {
             return Integer.toString(getNum());
         }
     }
     public static Map arrMap = new HashMap();
     public static Map ordMap = new HashMap();
     private int min( int i, int j ) {
         if( j < i ) i = j;
         return i;
     }
 
     private Map phys;
     public void findPossiblePhys() {
         phys = new HashMap();
         LinkedList q = new LinkedList();
         for( Iterator nodeIt = repList.iterator(); nodeIt.hasNext(); ) {
             final DNode node = (DNode) nodeIt.next();
             Set s = new HashSet();
             phys.put( node, s );
             if( phys(node) != null ) {
                 s.add(phys(node));
                 q.add(node);
             }
         }
         while( !q.isEmpty() ) {
             DNode node = (DNode) q.removeFirst();
             Set nodeSet = allPhys(node);
             for( Iterator adjIt = adjacent(node).iterator(); adjIt.hasNext(); ) {
                 final DNode adj = (DNode) adjIt.next();
                 if( phys(adj) != null ) continue;
                 Set adjSet = allPhys(adj);
                 if( !adjSet.addAll(nodeSet) ) continue;
                 q.addLast(adj);
             }
         }
     }
     private Set intersect( Set s1, Set s2 ) {
         Set ret = new HashSet();
         ret.addAll( s1 );
         ret.retainAll( s2 );
         return ret;
     }
     private Set union( Set s1, Set s2 ) {
         Set ret = new HashSet();
         ret.addAll( s1 );
         ret.addAll( s2 );
         return ret;
     }
     private Set allPhys( DNode node ) {
         return (Set) phys.get(node.rep());
     }
 }
