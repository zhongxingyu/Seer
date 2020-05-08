 
 /***************************************************************************
  *   Copyright (C) 2010 iCoreTech research labs                            *
  *   Contributed code from:                                                *
  *   - Valerio Chiodino - keytwo at keytwo dot net                         *
  *   - Fabio Tunno      - fat at fatshotty dot net                         *
  *                                                                         *
  *   This program is free software: you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation, either version 3 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program. If not, see http://www.gnu.org/licenses/     *
  *                                                                         *
  ***************************************************************************/
 
 package fm.audiobox.core.models;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 
 import fm.audiobox.core.observables.Event;
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConnector;
 import fm.audiobox.interfaces.IEntity;
 
 
 /**
  * CoverUrls class class is one of the lighter models and offers defaults informations only.
  * 
  * <p>
  * 
  * CoverUrls XML looks like this: 
  * 
  * <pre>
  * @code
  * <covers> 
  *   <l>http://media.audiobox.fm/images/albums/HOBh1lMt/l.jpg?1277867432</l> 
  *   <m>http://media.audiobox.fm/images/albums/HOBh1lMt/m.jpg?1277867432</m> 
  *   <s>http://media.audiobox.fm/images/albums/HOBh1lMt/s.jpg?1277867432</s> 
  * </covers>
  * @endcode
  * </pre>
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  */
 public class ArtWork extends AbstractEntity implements Serializable {
 
   private static final long serialVersionUID = 1L;
 
   /** The XML tag name for the Album element */
   public static final String NAMESPACE = null;
   public static final String TAGNAME = "artwork|ar";
 
   private String large;
   private String medium;
   private String small;
  
  @SuppressWarnings("unused")
   private IEntity parent;
 
   /**
    * <p>Constructor for CoverUrls.</p>
    */
   public ArtWork(IConnector connector, IConfiguration config){
     super(connector, config);
   }
 
 
   @Override
   public String getTagName() {
     return TAGNAME;
   }
 
   @Override
   public String getNamespace(){
     return NAMESPACE;
   }
 
 
   /* ------------------- */
   /* Getters and setters */
   /* ------------------- */
 
   /**
    * This method is used by the parser. Do not use it directly.
    * 
    * <p>Setter for the field <code>l</code>.</p>
    *
    * @param large a {@link java.lang.String} object.
    */
   @Deprecated
   public void setLarge(String large) {
     this.large = large;
   }
 
   /**
    * <p>Getter for the field <code>l</code>.</p>
    *
    * @return the large
    */
   public String getLarge() {
     return this.large;
   }
 
 
   /**
    * This method is used by the parser. Do not use it directly.
    * 
    * <p>Setter for the field <code>m</code>.</p>
    *
    * @param medium a {@link java.lang.String} object.
    */
   @Deprecated
   public void setMedium(String medium) {
     this.medium = medium;
   }
 
   /**
    * <p>Getter for the field <code>m</code>.</p>
    *
    * @return the medium
    */
   public String getMedium() {
     return this.medium;
   }
 
 
   /**
    * This method is used by the parser. Do not use it directly.
    * 
    * <p>Setter for the field <code>s</code>.</p>
    *
    * @param small a {@link java.lang.String} object.
    */
   public void setSmall(String small) {
     this.small = small;
   }
 
   /**
    * <p>Getter for the field <code>s</code>.</p>
    *
    * @return the small
    */
   public String getSmall() {
     return this.small;
   }
 
 
   @Override
   public Method getSetterMethod(String tagName) throws SecurityException, NoSuchMethodException {
 
     if ( tagName.equals("l") ){
       return this.getClass().getMethod("setLarge", String.class);
 
     } else if ( tagName.equals("m") ){
       return this.getClass().getMethod("setMedium", String.class);
 
     } else if ( tagName.equals("s") ){
       return this.getClass().getMethod("setSmall", String.class);
 
     }
 
     return null;
   }
 
 
   @Override
   protected void copy(IEntity entity) {
     ArtWork artwork = (ArtWork) entity;
 
     this.large = artwork.getLarge();
     this.medium = artwork.getMedium();
     this.small = artwork.getSmall();
 
     this.setChanged();
     Event event = new Event( this, Event.States.ENTITY_REFRESHED );
     this.notifyObservers(event);
 
   }
 
 
   @Override
   public String getApiPath() {
     return null;
   }
 
   @Override
   public void setParent(IEntity parent) {
     this.parent = parent;
   }
 
 }
