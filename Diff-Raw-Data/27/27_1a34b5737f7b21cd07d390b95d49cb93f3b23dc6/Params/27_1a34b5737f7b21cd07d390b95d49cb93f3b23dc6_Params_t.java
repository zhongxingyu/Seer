 package org.jbundle.thin.base.db;
 
 /**
  * @(#)Params.java  1.16 95/12/14 Don Corley
  *
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  *
  */
 
 /**
  * Parameter keys to set for an application or applet.
  */
 public interface Params
 {
     /**
      * The fieldname for most primary keys in the system.
      */
     public static final String ID = "ID";
     /**
      * RMI Server name and port number (if not the RMI port 1099).
      */
     public static final String REMOTE_HOST = "remotehost";
    public static final String CONNECTION_TYPE = "connectionType";
     /**
      * Constant none.
      */
     public static final String NONE = "none";
     /**
      * For standalone apps, you need to know where your resources are.
      */
     public static final String CODEBASE = "codebase";
     /**
      * The RMI resource name (for name lookup).
      */
     public static final String REMOTEAPP = "remoteapp";
     /**
      * The default RMI resource name.
      */
     public static final String DEFAULT_REMOTE_APP = "appserver";
     /**
      * Background image.
      */
     public static final String BACKGROUND = "background";
     /**
      * Background color.
      */
     public static final String BACKGROUNDCOLOR = "backgroundcolor";
     /**
      * Resource file.
      */
     public static final String RESOURCE = "resource";
     /**
      * Server name.
      */
     public static final String SERVLET = "servlet";
     public static final String XHTMLSERVLET = "xmlservlet";
     public static final String IMAGE_PATH = "image";
     public static final String TABLE_PATH = "table";
     public static final String IMAGE_PARAM = "image";
     public static final String TABLE_PARAM = "table";
     public static final String WEBSTART_PARAM = "jnlp";
     public static final String WEBSTART_APPLET_PARAM = "jnlpapplet";
     public static final String WSDL_PARAM = "wsdl";
     public static final String HELP = "help";         // Help page
 
     /**
      * User name.
      */
     public static final String USER_NAME = "user";
     /**
      * User ID.
      */
     public static final String USER_ID = "userid";
     /**
      * 
      */
     public static final String PASSWORD = "password";
     /**
      * 
      */
     public static final String DOMAIN = "domain";
     /**
      * 
      */
     public static final String AUTH_TOKEN = "auth";
     /**
      * 
      */
     public static final String SECURITY_LEVEL = "securityLevel";
     /**
      * 
      */
     public static final String SECURITY_MAP = "securityMap";
     /**
      * 
      */
     public static final String USER_PROPERTIES = "userProperties";
     /**
     *
     */
     public static final String CONTACT_TYPE = "contactType";
     /**
      *
      */
     public static final String CONTACT_ID = "contactID";
     /**
      *
      */
     public static final String TASK = "task";
     /**
      * Applet page
      */
     public static final String APPLET = "applet";
     /**
      * The current language.
      */
     public static final String LANGUAGE = "language";
     /**
      * Pass a screen class.
      */
     public static final String SCREEN = "screen";
     /**
      * The menu param.
      */
     public static final String MENU = "menu";
     /**
      * Mail param.
      */
     public static final String MAIL = "mail";   // E-Mail address
     /**
      * Phone param.
      */
     public static final String PHONE = "phone";
     /**
      * Fax param.
      */
     public static final String FAX = "fax";
     /**
      * URL param.
      * Note: This param is used in two independent areas: 1. to display a screen in a JHtml panel and
      * 2. to get the URL in a Servlet. This should not cause a conflict.
      */
     public static final String URL = "url";
     /**
      * Field.
      */
     public static final String FIELD = "field";
     /**
      * Command to get the field data from a session.
      */
     public static final String GET_FIELD_DATA = "getFieldData";
     /**
      * Command to get the User Registration Properties from a session.
      */
     public static final String RETRIEVE_USER_PROPERTIES = "retrieveUserProperties";
     /**
      * Command to get the User Registration Properties from a session.
      */
     public static final String SAVE_USER_PROPERTIES = "saveUserProperties";
     
     public static final String NAVMENUS = "navmenus";
     public static final String MENUBARS = "menubars";
     public static final String TRAILERS = "trailers";
     public static final String LOGOS = "logos";
 }
