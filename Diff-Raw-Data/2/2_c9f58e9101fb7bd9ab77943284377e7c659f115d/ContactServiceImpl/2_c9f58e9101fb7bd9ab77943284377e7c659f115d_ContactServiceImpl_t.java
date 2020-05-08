 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.application.contact.impl;
 
 import java.util.List;
 
 import javax.mail.internet.MimeMessage;
 
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import fr.peralta.mycellar.application.contact.ContactService;
 import fr.peralta.mycellar.application.shared.AbstractEntityService;
 import fr.peralta.mycellar.domain.contact.Contact;
 import fr.peralta.mycellar.domain.contact.repository.ContactOrder;
 import fr.peralta.mycellar.domain.contact.repository.ContactOrderEnum;
 import fr.peralta.mycellar.domain.contact.repository.ContactRepository;
 import fr.peralta.mycellar.domain.shared.exception.BusinessError;
 import fr.peralta.mycellar.domain.shared.exception.BusinessException;
 import fr.peralta.mycellar.domain.wine.Producer;
 
 /**
  * @author speralta
  */
 @Service
 public class ContactServiceImpl extends
         AbstractEntityService<Contact, ContactOrderEnum, ContactOrder, ContactRepository> implements
         ContactService {
 
     private ContactRepository contactRepository;
 
     private JavaMailSender javaMailSender;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long countLastContacts() {
         return contactRepository.countLastContacts();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Contact> getLastContacts(ContactOrder orders, int first, int count) {
         return contactRepository.getLastContacts(orders, first, count);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Contact> getAllForProducer(Producer producer, ContactOrder orders, int first,
             int count) {
         return contactRepository.getAllForProducer(producer, orders, first, count);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long countForProducer(Producer producer) {
         return contactRepository.countForProducer(producer);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Scheduled(cron = "0 0 0 * * *")
     public void sendReminders() {
         final StringBuilder content = new StringBuilder();
         List<Contact> contacts = contactRepository.getAllToContact();
         for (Contact contact : contacts) {
             content.append("Domaine ").append(contact.getProducer().getName())
                     .append(" à recontacter le ").append(contact.getNext()).append("\r\n");
             content.append("Dernier contact le ").append(contact.getCurrent()).append(" :")
                     .append("\r\n").append(contact.getText()).append("\r\n");
         }
         MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {
             @Override
             public void prepare(MimeMessage mimeMessage) throws Exception {
                 MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                helper.setTo("stephanie@cave-et-terroirs.fr");
                 helper.setFrom("contact@mycellar.peralta.fr");
                 helper.setSubject("Contacts à recontacter");
                 helper.setText(content.toString());
             }
         };
         try {
             javaMailSender.send(mimeMessagePreparator);
         } catch (Exception e) {
             throw new RuntimeException("Cannot send email.", e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate(Contact entity) throws BusinessException {
         if ((entity.getId() == null) && (find(entity.getProducer(), entity.getCurrent()) != null)) {
             throw new BusinessException(BusinessError.CONTACT_00001);
         }
     }
 
     /**
      * @param producer
      * @param current
      * @return
      */
     @Override
     public Contact find(Producer producer, LocalDate current) {
         return contactRepository.find(producer, current);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected ContactRepository getRepository() {
         return contactRepository;
     }
 
     /**
      * @param contactRepository
      *            the contactRepository to set
      */
     @Autowired
     public void setContactRepository(ContactRepository contactRepository) {
         this.contactRepository = contactRepository;
     }
 
     /**
      * @param javaMailSender
      *            the javaMailSender to set
      */
     @Autowired
     public void setJavaMailSender(JavaMailSender javaMailSender) {
         this.javaMailSender = javaMailSender;
     }
 
 }
