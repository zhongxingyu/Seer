 package com.redhat.ecs.services.docbookcompiling;
 
 public class DocbookBuilderConstants
 {
 
 	/** The Docbook role (which becomes a CSS class) for the bug link para */
 	public static final String ROLE_CREATE_BUG_PARA = "RoleCreateBugPara";
 	/** The Docbook role (which becomes a CSS class) for the skynet link para */
 	public static final String ROLE_VIEW_IN_SKYNET_PARA = "RoleViewInSkyNetPara";
 	/**
 	 * The Docbook role (which becomes a CSS class) for the Skynet build version
 	 * para
 	 */
 	public static final String ROLE_BUILD_VERSION_PARA = "RoleBuildVersionPara";
 	
 	/** The Concept tag ID */
 	public static final Integer CONCEPT_TAG_ID = 5;
 	/** The Concept tag name */
 	public static final String CONCEPT_TAG_NAME = "Concept";
 	/** The Conceptual Overview tag ID */
 	public static final Integer CONCEPTUALOVERVIEW_TAG_ID = 93;
 	/** The Conceptual Overview tag name */
 	public static final String CONCEPTUALOVERVIEW_TAG_NAME = "Overview";
 	/** The Reference tag ID */
 	public static final Integer REFERENCE_TAG_ID = 6;
 	/** The Reference tag name */
 	public static final String REFERENCE_TAG_NAME = "Reference";
 	/** The Task tag ID */
 	public static final Integer TASK_TAG_ID = 4;
 	/** The Task tag name */
 	public static final String TASK_TAG_NAME = "Task";
 	/** The HTML returned when a Topic's XML could not be transformed */
 	public static final String XSL_ERROR_TEMPLATE = "<html><head><title>ERROR</title></head><body>The topic could not be transformed into HTML</body></html>";
 	/** The HTML returned when a Topic's XML could not be transformed */
 	public static final String XML_ERROR_TEMPLATE = "The topic has invalid XML";
 	/** The encoding of the XML, used when converting a DOM object to a string */
 	public static final String XML_ENCODING = "UTF-8";
 
 	/** The Tag Description tag ID */
 	public static final Integer TAG_DESCRIPTION_TAG_ID = 215;
 	
 	public static final Integer TYPE_CATEGORY_ID = 4;
 	public static final Integer TECHNOLOGY_CATEGORY_ID = 3;
 	public static final Integer COMMON_NAME_CATEGORY_ID = 17;
 	public static final Integer CONCERN_CATEGORY_ID = 2;
 	public static final Integer WRITER_CATEGORY_ID = 12;
 	/** The Home tag ID */
 	public static final Integer HOME_TAG_ID = 216;
 	/** The BlobConstant ID for the Rocbook DTD */
 	public static final Integer ROCBOOK_DTD_BLOB_ID = 9;
 	
 	/** The StringConstantsID that represents the Revision_History.xml file */
 	public static final Integer REVISION_HISTORY_XML_ID = 15;
 	/** The StringConstantsID that represents the Book.xml file */
 	public static final Integer BOOK_XML_ID = 1;
 	/** The StringConstantsID that represents the Book.ent file */
 	public static final Integer BOOK_ENT_ID = 2;
 	/** The StringConstantsID that represents the Book_Info.xml file */
 	public static final Integer BOOK_INFO_XML_ID = 3;
 	/** The StringConstantsID that represents the Author_Group.xml file */
 	public static final Integer AUTHOR_GROUP_XML_ID = 4;
 	/** The StringConstantsID that represents the publican.cfg file */
 	public static final Integer PUBLICAN_CFG_ID = 5;
 	/** The StringConstantsID that represents the icon.svg file */
 	public static final Integer ICON_SVG_ID = 6;
 	/** The StringConstantsID that represents the error topic xml template */
 	public static final Integer ERROR_TOPIC_ID = 7;
 	/** The StringConstantsID that represents the error topic xml template */
 	public static final Integer ERRORTAGS_TOPIC_ID = 8;
 	/** The StringConstantsID that represents the Makefile file */
 	public static final Integer MAKEFILE_ID = 16;
 	/** The StringConstantsID that represents the spec.in file */
 	public static final Integer SPEC_IN_ID = 17;
 	/** The StringConstantsID that represents the package.sh file */
 	public static final Integer PACKAGE_SH_ID = 18;
 	/** The StringConstantsID that represents the StartPage.xml file */
 	public static final Integer START_PAGE_ID = 19;
 	/** The StringConstantsID that represents the jboss.svg file */
 	public static final Integer JBOSS_SVG_ID = 20;
 	/** The StringConstantsID that represents the yahoo_dom_event.js file */
 	public static final Integer YAHOO_DOM_EVENT_JS_ID = 21;
 	/** The StringConstantsID that represents the treeview_min.js file */
 	public static final Integer TREEVIEW_MIN_JS_ID = 22;
 	/** The StringConstantsID that represents the treeview.css file */
 	public static final Integer TREEVIEW_CSS_ID = 23;
 	/** The StringConstantsID that represents the jquery_min.js file */
 	public static final Integer JQUERY_MIN_JS_ID = 24;
 
 	public static final Integer TREEVIEW_SPRITE_GIF_ID = 1;
 	public static final Integer TREEVIEW_LOADING_GIF_ID = 2;
 	public static final Integer CHECK1_GIF_ID = 3;
 	public static final Integer CHECK2_GIF_ID = 4;
 	public static final Integer FAILPENGUIN_PNG_ID = 5;
 
 	public static final Integer PLUGIN_XML_ID = 25;
 	public static final Integer ECLIPSE_PACKAGE_SH_ID = 26;
 	public static final Integer PUBLICAN_ECLIPSE_CFG_ID = 27;
 	
 	/** A marker to replace with the date in a string */
 	public static final String DATE_YYMMDD_TEXT_MARKER = "#YYMMDD#";
 	public static final String BOOK_XML_XI_INCLUDES_MARKER = "#XI_INCLUDES#";
 
 	/** This marker is replaced with the SkyNet build version */
 	public static final String BUILD_MARKER = "#SKYNETBUILD#";
 	/**
 	* A marker that can be included in generic sections and replaced with
 	* specific data
 	*/
 	public static final String ROLE_MARKER = "#ROLE#";
 	/**
 	* A marker that can be included in generic sections and replaced with
 	* specific data
 	*/
 	public static final String TAG_FILTER_URL_MARKER = "#TAGFILTERURL#";
 	/**
 	* A marker that can be included in generic sections and replaced with
 	* specific data
 	*/
 	public static final String TOPIC_ID_MARKER = "#TOPICID#";
 	/**
 	* A marker that can be included in generic sections and replaced with
 	* specific data
 	*/
 	public static final String TOPIC_ERROR_LINK_MARKER = "#TOPICERRORLINK#";
 	
 	/** A prefix for error xref ids */
 	public static final String ERROR_XREF_ID_PREFIX = "TagErrorXRef";
 }
