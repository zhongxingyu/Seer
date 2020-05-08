 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import java.io.*;
 import org.wings.io.Device;
 
 /**
  * This class handles a HTTP GET Address that can be updated
  * with additional GET parameters.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class RequestURL extends SimpleURL
 {
     private static final byte[] _delimiter = "_".getBytes();
     private static final byte[] _ampStr    = "&amp;".getBytes();
     private static final byte[] _questMark = "?".getBytes();
     private static final String DEFAULT_RESOURCE_NAME = "_";
 
     private String baseParameters;
 
     private boolean hasQuestMark;
 
     private String epoch;
 
     private String resource;
 
     private StringBuffer parameters = null;
 
     /**
      * 
      */
     public RequestURL() { 
     }
 
     /**
     * copy constructor.
     */
    private RequestURL(RequestURL other) {
        this.baseParameters = other.baseParameters;
        this.hasQuestMark = other.hasQuestMark;
        this.epoch = other.epoch;
        this.resource = other.resource;
        StringBuffer params = other.parameters;
        parameters = (params == null) ? params : new StringBuffer(params.toString());
    }

    /**
      * 
      */
     public RequestURL(String baseURL, String encodedBaseURL) { 
         setBaseURL(baseURL, encodedBaseURL);
     }
 
     /**
      *
      */
     public void setEpoch(String e) {
         epoch = e;
     }
 
     /**
      *
      */
     public String getEpoch() {
         return epoch;
     }
 
     /**
      *
      */
     public void setResource(String r) {
         resource = r;
     }
 
     /**
      *
      */
     public String getResource() {
         return resource;
     }
 
     /**
      *
      */
     public void setBaseURL(String b, String encoded) {
         baseURL = b;
 
         baseParameters = encoded.substring(b.length());
         if ( baseParameters.length()==0 )
             baseParameters = null;
 
         if ( baseParameters!=null )
             hasQuestMark = baseParameters.indexOf('?')>=0;
         else
             hasQuestMark = false;
     }
 
 
     /**
      * Add an additional parameter to be included in the GET paramter
      * list. Usually, this paramter will be in the form 'name=value'.
      *
      * @param parameter to be included in the GET parameters.
      * @return a reference to <code>this</code> to simplify 'call chaining'
      */
     public RequestURL addParameter(String parameter) {
         if (parameter!=null) {
             if (parameters == null) 
                 parameters = new StringBuffer();
             else
                 parameters.append("&amp;");
             parameters.append(parameter);
         }
         return this;
     }
 
     /**
      * Add an additional name/value pair to be included in the GET paramter
      * list. The added parameter will be 'name=value'
      *
      * @param name  the name of the parameter
      * @param value the value of the parameter
      * @return a reference to <code>this</code> to simplify 'call chaining'
      */
     public RequestURL addParameter(String name, String value) {
         addParameter(name);
         parameters.append("=").append(value);
         return this;
     }
 
     /**
      * Add an additional name/value pair to be included in the GET paramter
      * list. The added name will be the encoded LowLevelEventId of the 
      * LowLevelEventListener.
      *
      * @param comp 
      * @param value the value of the parameter
      * @return a reference to <code>this</code> to simplify 'call chaining'
      */
     public RequestURL addParameter(LowLevelEventListener comp, String value) {
         addParameter(comp.getEncodedLowLevelEventId(), value);
 
         return this;
     }
 
     /**
      * Add an additional name/value pair to be included in the GET paramter
      * list. The added parameter will be 'name=value'
      *
      * @param name  the name of the parameter
      * @param value the value of the parameter
      * @return a reference to <code>this</code> to simplify 'call chaining'
      */
     public RequestURL addParameter(String name, int value) {
         addParameter(name);
         parameters.append("=").append(value);
         return this;
     }
 
     /**
      * clear all additional paramters given in the {@link #addParameter(String)} call.
      */
     public void clear() {
         if (parameters != null) {
             parameters.setLength(0);
         }
         setEpoch(null);
         setResource(null);
     }
 
     /**
      * Writes the context Address to the output Device. Appends all
      * parameters given. Only the context URL is given, since all GET urls generated
      * by wings are relative to the WingS servlet.
      * Tries to avoid charset conversion as much as possible by precalculating the
      * byteArray representation of the non-parameter part.
      *
      * @param d the Device to write to 
      */
     public void write(Device d) throws IOException {
         super.write(d);
 
         if (resource != null && epoch != null) {
             d.print(epoch);
             d.write(_delimiter);
         }
 
         if (resource != null) {
             d.print(resource);
         }
         else {
             /*
              * The default resource name. Work around a bug in some
              * browsers that fail to assemble URLs.
              * (TODO: verify and give better explanation here).
              */
             d.print( DEFAULT_RESOURCE_NAME );
         }
 
         if ( baseParameters!=null ) {
             d.print(baseParameters);
         }
 
         if (parameters != null && parameters.length() > 0) {
             d.write (hasQuestMark ? _ampStr : _questMark);
             d.print(parameters.toString());
         }
     }
 
     /**
      * Returns the string representation of the context URL plus
      * all paramters given.
      *
      * @return
      */
     public String toString() {
         StringBuffer erg = new StringBuffer();
 
         if ( baseURL!=null ) {
             erg.append(baseURL);
         }
 
         if ( resource!=null && epoch!=null ) {
             erg.append(epoch);
             erg.append("_");
         }
 
         if ( resource!=null ) {
             erg.append(resource);
         }
         else {
             erg.append( DEFAULT_RESOURCE_NAME );
         }
 
         if ( baseParameters!=null ) {
             erg.append(baseParameters);
         }
 
         if (parameters != null && parameters.length() > 0) {
             erg.append(hasQuestMark ? "&" : "?");
             erg.append(parameters.toString());
         }
 
         return erg.toString();
     }
 
     private final boolean eq(Object a, Object b) {
         return (a == b) || (a != null && a.equals(b));
     }
 
     public boolean equals(Object o) {
         if (o == null) return false;
         if (!super.equals(o)) return false;
         RequestURL other = (RequestURL) o;
         return (hasQuestMark == other.hasQuestMark
                 && eq(baseParameters, other.baseParameters)
                 && eq(epoch, other.epoch)
                 && eq(resource, other.resource)
                 && eq(parameters, other.parameters));
     }
 
     /** 
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
         return baseURL != null ? baseURL.hashCode() : 0;
     }
     
     /**
      * Deep copy.
      * @return object with cloned contents
      */
    public Object clone() {
        return new RequestURL(this);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
