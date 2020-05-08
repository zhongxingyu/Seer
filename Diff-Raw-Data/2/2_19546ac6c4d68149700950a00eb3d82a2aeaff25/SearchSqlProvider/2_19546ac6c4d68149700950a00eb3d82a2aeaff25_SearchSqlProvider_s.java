 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations
  * (FAO) and the Lesotho Land Administration Authority (LAA). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the names of FAO, the LAA nor the names of
  * its contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.search.repository;
 
 import java.util.Date;
 import static org.apache.ibatis.jdbc.SqlBuilder.*;
 import org.sola.common.StringUtility;
 import org.sola.services.ejb.search.repository.entities.BaUnitSearchParams;
 import org.sola.services.ejb.search.repository.entities.BaUnitSearchResult;
 import static org.sola.services.ejb.search.repository.entities.BaUnitSearchResult.QUERY_PARAM_LAND_USE_CODE;
 import org.sola.services.ejb.search.repository.entities.DisputeSearchResult;
 
 /**
  *
  * @author soladev
  */
 public class SearchSqlProvider {
 
     public static final String PARAM_APPLICATION_ID = "applicationId";
     private static final String APPLICATION_GROUP = "application";
     private static final String SERVICE_GROUP = "service";
     private static final String RRR_GROUP = "rrr";
     private static final String PROPERTY_GROUP = "Property";
     private static final String SOURCE_GROUP = "Source";
     private static final String AGENT_GROUP = "Agent";
     private static final String CONTACT_PERSON_GROUP = "Contact person";
     private static final String CHANGE_ACTION = "changed";
     private static final String ADDED_PROPERTY = "ADDED PROPERTY: ";
     private static final String DELETED_PROPERTY = "DELETED PROPERTY: ";
     private static final String ADDED_SOURCE = "ADDED DOCUMENT: Ref#";
     private static final String DELETED_SOURCE = "REMOVED DOCUMENT: Ref#";
     private static final String ADDED_AGENT = "ADDED AGENT: ";
     private static final String DELETED_AGENT = "REMOVED AGENT: ";
     private static final String ADDED_CONTACT_PERSON = "ADDED CONTACT PERSON: ";
     private static final String DELETED_CONTACT_PERSON = "REMOVED CONTACT PERSON: ";
 
     public static String buildApplicationLogSql() {
         String sql;
         int sortClassifier = 1;
 
         // Application 
         BEGIN();
         SELECT("'" + APPLICATION_GROUP + "' AS record_group");
         SELECT("'" + APPLICATION_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("app1.id AS record_id");
         SELECT("app1.rowversion AS record_sequence");
         SELECT("app1.nr AS nr");
         SELECT("CASE WHEN COALESCE(prev.action_code, 'null') = app1.action_code "
                 + " THEN '" + CHANGE_ACTION + "' ELSE app1.action_code END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("app1.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = app1.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application app1 "
                 + " LEFT JOIN application.application_historic prev "
                 + " ON app1.id = prev.id AND (app1.rowversion - 1) = prev.rowversion");
         WHERE("app1.id = #{" + PARAM_APPLICATION_ID + "}");
 
         sql = SQL() + " UNION ";
         sortClassifier++;
 
         //Application History
         BEGIN();
         SELECT("'" + APPLICATION_GROUP + "' AS record_group");
         SELECT("'" + APPLICATION_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("app_hist.id AS record_id");
         SELECT("app_hist.rowversion AS record_sequence");
         SELECT("app_hist.nr AS nr");
         SELECT("CASE WHEN COALESCE(prev.action_code, 'null') = app_hist.action_code "
                 + " THEN '" + CHANGE_ACTION + "' ELSE app_hist.action_code END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("app_hist.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = app_hist.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application_historic app_hist "
                 + " LEFT JOIN application.application_historic prev "
                 + " ON app_hist.id = prev.id AND (app_hist.rowversion - 1) = prev.rowversion");
         WHERE("app_hist.id = #{" + PARAM_APPLICATION_ID + "}");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // Service
         BEGIN();
         SELECT("'" + SERVICE_GROUP + "' AS record_group");
         SELECT("ser1.request_type_code AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("ser1.id AS record_id");
         SELECT("ser1.rowversion AS record_sequence");
         SELECT("ser1.service_order::text AS nr");
         SELECT("CASE WHEN COALESCE(prev.action_code, 'null') = ser1.action_code "
                 + " THEN '" + CHANGE_ACTION + "' ELSE ser1.action_code END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("ser1.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = ser1.change_user::text)"
                 + " AS user_fullname");
         FROM("application.service ser1 "
                 + " LEFT JOIN application.service_historic prev "
                 + " ON ser1.id = prev.id AND (ser1.rowversion - 1) = prev.rowversion");
         WHERE("ser1.application_id = #{" + PARAM_APPLICATION_ID + "}");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // Service History
         BEGIN();
         SELECT("'" + SERVICE_GROUP + "' AS record_group");
         SELECT("ser_hist.request_type_code AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("ser_hist.id AS record_id");
         SELECT("ser_hist.rowversion AS record_sequence");
         SELECT("ser_hist.service_order::text AS nr");
         SELECT("CASE WHEN COALESCE(prev.action_code, 'null') = ser_hist.action_code "
                 + " THEN '" + CHANGE_ACTION + "' ELSE ser_hist.action_code END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("ser_hist.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = ser_hist.change_user::text)"
                 + " AS user_fullname");
         FROM("application.service ser");
         FROM("application.service_historic ser_hist"
                 + " LEFT JOIN application.service_historic prev "
                 + " ON ser_hist.id = prev.id AND (ser_hist.rowversion - 1) = prev.rowversion");
         WHERE("ser.application_id = #{" + PARAM_APPLICATION_ID + "}");
         WHERE("ser_hist.rowidentifier = ser.rowidentifier");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // Property
         BEGIN();
         SELECT("'" + PROPERTY_GROUP + "' AS record_group");
         SELECT("'" + PROPERTY_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("prop1.ba_unit_id AS record_id");
         SELECT("prop1.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("replace(prop1.change_action,'i','" + ADDED_PROPERTY + "')||pb.name_firstpart||'-'||pb.name_lastpart AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("prop1.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = prop1.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application_property prop1 LEFT JOIN administrative.ba_unit pb ON prop1.ba_unit_id=pb.id ");
         WHERE("prop1.application_id = #{" + PARAM_APPLICATION_ID + "}");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // Application property History
         BEGIN();
         SELECT("'" + PROPERTY_GROUP + "' AS record_group");
         SELECT("'" + PROPERTY_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("prop_hist.ba_unit_id AS record_id");
         SELECT("prop_hist.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("CASE WHEN prop_hist.change_action = 'i' then replace(prop_hist.change_action,'i','" + ADDED_PROPERTY + "')||' - '|| pbh.name_firstpart|| '-'||pbh.name_lastpart"
                 + "  WHEN prop_hist.change_action = 'd' then replace(prop_hist.change_action,'d','" + DELETED_PROPERTY + "')||' - '|| pbh.name_firstpart|| '-'||pbh.name_lastpart"
                 + "  END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("prop_hist.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = prop_hist.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application_property_historic prop_hist LEFT JOIN administrative.ba_unit pbh ON prop_hist.ba_unit_id=pbh.id");
         WHERE("prop_hist.application_id = #{" + PARAM_APPLICATION_ID + "}");
 
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // SOURCE
         BEGIN();
         SELECT("'" + SOURCE_GROUP + "' AS record_group");
         SELECT("'" + SOURCE_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("source1.source_id AS record_id");
         SELECT("source1.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("replace(source1.change_action,'i','" + ADDED_SOURCE + "')||coalesce(source.reference_nr,'')||' - '||coalesce(source.type_code,'')   AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("source1.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = source1.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application_uses_source source1  "
                 + " LEFT JOIN source.source source "
                 + " ON source1.source_id = source.id ");
         WHERE("source1.application_id = #{" + PARAM_APPLICATION_ID + "}");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // Application Source History
         BEGIN();
         SELECT("'" + SOURCE_GROUP + "' AS record_group");
         SELECT("'" + SOURCE_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("source1.source_id AS record_id");
         SELECT("source1.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("CASE WHEN source1.change_action = 'i' then replace(source1.change_action,'i','" + ADDED_SOURCE + "')||coalesce(source.reference_nr,'')||' - '||coalesce(source.type_code,'') "
                 + "  WHEN source1.change_action = 'd' then replace(source1.change_action,'d','" + DELETED_SOURCE + "')||coalesce(source.reference_nr,'')||' - '||coalesce(source.type_code,'') "
                 + "  END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("source1.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = source1.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application_uses_source_historic source1  "
                 + " LEFT JOIN source.source source "
                 + " ON source1.source_id = source.id ");
         WHERE("source1.application_id = #{" + PARAM_APPLICATION_ID + "}");
 
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // AGENT 
         BEGIN();
 
         SELECT("'" + AGENT_GROUP + "' AS record_group");
         SELECT("'" + AGENT_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("app.agent_id AS record_id");
         SELECT("app.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("replace(party.change_action,'i','" + ADDED_AGENT + "')||' - '||party.name||' '||coalesce(party.last_name,'') AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("app.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = app.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application app");
         FROM("party.party party");
         WHERE("app.id = #{" + PARAM_APPLICATION_ID + "}");
         WHERE("app.agent_id=party.id");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // AGENT History
         BEGIN();
 
         SELECT("'" + AGENT_GROUP + "' AS record_group");
         SELECT("'" + AGENT_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("app.agent_id AS record_id");
         SELECT("app.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("CASE WHEN (app.change_action='i') then replace(party.change_action,'i','" + ADDED_AGENT + "')||' - '||coalesce(party.name,'')||' '||coalesce(party.last_name,'')"
                 + " ELSE  replace(app.change_action,app.change_action,'" + DELETED_AGENT + "')||' - '||coalesce(party.name,'')||' '||coalesce(party.last_name,'')"
                 + " END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("app.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = app.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application new_app");
         FROM("application.application_historic app"
                 + " LEFT JOIN party.party party"
                 + "  ON app.agent_id = party.id");
         WHERE("app.id = #{" + PARAM_APPLICATION_ID + "}");
         WHERE("app.agent_id != new_app.agent_id");
         WHERE("app.agent_id=party.id");
         WHERE("((app.rowversion - 1) = new_app.rowversion OR (app.rowversion) = new_app.rowversion)");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
 
         // contact_person 
         BEGIN();
 
         SELECT("'" + CONTACT_PERSON_GROUP + "' AS record_group");
         SELECT("'" + CONTACT_PERSON_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("app.contact_person_id AS record_id");
         SELECT("app.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("replace(party.change_action,app.change_action,'" + ADDED_CONTACT_PERSON + "')||' - '||party.name||' '||coalesce(party.last_name,'') AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("app.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = app.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application app");
         FROM("party.party party");
         WHERE("app.id = #{" + PARAM_APPLICATION_ID + "}");
         WHERE("app.contact_person_id=party.id");
 
         sql = sql + SQL() + " UNION ";
         sortClassifier++;
 
         // contact_person History
         BEGIN();
 
         SELECT("'" + CONTACT_PERSON_GROUP + "' AS record_group");
         SELECT("'" + CONTACT_PERSON_GROUP + "' AS record_type");
         SELECT(sortClassifier + " as sort_classifier");
         SELECT("app.contact_person_id AS record_id");
         SELECT("app.rowversion AS record_sequence");
         SELECT("''::text AS nr");
         SELECT("CASE WHEN (app.change_action='i') then replace(party.change_action,'i','" + ADDED_CONTACT_PERSON + "')||' - '||coalesce(party.name,'')||' '||coalesce(party.last_name,'')"
                 + " ELSE  replace(app.change_action,app.change_action,'" + DELETED_CONTACT_PERSON + "')||' - '||coalesce(party.name,'')||' '||coalesce(party.last_name,'')"
                 + " END AS action_code");
         SELECT("NULL::text AS notation");
         SELECT("app.change_time");
         SELECT("(SELECT (appuser.first_name::text || ' '::text) || appuser.last_name::text"
                 + " FROM system.appuser"
                 + " WHERE appuser.username::text = app.change_user::text)"
                 + " AS user_fullname");
         FROM("application.application new_app");
         FROM("application.application_historic app"
                 + " LEFT JOIN party.party_historic party"
                 + "  ON app.contact_person_id = party.id");
         WHERE("app.id = #{" + PARAM_APPLICATION_ID + "}");
         WHERE("app.contact_person_id != new_app.contact_person_id");
         WHERE("app.contact_person_id=party.id");
         WHERE("((app.rowversion - 1) = new_app.rowversion OR (app.rowversion) = new_app.rowversion)");
 
         ORDER_BY("change_time, sort_classifier, nr");
 
         sql = sql + SQL();
 
         return sql;
     }
 
     /**
      * Uses the BA Unit Search parameters to build an appropriate SQL Query.
      * This method does not inject the search parameter values into the SQL as
      * that would prevent the database from performing statement caching.
      *
      * @param params BaUnit search parameters
      * @return SQL String
      */
     public static String buildSearchBaUnitSql(BaUnitSearchParams params) {
         String sql;
         BEGIN();
         SELECT("DISTINCT prop.id");
         SELECT("prop.name");
         SELECT("prop.name_firstpart");
         SELECT("prop.name_lastpart");
         SELECT("prop.status_code");
         SELECT("rrr.registration_number");
         SELECT("rrr.lease_number");
         SELECT("rrr.land_use_code");
         SELECT("rrr.registration_date");
         SELECT("(SELECT MIN(r1.registration_date) "
                 + "FROM administrative.rrr r1 "
                 + "WHERE r1.nr = rrr.nr "
                 + "AND   r1.ba_unit_id = rrr.ba_unit_id) AS original_registration_date");
         SELECT("(SELECT string_agg(COALESCE(p1.name, '') || ' ' || COALESCE(p1.last_name, ''), '; ') "
                 + "FROM administrative.party_for_rrr pr1, party.party p1 "
                 + "WHERE pr1.rrr_id = rrr.id "
                 + "AND p1.id = pr1.party_id ) AS rightholders");
         SELECT("rrr.rowversion");
         SELECT("rrr.change_user");
         SELECT("rrr.rowidentifier");
         FROM("administrative.ba_unit prop");
         FROM("administrative.rrr rrr");
         WHERE("rrr.ba_unit_id = prop.id");
         WHERE("rrr.type_code = 'lease'");
         // Only search rrr that have a status that matches the ba unit status. 
         WHERE("rrr.status_code = prop.status_code");
 
         if (!StringUtility.isEmpty(params.getLeaseNumber())) {
             WHERE("compare_strings(#{" + BaUnitSearchResult.QUERY_PARAM_LEASE_NUMBER + "}, "
                    + "COALESCE(rrr.lease_number, '')");
         }
         if (!StringUtility.isEmpty(params.getLandUseCode())) {
             WHERE("rrr.land_use_code = #{" + BaUnitSearchResult.QUERY_PARAM_LAND_USE_CODE + "}");
         }
 
         if (!StringUtility.isEmpty(params.getOwnerName())) {
             FROM("administrative.party_for_rrr pr");
             FROM("party.party p");
             WHERE("pr.rrr_id = rrr.id");
             WHERE("p.id = pr.party_id");
             WHERE("compare_strings(#{" + BaUnitSearchResult.QUERY_PARAM_OWNER_NAME + "}, "
                     + "COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, '') || ' ' || COALESCE(p.alias, ''))");
         }
 
         if (!StringUtility.isEmpty(params.getNameFirstPart())) {
             WHERE("compare_strings(#{" + BaUnitSearchResult.QUERY_PARAM_NAME_FIRSTPART
                     + "}, COALESCE(prop.name_firstpart, ''))");
         }
         if (!StringUtility.isEmpty(params.getNameLastPart())) {
             WHERE("compare_strings(#{" + BaUnitSearchResult.QUERY_PARAM_NAME_LASTPART
                     + "}, COALESCE(prop.name_lastpart, ''))");
         }
         ORDER_BY(BaUnitSearchResult.QUERY_ORDER_BY + " LIMIT 100");
         sql = SQL();
         return sql;
     }
 }
