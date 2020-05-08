 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2011, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.bean;
 
 import java.io.Serializable;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 import org.richfaces.component.UIDataGrid;
 import org.richfaces.tests.metamer.Attributes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for rich:dataGrid.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean(name = "richDataGridBean")
 @ViewScoped
 public class RichDataGridBean implements Serializable {
 
     private static final long serialVersionUID = 4814439475400649809L;
     private static Logger logger;
     private Attributes attributes;
     // true = model, false = empty table
     private boolean state = true;
     private int page = 1;
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
 
         attributes = Attributes.getComponentAttributesFromFacesConfig(UIDataGrid.class, getClass());
 
         attributes.setAttribute("columns", 3);
         attributes.setAttribute("rendered", true);
 
         // attributes defined directly in page
         attributes.remove("value");
         attributes.remove("var");
 
         // TODO attributes which needs to be tested in another way
         attributes.remove("iterationStatusVar");
         attributes.remove("rowKeyVar");
         attributes.remove("stateVar");
     }
 
     public Attributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     public boolean isState() {
         return state;
     }
 
     public void setState(boolean state) {
         this.state = state;
     }
 
     public int getPage() {
         return page;
     }
 
     public void setPage(int page) {
         this.page = page;
     }
 }
