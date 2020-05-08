 package togos.ccouch3;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import togos.blob.ByteChunk;
 import togos.blob.util.BlobUtil;
 import togos.ccouch3.FlowUploader.Indexer.IndexedObjectSink;
 import togos.ccouch3.FlowUploader.StandardTransferTracker.Counter;
 import togos.ccouch3.hash.BitprintDigest;
 import togos.ccouch3.hash.BitprintHashURNFormatter;
 import togos.ccouch3.hash.HashFormatter;
 import togos.ccouch3.repo.Repository;
 import togos.ccouch3.repo.SHA1FileRepository;
 import togos.ccouch3.slf.RandomAccessFileBlob;
 import togos.ccouch3.slf.SimpleListFile2;
 
 public class FlowUploader
 {
 	static class Actions {
 		public static final int SKIP_THE_FILE = 2;
 		public static final int THROW_AN_EXCEPTION = 1;
 	}
 	
 	static class FileReadError extends IOException {
 		private static final long serialVersionUID = 4457136769951017923L;
 		public final File file;
 		public FileReadError( File file, Exception cause ) {
 			super(cause);
 			this.file = file;
 		}
 	}
 	
 	//// Hash stuff ////
 	
 	interface MessageDigestFactory {
 		public MessageDigest createMessageDigest();
 	}
 	
 	interface StreamURNifier {
 		public String digest( InputStream is ) throws IOException;
 	}
 
 	static class MessageDigestor implements StreamURNifier {
 		final MessageDigestFactory messageDigestFactory;
 		final HashFormatter hform;
 		
 		public MessageDigestor( MessageDigestFactory fac, HashFormatter form ) {
 			this.messageDigestFactory = fac;
 			this.hform = form;
 		}
 		
 		@Override
 		public String digest(InputStream is) throws IOException {
 			MessageDigest d = messageDigestFactory.createMessageDigest();
 			
 			byte[] buffer = new byte[65536];
 			while( true ) {
 				int z = is.read( buffer );
 				if( z <= 0 ) break;
 				d.update( buffer, 0, z );
 					
 			}
 			
 			return hform.format( d.digest() );
 		}
 	}
 	
 	static class BitprintMessageDigestFactory implements MessageDigestFactory {
 		@Override
 		public MessageDigest createMessageDigest() {
 			return new BitprintDigest();
 		}
 	}
 	
 	public static final BitprintMessageDigestFactory BITPRINT_MESSAGE_DIGEST_FACTORY = new BitprintMessageDigestFactory(); 
 	public static final MessageDigestor BITPRINT_STREAM_URNIFIER = new MessageDigestor( BITPRINT_MESSAGE_DIGEST_FACTORY, BitprintHashURNFormatter.INSTANCE );
 	
 	//// Queue stuff ////
 	
 	interface Sink<E> {
 		public void give( E value ) throws Exception;
 	}
 	
 	static class QueueSink<E> implements Sink<E> {
 		protected final BlockingQueue<E> q;
 		public QueueSink( BlockingQueue<E> q ) { this.q = q; }
 		public void give(E value) throws Exception { q.put(value); }
 	}
 	
 	static abstract class QueueRunner implements Runnable {
 		BlockingQueue<Object> inQueue;
 		public QueueRunner( BlockingQueue<Object> inQueue ) {
 			this.inQueue = inQueue;
 		}
 		protected abstract boolean handleMessage( Object m ) throws Exception;
 		protected abstract void cleanUp() throws Exception;
 		public void run() {
 			Object m;
 			try {
 				while( (m = inQueue.take()) != null && handleMessage(m) );
 			} catch( InterruptedException e ) {
 				Thread.currentThread().interrupt();
 			} catch( Exception e ) {
 				throw new RuntimeException(e);
 			} finally {
 				try {
 					cleanUp();
 				} catch( Exception e ) {
 					System.err.print("Error in "+getClass()+" cleanup: ");
 					e.printStackTrace(System.err);
 				}
 			}
 		}
 	}
 	
 	interface TransferTracker {
 		// Units = e.g. files, or whatever you want to track.
 		public void transferred( long byteCount, int unitCount, String tag );
 		public void sendingFile( String filename );
 	}
 	
 	static class StandardTransferTracker implements TransferTracker {
 		class Counter {
 			long byteCount = 0;
 			long unitCount = 0;
 		}
 		
 		protected long totalByteCount = 0, totalUnitCount = 0;
 		protected HashMap<String,Counter> counters = new HashMap<String,Counter>();
 		protected String currentFilename;
 		
 		protected Counter getCounter( String name ) {
 			Counter c = counters.get(name);
 			if( c == null ) counters.put( name, c = new Counter() );
 			return c;
 		}
 		
 		@Override
 		public synchronized void transferred(long byteCount, int unitCount, String tag) {
 			totalByteCount += byteCount;
 			totalUnitCount += unitCount;
 			Counter c = getCounter(tag);
 			c.byteCount += byteCount;
 			c.unitCount += unitCount;
 		}
 		
 		public void sendingFile( String filename ) {  currentFilename = filename;  }
 		
 		public String getCurrentFilename() {  return currentFilename;  }
 		public long getTotalByteCount() {  return totalByteCount;  }
 		public long getTotalUnitCount() {  return totalUnitCount;  }
 	}
 	
 	//// Message types ////
 	
 	/**
 	 * Information about a commit not including the target URN and date
 	 * (which may have yet to be calculated)
 	 */
 	static class CommitConfig {
 		public final String headName;
 		public final String description;
 		public final String authorName;
 		public final String[] tags;
 		
 		public CommitConfig( String headName, String description, String authorName, String[] tags ) {
 			this.headName = headName;
 			this.description = description;
 			this.authorName = authorName;
 			this.tags = tags;
 		}
 		
 		public Commit toCommit( String targetUrn, String[] parentCommitUrns, long timestamp ) {
 			return new Commit( targetUrn, parentCommitUrns, tags, authorName, description, timestamp );
 		}
 	}
 	
 	static class UploadTask {
 		public final String name;
 		public final String path;
 		/** If non-null, will be used to create commits */
 		public final CommitConfig commitConfig;
 		
 		public UploadTask( String name, String path, CommitConfig commitInfo ) {
 			this.name = name;
 			this.path = path;
 			this.commitConfig = commitInfo;
 		}
 		
 		@Override
 		public String toString() { return "UploadTask name="+name+", path="+path; }
 	}
 	
 	/** Marks the end of a stream of messages, usually indicating that the recipient can quit. */
 	static class EndMessage {
 		public static final EndMessage INSTANCE = new EndMessage();
 		private EndMessage() {}
 		@Override
 		public String toString() { return "EndMessage"; }
 	}
 	
 	static class BlobInfo {
 		public final String urn;
 		public final byte[] blob;
 		public final int offset, length;
 		
 		public BlobInfo( String urn, byte[] blob, int offset, int length ) {
 			assert offset >= 0;
 			assert offset <= blob.length;
 			assert offset + length <= blob.length;
 			this.urn = urn;
 			this.blob = blob;
 			this.offset = offset;
 			this.length = length;
 		}
 		
 		public BlobInfo( String urn, ByteChunk c ) {
 			this( urn, c.getBuffer(), c.getOffset(), c.getSize() );
 		}
 		
 		public BlobInfo( String urn, byte[] blob ) {
 			this( urn, blob, 0, blob.length );
 		}
 	}
 	
 	static class FullyStoredMarker {
 		public final String urn;
 		public FullyStoredMarker( String urn ) { this.urn = urn; }
 	}
 	
 	static class LogMessage {
 		public final byte[] message;
 		public LogMessage( byte[] message ) { this.message = message; }
 	}
 	
 	static class IndexResult {
 		public final FileInfo fileInfo;
 		public final boolean anyNewData;
 		
 		public IndexResult( FileInfo fi, boolean anyNewData ) {
 			this.fileInfo = fi;
 			this.anyNewData = anyNewData;
 		}
 	}
 	
 	static class FileMissing {
 		public final String path;
 		public final String urn;
 		
 		public FileMissing( String path, String urn ) {
 			this.path = path;
 			this.urn = urn;
 		}
 		
 		@Override
 		public String toString() {
 			return "FileMissing path="+path+", urn="+urn;
 		}
 	}
 	
 	static class PutHead {
 		public final String name;
 		public final int number;
 		public final String headDataUrn;
 		
 		public PutHead( String name, int minNumber, String headDataUrn ) {
 			this.name = name;
 			this.number = minNumber;
 			this.headDataUrn = headDataUrn;
 		}
 	}
 	
 	////
 	
 	static final ByteChunk YES_MARKER = BlobUtil.byteChunk("Y");
 	
 	protected static void mkParentDirs( File f ) {
 		File p = f.getParentFile();
 		if( p != null && !p.exists() ) p.mkdirs();
 	}
 	
 	protected static SimpleListFile2 mkSlf( File f ) {
 		mkParentDirs(f);
 		try {
 			return new SimpleListFile2( new RandomAccessFileBlob(f, "rw"), 16, true );
 		} catch( FileNotFoundException e ) {
 			// This should not happen!
 			throw new RuntimeException(e);
 		}
 	}
 		
 	static final String[] IGNORE_FILENAMES = {
 		"thumbs.db", "desktop.ini"
 	};
 	
 	// TODO: split into URN cache and upload cache; they are sometimes needed independently
 	
 	interface HashCache {
 		public String getFileUrn( File f ) throws Exception;
 		public void cacheFileUrn( File f, String urn ) throws Exception;
 	}
 	
 	class SLFHashCache implements HashCache {
 		final File fileUrnCacheFile;
 		final File dirUrnCacheFile;
 		SimpleListFile2 fileUrnCache;
 		SimpleListFile2 dirUrnCache;
 		
 		public SLFHashCache( File cacheDir ) {
 			this.fileUrnCacheFile = new File(cacheDir + "/file-urns.slf2");
 			this.dirUrnCacheFile = new File(cacheDir + "/dir-urns.slf2");
 		}
 		
 		protected synchronized SimpleListFile2 getFileUrnCache() {
 			if( fileUrnCache == null ) {
 				fileUrnCache = mkSlf(fileUrnCacheFile);
 			}
 			return fileUrnCache;
 		}
 		
 		protected synchronized SimpleListFile2 getDirUrnCache() {
 			if( dirUrnCache == null ) {
 				dirUrnCache = mkSlf(dirUrnCacheFile);
 			}
 			return dirUrnCache;
 		}
 		
 		protected SimpleListFile2 getUrnCache( File f ) {
 			return f.isDirectory() ? getDirUrnCache() : getFileUrnCache(); 
 		}
 		
 		protected ByteChunk fileIdChunk( File f ) throws IOException {
 			String fileId = f.getCanonicalPath() + ";mtime=" + f.lastModified() + ";size=" + f.length();
 			return BlobUtil.byteChunk(fileId);
 		}
 		
 		@Override
 		public void cacheFileUrn(File f, String urn) throws IOException {
 			SimpleListFile2 c = getUrnCache(f);
 			ByteChunk fileIdChunk = fileIdChunk(f);
 			ByteChunk urnChunk = BlobUtil.byteChunk(urn);
 			synchronized( c ) { c.put( fileIdChunk, urnChunk ); }
 		}
 		
 		@Override
 		public String getFileUrn(File f) throws Exception {
 			SimpleListFile2 c = getUrnCache(f);
 			ByteChunk fileIdChunk = fileIdChunk(f);
 			ByteChunk urnChunk;
 			synchronized( c ) { urnChunk = c.get( fileIdChunk ); }
 			return urnChunk != null ? BlobUtil.string( urnChunk ) : null;
 		}
 	}
 	
 	interface UploadCache {
 		public boolean isFullyUploaded( String urn ) throws Exception;
 		public void markFullyUploaded( String urn ) throws Exception;
 	}
 	
 	class SLFUploadCache implements UploadCache {
 		final File uploadedCacheFile;
 		
 		SimpleListFile2 uploadedCache;
 		
 		public SLFUploadCache( File cacheDir, String serverName ) {
 			this.uploadedCacheFile = new File(cacheDir + "/uploaded-to-"+serverName+".slf2");
 		}
 		
 		protected synchronized SimpleListFile2 getUploadedCache() {
 			if( uploadedCache == null ) {
 				uploadedCache = mkSlf(uploadedCacheFile);
 			}
 			return uploadedCache;
 		}
 		
 		@Override
 		public void markFullyUploaded(String urn) throws Exception {
 			SimpleListFile2 c = getUploadedCache();
 			ByteChunk urnChunk = BlobUtil.byteChunk(urn);
 			synchronized( c ) { c.put(urnChunk, YES_MARKER); }
 		}
 		
 		@Override
 		public boolean isFullyUploaded(String urn) throws Exception {
 			SimpleListFile2 c = getUploadedCache();
 			ByteChunk urnChunk = BlobUtil.byteChunk(urn);
 			ByteChunk storedMarker;
 			synchronized( c ) { storedMarker = c.get(urnChunk); }
 			return YES_MARKER.equals(storedMarker);
 		}
 	}
 	
 	static class Indexer
 	{
 		public static class IndexedObjectSink implements Sink<Object> {
 			public final Sink<Object> indexResultSink;
 			public final UploadCache uploadCache;
 			
 			public IndexedObjectSink( UploadCache uc, Sink<Object> sink ) {
 				this.uploadCache = uc;
 				this.indexResultSink = sink;
 			}
 			
 			public boolean isFullyUploaded( String urn ) throws Exception {
 				return uploadCache.isFullyUploaded(urn);
 			}
 			
 			public void give( Object thing ) throws Exception {
 				indexResultSink.give(thing);
 			}
 		}
 		
 		protected final DirectorySerializer dirSer;
 		protected final StreamURNifier digestor;
 		protected final HashCache hashCache;
 		protected final IndexedObjectSink[] destinations;
 		protected final int howToHandleFileReadErrors;
 		protected final boolean debug;
 
 		public Indexer(
 			DirectorySerializer dirSer, StreamURNifier digestor,
 			HashCache hashCache, IndexedObjectSink[] destinations,
 			int howToHandleFileReadErrors, boolean debug
 		) {
 			this.dirSer = dirSer;
 			this.digestor = digestor;
 			this.destinations = destinations;
 			this.hashCache = hashCache;
 			this.howToHandleFileReadErrors = howToHandleFileReadErrors;
 			this.debug = debug;
 		}
 		
 		protected boolean shouldIgnore( File f ) {
 			if( f.isHidden() || f.getName().startsWith(".") ) return true;
 			String name = f.getName().toLowerCase();
 			for( String ifn : IGNORE_FILENAMES ) if( ifn.equals(name) ) return true;
 			return false;
 		}
 		
 		public Collection<DirectoryEntry> indexDirectoryEntries( File file ) throws Exception {
 			ArrayList<DirectoryEntry> entries = new ArrayList<DirectoryEntry>();
 			
 			for( File c : file.listFiles() ) if( !shouldIgnore(c) ) {
 				IndexResult indexResult;
 				try {
 					indexResult = index(c);
 				} catch( FileReadError e ) {
 					switch( howToHandleFileReadErrors ) {
 					case Actions.SKIP_THE_FILE:
 						System.err.println("Skipping file due to read errors: "+e.file+": "+e.getCause().getMessage());
 						continue;
 					case Actions.THROW_AN_EXCEPTION: throw e;
 					default: throw new Exception("Invalid file read error handling option: "+howToHandleFileReadErrors);
 					}
 				}
 				entries.add( new DirectoryEntry( c.getName(), indexResult.fileInfo ) );
 			}
 			
 			return entries;
 		}
 		
 		protected boolean isFullyUploadedEverywhere(String urn) throws Exception {
 			for( IndexedObjectSink d : destinations ) {
 				if( !d.isFullyUploaded(urn) ) return false;
 			}
 			return true;
 		}
 		
 		protected IndexResult index( File file ) throws Exception {
 			if( debug ) System.err.println("Indexer: "+file+"...");
 			String cachedUrn = hashCache.getFileUrn( file );
 			if( cachedUrn != null && isFullyUploadedEverywhere(cachedUrn) ) {
 				if( debug ) System.err.println("Indexer: "+file+": already hashed+uploaded: "+cachedUrn);
 				// Then we don't need to recurse into subdirectories!
 				return new IndexResult(
 					new FileInfo(
 						file.getCanonicalPath(),
 						cachedUrn,
 						file.isDirectory() ? FileInfo.FILETYPE_DIRECTORY : FileInfo.FILETYPE_BLOB,
 						file.length(),
 						file.lastModified()
 					), false
 				);
 			}
 			
 			FileInfo fi;
 			if( file.isFile() ) {
 				String fileUrn;
 				if( cachedUrn != null ) {
 					fileUrn = cachedUrn;
 				} else {
 					FileInputStream fis = null;
 					try {
 						fis = new FileInputStream( file );
 						fileUrn = digestor.digest(fis);
 					} catch( IOException e ) {
 						throw new FileReadError( file, e );
 					} finally {
 						if( fis != null ) fis.close();
 					}
 				}
 				
 				fi = new FileInfo(
 					file.getCanonicalPath(),
 					fileUrn,
 					FileInfo.FILETYPE_BLOB,
 					file.length(),
 					file.lastModified()
 				);
 				
 				hashCache.cacheFileUrn( file, fileUrn );
 				
 				for( IndexedObjectSink d : destinations ) {
 					if( !d.isFullyUploaded(fi.urn) ) d.give(fi);
 				}
 			} else if( file.isDirectory() ) {
 				// Even if we already know the URN, we still need to walk the entire
 				// directory to push all non-fully-uploaded subfiles/directories to the uploader.
 				Collection<DirectoryEntry> entries = indexDirectoryEntries( file );
 				
 				// Will become true if it has subdirectories *that we want to index*.
 				// If so, we *cannot* rely on the hash cache, and will not store it.
 				/*
 				boolean includesSubDirs = false;
 				for( DirectoryEntry e : entries ) {
 					if( e.fileType == DirectoryEntry.FILETYPE_DIRECTORY ) {
 						includesSubDirs = true;
 					}
 				}
 				*/
 				
 				// On harold (Ubunty, some encfs or something) munging files
 				// does not alter the mtime of the directory containing the files!
 				// Therefore we can never cache directory hashes.
 				boolean canCacheDirectoryHash = false; //!includesSubDirs;
 				
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				dirSer.serialize(entries, baos);
 				baos.close();
 				
 				String rdfBlobUrn = digestor.digest(new ByteArrayInputStream(baos.toByteArray()));
 				String treeUrn = "x-rdf-subject:" + rdfBlobUrn;
 				
 				fi = new FileInfo(
 					file.getCanonicalPath(),
 					treeUrn,
 					FileInfo.FILETYPE_DIRECTORY,
 					file.length(),
 					file.lastModified()
 				);
 				
 				if( canCacheDirectoryHash && treeUrn != cachedUrn ) {
 					hashCache.cacheFileUrn( file, treeUrn );
 				}
 				if( isFullyUploadedEverywhere(treeUrn) ) {
 					if( debug ) System.err.println("Indexer: "+file+": already uploaded: "+treeUrn); 
 					return new IndexResult( fi, false );
 				}
 				
 				BlobInfo blobInfo = new BlobInfo( rdfBlobUrn, baos.toByteArray() );
 				
 				for( IndexedObjectSink d : destinations ) {
 					if( !d.isFullyUploaded(fi.urn) ) {
 						d.give( blobInfo );
 						d.give( new FullyStoredMarker(treeUrn) );
 					}
 				}
 			} else if( !file.exists() ) {
 				throw new RuntimeException(file+" does not exist");
 			} else {
 				throw new RuntimeException("Don't know how to index "+file);
 			}
			if( debug ) System.err.println("Indexer: "+file+": done: "+cachedUrn);
 			return new IndexResult( fi, true );
 		}
 		
 		protected IndexResult index( String path ) throws Exception {
 			return index( new File(path) );
 		}
 	}
 	
 	//// Put it all together ////
 	
 	Collection<UploadTask> tasks;
 	public int howToHandleFileReadErrors = Actions.THROW_AN_EXCEPTION;
 	public boolean debug;
 	public boolean showTransferSummary;
 	public boolean showProgress;
 	public boolean reportPathUrnMapping = false;
 	public boolean reportUrn = false;
 	public StreamURNifier digestor = BITPRINT_STREAM_URNIFIER;
 	public DirectorySerializer dirSer = new NewStyleRDFDirectorySerializer();
 	public File cacheDir;
 	
 	// Used when creating commits:
 	public File dataDir;
 	public File headDir;
 	public String storeSector;
 	
 	// Used when uploading:
 	public UploadClientSpec[] uploadClientSpecs;
 	
 	public FlowUploader( Collection<UploadTask> tasks ) {
 		this.tasks = tasks;
 	}
 	
 	protected HashCache hashCache;
 	protected synchronized HashCache getHashCache() {
 		if( cacheDir == null ) {
 			return new HashCache() {
 				@Override public String getFileUrn(File f) throws Exception { return null; }
 				@Override public void cacheFileUrn(File f, String urn) throws Exception {}
 			};
 		}
 		if( hashCache == null ) {
 			hashCache = new SLFHashCache(cacheDir);
 		}
 		return hashCache;
 	}
 	
 	protected Map<String,UploadCache> uploadCaches = new HashMap<String,UploadCache>();
 	protected synchronized UploadCache getUploadCache( String serverName ) {
 		if( cacheDir == null ) {
 			return new UploadCache() {
 				@Override public void markFullyUploaded(String urn) throws Exception {}
 				@Override public boolean isFullyUploaded(String urn) throws Exception { return false; }
 			};
 		}
 		UploadCache uc = uploadCaches.get(serverName);
 		if( uc == null ) {
 			uploadCaches.put( serverName, uc = new SLFUploadCache(cacheDir, serverName) );
 		}
 		return uc;
 	}
 	
 	protected Repository localRepository;
 	protected synchronized Repository getLocalRepository() {
 		if( localRepository == null ) {
 			if( dataDir == null ) throw new RuntimeException("Can't instantiate local repository; dataDir is null");
 			if( storeSector == null ) throw new RuntimeException("Can't instantiate local repository; storeSector is null");
 			localRepository = new SHA1FileRepository(dataDir, storeSector);
 		}
 		return localRepository;
 	}
 	
 	protected CommitManager cman;
 	protected synchronized CommitManager getCommitManager() {
 		if( cman == null ) {
 			cman = new CommitManager( getLocalRepository(), digestor );
 		}
 		return cman;
 	}
 	
 	protected HeadManager hman;
 	protected synchronized HeadManager getHeadManager() {
 		if( hman == null ) {
 			if( headDir == null ) throw new RuntimeException("Can't instantiate head manager; headDir is null");
 			hman = new HeadManager(headDir);
 		}
 		return hman;
 	}
 	
 	protected void report( FileInfo fileInfo ) {
 		if( reportUrn ) {
 			System.out.println(fileInfo.urn);
 		}
 		if( reportPathUrnMapping ) {
 			System.out.println(fileInfo.path+"\t"+fileInfo.urn);
 		}
 	}
 	
 	public void runIdentify() throws Exception {
 		final Indexer indexer = new Indexer( dirSer, digestor, getHashCache(), new IndexedObjectSink[0], howToHandleFileReadErrors, debug );
 		for( UploadTask ut : tasks ) {
 			IndexResult indexResult = indexer.index(ut.path);
 			report( indexResult.fileInfo );
 		}
 	}
 	
 	protected static void pipe( InputStream is, OutputStream os ) throws IOException {
 		byte[] buffer = new byte[65536];
 		for( int z = is.read(buffer); z>0; z = is.read(buffer) ) {
 			os.write(buffer, 0, z);
 		}
 	}
 	
 	static class Piper extends Thread {
 		protected final InputStream is;
 		protected final OutputStream os;
 		
 		public Piper( InputStream is, OutputStream os ) {
 			this.is = is;
 			this.os = os;
 		}
 		
 		@Override
 		public void interrupt() {
 			super.interrupt();
 			try {
 				is.close();
 			} catch( IOException e ) {
 				e.printStackTrace();
 			}
 		}
 		
 		@Override
 		public void run() {
 			try {
 				pipe(is,os);
 			} catch( IOException e ) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	boolean anythingSent = false;
 	
 	/**
 	 * This method creates, starts, and waits for a bunch of threads that comminicate via
 	 * pipes and queues.  The code does not make this super obvious, but the flow
 	 * is fairly simple:
 	 * 
 	 * indexer -> head requestor -> head response handler -> uploader -> upload response handler
 	 *                                \-> head error piper                 \-> upload error piper
 	 *                                
 	 * if indexer finishes without sending anything on to the next process, the other threads
 	 * are aborted.
 	 */
 	public int runUpload() {
 		final StandardTransferTracker tt = new StandardTransferTracker();
 		final UploadClient[] uploadClients = new UploadClient[uploadClientSpecs.length];
 		final IndexedObjectSink[] indexedObjectSinks = new IndexedObjectSink[uploadClientSpecs.length];
 		for( int i=0; i<uploadClientSpecs.length; ++i ) {
 			final UploadCache uc = getUploadCache(uploadClientSpecs[i].getServerName());
 			uploadClients[i] = uploadClientSpecs[i].createClient( uc, tt );
 			indexedObjectSinks[i] = new IndexedObjectSink( uc, uploadClients[i] );
 		}
 		
 		final LinkedBlockingQueue<Object> uploadTaskQueue     = new LinkedBlockingQueue<Object>(tasks);
 		
 		final Indexer indexer = new Indexer( dirSer, digestor, getHashCache(), indexedObjectSinks, howToHandleFileReadErrors, debug );
 		final QueueRunner indexRunner = new QueueRunner( uploadTaskQueue ) {
 			public boolean handleMessage( Object m ) throws Exception {
 				if( m instanceof UploadTask ) {
 					UploadTask ut = (UploadTask)m;
 					IndexResult indexResult = indexer.index(ut.path);
 					long timestamp = System.currentTimeMillis();
 					report( indexResult.fileInfo );
 					
 					if( indexResult.anyNewData ) {
 						// If any new data was uploaded, send the name -> URN mapping to the server
 						// to be logged.  We want to NOT do this if we are only indexing and not
 						// sending!  In this case anyNewData will also be false.
 						String objectTypeName = indexResult.fileInfo.fileType == FileInfo.FILETYPE_BLOB ? "File" : "Directory";
 						String message =
 							"[" + new Date(System.currentTimeMillis()).toString() + "] Uploaded\n" +
 							objectTypeName + " '" + ut.name + "' = " + indexResult.fileInfo.urn;
 						LogMessage lm = new LogMessage(BlobUtil.bytes(message));
 						for( IndexedObjectSink d : indexedObjectSinks ) d.give( lm );
 					}
 					
 					if( ut.commitConfig != null ) {
 						CommitManager.CommitSaveResult csr = getCommitManager().saveCommit(
 							new File(ut.path), indexResult.fileInfo.urn, timestamp, ut.commitConfig );
 						
 						BlobInfo commitBlobInfo = new BlobInfo( csr.latestCommitDataUrn, csr.latestCommitData );
 						for( IndexedObjectSink d : indexedObjectSinks ) {
 							// Log the commit and send the data to any server that doesn't have it
 							if( !d.isFullyUploaded(csr.latestCommitDataUrn) ) {
 								String message =
 										"[" + new Date(System.currentTimeMillis()).toString() + "] Uploaded\n" +
 										"Commit '" + ut.name + "' = " + csr.latestCommitUrn;
 									LogMessage lm = new LogMessage(BlobUtil.bytes(message));
 									d.give( lm );
 								d.give( commitBlobInfo );
 								d.give( new FullyStoredMarker(csr.latestCommitDataUrn) );
 							}
 							
 						}
 						
 						if( ut.commitConfig.headName != null ) {
 							int headNum = getHeadManager().addHead(ut.commitConfig.headName, 0, csr.latestCommitData);
 							PutHead ph = new PutHead( ut.commitConfig.headName, headNum, csr.latestCommitDataUrn );
 							String headNameUrn = "ccouch-head:"+ut.commitConfig.headName+"/"+headNum;
 							for( IndexedObjectSink d : indexedObjectSinks ) {
 								// Send the head to any server that doesn't have it
 								if( !d.isFullyUploaded(headNameUrn) ) {
 									d.give( ph );
 									d.give( new FullyStoredMarker(headNameUrn) );
 								}
 							}
 						}
 					}
 					
 					return true;
 				} else if( m instanceof EndMessage ) {
 					return false;
 				} else {
 					throw new RuntimeException("Unrecognised message type "+m.getClass());
 				}
 			}
 			
 			@Override
 			protected void cleanUp() throws Exception {
 				for( IndexedObjectSink d : indexedObjectSinks ) {
 					d.give( EndMessage.INSTANCE );
 				}
 			}
 		};
 		
 		final Thread indexThread = new Thread( indexRunner, "Indexer" );
 		final Thread progressThread = new Thread() {
 			public void run() {
 				try {
 					while( !Thread.interrupted() ) {
 						String fn = tt.getCurrentFilename();
 						if( fn == null ) fn = "";
 						if( fn.length() > 53 ) fn = "..."+fn.substring( fn.length()-50 );
 						System.err.print( String.format("% 10d / % 7d ; %50s\r", tt.getTotalByteCount(), tt.getTotalUnitCount(), fn) );
 						System.err.flush();
 						Thread.sleep(1000);
 					}
 				} catch( InterruptedException e ) {}
 			}
 		};
 		
 		for( UploadClient uc : uploadClients ) uc.start();
 		// UploadClients must be initialized (uc.start() must take care of this before it returns)
 		// before anything can be written to them, which is why I start them before starting
 		// the index thread!
 		indexThread.start();
 		if( showProgress ) progressThread.start();
 		
 		uploadTaskQueue.add( EndMessage.INSTANCE );
 		
 		boolean error = false;
 		try {
 			indexThread.join();
 			
 			for( UploadClient uc : uploadClients ) uc.join();
 			
 			progressThread.interrupt();
 			
 			for( UploadClient uc : uploadClients ) {
 				if( uc instanceof CommandUploadClient ) {
 					CommandUploadClient cuc = (CommandUploadClient)uc; 
 					if( cuc.anythingSent ) {
 						if( cuc.headProcExitCode != 0 ) {
 							System.err.println("Error: Head process exited with code "+cuc.headProcExitCode);
 							error = true;
 						}
 						if( cuc.uploadProcExitCode != 0 ) {
 							System.err.println("Error: Upload process exited with code "+cuc.uploadProcExitCode);
 							error = true;
 						}
 					}
 				}
 			}
 		} catch( InterruptedException e ) {
 			indexThread.interrupt();
 			for( UploadClient uc : uploadClients ) uc.halt();
 			Thread.currentThread().interrupt();
 			progressThread.interrupt();
 		}
 		
 		if( showTransferSummary ) {
 			if( tt.counters.isEmpty() ) {
 				System.err.println( "No transfers!" );
 			}
 			for( Map.Entry<String,Counter> thing : tt.counters.entrySet() ) {
 				System.err.println( "Transferred "+thing.getValue().byteCount+" bytes in "+thing.getValue().unitCount+" "+thing.getKey()+"s");
 			}
 		}
 		
 		return error ? 1 : 0;
 	}
 	
 	static String stripTrailingSlash( String path ) {
 		return ( path.length() > 0 && path.charAt(path.length()-1) == '/' ) ?
 			path.substring(0, path.length()-1) : path;
 	}
 	
 	/**
 	 * Returns an appropriate directory for storing cache files
 	 * within the repository at the given path.
 	 */
 	static String repoCacheDir( String repoPath ) {
 		return stripTrailingSlash(repoPath) + "/cache/flow-uploader";
 	}
 	
 	/**
 	 * Returns an appropriate directory for storing data files
 	 * (this directory will contain 'sector' directories)
 	 * within the repository at the given path.
 	 */
 	static String repoDataDir( String repoPath ) {
 		return stripTrailingSlash(repoPath) + "/data";
 	}
 	
 	/**
 	 * Returns an appropriate directory for storing head files
 	 * within the repository at the given path.
 	 */
 	static String repoHeadDir( String repoPath ) {
 		return stripTrailingSlash(repoPath) + "/heads";
 	}
 	
 	static class FlowUploaderCommand {
 		public static final int MODE_RUN   = 0;
 		public static final int MODE_ERROR = 1;
 		public static final int MODE_HELP  = 2;
 		
 		public static FlowUploaderCommand error( String errorMessage ) {
 			return new FlowUploaderCommand( MODE_ERROR, errorMessage, null );
 		}
 		
 		public static FlowUploaderCommand normal( FlowUploader up ) {
 			return new FlowUploaderCommand( MODE_RUN, null, up );
 		}
 
 		public static FlowUploaderCommand help() {
 			return new FlowUploaderCommand( MODE_HELP, null, null );
 		}
 		
 		public final int mode;
 		public final String errorMessage;
 		public final FlowUploader flowUploader;
 		
 		public FlowUploaderCommand( int mode, String errorMessage, FlowUploader fu ) {
 			this.mode = mode;
 			this.errorMessage = errorMessage;
 			this.flowUploader = fu;
 		}
 	}
 	
 	interface UploadClientSpec {
 		public String getServerName();
 		public UploadClient createClient( UploadCache uc, TransferTracker tt );
 	}
 	
 	static class CommandUploadClientSpec implements UploadClientSpec {
 		public final String name;
 		public final String[] command;
 		
 		public CommandUploadClientSpec( String name, String[] command ) {
 			this.name = name;
 			this.command = command;
 		}
 		
 		public String getServerName() { return name; }
 		
 		public UploadClient createClient( UploadCache uc, TransferTracker tt ) {
 			return new CommandUploadClient( name, command, uc, tt );
 		}
 	}
 	
 	static String[] readSubCommandArguments( Iterator<String> args ) {
 		String a;
 		List<String> sc = new ArrayList<String>();
 		for( a = args.hasNext() ? args.next() : null; a != null && !"--".equals(a) && !";".equals(a); a = args.hasNext() ? args.next() : null ) {
 			sc.add( a );
 		}
 		if( a == null ) {
 			System.err.println("Warning: no ending '--' found after '-server-command'.");
 		}
 		return sc.toArray(new String[sc.size()]);
 	}
 	
 	static FlowUploaderCommand fromArgs( Iterator<String> args, boolean requireServer, boolean alwaysShowUrns ) throws Exception {
 		ArrayList<UploadTask> tasks = new ArrayList<UploadTask>();
 		boolean verbose = false;
 		boolean debug = false;
 		boolean showProgress = false;
 		String cacheDir = null;
 		String dataDir = null;
 		String headDir = null;
 		String storeSector = "user";
 		String serverName = null;
 		String[] serverCommand = null;
 		
 		String repoName = null;
 		int howToHandleReadErrors = Actions.THROW_AN_EXCEPTION;
 		
 		// Optional commit info
 		String commitName = null;
 		String commitAuthor = null;
 		String commitMessage = null;
 		
 		ArrayList<UploadClientSpec> uploadClientSpecs = new ArrayList<UploadClientSpec>(); 
 		for( ; args.hasNext(); ) {
 			String a = args.next();
 			
 			// Verbosity options
 			if( "-v".equals(a) ) {
 				verbose = true;
 				showProgress = true;
 			} else if( "-show-progress".equals(a) ) {
 				showProgress = true;
 			} else if( "-debug".equals(a) ) {
 				debug =true;
 			
 			// Commit options
 			} else if( "-m".equals(a) ) {
 				commitMessage = args.next();
 			} else if( "-a".equals(a) ) {
 				commitAuthor = args.next();
 			} else if( "-n".equals(a) ) {
 				commitName = args.next();
 			
 			} else if( "-no-cache".equals(a) ) {
 				cacheDir = "DO-NOT-CACHE";
 			
 			} else if( "-repo".equals(a) || a.startsWith("-local-repo:") ) {
 				if( a.startsWith("-local-repo:") ) {
 					repoName = a.substring(12);
 				}
 				
 				String repoDir = args.next();
 				cacheDir = repoCacheDir( repoDir );
 				dataDir  = repoDataDir( repoDir );
 				headDir  = repoHeadDir( repoDir ); 
 				
 			} else if( "-cache-dir".equals(a) ) {
 				cacheDir = args.next();
 			} else if( "-data-dir".equals(a) ) {
 				dataDir = args.next();
 				
 			} else if( "-sector".equals(a) ) {
 				storeSector = args.next();
 			} else if( "-server-name".equals(a) ) {
 				serverName = args.next();
 			} else if( a.startsWith("-command-server:") ) {
 				final String sn = a.substring(16);
 				uploadClientSpecs.add( new CommandUploadClientSpec(sn, readSubCommandArguments(args)) );
 			} else if( "-server-command".equals(a) ) {
 				serverCommand = readSubCommandArguments(args);
 			} else if( "-skip-files-with-read-errors".equals(a) ) {
 				howToHandleReadErrors = Actions.SKIP_THE_FILE;
 			} else if( CCouch3Command.isHelpArgument(a) ) {
 				return FlowUploaderCommand.help();
 			} else if( !a.startsWith("-") ) {
 				CommitConfig commitConfig;
 				if( commitAuthor == null && commitMessage == null && commitName == null ) {
 					commitConfig = null;
 				} else {
 					if( commitName != null && repoName == null ) {
 						return FlowUploaderCommand.error("Must specify a local repo name (with -local-repo:<repo-name> <repo-path>) before -n <head-name> or use -full-head-name <full-head-name>");
 					}
 					
 					commitConfig = new CommitConfig( repoName + "/" + commitName, commitMessage, commitAuthor, new String[0] );
 					commitMessage = null;
 					commitName = null;
 				}
 				
 				tasks.add( new UploadTask(a, a, commitConfig) );
 			} else {
 				return FlowUploaderCommand.error("Unrecognised argument: "+a);
 			}
 		}
 		
 		File cacheDirFile;
 		if( "DO-NOT-CACHE".equals(cacheDir) ) {
 			cacheDirFile = null;
 		} else if( cacheDir == null ) {
 			String homeDir = System.getProperty("user.home");
 			if( homeDir == null ) homeDir = ".";
 			cacheDirFile = new File(repoCacheDir(homeDir + "/.ccouch"));
 		} else {
 			cacheDirFile = new File(cacheDir);
 		}
 		
 		File dataDirFile;
 		if( dataDir == null ) {
 			String homeDir = System.getProperty("user.home");
 			if( homeDir == null ) homeDir = ".";
 			dataDirFile = new File(repoDataDir(homeDir + "/.ccouch"));
 		} else {
 			dataDirFile = new File(dataDir);
 		}
 		
 		File headDirFile;
 		if( headDir == null ) {
 			String homeDir = System.getProperty("user.home");
 			if( homeDir == null ) homeDir = ".";
 			headDirFile = new File(repoHeadDir(homeDir + "/.ccouch"));
 		} else {
 			headDirFile = new File(headDir);
 		}
 		
 		// Set up upload clients //
 		
 		if( (serverName == null) != (serverCommand == null) ) {
 			return FlowUploaderCommand.error("-server-name and -server-command must both be specified if either are.");
 		}
 		if( serverName != null && serverCommand != null ) {
 			uploadClientSpecs.add( new CommandUploadClientSpec(serverName, serverCommand) );
 		}
 		if( requireServer ) {
 			if( uploadClientSpecs.size() == 0 ) {
 				return FlowUploaderCommand.error("No servers specified.");
 			}
 		}
 		
 		boolean showUrns = verbose || alwaysShowUrns;
 		
 		FlowUploader fu = new FlowUploader(tasks);
 		fu.debug = debug;
 		fu.showProgress = showProgress;
 		fu.cacheDir = cacheDirFile;
 		fu.dataDir = dataDirFile;
 		fu.headDir = headDirFile;
 		fu.storeSector = storeSector;
 		fu.showTransferSummary = verbose;
 		fu.reportPathUrnMapping = showUrns && tasks.size() != 1;
 		fu.reportUrn = showUrns && tasks.size() == 1;
 		fu.uploadClientSpecs = uploadClientSpecs.toArray(new UploadClientSpec[uploadClientSpecs.size()]);
 		fu.howToHandleFileReadErrors = howToHandleReadErrors;
 		return FlowUploaderCommand.normal( fu );
 	}
 	
 	static final String UPLOAD_USAGE =
 		"Usage: ccouch3 upload <options> <file/dir> ...\n" +
 		"\n" +
 		"Upload files and directories to a repository by piping cmd-server commands\n" +
 		"to another program (probably 'ssh somewhere \"ccouch3 cmd-server\"').\n" +
 		"\n" +
 		"Options:\n" +
 		"  -n <name>    ; Give a name for the next commit\n" +
 		"  -a <author>  ; Give an author name for the next commit\n" +
 		"  -m <message> ; Give a description for the next commit\n" +
 		"  -local-repo <path> ; Path to local ccouch repository to store cache in.\n" +
 		"  -local-repo:<name> <path> ; Path to a named local repository; this is needed\n" +
 		"                 when creating a named commit with '-n'\n" +
 		"  -no-cache    ; Do not cache file hashes or upload records.\n" +
 		"  -command-server:<name> <cmd> <arg> <arg> ... ';' ; Add a command to pipe\n" +
 		"                 cmd-server commands to\n" +
 		//"  -server-name <name> ; name of repository you are uploading to;\n" +
 		//"    this is used for tracking which files have already been uploaded where.\n" +
 		//"  -server-command <cmd> <arg> <arg> ... -- ; Command to pipe cmd-server\n" +
 		//"    commands to.\n" +
 		"  -show-progress ; show progress information on stderr.\n" +
 		"\n" +
 		"Note that commit info (besides author) must be given separately for each file\n" +
 		"listed that is to have a commit created for it, and commit information must\n" +
 		"be given *before* the path to the file/directory to be uploaded\n" +
 		"\n" +
 		"Local repository names are used as the first path component of heads.\n" +
 		"Remote repository names are used to keep track of what files have already\n" +
 		"been uploaded where.\n" +
 		"\n" +
 		"You can specify more than one remote server, and files will be uploaded to\n" +
 		"all of them.\n" +
 		"\n" +
 		"Example usage:\n" +
 		"  ccouch3 upload \\\n" +
 		"    -command-server:example.org ssh tom@example.org \"ccouch3 cmd-server\" ';' \\\n" +
 		"    -local-repo:/home/tom/ccouch \\\n" +
 		"    -a Tom -n archives/tom/pictures -m \"Tom's pictures\" /home/tom/pics/ \\\n" +
 		"    -a Tom -n archives/tom/docs -m \"Tom's documents\" /home/tom/docs/";
 	
 	public static int uploadMain( Iterator<String> args ) throws Exception {
 		FlowUploaderCommand fuc = fromArgs(args, true, false);
 		switch( fuc.mode ) {
 		case( FlowUploaderCommand.MODE_ERROR ):
 			System.err.println( "Error: " + fuc.errorMessage + "\n" );
 			System.err.println( UPLOAD_USAGE );
 			return 1;
 		case( FlowUploaderCommand.MODE_HELP ):
 			System.out.println( UPLOAD_USAGE );
 			return 0;
 		default:
 			return fuc.flowUploader.runUpload();
 		}
 	}
 	
 	static final String IDENTIFY_USAGE =
 		"Usage: ccouch3 id <file/dir> ...\n" +
 		"\n" +
 		"Identify files and directories without copying them anywhere.\n" +
 		"\n" +
 		"Options:\n" +
 		"  -repo <path> ; Path to local ccouch repository to store cache in.\n" +
 		"  -no-cache    ; Do not cache file hashes or upload records.\n" +
 		"\n" +
 		"Example usage:\n" +
 		"  ccouch3 id something.txt some-directory/ somethingelse.zip";
 	
 	public static int identifyMain( Iterator<String> args ) throws Exception {
 		FlowUploaderCommand fuc = fromArgs(args, false, true);
 		switch( fuc.mode ) {
 		case( FlowUploaderCommand.MODE_ERROR ):
 			System.err.println( "Error: " + fuc.errorMessage + "\n" );
 			System.err.println( IDENTIFY_USAGE );
 			return 1;
 		case( FlowUploaderCommand.MODE_HELP ):
 			System.out.println( IDENTIFY_USAGE );
 			return 0;
 		default:
 			fuc.flowUploader.runIdentify();
 			return 0;
 		}
 	}
 	
 	public static void main( String[] args ) throws Exception {
 		System.exit(uploadMain( Arrays.asList(args).iterator() ));
 	}
 }
