 package org.brylex.barcoder.rest;
 
 import org.brylex.barcoder.Barcode;
 import org.brylex.barcoder.repository.BarcodeRepository;
 import org.brylex.barcoder.service.BarcodeService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLEncoder;
 
 @Component
 @Path("barcode")
 public class BarcodeResource
 {
     @Autowired
     private BarcodeService service;
 
     @Autowired
     private BarcodeRepository repository;
 
     @Context
     private UriInfo uriInfo;
 
     @PUT
     @Path("{url}")
     public Response create(@PathParam("url") URL url) throws Exception
     {
        URI uri = UriBuilder.fromUri(url.toURI()).path(".png").build();
 
        service.create(uri.toURL());
 
        String encoded = URLEncoder.encode(uri.toString(), "UTF-8");

        URI imageUri = uriInfo.getBaseUriBuilder().path("barcode").path("{uri}").buildFromEncoded(encoded);

        return Response.created(imageUri).type("image/x-png").build();
     }
 
     @GET
     @Path("{url}.png")
     @Produces("image/x-png")
    public Response get(@PathParam("url") URL url) throws Exception
     {
        URI uri = UriBuilder.fromUri(url.toURI()).path(".png").build();

        Barcode barcode = repository.getByUrl(uri.toURL());
         if (barcode == null)
         {
             return Response.status(Response.Status.NOT_FOUND).entity("There's no barcode available for URL '" + url + "'.").build();
         }
 
         return Response.ok(barcode.getBytes()).build();
     }
     
 }
