 package com.sgcib.currencyconverter.converter;
 

import com.google.common.collect.ImmutableMap;

 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
import java.util.HashMap;
import java.util.Map;

 import static com.sgcib.currencyconverter.server.MediaType.APPLICATION_JSON;
 

 @Path("/convert")
 public class ConverterResource {
     private final CurrencyConverter currencyConverter;
 
     public ConverterResource() {
         this.currencyConverter = new CurrencyConverter();
     }
 
     @GET
     @Produces(APPLICATION_JSON)
     @Path("/{fromCurrency}/{toCurrency}")
     public Money convert(@DefaultValue("0") @QueryParam("amount") double amount,
                          @PathParam("fromCurrency") String fromCurrency,
                          @PathParam("toCurrency") String toCurrency) {
         return currencyConverter.convert(amount, fromCurrency,toCurrency);
     }
 
 }
