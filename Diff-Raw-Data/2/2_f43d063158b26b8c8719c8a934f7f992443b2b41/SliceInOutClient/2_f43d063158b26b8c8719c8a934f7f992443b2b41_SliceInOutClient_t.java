 package de.zib.gndmc;
 
 /*
  * Copyright 2008-2010 Zuse Institute Berlin (ZIB)
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
 
 
 import de.zib.gndmc.DSpace.beans.SliceCreationBean;
 import de.zib.gndmc.GORFX.GORFXClientUtils;
 import de.zib.gndmc.GORFX.beans.FileTransferBean;
 import de.zib.gndms.dspace.client.DSpaceClient;
 import de.zib.gndms.dspace.slice.client.SliceClient;
 import de.zib.gndms.dspace.subspace.client.SubspaceClient;
 import de.zib.gndms.gritserv.delegation.DelegationAux;
 import de.zib.gndms.kit.application.AbstractApplication;
 import de.zib.gndms.model.common.ImmutableScopedName;
 import de.zib.gndms.stuff.exception.FinallyException;
 import org.apache.axis.message.MessageElement;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.axis.types.URI;
 import org.globus.gsi.GlobusCredentialException;
 import org.kohsuke.args4j.Option;
 import types.ContextT;
 import types.FileTransferResultT;
 
 import java.io.*;
 import java.rmi.RemoteException;
 import java.util.*;
 
 /**
  * @author  try ma ik jo rr a zib
  * @version $Id$
  *          <p/>
  *          User: mjorra, Date: 22.07.2010, Time: 17:05:28
  */
 public class SliceInOutClient {
 
     private SliceCreationBean creationBean;
     private FileTransferBean transferBean;
 
 
     private SubspaceClient subSpaceClient;
     private DSpaceClient dspaceClient;
 
 
     public void run( ) throws Exception, RemoteException, GlobusCredentialException {
 
         DSpaceClient dc = getDSpaceClient();
         String v = queryDMSVersion();
         System.out.println( "Connected to GNDMS: " + v );
 
         System.out.println( "Creating slice" );
         SliceClient sc = createSlice();
 
         String loc = sc.getSliceLocation();
 
 
         ContextT ctx = new ContextT( );
         EndpointReferenceType delegatEPR = null;
         if(! transferBean.isDisableDelegation() ) {
             System.out.println( "Setting up delegation" );
            delegatEPR = GORFXClientUtils.setupDelegation( ctx, getDspaceURI(), transferBean.getProxyFile() );
         }
 
         System.out.println( "Copy " + getSourcePath() + " -> " + loc );
         FileTransferResultT res = GORFXClientUtils.performCopy( getGorfxURI(), ctx, getSourcePath(), loc );
         System.out.println( "File transfer passed" );
         showCopyResult( res );
 
 
 
         System.out.println( "\nNow the otherway round!" );
         System.out.println( "Copy " + loc + " -> " + getDestinationPath() );
         res = GORFXClientUtils.performCopy( getGorfxURI(), ctx, loc, getDestinationPath() );
         System.out.println( "File transfer passed -- again" );
         showCopyResult( res );
 
 
         System.out.println( "\nOkay, all done. Cleaning up!" );
         System.out.println( "\t* Destroying Slice" );
         destroySlice( sc );
 
         if(! transferBean.isDisableDelegation() ) {
             System.out.println( "\t* Destroying delegate" );
             DelegationAux.destroyDelegationEPR( delegatEPR );
         }
         System.out.println( "Done." );
     }
 
     
 
     public SliceInOutClient( ) {
 
     }
 
     public SliceInOutClient( Properties prop ) {
         this();
         setProperties( prop );
     }
 
 
     public SliceInOutClient( String propFileName ) {
         this();
         setProperties( propFileName );
     }
 
     private void setProperties( Properties prop ) {
         creationBean = new SliceCreationBean();
         creationBean.setProperties( prop );
         
         transferBean = new FileTransferBean( );
         transferBean.setProperties( prop );
     }
 
 
     public SliceClient createSlice( ) throws URI.MalformedURIException, RemoteException {
 
         Calendar tt = new GregorianCalendar( );
         if( creationBean.hasLifeSpan( ) )
             tt.add( Calendar.MINUTE, (int) creationBean.getLifeSpan() );
         else
             tt.add( Calendar.MINUTE, 20 );
         long ssize;
         if( creationBean.hasSize() )
             ssize = creationBean.getSize();
         else
             ssize = (long) (20 * 1024 * Math.pow( 10, 3 ));
         return createSlice( tt, ssize );
     }
 
 
     public SliceClient createSlice( Calendar tt, long ssize ) throws URI.MalformedURIException, RemoteException {
 
         return getSubSpaceClient().createSlice( getSliceKindURI(), tt, ssize );
     }
 
 
     public SubspaceClient getSubSpaceClient() throws URI.MalformedURIException, RemoteException {
 
         if( subSpaceClient == null  ) {
             if( getSubSpaceQName() == null )
                 throw new IllegalStateException( "SubSpace URI is required" );
 
             subSpaceClient =  getDSpaceClient().findSubspace( getSubSpaceQName() );
 
         }
         return subSpaceClient;
     }
 
 
     public DSpaceClient getDSpaceClient() throws URI.MalformedURIException, RemoteException {
 
         if( dspaceClient == null ) {
             if( getDspaceURI() == null || getDspaceURI().trim().equals( "" ) )
                 throw new IllegalStateException( "dspace URI is required" );
 
             dspaceClient = new DSpaceClient( getDspaceURI() );
         }
 
         return dspaceClient;
     }
 
 
     public String queryDMSVersion ( ) throws URI.MalformedURIException, RemoteException {
         return (String) getDSpaceClient().callMaintenanceAction( ".sys.ReadGNDMSVersion ", null );
     }
 
     public void destroySlice( SliceClient sl ) throws RemoteException {
         sl.setTerminationTime( new GregorianCalendar( ) );
     }
 
 
     // timeout in seconds
     public boolean waitForSliceDestruction( SliceClient sl, int timeout, boolean verbose )  {
 
         boolean dest=false;
         if( verbose ) System.out.println( "Waiting for slice removal (this may take a while): " );
         String rs;
         for( int i=0; i < timeout && !dest; ++i ) {
             if( verbose ) System.out.print( i+1 + " " );
             try {
                 rs = sl.getSliceKind();
                 if( rs == null || "".equals( rs.trim() ) ) {
                     if( verbose ) System.out.println( "rs is empty..." );
                     dest=true;
                 }
                 try {
                     Thread.sleep( 1000 );
                 } catch ( InterruptedException e ) {
                     e.printStackTrace();
                 }
             } catch ( RemoteException e ) {
                 dest=true;
                 if( verbose ) System.out.println( "\nSource slice removed" );
             }
         }
         return dest;
     }
 
     
     public void setProperties( String fn ) {
 
         // read property file
         InputStream f = null;
         RuntimeException exc = null;
         try {
             f = new FileInputStream( fn );
             Properties prop = new Properties( );
             prop.load( f );
             setProperties( prop );
         } catch ( FileNotFoundException e ) {
             exc =  new RuntimeException( "Failed to load properties file " + fn, e );
         } catch ( IOException e ) {
             exc =  new RuntimeException( "Failed to read properties from file " + fn, e );
         } finally {
             if( f != null )
                 try {
                     f.close( );
                 } catch ( IOException e ) {
                     RuntimeException re =
                         new RuntimeException( "Failed to close properties file " + fn, e );
                     if( exc != null )
                         re.initCause( exc );
                     throw re;
                 }
             if( exc != null )
                 throw exc;
         }
     }
 
 
     public static void showCopyResult( FileTransferResultT res ) throws Exception {
 
         MessageElement[] mes = res.get_any();
         StringWriter sw = new StringWriter( );
         for( MessageElement me : mes )
             sw.write( "    " + ( (String) me.getObjectValue( String.class ) ) + '\n' ) ;
 
         System.out.println( "Copied the following file(s):\n" + sw.toString() );
     }
 
 
     public String getSliceKindURI() {
         return creationBean.getSliceKindURI();
     }
 
 
     public ImmutableScopedName getSubSpaceQName() {
         return creationBean.getSubSpaceScopedName();
     }
 
 
     public String getDspaceURI() {
         return creationBean.getDspaceURI();
     }
 
 
     public SliceCreationBean getCreationBean() {
         return creationBean;
     }
 
 
     public void setCreationBean( SliceCreationBean creationBean ) {
         this.creationBean = creationBean;
     }
 
 
     public String getGorfxURI() {
         return transferBean.getGorfxURI();
     }
 
 
     public String getSourcePath() {
         return transferBean.getSourceAddress();
     }
 
 
     public String getDestinationPath() {
         return transferBean.getDestinationAddress();
     }
 
 
     public FileTransferBean getTransferBean() {
         return transferBean;
     }
 
 
     public void setTransferBean( FileTransferBean transferBean ) {
         this.transferBean = transferBean;
     }
 
 
     public static void main( String[] args ) throws Exception {
 
         SliceInOutClientApp app = new SliceInOutClientApp();
         app.run( args );
 
         System.exit( 0 );
     }
 
 
     public static class SliceInOutClientApp extends AbstractApplication {
 
         @Option( name="-p", required=true, usage="", metaVar="property-file" )
         protected String props;
         @Option( name="-e", required=false, usage="Creates an example \"propertiy-file\".\nWARNING the given file will be overwritten" )
         protected boolean example;
 
         public void run() throws Exception {
             // not required here
             if( example ) {
                 Properties prop = new Properties( );
                 (new SliceCreationBean() ).createExampleProperties( prop );
                 (new FileTransferBean() ).createExampleProperties( prop );
                 storeProperties( prop, props );
             } else {
                 if( verifyProps( props ) ) {
                     SliceInOutClient c = new SliceInOutClient( props );
                     c.run();
                 }
             }
         }
 
 
         private boolean verifyProps( String fn ) {
 
             InputStream f = null;
             RuntimeException exc = null;
             Properties prop = new Properties( );
 
             try {
                 f = new FileInputStream( fn );
                 prop.load( f );
             } catch ( FileNotFoundException e ) {
                 exc =  new RuntimeException( "Failed to load properties file " + fn, e );
             } catch ( IOException e ) {
                 exc =  new RuntimeException( "Failed to read properties from file " + fn, e );
             } finally {
                 if( f != null )
                     try {
                         f.close( );
                     } catch ( IOException e ) {
                         RuntimeException re =
                             new RuntimeException( "Failed to close properties file " + fn, e );
                         if( exc != null )
                             re.initCause( exc );
                         throw re;
                     }
                 if( exc != null )
                     throw exc;
             }
 
             
             ArrayList<String> bad = new ArrayList<String>( 0 );
             for( Object k: prop.keySet() ) {
                 String s = String.class.cast( k );
                 String v = prop.getProperty( s );
                 if( v.contains( "<" ) || v.contains( ">" ) )
                     bad.add( s );
             }
 
             if( bad.size() > 0 ) {
                 printHintMessage( fn, bad );
                 return false;
             }
 
             return true;
         }
 
 
         protected void printHintMessage( String fn, List<String> bad ) {
             System.out.println( "\nYour properties-file \""+fn+"\" contains placeholders for the following "
                 +"properties:" );
             for ( String s : bad ) {
                 System.out.println( "\t"+s );
             }
             System.out.println( "\nPlease fix them and try again." );
         }
 
 
         public void storeProperties( Properties prop, String fn ) {
 
             // read property file
             OutputStream f = null;
             RuntimeException exc = null;
             try {
                 f = new FileOutputStream( fn );
                 prop.store( f, "SliceInOutClient example properties." );
             } catch ( FileNotFoundException e ) {
                 exc =  new RuntimeException( "Failed to open properties file " + fn, e );
             } catch ( IOException e ) {
                 exc =  new RuntimeException( "Failed to store properties to file " + fn, e );
             } finally {
                 if( f != null )
                     try {
                         f.close( );
                     } catch ( IOException e ) {
                         if( exc != null )
                             throw new FinallyException( "Failed to close properties file " + fn, exc, e );
 
                         throw new RuntimeException( "Failed to close properties file " + fn, e );
                     }
                 if( exc != null )
                     throw exc;
             }
         }
     }
 }
