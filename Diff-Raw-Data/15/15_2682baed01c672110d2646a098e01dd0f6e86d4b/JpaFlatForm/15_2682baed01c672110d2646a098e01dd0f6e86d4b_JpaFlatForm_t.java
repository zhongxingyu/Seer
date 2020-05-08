 /**
  * The contents of this file are subject to the AED Public Use License Agreement, Version 1.0 (the "License");
  * use in any manner is strictly prohibited except in compliance with the terms of the License.
  * The License is available at http://gatherdata.org/license.
  *
  * Copyright (c) AED.  All Rights Reserved
  */
 /**
  * 
  */
 package org.gatherdata.data.dao.jpa.model;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.gatherdata.commons.net.CbidFactory;
 import org.gatherdata.data.core.model.FlatForm;
 import org.gatherdata.data.core.model.RenderedValue;
 import org.gatherdata.data.core.model.impl.MutableFlatForm;
 import org.joda.time.DateTime;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * JPA annotated sub-class of a FlatForm, ready for storage.
  * 
  */
 @Entity
 @Table(name = "FLAT_FORM")
 public class JpaFlatForm extends MutableFlatForm implements FlatForm {
 
 	/**
      * 
      */
 	private static final long serialVersionUID = -8313084518428651473L;
 
 	@Id
 	@Column(name = "UID")
 	private String uidAsString;
 
 	@Transient
 	private URI uid;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "DATE_CREATED")
 	private Calendar dateCreatedAsCalendar;
 
 	@Basic
 	@Column(name = "NAMESPACE")
 	private String namespaceAsString;
 
 	@Transient
 	private URI lazyNamespace;
 
 	@OneToMany (mappedBy = "form", cascade = CascadeType.ALL)
 	private List<JpaRenderedValue> jpaValues = new ArrayList<JpaRenderedValue>();
 
 	/**
      * 
      */
 	public JpaFlatForm() {
 		;
 	}
 
 	public URI getUid() {
 		if (uid == null) {
 			try {
 				this.uid = new URI(uidAsString);
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 		}
 		return uid;
 	}
 
 	@Override
 	public void setUid(URI uid) {
 		this.uidAsString = uid.toASCIIString();
 		this.uid = uid;
 	}
 
 	@Basic
 	public DateTime getDateCreated() {
 		if ((dateCreated == null) && (dateCreatedAsCalendar != null)) {
 			this.dateCreated = new DateTime(dateCreatedAsCalendar
 					.getTimeInMillis());
 		}
 		return dateCreated;
 	}
 
 	@Override
 	public void setDateCreated(DateTime dateCreated) {
 		this.dateCreated = dateCreated;
 		if (this.dateCreatedAsCalendar == null) {
 			this.dateCreatedAsCalendar = new GregorianCalendar();
 		}
 		dateCreatedAsCalendar.setTimeInMillis(dateCreated.getMillis());
 	}
 
 	@Basic
 	public URI getNamespace() {
 		if ((this.lazyNamespace == null) && (this.namespaceAsString != null)) {
 			try {
 				this.lazyNamespace = new URI(this.namespaceAsString);
 			} catch (URISyntaxException e) {
 				this.lazyNamespace = null;
 			}
 		}
 		return this.lazyNamespace;
 	}
 
 	public void setNamespace(URI namespace) {
 		this.lazyNamespace = namespace;
 		this.namespaceAsString = (namespace != null) ? namespace
 				.toASCIIString() : null;
 	}
 
 	@OneToMany
 	public List<JpaRenderedValue> getValues() {
 		return jpaValues;
 	}
 
 	public void add(JpaRenderedValue renderedValue) {
 		jpaValues.add(renderedValue);
 		renderedValue.setForm(this);
 	}
 
 	public void remove(RenderedValue renderedValue) {
 		jpaValues.remove(renderedValue);
 	}
 
 	protected void setValues(List<JpaRenderedValue> values) {
 		this.jpaValues = values;
 	}
 
 	/**
 	 * @see java.lang.Object#equals(Object)
 	 */
 	public boolean equals(Object object) {
 		if (!(object instanceof FlatForm)) {
 			return false;
 		}
 		FlatForm rhs = (FlatForm) object;
 		return new EqualsBuilder().append(this.getNamespace(),
 				rhs.getNamespace()).append(this.getUid(), rhs.getUid())
 				.isEquals();
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() {
 		return new HashCodeBuilder(969177683, -607910761).append(
 				this.getNamespace()).append(this.getUid()).toHashCode();
 	}
 
 	public JpaFlatForm copy(FlatForm template) {
 		if (template != null) {
 			setDateCreated(template.getDateCreated());
 			setNamespace(template.getNamespace());
 			setUid(template.getUid());
 			
 			jpaValues.clear();
 			List<? extends RenderedValue> templateValues = template.getValues();
 			if (templateValues != null) {
 				for (RenderedValue templateValue : templateValues) {
 					add(new JpaRenderedValue().copy(templateValue));
 				}
 			}
 		}
 		return this;
 	}
 	
     public URI selfIdentify() {
         if (dateCreatedAsCalendar == null) {
             dateCreatedAsCalendar = GregorianCalendar.getInstance();
         }
         URI selfId = CbidFactory.createCbid(FlatForm.class.getSimpleName() + getDateCreated() + Integer.toHexString(hashCode()));
         if (this.getUid() == null) {
             setUid(selfId);
         }
         return selfId;
     }
 
     @Override
     public String toString() {
         return "FlatForm [uid=" + uidAsString + ", dateCreated=" + ((dateCreatedAsCalendar != null) ? getDateCreated().toString() : "null")
                 + ", namespace=" + namespaceAsString + "]";
     }
     
     
 
 }
