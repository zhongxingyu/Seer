 /*
  * Copyright (c) 2002-2010 "Neo Technology,"
  *     Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  * 
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.kernel.impl.transaction.xaframework;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.ReadableByteChannel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.transaction.xa.XAException;
 import javax.transaction.xa.Xid;
 
 import org.neo4j.helpers.Pair;
 import org.neo4j.kernel.Config;
 import org.neo4j.kernel.impl.cache.LruCache;
 import org.neo4j.kernel.impl.util.ArrayMap;
 import org.neo4j.kernel.impl.util.FileUtils;
 import org.neo4j.kernel.impl.util.StringLogger;
 
 /**
  * <CODE>XaLogicalLog</CODE> is a transaction and logical log combined. In
  * this log information about the transaction (such as started, prepared and
  * committed) will be written. All commands participating in the transaction
  * will also be written to the log.
  * <p>
  * Normally you don't have to do anything with this log except open it after it
  * has been instanciated (see {@link XaContainer}). The only method that may be
  * of use when implementing a XA compatible resource is the
  * {@link #getCurrentTxIdentifier}. Leave everything else be unless you know
  * what you're doing.
  * <p>
  * When the log is opened it will be scaned for uncompleted transactions and
  * those transactions will be re-created. When scan of log is complete all
  * transactions that hasn't entered prepared state will be marked as done
  * (implies rolledback) and dropped. All transactions that have been prepared
  * will be held in memory until the transaction manager tells them to commit.
  * Transaction that already started commit but didn't get flagged as done will
  * be re-committed.
  */
 public class XaLogicalLog
 {
     private Logger log;
     
     private static final char CLEAN = 'C';
     private static final char LOG1 = '1';
     private static final char LOG2 = '2';
 
     private FileChannel fileChannel = null;
     private final ByteBuffer buffer;
     private LogBuffer writeBuffer = null;
     private long previousLogLastCommittedTx = -1;
     private long logVersion = 0;
     private ArrayMap<Integer,LogEntry.Start> xidIdentMap = 
         new ArrayMap<Integer,LogEntry.Start>( 4, false, true );
     private Map<Integer,XaTransaction> recoveredTxMap = 
         new HashMap<Integer,XaTransaction>();
     private int nextIdentifier = 1;
     private boolean scanIsComplete = false;
 
     private String fileName = null;
     private final XaResourceManager xaRm;
     private final XaCommandFactory cf;
     private final XaTransactionFactory xaTf;
     private char currentLog = CLEAN;
     private boolean keepLogs = false;
     private boolean autoRotate = true;
     private long rotateAtSize = 10*1024*1024; // 10MB
     private boolean backupSlave = false;
     private boolean slave = false;
     private boolean useMemoryMapped = true;
     private final String storeDir;
 
     private final StringLogger msgLog;
     
     private final LruCache<Long, Pair<Long, Long>> txStartPositionCache =
         new LruCache<Long, Pair<Long,Long>>( "Tx start position cache", 1000, null );
     
     XaLogicalLog( String fileName, XaResourceManager xaRm, XaCommandFactory cf,
         XaTransactionFactory xaTf, Map<Object,Object> config )
     {
         this.fileName = fileName;
         this.xaRm = xaRm;
         this.cf = cf;
         this.xaTf = xaTf;
         this.useMemoryMapped = getMemoryMapped( config );
         log = Logger.getLogger( this.getClass().getName() + "/" + fileName );
         buffer = ByteBuffer.allocateDirect( 9 + Xid.MAXGTRIDSIZE
             + Xid.MAXBQUALSIZE * 10 );
         storeDir = (String) config.get( "store_dir" );
         msgLog = StringLogger.getLogger( new File( storeDir, "messages.log" ).getPath() );
     }
     
     private boolean getMemoryMapped( Map<Object,Object> config )
     {
         String configValue = config != null ?
                 (String) config.get( Config.USE_MEMORY_MAPPED_BUFFERS ) : null;
         return configValue != null ? Boolean.parseBoolean( configValue ) : true;
     }
     
     synchronized void open() throws IOException
     {
         String activeFileName = fileName + ".active";
         if ( !new File( activeFileName ).exists() )
         {
             if ( new File( fileName ).exists() )
             {
                 // old < b8 xaframework with no log rotation and we need to 
                 // do recovery on it
                 open( fileName );
             }
             else
             {
                 open( getLog1FileName() );
                 setActiveLog( LOG1 );
             }
         }
         else
         {
             FileChannel fc = new RandomAccessFile( activeFileName , 
                 "rw" ).getChannel();
             byte bytes[] = new byte[256];
             ByteBuffer buf = ByteBuffer.wrap( bytes );
             int read = fc.read( buf );
             fc.close();
             if ( read != 4 )
             {
                 throw new IllegalStateException( "Read " + read + 
                     " bytes from " + activeFileName + " but expected 4" ); 
             }
             buf.flip();
             char c = buf.asCharBuffer().get();
             File copy = new File( fileName + ".copy" );
             safeDeleteFile( copy );
             if ( c == CLEAN )
             {
                 // clean
                 String newLog = getLog1FileName();
                 File file = new File( newLog );
                 if ( file.exists() )
                 {
                     fixCleanKill( newLog );
                 }
                 file = new File( getLog2FileName() );
                 if ( file.exists() )
                 {
                     fixCleanKill( file.getPath() );
                 }
                 open( newLog );
                 setActiveLog( LOG1 );
             }
             else if ( c == LOG1 )
             {
                 String newLog = getLog1FileName();
                 if ( !new File( newLog ).exists() )
                 {
                     throw new IllegalStateException( 
                         "Active marked as 1 but no " + newLog + " exist" );
                 }
                 currentLog = LOG1;
                 File otherLog = new File( getLog2FileName() );
                 if ( otherLog.exists() )
                 {
                     if ( !otherLog.delete() )
                     {
                         log.warning( "Unable to delete " + copy.getName() );
                     }
                 }
                 open( newLog );
             }
             else if ( c == LOG2 )
             {
                 String newLog = getLog2FileName();
                 if ( !new File( newLog ).exists() )
                 {
                     throw new IllegalStateException( 
                         "Active marked as 2 but no " + newLog + " exist" );
                 }
                 File otherLog = new File( getLog1FileName() );
                 if ( otherLog.exists() )
                 {
                     if ( !otherLog.delete() )
                     {
                         log.warning( "Unable to delete " + copy.getName() );
                     }
                 }
                 currentLog = LOG2;
                 open( newLog );
             }
             else
             {
                 throw new IllegalStateException( "Unknown active log: " + c );
             }
         }
         
         instantiateCorrectWriteBuffer();
     }
 
     private void instantiateCorrectWriteBuffer() throws IOException
     {
         if ( !useMemoryMapped )
         {
             writeBuffer = new DirectMappedLogBuffer( fileChannel );
         }
         else
         {
             writeBuffer = new MemoryMappedLogBuffer( fileChannel );
         }
     }
 
     private void safeDeleteFile( File file )
     {
         if ( file.exists() )
         {
             if ( !file.delete() )
             {
                 log.warning( "Unable to delete " + file.getName() );
             }
         }
     }
     
     private void fixCleanKill( String fileName ) throws IOException
     {
         File file = new File( fileName );
         if ( !keepLogs )
         {
             if ( !file.delete() )
             {
                 throw new IllegalStateException( 
                     "Active marked as clean and unable to delete log " + 
                     fileName );
             }
         }
         else
         {
             renameCurrentLogFileAndIncrementVersion( fileName, file.length() );
         }
     }
     
     private void open( String fileToOpen ) throws IOException
     {
         fileChannel = new RandomAccessFile( fileToOpen, "rw" ).getChannel();
         if ( fileChannel.size() != 0 )
         {
             doInternalRecovery( fileToOpen );
         }
         else
         {
             logVersion = xaTf.getCurrentVersion();
             buffer.clear();
             buffer.putLong( logVersion );
             long lastTxId = xaTf.getLastCommittedTx();
             buffer.putLong( lastTxId );
             previousLogLastCommittedTx = lastTxId; 
             buffer.flip();
             fileChannel.write( buffer );
             scanIsComplete = true;
             msgLog.logMessage( "Opened [" + fileToOpen + "] clean empty log, version=" + logVersion );
         }
     }
 
     public boolean scanIsComplete()
     {
         return scanIsComplete;
     }
 
     private int getNextIdentifier()
     {
         nextIdentifier++;
         if ( nextIdentifier < 0 )
         {
             nextIdentifier = 1;
         }
         return nextIdentifier;
     }
 
     // returns identifier for transaction
     // [TX_START][xid[gid.length,bid.lengh,gid,bid]][identifier][format id]
     public synchronized int start( Xid xid ) throws XAException
     {
         if ( backupSlave )
         {
             throw new XAException( "Resource is configured as backup slave, " + 
                 "no new transactions can be started for " + fileName + "." + 
                 currentLog );
         }
         int xidIdent = getNextIdentifier();
         try
         {
             byte globalId[] = xid.getGlobalTransactionId();
             byte branchId[] = xid.getBranchQualifier();
             int formatId = xid.getFormatId();
             long position = writeBuffer.getFileChannelPosition();
             writeBuffer.put( LogEntry.TX_START ).put( (byte) globalId.length ).put(
                 (byte) branchId.length ).put( globalId ).put( branchId )
                 .putInt( xidIdent ).putInt( formatId );
             xidIdentMap.put( xidIdent, 
                     new LogEntry.Start( xid, xidIdent, position ) );
         }
         catch ( IOException e )
         {
             throw new XAException( "Logical log couldn't start transaction: "
                 + e );
         }
         return xidIdent;
     }
 
     // [TX_PREPARE][identifier]
     public synchronized void prepare( int identifier ) throws XAException
     {
         LogEntry.Start startEntry = xidIdentMap.get( identifier );
         assert startEntry != null;
         try
         {
             writeBuffer.put( LogEntry.TX_PREPARE ).putInt( identifier );
             writeBuffer.force();
         }
         catch ( IOException e )
         {
             throw new XAException( "Logical log unable to mark prepare ["
                 + identifier + "] " + e );
         }
     }
     
     // [TX_1P_COMMIT][identifier]
     public synchronized void commitOnePhase( int identifier, long txId, int masterId )
         throws XAException
     {
         LogEntry.Start startEntry = xidIdentMap.get( identifier );
         assert startEntry != null;
         assert txId != -1;
         try
         {
             writeBuffer.put( LogEntry.TX_1P_COMMIT ).putInt( 
                 identifier ).putLong( txId ).putInt( masterId );
             writeBuffer.force();
             cacheTxStartPosition( txId, startEntry );
         }
         catch ( IOException e )
         {
             throw new XAException( "Logical log unable to mark 1P-commit ["
                 + identifier + "] " + e );
         }
     }
 
     private synchronized void cacheTxStartPosition( long txId, LogEntry.Start startEntry )
     {
         txStartPositionCache.put( txId, new Pair<Long, Long>(
                 logVersion, startEntry.getStartPosition() ) );
     }
 
     // [DONE][identifier]
     public synchronized void done( int identifier ) throws XAException
     {
         if ( backupSlave )
         {
             return;
         }
         assert xidIdentMap.get( identifier ) != null;
         try
         {
             writeBuffer.put( LogEntry.DONE ).putInt( identifier );
             xidIdentMap.remove( identifier );
         }
         catch ( IOException e )
         {
             throw new XAException( "Logical log unable to mark as done ["
                 + identifier + "] " + e );
         }
     }
 
     // [DONE][identifier] called from XaResourceManager during internal recovery
     synchronized void doneInternal( int identifier ) throws IOException
     {
         buffer.clear();
         buffer.put( LogEntry.DONE ).putInt( identifier );
         buffer.flip();
         fileChannel.write( buffer );
         xidIdentMap.remove( identifier );
     }
 
     // [TX_2P_COMMIT][identifier]
     public synchronized void commitTwoPhase( int identifier, long txId, int masterId ) 
         throws XAException
     {
         LogEntry.Start startEntry = xidIdentMap.get( identifier );
         assert startEntry != null;
         assert txId != -1;
         try
         {
             writeBuffer.put( LogEntry.TX_2P_COMMIT ).putInt( 
                 identifier ).putLong( txId ).putInt( masterId );
             writeBuffer.force();
             cacheTxStartPosition( txId, startEntry );
         }
         catch ( IOException e )
         {
             throw new XAException( "Logical log unable to mark 2PC ["
                 + identifier + "] " + e );
         }
     }
     
     // [COMMAND][identifier][COMMAND_DATA]
     public synchronized void writeCommand( XaCommand command, int identifier )
         throws IOException
     {
         checkLogRotation();
         assert xidIdentMap.get( identifier ) != null;
         writeBuffer.put( LogEntry.COMMAND ).putInt( identifier );
         command.writeToFile( writeBuffer ); // fileChannel, buffer );
     }
 
     private void applyEntry( LogEntry entry ) throws IOException
     {
         if ( entry instanceof LogEntry.Start )
         {
             applyStartEntry( (LogEntry.Start) entry );
         }
         else if ( entry instanceof LogEntry.Prepare )
         {
             applyPrepareEntry( (LogEntry.Prepare ) entry );
         }
         else if ( entry instanceof LogEntry.Command )
         {
             applyCommandEntry( (LogEntry.Command ) entry );
         }
         else if ( entry instanceof LogEntry.OnePhaseCommit )
         {
             applyOnePhaseCommitEntry( (LogEntry.OnePhaseCommit ) entry );
         }
         else if ( entry instanceof LogEntry.TwoPhaseCommit )
         {
             applyTwoPhaseCommitEntry( (LogEntry.TwoPhaseCommit ) entry );
         }
         else if ( entry instanceof LogEntry.Done )
         {
             applyDoneEntry( (LogEntry.Done ) entry );
         }
         else
         {
             throw new RuntimeException( "Unrecognized log entry " + entry );
         }
     }
     
     private void applyStartEntry( LogEntry.Start entry) throws IOException
     {
         int identifier = entry.getIdentifier();
         if ( identifier >= nextIdentifier )
         {
             nextIdentifier = (identifier + 1);
         }
         // re-create the transaction
         Xid xid = entry.getXid();
         xidIdentMap.put( identifier, entry );
         XaTransaction xaTx = xaTf.create( identifier );
         xaTx.setRecovered();
         recoveredTxMap.put( identifier, xaTx );
         xaRm.injectStart( xid, xaTx );
         // force to make sure done record is there if 2PC tx and global log
         // marks tx as committed
         fileChannel.force( false ); 
     }
 
 
     private void applyPrepareEntry( LogEntry.Prepare prepareEntry ) throws IOException
     {
         // get the tx identifier
         int identifier = prepareEntry.getIdentifier();
         LogEntry.Start entry = xidIdentMap.get( identifier );
         if ( entry == null )
         {
             throw new IOException( "Unknown xid for identifier " + identifier );
         }
         Xid xid = entry.getXid();
         if ( xaRm.injectPrepare( xid ) )
         {
             // read only we can remove
             xidIdentMap.remove( identifier );
             recoveredTxMap.remove( identifier );
         }
     }
 
     private void applyOnePhaseCommitEntry( LogEntry.OnePhaseCommit commit ) 
         throws IOException
     {
         int identifier = commit.getIdentifier();
         long txId = commit.getTxId();
         LogEntry.Start startEntry = xidIdentMap.get( identifier );
         if ( startEntry == null )
         {
             throw new IOException( "Unknown xid for identifier " + identifier );
         }
         Xid xid = startEntry.getXid();
         try
         {
             XaTransaction xaTx = xaRm.getXaTransaction( xid );
             xaTx.setCommitTxId( txId );
             xaRm.injectOnePhaseCommit( xid );
             cacheTxStartPosition( txId, startEntry );
             msgLog.logMessage( "Injected one phase commit, txId=" + commit.getTxId() );
         }
         catch ( XAException e )
         {
             e.printStackTrace();
             throw new IOException( e.getMessage() );
         }
     }
 
     private void applyDoneEntry( LogEntry.Done done ) throws IOException
     {
         // get the tx identifier
         int identifier = done.getIdentifier();
         LogEntry.Start entry = xidIdentMap.get( identifier );
         if ( entry == null )
         {
             throw new IOException( "Unknown xid for identifier " + identifier );
         }
         Xid xid = entry.getXid();
         xaRm.pruneXid( xid );
         xidIdentMap.remove( identifier );
         recoveredTxMap.remove( identifier );
     }
 
     private void applyTwoPhaseCommitEntry( LogEntry.TwoPhaseCommit commit ) throws IOException
     {
         int identifier = commit.getIdentifier();
         long txId = commit.getTxId();
         LogEntry.Start startEntry = xidIdentMap.get( identifier );
         if ( startEntry == null )
         {
             throw new IOException( "Unknown xid for identifier " + identifier );
         }
         Xid xid = startEntry.getXid();
         if ( xid == null )
         {
             throw new IOException( "Xid null for identifier " + identifier );
         }
         try
         {
             XaTransaction xaTx = xaRm.getXaTransaction( xid );
             xaTx.setCommitTxId( txId );
             xaRm.injectTwoPhaseCommit( xid );
             cacheTxStartPosition( txId, startEntry );
             msgLog.logMessage( "Injected two phase commit, txId=" + commit.getTxId() );
         }
         catch ( XAException e )
         {
             e.printStackTrace();
             throw new IOException( e.getMessage() );
         }
     }
     
     private void applyCommandEntry( LogEntry.Command entry ) throws IOException
     {
         int identifier = entry.getIdentifier();
         XaCommand command = entry.getXaCommand();
         if ( command == null )
         {
             throw new IOException( "Null command for identifier " + identifier );
         }
         command.setRecovered();
         XaTransaction xaTx = recoveredTxMap.get( identifier );
         xaTx.injectCommand( command );
     }
 
     private void checkLogRotation() throws IOException
     {
         if ( autoRotate && 
             writeBuffer.getFileChannelPosition() >= rotateAtSize )
         {
             long currentPos = writeBuffer.getFileChannelPosition();
             long firstStartEntry = getFirstStartEntry( currentPos ); 
             // only rotate if no huge tx is running
             if ( ( currentPos - firstStartEntry ) < rotateAtSize / 2 )
             {
                 rotate();
             }
         }
     }
 
     private void renameCurrentLogFileAndIncrementVersion( String logFileName, 
         long endPosition ) throws IOException
     {
 //        System.out.println( " ---- Performing clean close on " + logFileName + " -----" );
 //        DumpLogicalLog.main( new String[] { logFileName } );
 //        System.out.println( " ----- end ----" );
         File file = new File( logFileName );
         if ( !file.exists() )
         {
             throw new IOException( "Logical log[" + logFileName + 
                 "] not found" );
         }
         String newName = getFileName( xaTf.getAndSetNewVersion() );
         File newFile = new File( newName );
         boolean renamed = FileUtils.renameFile( file, newFile );
         
         if ( !renamed )
         {
             throw new IOException( "Failed to rename log to: " + newName );
         }
         else
         {
             try
             {
                 FileChannel channel = new RandomAccessFile( newName, 
                     "rw" ).getChannel();
                 FileUtils.truncateFile( channel, endPosition );
             }
             catch ( IOException e )
             {
                 log.log( Level.WARNING, 
                     "Failed to truncate log at correct size", e );
             }
         }
 //        System.out.println( " ---- Created " + newName + " -----" );
 //        DumpLogicalLog.main( new String[] { newName } );
 //        System.out.println( " ----- end ----" );
     }
     
     private void deleteCurrentLogFile( String logFileName ) throws IOException
     {
         File file = new File( logFileName );
         if ( !file.exists() )
         {
             throw new IOException( "Logical log[" + logFileName + 
                 "] not found" );
         }
         boolean deleted = FileUtils.deleteFile( file );
         if ( !deleted )
         {
             log.warning( "Unable to delete clean logical log[" + logFileName +
                 "]" );
         }
     }
     
     private void releaseCurrentLogFile() throws IOException
     {
         if ( writeBuffer != null )
         {
             writeBuffer.force();
             writeBuffer = null;
         }
         fileChannel.close();
         fileChannel = null;
     }
 
     public synchronized void close() throws IOException
     {
         if ( fileChannel == null || !fileChannel.isOpen() )
         {
             log.fine( "Logical log: " + fileName + " already closed" );
             return;
         }
         long endPosition = writeBuffer.getFileChannelPosition();
         if ( xidIdentMap.size() > 0 )
         {
             log.info( "Close invoked with " + xidIdentMap.size() + 
                 " running transaction(s). " );
             writeBuffer.force();
             writeBuffer = null;
             fileChannel.close();
             log.info( "Dirty log: " + fileName + "." + currentLog + 
                 " now closed. Recovery will be started automatically next " + 
                 "time it is opened." );
             return;
         }
         releaseCurrentLogFile();
         char logWas = currentLog;
         if ( currentLog != CLEAN ) // again special case, see above
         {
             setActiveLog( CLEAN );
         }
         if ( !keepLogs || backupSlave )
         {
             if ( logWas == CLEAN )
             {
                 // special case going from old xa version with no log rotation
                 // and we started with a recovery
                 deleteCurrentLogFile( fileName );
             }
             else
             {
                 deleteCurrentLogFile( fileName + "." + logWas );
             }
         }
         else
         {
             renameCurrentLogFileAndIncrementVersion( fileName + "." + 
                 logWas, endPosition );
         }
         msgLog.logMessage( "Closed log " + fileName );
     }
     
     private long[] readLogHeader( ByteBuffer buffer,
             ReadableByteChannel channel, boolean strict ) throws IOException
     {
         buffer.clear();
         buffer.limit( 16 );
         if ( channel.read( buffer ) != 16 )
         {
             if ( strict )
             {
                 throw new IOException( "Unable to read log version and last committed tx" );
             }
             return null;
         }
         buffer.flip();
         return new long[] { buffer.getLong(), buffer.getLong() };
     }
     
     private long[] readAndAssertLogHeader( ByteBuffer buffer,
             ReadableByteChannel channel, long expectedVersion ) throws IOException
     {
         long[] header = readLogHeader( buffer, channel, true );
         if ( header[0] != expectedVersion )
         {
             throw new IOException( "Wrong version in log. Expected " + expectedVersion +
                     ", but got " + header[0] );
         }
         return header;
     }
     
     StringLogger getStringLogger()
     {
         return msgLog;
     }
 
     private void doInternalRecovery( String logFileName ) throws IOException
     {
         log.info( "Non clean shutdown detected on log [" + logFileName + 
             "]. Recovery started ..." );
         msgLog.logMessage( "Non clean shutdown detected on log [" + logFileName + 
             "]. Recovery started ..." );
         // get log creation time
         long[] header = readLogHeader( buffer, fileChannel, false );
         if ( header == null )
         {
             log.info( "Unable to read header information, "
                 + "no records in logical log." );
             msgLog.logMessage( "No log version found for " + logFileName );
             fileChannel.close();
             boolean success = FileUtils.renameFile( new File( logFileName ), 
                 new File( logFileName + "_unknown_timestamp_" + 
                     System.currentTimeMillis() + ".log" ) );
             assert success;
             fileChannel = new RandomAccessFile( logFileName, 
                 "rw" ).getChannel();
             return;
         }
         logVersion = header[0];
         long lastCommittedTx = header[1];
         previousLogLastCommittedTx = lastCommittedTx;
         log.fine( "Logical log version: " + logVersion + " with committed tx[" +
             lastCommittedTx + "]" );
         msgLog.logMessage( "[" + logFileName + "] logVersion=" + logVersion + 
                 " with committed tx=" + lastCommittedTx );
         long logEntriesFound = 0;
         long lastEntryPos = fileChannel.position();
         LogEntry entry;
         while ( (entry = readEntry()) != null )
         {
             applyEntry( entry );
             logEntriesFound++;
             lastEntryPos = fileChannel.position();
         }
         // make sure we overwrite any broken records
         fileChannel.position( lastEntryPos );
 
         msgLog.logMessage( "[" + logFileName + "] entries found=" + logEntriesFound + 
                 " lastEntryPos=" + lastEntryPos  );
         
         // zero out the slow way since windows don't support truncate very well
         buffer.clear();
         while ( buffer.hasRemaining() )
         {
             buffer.put( (byte)0 );
         }
         buffer.flip();
         long endPosition = fileChannel.size();
         do
         {
             long bytesLeft = fileChannel.size() - fileChannel.position();
             if ( bytesLeft < buffer.capacity() )
             {
                 buffer.limit( (int) bytesLeft );
             }
             fileChannel.write( buffer );
             buffer.flip();
         } while ( fileChannel.position() < endPosition );
         fileChannel.position( lastEntryPos );
         scanIsComplete = true;
         log.fine( "Internal recovery completed, scanned " + logEntriesFound
             + " log entries." );
         
         xaRm.checkXids();
         if ( xidIdentMap.size() == 0 )
         {
             log.fine( "Recovery completed." );
         }
         else
         {
             log.fine( "[" + logFileName + "] Found " + xidIdentMap.size()
                 + " prepared 2PC transactions." );
             for ( LogEntry.Start startEntry : xidIdentMap.values() )
             {
                 log.fine( "[" + logFileName + "] 2PC xid[" + 
                     startEntry.getXid() + "]" );
             }
         }
         recoveredTxMap.clear();
     }
 
     // for testing, do not use!
     void reset()
     {
         xidIdentMap.clear();
         recoveredTxMap.clear();
     }
 
     private LogEntry readEntry() throws IOException
     {
         long position = fileChannel.position();
         LogEntry entry = LogIoUtils.readEntry( buffer, fileChannel, cf );
         if ( entry instanceof LogEntry.Start )
         {
             ((LogEntry.Start) entry).setStartPosition( position );
         }
         return entry;
     }
 
     private ArrayMap<Thread,Integer> txIdentMap = 
         new ArrayMap<Thread,Integer>( 5, true, true );
 
     void registerTxIdentifier( int identifier )
     {
         txIdentMap.put( Thread.currentThread(), identifier );
     }
 
     void unregisterTxIdentifier()
     {
         txIdentMap.remove( Thread.currentThread() );
     }
 
     /**
      * If the current thread is committing a transaction the identifier of that
      * {@link XaTransaction} can be obtained invoking this method.
      * 
      * @return the identifier of the transaction committing or <CODE>-1</CODE>
      *         if current thread isn't committing any transaction
      */
     public int getCurrentTxIdentifier()
     {
         Integer intValue = txIdentMap.get( Thread.currentThread() );
         if ( intValue != null )
         {
             return intValue;
         }
         return -1;
     }
 
     public ReadableByteChannel getLogicalLog( long version ) throws IOException
     {
         return getLogicalLog( version, 0 );
     }
     
     public ReadableByteChannel getLogicalLog( long version, long position ) throws IOException
     {
         String name = getFileName( version );
         if ( !new File( name ).exists() )
         {
             throw new IOException( "No such log version:" + version );
         }
         FileChannel channel = new RandomAccessFile( name, "r" ).getChannel();
         channel.position( position );
         return channel;
     }
     
     private List<LogEntry> extractPreparedTransactionFromLog( int identifier, 
             FileChannel log ) throws IOException
     {
         LogEntry.Start startEntry = xidIdentMap.get( identifier );
         log.position( startEntry.getStartPosition() );
         List<LogEntry> logEntryList = new ArrayList<LogEntry>();
         LogEntry entry;
         while ( (entry = LogIoUtils.readEntry( buffer, log, cf )) != null )
         {
             // TODO For now just skip Prepare entries
             if ( entry.getIdentifier() != identifier )
             {
                 continue;
             }
             if ( entry instanceof LogEntry.Prepare )
             {
                 break;
             }
             if ( entry instanceof LogEntry.Start || entry instanceof LogEntry.Command )
             {
                 logEntryList.add( entry );
             }
             else
             {
                 throw new RuntimeException( "Expected start or command entry but found: " + entry );
             }
         }
         if ( logEntryList.isEmpty() )
         {
             throw new IOException( "Transaction for internal identifier[" + identifier + 
                     "] not found in current log" );
         }
         return logEntryList;
     }
     
     private List<LogEntry> extractTransactionFromLog( long txId, 
             long expectedVersion, ReadableByteChannel log ) throws IOException
     {
         // Assertions in read?
         List<LogEntry> logEntryList = null;
         Map<Integer,List<LogEntry>> transactions = 
             new HashMap<Integer,List<LogEntry>>();
         LogEntry entry;
         while ( (entry = LogIoUtils.readEntry( buffer, log, cf )) != null && 
                 logEntryList == null )
         {
             if ( entry instanceof LogEntry.Start )
             {
                 List<LogEntry> list = new LinkedList<LogEntry>();
                 list.add( entry );
                 transactions.put( entry.getIdentifier(), list );
             }
             else if ( entry instanceof LogEntry.Commit )
             {
                 if ( ((LogEntry.Commit) entry).getTxId() == txId )
                 {
                     logEntryList = transactions.get( entry.getIdentifier() );
                     logEntryList.add( entry );
                 }
                 else
                 {
                     transactions.remove( entry.getIdentifier() );
                 }
             }
             else if ( entry instanceof LogEntry.Command || entry instanceof LogEntry.Prepare )
             {
                 transactions.get( entry.getIdentifier() ).add( entry );
             }
             else if ( entry instanceof LogEntry.Done )
             {
                 transactions.remove( entry.getIdentifier() );
             }
             else
             {
                 throw new RuntimeException( "Unknown entry: " + entry );
             }
         }
         if ( logEntryList == null )
         {
             msgLog.logMessage( "txId=" + txId + " not found in log=" + expectedVersion  );
             throw new IOException( "Transaction[" + txId + 
                     "] not found in log (" + expectedVersion/* + ", " + prevTxId*/ + ")" );
         }
         logEntryList.add( new LogEntry.Done( logEntryList.get( 0 ).getIdentifier() ) );
         return logEntryList;
     }
 
     private void assertLogCanContainTx( long txId, long prevTxId ) throws IOException
     {
         if ( prevTxId >= txId )
         {
             throw new IOException( "Log says " + txId + 
                     " can not exist in this log (prev tx id=" + prevTxId + ")" );
         }
     }
     
     public synchronized ReadableByteChannel getPreparedTransaction( int identifier )
             throws IOException
     {
         File writeOutDir = new File( storeDir, "tmp-write-outs" );
         if ( !writeOutDir.exists() )
         {
             writeOutDir.mkdir();
         }
         File txFile = File.createTempFile( "temp-write-out-", "-" + identifier , writeOutDir );
         FileChannel log = (FileChannel) getLogicalLogOrMyself( logVersion, 0 );
         List<LogEntry> logEntryList = extractPreparedTransactionFromLog( identifier, log );
         log.close();
         
         writeOutLogEntryList( logEntryList, txFile, false );
         return new RandomAccessFile( txFile, "r" ).getChannel();
     }
 
     private void writeOutLogEntryList( List<LogEntry> logEntryList, File txFile, boolean tempWriteOutFirst ) throws IOException
     {
         int identifier = logEntryList.get( 0 ).getIdentifier();
         File tempFile = tempWriteOutFirst ? File.createTempFile( "extracted-tx-", "-" + identifier ) : txFile;
         msgLog.logMessage( "write out log entry list to file:" + tempFile );
         FileChannel txLog = new RandomAccessFile( tempFile, "rw" ).getChannel();
         LogBuffer buf = new DirectMappedLogBuffer( txLog );
         for ( LogEntry entry : logEntryList )
         {
             LogIoUtils.writeLogEntry( entry, buf );
         }
         buf.force();
         txLog.close();
         
         if ( tempWriteOutFirst )
         {
             if ( !tempFile.renameTo( txFile ) )
             {
                 throw new IOException( "Failed to rename " + tempFile + " to " + txFile );
             }
         }
     }
     
     private List<LogEntry> extractLogEntryList( long txId ) throws IOException
     {
         String name = fileName + ".tx_" + txId;
         File txFile = new File( name );
         List<LogEntry> logEntryList = null;
         if ( txFile.exists() )
         {
             // It was already written out to file?
             FileChannel channel = new RandomAccessFile( name, "r" ).getChannel();
             logEntryList = extractTransactionFromLog( txId, -1, channel );
             channel.close();
             return logEntryList;
         }
         Pair<Long, Long> cachedInfo = this.txStartPositionCache.get( txId );
         if ( cachedInfo != null )
         {
             // We have log version and start position cached
             long version = cachedInfo.first();
             ReadableByteChannel log = getLogicalLogOrMyself( version, cachedInfo.other() );
             logEntryList = extractTransactionFromLog( txId, version, log );
             log.close();
         }
         else
         {
             // We have to look backwards in log files
             long version = findLogContainingTxId( txId )[0];
             if ( version == -1 )
             {
                 throw new RuntimeException( "txId:" + txId + " not found in any logical log "
                                             + "(starting at " + logVersion
                                             + " and searching backwards" );
             }
 
             ReadableByteChannel log = getLogicalLogOrMyself( version, 0 );
             long[] header = readAndAssertLogHeader( buffer, log, version );
             long prevTxId = header[1];
             assertLogCanContainTx( txId, prevTxId );
             logEntryList = extractTransactionFromLog( txId, version, log );
             log.close();
         }
         return logEntryList;
     }
 
     public synchronized ReadableByteChannel getCommittedTransaction( long txId )
         throws IOException
     {
         String name = fileName + ".tx_" + txId;
        File txFile = new File( name );
         List<LogEntry> logEntryList = extractLogEntryList( txId );
         writeOutLogEntryList( logEntryList, txFile, true );
         ReadableByteChannel result = new RandomAccessFile( txFile, "r" ).getChannel();
         return result;
     }
     
     public static final int MASTER_ID_REPRESENTING_NO_MASTER = -1;
 
     public synchronized int getMasterIdForCommittedTransaction( long txId ) throws IOException
     {
         if ( txId == 1 )
         {
             return MASTER_ID_REPRESENTING_NO_MASTER;
         }
         
         List<LogEntry> logEntryList = extractLogEntryList( txId );
         for ( LogEntry entry : logEntryList )
         {
             if ( entry instanceof LogEntry.Commit )
             {
                 return ((LogEntry.Commit) entry).getMasterId();
             }
         }
         throw new RuntimeException( "Unable to find commit entry in for txId[" + 
                 txId + "]" );// in log[" + version + "]" );
     }
     
     private ReadableByteChannel getLogicalLogOrMyself( long version, long position )
             throws IOException
     {
         if ( version < logVersion )
         {
             return getLogicalLog( version, position );
         }
         else if ( version == logVersion )
         {
             String currentLogName = getCurrentLogFileName();
             FileChannel channel = new RandomAccessFile( currentLogName, "r" ).getChannel();
             channel.position( position );
             return channel;
         }
         else
         {
             throw new RuntimeException( "Version[" + version + 
                     "] is higher then current log version[" + logVersion + "]" );
         }
     }
     
     private String getCurrentLogFileName()
     {
         return currentLog == LOG1 ? getLog1FileName() : getLog2FileName();
     }
 
     private long[] findLogContainingTxId( long txId ) throws IOException
     {
         long version = logVersion;
         long committedTx = previousLogLastCommittedTx;
         while ( version >= 0 )
         {
             ReadableByteChannel log = getLogicalLogOrMyself( version, 0 );
             ByteBuffer buf = ByteBuffer.allocate( 16 );
             long[] header = readAndAssertLogHeader( buf, log, version );
             committedTx = header[1];
             log.close();
             if ( committedTx < txId )
             {
                 break;
             }
             version--;
         }
         return new long[] { version, committedTx };
     }
     
     public long getLogicalLogLength( long version )
     {
         File file = new File( getFileName( version ) );
         return file.exists() ? file.length() : -1;
     }
     
     public boolean hasLogicalLog( long version )
     {
         return new File( getFileName( version ) ).exists();
     }
     
     public boolean deleteLogicalLog( long version )
     {
         File file = new File(getFileName( version ) );
         return file.exists() ? FileUtils.deleteFile( file ) : false;
     }
     
     public void makeBackupSlave()
     {
         if ( xidIdentMap.size() > 0 )
         {
             throw new IllegalStateException( "There are active transactions" );
         }
         backupSlave = true;
     }
     
     private class LogApplier
     {
         private final ReadableByteChannel byteChannel;
         
         private LogEntry.Start startEntry;
         
         LogApplier( ReadableByteChannel byteChannel )
         {
             this.byteChannel = byteChannel;
         }
         
         boolean readAndApplyEntry() throws IOException
         {
             LogEntry entry = LogIoUtils.readEntry( buffer, byteChannel, cf );
             if ( entry != null )
             {
                 applyEntry( entry );
             }
             return entry != null;
         }
 
         boolean readAndWriteAndApplyEntry( int newXidIdentifier ) throws IOException
         {
             LogEntry entry = LogIoUtils.readEntry( buffer, byteChannel, cf );
             if ( entry != null )
             {
                 entry.setIdentifier( newXidIdentifier );
                 if ( entry instanceof LogEntry.Commit )
                 {
                     // hack to get done record written after commit record
                     msgLog.logMessage( "Applying external tx: " + ((LogEntry.Commit) entry).getTxId() );
                 }
                 else if ( entry instanceof LogEntry.Start )
                 {
                     startEntry = (LogEntry.Start) entry;
                 }
                 LogIoUtils.writeLogEntry( entry, writeBuffer );
                 applyEntry( entry );
                 return true;
             }
             return false;
         }
         
     }
     
     public synchronized void applyLog( ReadableByteChannel byteChannel ) 
         throws IOException
     {
         if ( !backupSlave )
         {
             throw new IllegalStateException( "This is not a backup slave" );
         }
         if ( xidIdentMap.size() > 0 )
         {
             throw new IllegalStateException( "There are active transactions" );
         }
         long[] header = readLogHeader( buffer, byteChannel, true );
         logVersion = header[0];
         long previousCommittedTx = header[1];
         if ( logVersion != xaTf.getCurrentVersion() )
         {
             throw new IllegalStateException( "Tried to apply version " + 
                 logVersion + " but expected version " + 
                 xaTf.getCurrentVersion() );
         }
         log.fine( "Logical log version: " + logVersion + 
             "(previous committed tx=" + previousCommittedTx + ")" );
         msgLog.logMessage( "Applying log version=" + logVersion + 
             " (previous committed tx=" + previousCommittedTx + ")" );
         long logEntriesFound = 0;
         LogApplier logApplier = new LogApplier( byteChannel );
         scanIsComplete = false;
         while ( logApplier.readAndApplyEntry() )
         {
             logEntriesFound++;
         }
         scanIsComplete = true;
         byteChannel.close();
         xaTf.flushAll();
         xaTf.getAndSetNewVersion();
         xaRm.reset();
         msgLog.logMessage( "Apply of log version=" + logVersion + " successfull, " + 
                 logEntriesFound + " nr of log entries found." );
         log.info( "Log[" + fileName + "] version " + logVersion + 
                 " applied successfully." );
     }
 
     public synchronized void applyTransactionWithoutTxId( ReadableByteChannel byteChannel, 
             long nextTxId, int masterId ) throws IOException
     {
         if ( nextTxId != (xaTf.getLastCommittedTx() + 1) )
         {
             throw new IllegalStateException( "Tried to apply tx " + 
                 nextTxId + " but expected transaction " + 
                 (xaTf.getCurrentVersion() + 1) );
         }
         log.fine( "Logical log version: " + logVersion + 
             ", committing tx=" + nextTxId + ")" );
 //        System.out.println( "applyTxWithoutTxId#start @ pos: " + writeBuffer.getFileChannelPosition() );
         long logEntriesFound = 0;
         scanIsComplete = false;
         LogApplier logApplier = new LogApplier( byteChannel );
         int xidIdent = getNextIdentifier();
         long startEntryPosition = writeBuffer.getFileChannelPosition();
         while ( logApplier.readAndWriteAndApplyEntry( xidIdent ) )
         {
             logEntriesFound++;
         }
         byteChannel.close();
         LogEntry.Start startEntry = logApplier.startEntry;
         if ( startEntry == null )
         {
             throw new IOException( "Unable to find start entry" );
         }
         startEntry.setStartPosition( startEntryPosition );
 //        System.out.println( "applyTxWithoutTxId#before 1PC @ pos: " + writeBuffer.getFileChannelPosition() );
         LogEntry.OnePhaseCommit commit = new LogEntry.OnePhaseCommit( 
                 xidIdent, nextTxId, masterId );
         LogIoUtils.writeLogEntry( commit, writeBuffer );
         Xid xid = startEntry.getXid();
         try
         {
             XaTransaction xaTx = xaRm.getXaTransaction( xid );
             xaTx.setCommitTxId( nextTxId );
             xaRm.commit( xid, true );
             LogEntry doneEntry = new LogEntry.Done( startEntry.getIdentifier() );
             LogIoUtils.writeLogEntry( doneEntry, writeBuffer );
             xidIdentMap.remove( startEntry.getIdentifier() );
             recoveredTxMap.remove( startEntry.getIdentifier() );
             cacheTxStartPosition( nextTxId, startEntry );
         }
         catch ( XAException e )
         {
             e.printStackTrace();
             throw new IOException( e.getMessage() );
         }
         
 //        LogEntry.Done done = new LogEntry.Done( entry.getIdentifier() );
 //        LogIoUtils.writeLogEntry( done, writeBuffer );
         // xaTf.setLastCommittedTx( nextTxId ); // done in doCommit
         scanIsComplete = true;
 //        log.info( "Tx[" + nextTxId + "] " + " applied successfully." );
         msgLog.logMessage( "Applied external tx and generated tx id=" + nextTxId );
 //        System.out.println( "applyTxWithoutTxId#end @ pos: " + writeBuffer.getFileChannelPosition() );
     }
     
     public synchronized void applyTransaction( ReadableByteChannel byteChannel )
         throws IOException
     {
 //        System.out.println( "applyFullTx#start @ pos: " + writeBuffer.getFileChannelPosition() );
         long logEntriesFound = 0;
         scanIsComplete = false;
         LogApplier logApplier = new LogApplier( byteChannel );
         int xidIdent = getNextIdentifier();
         while ( logApplier.readAndWriteAndApplyEntry( xidIdent ) )
         {
             logEntriesFound++;
         }
         byteChannel.close();
         scanIsComplete = true;
 //        System.out.println( "applyFullTx#end @ pos: " + writeBuffer.getFileChannelPosition() );
     }
     
     private String getLog1FileName()
     {
         return fileName + ".1";
     }
     
     private String getLog2FileName()
     {
         return fileName + ".2";
     }
     
     public synchronized void rotate() throws IOException
     {
         xaTf.flushAll();
         String newLogFile = getLog2FileName();
         String currentLogFile = getLog1FileName();
         char newActiveLog = LOG2;
         long currentVersion = xaTf.getCurrentVersion();
         String oldCopy = getFileName( currentVersion );
         if ( currentLog == CLEAN || currentLog == LOG2 )
         {
             newActiveLog = LOG1;
             newLogFile = getLog1FileName();
             currentLogFile = getLog2FileName();
         }
         else
         {
             assert currentLog == LOG1;
         }
         assertFileDoesntExist( newLogFile, "New log file" );
         assertFileDoesntExist( oldCopy, "Copy log file" );
 //        System.out.println( " ---- Performing rotate on " + currentLogFile + " -----" );
 //        DumpLogicalLog.main( new String[] { currentLogFile } );
 //        System.out.println( " ----- end ----" );
         msgLog.logMessage( "Rotating [" + currentLogFile + "] @ version=" + 
                 currentVersion + " to " +  newLogFile + "from position " + 
                 writeBuffer.getFileChannelPosition() );
         long endPosition = writeBuffer.getFileChannelPosition();
         writeBuffer.force();
         FileChannel newLog = new RandomAccessFile( 
             newLogFile, "rw" ).getChannel();
         buffer.clear();
         buffer.putLong( currentVersion + 1 );
         long lastTx = xaTf.getLastCommittedTx();
         buffer.putLong( lastTx ).flip();
         previousLogLastCommittedTx = lastTx;
         if ( newLog.write( buffer ) != 16 )
         {
             throw new IOException( "Unable to write log version to new" );
         }
         fileChannel.position( 0 );
         long[] header =  readAndAssertLogHeader( buffer, fileChannel, currentVersion );
         if ( xidIdentMap.size() > 0 )
         {
             fileChannel.position( getFirstStartEntry( endPosition ) );
         }
         LogEntry entry;
         // Set<Integer> startEntriesWritten = new HashSet<Integer>();
         while ((entry = LogIoUtils.readEntry( buffer, fileChannel, cf )) != null )
         {
             if ( xidIdentMap.get( entry.getIdentifier() ) != null )
             {
                 if ( entry instanceof LogEntry.Start )
                 {
                     LogEntry.Start startEntry = (LogEntry.Start) entry;
                     startEntry.setStartPosition( newLog.position() );
                     // overwrite old start entry with new that has updated position
                     xidIdentMap.put( startEntry.getIdentifier(), startEntry );
                     // startEntriesWritten.add( entry.getIdentifier() );
                 }
 //                if ( !startEntriesWritten.contains( entry.getIdentifier() ) )
 //                {
 //                    throw new IOException( "Unable to rotate log since start entry for identifier[" +
 //                            entry.getIdentifier() + "] not written" );
 //                }
                 LogBuffer newLogBuffer = new DirectLogBuffer( newLog, buffer );
                 LogIoUtils.writeLogEntry( entry, newLogBuffer );
             }
         }
         msgLog.logMessage( "Rotate: old log scanned, newLog @ pos=" + 
                 newLog.position() );
         newLog.force( false );
         releaseCurrentLogFile();
         setActiveLog( newActiveLog );
         if ( keepLogs )
         {
             renameCurrentLogFileAndIncrementVersion( currentLogFile, 
                 endPosition );
         }
         else
         {
             deleteCurrentLogFile( currentLogFile );
             xaTf.getAndSetNewVersion();
         }
         if ( xaTf.getCurrentVersion() != ( currentVersion + 1 ) )
         {
             throw new IOException( "version change failed" );
         }
         fileChannel = newLog;
         instantiateCorrectWriteBuffer();
         msgLog.logMessage( "Log rotated, newLog @ pos=" + 
                 writeBuffer.getFileChannelPosition() + " and version " + 
                 (currentVersion + 1) ); 
     }
 
     private void assertFileDoesntExist( String file, String description ) throws IOException
     {
         if ( new File( file ).exists() )
         {
             throw new IOException( description + ": " + file + " already exist" );
         }
     }
     
     private long getFirstStartEntry( long endPosition )
     {
         long firstEntryPosition = endPosition;
         for ( LogEntry.Start entry : xidIdentMap.values() )
         {
             if ( entry.getStartPosition() < firstEntryPosition )
             {
                 assert entry.getStartPosition() > 0; 
                 firstEntryPosition = entry.getStartPosition();
             }
         }
         msgLog.logMessage( "Rotate log first start entry @ pos=" + 
                 firstEntryPosition );
         return firstEntryPosition;
     }
 
     private void setActiveLog( char c ) throws IOException
     {
         if ( c != CLEAN && c != LOG1 && c != LOG2 )
         {
             throw new IllegalArgumentException( "Log must be either clean, " + 
                 "1 or 2" );
         }
         if ( c == currentLog )
         {
             throw new IllegalStateException( "Log should not be equal to " + 
                 "current " + currentLog );
         }
         ByteBuffer bb = ByteBuffer.wrap( new byte[4] );
         bb.asCharBuffer().put( c ).flip();
         FileChannel fc = new RandomAccessFile( fileName + ".active" , 
             "rw" ).getChannel();
         int wrote = fc.write( bb );
         if ( wrote != 4 )
         {
             throw new IllegalStateException( "Expected to write 4 -> " + wrote );
         }
         fc.force( false );
         fc.close();
         currentLog = c;
     }
     
     public void setKeepLogs( boolean keep )
     {
         this.keepLogs = keep;
     }
     
     public boolean isLogsKept()
     {
         return this.keepLogs;
     }
     
     public void setAutoRotateLogs( boolean autoRotate )
     {
         this.autoRotate = autoRotate;
     }
     
     public boolean isLogsAutoRotated()
     {
         return this.autoRotate;
     }
     
     public void setLogicalLogTargetSize( long size )
     {
         this.rotateAtSize = size;
     }
     
     public long getLogicalLogTargetSize()
     {
         return this.rotateAtSize;
     }
     
     public String getFileName( long version ) 
     {
         return fileName + ".v" + version;
     }
 }
