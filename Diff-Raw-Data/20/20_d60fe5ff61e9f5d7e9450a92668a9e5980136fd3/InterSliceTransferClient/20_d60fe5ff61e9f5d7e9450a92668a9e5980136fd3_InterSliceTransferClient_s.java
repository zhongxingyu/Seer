 package de.zib.gndmc.GORFX;
 
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
 
 
 
 import de.zib.gndms.dspace.slice.client.SliceClient;
 import de.zib.gndms.dspace.subspace.client.SubspaceClient;
 import de.zib.gndms.dspace.client.DSpaceClient;
 import de.zib.gndms.kit.application.AbstractApplication;
 import de.zib.gndms.gritserv.typecon.GORFXClientTools;
 import de.zib.gndms.gritserv.typecon.GORFXTools;
 import de.zib.gndmc.GORFX.GORFXClientUtils;
 import org.apache.axis.message.MessageElement;
 import org.kohsuke.args4j.Option;
 import types.*;
 
 import javax.print.attribute.standard.DocumentName;
 import javax.xml.namespace.QName;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * @author  try ma ik jo rr a zib
  * @version  $Id$
  * <p/>
  * User: mjorra, Date: 04.11.2008, Time: 18:28:22
  */
 public class InterSliceTransferClient extends AbstractApplication {
 
     public static int MILLIS = 500;
     
     // do provider stagin
     // create new slice on different host
     // transfer staged data to created slice
 
     @Option( name="-uri", required=true, usage="URI of the gorfx service" )
     public String uri;
     @Option( name="-duri", required=true, usage="Destination dspace uri (where TransferTest subspace resides)" )
     public String duri;
     @Option( name="-dn", required=true, usage="Your grid DN" )
     public String dn;
     @Option( name="-props", required=true, usage="properties for the orq request" )
     public String props;
 
     @Option( name="-sk", metaVar="URI", required=false, usage="slice kind uri for target slice" )
     public String skuri="http://www.c3grid.de/G2/SliceKind/transfer";
 
 
     final private ContextT xsdContext = new ContextT();
 
     public void run() throws Exception {
 
         // Create reusable context with pseudo DN
         final ContextTEntry entry =
             GORFXTools.createContextEntry("Auth.DN", dn );
         xsdContext.setEntry(new ContextTEntry[] { entry });
 
         // setup delegation
         GORFXClientUtils.setupDelegation( xsdContext, uri, null );
 
 
         // perform staging
         SliceReference sr =
             GORFXClientUtils.doStageIn( uri, props, xsdContext, GORFXClientUtils.newContract(), MILLIS );
 
         // create a new Slice in the target subspace
         DSpaceClient dcnt = new DSpaceClient( duri );
         SubspaceClient subc =
             new SubspaceClient(
                 dcnt.getSubspace( new QName( "http://www.c3grid.de/G2/Subspace", "TransferSpace" ) ).getEndpointReference() );
 
         Calendar tt = new GregorianCalendar( );
        tt.add( Calendar.DAY, 1 );
         long ssize = (long) (20 * 1024 * Math.pow( 10, 3 ));
         SliceClient slice = subc.createSlice( skuri, tt, ssize );
         System.out.println( "DestSlice location: " + slice.getSliceLocation() );
 
         // prepare inter slice transfer
         InterSliceTransferORQT istorq =
             GORFXClientTools.createInterSliceTransferORQT( sr, new SliceReference( slice.getEndpointReference() ) );
 
         // fire up IST task
         InterSliceTransferResultT isrt =
             GORFXClientUtils.commonTaskExecution( "slice transfer", uri, istorq, xsdContext,
                 GORFXClientUtils.newContract(), MILLIS, InterSliceTransferResultT.class );
 
         // show result
         MessageElement[] mes = isrt.get_any( );
         ArrayList<String> al = new ArrayList( mes.length );
         for( int i=0; i < mes.length; ++i )
             al.add( (String) mes[i].getObjectValue( String.class ) );
         String[] fls = al.toArray( new String[al.size()] );
         System.out.println( "Copied files: " );
         for( String s: Arrays.asList( fls ) ) {
             System.out.println( "\t" + s );
         }
         System.exit( 0 );
 
     }
 
 
     public static void main( String[] args ) throws Exception {
 
         InterSliceTransferClient cnt = new InterSliceTransferClient();
         cnt.run( args );
     }
 }
