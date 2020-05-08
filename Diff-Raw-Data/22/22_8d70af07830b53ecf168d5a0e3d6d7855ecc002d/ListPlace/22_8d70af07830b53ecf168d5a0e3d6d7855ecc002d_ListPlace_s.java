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
 
 import com.google.gwt.place.shared.PlaceTokenizer;
 import com.google.gwt.place.shared.Prefix;
 
 public class ListPlace extends HasSelectableTablePlace {
 
 	public ListPlace(String documentID, String tableName) {
 		super(documentID, tableName);
 	}
 
 	@Prefix("list")
 	public static class Tokenizer extends HasSelectableTablePlace.Tokenizer implements PlaceTokenizer<ListPlace> {
 
 		@Override
 		public String getToken(ListPlace place) {
			return documentKey + place.getDocumentID() + seperator + tableKey + place.getTableName();
 		}
 
 		@Override
 		public ListPlace getPlace(String token) {
			String[] tokenArray = token.split(seperator);
 
 			if (tokenArray.length != 2)
 				return new ListPlace("", "");
 
 			String documentID = "", tableName = "";
 
 			if (documentKey.equals(tokenArray[0].substring(0, documentKey.length()))) {
 				documentID = tokenArray[0].substring(documentKey.length());
 			}
 
 			if (tableKey.equals(tokenArray[1].substring(0, tableKey.length()))) {
 				tableName = tokenArray[1].substring(tableKey.length());
 			}
 
 			return new ListPlace(documentID, tableName);
 		}
 	}
 
 }
