 package com.amee.base.resource;
 
import com.amee.base.validation.ValidationException;

 public interface ResourceHandler<E> {
 
    public E handle(RequestWrapper requestWrapper) throws ValidationException;
 }
