 /*
  *
  *This file is part of opensearch.
  *Copyright © 2009, Dansk Bibliotekscenter a/s,
  *Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
  *
  *opensearch is free software: you can redistribute it and/or modify
  *it under the terms of the GNU General Public License as published by
  *the Free Software Foundation, either version 3 of the License, or
  *(at your option) any later version.
  *
  *opensearch is distributed in the hope that it will be useful,
  *but WITHOUT ANY WARRANTY; without even the implied warranty of
  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *GNU General Public License for more details.
  *
  *You should have received a copy of the GNU General Public License
  *along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * \file ESHarvest.java
  * \brief
  */
 
 package dk.dbc.opensearch.components.harvest ;
 
 import dk.dbc.opensearch.common.db.IDBConnection;
 import dk.dbc.opensearch.common.db.OracleDBConnection;
 import dk.dbc.opensearch.common.db.OracleDBPooledConnection;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.util.logging.Level;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.apache.log4j.Logger;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 // \todo: Remove HarvesterInvalidStatusChangeException. It is unneccesary after the new setStatusXXX
 
 /**
  * The ES-base implementation of the Harvester-backend. The ESHarvester delivers jobs 
  * to a frontend, i.e. the DataDock, delivers data through {@link getData} and maintains the state of 
  * the jobs in the ES-base through {@link setStatusSuccess}, {@link setStatusFailure} and {@link setStatusRetry}.
  */
 public class ESHarvest implements IHarvest
 {
     private OracleDBPooledConnection connectionPool = null; // The connectionPool, given through the constuctor
     private String        databasename;   // The ES-base databasename - given through the constructor
 
     Logger log = Logger.getLogger( ESHarvest.class );
 
     /**
      *   Creates a new ES-Harvester.
      *   The Harvester 
      */
     public ESHarvest( OracleDBPooledConnection connectionPool , String databasename ) throws HarvesterIOException
     {
 	this.connectionPool = connectionPool;
 	this.databasename = databasename;
     }
 
 
 
     /**
      *  Starts the ES-Harvester. When the ES-harvester is started, it will look into the ES-base
      *  and find jobs currently in progress {@link cleanupESBase} and set them to queued.
      *  Please notice, that as a consequence of the above, only one ES-Harvester is allowed to
      *  run on an ES-base.
      */
     public void start( ) throws HarvesterIOException
     {
 	// \todo: Why do we want to call start on a Harvester? Shouldn't it not just start when it is created? (i.e. the constructor is called)
 
 	// \Note: Currently this function is "empty". At some point it should be removed completely.
 	log.info( "Starting the ES-Harvester" );
         log.debug( "ESHarvest started" );
 
     }
 
 
     /**
      *  Shuts down the ES-base connection.
      */
     public void shutdown() throws HarvesterIOException
     {
         /**
            \Note: It could be discussed wheter the cleanup-method mentioned in the
            * the constructor also should be called from here.
            * The argument for doing this, is that in case the DataDock gracefully crashes,
            * then the ES-base could be updated, and if data-deliverers looks at the ES-base
            * then they will see their records as (rightfully) queued and not (wrongfully)
            * inProgress. Of course if the DataDock does not die gracefully, then the
            * shutdown-method may not be run.
            */
 
 	log.info( "ESHarvest shutdown" );
 
 	try 
 	{
 	    connectionPool.shutdown();
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String(  "Error when closing the Oracle pooled connection" );
 	    log.fatal( errorMsg , sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
     }
 
 
     /**
      *  Retrieve a list of jobs from the ESHarvester.
      */
     public ArrayList<IJob> getJobs( int maxAmount ) throws HarvesterIOException
     {
 	log.info( String.format( "The ES-Harvester was requested for %s jobs", maxAmount ) );
 
 	// get a connection from the connectionpool:
 	Connection conn;
 
 	try
 	{
 	    conn = connectionPool.getConnection();
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String("Could not get a db-connection from the connection pool");
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
         ArrayList<IJob> theJobList = new ArrayList<IJob>();
 	try 
 	{
 	    Statement stmt = conn.createStatement();
 	    stmt.setMaxRows( maxAmount );
 	    //	    List<Integer> takenList = new ArrayList<Integer>();
 		
 	    // \todo: Single query to retrieve all available queued packages _and_
 	    //        their supplementalId3 - must be veriefied
 	    // get queued targetreference, lbnr and referencedata (supplementalId3):
 	    // \todo: SELECT FOR UPDATE i stedet for SELECT?
 	    String queryStr = new String( "SELECT suppliedrecords.targetreference, suppliedrecords.lbnr, suppliedrecords.supplementalId3 " + 
 					  "FROM taskpackagerecordstructure, suppliedrecords " + 
 					  "WHERE suppliedrecords.targetreference " + 
 					  "IN (SELECT targetreference FROM updatepackages  WHERE databasename = '" +
 					  databasename + "') " +
 					  "AND taskpackagerecordstructure.recordstatus = 2 " + 
 					  "AND taskpackagerecordstructure.targetreference = suppliedrecords.targetreference " + 
 					  "AND taskpackagerecordstructure.lbnr = suppliedrecords.lbnr " + 
 					  "ORDER BY suppliedrecords.targetreference, suppliedrecords.lbnr" );
 	    log.debug( queryStr );
 	    ResultSet rs = stmt.executeQuery( queryStr );
 		
 	    while( rs.next() )
             {
 		int targetRef        = rs.getInt( 1 );    // suppliedrecords.targetreference
 		int lbnr             = rs.getInt( 2 );    // suppliedrecords.lbnr
 		String referenceData = rs.getString( 3 ); // suppliedrecords.supplementalId3
 			
 		ESIdentifier id = new ESIdentifier( targetRef, lbnr );
 
 		// Update Recordstatus
 		setRecordStatusToInProgress( id, conn );
 
 		Document doc = null;
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			
 		boolean DocOK = true; // The Doc structure has no problems
 		try
 		{
 		    DocumentBuilder builder = factory.newDocumentBuilder();
 		    doc = builder.parse( new InputSource( new ByteArrayInputStream( referenceData.getBytes() ) ) );
 		}
 		catch( ParserConfigurationException pce )
 		{
 		    log.error( String.format( "Caught error while trying to instantiate documentbuilder '%s'", pce ) );
 		    DocOK = false;
 		}
 		catch( SAXException se )
 		{
 		    log.error( String.format( "Could not parse data: '%s'", se ) );
 		    DocOK = false;
 		}
 		catch( IOException ioe )
 		{
 		    log.error( String.format( "Could not cast the bytearrayinputstream to a inputsource: '%s'", ioe ) );
 		    DocOK = false;
 		}
 
 		if ( DocOK ) 
 		{
 		    Job theJob = new Job( id, doc );
 		    theJobList.add( theJob );
 		} 
 		else 
 		{
 		    try
 		    {
 			setStatusFailure( id, "The referencedata contains malformed XML" );
 		    } 
 		    catch ( HarvesterUnknownIdentifierException huie )
 		    {
 			log.error( String.format( "Error when changing JobStatus (unknown identifier) ID: %s Msg: %s", id, huie.getMessage() ), huie );
 		    }
 		    catch ( HarvesterInvalidStatusChangeException hisce )
 		    {
 			log.error( String.format( "Error when changing JobStatus (invalid status) ID: %s Msg: %s ", id, hisce.getMessage() ), hisce );
 		    }
 		}
 	    }
 	}
         catch( SQLException sqle )
 	{
 	    String errorMsg = new String( "A Database Error occured" );
 	    log.fatal( errorMsg );
 	    throw new HarvesterIOException( errorMsg , sqle );
 	}
 
 	log.info( String.format( "Found %s available Jobs", theJobList.size() ) );
 
 	releaseConnection( conn );
 
         return theJobList;
     }
 
 
 
     /**
      *  Retrieves data from the ES-base associated with the jobId.
      */
     public byte[] getData( IIdentifier jobId ) throws HarvesterUnknownIdentifierException, HarvesterIOException
     {
         log.info( String.format( "ESHarvest.getData( identifier %s ) ", jobId ) );
 
 	// get a connection from the connectionpool:
 	Connection conn;
 
 	try
 	{
 	    conn = connectionPool.getConnection();
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String("Could not get a db-connection from the connection pool");
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
         //get the data associated with the identifier from the record field
         byte[] returnData = null;
         ESIdentifier ESJobId = (ESIdentifier)jobId;
 
         try
 	{
 	    Statement stmt = conn.createStatement();
 	    String queryString = String.format( "SELECT record " + 
 						"FROM suppliedrecords " + 
 						"WHERE targetreference = %s " + 
 						"AND lbnr = %s" ,
 						ESJobId.getTargetRef() , ESJobId.getLbNr() );
 	    log.debug( queryString );
 	    ResultSet rs = stmt.executeQuery( queryString );
 	    if( ! rs.next() )
 	    {
 		// The ID does not exist
 		String errorMsg = String.format( "the Identifier %s is unknown in the base", ESJobId );
 		log.error( errorMsg );
 		throw new HarvesterUnknownIdentifierException( errorMsg );
 	    }
 	    else
 	    {
 		// The ID exist
 		Blob data = rs.getBlob( 1 );
 		long blobLength = data.length();
 		if( blobLength > 0 )
 		{
 		    // Return data
 		    returnData = data.getBytes( 1l, (int)blobLength );
 		}
 		else
 		{
 		    // For some unknown reason, there is no data associated with the ID.
 		    log.error( String.format( "No data associated with id %s", ESJobId ) );
 		}
 	    }
 	}
         catch( SQLException sqle )
 	{
 	    String errorMsg = new String( "A database error occured " );
 	    log.fatal(  errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
 	releaseConnection( conn );
 
         return returnData;
     }
 
 
     public void setStatusFailure( IIdentifier Id, String failureDiagnostic ) 
 	throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
     {
         log.info( String.format( "ESHarvest.setStatusFailure( identifier %s ) ", Id ) );
 
 	Connection conn;
 
 	try
 	{
 	    conn = connectionPool.getConnection();
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String("Could not get a db-connection from the connection pool");
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
 
 	ESIdentifier EsId = (ESIdentifier)Id;
 	setStatus( EsId, JobStatus.FAILURE, conn );
 	try 
 	{
 	    setFailureDiagnostic( EsId, failureDiagnostic, conn );
 	}
 	catch( SQLException sqle ) 
 	{
 	    String errorMsg = String.format( "Could not set failureDiagnostic on Id: %s", EsId );
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
 	releaseConnection( conn );
     }
 
     public void setStatusSuccess( IIdentifier Id, String PID )
 	throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
     {
         log.info( String.format( "ESHarvest.setStatusSuccess( identifier %s ) ", Id ) );
 
 	Connection conn;
 
 	try
 	{
 	    conn = connectionPool.getConnection();
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String("Could not get a db-connection from the connection pool");
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
 	ESIdentifier EsId = (ESIdentifier)Id;
 	setStatus( EsId, JobStatus.SUCCESS, conn );
 	setPIDInTaskpackageRecordStructure( EsId, PID, conn );
 
 	releaseConnection( conn );
 
     }
 
     public void setStatusRetry( IIdentifier Id )
 	throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
     {
         log.info( String.format( "ESHarvest.setStatusRetry( identifier %s ) ", Id ) );
 
 	Connection conn;
 
 	try
 	{
 	    conn = connectionPool.getConnection();
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String("Could not get a db-connection from the connection pool");
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
 	setStatus( (ESIdentifier)Id, JobStatus.RETRY, conn );
 
 	releaseConnection( conn );
     }
 
 
 
     /**
      *  Changes the status of a record in the ES-base. 
      *  If the status is {@link JobStatus.RETRY}, then the status of the record will be changed from inProgress to queued.
      *  If the status is allready set to either Success or Failure, then an exception is thrown, since it
      *  is not allowed to change status on an allready finished record.
      */
     private void setStatus( ESIdentifier jobId, JobStatus status, Connection conn ) 
 	throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
     {
         log.info( String.format( "ESHarvester was requested to set status %s on data identified by the identifier %s", status, jobId ) );
 
         // check if the status associated with the identifier has previously been set to success, failure 
 	// or is queued (i.e. the job is not in progress).
         // If not, set it to what the parameter says.
 	// Otherwise it is an error - you cannot update a job which is not in progress.
 
         ESIdentifier ESJobId = (ESIdentifier)jobId;
         try
 	{
 	    // Lock row for update
 	    Statement stmt = conn.createStatement();
 	    String fetchStatusString = String.format( "SELECT recordstatus " + 
 						      "FROM taskpackagerecordstructure " + 
 						      "WHERE targetreference = %s " + 
 						      "AND lbnr = %s " + 
 						      "FOR UPDATE OF recordstatus", 
 						      ESJobId.getTargetRef() , ESJobId.getLbNr() );
 	    ResultSet rs = stmt.executeQuery( fetchStatusString );
 	    int counter = 0;
 	    if ( !rs.next() ) 
 	    {
 		// No more rows. If this is the first time rs.next() is called, then no rows where found
 		// with the above statement, and we should throw an exception.
 		if (counter == 0) 
 		    {
 			conn.rollback();
 			stmt.close();
 			String errorMsg = String.format( "recordstatus requested for unknown identifier: %s ", ESJobId );
 			log.error( errorMsg );
 			throw new HarvesterUnknownIdentifierException( errorMsg );
 		    } 
 	    }
 	    else
 	    {
 		++counter;
 			
 		//check if the status is set already, i.e. the record is _not_ inProgress:
 		int recordStatus = rs.getInt( 1 );
 		if( recordStatus != 3 )
 		{
 		    String errorMsg = String.format( "the status is already set to %s for identifier: %s", recordStatus, ESJobId );
 		    log.error( errorMsg );
 		    conn.rollback();
 		    stmt.close();
 		    throw new HarvesterInvalidStatusChangeException( errorMsg );
 		}
 			
 		// Set the status:
 		int new_recordStatus = 0;
 		switch( status )
 		{
 		case SUCCESS:
 		    new_recordStatus = 1;
 		    break;
 		case RETRY:
 		    new_recordStatus = 2;
 		    break;
 		case FAILURE:
 		    new_recordStatus = 4;
 		    break;
 		default:
 		    // I suspect that this case cannot happen!
 		    conn.rollback();
 		    stmt.close();
 		    throw new HarvesterInvalidStatusChangeException( "Unknown status" );
 		}
 		String updateString = String.format( "UPDATE taskpackagerecordstructure " + 
 						     "SET recordstatus = %s " + 
 						     "WHERE targetreference = %s " + 
 						     "AND lbnr = %s ", new_recordStatus, 
 						     ESJobId.getTargetRef(), ESJobId.getLbNr()  );
 			
 		log.debug( String.format( "Updating with: %s", updateString ) );
 		int updateResult = stmt.executeUpdate( updateString );
 
 		if( updateResult != 1 )
 		{
 		    log.warn( String.format( "unknown status update attempt on identifier: %s - updateResult=%s", ESJobId, updateResult ) );
 		}
 
 		// Check the taskpackage for update of TP-status:			
 		setTaskPackageStatus( ESJobId.getTargetRef(), conn );
 	    }
 	} 
 	catch( SQLException sqle ) 
 	{
 	    log.fatal( "A database error occured", sqle );
 	    throw new HarvesterIOException( "A database error occured", sqle );
 	}
     }
 
 
     /**
      *
      * Finds all records in ES-base with recordstatus inProgress (3), and changes them to recordstatus queued (2).
      *
      */
     public void changeRecordstatusFromInProgressToQueued( ) throws HarvesterIOException
     {
         log.info( "Cleaning up ES-base" );
 
 	Connection conn;
 
 	try
 	{
 	    conn = connectionPool.getConnection();
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String("Could not get a db-connection from the connection pool");
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 	
         try
 	{
 	    Statement stmt = conn.createStatement();
 	    // Locking the rows:
 	    int res1 = stmt.executeUpdate( "SELECT recordstatus " + 
 					   "FROM taskpackagerecordstructure " + 
 					   "WHERE recordstatus = 3 " +
 					   "AND databasename = '" + databasename + "' " +
 					   "FOR UPDATE OF recordstatus" );
 	    // Updating the rows:
 	    log.debug("Select for update: " + res1);
 	    if (res1 == 0) 
 	    {
 		// no rows for update - just close down the statement:
 		conn.rollback();
 		stmt.close();
 	    } 
 	    else 
 	    {
 		int res2 = stmt.executeUpdate( "UPDATE taskpackagerecordstructure " + 
 					       "SET recordstatus = 2 " + 
 					       "WHERE recordstatus = 3 " +
 					       "AND databasename = '" + databasename + "'");
 		log.debug("Update: " + res2);
 		stmt.close();
 		conn.commit();
 	    }
 	}
         catch( SQLException sqle )
 	{
 	    String errorMsg = new String( "An SQL error occured while cleaning up the ES-base" );
 	    System.out.println( errorMsg );
 	    log.fatal( errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
 	releaseConnection( conn );
 
         log.debug( "Done cleaning up ES-base" );
     }
 
 
     /**
      * Updates the field taskpackagerecordstructure.recordstatus in ES to be 
      * inProgress (value: 3) for the taskpackagerecordstructure with targetRef and lbnr.
      */
    private void setRecordStatusToInProgress( ESIdentifier ESJobId, Connection conn ) throws HarvesterIOException, SQLException
     {
 
 	log.debug( String.format( "Updating recordstatus for ID: %s", ESJobId ) );
 
 	// Locking the row for update:
 	Statement stmt = conn.createStatement();
 	int res1 = stmt.executeUpdate("SELECT recordstatus " + 
 				      "FROM taskpackagerecordstructure " + 
 				      "WHERE targetreference = " + ESJobId.getTargetRef() + 
 				      " AND lbnr = " + ESJobId.getLbNr() + 
 				      " AND recordstatus = 2 " + 
 				      "FOR UPDATE OF recordstatus");
 	// Testing all went well:
 	if (res1 != 1) 
 	{
 	    // Something went wrong - we did not lock a single row for update
	    log.fatal( "Error: Result from select for update was " + res1 + ". Not 1." );
	    String errorMsg = String.format( "RecordStatus was allready set in ES-base for %s", ESJobId );
	    log.fatal( errorMsg );
 	    conn.rollback();
	    throw new HarvesterIOException( errorMsg );
 	}
 
 	// Updating recordstatus in row:
 	int res2 = stmt.executeUpdate("UPDATE taskpackagerecordstructure " + 
 				      "SET recordstatus = 3 " + 
 				      "WHERE targetreference = " + ESJobId.getTargetRef() + 
 				      " AND lbnr = " + ESJobId.getLbNr() + 
 				      " AND recordstatus = 2");
 	if (res2 != 1) 
 	{
 	    // Something went wrong - we did not update a single row
 	    log.error( "Error: Result from update was " + res2 + ". Not 1" );
 	    conn.rollback();
 	    return;
 	}
 
 	// Committing the update:
 	// \todo: Is this inefficient?
 	stmt.close();
 	conn.commit();
     }
 
 
     /**
      *  Changes the status on a taskpackage if all assoicated records are finished.
      */
     private void setTaskPackageStatus( int targetref, Connection conn ) throws HarvesterInvalidStatusChangeException, SQLException
     {
 
 	log.debug( String.format( "setTaskPackageStatus with targetRef %s", targetref ) );
 
 	Statement stmt = conn.createStatement();
 
 	// Check if status on TP needs to be updated.
 	// This happens if the record was the last in the TP to get a status of Success or Failure.
 	String noofrecsQuery = String.format( "SELECT noofrecs, noofrecs_treated " + 
 					      "FROM taskspecificupdate " + 
 					      "WHERE targetreference = %s", 
 					      targetref );
 
 	log.debug( noofrecsQuery );
 	ResultSet rs1 = stmt.executeQuery( noofrecsQuery );
 	while ( rs1.next() ) 
 	{
 	    int noofrecs = rs1.getInt( 1 );
 	    int noofrecs_treated = rs1.getInt( 2 );
 	    log.debug( String.format( "NoOfRecords: %s   NoOfRecordsTreated: %s",
 				      noofrecs, noofrecs_treated ) );
 	    if ( noofrecs < noofrecs_treated ) 
 	    {
 
 		// This is an error. There were more treated records than actual records. 
 		// This _must_ never happen.
 		throw new HarvesterInvalidStatusChangeException( String.format( "Error: There were more treated records than actual records in taskpackage %s. This should never ever happen.", targetref ) );
 	    } 
 	    else if ( noofrecs == noofrecs_treated ) 
 	    {
 
 		// find the number of success and failures on the taskpackage:
 		String failure_success_query = String.format( "SELECT scount, fcount  " + 
 							      "FROM " +
 							      "(SELECT count( recordstatus ) scount " + 
 							      "FROM taskpackagerecordstructure " + 
 							      "WHERE targetreference = %s " + 
 							      "AND recordstatus = 1) a , " + 
 							      "(SELECT count( recordstatus ) fcount " +
 							      "FROM taskpackagerecordstructure " + 
 							      "WHERE targetreference = %s " + 
 							      "AND recordstatus = 4) b",
 							      targetref,
 							      targetref );
 		log.debug( failure_success_query );
 		ResultSet failure_success_rs = stmt.executeQuery( failure_success_query );
 		int counter2 = 0;
 		int success_count = 0;
 		int failure_count = 0;
 		while ( failure_success_rs.next() ) 
 		{
 		    success_count = failure_success_rs.getInt( 1 );
 		    failure_count = failure_success_rs.getInt( 2 );
 			    
 		    ++counter2;
 		}
 		if (counter2 != 1 ) 
 		{
 		    // \todo: 
 		    // either zero or more than one row retrieved - this should not happen.
 		    // Throw an exception?
 		}
 
 		// \todo: Should we test for to many updates?
 		int update_status = 0;
 		// update the TaskSpecificUpdate:
 		if ( success_count == noofrecs ) 
 		{
 		    // All was posts was succesfully handled:
 		    update_status = 1;
 		} 
 		else if ( failure_count == noofrecs ) 
 		{
 		    // All was posts was handled with failure:
 		    update_status = 3;
 		} 
 		else 
 		{
 		    // Posts were mixed with both success and failure:
 		    update_status = 2;
 		}
 			
 		String update_taskpackage_status_query = String.format( "SELECT updatestatus " + 
 									"FROM taskspecificupdate " + 
 									"WHERE targetreference = %s " + 
 									"FOR UPDATE OF updatestatus", 
 									targetref );
 		log.debug( update_taskpackage_status_query );
 		ResultSet update_taskpackage_status_rs = stmt.executeQuery( update_taskpackage_status_query );
 		if ( ! update_taskpackage_status_rs.next() ) 
 		{
 		    String errorMsg = String.format( "The updatestatus for the taskpackage could not be updated. TaskSpecificUpdate with targetref %s was not found in base", targetref );
 		    log.error( errorMsg );
 		    conn.rollback();
 		    stmt.close();
 		    throw new HarvesterInvalidStatusChangeException( errorMsg );
 		}
 		else
 		{
 		    int current_update_status = update_taskpackage_status_rs.getInt( 1 );
 		    if ( current_update_status != 0 ) 
 		    {
 			// This should never happen.
 		        String errorMsg = String.format( "The status for the taskpackage with targetRef %s was allready set to %s", targetref, current_update_status );
 			log.error( errorMsg );
 			conn.rollback();
 			stmt.close();
 			throw new HarvesterInvalidStatusChangeException( errorMsg );
 		    } 
 
 		    String update_taskpackage_status = String.format( "UPDATE taskspecificupdate " + 
 								      "SET updatestatus = %s " + 
 								      "WHERE targetreference = %s",
 								      update_status,
 								      targetref );
 		    log.debug( update_taskpackage_status );
 		    try
 		    {
 			int res = stmt.executeUpdate( update_taskpackage_status );
 			log.debug( String.format( "%s rows updated" , res ) );
 			conn.commit();
 		    } 
 		    catch( SQLException sqle )
 		    {
 			String errorMsg = String.format( "An SQL error occured when trying to update updatestatus in TaskSpecificUpdate with targetref %s" , targetref);
 			log.error( errorMsg, sqle );
 			conn.rollback();
 			stmt.close();
 			throw new HarvesterInvalidStatusChangeException( errorMsg, sqle );
 		    }
 		}
 	    }
 	}
 	stmt.close();
 
     }
 
 
     private void setFailureDiagnostic( ESIdentifier Id, String failureDiagnostic, Connection conn ) throws HarvesterIOException, SQLException
     {
 	// \Note: It is only possible to add one diagnostic to a record, since you can only make one
 	//        call to either setSuccess or setFailure.
 
 	// It is not possible to use ' in a failure diagnostic - we therefore rip them from the diagnostic here:
 	log.info( String.format( "OLD: setStatusFailure with failure diagnostic: [%s]", failureDiagnostic ) );
 	failureDiagnostic = failureDiagnostic.replace('\'', ' ');
 	log.info( String.format( "NEW: setStatusFailure with failure diagnostic: [%s]", failureDiagnostic ) );
 
 	int diagnosticId = 0;
 	Statement stmt = null;
 	try
 	{
 	    stmt = conn.createStatement();
 
 	    // Get unique diagnostics.number:
 	    ResultSet rs = stmt.executeQuery( "select diagIdSeq.nextval from dual" );
 	    if ( !rs.next() ) 
 	    {
 		String errorMsg = String.format( "Could not create a unique identifier for diagIdSeq in the ES base for ESId: %s.", Id );
 		log.fatal( errorMsg );
 		conn.rollback();
 		stmt.close();
 		throw new HarvesterIOException( errorMsg );
 	    }
 	    diagnosticId = rs.getInt( 1 );
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = String.format( "A database error occured when trying to retrive unique id from diagIdSeq in ES base for ESId: %s.", Id );
 	    log.fatal( errorMsg, sqle );
 	    stmt.close();
 	    conn.rollback();
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
 	// Create new row in diagnostics table:
 	try 
 	{
 	    int lbnr = 1;
 	    String diagSetId = new String("'10.100.1.1'");
 	    int condition = 100;
 	    String insert_stmt = String.format( "INSERT INTO " +
 						"diagnostics (id, lbnr, diagnosticSetId, condition, addInfo) " +
 						"VALUES ( " + 
 						diagnosticId + ", " +
 						lbnr + ", " +
 						diagSetId + ", " +
 						condition + ", '" +
 						failureDiagnostic + "' )" );
 	    log.debug( "Inserting into diagnostics using: [" + insert_stmt + "]" );
 	    stmt.executeQuery( insert_stmt );
 	}
 	catch( SQLException sqle )
 	{
 	    String errorMsg = new String( "Could not insert diagnostic with id: " + diagnosticId );
 	    log.fatal( errorMsg, sqle );
 	    conn.rollback();
 	    stmt.close();
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 	
 	// Attach diagnostic to failed record:
 	try
 	{
 	    String update_query = String.format( "SELECT recordOrSurDiag2 " + 
 						 "FROM taskpackagerecordstructure " +
 						 "WHERE targetreference = %s " +
 						 "AND lbnr = %s " +
 						 "FOR UPDATE OF recordOrSurDiag2",
 						 Id.getTargetRef(), Id.getLbNr() );
 	    int res = stmt.executeUpdate( update_query );
 
 	    // Testing all went well:
 	    if (res != 1) 
 	    {
 		// Something went wrong - we did not lock a single row for update
 		log.error( "Error: Result from select for update was " + res + ". Not 1." );
 		conn.rollback();
 		return;
 	    }
 	    
 	    // Updating recordOrSurDiag2 in row:
 
 	    String update_query2 = String.format( "UPDATE taskpackagerecordstructure " + 
 						  "SET recordOrSurDiag2 = %s " + 
 						  "WHERE targetreference = %s " +
 						  "AND lbnr = %s ",
 						  diagnosticId, 
 						  Id.getTargetRef(), Id.getLbNr() );
 	    int res2 = stmt.executeUpdate( update_query2 ); 
 
 	    if (res2 != 1) 
 	    {
 		// Something went wrong - we did not update a single row
 		log.error( "Error: Result from update was " + res2 + ". Not 1" );
 		conn.rollback();
 		return;
 	    }
 
 	    stmt.close();
 	    conn.commit();
 	    
 	}
 	catch ( SQLException sqle )
 	{
 	    String errorMsg = String.format( "Could not attach diagnostic (Id: %s) to taskpackagerecordstructure (Id: %s) with text: [%s]", diagnosticId, Id, failureDiagnostic );
 	    log.fatal( errorMsg, sqle );
 	    stmt.close();
 	    conn.rollback();
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
     }
 
 
     private void setPIDInTaskpackageRecordStructure( ESIdentifier Id, String PID, Connection conn ) 
     {
 	// When data is successfully stored in Fedora, the PID for the data must be stored
 	// in the ES-base on the original record.
 	// The field in the database is: taskpackagerecordstructure.record_id,
 	// which is a varchar(25). This sets a restriction on the size of the PID.
 
 	try
 	{
 	    Statement stmt = conn.createStatement();
 	    
 	    String select_query = String.format( "SELECT record_id " + 
 						 "FROM taskpackagerecordstructure " + 
 						 "WHERE targetreference = %s " +
 						 "AND lbnr = %s " +
 						 "FOR UPDATE OF record_id",
 						 Id.getTargetRef(), Id.getLbNr());
 	    int res1 = stmt.executeUpdate( select_query );
 
 	    // Testing all went well:
 	    if (res1 != 1) 
 	    {
 		// Something went wrong - we did not lock a single row for update
 		log.error( "Error: Result from select for update was " + res1 + ". Not 1." );
 		conn.rollback();
 		return;
 	    }
 	    
 	    String update_query = String.format( "UPDATE taskpackagerecordstructure " + 
 						 "SET record_id = '%s' " + 
 						 "WHERE targetreference = %s " +
 						 "AND lbnr = %s ",
 						 PID, Id.getTargetRef(), Id.getLbNr() );
 	    int res2 = stmt.executeUpdate( update_query ); 
 	    
 	    // Testing all went well:
 	    if (res2 != 1) 
 	    {
 		// Something went wrong - we did not lock a single row for update
 		log.error( "Error: Result from select for update was " + res2 + ". Not 1." );
 		conn.rollback();
 		return;
 	    }
 
 	    stmt.close();
 	    conn.commit();
 	    
 	}
 	catch( SQLException sqle )
 	{
 	    // If, for some reason, the PID can not be set in the ES-base we must not
 	    // break down. Just set a warning.
 	    // Note: When the record_id is set to >= 64 in the ES-base, this method 
 	    // _should_ not set a warning.
 	    String errorMsg = String.format( "Could not set the PID: [%s] for the identifier: %s.", PID, Id);
 	    log.warn( errorMsg );
 	}
 	
     }
 
 
     private void releaseConnection( Connection conn ) throws HarvesterIOException
     {
 	/*
 	if ( ods == null )
 	{
 	    throw new SQLException("Could not release connection. The OracleDataSource is null (unintialized?)");
 	}
 	*/
 
 	// Close the connection:
 	try
 	{
 	    if ( !conn.isClosed() )
 	    {
 		conn.close();
 	    }
 	}
         catch( SQLException sqle )
 	{
 	    String errorMsg = new String( "Could not close the database connection" );
 	    log.fatal(  errorMsg, sqle );
 	    throw new HarvesterIOException( errorMsg, sqle );
 	}
 
     }
 
 }
