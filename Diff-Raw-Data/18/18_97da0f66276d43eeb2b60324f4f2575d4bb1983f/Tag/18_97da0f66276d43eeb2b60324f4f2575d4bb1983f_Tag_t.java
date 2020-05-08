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
 
 package org.footware.server.db;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import org.footware.shared.dto.TagDTO;
 
 /**
  * Class for ER mapping of Tags
  */
 @Entity
 public class Tag implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private long id;
 	
 	@Column(length=16)
 	private String tag;
 	
 	protected Tag() {}
 	
 	/**
 	 * Creates a new Tag
 	 * @param tag name of the tag
 	 */
 	public Tag(String tag) {
 		this.tag = tag;
 	}
 
 	/**
 	 * Creates a new Tag from DTO
 	 * @param tag tag received from web frontend 
 	 */
 	public Tag(TagDTO tag) {
 		this.id = tag.getId();
 		this.tag = tag.getTag();
 	}
 
 	/**
 	 * Gets the id of the corresponding DB row
 	 * @return the ID of the row in the DB
 	 */
 	public long getId() {
 		return id;
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
 
 	/**
 	 * Gets the TagDTO for the current Tag state
 	 * @return the TagDTO for the current Tag state
 	 */
 	public TagDTO getTagDTO() {
		return new TagDTO(tag);
 	}
 
 }
