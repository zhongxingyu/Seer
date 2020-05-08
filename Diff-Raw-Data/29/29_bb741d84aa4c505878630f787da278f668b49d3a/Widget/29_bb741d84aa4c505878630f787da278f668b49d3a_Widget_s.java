 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.overlord.gadgets.server.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.*;
 
 /**
  * @author: Jeff Yu
  * @date: 5/04/12
  */
 @Entity
 @Table(name="GS_WIDGET")
 public class Widget implements Serializable{
 
 	private static final long serialVersionUID = 3619016542917740925L;
 
 	@Id
     @GeneratedValue
     @Column(name="WIDGET_ID")
     private long id;
 
     @Column(name="WIDGET_NAME")
     private String name;
 
     @Column(name="WIDGET_URL")
     private String appUrl;
 
     @Column(name="WIDGET_ORDER")
     private long order;
     
    @OneToMany(mappedBy = "widget")
    private List<WidgetPreference> prefs = new ArrayList<WidgetPreference>();
 
     @ManyToOne
     private Page page;
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public long getOrder() {
         return order;
     }
 
     public void setOrder(long order) {
         this.order = order;
     }
 
     public String getAppUrl() {
         return appUrl;
     }
 
     public void setAppUrl(String appUrl) {
         this.appUrl = appUrl;
     }
 
     public Page getPage() {
         return page;
     }
 
     public void setPage(Page page) {
         this.page = page;
     }
 
 	public List<WidgetPreference> getPrefs() {
 		return prefs;
 	}
 
 	public void setPrefs(List<WidgetPreference> prefs) {
 		this.prefs = prefs;
 	}
 
 }
