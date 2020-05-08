 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.repository.impl.messaging;
 
 import org.patientview.patientview.model.Message;
 import org.patientview.patientview.model.Message_;
 import org.patientview.repository.AbstractHibernateDAO;
 import org.patientview.repository.messaging.MessageDao;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Transactional(propagation = Propagation.MANDATORY)
 @Repository(value = "messageDao")
 public class MessageDaoImpl extends AbstractHibernateDAO<Message> implements MessageDao {
 
     @Override
     public Message get(Long id) {
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Message> criteria = builder.createQuery(Message.class);
 
         Root<Message> root = criteria.from(Message.class);
 
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(root.get(Message_.deleted), false));
         wherePredicates.add(builder.equal(root.get(Message_.id), id));
 
         buildWhereClause(criteria, wherePredicates);
 
         try {
             return getEntityManager().createQuery(criteria).getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     @Override
     public List<Message> getMessages(Long conversationId) {
         // only want to return frameworks if we have a user filter
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Message> criteria = builder.createQuery(Message.class);
 
         Root<Message> root = criteria.from(Message.class);
 
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(root.get(Message_.deleted), false));
         wherePredicates.add(builder.equal(root.get(Message_.conversation), conversationId));
 
         buildWhereClause(criteria, wherePredicates);
 
         criteria.orderBy(builder.asc(root.get(Message_.date)));
 
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List<Message> getUnreadMessages(Long recipientId, Long conversationId) {
         // only want to return frameworks if we have a user filter
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Message> criteria = builder.createQuery(Message.class);
 
         Root<Message> root = criteria.from(Message.class);
 
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(root.get(Message_.deleted), false));
         wherePredicates.add(builder.equal(root.get(Message_.conversation), conversationId));
         wherePredicates.add(builder.equal(root.get(Message_.recipient), recipientId));
         wherePredicates.add(builder.equal(root.get(Message_.hasRead), false));
 
         buildWhereClause(criteria, wherePredicates);
 
         criteria.orderBy(builder.asc(root.get(Message_.date)));
 
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public Long getNumberOfUnreadMessages(Long recipientId, Long conversationId) {
         // only want to return frameworks if we have a user filter
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
 
         Root<Message> root = criteria.from(Message.class);
 
         criteria.select(builder.count(root));
 
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(root.get(Message_.deleted), false));
         wherePredicates.add(builder.equal(root.get(Message_.conversation), conversationId));
         wherePredicates.add(builder.equal(root.get(Message_.recipient), recipientId));
         wherePredicates.add(builder.equal(root.get(Message_.hasRead), false));
 
         buildWhereClause(criteria, wherePredicates);
 
        //criteria.orderBy(builder.asc(root.get(Message_.date)));
 
         return getEntityManager().createQuery(criteria).getSingleResult();
     }
 
     @Override
     public Message getLatestMessage(Long conversationId) {
         // only want to return frameworks if we have a user filter
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Message> criteria = builder.createQuery(Message.class);
 
         Root<Message> root = criteria.from(Message.class);
 
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(root.get(Message_.deleted), false));
         wherePredicates.add(builder.equal(root.get(Message_.conversation), conversationId));
 
         buildWhereClause(criteria, wherePredicates);
 
         criteria.orderBy(builder.desc(root.get(Message_.date)));
 
         TypedQuery<Message> query = getEntityManager().createQuery(criteria);
         query.setFirstResult(0);
         query.setMaxResults(1);
 
         try {
             return query.getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     @Override
     public void save(Message message) {
         if (!message.hasValidId()) {
             message.setDate(new Date());
         }
 
         super.save(message);
     }
 
     @Override
     public void delete(Message message) {
         message.setDeleted(true);
         save(message);
     }
 }
