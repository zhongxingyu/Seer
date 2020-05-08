 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2011 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs.utils;
 
 public class Logger {
 	private static boolean isDebug = false;
 	
 	public static void debug(String s){
 		if(isDebug)
 			System.out.println("[DEBUG] "+s);
 	}
 	
 	public static void info(String s){
 		System.out.println("[INFO] "+s);
 	}
 	
 	public static void warning(String s){
 		System.out.println("[WARNING] "+s);
 	}
 	
 	public static void error(String s){
		System.out.println("[ERROR] "+s);
 	}
 
 	public static void setDebug(boolean isDebug) {
 		Logger.isDebug = isDebug;
 	}
 
 	public static boolean isDebug() {
 		return isDebug;
 	}
 }
