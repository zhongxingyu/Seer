 /*
  * Copyright 2012 jlgranda.
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
 package org.eqaula.glue.controller.profile;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.ejb.TransactionAttribute;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import org.eqaula.glue.cdi.Current;
 import org.eqaula.glue.cdi.Web;
 import org.eqaula.glue.controller.BussinesEntityHome;
 import org.eqaula.glue.model.BussinesEntity;
 import org.eqaula.glue.model.Group;
 import org.eqaula.glue.model.profile.Profile;
 import org.eqaula.glue.profile.ProfileService;
 import org.eqaula.glue.util.Dates;
 import org.eqaula.glue.web.ParamsBean;
 import org.jboss.seam.international.status.Messages;
 import org.jboss.seam.security.Authenticator;
 import org.jboss.seam.security.Credentials;
 import org.jboss.seam.security.Identity;
 import org.jboss.seam.security.external.openid.OpenIdAuthenticator;
 import org.jboss.seam.security.management.IdmAuthenticator;
 import org.jboss.seam.transaction.Transactional;
 import org.picketlink.idm.api.AttributesManager;
 import org.picketlink.idm.api.IdentitySession;
 import org.picketlink.idm.api.PersistenceManager;
 import org.picketlink.idm.api.User;
 import org.picketlink.idm.common.exception.IdentityException;
 import org.picketlink.idm.impl.api.PasswordCredential;
 
 /**
  *
  * @author jlgranda
  */
 @Named
 @ViewScoped
 public class ProfileHome extends BussinesEntityHome<Profile> implements Serializable {
 
     private static final long serialVersionUID = 7632987414391869389L;
     private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ProfileHome.class);
     @Inject
     @Web
     private EntityManager em;
     private Messages msg;
     @Inject
     private Identity identity;
     @Inject
     private Credentials credentials;
     @Inject
     private IdentitySession security;
     @Inject
     private OpenIdAuthenticator oidAuth;
     @Inject
     private IdmAuthenticator idmAuth;
     private String password;
     private String passwordConfirm;
     private String structureName;
     @Inject
     private ParamsBean params;
     @Inject
     private ProfileService ps;
 
     public Long getProfileId() {
         return (Long) getId();
     }
 
     public void setProfileId(Long profileId) {
         setId(profileId);
     }
 
     public String getStructureName() {
         return structureName;
     }
 
     public void setStructureName(String structureName) {
         this.structureName = structureName;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPasswordConfirm() {
         return passwordConfirm;
     }
 
     public void setPasswordConfirm(String passwordConfirm) {
         this.passwordConfirm = passwordConfirm;
     }
 
     @Override
     protected Profile createInstance() {
         Date now = Calendar.getInstance().getTime();
         Profile profile = new Profile();
         profile.setCreatedOn(now);
         profile.setLastUpdate(now);
         profile.setActivationTime(now);
         profile.setExpirationTime(Dates.addDays(now, 364));
         profile.setAuthor(null); //Establecer al usuario actual
         //profile.buildAttributes(bussinesEntityService);
         return profile;
     }
 
     @Produces
     @Named("profile")
     @Current
     @TransactionAttribute
     public Profile load() {
         if (isIdDefined()) {
             wire();
         } else {
             if (identity.isLoggedIn()){
                 setInstance(ps.getProfileByUsername(identity.getUser().getKey()));
             } else {
                 setInstance(new Profile());
             }
         }
         return getInstance();
     }
 
     @TransactionAttribute
     public void wire() {
         getInstance();
     }
 
     public boolean isWired() {
         return true;
     }
 
     public Profile getDefinedInstance() {
         return isIdDefined() ? getInstance() : null;
     }
 
     @PostConstruct
     public void init() {
         setEntityManager(em);
         bussinesEntityService.setEntityManager(em);
         ps.setEntityManager(em);
     }
 
     @Override
     public Class<Profile> getEntityClass() {
         return Profile.class;
     }
 
     @TransactionAttribute
     public String register() throws IdentityException {
         createUser();
 
         credentials.setUsername(getInstance().getUsername());
         credentials.setCredential(new PasswordCredential(getPassword()));
 
         oidAuth.setStatus(Authenticator.AuthenticationStatus.FAILURE);
         identity.setAuthenticatorClass(IdmAuthenticator.class);
 
         /*
          * Try twice to work around some state bug in Seam Security
          * TODO file issue in seam security
          */
         String result = identity.login();
         if (Identity.RESPONSE_LOGIN_EXCEPTION.equals(result)) {
             result = identity.login();
         }
 
         return result;
     }
 
     @TransactionAttribute
     private void createUser() throws IdentityException {
         // TODO validate username, email address, and user existence
         PersistenceManager identityManager = security.getPersistenceManager();
         User user = identityManager.createUser(getInstance().getUsername());
 
         AttributesManager attributesManager = security.getAttributesManager();
         attributesManager.updatePassword(user, getPassword());
         attributesManager.addAttribute(user, "email", getInstance().getEmail());
        
         em.flush();
 
         // TODO figure out a good pattern for this...
         //setInstance(createInstance());
         //getInstance().setEmail(email);
         getInstance().setPassword(getPassword());
         getInstance().getIdentityKeys().add(user.getKey());
         getInstance().setUsernameConfirmed(true);
         getInstance().setShowBootcamp(true);
         create(getInstance()); //
         setProfileId(getInstance().getId());
         wire();
         getInstance().setName(getInstance().getUsername()); //Para referencia
         getInstance().setType(bussinesEntityService.findBussinesEntityTypeByName(Profile.class.getName()));
         getInstance().buildAttributes(bussinesEntityService);
         save(getInstance()); //Actualizar estructura de datos
 
     }
 
     @TransactionAttribute
     public String saveAjax() {
         Date now = Calendar.getInstance().getTime();
         getInstance().setLastUpdate(now);
         /*for (BussinesEntityAttribute a : getInstance().getAttributes()) {
          if (a.getProperty().getType().equals("java.lang.String") && a.getValue() == null) {
          a.setValue(a.getName());
          a.setStringValue(a.getName());
          }
          }*/
         save(getInstance());
         return "/pages/profile/view?faces-redirect=true&profileId=" + getProfileId();
     }
 
     @TransactionAttribute
     public String saveProfile() {
         Date now = Calendar.getInstance().getTime();
         getInstance().setLastUpdate(now);
         if (getInstance().isPersistent()) {
             save(getInstance());
         } else {
             try {
                 register();
             } catch (IdentityException ex) {
                 Logger.getLogger(ProfileHome.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         if ("list".equalsIgnoreCase(getBackView())) {
             return "/pages/profile/list?typeName=org.eqaula.glue.model.Profile";
         } else {
             return "/pages/profile/view?faces-redirect=true&profileId=" + getProfileId();
         }
     }
 
     @TransactionAttribute
     public void displayBootcampAjax() {
         getInstance().setShowBootcamp(true);
         update();
     }
 
     @TransactionAttribute
     public void dismissBootcampAjax() {
         getInstance().setShowBootcamp(false);
         update();
     }
 
     public void commuteEdition() {
         setEditionEnabled(!isEditionEnabled());
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Edición activa " + isEditionEnabled(), null));
 
     }
 
     @Transactional
     public void addBussinesEntity(Group group) {
         Date now = Calendar.getInstance().getTime();
         String name = "Nuevo " + (group.getProperty() != null ? group.getProperty().getLabel() : "elemento") + " " + (group.findOtherMembers(this.getInstance()).size() + 1);
         BussinesEntity entity = new BussinesEntity();
         entity.setName(name);
         //TODO implementar generador de códigos para entidad de negocio
         entity.setCode((group.getProperty() != null ? group.getProperty().getLabel() : "elemento") + " " + (group.findOtherMembers(this.getInstance()).size() + 1));
         entity.setCreatedOn(now);
         entity.setLastUpdate(now);
         entity.setActivationTime(now);
         entity.setExpirationTime(Dates.addDays(now, 364));
         entity.setAuthor(null); //Establecer al usuario actual
         entity.buildAttributes(bussinesEntityService);
         //Set default values into dinamycs properties
         //TODO idear un mecanismo generico de inicialización de variables dinamicas
         //entity.getBussinessEntityAttribute("title").setValue(name);
 
         group.add(entity);
 
         setBussinesEntity(entity); //Establecer para edición
     }
 
     @Transactional
     public void saveBussinesEntity() {
 
         try {
             if (getBussinesEntity() == null) {
                 throw new NullPointerException("bussinessEntity is null");
             }
 
             if (getBussinesEntity().getName().equalsIgnoreCase("")) {
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No name", null));
             } else {
                 save(getBussinesEntity());
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se actualizó con exito " + bussinesEntity.getName(), ""));
             }
 
         } catch (Exception e) {
             System.out.println("saveBussinesEntity ERROR = " + e.getMessage());
             e.printStackTrace();
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
         }
     }
 }
