 /*
  * Created on 24-Nov-2004
 */
 package de.fuberlin.wiwiss.ng4j.swp.impl;
 
 import java.io.IOException;
 import java.security.NoSuchProviderException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.axis.components.uuid.SimpleUUIDGen;
 import org.apache.log4j.Logger;
 import org.bouncycastle.openpgp.PGPException;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.mem.GraphMem;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.NiceIterator;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 import de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraph;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPAlgorithmNotSupportedException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPBadDigestException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPBadSignatureException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPInvalidKeyException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPNoSuchAlgorithmException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPNoSuchDigestMethodException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPSignatureException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPValidationException;
 import de.fuberlin.wiwiss.ng4j.swp.impl.SWPNamedGraphImpl;
 import de.fuberlin.wiwiss.ng4j.swp.SWPAuthority;
 import de.fuberlin.wiwiss.ng4j.swp.SWPNamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.swp.SWPWarrant;
 import de.fuberlin.wiwiss.ng4j.swp.util.FileUtils;
 import de.fuberlin.wiwiss.ng4j.swp.util.OpenPGPUtils;
 import de.fuberlin.wiwiss.ng4j.swp.util.PKCS12Utils;
 import de.fuberlin.wiwiss.ng4j.swp.util.SWPSignatureUtilities;
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.SWP;
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.SWP_V;
 import de.fuberlin.wiwiss.ng4j.triql.TriQLQuery;
 
 
 /**
  * 
 * Last commit info    :   $Author: cyganiak $
 * $Date: 2008/08/19 12:12:45 $
 * $Revision: 1.14 $
  * 
  * @author Chris Bizer.
  * @author Rowland Watkins.
  */
 public class SWPNamedGraphSetImpl extends NamedGraphSetImpl implements SWPNamedGraphSet
 {
 	 private static final Logger logger = Logger.getLogger( SWPNamedGraphSetImpl.class );
 	 private static boolean debug = logger.isDebugEnabled();
 	 //This means we no longer have to rely on a not-so-well known uuid impl.
 	 //Now dependent on Axis.
 	 private SimpleUUIDGen uuidGen = new SimpleUUIDGen ();
 	 
 	 //Some constants so we don't make an strange typos in queries
 	 private static final String QUERY_NODE_GRAPH = "graph";
 	 private static final String QUERY_NODE_WARRANT = "warrant";
 	 private static final String QUERY_NODE_SIG = "signature";
 	 private static final String QUERY_NODE_CERT = "certificate";
 	 private static final String QUERY_NODE_SMETHOD = "smethod";
 	 private static final String QUERY_NODE_DIGEST = "digest";
 	 private static final String QUERY_NODE_DMETHOD = "dmethod";
    
     public boolean swpAssert(SWPAuthority authority, ArrayList listOfAuthorityProperties) {
 		// Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		// Assert all graphs in the graphset. 
         Iterator graphIterator = this.listGraphs();
         while (graphIterator.hasNext()) {
             NamedGraph currentGraph = (NamedGraph) graphIterator.next();
             warrantGraph.add(new Triple(currentGraph.getGraphName(), SWP.assertedBy, warrantGraph.getGraphName()));
         }
         // Add a description of the authorty to the warrant graph
         authority.addDescriptionToGraph(warrantGraph, listOfAuthorityProperties);
 		// Add warrant graph to graphset
 		this.addGraph(warrantGraph);
         return true;
     }
 
     public boolean swpAssert(SWPAuthority authority) {
         return swpAssert(authority, new ArrayList());
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#swpQuote(de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority, java.util.ArrayList)
      */
     public boolean swpQuote( SWPAuthority authority, ArrayList listOfAuthorityProperties ) 
 	{
         // Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		// Assert all graph in the graphset.
         Iterator graphIterator = this.listGraphs();
         while ( graphIterator.hasNext() ) 
 		{
             NamedGraph currentGraph = ( NamedGraph ) graphIterator.next();
             warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.quotedBy, warrantGraph.getGraphName() ) );
         }
         // Add a description of the authorty to the warrant graph
         authority.addDescriptionToGraph( warrantGraph, listOfAuthorityProperties );
 
 		// Add warrant graph to graphset
 		this.addGraph( warrantGraph );
         return true;
     }
 
    public boolean swpQuote( SWPAuthority authority ) 
    {
         return swpQuote( authority, new ArrayList() );
    }
 
    
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#assertGraphs(java.util.ArrayList, de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority, java.util.ArrayList)
      */
     public boolean assertGraphs( ArrayList listOfGraphNames, SWPAuthority authority, ArrayList listOfAuthorityProperties ) 
 	{
         // Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		// Assert all graph in the list.
         Iterator graphNameIterator = listOfGraphNames.iterator();
         while ( graphNameIterator.hasNext() ) 
 		{
 			NamedGraph currentGraph = null;
 			Object next = graphNameIterator.next();
 			if ( next instanceof Node )
 			{
 				currentGraph = ( NamedGraph ) this.getGraph( ( Node ) next );
 			}
 			else if ( next instanceof String )
 			{
 				currentGraph = ( NamedGraph ) this.getGraph( ( String ) next );
 			}
 			else if ( next instanceof NamedGraph )
 			{
 				currentGraph = ( NamedGraph ) next;
 			}
 			
             warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.assertedBy, warrantGraph.getGraphName() ) );
         }
         // Add a description of the authorty to the warrant graph
         
         authority.addDescriptionToGraph( warrantGraph, listOfAuthorityProperties );
 		
 
 		// Add warrant graph to graphset
 		this.addGraph( warrantGraph );
         return true;
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#assertGraphs(java.util.ArrayList, de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority, java.util.ArrayList)
      */
     public boolean quoteGraphs( ArrayList listOfGraphNames, SWPAuthority authority, ArrayList listOfAuthorityProperties ) 
     {
         // Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		// Assert all graph in the list.
         Iterator graphNameIterator = listOfGraphNames.iterator();
         while ( graphNameIterator.hasNext() ) 
         {
 			NamedGraph currentGraph = null;
 			Object next = graphNameIterator.next();
 			if ( next instanceof Node )
 			{
 				currentGraph = ( NamedGraph ) this.getGraph( ( Node ) next );
 			}
 			else if ( next instanceof String )
 			{
 				currentGraph = ( NamedGraph ) this.getGraph( ( String ) next );
 			}
 			else if ( next instanceof NamedGraph )
 			{
 				currentGraph = ( NamedGraph ) next;
 			}
             
             warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.quotedBy, warrantGraph.getGraphName() ) );
         }
         // Add a description of the authorty to the warrant graph
         
 		authority.addDescriptionToGraph( warrantGraph, listOfAuthorityProperties );
 		
 
 		// Add warrant graph to graphset
 		this.addGraph( warrantGraph );
         return true;
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#assertWithSignature(de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, java.util.ArrayList)
      */
     public boolean assertWithSignature( SWPAuthority authority, 
     									Node signatureMethod, 
     									Node digestMethod, 
     									ArrayList listOfAuthorityProperties, 
     									String keystore,
     									String password ) 
 	throws SWPBadSignatureException, SWPBadDigestException 
     {		
 		//	Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		// Assert all graphs in the graphset.
 		boolean result = false;
 		
 		// Add authority properties supplied by user into warrantgraph.
 		authority.addDescriptionToGraph( warrantGraph, listOfAuthorityProperties );
 		
 		// Query set to see if any previous warrants have been signed.
 		Iterator graphIterator = this.listGraphs();
 		String warrantQuery = "SELECT * WHERE (?graph swp:assertedBy ?graph) (?graph swp:signature ?signature) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         Iterator witr = TriQLQuery.exec( this, warrantQuery );
 		if ( witr.hasNext() )
 		{
             while ( witr.hasNext() )
             {
                 Map results = ( Map ) witr.next();
 	            Node graph = ( Node ) results.get( QUERY_NODE_GRAPH );
 	            if ( debug )
 	            	logger.debug( graph );
 				
 				NamedGraph warrant = this.getGraph( graph );
 				// If we find a signed warrant graph, check that all graphs are asserted by it.
 				// TODO we probably want to make this more intelligent to handle graphs that
 				// have not already been asserted.
 				// An example of this is when a NamedGraphSet has been updated - we'll want
 				// to assert all new graphs. Current code does not really support this.
 				while ( graphIterator.hasNext() ) 
 			    {
 			        
 					NamedGraph currentGraph = ( NamedGraph ) graphIterator.next();
 					if ( warrant.contains( currentGraph.getGraphName(), SWP.assertedBy, graph ) )
 					{
 						logger.warn( "Warrant graph: "+currentGraph.getGraphName()+" already asserted and signed; skipping." );
 					}        
 				}  
             }
 		}
 		// if we don't find a signed warrant graph, just assert all graphs
 		// and sign the warrant.
 		else if ( graphIterator.hasNext() )
 		{
 			while ( graphIterator.hasNext() ) 
 		    {
 				NamedGraph currentGraph = ( NamedGraph ) graphIterator.next();
 				try 
 				{
 					String graphDigest = SWPSignatureUtilities.calculateDigest( currentGraph, digestMethod );
 					warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.assertedBy, warrantGraph.getGraphName() ) );
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
 			catch ( SWPInvalidKeyException e ) 
 			{
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			}  
 			catch ( SWPSignatureException e ) 
 			{	
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( SWPNoSuchAlgorithmException e ) 
 			{
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( NoSuchProviderException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( IOException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( PGPException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( SWPAlgorithmNotSupportedException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			}
 		
         
 			// Add warrant graph to graphset
 			this.addGraph( warrantGraph );
 			result = true;
 		}
 		else
 			result = false;
 		return result;
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#quoteWithSignature(de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, java.util.ArrayList)
      */
     public boolean quoteWithSignature( SWPAuthority authority, 
     									Node signatureMethod, 
     									Node digestMethod, 
     									ArrayList listOfAuthorityProperties, 
     									String keystore,
     									String password ) throws SWPBadSignatureException
     {
 		// Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		// Assert all graphs in the graphset.
 		boolean result = false;
 		
 		// Add authority properties supplied by user into warrantgraph.
 		authority.addDescriptionToGraph( warrantGraph, listOfAuthorityProperties );
 		
 		// Query set to see if any previous warrants have been signed.
 		Iterator graphIterator = this.listGraphs();
 		String warrantQuery = "SELECT * WHERE (?graph swp:assertedBy ?graph) (?graph swp:signature ?signature) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         Iterator witr = TriQLQuery.exec( this, warrantQuery );
 		if ( witr.hasNext() )
 		{
             while ( witr.hasNext() )
             {
                 Map results = ( Map ) witr.next();
 	            Node graph = ( Node ) results.get( QUERY_NODE_GRAPH );
 	            if ( debug )
 	            	logger.debug( graph );
 				
 				NamedGraph warrant = this.getGraph( graph );
 				// If we find a signed warrant graph, check that all graphs are asserted by it.
 				// TODO we probably want to make this more intelligent to handle graphs that
 				// have not already been asserted.
 				// An example of this is when a NamedGraphSet has been updated - we'll want
 				// to assert all new graphs. Current code does not really support this.
 				while ( graphIterator.hasNext() ) 
 			    {
 			        
 					NamedGraph currentGraph = ( NamedGraph ) graphIterator.next();
 					if ( warrant.contains( currentGraph.getGraphName(), SWP.quotedBy, graph ) )
 					{
 						logger.warn( "Warrant graph: "+currentGraph.getGraphName()+" already quoted and signed; skipping." );
 					}        
 				}  
             }
 		}
 		// if we don't find a signed warrant graph, just assert all graphs
 		// and sign the warrant.
 		else if ( graphIterator.hasNext() )
 		{
 			while ( graphIterator.hasNext() ) 
 		    {
 				NamedGraph currentGraph = ( NamedGraph ) graphIterator.next();
 				try 
 				{
 					String graphDigest = SWPSignatureUtilities.calculateDigest( currentGraph, digestMethod );
 					warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.quotedBy, warrantGraph.getGraphName() ) );
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
 			catch ( SWPInvalidKeyException e ) 
 			{
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			}  
 			catch ( SWPSignatureException e ) 
 			{	
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( SWPNoSuchAlgorithmException e ) 
 			{
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( NoSuchProviderException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( IOException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( PGPException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( SWPAlgorithmNotSupportedException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			}
 		
         
 			// Add warrant graph to graphset
 			this.addGraph( warrantGraph );
 			result = true;
 		}
 		else
 			result = false;
 		return result;
         
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#assertGraphsWithSignature(java.util.ArrayList, de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, java.util.ArrayList)
      */
     public boolean assertGraphsWithSignature( ArrayList listOfGraphURIs, 
     										SWPAuthority authority, 
     										Node signatureMethod, 
     										Node digestMethod, 
     										ArrayList listOfAuthorityProperties, 
     										String keystore,
     										String password ) throws SWPBadSignatureException
     {
 		//		 Create a new warrant graph.
 		SWPNamedGraph warrantGraph = createNewWarrantGraph();
 		// Assert all graphs in the graphset.
 		
 		authority.addDescriptionToGraph( warrantGraph, listOfAuthorityProperties );
 		ArrayList graphsAsserted = new ArrayList();
 		ArrayList graphsIgnored = new ArrayList();
 		
         Iterator graphIterator = listOfGraphURIs.iterator();
         while ( graphIterator.hasNext() ) 
         {
 			String graph = ( String )graphIterator.next();
 			if ( this.containsGraph( graph ) )
 			{
 				NamedGraph currentGraph = this.getGraph( graph );
 				if ( currentGraph.contains( currentGraph.getGraphName(), SWP.assertedBy, Node.ANY ) )
 				{
 					logger.warn( "Graph: "+currentGraph.getGraphName()+" already asserted; skipping.");
 					graphsIgnored.add( currentGraph.getGraphName() );
 					continue;
 				}
 				else
 				{
 					graphsAsserted.add( currentGraph.getGraphName() );
 					try 
 					{
 						String graphDigest = SWPSignatureUtilities.calculateDigest( currentGraph, digestMethod );
 						warrantGraph.add( new Triple( currentGraph.getGraphName(), SWP.assertedBy, warrantGraph.getGraphName() ) );
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
 			else
 				continue;
                       
         }  
 		
 		Iterator git = graphsAsserted.iterator();
 		Iterator iit = graphsIgnored.iterator();
 		if ( git.hasNext() )
 		{	
 			if ( debug )
 			{
 				logger.debug( "Graphs to be asserted: " );
 			
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
 			catch ( SWPInvalidKeyException e ) 
 			{
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			}  
 			catch ( SWPSignatureException e ) 
 			{	
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( SWPNoSuchAlgorithmException e ) 
 			{
 				//make the private key unusable
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( NoSuchProviderException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( IOException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( PGPException e ) 
 			{
 				logger.error( e.getMessage() );
 				pkey = null;
 				return false;
 			} 
 			catch ( SWPAlgorithmNotSupportedException e ) 
 			{
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
 			
 			
 			logger.warn( "Graphs already asserted and ignored: " );
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
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#getAllWarrants(de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority)
      */
     
     public ExtendedIterator getAllWarrants( SWPAuthority authority ) 
     {
 		String warrantQuery = "SELECT * WHERE ?warrant (?warrant swp:assertedBy ?warrant) (?graph swp:authority <"+authority.getID()+">) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         final Iterator witr = TriQLQuery.exec( this, warrantQuery );
 		
         return new NiceIterator()
         {
 			
 			public boolean hasNext() 
 			{
 				return witr.hasNext();
 			}
 
 			public Object next() 
 			{
 				Map results =  ( Map ) witr.next();
 				Node graphURI = ( Node ) results.get( QUERY_NODE_GRAPH );
 				SWPWarrant warrant = new SWPWarrantImpl( SWPNamedGraphSetImpl.this.getGraph( graphURI ) ); 
 				return warrant;
 			}
         };
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#getAllAssertedGraphs(de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority)
      */
     
     public ExtendedIterator getAllAssertedGraphs( SWPAuthority authority ) 
 	{
 		String warrantQuery = "SELECT * WHERE (?graph swp:assertedBy ?wg) (?graph swp:authority <"+authority.getID()+">) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         final Iterator witr = TriQLQuery.exec( this, warrantQuery );
 		Node warrant = null;
 		while ( witr.hasNext() )
 		{
 			Map results =  ( Map ) witr.next();
 			Node graphURI = ( Node ) results.get( QUERY_NODE_GRAPH );
 			warrant = graphURI;
 			
 		}
 		final Iterator qit = this.findQuads( Node.ANY, Node.ANY, SWP.assertedBy, warrant );
 		
         return new NiceIterator()
         {
 			
 			public boolean hasNext() 
 			{
 				return qit.hasNext();
 			}
 
 			public Object next() 
 			{
 				Quad quad = ( Quad ) qit.next();
 				NamedGraph graph = SWPNamedGraphSetImpl.this.getGraph( quad.getSubject() );
 
 				return graph;
 			}
         };
     }
 
     /* (non-Javadoc)
      * @see de.fuberlin.wiwiss.ng4j.swp.signature.SWPNamedGraphSet#getAllquotedGraphs(de.fuberlin.wiwiss.ng4j.swp.signature.SWPAuthority)
      */
     
     public ExtendedIterator getAllQuotedGraphs( SWPAuthority authority ) 
     {
    	String warrantQuery = "SELECT * WHERE (?graph swp:quotedBy ?wg) (?graph swp:authority <"+authority.getID()+">) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         final Iterator witr = TriQLQuery.exec( this, warrantQuery );
 		Node warrant = null;
 		while ( witr.hasNext() )
 		{
 			Map results =  ( Map ) witr.next();
 			Node graphURI = ( Node ) results.get( QUERY_NODE_GRAPH );
 			if ( debug )
				logger.debug( "Quoted Graph: " +graphURI );
 			warrant = graphURI;
 			
 		}
 		final Iterator qit = this.findQuads( Node.ANY, Node.ANY, SWP.quotedBy, warrant );
 		
         return new NiceIterator()
         {
 			
 			public boolean hasNext() 
 			{
 				return qit.hasNext();
 			}
 
 			public Object next() 
 			{
 				Quad quad = ( Quad ) qit.next();
 				NamedGraph graph = SWPNamedGraphSetImpl.this.getGraph( quad.getSubject() );
 
 				return graph;
 			}
         };
     }
 
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
     	String canonicalTripleList;
     	Iterator ngsIt = this.listGraphs();
     	
     	// For each NamedGraph in the NamedGraphSet, we will check for 
     	// the swp:assertedBy triple. We then take the object of that
     	// triple and query it to find if it contains a signature and
     	// authority with an associated certificate.
     	while ( ngsIt.hasNext() )
     	{
     		Quad quad = null;
     		NamedGraph ng = ( NamedGraph )ngsIt.next();
         	
     		Iterator it = findQuads( Node.ANY, Node.ANY, SWP.assertedBy, ng.getGraphName() );
 			
     		if ( it.hasNext() )
     		{	
     			quad = ( Quad )it.next();
     			String warrantQuery = "SELECT * WHERE <"+ng.getGraphName().toString()+"> (<"+ng.getGraphName().toString()+"> swp:signature ?signature . <"+ng.getGraphName().toString()+"> swp:signatureMethod ?smethod . <"+ng.getGraphName().toString()+"> swp:authority ?authority . ?authority swp:X509Certificate ?certificate) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 	            Iterator witr = TriQLQuery.exec( this, warrantQuery );
 				if ( witr.hasNext() )
 				{
 	                while ( witr.hasNext() )
 	                {
 	                    Map result = ( Map ) witr.next();
         	            Node cert = ( Node ) result.get( "certificate" );
         	            Node signature = ( Node ) result.get( "signature" );
 						Node signatureMethod = ( Node ) result.get( "smethod" );
         	            String certificate = cert.getLiteral().getLexicalForm();
         	            String certs = "-----BEGIN CERTIFICATE-----\n" +
         	            					certificate + "\n-----END CERTIFICATE-----";
         	            // If the certificate and signature are not null, we can use these
         	            // to verify the signature. 
         	            // We, of course, need to provide the warrant graph as it
         	            // was *before* adding the signature. We therefore remove,
         	            // the signature and add back again later.
         	            if ( ( cert != null ) & ( signature != null )  )
         	            {
         	                try 
         	                {
         	                	Iterator exit = ng.find( ng.getGraphName(), SWP.signature, Node.ANY );
         	                	ArrayList li = new ArrayList();
         	                	while ( exit.hasNext() )
         	                	{
         	                		li.add( ( Triple )exit.next() );
         	                	}
         	                	for ( Iterator i = li.iterator(); i.hasNext(); )
         	                	{
         	                		ng.delete( ( Triple )i.next() );
         	                	}
         	                	// If the warrant's signature is ok, we want to test whether the graph
         	                	// digests of the graphs it asserts are ok. 
         	                	// We simply take the graphs and get their digests and compare the 
         	                	// string representations.
         	                	// After this, we then add to our verification graph the results of
         	                	// this process.
         	                    if ( SWPSignatureUtilities.validateSignature( ng, signatureMethod, signature.getLiteral().getLexicalForm(), certs ) )
         	                    {
         	                    	logger.info( "Warrant graph " + ng.getGraphName().toString() + " successfully verified." );
 									verificationGraph.add( new Triple( ng.getGraphName(), SWP_V.successful, Node.createLiteral( "true" ) ) );
 									
 									String asserteddigestQuery = "SELECT * WHERE (?graph swp:assertedBy <"+ng.getGraphName().toString()+"> . ?graph swp:digest ?digest . ?graph swp:digestMethod ?dmethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 						            Iterator ditr = TriQLQuery.exec( this, asserteddigestQuery );
 									String quoteddigestQuery = "SELECT * WHERE (?graph swp:quotedBy <"+ng.getGraphName().toString()+"> . ?graph swp:digest ?digest . ?graph swp:digestMethod ?dmethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 									Iterator qitr = TriQLQuery.exec( this, quoteddigestQuery );
 									if ( ditr.hasNext() )
 									{
 										while ( ditr.hasNext() )
 										{
 											Map dresult = ( Map ) ditr.next();
 											Node graph = ( Node ) dresult.get( QUERY_NODE_GRAPH );
 											Node digest = ( Node ) dresult.get( "digest" );
 					        	        	Node digestMethod = (Node) dresult.get( "dmethod" );
 										
 											String digest1 = SWPSignatureUtilities.calculateDigest( this.getGraph( graph ), digestMethod );
 											if ( digest1.equals( digest.getLiteral().getLexicalForm() ) )
 											{
 												verificationGraph.add( new Triple( graph, SWP_V.successful, Node.createLiteral( "true" ) ) );
 											}
 											else verificationGraph.add( new Triple( graph, SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
 										}
 									}
 									else if ( qitr.hasNext() )
 									{
 										while ( qitr.hasNext() )
 										{
 											Map dresult = ( Map ) qitr.next();
 											Node graph = ( Node ) dresult.get( QUERY_NODE_GRAPH );
 											Node digest = ( Node ) dresult.get( "digest" );
 					        	        	Node digestMethod = (Node) dresult.get( "dmethod" );
 										
 											String digest1 = SWPSignatureUtilities.calculateDigest( this.getGraph( graph ), digestMethod );
 											if ( digest1.equals( digest.getLiteral().getLexicalForm() ) )
 											{
 												verificationGraph.add( new Triple( graph, SWP_V.successful, Node.createLiteral( "true" ) ) );
 											}
 											else verificationGraph.add( new Triple( graph, SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
 										}
 									}
         	                    }
         	                    else
         	                    {
 									logger.error( "Warrant graph " + ng.getGraphName().toString() + " verification failure!" );
         	                    	verificationGraph.add( new Triple( ng.getGraphName(), SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
         	                    }
         	                    
         	                    for ( Iterator i = li.iterator(); i.hasNext(); )
         	                    {
         	                    	ng.add( ( Triple )i.next() );
         	                    }
         	                }
         	                catch ( SWPInvalidKeyException e ) 
         	                {
 								logger.error( e.getMessage() );
 								return false;
 							} 
         	                catch ( SWPSignatureException e ) 
         	                {
         	                	logger.error( e.getMessage() );
 								return false;
 							}  
         	                catch ( SWPNoSuchAlgorithmException e ) 
         	                {
         	                	logger.error( e.getMessage() );
 								return false;
 							} 
         	                catch ( SWPValidationException e ) 
         	                {
         	                	logger.error( e.getMessage() );
 								return false;
 							} 
 							catch ( SWPNoSuchDigestMethodException e ) 
 							{ 
 								logger.error( e.getMessage() );
 								return false;
 							}
         	            }
         	            else
 						{
 							logger.error( "Warrant graph " + ng.getGraphName().toString() + " verification failure!" );
 	                    	verificationGraph.add( new Triple( ng.getGraphName(), SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
 						}
 	                }
     			}	
 				else
 				{
 					logger.error( "Warrant graph " + ng.getGraphName().toString() + " verification failure!" );
                 	verificationGraph.add( new Triple( ng.getGraphName(), SWP_V.notSuccessful, Node.createLiteral( "true" ) ) );
 				}
     		}
     		else
     			continue;
     			
     	}		
 		return true;
     }
 
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
 		Node warrantGraphName = Node.createURI( "urn:uuid:" + uuidGen.nextUUID() );
 		SWPNamedGraph warrantGraph = new SWPNamedGraphImpl( warrantGraphName, new GraphMem() );
 		warrantGraph.add( new Triple( warrantGraphName, SWP.assertedBy, warrantGraphName ) );
         return warrantGraph;
     }
 
 }
 
 /*
  *  (c)   Copyright 2004, 2005 Chris Bizer (chris@bizer.de) & Rowland Watkins (rowland@grid.cx) 
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
