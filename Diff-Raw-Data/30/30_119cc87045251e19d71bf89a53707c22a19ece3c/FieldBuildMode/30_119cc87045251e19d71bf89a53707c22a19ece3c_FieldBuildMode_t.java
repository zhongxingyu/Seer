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
 
 import screen.tools.sbs.utils.FieldString;
 
 public class FieldBuildMode {
 	
 	public enum Type{
 		ALL,
 		RELEASE,
 		DEBUG
 	}
 	
 	Type type;
 	boolean isValid;
 	
 	public FieldBuildMode() {
 		type = Type.ALL;
 		isValid = true;
 	}
 	
 	public FieldBuildMode(FieldBuildMode.Type type){
 		this.type = type;
 		isValid = true;
 	}
 	
 	public FieldBuildMode(String string) {
 		type = Type.ALL;
 		set(string);
 	}
 	
 	public void set(FieldBuildMode.Type type){
 		this.type = type;
 		isValid = true;
 	}
 	
 	public void set(String value){
 		FieldString field = new FieldString(value);
 		String string;
 		try {
 			string = field.getString();
 			isValid = true;
 			if("release".equals(string)){
 				type = Type.RELEASE;
 			}
 			else if ("debug".equals(string)){
 				type = Type.DEBUG;
 			}
 			else if ("all".equals(string)){
 				type = Type.ALL;
 			}
 			else{
 				type = Type.ALL;
 				isValid = false;
 			}
 		} catch (FieldException e) {
 			type = Type.ALL;
 			isValid = false;
 		}
 
 	}
 	
 	public Type get() throws FieldException {
 		if(!isValid)
 			throw new FieldException();
 		return type;
 	}
 	
 	public String getAsString() throws FieldException {
 		if(!isValid)
 			throw new FieldException();
 		switch (type) {
 		case DEBUG:
 			return "debug";
 		case RELEASE:
 			return "release";
 		default:
 			return "all";
 		}
 	}
 	
 	public boolean isSameMode(boolean isRelease){
		if(!isValid)
			return false;
		else if(type == Type.ALL)
 			return true;
 		else if(type == Type.RELEASE && isRelease)
 			return true;
 		else if(type == Type.DEBUG && !isRelease)
 			return true;
 		else
 			return false;
 	}
 }
