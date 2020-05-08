 // cinnamon - the Open Enterprise CMS project
 // Copyright (C) 2007-2009 Horner GmbH (http://www.horner-project.eu)
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 // (or visit: http://www.gnu.org/licenses/lgpl.html)
 
 package cinnamon.global;
 
 public class Constants {
 
     public static final String SERVER_VERSION = "3.0.0.3";
     
 	public static final String PS_LABEL_CREATED="_created";
 	public static final String ACL_DEFAULT="_default_acl";
 
 	public static final String OBJTYPE_DEFAULT = "_default_objtype";
     public static final String OBJTYPE_TRANSLATION_TASK = "_translation_task";
 	public static final String OBJTYPE_WORKFLOW = "_workflow";
 	/**
 	 * The name of the object type for workflow template objects.
 	 */
 	public static final String OBJTYPE_WORKFLOW_TEMPLATE = "_workflow_template";
 	public static final String OBJTYPE_TASK_DEFINITION= "_task_definition";
 	public static final String OBJTYPE_TASK = "_task";
 	public static final String OBJTYPE_SEARCH = "_search";
 	public static final String OBJTYPE_SUPPORTING_DOCUMENT = "_supporting_document";
 	public static final String OBJTYPE_RENDITION = "_rendition";
 	public static final String OBJTYPE_CART = "_cart";
 	public static final String OBJTYPE_CONFIG = "_config";
 	public static final String OBJTYPE_IMAGE = "image";
 	public static final String OBJTYPE_DOCUMENT = "document";
 
 	public static final String FOLDER_TYPE_DEFAULT = "_default_folder_type";
 	public static final String INDEX_GROUP_DEFAULT = "_default_index_group";
 	
 	public static final String GROUP_SUPERUSERS="_superusers";
     public static final String GROUP_USERS = "_users";
 	public static final String ALIAS_EVERYONE="_everyone";
 	public static final String ALIAS_OWNER="_owner";
 	
 	public static final String ROOT_FOLDER_NAME = "root";
 	public static final String SYSTEM_FOLDER_NAME = "system";
 	public static final String WORKFLOW_FOLDER_NAME = "workflows";
 	public static final String WORKFLOW_TEMPLATE_FOLDER_NAME = "templates";
 	public static final String WORKFLOW_TASK_DEFINITION_FOLDER_NAME = "task_definitions";
 	public static final String WORKFLOW_TASK_FOLDER_NAME = "tasks";
 
     public static final String RELATION_RESOLVER_FIXED = "FixedRelationResolver";
 	public static final String RELATION_TYPE_WORKFLOW_TO_START_TASK = "_workflow_start_task";
 	public static final String RELATION_TYPE_WORKFLOW_TO_TASK = "_workflow_task";
 	public static final String RELATION_TYPE_WORKFLOW_TO_DEADLINE_TASK = "_workflow_deadline_task";
     public static final String RELATION_TYPE_CHILD = "child_content";
     public static final String RELATION_TYPE_RENDITION = "rendition";
     public static final String RELATION_TYPE_TRANSLATION_SOURCE = "translation_source";
     public static final String RELATION_TYPE_TRANSLATION_ROOT = "translation_root";
     public static final String RELATION_TYPE_CHILD_NO_CONTENT = "child_no_content";
     public static final String RELATION_TYPE_TRANSLATION_SOURCE_LIST = "translation_source_list";
     public static final String RELATION_TYPE_TRANSLATION_TARGET_LIST = "translation_target_list";
 
     public static final String USER_SUPERADMIN_NAME = "admin";
 
 	public static final String WORKFLOW_FOLDER_PATH = "/root/system/workflows";
 	public static final String WORKFLOW_TEMPLATE_PATH = WORKFLOW_FOLDER_PATH +"/"+ WORKFLOW_TEMPLATE_FOLDER_NAME;
 	public static final String WORKFLOW_TASK_PATH = WORKFLOW_FOLDER_PATH +"/"+ WORKFLOW_TASK_FOLDER_NAME;
 	public static final String WORKFLOW_TASK_DEFINITION_PATH = WORKFLOW_FOLDER_PATH + "/" + WORKFLOW_TASK_DEFINITION_FOLDER_NAME;
 
 	public static final String PROCSTATE_TASK_TODO = "todo";
 	public static final String PROCSTATE_TASK_DONE = "done";
 	public static final String PROCSTATE_TRANSITION_READY = "transition_ready";
	public static final String PROCSTATE_TRANSITION_FAILED = "transition_failed";
 	public static final String PROCSTATE_WORKFLOW_STARTED = "running";
 	public static final String PROCSTATE_WORKFLOW_FINISHED = "finished";
 	public static final String PROCSTATE_REVIEW_OK = "review_ok";
 
    public static final String METASET_TASK_DEFINITION = "task_definition";
    public static final String METASET_WORKFLOW_TEMPLATE = "workflow_template";
    public static final String METASET_LOG = "log";
    public static final String METASET_TRANSITION = "transition";
    public static final String METASET_RENDER_INPUT = "render_input";
    public static final String METASET_RENDER_OUTPUT = "render_output";
    public static final String METASET_TIKA = "tika";
    public static final String METASET_TEST = "test";
    public static final String METASET_TRANSLATION_EXTENSION = "translation_extension";
    public static final String METASET_SEARCH = "search";
    public static final String METASET_CART = "cart";
    public static final String METASET_NOTIFICATION = "notification";
    
     // Constants for the RenderServer extension
     public static final String RENDER_SERVER_LIFECYCLE = "_RenderServerLC";
     public static final String RENDERSERVER_RENDER_TASK_NEW = "newRenderTask";
     public static final String RENDERSERVER_RENDER_TASK_RENDERING = "executeRenderTask";
     public static final String RENDERSERVER_RENDER_TASK_FINISHED = "finishedRenderTask";
     public static final String RENDERSERVER_RENDER_TASK_FAILED = "failedRenderTask";
     public static final String OBJECT_TYPE_RENDER_TASK = "_render_task";
     
     /**
      * Metaset type for thumbnail images embedded in custom xml data. 
      */
     public static final String METASET_THUMBNAIL = "thumbnail";
     
     /**
      * Maximum length of cinnamon.i18n.Language.isoCode
      */
     public static final int MAX_ISO_CODE_LENGTH = 32;
 
     public static final int LARGE_TEXT_FIELD = 10485760;
     // max_field_size in postgres is 1 GByte, but somehow Hibernate creates a varchar instead of text and that is
     // limited to 10 MByte.
 
     public static final int METADATA_SIZE = LARGE_TEXT_FIELD;
     public static final int NAME_LENGTH = 128;
 
 	public static final int DESCRIPTION_SIZE = 255;
 
 	/**
 	 * Default length of database fields which contain XPATH statements.
 	 */
 	public static final int XPATH_LENGTH = LARGE_TEXT_FIELD;
 	
 	/**
 	 * Default length of database fields which contain XML parameters.
 	 */
 	public static final int XML_PARAMS = LARGE_TEXT_FIELD;
 
     /*
      * We cannot make the INDEX_* and FIELD_* into an enum, because the user
      * may add their own index items and fields dynamically. But the constants here
      * are to be used for code that needs to refer to system-relevant index items and fields.
      * Also, for historical reasons, the name of the index item is not always fully similar to the field name.
      */
     public static final String INDEX_FOLDER_OWNER = "index.folder.owner";
     public static final String FIELD_FOLDER_OWNER = "owner";
     public static final String INDEX_FOLDER_NAME = "index.folder.name";
     public static final String FIELD_FOLDER_NAME = "name";
     public static final String INDEX_FOLDER_TYPE = "index.folder.type";
     public static final String FIELD_FOLDER_TYPE = "folderType";
     public static final String INDEX_FOLDER_PARENT_ID = "index.folder.parent_id";
     public static final String FIELD_FOLDER_PARENT_ID = "parent_id";
     public static final String INDEX_FOLDER_PATH = "index.folder.path";
     public static final String FIELD_FOLDER_PATH = "folderpath";
     public static final String INDEX_PROCSTATE = "index.procstate";
     public static final String FIELD_PROCSTATE = "procstate";
     public static final String INDEX_APPNAME = "index.appname";
     public static final String FIELD_APPNAME = "appname";
     public static final String INDEX_VERSION = "index.version";
     public static final String FIELD_VERSION = "version";
     public static final String INDEX_NAME = "index.name";
     public static final String FIELD_NAME = "name";
     public static final String INDEX_OBJECT_TYPE_NAME = "index.object_type_name";
     public static final String FIELD_OBJECT_TYPE_NAME = "objectTypeName";
     public static final String INDEX_ROOT = "index.root";
     public static final String FIELD_ROOT = "root";
     public static final String INDEX_PARENT_ID = "index.parentId";
     public static final String FIELD_PARENT_ID = "parent_id";
     public static final String INDEX_CONTENT_SIZE = "index.contentsize";
     public static final String FIELD_CONTENT_SIZE = "contentsize";
     public static final String INDEX_LOCKED_BY = "index.lockedby";
     public static final String FIELD_LOCKED_BY = "lockedby";
     public static final String INDEX_CREATOR = "index.creator";
     public static final String FIELD_CREATOR = "creator";
     public static final String INDEX_MODIFIER = "index.modifier";
     public static final String FIELD_MODIFIER = "modifier";
     public static final String INDEX_OWNER = "index.owner";
     public static final String FIELD_OWNER = "owner";
     public static final String INDEX_FORMAT = "index.format";
     public static final String FIELD_FORMAT = "format";
     public static final String INDEX_OBJECT_TYPE = "index.objecttype";
     public static final String FIELD_OBJECT_TYPE = "objecttype";
     public static final String INDEX_LANGUAGE = "index.language";
     public static final String FIELD_LANGUAGE = "language";
     public static final String INDEX_ACL = "index.acl";
     public static final String FIELD_ACL = "acl";
     public static final String INDEX_LIFECYCLE_STATE = "index.lifecycle.state";
     public static final String FIELD_LIFECYCLE_STATE = "lifecyclestate";
     public static final String INDEX_CREATED_DATE = "index.created.date";
     public static final String FIELD_CREATED_DATE = "created.date";
     public static final String INDEX_MODIFIED_DATE = "index.modified.date";
     public static final String FIELD_MODIFIED_DATE = "modified.date";
     public static final String INDEX_CREATED_TIME = "index.created.time";
     public static final String FIELD_CREATED_TIME = "created.time";
     public static final String INDEX_MODIFIED_TIME = "index.modified.time";
     public static final String FIELD_MODIFIED_TIME = "modified.time";
     public static final String INDEX_LATEST_HEAD = "index.latesthead";
     public static final String FIELD_LATEST_HEAD = "latesthead";
     public static final String INDEX_LATEST_BRANCH = "index.latestbranch";
     public static final String FIELD_LATEST_BRANCH = "latestbranch";
     public static final String INDEX_PATH = "index.path";
     public static final String FIELD_PATH = "folderpath";
     public static final String INDEX_TIKA = "index.tika";
     public static final String FIELD_TIKA = "content";
     public static final String INDEX_ACTIVE_WORKFLOW = "index.workflow.active_workflow";
     public static final String FIELD_ACTIVE_WORKFLOW = "active_workflow";
     public static final String INDEX_WORKFLOW_DEADLINE = "index.workflow_deadline";
     public static final String FIELD_WORKFLOW_DEADLINE = "workflow_deadline";
     
     public static void main(String... args){
         System.out.print(Constants.SERVER_VERSION);
     }
 }
