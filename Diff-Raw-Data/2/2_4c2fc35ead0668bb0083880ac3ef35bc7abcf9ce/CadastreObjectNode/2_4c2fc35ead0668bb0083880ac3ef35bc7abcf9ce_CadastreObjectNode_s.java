 /**
  * ******************************************************************************************
  * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.ejb.cadastre.repository.entities;
 
 import java.util.List;
 import javax.persistence.Column;
 import org.sola.services.common.repository.AccessFunctions;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 
 /**
  * Abstract Entity used to retrieve node data from the cadastre_object table geoms.
  *
  * @author Elton Manoku
  */
 public class CadastreObjectNode extends AbstractReadOnlyEntity {
 
     /**
      * Clause to create a spatial extent based on min and max, x and y values.
      */
     private final static String EXTENT_DEFINITION = "ST_SetSRID("
            + "ST_MakeBox3D(ST_Point(#{minx}, #{miny}),ST_Point(#{maxx}, #{maxy})), #{srid})";
     /**
      * FROM clause to obtain all CO node coordinates intersecting the given extent
      */
     public final static String QUERY_GET_BY_RECTANGLE_FROM_PART =
             "st_dumppoints((select st_transform(co.geom_polygon, #{srid}) from cadastre.cadastre_object co "
             + " where type_code= #{cadastre_object_type} and status_code= 'current' "
             + " and ST_Intersects(st_transform(co.geom_polygon, #{srid}), " + EXTENT_DEFINITION + ") limit 1)) t ";
     /**
      * Obtains all CO node coordinates intersecting the given extent
      */
     public final static String QUERY_GET_BY_RECTANGLE_WHERE_PART =
             " ST_Intersects(st_transform(t.geom, #{srid}), " + EXTENT_DEFINITION + ") ";
     /**
      * Not sure!
      */
     public final static String QUERY_GET_BY_RECTANGLE_POTENTIAL_FROM_PART =
             "(select distinct ST_Line_Interpolate_Point(st_intersection("
             + "st_boundary(st_transform(geom_polygon, #{srid}))," + EXTENT_DEFINITION
             + "), 0.5) as geom from cadastre.cadastre_object co "
             + "where type_code= #{cadastre_object_type} "
             + "and status_code= 'current' and ST_Intersects(st_transform(geom_polygon, #{srid}),"
             + EXTENT_DEFINITION + ") and st_geometrytype(st_intersection(st_boundary(st_transform(geom_polygon, #{srid})),"
             + EXTENT_DEFINITION + ")) = 'ST_LineString' limit 1) t";
     @Column(name = "id")
     @AccessFunctions(onSelect = "st_astext(st_transform(geom, #{srid}))")
     private String id;
     @Column(name = "geom")
     @AccessFunctions(onSelect = "st_asewkb(st_transform(geom, #{srid}))")
     private byte[] geom;
     private List<CadastreObject> cadastreObjectList;
 
     public byte[] getGeom() {
         return geom;
     }
 
     public void setGeom(byte[] geom) { //NOSONAR
         this.geom = geom; //NOSONAR
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public List<CadastreObject> getCadastreObjectList() {
         return cadastreObjectList;
     }
 
     public void setCadastreObjectList(List<CadastreObject> cadastreObjectList) {
         this.cadastreObjectList = cadastreObjectList;
     }
 }
