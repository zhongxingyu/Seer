 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser;
 
 import java.util.Hashtable;
 import java.util.Locale;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.WikiUser;
 
 /**
  * This class is a utility class used to hold configuration settings for the
  * parser.
  */
 public class ParserInput {
 
 	private boolean allowSectionEdit = true;
 	private String context = null;
 	/** Depth is used to prevent infinite nesting of templates and other objects. */
 	private int depth = 0;
 	private Locale locale = null;
 	private TableOfContents tableOfContents = new TableOfContents();
 	/** Template inclusion tracks whether or not template code is being parsed.  A counter is used to deal with nested templates. */
 	private int templateDepth = 0;
 	/** Hashtable of generic temporary objects used during parsing. */
 	private final Hashtable tempParams = new Hashtable();
 	private String topicName = null;
 	/** IP address of the current user. */
 	private String userIpAddress = null;
 	private String virtualWiki = null;
 	/** Current WikiUser (if any). */
 	private WikiUser wikiUser = null;
 
 	/**
 	 *
 	 */
 	public ParserInput() {
 	}
 
 	/**
 	 * This method will return <code>true</code> if edit links are allowed
 	 * next to each section heading.  During preview and in some other
 	 * instances that feature needs to be disabled.
 	 *
 	 * @return Returns <code>true</code> if edit links are allowed next to
 	 *  each section heading.
 	 */
 	public boolean getAllowSectionEdit() {
 		return allowSectionEdit;
 	}
 
 	/**
 	 * Set method used to indicate whether or not to allow edit links
 	 * next to each section heading.  During preview and in some other
 	 * instances that feature needs to be disabled.
 	 *
 	 * @param allowSectionEdit Set to <code>true</code> if edits links are
 	 *  allowed next to each section heading, <code>false</code> otherwise.
 	 */
 	public void setAllowSectionEdit(boolean allowSectionEdit) {
 		this.allowSectionEdit = allowSectionEdit;
 	}
 
 	/**
 	 * Get the servlet context associated with the current parser input
 	 * instance.  Servlet context is used when building links.
 	 *
 	 * @return The servlet context associated with the current parser
 	 *  input instance.
 	 */
 	public String getContext() {
 		return context;
 	}
 
 	/**
 	 * Set the servlet context associated with the current parser input
 	 * instance.  Servlet context is used when building links.
 	 *
 	 * @param context The servlet context associated with the current parser
 	 *  input instance.
 	 */
 	public void setContext(String context) {
 		this.context = context;
 	}
 
 	/**
 	 * Since it is possible to call a new parser instance from within another
 	 * parser instance, depth provides a way to determine how many times the
 	 * parser has nested, thus providing a way of avoiding infinite loops.
 	 *
 	 * @return The current nesting level of the parser instance.
 	 */
 	public int getDepth() {
 		return depth;
 	}
 
 	/**
 	 * This method decreases the current parser instance depth and should
 	 * only be called when a parser instance exits.  Depth is useful as a
 	 * way of avoiding infinite loops in the parser.
 	 */
 	public void decrementDepth() {
 		this.depth--;
 	}
 
 	/**
 	 * This method increases the current parser instance depth and should
 	 * only be called when a instantiating a new parser instance.  Depth is
 	 * useful as a way of avoiding infinite loops in the parser.
 	 */
 	public void incrementDepth() {
 		this.depth++;
 	}
 
 	/**
 	 * Since it is possible to call a new parser instance from within another
 	 * parser instance, depth provides a way to determine how many times the
 	 * parser has nested, thus providing a way of avoiding infinite loops.
 	 *
 	 * @param depth The current nesting level of the parser instance.
 	 */
 	public void setDepth(int depth) {
 		this.depth = depth;
 	}
 
 	/**
 	 * Get the locale associated with the current parser input instance.
 	 * Locale is used primarily when building links or displaying messages.
 	 *
 	 * @return The locale associated with the current parser input instance.
 	 */
 	public Locale getLocale() {
 		return locale;
 	}
 
 	/**
 	 * Set the locale associated with the current parser input instance.
 	 * Locale is used primarily when building links or displaying messages.
 	 *
 	 * @param locale The locale associated with the current parser input
 	 *  instance.
 	 */
 	public void setLocale(Locale locale) {
 		this.locale = locale;
 	}
 
 	/**
 	 * Get the table of contents object associated with the current parser
 	 * input instance.  The table of contents is used for building an internal
 	 * set of links to headings in the current document.
 	 *
 	 * @return The table of contents object associated with the current parser
 	 *  input instance.
 	 */
 	public TableOfContents getTableOfContents() {
 		return this.tableOfContents;
 	}
 
 	/**
 	 * Set the table of contents object associated with the current parser
 	 * input instance.  The table of contents is used for building an internal
 	 * set of links to headings in the current document.
 	 *
 	 * @param tableOfContents The table of contents object associated with the
 	 *  current parser input instance.
 	 */
 	public void setTableOfContents(TableOfContents tableOfContents) {
 		this.tableOfContents = tableOfContents;
 	}
 
 	/**
 	 * Get the Hashtable of arbitrary temporary parameters associated with
 	 * the current parser input instance.  This hashtable provides a method
 	 * for the parser to keep track of arbitrary data during the parsing
 	 * process.
 	 *
 	 * @return The Hashtable of arbitrary temporary parameters associated with
 	 *  the current parser input instance.
 	 */
 	public Hashtable getTempParams() {
 		return this.tempParams;
 	}
 
 	/**
 	 * Get the depth level when template code is being parsed.
 	 *
 	 * @return The current number of template inclusions.
 	 */
 	public int getTemplateDepth() {
 		return templateDepth;
 	}
 
 	/**
 	 * This method decreases the current template inclusion depth and should
 	 * only be called when a template finishes processing.
 	 */
 	public void decrementTemplateDepth() {
 		this.templateDepth--;
 	}
 
 	/**
 	 * This method decreases the current template inclusion depth and should
 	 * only be called when a template begins processing.
 	 */
 	public void incrementTemplateDepth() {
 		this.templateDepth++;
 	}
 
 	/**
 	 * Set the depth level when template code is being parsed.
 	 *
 	 * @param templateDepth The current number of template inclusions.
 	 */
 	public void setTemplateDepth(int templateDepth) {
 		this.templateDepth = templateDepth;
 	}
 
 	/**
 	 * Get the topic name for the topic being parsed by this parser input
 	 * instance.
 	 *
 	 * @return The topic name for the topic being parsed by this parser input
 	 * instance.
 	 */
 	public String getTopicName() {
 		return this.topicName;
 	}
 
 	/**
 	 * Set the topic name for the topic being parsed by this parser input
 	 * instance.
 	 *
 	 * @param topicName The topic name for the topic being parsed by this
 	 *  parser input instance.
 	 */
 	public void setTopicName(String topicName) {
 		this.topicName = topicName;
 	}
 
 	/**
 	 * Get the user IP address associated with the current parser input
 	 * instance.  The user IP address is used primarily when parsing
 	 * signatures.
 	 *
 	 * @return The user IP address associated with the current parser input
 	 * instance.
 	 */
 	public String getUserIpAddress() {
 		return this.userIpAddress;
 	}
 
 	/**
 	 * Set the user IP address associated with the current parser input
 	 * instance.  The user IP address is used primarily when parsing
 	 * signatures.
 	 *
 	 * @param userIpAddress The user IP address associated with the current
 	 *  parser input instance.
 	 */
 	public void setUserIpAddress(String userIpAddress) {
 		this.userIpAddress = userIpAddress;
 	}
 
 	/**
 	 * Get the virtual wiki name associated with the current parser input
 	 * instance.  The virtual wiki name is used primarily when parsing links.
 	 *
 	 * @return The virtual wiki name associated with the current parser input
 	 * instance.
 	 */
 	public String getVirtualWiki() {
 		return this.virtualWiki;
 	}
 
 	/**
 	 * Set the virtual wiki name associated with the current parser input
 	 * instance.  The virtual wiki name is used primarily when parsing links.
 	 *
 	 * @param virtualWiki The virtual wiki name associated with the current
 	 *  parser input instance.
 	 */
 	public void setVirtualWiki(String virtualWiki) {
 		this.virtualWiki = virtualWiki;
 	}
 
 	/**
 	 * Get the wiki user object associated with the current parser input
 	 * instance.  The wiki user object is used primarily when parsing
 	 * signatures.
 	 *
 	 * @return The wiki user object associated with the current parser input
 	 * instance.
 	 */
 	public WikiUser getWikiUser() {
 		return this.wikiUser;
 	}
 
 	/**
 	 * Set the wiki user object associated with the current parser input
 	 * instance.  The wiki user object is used primarily when parsing
 	 * signatures.
 	 *
 	 * @param user The wiki user object associated with the current
 	 *  parser input instance.
 	 */
 	public void setWikiUser(WikiUser user) {
		if (user!=null && !user.hasRole(Role.ROLE_USER)) {
 			// FIXME - setting the user to null may not be necessary, but it is
 			// consistent with how the code behaved when Utilities.currentUser()
 			// returned null for non-logged-in users
 			user = null;
 		}
 		this.wikiUser = user;
 	}
 }
