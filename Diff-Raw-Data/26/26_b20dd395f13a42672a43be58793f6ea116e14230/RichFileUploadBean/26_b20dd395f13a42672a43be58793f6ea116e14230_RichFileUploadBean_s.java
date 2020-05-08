 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
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
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import org.richfaces.component.UIFileUpload;
 import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

 import org.richfaces.tests.metamer.Attributes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for rich:fileUpload.
  *
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean(name = "richFileUploadBean")
 @ViewScoped
 public class RichFileUploadBean implements Serializable {
 
     private static final long serialVersionUID = -1L;
     private static Logger logger;
     private Attributes attributes;
    private List<UploadItem> items;
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
        items = new ArrayList<UploadItem>();
 
         attributes = Attributes.getUIComponentAttributes(UIFileUpload.class, getClass(), false);
 
         attributes.setAttribute("enabled", true);
         attributes.setAttribute("noDuplicate", false);
         attributes.setAttribute("rendered", true);
 
         // will be tested in another way
         attributes.remove("validator");
         attributes.remove("fileUploadListener");
     }
 
     public Attributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
    public List<UploadItem> getItems() {
        return items;
     }
 
     public void listener(UploadEvent event) {
        UploadItem item = event.getUploadItem();
 
        if (item != null) {
            items.add(item);
            item.getFile().delete();
         }
     }
 
     public String clearUploadedData() {
        items.clear();
         return null;
     }
 }
