 /**
  * Elastic Grid
  * Copyright (C) 2008-2010 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.elasticgrid.admin.model;
 
import com.extjs.gxt.ui.client.data.BaseModel;
 import com.extjs.gxt.ui.client.data.BaseTreeModel;
 import java.io.Serializable;
 import java.util.List;
 
 public class Application extends BaseTreeModel implements Serializable {
 
     public Application() {
     }
 
     public Application(String name, List<Service> services) {
         setName(name);
         setServices(services);
     }
 
     public String getName() {
         return get("name");
     }
 
     public void setName(String name) {
         set("name", name);
     }
 
     public List<Service> getServices() {
         return get("services");
     }
 
     public void setServices(List<Service> services) {
         set("services", services);
         set("number_of_services", services.size());
        for (Service service : services)
           add(service);
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("Application");
         sb.append("{name='").append(getName()).append('\'');
         sb.append('}');
         return sb.toString();
     }
 
 }
