 package org.penguin.kayako;
 
 import org.penguin.kayako.domain.TicketType;
 import org.penguin.kayako.domain.TicketTypeCollection;
 import org.penguin.kayako.exception.ApiRequestException;
 import org.penguin.kayako.exception.ApiResponseException;
 
 import java.util.List;
 
 /**
 * Wrapper for any API calls specific to ticket types.
  *
  * @author fatroom
  */
 public class TicketTypeConnector extends AbstractConnector {
 
     protected TicketTypeConnector(final KayakoClient client) {
         super(client);
     }
 
     /**
      * Retrieve a list of all the ticket types in the help desk.
      *
      * @return An collection of types known in system
      * @throws ApiResponseException A wrapped exception of anything that went wrong when handling the response from kayako
      * @throws ApiRequestException  A wrapped exception of anything that went wrong sending the request to kayako
      */
     public List<TicketType> list() throws ApiRequestException, ApiResponseException {
         return getApiRequest()
                 .get()
                 .as(TicketTypeCollection.class)
                 .getTypes();
     }
 
     /**
      * Retrieve the ticket type identified by id.
      *
      * @param typeId identifier
      * @return An collection of with requested types
      * @throws ApiResponseException A wrapped exception of anything that went wrong when handling the response from kayako
      * @throws ApiRequestException  A wrapped exception of anything that went wrong sending the request to kayako
      */
     public List<TicketType> forId(final int typeId) throws ApiRequestException, ApiResponseException {
         return getApiRequest()
                 .withPathRaw(String.valueOf(typeId))
                 .get()
                 .as(TicketTypeCollection.class)
                 .getTypes();
     }
 
     @Override
     protected ApiRequest getApiRequest() {
         ApiRequest request = super.getApiRequest();
         return request
                 .withPath("Tickets")
                 .withPath("TicketType");
     }
 }
