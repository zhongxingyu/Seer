 /*
  * Copyright 2010 Andreas Brauchli, René Buffat, Florian Widmer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.footware.shared.dto;
 
 import java.util.List;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 public class TrackVisualizationDTO implements IsSerializable {
     
     public static String TYPE_SPEED = "Speed plot";
     public static String TYPE_ELEVATION = "Elevation plot";
     
     private String type;
     private String xUnit;
     private String yUnit;
     
     private List<TrackVisualizationPointDTO> data;
     
     public TrackVisualizationDTO() {
         // TODO Auto-generated constructor stub
     }
     
     public TrackVisualizationDTO(List<TrackVisualizationPointDTO> data, String type, String xUnit, String yUnit) {
         this.data = data;
         this.type = type;
         this.xUnit = xUnit;
         this.yUnit = yUnit;
     }
 
     /**
      * @return the xUnit
      */
     public String getxUnit() {
         return xUnit;
     }
 
     /**
      * @param xUnit the xUnit to set
      */
     public void setxUnit(String xUnit) {
         this.xUnit = xUnit;
     }
 
     /**
      * @return the yUnit
      */
     public String getyUnit() {
         return yUnit;
     }
 
     /**
      * @param yUnit the yUnit to set
      */
     public void setyUnit(String yUnit) {
         this.yUnit = yUnit;
     }
 
     /**
      * @return the type
      */
     public String getType() {
         return type;
     }
 
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         this.type = type;
     }
 
     /**
      * @return the data
      */
     public List<TrackVisualizationPointDTO> getData() {
         return data;
     }
 
     /**
      * @param data the data to set
      */
     public void setData(List<TrackVisualizationPointDTO> data) {
         this.data = data;
     }
 
 }
