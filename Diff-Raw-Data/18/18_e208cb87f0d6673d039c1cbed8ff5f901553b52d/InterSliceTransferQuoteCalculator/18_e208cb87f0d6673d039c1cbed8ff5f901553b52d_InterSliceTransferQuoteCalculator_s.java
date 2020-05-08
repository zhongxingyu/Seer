 package de.zib.gndms.taskflows.interslicetransfer.server.logic;
 
 /*
  * Copyright 2008-${YEAR} Zuse Institute Berlin (ZIB)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 
 import de.zib.gndms.common.model.FileStats;
 import de.zib.gndms.common.model.gorfx.types.Quote;
 import de.zib.gndms.common.rest.UriFactory;
 import de.zib.gndms.gndmc.dspace.SliceClient;
 import de.zib.gndms.logic.model.gorfx.taskflow.UnsatisfiableOrderException;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.taskflows.filetransfer.server.logic.AbstractTransferQuoteCalculator;
 import de.zib.gndms.taskflows.interslicetransfer.client.model.InterSliceTransferOrder;
 import org.globus.ftp.exception.ClientException;
 import org.globus.ftp.exception.ServerException;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 
 import javax.inject.Inject;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.List;
 
 /**
  * @author  try ma ik jo rr a zib
  * @version  $Id$
  * <p/>
  * User: mjorra, Date: 04.11.2008, Time: 15:37:13
  */
 public class InterSliceTransferQuoteCalculator extends
     AbstractTransferQuoteCalculator<InterSliceTransferOrder> {
 
     private SliceClient sliceClient;
 
     public InterSliceTransferQuoteCalculator() {
     }
 
 
     public List<Quote> createQuotes() throws ServerException, IOException, ClientException, URISyntaxException {
 
         prepareSourceUrl();
         
         return super.createQuotes();
     }
 
 
     @Override
     public boolean validate() {
         prepareSourceUrl();
         return super.validate();  // not required here
     }
 
 
     protected void prepareSourceUrl() {
 
         prepareSourceUrl( getOrder(), sliceClient );
     }
 
 
     /**
      * Prepares the source url
      *
      * If the url is present in the order this does nothing else
      * it fetches the GridFTP-url for source slice.
      *
      * @param order An order delegate with the interSlice transfer order.
      * @param sliceClient A slice-client with a valid rest template instance.
      */
     public static void prepareSourceUrl( DelegatingOrder<InterSliceTransferOrder> order,
                                          SliceClient sliceClient ) {
 
         InterSliceTransferOrder ist = order.getOrderBean();
         if( ist.getSourceURI() == null  ) {
            final ResponseEntity<String> responseEntity =
                    sliceClient.getGridFtpUrl( ist.getSourceSlice(), order.getDNFromContext() );
            if( HttpStatus.OK.equals( responseEntity.getStatusCode() ) )
                ist.setSourceURI( responseEntity.getBody() );
            else
                throw new UnsatisfiableOrderException( "Invalid source slice specifier" );
         }
     }
 
 
     public SliceClient getSliceClient() {
 
         return sliceClient;
     }
 
 
     @Inject
     public void setSliceClient( final SliceClient sliceClient ) {
 
         this.sliceClient = sliceClient;
     }
 }
