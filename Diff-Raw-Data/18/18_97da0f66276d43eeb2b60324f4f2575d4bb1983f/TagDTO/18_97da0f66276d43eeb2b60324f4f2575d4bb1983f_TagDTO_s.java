 /*
  * Copyright 2010 Andreas Brauchli, René Buffat, Florian Widmer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.footware.shared.dto;
 
 import java.io.Serializable;
 
import org.footware.server.db.Tag;

 public class TagDTO implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private long id;
 	private String tag;
 
 	/**
 	 * Gets the id of the corresponding DB row
 	 * @return the ID of the row in the DB
 	 */
 	public long getId() {
 		return id;
 	}
 	
 	/**
 	 * Creates a new Tag
 	 * @param tag name of the tag
 	 */
 	public TagDTO(String tag) {
 		this.tag = tag;
 	}
	
	/**
	 * Creates a TagDTO from a Tag 
	 * @param tag Tag object to create the TagDTO from
	 */
	public TagDTO(Tag tag) {
		this.tag = tag.getTag();
	}
 
 	/**
 	 * Gets the tag
 	 * @return tag the tag name
 	 */
 	public String getTag() {
 		return tag;
 	}
 	
 	/**
 	 * Sets / Renames the tag
 	 * @param tag new tag name
 	 */
 	public void setTag(String tag) {
 		this.tag = tag;
 	}
 }
