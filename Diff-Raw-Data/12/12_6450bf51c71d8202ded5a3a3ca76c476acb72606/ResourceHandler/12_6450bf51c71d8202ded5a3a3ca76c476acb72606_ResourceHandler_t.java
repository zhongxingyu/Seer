 package com.amee.base.resource;
 
 public interface ResourceHandler<E> {
 
    public E handle(RequestWrapper requestWrapper);
 }
