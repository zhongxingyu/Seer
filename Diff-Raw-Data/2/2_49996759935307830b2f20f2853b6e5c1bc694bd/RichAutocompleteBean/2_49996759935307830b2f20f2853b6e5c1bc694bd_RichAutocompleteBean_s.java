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
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import org.richfaces.component.UIAutocomplete;
 
 import org.richfaces.tests.metamer.Attributes;
 import org.richfaces.tests.metamer.model.Capital;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for rich:autocomplete.
  * http://community.jboss.org/wiki/richfacesautocompletecomponentbehavior
  *
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean(name = "richAutocompleteBean")
 // cannot be view-scoped (see https://jira.jboss.org/browse/RF-9287)
 @SessionScoped
 public class RichAutocompleteBean implements Serializable {
 
     private static final long serialVersionUID = -1L;
     private static Logger logger;
     private Attributes attributes;
     @ManagedProperty(value = "#{model.capitals}")
     private List<Capital> capitals;
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
 
         attributes = Attributes.getUIComponentAttributes(UIAutocomplete.class, getClass());
         attributes.setAttribute("converterMessage", "converter message");
         attributes.setAttribute("mode", "ajax");
         attributes.setAttribute("rendered", true);
         attributes.setAttribute("tokens", ", ");
         attributes.setAttribute("validatorMessage", "validator message");
 
         attributes.remove("autocompleteMethod");
         attributes.remove("converter");
         attributes.remove("fetchValue");
         attributes.remove("itemConverter");
         attributes.remove("validator");
         attributes.remove("valueChangeListener");
 
         // these are hidden attributes
         attributes.remove("autocompleteList");
         attributes.remove("localValue");
         attributes.remove("localValueSet");
         attributes.remove("submittedValue");
         attributes.remove("valid");
         attributes.remove("validators");
         attributes.remove("valueChangeListeners");
     }
 
     public Attributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     public List<String> autocomplete(String prefix) {
         ArrayList<String> result = new ArrayList<String>();
         if (prefix.length() > 0) {
             Iterator<Capital> iterator = capitals.iterator();
             while (iterator.hasNext()) {
                 Capital elem = ((Capital) iterator.next());
                 if ((elem.getState() != null && elem.getState().toLowerCase().indexOf(prefix.toLowerCase()) == 0)
                         || "".equals(prefix)) {
                     result.add(elem.getState());
                 }
             }
         } else {
            for (int i = 0; i < 10; i++) {
                 result.add(capitals.get(i).getState());
             }
         }
         return result;
     }
 
     public void setCapitals(List<Capital> capitals) {
         this.capitals = capitals;
     }
 }
