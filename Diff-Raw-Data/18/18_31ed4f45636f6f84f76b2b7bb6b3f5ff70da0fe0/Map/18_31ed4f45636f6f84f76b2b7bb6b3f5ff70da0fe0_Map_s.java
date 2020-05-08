 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 package org.generationcp.middleware.pojos.gdms;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 
 /**
  * <b>Description</b>: POJO for Map table.
  * 
  * <br>
  * <br>
  * 
  * <b>Author</b>: Michael Blancaflor
  * <br>
  * <b>File Created</b>: Jul 9, 2012
  */
 @Entity
 @Table(name = "gdms_map")
 public class Map implements Serializable{
 
     private static final long serialVersionUID = 1803546446290398372L;
 
     @Id
     @Basic(optional = false)
     @Column(name = "map_id")
     private Integer mapId;
     
     @Basic(optional = false)
     @Column(name = "map_name")
     private String mapName;
     
     @Basic(optional = false)
     @Column(name = "map_type")
     private String mapType;
     
     @Column(name = "mp_id")
     private Integer mpId;
     
     private int markerCount;
     private Float maxStartPosition;
     private String linkageGroup;
     
     public Map() {}
 
     public static final String GET_MAP_DETAILS_BY_NAME = 
         "SELECT COUNT(DISTINCT gdms_mapping_data.marker_id) AS marker_count " +
     	"       , MAX(gdms_mapping_data.start_position) AS max " +
         "       , gdms_mapping_data.linkage_group AS Linkage_group " +
    	"       , gdms_mapping_data.map_name AS map " +
        "       , gdms_map.map_type AS map_type " +
     	"FROM gdms_mapping_data, gdms_map " +
         "WHERE gdms_mapping_data.map_id=gdms_map.map_id AND lower(gdms_mapping_data.map_name) LIKE (:nameLike) " +
         "GROUP BY gdms_mapping_data.linkage_group, gdms_mapping_data.map_name " +
         "ORDER BY gdms_mapping_data.map_name, gdms_mapping_data.linkage_group ";
        
     public static final String COUNT_MAP_DETAILS_BY_NAME = 
         "SELECT COUNT(DISTINCT gdms_mapping_data.linkage_group, gdms_mapping_data.map_name) " +
         "FROM `gdms_mapping_data` JOIN `gdms_map` ON gdms_mapping_data.map_id=gdms_map.map_id " +
         "WHERE lower(gdms_mapping_data.map_name) LIKE (:nameLike) ";
     
     public Map(Integer mapId, String mapName, String mapType, Integer mpId) {
         super();
         this.mapId = mapId;
         this.mapName = mapName;
         this.mapType = mapType;
         this.mpId = mpId;
     }
 
 
     public Map(Integer mapId) {
         this.mapId = mapId;
     }
     
     public Map(int markerCount, Float maxStartPosition, String linkageGroup, String mapName, String mapType) {
     	this.markerCount = markerCount;
     	this.maxStartPosition = maxStartPosition;
     	this.linkageGroup = linkageGroup;
     	this.mapName = mapName;
     	this.mapType = mapType;
     }
     
     public Integer getMapId() {
         return mapId;
     }
 
     
     public void setMapId(Integer mapId) {
         this.mapId = mapId;
     }
 
     
     public String getMapName() {
         return mapName;
     }
 
     
     public void setMapName(String mapName) {
         this.mapName = mapName;
     }
 
     
     public String getMapType() {
         return mapType;
     }
 
     
     public void setMapType(String mapType) {
         this.mapType = mapType;
     }
 
     
     public Integer getMpId() {
         return mpId;
     }
 
     
     public void setMpId(Integer mpId) {
         this.mpId = mpId;
     }
 
     public int getMarkerCount() {
     	return markerCount;
     }
     
     public void setMarkerCount(int markerCount) {
     	this.markerCount = markerCount; 
     }
     
     public Float getMaxStartPosition() {
     	return maxStartPosition;
     }
     
     public void setMaxStartPosition(Float maxStartPosition) {
     	this.maxStartPosition = maxStartPosition; 
     }
     
     public String getLinkageGroup() {
     	return linkageGroup;
     }
     
     public void setLinkageGroup(String linkageGroup) {
     	this.linkageGroup = linkageGroup; 
     }    
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
 
         if (obj instanceof Map) {
             Map param = (Map) obj;
             if (this.getMapId() == param.getMapId()) {
                 return true;
             }
         }
 
         return false;
     }
     
     @Override
     public int hashCode() {
         return this.mapId;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("Map [mapId=");
         builder.append(mapId);
         builder.append(", mapName=");
         builder.append(mapName);
         builder.append(", mapType=");
         builder.append(mapType);
         builder.append(", mpId=");
         builder.append(mpId);
         builder.append("]");
         return builder.toString();
     }
     
 }
 
