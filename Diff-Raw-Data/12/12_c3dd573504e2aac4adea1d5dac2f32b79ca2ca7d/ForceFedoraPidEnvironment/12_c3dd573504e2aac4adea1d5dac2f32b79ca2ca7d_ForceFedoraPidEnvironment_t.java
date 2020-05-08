 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * \file
  * \brief
  */
 
 
 package dk.dbc.opensearch.plugins;
 
 import dk.dbc.opensearch.common.fedora.PID;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.helpers.OpensearchNamespaceContext;
 import dk.dbc.opensearch.common.pluginframework.IPluginEnvironment;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 
 import java.io.ByteArrayInputStream;
 import java.util.Map;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.InputSource;
 
 public class ForceFedoraPidEnvironment implements IPluginEnvironment
 {
     
     private static Logger log = Logger.getLogger( ForceFedoraPidEnvironment.class );
 
     private IObjectRepository repository;
 
    // Create lock-object to guarantee sequential access to XPathFactory.
    // It is static to ensure that all instances of ForceFedoraPidEnvironment can access it.
    // It is private to ensure only we - inside this class can access it - thereby noone else locks on it and slows us down.
    private static final Object XPathFactoryLock = new Object();

     ForceFedoraPidEnvironment( IObjectRepository repository, Map< String, String > args ) throws PluginException
     {
 	this.repository = repository;
     }
 
 
 
 
     private String getDCIdentifierFromOriginal( CargoContainer cargo, String xpathStr ) throws PluginException
     {
         CargoObject co = cargo.getCargoObject( DataStreamType.OriginalData );
 
         NamespaceContext nsc = new OpensearchNamespaceContext();
 
         if ( co == null )
         {
             String error = "ForceFedoraPid-plugin Could not retrieve CargoObject with original data from CargoContainer";
             log.error( error );
             throw new PluginException( String.format( error ) );
         }
 
         byte[] b = co.getBytes();
 
 	// BUG 11838:
 	log.trace( String.format( "BUG#11838: Retrieved the following data from the CargoContainer ( size: %s ):\n%s", b.length, new String( b ) ) );
 
 	// BUG 11838:
 	// This is very ugly and probably not safe!
 	// The main problem here is, that XPathFactory is not thread-safe, and must not be invoked
 	// from more than one thread at once - throughout the application. It is the programmers responsibility to ensure this, which in my opinion
 	// is pretty much impossible. Assume You use some third party software which uses XPathFactory in another thread and 
 	// you at the same time invoke XPathFactory from your thread - then we have a race-condition - one you have no way to avoid what so ever :(
 	// 
 	// I have moved the XPathFactory invocation into a synchronized-block in order to prevent a race-condition in this class.
	// I have created an object XPathFactoryLock which is private to all instanceses of ForceFedoraPidEnvironment,
	// in order to have a safe object to synchronize on. For more information see bug 11838.
	//
 	// I have checked the Datadock-code, and found another invocation of XPathFactory in FoxmlDocument.java.
 	// There is therefore an off chance that these two invocations happen simultaniously.
         XPath xpath = null;
	synchronized( this.XPathFactoryLock )
 	{
 	    xpath = XPathFactory.newInstance().newXPath();
 	}
         xpath.setNamespaceContext( nsc );
         XPathExpression xPathExpression;
 
         // String xpathStr = "/ting:container/dkabm:record/ac:identifier";
         try
         {
             xPathExpression = xpath.compile( xpathStr );
         }
         catch( XPathExpressionException e )
         {
             throw new PluginException( String.format( "Could not compile xpath expression '%s'", xpathStr ), e );
         }
 
         InputSource docbookSource = new InputSource( new ByteArrayInputStream( b ) );
 
         // Find id of the docbook document
         String regNum;
         try
         {
             regNum = xPathExpression.evaluate( docbookSource ).replaceAll( "\\s", "+" );
         }
         catch( XPathExpressionException xpe )
         {
             throw new PluginException( "Could not evaluate xpath expression to find registration number", xpe );
         }
 
         log.trace( String.format( "registration number found [%s]", regNum ) );
 
         if ( regNum == null || regNum.equals( "" ) )
         {
             return null;
         }
 
         String newID = null;
         try
         {
             newID = co.getSubmitter() + ":" + regNum.substring( 0, regNum.indexOf( '|' ) );
             if ( newID.length() > 64 )
             {
                 log.warn( String.format( "Constructed ID %s to long shortning to %s", newID, newID.substring( 0, 64 ) ) );
                 newID = newID.substring( 0, 64 );
             }
         }
         catch ( StringIndexOutOfBoundsException sioobe )
         {
             log.debug( String.format( "Wrapping index out of bounds exceptio and throwing plugin exception", sioobe.getMessage() ) );
             throw new PluginException( "Could not get new id for cargo", sioobe );
         }
 
         //We assume that co.submitter is at least 1 in length if set, so newID must be
         //at least 3 in length to be valid (1 for submitter 1 for ":" and 1 for regNum)
         //So anything shorter than 3 is invalid and we return null to tell we cant make
         //a valid indentifier
         if( newID.length() < 3 )
         {
             return null;
         }
 
         return newID;
     }
 
 
     public CargoContainer run( CargoContainer cargo ) throws PluginException
     {
 	
 	String s = getDCIdentifierFromOriginal( cargo, "/ting:container/oso:object/oso:identifier" );
 	if ( s == null )
 	{
 	    // We did not find anything in the above xpath.
 	    // Lets try this one:
 	    // This is a PG WebService ThemaObject search: (see bug#10085)
 	    s = getDCIdentifierFromOriginal( cargo, "/ting:container/dkabm:record/ac:identifier" );
 	    if( s == null )
 	    {
 		throw new PluginException( "could not create valid identifier from data" );
 	    }
 	}
 
         log.info( String.format( "Forcing Store ID to %s", s ) );
         cargo.setIdentifier( new PID( s ));
         
         return cargo;
     }
 
 
 }
