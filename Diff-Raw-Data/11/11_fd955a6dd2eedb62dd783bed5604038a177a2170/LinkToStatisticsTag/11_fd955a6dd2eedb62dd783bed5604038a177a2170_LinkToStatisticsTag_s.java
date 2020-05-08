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
 import java.net.URL;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import com.leafdigital.hawthorn.util.*;
 import com.leafdigital.hawthorn.util.Auth.Permission;
 
 /** Tag displays recent messages and users on a Hawthorn chat channel. */
 public class LinkToStatisticsTag extends SimpleTagSupport
 {
 	@Override
 	public void doTag() throws JspException, IOException
 	{
 		// Get init tag with basic settings
 		InitTag init =
 			(InitTag)getJspContext().getAttribute(InitTag.HAWTHORN_INIT_TAG);
 		if(init==null)
 		{
 			throw new JspException("Cannot use <linkToStatistics> without <init>");
 		}
 
 		// Check permission
 		if(!init.getPermissionSet().contains(Permission.ADMIN))
 		{
 			throw new JspException("Current user does not have ADMIN permission");
 		}
 
 		// Get auth code
 		long time = System.currentTimeMillis() + init.getKeyExpiry();
 		String key = init.getKey("!system", time, true);
 
 		// Output list
 		getJspContext().getOut().println("<ul class='hawthorn_statslinks'>");
 		URL[] servers = init.getServers();
 		for(URL server : servers)
 		{
 			getJspContext().getOut().println(
 				"<li><a href='"+XML.esc(server.toString())+"hawthorn/html/statistics"
				+ "?channel=!system&amp;user=_admin&amp;displayname=_&amp;keytime="
 				+ time + "&amp;key=" + key + "'>"
 				+ XML.esc(server.toString()) + "</a></li>");
 		}
 		getJspContext().getOut().println("</ul>");
 	}
 }
