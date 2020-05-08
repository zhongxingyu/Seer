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
 package net.java.dev.cejug.classifieds.service.endpoint;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.interceptor.Interceptors;
 import javax.jws.WebService;
 import javax.xml.ws.WebServiceException;
 
 import net.java.dev.cejug.classifieds.business.interfaces.AdvertisementOperationsLocal;
 import net.java.dev.cejug.classifieds.business.interfaces.CategoryOperationsLocal;
 import net.java.dev.cejug.classifieds.business.interfaces.ClassifiedsBusinessLocal;
 import net.java.dev.cejug.classifieds.business.interfaces.ClassifiedsBusinessRemote;
 import net.java.dev.cejug.classifieds.business.interfaces.LoadAtomOperationLocal;
 import net.java.dev.cejug.classifieds.business.interfaces.LoadRssOperationLocal;
 import net.java.dev.cejug.classifieds.service.interceptor.TimerInterceptor;
 import net.java.dev.cejug_classifieds.metadata.business.Advertisement;
 import net.java.dev.cejug_classifieds.metadata.business.AdvertisementCollection;
 import net.java.dev.cejug_classifieds.metadata.business.AdvertisementCollectionFilter;
 import net.java.dev.cejug_classifieds.metadata.business.PublishingHeader;
 import net.java.dev.cejug_classifieds.metadata.business.SyndicationFilter;
 import net.java.dev.cejug_classifieds.metadata.common.BundleRequest;
 import net.java.dev.cejug_classifieds.metadata.common.CategoryCollection;
 import net.java.dev.cejug_classifieds.metadata.common.CreateCustomerParam;
 import net.java.dev.cejug_classifieds.metadata.common.CustomerCollection;
 import net.java.dev.cejug_classifieds.metadata.common.DeleteCustomerParam;
 import net.java.dev.cejug_classifieds.metadata.common.ReadCustomerBundleParam;
 import net.java.dev.cejug_classifieds.metadata.common.ServiceStatus;
 import net.java.dev.cejug_classifieds.metadata.common.UpdateCustomerParam;
 import net.java.dev.cejug_classifieds.rss.Rss;
 
 import org.w3._2005.atom.FeedType;
 
 /**
  * Business Service implementation of the interface defined in the
  * cejug-classifieds-business.
  * 
  * @author $Author: felipegaucho $
  * @version $Rev: 577 $ ($Date: 2008-09-03 18:57:21 +0200 (Wed, 03 Sep 2008) $)
  * @see net.java.dev.cejug_classifieds.business.CejugClassifiedsBusiness
  */
 @Interceptors(TimerInterceptor.class)
 @Stateless
 @WebService(endpointInterface = "net.java.dev.cejug_classifieds.business.CejugClassifiedsBusiness", serviceName = "CejugClassifiedsServiceBusiness", portName = "CejugClassifiedsBusiness", targetNamespace = "http://cejug-classifieds.dev.java.net/business")
 public class BusinessEndpointDecorator implements ClassifiedsBusinessLocal,
 		ClassifiedsBusinessRemote {
 	/**
 	 * Used by not yet implemented operations: {@value} .
 	 */
 	private static final String NOT_IMPLEMENTED = "operation not yet implemented";
 
 	@EJB
 	private transient LoadRssOperationLocal loadRssImpl;
 
 	@EJB
 	private transient LoadAtomOperationLocal loadAtomImpl;
 
 	@EJB
 	private transient AdvertisementOperationsLocal crudAdvertisement;
 
	@EJB
	private CategoryOperationsLocal crudCategory;

 	/**
 	 * @return an <a href=
 	 *         "http://en.wikipedia.org/wiki/Atom_(standard)#Example_of_an_Atom_1.0_Feed"
 	 *         >ATOM 1.0 document </a>
 	 * @param filter
 	 *            a set of constraint on the advertisement's search in the
 	 *            database.
 	 */
 	public FeedType loadAtomOperation(SyndicationFilter filter) {
 		return loadAtomImpl.loadAtomOperation(filter);
 
 	}
 
 	/**
 	 * @return an <a href= "http://en.wikipedia.org/wiki/RSS_(file_format)" >RSS
 	 *         2.0 document </a>
 	 * @param filter
 	 *            a set of constraint on the advertisement's search in the
 	 *            database.
 	 */
 	public Rss loadRssOperation(SyndicationFilter filter) {
 		return loadRssImpl.loadRssOperation(filter);
 	}
 
 	public Advertisement publishOperation(final Advertisement advertisement,
 			final PublishingHeader header) {
 		return crudAdvertisement.publishOperation(advertisement, header);
 	}
 
 	public AdvertisementCollection loadAdvertisementOperation(
 			final AdvertisementCollectionFilter filter) {
 		return crudAdvertisement.loadAdvertisementOperation(filter);
 	}
 
 	public CategoryCollection readCategoryBundleOperation(
 			final BundleRequest bundleRequest) {
 		CategoryCollection collection = new CategoryCollection();
		collection.getAdvertisementCategory().addAll(
				crudCategory.readBundleOperation(bundleRequest));
 		return collection;
 	}
 
 	public ServiceStatus reportSpamOperation(final long advId) {
 		// TODO
 		throw new WebServiceException(NOT_IMPLEMENTED);
 	}
 
 	public ServiceStatus createCustomerOperation(
 			final CreateCustomerParam newCustomer) {
 		// TODO
 		throw new WebServiceException(NOT_IMPLEMENTED);
 	}
 
 	public ServiceStatus deleteCustomerOperation(
 			final DeleteCustomerParam obsoleteCustomer) {
 		// TODO
 		throw new WebServiceException(NOT_IMPLEMENTED);
 	}
 
 	public CustomerCollection readCustomerBundleOperation(
 			final ReadCustomerBundleParam getCustomer) {
 		// TODO
 		throw new WebServiceException(NOT_IMPLEMENTED);
 	}
 
 	public ServiceStatus updateCustomerOperation(
 			final UpdateCustomerParam partialCustomer) {
 		// TODO
 		throw new WebServiceException(NOT_IMPLEMENTED);
 	}
 }
