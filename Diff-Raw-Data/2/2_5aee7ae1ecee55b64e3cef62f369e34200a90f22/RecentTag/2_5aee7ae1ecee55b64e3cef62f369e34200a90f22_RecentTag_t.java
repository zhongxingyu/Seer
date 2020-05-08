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
 public class RecentTag extends SimpleTagSupport
 {
 	static final String HAWTHORN_GET_RECENT_COUNT =
 		"hawthorn.recentCount";
 
 	private String channel,
 		loadingText="(Loading chat information, please wait...)",
 		noScriptText="(Chat features are not available because JavaScript is " +
 			"disabled.)",
 		recentText="Recent messages",
 		namesText="People in chat";
	private int maxMessages=3, maxAge=15*60*1000, maxNames=5, headingLevel=3;
 
 	@Override
 	public void doTag() throws JspException, IOException
 	{
 		// Get init tag with basic settings
 		InitTag init =
 			(InitTag)getJspContext().getAttribute(InitTag.HAWTHORN_INIT_TAG);
 		if(init==null)
 		{
 			throw new JspException("Cannot use <recent> without <init>");
 		}
 
 		// Get index of this recent within page
 		Integer previousCount =
 			(Integer)getJspContext().getAttribute(HAWTHORN_GET_RECENT_COUNT);
 		int index = previousCount==null ? 0 : previousCount;
 		getJspContext().setAttribute(HAWTHORN_GET_RECENT_COUNT, index+1);
 
 		// Output div
 		getJspContext().getOut().println("<div id='hawthorn_recent" + index
 			+ "' class='hawthorn_recent' style='display:none'>" +
 			XML.esc(loadingText) + "</div>");
 
 		// Output no-script text
 		getJspContext().getOut().println("<noscript>" + XML.esc(noScriptText) +
 			"</noscript>");
 
 		// Work out JavaScript
 		long keyTime = System.currentTimeMillis() + init.getKeyExpiry();
 		String js = "{user:'" + init.getUser() + "',displayName:'"
 			+ JS.escInlineAttr(init.getDisplayName()) + "',extra:'"
 			+ JS.escInlineAttr(init.getExtra()) + "',channel:'"+channel
 			+ "',maxMessages:" + maxMessages + ",maxAge:" + maxAge
 			+ ",maxNames:" + maxNames + ",permissions:'" + Auth.getPermissions(
 				init.getPermissionSet()) + "',key:'"
 			+ init.getKey(channel, keyTime, false)
 			+ "',keyTime:" + keyTime+ ",sayOnly:true,id:'hawthorn_recent"+index+"'"
 			+ ",headingLevel:" + headingLevel + ",namesText:'"
 			+ JS.escInlineAttr(namesText) + "',recentText:'"
 			+ JS.escInlineAttr(recentText) + "',sayOnly:true}";
 
 		// Print script tag if included
 		init.printJS(getJspContext().getOut(),false);
 
 		// Print the per-instance script
 		getJspContext().getOut().println("<script type='text/javascript'>\n"
 			+ "/* <![CDATA[ */");
 		getJspContext().getOut().println(
 			"document.getElementById('hawthorn_recent" + index + "')." +
 			"style.display='block';");
 		if(init.getDefer())
 		{
 			if(index==0)
 			{
 				getJspContext().getOut().println(
 					"var hawthorn_recent = new Array();");
 			}
 			getJspContext().getOut().println(
 				"hawthorn_recent.push("+js+");");
 		}
 		else
 		{
 			getJspContext().getOut().println(
 				"hawthorn.handleRecent("+js+");");
 		}
 		getJspContext().getOut().println("/* ]]> */\n</script>");
 	}
 
 	/** @param channel Hawthorn channel ID */
 	public void setChannel(String channel)
 	{
 		this.channel = channel;
 	}
 
 	/** @param maxMessages Maximum number of messages to display */
 	public void setMaxMessages(int maxMessages)
 	{
 		this.maxMessages = maxMessages;
 	}
 
 	/** @param maxAge Maximum age in milliseconds of messages to display */
 	public void setMaxAge(int maxAge)
 	{
 		this.maxAge = maxAge;
 	}
 
 	/** @param maxNames Maximum number of users to display */
 	public void setMaxNames(int maxNames)
 	{
 		this.maxNames = maxNames;
 	}
 
 	/** @param headingLevel Level for headings, 3 = h3 */
 	public void setHeadingLevel(int headingLevel)
 	{
 		this.headingLevel = headingLevel;
 	}
 
 	/** @param recentText Text of 'Recent messages' heading ("" = no heading) */
 	public void setRecentText(String recentText)
 	{
 		this.recentText = recentText;
 	}
 
 	/** @param namesText Text of 'Names' heading ("" = no heading) */
 	public void setNamesText(String namesText)
 	{
 		this.namesText = namesText;
 	}
 
 	/** @param loadingText Text to display while loading */
 	public void setLoadingText(String loadingText)
 	{
 		this.loadingText = loadingText;
 	}
 
 	/** @param noScriptText Text to display if JavaScript is unavailable */
 	public void setNoScriptText(String noScriptText)
 	{
 		this.noScriptText = noScriptText;
 	}
 }
