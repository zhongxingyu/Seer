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
 
import org.irmacard.idemix.util.CardVersion;
 
public class IdemixVersion extends org.irmacard.credentials.util.CardVersion {
 	private static final long serialVersionUID = 1L;
	CardVersion version;
 
	public IdemixVersion(CardVersion version) {
 		this.version = version;
 	}
 	
 	public String toString() {
		return "IRMAcard " + version.toString();
 	}
 }
