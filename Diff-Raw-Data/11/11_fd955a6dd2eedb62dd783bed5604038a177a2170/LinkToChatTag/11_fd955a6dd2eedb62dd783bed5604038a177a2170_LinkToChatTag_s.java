 /*
 Copyright 2009 Samuel Marshall
 http://www.leafdigital.com/software/hawthorn/
 
 This file is part of Hawthorn.
 
 Hawthorn is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Hawthorn is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Hawthorn.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.leafdigital.hawthorn.jsp;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import com.leafdigital.hawthorn.util.*;
 
 /** Tag displays recent messages and users on a Hawthorn chat channel. */
 public class LinkToChatTag extends SimpleTagSupport
 {
 	static final String HAWTHORN_LINK_TO_CHAT_COUNT =
 		"hawthorn.linkToChatCount";
 
 	private String channel,title,icon,iconAlt="Opens in new window";
 
 	@Override
 	public void doTag() throws JspException, IOException
 	{
 		// Get init tag with basic settings
 		InitTag init =
 			(InitTag)getJspContext().getAttribute(InitTag.HAWTHORN_INIT_TAG);
 		if(init==null)
 		{
 			throw new JspException("Cannot use <linkToChat> without <init>");
 		}
 
 		// Get index of this tag within page
 		Integer previousCount =
 			(Integer)getJspContext().getAttribute(HAWTHORN_LINK_TO_CHAT_COUNT);
 		int index = previousCount==null ? 0 : previousCount;
 		getJspContext().setAttribute(HAWTHORN_LINK_TO_CHAT_COUNT, index+1);
 
 		// Get auth code
 		long time = System.currentTimeMillis() + init.getKeyExpiry();
 		String key = init.getKey(channel, time, false);
 
 		// Output div
 		getJspContext().getOut().println("<div class='hawthorn_linktochat' "
 			+ "style='display:none' id='hawthorn_linktochat"+index+"'>");
 		getJspContext().getOut().println(
 			"<script type='text/javascript'>document.getElementById("
 			+ "'hawthorn_linktochat"+index+"').style.display='block';</script>");
 
 		// Output link
 		getJspContext().getOut().println(
 			"<a href='#' onclick=\"return hawthorn.openPopup('"
 			+ JS.escInlineAttr(init.getPopupUrl()) + "','"
 			+ JS.escInlineAttr(init.getReAcquireUrl()) + "','"
 			+ channel + "','"
 			+ init.getUser() + "','" + JS.escInlineAttr(init.getDisplayName()) + "','"
 			+ Auth.getPermissions(init.getPermissionSet()) + "',"
 			+ time+",'" + key + "','" + JS.escInlineAttr(title) + "');\">");
 
 		// Print content
 		getJspBody().invoke(null);
 
 		// Print icon if provided
 		if(icon != null)
 		{
 			getJspContext().getOut().println(
 				" <img src='"+XML.esc(icon)+"' alt='"+XML.esc(iconAlt)+"' title='"+
 				XML.esc(iconAlt)+"' />");
 		}
 
 		// CLose tags
 		getJspContext().getOut().println("</a></div>");
 
 		// Print script tag if not deferred
 		init.printJS(getJspContext().getOut(),false);
 	}
 
 	/** @param channel Hawthorn channel ID */
 	public void setChannel(String channel)
 	{
 		this.channel = channel;
 	}
 
 	/** @param title Popup window title */
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 
 	/** @param icon Icon URL (may be relative) */
 	public void setIcon(String icon)
 	{
 		this.icon = icon;
 	}
 
 	/** @param iconAlt Alt text/title for icon */
 	public void setIconAlt(String iconAlt)
 	{
 		this.iconAlt = iconAlt;
 	}
 }
