 package de.zib.gndms.taskflows.esgfStaging.server;
 
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import de.zib.gndms.common.dspace.service.SubspaceService;
 import de.zib.gndms.common.kit.security.SetupSSL;
 import de.zib.gndms.common.model.gorfx.types.SliceResultImpl;
 import de.zib.gndms.common.rest.MyProxyToken;
 import de.zib.gndms.common.rest.Specifier;
 import de.zib.gndms.common.rest.UriFactory;
 import de.zib.gndms.gndmc.utils.DownloadResponseExtractor;
 import de.zib.gndms.gndmc.utils.HTTPGetter;
 import de.zib.gndms.kit.config.ConfigProvider;
 import de.zib.gndms.kit.config.MandatoryOptionMissingException;
 import de.zib.gndms.kit.config.MapConfig;
 import de.zib.gndms.kit.security.CredentialProvider;
 import de.zib.gndms.kit.security.GetCredentialProviderFor;
 import de.zib.gndms.kit.security.SSLCredentialInstaller;
 import de.zib.gndms.logic.model.dspace.SliceConfiguration;
 import de.zib.gndms.logic.model.gorfx.TaskFlowAction;
 import de.zib.gndms.model.common.PersistentContract;
 import de.zib.gndms.model.dspace.Slice;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.model.util.TxFrame;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.Task;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import de.zib.gndms.taskflows.esgfStaging.client.ESGFStagingTaskFlowMeta;
 import de.zib.gndms.taskflows.esgfStaging.client.model.ESGFStagingOrder;
 import org.jetbrains.annotations.NotNull;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.security.KeyStoreException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.util.Map;
 
 /**
  * @author bachmann@zib.de
  * @date 14.02.11  17:53
  * @brief The action of the dummy task flow.
  *
  * @see ESGFStagingOrder
  */
 public class ESGFStagingTFAction extends TaskFlowAction< ESGFStagingOrder > {
     // TODO: derive from AbstractProviderStagInAction (e.g. see createNewSlice)
 
     public static final long DEFAULT_SLICE_SIZE = 100*1024*1024; // 100MB
 
     private SubspaceService subspaceService;
     private static final String PROXY_FILE_NAME = File.separator + "x509_proxy.pem";
 
     @Override
     public Class< ESGFStagingOrder > getOrderBeanClass( ) {
         return ESGFStagingOrder.class;
     }
 
 
     public ESGFStagingTFAction() {
         super( ESGFStagingTaskFlowMeta.TASK_FLOW_TYPE_KEY );
     }
 
 
     public ESGFStagingTFAction(@NotNull EntityManager em, @NotNull Dao dao, @NotNull Taskling model) {
 
         super( ESGFStagingTaskFlowMeta.TASK_FLOW_TYPE_KEY, em, dao, model );
     }
 
 
     @Override
     protected void onCreated( @NotNull String wid, @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState ) throws Exception {
 
         ensureOrder();
 
         // set max progress to number of files to download
         if( !isRestartedTask ) {
             ESGFStagingOrder order = getOrderBean();
 
             final Session session = getDao().beginSession();
             try {
                 Task task = getTask( session );
                 task.setProgress(0);
                 task.setMaxProgress(order.getUrls().size());
                 session.success();
             }
             finally { session.finish(); }
 
             super.onCreated( wid, state, isRestartedTask, altTaskState );    // overridden method implementation
         }
 
         createNewSlice();
         super.onCreated(wid, state, isRestartedTask, altTaskState);
     }
 
 
     @Override
     protected void onInProgress( @NotNull String wid, @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState ) throws Exception {
         final ESGFStagingOrder order = getOrderBean();
         
         final Map< String, String > urls = order.getUrls();
         final Slice slice = findSlice();
         final String slicePath = slice.getSubspace().getPathForSlice( slice );
         
         // get certificate and private key by credentials
         final SetupSSL setupSSL;
         try {
             setupSSL = prepareProxy();
         }
         catch( Throwable e ) {
             logger.error( "Could not authenticate against ESGF Provider." );
             transit( TaskState.FAILED );
             throw new Exception( "Could not authenticate against ESGF Provider.", e );
         }
         
         final HTTPGetter httpGetter = new HTTPGetter();
         httpGetter.setupSSL( setupSSL, getKeyPassword() );
 
         final Task detachedTask = getDetachedTask();
 
         int progress = detachedTask.getProgress();
         int i = 0;
         for( String url: urls.keySet() ) {
             // skip already processed entries
             if( i++ < progress )
                 continue;
 
             File urlFile = new File( url );
             File outFile = new File( slicePath + File.separatorChar + urlFile.getName() );
 
             // download file
             {
                 httpGetter.setExtractor( 200, new DownloadResponseExtractor( outFile.getCanonicalPath() ) );
                 final int statusCode = httpGetter.get( url );
                         
                 if( 200 != statusCode )
                 {
                     transit(TaskState.FAILED);
                     throw new Exception( "Could not download all files. HTTP GET returned status code " + statusCode );
                 }
             }
 
             // check for correct checksum
             {
                 String checksum = urls.get( url );
                 String compare = makeMD5( outFile );
 
                 if( ! checksum.equals( compare) ) {
                     transit(TaskState.FAILED);
                     throw new Exception( "Downloaded file " + outFile.getName() + " has wrong checksum." );
                 }
             }
 
             setProgress( ++progress );
         }
 
         transitWithPayload(
                 new SliceResultImpl(
                         ESGFStagingTaskFlowMeta.TASK_FLOW_TYPE_KEY,
                         getSliceSpecifier() ),
                 TaskState.FINISHED );
 
         super.onInProgress(wid, state, isRestartedTask, altTaskState);    // overridden method implementation
     }
 
 
     public CredentialProvider getCredentialProvider() {
         CredentialProvider credentialProvider = new GetCredentialProviderFor(
                 getOrder(),
                 ESGFStagingTaskFlowMeta.REQUIRED_AUTHORIZATION.get( 0 ),
                 getMyProxyFactoryProvider()
         ).invoke();
         credentialProvider.setInstaller( new SSLCredentialInstaller() );
         return  credentialProvider;
     }
 
 
     protected String getKeyPassword( ) {
         final MyProxyToken token = getOrder().getMyProxyToken().get( ESGFStagingTaskFlowMeta.REQUIRED_AUTHORIZATION.get( 0 ) );
         if( null == token )
             throw new IllegalArgumentException( "No " + ESGFStagingTaskFlowMeta.REQUIRED_AUTHORIZATION.get( 0 ) + " token provided in Order." );
 
         return token.getPassword();
     }
 
 
     protected SetupSSL prepareProxy( )
             throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
             UnrecoverableKeyException, MandatoryOptionMissingException {
         final SetupSSL setupSSL = new SetupSSL();
         
         final MyProxyToken token = getOrder().getMyProxyToken().get( ESGFStagingTaskFlowMeta.REQUIRED_AUTHORIZATION.get( 0 ) );
         if( null == token )
             throw new IllegalArgumentException( "No for purpose " + ESGFStagingTaskFlowMeta.REQUIRED_AUTHORIZATION.get( 0 ) + " token provided in Order." );
 
         final String password = token.getPassword();
         final String trustStoreLocation = getOfferTypeConfig().getOption( "trustStoreLocation" );
         final String trustStorePassword = getOfferTypeConfig().getOption( "trustStorePassword" );
 
         setupSSL.setTrustStoreLocation( trustStoreLocation );
         setupSSL.prepareTrustStore( trustStorePassword, "JKS" );
         setupSSL.prepareKeyStore( password, "JKS" );
 
         try {
             SSLCredentialInstaller.InstallerParams params = new SSLCredentialInstaller.InstallerParams();
 
             params.setAlias( "privateKey" );
             params.setPassword( password );
             params.setSetupSSL( setupSSL );
 
             getCredentialProvider().installCredentials( params );
         }
         catch ( Exception e ) {
             logger.debug( "Couldn't deploy credentials " );
             throw new IllegalStateException( "Couldn't deploy credentials: ", e );
         }
 
         return setupSSL;
     }
     
     
     public static String makeMD5( File file ) throws Exception {
         final MessageDigest md = MessageDigest.getInstance( "MD5" );
         final FileInputStream fis = new FileInputStream( file );
         final byte[] dataBytes = new byte[1024];
  
         int nread = 0;
  
         while( ( nread = fis.read( dataBytes ) ) != -1 ) {
             md.update( dataBytes, 0, nread );
         }
  
         final byte[] mdbytes = md.digest();
  
         //convert the byte to hex format
         final StringBuffer sb = new StringBuffer( "" );
         for( byte mdbyte : mdbytes ) {
             sb.append( Integer.toString( ( mdbyte & 0xff ) + 0x100, 16 ).substring( 1 ) );
         }
 
         return sb.toString();
     }
 
 
     private Slice findSlice() {
         final String sliceId = getSliceId();
         getLogger().info( "findSlice(" + ( sliceId == null ? "null" : '"' + sliceId + '"' ) + ')' );
         if (sliceId == null)
             return null;
 
 
         final EntityManager em = getEntityManager();
         final TxFrame txf = new TxFrame(em);
         try {
             final Slice slice = em.find(Slice.class, sliceId);
             txf.commit();
             return slice;
         }
         finally { txf.finish();  }
     }
 
 
     private void setProgress( int progress ) {
         final Session session = getDao().beginSession();
         try {
             Task task = getTask( session );
             task.setProgress( progress );
             session.success();
         }
         finally { session.finish(); }
     }
 
 
     private Task getDetachedTask() {
         final Session session = getDao().beginSession();
         try {
             Task task = getTask( session );
             session.success();
             return task;
         }
         finally { session.finish(); }
     }
 
 
     private void createNewSlice() throws MandatoryOptionMissingException {
         final ConfigProvider config = getOfferTypeConfig();
 
         final String subspaceUrl = config.getOption( "subspace" );
         String sliceKindKey = config.getOption( "sliceKind" );
 
 
         SliceConfiguration sconf = new SliceConfiguration();
         sconf.setTerminationTime( getContract().getResultValidity() );
         sconf.setSize( DEFAULT_SLICE_SIZE );
 
         ResponseEntity< Specifier< Void > > sliceSpec = subspaceService.createSlice(
                 subspaceUrl,
                 sliceKindKey,
                 sconf.getStringRepresentation(),
                 getOrder().getDNFromContext() );
 
         if (! HttpStatus.CREATED.equals( sliceSpec.getStatusCode() ) )
             throw new IllegalStateException( "Slice creation failed" );
 
         setSliceSpecifier( sliceSpec.getBody() );
 
         // to provoke nasty test condition uncomment the following line
         //throw new NullPointerException( );
         getLogger().info( "createNewSlice() = " + getSliceId() );
     }
 
 
     protected void setSliceSpecifier( Specifier<Void> sliceId ) {
         final Session session = getDao().beginSession();
         try {
             setSliceSpecifier( sliceId, session );
             session.success();
         }
         finally {
             session.finish();
         }
     }
 
 
     private void setSliceSpecifier( final Specifier< Void > sliceId, final Session session ) {
         final Task task = getTask( session );
         task.setPayload(sliceId);
     }
     
     
     protected Specifier< Void > getSliceSpecifier( ) {
         // maybe cache the slice id
         final Session session = getDao().beginSession();
         try {
             final Task task = getTask( session );
             Specifier<Void> sliceSpec = ( Specifier<Void> ) task.getPayload();
             session.success();
             return sliceSpec;
         }
         finally {
             session.finish();
         }
     }
 
 
     protected String getSliceId( ) {
         return getSliceSpecifier().getUriMap().get( UriFactory.SLICE );
     }
 
 
     public PersistentContract getContract() {
         final Session session = getDao().beginSession();
         try {
             final Task task = getTask(session);
             final PersistentContract ret = task.getContract();
             session.finish();
             return ret;
         }
         finally {
             session.success();
         }
     }
 
 
     protected @NotNull
     MapConfig getOfferTypeConfig() {
        return new MapConfig( getTaskFlowTypeConfigMapData() );
     }
 
 
     @Override
     public DelegatingOrder<ESGFStagingOrder> getOrder() {
 
         DelegatingOrder< ESGFStagingOrder > order = null;
 
         final Session session = getDao().beginSession();
         try {
             order = ( DelegatingOrder< ESGFStagingOrder > ) getTask( session ).getOrder( );
             session.success();
         }
         finally { session.finish(); }
 
         return order;
     }
 
 
     public SubspaceService getSubspaceService() {
         return subspaceService;
     }
 
     @SuppressWarnings( "SpringJavaAutowiringInspection" )
     @Inject
     public void setSubspaceService( SubspaceService subspaceService ) {
         this.subspaceService = subspaceService;
     }
 }
