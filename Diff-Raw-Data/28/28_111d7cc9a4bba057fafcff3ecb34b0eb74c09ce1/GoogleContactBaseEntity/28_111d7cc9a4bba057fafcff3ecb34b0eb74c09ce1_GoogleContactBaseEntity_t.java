 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.google.contact.wrappers;
 
 import com.google.gdata.data.BaseEntry;
 import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
 import com.google.gdata.data.TextConstruct;
 
 abstract public class GoogleContactBaseEntity<W extends BaseEntry<?>> {
 			
 	@SuppressWarnings("unchecked")
 	static public <T> T getWrappedEntity(Class<T> type, GoogleContactBaseEntity<?> entity) {
 		return (T) entity.wrapped;
 	}
 	
 	protected W wrapped;
 		
 	public GoogleContactBaseEntity(W wrapped) {
 		this.wrapped = wrapped;
 	}
 	
 	public void setId(String id) { 
 		wrapped.setId(id);
 	}
 	
 	public String getId() {
 		return wrapped.getId();
 	}
 	
 	public void setDraft(Boolean draft) {
 		wrapped.setDraft(draft);
 	}
 	
 	public Boolean getDraft() {
 		return wrapped.isDraft();
 	}
 	
 	public void setTitle(TextConstruct title) {
 		wrapped.setTitle(title);
 	}
 	
 	public TextConstruct getTitle() {
 		return wrapped.getTitle();
 	}
 	
 	public void setUpdated(Long datetime) {
 		wrapped.setUpdated(new DateTime(datetime));
 	}
 	
 	public Long getUpdated() {
 		return wrapped.getUpdated() != null ? wrapped.getUpdated().getValue() : 0l;
 	}
 	
 	public void setInmutable(Boolean inmutable) {
 		wrapped.setImmutable(inmutable);
 	}
 	
 	public Boolean getInmutable() {
 		return wrapped.isImmutable();
 	}
 	
 	public void setCanEdit(Boolean canEdit) {
 		wrapped.setCanEdit(canEdit);
 	}
 	
 	public Boolean getCanEdit() {
 		return wrapped.getCanEdit();
 	}
 	
 	// Only getter
	public Link getEditLink() {
		return wrapped.getEditLink();
	}
	
	// Only getter
 	public Long getEdited() {
 		return wrapped.getEdited() != null ? wrapped.getEdited().getValue() : 0l;
 	}	
 }
