 /*
 *  Nokia Data Gathering
 *
 *  Copyright (C) 2011 Nokia Corporation
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/
 */
 
 package controllers;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import flexjson.JSONSerializer;
 import flexjson.transformer.AbstractTransformer;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ArrayList;
 import models.Category;
 import models.Answer;
 import models.NdgGroup;
 import models.NdgResult;
 import models.NdgUser;
 import models.Survey;
 import models.UserRole;
 import models.constants.SurveyStatusConsts;
 import play.db.jpa.JPA;	
 import java.lang.Exception;
 
 public class ListData extends NdgController {
 
     public static void categories(int surveyId) {
         String query = "survey_id = " + String.valueOf(surveyId) + " order by categoryIndex";
         List<Category> categories = Category.find(query).fetch();
 
         JSONSerializer categoryListSerializer = new JSONSerializer();
         categoryListSerializer.include("id", "categoryIndex", "questionCollection", "label").rootName("categories");
 
         renderJSON(categoryListSerializer.serialize(categories));
     }
 
     public static void results(int surveyId, int startIndex, int endIndex, boolean isAscending, String orderBy,
                                String searchField, String searchText) {
        try {
             String query = getQuery( "survey_id", String.valueOf( surveyId ), false,
                                      searchField, searchText, null, isAscending );//sorting is not needed now
 
             long totalItems =  NdgResult.count( query );
 
             query = getQuery( "survey_id" , String.valueOf( surveyId ), false,
                               searchField, searchText, orderBy, isAscending );
 
             List<NdgResult> results = NdgResult.find(query.toString()).from(startIndex).fetch(endIndex - startIndex);
             JSONSerializer surveyListSerializer = new JSONSerializer();
            surveyListSerializer.include("id", "resultId", "title", "startTime", "ndgUser.username", "latitude")
                                         .exclude("*").rootName("items");
 
             renderJSON(addRangeToJson(surveyListSerializer.serialize(results), startIndex, totalItems));
 	    }
 	    catch(Exception ex) {
            ex.printStackTrace();
 	    }
     }
 
     public static void graphall(int questionId) {
         String query = "select a.textData, count(a.textData) as cnt from Answer a where question_id = "
                      + String.valueOf(questionId) + " group by text_data order by cnt asc";
         List<Answer> answers = Answer.find(query).fetch();
 
         JSONSerializer graphSerializer = new JSONSerializer();
         graphSerializer.include("cnt").exclude("*").rootName("data");
 
         renderJSON(graphSerializer.serialize(answers));
     }
 
     public static void graphselected( int questionId, String ids ) {
        String query = "select a.textData, count(a.textData) as cnt from Answer a where ndg_result_id in ("
                     + String.valueOf(ids) + ") and question_id = " + String.valueOf(questionId)
                     + " group by text_data order by cnt asc";
        List<Answer> answers = Answer.find(query).fetch();
 
        JSONSerializer graphSerializer = new JSONSerializer();
        graphSerializer.include("cnt").exclude("*").rootName("data");
 
        renderJSON(graphSerializer.serialize(answers));
     }
 
     public static void surveys(int startIndex, int endIndex, boolean isAscending, String orderBy, String searchField,
                                String searchText, String filter) {
         NdgUser currentUser = NdgUser.find("byUserName", session.get("ndgUser")).first();
         NdgUser currentUserAdmin = NdgUser.find("byUserName", currentUser.userAdmin).first();
 
         List<Survey> surveys = null;
         String query;
 
         if (filter != null && filter.length() > 0) {
             query = getQuery2Filters( "available" , String.valueOf( SurveyStatusConsts.getStatusFlag( filter ) ),
                                       "ndg_user_id", String.valueOf(currentUserAdmin.getId()), false,
                                       searchField, searchText, null, isAscending );//sorting is not needed now
         }
         else {
             query = getQuery( "ndg_user_id" , String.valueOf(currentUserAdmin.getId()), false,
                               searchField, searchText, null, isAscending );//sorting is not needed now
         }
 
         long totalItems = 0;
         totalItems = Survey.count( query );
 
         if ( orderBy != null && orderBy.equals( "resultCollection" ) ) {
             surveys = Survey.find( query ).fetch();
             Collections.sort( surveys, new SurveyNdgResultCollectionComapator() );
 
             if ( !isAscending ) {
                 Collections.reverse( surveys );
             }
 
             int subListEndIndex = surveys.size() <= endIndex ? surveys.size() : endIndex;
             surveys = surveys.subList( startIndex, subListEndIndex );
         } else {
             if (filter != null && filter.length() > 0) {
                 query = getQuery2Filters( "available", String.valueOf( SurveyStatusConsts.getStatusFlag( filter ) ),
                                           "ndg_user_id", String.valueOf(currentUserAdmin.getId()), false,
                                           searchField, searchText, orderBy, isAscending );
             }
             else {
                 query = getQuery( "ndg_user_id", String.valueOf(currentUserAdmin.getId()), false,
                                   searchField, searchText, orderBy, isAscending );
             }
             surveys = Survey.find( query ).from( startIndex ).fetch( endIndex - startIndex );
         }
         serializeSurveys(surveys, startIndex, totalItems);
     }
 
     private static String getQuery(String filterName, String filterValue, boolean isFilterString, String searchField,
                                    String searchText, String orderBy, boolean isAscending ) {
         StringBuilder query = new StringBuilder();
 
         String statusQuery = "";
         String searchQuery = "";
         String sortingQuery = "";
 
         if ( filterName != null && filterName.length() > 0
           && filterValue != null && filterValue.length() > 0 ) {
             statusQuery = filterName + "=" + ( isFilterString ? ("'" + filterValue + "'") : filterValue );
         }
 
         if ( searchField != null && searchText != null && searchText.length() > 0 ) {
             searchQuery = searchField + " like '%" + searchText + "%'";
         }
 
         if ( orderBy != null && orderBy.length()> 0 ) {
             sortingQuery = "order by " + orderBy + ( isAscending ? " asc" : " desc" );
         }
 
         query.append( statusQuery )
                 .append( ( statusQuery.length() > 0 && searchQuery.length() > 0 ) ? " and " : ' ' )
                    .append( searchQuery )
                       .append( ' ' )
                          .append( sortingQuery );
 
         return query.toString();
     }
 
     private static String getQuery2Filters(String filterName, String filterValue, String filterName2,
                                            String filterValue2, boolean isFilterString, String searchField,
                                            String searchText, String orderBy, boolean isAscending ) {
         StringBuilder query = new StringBuilder();
 
         String statusQuery = "";
         String searchQuery = "";
         String sortingQuery = "";
 
         if ( filterName != null && filterName.length() > 0
           && filterValue != null && filterValue.length() > 0 ) {
             statusQuery = filterName + "=" + ( isFilterString ? ("'" + filterValue + "'") : filterValue );
         }
 
         if ( filterName2 != null && filterName2.length() > 0
           && filterValue2 != null && filterValue2.length() > 0 ) {
             statusQuery += " and " + filterName2 + "="
                         + ( isFilterString ? ("'" + filterValue2 + "'") : filterValue2 );
         }
 
         if ( searchField != null && searchText != null && searchText.length() > 0 ) {
             searchQuery = searchField + " like '%" + searchText + "%'";
         }
 
         if ( orderBy != null && orderBy.length()> 0 ) {
             sortingQuery = "order by " + orderBy + ( isAscending ? " asc" : " desc" );
         }
 
         query.append( statusQuery )
                 .append( ( statusQuery.length() > 0 && searchQuery.length() > 0 ) ? " and " : ' ' )
                    .append( searchQuery )
                       .append( ' ' )
                          .append( sortingQuery );
 
         return query.toString();
     }
 
     public static void users(int startIndex, int endIndex, boolean isAscending, String orderBy, String searchField,
                              String groupName, String searchText) {
         NdgUser currentUser = NdgUser.find("byUserName", session.get("ndgUser")).first();
 
         List<NdgUser> users = null;
         String query;
 
         if(groupName != null && groupName.length() > 0) {
             query = getQuery( "ndg_group.groupName", groupName, true, searchField, searchText,
                                      null, isAscending );//sorting is not needed now
         }
         else {
             query = getQuery( "userAdmin", currentUser.userAdmin, true, searchField, searchText,
                                          null, isAscending );//sorting is not needed now
         }
 
         long totalItems = NdgUser.count( query );
 
         if (orderBy != null && orderBy.equals("userRoleCollection")) {
             users = NdgUser.find(query).fetch();
 
             Collections.sort(users, new NdgUserUserRoleCollectionComapator(isAscending));
             int subListEndIndex = users.size() <= endIndex ? users.size() : endIndex;
 
             users = users.subList(startIndex, subListEndIndex);
         } else {
             if(groupName != null && groupName.length() > 0) {
                 query = getQuery( "ndg_group.groupName", groupName, true, searchField, searchText, orderBy,
                                   isAscending );
             } else {
                 query = getQuery( "userAdmin", currentUser.userAdmin, true, searchField, searchText, orderBy,
                                   isAscending );
             }
             users = NdgUser.find(query.toString()).from(startIndex).fetch(endIndex - startIndex);
         }
         serializeUsers(users, startIndex, totalItems);
     }
 
     public static void groups(int startIndex, int endIndex, boolean isAscending, String orderBy, String searchField,
                               String searchText) {
         NdgUser currentUser = NdgUser.find("byUserName", session.get("ndgUser")).first();
         NdgUser currentUserAdmin = NdgUser.find("byUserName", currentUser.userAdmin).first();
 
         List<NdgGroup> groups = null;
         long totalItems = 0;
 
         String query = getQuery( "ndg_user_id", String.valueOf(currentUserAdmin.getId()), false, searchField,
                                  searchText, null, isAscending );//sorting is not needed now
 
         totalItems = NdgGroup.count( query );
 
         if (orderBy != null && orderBy.equals("resultCollection")) {
             groups = NdgGroup.find( query ).fetch();
 
             int subListEndIndex = groups.size() <= endIndex ? groups.size() : endIndex;
 
             groups = groups.subList(startIndex, subListEndIndex);
         } else {
             query = getQuery( "ndg_user_id", String.valueOf(currentUserAdmin.getId()), false, searchField, searchText,
                               orderBy, isAscending );
             groups = NdgGroup.find(query.toString()).from(startIndex).fetch(endIndex);
         }
         serializeGroups(groups, startIndex, totalItems);
     }
 
     private static void serializeUsers(List<NdgUser> users, int startIndex, long totalSize) {
         JSONSerializer userListSerializer = new JSONSerializer();
         userListSerializer.include("id", "username", "phoneNumber", "email", "userRoleCollection.ndgRole.roleName",
                                    "firstName", "lastName").exclude("*").rootName("items");
 
         renderJSON(addRangeToJson(userListSerializer.serialize(users), startIndex, totalSize));
     }
 
     private static void serializeSurveys(List<Survey> subList, int startIndex, long totalSize) {
         JSONSerializer surveyListSerializer = new JSONSerializer();
         surveyListSerializer.transform(new NdgResultCollectionTransformer(), "resultCollection");
         surveyListSerializer.include("id", "title", "uploadDate", "idUser", "surveyId", "ndgUser.username",
                                      "resultCollection", "available").exclude("*").rootName("items");
 
         renderJSON(addRangeToJson(surveyListSerializer.serialize(subList), startIndex, totalSize));
     }
 
     private static void serializeGroups(List<NdgGroup> subList, int startIndex, long totalSize) {
         JSONSerializer surveyListSerializer = new JSONSerializer();
         surveyListSerializer.transform(new NdgUserCollectionTransformer(), "userCollection");
         surveyListSerializer.include("groupName", "userCollection").exclude("*").rootName("items");
 
         renderJSON(addRangeToJson(surveyListSerializer.serialize(subList), startIndex, totalSize));
     }
 
     private static String addRangeToJson(String jsonString, int startIndex, long totalSize) {
         JsonParser parser = new JsonParser();
         JsonElement element = parser.parse(jsonString);
 
         if (element.isJsonObject()) {
             JsonObject object = element.getAsJsonObject();
             object.addProperty("startIndex", startIndex);
             object.addProperty("totalSize", totalSize);
         }
         return element.toString();
     }
 
     public static class SurveyNdgResultCollectionComapator implements Comparator<Survey> {
 
         public int compare(Survey o1, Survey o2) {
             return o1.resultCollection.size() < o2.resultCollection.size() ? 1 : -1;
         }
     }
 
     public static void sendSurveysUserList(String formID) {
         NdgUser currentUser = NdgUser.find("byUserName", session.get("ndgUser")).first();
         List<NdgUser> users = JPA.em().createNamedQuery("findUserSendingSurvey")
                                                                       .setParameter("surveyId", formID)
                                                                       .setParameter("userAdmin", currentUser.userAdmin)
                                                                       .getResultList();
 
         JSONSerializer userListSerializer = new JSONSerializer();
         userListSerializer.include("id", "username", "phoneNumber").exclude("*").rootName("items");
 
         renderJSON(userListSerializer.serialize(users));
     }
 
     private static class NdgUserUserRoleCollectionComapator implements Comparator<NdgUser> {
 
         private boolean isAscending = true;
 
         NdgUserUserRoleCollectionComapator(boolean isAscending) {
             this.isAscending = isAscending;
         }
 
         public int compare(NdgUser o1, NdgUser o2) {
             int retval = 0;
 
             UserRole role1 = (UserRole) o1.userRoleCollection.toArray()[0];
             UserRole role2 = (UserRole) o2.userRoleCollection.toArray()[0];
             retval = role1.ndgRole.id.compareTo(role2.ndgRole.id);
 
             return isAscending ? retval : -retval;
         }
     }
 
     private static class NdgResultCollectionTransformer extends AbstractTransformer {
 
         public void transform(Object collection) {
             Collection ndgResultCollection = (Collection) collection;
             getContext().write(String.valueOf(ndgResultCollection.size()));
         }
     }
 
     private static class NdgUserCollectionTransformer extends AbstractTransformer {
 
         public void transform(Object collection) {
             Collection ndgUserCollection = (Collection) collection;
             getContext().write(String.valueOf(ndgUserCollection.size()));
         }
     }
 }
