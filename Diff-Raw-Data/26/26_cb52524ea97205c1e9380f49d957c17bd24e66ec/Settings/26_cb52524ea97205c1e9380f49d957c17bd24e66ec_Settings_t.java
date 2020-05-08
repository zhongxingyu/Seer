 /*
 Settings.java
 Copyright (C) 2010  Belledonne Communications, Grenoble, France
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package org.linphone.jlinphone.gui;
 
 public interface Settings {
 
 	public static final String SIP_USERNAME = "org.jlinphone.settings.sip.username";
 	public static final String SIP_PASSWORD = "org.jlinphone.settings.sip.password";
 	public static final String SIP_DOMAIN = "org.jlinphone.settings.sip.domain";
 	public static final String SIP_PROXY = "org.jlinphone.settings.sip.proxy";
 	public static final String SIP_TRANSPORT = "org.jlinphone.settings.sip.transport";
 	
 	public static final String ADVANCED_DEBUG = "org.jlinphone.settings.advanced.debug";
 	public static final String ADVANCED_SUBSTITUTE_PLUS_TO_DOUBLE_ZERO = "org.jlinphone.settings.advanced.zerotoplus";
	public static final String ADVANCED_PTIME = "org.jlinphone.settings.advanced.ptime";
 	
 
 	public abstract boolean getBoolean(String key, boolean defaultValue);
 
 	public abstract String getString(String key, String defaultValue);
 
 }
