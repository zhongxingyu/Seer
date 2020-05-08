 /*******************************************************************************
  * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Luis Silva - ACGHSync
  *     João Antunes - initial API and implementation
  ******************************************************************************/
 package pt.ist.maidSyncher.api.activeCollab;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 public class ACTask extends ACObject {
 
     public static final String CLASS_VALUE = "Task";
 
     // attributes
     private String _name;
     private String _body;
     private boolean _visibility;
     private int _categoryId;
     private int _labelId;
     private int projectId;
     private int _milestoneId;
     private int _priority;
     private int _assigneeId;
     private Set<Long> otherAssigneesId;
     private Date _dueOn;
 
     private Boolean complete;
 
     private boolean archived;
 
     public ACTask() {
         super();
     }
 
     public ACTask(JSONObject jsonObj) throws IOException {
         super(jsonObj);
 
     }
 
     @Override
     protected void init(JSONObject jsonObj) throws IOException {
         _name = JsonRest.getString(jsonObj, "name");
         _body = JsonRest.getString(jsonObj, "body");
         Boolean visibilityFromInt = JsonRest.getBooleanFromInt(jsonObj, "visibility");
         _visibility = visibilityFromInt == null ? false : visibilityFromInt;
         _categoryId = JsonRest.getInt(jsonObj, "category_id");
         _labelId = JsonRest.getInt(jsonObj, "label_id");
         _milestoneId = JsonRest.getInt(jsonObj, "milestone_id");
         _priority = JsonRest.getInt(jsonObj, "priority");
         _assigneeId = JsonRest.getInt(jsonObj, "assignee_id");
         _dueOn = JsonRest.getDate(jsonObj, "due_on");
         if (_dueOn != null) {
             Calendar cal = Calendar.getInstance();
             cal.setTime(_dueOn);
             cal.set(Calendar.HOUR_OF_DAY, 23);
             cal.set(Calendar.MINUTE, 59);
             cal.set(Calendar.SECOND, 59);
             _dueOn = cal.getTime();
         }
 //        getSubTasks();
 
         setComplete(JsonRest.getBooleanFromInt(jsonObj, "is_completed"));
 
         this.otherAssigneesId = new HashSet<Long>();
         JSONArray jsonArray = JsonRest.getJSONArray(jsonObj, "other_assignee_ids");
         if (jsonArray != null) {
             for (Object object : jsonArray) {
                 Long value = Long.valueOf((String) object);
                 otherAssigneesId.add(value);
             }
         }
 
         Boolean archivedBoolean = JsonRest.getBooleanFromInt(jsonObj, "is_archived");
         setArchived(archivedBoolean == null ? false : archivedBoolean);
 
         setProjectId(JsonRest.getInt(jsonObj, "project_id"));
 
     }
 
     static public ACTask moveTo(long taskId, long currentProjectId, long newProjectId) throws IOException {
         String path =
                 getRequestProcessor()
                 .getBasicUrlForPath("projects/" + currentProjectId + "/tasks/" + taskId + "/move-to-project");
         StringBuilder postData = new StringBuilder();
         JsonRest.setString(postData, "move_to_project_id", String.valueOf(newProjectId));
         return new ACTask(getRequestProcessor().processPost(path, postData.toString()));
 
     }
 
     @Override
     public String toJSONString() {
         StringBuilder postData = new StringBuilder();
         JsonRest.setString(postData, "task[name]", _name);
         JsonRest.setString(postData, "task[body]", _body);
         JsonRest.setIntFromBoolean(postData, "task[visibility]", _visibility);
         JsonRest.setInt(postData, "task[category_id]", _categoryId);
         JsonRest.setInt(postData, "task[label_id]", _labelId);
         JsonRest.setInt(postData, "task[milestone_id]", _milestoneId);
         JsonRest.setInt(postData, "task[priority]", _priority);
         JsonRest.setInt(postData, "task[assignee_id]", _assigneeId);
         JsonRest.setDate(postData, "task[due_on]", _dueOn);
         if (complete != null)
             JsonRest.setIntFromBoolean(postData, "task[is_complete]", complete); //? not sure
         //if it should be done through this or through the {context}/complete API call
         return postData.toString();
     }
 
     public static ACTask createTask(ACTask preliminarObject, ACProject acProject) throws IOException {
         return createTask(preliminarObject, acProject.getId());
     }
 
     public static ACTask createTask(ACTask preliminarObject, long projectId) throws IOException {
         String path = getRequestProcessor().getBasicUrlForPath("projects/" + projectId + "/tasks/add");
 
         ACTask recentlyCreatedACTask = new ACTask(postObject(path, preliminarObject));
         if (preliminarObject.getComplete() != null && preliminarObject.getComplete() == true) {
             if (ObjectUtils.equals(preliminarObject.getComplete(), recentlyCreatedACTask.getComplete()) == false) {
                 //time to make the recentlyCreatedACTask complete
                 recentlyCreatedACTask = recentlyCreatedACTask.postComplete();
             }
         }
         return recentlyCreatedACTask;
     }
 
     /**
      * Posts to {context}/complete and returns the new ACTask
      * 
      * @throws IOException In case something goes wrong with the post
      */
     public ACTask postComplete() throws IOException {
         String urlToUse = getUrl() + "/complete";
        return new ACTask(getRequestProcessor().processPost(urlToUse, null));
     }
 
     /**
      * Posts to {context}/reopen and returns the new ACTask
      * 
      * @throws IOException In case something goes wrong with the post
      */
     public ACTask postReopen() throws IOException {
         String urlToUse = getUrl() + "/reopen";
        return new ACTask(getRequestProcessor().processPost(urlToUse, null));
     }
 
     /**
      * 
      * @param url the base url of the object to update
      * @return
      * @throws IOException
      */
     public ACTask update(String url) throws IOException {
         checkArgument(StringUtils.isBlank(url) == false);
         //let us construct the URL and send an edit
         url += "/edit";
         return new ACTask(ACTask.postObject(url, this));
     }
 
     public String getName() {
         return _name;
     }
 
     public void setName(String name) {
         _name = name;
     }
 
     public String getBody() {
         return _body;
     }
 
     public void setBody(String body) {
         _body = body;
     }
 
     public boolean getVisibility() {
         return _visibility;
     }
 
     public void setVisibility(boolean visibility) {
         _visibility = visibility;
     }
 
     public int getCategoryId() {
         return _categoryId;
     }
 
     public void setCategoryId(int categoryId) {
         _categoryId = categoryId;
     }
 
     public int getLabelId() {
         return _labelId;
     }
 
     public void setLabelId(int labelId) {
         _labelId = labelId;
     }
 
     public int getMilestoneId() {
         return _milestoneId;
     }
 
     public void setMilestoneId(int milestoneId) {
         _milestoneId = milestoneId;
     }
 
     public int getPriority() {
         return _priority;
     }
 
     public void setPriority(int priority) {
         _priority = priority;
     }
 
     public int getAssigneeId() {
         return _assigneeId;
     }
 
     public void setAssigneeId(int assigneeId) {
         _assigneeId = assigneeId;
     }
 
     public Date getDueOn() {
         return _dueOn;
     }
 
     public void setDueOn(Date dueOn) {
         _dueOn = dueOn;
     }
 
     public Set<ACSubTask> getSubTasks() throws IOException {
         Set<ACSubTask> subtasks = new HashSet<ACSubTask>();
         JSONArray jsonArr = (JSONArray) getRequestProcessor().processGet(getUrl() + "/subtasks");
         if (jsonArr != null) {
             for (Object object : jsonArr) {
                 JSONObject jsonObject = (JSONObject) object;
                 subtasks.add(new ACSubTask(jsonObject));
             }
         }
         return subtasks;
     }
 
     public Set<ACComment> getComments() throws IOException {
         Set<ACComment> comments = new HashSet<ACComment>();
         JSONArray jsonArr = (JSONArray) getRequestProcessor().processGet(getUrl() + "/comments");
         //this URL has the
 
         if (jsonArr != null) {
             for (Object object : jsonArr) {
                 JSONObject jsonObject = (JSONObject) object;
                 comments.add(new ACComment(jsonObject));
             }
         }
         return comments;
 
     }
 
     public ACCategory getCategory() throws IOException {
         if (this._categoryId == -1 || this._categoryId == 0)
             return null;
         String decodedUrl = URLDecoder.decode(getUrl(), "UTF-8");
         int lastIndexOfSlash = StringUtils.lastIndexOf(decodedUrl, "/");
         String urlToUse = StringUtils.substring(decodedUrl, 0, lastIndexOfSlash);
         return new ACCategory((JSONObject) getRequestProcessor().processGet(urlToUse + "/categories/" + _categoryId));
 
     }
 
     public Boolean getComplete() {
         return complete;
     }
 
     public void setComplete(Boolean complete) {
         this.complete = complete;
     }
 
     public Set<Long> getOtherAssigneesId() {
         return otherAssigneesId;
     }
 
     public boolean isArchived() {
         return archived;
     }
 
     public void setArchived(boolean archived) {
         this.archived = archived;
     }
 
     public int getProjectId() {
         return projectId;
     }
 
     public void setProjectId(int projectId) {
         this.projectId = projectId;
     }
 
 }
