 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
 
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 
 package net.java.dev.cejug.classifieds.server.ejb3.entity;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;

import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * @author $Author$
  * @version $Rev$ ($Date$)
  * 
  */
 @Entity
 @Table(name = "ADVERTISEMENT")
 @NamedQuery(name = "selectAdvertisementByFilter", query = "SELECT adv FROM AdvertisementEntity adv")
 public class AdvertisementEntity extends AbstractEntity {
 
     /**
      * This assumes a scheduled process (quartz?) that will update the status of
      * the advertisements.
      */
     public enum AdvertisementStatus {
         ONLINE, ARCHIVE, CANCELED
     }
 
     @Column(name = "TITLE", nullable = false)
     private String title;
 
     @Column(name = "SUMMARY", nullable = false)
     private String summary;
 
     @Column(name = "TEXT", nullable = false)
     private String text;
 
     @JoinColumn(name = "CUSTOMER_ID", nullable = false)
     @ManyToOne
     private CustomerEntity customer;
 
     @ManyToMany
     @JoinTable(name = "ADVERTISEMENT_KEYWORD", joinColumns = @JoinColumn(name = "ADVERTISEMENT_ID", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "KEYWORD_ID", referencedColumnName = "ID"))
     private Collection<AdvertisementKeywordEntity> keywords;
 
    @ManyToOne(cascade=CascadeType.ALL)
     @JoinColumn(name = "ADVERTISEMENT_TYPE_ID")
     private AdvertisementTypeEntity type;
 
     @Column(name = "START", nullable = false)
     @Temporal(TemporalType.DATE)
     private Calendar start;
 
     @Column(name = "FINISH", nullable = false)
     @Temporal(TemporalType.DATE)
     private Calendar finish;
 
     @Column(nullable = false)
     @Enumerated(EnumType.STRING)
     private AdvertisementStatus state = AdvertisementStatus.ONLINE;
 
     public String getTitle() {
 
         return title;
     }
 
     public void setTitle(String title) {
 
         this.title = title;
     }
 
     public String getSummary() {
 
         return summary;
     }
 
     public void setSummary(String summary) {
 
         this.summary = summary;
     }
 
     public String getText() {
 
         return text;
     }
 
     public void setText(String text) {
 
         this.text = text;
     }
 
     public CustomerEntity getCustomer() {
 
         return customer;
     }
 
     public void setCustomer(CustomerEntity customer) {
 
         this.customer = customer;
     }
 
     public Collection<AdvertisementKeywordEntity> getKeywords() {
 
         return keywords;
     }
 
     public void setKeywords(Collection<AdvertisementKeywordEntity> keywords) {
 
         this.keywords = keywords;
     }
 
     public void addKeyword(String keyword) {
 
         AdvertisementKeywordEntity advKeyword = new AdvertisementKeywordEntity();
         advKeyword.setName(keyword);
         addKeyword(advKeyword);
     }
 
     public void addKeyword(AdvertisementKeywordEntity keyword) {
 
         if (this.keywords == null) {
             this.keywords = new ArrayList<AdvertisementKeywordEntity>();
         }
         keywords.add(keyword);
     }
 
     /**
      * @return the type
      */
     public AdvertisementTypeEntity getType() {
 
         return type;
     }
 
     /**
      * @param type
      *            the type to set
      */
     public void setType(AdvertisementTypeEntity type) {
 
         this.type = type;
     }
 
     public Calendar getStart() {
 
         return start;
     }
 
     public void setStart(Calendar start) {
 
         this.start = start;
     }
 
     public Calendar getFinish() {
 
         return finish;
     }
 
     public void setFinish(Calendar finish) {
 
         this.finish = finish;
     }
 
     public AdvertisementStatus getState() {
 
         return state;
     }
 
     public void setState(AdvertisementStatus state) {
 
         this.state = state;
     }
 }
