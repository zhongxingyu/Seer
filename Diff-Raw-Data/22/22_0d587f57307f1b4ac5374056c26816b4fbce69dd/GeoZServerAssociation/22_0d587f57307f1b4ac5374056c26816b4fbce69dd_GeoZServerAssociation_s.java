 //
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public License
 // as published by the Free Software Foundation; either version 2.1 of
 // the license, or (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite
 // 330, Boston, MA  02111-1307, USA.
 //
      
 package org.vfny.geoserver.zserver;
 
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Properties;
 import java.util.Enumeration;
 import java.net.Socket;
 import java.util.logging.Logger;
 import java.math.BigInteger;
 
 import com.k_int.z3950.util.*;
 import com.k_int.gen.AsnUseful.*;
 import com.k_int.gen.Z39_50_APDU_1995.*;
 import com.k_int.gen.NegotiationRecordDefinition_charSetandLanguageNegotiation_3.*; 
 // Used to represent arbritray query structures
 import com.k_int.util.RPNQueryRep.RootNode;
 // for OID Register
 import com.k_int.codec.util.*;
 import com.k_int.IR.*;
 import com.k_int.IR.Syntaxes.Conversion.*;
 
 // For XML components
 import  org.w3c.dom.Document;
 import  org.apache.xml.serialize.OutputFormat;
 import  org.apache.xml.serialize.Serializer;
 import  org.apache.xml.serialize.SerializerFactory;
 import  org.apache.xml.serialize.XMLSerializer;
 
 
 /**
  * GeoZServerAssociation backend Z3950 association.
  * Code based on ZServerAssociation, in jzkit source tree:
  * @author:     Ian Ibbotson (ibbo@k-int.com)
  * Company:     KI
  *
  * This class handles a connection, handling the init,
  * search and present requests.  
 * @author: Chris Holmes, TOPP
 * @version $VERSION$
  *
  * TODO: change this so it extends ZServerAssociation, instead
  * of rewriting things, as there are many methods used which
  * were not changed.  Also, unit tests, but you'd need a client
  * or else sample encoded z3950 requests.
  * <p>NOTE: As it stands right now, most of the code is
  * from ZServerAssociation, and there are several portions I
  * don't really understand.  I think the ZServerAssociation is
  * built to handle different implementations of Searchable, so 
  * that all one should do is code Searchable and Search task, and
  * leave ZServerAssociation the same.  But the association was
  * not working right, so I had to hack a few things together to 
  * get it to work.  It had a bunch of XSLT transformations, where
  * you would use an XML representation for jzkit to do things
  * internally, and then transform them on return.  But I don't
  * know xslt, and it didn't seem worth it, especially given the amount
  * of fields in the CSDGM (fgdc's metadata format).  Instead the
  * user is required to put store the different formats in the 
  * data folder, and those are just returned.  We also needed to
  * get properties from the server for information on where
  * the index and data folders are stored.  So yes, this class
  * is mostly me hacking stuff together, but I'll try to throw comments
  * into areas that I figured out, as many of the jzkit and z3950
  * ways of doing thing aren't immediately obvious.
  * <p> REVISIT: Would be nice to do the xslt transformations.  Ideal
  * would be a web based form which would generate the proper FGDC
  * metadata xml, which could then be transformed with xslt into
  * html, text or sgml.  This would be quite involved, but it is 
  * the next logical step for ease of use and integration.  
  */
 public class GeoZServerAssociation implements TargetAPDUListener
 {
 
     /** Standard logging instance for class */
     private static final Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.zserver");
     
     /** to generate search tasks. */
     private Searchable search_service = null;
     
     /** how this attaches to the socket. */
     private ZTargetEndpoint assoc = null;  
 
     /** The names of the searches run by this client.*/  
     private Hashtable active_searches = new Hashtable();
     /* Impl notes: Used
      * mostly by Present request, because z3950 is stateful,
      * so it needs to know what searches it has run, so 
      * it can return more specific records.*/
 
     /** Handles the translation of Object IDs into their names. */
         private OIDRegister reg = OIDRegister.getRegister();
     /*	Impl: z3950 specifies that most everything, such as attributes,
      * relations, return formats and more, are sent with an ID.  The
      * list of ids is at: http://www.loc.gov/z3950/agency/defns/oids.html.
      * jzkit uses this OIDRegister to do the translations.  The a2jruntime 
      * should hold all the information to propertly fill the OIDRegister
      * with appropriate information.
      */
 
 
     /**for notification messages when requests come in. */
     private GenericEventToTargetListenerAdapter event_adapter = null;
 
     /** the properties needed to do a search, such as where the 
      * datafolders live, as well as where the attribute mappings
      * and index live */
     private Properties serverProps;
 
     
 
     public GeoZServerAssociation(Socket s, Properties p) {
 	
 	//TODO: put this in nicer place...this is just a hack to get old html
 	//oid working...should do something like Isite where it translates
 	//old oids into the new ones.
 	OIDRegisterEntry html = 
 	    new OIDRegisterEntry("html", "{1,2,840,10003,5,1000,34,1}", 
 				 "record format", null);
 	reg.register_oid(html);
 	
 	//to generate search tasks.
 	this.search_service = (Searchable)new GeoSearchable();   
 	serverProps = p;
 	search_service.init(p);
 		
 	//set up the socket and enable listening.
 	assoc = new ZTargetEndpoint(s);
 	event_adapter = new GenericEventToTargetListenerAdapter(this);
 	assoc.getPDUAnnouncer().addObserver( event_adapter );
 	assoc.start();
   }
 
     /**
      * Handles the incoming Init Requset.
      * @param e the event that holds the pdu of the request.
      */
   public void incomingInitRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming initRequest....");
     
     /* Impl notes: So the way jzkit works is with this a2j method, 
      * asn to java.  It can run automatically which is nice, but
      * the objects it generates are a little different.  Basically the
      * asn for z3950 is a specification of the various requests, what
      * exactly they are.  It looks like this: 
      * SearchRequest ::= SEQUENCE{
      * referenceId          ReferenceId OPTIONAL,
      * smallSetUpperBound    [13]  IMPLICIT INTEGER,
      * replaceIndicator      [16]  IMPLICIT BOOLEAN,
      * resultSetName      [17]  IMPLICIT InternationalString,
      * etc...  The way jzkit deals with this is that a2j generates
      * a bunch of classes, so there is a searchRequest_type and
      * searchRequest_codec class.  The type is the useful one, where
      * one can actually work with it.  Most of it makes sense, but
      * one confusing part is the choice object.  Many of the classes
      * are derived from it, and basically they have two important
      * fields, which and o.  Which says which of the choices is stored
      * in the class, and o is the Object of that choice.  So to figure
      * out what's in a class, you need to first use which, and then
      * access the object stored based on the which value.  Yeah, it's
      * pretty obscure, and jzkit documentation is not that good.  But 
      * read through the code and it might make a little more sense.  I'll
      * try to point out a few examples. */
     InitializeRequest_type init_request = 
 	(InitializeRequest_type) (e.getPDU().o); 
     /* Like here, we have to cast the object held in the PDU to the
      * InitializeRequest_type class, so we can use it.*/
     
     for ( int i=0; i<Z3950Constants.z3950_option_names.length; i++ ) {
 	if ( init_request.options.isSet(i) )
 	    LOGGER.finer("Origin requested service: "
 			+Z3950Constants.z3950_option_names[i]);
     }
 
     // Did the origin request scan?
     if ( init_request.options.isSet(7) )
     {
 	//the geo profile does not require support of scan.
 	init_request.options.clearBit(7);
 	    LOGGER.finer("Origin requested scan, not supported by" + 
 			"this backend realisation.");
     }
 
     // If we are talking v2, userInformationField may contain 
     //a character Set Negotiation field, if v3, 
     //otherInfo may contain a charset/language negotiation feature.
     if ( init_request.userInformationField != null )
     {
       OIDRegisterEntry ent = reg.lookupByOID
 	  (init_request.userInformationField.direct_reference);
       if ( ent != null )
         LOGGER.finer("Init Request contains userInformationField oid="
 		     + ent.getName());
       else
 	  LOGGER.finer("Unkown external in userInformationField");
       // The OID for the external should be found 
       //in userInformationField.direct_reference
     }
 
     if ( init_request.otherInfo != null )
     {
       LOGGER.finer("Init Request contains otherInfo entries");
       for ( Enumeration other_info_enum = init_request.otherInfo.elements(); 
 	    other_info_enum.hasMoreElements(); )
       {
 	  LOGGER.finer("Processing otherInfo entry...");
 	  // Process the external at other_info_enum.nextElement();
 	  OtherInformationItem43_type oit = 
 	      (OtherInformationItem43_type)(other_info_enum.nextElement());
 
         LOGGER.finer("Processing OtherInformationItem43_type");
 
         switch ( oit.information.which )
         {
           case information_inline45_type.externallydefinedinfo_CID:
             EXTERNAL_type et = (EXTERNAL_type)(oit.information.o);
             if ( et.direct_reference != null )
             {
               OIDRegisterEntry ent = reg.lookupByOID(et.direct_reference);
               LOGGER.finer("External with direct reference, oid="
 			   +ent.getName());
 
               // Are we dealing with character set negotiation.
               if ( ent.getName().equals("z_charset_neg_3") )
                 handleNLSNegotiation((CharSetandLanguageNegotiation_type)
 				     (et.encoding.o));
             }
             break;
           default:
             LOGGER.finer("Currently unhandled OtherInformationType");
             break;
         }
       }
     }
 
     try
     {
       assoc.sendInitResponse(init_request.referenceId,
                              init_request.protocolVersion,
                              init_request.options,
                              init_request.preferredMessageSize.longValue(),
                              init_request.exceptionalRecordSize.longValue(),
                              true,
                              "174",
                             "TOPP geoserver zserver",
                             "$Id: GeoZServerAssociation.java,v 1.3 2003/01/16 23:32:31 cholmesny Exp $",
                              null,
                              null);
     }
     catch ( java.io.IOException ioe )
     {
       ioe.printStackTrace();
     }
   }
 
     /**
      * Handles the language and charset negotiation.
      * @param neg the negotiation type object.
      */
   private void handleNLSNegotiation(CharSetandLanguageNegotiation_type neg)
   {
     LOGGER.finer("Handle Character Set and Language Negotiation");
 
     if ( neg.which == CharSetandLanguageNegotiation_type.proposal_CID )
     {
       OriginProposal_type op = (OriginProposal_type)(neg.o);
 
       // Deal with any proposed character sets.
       if ( op.proposedCharSets != null )
       {
         for ( Enumeration prop_charsets = op.proposedCharSets.elements(); 
 	      prop_charsets.hasMoreElements();)
         {
           proposedCharSets_inline0_choice1_type c = 
 	      (proposedCharSets_inline0_choice1_type)
 	      (prop_charsets.nextElement());
           switch ( c.which )
           {
             case proposedCharSets_inline0_choice1_type.iso10646_CID:
               // The client proposes an iso 10646 id for a character set
               Iso10646_type iso_type = (Iso10646_type)(c.o);
               OIDRegisterEntry ent = reg.lookupByOID(iso_type.encodingLevel);
               LOGGER.finer("Client proposes iso10646 charset: "+ent.getName());
               break;
             default:
               LOGGER.warning("Unhandled character set encoding");
               break;
           }
         }
       }
     }
   }
 
 
 
     /**
      * Handles the incoming Search Requset.
      * @param e the event that holds the pdu of the request.
      */
   public void incomingSearchRequest(APDUEvent e)
   {
     LOGGER.finer("Processing incomingSearch_Request");
 
     SearchRequest_type search_request = (SearchRequest_type) (e.getPDU().o);
 
     // Create a search response
     PDU_type pdu = new PDU_type();
     pdu.which=PDU_type.searchresponse_CID;
     SearchResponse_type response = new SearchResponse_type();
     pdu.o = response;
 
     int ssub = search_request.smallSetUpperBound.intValue();
     int lslb = search_request.largeSetLowerBound.intValue();
     int mspn = search_request.mediumSetPresentNumber.intValue();
     
     LOGGER.finer("ssub = " + ssub + " lslb = " + lslb + " mspn = " + mspn);
 
     response.referenceId = search_request.referenceId; 
     
     // Assume failure unless something below sets to true
     response.searchStatus = Boolean.FALSE;
 
     RootNode rn = null;
 
     try
     {
       switch(search_request.query.which)
       {
         case Query_type.type_0_CID:
           LOGGER.finer("Processing Any Query");
           // Any
           break;
         case Query_type.type_1_CID:
         case Query_type.type_101_CID:
           // RPN query
           LOGGER.finer("Processing RPN Query");
           rn = com.k_int.z3950.util.RPN2Internal.zRPNStructure2RootNode
 	      ((RPNQuery_type)(search_request.query.o));
           break;
         case Query_type.type_2_CID:
         case Query_type.type_100_CID:
         case Query_type.type_102_CID:
           LOGGER.finer("Processing OctetString Query");
           // Octet String
           break;
       }
 
       if ( rn != null )
       {
         LOGGER.finer("Got root node");
 
         IRQuery q = new IRQuery();
         q.collections = search_request.databaseNames;
         q.query = new com.k_int.IR.QueryModels.RPNTree(rn);
 
         // Process the query
         LOGGER.finer("Create Search Task with query:" + q);
         SearchTask st = search_service.createTask
 	    (q, search_request.referenceId);
 	
 
         // Evaluate the query, waiting 10 seconds for the task to complete. 
 	//We really want to be
         // able to pass in a predicate to wait for here, 
 	//e.g. evaluate(10000, "NumHits > 100 OR Status=Complete");
         LOGGER.finer("Evaluate Search Task");
         try
         {
           st.evaluate(10000);
         }
         catch ( TimeoutExceededException tee )
         {
           LOGGER.finer("Timeout exceeded waiting for search to complete");
         }
 
         if ( search_request.resultSetName != null ) {
 	    LOGGER.finer("putting st with name " + 
 			 search_request.resultSetName);
 	    active_searches.put(search_request.resultSetName, st);
 	}
 	active_searches.put("Default", st);
 	
         // Result records processing
         int result_count = st.getTaskResultSet().getFragmentCount();
         LOGGER.finer("result count is "+result_count);
 
         // Number of hits
         response.resultCount = BigInteger.valueOf(result_count);
 
         // Figure out Search Status
         if ( ( st.getTaskStatusCode() != SearchTask.TASK_FAILURE ) )
         {
           response.searchStatus = Boolean.TRUE;
         }
         else
         {
           // Figure out Result Set Status
           switch ( st.getTaskStatusCode() )
           {
             case SearchTask.TASK_EXECUTING_ASYNC:
             case SearchTask.TASK_EXECUTING_SYNC:
               // resultSetStatus = 2 (interim) Partial results available, 
 		//not necessarily valid
               response.resultSetStatus = BigInteger.valueOf(2);
               break;
             case SearchTask.TASK_FAILURE:
               // resultSetStatus = 3 (none) No result set
               response.resultSetStatus = BigInteger.valueOf(3);
               break;
           }
         }
 
         LOGGER.finer("Is "+result_count+" <= "+lslb +" or <= "+ssub);
 
         if ( ( result_count <= lslb ) || ( result_count <= ssub ) )
         {
           LOGGER.finer("Yep");
 
           int start = 1;
           int count = ( result_count <= ssub ? result_count : mspn );
 
           if ( count > 0 )
 	      {
 		  LOGGER.finer("Asking for "+count+
 			       " response records from "+start);
 		  LOGGER.finer("Search request default = " 
 			 + search_request.preferredRecordSyntax);
 		  response.records = createRecordsFor
 		      (st, search_request.preferredRecordSyntax, start, 
 		       count, GeoProfile.BREIF_SET);
 		  
  
 		  if (response.records.which == 
 		      Records_type.responserecords_CID ){
 		      LOGGER.finer("We have records");
 		      
 		      // Looks like we managed to present some records OK..
 		      response.numberOfRecordsReturned = 
 			  BigInteger.valueOf(((Vector)
 					      (response.records.o)).size());
 		      response.nextResultSetPosition = 
 			  BigInteger.valueOf(count+1);
 		      response.presentStatus = BigInteger.valueOf(0);
 		  } else {  // Non surrogate diagnostics ( Single or Multiple )
 		      
 		      LOGGER.finer("Diagnostics");
 		      response.presentStatus = BigInteger.valueOf(5);
 		      response.numberOfRecordsReturned = 
 			  BigInteger.valueOf(0);
 		      response.nextResultSetPosition = 
 			  BigInteger.valueOf(1);
 		  }
 	      } else {
 		  LOGGER.finer("No need to piggyback records....");
 		  response.numberOfRecordsReturned = BigInteger.valueOf(0);
 		  response.nextResultSetPosition = BigInteger.valueOf(1);
 		  response.presentStatus = BigInteger.valueOf(0);
 	      }
         } else {
 	    response.presentStatus = BigInteger.valueOf(5);
 	    response.numberOfRecordsReturned = BigInteger.valueOf(0);
 	    response.nextResultSetPosition = BigInteger.valueOf(1);  	
 	}
       }
       else
 	  LOGGER.finer("Unable to process query into root node");
     }
     catch ( com.k_int.IR.InvalidQueryException iqe )
 	{
 	    // Need to send a diagnostic
 	    iqe.printStackTrace();
 	    response.resultCount = BigInteger.valueOf(0);
 	    response.presentStatus = BigInteger.valueOf(5);  // Failure
 	    response.numberOfRecordsReturned = BigInteger.valueOf(0);
 	    response.nextResultSetPosition = BigInteger.valueOf(0);
 	    response.resultSetStatus = BigInteger.valueOf(3);
 	    // No result set available
 	    response.records = createNSD(null, iqe.toString());
 	}
     catch ( SearchException se )
 	{
 	    // We need to populate a diagnostic here
 	    LOGGER.finer("Search returning diagnostic. Reason:"+se.toString());
 	    se.printStackTrace();
 	    response.resultCount = BigInteger.valueOf(0);
 	    response.presentStatus = BigInteger.valueOf(5); // Failure
 	    response.numberOfRecordsReturned = BigInteger.valueOf(0);
 	    response.nextResultSetPosition = BigInteger.valueOf(0);
 	    response.resultSetStatus = BigInteger.valueOf(3); 
 	    // No result set available
 	    response.records = createNSD((String)(se.additional), 
 					 se.toString());
 	}
     
     LOGGER.finer("Send search response : ");
     try
 	{
 	    assoc.encodeAndSend(pdu);
 	}
     catch ( java.io.IOException ioe )
 	{
 	    ioe.printStackTrace();
 	}  
   }
     
     public void incomingPresentRequest(APDUEvent e)
     {
 	LOGGER.finer("Incoming present_Request");
 	
 	PresentRequest_type present_request = 
 	    (PresentRequest_type) (e.getPDU().o);
 	
 	String requested_element_set = 
 	    (String)((ElementSetNames_type)
 		     present_request.recordComposition.o).o;
 	if (requested_element_set == null) {
 	    //default to full record.
 	    requested_element_set = GeoProfile.FULL_SET;
 	}
 	
 	// Create a present response
     PDU_type pdu = new PDU_type();
     pdu.which=PDU_type.presentresponse_CID;
     PresentResponse_type response = new PresentResponse_type();
     pdu.o = response;
     response.referenceId = present_request.referenceId;
     response.otherInfo = null;
 
     SearchTask st = (SearchTask)
 	(active_searches.get(present_request.resultSetId));
     if (st == null)  st = (SearchTask)(active_searches.get("Default"));
     //the last search done is always stored in the hashtable 
     //with both the resultSetName of the searchRequest and the value "Default"
     //So if the resultSetId can't be found the last search task is used.
     
     int start = present_request.resultSetStartPoint.intValue();
     int count = present_request.numberOfRecordsRequested.intValue();
     
     response.records = createRecordsFor
 	(st, present_request.preferredRecordSyntax, start, 
 	 count, requested_element_set);
     
     if ( response.records.which == Records_type.responserecords_CID )
 	{
 	    // Looks like we managed to present some records OK..
 	    response.numberOfRecordsReturned = 
 		BigInteger.valueOf(((Vector)(response.records.o)).size());
 	    if ( start+count >= st.getTaskResultSet().getFragmentCount() ){
 		response.nextResultSetPosition = BigInteger.valueOf(0);
 	    } else {
 		response.nextResultSetPosition = 
 		    BigInteger.valueOf(start+count);
 	    }
 	    response.presentStatus = BigInteger.valueOf(0);
 	} else { // Non surrogate diagnostics ( Single or Multiple )
 	    response.numberOfRecordsReturned = BigInteger.valueOf(0);
 	    response.nextResultSetPosition = 
 		present_request.resultSetStartPoint;
 	    
 	    response.presentStatus = BigInteger.valueOf(5);
 	}
     
     try
 	{
 	    assoc.encodeAndSend(pdu);
 	}
     catch ( java.io.IOException ioe )
 	{
 	    ioe.printStackTrace();
 	} 
     }
     
     /**
      * This will not be in final GeoZServerAssociation, just
      * use ZServerAssociation, when we are a child of it.*/
     public void incomingDeleteResultSetRequest(APDUEvent e)
     {
 	LOGGER.finer("Incoming deleteResultSetRequest");
 
 	DeleteResultSetRequest_type delete_request = 
 	    (DeleteResultSetRequest_type) (e.getPDU().o);
 	
 	// Create a DeleteResultSetResponse
 	PDU_type pdu = new PDU_type();
 	pdu.which=PDU_type.deleteresultsetresponse_CID;
 	DeleteResultSetResponse_type response = 
 	    new DeleteResultSetResponse_type();
 	pdu.o = response;
 	response.referenceId = delete_request.referenceId;
 	
 	
 	if ( delete_request.deleteFunction.intValue() == 0 )
 	    {
 		// Delete the result sets identified by 
 		//delete_request.resultSetList
 		for ( Enumeration task_list = 
 			  delete_request.resultSetList.elements(); 
 		      task_list.hasMoreElements(); ) {
 		    String next_rs = (String) task_list.nextElement();
 		    active_searches.remove(next_rs);
 		    // search_service.deleteTask(st.getTaskIdentifier());
 		}
 	    }
 	else
 	    {
 		// Function must be 1 : All sets in the association
 		
 		active_searches.clear();
 	    }
 	
 	response.deleteOperationStatus=BigInteger.valueOf(0); // 0 = Success, 
 	//1=resultSetDidNotExist, 2=previouslyDeletedByTarget, 
 	//3=systemProblemAtTarget
 	// 4 = accessNotAllowed, 5=resourceControlAtOrigin
 	try
 	    {
 		assoc.encodeAndSend(pdu);
     }
 	catch ( java.io.IOException ioe )
 	    {
 		ioe.printStackTrace();
 	    }
     }
 
 
     /**
      * closes the client's connection.
      */
   public void incomingClose(APDUEvent e)
   {
     LOGGER.finer("Close...");
     assoc.getPDUAnnouncer().deleteObserver(event_adapter);
     assoc.getPDUAnnouncer().deleteObservers();
     event_adapter = null;
     assoc.shutdown();
 
     try
     {
       assoc.join();
     }
     catch ( java.lang.InterruptedException ie )
     {
     }
     LOGGER.finer("Done joining with assoc thread");
 
     LOGGER.finer("Deleting tasks...");
     // Blow away any active search tasks. When these tasks finalize, 
     // they should make
     // sure they release any resources held by the creating Searchable object
     active_searches.clear();
     search_service.destroy();
     search_service = null;
 
 
     assoc = null;
   }
 
 
 	
     /**
      * Retrieves the record from the search task and encodes it
      * to send.
      * @param st The search task that holds the records.
      * @param preferredRecordSyntax the OID of the syntax.
      * @param start the number of the first record to return.
      * @param count the number of records to return.
      * @param recordFormatSetname the size of the set.
      */
   Records_type createRecordsFor(SearchTask st,
                                 int[] preferredRecordSyntax,
                                 int start,
                                 int count, String recordFormatSetname)
   {
     Records_type retval = new Records_type();
 
     LOGGER.finer("createRecordsFor(st, "+start+","+count+")");
     LOGGER.finer("pref rec syn = " + preferredRecordSyntax);
     LOGGER.finer("record setname = " + recordFormatSetname);
     LOGGER.finer("search task = " + st);
     
     // Try and do a normal present
     try
     {
 	if ( start < 1 ) 
 	    throw new PresentException("Start record must be > 0","13");
 	int numRecs = st.getTaskResultSet().getFragmentCount();
 	
 	LOGGER.finer("numresults = " + numRecs);
 	int requestedNum = start + count - 1;
 	if ( requestedNum >  numRecs ) {
 	    count = numRecs - (start - 1);
 	    if (start + count -1 > numRecs) {
 		LOGGER.finer(requestedNum + " < " + numRecs);
 		throw new PresentException
 		    ("Start+Count-1 (" +requestedNum + ") must be < the " + 
 		     " number of items in the result set: " +numRecs ,"13");
 	    }
 	}
 	
 	
 	if ( st == null )
 	    throw new PresentException("Unable to locate result set","30");
 	
 	Vector v =  new Vector();
 	retval.which = Records_type.responserecords_CID;
 	retval.o = v;
 	
 	OIDRegisterEntry requested_syntax = null;
 	String requested_syntax_name = null;
 	if ( preferredRecordSyntax != null ) {
 	    requested_syntax = reg.lookupByOID(preferredRecordSyntax);
 	    if(requested_syntax==null) { // unsupported record syntax
 		StringBuffer oid=new StringBuffer();
 		for(int i=0; i<preferredRecordSyntax.length; i++) {
 		    if(i!=0)
 			oid.append('.');
 		    oid.append(preferredRecordSyntax[i]);
 		}
 		LOGGER.warning("Unsupported preferredRecordSyntax="
 			       +oid.toString());
 		
 		// Need to set up diagnostic in here
 		retval.which = Records_type.nonsurrogatediagnostic_CID;
 		DefaultDiagFormat_type default_diag = 
 		    new DefaultDiagFormat_type();
 		retval.o = default_diag;
 		
 		default_diag.diagnosticSetId = reg.oidByName("diag-1");
 		default_diag.condition = BigInteger.valueOf(239);
 		default_diag.addinfo = new addinfo_inline14_type();
 		default_diag.addinfo.which = 
 		    addinfo_inline14_type.v2addinfo_CID;
 		default_diag.addinfo.o = (Object)(oid.toString());
 		return retval;
 	    }
 	    LOGGER.finer("requested_syntax="+requested_syntax);
 	    requested_syntax_name = requested_syntax.getName();
 	    LOGGER.finer("requested_syntax_name="+requested_syntax_name);
 	}
 	else
       {
	  requested_syntax_name = "xml"; //REVISIT: should this be
 	  //default?  We're sure to have it...
 	  requested_syntax = reg.lookupByName(requested_syntax_name);
       }
 	
 	st.setRequestedSyntax(requested_syntax);
 	st.setRequestedSyntaxName(requested_syntax_name);
 		
 	InformationFragment[] raw_records;
 	RecordFormatSpecification rfSpec = 
 	    new RecordFormatSpecification
 		(requested_syntax_name, null, recordFormatSetname);
 	LOGGER.finer("calling getFragment("+(start)+","+count+")");
 	raw_records = st.getTaskResultSet().getFragment(start, count, rfSpec);
 	
 	if ( raw_records == null )
 	    throw new PresentException("Error retrieving records","30");
 
 	for ( int i=0; i<raw_records.length; i++ ) {
 		LOGGER.finer("Adding record "+i+" to result");
 		
 		NamePlusRecord_type npr = new NamePlusRecord_type();
 		npr.name = raw_records[i].getSourceCollectionName();
 		npr.record = new record_inline13_type();
 		npr.record.which = record_inline13_type.retrievalrecord_CID;
 		EXTERNAL_type rec = new EXTERNAL_type();
 		npr.record.o = rec;
 		
 		if ( requested_syntax_name.equals(Z3950Constants.RECSYN_HTML) 
 		     || requested_syntax_name.equals("sgml")) {
 		    LOGGER.finer("Returning OctetAligned record for "
 				 +requested_syntax_name);
                     rec.direct_reference = 
 			reg.oidByName(requested_syntax_name);
 		    rec.encoding = new encoding_inline0_type();
 		    rec.encoding.which = 
 			encoding_inline0_type.octet_aligned_CID;
 		    String raw_string = (String)raw_records[i].getOriginalObject();
 		    rec.encoding.o = raw_string.getBytes(); 
 		    if(raw_string.length()==0) {
 			
 			// can't make a html record
 			retval.which = Records_type.nonsurrogatediagnostic_CID;
 			DefaultDiagFormat_type default_diag =
 			    new DefaultDiagFormat_type();
 			retval.o = default_diag;
 			
 			default_diag.diagnosticSetId = reg.oidByName("diag-1");
 			default_diag.condition = BigInteger.valueOf(227);
 			default_diag.addinfo = new addinfo_inline14_type();
 			default_diag.addinfo.which = 
 			    addinfo_inline14_type.v2addinfo_CID;
 			default_diag.addinfo.o = 
 			    (Object)"1.2.840.10003.5.109.3";
 			return retval;
 			
 		    }
 		}
 		else if ( requested_syntax_name.equals
 			  (Z3950Constants.RECSYN_XML) ) {
 	
 		    // Since XML is our canonical internal schema, 
 		    //all realisations of InformationFragment
 		    // are capable of providing an XML representation of 
 		    //themselves, so just use the
 		    // Fragments getDocument method.
 		    LOGGER.finer("Returning OctetAligned XML");
 		    java.io.StringWriter sw = new java.io.StringWriter();
 		    
 		    try
 			{
 			    OutputFormat format = 
 				new OutputFormat
 				    (raw_records[i].getDocument() );
 			    XMLSerializer serial = 
 				new XMLSerializer( sw, format );
 			    serial.asDOMSerializer();
 			    serial.serialize( raw_records[i].getDocument().
 					      getDocumentElement() );
 			}
 		    catch ( Exception e )
 			{
 			    LOGGER.severe("Problem serializing dom tree to" + 
 					  " result record" + e.getMessage());
 			}
 		    
 		    rec.direct_reference = 
 			reg.oidByName(requested_syntax_name);
 		    rec.encoding = new encoding_inline0_type();
 		    rec.encoding.which = 
 			encoding_inline0_type.octet_aligned_CID;
 		    rec.encoding.o = sw.toString().getBytes();  	  
		} else if  ( requested_syntax_name.equals
			     (Z3950Constants.RECSYN_SUTRS)){
 		    rec.direct_reference = 
 			reg.oidByName(requested_syntax_name);
 		    rec.encoding = new encoding_inline0_type();
 		    rec.encoding.which = 
 			encoding_inline0_type.single_asn1_type_CID;
 		    rec.encoding.o = 
 			((String)(raw_records[i].getOriginalObject()));
 		}
 		v.add(npr);
 		
 	}
     }
     catch ( PresentException pe ) {
 	LOGGER.warning("Processing present exception: "+pe.toString());
 	
       // Need to set up diagnostic in here
 	retval.which = Records_type.nonsurrogatediagnostic_CID;
       DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
       retval.o = default_diag;
 
       default_diag.diagnosticSetId = reg.oidByName("diag-1");
       
       if ( pe.additional != null )
 	  default_diag.condition = 
 	      BigInteger.valueOf( Long.parseLong(pe.additional.toString()) );
       else
         default_diag.condition = BigInteger.valueOf(0);
 
       default_diag.addinfo = new addinfo_inline14_type();
       default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
       default_diag.addinfo.o = (Object)(pe.toString());
     } 
     
     LOGGER.finer("retval = " + retval);
     return retval;
   }
 
     //TODO: It'd be nice to have better diagnostics, use explicit diagnostic
     //(use DiagFormat_type from jzkit) and specify exactly what the problem is
   private Records_type createNSD(String diag_code, String additional)
   {
     Records_type retval = new Records_type();
     retval.which = Records_type.nonsurrogatediagnostic_CID;
     DefaultDiagFormat_type default_diag = new DefaultDiagFormat_type();
     retval.o = default_diag;
  
     default_diag.diagnosticSetId = reg.oidByName("diag-1");
  
     if ( diag_code != null )
       default_diag.condition = BigInteger.valueOf( Long.parseLong(diag_code) );
     else
       default_diag.condition = BigInteger.valueOf(0);
  
     default_diag.addinfo = new addinfo_inline14_type();
     default_diag.addinfo.which = addinfo_inline14_type.v2addinfo_CID;
     default_diag.addinfo.o = (Object)(additional);
 
     return retval;
   }
 
         /** not supported, needed for TargetADPU interface*/
   public void incomingAccessControlRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming accessControlRequest");
   }
     /** not supported, needed for TargetADPU interface*/
   public void incomingAccessControlResponse(APDUEvent e)
   {
     LOGGER.finer("Incoming AccessControlResponse");
   }
 
     /** not supported, needed for TargetADPU interface*/
   public void incomingResourceControlRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming resourceControlRequest");
   }
     
     /** not supported, needed for TargetADPU interface*/
   public void incomingTriggerResourceControlRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming triggetResourceControlRequest");
   }
 
  /** not supported, needed for TargetADPU interface*/
   public void incomingResourceReportRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming resourceReportRequest");
   }
 
 
 
 
   public void incomingScanRequest(APDUEvent e)
   {
     ScanRequest_type scan_request = (ScanRequest_type) (e.getPDU().o);
 
     int step_size = 0;
     int scan_status = 0;
     int number_of_entries_returned = 0;
     int position_of_term = 0;
 
     try
     {
       if ( this.search_service instanceof Scanable )
       {
         Scanable s = (Scanable)this.search_service;
         if ( s.isScanSupported() )
         {
           String name = null;
           OIDRegisterEntry ent = reg.lookupByOID(scan_request.attributeSet);
           if ( ent != null )
             name=ent.getName();
 
           RootNode rn = new RootNode();
 
           int i1 = ( scan_request.stepSize == null ? 0 : scan_request.stepSize.intValue() );
           int i2 = ( scan_request.numberOfTermsRequested == null ? 0 : 
                             scan_request.numberOfTermsRequested.intValue() );
           int i3 = ( scan_request.preferredPositionInResponse == null ? 0 : 
                             scan_request.preferredPositionInResponse.intValue() );
 
           ScanRequestInfo sri = new ScanRequestInfo();
           sri.collections = scan_request.databaseNames;
           sri.attribute_set = name;
           sri.term_list_and_start_point = com.k_int.z3950.util.RPN2Internal.convertAPT(scan_request.termListAndStartPoint,rn);
           sri.step_size = i1;
           sri.number_of_terms_requested = i2;
           sri.position_in_response = i3;
 
           ScanInformation scan_result = s.doScan(sri);
 
           assoc.sendScanResponse(scan_request.referenceId,
                                  BigInteger.valueOf(i1),
                                  BigInteger.valueOf(scan_status),
                                  BigInteger.valueOf(scan_result.position),
                                  scan_result.results,
                                  scan_request.attributeSet,
                                  null);
         }
       }
     }
     catch ( java.io.IOException ioe )
     {
       ioe.printStackTrace();
     }
   }
 
      /** not supported, needed for TargetADPU interface*/
   public void incomingSortRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming sortRequest");
   }
 
     /** not supported, needed for TargetADPU interface*/
   public void incomingSegmentRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming segmentRequest");
   }
 
     /** not supported, needed for TargetADPU interface*/
   public void incomingExtendedServicesRequest(APDUEvent e)
   {
     LOGGER.finer("Incoming extendedServicesRequest");
   }
 
 
 }
 
