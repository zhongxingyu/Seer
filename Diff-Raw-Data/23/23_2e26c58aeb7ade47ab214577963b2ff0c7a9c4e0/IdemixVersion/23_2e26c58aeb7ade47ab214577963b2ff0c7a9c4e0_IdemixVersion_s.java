 /**
  * IdemixVersion.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, April 2013.
  */
 
 package org.irmacard.credentials.idemix.util;
 
import org.irmacard.credentials.util.CardVersion;
 
public class IdemixVersion extends CardVersion {
 	private static final long serialVersionUID = 1L;
	byte[] version;
 
	public IdemixVersion(byte[] version) {
 		this.version = version;
 	}
 	
 	public String toString() {
		return "Idemix " + version[1] + "." + version[2] + "." + version[3];
 	}
 }
