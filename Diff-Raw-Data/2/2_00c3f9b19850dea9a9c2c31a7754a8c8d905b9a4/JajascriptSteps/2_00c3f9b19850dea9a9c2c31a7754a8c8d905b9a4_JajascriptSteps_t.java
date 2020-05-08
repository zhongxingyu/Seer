 package org.diveintojee.codestory2013.steps;
 
 import com.google.common.collect.ImmutableMap;
 
 import com.sun.jersey.api.client.ClientResponse;
 
 import org.apache.commons.codec.EncoderException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.diveintojee.codestory2013.jajascript.JajascriptResource;
 import org.diveintojee.codestory2013.jajascript.Rent;
 import org.diveintojee.codestory2013.jajascript.UppercasePropertyNamingStrategy;
 import org.jbehave.core.annotations.Named;
 import org.jbehave.core.annotations.When;
 import org.jbehave.core.model.ExamplesTable;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 /**
  * @author louis.gueye@gmail.com
  */
 public class JajascriptSteps extends BackendBaseSteps {
 
     public JajascriptSteps(Exchange exchange) {
         super(exchange);
     }
 
     @Override
     public Map<String, String> actualRow(ClientResponse clientResponse) {
         ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
         builder.put("code", String.valueOf(clientResponse.getStatus()));
         builder.put("body", String.valueOf(clientResponse.getEntity(String.class)));
         return builder.build();
     }
 
    @When("the server is asked to optimize the provided payload: $payload")
     public void askQuestion(@Named("payload") ExamplesTable payloadAsTable)
             throws EncoderException, IOException {
         this.exchange.getRequest().setType(MediaType.APPLICATION_JSON);
         this.exchange.getRequest().setRequestedType(MediaType.APPLICATION_JSON);
         final
         String
                 uri =
                 UriBuilder.fromResource(JajascriptResource.class).path("optimize")
                         .build().toString();
         this.exchange.getRequest().setUri(uri);
         Rent[] payload = fromPayLoadAsTable(payloadAsTable);
         ObjectMapper objectMapper = new ObjectMapper();
         objectMapper.setPropertyNamingStrategy(new UppercasePropertyNamingStrategy());
         this.exchange.getRequest().setBody(objectMapper
                 .writeValueAsString(payload));
         this.exchange.sendPostRequest();
     }
 
     private Rent[] fromPayLoadAsTable(ExamplesTable payloadAsTable) {
         Rent[] rents = new Rent[payloadAsTable.getRowCount()];
         for (int i = 0; i < payloadAsTable.getRowCount(); i++) {
             Map<String, String> row = payloadAsTable.getRow(i);
             final String vol = row.get("vol");
             final String depart = row.get("depart");
             final String duree = row.get("duree");
             final String prix = row.get("prix");
             Rent rent = new Rent(vol,
                     Integer.valueOf(depart),
                     Integer.valueOf(duree),
                     Long.valueOf(prix));
             rents[i] = rent;
         }
         return rents;
     }
 
 }
