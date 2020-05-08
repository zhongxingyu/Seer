 /*
  * Kurento Commons MSControl: Simplified Media Control API for the Java Platform based on jsr309
  * Copyright (C) 2012  Tikal Technologies
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.kurento.mscontrol.commons;
 
 import com.kurento.commons.config.Parameters;
 
 public abstract class MediaResource extends Joinable {
 
 	/**
 	 * Sets current element configuration. If there is a previous configuration
 	 * it modifies this configuration.
 	 * 
	 * @param parameters
 	 *            The configuration to update
 	 */
 	public abstract void setConfiguration(Parameters parameters);
 
 	/**
 	 * This method allows to get information about the network connection, see
 	 * documentation of each platform to know which information can be get.
 	 * 
	 * @param parameters
 	 *            The parameters with the information to get
 	 */
 	public abstract void getInfo(Parameters parameters);
 
 	/**
 	 * This method allows to do actions on resource components, see each
 	 * platform documentation to know what actions can be done
 	 * 
 	 * @param parameters
 	 *            The actions to perform
 	 */
 	public abstract void doActions(Parameters parameters);
 }
