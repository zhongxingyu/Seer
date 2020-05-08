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
 package org.jamwiki.parser.jflex;
 
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Utility class used during parsing.  This class holds the elements of an
  * HTML tag (open tag, close tag, content) as it is generated during parsing.
  */
 class JFlexTagItem {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(JFlexTagItem.class.getName());
 
 	private String tagClose = null;
 	private final StringBuffer tagContent = new StringBuffer();
 	private String tagOpen = null;
 
 	/**
 	 *
 	 */
 	JFlexTagItem() {
 	}
 
 	/**
 	 *
 	 */
 	protected String getTagClose() {
 		return this.tagClose;
 	}
 
 	/**
 	 *
 	 */
 	protected void setTagClose(String tagClose) {
 		this.tagClose = tagClose;
 	}
 
 	/**
 	 *
 	 */
 	protected StringBuffer getTagContent() {
 		return this.tagContent;
 	}
 
 	/**
 	 *
 	 */
 	protected String getTagOpen() {
 		return this.tagOpen;
 	}
 
 	/**
 	 *
 	 */
 	protected void setTagOpen(String tagOpen) {
 		this.tagOpen = tagOpen;
 	}
 
 	/**
 	 *
 	 */
 	public String toString(boolean trim) {
 		String content = this.tagContent.toString();
 		if (trim) {
 			content = content.trim();
 		}
		if (this.tagOpen == null || this.tagClose == null) {
			return content;
 		}
		return this.tagOpen + content + this.tagClose;
 	}
 }
