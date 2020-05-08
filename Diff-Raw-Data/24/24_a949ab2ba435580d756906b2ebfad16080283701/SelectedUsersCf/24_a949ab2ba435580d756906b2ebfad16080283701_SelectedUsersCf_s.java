 /*
  * Created by Andrey Markelov 11-11-2012.
  * Copyright Mail.Ru Group 2012. All rights reserved.
  */
 package ru.mail.jira.plugins.up;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.bc.user.search.UserPickerSearchService;
 import com.atlassian.jira.config.properties.ApplicationProperties;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.customfields.converters.UserConverterImpl;
 import com.atlassian.jira.issue.customfields.impl.UserCFType;
 import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
 import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.user.util.UserUtil;
 
 /**
  * Single selected users field.
  * 
  * @author Andrey Markelov
  */
 public class SelectedUsersCf
     extends UserCFType
 {
     /**
      * Plugin data.
      */
     private final PluginData data;
 
     /**
      * User util.
      */
     private final UserUtil userUtil;
 
     /**
      * Constructor.
      */
     public SelectedUsersCf(
         CustomFieldValuePersister customFieldValuePersister,
         GenericConfigManager genericConfigManager,
         ApplicationProperties applicationProperties,
         JiraAuthenticationContext authenticationContext,
         UserPickerSearchService searchService,
         PluginData data,
         UserUtil userUtil)
     {
         super(
             customFieldValuePersister,
             new UserConverterImpl(userUtil),
             genericConfigManager,
             applicationProperties,
             authenticationContext,
             searchService);
         this.data = data;
         this.userUtil = userUtil;
     }
 
     @Override
     public Map<String, Object> getVelocityParameters(
         Issue issue,
         CustomField field,
         FieldLayoutItem fieldLayoutItem)
     {
         Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
 
         Map<String, String> map = new HashMap<String, String>();
         Set<String> users = data.getStoredUsers(field.getId());
         for (String user : users)
         {
             User userObj = userUtil.getUserObject(user);
             if (userObj != null)
             {
                 map.put(userObj.getName(), userObj.getDisplayName());
             }
         }
 
         TreeMap<String, String> sorted_map = new TreeMap<String, String>(new ValueComparator(map));
         sorted_map.putAll(map);
         params.put("map", sorted_map);
 
         return params;
     }
 }
