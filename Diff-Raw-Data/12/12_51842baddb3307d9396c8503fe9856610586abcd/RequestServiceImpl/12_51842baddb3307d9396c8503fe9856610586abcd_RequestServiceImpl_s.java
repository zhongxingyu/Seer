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
 
 import org.accesointeligente.client.services.RequestService;
 import org.accesointeligente.model.*;
 import org.accesointeligente.server.*;
 import org.accesointeligente.shared.*;
 
 import net.sf.gilead.core.PersistentBeanManager;
 import net.sf.gilead.gwt.PersistentRemoteService;
 
 import org.hibernate.*;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.List;
 
 public class RequestServiceImpl extends PersistentRemoteService implements RequestService {
 	private static final long serialVersionUID = -8965250779021980788L;
 	private PersistentBeanManager persistentBeanManager;
 
 	public RequestServiceImpl() {
 		persistentBeanManager = HibernateUtil.getPersistentBeanManager();
 		setBeanManager(persistentBeanManager);
 	}
 
 	@Override
 	public Request saveRequest(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.saveOrUpdate(request);
 			hibernate.getTransaction().commit();
 			return request;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteRequest(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.delete(request);
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<RequestCategory> getCategories() throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(RequestCategory.class);
 			criteria.addOrder(Order.asc("id"));
 			List<RequestCategory> categories = (List<RequestCategory>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return categories;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request getRequest(Integer requestId) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("categories", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.setFetchMode("responses", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("id", requestId));
 			Request request = (Request) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request getRequest(String remoteIdentifier) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.eq("remoteIdentifier", remoteIdentifier));
 			Request request = (Request) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.asc("confirmationDate"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.setFetchMode("responses", FetchMode.JOIN);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			params = (RequestSearchParams) persistentBeanManager.merge(params);
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.asc("confirmationDate"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.setFetchMode("responses", FetchMode.JOIN);
 			if(params != null) {
 				SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 			}
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserFavoriteRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 			String query = "select distinct f.request from UserFavoriteRequest f join fetch f.request.institution join fetch f.request.favorites left join fetch f.request.responses where f.user = :user";
 			Query hQuery = hibernate.createQuery(query);
 			hQuery.setParameter("user", user);
 			List<Request> requests = hQuery.list();
 			hibernate.getTransaction().commit();
 			return (List<Request>) persistentBeanManager.clone(requests);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserFavoriteRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 			String query = "select distinct f.request from UserFavoriteRequest f join fetch f.request.institution join fetch f.request.favorites left join fetch f.request.responses where f.user = :user";
 			Query hQuery;
 			if(params != null) {
 				query += SearchParamParseUtil.queryAddSearchParams(params);
 				hQuery = hibernate.createQuery(query);
 				hQuery.setParameter("user", user);
 				if (query.contains("minDate")) {
 					hQuery.setParameter("minDate", params.getMinDate());
 				}
 				if (query.contains("maxDate")) {
 					hQuery.setParameter("maxDate", params.getMaxDate());
 				}
 			} else {
 				hQuery = hibernate.createQuery(query);
 				hQuery.setParameter("user", user);
 			}
 			List<Request> requests = hQuery.list();
 			hibernate.getTransaction().commit();
 			return (List<Request>) persistentBeanManager.clone(requests);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserDraftList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.eq("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.asc("creationDate"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.setFetchMode("responses", FetchMode.JOIN);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.asc("confirmationDate"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.setFetchMode("responses", FetchMode.JOIN);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.asc("confirmationDate"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.setFetchMode("responses", FetchMode.JOIN);
 			if(params != null) {
 				SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 			}
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Attachment> getResponseAttachmentList(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Attachment.class);
 			criteria.add(Restrictions.eq("response", response));
 			criteria.addOrder(Order.asc("name"));
 			List<Attachment> attachments = (List<Attachment>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return attachments;
 		} catch (Exception ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserFavoriteRequest getFavoriteRequest(Request request, User user) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(UserFavoriteRequest.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.eq("request", request));
 			UserFavoriteRequest favorite = (UserFavoriteRequest) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			if (favorite != null) {
 				return (UserFavoriteRequest) persistentBeanManager.clone(favorite);
 			} else {
 				return null;
 			}
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserFavoriteRequest createFavoriteRequest(Request request, User user) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			UserFavoriteRequest favorite = new UserFavoriteRequest();
 			favorite.setRequest(request);
 			favorite.setUser(user);
 			hibernate.save(favorite);
 			hibernate.getTransaction().commit();
 			return favorite;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteFavoriteRequest(UserFavoriteRequest favorite) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			favorite = (UserFavoriteRequest) persistentBeanManager.merge(favorite);
 			hibernate.delete(favorite);
 			hibernate.getTransaction().commit();
 			return;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<RequestComment> getRequestComments(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(RequestComment.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("request", request));
 			criteria.addOrder(Order.desc("date"));
 			List<RequestComment> comments = (List<RequestComment>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return comments;
 		} catch (Exception ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public RequestComment createRequestComment(RequestComment comment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			comment = (RequestComment) persistentBeanManager.merge(comment);
 			hibernate.save(comment);
 			hibernate.getTransaction().commit();
 			return comment;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteRequestComment(RequestComment comment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			comment = (RequestComment) persistentBeanManager.merge(comment);
 			hibernate.delete(comment);
 			hibernate.getTransaction().commit();
 			return;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Response saveResponse(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			response = (Response) persistentBeanManager.merge(response);
 			hibernate.save(response);
 			hibernate.getTransaction().commit();
 			return response;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteResponse(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.delete(response);
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Attachment saveAttachment(Attachment attachment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			attachment = (Attachment) persistentBeanManager.merge(attachment);
 			hibernate.saveOrUpdate(attachment);
 			hibernate.getTransaction().commit();
 			return attachment;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 
 	@Override
 	public void deleteAttachment(Attachment attachment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.delete(attachment);
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserRequestQualification saveUserRequestQualification(UserRequestQualification qualification) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			qualification = (UserRequestQualification) persistentBeanManager.merge(qualification);
 			hibernate.saveOrUpdate(qualification);
 			hibernate.getTransaction().commit();
 			qualification.setRequest(updateRequestQualification(qualification.getRequest()));
 			return (UserRequestQualification) persistentBeanManager.clone(qualification);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request updateRequestQualification(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			request = (Request) persistentBeanManager.merge(request);
 			Double averageQualification = (Double) hibernate.createQuery("select avg(qualification) from UserRequestQualification where Request = :request").setParameter("request", request).uniqueResult();
 			request.setQualification(averageQualification);
 			hibernate.update(request);
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserResponse saveUserResponse(UserResponse userResponse) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			userResponse = (UserResponse) persistentBeanManager.merge(userResponse);
 			hibernate.saveOrUpdate(userResponse);
 
 			Response response = userResponse.getResponse();
 			Request request = response.getRequest();
 			User user = request.getUser();
 			hibernate.getTransaction().commit();
 
 			Emailer emailer = new Emailer();
 			emailer.setRecipient(user.getEmail());
 			emailer.setSubject(String.format(ApplicationProperties.getProperty("email.user.response.subject"), request.getRemoteIdentifier()));
 			emailer.setBody(String.format(ApplicationProperties.getProperty("email.user.response.body"),  userResponse.getInformation()) + ApplicationProperties.getProperty("email.signature"));
 			emailer.connectAndSend();
 
 			return (UserResponse) persistentBeanManager.clone(userResponse);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserResponse getUserResponse(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 		try {
 			response = (Response) persistentBeanManager.merge(response);
 			Criteria criteria = hibernate.createCriteria(UserResponse.class);
 			criteria.add(Restrictions.eq("response", response));
 			UserResponse userResponse = (UserResponse) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 
 			return (UserResponse) persistentBeanManager.clone(userResponse);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getBestVotedRequests() throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.desc("qualification"));
 			criteria.setFirstResult(0);
 			criteria.setMaxResults(5);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 }
