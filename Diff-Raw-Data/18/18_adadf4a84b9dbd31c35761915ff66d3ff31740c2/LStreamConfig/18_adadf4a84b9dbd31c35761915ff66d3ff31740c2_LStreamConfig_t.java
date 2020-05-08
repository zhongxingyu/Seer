/**  
 * L - the light-weight Java logging utility designed for brevity and simplicity.
 * Copyright (C) 2012 Ajay Gopinath
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/

 package com.agopinath.lthelogutil.streams;
 
 public final class LStreamConfig {
 	static final String LINE_SEPARATOR = System.getProperty("line.separator");
 	static final String LSTREAMID_UNASSIGNED = "UNASSIGNED";
 	
 	public static final boolean isLStreamIDUnassigned(final String lStreamID) {
 		return lStreamID.equals(LSTREAMID_UNASSIGNED);
 	}
 }
