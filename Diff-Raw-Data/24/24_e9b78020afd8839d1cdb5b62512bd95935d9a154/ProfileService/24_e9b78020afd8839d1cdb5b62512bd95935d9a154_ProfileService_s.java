 /**
  * This file is part of Glue: Adhesive BRMS
  * 
 * Copyright (c)2012 José Luis Granda <jlgranda@eqaula.org> (Eqaula Tecnologías
  * Cia Ltda) Copyright (c)2012 Eqaula Tecnologías Cia Ltda (http://eqaula.org)
  * 
 * If you are developing and distributing open source applications under the GNU
  * General Public License (GPL), then you are free to re-distribute Glue under
  * the terms of the GPL, as follows:
  * 
 * GLue is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
 * Glue is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
 * You should have received a copy of the GNU General Public License along with
  * Glue. If not, see <http://www.gnu.org/licenses/>.
  * 
 * For individuals or entities who wish to use Glue privately, or internally,
  * the following terms do not apply:
  * 
 * For OEMs, ISVs, and VARs who wish to distribute Glue with their products, or
  * host their product online, Eqaula provides flexible OEM commercial licenses.
  * 
 * Optionally, Customers may choose a Commercial License. For additional
  * details, contact an Eqaula representative (sales@eqaula.org)
  */
 package org.eqaula.glue.profile;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Random;
 
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import org.eqaula.glue.model.BussinesEntity;
 import org.eqaula.glue.util.PersistenceUtil;
 import org.eqaula.glue.model.profile.Profile;
 import org.eqaula.glue.model.profile.Profile_;
 import org.eqaula.glue.service.BussinesEntityService;
 
 import org.eqaula.glue.util.Strings;
 
 /**
  *
  * @author jlgranda
  */
 public class ProfileService extends PersistenceUtil<Profile> implements Serializable {
 
     private static final long serialVersionUID = -4022772083704382039L;
     private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ProfileService.class);
     
     @Inject
     private BussinesEntityService bussinesEntityService;
     
     public ProfileService() {
         super(Profile.class);
     }
 
     @Override
     public void setEntityManager(EntityManager em) {
         this.em = em;
         bussinesEntityService.setEntityManager(em);
     }
 
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void create(final Profile profile) {
         profile.setShowBootcamp(true);
         super.create(profile);
     }
 
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void save(final Profile user) {
         super.save(user);
         em.flush();
     }
 
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public String getRandomUsername(String seed) {
         String username = Strings.canonicalize(seed);
         while (!isUsernameAvailable(username)) {
             username += new Random(System.currentTimeMillis()).nextInt() % 100;
         }
         return username;
     }
 
     public long getProfileCount() {
         return count(Profile.class);
     }
 
     public List<Profile> getProfiles(final int limit, final int offset) {
         return findAll(Profile.class);
     }
 
     public Profile getProfileByUsername(final String username) throws NoResultException {
         TypedQuery<Profile> query = em.createQuery("SELECT p FROM Profile p WHERE p.username = :username", Profile.class);
         query.setParameter("username", username);
 
         Profile result = query.getSingleResult();
         return result;
     }
 
     public Profile getProfileByEmail(final String email) throws NoResultException {
         TypedQuery<Profile> query = em.createQuery("SELECT p FROM Profile p WHERE p.email = :email", Profile.class);
         query.setParameter("email", email);
         Profile result = query.getSingleResult();
         return result;
     }
 
     public boolean hasProfileByIdentityKey(final String key) throws NoResultException {
         try {
             getProfileByIdentityKey(key);
             return true;
         } catch (NoResultException e) {
             return false;
         }
     }
 
     public Profile getProfileByIdentityKey(final String key) throws NoResultException {
         TypedQuery<Profile> query = em.createQuery(
                 "SELECT p FROM Profile p JOIN p.identityKeys k WHERE k = :identityKey", Profile.class);
         query.setParameter("identityKey", key);
 
         Profile result = query.getResultList().isEmpty() ? null : query.getResultList().get(0);
         return result;
     }
     
     public BussinesEntity getBussinesEntityForCode(final String code) throws NoResultException {
         TypedQuery<BussinesEntity> query = em.createQuery(
                 "SELECT b FROM BussinesEntity b WHERE b. = :identityKey", BussinesEntity.class);
         query.setParameter("identityKey", code);
 
         BussinesEntity result = query.getResultList().isEmpty() ? null : query.getResultList().get(0);
         return result;
     }
     
     public Profile getProfileById(final Long id) {
         return (Profile) findById(Profile.class, id);
     }
 
     public boolean isUsernameAvailable(String username) {
         try {
             getProfileByUsername(username);
             return false;
         } catch (NoResultException e) {
             return true;
         }
     }
     
     public boolean isDniAviable(String code){
         try {            
            BussinesEntity b = bussinesEntityService.findBussinesEntityForCode(code);            
            return false;
         } catch (NoResultException e) {
             return true;
         }
     }
     
     public boolean isEmailAddressAvailable(String email) {
         try {
             getProfileByEmail(email);
             return false;
         } catch (NoResultException e) {
             return true;
         }
     }
 
     public Profile findByName(final String name) {
 
         log.info("find Profile with name " + name);
 
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<Profile> query = builder.createQuery(Profile.class);
 
         Root<Profile> bussinesEntityType = query.from(Profile.class);
 
         query.where(builder.equal(bussinesEntityType.get(Profile_.name), name));
 
         return getSingleResult(query);
     }
 }
