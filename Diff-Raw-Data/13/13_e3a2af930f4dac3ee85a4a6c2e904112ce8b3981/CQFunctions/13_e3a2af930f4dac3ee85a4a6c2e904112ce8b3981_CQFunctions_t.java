 /*
  * Copyright 2012 - Six Dimensions
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.sixdimensions.wcm.cq.cqex.functions;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tldgen.annotations.Function;
 
 import com.day.cq.wcm.api.Page;
 
 /**
  * Functions for CQ5.
  * 
  * @author dklco
  */
 public class CQFunctions {
 	private static final Logger log = LoggerFactory
 			.getLogger(CQFunctions.class);
 
 	/**
	 * Gets the navigation title, falling back on the following methods.
	 * 
 	 * <ul>
 	 * <li>getNavigationTitle</li>
 	 * <li>getPageTitle</li>
 	 * <li>getTitle</li>
 	 * <li>getName</li>
 	 * </ul>
 	 * 
 	 * @param page
 	 *            the page for which to get the title
 	 * @return the title to display in the navigation for the page
 	 */
 	@Function(example = "${cqex:getNavTitle(page)}")
 	public static String getNavTitle(final Page page) {
 		log.trace("getNavTitle");
 
 		if (page == null) {
 			return null;
 		}
 
 		String title = "";
 		if (page.getNavigationTitle() != null) {
 			title = page.getNavigationTitle();
 		} else if (page.getPageTitle() != null) {
 			title = page.getPageTitle();
 		} else if (page.getTitle() != null) {
 			title = page.getTitle();
 		} else {
 			title = page.getName();
 		}
 		return title;
 	}
 
 	/**
	 * Gets the page title, falling back on the following methods.
 	 * <ul>
 	 * <li>getPageTitle</li>
 	 * <li>getTitle</li>
 	 * <li>getName</li>
 	 * </ul>
 	 * 
 	 * @param page
 	 *            the page for which to get the title
 	 * @return the title to display in the navigation for the page
 	 */
 	@Function(example = "${cqex:getPageTitle(page)}")
 	public static String getPageTitle(final Page page) {
 		log.trace("getPageTitle");
 
 		if (page == null) {
 			return null;
 		}
 
 		String title = "";
 		if (page.getPageTitle() != null) {
 			title = page.getPageTitle();
 		} else if (page.getTitle() != null) {
 			title = page.getTitle();
 		} else {
 			title = page.getName();
 		}
 		return title;
 	}
 
 	/**
 	 * Do not invoke the default constructor.
 	 */
 	protected CQFunctions() {
 		// prevents calls from subclass
 		throw new UnsupportedOperationException();
 	}
 }
