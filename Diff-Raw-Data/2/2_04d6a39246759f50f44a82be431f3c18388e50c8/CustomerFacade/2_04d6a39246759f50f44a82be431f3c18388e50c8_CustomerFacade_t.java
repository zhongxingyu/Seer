 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.server.ejb3.entity.facade;
 
 import java.util.ArrayList;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 import net.java.dev.cejug.classifieds.server.ejb3.entity.CustomerEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.DomainEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.QuotaEntity;
 
 /**
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Stateless
 public class CustomerFacade extends EntityFacade<CustomerEntity> implements
 		CustomerFacadeLocal {
 
 	@EJB
 	DomainFacadeLocal domainFacade;
 
 	@Override
 	public CustomerEntity updateCustomer(CustomerEntity entity)
 			throws Exception {
 
 		return update(entity);
 	}
 
 	@Override
 	public CustomerEntity findOrCreate(int domainId, String login)
 			throws Exception {
 
 		Query query = manager
 				.createNamedQuery("selectCustomerByLoginAndDomain");
		query.setParameter("d", Integer.valueOf(domainId));
 		query.setParameter("l", login);
 
 		try {
 			return (CustomerEntity) query.getSingleResult();
 		} catch (NoResultException error) {
 			/*
 			 * TODO: if the customer does not exist, insert a new customer
 			 * automatically. It should also add the default "welcome quotas",
 			 * to be defined by an external rule from database. The below code
 			 * is just a test.
 			 */
 
 			// loading domain
 			DomainEntity domain = domainFacade.get(domainId);
 
 			// insert a new customer
 			CustomerEntity customer = new CustomerEntity();
 			customer.setDomain(domain);
 			customer.setLogin(login);
 			customer.setQuotas(new ArrayList<QuotaEntity>());
 			manager.persist(customer);
 			return customer;
 		}
 	}
 }
