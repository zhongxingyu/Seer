 
 /**********************************************************************
  $Id: PhysicalOperator.java,v 1.14 2002/10/03 15:33:16 tufte Exp $
 
 
   NIAGARA -- Net Data Management System                                 
                                                                         
   Copyright (c)    Computer Sciences Department, University of          
                        Wisconsin -- Madison                             
   All Rights Reserved.                                                  
                                                                         
   Permission to use, copy, modify and distribute this software and      
   its documentation is hereby granted, provided that both the           
   copyright notice and this permission notice appear in all copies      
   of the software, derivative works or modified versions, and any       
   portions thereof, and that both notices appear in supporting          
   documentation.                                                        
                                                                         
   THE AUTHORS AND THE COMPUTER SCIENCES DEPARTMENT OF THE UNIVERSITY    
   OF WISCONSIN - MADISON ALLOW FREE USE OF THIS SOFTWARE IN ITS "        
   AS IS" CONDITION, AND THEY DISCLAIM ANY LIABILITY OF ANY KIND         
   FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE USE OF THIS SOFTWARE.   
                                                                         
   This software was developed with support by DARPA through             
    Rome Research Laboratory Contract No. F30602-97-2-0247.  
 **********************************************************************/
 
 
 package niagara.query_engine;
 
 /**
  * This is the Abstract <code>PhysicalOperator</code> class from which
  * all operator are derived. It provides the basic control flow and all
  * each operator has to do is to implement stubs.
  *
  * @version 1.0
  */
 
 import java.lang.reflect.Array;
 import java.io.*;
 
 import niagara.utils.*;
 import niagara.data_manager.*;
 
 import org.w3c.dom.Document;
 
 public abstract class PhysicalOperator {
 
     // Source streams and information about them
     // blocking indicates if the operator blocks on that source stream or not
     private SourceTupleStream[] sourceStreams;
     private boolean[] blockingSourceStreams;
     int numSourceStreams;
 
     // Sink streams and information about them
     // partial result count is the counts of the duplicated
     // partial result request message expected for each stream
     private SinkTupleStream[] sinkStreams;
     private int[] sinkStreamsPartialResultCount;
     int numSinkStreams;
 
     //The required responsiveness to control messages
     private int responsiveness;
 
     //  The vector of open source streams to read from 
     // active means we are currently reading from that stream,
     // note a stream can be open but not active - this is due
     // to partial result processing
     private StreamIdList activeSourceStreams;
 
     // The index (in readInputStreams) of the last source stream read
     private int lastReadSourceStream;
 
     // move head operator functionality into PhysOp
     private boolean isHeadOperator;
     private QueryInfo queryInfo;
 
     // for triggers
     protected static DataManager DM;
    protected String name = "default";
 
     /**
      * This class is used to store the result of a read operation from
      * a set of source streams. There are two components (a) the stream element
      * read and (b) the stream from which the result was read.
      */
 
     private class SourceStreamsObject {
 	public StreamTupleElement tuple;
 	public int streamId;
     }
 
     /**
      * This is the constructor for the Operator class that initializes
      * it with the appropriate logical operator, source streams,
      * sink streams, whether the operator is blocking or non-blocking
      * and the responsiveness of the operator to control information.
      *
      * @param sourceStreams The Source Streams associated with the operator
      * @param sinkStreams The Sink Streams associated with the
      *                           operator
      * @param blockingSourcetreams A boolean array that specifies the source
      *                             streams due to which the operator blocks.
      * @param responsiveness The responsiveness, in milliseconds, to control
      *                       messages
      */
      
     public PhysicalOperator (SourceTupleStream[] sourceStreams,
 			     SinkTupleStream[] sinkStreams,
 			     boolean[] blockingSourceStreams,
 			     Integer responsiveness) {
 	this.sourceStreams = sourceStreams;
 	numSourceStreams = sourceStreams.length;
 	this.sinkStreams = sinkStreams;
 	numSinkStreams = sinkStreams.length;
 
 	// Initialize which source streams the operator blocks on
 	this.blockingSourceStreams = blockingSourceStreams;
 
 	// Set the number of duplicate partial request messages in each
 	// sink stream to 0
 	sinkStreamsPartialResultCount = new int[numSinkStreams];
 	for (int i = 0; i < numSinkStreams; i++) {
 	    sinkStreamsPartialResultCount[i] = 0;
 	}
 
 	this.responsiveness = responsiveness.intValue();
 
 	// Initially, have to read from all input streams (all input streams
 	// are open)
 	activeSourceStreams = new StreamIdList(numSourceStreams);
 	for (int src = 0; src < numSourceStreams; src++) {
 	    activeSourceStreams.add(src);
 	}
 
 	// Start reading from the first input stream
 	lastReadSourceStream = 0;
     }
 
     /**
      * Is this operator ready for execution (All sink
      * streams in place?)
      */
     public boolean isReady() {
 	for (int i = 0; i < sinkStreams.length; i++) {
 	    if (sinkStreams[i] == null)
 		return false;
 	}
 	return true;
     }
 
     /**
      * This function sets up the flow of control for the operator by
      * reading from input streams and writing to sink streams. All
      * control messages are handled here.
      *
      */
     public final void execute () {
 
 	// Set up an object for reading from source streams
 	SourceStreamsObject sourceObject = new SourceStreamsObject();
 
 	try {
 	    // First initialize any necessary data structures etc. for the
 	    // operator, shut down if this fails
 	    initialize();
 
 	    // Loop by reading inputs and processing them until there is at
 	    // least one open input stream
 	    while (existsUnClosedSourceStream()) {	
 		// Read the object from any of the valid input streams,
 		// timing out if nothing available in any input stream
 		getFromSourceStreams(sourceObject);
 
 		if (!(sourceObject.tuple == null)) {
 		    //If this is a punctuation, then handle it specially
 		    if (sourceObject.tuple.isPunctuation())
 			processPunctuation
 			    ((StreamPunctuationElement) sourceObject.tuple,
 			     sourceObject.streamId);
 
 		    else {
 			// There was some tuple element read, so process it
 			// using the appropriate method 
   			if (isBlocking()) {
 	 		    blockingProcessSourceTupleElement
 		 		(sourceObject.tuple,
 				 sourceObject.streamId);
 			} else {
 			    nonblockingProcessSourceTupleElement
 				(sourceObject.tuple,
 				 sourceObject.streamId);
 			}
 		    }
 		}
 
 		// Now check to see whether there are any control elements from
 		// the sink streams and process them if so.
 		checkForSinkCtrlMsg();
 	    } // end of while loop
 	} catch (java.lang.InterruptedException e) {
 	    shutDownOperator();
 	    internalCleanUp();
 	    return;
 	} catch (ShutdownException see) {
 	    shutDownOperator();
 	    internalCleanUp();
 	    return;
 	} catch (UserErrorException uee) {
 	    // KT - really should propagate this error back to client,
 	    // however, for now will just print error message
 	    System.err.println("USER ERROR: " + uee.getMessage());
 	    shutDownOperator();
 	    internalCleanUp();
 	    return;	    
 	}
 
 	// shut down normally by closing sink streams and do
 	// any necessary clean up
 	closeSinkStreams();
 	internalCleanUp();
     }
 
     /** 
      * get stream at index idx - yeuch, I hate this function and
      * the one below KT
      */
     public final SinkTupleStream getSinkStream(int idx) {
 	return sinkStreams[idx];
     }
 
     /** 
      * add this stream to sink streams at index i - boy, I do not
      * like this function or its use in ExecutionScheduler, but
      * I don't want to change it now - just need to get the
      * batching working KT
      */
     public final void setSinkStream(int idx, SinkTupleStream newStream) {
 	sinkStreams[idx] = newStream;
     }
 
     /**
      * This function returns true if there is at least one unclosed
      * source stream, false otherwise.
      *
      * @return true if >=1 unclosed source stream, false otherwise
      */
     private boolean existsUnClosedSourceStream () {
 	for (int i = 0; i<numSourceStreams; i++) {
 	    if (!sourceStreams[i].isClosed()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     /**
      * This function returns true if the operator is blocking and false
      * otherwise
      *
      * @return True if the operator is blocking and false otherwise
      */
     private boolean isBlocking () {
 	// If any blocking source stream is not closed, then return false
 	// else return true
 	for (int src = 0; src < numSourceStreams; src++) {
 	    if (blockingSourceStreams[src] && !sourceStreams[src].isClosed()) {
 		// Operator is blocking
 		return true;
 	    }
 	}
 	// Operator is non-blocking
 	return false;
     }
 
 
     /**
      * This function sends (best effort) shutdown messages to all
      * source and sink streams
      */
     private void shutDownOperator () {	
 	shutdownTrigOp();
 
 	// try to close each sink stream, even if we get an error closing
 	// one stream, we still try to close all the rest
 	// ignore all errors since we are shutting down (this is
 	// abnormal shutdown - some error has occurred or user requested this)
 	for(int i = 0; i<numSourceStreams; i++) {
 	    if(!sourceStreams[i].isClosed()) {
 		try {
 		    sendCtrlMsgToSource(CtrlFlags.SHUTDOWN, i);
 		} catch(ShutdownException e) {
 		}
 	    }
 	}
 	for(int i = 0; i<numSinkStreams; i++) {
 	    if(!sinkStreams[i].isClosed()) {
 		try {
 		    sendCtrlMsgToSink(CtrlFlags.SHUTDOWN, i);
 		} catch (ShutdownException e) {
 		} catch (InterruptedException e) {
 		}
 	    }
 	}
     }
 
     /**
      * This function reads from a set of source streams specified by
      * the variable activeSourceStreams and modifies sourceObject that
      * contains the source element read and the stream from which it was
      * read. If no source element was read, the the element field in
      * sourceObject is set to null. This operation splits the
      * responsiveness time of the operator among all the source streams it
      * can read from. If this responsiveness time is exceeded, it returns null
      * in the element field of sourceObject.
      *
      * @exception java.lang.InterruptedException Thrown if the thread is
      *            interrupted during execution
      *            write to a previously closed stream
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void getFromSourceStreams (SourceStreamsObject sourceObject)
 	throws java.lang.InterruptedException, ShutdownException {
 	
 	// Get the number of source streams to read from
 	// and calculate the time out for each stream
 	int numActiveSourceStreams = activeSourceStreams.size();
 	int timeout = responsiveness/numActiveSourceStreams;
 	
 	// Make sure the last read source stream is a valid index
 	if (lastReadSourceStream >= numActiveSourceStreams) {
 	    lastReadSourceStream = 0;
 	}
 	
 	// Start from next source stream
 	lastReadSourceStream = 
 	    (lastReadSourceStream + 1)%numActiveSourceStreams;
 	
 	// Store this stream index
 	int startReadSourceStream = lastReadSourceStream;
 
 	// Loop over all source streams, until a full round is completed
 	do {
 	    // Get the next stream id to read from
 	    int streamId = activeSourceStreams.get(lastReadSourceStream);
 	    
 	    // Wait for input from a source stream until the time out
 	    StreamTupleElement tuple = 
 		sourceStreams[streamId].getTuple(timeout);
 
 	    if (!(tuple == null)) {
 		// We have a tuple, set appropriate values for result
 		// and return indicating the operator can continue
 		sourceObject.tuple = tuple;
 		sourceObject.streamId = streamId;
 		return; 
 	    } else {
 		// getNextTuple returned null, meaning we got a control
 		// message or the call timed out
 
 		// if we timed out, try the next stream
 		if(sourceStreams[streamId].timedOut()) {
 		    lastReadSourceStream = 
 			(lastReadSourceStream + 1)%numActiveSourceStreams;
 		    continue;
 		}
 		    
 		// ok, we got a ctrl message, so process it
 		int ctrlFlag = sourceStreams[streamId].getCtrlFlag();
 
 		// No tuple element to be returned
 		sourceObject.tuple = null;
 		processCtrlMsgFromSource(ctrlFlag, streamId);
 	    } 
	} while (startReadSourceStream != lastReadSourceStream &
		 existsUnClosedSourceStream());
 	
 	// No luck with any source stream, operator can still continue
 	sourceObject.tuple = null;
 	return;
     }
 
 
     /**
      * This function checks the sink streams for any control messages
      * and processes them if necessary
      *
      * @exception java.lang.InterruptedException If the thread is interrupted
      *            during execution
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void checkForSinkCtrlMsg()
 	throws java.lang.InterruptedException, ShutdownException {
 	// Loop over all sink streams, checking for control elements
 	for (int sinkId = 0; sinkId < numSinkStreams; sinkId++) {
 	    // Make sure the stream is not closed before checking is done
 	    if (!sinkStreams[sinkId].isClosed()) {
 		int ctrlFlag = sinkStreams[sinkId].getCtrlMsg();
 
 		// If got a ctrl message, process it
 		if (ctrlFlag !=  CtrlFlags.NULLFLAG) 
 		    processCtrlMsgFromSink(ctrlFlag, sinkId);
 	    }
 	}
     }
 
     /**
      * This function closes all sink streams that have not previously
      * been closed.
      */
     private void closeSinkStreams ()  {
 	// Loop over all unclosed sink streams and close them
 	// this will send EOS messages up all the sink streams
 	for (int sinkId = 0; sinkId < numSinkStreams; sinkId++) {
 	    if (!sinkStreams[sinkId].isClosed()) {
 		try {
 		    sinkStreams[sinkId].endOfStream();
 		} catch (InterruptedException e) {
 		    // ignore since we are closing the stream
 		} catch (ShutdownException e) {
 		    // ignore since we are closing the stream anyway
 		}
 	    }
 	}
     }
 
     /**
      * This function processes a control element from a source stream.
      *
      * @param ctrlFlag The id of the control message received
      * @param streamId The id of the source stream from which the control
      *                 message was read
      *
      * @exception java.lang.InterruptedException Thread is interrupted
      *            during execution
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void processCtrlMsgFromSource (int ctrlFlag, int streamId)
 	throws java.lang.InterruptedException, ShutdownException {
 	// upstream control messages are SYNCH_PARTIAL
 	// END_PARTIAL and EOS. We should not get GET_PARTIAL,
 	// NULLFLAG or SHUTDOWN here (SHUTDOWN handled with exceptions)
 
 	switch (ctrlFlag) {
 	case CtrlFlags.SYNCH_PARTIAL:
 	    // This stream should no longer be active and should be
 	    // in sychronizing partial state
 	    activeSourceStreams.remove(streamId);
 	    sourceStreams[streamId].setStatus(SourceTupleStream.SynchPartial);
 	    // Need to handle the creation of partial results
 	    updatePartialResultCreation ();
 	    return;
 
 	case CtrlFlags.END_PARTIAL:
 	    // End of partial result, so stop reading from this stream
 	    // and set status appropriately
 	    activeSourceStreams.remove(streamId);
 	    sourceStreams[streamId].setStatus(SourceTupleStream.EndPartial);
 	    // Need to handle the creation of partial results
 	    updatePartialResultCreation ();
 	    return;
 
 	case CtrlFlags.EOS:		    
 	    // This is the end of stream, so mark the stream as closed
 	    // and remove it from the list of streams to read from
 	    sourceStreams[streamId].setStatus(SourceTupleStream.Closed);
 	    activeSourceStreams.remove(streamId);
 	    
 	    // If this causes the operator to become non-blocking, then
 	    // put out the current output and clear the current output
 	    if (blockingSourceStreams[streamId] && !isBlocking()) {
 		// Output the current results (which are not partial anymore)
 		// and indicate to operator that it should clear the
 		// current results since we are transition to nonblocking
 		flushCurrentResults(false);
 		    
 		// Update partial result creation if any. This is necessary 
 		// because partial result creation may terminate when all 
 		// input streams are either synchronized or closed.
 		updatePartialResultCreation();
 
 		shutdownTrigOp();
 	    }
 	    return;
 
 	    default:
 		throw new PEException("KT unexpected control message from source " + CtrlFlags.name[ctrlFlag]);
 	}
     }
 
     /**
      * This function handles the creation of partial results
      *
      * @exception java.lang.InterruptedException Thrown if the thread is
      *            interrupted during execution
      * @exception ShutdownException query shutdown by user or execution error
      */
     private final void updatePartialResultCreation ()
 	throws java.lang.InterruptedException, ShutdownException {
 
 	// If activeSourceStreams is not empty, then synchronization has not
 	// happened yet, so just return and continue processing
 	if (!activeSourceStreams.isEmpty()) {
 	    return;
 	} else {
 	    // Synchronization has happened, check whether partial results are
 	    // required at all - note that both blocking and non-blocking
 	    // operators synchronize on partial results
 	    boolean isPartialResult = false;
 
 	    // KT - SynchPartial comes from non-blocking sources,
 	    // EndPartial from blocking sources
 	    for (int src=0; src<numSourceStreams && !isPartialResult; src++) {
 		int streamStatus = sourceStreams[src].getStatus();
 		if (streamStatus == SourceTupleStream.SynchPartial ||
 		    streamStatus == SourceTupleStream.EndPartial) {
 		    isPartialResult = true;
 		}
 	    }
 
 	    // If all streams are closed, then there are no partial results
 	    // so just return
 	    if (!isPartialResult) 
 		return;
 
 	    // There are partial results, so write them out
 	    // Write out the (partial) current output, if this is 
 	    // a blocking operator
 	    if (isBlocking()) 
 		flushCurrentResults(true);
 
 	    // Variable to check whether the partial output is partial or
 	    // just synchronized
 	    // output is partial if 1) this operator is blocking or 
 	    // 2) some of the input is partial
 	    boolean isPartialOutput = isBlocking();
 
 	    // Loop over all the source streams and change all streams
 	    // with partial results to Open
 	    for (int src = 0; src < numSourceStreams; ++src) {
 		if (sourceStreams[src].getStatus() ==
 		    SourceTupleStream.SynchPartial) {
 		    // This is a non-blocking stream, so first make it
 		    // open and then add it to the list of streams to read from
 		    sourceStreams[src].setStatus(SourceTupleStream.Open);
 		    activeSourceStreams.add(src);
 		} else if (sourceStreams[src].getStatus() ==
 			   SourceTupleStream.EndPartial) {
 
 		    // This is a blocking stream, so remove traces of
 		    // partial result
 		    removeEffectsOfPartialResult(src);
 
 		    // Make the stream open and add it to the list
 		    // of streams to read from
 		    sourceStreams[src].setStatus(SourceTupleStream.Open);
 		    activeSourceStreams.add(src);
 
 		    // The result is a partial result
 		    isPartialOutput = true;
 		}
 	    }
 
 	    // If the output is a partial result, then put the appropriate
 	    // control msg, else send a synchronize partial message
 	    if (isPartialOutput) {
 		sendCtrlMsgToSinks(CtrlFlags.END_PARTIAL);
 	    } else {
 		sendCtrlMsgToSinks(CtrlFlags.SYNCH_PARTIAL);
 	    }
 	}
     }
 
     /**
      * This function processes a control element from a sink stream.
      *
      * @param ctrlFlag the id of the control message received
      * @param streamId The id of the sink stream from which the control
      *                 message was read
      *
      * @exception java.lang.InterruptedException If the thread is interrupted
      *            during execution
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void processCtrlMsgFromSink(int ctrlFlag, int streamId)
 	throws java.lang.InterruptedException, ShutdownException {
 	// downstream control message is GET_PARTIAL
 	// We should not get SYNCH_PARTIAL, END_PARTIAL, EOS or NULLFLAG 
 	// here (SHUTDOWN is handled with exceptions)
 
 	switch (ctrlFlag) {
 	case CtrlFlags.GET_PARTIAL:
 	    processGetPartialFromSink(streamId);
 	    break;
 
 	default:
 	    throw new PEException("KT unexpected control message from sink " + CtrlFlags.name[ctrlFlag]);
 	}
     }
 
     /**
      * This function processes a GetPartialResult control element received
      * from a sink stream. If the control element is not a duplicate
      * previously received from some other sink stream, then it is
      * sent to all open source streams.
      *
      * @param streamId The id of the sink stream from which the getpartial
      *                 element was read
      *
      * @exception java.lang.InterruptedException If the thread is interrupted
      *            during execution
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void processGetPartialFromSink(int streamId)
 	throws java.lang.InterruptedException, ShutdownException {
 
 	if (sinkStreamsPartialResultCount[streamId] == 0) {
 	    // This is not a duplicate control element - so update
 	    // counts on all other sink streams
 	    for(int i=0; i < numSinkStreams; i++) {
 		if (i != streamId) {
 		    sinkStreamsPartialResultCount[i]++;
 		}
 	    }
 	    // Send the control element to all the open source streams
 	    if(existsUnClosedSourceStream())
 		sendCtrlMsgToSources(CtrlFlags.GET_PARTIAL);
 	    // else ignore GET_PARTIAL, final results are coming soon KT
 	} else {
 	    // This is a duplicate control element
 	    sinkStreamsPartialResultCount[streamId]--;
 	}
     }
 
 
     //////////////////////////////////////////////////////////////////
     // Methods visible to derived classes of PhysicalOperator (also //
     // used by member functions of PhysicalOperator) used to        //
     // manipulate input and output streams                          //
     //////////////////////////////////////////////////////////////////
 
     /**
      * Puts a tuple into a specified sink stream. To be called
      * by the operator implementations themselves. (sort of
      * replaced putToDestinationStreams)
      *
      * @param tuple The tuple to be put in the stream
      * @param streamId the id of the stream where the tuple is to go
      *
      * @exception java.lang.InterruptedException Thread is interrupted during
      *            execution
      * @exception ShutdownException query shutdown by user or execution error
      */
     public final void putTuple(StreamTupleElement tuple, int streamId)
 	throws java.lang.InterruptedException, ShutdownException {
 
 	// If the required stream is already closed, throw an error
 	// KT - not sure what to do here, the previous code in
 	// putToSinkStreams ignored this error
 	if (sinkStreams[streamId].isClosed()) {
 	    throw new PEException("KT putting tuple to closed stream - can I ignore this? previous code ignored it");
 	}
 
 	boolean sent = false;
 	
 	// Loop until the required element is sent 
 	while (!sent) {
 	    // Try to send to the required sink stream
 	    int ctrlFlag = sinkStreams[streamId].putTuple(tuple);
 
 	    // Check if it was successfull
 	    if (ctrlFlag == CtrlFlags.NULLFLAG) {
 		sent = true;
 	    } else {
 		// process the control message
 		processCtrlMsgFromSink(ctrlFlag, streamId);
 	    }
 	}
     }
 
     /**
      * This function puts a control element to a source stream
      *
      * @param ctrlFlag The id of the control message to be sent
      * @param streamId The source stream to which the control element
      *                 is to be put
      *
      * @return True if the operator is to continue and false otherwise
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void sendCtrlMsgToSource (int ctrlFlag, int streamId)
 	throws ShutdownException{
 	int retCtrlFlag = sourceStreams[streamId].putCtrlMsg(ctrlFlag);
 
 	// only control flag putCtrlMsg may return is EOS
 	if(retCtrlFlag != CtrlFlags.NULLFLAG) {
 	    throw new PEException("Unexpected ctrl flag in sendCtrlMsgToSource " + CtrlFlags.name[ctrlFlag]);
 	}
 	return;
     }
 
 
     /**
      * This function puts a control element to all source streams
      *
      * @param ctrlFlag The id of the control message to be sent
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void sendCtrlMsgToSources(int ctrlFlag)
 	throws ShutdownException {
 	// Loop over all source streams and put the control
 	// element in all open ones
 	for (int i = 0; i < numSourceStreams; i++) {
 	    if (!sourceStreams[i].isClosed()) {
 		sendCtrlMsgToSource(ctrlFlag, i);
 	    }
 	}
     }
 
 
     /**
      * This function puts a control element to a sink stream
      *
      * @param ctrlFlag The ide of the ctrl message to be sent
      * @param streamId The sink stream to which the control message
      *                 is to be put
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void sendCtrlMsgToSink(int ctrlFlag, int streamId)
 	throws java.lang.InterruptedException, ShutdownException {
 	int newCtrlFlag;
 	do {
 	     newCtrlFlag = sinkStreams[streamId].putCtrlMsg(ctrlFlag);
 	     if (newCtrlFlag != CtrlFlags.NULLFLAG) {
 		 processCtrlMsgFromSink(newCtrlFlag, streamId);
 	    }
 	}
 	while (newCtrlFlag != CtrlFlags.NULLFLAG); // *NEVER* give up!
     }
 
 
     /**
      * This function puts a control element to all sink streams that
      * have not been previously closed
      *
      * @param ctrlFlag The id of the control message to be sent
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
     private void sendCtrlMsgToSinks(int ctrlFlag)
 	throws java.lang.InterruptedException, ShutdownException {
 
 	// Loop over all sink streams and put the control
 	// element in all open ones
 	for (int sinkId = 0; sinkId < numSinkStreams; sinkId++) {
 	    if (!sinkStreams[sinkId].isClosed()) {
 		sendCtrlMsgToSink(ctrlFlag, sinkId);
 	    }
 	}
     }
 
     ////////////////////////////////////////////////////////////////////
     // The following functions provide the hooks to write actual      //
     // operator classes. This is done by deriving from this class and //
     // over-riding the following operators.                           //
     ////////////////////////////////////////////////////////////////////
 
     /**
      * This function initializes the data structures for an operator
      *
      */
     private void initialize() {
 	if(isHeadOperator)
 	    // Set this thread in the query info object
 	    queryInfo.setHeadOperatorThread(Thread.currentThread());
 	opInitialize();
 	return;
     }
 
     /**
      * if an operator needs to do initialization, it should override
      * this function
      */
     protected void opInitialize() {
 	return;
     }
 
     /**
      * does operator keep state??
      */
     // all operators must implement this
     public abstract boolean isStateful();
 
     /**
      * This function processes a tuple element read from a source stream
      * when the operator is in a non-blocking state.
      *
      * @param tupleElement The tuple element read from a source stream
      * @param streamId The source stream from which the tuple was read
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
     // NONBLOCKING ops should implement this
     protected void nonblockingProcessSourceTupleElement (
 					StreamTupleElement tuple,
 					int streamId) 
 	throws ShutdownException, InterruptedException, UserErrorException {
 	throw new PEException("KT should not get here - this function shouldn't have been called or subclass should have overwritten it");
     }
 
 
     /**
      * This function processes a tuple element read from a source stream
      * when the operator is in a blocking state.
      *
      * @param tupleElement The tuple element read from a source stream
      * @param streamId The source stream from which the tuple was read
      *
      * @exception UserErrorException User made a mistake - bad url, bad
      *                                file, parse error in user-provided
      *                                doc, etc.
      * @exception ShutdownException Shutdown due to execution error or 
      *            client request
      */
     // BLOCKING ops should implement this
     protected void blockingProcessSourceTupleElement (
 						 StreamTupleElement tuple,
 						 int streamId) 
 	throws UserErrorException, ShutdownException {
 	throw new PEException("KT should not get here - this function shouldn't have been called or subclass should have overwritten it");
     }
 
 
     /**
      * Requests that a (blocking) operator flush the current version
      * of its results to the sink. This flush request may be do
      * to a GET_PARTIAL request or due to end of stream.
      *
      * @param partial true if this is partial result output, false if
      * this is the final result output
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
     // BLOCKING OPs should implement this
     protected void flushCurrentResults(boolean partial) 
 	throws ShutdownException, InterruptedException {
 	throw new PEException("KT should not get here - this function shouldn't have been called or subclass should have overwritten it");
     }
 
     /**
      * This function removes the effects of the partial results in a given
      * source stream.
      *
      * @param streamId The id of the source streams the partial result of
      *                 which are to be removed.
      */
     // all operators that keep state should implement this function
     protected void removeEffectsOfPartialResult (int streamId) {
 	if(isStateful()) {
 	    throw new PEException("KT should not get here - this function shouldn't have been called or subclass should have overwritten it");
 	} else {
 	    return; // nothing to be done...
 	}
     }
 
     /**
      * This function handles punctuations for the given operator. The
      * default behavior is to ignore the punctuation and continue
      *
      * @param tuple The current input tuple to examine.
      * @param streamId The id of the source streams the partial result of
      *                 which are to be removed.
      *
      */
 
     protected void processPunctuation(StreamPunctuationElement tuple,
 				      int streamId)
 	throws ShutdownException, InterruptedException {
 	//Default operator behavior is to ignore punctuation
 
 	return;
     }
 
     /**
      * Operators override this method to perform any clean up actions.
      */
     protected void cleanUp() {
     }
 
     private void internalCleanUp() {
         if(isHeadOperator) {
 	    // Remove the query info object from the active query list
 	    // Hack added so client server queries are not removed
 	    // from their respective connections activeQuery list
 	    if(queryInfo.removeFromActiveQueries()) {
 		queryInfo.removeFromActiveQueryList();
 	    }
 	}   
         cleanUp();
     }
     public void setAsHead(QueryInfo queryInfo) {
 	this.queryInfo = queryInfo;
 	if(queryInfo == null) {
 	    throw new PEException("null query info in PhysOp.setAsHead");
 	}
 	isHeadOperator = true;
     }
 
     /**
      * <code>setResultDocument</code> provides an
      * owner Document for XML nodes that are newly
      * created by any operator in the query plan
      *
      * @param doc a <code>Document</code> 
      */
     public void setResultDocument(Document doc) {
 	return; // do nothing by default
 	// KT FIX - code should be once ExecutionScheduler is fixed
         // The default physical operator does not create new XML nodes
 	//throw new PEException("KT shouldn't get here");
     }
 
     protected void shutdownTrigOp() {
 	return;
     }
 
     public static void setDataManager(DataManager d) {
 	DM = d;
     }
 
     /**
      * Print a message plus some information about the operator
      */
     void printMessage(String msg) {
 	System.out.print(this.getClass().getName() + " ");
 	if(isBlocking()) {
 	    System.out.print("(blocking):  ");
 	} else {
 	    System.out.print("(non-blocking):  ");
 	}
 	System.out.print(msg);
 	System.out.println();
     }
 
     class StreamIdList {
 	int streamIds[];
 	int allocSize;
 	int currSize;
 
 	public StreamIdList(int maxSize) {
 	    allocSize = maxSize;
 	    streamIds = new int[allocSize];
 	    currSize = 0;
 	}
 	
 	public void add(int val) {
 	    streamIds[currSize] = val;
 	    currSize++;
 	}
 
 	public int size() {
 	    return currSize;
 	}
 
 	public int get(int idx) {
 	    if(idx >= currSize)
		throw new PEException("KT in ReadSourceStreams bad get. Operator: " + name + " idx is " + idx + " currSize is " + currSize);	   
	    return streamIds[idx];
 	}
 
 	public void remove(int streamId) {
 	    // find the index of thi stream, the remove the stream
 	    // from the list
 	    int idx = indexOf(streamId);
 	    for(int i = idx; i<currSize-1; i++)
 		streamIds[i] = streamIds[i+1];
 	    currSize--;
 	}
 
 	private int indexOf(int streamId) {
 	    for(int i = 0; i<currSize; i++) {
 		if(streamIds[i] == streamId)
 		    return i;
 	    }
 	    return -1;
 	}
 
 	public boolean isEmpty() {
 	    return currSize == 0;
 	}
     } 
 }
