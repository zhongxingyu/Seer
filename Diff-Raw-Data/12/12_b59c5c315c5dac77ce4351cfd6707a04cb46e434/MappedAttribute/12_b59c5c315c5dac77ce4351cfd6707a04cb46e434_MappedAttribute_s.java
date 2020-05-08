 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.jmx.adaptor.snmp.config.attribute;
 
 import javax.xml.bind.annotation.XmlAttribute;
 
 /**
  * An attribute mapping, by default readonly.
  * 
  * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
  * @version $Revision: 110494 $
  */
 public class MappedAttribute {
 	private String name;
 	private String oid;
 	private String mode;
	private boolean isReadWrite = false;
	private String type = "";
 	private String mbName = ""; //the name of the mBean this MappedAttribute is associated with
 	private String snmpType = ""; //the type for the MIB we should use, if provided
 	private String oidPrefix = "";
 	private String oidDefName = "";
 
 	public MappedAttribute() {
 	}
 
 	/** Attribute name */
 	public String getName() {
 		return name;
 	}
 
 	@XmlAttribute(name="name")
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/** Attribute oid */
 	public String getOid() {
 		return oid;
 	}
 
 	@XmlAttribute(name="oid")
 	public void setOid(String oid) {
 		this.oid = oid;
 	}
 
 	/** Attribute mode (ro/rw) */
 	public boolean isReadWrite() {
 		return isReadWrite;
 	}
 	
 	/** Attribute mode (ro/rw) */
 	public String getMode() {
 		return mode;
 	}
 
 	@XmlAttribute(name="mode")
 	public void setMode(String mode) {
 		this.mode = mode;
 		if(mode != null && mode.equalsIgnoreCase("rw")) {
 			isReadWrite = true;
 		}
 	}
 	
	public String getType(){
		return type;
	}
		
	public void setType(String type){
		this.type = type;
	}
	
 	public String getMbean(){
 		return this.mbName;
 	}
 	
 	@XmlAttribute(name="name")
 	public void setMbean(String mbName){
 		this.mbName = mbName;
 	}
 	
 	public String getSnmpType(){
 		return this.snmpType;
 	}
 	
 	public void setSnmpType(String snmpType){
 		this.snmpType=snmpType;
 	}
 	
 	public String getOidPrefix(){
 		return this.oidPrefix;
 	}
 	
 	public void setOidPrefix(String oidPrefix){
 		if (oidPrefix.charAt(0) == '.')
 			this.oidPrefix = oidPrefix.substring(1);
 		else
 			this.oidPrefix = oidPrefix;
 	}
 	
 	public String getOidDefName(){
 		return this.oidDefName;
 	}
 	
 	public void setOidDefName(String oidDefName){
 		this.oidDefName = oidDefName;
 	}
 
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		buf.append("[name=").append(name);
 		buf.append(", oid=").append(oid);
 		buf.append(", rw=").append(isReadWrite);
 		buf.append("]");
 		return buf.toString();
 	}
 }
