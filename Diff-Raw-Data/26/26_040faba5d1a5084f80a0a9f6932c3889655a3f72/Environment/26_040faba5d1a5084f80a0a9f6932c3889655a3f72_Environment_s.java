 /*
  *  soapUI, copyright (C) 2004-2011 eviware.com 
  *
  *  soapUI is free software; you can redistribute it and/or modify it under the 
  *  terms of version 2.1 of the GNU Lesser General Public License as published by 
  *  the Free Software Foundation.
  *
  *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU Lesser General Public License for more details at gnu.org.
  */
 
 package com.eviware.soapui.model.environment;
 
 import com.eviware.soapui.config.ServiceConfig;
 import com.eviware.soapui.model.project.Project;
 
 public interface Environment
 {
 
 	public void setProject( Project project );
 
 	public Project getProject();
 
 	public void release();
 
 	public Service addNewService( String name, ServiceConfig.Type.Enum serviceType );
 
 	public void removeService( Service service );
 
 	public String getName();
 
 	public Property addNewProperty( String name, String value );
 
	public void removeProperty( String name );
 
 	public void changePropertyName( String name, String value );
 
 	public void changePropertyValue( String name, String value );
 
 }
