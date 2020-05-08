 /*
  * Copyright (C) 2012 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.shared.libglom.layout;
 
 import java.io.Serializable;
 
 /**
 * @author Murray Cumming <murrayc@openismus.com>
 *
  */
 public class TableToViewDetails implements Serializable {
	private static final long serialVersionUID = 4649142373796447016L;
 
	public String tableName;
	public UsesRelationship usesRelationship;
 }
