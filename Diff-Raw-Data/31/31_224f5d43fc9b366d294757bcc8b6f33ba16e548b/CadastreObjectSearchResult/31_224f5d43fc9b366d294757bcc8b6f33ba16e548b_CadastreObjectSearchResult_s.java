 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
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
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.search.repository.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Table;
 import org.sola.services.common.repository.AccessFunctions;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 
 /**
  *
  * @author Elton Manoku
  */
 @Table(name = "cadastre_object", schema = "cadastre")
 public class CadastreObjectSearchResult extends AbstractReadOnlyEntity {
 
     public static final String SEARCH_STRING_PARAM = "search_string";
     public static final String SEARCH_BY_NUMBER = "NUMBER";
     public static final String SEARCH_BY_BAUNIT = "BAUNIT";
     public static final String SEARCH_BY_OWNER_OF_BAUNIT = "OWNER_OF_BAUNIT";
     public static final String SEARCH_BY_BAUNIT_ID = "BAUNIT_ID";
     
     public static final String QUERY_WHERE_SEARCHBY_NUMBER = "status_code= 'current' and "
             + "compare_strings(#{search_string}, name_firstpart || ' ' || name_lastpart)";
     
     public static final String QUERY_SELECT_SEARCHBY_BAUNIT = "distinct co.id, "
             + "ba_unit.name_firstpart || '/ ' || ba_unit.name_lastpart || "
             + "' > ' || co.name_firstpart || '/ ' || co.name_lastpart as label, "
             + "st_asewkb(geom_polygon) as the_geom";
 
     public static final String QUERY_FROM_SEARCHBY_BAUNIT = "cadastre.cadastre_object  co "
             + " inner join administrative.ba_unit_contains_spatial_unit bas "
             + " on co.id = bas.spatial_unit_id "
             + " inner join administrative.ba_unit on ba_unit.id = bas.ba_unit_id";
 
    public static final String QUERY_WHERE_SEARCHBY_BAUNIT = "co.status_code= 'current' "
            + "and ba_unit.status_code = 'current' "
             + "and compare_strings(#{search_string}, "
             + "ba_unit.name_firstpart || ' ' || ba_unit.name_lastpart)";
 
     public static final String QUERY_SELECT_SEARCHBY_OWNER_OF_BAUNIT = " distinct co.id, "
             + "coalesce(party.name, '') || ' ' || coalesce(party.last_name, '') || "
             + "' > ' || co.name_firstpart || '/ ' || co.name_lastpart as label, "
             + "st_asewkb(co.geom_polygon) as the_geom";
     
     public static final String QUERY_FROM_SEARCHBY_OWNER_OF_BAUNIT = "cadastre.cadastre_object co "
             + "inner join administrative.ba_unit_contains_spatial_unit bas "
             + "on co.id = bas.spatial_unit_id"
             + " inner join administrative.ba_unit "
            + "on (bas.ba_unit_id= ba_unit.id and ba_unit.status_code= 'current') "
             + "inner join administrative.rrr "
             + "on (ba_unit.id = rrr.ba_unit_id and rrr.status_code = 'current' "
             + "and rrr.type_code = 'ownership') "
             + "inner join administrative.party_for_rrr pfr on rrr.id = pfr.rrr_id "
             + "inner join party.party on pfr.party_id= pfr.party_id ";
     
    public static final String QUERY_WHERE_SEARCHBY_OWNER_OF_BAUNIT = "co.status_code= 'current' "
             + "and compare_strings(#{search_string}, "
             + "coalesce(party.name, '') || ' ' || coalesce(party.last_name, ''))";

//            public static final String QUERY_WHERE_SEARCHBY_OWNER_OF_BAUNIT = "status_code= 'current' and "
//            + " id in (select spatial_unit_id "
//            + " from administrative.ba_unit_contains_spatial_unit bas "
//            + " inner join administrative.ba_unit on "
//            + " (bas.ba_unit_id= ba_unit.id and ba_unit.status_code= 'current') "
//            + " inner join administrative.rrr on (ba_unit.id = rrr.ba_unit_id "
//            + " and rrr.status_code = 'current' and rrr.type_code = 'ownership') "
//            + " inner join administrative.party_for_rrr pfr on rrr.id = pfr.rrr_id "
//            + " inner join party.party on pfr.party_id= pfr.party_id "
//            + " where compare_strings(#{search_string}, coalesce(party.name, '') "
//            + " || ' ' || coalesce(party.last_name, '')))";
     
     public static final String QUERY_WHERE_GET_NEW_PARCELS = "transaction_id IN "
             + "(SELECT cot.transaction_id "
             + "FROM (administrative.ba_unit_contains_spatial_unit ba_su "
             + "INNER JOIN cadastre.cadastre_object co ON ba_su.spatial_unit_id = co.id) "
             + "INNER JOIN cadastre.cadastre_object_target cot ON co.id = cot.cadastre_object_id "
             + "WHERE ba_su.ba_unit_id = #{search_string}) "
             + "AND (SELECT COUNT(1) FROM administrative.ba_unit_contains_spatial_unit WHERE spatial_unit_id = cadastre_object.id) = 0 "
             + "AND status_code = 'current'";
     
     @Column(name = "id")
     private String id;
     @Column(name = "label")
     @AccessFunctions(onSelect = "name_firstpart || '/ ' || name_lastpart")
     private String label;
     @Column(name = "the_geom")
     @AccessFunctions(onSelect = "st_asewkb(geom_polygon)")
     private byte[] theGeom;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public byte[] getTheGeom() {
         return theGeom;
     }
 
     public void setTheGeom(byte[] theGeom) {
         this.theGeom = theGeom;
     }
 }
