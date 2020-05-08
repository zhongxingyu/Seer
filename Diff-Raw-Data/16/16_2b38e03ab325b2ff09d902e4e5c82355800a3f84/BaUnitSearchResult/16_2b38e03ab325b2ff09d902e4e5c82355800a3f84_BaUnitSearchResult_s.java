 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations (FAO)
  * and the Lesotho Land Administration Authority (LAA). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the names of FAO, the LAA nor the names of its contributors may be used to
  *       endorse or promote products derived from this software without specific prior
  * 	  written permission.
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
 package org.sola.services.ejb.search.repository.entities;
 
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Id;
 import org.sola.services.common.repository.entities.AbstractEntity;
 
 public class BaUnitSearchResult extends AbstractEntity {
 
     public static final String QUERY_PARAM_NAME_FIRSTPART = "nameFirstPart";
     public static final String QUERY_PARAM_NAME_LASTPART = "nameLastPart";
     public static final String QUERY_PARAM_OWNER_NAME = "ownerName";
     public static final String QUERY_PARAM_LEASE_NUMBER = "leaseNumber";
     public static final String QUERY_ORDER_BY = "prop.name_firstpart, prop.name_lastpart ";
     
     public static final String QUERY_SEARCH_SELECT_PART = ""
             + "SELECT DISTINCT prop.id, prop.name_firstpart, prop.name_lastpart, prop.status_code, "
             + "rrr.registration_number, rrr.lease_number, rrr.registration_date, prop.rowversion, prop.change_user, prop.rowidentifier, "
             + "(SELECT string_agg(COALESCE(p1.name, '') || ' ' || COALESCE(p1.last_name, ''), '; ') "
             + "FROM administrative.rrr rrr1, administrative.party_for_rrr pr1, party.party p1 "
             + "WHERE rrr1.id = rrr.id AND rrr1.status_code = 'current' "
             + "AND pr1.rrr_id = rrr1.id AND p1.id = pr1.party_id) AS rightholders "
             + "FROM administrative.ba_unit prop LEFT JOIN ( "
            + "(SELECT r.id, r.ba_unit_id, r.lease_number, r.registration_number, r.registration_date FROM administrative.rrr r "
             + "    WHERE r.type_code='lease' AND r.status_code='current') rrr "
             + "INNER JOIN (administrative.party_for_rrr pr INNER JOIN party.party p ON pr.party_id = p.id) "
             + "ON rrr.id = pr.rrr_id) "
             + "ON prop.id = rrr.ba_unit_id ";
     
     public static final String QUERY_SEARCH_BY_PARAMS = QUERY_SEARCH_SELECT_PART 
             + "WHERE (POSITION(#{" + QUERY_PARAM_LEASE_NUMBER + "} IN COALESCE(rrr.lease_number, '')) > 0) "
             + "AND (compare_strings(#{" + QUERY_PARAM_OWNER_NAME + "}, COALESCE(p.name, '') || ' ' || "
             + "COALESCE(p.last_name, '') || ' ' || COALESCE(p.alias, '')) OR #{" + QUERY_PARAM_OWNER_NAME + "}='') "
             + "AND (COALESCE(prop.name_firstpart, '')=#{" + QUERY_PARAM_NAME_FIRSTPART + "} OR #{" + QUERY_PARAM_NAME_FIRSTPART + "} = '') "
             + "AND (COALESCE(prop.name_lastpart, '')=#{" + QUERY_PARAM_NAME_LASTPART + "} OR #{" + QUERY_PARAM_NAME_LASTPART + "} = '') "
             + "AND (prop.status_code = 'current' OR prop.status_code = 'historic') "
             + " ORDER BY " + QUERY_ORDER_BY
             + "LIMIT 100";
    
     @Id
     @Column
     private String id;
     @Column
     private String name;
     @Column(name = "name_firstpart")
     private String nameFirstPart;
     @Column(name = "name_lastpart")
     private String nameLastPart;
     @Column(name = "status_code")
     private String statusCode;
     @Column
     private String rightholders;
     @Column(name="registration_number")
     private String registrationNumber;
     @Column(name="lease_number")
     private String leaseNumber;
     @Column(name="registration_date")
     private Date registrationDate;
 
     public BaUnitSearchResult() {
         super();
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getNameFirstPart() {
         return nameFirstPart;
     }
 
     public void setNameFirstPart(String nameFirstPart) {
         this.nameFirstPart = nameFirstPart;
     }
 
     public String getNameLastPart() {
         return nameLastPart;
     }
 
     public void setNameLastPart(String nameLastPart) {
         this.nameLastPart = nameLastPart;
     }
 
     public String getRightholders() {
         return rightholders;
     }
 
     public void setRightholders(String rightholders) {
         this.rightholders = rightholders;
     }
 
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String statusCode) {
         this.statusCode = statusCode;
     }
 
     public Date getRegistrationDate() {
         return registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         this.registrationDate = registrationDate;
     }
 
     public String getRegistrationNumber() {
         return registrationNumber;
     }
 
     public void setRegistrationNumber(String registrationNumber) {
         this.registrationNumber = registrationNumber;
     }
 
     public String getLeaseNumber() {
         return leaseNumber;
     }
 
     public void setLeaseNumber(String leaseNumber) {
         this.leaseNumber = leaseNumber;
     }
 }
