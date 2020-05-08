//$Header: /cvsroot/ng4j/ng4j/src/de/fuberlin/wiwiss/ng4j/swp/impl/SWPWarrantImpl.java,v 1.7 2009/01/21 01:32:57 jenpc Exp $
 package de.fuberlin.wiwiss.ng4j.swp.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.security.Signature;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 
 import sun.misc.BASE64Decoder;
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFactory;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.NiceIterator;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.ng4j.sparql.NamedGraphDataset;
 import de.fuberlin.wiwiss.ng4j.swp.SWPAuthority;
 import de.fuberlin.wiwiss.ng4j.swp.SWPWarrant;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPCertificateException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPNoSuchAlgorithmException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPSignatureException;
 import de.fuberlin.wiwiss.ng4j.swp.util.SWPSignatureUtilities;
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.SWP;
 
 public class SWPWarrantImpl implements SWPWarrant
 {
 
 	public static final String NL = System.getProperty("line.separator") ;
 	
 	protected NamedGraph warrant;
 	protected NamedGraphSet local = new NamedGraphSetImpl();
 	protected NamedGraphDataset localDS;
 	
 	public SWPWarrantImpl( NamedGraph graph )
 	{
 		warrant = graph;
 		local.addGraph( warrant );
 		localDS = new NamedGraphDataset( local );
 	}
 	
 	public class NiceGraphIterator extends NiceIterator {
 
 		final ResultSet results;
 
 		public NiceGraphIterator(ResultSet results) {
 			super();
 			this.results = results;
 		}
 
 		public boolean hasNext() 
 		{
 			return results.hasNext();
 		}
 
 		public Object next() 
 		{
 			QuerySolution solution = results.nextSolution();
 			Resource graphURI = solution.getResource( "?graph" );
 			return graphURI.getURI();
 		}
 	}
 	
 	public ExtendedIterator getGraphs() 
 	{
 		//String warrantQuery = "SELECT * WHERE ?warrant (?graph ?p ?warrant) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		String warrantQuery = "SELECT ?graph" + NL
			+ "WHERE { GRAPH ?warrant {" + NL
			+ "?graph ?p ?warrant" + NL
			+ " } }";
 		QueryExecution qe = QueryExecutionFactory.create( warrantQuery, localDS );
 		ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
         return new NiceGraphIterator(results);
 	}
 
 	public ExtendedIterator getAssertedGraphs() 
 	{
 		//String warrantQuery = "SELECT * WHERE ?warrant (?graph swp:assertedBy ?warrant) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         String warrantQuery = "SELECT ?graph" + NL
 			+ "WHERE { GRAPH ?warrant {" + NL
			+ "?graph <" + SWP.assertedBy + "> ?warrant" + NL
 			+ " } }";
 		QueryExecution qe = QueryExecutionFactory.create( warrantQuery, localDS );
 		ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
         return new NiceGraphIterator(results);
 	}
 
 	public ExtendedIterator getQuotedGraphs() 
 	{
 		//String warrantQuery = "SELECT * WHERE ?warrant (?graph swp:quotedBy ?warrant) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		String warrantQuery = "SELECT ?graph" + NL
 			+ "WHERE { GRAPH ?warrant {" + NL
			+ "?graph <" + SWP.quotedBy + "> ?warrant" + NL
 			+ " } }";
 		QueryExecution qe = QueryExecutionFactory.create( warrantQuery, localDS );
 		ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
         return new NiceGraphIterator(results);
 	}
 
 	public SWPAuthority getAuthority() throws SWPCertificateException 
 	{
 		SWPAuthority authority = new SWPAuthorityImpl();
 		String warrantUriString = warrant.getGraphName().getURI();
 //		String query = "SELECT * WHERE <"+warrant.getGraphName().getURI()+"> (<"+warrant.getGraphName().getURI()+"> swp:authority ?authority . ?authority swp:X509Certificate ?certificate) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		String query = "SELECT ?authority ?certificate" + NL
         	+ "WHERE { GRAPH <" + warrantUriString + "> { " + NL
         	+ "<" + warrantUriString + "> <" + SWP.authority + "> ?authority ." + NL
         	+ "?authority <" + SWP.X509Certificate + "> ?certificate" + NL
         	+ " } }";
 		QueryExecution qe = QueryExecutionFactory.create( query, localDS );
 		ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
 		if ( results.hasNext() )
 		{
 			X509Certificate certificate = null;
 			QuerySolution solution = results.nextSolution();
 			RDFNode auth = solution.get( "?authority" );
 			Literal cert = solution.getLiteral( "?certificate" );
 			String certs = "-----BEGIN CERTIFICATE-----\n" +
 							cert.getLexicalForm() + 
 							"\n-----END CERTIFICATE-----";
 			authority.setID( auth.asNode() );
 				
 			try 
 			{
 				CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
 				certificate = ( X509Certificate ) cf.generateCertificate( new ByteArrayInputStream( certs.getBytes() ) );
 			} 
 			catch ( CertificateException e ) 
 			{
 				throw new SWPCertificateException( "Error reading X509 Certificate PEM from Warrant graph." );
 			}
 	    		
 			authority.setCertificate( certificate );
 			
 		}
 		//else throw new SWPAuthorityNotFoundException();
 		
 		return authority;
 	}
 
 	public Signature getSignature() throws SWPSignatureException 
 	{
 		Signature sig = null;
 		byte[] signature = null;
 		String warrantUriString = warrant.getGraphName().getURI();
 		//String query = "SELECT * WHERE (<"+warrant.getGraphName().getURI()+"> swp:signature ?signature) (<"+warrant.getGraphName().getURI()+"> swp:signatureMethod ?smethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		String query = "SELECT ?signature ?smethod" + NL
         	+ "WHERE { GRAPH <" + warrantUriString + "> { " + NL
         	+ "<" + warrantUriString + "> <" + SWP.signature + "> ?signature ." + NL
         	+ "<" + warrantUriString + "> <" + SWP.signatureMethod + "> ?smethod" + NL
         	+ " } }";
         QueryExecution qe = QueryExecutionFactory.create( query, localDS );
 		ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
 		if ( results.hasNext() )
 		{
 			QuerySolution solution = results.nextSolution();
 			Literal sigValue = solution.getLiteral( "?signature" );
 			RDFNode sigMethod = solution.get( "?smethod" );
 			
         	try 
 			{
 				BASE64Decoder decoder = new BASE64Decoder();
 				signature = decoder.decodeBuffer( sigValue.getLexicalForm() );
 				sig = SWPSignatureUtilities.getSignatureAlgorithm( sigMethod.asNode() );
 			} 
 			catch ( IOException e ) 
 			{
 				throw new SWPSignatureException( "Error occured decoding signature value from Warrant graph." );
 			} 
 			catch ( SWPNoSuchAlgorithmException e ) 
 			{
 				
 				throw new SWPSignatureException( e.getMessage() );
 			}
 		}
 		else return null;
 		
 		return sig;
 	}
 
 	public boolean isSigned() 
 	{
 		boolean result = false;
 		String warrantUriString = warrant.getGraphName().getURI();
 		//String query = "SELECT * WHERE (<"+warrant.getGraphName().getURI()+"> swp:signature ?signature) (<"+warrant.getGraphName().getURI()+"> swp:authority ?authority) (<"+warrant.getGraphName().getURI()+"> swp:signatureMethod ?smethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		String query = "SELECT ?signature ?smethod ?authority" + NL
         	+ "WHERE { GRAPH <" + warrantUriString + "> { " + NL
         	+ "<" + warrantUriString + "> <" + SWP.signature + "> ?signature ." + NL
         	+ "<" + warrantUriString + "> <" + SWP.signatureMethod + "> ?smethod ." + NL
         	+ "<" + warrantUriString + "> <" + SWP.authority + "> ?authority" + NL
         	+ " } }";
         QueryExecution qe = QueryExecutionFactory.create( query, localDS );
 		ResultSet results = ResultSetFactory.copyResults( qe.execSelect() );
 		if ( results.hasNext() )
 		{
 			QuerySolution s = results.nextSolution();
 			boolean hasSigValue = s.contains( "?signature" );
 			boolean hasSigMethod = s.contains( "?smethod" );
 			boolean hasAuthority = s.contains( "?authority" );
 			if ( hasSigValue & hasSigMethod & hasAuthority )
 				result = true;
 		}
 		return result;
 	}
 }
 
 /*
  *  (c)   Copyright 2004, 2005, 2006, 2007, 2008 Rowland Watkins (rowland@grid.cx) & Chris Bizer (chris@bizer.de)
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
