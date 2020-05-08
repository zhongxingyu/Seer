 package de.fuberlin.wiwiss.ng4j.impl.idbased;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.shared.ReificationStyle;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 import de.fuberlin.wiwiss.jenaext.NodeDictionary;
 import de.fuberlin.wiwiss.jenaext.impl.IdBasedGraphMem;
 import de.fuberlin.wiwiss.jenaext.impl.NodeDictionaryImpl;
 
 
 /**
  * Named graph set that is particularily well suited for the Semantic Web
  * client.
  * Uses specific graph implementations that are optimized for read-only access.
  * Furthermore, during the execution of triple pattern queries these graph
  * implementations use identifiers for RDF nodes instead of the nodes themself.
  *
  * @author Olaf Hartig
  */
 public class IdBasedNamedGraphSetImpl extends NamedGraphSetImpl
 {
 	// members
 
 	final protected NodeDictionary nodeDict;
 
 
 	// initialization
 
 	public IdBasedNamedGraphSetImpl ()
 	{
 		this( new NodeDictionaryImpl() );
 	}
 
 	public IdBasedNamedGraphSetImpl ( NodeDictionary nodeDict )
 	{
 		if ( nodeDict == null ) {
 			throw new IllegalArgumentException( "The given Node dictionary is null." );
 		}
 
 		this.nodeDict = nodeDict;
 	}
 
 	/**
 	 * Creates a set of identifier-based named graphs by copying the given set.
 	 * This constructor copies the given set but it does not create a new
	 * {@link IdBasedNamedGraph} object for each named graph. Instead, it
 	 * simply copies the references to the graphs as given in the given set.
 	 */
 	public IdBasedNamedGraphSetImpl ( IdBasedNamedGraphSetImpl template )
 	{
 		nodeDict = template.nodeDict;
 		for ( NamedGraph g : template.graphs) {
 			addGraph( g );
 		}
 	}
 
 
 	// accessor methods
 
 	/**
 	 * Returns the node dictionary used by this graph set.
 	 */
 	public NodeDictionary getNodeDictionary ()
 	{
 		return nodeDict;
 	}
 
 
 	// redefinitions of NamedGraphSetImpl methods
 
 	/**
 	 * Uses the RDF graph implementation and the named graph implementation for
 	 * the Semantic Web client ({@link IdBasedGraphMem} and {@link IdBasedNamedGraphImpl}).
 	 * 
 	 * @see de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl#createNamedGraphInstance(com.hp.hpl.jena.graph.Node)
 	 */
 	@Override
 	protected NamedGraph createNamedGraphInstance ( Node graphName )
 	{
 		if ( ! graphName.isURI() ) {
 			throw new IllegalArgumentException( "Graph names must be URIs" );
 		}
 
 		return new IdBasedNamedGraphImpl( graphName, new IdBasedGraphMem(nodeDict,ReificationStyle.Standard) );
 	}
 
 	/**
 	 * Uses the union graph defined below.
 	 * 
 	 * @see de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl#asJenaGraph(com.hp.hpl.jena.graph.Node)
 	 */
 	@Override
 	public Graph asJenaGraph ( Node defaultGraphForAdding )
 	{
 		if ( defaultGraphForAdding != null && ! containsGraph(defaultGraphForAdding) ) {
 			createGraph( defaultGraphForAdding );
 		}
 
 		IdBasedUnionGraph unionGraph = new IdBasedUnionGraph( graphs, nodeDict );
 		if ( defaultGraphForAdding != null ) {
 			unionGraph.setBaseGraph( getGraph(defaultGraphForAdding) );
 		}
 
 		return unionGraph;
 	}
 
 	public String toString ()
 	{
 		return "IdBasedNamedGraphSetImpl with " + countQuads() + " quads in " + countGraphs() + " graphs";
 	}
 
 }
 
 /*
  * (c) Copyright 2009 - 2010 Christian Bizer (chris@bizer.de)
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. The name of the author may not
  * be used to endorse or promote products derived from this software without
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
  * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  */
