 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.entity.user;
 
 /*
  * #%L
  * Cleia
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import com.fasterxml.jackson.annotation.JsonView;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 /**
  *
  * @author katsu
  */
 @Entity
 @Table(name = "groups1")
 public class Group implements Serializable {
     @Id
    @JsonView({Views.Public.class,Views.MedicalData.class})
     @Column(name = "value1")
     private String value;
     @JsonView(Views.Level2.class)
     @ManyToMany(mappedBy = "groups")
     private List<User> users;
      @JsonView(Views.Public.class)
     @Column(nullable = false)
     private boolean enabled;
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public List<User> getUsers() {
         return users;
     }
 
     public void setUsers(List<User> users) {
         this.users = users;
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
     
     
     
      public void addUser(User user) {
         if (this.users == null) {
             this.setUsers(new ArrayList<User>());
         }
         if (!this.users.contains(user)) {
             this.users.add(user);
         }
         if (user.getGroups() == null) {
             user.setGroups(new ArrayList<Group>());
         }
         if (!user.getGroups().contains(this)) {
             user.getGroups().add(this);
         }
 
     }
 }
