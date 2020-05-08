 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server.services;
 
 import org.accesointeligente.client.services.ContactService;
 import org.accesointeligente.model.Contact;
 import org.accesointeligente.server.ApplicationProperties;
 import org.accesointeligente.server.Emailer;
 import org.accesointeligente.server.HibernateUtil;
 import org.accesointeligente.shared.Gender;
 import org.accesointeligente.shared.ServiceException;
 
 import net.sf.gilead.core.PersistentBeanManager;
 import net.sf.gilead.gwt.PersistentRemoteService;
 
 import org.hibernate.Session;
 
 public class ContactServiceImpl extends PersistentRemoteService implements ContactService {
 	private PersistentBeanManager persistentBeanManager;
 
 	public ContactServiceImpl() {
 		persistentBeanManager = HibernateUtil.getPersistentBeanManager();
 		setBeanManager(persistentBeanManager);
 	}
 
 	@Override
 	public Contact saveContact(Contact contact) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.saveOrUpdate(contact);
 			hibernate.getTransaction().commit();
 
 			Emailer emailer = new Emailer();
 			emailer.setRecipient(contact.getEmail());
 			emailer.setSubject(String.format(ApplicationProperties.getProperty("email.contact.subject"), contact.getSubject()));
			emailer.setBody(String.format(ApplicationProperties.getProperty("email.contact.body"), "Sr(a).", contact.getName(), contact.getMessage()) + ApplicationProperties.getProperty("email.signature"));
 			emailer.connectAndSend();
 
 			return contact;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 }
