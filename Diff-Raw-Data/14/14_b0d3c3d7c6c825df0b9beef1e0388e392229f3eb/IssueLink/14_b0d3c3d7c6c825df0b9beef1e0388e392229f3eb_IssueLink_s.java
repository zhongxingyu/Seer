 /**
  * jira-client - a simple JIRA REST client
  * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package net.rcarz.jiraclient;
 
 import java.util.Map;
 
 import net.sf.json.JSON;
 import net.sf.json.JSONObject;
 
 /**
  * Represents an issue link.
  */
 public final class IssueLink extends Resource {
 
     private LinkType type = null;
     private Issue inwardIssue = null;
 
     /**
      * Creates a issue link from a JSON payload.
      *
      * @param restclient REST client instance
      * @param json JSON payload
      */
     protected IssueLink(RestClient restclient, JSONObject json) {
         super(restclient);
 
         if (json != null)
             deserialise(json);
     }
 
     private void deserialise(JSONObject json) {
         Map map = json;
 
         self = Field.getString(map.get("self"));
         id = Field.getString(map.get("id"));
         type = Field.getResource(LinkType.class, map.get("type"), restclient);
         inwardIssue = Field.getResource(Issue.class, map.get("inwardIssue"), restclient);
     }
 
     /**
      * Retrieves the given issue link record.
      *
      * @param restclient REST client instance
      * @param id Internal JIRA ID of the issue link
      *
      * @return a issue link instance
      *
      * @throws JiraException when the retrieval fails
      */
     public static IssueLink get(RestClient restclient, String id)
         throws JiraException {
 
         JSON result = null;
 
         try {
             result = restclient.get(RESOURCE_URI + "issueLink/" + id);
         } catch (Exception ex) {
             throw new JiraException("Failed to retrieve issue link " + id, ex);
         }
 
         if (!(result instanceof JSONObject))
             throw new JiraException("JSON payload is malformed");
 
         return new IssueLink(restclient, (JSONObject)result);
     }
 
     @Override
     public String toString() {
         return String.format("%s %s", getType().getInward(), getInwardIssue());
     }
 
     public LinkType getType() {
         return type;
     }
 
     public Issue getInwardIssue() {
         return inwardIssue;
     }
 }
 
