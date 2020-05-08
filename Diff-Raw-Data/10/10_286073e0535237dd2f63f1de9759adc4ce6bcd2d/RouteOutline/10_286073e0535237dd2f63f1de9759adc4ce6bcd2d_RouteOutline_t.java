 /**
  * Copyright (c) 2012 Busko Trust
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.busko.routemanager.model.transit.community;
 
 import org.busko.routemanager.model.Displayable;
 import org.busko.routemanager.model.transit.gtfs.Agency;
 import org.busko.routemanager.model.transit.gtfs.Route;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.util.Date;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord(finders = { "findRouteOutlinesByRoute" })
 public class RouteOutline implements Displayable {
 
     @Size(max = 256)
     private String username;
 
     @NotNull
    @Size(max = 30)
     private String routeName;
 
     @Size(max = 1000)
     private String routeDescription;
 
     @Lob
     @Basic(fetch = FetchType.LAZY)
     private byte[] fileContent = null;
 
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "MS")
     private Date submittedDateTime;
 
     @ManyToOne
     private Agency agency;
 
     @ManyToOne
     private Route route;
 
     @Transient
     private CommonsMultipartFile fileData;
 
     public void uploadFileData() {
         if (fileData == null) return;
         fileContent = fileData.getBytes();
     }
 
     public Route createAndAssociateNewRoute() {
        String routeId = routeName.length() > 20 ? routeName.substring(0, 20) : routeName;
         route = new Route();
        route.setRouteId(routeId);
         route.setRouteShortName(routeName);
         route.setRouteType(Route.ROUTETYPE_BUS);
        route.setUniqueEncoding(routeId);
         route.setLive(false);
         if (agency != null) {
             route.setAgency(agency);
         }
         setRoute(route);
         return route;
     }
 
     @Override
     public String getDisplayName() {
         return toString();
     }
 }
