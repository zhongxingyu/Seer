 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.resources;
 
 import com.sun.jersey.api.NotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import javax.servlet.ServletContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.core.UriInfo;
 import org.apache.commons.lang3.Validate;
 import org.lafayette.server.Registry;
 import org.lafayette.server.ServerContextListener;
 import org.lafayette.server.domain.DomainObject;
 import org.lafayette.server.domain.Finders;
 import org.lafayette.server.http.MediaType;
 import org.lafayette.server.http.UriList;
 import org.lafayette.server.log.Log;
 import org.lafayette.server.log.Logger;
 import org.msgpack.MessagePack;
 
 /**
  * Common functionality for all resource classes.
  *
  * TODO Write tests for this class.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
abstract class BaseResource {
 
     /**
      * Logger facility.
      */
     protected final Logger log = Log.getLogger(this);
     /**
      * List of URIs available from a resource's index.
      */
     private final UriList indexUriList;
     /**
      * Used to format message pack format.
      */
     private final MessagePack msgpack = new MessagePack();
     /**
      * Servlet context injected by Jersey.
      */
     @Context private ServletContext servlet;
     /**
      * Security context injected by Jersey.
      */
     @Context private HttpHeaders headers;
     /**
      * URI info injected by Jersey.
      */
     @Context private UriInfo uriInfo;
     /**
      * Security context injected by Jersey.
      */
     @Context private SecurityContext security;
     /**
      * Finder factory.
      *
      * Lazy computed.
      */
     private Finders finders;
 
     /**
      * Dedicated constructor.
      */
     public BaseResource() {
         super();
         indexUriList = new UriList();
         indexUriList.setComment("# Available URIs:");
         init();
     }
 
     /**
      * Initialize resource.
      *
      * - call {@link #addUrisToIndexList(org.lafayette.server.http.UriList)}
      */
     private void init() {
         try {
             addUrisToIndexList(indexUriList);
         } catch (URISyntaxException ex) {
             log.fatal("Error adding index uris: %s", ex);
         }
     }
 
     /**
      * Add all URIs to all sub resources to the given list.
      *
      * @param list list to add URIs
      * @throws URISyntaxException if URI has bad syntax
      */
     protected abstract void addUrisToIndexList(UriList list) throws URISyntaxException;
 
     /**
      * Get object registry for shared objects.
      *
      * @return never {@code null}
      */
     protected Registry registry() {
         return (Registry) servlet.getAttribute(ServerContextListener.REGISRTY);
     }
 
     /**
      * Get the servlet context.
      *
      * @return never {@code null}
      */
     protected ServletContext servlet() {
         return servlet;
     }
 
     /**
      * Get the requests HTTP headers.
      *
      * @return never {@code null}
      */
     protected HttpHeaders headers() {
         return headers;
     }
 
     /**
      * Get the requests URI info.
      *
      * @return never {@code null}
      */
     protected UriInfo uriInfo() {
         return uriInfo;
     }
 
     /**
      * Get the requests security context.
      *
      * @return never {@code null}
      */
     protected SecurityContext security() {
         return security;
     }
 
     /**
      * Get domain object finders.
      *
      * @return never {@code null} and same instance
      */
     protected synchronized Finders finders() {
         if (null == finders) {
             finders = new Finders(registry().getMappers());
         }
 
         return finders;
     }
 
     /**
      * Create an 500 error response.
      *
      * @param message error message
      * @return never {@code null}
      */
     protected Response createServerErrorResponse(final String message) {
         return Response.serverError()
                 .entity(message)
                 .build();
     }
 
     /**
      * Create an 400 error response.
      *
      * @param message error message
      * @return never {@code null}
      */
     protected Response createClientErrorResponse(final String message) {
         return Response.status(Response.Status.BAD_REQUEST)
                 .entity(message)
                 .build();
     }
 
     /**
      * Throws a error exception with status code 500.
      *
      * @param message error message
      */
     protected void raiseServerError(final String message) {
         throw new WebApplicationException(createServerErrorResponse(message));
     }
 
     /**
      * Throws a error exception with status code 400.
      *
      * @param message error message
      */
     protected void raiseClientError(final String message) {
         throw new WebApplicationException(createClientErrorResponse(message));
     }
 
     /**
      * Throw a 404 error.
      *
      * @param resource resource name
      * @param id id of resource
      * @throws NotFoundException always
      */
     protected void raiseIdNotFoundError(final String resource, final String id) throws NotFoundException {
         final String message = String.format("Can't find '%s' with id '%s'!", resource, id);
         throw new NotFoundException(message);
     }
 
     /**
      * Throws a web application error for a missing request property.
      *
      * @param name of missing property
      * @throws WebApplicationException always
      */
     protected void raiseMissingPropertyError(final String name) throws WebApplicationException {
         final String message = String.format("Property '%s' missing!", name);
         throw new WebApplicationException(createServerErrorResponse(message));
     }
 
     /**
      * Returns message pack marshalled domain object.
      *
      * @param object to marshal, must not be {@code null}
      * @return message pack formated bytes
      */
     protected byte[] formatMessagePack(final DomainObject object) {
         Validate.notNull(object);
         try {
             return msgpack.write(object);
         } catch (IOException ex) {
             log.fatal("Can not marshall object to message pack: %s!", object);
             return new byte[] {};
         }
     }
 
     /**
      * Returns the resource index as plain text.
      *
      * @return never {@code null}
      */
     @GET
     @Produces(MediaType.TEXT_URI_LIST)
     public String indexAsUriList() {
         log.info("Respond with URI list for %s", servlet.getRealPath(""));
         return indexUriList.toString();
     }
 }
