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
 
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IIdentifier;
 import dk.dbc.opensearch.common.types.TaskInfo;
 import dk.dbc.opensearch.common.db.OracleDBPooledConnection;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
// \todo: Remove HarvesterInvalidStatusChangeException. It is unneccesary after the new setStatusXXX

 /**
  * The ES-base implementation of the Harvester-backend. The ESHarvester delivers jobs 
  * to a frontend, i.e. the DataDock, delivers data through {@link #getCargoContainer} and maintains the state of 
  * the jobs in the ES-base through {@link #setStatusSuccess}, {@link #setStatusFailure}.
  */
 public final class ESHarvest implements IHarvest
 {
     Logger log = Logger.getLogger( ESHarvest.class );
 
 
     private OracleDBPooledConnection connectionPool = null; // The connectionPool, given through the constuctor
     private String databasename; // The ES-base databasename - given through the constructor
 
     
     private LinkedList< ESIdentifier > jobCandidatesQueue = new LinkedList< ESIdentifier >();
     
 
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
      * This method is not really used.
      */
     public void start() throws HarvesterIOException
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
 
     
     private int retrieveCandidatesToQueue( Connection conn ) throws HarvesterIOException
     {
         int retrievedAmount = 0;
 
         try
         {
             int targetRef = -1;
 
             //
             // Find the next targetreference:
             //
             PreparedStatement pstmt = conn.prepareStatement( "SELECT targetreference " +
                                      "FROM updatepackages " +
                                      "WHERE taskstatus = ? " +
                                      "AND databasename = ? " +
                                      "ORDER BY update_priority , creationdate , targetreference" );
             pstmt.setInt( 1, 0 ); // taskstatus
             pstmt.setString( 2, databasename );
 
             ResultSet rs1 = pstmt.executeQuery ( );
             if ( rs1.next() )
             {
                 targetRef = rs1.getInt(1);
             }
             else
             {
                 // No candidates found. Queue empty.
                 return 0;
             }
 
             rs1.close();
 
             //
             // Set the taskpackage with the found targetreference to active:
             //
             PreparedStatement activateTPStmt = conn.prepareStatement( "UPDATE taskpackage " +
                                           "SET taskstatus=1, accessdate=sysdate, substatus=substatus+1 " +
                                           "WHERE targetreference = ?" );
             activateTPStmt.setInt( 1, targetRef );
             int res = activateTPStmt.executeUpdate();
 
             if ( res != 1 )
             {
                 conn.rollback();
                 // stmt.close();
                 pstmt.close();
                 String errorMsg = String.format( "Error: updated %s row(s). 1 row was expected", res );
                 log.fatal( errorMsg );
 		releaseConnection( conn );
                 throw new HarvesterIOException( errorMsg );
             }
 
             //
             // Retrieve all the lbnr's associated with the targetreference:
             //
             PreparedStatement selectLbnrQueryStmt = conn.prepareStatement( "SELECT lbnr " +
                                            "FROM taskpackagerecordstructure " +
                                            "WHERE targetreference = ? " +
                                            "AND recordstatus = 2 " +
                                            "ORDER BY lbnr" );
             selectLbnrQueryStmt.setInt( 1, targetRef );
             ResultSet rs2 = selectLbnrQueryStmt.executeQuery( );
 
             while ( rs2.next() )
             {
                 ESIdentifier id = new ESIdentifier( targetRef, rs2.getInt(1) );
                 // Add pair to backend of queue
                 jobCandidatesQueue.add( id );
                 retrievedAmount++;
             }
 
         }
         catch ( SQLException sqle )
         {
             String errorMsg = new String( "An SQL error occured while trying to retrieve job candidates" );
             log.fatal( errorMsg, sqle );
 	    releaseConnection( conn );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 
         return retrievedAmount;
     }
 
 
     public String retrieveReferenceData( ESIdentifier id, Connection conn) throws HarvesterIOException
     {
         String referenceData = null;
 
         try
         {
             PreparedStatement pstmt = conn.prepareStatement( "SELECT supplementalid3 " +
                                      "FROM suppliedrecords " +
                                      "WHERE targetreference = ? " +
                                      "AND lbnr = ?");
             pstmt.setInt( 1, id.getTargetRef() );
             pstmt.setInt( 2, id.getLbNr() );
             ResultSet rs = pstmt.executeQuery( );
 
             if ( rs.next() )
             {
                 referenceData = rs.getString(1);
             }
             else
             {
                 String errorMsg = String.format( "Could not retrieve reference data for: %s", id );
                 log.error( errorMsg );
                 releaseConnection( conn );
                 throw new HarvesterIOException( errorMsg );
             }
         }
         catch( SQLException sqle )
         {
             String errorMsg = new String( "An sql exception was caught" );
             log.fatal( errorMsg, sqle );
             releaseConnection( conn );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 
         return referenceData;
     }
 
     Document createReferenceDataDocument( String referenceData, ESIdentifier id ) 
     {
 
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
             log.error( String.format( "Caught error while trying to instantiate documentbuilder '%s'", pce.getMessage() ) );
             DocOK = false;
         }
         catch( SAXException se )
         {
             log.error( String.format( "Could not parse data: '%s'", se.getMessage() ) );
             DocOK = false;
         }
         catch( IOException ioe )
         {
             log.error( String.format( "Could not cast the bytearrayinputstream to a inputsource: '%s'", ioe.getMessage() ) );
             DocOK = false;
         }
 
         if ( DocOK )
         {
             return doc;
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
             catch ( HarvesterIOException hioe )
             {
                 log.error( String.format( "IO Error when changing JobStatus ID: %s Msg: %s ", id, hioe.getMessage() ), hioe );
             }
         }
 
         return doc;
     }
     
 
     public List< TaskInfo > getJobs( int maxAmount ) throws HarvesterIOException, HarvesterInvalidStatusChangeException
     {
         log.info( String.format( "The ES-Harvester was requested for %s jobs", maxAmount ) );
 
         // get a connection from the connectionpool:
         Connection conn;
         try
         {
             conn = connectionPool.getConnection();
         }
         catch ( SQLException sqle )
         {
             String errorMsg = new String("Could not get a db-connection from the connection pool");
             log.fatal( errorMsg, sqle );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 
         List< TaskInfo > theJobList = new ArrayList< TaskInfo >();
 
         log.info( String.format( "Queue size is %s", jobCandidatesQueue.size() ) );
         int retrievedAmount = jobCandidatesQueue.size();
         while ( jobCandidatesQueue.size() < maxAmount )
         {
             log.info( "Adding more candidates to Queue" );
 
             // retrive more candidates and add them to jobCandidatesQueue:
             int retrieved = retrieveCandidatesToQueue( conn );
             retrievedAmount += retrieved;
             log.info( String.format( "Queue size is now: %s", jobCandidatesQueue.size() ) );
             if ( retrieved == 0 )
             {
                 // No new candidates were added.
                 log.info("Break out");
                 break;
             }
         }
 
         int addAmount = retrievedAmount < maxAmount ? retrievedAmount : maxAmount;
         log.info( "addAmount = " + addAmount );
 
         for ( int i = 0; i < addAmount; ++i )
         {
             // Update Recordstatus
             ESIdentifier id = jobCandidatesQueue.removeFirst();
             try
             {
                 setRecordStatusToInProgress( id, conn );
             }
             catch( SQLException sqle )
             {
                 String errorMsg = new String( "An SQL error occured while trying to change recordstatus" );
                 log.fatal( errorMsg, sqle );
 		releaseConnection( conn );
                 throw new HarvesterIOException( errorMsg, sqle );
             }
 
             String referenceData = retrieveReferenceData( id, conn );
 
             Document doc = createReferenceDataDocument( referenceData, id );
             TaskInfo theJob = new TaskInfo( id, doc );
             theJobList.add( theJob );
 
         }
 
         log.info( String.format( "Found %s available Jobs", theJobList.size() ) );
 
         releaseConnection( conn );
 
     	return theJobList;
     }
 
     
     // This function consist of a part of the "old" getData function,
     // since part of the functionbody was needed in getCargoContainer 
     private byte[] getDataDBCall( ESIdentifier ESJobId, Connection conn ) throws HarvesterUnknownIdentifierException, HarvesterIOException
     {
 	byte[] returnData = null;
 
         try
         {
             PreparedStatement pstmt = conn.prepareStatement( "SELECT record " +
                                      "FROM suppliedrecords " +
                                      "WHERE targetreference = ? " +
                                      "AND lbnr = ?" );
             pstmt.setInt( 1, ESJobId.getTargetRef() );
             pstmt.setInt( 2, ESJobId.getLbNr() );
             ResultSet rs = pstmt.executeQuery( );
             if ( ! rs.next() )
             {
                 // The ID does not exist
                 String errorMsg = String.format( "the Identifier %s is unknown in the base", ESJobId );
                 log.error( errorMsg );
 		releaseConnection( conn );
                 throw new HarvesterUnknownIdentifierException( errorMsg );
             }
             else
             {
                 // The ID exist
                 Blob data = rs.getBlob( 1 );
                 long blobLength = data.length();
                 if ( blobLength > 0 )
                 {
                     // Return data
                     returnData = data.getBytes( 1L, (int)blobLength );
                 }
                 else
                 {
                     // For some unknown reason, there is no data associated with the ID.
                     String errorMsg = String.format( "No data associated with id %s", ESJobId );
                     log.error( errorMsg );
 		    releaseConnection( conn );
                     throw new HarvesterIOException( errorMsg );
                 }
             }
         }
         catch( SQLException sqle )
         {
             String errorMsg = new String( "A database error occured " );
             log.fatal(  errorMsg, sqle );
 	    releaseConnection( conn );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 	
         return returnData;
     }
 
 
 
     public CargoContainer getCargoContainer( IIdentifier jobId ) throws HarvesterUnknownIdentifierException, HarvesterIOException
     {
         ESIdentifier id = (ESIdentifier)jobId;
 
         // get a connection from the connectionpool:
         Connection conn;
         try
         {
             conn = connectionPool.getConnection();
         }
         catch ( SQLException sqle )
         {
             String errorMsg = new String("Could not get a db-connection from the connection pool");
             log.fatal( errorMsg, sqle );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 	
         String referenceData = retrieveReferenceData( id, conn );
         Document doc = createReferenceDataDocument( referenceData, id );
         TaskInfo job = new TaskInfo( id, doc );
 
         // Retrieve the data.
         byte[] data = getDataDBCall( id , conn );
 
         log.debug( "Creating CargoContainer" );
 	CargoContainer cargo = new CargoContainer();
 
         try
         {
             cargo.add( DataStreamType.OriginalData, job.getFormat(), job.getSubmitter(), job.getLanguage(), job.getMimeType(),  data );
         }
         catch ( IOException ioe )
         {
             String errorMsg = new String( "Could not add OriginalData to CargoContainer" );
             log.fatal( errorMsg, ioe );
 	    releaseConnection( conn );
             throw new HarvesterIOException( errorMsg, ioe );
         }
 	catch ( RuntimeException e )
 	{
             log.fatal( "Unknown Exception caught", e );
 	    releaseConnection( conn );
 	    throw e;
 	}
 
         releaseConnection( conn );
         return cargo;
     }
 
     /**
      *  Releases a job based on the {@link TaskInfo}.
      *  <p>
      *  When a job is requested using {@link #getJobs} the status of the job in the ES-base 
      *  is changed to reflect that the job is in progress. This function releases this status,
      *  ie. remove the in progress status.
      */
     @Override
     public void releaseJob( IIdentifier jobId ) throws HarvesterIOException
     {

 	ESIdentifier id = (ESIdentifier)jobId;
 
 	log.debug( String.format( "Releasing job: %s", id.toString() ) );
 	
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
 	    PreparedStatement pstmt = conn.prepareStatement( "SELECT targetreference, lbnr, recordstatus " +
                                                              "FROM taskpackagerecordstructure " +
                                                              "WHERE recordstatus = ? " +
                                                              "AND targetreference = ? " +
 							     "ANd lbnr = ? " +
 							     "FOR UPDATE OF recordstatus" );
 	    pstmt.setInt( 1, 3 ); // look for active records
 	    pstmt.setInt( 2, id.getTargetRef() );
 	    pstmt.setInt( 3, id.getLbNr() );
 	    ResultSet rs = pstmt.executeQuery( );
 
 	    int counter = 0;
 	    while ( rs.next() ) 
 	    {
 		++counter;
 	        int targetRef = rs.getInt(1);
 		int lbnr      = rs.getInt(2);
 		log.info( String.format( "Locking for update: targetRef: %s  Lbnr: %s", targetRef, lbnr ) );
 	    }
 	    log.info( String.format( "Locked %d rows for update.", counter ) );
 	    if ( counter == 0 )
 	    {
                 // no rows for update - just close down the statement:
                 conn.rollback();
                 pstmt.close();
 	    }
             else
             {
 		PreparedStatement pstmt2 = conn.prepareStatement( "UPDATE taskpackagerecordstructure " +
 								  "SET recordstatus = ? " + 
 								  "WHERE recordstatus = ? " + 
 								  "AND targetreference = ? " + 
 								  "AND lbnr = ?" );
 
 		pstmt2.setInt( 1, 2 );
 		pstmt2.setInt( 2, 3 );
 		pstmt2.setInt( 3, id.getTargetRef() );
 		pstmt2.setInt( 4, id.getLbNr() );
 		int res2 = pstmt2.executeUpdate( );
 
                 log.info("Updating " + res2 + " rows");
 
                 pstmt.close();
                 pstmt2.close();
                 conn.commit();
             }
 
 	}
         catch( SQLException sqle )
         {
             String errorMsg = String.format( "An SQL error occured while releasing job: %s", id.toString() );
             log.fatal( errorMsg, sqle );
             throw new HarvesterIOException( errorMsg, sqle );
 	}
 	finally
 	{
 	    releaseConnection( conn );
 	}
 
 	log.debug( String.format( "Job released: %s", id.toString() ) );
     }
 
 
     public void setStatusFailure( IIdentifier Id, String failureDiagnostic ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
     {
 	// We accept a null string, but in such cases we will write the empty string:
 	failureDiagnostic = failureDiagnostic == null ? "" : failureDiagnostic;
 
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
 	    releaseConnection( conn );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 
         releaseConnection( conn );
     }
 
 
     public void setStatusSuccess( IIdentifier Id, String PID ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
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
 
 
     /**
      *  Changes the status of a record in the ES-base. 
      *  If the status is {@link JobStatus.RETRY}, then the status of the record will be changed from inProgress to queued.
      *  If the status is allready set to either Success or Failure, then an exception is thrown, since it
      *  is not allowed to change status on an allready finished record.
      */
     private void setStatus( ESIdentifier jobId, JobStatus status, Connection conn ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
     {
         log.info( String.format( "ESHarvester was requested to set status %s on data identified by the identifier %s", status, jobId ) );
 
         // check if the status associated with the identifier has previously been set to success, failure 
 	    // or is queued (i.e. the job is not in progress).
         // If not, set it to what the parameter says.
 	    // Otherwise it is an error - you cannot update a job which is not in progress.
 
         ESIdentifier ESJobId = jobId;
         try
         {
             // Lock row for update
             PreparedStatement pstmt = conn.prepareStatement( "SELECT recordstatus " +
                                     "FROM taskpackagerecordstructure " +
                                     "WHERE targetreference = ? " +
                                     "AND lbnr = ? " +
                                     "FOR UPDATE OF recordstatus" );
             pstmt.setInt( 1, ESJobId.getTargetRef() );
             pstmt.setInt( 2, ESJobId.getLbNr() );
             ResultSet rs = pstmt.executeQuery( );
 	    
             int counter = 0;
             if ( ! rs.next() )
             {
                 // No more rows. If this is the first time rs.next() is called, then no rows where found
                 // with the above statement, and we should throw an exception.
                 if (counter == 0)
                 {
                     conn.rollback();
                     pstmt.close();
                     String errorMsg = String.format( "recordstatus requested for unknown identifier: %s ", ESJobId );
                     log.error( errorMsg );
 		    releaseConnection( conn );
                     throw new HarvesterUnknownIdentifierException( errorMsg );
                 }
             }
             else
             {
                 ++counter;
 
                 //check if the status is set already, i.e. the record is _not_ inProgress:
                 int recordStatus = rs.getInt( 1 );
                 if ( recordStatus != 3 )
                 {
                     String errorMsg = String.format( "the status is already set to %s for identifier: %s", recordStatus, ESJobId );
                     log.error( errorMsg );
                     conn.rollback();
                     pstmt.close();
                     releaseConnection( conn );
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
                         pstmt.close();
                         releaseConnection( conn );
                         throw new HarvesterInvalidStatusChangeException( "Unknown status" );
                 }
 
                 PreparedStatement pstmt2 = conn.prepareStatement( "UPDATE taskpackagerecordstructure " +
                                           "SET recordstatus = ? " +
                                           "WHERE targetreference = ? " +
                                           "AND lbnr = ? " );
                 pstmt2.setInt( 1, new_recordStatus );
                 pstmt2.setInt( 2, ESJobId.getTargetRef() );
                 pstmt2.setInt( 3, ESJobId.getLbNr() );
                 int updateResult = pstmt2.executeUpdate( );
 
                 if( updateResult != 1 )
                 {
                     log.warn( String.format( "unknown status update attempt on identifier: %s - updateResult=%s", ESJobId, updateResult ) );
                 }
 
                 // Check the taskspecificUpdate for update of TP-status:
                 setTaskPackageStatus( ESJobId.getTargetRef(), conn );
             }
         }
         catch( SQLException sqle )
         {
             log.fatal( "A database error occured", sqle );
 	    releaseConnection( conn );
             throw new HarvesterIOException( "A database error occured", sqle );
         }
     }
 
 
 
 
     /**
      * Updates the field taskpackagerecordstructure.recordstatus in ES to be 
      * inProgress (value: 3) for the taskpackagerecordstructure with targetRef and lbnr.
      */
     private void setRecordStatusToInProgress( ESIdentifier ESJobId, Connection conn ) throws HarvesterIOException, SQLException
     {
         log.info( String.format( "Updating recordstatus for ID: %s", ESJobId ) );
 
         // Locking the row for update:
         PreparedStatement pstmt = conn.prepareStatement( "SELECT recordstatus " +
                                  "FROM taskpackagerecordstructure " +
                                  "WHERE targetreference = ? " +
                                  "AND lbnr = ? " +
                                  "AND recordstatus = ? " +
                                  "FOR UPDATE OF recordstatus" );
         pstmt.setInt( 1, ESJobId.getTargetRef() );
         pstmt.setInt( 2, ESJobId.getLbNr() );
         pstmt.setInt( 3, 2 ); // recordstatus
         int res1 = pstmt.executeUpdate( );
         
         // Testing all went well:
         if (res1 != 1)
         {
             // Something went wrong - we did not lock a single row for update
             log.fatal( "Error: Result from select for update was " + res1 + ". Not 1." );
             String errorMsg = String.format( "RecordStatus was allready set in ES-base for %s", ESJobId );
             log.fatal( errorMsg );
             pstmt.close();
             conn.rollback();
 	    releaseConnection( conn );
             throw new HarvesterIOException( errorMsg );
         }
 
         // Updating recordstatus in row:
         PreparedStatement pstmt2 = conn.prepareStatement( "UPDATE taskpackagerecordstructure " +
                                   "SET recordstatus = ? " +
                                   "WHERE targetreference = ? " +
                                   "AND lbnr = ? " +
                                   "AND recordstatus = ?" );
         pstmt2.setInt( 1, 3 ); // recordstatus (after)
         pstmt2.setInt( 2, ESJobId.getTargetRef() );
         pstmt2.setInt( 3, ESJobId.getLbNr() );
         pstmt2.setInt( 4, 2 ); // recordstatus (before)
         int res2 = pstmt2.executeUpdate( );
 
         if ( res2 != 1 )
         {
             // Something went wrong - we did not update a single row
             log.error( "Error: Result from update was " + res2 + ". Not 1" );
             pstmt.close();
             pstmt2.close();
             conn.rollback();
             return;
         }
 
         // Committing the update:
         // \todo: Is this inefficient?
         pstmt.close();
         pstmt2.close();
         conn.commit();
     }
 
 
     private enum TaskStatus
     {
         PENDING,
         ACTIVE,
         COMPLETE,
         ABORTED;
     }
 
 
     private void testAndSetTaskpackageTaskstatusToActive( int targetref, Connection conn ) throws HarvesterInvalidStatusChangeException, SQLException
     {	
         log.info( "Testing wether Taskpackage.Taskstatus should be updated.");
 
         PreparedStatement pstmt = conn.prepareStatement( "SELECT taskstatus " +
                                  "FROM taskpackage " +
                                  "WHERE targetreference = ?" );
         pstmt.setInt( 1, targetref );
         ResultSet rs = pstmt.executeQuery( );
 
         if ( rs.next() )
         {
             int currentStatus = rs.getInt( 1 );
             if ( currentStatus != 1 ) // Not Active
             {
                 // Set the status to active:
                 log.info( String.format( "Setting taskpackage.taskstatus from %s to %s for targetref %s",
 					 currentStatus, 1, targetref) );
                 setTaskpackageTaskstatusAndSubstatus( targetref, TaskStatus.ACTIVE, conn );
             }
         }
         else
         {
             log.warn( String.format( "Could not find the taskpackage for targetref %s", targetref ) );
         }
 
         pstmt.close();
     }
 
 
    private void setTaskpackageTaskstatusAndSubstatus( int targetref, TaskStatus status, Connection conn ) throws SQLException, HarvesterInvalidStatusChangeException
     {
         log.info( String.format( "Setting Taskpackage.taskstatus with targetref [%s] to %s.", targetref, status ) );
 
         int taskstatus = 0;
         switch (status)
         {
             case PENDING:
                 taskstatus = 0;
                 break;
             case ACTIVE:
                 taskstatus = 1;
                 break;
             case COMPLETE:
                 taskstatus = 2;
                 break;
             case ABORTED:
                 taskstatus = 3;
                 break;
             default:
                 // This should not happen!
                 conn.rollback();
		releaseConnection( conn );
                throw new HarvesterInvalidStatusChangeException( String.format( "Unknown status for Taskpackage.taskstatus: %s", status ) );
         }
 
         // lock row for update:
         PreparedStatement pstmt = conn.prepareStatement( "SELECT taskstatus, substatus " +
                                  "FROM taskpackage " +
                                  "WHERE targetreference = ? " +
                                  "FOR UPDATE OF taskstatus, substatus" );
         pstmt.setInt( 1, targetref );
         ResultSet rs = pstmt.executeQuery( );
 
         if ( !rs.next() )
         {
             // No rows for update - give a warning:
                 log.warn( String.format( "Could not find a row for update for targetref: %s", targetref ) );
                 conn.rollback();
             pstmt.close();
 
         }
         else
         {
             int substatus = rs.getInt(2);
             substatus++; // increment to next value for update of substatus
 
                 // Perform the update:
             PreparedStatement pstmt2 = conn.prepareStatement( "UPDATE taskpackage " +
                                      "SET taskstatus = ?, substatus = ? " +
                                      "WHERE targetreference = ? ");
             pstmt2.setInt( 1, taskstatus );
             pstmt2.setInt( 2, substatus );
             pstmt2.setInt( 3, targetref );
             int res2 = pstmt2.executeUpdate( );
 
             if ( res2 == 0 )
             {
                 log.warn( String.format( "Could not update taskstatus for taskpackage with targetref: %s", targetref ) );
             }
             else
             {
                 log.info( String.format( "Successfully set Taskpackage.taskstatus with targetref [%s] to %s.", targetref, status ) );
             }
 
             pstmt.close();
             conn.commit();
         }
     }
 
 
     /**
      *  Changes the status on a taskpackage if all assoicated records are finished.
      */
     private void setTaskPackageStatus( int targetref, Connection conn ) throws HarvesterInvalidStatusChangeException, SQLException
     {
         log.debug( String.format( "setTaskPackageStatus with targetRef %s", targetref ) );
 
         PreparedStatement pstmt = conn.prepareStatement( "SELECT noofrecs, noofrecs_treated " +
                                  "FROM taskspecificupdate " +
                                  "WHERE targetreference = ?" );
         pstmt.setInt( 1, targetref );
         ResultSet rs1 = pstmt.executeQuery( );
 
         while ( rs1.next() )
         {
             int noofrecs = rs1.getInt( 1 );
             int noofrecs_treated = rs1.getInt( 2 );
             log.debug( String.format( "NoOfRecords: %s   NoOfRecordsTreated: %s", noofrecs, noofrecs_treated ) );
 
             if ( noofrecs < noofrecs_treated )
             {
                 // This is an error. There were more treated records than actual records.
                 // This _must_ never happen.
 		releaseConnection( conn );
                 throw new HarvesterInvalidStatusChangeException( String.format( "Error: There were more treated records than actual records in taskpackage %s. This should never ever happen.", targetref ) );
             }
             else if ( noofrecs == noofrecs_treated )
             {
 		
                 // find the number of success and failures on the taskpackage:
                 PreparedStatement pstmt2 = conn.prepareStatement( "SELECT scount, fcount  " +
                                           "FROM " +
                                           "(SELECT count( recordstatus ) scount " +
                                           " FROM taskpackagerecordstructure " +
                                           " WHERE targetreference = ? " +
                                           " AND recordstatus = ?) a , " +
                                           "(SELECT count( recordstatus ) fcount " +
                                           " FROM taskpackagerecordstructure " +
                                           " WHERE targetreference = ? " +
                                           " AND recordstatus = ?) b" );
                 pstmt2.setInt( 1, targetref );
                 pstmt2.setInt( 2, 1 ); // recordstatus == 1 (success)
                 pstmt2.setInt( 3, targetref );
                 pstmt2.setInt( 4, 4 ); // recordstatus == 4 (failure)
                 ResultSet failure_success_rs = pstmt2.executeQuery( );
 
                 int counter2 = 0;
                 int success_count = 0;
                 int failure_count = 0;
                 while ( failure_success_rs.next() )
                 {
                     success_count = failure_success_rs.getInt( 1 );
                     failure_count = failure_success_rs.getInt( 2 );
                     ++counter2;
                 }
 
                 pstmt2.close();
 
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
 
                 PreparedStatement pstmt3 = conn.prepareStatement( "SELECT updatestatus " +
                                           "FROM taskspecificupdate " +
                                           "WHERE targetreference = ? " +
                                           "FOR UPDATE OF updatestatus" );
                 pstmt3.setInt( 1, targetref );
                 ResultSet update_taskpackage_status_rs = pstmt3.executeQuery( );
 
                 if ( ! update_taskpackage_status_rs.next() )
                 {
                     String errorMsg = String.format( "The updatestatus for the taskpackage could not be updated. TaskSpecificUpdate with targetref %s was not found in base", targetref );
                     log.error( errorMsg );
                     conn.rollback();
                     pstmt3.close();
 		    releaseConnection( conn );
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
                         pstmt3.close();
 			releaseConnection( conn );
                         throw new HarvesterInvalidStatusChangeException( errorMsg );
 			
                     }
 
                     PreparedStatement pstmt4 = conn.prepareStatement( "UPDATE taskspecificupdate " +
                                                                               "SET updatestatus = ? " +
                                                                               "WHERE targetreference = ?" );
                     pstmt4.setInt( 1, update_status );
                     pstmt4.setInt( 2, targetref );
 
                     try
                     {
                         int res = pstmt4.executeUpdate( );
                         log.debug( String.format( "%s rows updated" , res ) );
                         conn.commit();
                         // set the taskpackage.taskstatus to complete:
                         setTaskpackageTaskstatusAndSubstatus( targetref, TaskStatus.COMPLETE, conn );
                     }
                     catch( SQLException sqle )
                     {
                         String errorMsg = String.format( "An SQL error occured when trying to update updatestatus in TaskSpecificUpdate with targetref %s" , targetref);
                         log.error( errorMsg, sqle );
                         pstmt4.close();
                         conn.rollback();
 			releaseConnection( conn );
                         throw new HarvesterInvalidStatusChangeException( errorMsg, sqle );
                     }
                 }
 
                 pstmt3.close();
             }
         }
 
         pstmt.close();
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
         PreparedStatement pstmt = null;
 
         try
         {
             // Get unique diagnostics.number:
     	    pstmt = conn.prepareStatement( "select diagIdSeq.nextval from dual" );
             ResultSet rs = pstmt.executeQuery( );
 
             if ( !rs.next() )
             {
                 String errorMsg = String.format( "Could not create a unique identifier for diagIdSeq in the ES base for ESId: %s.", Id );
                 log.fatal( errorMsg );
         		pstmt.close();
                 conn.rollback();
 		releaseConnection( conn );
                 throw new HarvesterIOException( errorMsg );
             }
 
             diagnosticId = rs.getInt( 1 );
         }
         catch( SQLException sqle )
         {
             String errorMsg = String.format( "A database error occured when trying to retrive unique id from diagIdSeq in ES base for ESId: %s.", Id );
             log.fatal( errorMsg, sqle );
             pstmt.close();
             conn.rollback();
 	    releaseConnection( conn );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 
         pstmt.close();
 
         // Create new row in diagnostics table:
         PreparedStatement pstmt2 = null;
         try
         {
             int lbnr = 1;
             String diagSetId = new String("'10.100.1.1'");
             int condition = 100;
 
             pstmt2 = conn.prepareStatement( "INSERT INTO " + 
 					    "diagnostics (id, lbnr, diagnosticSetId, condition, addInfo) " +
 					    "VALUES ( ?, ?, ?, ?, ? )" );
             pstmt2.setInt( 1, diagnosticId );
             pstmt2.setInt( 2, lbnr );
             pstmt2.setString( 3, diagSetId );
             pstmt2.setInt( 4, condition );
             pstmt2.setString( 5, failureDiagnostic );
             pstmt2.executeQuery( );
 
         }
         catch( SQLException sqle )
         {
             String errorMsg = new String( "Could not insert diagnostic with id: " + diagnosticId );
             log.fatal( errorMsg, sqle );
             conn.rollback();
             pstmt2.close();
 	    releaseConnection( conn );
             throw new HarvesterIOException( errorMsg, sqle );
         }
 
         // Attach diagnostic to failed record:
         PreparedStatement pstmt3 = null;
         try
         {
             pstmt3 = conn.prepareStatement( "SELECT recordOrSurDiag2 " +
                             "FROM taskpackagerecordstructure " +
                             "WHERE targetreference = ? " +
                             "AND lbnr = ? " +
                             "FOR UPDATE OF recordOrSurDiag2" );
             pstmt3.setInt( 1, Id.getTargetRef() );
             pstmt3.setInt( 2, Id.getLbNr() );
             int res = pstmt3.executeUpdate( );
 
             // Testing all went well:
             if (res != 1)
             {
                 // Something went wrong - we did not lock a single row for update
                 log.error( "Error: Result from select for update was " + res + ". Not 1." );
                 pstmt3.close();
                 conn.rollback();
                 return;
             }
 
             // Updating recordOrSurDiag2 in row:
             pstmt3 = conn.prepareStatement( "UPDATE taskpackagerecordstructure " +
                                             "SET recordOrSurDiag2 = ? " +
                                             "WHERE targetreference = ? " +
                                             "AND lbnr = ?" );
             pstmt3.setInt( 1, diagnosticId );
             pstmt3.setInt( 2, Id.getTargetRef() );
             pstmt3.setInt( 3, Id.getLbNr() );
             int res2 = pstmt3.executeUpdate( );
 	    
             if (res2 != 1)
             {
                 // Something went wrong - we did not update a single row
                 log.error( "Error: Result from update was " + res2 + ". Not 1" );
                 pstmt3.close();
                 conn.rollback();
                 return;
             }
 
             pstmt3.close();
             conn.commit();
         }
         catch ( SQLException sqle )
         {
             String errorMsg = String.format( "Could not attach diagnostic (Id: %s) to taskpackagerecordstructure (Id: %s) with text: [%s]", diagnosticId, Id, failureDiagnostic );
             log.fatal( errorMsg, sqle );
             pstmt3.close();
             conn.rollback();
 	    releaseConnection( conn );
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
             PreparedStatement pstmt = conn.prepareStatement( "SELECT record_id " +
                                      "FROM taskpackagerecordstructure " +
                                      "WHERE targetreference = ? " +
                                      "AND lbnr = ? " +
                                      "FOR UPDATE OF record_id" );
             pstmt.setInt( 1, Id.getTargetRef() );
             pstmt.setInt( 2, Id.getLbNr() );
             int res1 = pstmt.executeUpdate( );
 
             // Testing all went well:
             if (res1 != 1)
             {
                 // Something went wrong - we did not lock a single row for update
                 log.error( "Error: Result from select for update was " + res1 + ". Not 1." );
                 pstmt.close();
                 conn.rollback();
                 return;
             }
 
             pstmt.close();
 	   
             PreparedStatement pstmt2 = conn.prepareStatement( "UPDATE taskpackagerecordstructure " +
                                                               "SET record_id = ? " +
                                                               "WHERE     targetreference = ? " +
                                                               "      AND lbnr = ?" );
             pstmt2.setString( 1, PID );
             pstmt2.setInt( 2, Id.getTargetRef() );
             pstmt2.setInt( 3, Id.getLbNr() );
             int res2 = pstmt2.executeUpdate( );
 
             // Testing all went well:
             if (res2 != 1)
             {
                 // Something went wrong - we did not lock a single row for update
                 log.error( "Error: Result from select for update was " + res2 + ". Not 1." );
                 pstmt2.close();
                 conn.rollback();
                 return;
             }
 
             pstmt2.close();
             conn.commit();
         }
         catch( SQLException sqle )
         {
             // If, for some reason, the PID can not be set in the ES-base we must not
             // break down. Just set a warning.
             // Note: When the record_id is set to >= 64 in the ES-base, this method
             // _should_ not set a warning.
             String errorMsg = String.format( "Could not set the PID: [%s] for the identifier: %s.", PID, Id);
             log.warn( errorMsg, sqle );
         }
     }
 
 
     private void releaseConnection( Connection conn ) // throws HarvesterIOException
     {
         // Close the connection:
 	log.trace( "Trying to Release Connection");
 
         try
         {
             if ( !conn.isClosed() )
             {
                 conn.close();
 		log.debug( "Connection closed" );
             }
 	    else 
 	    {
 		log.debug( "Connection was already closed" );
 	    }
         }
         catch( SQLException sqle )
         {
 
 	    log.debug( "An Exception occured when trying to release the connection" );
 
             String errorMsg = new String( "Could not release the database connection" );
             log.fatal(  errorMsg, sqle );
             // throw new HarvesterIOException( errorMsg, sqle );
         }
     }
 
     private final class ESIdentifier implements IIdentifier
     {
         private int targetReference;
         private int lbNr;
 
         ESIdentifier( int targetRef, int lbNr )
         {
             targetReference = targetRef;
             this.lbNr = lbNr;
         }
 
         int getTargetRef()
         {
             return targetReference;
         }
 
         int getLbNr()
         {
             return lbNr;
         }
 
         @Override
         public String toString()
         {
             return String.format( "[TargetRef=%s Lbnr=%s]", targetReference, lbNr );
         }
     }
 }
