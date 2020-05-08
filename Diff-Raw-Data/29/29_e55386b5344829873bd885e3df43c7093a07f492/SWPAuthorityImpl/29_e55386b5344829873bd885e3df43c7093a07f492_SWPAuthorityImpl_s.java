 
 package de.fuberlin.wiwiss.ng4j.swp.impl;
 
 import java.util.ArrayList;
 
 import com.hp.hpl.jena.datatypes.DatatypeFormatException;
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.shared.AddDeniedException;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import java.security.PublicKey;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.X509Certificate;
 
 import org.apache.commons.codec.binary.Base64;
 
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.swp.SWPAuthority;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPMissingAuthorityPropertyException;
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.SWP;
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.FOAF;
 
 /**
  * 
  * An SWPAuthority represents information about an authority
  * like id, label, eMail, keys and certificates 
  * 
  * @author chris bizer
  *
  */
 public class SWPAuthorityImpl implements SWPAuthority
 {
 
     private Node id;
     private String label;
     private String email = null;
     private PublicKey publickey;
 	private X509Certificate certificate;
 	private NamedGraph graph = null;
 
 	public SWPAuthorityImpl(){}
 	
 	/**
 	 * 
 	 * @param id
 	 */
     public SWPAuthorityImpl( Node id ) 
     {
     	setID( id );
 	}
 
     /**
      * 
      * Sets the ID of the authority.
      * Authorities can by identified using a URIref or a bNode
      * 
      * @param id
      * 
      */
 	public void setID( Node id ) 
 	{
         this.id = id ;
     }
 	
 	/**
 	 * 
 	 * @return id
 	 */
 	public Node getID() 
 	{
 		return this.id;
     }
 	
 	/**
      * 
      * Sets the Label / Name of the authority.
      * Will be serialized using rdfs:label
      * 
      * @param label
      * 
      */
 	public void setLabel( String label ) 
 	{
         this.label = label ;
     }
 	
 	/**
 	 * 
 	 * @return label
 	 */
 	public String getLabel()  
 	{
 		return this.label;
     }
 
     /**
      * 
      * Sets the eMail address of the authority.
      * Will be serialized using foaf:mbox
      * 
      * @param email
      * 
      */
 	public void setEmail( String email )
 	{
         this.email = email ;
     }
 	
 	/**
 	 * 
 	 * @return email
 	 */
 	public String getEmail() 
 	{
 		return this.email;
     }
 	
     /**
      * 
      * Sets the public key of the authority.
      * Will be serialized using swp:hasKey
      * 
      * @param key
      * 
      */
 	public void setPublicKey( PublicKey key ) 
 	{
         this.publickey = key ;
     }
 	
 	/**
 	 * 
 	 * @return publickey
 	 */
 	public PublicKey getPublicKey()
 	{
 		return this.publickey;
     }
 
     /**
      * 
      * Sets the certificate of the authority.
      * 
      * @param certificate
      * 
      */
 	public void setCertificate( X509Certificate certificate )
 	{
         this.certificate = certificate ;
     }
 	
 	/**
 	 * 
 	 * @return certificate
 	 * 
 	 */
 	public X509Certificate getCertificate() 
 	{
 		return this.certificate;
     }
 	
     /**
      * 
      * Adds an additional property of the authority.
      * 
      * @param predicate
      * @param object
      * 
      */
 	public void addProperty( Node predicate, Node object ) 
 	{
 		if ( this.graph != null )
 		{
 			graph.add( new Triple( this.getID(), predicate, object ) );
 		}
 		else
 		{
 			//graph not ready!
 		}
     }
 
 	/**
      * 
      * Returns an iterator over all property values (nodes) for a given property.
      * 
      * @return predicate
      * 
      */
 	public ExtendedIterator getProperty( Node predicate )
 	{
 		return null;
     }
 
 	/**
      * 
      * Adds a description of the authority to a graph.
      * 
      * The listOfAuthorityProperties determines which information
      * about the authority is added.
      * 
     * @param graph
      * @param listOfAuthorityProperties
      * @throws SWPMissingAuthorityPropertyException
      * 
      * @return boolean
 	 *  
      * 
      */
	public boolean addDescriptionToGraph( NamedGraph graph, ArrayList<Node> listOfAuthorityProperties ) 
 	{
 		// Add swp:authority
		graph.add( new Triple( graph.getGraphName(), SWP.authority, this.getID() ) );
 		
 		
         // Check if the eMail address has to be added.
 		if ( this.getID().isBlank() && this.getEmail() != null ) 
 		{
             graph.add( new Triple(this.getID(), FOAF.mbox.asNode(), Node.createURI( "mailto:" + this.getEmail() ) ) );
 		}
 		
 		
 		if ( listOfAuthorityProperties != null)
 		{
 			// Add authority description
 			if ( listOfAuthorityProperties.contains( FOAF.mbox.asNode() ) ) 
 			{
				graph.add(new Triple( this.getID(), FOAF.mbox.asNode(), Node.createURI( "mailto:" + this.getEmail() ) ) );
 			}
 
 			//Node rdfsLabel = Node.createURI("http://www.w3.org/2000/01/rdf-schema#label");
 			//Not fatal, so won't throw exception if missing
 			if ( listOfAuthorityProperties.contains( RDFS.label.asNode() ) ) 
 			{
				graph.add( new Triple( this.getID(), RDFS.label.asNode(), Node.createLiteral( this.getLabel(), null, null ) ) );
 			}
 
         
 			if ( listOfAuthorityProperties.contains( SWP.RSAKey ) ) 
         	{
 				// We need code for publishing information about a RSA key here, using the SWP-2 and the XML-Sig vocabulary
				graph.add( new Triple( this.getID(), 
         								SWP.RSAKey, 
         								Node.createLiteral( new String( Base64.encodeBase64Chunked( this.getPublicKey().getEncoded() ) ), 
         													null, 
         													XSDDatatype.XSDbase64Binary) ) );
         	}
 
 			if ( listOfAuthorityProperties.contains( SWP.X509Certificate ) ) 
 			{
 				// We need code for publishing information about a X509 certificate here, using the SWP-2 and the XML-Sig vocabulary
         		try {
        			graph.add( new Triple( this.getID(), 
 											SWP.X509Certificate, 
 											Node.createLiteral( new String( Base64.encodeBase64Chunked( this.getCertificate().getEncoded() ) ), 
 																null, 
 																XSDDatatype.XSDbase64Binary ) ) );
         		} 
         		catch ( AddDeniedException e ) 
         		{
         			return false;
         		} 
         		catch ( DatatypeFormatException e ) 
         		{
         			return false;
         		} 
         		catch ( CertificateEncodingException e ) 
         		{
         			return false;
         		}
 			}
 		}
 		else if ( this.getCertificate() != null)
 		{
 			try {
				graph.add( new Triple( this.getID(), 
 						SWP.X509Certificate, 
 						Node.createLiteral( new String( Base64.encodeBase64Chunked( this.getCertificate().getEncoded() ) ), 
 											null, 
 											XSDDatatype.XSDbase64Binary ) ) );
 			} catch (AddDeniedException e) {
 				throw new RuntimeException(e);
 			} catch (DatatypeFormatException e) {
 				throw new RuntimeException(e);
 			} catch (CertificateEncodingException e) {
 				throw new RuntimeException(e);
 			}
 		}
        this.graph = graph;
 
         return true;
 	}
 	
 
 	/**
      * 
      * Returns a graph containing all information about the authority.
      * 
      * @return graph
      *
      */
 	public Graph getGraph() 
 	{
 		return this.graph;
     }
 
 }
 
 /*
  *  (c)   Copyright 2004 - 2009 Chris Bizer (chris@bizer.de)
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
