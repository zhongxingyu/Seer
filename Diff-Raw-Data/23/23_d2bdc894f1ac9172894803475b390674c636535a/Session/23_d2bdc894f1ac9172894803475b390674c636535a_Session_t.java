 /*
  * Copyright 2012 Herald, Southeast University.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package cn.edu.seu.herald.session;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 /**
  * Provides a way to identify a user across more than one application request
  * or visit to any web application of herald and to store information
  * about that user.
  * <p>The session should be implemented accross any common language and platform,
  * by using the technology: XML and JSON etc.</p>
  * @author rAy <predator.ray@gmail.com>
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name = "session")
 public class Session implements Serializable {

     @XmlElement
     private String id;

     @XmlElement
     private long creationTime;

     @XmlElement
     private long lastAccessedTime;

     @XmlElement
     private String uri;

     @XmlElement
     @XmlJavaTypeAdapter(MapAdapter.class)
     private Map<String, Object> properties;

     private Session() {}

     /**
      * Creates a session by its unique id and creation time.
      * The session will be identified by the id
      * @param id the identifier of the session
      * @param creationTime the time when the session was created,
      * measured in milliseconds since midnight January 1, 1970 GMT.
      */
     public Session(String id, long creationTime) {
         this.id = id;
         this.creationTime = creationTime;
         this.lastAccessedTime = creationTime;
         this.properties = new LinkedHashMap<String, Object>();
     }
 
     /**
      * Returns the object bound with the specified name in this session,
      * or null if no object is bound under the name.
      * @param name a string specifying the name of the object
      * @return the object with the specified name
      */
     public Object getAttribute(String name) {
         return properties.get(name);
     }
 
     /**
      * Returns an Enumeration of String objects containing the names of all
      * the objects bound to this session.
      * @return an Enumeration of String objects specifying the names of all the objects
      * bound to this session
      */
     public Enumeration<String> getAttributeNames() {
         return Collections.enumeration(properties.keySet());
     }
 
     /**
      * Returns the time when this session was created,
      * measured in milliseconds since midnight January 1, 1970 GMT.
      * @return a long specifying when this session was created,
      * expressed in milliseconds since 1/1/1970 GMT
      */
     public long getCreationTime() {
         return creationTime;
     }
 
     /**
      * Returns a string containing the unique identifier assigned to
      * this session. The identifier is assigned by the core layer and is
      * implementation dependent.
      * @return a string specifying the identifier assigned to this session
      */
     public String getId() {
         return id;
     }

     public void setLastAccessedTime(long lastAccessedTime) {
         this.lastAccessedTime = lastAccessedTime;
     }
 
     /**
      * Returns the last time the client sent a request associated with this
      * session, as the number of milliseconds since midnight January 1,
      * 1970 GMT, and marked by the time the container received the request.
      * Actions that your application takes, such as getting or setting a value
      * associated with the session, do not affect the access time.
      * @return a long representing the last time the client sent a request associated
      * with this session, expressed in milliseconds since 1/1/1970 GMT
      */
     public long getLastAccessedTime() {
         return lastAccessedTime;
     }
 
     /**
      * Removes the object bound with the specified name from this session.
      * If the session does not have an object bound with the specified name, this method
      * does nothing.
      * @param name the name of the object to remove from this session
      */
     public void removeAttribute(String name) {
         properties.remove(name);
     }
 
     /**
      * Binds an object to this session, using the name specified.
      * If an object of the same name is already bound to the session, the object is
      * replaced.
      * @param name the name to which the object is bound; cannot be null
      * @param value the object to be bound
      */
     public void setAttribute(String name, Object value) {
         properties.put(name, value);
     }
 
     /**
      * Returns the URI to this session
      * @return the URI to this session
      */
     public String getUri() {
         return uri;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
 }
