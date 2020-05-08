 /*
  * Copyright (C) 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.shared.layout;
 
 /**
  * Represents the libglom LayoutItem_Portal class.
  * 
  * @author Ben Konrath <ben@bagu.org>
  */
 @SuppressWarnings("serial")
public class LayoutItemPortal extends LayoutItem {
 
 	// This enum is identical to LayoutItem_Portal.navigation_type in libglom.
 	// @formatter:off
 	public enum NavigationType {
 		NAVIGATION_NONE, /** < No navigation will be offered. */
 		NAVIGATION_AUTOMATIC, /** < The destination related table will be chosen automatically based on the relationship
 									and the visible fields. */
 		NAVIGATION_SPECIFIC	/** < The destination related table will be determined by a specified relationship. */
 	};
 	// @formatter:on
 
 	private NavigationType navigationType = NavigationType.NAVIGATION_AUTOMATIC;
 
 	/**
 	 * Discover what type of navigation should be used when the user activates a related record row.
 	 */
 	public NavigationType getNavigationType() {
 		return navigationType;
 	}
 
 	/**
 	 * Set what type of navigation should be used when the user activates a related record row.
 	 */
 	public void setNavigationType(NavigationType navigationType) {
 		this.navigationType = navigationType;
 	}
 
 }
