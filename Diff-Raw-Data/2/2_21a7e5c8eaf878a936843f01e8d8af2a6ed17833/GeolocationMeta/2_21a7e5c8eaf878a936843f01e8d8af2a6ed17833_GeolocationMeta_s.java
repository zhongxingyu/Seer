 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
package org.icefaces.mobi.component.camera;
 
 
 import org.icefaces.ace.meta.annotation.ClientBehaviorHolder;
 import org.icefaces.ace.meta.annotation.ClientEvent;
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIComponentBaseMeta;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 
 @Component(
         tagName = "geolocation",
         componentClass = "org.icefaces.mobi.component.geolocation.Geolocation",
         rendererClass = "org.icefaces.mobi.component.geolocation.GeolocationRenderer",
         generatedClass = "org.icefaces.mobi.component.geolocation.GeolocationBase",
         componentType = "org.icefaces.Geolocation",
         rendererType = "org.icefaces.GeolocationRenderer",
         extendsClass = "javax.faces.component.UIComponentBase",
         componentFamily = "org.icefaces.Geolocation",
         tlddoc = "This mobility component captures an geolocation object" +
                 " of longitude and latitude, heading, speed and altitude via" +
                 " html5 navigator api"
 )
 @ResourceDependencies({
         @ResourceDependency(library = "org.icefaces.component.util", name = "component.js")
 })
 @ClientBehaviorHolder(events = {
 	@ClientEvent(name="activate", javadoc="...", tlddoc="...", defaultRender="@this", defaultExecute="@all")
 }, defaultEvent="activate")
 public class GeolocationMeta extends UIComponentBaseMeta {
 
     @Property(tlddoc = "latitude of mobile device in decimal degrees")
     private Double latitude;
 
     @Property(tlddoc = "longitude of mobile device in decimal degrees")
     private Double longitude;
     
     @Property(tlddoc = "altitude of mobile device in meters")
     private Double altitude;
 
     @Property(tlddoc = "direction of mobile device in degrees from North")
     private Double direction;
 
     @Property(defaultValue = "false",
             tlddoc = "When disabled, geolocation is not activated")
     private boolean disabled;
 
     @Property(tlddoc = "tabindex of the component")
     private Integer tabindex;
 
     @Property(tlddoc = "style will be rendered on the root element of this " +
             "component.")
     private String style;
 
     @Property(tlddoc = "style class will be rendered on the root element of " +
             "this component.")
     private String styleClass;
 
     @Property(defaultValue = "false",
             tlddoc = "When singleSubmit is true, changing the value of this component" +
                     " will submit and execute this component only. Equivalent to " +
                     " execute=\"@this\" render=\"@all\" of the f ajax tag. " +
                     "When singleSubmit is false, no submit occurs. The value is simply  " +
                     "stored in the hidden field. The default value is false.")
     private boolean singleSubmit;
 
 }
