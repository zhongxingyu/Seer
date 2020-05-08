 package com.amee.service.environment;
 
 import com.amee.service.auth.ResourceActions;
 
 // TODO: Remove constants no longer used.
 public class EnvironmentConstants {
 
     public final static String VIEW_ENVIRONMENTS = "sites/environments.ftl";
     public final static String VIEW_ENVIRONMENT = "sites/environment.ftl";
 
     public final static String VIEW_SITES = "sites/sites.ftl";
     public final static String VIEW_SITE = "sites/site.ftl";
 
     public final static String VIEW_SITE_APPS = "sites/siteApps.ftl";
     public final static String VIEW_SITE_APP = "sites/siteApp.ftl";
 
     public final static String VIEW_GROUPS = "sites/groups.ftl";
     public final static String VIEW_GROUP = "sites/group.ftl";
 
     public final static String VIEW_ROLES = "sites/roles.ftl";
     public final static String VIEW_ROLE = "sites/role.ftl";
     public final static String VIEW_ROLE_ACTIONS = "sites/roleActions.ftl";
 
     public final static String VIEW_USERS = "sites/users.ftl";
    public final static String VIEW_USER = "sites/auth.ftl";
     public final static String VIEW_USER_UPLOAD = "sites/userUpload.ftl";
 
     public final static String VIEW_USER_GROUPS = "sites/userGroups.ftl";
     public final static String VIEW_USER_GROUP = "sites/userGroup.ftl";
 
     public final static String VIEW_USER_GROUP_ROLES = "sites/userGroupRoles.ftl";
     public final static String VIEW_USER_GROUP_ROLE = "sites/userGroupRole.ftl";
 
     public final static String VIEW_TASKS = "sites/tasks.ftl";
     public final static String VIEW_TASK = "sites/task.ftl";
 
     public final static String VIEW_ATTRIBUTE_GROUPS = "sites/attributeGroups.ftl";
     public final static String VIEW_ATTRIBUTE_GROUP = "sites/attributeGroup.ftl";
 
     public final static String ACTION_ENVIRONMENT_PREFIX = "environment";
     public final static String ACTION_ENVIRONMENT_VIEW = ACTION_ENVIRONMENT_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_ENVIRONMENT_CREATE = ACTION_ENVIRONMENT_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_ENVIRONMENT_MODIFY = ACTION_ENVIRONMENT_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_ENVIRONMENT_DELETE = ACTION_ENVIRONMENT_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_ENVIRONMENT_LIST = ACTION_ENVIRONMENT_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_SITE_PREFIX = "site";
     public final static String ACTION_SITE_VIEW = ACTION_SITE_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_SITE_CREATE = ACTION_SITE_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_SITE_MODIFY = ACTION_SITE_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_SITE_DELETE = ACTION_SITE_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_SITE_LIST = ACTION_SITE_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_SITE_APP_PREFIX = "site.app";
     public final static String ACTION_SITE_APP_VIEW = ACTION_SITE_APP_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_SITE_APP_CREATE = ACTION_SITE_APP_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_SITE_APP_MODIFY = ACTION_SITE_APP_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_SITE_APP_DELETE = ACTION_SITE_APP_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_SITE_APP_LIST = ACTION_SITE_APP_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_SITE_ALIAS_PREFIX = "site.alias";
     public final static String ACTION_SITE_ALIAS_VIEW = ACTION_SITE_ALIAS_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_SITE_ALIAS_CREATE = ACTION_SITE_ALIAS_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_SITE_ALIAS_MODIFY = ACTION_SITE_ALIAS_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_SITE_ALIAS_DELETE = ACTION_SITE_ALIAS_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_SITE_ALIAS_LIST = ACTION_SITE_ALIAS_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_GROUP_PREFIX = "site.group";
     public final static String ACTION_GROUP_VIEW = ACTION_GROUP_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_GROUP_CREATE = ACTION_GROUP_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_GROUP_MODIFY = ACTION_GROUP_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_GROUP_DELETE = ACTION_GROUP_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_GROUP_LIST = ACTION_GROUP_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_ROLE_PREFIX = "site.role";
     public final static String ACTION_ROLE_VIEW = ACTION_ROLE_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_ROLE_CREATE = ACTION_ROLE_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_ROLE_MODIFY = ACTION_ROLE_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_ROLE_DELETE = ACTION_ROLE_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_ROLE_LIST = ACTION_ROLE_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_USER_PREFIX = "site.auth";
     public final static String ACTION_USER_VIEW = ACTION_USER_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_USER_CREATE = ACTION_USER_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_USER_MODIFY = ACTION_USER_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_USER_DELETE = ACTION_USER_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_USER_LIST = ACTION_USER_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_SCHEDULED_TASK_PREFIX = "scheduledTask";
     public final static String ACTION_SCHEDULED_TASK_VIEW = ACTION_SCHEDULED_TASK_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_SCHEDULED_TASK_CREATE = ACTION_SCHEDULED_TASK_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_SCHEDULED_TASK_MODIFY = ACTION_SCHEDULED_TASK_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_SCHEDULED_TASK_DELETE = ACTION_SCHEDULED_TASK_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_SCHEDULED_TASK_LIST = ACTION_SCHEDULED_TASK_PREFIX + ResourceActions.ACTION_LIST;
 
     public final static String ACTION_ATTRIBUTE_GROUP_PREFIX = "attributeGroup";
     public final static String ACTION_ATTRIBUTE_GROUP_VIEW = ACTION_ATTRIBUTE_GROUP_PREFIX + ResourceActions.ACTION_VIEW;
     public final static String ACTION_ATTRIBUTE_GROUP_CREATE = ACTION_ATTRIBUTE_GROUP_PREFIX + ResourceActions.ACTION_CREATE;
     public final static String ACTION_ATTRIBUTE_GROUP_MODIFY = ACTION_ATTRIBUTE_GROUP_PREFIX + ResourceActions.ACTION_MODIFY;
     public final static String ACTION_ATTRIBUTE_GROUP_DELETE = ACTION_ATTRIBUTE_GROUP_PREFIX + ResourceActions.ACTION_DELETE;
     public final static String ACTION_ATTRIBUTE_GROUP_LIST = ACTION_ATTRIBUTE_GROUP_PREFIX + ResourceActions.ACTION_LIST;
 }
