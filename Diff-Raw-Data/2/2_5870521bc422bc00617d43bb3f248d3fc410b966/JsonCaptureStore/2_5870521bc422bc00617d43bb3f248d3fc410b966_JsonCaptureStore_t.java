 package com.redhat.rcm.nexus.capture.store;
 
 import static com.redhat.rcm.nexus.capture.model.CaptureSession.key;
 import static com.redhat.rcm.nexus.capture.model.ModelSerializationUtils.getGson;
 import static com.redhat.rcm.nexus.util.PathUtils.joinFile;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
 import org.codehaus.plexus.util.IOUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonatype.nexus.artifact.Gav;
 import org.sonatype.nexus.artifact.GavCalculator;
 import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
 import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
 import org.sonatype.nexus.proxy.item.StorageItem;
 
 import com.google.gson.reflect.TypeToken;
 import com.redhat.rcm.nexus.capture.model.CaptureSession;
 import com.redhat.rcm.nexus.capture.model.CaptureSessionCatalog;
 import com.redhat.rcm.nexus.capture.model.CaptureSessionRef;
 import com.redhat.rcm.nexus.capture.model.CaptureTarget;
 import com.redhat.rcm.nexus.capture.store.CaptureSessionQuery.QueryMode;
 
 @Named( "json" )
 public class JsonCaptureStore
     implements CaptureStore, Initializable
 {
 
     private static final String CATALOG_FILENAME = "catalog.json";
 
     private static final TypeToken<Set<CaptureSessionCatalog>> CATALOG_SET_TYPE_TOKEN =
         new TypeToken<Set<CaptureSessionCatalog>>()
         {
         };
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     @Inject
     private ApplicationConfiguration applicationConfiguration;
 
     @Inject
     @Named( "maven2" )
     GavCalculator gavCalculator;
 
     private final Map<String, CaptureSession> sessions = new HashMap<String, CaptureSession>();
 
     private final Map<String, CaptureSessionCatalog> catalogs = new HashMap<String, CaptureSessionCatalog>();
 
     private File workDir;
 
     public JsonCaptureStore()
     {
         System.out.println( "\n\n\n\nStarting JSON capture store!\nInstance: " + this + "\n\n\n\n" );
     }
 
     public CaptureSessionRef closeCurrentLog( final String user, final String buildTag )
         throws CaptureStoreException
     {
         final CaptureSession session = sessions.remove( key( user, buildTag ) );
        return session == null ? null : session.ref();
     }
 
     public void deleteLogs( final CaptureSessionQuery query )
         throws CaptureStoreException
     {
         boolean changed = false;
         for ( final Map.Entry<String, CaptureSession> entry : new HashMap<String, CaptureSession>( sessions ).entrySet() )
         {
             if ( query.matches( entry.getValue() ) )
             {
                 if ( sessions.remove( entry.getKey() ) != null )
                 {
                     final CaptureSessionCatalog catalog = catalogs.get( entry.getKey() );
                     catalog.remove( entry.getValue().getStartDate() );
 
                     if ( QueryMode.FIRST_MATCHING == query.getMode() )
                     {
                         changed = true;
                         break;
                     }
                 }
             }
         }
 
         if ( !changed || QueryMode.ALL_MATCHING == query.getMode() )
         {
             catalogs: for ( final Map.Entry<String, CaptureSessionCatalog> entry : new HashMap<String, CaptureSessionCatalog>( catalogs ).entrySet() )
             {
                 final CaptureSessionCatalog catalog = entry.getValue();
                 if ( query.matches( catalog ) && catalog.getSessions() != null && !catalog.getSessions().isEmpty() )
                 {
                     for ( final Date startDate : new HashSet<Date>( catalog.getSessions().keySet() ) )
                     {
                         if ( query.matches( startDate ) )
                         {
                             catalog.remove( startDate );
                             changed = true;
 
                             if ( QueryMode.FIRST_MATCHING == query.getMode() )
                             {
                                 break catalogs;
                             }
                         }
                     }
 
                     if ( !changed && QueryMode.FIRST_MATCHING == query.getMode() )
                     {
                         // we matched a catalog, but not a session...QUIT.
                         break;
                     }
                 }
             }
         }
 
         if ( changed )
         {
             writeCatalogs();
         }
     }
 
     public List<CaptureSessionRef> getLogs( final CaptureSessionQuery query )
         throws CaptureStoreException
     {
         final List<CaptureSessionRef> result = new ArrayList<CaptureSessionRef>();
         for ( final CaptureSessionCatalog catalog : catalogs.values() )
         {
             if ( query.matches( catalog ) && catalog.getSessions() != null )
             {
                 for ( final Map.Entry<Date, File> entry : catalog.getSessions().entrySet() )
                 {
                     if ( query.matches( entry.getKey() ) )
                     {
                         result.add( new CaptureSessionRef( catalog.getUser(), catalog.getBuildTag(), entry.getKey() ) );
                     }
                 }
             }
         }
 
         return result;
     }
 
     public CaptureSession readLog( final CaptureSessionRef ref )
         throws CaptureStoreException
     {
         CaptureSession session = sessions.get( ref.key() );
 
         if ( session == null || !ref.getDate().equals( session.getStartDate() ) )
         {
             final CaptureSessionCatalog catalog = catalogs.get( ref.key() );
             if ( catalog != null )
             {
                 final TreeMap<Date, File> sessions = catalog.getSessions();
                 final File f = sessions.get( ref.getDate() );
                 if ( f != null && f.exists() )
                 {
                     session = readSession( f );
                 }
             }
         }
 
         return session;
     }
 
     public CaptureSession readLatestLog( final String user, final String buildTag )
         throws CaptureStoreException
     {
         CaptureSession session = getSession( user, buildTag, null, false );
         if ( session == null )
         {
             final CaptureSessionCatalog catalog = catalogs.get( key( user, buildTag ) );
             if ( catalog != null )
             {
                 final TreeMap<Date, File> sessions = catalog.getSessions();
                 if ( !sessions.isEmpty() )
                 {
                     final LinkedList<Date> dates = new LinkedList<Date>( sessions.keySet() );
 
                     final File f = sessions.get( dates.getLast() );
 
                     session = readSession( f );
                 }
             }
         }
 
         return session;
     }
 
     private CaptureSession readSession( final File sessionFile )
         throws CaptureStoreException
     {
         FileReader reader = null;
         try
         {
             reader = new FileReader( sessionFile );
 
             return getGson().fromJson( reader, CaptureSession.class );
         }
         catch ( final IOException e )
         {
             throw new CaptureStoreException( "Failed to read capture-session from disk."
                             + "\nFile Path: {0}\nReason: {1}", e, sessionFile.getAbsolutePath(), e.getMessage() );
         }
         finally
         {
             IOUtil.close( reader );
         }
     }
 
     public void logResolved( final String user, final String buildTag, final String captureSource,
                              final List<String> processedRepositories, final String path, final StorageItem item )
         throws CaptureStoreException
     {
         final CaptureSession session = getSession( user, buildTag, captureSource, true );
 
         final Gav gav = toGav( path );
         session.add( new CaptureTarget( processedRepositories, path, gav, item ) );
         output( session );
     }
 
     public void logUnresolved( final String user, final String buildTag, final String captureSource,
                                final List<String> processedRepositories, final String path )
         throws CaptureStoreException
     {
         final CaptureSession session = getSession( user, buildTag, captureSource, true );
 
         final Gav gav = toGav( path );
         session.add( new CaptureTarget( processedRepositories, path, gav ) );
         output( session );
     }
 
     private synchronized CaptureSession getSession( final String user, final String buildTag,
                                                     final String captureSource, final boolean create )
     {
         CaptureSession session = sessions.get( key( user, buildTag ) );
         if ( create && session == null )
         {
             session = new CaptureSession( user, buildTag, captureSource );
             sessions.put( session.key(), session );
         }
 
         return session;
     }
 
     private Gav toGav( final String path )
     {
         Gav gav = null;
         try
         {
             gav = gavCalculator.pathToGav( path );
         }
         catch ( final IllegalArtifactCoordinateException e )
         {
             logger.error( String.format( "Cannot calculate GAV (artifact coordinate) from path: '%s'.\nReason: %s",
                                          path, e.getMessage() ), e );
         }
 
         return gav;
     }
 
     private synchronized void output( final CaptureSession session )
         throws CaptureStoreException
     {
         final File sessionFile = getSessionFile( session );
 
         if ( !sessionFile.getParentFile().isDirectory() && !sessionFile.getParentFile().mkdirs() )
         {
             throw new CaptureStoreException( "Cannot log capture-session to disk. Failed to create storage directory: {0}.",
                                              sessionFile.getParentFile() );
         }
 
         FileWriter writer = null;
         try
         {
             writer = new FileWriter( sessionFile );
             writer.write( getGson().toJson( session ) );
         }
         catch ( final IOException e )
         {
             throw new CaptureStoreException( "Failed to write capture-session to disk."
                                                              + "\nUser: {0}"
                                                              + "\nBuild-Tag: {1}\nCapture-Source: {2}\nFile Path: {3}\nReason: {4}",
                                              e,
                                              session.getUser(),
                                              session.getBuildTag(),
                                              session.getCaptureSource(),
                                              sessionFile.getAbsolutePath(),
                                              e.getMessage() );
         }
         finally
         {
             IOUtil.close( writer );
         }
 
         catalog( session );
     }
 
     private void catalog( final CaptureSession session )
         throws CaptureStoreException
     {
         CaptureSessionCatalog catalog = catalogs.get( session.key() );
         if ( catalog == null )
         {
             catalog = new CaptureSessionCatalog( session.getBuildTag(), session.getCaptureSource(), session.getUser() );
             catalogs.put( session.key(), catalog );
         }
 
         catalog.add( session );
 
         writeCatalogs();
     }
 
     private void readCatalogs()
         throws IOException
     {
         final File catalogFile = new File( workDir(), CATALOG_FILENAME );
         if ( catalogFile.exists() && catalogFile.length() > 0 )
         {
             FileReader reader = null;
             try
             {
                 reader = new FileReader( catalogFile );
 
                 Set<CaptureSessionCatalog> cats = null;
                 cats = getGson().fromJson( reader, CATALOG_SET_TYPE_TOKEN.getType() );
 
                 if ( cats != null )
                 {
                     for ( final CaptureSessionCatalog cat : cats )
                     {
                         catalogs.put( key( cat.getUser(), cat.getBuildTag() ), cat );
                     }
                 }
             }
             finally
             {
                 IOUtil.close( reader );
             }
         }
     }
 
     private void writeCatalogs()
         throws CaptureStoreException
     {
         final File catalogFile = new File( workDir(), CATALOG_FILENAME );
         catalogFile.getParentFile().mkdirs();
 
         FileWriter writer = null;
         try
         {
             writer = new FileWriter( catalogFile );
             writer.write( getGson().toJson( new HashSet<CaptureSessionCatalog>( catalogs.values() ) ) );
         }
         catch ( final IOException e )
         {
             throw new CaptureStoreException( "Failed to write capture-session catalog to disk.\nFile: {0}\nReason: {1}",
                                              e,
                                              catalogFile.getAbsolutePath(),
                                              e.getMessage() );
         }
         finally
         {
             IOUtil.close( writer );
         }
     }
 
     private synchronized File getSessionFile( final CaptureSession session )
         throws CaptureStoreException
     {
         File sessionFile = session.getFile();
         if ( sessionFile == null )
         {
             final String filename =
                 String.format( "%1$s-%3$s-%2$tY-%2$tm-%2$td_%2$tH-%2$tM-%2$tS%2$tz.json", session.getBuildTag(),
                                session.getStartDate(), session.getCaptureSource() );
 
             sessionFile = joinFile( workDir(), session.getUser(), filename );
 
             session.setFile( sessionFile );
         }
 
         return sessionFile;
     }
 
     private File workDir()
     {
         if ( workDir == null )
         {
             File dir;
             try
             {
                 dir = applicationConfiguration.getWorkingDirectory().getCanonicalFile();
             }
             catch ( final IOException e )
             {
                 dir = applicationConfiguration.getWorkingDirectory().getAbsoluteFile();
             }
 
             workDir = joinFile( dir, "capture-sessions" );
         }
 
         return workDir;
     }
 
     public synchronized void initialize()
         throws InitializationException
     {
         // FIXME: See https://issues.sonatype.org/browse/NEXUS-3308
         try
         {
             readCatalogs();
         }
         // catch ( final IOException e )
         // {
         // System.out.println( "\n\n\n\n\nFailed to read catalogs.json file!!!\n\n\n\n\n" );
         //
         // throw new InitializationException( String.format( "Failed to read catalogs from the filesystem: %s",
         // e.getMessage() ), e );
         // }
         catch ( final Exception e )
         {
             throw new Error( "[NEXUS-3308] FAILURE to initialize JSON capture-session store: " + e.getMessage(), e );
         }
     }
 
 }
