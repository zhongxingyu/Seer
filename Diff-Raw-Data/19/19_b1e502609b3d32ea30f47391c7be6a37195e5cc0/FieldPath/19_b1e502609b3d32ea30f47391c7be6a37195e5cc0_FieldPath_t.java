 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2010 Ratouit Thomas                                    *
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
 
 import screen.tools.sbs.objects.EnvironmentVariables;
 
 public class FieldPath {
 	FieldString fieldString;
 	FieldBuildMode fieldBuildMode; 
 	
 	public FieldPath() {
 		fieldString = new FieldString();
 		fieldBuildMode = new FieldBuildMode();
 	}
 	
 	public FieldPath(String path) {
 		fieldString = new FieldString(path);
 		fieldBuildMode =  new FieldBuildMode();
 	}
 	
 	public boolean isEmpty(){
 		return fieldString.isEmpty();
 	}
 	
 	public boolean isValid(EnvironmentVariables additionalVars){
 		return fieldString.isValid(additionalVars);
 	}
 
 	public boolean isValid(){
 		return isValid(null);
 	}
 
 	public void setString(String path) {
 		fieldString.setString(path);
 	}
 
 	public String getOriginalString() {
 		return fieldString.getOriginalString();
 	}
 	
 	public String getString(EnvironmentVariables additionalVars) {
 		String ret = fieldString.getString(additionalVars);
 		if(ret == null)
			return null;
		ret = ret.replaceAll("\\\\ ", " ");
		ret = ret.replaceAll("\\\\", "/");
 		if(!ret.endsWith("/"))
			ret += "/";
 		return ret;
 	}
 
 	public String getString(){
 		return getString(null);
 	}
 	
	public String getCMakeString(EnvironmentVariables additionalVars){
		return getString(additionalVars).replaceAll(" ", "\\\\ ");
	}
	
	public String getCMakeString(){
		return getCMakeString(null);
	}

	
 	public void setBuildMode(FieldBuildMode mode){
 		fieldBuildMode = mode;
 	}
 	
 	public FieldBuildMode getBuildMode(){
 		return fieldBuildMode;
 	}
 }
