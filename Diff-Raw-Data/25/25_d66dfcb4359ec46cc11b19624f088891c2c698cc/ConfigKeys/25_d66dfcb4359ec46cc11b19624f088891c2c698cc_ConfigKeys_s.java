 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * Created on May 29, 2004 by pieter
  *
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.util.preferences;
 
 /**
 * Encapsulate all configuration keys in constants. This is more typesafe and provides
 * a nice overview of all configuration keys. Last but not least this lets us autocomplete
 * configuration keys under eclipse ;-)
 * 
 * @author pieter
* @version $Id: ConfigKeys.java,v 1.11 2004/08/19 04:03:28 jamesyong Exp $
 */
 
 public class ConfigKeys {
 
 	// Configuration values for the keys in this section are set by the web-application itself
 	public static final String APPLICATION_PATH = "application.path";
 	public static final String INSTALLATION = "installation";
 
 	// The installation config file contains keys that are installation specific and differ from
 	// the default config.
 	public static final String INSTALLATION_CONFIG = "installation.config";
 	
 	public static final String FILECHANGES_DELAY = "file.changes.delay";
 
 	// The remaining keys are set in default.config and can be overriden in the installation config 
 	public static final String RESOURCE_DIR = "resource.dir";
 	public static final String CONFIG_DIR = "config.dir";
 	public static final String DATABASE_PROPERTIES = "database.properties";
 	public static final String DATABASE_DRIVER_PROPERTIES = "database.driver.properties";
 	public static final String SQL_QUERIES_GENERIC = "sql.queries.generic";
 	public static final String SQL_QUERIES_DRIVER = "sql.queries.driver";
 
 	public static final String ENCODING = "encoding";
 	public static final String SERVLET_NAME = "servlet.name";	// transient!
 	public static final String DEFAULT_CONFIG = "default.config"; // transient
 	public static final String CONTEXT_NAME = "context.name"; // transient
 	public static final String SERVLET_EXTENSION = "servlet.extension";
 	public static final String COOKIE_NAME_DATA = "cookie.name.data";
 	public static final String COOKIE_NAME_USER = "cookie.name.user";
 	public static final String COOKIE_AUTO_LOGIN = "cookie.name.autologin";
 	public static final String COOKIE_USER_HASH = "cookie.name.userHash";
 		
 	public static final String ANONYMOUS_USER_ID = "anonymous.userId";
 	public static final String DEFAULT_USER_GROUP = "defaultUserGroup";
 	public static final String USER_HASH_SEQUENCE = "user.hash.sequence";
 
 	public static final String VERSION = "version";
 
 	public static final String FORUM_LINK = "forum.link";
 	public static final String FORUM_NAME = "forum.name";
 	public static final String FORUM_PAGE_TITLE = "forum.page.title";
 	public static final String FORUM_PAGE_METATAG_KEYWORDS = "forum.page.metatag.keywords";
 	public static final String FORUM_PAGE_METATAG_DESCRIPTION = "forum.page.metatag.description";
 
 	public static final String TMP_DIR = "tmp.dir";
 	public static final String CACHE_DIR = "cache.dir";
 
 	public static final String DAO_DRIVER = "dao.driver";
 
 	public static final String DATE_TIME_FORMAT = "dateTime.format";
 
 	public static final String TOPICS_PER_PAGE = "topicsPerPage";
 	public static final String POST_PER_PAGE = "postsPerPage";
 
 	public static final String RSS_FILENAME_FORUM = "rss.filename.forum";
 	public static final String RSS_FILENAME_TOPIC = "rss.filename.topic";
 	public static final String RSS_TOPICS = "rss.topics";
 
 	public static final String I18N_DEFAULT = "i18n.default";
	public static final String I18N_USER_DEFAULT = "i18n.user.default";
 
 	public static final String MAIL_LOST_PASSWORD_MESSAGE_FILE = "mail.lostPassword.messageFile";
 	public static final String MAIL_NOTIFY_ANSWERS = "mail.notify.answers";
 	public static final String MAIL_SENDER = "mail.sender";
 	public static final String MAIL_CHARSET = "mail.charset";
 	public static final String MAIL_NEW_ANSWER_MESSAGE_FILE = "mail.newAnswer.messageFile";
 	public static final String MAIL_NEW_ANSWER_SUBJECT = "mail.newAnswer.subject";
 	public static final String MAIL_NEW_PM_SUBJECT = "mail.newPm.subject";
 	public static final String MAIL_NEW_PM_MESSAGE_FILE = "mail.newPm.messageFile";
 	public static final String MAIL_MESSSAGE_FORMAT = "mail.messageFormat";
 	public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
 	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
 	public static final String MAIL_SMTP_USERNAME = "mail.smtp.username";
 	public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
 
 	public static final String SMILIE_IMAGE_DIR = "smilie.image.dir";
 	public static final String SMILIE_IMAGE_PATTERN = "smilie.image.pattern";
 
 	public static final String AVATAR_MAX_WIDTH = "avatar.maxWidth";
 	public static final String AVATAR_MAX_HEIGHT = "avatar.maxHeight";
 
 	public static final String LOGIN_MODE = "loginMode";
 	public static final String LOGIN_SERVER_CLASS = "loginServer.class";
 
 	public static final String EXTERNAL_CONNECTION = "external.database.connection.string";
 	public static final String EXTERNAL_QUERY_VALIDATE_LOGIN = "external.database.query.validateLogin";
 	public static final String EXTERNAL_QUERY_SELECT_NAME = "external.database.query.selectName";
 	public static final String EXTERNAL_QUERY_SELECT_GROUPS = "external.database.query.selectGroups";
 	public static final String EXTERNAL_UNIQUE_NAMES = "external.uniqueNames";
 	
 
 	private ConfigKeys() {
 	}
 }
