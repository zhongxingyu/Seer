 /*
  * Copyright 2012 cesar.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.eqaula.glue.controller;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.ejb.TransactionAttribute;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import org.apache.commons.lang.SerializationUtils;
 import org.eqaula.glue.cdi.Web;
 import org.eqaula.glue.controller.profile.ProfileHome;
 import org.eqaula.glue.model.Property;
 import org.eqaula.glue.model.Structure;
 import org.eqaula.glue.service.BussinesEntityTypeService;
 import org.eqaula.glue.util.UI;
 import org.jboss.seam.transaction.Transactional;
 import org.primefaces.context.RequestContext;
 import org.primefaces.event.SelectEvent;
 import org.primefaces.event.UnselectEvent;
 
 /**
  *
  * @author cesar
  */
 @Named
 @ViewScoped
 public class PropertyHome extends BussinesEntityHome<Property> implements Serializable {
 
     private static final long serialVersionUID = 7632987414391869389L;
     private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(PropertyHome.class);
     @Inject
     @Web
     private EntityManager em;
     @Inject
     private BussinesEntityTypeHome bussinesEntityTypeHome;
     @Inject
     private BussinesEntityTypeService bussinesEntityTypeService;
     private Long structureId;
     private Long bussinesEntityTypeId;
     private String propertyStringValue;
 
     public PropertyHome() {
         log.info("eqaula --> Inicializo Property Home");
     }
 
     public Long getPropertyId() {
         return (Long) getId();
     }
 
     public void setPropertyId(Long propertyId) {
         setId(propertyId);
     }
 
     public Long getStructureId() {
         return structureId;
     }
 
     public void setStructureId(Long structureId) {
         this.structureId = structureId;
     }
 
     public Long getBussinesEntityTypeId() {
         return bussinesEntityTypeId;
     }
 
     public void setBussinesEntityTypeId(Long bussinesEntityTypeId) {
         this.bussinesEntityTypeId = bussinesEntityTypeId;
     }
 
     public String getPropertyStringValue() {
         if (this.propertyStringValue == null) {
             if (getInstance() != null) {
                 if (getInstance().getValue() != null) {
                     if ("java.util.Date".equals(getInstance().getType())) {
                         Date date = (Date) getInstance().getValue();
                         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                         String fecha = sdf.format(date);
                         setPropertyStringValue(fecha);
                     } else {
                         log.info("eqaula --> Instance from Property Value not Date for " + propertyStringValue);
                        setPropertyStringValue(getInstance().getValue().toString());
                     }
                 } else {
                     log.info("eqaula --> Instance from Property Value not Date for " + propertyStringValue);
                     this.propertyStringValue = "";
                 }
             } else {
                 log.info("eqaula --> Instance from Property for " + propertyStringValue);
                 this.propertyStringValue = "";
             }
         }
         return propertyStringValue;
     }
 
     public void setPropertyStringValue(String propertyStringValue) {
         this.propertyStringValue = propertyStringValue;
     }
 
     @Override
     protected Property createInstance() {
         Property property = new Property();
         return property;
     }
 
     @TransactionAttribute
     public void load() {
         log.info("eqaula --> Loading instance from Property for " + getId());
         if (isIdDefined()) {
             wire();
         }
         log.info("eqaula --> Loaded instance " + getInstance());
     }
 
     @TransactionAttribute
     public void wire() {
         getInstance();
     }
 
     public boolean isWired() {
         return true;
     }
 
     public Property getDefinedInstance() {
         return isIdDefined() ? getInstance() : null;
     }
 
     @PostConstruct
     public void init() {
         setEntityManager(em);
         bussinesEntityTypeService.setEntityManager(em);
     }
 
     @Override
     public Class<Property> getEntityClass() {
         return Property.class;
     }
 
     @TransactionAttribute
     public String saveProperty() {
         log.info("eqaula --> saving " + getInstance().getName());
 
         if (getInstance().isPersistent()) {
             this.getInstance().setValue(converterToType(propertyStringValue));
             save(getInstance());
         } else {
             try {                
                 Structure s = bussinesEntityTypeService.getStructure(getStructureId()); //Retornar la estrucura.
                 s.addProperty(this.getInstance());
             } catch (Exception ex) {
                 //log.info("eqaula --> error saving new" + getInstance().getName());
             }
         }
         return "/pages/admin/bussinesentitytype/bussinesentitytype?faces-redirect=true&bussinesEntityTypeId=" + getBussinesEntityTypeId();
     }
 
     @Transactional
     public String deleteProperty() {
         try {
             if (getInstance() == null) {
                 throw new NullPointerException("property is null");
             }
 
             if (getInstance().isPersistent()) {                
                 Structure s = bussinesEntityTypeService.getStructure(getStructureId());                
                 boolean mensaje = s.removeProperty(getInstance());                
                 delete(getInstance());                    
                 save(s);                                
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                 //RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente
                 
             } else {
                 //remover de la lista, si aún no esta persistido
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Debe agregar una propiedad para ser borrada", ""));
             }
 
         } catch (Exception e) {            
             e.printStackTrace();
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
         }
         return "/pages/admin/bussinesentitytype/bussinesentitytype?faces-redirect=true&bussinesEntityTypeId=" + getBussinesEntityTypeId();
     }
 
     public void onRowSelect(SelectEvent event) {
         FacesMessage msg = new FacesMessage(UI.getMessages("module.Property") + " " + UI.getMessages("common.selected"), ((Property) event.getObject()).getName());
         FacesContext.getCurrentInstance().addMessage(null, msg);
     }
 
     public void onRowUnselect(UnselectEvent event) {
         FacesMessage msg = new FacesMessage(UI.getMessages("module.Property") + " " + UI.getMessages("common.unselected"), ((Property) event.getObject()).getName());
         FacesContext.getCurrentInstance().addMessage(null, msg);
         this.setBussinesEntity(null);
     }
 
     public Serializable converterToType(String value) {
 
         Object o = new Object();
         try {
             if ("java.lang.String".equals(getInstance().getType()) || "java.lang.String[]".equals(getInstance().getType()) || "java.lang.MultiLineString".equals(getInstance().getType())) {
                 o = value;
             } else if ("java.lang.Long".equals(getInstance().getType())) {
                 o = Long.valueOf(value);
             } else if ("java.lang.Float".equals(getInstance().getType())) {
                 o = Float.valueOf(value);
             } else if ("java.lang.Boolena".equals(getInstance().getType())) {
                 o = Boolean.valueOf(value);
             } else if ("java.util.Date".equals(getInstance().getType())) {
                 SimpleDateFormat sdf;
                 sdf = new SimpleDateFormat("dd/MM/yyyy");
                 Date fecha = null;
                 try {
                     fecha = sdf.parse(value);
                 } catch (ParseException pe) {
                     //log.info("eqaula --> error converter date:"+pe.getMessage());
                 }
                 o = fecha;
             } else if ("java.lang.Double".equals(getInstance().getType())) {
                 o = Double.valueOf(value);
             } else {
                 o = value;
             }
         } catch (Exception e) {
             //log.info("eqaula --> error converter: " + value);
         }
         return (Serializable) o;
     }      
 }
