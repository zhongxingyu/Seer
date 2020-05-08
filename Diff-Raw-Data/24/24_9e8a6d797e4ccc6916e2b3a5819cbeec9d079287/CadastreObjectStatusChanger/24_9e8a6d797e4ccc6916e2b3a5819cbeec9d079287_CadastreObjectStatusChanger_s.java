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
 package org.sola.services.ejb.cadastre.repository.entities;
 
 import java.util.Calendar;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Table;
 import org.sola.services.common.repository.AccessFunctions;
 import org.sola.services.common.repository.entities.AbstractStatusChangerEntity;
 
 @Table(schema = "cadastre", name = "cadastre_object")
 public class CadastreObjectStatusChanger extends AbstractStatusChangerEntity {
     public static final String QUERY_WHERE_SEARCHBYTRANSACTION_PENDING = 
            " transaction_id = #{transaction_id} or id in (select cadastre_object_id from "
            + " cadastre.cadastre_object_target where transaction_id= #{transaction_id})";
     public static final String QUERY_WHERE_SEARCHBYTRANSACTION_TARGET = 
             " id in (select cadastre_object_id from "
             + " cadastre.cadastre_object_target where transaction_id= #{transaction_id})";
     
     @Column(name="approval_datetime")
     private Date approvalDatetime;
 
     @Column(name="historic_datetime")
     private Date historicDatetime;
 
     @Column(name = "geom_polygon")
     @AccessFunctions(onSelect = "st_asewkb(geom_polygon)",
     onChange = "get_geometry_with_srid(#{geomPolygon})")
     private byte[] geomPolygon;
 
     public Date getApprovalDatetime() {
         return approvalDatetime;
     }
 
     public void setApprovalDatetime(Date approvalDatetime) {
         this.approvalDatetime = approvalDatetime;
     }
 
     public Date getHistoricDatetime() {
         return historicDatetime;
     }
 
     public void setHistoricDatetime(Date historicDatetime) {
         this.historicDatetime = historicDatetime;
     }
 
     public byte[] getGeomPolygon() {
         return geomPolygon;
     }
 
     public void setGeomPolygon(byte[] geomPolygon) {
         this.geomPolygon = geomPolygon;
     }
 
     
     @Override
     public void preSave() {
         if (this.getStatusCode().equals(AbstractStatusChangerEntity.STATUS_CURRENT)
                 && this.getApprovalDatetime() == null){
             this.setApprovalDatetime(Calendar.getInstance().getTime());
         }else if (this.getStatusCode().equals(AbstractStatusChangerEntity.STATUS_HISTORIC)
                 && this.getHistoricDatetime() == null){
             this.setHistoricDatetime(Calendar.getInstance().getTime());
         }
         super.preSave();
     }
 }
