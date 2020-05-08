 /**
  * Copyright (c) 2000-2007, Shakarchi Asaf
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package velo.entity;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.MappedSuperclass;
 
 /**
  * An entity that represents a abstract user comments for entities
  * 
  * @author Asaf Shakarchi
  */
 @MappedSuperclass
 public abstract class EntityUserComment extends BaseEntity implements Serializable { 
 
 	private static final long serialVersionUID = 1987302492306161413L;
 
 	private String comment;
 	private User commentBy;
 
 	
 	/**
 	 * @return the comment
 	 */
	@Column(name="COMMENT")
 	public String getComment() {
 		return comment;
 	}
 
 	/**
 	 * @param comment the comment to set
 	 */
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	/**
 	 * @return the commentBy
 	 */
 	//optional=true since users might get deleted from db, otherwise Hibernate will throw exceptions if IDs exists without the entity
 	@ManyToOne(optional=true)
 	@JoinColumn(name = "USER_ID")
 	public User getCommentBy() {
 		return commentBy;
 	}
 
 	/**
 	 * @param commentBy the commentBy to set
 	 */
 	public void setCommentBy(User commentBy) {
 		this.commentBy = commentBy;
 	}
 }
