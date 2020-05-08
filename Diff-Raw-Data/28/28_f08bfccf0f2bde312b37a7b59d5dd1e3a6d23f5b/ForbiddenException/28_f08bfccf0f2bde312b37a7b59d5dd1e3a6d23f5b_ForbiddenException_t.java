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
 
 package org.lafayette.server.http;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 /**
  * Used to signal failed authorization.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class ForbiddenException extends WebApplicationException {
 
     public ForbiddenException() {
         this("");
     }
 
     public ForbiddenException(final String message) {
        super(Response.status(Response.Status.FORBIDDEN).entity(message).build());
     }
 
 }
