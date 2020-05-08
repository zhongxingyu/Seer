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
        service.create(url);
 
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(".png");
 
        return Response.created(builder.build()).type("image/x-png").build();
     }
 
     @GET
     @Path("{url}.png")
     @Produces("image/x-png")
    public Response get(@PathParam("url") URL url)
     {
        Barcode barcode = repository.getByUrl(url);
         if (barcode == null)
         {
             return Response.status(Response.Status.NOT_FOUND).entity("There's no barcode available for URL '" + url + "'.").build();
         }
 
         return Response.ok(barcode.getBytes()).build();
     }
     
 }
