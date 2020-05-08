 package de.zib.gndmc.DSpace.tests;
 
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
 
 
 
 import de.zib.gndms.dspace.client.DSpaceClient;
 import de.zib.gndms.dspace.slice.client.SliceClient;
 import de.zib.gndms.dspace.slice.common.SliceConstants;
 import de.zib.gndms.dspace.slice.stubs.types.SliceReference;
 import de.zib.gndms.dspace.subspace.client.SubspaceClient;
 import de.zib.gndms.dspace.subspace.stubs.types.SubspaceReference;
 import de.zib.gndms.gritserv.delegation.DelegationAux;
 import org.apache.axis.message.MessageElement;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.axis.message.addressing.ReferencePropertiesType;
 import org.apache.axis.types.URI;
 import org.globus.gsi.GlobusCredential;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 import types.ContextT;
 
 import javax.xml.namespace.QName;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 
 /**
  * Yet another test client
  *
  * This test client does unit testing on the dspace entities.
  * It expects a dspace which is set up using the default setup-dataprovider.sh script. Also setup-testsubspace.sh should
  * be executed befor running this test.
  * If there are some more subspaces or the like, some tests may trigger assertions even so they are correct.
  *
  *
  * @author  try ma ik jo rr a zib
  * @version  $Id$
  * <p/>
  * User: mjorra, Date: 27.10.2008, Time: 17:39:54
  */
 public class Yatc {
 
     private static final String[] C3GRID_SLICE_KINDS =
         {"http://www.c3grid.de/G2/SliceKind/Staging", "http://www.c3grid.de/G2/SliceKind/DMS"};
     private static final String[] PTGRID_SLICE_KINDS =
         {"http://www.ptgrid.de/G1/SliceKind/Staging_RW", "http://www.c3grid.de/G2/SliceKind/DMS"};
     private static String[] SLICE_KINDS = PTGRID_SLICE_KINDS;
 
     private String dspaceURI;
     // c3grid workspace
     // private static final String SCHEMA_URI = "http://www.c3grid.de/G2/Subspace";
     // ptgrid workspace
     private static final String SCHEMA_URI = "http://www.ptgrid.de/G1/Subspace";
     private String localName;
     private String scopeName;
     private String gsiPath;
     private DSpaceClient client;
     private SliceClient slice;
     // some slice test values required in different methods
     private GregorianCalendar tt;
     private long ssize;
     private String skuri;
 
     private boolean useDelegation = true;
 
 
     // Params should be self-explanatory. gsiPath denotes the public grid ftp location
     // of the provided workspace. And scope- resp. localName are the address of the test
     // subspace.
     @Parameters( { "dspaceURI", "scopeName", "localName", "gsiPath" } )
     public Yatc( String dspaceURI, String scopeName, String localName, String gsiPath ) {
         this.dspaceURI = dspaceURI;
         this.scopeName = scopeName;
         this.localName = localName;
         this.gsiPath = gsiPath;
     }
 
 
     @BeforeClass( groups={ "dspace", "subspace" } )
     public void beforeTest( ) throws Exception {
         client = new DSpaceClient( dspaceURI );
 
         if( useDelegation ) {
             // with delegation
            String delfac = DelegationAux.createDelegationAddress( dspaceURI );
             GlobusCredential credential = DelegationAux.findCredential( DelegationAux.defaultProxyFileName( "1000" ) );
             EndpointReferenceType epr = DelegationAux.createProxy( delfac, credential );
             DelegationAux.addDelegationEPR( new ContextT(), epr );
             client.setProxy( credential );
         }
     }
 
 
 
     @Parameters( { "noSubspaces", "noSchemas"} )
     @Test( groups={ "dspace"  } ) 
     public void runDspaceTests( int noSubspaces, int noSchemas ) throws Exception {
 
         System.out.println( ">>> Performing dspace tests" );
 
         System.out.println( "checking listSupportedSchemas()" );
         URI[] uris = client.listSupportedSchemas();
 
         Assert.assertEquals ( uris.length, noSchemas, "length of result" );
         URI suri = new URI( SCHEMA_URI );
         Assert.assertEquals ( uris[0], suri, "expected value?" );
 
         System.out.println( "\nChecking listPublicSubspaces() and getSubspace( )" );
         SubspaceReference[] srefs = client.listPublicSubspaces( suri );
         Assert.assertEquals ( srefs.length, noSubspaces, "length of result"  );
 
         SubspaceClient sc = client.findSubspace( scopeName, localName ); // findSubspace uses getSubspace
 
         Assert.assertEquals ( srefs[0].getEndpointReference().toString(), sc.getEndpointReference( ).toString(), "equal subspace eprs?" );
     }
 
 
     @Parameters( { "noSliceKinds"} )
     @Test( dependsOnGroups={ "dspace" }, groups={"subspace"} )
     public void runSubspaceTests( int noSliceKinds ) throws Exception {
 
         System.out.println( "\n>>> Performing dspace.subspace tests" );
         SubspaceClient subc = client.findSubspace( scopeName, localName ); // findSubspace uses getSubspace
 
         System.out.println( "checking listCreatableSliceKinds()" );
         URI[] sks = subc.listCreatableSliceKinds();
         Assert.assertEquals ( sks.length, noSliceKinds, "slice kind count" );
         URI sk1 = new URI(  SLICE_KINDS[0] );
         List<URI> uris = Arrays.asList( sks );
         Assert.assertTrue( uris.contains( sk1 ), "Contains "+ sk1 + "?" );
         // URI sk2 = new URI(  SLICE_KINDS[1] );
         // assertTrue( uris.contains( sk2 ), "Contains "+ sk2 + "?" );
 
         System.out.println( "\nChecking createSlice()" );
 
         skuri = SLICE_KINDS[1];
         tt = new GregorianCalendar( );
         tt.add( Calendar.YEAR, 20 );
         ssize = (long) (20 * 1024 * Math.pow( 10, 3 ));
         slice = subc.createSlice( skuri, tt, ssize );
         sliceTests( slice, gsiPath, scopeName, localName );
         analyseEPR( slice.getEndpointReference() );
     }
 
 
     @Parameters( { "testScopeName", "testLocalName", "testGsiPath" } )
     @Test( dependsOnGroups={ "subspace" }, groups={"slice"} )
     public void runSliceTests(  String testScopeName, String testLocalName, String testGsiPath ) throws Exception {
 
         System.out.println( "\n>>> Performing dspace.slice tests" );
         System.out.println( "checking transformSlice( SliceKind )" );
 
         skuri = SLICE_KINDS[0];
         SliceReference sref = slice.transformSliceTo( skuri, null );
         SliceClient sc = new SliceClient( sref.getEndpointReference() );
         sliceTests( sc, gsiPath, scopeName, localName );
 
 
         boolean dest=false;
        // slice.setTerminationTime( new GregorianCalendar( ) );
         System.out.println( "Waiting for slice removal (this may take a while): " );
         String rs;
         for( int i=0; i < 120 && !dest; ++i ) {
             System.out.print( i+1 + " " );
             try {
                 rs = slice.getSliceKind();
                 if( rs == null || "".equals( rs.trim() ) ) {
                     System.out.println( "rs is empty..." );
                     dest=true;
                 }
             } catch ( RemoteException e ) {
                 dest=true;
                 System.out.println( "\nSource slice removed" );
             }
             Thread.sleep( 1000 );
         }
 
         if( ! dest ) {
             System.out.println( "Transformed slice still exists." );
             Assert.assertTrue( dest, "slice destruction" );
         }
 
         System.out.println( "\n>>> Performing dspace.slice tests" );
         System.out.println( "checking transformSlice( Subspace )" );
         sref = sc.transformSliceTo( new QName( testScopeName, testLocalName ) , null );
         slice = new SliceClient( sref.getEndpointReference() );
         sliceTests( slice, testGsiPath, testScopeName, testLocalName );
 
 
         System.out.println( "checking transformSlice( SliceTypeSpecifierSubspace )" );
         skuri = SLICE_KINDS[1];
         sref = slice.transformSliceTo( skuri, new QName( scopeName, localName ), null );
         sc = new SliceClient( sref.getEndpointReference() );
         sliceTests( sc, gsiPath, scopeName, localName );
     }
 
 
     private void sliceTests( SliceClient sc, String gp, String sn, String ln ) throws Exception {
         System.out.println( "Check slice values" );
         Assert.assertEquals(  skuri, sc.getSliceKind( ), "slicekind" );
         Assert.assertEquals(  tt, sc.getTerminationTime(), "terminationtime" );
         Assert.assertEquals(  ssize, sc.getTotalStorageSize(), "storage size" );
         String loc = sc.getSliceLocation();
         Assert.assertTrue(  loc.startsWith( gp ), "partial location check" );
         SubspaceClient ssref = sc.getSubspace();
         SubspaceClient subc = client.findSubspace( sn, ln );
         Assert.assertEquals(  ssref.getEndpointReference().toString( ),
             subc.getEndpointReference().toString( ),
             "Subspace epr"
         );
 
     }
 
     private void analyseEPR( EndpointReferenceType epr ) throws Exception {
 
         ReferencePropertiesType ept = epr.getProperties();
         MessageElement me = ept.get( SliceConstants.RESOURCE_KEY );
         String s = ( String ) me.getObjectValue( String.class );
         System.out.println( s );
     }
 }
