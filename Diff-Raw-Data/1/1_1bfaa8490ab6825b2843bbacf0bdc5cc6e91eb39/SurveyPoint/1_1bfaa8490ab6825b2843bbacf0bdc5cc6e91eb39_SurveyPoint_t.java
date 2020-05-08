 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted
  * provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
  * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
  * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.ejb.cadastre.repository.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.Table;
import org.sola.common.StringUtility;
 import org.sola.services.common.repository.AccessFunctions;
 import org.sola.services.common.repository.entities.AbstractVersionedEntity;
 
 /**
  * Entity representing the cadastre.survey_point table.
  *
  * @author manoku
  */
 @Table(name = "survey_point", schema = "cadastre")
 public class SurveyPoint extends AbstractVersionedEntity {
 
     /**
      * WHERE clause to return survey points based on the transaction id
      */
     public static final String QUERY_WHERE_SEARCHBYTRANSACTION =
             "transaction_id = #{transaction_id}";
     @Id
     @Column(name = "transaction_id", updatable = false)
     private String transactionId;
     @Id
     @Column(name = "id")
     private String id;
     @Column(name = "boundary")
     private boolean boundary;
     @Column(name = "linked")
     private boolean linked;
     @Column(name = "geom")
     @AccessFunctions(onSelect = "st_asewkb(geom)",
     onChange = "get_geometry_with_srid(#{geom})")
     private byte[] geom;
     @Column(name = "original_geom")
     @AccessFunctions(onSelect = "st_asewkb(original_geom)",
     onChange = "get_geometry_with_srid(#{originalGeom})")
     private byte[] originalGeom;
 
     public boolean isBoundary() {
         return boundary;
     }
 
     public void setBoundary(boolean boundary) {
         this.boundary = boundary;
     }
 
     public boolean isLinked() {
         return linked;
     }
 
     public void setLinked(boolean linked) {
         this.linked = linked;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public byte[] getGeom() {
         return geom;
     }
 
     public void setGeom(byte[] geom) { //NOSONAR
         this.geom = geom; //NOSONAR
     }
 
     public byte[] getOriginalGeom() {
         return originalGeom;
     }
 
     public void setOriginalGeom(byte[] originalGeom) { //NOSONAR
         this.originalGeom = originalGeom; //NOSONAR
     }
 
     public String getTransactionId() {
         return transactionId;
     }
 
     public void setTransactionId(String transactionId) {
         this.transactionId = transactionId;
     }
 }
