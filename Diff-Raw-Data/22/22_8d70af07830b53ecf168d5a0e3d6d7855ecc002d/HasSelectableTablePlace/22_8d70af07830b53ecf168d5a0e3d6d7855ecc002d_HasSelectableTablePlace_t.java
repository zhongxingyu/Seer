 /*
  * Copyright (C) 2011 Openismus GmbH
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
 
 package org.glom.web.client.place;
 
 import com.google.gwt.place.shared.Place;
 
 /**
  * Super type for Place objects (bookmarkable URLs) that display the TableSelectionView.
  * 
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 public abstract class HasSelectableTablePlace extends Place {
 
 	private String documentID;
 	private String tableName;
 
 	public HasSelectableTablePlace(String documentID, String tableName) {
 		this.documentID = documentID;
 		this.tableName = tableName;
 	}
 
 	public String getDocumentID() {
 		return documentID;
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 
 	public static class Tokenizer {
 		protected final String documentKey = "document=";
 		protected final String tableKey = "table=";
		protected final String separator = "&";
 	}
 
 }
