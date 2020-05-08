 /* Soot - a J*va Optimization Framework
  * Copyright (C) 2002 Ondrej Lhotak
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
 
 package soot.jimple.spark.pag;
 import java.util.*;
 
 import soot.jimple.*;
 import soot.*;
 import soot.jimple.spark.sets.*;
 import soot.jimple.spark.solver.OnFlyCallGraph;
 import soot.jimple.spark.internal.*;
 import soot.jimple.spark.builder.*;
 import soot.jimple.toolkits.callgraph.Edge;
 import soot.jimple.toolkits.pointer.util.NativeMethodDriver;
 import soot.util.*;
 import soot.util.queue.*;
 import soot.options.SparkOptions;
 import soot.tagkit.*;
 import soot.toolkits.scalar.Pair;
 
 /** Pointer assignment graph.
  * @author Ondrej Lhotak
  */
 public class PAG implements PointsToAnalysis {
 	private HashMap<SootMethod, MethodPAG> methodToMethodPAG = new HashMap<SootMethod,MethodPAG>();
 	//this PAG contains all the MethodPAGs that created nodes in this pag
 	public MethodPAG methodPAGForMethod(SootMethod m) {
 		MethodPAG methodPag = methodToMethodPAG.get(m);
 		if(methodPag == null) {
 			methodPag = new MethodPAG(this, m);
 			methodToMethodPAG.put(m, methodPag);
 		}
 		return methodPag;
 	}
 
	protected PAG() {
 		typeManager = new TypeManager(this);
 		if( !opts.ignore_types() ) {
 			typeManager.setFastHierarchy( Scene.v().getOrMakeFastHierarchy() );
 		}
 		switch( opts.set_impl() ) {
 		case SparkOptions.set_impl_hash:
 			setFactory = HashPointsToSet.getFactory();
 			break;
 		case SparkOptions.set_impl_hybrid:
 			setFactory = HybridPointsToSet.getFactory();
 			break;
 		case SparkOptions.set_impl_heintze:
 			setFactory = SharedHybridSet.getFactory();
 			break;
 		case SparkOptions.set_impl_sharedlist:
 			setFactory = SharedListSet.getFactory();
 			break;
 		case SparkOptions.set_impl_array:
 			setFactory = SortedArraySet.getFactory();
 			break;
 		case SparkOptions.set_impl_bit:
 			setFactory = BitPointsToSet.getFactory();
 			break;
 		case SparkOptions.set_impl_double:
 			P2SetFactory oldF;
 			P2SetFactory newF;
 			switch( opts.double_set_old() ) {
 			case SparkOptions.double_set_old_hash:
 				oldF = HashPointsToSet.getFactory();
 				break;
 			case SparkOptions.double_set_old_hybrid:
 				oldF = HybridPointsToSet.getFactory();
 				break;
 			case SparkOptions.double_set_old_heintze:
 				oldF = SharedHybridSet.getFactory();
 				break;
 			case SparkOptions.double_set_old_sharedlist:
 				oldF = SharedListSet.getFactory();
 				break;
 			case SparkOptions.double_set_old_array:
 				oldF = SortedArraySet.getFactory();
 				break;
 			case SparkOptions.double_set_old_bit:
 				oldF = BitPointsToSet.getFactory();
 				break;
 			default:
 				throw new RuntimeException();
 			}
 			switch( opts.double_set_new() ) {
 			case SparkOptions.double_set_new_hash:
 				newF = HashPointsToSet.getFactory();
 				break;
 			case SparkOptions.double_set_new_hybrid:
 				newF = HybridPointsToSet.getFactory();
 				break;
 			case SparkOptions.double_set_new_heintze:
 				newF = SharedHybridSet.getFactory();
 				break;
 			case SparkOptions.double_set_new_sharedlist:
 				newF = SharedListSet.getFactory();
 				break;
 			case SparkOptions.double_set_new_array:
 				newF = SortedArraySet.getFactory();
 				break;
 			case SparkOptions.double_set_new_bit:
 				newF = BitPointsToSet.getFactory();
 				break;
 			default:
 				throw new RuntimeException();
 			}
 			setFactory = DoublePointsToSet.getFactory( newF, oldF );
 			break;
 		default:
 			throw new RuntimeException();
 		}
 	}
	public PAG( final SparkOptions opts ) {
		this();
		this.opts = opts;
		if( opts.add_tags() ) {
			Node.collectNodeTags();
		}
		PAGNodeFactory.initialize(opts);		
	}
 
 	private HashMap<Node,Node> replacements = new HashMap<Node,Node>();
 	private void setLeftNodeReplacesRightNode(Node left, Node right) {
 		replacements.put(right, left);
 	}
 	public Node getReplacementForNode(Node node) { 
 		return node.getReplacement(this, replacements);		
     }
 	
 	private HashMap<Node, PointsToSetInternal> p2sets = new HashMap<Node,PointsToSetInternal>();
 	public PointsToSetInternal getP2SetForNode(Node node) {
 		Node rep = getReplacementForNode(node);
 		PointsToSetInternal p2set = p2sets.get(rep);
 		if(p2set == null)
 			return EmptyPointsToSet.v();
 		else
 			return p2set;
 	}
 	
 	/** Returns the points-to set for this node, makes it if necessary. */
     public PointsToSetInternal makeP2SetForNode(Node node) {
     	Node rep = getReplacementForNode(node);
 		PointsToSetInternal p2set = p2sets.get(rep);
 		if(p2set == null) {
 			assert(rep.getType() == node.getType());
 			p2set = this.getSetFactory().newSet( rep.getType(), this );
         	p2sets.put(rep, p2set);
         	return p2set;
 		} else {
 			return p2set;
 		}		
     }
 	
     /** Merge with the node other. */
     public void mergeNodes( Node one, Node other ) {
     	assert(getReplacementForNode(other) == other) : "shouldn't happen; other should not have been replaced";
         
         Node oneRep = getReplacementForNode(one);
         if( other == oneRep ) //other is the leader
         	return;
         
         //other has not yet been replaced, so replace it with oneRep
         setLeftNodeReplacesRightNode(oneRep, other);
         
         PointsToSetInternal oneP2Set = p2sets.get(one); //null if one is a slave; if not null, then one is the leader
         assert(oneP2Set == null || one == oneRep);
         PointsToSetInternal otherP2Set = p2sets.get(other);
         if( otherP2Set != oneP2Set
                 && otherP2Set != null 
                 && !otherP2Set.isEmpty() ) {
         	//the otherP2Set is not null and not empty and we don't have it already
         	
         	PointsToSetInternal oneRepP2Set = p2sets.get(oneRep);
             if( oneRepP2Set == null || oneRepP2Set.isEmpty() ) {
             	p2sets.put(oneRep, otherP2Set);                
             } else {
             	oneRepP2Set.mergeWith( otherP2Set );
             }
         }
         //other is now the slave of oneRep; don't need the p2set any more
         p2sets.remove(other);
         
         
         this.mergedWith( oneRep, other );
         if( (other instanceof VarNode)
                 && (oneRep instanceof VarNode )
                 && ((VarNode) other).isInterProcTarget() )
             ((VarNode) oneRep).setInterProcTarget();
     }
 	/** Returns the set of objects pointed to by variable l. */
 	public PointsToSet reachingObjects( Local l ) {
 		VarNode n = findLocalVarNode( l );
 		if( n == null ) {
 			return EmptyPointsToSet.v();
 		}
 		return this.getP2SetForNode(n);
 	}
 
 	/** Returns the set of objects pointed to by variable l in context c. */
 	public PointsToSet reachingObjects( Context c, Local l ) {
 		VarNode n = findContextVarNode( l, c );
 		if( n == null ) {
 			return EmptyPointsToSet.v();
 		}
 		return this.getP2SetForNode(n);
 	}
 
 	/** Returns the set of objects pointed to by static field f. */
 	public PointsToSet reachingObjects( SootField f ) {
 		if( !f.isStatic() )
 			throw new RuntimeException( "The parameter f must be a *static* field." );
 		VarNode n = findGlobalVarNode( f );
 		if( n == null ) {
 			return EmptyPointsToSet.v();
 		}
 		return this.getP2SetForNode(n);
 	}
 
 	/** Returns the set of objects pointed to by instance field f
 	 * of the objects in the PointsToSet s. */
 	 public PointsToSet reachingObjects( PointsToSet s, final SootField f ) {
 		if( f.isStatic() )
 			throw new RuntimeException( "The parameter f must be an *instance* field." );
 
 		return reachingObjectsInternal( s, f );
 	 }
 
 	 /** Returns the set of objects pointed to by elements of the arrays
 	  * in the PointsToSet s. */
 	 public PointsToSet reachingObjectsOfArrayElement( PointsToSet s ) {
 		 return reachingObjectsInternal( s, ArrayElement.v() );
 	 }
 
 	 private PointsToSet reachingObjectsInternal( PointsToSet s, final SparkField f ) {
 		 if( getOpts().field_based() || getOpts().vta() ) {
 			 VarNode n = findGlobalVarNode( f );
 			 if( n == null ) {
 				 return EmptyPointsToSet.v();
 			 }
 			 return this.getP2SetForNode(n);
 		 }
 		 if( (getOpts()).propagator() == SparkOptions.propagator_alias ) {
 			 throw new RuntimeException( "The alias edge propagator does not compute points-to information for instance fields! Use a different propagator." );
 		 }
 		 PointsToSetInternal bases = (PointsToSetInternal) s;
 		 final PointsToSetInternal ret = setFactory.newSet( 
 				 (f instanceof SootField) ? ((SootField)f).getType() : null, this );
 		 bases.forall( new P2SetVisitor() {
 			 public final void visit( Node n ) {
 				 Node nDotF = ((AllocNode) n).dot( f );
 				 if(nDotF != null) ret.addAll( getP2SetForNode(nDotF), null );
 			 }} );
 		 return ret;
 	 }
 
 	 public P2SetFactory getSetFactory() {
 		 return setFactory;
 	 }
 	 public void cleanUpMerges() {
 		 if( opts.verbose() ) {
 			 G.v().out.println( "Cleaning up graph for merged nodes" );
 		 }
 		 Map[] maps = { simple, alloc, store, load,
 				 simpleInv, allocInv, storeInv, loadInv };
 		 for (Map<Object, Object> m : maps) {
 			 for (Object object : m.keySet()) {
 				 lookup( m, object );
 			 }
 		 }
 		 somethingMerged = false;
 		 if( opts.verbose() ) {
 			 G.v().out.println( "Done cleaning up graph for merged nodes" );
 		 }
 	 }
 	 public boolean doAddSimpleEdge( VarNode from, VarNode to ) {
 		 return addToMap( simple, from, to ) | addToMap( simpleInv, to, from );
 	 }
 
 	 public boolean doAddStoreEdge( VarNode from, FieldRefNode to ) {
 		 return addToMap( store, from, to ) | addToMap( storeInv, to, from );
 	 }
 
 	 public boolean doAddLoadEdge( FieldRefNode from, VarNode to ) {
 		 return addToMap( load, from, to ) | addToMap( loadInv, to, from );
 	 }
 
 	 public boolean doAddAllocEdge( AllocNode from, VarNode to ) {
 		 return addToMap( alloc, from, to ) | addToMap( allocInv, to, from );
 	 }
 
 	 /** Node uses this to notify PAG that n2 has been merged into n1. */
 	 void mergedWith( Node n1, Node n2 ) {
 		 if( n1.equals( n2 ) ) throw new RuntimeException( "oops" );
 
 		 somethingMerged = true;
 		 if( this.getOnFlyCallGraph() != null ) this.getOnFlyCallGraph().mergedWith( n1, n2 );
 
 		 Map[] maps = { simple, alloc, store, load,
 				 simpleInv, allocInv, storeInv, loadInv };
 		 for (Map<Node, Object> m : maps) {
 			 if( !m.keySet().contains( n2 ) ) continue;
 
 			 Object[] os = { m.get( n1 ), m.get( n2 ) };
 			 int size1 = getSize(os[0]); int size2 = getSize(os[1]);
 			 if( size1 == 0 ) {
 				 if( os[1] != null ) m.put( n1, os[1] );
 			 } else if( size2 == 0 ) {
 				 // nothing needed
 			 } else if( os[0] instanceof HashSet ) {
 				 if( os[1] instanceof HashSet ) {
 					 ((HashSet) os[0]).addAll( (HashSet) os[1] );
 				 } else {
 					 Node[] ar = (Node[]) os[1];
 					 for (Node element0 : ar) {
 						 ( (HashSet<Node>) os[0] ).add( element0 );
 					 }
 				 }
 			 } else if( os[1] instanceof HashSet ) {
 				 Node[] ar = (Node[]) os[0];
 				 for (Node element0 : ar) {
 					 ((HashSet<Node>) os[1]).add( element0 );
 				 }
 				 m.put( n1, os[1] );
 			 } else if( size1*size2 < 1000 ) {
 				 Node[] a1 = (Node[]) os[0];
 				 Node[] a2 = (Node[]) os[1];
 				 Node[] ret = new Node[size1+size2];
 				 System.arraycopy( a1, 0, ret, 0, a1.length ); 
 				 int j = a1.length;
 				 outer: for (Node rep : a2) {
 					 for( int k = 0; k < j; k++ )
 						 if( rep == ret[k] ) continue outer;
 					 ret[j++] = rep;
 				 }
 				 Node[] newArray = new Node[j];
 				 System.arraycopy( ret, 0, newArray, 0, j );
 				 m.put( n1, ret = newArray );
 			 } else {
 				 HashSet<Node> s = new HashSet<Node>( size1+size2 );
 				 for (Object o : os) {
 					 if( o == null ) continue;
 					 if( o instanceof Set ) {
 						 s.addAll( (Set) o );
 					 } else {
 						 Node[] ar = (Node[]) o;
 						 for (Node element1 : ar) {
 							 s.add( element1 );
 						 }
 					 }
 				 }
 				 m.put( n1, s );
 			 }
 			 m.remove( n2 );
 		 }
 	 }
 	 protected final static Node[] EMPTY_NODE_ARRAY = new Node[0];
 	 protected Node[] lookup( Map<Object, Object> m, Object key ) {
 		 Object valueList = m.get( key );
 		 if( valueList == null ) {
 			 return EMPTY_NODE_ARRAY;
 		 }
 		 if( valueList instanceof Set ) {
 			 try {
 				 m.put( key, valueList = 
 					 ( (Set) valueList ).toArray( EMPTY_NODE_ARRAY ) );
 			 } catch( Exception e ) {
 				 for( Iterator it = ((Set)valueList).iterator(); it.hasNext(); ) {
 					 G.v().out.println( ""+it.next() );
 				 }
 				 throw new RuntimeException( ""+valueList+e );
 			 }
 		 }
 		 Node[] ret = (Node[]) valueList;
 		 if( somethingMerged ) {
 			 for( int i = 0; i < ret.length; i++ ) {
 				 Node reti = ret[i];
 				 Node rep = this.getReplacementForNode(reti);
 				 if( rep != reti || rep == key ) {
 					 Set<Node> s;
 					 if( ret.length <= 75 ) {
 						 int j = i;
 						 outer: for( ; i < ret.length; i++ ) {
 							 reti = ret[i];
 							 rep = this.getReplacementForNode(reti);
 							 if( rep == key ) continue;
 							 for( int k = 0; k < j; k++ )
 								 if( rep == ret[k] ) continue outer;
 							 ret[j++] = rep;
 						 }
 						 Node[] newArray = new Node[j];
 						 System.arraycopy( ret, 0, newArray, 0, j );
 						 m.put( key, ret = newArray );
 					 } else {
 						 s = new HashSet<Node>( ret.length * 2 );
 						 for( int j = 0; j < i; j++ ) s.add( ret[j] );
 						 for( int j = i; j < ret.length; j++ ) {
 							 rep = this.getReplacementForNode(ret[j]);
 							 if( rep != key ) {
 								 s.add( rep );
 							 }
 						 }
 						 m.put( key, ret = s.toArray( EMPTY_NODE_ARRAY ) );
 					 }
 					 break;
 				 }
 			 }
 		 }
 		 return ret;
 	 }
 
 	 public Node[] simpleLookup( VarNode key ) 
 	 { return lookup( simple, key ); }
 	 public Node[] simpleInvLookup( VarNode key ) 
 	 { return lookup( simpleInv, key ); }
 	 public Node[] loadLookup( FieldRefNode key ) 
 	 { return lookup( load, key ); }
 	 public Node[] loadInvLookup( VarNode key ) 
 	 { return lookup( loadInv, key ); }
 	 public Node[] storeLookup( VarNode key ) 
 	 { return lookup( store, key ); }
 	 public Node[] storeInvLookup( FieldRefNode key ) 
 	 { return lookup( storeInv, key ); }
 	 public Node[] allocLookup( AllocNode key ) 
 	 { return lookup( alloc, key ); }
 	 public Node[] allocInvLookup( VarNode key ) 
 	 { return lookup( allocInv, key ); }
 	 
 	 public Map<Object, Object> storeEdges() { return store; }
 	 public Map<Object, Object> allocEdges() { return alloc; }
 	 public Map<Object, Object> loadEdges() { return load; }
 	 public Map<Object, Object> simpleEdges() { return simple; }
 	 public Set<Object> simpleSources() { return simple.keySet(); }
 	 public Set<Object> allocSources() { return alloc.keySet(); }
 	 public Set<Object> storeSources() { return store.keySet(); }
 	 public Set<Object> loadSources() { return load.keySet(); }
 	 public Set<Object> simpleInvSources() { return simpleInv.keySet(); }
 	 public Set<Object> allocInvSources() { return allocInv.keySet(); }
 	 public Set<Object> storeInvSources() { return storeInv.keySet(); }
 	 public Set<Object> loadInvSources() { return loadInv.keySet(); }
 
 	 public Iterator<Object> simpleSourcesIterator() { return simple.keySet().iterator(); }
 	 public Iterator<Object> allocSourcesIterator() { return alloc.keySet().iterator(); }
 	 public Iterator<Object> storeSourcesIterator() { return store.keySet().iterator(); }
 	 public Iterator<Object> loadSourcesIterator() { return load.keySet().iterator(); }
 	 public Iterator<Object> simpleInvSourcesIterator() { return simpleInv.keySet().iterator(); }
 	 public Iterator<Object> allocInvSourcesIterator() { return allocInv.keySet().iterator(); }
 	 public Iterator<Object> storeInvSourcesIterator() { return storeInv.keySet().iterator(); }
 	 public Iterator<Object> loadInvSourcesIterator() { return loadInv.keySet().iterator(); }
 
 	 static private int getSize( Object set ) {
 		 if( set instanceof Set ) return ((Set) set).size();
 		 else if( set == null ) return 0;
 		 else return ((Object[]) set).length;
 	 }
 
 
 	 protected P2SetFactory setFactory;
 	 protected boolean somethingMerged = false;
 
 	 /** Returns the set of objects pointed to by instance field f
 	  * of the objects pointed to by l. */
 	 public PointsToSet reachingObjects( Local l, SootField f ) {
 		 return reachingObjects( reachingObjects(l), f );
 	 }
 
 	 /** Returns the set of objects pointed to by instance field f
 	  * of the objects pointed to by l in context c. */
 	 public PointsToSet reachingObjects( Context c, Local l, SootField f ) {
 		 return reachingObjects( reachingObjects(c, l), f );
 	 }
 
 	 /** Finds the GlobalVarNode for the variable value, or returns null. */
 	 public GlobalVarNode findGlobalVarNode( Object value ) {
 		 if( opts.rta() ) {
 			 value = null;
 		 }
 		 return GlobalVarNode.globalVarNode(value);		 
 	 }
 	 /** Finds the LocalVarNode for the variable value, or returns null. */
 	 public LocalVarNode findLocalVarNode( Object value ) {
 		 if( opts.rta() ) {
 			 value = null;
 		 } 
 		 
 		 return LocalVarNode.localVarNode(value);		 
 	 }
 	 
 	 /** Finds the ContextVarNode for base variable value and context
 	  * context, or returns null. */
 	 public ContextVarNode findContextVarNode( Object baseValue, Context context ) {
 		 LocalVarNode base = findLocalVarNode( baseValue );
 		 if( base == null ) return null;
 		 return base.context( context );
 	 }
 
 
 	 /** Finds the FieldRefNode for base variable value and field
 	  * field, or returns null. */
 	 public FieldRefNode findLocalFieldRefNode( Object baseValue, SparkField field ) {
 		 VarNode base = findLocalVarNode( baseValue );
 		 if( base == null ) return null;
 		 return base.dot( field );
 	 }
 	 /** Finds the FieldRefNode for base variable value and field
 	  * field, or returns null. */
 	 public FieldRefNode findGlobalFieldRefNode( Object baseValue, SparkField field ) {
 		 VarNode base = findGlobalVarNode( baseValue );
 		 if( base == null ) return null;
 		 return base.dot( field );
 	 }
 	 
 	 /** Finds the AllocDotField for base AllocNode an and field
 	  * field, or returns null. */
 	 public AllocDotField findAllocDotField( AllocNode an, SparkField field ) {
 		 return an.dot( field );
 	 }
 	 
 
 	 private boolean addSimpleEdge( VarNode from, VarNode to ) {
 		 boolean ret = false;
 		 if( doAddSimpleEdge( from, to ) ) {
 			 edgeQueue.add( from );
 			 edgeQueue.add( to );
 			 ret = true;
 		 }
 		 if( opts.simple_edges_bidirectional() ) {
 			 if( doAddSimpleEdge( to, from ) ) {
 				 edgeQueue.add( to );
 				 edgeQueue.add( from );
 				 ret = true;
 			 }
 		 }
 		 return ret;
 	 }
 
 	 private boolean addStoreEdge( VarNode from, FieldRefNode to ) {
 		 if( !opts.rta() ) {
 			 if( doAddStoreEdge( from, to ) ) {
 				 edgeQueue.add( from );
 				 edgeQueue.add( to );
 				 return true;
 			 }
 		 }
 		 return false;
 	 }
 
 	 private boolean addLoadEdge( FieldRefNode from, VarNode to ) {
 		 if( !opts.rta() ) {
 			 if( doAddLoadEdge( from, to ) ) {
 				 edgeQueue.add( from );
 				 edgeQueue.add( to );
 				 return true;
 			 }
 		 }
 		 return false;
 	 }
 
 	 private boolean addAllocEdge( AllocNode from, VarNode to ) {
 		 FastHierarchy fh = typeManager.getFastHierarchy();
 		 if( fh == null || to.getType() == null         
 				 || fh.canStoreType( from.getType(), to.getType() ) ) {
 			 if( doAddAllocEdge( from, to ) ) {
 				 edgeQueue.add( from );
 				 edgeQueue.add( to );
 				 return true;
 			 }
 		 }
 		 return false;
 	 }
 	 
 	 /** Adds an edge to the graph, returning false if it was already there. */
 	 public final boolean addEdge( Node from, Node to ) {
 		 from = this.getReplacementForNode(from);
 		 to = this.getReplacementForNode(to);
 		 if( from instanceof VarNode ) {
 			 if( to instanceof VarNode ) {
 				 return addSimpleEdge( (VarNode) from, (VarNode) to );
 			 } else {
 				 return addStoreEdge( (VarNode) from, (FieldRefNode) to );
 			 }
 		 } else if( from instanceof FieldRefNode ) {
 			 return addLoadEdge( (FieldRefNode) from, (VarNode) to );
 
 		 } else {
 			 return addAllocEdge( (AllocNode) from, (VarNode) to );
 		 }
 	 }
 
 	 protected ChunkedQueue edgeQueue = new ChunkedQueue();
 	 public QueueReader edgeReader() { return edgeQueue.reader(); }
 
 	 protected void recordCallAssign(InvokeExpr expr, Pair<?,?> pair) {
 		 callAssigns.put(expr, pair);    	    	
 	 }
 
 	 public TypeManager getTypeManager() {
 		 return typeManager;
 	 }
 
 	 public void useOnFlyCallGraph() { 
 		 this.ofcg = new OnFlyCallGraph(this);        
 	 }
 	 public OnFlyCallGraph getOnFlyCallGraph() { return ofcg; }
 	 /** Adds the base of a dereference to the list of dereferenced 
 	  * variables. */
 	 public void addDereference( VarNode base ) {
 		 dereferences.add( base );
 	 }
 
 	 /** Returns list of dereferences variables. */
 	 public List<VarNode> getDereferences() {
 		 return dereferences;
 	 }
 
 	 /** Returns SparkOptions for this graph. */
 	 public SparkOptions getOpts() { return opts; }
 
 	 final public void addCallTarget( Edge e ) {
 		 if( !e.passesParameters() ) return;
 		 MethodPAG srcmpag = this.methodPAGForMethod(e.src());
 		 MethodPAG tgtmpag = this.methodPAGForMethod(e.tgt());
 		 if( e.isExplicit() || e.kind() == Kind.THREAD ) {
 			 addCallTarget( srcmpag, tgtmpag, (Stmt) e.srcUnit(),
 					 e.srcCtxt(), e.tgtCtxt() );
 		 } else {
 			 if( e.kind() == Kind.PRIVILEGED ) {
 				 // Flow from first parameter of doPrivileged() invocation
 				 // to this of target, and from return of target to the
 				 // return of doPrivileged()
 
 				 InvokeExpr ie = e.srcStmt().getInvokeExpr();
 
 				 Node parm = srcmpag.buildNodeForValue(ie.getArg(0) );
 				 parm = srcmpag.parameterize( parm, e.srcCtxt() );
 				 parm = parm.getReplacement(this);
 
 				 Node thiz = PAGNodeFactory.v().makeThis(tgtmpag.getMethod());
 				 thiz = tgtmpag.parameterize( thiz, e.tgtCtxt() );
 				 thiz = thiz.getReplacement(this);
 
 				 addEdge( parm, thiz );
 				 recordCallAssign(ie, new Pair(parm, thiz));
 				 callToMethod.put(ie, srcmpag.getMethod());
 
 				 if( e.srcUnit() instanceof AssignStmt ) {
 					 AssignStmt as = (AssignStmt) e.srcUnit();
 
 					 Node ret = PAGNodeFactory.v().makeRet(tgtmpag.getMethod());
 					 ret = tgtmpag.parameterize( ret, e.tgtCtxt() );
 					 ret = ret.getReplacement(this);
 
 					 Node lhs = srcmpag.buildNodeForValue(as.getLeftOp());
 					 lhs = srcmpag.parameterize( lhs, e.srcCtxt() );
 					 lhs = lhs.getReplacement(this);
 
 					 addEdge( ret, lhs );
 					 recordCallAssign(ie, new Pair(ret, lhs));
 					 callToMethod.put(ie, srcmpag.getMethod());
 				 }
 			 } else if( e.kind() == Kind.FINALIZE ) {
 				 Node srcThis = PAGNodeFactory.v().makeThis(srcmpag.getMethod());
 				 srcThis = srcmpag.parameterize( srcThis, e.srcCtxt() );
 				 srcThis = srcThis.getReplacement(this);
 
 				 Node tgtThis = PAGNodeFactory.v().makeThis(tgtmpag.getMethod());
 				 tgtThis = tgtmpag.parameterize( tgtThis, e.tgtCtxt() );
 				 tgtThis = tgtThis.getReplacement(this);
 
 				 addEdge( srcThis, tgtThis );
 			 } else if( e.kind() == Kind.NEWINSTANCE ) {
 				 Stmt s = (Stmt) e.srcUnit();
 				 InstanceInvokeExpr iie = (InstanceInvokeExpr) s.getInvokeExpr();
 
 				 Node cls = srcmpag.buildNodeForValue(iie.getBase());
 				 cls = srcmpag.parameterize( cls, e.srcCtxt() );
 				 cls = cls.getReplacement(this);
 				 Node newObject = PAGNodeFactory.v().makeNewInstance(this, (VarNode) cls );
 
 				 Node initThis = PAGNodeFactory.v().makeThis(tgtmpag.getMethod());
 				 initThis = tgtmpag.parameterize( initThis, e.tgtCtxt() );
 				 initThis = initThis.getReplacement(this);
 
 				 addEdge( newObject, initThis );
 				 if (s instanceof AssignStmt) {
 					 AssignStmt as = (AssignStmt)s;
 					 Node asLHS = srcmpag.buildNodeForValue(as.getLeftOp());
 					 asLHS = srcmpag.parameterize( asLHS, e.srcCtxt());
 					 asLHS = asLHS.getReplacement(this);
 					 addEdge( newObject, asLHS);
 				 }
 				 recordCallAssign(s.getInvokeExpr(), new Pair(newObject, initThis));
 				 callToMethod.put(s.getInvokeExpr(), srcmpag.getMethod());
 			 } else if( e.kind() == Kind.REFL_INVOKE ) {
 				 // Flow (1) from first parameter of invoke(..) invocation
 				 // to this of target, (2) from the contents of the second (array) parameter
 				 // to all parameters of the target, and (3)  from return of target to the
 				 // return of invoke(..)
 
 				 //(1)
 				 InvokeExpr ie = e.srcStmt().getInvokeExpr();
 
 				 Value arg0 = ie.getArg(0);
 				 //if "null" is passed in, omit the edge
 				 if(arg0!=NullConstant.v()) {
 					 Node parm0 = srcmpag.buildNodeForValue(arg0 );
 					 parm0 = srcmpag.parameterize( parm0, e.srcCtxt() );
 					 parm0 = parm0.getReplacement(this);
 
 					 Node thiz = PAGNodeFactory.v().makeThis(tgtmpag.getMethod());
 					 thiz = tgtmpag.parameterize( thiz, e.tgtCtxt() );
 					 thiz = thiz.getReplacement(this);
 
 					 addEdge( parm0, thiz );
 					 recordCallAssign(ie, new Pair(parm0, thiz));
 					 callToMethod.put(ie, srcmpag.getMethod());
 				 }
 
 				 //(2)
 				 Value arg1 = ie.getArg(1);
 				 SootMethod tgt = e.getTgt().method();
 				 //if "null" is passed in, or target has no parameters, omit the edge
 				 if(arg1!=NullConstant.v() && tgt.getParameterCount()>0) {
 					 Node parm1 = srcmpag.buildNodeForValue( arg1 );
 					 parm1 = srcmpag.parameterize( parm1, e.srcCtxt() );
 					 parm1 = parm1.getReplacement(this);
 					 FieldRefNode parm1contents = PAGNodeFactory.v().makeFieldRefNode( (VarNode) parm1, ArrayElement.v() );
 
 					 for(int i=0;i<tgt.getParameterCount(); i++) {
 						 //if no reference type, create no edge
 						 if(!(tgt.getParameterType(i) instanceof RefLikeType)) continue;
 
 						 Node tgtParmI = PAGNodeFactory.v().makeParm(tgtmpag.getMethod(), i);
 						 tgtParmI = tgtmpag.parameterize( tgtParmI, e.tgtCtxt() );
 						 tgtParmI = tgtParmI.getReplacement(this);
 
 						 addEdge( parm1contents, tgtParmI );
 						 recordCallAssign(ie, new Pair(parm1contents, tgtParmI));
 					 }
 				 }
 
 				 //(3)
 				 //only create return edge if we are actually assigning the return value and
 				 //the return type of the callee is actually a reference type
 				 if( e.srcUnit() instanceof AssignStmt && (tgt.getReturnType() instanceof RefLikeType)) {
 					 AssignStmt as = (AssignStmt) e.srcUnit();
 
 					 Node ret = PAGNodeFactory.v().makeRet(tgtmpag.getMethod());
 					 ret = tgtmpag.parameterize( ret, e.tgtCtxt() );
 					 ret = ret.getReplacement(this);
 
 					 Node lhs = srcmpag.buildNodeForValue(as.getLeftOp());
 					 lhs = srcmpag.parameterize( lhs, e.srcCtxt() );
 					 lhs = lhs.getReplacement(this);
 
 					 addEdge( ret, lhs );
 					 recordCallAssign(ie, new Pair(ret, lhs));
 				 }
 			 } else if( e.kind() == Kind.REFL_CLASS_NEWINSTANCE || e.kind() == Kind.REFL_CONSTR_NEWINSTANCE) {
 				 // (1) create a fresh node for the new object
 				 // (2) create edge from this object to "this" of the constructor
 				 // (3) if this is a call to Constructor.newInstance and not Class.newInstance,
 				 //     create edges passing the contents of the arguments array of the call
 				 //     to all possible parameters of the target
 				 // (4) if we are inside an assign statement,
 				 //     assign the fresh object from (1) to the LHS of the assign statement
 
 				 Stmt s = (Stmt) e.srcUnit();
 				 InstanceInvokeExpr iie = (InstanceInvokeExpr) s.getInvokeExpr();
 
 				 //(1)
 				 Node cls = srcmpag.buildNodeForValue(iie.getBase() );
 				 cls = srcmpag.parameterize( cls, e.srcCtxt() );
 				 cls = cls.getReplacement(this);
 				 if( cls instanceof ContextVarNode ) cls = findLocalVarNode( ((VarNode)cls).getVariable() );
 
 				 VarNode newObject = PAGNodeFactory.v().makeGlobalVarNode( cls, RefType.v( "java.lang.Object" ) );
 				 SootClass tgtClass = e.getTgt().method().getDeclaringClass();
 				 RefType tgtType = tgtClass.getType();                
 				 AllocNode site = PAGNodeFactory.v().makeAllocNode( new Pair(cls, tgtClass), tgtType, null );
 				 addEdge( site, newObject );
 
 				 //(2)
 				 Node initThis = PAGNodeFactory.v().makeThis(tgtmpag.getMethod());
 				 initThis = tgtmpag.parameterize( initThis, e.tgtCtxt() );
 				 initThis = initThis.getReplacement(this);
 				 addEdge( newObject, initThis );
 
 				 //(3)
 				 if(e.kind() == Kind.REFL_CONSTR_NEWINSTANCE) {
 					 Value arg = iie.getArg(0);
 					 SootMethod tgt = e.getTgt().method();
 					 //if "null" is passed in, or target has no parameters, omit the edge
 					 if(arg!=NullConstant.v() && tgt.getParameterCount()>0) {
 						 Node parm0 = srcmpag.buildNodeForValue( arg );
 						 parm0 = srcmpag.parameterize( parm0, e.srcCtxt() );
 						 parm0 = parm0.getReplacement(this);
 						 FieldRefNode parm1contents = PAGNodeFactory.v().makeFieldRefNode( (VarNode) parm0, ArrayElement.v() );
 
 						 for(int i=0;i<tgt.getParameterCount(); i++) {
 							 //if no reference type, create no edge
 							 if(!(tgt.getParameterType(i) instanceof RefLikeType)) continue;
 
 							 Node tgtParmI = PAGNodeFactory.v().makeParm(tgtmpag.getMethod(), i);
 							 tgtParmI = tgtmpag.parameterize( tgtParmI, e.tgtCtxt() );
 							 tgtParmI = tgtParmI.getReplacement(this);
 
 							 addEdge( parm1contents, tgtParmI );
 							 recordCallAssign(iie, new Pair(parm1contents, tgtParmI));
 						 }
 					 }
 				 }
 
 				 //(4)
 				 if (s instanceof AssignStmt) {
 					 AssignStmt as = (AssignStmt)s;
 					 Node asLHS = srcmpag.buildNodeForValue(as.getLeftOp());
 					 asLHS = srcmpag.parameterize( asLHS, e.srcCtxt());
 					 asLHS = asLHS.getReplacement(this);
 					 addEdge( newObject, asLHS);
 				 }
 				 recordCallAssign(s.getInvokeExpr(), new Pair(newObject, initThis));
 				 callToMethod.put(s.getInvokeExpr(), srcmpag.getMethod());
 			 } else {
 				 throw new RuntimeException( "Unhandled edge "+e );
 			 }
 		 }
 	 }
 
 
 	 /** Adds method target as a possible target of the invoke expression in s.
 	  * If target is null, only creates the nodes for the call site,
 	  * without actually connecting them to any target method.
 	  **/
 	 final private void addCallTarget( MethodPAG srcmpag,
 			 MethodPAG tgtmpag,
 			 Stmt s,
 			 Context srcContext,
 			 Context tgtContext ) {
 		 InvokeExpr ie = s.getInvokeExpr();
 		 boolean virtualCall = callAssigns.containsKey(ie);
 		 int numArgs = ie.getArgCount();
 		 for( int i = 0; i < numArgs; i++ ) {
 			 Value arg = ie.getArg( i );
 			 if( !( arg.getType() instanceof RefLikeType ) ) continue;
 			 if( arg instanceof NullConstant ) continue;
 
 			 Node argNode = srcmpag.buildNodeForValue( arg );
 			 argNode = srcmpag.parameterize( argNode, srcContext );
 			 argNode = argNode.getReplacement(this);
 
 			 Node parm = PAGNodeFactory.v().makeParm(tgtmpag.getMethod(), i);
 			 parm = tgtmpag.parameterize( parm, tgtContext );
 			 parm = parm.getReplacement(this);
 
 			 addEdge( argNode, parm );
 			 recordCallAssign(ie, new Pair(argNode, parm));
 			 callToMethod.put(ie, srcmpag.getMethod());
 
 		 }
 		 if( ie instanceof InstanceInvokeExpr ) {
 			 InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
 
 			 Node baseNode = srcmpag.buildNodeForValue(iie.getBase() );
 			 baseNode = srcmpag.parameterize( baseNode, srcContext );
 			 baseNode = baseNode.getReplacement(this);
 
 			 Node thisRef = PAGNodeFactory.v().makeThis(tgtmpag.getMethod());
 			 thisRef = tgtmpag.parameterize( thisRef, tgtContext );
 			 thisRef = thisRef.getReplacement(this);
 			 addEdge( baseNode, thisRef );
 			 recordCallAssign(ie, new Pair(baseNode, thisRef));
 			 callToMethod.put(ie, srcmpag.getMethod());
 			 if (virtualCall && !virtualCallsToReceivers.containsKey(ie)) {
 				 virtualCallsToReceivers.put(ie, baseNode);
 			 }
 		 }
 		 if( s instanceof AssignStmt ) {
 			 Value dest = ( (AssignStmt) s ).getLeftOp();
 			 if( dest.getType() instanceof RefLikeType && !(dest instanceof NullConstant) ) {
 
 				 Node destNode = srcmpag.buildNodeForValue( dest );
 				 destNode = srcmpag.parameterize( destNode, srcContext );
 				 destNode = destNode.getReplacement(this);
 
 				 Node retNode = PAGNodeFactory.v().makeRet(tgtmpag.getMethod());
 				 retNode = tgtmpag.parameterize( retNode, tgtContext );
 				 retNode = retNode.getReplacement(this);
 
 				 addEdge( retNode, destNode );
 				 recordCallAssign(ie, new Pair(retNode, destNode));
 				 callToMethod.put(ie, srcmpag.getMethod());
 			 }
 		 }
 	 }
 	 /* End of package methods. */
 
 	 protected SparkOptions opts;
 
 	 protected Map<Object, Object> simple = new HashMap<Object, Object>();
 	 protected Map<Object, Object> load = new HashMap<Object, Object>();
 	 protected Map<Object, Object> store = new HashMap<Object, Object>();
 	 protected Map<Object, Object> alloc = new HashMap<Object, Object>();
 
 	 protected Map<Object, Object> simpleInv = new HashMap<Object, Object>();
 	 protected Map<Object, Object> loadInv = new HashMap<Object, Object>();
 	 protected Map<Object, Object> storeInv = new HashMap<Object, Object>();
 	 protected Map<Object, Object> allocInv = new HashMap<Object, Object>();
 
 	 protected boolean addToMap( Map<Object, Object> m, Node key, Node value ) {
 		 Object valueList = m.get( key );
 
 		 if( valueList == null ) {
 			 m.put( key, valueList = new HashSet(4) );
 		 } else if( !(valueList instanceof Set) ) {
 			 Node[] ar = (Node[]) valueList;
 			 HashSet<Node> vl = new HashSet<Node>(ar.length+4);
 			 m.put( key, vl );
 			 for (Node element : ar)
 				 vl.add( element );
 			 return vl.add( value );
 			 /*
 	    Node[] ar = (Node[]) valueList;
             Node[] newar = new Node[ar.length+1];
             for( int i = 0; i < ar.length; i++ ) {
                 Node n = ar[i];
                 if( n == value ) return false;
                 newar[i] = n;
             }
             newar[ar.length] = value;
             m.put( key, newar );
             return true;
 			  */
 		 }
 		 return ((Set<Node>) valueList).add( value );
 	 }
 
 	 //ofcg is only here to pass around the ofcg with the pag.
 	 //the pag does not use the ofcg; so the ofcg is the "active" component
 	 //a propagator tells the ofcg when a node was updated and the ofcg will then add nodes to the pag
 	 //therefore: the ofcg can contain fewer methods than the pag, but never more
 	 protected OnFlyCallGraph ofcg;
 	 private final ArrayList<VarNode> dereferences = new ArrayList<VarNode>();
 	 protected TypeManager typeManager;
 	 
 	 public void setNativeMethodDriver(NativeMethodDriver driver) {
 		 this.nativeMethodDriver = driver;
 	 }
 	 public NativeMethodDriver nativeMethodDriver() {
 		 return this.nativeMethodDriver;
 	 }
 	 protected NativeMethodDriver nativeMethodDriver;
 
 	 public HashMultiMap callAssigns() {
 		 return this.callAssigns;
 	 }
 	 protected HashMultiMap /* InvokeExpr -> Set[Pair] */ callAssigns = new HashMultiMap();
 
 	 public Map<InvokeExpr, SootMethod> callToMethod() {
 		 return this.callToMethod;
 	 }
 	 protected Map<InvokeExpr, SootMethod> callToMethod = new HashMap<InvokeExpr, SootMethod>(); 
 
 	 public Map<InvokeExpr, Node> virtualCallsToReceivers() {
 		 return this.virtualCallsToReceivers;
 	 }
 	 protected Map<InvokeExpr, Node> virtualCallsToReceivers = new HashMap<InvokeExpr, Node>();
 
 }
 
