 /**
  * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  */
 package com.liferay.portletbox;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The purpose of this class is to isolate source code differences between different versions of Liferay Portal.
  *
  * @author  Neil Griffin
  */
 public class TestPages {
 
 	public static final List<PortalPage> PORTLETSPEC3_ISSUE_PAGES;
 
 	static {
 		PORTLETSPEC3_ISSUE_PAGES = new ArrayList<PortalPage>();
 		PORTLETSPEC3_ISSUE_PAGES.add(new PortalPage("PORTLETSPEC3-5", "1_WAR_PORTLETSPEC35portlet"));
 		PORTLETSPEC3_ISSUE_PAGES.add(new PortalPage("PORTLETSPEC3-7", "1_WAR_PORTLETSPEC37portlet"));
 		PORTLETSPEC3_ISSUE_PAGES.add(new PortalPage("PORTLETSPEC3-8", "1_WAR_PORTLETSPEC38portlet"));
 		PORTLETSPEC3_ISSUE_PAGES.add(new PortalPage("PORTLETSPEC3-9", "1_WAR_PORTLETSPEC39portlet"));
 		PORTLETSPEC3_ISSUE_PAGES.add(new PortalPage("PORTLETSPEC3-10", "1_WAR_PORTLETSPEC310portlet"));
		PORTLETSPEC3_ISSUE_PAGES.add(new PortalPage("PORTLETSPEC3-14", "1_WAR_PORTLETSPEC314portlet"));
 	}
 }
