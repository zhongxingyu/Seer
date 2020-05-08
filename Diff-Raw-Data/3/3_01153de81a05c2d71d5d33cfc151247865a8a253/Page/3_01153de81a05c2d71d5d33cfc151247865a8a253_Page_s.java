 /*
  * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,  
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.overlord.gadgets.server.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 /**
  * @author: Jeff Yu
  * @date: 4/04/12
  */
 @Entity
 @Table(name="GS_PAGE")
 public class Page implements Serializable{
 
 	private static final long serialVersionUID = -3949196421050038288L;
 
 	@Id
     @GeneratedValue
     @Column(name="PAGE_ID")
     private long id;
 
     @Column(name="PAGE_COLUMNS")
     private long columns;
 
     @Column(name="PAGE_NAME")
     private String name;
 
     @Column(name="PAGE_ORDER")
     private long pageOrder;
 
     @OneToMany(orphanRemoval = true, mappedBy = "page", fetch = FetchType.EAGER)
     private List<Widget> widgets = new ArrayList<Widget>();
 
     @ManyToOne(fetch = FetchType.EAGER)
     private User user;
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public long getColumns() {
         return columns;
     }
 
     public void setColumns(long columns) {
         this.columns = columns;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Widget> getWidgets() {
         return widgets;
     }
 
     public void setWidgets(List<Widget> widgets) {
         this.widgets = widgets;
     }
 
     public long getPageOrder() {
         return pageOrder;
     }
 
     public void setPageOrder(long pageOrder) {
         this.pageOrder = pageOrder;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 }
