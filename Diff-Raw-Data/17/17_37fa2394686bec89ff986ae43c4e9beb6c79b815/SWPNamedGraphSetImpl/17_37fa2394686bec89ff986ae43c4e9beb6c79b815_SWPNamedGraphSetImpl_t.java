 /*
  * Created on 24-Nov-2004
 */
 package de.fuberlin.wiwiss.ng4j.swp.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.axis.components.uuid.SimpleUUIDGen;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.mem.GraphMem;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFactory;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.NiceIterator;
 import com.hp.hpl.jena.util.iterator.WrappedIterator;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.ng4j.sparql.NamedGraphDataset;
 import de.fuberlin.wiwiss.ng4j.swp.SWPAuthority;
 import de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraph;
 import de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.swp.SWPWarrant;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPBadDigestException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPBadSignatureException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPInvalidKeyException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPNoSuchDigestMethodException;
 import de.fuberlin.wiwiss.ng4j.swp.util.FileUtils;
 import de.fuberlin.wiwiss.ng4j.swp.util.OpenPGPUtils;
 import de.fuberlin.wiwiss.ng4j.swp.util.PKCS12Utils;
 import de.fuberlin.wiwiss.ng4j.swp.util.SWPSignatureUtilities;
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.SWP;
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.SWP_V;
 
 
 /**
  * 
  * Last commit info    :   $Author: jenpc $
 * $Date: 2009/06/08 20:21:21 $
 * $Revision: 1.29 $
  * 
  * @author Chris Bizer.
  * @author Rowland Watkins.
  */
 public class SWPNamedGraphSetImpl extends NamedGraphSetImpl implements SWPNamedGraphSet
 {
 	protected static final Log logger = LogFactory.getLog( SWPNamedGraphSetImpl.class );
 	protected static final boolean debug = logger.isDebugEnabled();
 	//This means we no longer have to rely on a not-so-well known uuid impl.
 	//Now dependent on Axis.
 	protected SimpleUUIDGen uuidGen = new SimpleUUIDGen ();
 
 	protected NamedGraphDataset thisAsDS;
 	 
 	//Some constants so we don't make an strange typos in queries
 //	 private static final String QUERY_NODE_GRAPH = "?graph";
 //	 private static final String QUERY_NODE_WARRANT = "?warrant";
 	// TODO use or remove these other constants
 //	 private static final String QUERY_NODE_SIG = "signature";
 //	 private static final String QUERY_NODE_CERT = "certificate";
 //	 private static final String QUERY_NODE_SMETHOD = "smethod";
 //	 private static final String QUERY_NODE_DIGEST = "digest";
 //	 private static final String QUERY_NODE_DMETHOD = "dmethod";
 	 
 	public static final String NL = System.getProperty("line.separator") ;
 
 	public SWPNamedGraphSetImpl ()
 	{
 		thisAsDS = new NamedGraphDataset( this );
 	}
 
 	protected boolean actOnGraphs( SWPAuthority authority,
 			ArrayList<Node> listOfAuthorityProperties,
 			Node property, // typically SWP.assertedBy or SWP.quotedBy
 			List<Node> listOfGraphNames, // the particular graphs to act on; use null if all graphs in the set should be acted on
 			Node digestMethod, // the method to use when creating the digest; use null if digest should not be included
 			List<Triple> additionalWarrantStatements // additional triples to add to the warrant; null for none
 			) {
 		
 		// Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		
 		// Assert or quote all graphs in the graphset or in the list provided. 
 		Iterator graphNameIterator;
 		if ( listOfGraphNames != null ) 
 		{
 			graphNameIterator = listOfGraphNames.iterator();
 		} else 
 		{
 			graphNameIterator = this.listGraphs();
 		}
         while (graphNameIterator.hasNext()) {
         	NamedGraph currentGraph = null;
 			Object next = graphNameIterator.next();
 			if ( next instanceof Node )
 			{
 				currentGraph = this.getGraph( ( Node ) next );
 			}
 //			else if ( next instanceof String )
 //			{
 //				currentGraph = ( NamedGraph ) this.getGraph( ( String ) next );
 //			}
 			else if ( next instanceof NamedGraph )
 			{
 				currentGraph = ( NamedGraph ) next;
 			}
             warrantGraph.add(new Triple(currentGraph.getGraphName(), property, warrantGraph.getGraphName()));
             
             if ( digestMethod != null ) 
             {
     			// calculate and add the graph's digest to the warrant
             	try 
      			{
      				String graphDigest = SWPSignatureUtilities.calculateDigest( currentGraph, digestMethod );
      				warrantGraph.add( new Triple( currentGraph.getGraphName(), 
      											SWP.digest, 
      											Node.createLiteral( graphDigest, null, XSDDatatype.XSDbase64Binary ) ) );
      				warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.digestMethod, digestMethod ) );
      			} 
      			catch ( SWPNoSuchDigestMethodException e ) 
      			{
      				logger.error( e.getMessage() );
      				return false;
      			}
             }
         }
         
         // Add a description of the authorty to the warrant graph
         authority.addDescriptionToGraph(warrantGraph, listOfAuthorityProperties);
         
         if ( additionalWarrantStatements != null ) {
         	Iterator<Triple> additionalStmtsIterator = additionalWarrantStatements.iterator();
         	while ( additionalStmtsIterator.hasNext() ) {
         		Triple tripleToAdd = additionalStmtsIterator.next();
         		warrantGraph.add(tripleToAdd);
         	}
         }
         
 		// Add warrant graph to graphset
 		this.addGraph(warrantGraph);
         return true;
 	}
 
     public boolean swpAssert(SWPAuthority authority, ArrayList<Node> listOfAuthorityProperties) {
 		return actOnGraphs(authority, listOfAuthorityProperties, SWP.assertedBy, null, null, null);
     }
 
     public boolean swpAssert(SWPAuthority authority) {
         return swpAssert(authority, new ArrayList<Node>());
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#swpQuote(de.fuberlin.wiwiss.ng4j.swp.SWPAuthority, java.util.ArrayList)
      */
     public boolean swpQuote( SWPAuthority authority, ArrayList<Node> listOfAuthorityProperties ) 
 	{
     	return actOnGraphs(authority, listOfAuthorityProperties, SWP.quotedBy, null, null, null);
     }
 
     public boolean swpQuote( SWPAuthority authority ) 
     {
         return swpQuote( authority, new ArrayList<Node>() );
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#assertGraphs(java.util.ArrayList, de.fuberlin.wiwiss.ng4j.swp.SWPAuthority, java.util.ArrayList)
      */
     public boolean assertGraphs( ArrayList<Node> listOfGraphNames, SWPAuthority authority, ArrayList<Node> listOfAuthorityProperties ) 
 	{
     	return actOnGraphs(authority, listOfAuthorityProperties, SWP.assertedBy, listOfGraphNames, null, null);
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#quoteGraphs(java.util.ArrayList, de.fuberlin.wiwiss.ng4j.swp.SWPAuthority, java.util.ArrayList)
      */
     public boolean quoteGraphs( ArrayList<Node> listOfGraphNames, SWPAuthority authority, ArrayList<Node> listOfAuthorityProperties ) 
     {
     	return actOnGraphs(authority, listOfAuthorityProperties, SWP.quotedBy, listOfGraphNames, null, null);
     }
 
 	protected boolean actOnGraphsAndIncludeSignature( SWPAuthority authority,
 			ArrayList<Node> listOfAuthorityProperties,
 			Node property, // typically SWP.assertedBy or SWP.quotedBy
 			List<String> listOfGraphURIs, // the particular graphs to act on; use null if all graphs in the set should be acted on
 			Node digestMethod, // the method to use when creating the digest
 			Node signatureMethod,
 			String keystore,
 			String password,
 			List<Triple> additionalWarrantStatements // additional triples to add to the warrant; null for none
 			) throws SWPBadSignatureException
 	{
 		ArrayList<Node> graphsAsserted = new ArrayList<Node>();
 		ArrayList<Node> graphsIgnored = new ArrayList<Node>();
 		
 		// Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		
 		// Add authority properties supplied by user into warrantgraph.
 		authority.addDescriptionToGraph( warrantGraph, listOfAuthorityProperties );
 		
 		if ( additionalWarrantStatements != null ) {
         	Iterator<Triple> additionalStmtsIterator = additionalWarrantStatements.iterator();
         	while ( additionalStmtsIterator.hasNext() ) {
         		Triple tripleToAdd = additionalStmtsIterator.next();
         		warrantGraph.add(tripleToAdd);
         	}
         }
 		
 		// Assert all graphs in the graphset or all graphs in the list.
 		Iterator graphIterator;
 		if ( listOfGraphURIs != null )
 		{
 			graphIterator = listOfGraphURIs.iterator();
 		} 
 		else
 		{
 			graphIterator = this.listGraphs();
 			
 			// Query set to see if any previous warrants have been signed.
 			// Note that this query always uses swp:assertedBy (and never quotedBy) because warrants assert themselves.
 //			String warrantQuery = "SELECT * WHERE (?graph swp:assertedBy ?graph) (?graph swp:signature ?signature) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 			String warrantQuery = "SELECT ?warrant" + NL
 				+ "WHERE { GRAPH ?warrant {" + NL
 				+ "?warrant <" + SWP.assertedBy + "> ?warrant ." + NL
 				+ "?warrant <" + SWP.signature + "> ?signature" + NL
 				+ " } }";
 			
 			QueryExecution qe = QueryExecutionFactory.create( warrantQuery, thisAsDS );
 			ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
 			if ( results.hasNext() )
 			{
 	            while ( results.hasNext() )
 	            {
 	            	QuerySolution sol = results.nextSolution();
 	            	Node graphNode = sol.get("?warrant").asNode();
 		            if ( debug )
 		            	logger.debug( graphNode );
 					
 					NamedGraph warrant = this.getGraph( graphNode );
 					// If we find a signed warrant graph, check that all graphs are asserted by it.
 					// TODO we probably want to make this more intelligent to handle graphs that
 					// have not already been asserted.
 					// An example of this is when a NamedGraphSet has been updated - we'll want
 					// to assert all new graphs. Current code does not really support this.
 					while ( graphIterator.hasNext() ) 
 				    {
 						NamedGraph currentGraph = ( NamedGraph ) graphIterator.next();
 						if ( warrant.contains( currentGraph.getGraphName(), property, graphNode ) )
 						{
 							logger.warn( "Warrant graph: "+currentGraph.getGraphName()+" already " + property + " and signed; skipping." );
 						}        
 					}  
 	            }
 	            return false;
 			}
 		}
 		
 		// if we don't find a signed warrant graph, just assert all graphs
 		// and sign the warrant.
 		while ( graphIterator.hasNext() ) 
 	    {
 			NamedGraph currentGraph = null;
 			Object next = graphIterator.next();
 			if ( next instanceof String )
 			{
 				if ( this.containsGraph( (String)next ) )
 				{
 					currentGraph = this.getGraph( ( String ) next );
 				}
 				else
 				{
 					logger.warn("No such graph: " + next);
 					// skip
 					continue;
 				}
 			}
 			else if ( next instanceof NamedGraph )
 			{
 				currentGraph = ( NamedGraph ) next;
 			}
 			
 			if ( next instanceof String )
 			{
 				if ( currentGraph.contains( currentGraph.getGraphName(), property, Node.ANY ) )
 				{
 					logger.warn( "Graph: "+currentGraph.getGraphName()+" already " + property + "; skipping.");
 					graphsIgnored.add( currentGraph.getGraphName() );
 					continue;
 				}
 			}
 
 			graphsAsserted.add( currentGraph.getGraphName() );
 			
 			warrantGraph.add( new Triple( currentGraph.getGraphName(), property, warrantGraph.getGraphName() ) );
 			
 			// calculate and add the graph's digest to the warrant
 			try 
 			{
 				String graphDigest = SWPSignatureUtilities.calculateDigest( currentGraph, digestMethod );
 				warrantGraph.add( new Triple( currentGraph.getGraphName(), 
 											SWP.digest, 
 											Node.createLiteral( graphDigest, null, XSDDatatype.XSDbase64Binary ) ) );
 				warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.digestMethod, digestMethod ) );
 			} 
 			catch ( SWPNoSuchDigestMethodException e ) 
 			{
 				logger.error( e.getMessage() );
 				return false;
 			} 
 		}  
 		
 		Iterator<Node> git = graphsAsserted.iterator();
 		Iterator<Node> iit = graphsIgnored.iterator();
 		
 		if ( git.hasNext() )
 		{
 			if ( debug )
 			{
 				logger.debug( "Graphs to be " + property + ": " );
 			
 				while ( git.hasNext() )
 				{
 					logger.debug( git.next() );
 				}
 			}
 			
 			Object pkey = null;
 	    	// Sign the warrant graph now.
 			String warrantGraphSignature = null;
 			try 
 			{
 				if ( FileUtils.getExtension( keystore ).equals( "p12" ) )
 				{
 					pkey = PKCS12Utils.decryptPrivateKey( keystore, password );
 				}
 				else if ( FileUtils.getExtension( keystore ).equals( "asc" ) )
 				{
 					pkey = OpenPGPUtils.decryptPGP( keystore, password );
 				}
 	    	
 				if ( pkey != null )
 				{
 					// remember to add everything that is required to the graph before signing
 					// and adding the signature, otherwise we can NEVER verify the signature.
 					warrantGraph.add( new Triple( warrantGraph.getGraphName(),
 												SWP.signatureMethod,
 												signatureMethod ) );
 					warrantGraphSignature = SWPSignatureUtilities.calculateSignature( warrantGraph, signatureMethod, pkey );
 				
 					warrantGraph.add( new Triple( warrantGraph.getGraphName(), 
 												SWP.signature, 
 												Node.createLiteral( warrantGraphSignature, null, XSDDatatype.XSDbase64Binary ) ) );
 				}
 				else throw new SWPInvalidKeyException( "Private key empty." );
 			
 			} 
 			catch ( Exception e ) 
 			{
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			}  
 			
 			// Add warrant graph to graphset
 			this.addGraph( warrantGraph );
 			return true;
 		}
 		else if( iit.hasNext() )
 		{
 			logger.warn( "Graphs already " + property + " and ignored: " );
 			if ( debug )
 			{
 				while ( iit.hasNext() )
 				{
 					logger.debug( iit.next() );
 				}
 			}
 			return false;
 		}
 		else
 			return false;
 	}
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#assertWithSignature(de.fuberlin.wiwiss.ng4j.swp.SWPAuthority, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, java.util.ArrayList, java.lang.String, java.lang.String)
      */
     public boolean assertWithSignature( SWPAuthority authority, 
     									Node signatureMethod, 
     									Node digestMethod, 
     									ArrayList<Node> listOfAuthorityProperties, 
     									String keystore,
     									String password ) 
 	throws SWPBadSignatureException, SWPBadDigestException 
     {
     	return actOnGraphsAndIncludeSignature(authority, listOfAuthorityProperties, SWP.assertedBy, null, digestMethod,
     			signatureMethod, keystore, password, null);
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#quoteWithSignature(de.fuberlin.wiwiss.ng4j.swp.SWPAuthority, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, java.util.ArrayList, java.lang.String, java.lang.String)
      */
     public boolean quoteWithSignature( SWPAuthority authority, 
     									Node signatureMethod, 
     									Node digestMethod, 
     									ArrayList<Node> listOfAuthorityProperties, 
     									String keystore,
     									String password ) throws SWPBadSignatureException
     {
     	return actOnGraphsAndIncludeSignature(authority, listOfAuthorityProperties, SWP.quotedBy, null, digestMethod,
     			signatureMethod, keystore, password, null);
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#assertGraphsWithSignature(java.util.ArrayList, de.fuberlin.wiwiss.ng4j.swp.SWPAuthority, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, java.util.ArrayList, java.lang.String, java.lang.String)
      */
     public boolean assertGraphsWithSignature( ArrayList<String> listOfGraphURIs, 
     										SWPAuthority authority, 
     										Node signatureMethod, 
     										Node digestMethod, 
     										ArrayList<Node> listOfAuthorityProperties, 
     										String keystore,
     										String password ) throws SWPBadSignatureException
     {
     	return actOnGraphsAndIncludeSignature(authority, listOfAuthorityProperties, SWP.assertedBy, listOfGraphURIs, digestMethod,
     			signatureMethod, keystore, password, null);
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#getAllWarrants(de.fuberlin.wiwiss.ng4j.swp.SWPAuthority)
      */
     public ExtendedIterator getAllWarrants( SWPAuthority authority ) 
     {
 //		String warrantQuery = "SELECT * WHERE ?warrant (?warrant swp:assertedBy ?warrant) (?warrant swp:authority <"+authority.getID()+">) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		String warrantQuery = "SELECT ?warrant" + NL
 			+ "WHERE { GRAPH ?warrant {" + NL
 			+ "?warrant <" + SWP.assertedBy + "> ?warrant ." + NL
 			+ "?warrant <" + SWP.authority + "> <" + authority.getID()+">" + NL
 			+ " } }";
 		
 		QueryExecution qe = QueryExecutionFactory.create( warrantQuery, thisAsDS );
 		final ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
 		
         return new NiceIterator()
         {
 			
 			/* (non-Javadoc)
 			 * @see com.hp.hpl.jena.util.iterator.NiceIterator#hasNext()
 			 */
 			@Override
 			public boolean hasNext() 
 			{
 				return results.hasNext();
 			}
 
 			/* (non-Javadoc)
 			 * @see com.hp.hpl.jena.util.iterator.NiceIterator#next()
 			 */
 			@Override
 			public Object next() 
 			{
 				QuerySolution s = results.nextSolution();
 				Node graphURI = s.get( "?warrant" ).asNode();
 				SWPWarrant warrant = new SWPWarrantImpl( SWPNamedGraphSetImpl.this.getGraph( graphURI ) ); 
 				return warrant;
 			}
         };
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#getAllAssertedGraphs(de.fuberlin.wiwiss.ng4j.swp.SWPAuthority)
      */
     public ExtendedIterator getAllAssertedGraphs( SWPAuthority authority ) 
 	{
     	return this.getGraphsWithProperty(authority, SWP.assertedBy);
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#getAllQuotedGraphs(de.fuberlin.wiwiss.ng4j.swp.SWPAuthority)
      */
     public ExtendedIterator getAllQuotedGraphs( SWPAuthority authority ) 
 	{
     	return this.getGraphsWithProperty(authority, SWP.quotedBy);
     }
 
     protected ExtendedIterator getGraphsWithProperty( SWPAuthority authority, Node property ) {
     	String queryString = "SELECT ?graph" + NL
 			+ "WHERE { GRAPH ?warrant {" + NL
 			+ "?graph <" + property.getURI() + "> ?warrant ." + NL
 			+ "?warrant <" + SWP.authority + "> <" + authority.getID()+">" + NL
 			+ " } }";
     	
     	return getGraphsByQuery(queryString, "?graph");
 //    			"SELECT * WHERE (?graph <" + property.getURI() + "> ?wg) (?wg <http://www.w3.org/2004/03/trix/swp-2/authority> <"+authority.getID()+">)", 
 //    			"graph");
     }
 
     protected ExtendedIterator getGraphsByQuery(String query, String resultVariable) {
     	Collection<NamedGraph> graphs = new ArrayList<NamedGraph>();
     	Set<Node> names = new HashSet<Node>();
     	QueryExecution qe = QueryExecutionFactory.create( query, thisAsDS );
         ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
         while ( results.hasNext() ) {
         	QuerySolution solution = results.nextSolution();
         	Node node = solution.get(resultVariable).asNode();
 
 			// Make sure there are no duplicates
 			if (names.add(node)) {
 				NamedGraph graph = getGraph(node);
 				if (graph != null) {
 					graphs.add(graph);
 				}
 			}
         }
         return WrappedIterator.create(graphs.iterator());
     }
         
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet#verifyAllSignatures()
      */
     public boolean verifyAllSignatures() 
     {
     	//First, let's remove any previous verification
     	//graphs.
     	if ( this.containsGraph( SWP_V.default_graph ) )
     	{
     		this.removeGraph( SWP_V.default_graph );
     	}
     	//Now, we can create a new verification graph to record
     	//results.
     	NamedGraph verificationGraph = this.createGraph( SWP_V.default_graph );
     	Iterator<NamedGraph> ngsIt = this.listGraphs();
     	
     	// For each NamedGraph in the NamedGraphSet, we will check for 
     	// the swp:assertedBy triple. We then take the object of that
     	// triple and query it to find if it contains a signature and
     	// authority with an associated certificate.
     	while ( ngsIt.hasNext() )
     	{
     		Quad quad = null;
     		NamedGraph ng = ngsIt.next();
         	
     		Iterator it = findQuads( Node.ANY, Node.ANY, SWP.assertedBy, ng.getGraphName() );
 			
     		if ( it.hasNext() )
     		{	
     			quad = (Quad) it.next();
     			String ngName = ng.getGraphName().toString();
 //    			String warrantQuery = "SELECT * WHERE <"+ng.getGraphName().toString()+"> (<"+ng.getGraphName().toString()+"> swp:signature ?signature . <"+ng.getGraphName().toString()+"> swp:signatureMethod ?smethod . <"+ng.getGraphName().toString()+"> swp:authority ?authority . ?authority swp:X509Certificate ?certificate) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 	            String warrantQuery = "SELECT ?signature ?smethod ?certificate" + NL
 	            	+ "WHERE { GRAPH <" + ngName + "> { " + NL
 	            	+ "<" + ngName + "> <" + SWP.signature + "> ?signature ." + NL
 	            	+ "<" + ngName + "> <" + SWP.signatureMethod + "> ?smethod ." + NL
 	            	+ "<" + ngName + "> <" + SWP.authority + "> ?authority ." + NL
 	            	+ "?authority <" + SWP.X509Certificate + "> ?certificate" + NL
 	            	+ " } }";
 	            QueryExecution qe = QueryExecutionFactory.create( warrantQuery, thisAsDS );
 				ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
 				if ( results.hasNext() )
 				{
 	                while ( results.hasNext() )
 	                {
 	                	QuerySolution solution = results.nextSolution();
         	            Literal cert = solution.getLiteral( "?certificate" );
         	            Literal signature = solution.getLiteral( "?signature" );
         	            Node signatureMethod = solution.get( "?smethod" ).asNode();
         	            
         	            // If the certificate and signature are not null, we can use these
         	            // to verify the signature. 
         	            // We, of course, need to provide the warrant graph as it
         	            // was *before* adding the signature. We therefore remove,
         	            // the signature and add back again later.
        	            if ( ( cert != null ) && ( signature != null )  )
         	            {
             	            String certificate = cert.getLexicalForm();
             	            String certs = "-----BEGIN CERTIFICATE-----\n" +
             	            					certificate + "\n-----END CERTIFICATE-----";
         	                try 
         	                {
         	                	ExtendedIterator exit = ng.find( ng.getGraphName(), SWP.signature, Node.ANY );
         	                	ArrayList<Triple> li = new ArrayList<Triple>();
         	                	while ( exit.hasNext() )
         	                	{
         	                		li.add( ( Triple )exit.next() );
         	                	}
         	                	for ( Iterator<Triple> i = li.iterator(); i.hasNext(); )
         	                	{
         	                		ng.delete( i.next() );
         	                	}
         	                	// If the warrant's signature is ok, we want to test whether the graph
         	                	// digests of the graphs it asserts are ok. 
         	                	// We simply take the graphs and get their digests and compare the 
         	                	// string representations.
         	                	// After this, we then add to our verification graph the results of
         	                	// this process.
         	                    if ( SWPSignatureUtilities.validateSignature( ng, signatureMethod, signature.getLexicalForm(), certs ) )
         	                    {
         	                    	// The warrant graph's signature check succeeded
         	                    	recordWarrantGraphSignatureCheckResult( verificationGraph, ng, true);
 									
 //									String asserteddigestQuery = "SELECT * WHERE (?graph swp:assertedBy <"+ng.getGraphName().toString()+"> . ?graph swp:digest ?digest . ?graph swp:digestMethod ?dmethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 									String asserteddigestQuery = "SELECT ?graph ?digest ?dmethod" + NL
 						            + "WHERE { GRAPH <" + ngName + "> { " + NL
 						            	+ "?graph <" + SWP.assertedBy + "> <" + ngName + "> ." + NL
 						            	+ "?graph <" + SWP.digest + "> ?digest ." + NL
 						            	+ "?graph <" + SWP.digestMethod + "> ?dmethod" + NL
 						            	+ " } }";
 									QueryExecution dQE = QueryExecutionFactory.create( asserteddigestQuery, thisAsDS );
 									ResultSet dResults = ResultSetFactory.copyResults( dQE.execSelect() );
 									
 //									String quoteddigestQuery = "SELECT * WHERE (?graph swp:quotedBy <"+ng.getGraphName().toString()+"> . ?graph swp:digest ?digest . ?graph swp:digestMethod ?dmethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 									String quoteddigestQuery = "SELECT ?graph ?digest ?dmethod" + NL
 						            	+ "WHERE { GRAPH <" + ngName + "> { " + NL
 						            	+ "?graph <" + SWP.quotedBy + "> <" + ngName + "> ." + NL
 						            	+ "?graph <" + SWP.digest + "> ?digest ." + NL
 						            	+ "?graph <" + SWP.digestMethod + "> ?dmethod" + NL
 						           		+ " } }";
 									QueryExecution qQE = QueryExecutionFactory.create( quoteddigestQuery, thisAsDS );
 									ResultSet qResults = ResultSetFactory.copyResults( qQE.execSelect() );
 									
 									if ( dResults.hasNext() )
 									{
 										while ( dResults.hasNext() )
 										{
 											QuerySolution dS = dResults.nextSolution();
 											Node graph = dS.get( "?graph" ).asNode();
 											Literal digest = dS.getLiteral( "?digest" );
 					        	        	Node digestMethod = dS.get( "?dmethod" ).asNode();
 											
 											String digest1 = SWPSignatureUtilities.calculateDigest( this.getGraph( graph ), digestMethod );
 											if ( digest1.equals( digest.getLexicalForm() ) )
 											{
 												// The graph's digest check succeeded
 												recordDigestCheckResult( verificationGraph, ng, graph, true);
 											}
 											else
 											{
 												// The graph's digest check failed
 												recordDigestCheckResult( verificationGraph, ng, graph, false);
 											}
 										}
 									}
 									else if ( qResults.hasNext() )
 									{
 										while ( qResults.hasNext() )
 										{
 											QuerySolution qS = qResults.nextSolution();
 											Node graph = qS.get( "?graph" ).asNode();
 											Literal digest = qS.getLiteral( "?digest" );
 					        	        	Node digestMethod = qS.get( "?dmethod" ).asNode();
 										
 											String digest1 = SWPSignatureUtilities.calculateDigest( this.getGraph( graph ), digestMethod );
 											if ( digest1.equals( digest.getLexicalForm() ) )
 											{
 												// The graph's digest check succeeded
 												recordDigestCheckResult( verificationGraph, ng, graph, true);
 											}
 											else 
 											{
 												// The graph's digest check failed
 												recordDigestCheckResult( verificationGraph, ng, graph, false);
 											}
 										}
 									}
         	                    }
         	                    else
         	                    {
         	                    	// The warrant graph's signature check failed
         	                    	recordWarrantGraphSignatureCheckResult( verificationGraph, ng, false);
 								}
         	                    
         	                    for ( Iterator<Triple> i = li.iterator(); i.hasNext(); )
         	                    {
         	                    	ng.add( i.next() );
         	                    }
         	                }
         	                catch ( Exception e ) 
         	                {
 								logger.error( e.getMessage() );
 								return false;
 							}
         	            }
         	            else
 						{
         	            	// The warrant graph's certificate or signature is empty
         	            	recordMissingCertificateOrSignature( verificationGraph, ng );
 						}
 	                }
     			}	
 				else
 				{
 					// The warrant graph is incomplete
 					recordIncompleteWarrantGraph( verificationGraph, ng );
 				}
     		}
     		else
     			continue;
     			
     	}		
 		return true;
     }
 
     protected void recordWarrantGraphSignatureCheckResult( NamedGraph verificationGraph, 
     		NamedGraph warrantGraph, boolean succeeded ) {
     	
     	if ( succeeded ) {
     		logger.info( "Warrant graph " + warrantGraph.getGraphName().toString() + " successfully verified." );
         	verificationGraph.add( new Triple( warrantGraph.getGraphName(), SWP_V.successful, Node.createLiteral( "true" ) ) );
     	} else {
     		logger.error( "Warrant graph " + warrantGraph.getGraphName().toString() + " verification failure!" );
         	verificationGraph.add( new Triple( warrantGraph.getGraphName(), SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
     	}
     }
 
     protected void recordDigestCheckResult( NamedGraph verificationGraph,
     		NamedGraph warrantGraph, Node graphChecked, boolean succeeded ) {
     	
     	if ( succeeded ) {
     		verificationGraph.add( new Triple( graphChecked, SWP_V.successful, Node.createLiteral( "true" ) ) );
     	} else {
     		verificationGraph.add( new Triple( graphChecked, SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
     	}
     }
 
     protected void recordMissingCertificateOrSignature( NamedGraph verificationGraph,
     		NamedGraph warrantGraph ) {
     	
     	logger.error( "Warrant graph " + warrantGraph.getGraphName().toString() + " verification failure!" );
     	verificationGraph.add( new Triple( warrantGraph.getGraphName(), SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
     }
 
     protected void recordIncompleteWarrantGraph( NamedGraph verificationGraph,
     		NamedGraph warrantGraph ) {
     	
 		logger.error( "Warrant graph " + warrantGraph.getGraphName().toString() + " verification failure!" );
     	verificationGraph.add( new Triple( warrantGraph.getGraphName(), SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
     }
 
 	/* (non-Javadoc)
 	 * @see de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl#createNamedGraphInstance(com.hp.hpl.jena.graph.Node)
 	 */
 	@Override
 	protected NamedGraph createNamedGraphInstance( Node graphName ) 
 	{
 		if ( !graphName.isURI() ) 
 		{
 			throw new IllegalArgumentException( "Graph names must be URIs" );
 		}
 		return new SWPNamedGraphImpl( graphName, new GraphMem() );
 	}
 
     protected SWPNamedGraph createNewWarrantGraph() 
     {
 		Node warrantGraphName = Node.createURI( getNewWarrantGraphName(uuidGen.nextUUID()) );
 		SWPNamedGraph warrantGraph = new SWPNamedGraphImpl( warrantGraphName, new GraphMem() );
 		warrantGraph.add( new Triple( warrantGraphName, SWP.assertedBy, warrantGraphName ) );
         return warrantGraph;
     }
 
     protected String getNewWarrantGraphName(String uuid) {
     	return "urn:uuid:" + uuid;
     }
 
 }
 
 /*
  *  (c)   Copyright 2004 - 2009 Chris Bizer (chris@bizer.de) & Rowland Watkins (rowland@grid.cx) 
  *   	  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
