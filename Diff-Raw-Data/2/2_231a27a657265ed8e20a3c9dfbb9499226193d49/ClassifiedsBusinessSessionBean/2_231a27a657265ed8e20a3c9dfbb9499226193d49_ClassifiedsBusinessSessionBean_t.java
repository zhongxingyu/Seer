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
 package net.java.dev.cejug.classifieds.server.ejb3.bean;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.interceptor.Interceptors;
 import javax.jws.WebService;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.ws.WebServiceException;
 
 import net.java.dev.cejug.classifieds.server.ejb3.entity.AdvertisementEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.AdvertisementTypeEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.CategoryEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.CustomerEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.AdvertisementFacadeLocal;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.CategoryFacadeLocal;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.CustomerFacadeLocal;
 import net.java.dev.cejug.classifieds.server.ejb3.interceptor.TimerInterceptor;
 import net.java.dev.cejug.classifieds.server.generated.contract.Advertisement;
 import net.java.dev.cejug.classifieds.server.generated.contract.AdvertisementCategory;
 import net.java.dev.cejug.classifieds.server.generated.contract.AdvertisementCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.AdvertisementCollectionFilter;
 import net.java.dev.cejug.classifieds.server.generated.contract.AtomCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.AtomFilterCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.BundleRequest;
 import net.java.dev.cejug.classifieds.server.generated.contract.CategoryCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.Channel;
 import net.java.dev.cejug.classifieds.server.generated.contract.Customer;
 import net.java.dev.cejug.classifieds.server.generated.contract.FeedType;
 import net.java.dev.cejug.classifieds.server.generated.contract.Item;
 import net.java.dev.cejug.classifieds.server.generated.contract.PublishingHeader;
 import net.java.dev.cejug.classifieds.server.generated.contract.RssCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.RssFilterCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.ServiceStatus;
 import net.java.dev.cejug.classifieds.server.generated.contract.SpamReport;
 import net.java.dev.cejug.classifieds.server.generated.contract.TextType;
 
 /**
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Interceptors(TimerInterceptor.class)
 @Stateless
 @WebService(endpointInterface = "net.java.dev.cejug.classifieds.server.generated.contract.CejugClassifiedsBusiness", serviceName = "CejugClassifiedsServiceBusiness", portName = "CejugClassifiedsBusiness", targetNamespace = "http://cejug-classifieds.dev.java.net/business")
 public class ClassifiedsBusinessSessionBean implements
 		ClassifiedsBusinessRemote {
 
 	@EJB
 	private AdvertisementFacadeLocal advertisementFacade;
 	@EJB
 	private CustomerFacadeLocal customerFacade;
 	@EJB
 	private CategoryFacadeLocal categoryFacade;
 	/**
 	 * the global log manager, used to allow third party services to override
 	 * the defult logger.
 	 */
 	private static Logger logger = Logger.getLogger(
 			ClassifiedsBusinessSessionBean.class.getName(), "i18n/log");
 
 	private final DatatypeFactory factory;
 
 	public ClassifiedsBusinessSessionBean() {
 
 		try {
 			factory = DatatypeFactory.newInstance();
 		} catch (DatatypeConfigurationException e) {
 			// TODO: log
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 
 	@Override
 	public AtomCollection loadAtomOperation(AtomFilterCollection filter) {
 
 		try {
 			// TODO: converter filter in a map of parameters...
 			List<AdvertisementEntity> result = advertisementFacade
 					.readAll(AdvertisementEntity.class);
 
 			List<FeedType> atomCollection = new ArrayList<FeedType>();
 			for (AdvertisementEntity adv : result) {
 				FeedType feed = new FeedType();
 				TextType title = new TextType();
 				title.setType(adv.getTitle());
				//feed.getAuthorOrCategoryOrContributor().add(title);
 
 				Item item = new Item();
 				item.setAuthor(adv.getCustomer().getLogin());
 				item.setAuthor("INCOMPLETE DATA SET");
 				item.setDescription(adv.getSummary());
 				item.setPubDate(adv.getStart());
 				atomCollection.add(feed);
 			}
 
 			AtomCollection atoms = new AtomCollection();
 			// atoms.getAtomCollection().add(atomCollection);
 			return atoms;
 		} catch (Exception e) {
 			// TODO: log
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 
 	@Override
 	public RssCollection loadRssOperation(RssFilterCollection filter) {
 
 		try {
 			// TODO: converter filter in a map of parameters...
 			List<AdvertisementEntity> result = advertisementFacade
 					.readAll(AdvertisementEntity.class);
 
 			Channel channel = new Channel();
 			for (AdvertisementEntity adv : result) {
 				Item item = new Item();
 				// item.setAuthor(adv.getVoucher().getCustomer().getLogin());
 				item.setAuthor("INCOMPLETE DATA SET");
 				item.setTitle(adv.getTitle());
 				item.setDescription(adv.getSummary());
 				// item.setPubDate(adv.getPublishingPeriod().iterator().next().
 				// getDay());
 				channel.getItem().add(item);
 			}
 			RssCollection col = new RssCollection();
 			col.getRssCollection().add(channel);
 			return col;
 		} catch (Exception e) {
 			// TODO: log
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 
 	@Override
 	public ServiceStatus reportSpamOperation(SpamReport spam) {
 		ServiceStatus status = new ServiceStatus();
 		status.setDescription("OK");
 		status.setStatusCode(200);
 		return status;
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public ServiceStatus publishOperation(Advertisement advertisement,
 			PublishingHeader header) {
 
 		// TODO: to implement the real code.
 		try {
 			/*
 			 * // loading customer Map<String, String> params = new HashMap<String,
 			 * String>(); params.clear(); params.put("d",
 			 * header.getCustomerDomain()); params.put("l",
 			 * header.getCustomerLogin());
 			 */
 			CustomerEntity customer = customerFacade.findOrCreate(header
 					.getCustomerDomainId(), header.getCustomerLogin());
 
 			// validating advertisement PIN
 			AdvertisementEntity entity = new AdvertisementEntity();
 			// entity.setKeywords(advertisement.getKeywords()); // TODO
 			entity.setText(advertisement.getFullText());
 
 			entity.setCustomer(customer);
 
 			// TODO: load the AdvertisementTypeEntity
 			AdvertisementTypeEntity type = new AdvertisementTypeEntity();
 			type.setDescription("oo");
 			type.setMaxAttachmentSize(300L);
 			type.setName("courtesy");
 			type.setTextLength(250L);
 			entity.setType(type);
 
 			entity.setSummary(advertisement.getShortDescription());
 			entity.setTitle(advertisement.getHeadline());
 
 			CategoryEntity category = categoryFacade.read(CategoryEntity.class,
 					advertisement.getCategoryId());
 			entity.setCategory(category);
 
 			Calendar start = Calendar.getInstance();
 			Calendar finish = Calendar.getInstance();
 			finish.add(Calendar.HOUR, 3);
 			entity.setStart(start);
 			entity.setFinish(finish);
 			advertisementFacade.create(entity);
 
 			ServiceStatus status = new ServiceStatus();
 			status.setDescription("OK");
 			status.setStatusCode(202);
 
 			status
 					.setTimestamp(factory
 							.newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar
 									.getInstance()));
 
 			return status;
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 
 	}
 
 	@Override
 	public AdvertisementCollection loadAdvertisementOperation(
 			AdvertisementCollectionFilter filter) {
 
 		// TODO: load advertisements from timetable.... filtering with periods,
 		// etc..
 
 		try {
 			AdvertisementCollection collection = new AdvertisementCollection();
 			List<AdvertisementEntity> entities = advertisementFacade
 					.readAll(AdvertisementEntity.class);
 			for (AdvertisementEntity entity : entities) {
 				Advertisement adv = new Advertisement();
 				adv.setId(entity.getId());
 				Customer c = new Customer();
 				c.setDomain("www.cejug.org");
 				c.setLogin("test");
 				adv.setAdvertiser(c);
 				adv.setFullText(entity.getText());
 				adv
 						.setKeywords(Arrays.toString(entity.getKeywords()
 								.toArray()));
 				adv.setShortDescription(entity.getSummary());
 				adv.setHeadline(entity.getTitle());
 				collection.getAdvertisement().add(adv);
 			}
 			return collection;
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 
 	@Override
 	public CategoryCollection readCategoryBundleOperation(
 			BundleRequest bundleRequest) {
 		CategoryCollection categoryCollection = new CategoryCollection();
 		try {
 			List<CategoryEntity> categories = categoryFacade
 					.readAll(CategoryEntity.class);
 			if (categories != null) {
 				for (CategoryEntity category : categories) {
 					AdvertisementCategory cat = new AdvertisementCategory();
 					cat.setDescription(category.getDescripton());
 					cat.setName(category.getName());
 					int available = categoryFacade
 							.countAdvertisements(category);
 					cat.setAvailable(available);
 					categoryCollection.getAdvertisementCategory().add(cat);
 				}
 			}
 		} catch (Exception e) {
 			// TODO log.......
 			e.printStackTrace();
 			// throw new WebServiceException(e);
 		}
 		return categoryCollection;
 	}
 }
