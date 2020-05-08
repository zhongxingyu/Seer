 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.data;
 
 /**
  * A dynamic object base class. This class provides a basic dynamic
  * data container (a dictionary) that can be used for simple
  * serialization of the object. It also enables containers and others
  * to store additional data in this object, breaking the concept of
  * encapsulation (for improved utility).
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public abstract class DynamicObject {
 
     /**
      * The dictionary key for the object type. The value stored is a
      * string and should be descriptive outside of the Java world.
      */
     public static final String KEY_TYPE = "type";
 
     /**
      * The dictionary containing the serializable data for this
      * object.
      */
     public Dict dict = new Dict();
 
     /**
      * Creates a new dynamic object. This constructor is used to
      * enforce that subclasses set the type key properly.
      *
      * @param type           the type name
      */
    public DynamicObject(String type) {
         dict.add(KEY_TYPE, type);
     }
 
     /**
      * Returns a serialized representation of this object. Used when
      * accessing the object from outside pure Java. By default this
      * method will return the contained dictionary.
      *
      * @return the serialized representation of this object
      */
     public Dict serialize() {
         return dict;
     }
 }
